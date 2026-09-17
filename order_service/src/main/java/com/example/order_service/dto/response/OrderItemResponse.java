package com.example.order_service.dto.response;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        UUID inventoryReservationId,
        String sku,
        String productName,
        int quantity,
        long unitPrice,
        long lineTotal
) {
}
