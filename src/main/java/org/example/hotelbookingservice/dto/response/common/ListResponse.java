package org.example.hotelbookingservice.dto.response.common;

import java.util.List;

public record ListResponse<T>(
        List<T> data,
        PageMeta meta,
        Integer page,
        Integer limit,
        Long total,
        Integer totalPages
) {
}
