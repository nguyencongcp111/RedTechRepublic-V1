package com.example.order_service.messaging;

import com.example.order_service.enums.PaymentMethod;

import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        UUID userId,
        long totalAmount,
        String currency,
        PaymentMethod paymentMethod
) {
}