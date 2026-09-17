package com.example.product_service.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        Long totalItems,
        int totalPages,
        boolean isLast
) {
}
