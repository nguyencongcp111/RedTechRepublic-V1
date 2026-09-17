package com.example.inventory_service.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(
        @NotNull Integer quantityDelta
) {
}
