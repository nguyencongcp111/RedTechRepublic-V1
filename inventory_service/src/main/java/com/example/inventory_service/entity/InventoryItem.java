package com.example.inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.UUID;

@Entity
@Table(
        name = "inventory_items",
        check = @CheckConstraint(constraint = "on_hand >= 0 and reserved >= 0 and reserved <= on_hand")
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false, unique = true)
    private UUID productId;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @PositiveOrZero
    @Column(nullable = false)
    private int onHand;

    @PositiveOrZero
    @Column(nullable = false)
    private int reserved;

    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public int available() {
        return onHand - reserved;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng giữ hàng phải lớn hơn 0");
        }
        if (available() < quantity) {
            throw new IllegalStateException("Số sản phẩm trong kho không đủ");
        }
        reserved = Math.addExact(reserved, quantity);
    }

    public void confirm(int quantity) {
        ensureReservedQuantity(quantity);
        onHand = Math.subtractExact(onHand, quantity);
        reserved = Math.subtractExact(reserved, quantity);
    }

    public void release(int quantity) {
        ensureReservedQuantity(quantity);
        reserved = Math.subtractExact(reserved, quantity);
    }

    public void adjustOnHand(int quantityDelta) {
        int adjusted = Math.addExact(onHand, quantityDelta);
        if (adjusted < reserved || adjusted < 0) {
            throw new IllegalStateException("Điều chỉnh làm tồn kho nhỏ hơn lượng đang giữ");
        }
        onHand = adjusted;
    }

    private void ensureReservedQuantity(int quantity) {
        if (quantity <= 0 || reserved < quantity || onHand < quantity) {
            throw new IllegalStateException("Tồn kho đã giữ không còn hợp lệ");
        }
    }
}
