package com.example.order_service.client.dto;

import java.util.UUID;

public record ProductCatalogResponse(
        UUID id,
        String sku,
        String name,
        long price,
        String currency,
        String status
) {
    public boolean active() {
        return "ACTIVE".equals(status);
    }
}
