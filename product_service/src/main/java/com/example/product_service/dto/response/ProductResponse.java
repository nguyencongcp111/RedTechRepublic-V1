package com.example.product_service.dto.response;

import com.example.product_service.enums.ProductStatus;

import java.util.UUID;
import java.time.Instant;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        CategoryResponse category,
        String description,
        Long price,
        String currency,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt,
        long version,
        String imageUrl
) {}
