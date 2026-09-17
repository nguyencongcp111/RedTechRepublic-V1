package com.example.inventory_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateInventoryReservationRequest(
        @NotNull UUID orderId,
        @NotBlank @Size(max = 64) String sku,
        @Positive int quantity
) {


}
