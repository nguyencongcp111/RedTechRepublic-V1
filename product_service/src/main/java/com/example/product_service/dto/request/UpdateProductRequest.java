package com.example.product_service.dto.request;

import com.example.product_service.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull
        @PositiveOrZero
        Long price,

        @NotNull
        ProductStatus status,

        @NotNull java.util.UUID categoryId
) {}
