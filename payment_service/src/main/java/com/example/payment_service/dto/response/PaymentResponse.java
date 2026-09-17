package com.example.payment_service.dto.response;

import com.example.payment_service.enums.PaymentMethod;
import com.example.payment_service.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID id,

        UUID orderId,

        long amount,

        String currency,

        PaymentMethod paymentMethod,

        PaymentStatus status,

        String providerReference,

        String failureCode,

        String failureMessage,

        Instant createdAt,

        Instant updatedAt,

        Instant paidAt

) {
}
