package com.example.payment_service.repository;

import com.example.payment_service.entity.Payment;
import com.example.payment_service.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    long countByStatus(PaymentStatus status);
    @org.springframework.data.jpa.repository.Query("select p.currency, sum(p.amount) from Payment p where p.status = com.example.payment_service.enums.PaymentStatus.SUCCEEDED group by p.currency")
    List<Object[]> revenueByCurrency();
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByProviderReference(String providerReference);

    List<Payment> findAllByOrderId(UUID orderId);

    List<Payment> findAllByOrderIdIn(List<UUID> orderIds);

    List<Payment> findAllByStatus(PaymentStatus status);

    Optional<Payment> findFirstByOrderIdAndStatusIn(UUID orderId, List<PaymentStatus> statuses);
}
