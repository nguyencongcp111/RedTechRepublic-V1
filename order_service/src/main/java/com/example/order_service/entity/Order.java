package com.example.order_service.entity;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.enums.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Positive
    @Column(nullable = false)
    private int totalQuantity;

    @PositiveOrZero
    @Column(nullable = false)
    private long totalAmount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @NotBlank
    @Size(max = 128)
    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String requestHash;

    @Size(max = 1000)
    @Column(length = 1000)
    private String failureReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "OrderItem không được null");

        items.add(item);
        item.setOrder(this);

        totalQuantity = Math.addExact(
                totalQuantity,
                item.getQuantity()
        );

        totalAmount = Math.addExact(
                totalAmount,
                item.getLineTotal()
        );
    }

    public void removeItem(OrderItem item) {
        Objects.requireNonNull(item, "OrderItem không được null");

        if (!items.remove(item)) {
            throw new IllegalArgumentException(
                    "OrderItem không thuộc order này"
            );
        }

        totalQuantity = Math.subtractExact(
                totalQuantity,
                item.getQuantity()
        );

        totalAmount = Math.subtractExact(
                totalAmount,
                item.getLineTotal()
        );

        item.setOrder(null);
    }

    public void markInventoryReserved() {
        transition(OrderStatus.PENDING, OrderStatus.INVENTORY_RESERVED);
    }

    public void markPaymentProcessing() {
        transition(OrderStatus.INVENTORY_RESERVED, OrderStatus.PAYMENT_PROCESSING);
    }

    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            return;
        }
        transition(OrderStatus.PAYMENT_PROCESSING, OrderStatus.CONFIRMED);
    }

    public void fail(String reason) {
        if (status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể đánh dấu thất bại order ở trạng thái " + status);
        }
        status = OrderStatus.FAILED;
        failureReason = reason;
    }

    private void transition(OrderStatus expected, OrderStatus target) {
        if (status == target) {
            return;
        }
        if (status != expected) {
            throw new IllegalStateException("Không thể chuyển order từ " + status + " sang " + target);
        }
        status = target;
    }
}
