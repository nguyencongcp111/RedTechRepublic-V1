package com.example.inventory_service.dto.response;

import java.util.UUID;

public record InventoryItemResponse(
        UUID id,
        UUID productId,
        String sku,
        int onHand,
        int reserved,
        long version
) {
}
