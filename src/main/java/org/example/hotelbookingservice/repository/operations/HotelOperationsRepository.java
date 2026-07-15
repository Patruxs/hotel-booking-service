package org.example.hotelbookingservice.repository.operations;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.entity.Inventory;
import org.example.hotelbookingservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HotelOperationsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final InventoryRepository inventoryRepository;

    public HotelOperationsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }

    @Autowired
    public HotelOperationsRepository(NamedParameterJdbcTemplate jdbcTemplate, InventoryRepository inventoryRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.inventoryRepository = inventoryRepository;
    }

    public int update(String sql, MapSqlParameterSource params) {
        return jdbcTemplate.update(sql, params);
    }

    public <T> T queryForObject(String sql, MapSqlParameterSource params, Class<T> requiredType) {
        return jdbcTemplate.queryForObject(sql, params, requiredType);
    }

    public <T> List<T> query(String sql, MapSqlParameterSource params, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public Inventory upsertInventory(UUID hotelId,
                                     UUID roomTypeId,
                                     LocalDate date,
                                     int totalRooms,
                                     int availableRooms,
                                     boolean stopSell) {
        UUID id = UUID.randomUUID();
        if (inventoryRepository != null) {
            inventoryRepository.upsertInventory(id, hotelId, roomTypeId, date, totalRooms, availableRooms, stopSell);
            return findInventory(hotelId, roomTypeId, date).orElseThrow();
        }
        update("""
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms, stop_sell)
                values (:id, :hotelId, :roomTypeId, :date, :totalRooms, :availableRooms, :stopSell)
                on conflict (room_type_id, stay_date)
                do update set total_rooms = excluded.total_rooms,
                              available_rooms = excluded.available_rooms,
                              stop_sell = excluded.stop_sell,
                              updated_at = now()
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("date", date)
                .addValue("totalRooms", totalRooms)
                .addValue("availableRooms", availableRooms)
                .addValue("stopSell", stopSell));
        return findInventory(hotelId, roomTypeId, date).orElseThrow();
    }

    public Optional<Inventory> findInventory(UUID hotelId, UUID roomTypeId, LocalDate date) {
        if (inventoryRepository != null) {
            return inventoryRepository.findByHotel_IdAndRoomType_IdAndDate(hotelId, roomTypeId, date);
        }
        return query("""
                select *
                from inventories
                where hotel_id = :hotelId and room_type_id = :roomTypeId and stay_date = :date
                """, new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("date", date), HotelOperationsRepository::mapDetachedInventory)
                .stream().findFirst();
    }

    public Optional<Inventory> findInventoryById(UUID inventoryId, UUID hotelId, UUID roomTypeId) {
        if (inventoryRepository != null) {
            return inventoryRepository.findByIdAndHotel_IdAndRoomType_Id(inventoryId, hotelId, roomTypeId);
        }
        return query("""
                select *
                from inventories
                where id = :inventoryId
                  and hotel_id = :hotelId
                  and room_type_id = :roomTypeId
                """, new MapSqlParameterSource()
                .addValue("inventoryId", inventoryId)
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId), HotelOperationsRepository::mapDetachedInventory)
                .stream().findFirst();
    }

    public int deleteEligibleInventory(UUID inventoryId, UUID hotelId, UUID roomTypeId) {
        return update("""
                delete from inventories i
                where i.id = :inventoryId
                  and i.hotel_id = :hotelId
                  and i.room_type_id = :roomTypeId
                  and i.stay_date > current_date
                  and i.available_rooms = i.total_rooms
                  and not exists (
                      select 1
                      from booking_items bi
                      join bookings b on b.id = bi.booking_id
                      where bi.room_type_id = i.room_type_id
                        and b.hotel_id = i.hotel_id
                        and b.status in ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED')
                        and b.check_in <= i.stay_date
                        and b.check_out > i.stay_date
                  )
                """, new MapSqlParameterSource()
                .addValue("inventoryId", inventoryId)
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId));
    }

    public List<Inventory> listInventory(UUID hotelId, UUID roomTypeId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("""
                select *
                from inventories
                where hotel_id = :hotelId
                  and room_type_id = :roomTypeId
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId);
        if (from != null) {
            sql.append(" and stay_date >= :fromDate");
            params.addValue("fromDate", from);
        }
        if (to != null) {
            sql.append(" and stay_date < :toDate");
            params.addValue("toDate", to);
        }
        sql.append(" order by stay_date");
        return query(sql.toString(), params, HotelOperationsRepository::mapDetachedInventory);
    }

    public long countAvailableRoomTypes(UUID hotelId, LocalDate from, LocalDate to, long nights) {
        return queryForObject("""
                select count(*)
                from (
                    select rt.id
                    """ + availabilityBase() + """
                ) available_room_types
                """, availabilityParams(hotelId, from, to, nights, 1, 0), Long.class);
    }

    public <T> List<T> queryAvailableRoomTypes(UUID hotelId,
                                               LocalDate from,
                                               LocalDate to,
                                               long nights,
                                               int limit,
                                               int offset,
                                               RowMapper<T> rowMapper) {
        return query("""
                select rt.id, rt.hotel_id, rt.name, rt.description, rt.price_per_night, rt.max_guests,
                       coalesce(rt.number_of_bedrooms, 0) as number_of_bedrooms,
                       min(i.available_rooms) as available_rooms
                """ + availabilityBase() + """
                order by rt.price_per_night asc, rt.name asc
                limit :limit offset :offset
                """, availabilityParams(hotelId, from, to, nights, limit, offset), rowMapper);
    }

    private static String availabilityBase() {
        return """
                from room_types rt
                join hotels h on h.id = rt.hotel_id
                join inventories i on i.room_type_id = rt.id
                where rt.hotel_id = :hotelId
                  and h.status = 'ACTIVE'
                  and h.deleted_at is null
                  and rt.active
                  and rt.deleted_at is null
                  and i.stay_date >= :fromDate
                  and i.stay_date < :toDate
                  and not i.stop_sell
                  and i.available_rooms > 0
                group by rt.id, rt.hotel_id, rt.name, rt.description, rt.price_per_night, rt.max_guests,
                         rt.number_of_bedrooms
                having count(distinct i.stay_date) = :nights
                """;
    }

    private static MapSqlParameterSource availabilityParams(UUID hotelId,
                                                            LocalDate from,
                                                            LocalDate to,
                                                            long nights,
                                                            int limit,
                                                            int offset) {
        return new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("fromDate", from)
                .addValue("toDate", to)
                .addValue("nights", nights)
                .addValue("limit", limit)
                .addValue("offset", offset);
    }

    private static Inventory mapDetachedInventory(ResultSet rs, int rowNum) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId((UUID) rs.getObject("id"));
        inventory.setHotelId((UUID) rs.getObject("hotel_id"));
        inventory.setRoomTypeId((UUID) rs.getObject("room_type_id"));
        inventory.setDate(rs.getObject("stay_date", LocalDate.class));
        inventory.setTotalRooms(rs.getInt("total_rooms"));
        inventory.setAvailableRooms(rs.getInt("available_rooms"));
        inventory.setStopSell(rs.getBoolean("stop_sell"));
        inventory.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        inventory.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return inventory;
    }
}
