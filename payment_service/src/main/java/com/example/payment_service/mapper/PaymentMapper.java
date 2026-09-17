package com.example.payment_service.mapper;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PaymentMapper {

    public Payment toEntity(
            CreatePaymentRequest request,
            String idempotencyKey
    ) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .currency(
                        request.currency()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();
    }

    public PaymentResponse toDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getProviderReference(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getPaidAt()
        );
    }
}
