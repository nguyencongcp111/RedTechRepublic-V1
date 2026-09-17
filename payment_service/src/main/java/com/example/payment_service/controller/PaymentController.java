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

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment (
            @RequestHeader("Idempotency-Key") @Size(min = 1, max = 128) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return new ResponseEntity<>(
                paymentService.createPayment(idempotencyKey, request), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById (@PathVariable UUID id)
    {
        return new ResponseEntity<>(
                paymentService.getPaymentById(id), HttpStatus.OK
        );
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByOrderId (@PathVariable UUID orderId)
    {
        return new ResponseEntity<>(
                paymentService.getPaymentByOrderId(orderId), HttpStatus.OK
        );
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
}
