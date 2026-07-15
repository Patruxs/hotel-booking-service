package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.response.dashboard.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.response.dashboard.LatestReviewResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingResponse;
import org.example.hotelbookingservice.dto.response.dashboard.RevenuePointResponse;
import org.example.hotelbookingservice.services.IDashboardService;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DashboardServiceImpl extends Milestone6ServiceSupport implements IDashboardService {
    public DashboardServiceImpl(NamedParameterJdbcTemplate jdbcTemplate, IFileStorageService fileStorageService, UploadProperties uploadProperties) {
        super(jdbcTemplate, fileStorageService, uploadProperties);
    }

    @Override
    public DashboardStatsResponse dashboardStats(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        boolean admin = isAdmin(user);
        MapSqlParameterSource params = scope.params();
        String totalUsersSql = admin ? "(select count(*) from accounts)" : "0";
        String activeHotelsSql = admin ? "(select count(*) from hotels h where h.status = 'ACTIVE' and h.deleted_at is null)" : "0";
        String sql = """
                select
                    %s total_users,
                    (select count(*) from bookings b %s
                    ) total_bookings,
                    (select coalesce(sum(b.total_amount), 0)
                     from bookings b
                     where %s
                       and exists (
                           select 1
                           from payments p
                           where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                       )) revenue,
                    %s active_hotels
                """.formatted(totalUsersSql, scope.where("b"), scope.condition("b"), activeHotelsSql);
        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new DashboardStatsResponse(
                rs.getLong("total_users"),
                rs.getLong("total_bookings"),
                rs.getBigDecimal("revenue"),
                rs.getLong("active_hotels")
        ));
    }

    @Override
    public List<RevenuePointResponse> revenueChart(UUID hotelId, String groupBy, Integer year, LocalDate from, LocalDate to, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        String normalizedGroup = Set.of("day", "week", "month").contains(String.valueOf(groupBy)) ? groupBy : "month";
        LocalDate start = from == null ? LocalDate.of(year == null ? LocalDate.now().getYear() : year, 1, 1) : from;
        LocalDate end = to == null ? start.plusYears(1).minusDays(1) : to;
        MapSqlParameterSource params = scope.params()
                .addValue("from", start)
                .addValue("to", end);
        return jdbcTemplate.query("""
                select date_trunc(:groupBy, b.created_at) period, coalesce(sum(b.total_amount), 0) revenue
                from bookings b
                where """ + scope.condition("b") + """
                  and b.created_at::date between :from and :to
                  and exists (
                      select 1
                      from payments p
                      where p.booking_id = b.id and p.status in ('SUCCEEDED', 'REFUNDED', 'LATE_SUCCEEDED')
                  )
                group by period
                order by period
                """, params.addValue("groupBy", normalizedGroup), (rs, rowNum) -> {
            Instant period = rs.getTimestamp("period").toInstant();
            String label = formatPeriod(period, normalizedGroup);
            return new RevenuePointResponse(label, label, rs.getBigDecimal("revenue"));
        });
    }

    @Override
    public List<LatestReviewResponse> latestReviews(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        return jdbcTemplate.query("""
                select r.*, a.email, a.first_name, a.last_name, a.avatar_url
                from reviews r
                join accounts a on a.id = r.account_id
                where """ + scope.condition("r") + """
                  and r.deleted_at is null
                order by r.created_at desc
                limit 10
                """, scope.params(), (rs, rowNum) -> new LatestReviewResponse(
                rs.getObject("id", UUID.class),
                rs.getBigDecimal("rating"),
                rs.getString("comment"),
                rs.getTimestamp("created_at").toInstant(),
                accountSummary(rs, "", "avatar_url")
        ));
    }

    @Override
    public List<NewestBookingResponse> newestBookings(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        return jdbcTemplate.query("""
                select b.*
                from bookings b
                where """ + scope.condition("b") + """
                order by b.created_at desc
                limit 10
                """, scope.params(), (rs, rowNum) -> {
            UUID bookingId = rs.getObject("id", UUID.class);
            return new NewestBookingResponse(
                    bookingId,
                    rs.getString("guest_name"),
                    rs.getObject("check_in", LocalDate.class),
                    rs.getObject("check_out", LocalDate.class),
                    rs.getTimestamp("created_at").toInstant(),
                    bookingItems(bookingId)
            );
        });
    }

    @Override
    public Object commissionRevenue(UUID hotelId, Integer year, LocalDate from, LocalDate to, Authentication authentication) {
        requireAdmin(authentication);
        CurrentUser user = requireUser(authentication);
        ReportScope scope = reportScope(hotelId, user);
        if (from != null && to != null) {
            MapSqlParameterSource params = scope.params()
                    .addValue("from", from)
                    .addValue("to", to);
            return jdbcTemplate.query("""
                    select b.created_at::date revenue_date, coalesce(sum(b.commission_amount), 0) revenue
                    from bookings b
                    where """ + scope.condition("b") + """
                      and b.created_at::date between :from and :to
                      and b.status = 'COMPLETED'
                      and b.commission_amount is not null
                    group by revenue_date
                    order by revenue_date
                    """, params, (rs, rowNum) -> Map.of(
                    "date", rs.getObject("revenue_date", LocalDate.class).toString(),
                    "revenue", rs.getBigDecimal("revenue")
            ));
        }

        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        return jdbcTemplate.query("""
                select months.month_number, coalesce(sum(b.commission_amount), 0) revenue
                from generate_series(1, 12) months(month_number)
                left join bookings b on extract(month from b.created_at) = months.month_number
                    and extract(year from b.created_at) = :year
                    and """ + scope.condition("b") + """
                    and b.status = 'COMPLETED'
                    and b.commission_amount is not null
                group by months.month_number
                order by months.month_number
                """, scope.params().addValue("year", selectedYear), (rs, rowNum) -> rs.getBigDecimal("revenue"));
    }
}
