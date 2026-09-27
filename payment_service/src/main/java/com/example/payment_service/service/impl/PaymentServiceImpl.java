package com.example.payment_service.service.impl;

import com.example.payment_service.client.OrderClient;
import com.example.payment_service.client.dto.OrderSummaryResponse;
import com.example.payment_service.dto.request.CancelPaymentRequest;
import com.example.payment_service.dto.request.ConfirmPaymentRequest;
import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.enums.PaymentStatus;
import com.example.payment_service.exception.ResourceNotFoundException;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.outbox.PaymentOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;
    private final PaymentOutboxService paymentOutboxService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PaymentResponse createPayment (String idempotencyKey, CreatePaymentRequest request) {
        lockIdempotencyKey(idempotencyKey);
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            validateSameRequest(existing.get(), request);
            return paymentMapper.toDTO(existing.get());
        }

        lockIdempotencyKey("payment-order:" + request.orderId());
        if (paymentRepository.findFirstByOrderIdAndStatusIn(
                request.orderId(), List.of(PaymentStatus.PENDING, PaymentStatus.SUCCEEDED)
        ).isPresent()) {
            throw new IllegalStateException("Order đã có payment đang xử lý hoặc đã thành công");
        }

        OrderSummaryResponse order = orderClient.getOrder(request.orderId());
        if (!order.payable()) {
            throw new IllegalStateException("Order không ở trạng thái có thể thanh toán");
        }
        if (order.totalAmount() != request.amount()
                || !order.currency().equalsIgnoreCase(request.currency().trim())) {
            throw new IllegalStateException("Số tiền hoặc currency không khớp với order");
        }

        Payment payment = paymentMapper.toEntity(request, idempotencyKey);

        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        paymentOutboxService.recordStatus(savedPayment);

        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById (UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thanh toán"
                ));

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentByOrderId (UUID id) {

        return paymentRepository.findAllByOrderId(id)
                .stream().map(paymentMapper::toDTO).toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderIds(List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return List.of();
        return paymentRepository.findAllByOrderIdIn(orderIds)
                .stream().map(paymentMapper::toDTO).toList();
    }


    @Override
    @Transactional
    public PaymentResponse confirmPayment (UUID id, ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thanh toán"
                ));

        payment.succeed(request.providerReference().trim(), Instant.now());
        paymentOutboxService.recordStatus(payment);

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment (UUID id, CancelPaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thanh toán"
                ));

        payment.fail(request.reasonCode(), request.reason().trim());
        paymentOutboxService.recordStatus(payment);

        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refund (UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thanh toán"
                ));

        payment.refund();
        paymentOutboxService.recordStatus(payment);

        return paymentMapper.toDTO(payment);
    }

    private void validateSameRequest(Payment payment, CreatePaymentRequest request) {
        boolean same = payment.getOrderId().equals(request.orderId())
                && payment.getAmount() == request.amount()
                && payment.getCurrency().equalsIgnoreCase(request.currency().trim())
                && payment.getPaymentMethod() == request.paymentMethod();
        if (!same) {
            throw new com.example.payment_service.exception.IdempotencyKeyConflictException(
                    "Idempotency-Key đã được dùng cho payment khác"
            );
        }
    }

    private void lockIdempotencyKey(String key) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "select pg_advisory_xact_lock(hashtextextended(?, 0))"
            )) {
                statement.setString(1, key);
                statement.execute();
            }
        });
    }
}
