package com.example.payment_service.entity;

import com.example.payment_service.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {
    @Test
    void successfulPaymentCanBeRefunded() {
        Payment payment = Payment.builder().status(PaymentStatus.PENDING).build();
        payment.succeed("provider-1", Instant.parse("2026-01-01T00:00:00Z"));
        payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    @Test
    void failedPaymentCannotBeConfirmed() {
        Payment payment = Payment.builder().status(PaymentStatus.PENDING).build();
        payment.fail("DECLINED", "Declined");
        assertThrows(IllegalStateException.class,
                () -> payment.succeed("provider-2", Instant.now()));
    }
}
