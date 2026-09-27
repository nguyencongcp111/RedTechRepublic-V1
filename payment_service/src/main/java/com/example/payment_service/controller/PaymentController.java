package com.example.payment_service.controller;

import com.example.payment_service.dto.request.CancelPaymentRequest;
import com.example.payment_service.dto.request.ConfirmPaymentRequest;
import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    private final PaymentService paymentService;
    private final com.example.payment_service.client.OrderClient orderClient;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment (
            @RequestHeader("Idempotency-Key") @Size(min = 1, max = 128) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        requireOrder(request.orderId());
        return new ResponseEntity<>(
                paymentService.createPayment(idempotencyKey, request), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById (@PathVariable UUID id)
    {
        requireOrder(paymentService.getPaymentById(id).orderId());
        return new ResponseEntity<>(
                paymentService.getPaymentById(id), HttpStatus.OK
        );
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByOrderId (@PathVariable UUID orderId)
    {
        requireOrder(orderId);
        return new ResponseEntity<>(
                paymentService.getPaymentByOrderId(orderId), HttpStatus.OK
        );
    }


    @PostMapping("/by-orders")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrders(
            @RequestBody @Size(min = 1, max = 100) List<@jakarta.validation.constraints.NotNull UUID> orderIds
    ) {
        orderIds.stream().distinct().forEach(this::requireOrder);
        return ResponseEntity.ok(paymentService.getPaymentsByOrderIds(orderIds));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment (
            @PathVariable UUID paymentId,
            @Valid @RequestBody ConfirmPaymentRequest request
    ) {
        return new ResponseEntity<>(
                paymentService.confirmPayment(paymentId, request), HttpStatus.OK
        );
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment (
            @PathVariable UUID paymentId, @Valid @RequestBody CancelPaymentRequest request
    ) {
        requireOrder(paymentService.getPaymentById(paymentId).orderId());
        return new ResponseEntity<>(
                paymentService.cancelPayment(paymentId, request), HttpStatus.OK
        );
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refund (
            @PathVariable UUID paymentId
    ) {
        return new ResponseEntity<>(
                paymentService.refund(paymentId), HttpStatus.OK
        );
    }
    private void requireOrder(UUID orderId) {
        com.example.payment_service.security.Ownership.require(orderClient.getOrder(orderId).userId());
    }
}
