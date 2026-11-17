package com.example.inventory_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Gian hàng (booth) do ADMIN hoặc SELLER tạo. */
@Entity
@Table(name = "shops")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Shop {
    /** Id cố định của "Shop Hệ Thống" (tài khoản testadmin). */
    public static final UUID SYSTEM_SHOP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String address;

    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String phone;

    @NotBlank @Size(max = 255)
    @Column(nullable = false)
    private String email;

    /** Username của user tạo gian hàng (lấy từ token). */
    @NotBlank @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String ownerUsername;

    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
