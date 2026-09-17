package com.example.payment_service.client.dto;

import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        long totalAmount,
        String currency,
        String status
) {
    public boolean payable() {
        return "INVENTORY_RESERVED".equals(status) || "PAYMENT_PROCESSING".equals(status);
    }
}
