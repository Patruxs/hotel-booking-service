package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.response.RevenueStatisticResponse;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.services.IRevenueService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements IRevenueService {

    private final BookingRepository bookingRepository;
    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.10);

    @Override
    public List<RevenueStatisticResponse> getYearlyRevenueStatistics(int year) {
        List<Object[]> results = bookingRepository.getRevenueStatistics(year);

        List<RevenueStatisticResponse> responses = new ArrayList<>();

        for (Object[] row : results) {
            Integer hotelId = (Integer) row[0];
            String hotelName = (String) row[1];
            Integer month = (Integer) row[2];
            Long totalBookings = (Long) row[3];

            Double totalPriceDouble = ((Number) row[4]).doubleValue();
            BigDecimal totalRevenue = BigDecimal.valueOf(totalPriceDouble);

            BigDecimal adminCommission = totalRevenue.multiply(COMMISSION_RATE);

            RevenueStatisticResponse response = RevenueStatisticResponse.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .month(month)
                    .year(year)
                    .totalBookings(totalBookings.intValue())
                    .totalRevenue(totalRevenue)
                    .adminCommission(adminCommission)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @Override
    public List<RevenueStatisticResponse> getRevenueStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = bookingRepository.getRevenueStatisticsByDateRange(startDate, endDate);

        List<RevenueStatisticResponse> responses = new ArrayList<>();

        for (Object[] row : results) {
            Integer hotelId = (Integer) row[0];
            String hotelName = (String) row[1];
            Long totalBookings = (Long) row[2];

            // Handle TotalPrice
            Double totalPriceDouble = ((Number) row[3]).doubleValue();
            BigDecimal totalRevenue = BigDecimal.valueOf(totalPriceDouble);

            // Calculate commission
            BigDecimal adminCommission = totalRevenue.multiply(COMMISSION_RATE);

            RevenueStatisticResponse response = RevenueStatisticResponse.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .totalBookings(totalBookings.intValue())
                    .totalRevenue(totalRevenue)
                    .adminCommission(adminCommission)
                    .month(null)
                    .year(null)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @Override
    public byte[] exportRevenueToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        return new byte[0];
    }
}
