package com.example.inventory_service.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateInventoryItemRequest(
        @NotNull(message = "Không được để trống id sản phẩm")
        UUID productId,

        @NotBlank
        @Size(max=64)
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$",
                message = "SKU chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới." +
                        "Thử nhập lại"
        )
        String sku,

        @PositiveOrZero
        int onHand
) {
}
