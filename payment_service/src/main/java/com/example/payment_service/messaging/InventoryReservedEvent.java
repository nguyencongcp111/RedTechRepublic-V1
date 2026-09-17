package com.example.payment_service.messaging;

import com.example.payment_service.enums.PaymentMethod;

import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        UUID userId,
        long totalAmount,
        String currency,
        PaymentMethod paymentMethod
) {
}