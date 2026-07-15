package org.example.hotelbookingservice.dto.response.hotel.operations;

import java.util.List;
import org.example.hotelbookingservice.dto.response.common.PageMeta;

public record PaginatedResponse<T>(
        List<T> data,
        PageMeta meta
) {
}
