package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.response.RevenueStatisticResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface IRevenueService {
    List<RevenueStatisticResponse> getYearlyRevenueStatistics(int year);
    List<RevenueStatisticResponse> getRevenueStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
    byte[] exportRevenueToExcel(LocalDate startDate, LocalDate endDate) throws IOException;
}
