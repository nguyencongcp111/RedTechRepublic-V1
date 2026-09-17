package com.example.inventory_service.dto.response;

import com.example.inventory_service.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservationResponse(
        UUID id,
        UUID orderId,
        UUID inventoryItemId,
        String sku,
        int quantity,
        ReservationStatus status,
        Instant expiresAt,
        Instant createdAt
) {
}
