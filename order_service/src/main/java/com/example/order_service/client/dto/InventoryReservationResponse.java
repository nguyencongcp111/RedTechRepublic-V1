package com.example.order_service.client.dto;

import java.util.UUID;

public record InventoryReservationResponse(
        UUID id,
        UUID orderId,
        UUID inventoryItemId,
        String sku,
        int quantity,
        String status
) {
}
