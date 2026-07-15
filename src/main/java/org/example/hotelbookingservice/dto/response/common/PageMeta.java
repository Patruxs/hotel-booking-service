package org.example.hotelbookingservice.dto.response.common;

public record PageMeta(
        int limit,
        int offset,
        long total
) {
}
