package com.example.payment_service.service;

import com.example.payment_service.dto.request.CancelPaymentRequest;
import com.example.payment_service.dto.request.ConfirmPaymentRequest;
import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(String idempotencyKey, CreatePaymentRequest request);

    PaymentResponse getPaymentById(UUID id);

    List<PaymentResponse> getPaymentByOrderId(UUID id);

    List<PaymentResponse> getPaymentsByOrderIds(List<UUID> orderIds);

    PaymentResponse confirmPayment(UUID id, ConfirmPaymentRequest request);

    PaymentResponse cancelPayment(UUID id, CancelPaymentRequest request);

    PaymentResponse refund(UUID id);
}
