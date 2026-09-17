package com.example.payment_service.messaging;

import java.time.Instant;
import java.util.UUID;

public record PaymentStatusEvent(
        UUID paymentId,
        UUID orderId,
        String status,
        Instant occurredAt
) {
}
