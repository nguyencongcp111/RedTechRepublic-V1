package com.example.payment_service.entity;

import com.example.payment_service.enums.PaymentMethod;
import com.example.payment_service.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID orderId;

    @Positive
    @Column(nullable = false)
    private long amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotBlank
    @Size(max = 128)
    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Size(max = 255)
    @Column(unique = true)
    private String providerReference;

    @Size(max = 64)
    @Column(length = 64)
    private String failureCode;

    @Size(max = 1000)
    @Column(length = 1000)
    private String failureMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant paidAt;

    @Version
    private long version;

    public void succeed(String reference, Instant occurredAt) {
        if (status == PaymentStatus.SUCCEEDED && reference.equals(providerReference)) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Không thể xác nhận payment ở trạng thái " + status);
        }
        status = PaymentStatus.SUCCEEDED;
        providerReference = reference;
        paidAt = occurredAt;
        failureCode = null;
        failureMessage = null;
    }

    public void fail(String code, String message) {
        if (status == PaymentStatus.FAILED && java.util.Objects.equals(code, failureCode)
                && java.util.Objects.equals(message, failureMessage)) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Không thể hủy payment ở trạng thái " + status);
        }
        status = PaymentStatus.FAILED;
        failureCode = code;
        failureMessage = message;
    }

    public void refund() {
        if (status == PaymentStatus.REFUNDED) {
            return;
        }
        if (status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Chỉ payment thành công mới có thể hoàn tiền");
        }
        status = PaymentStatus.REFUNDED;
    }
}
