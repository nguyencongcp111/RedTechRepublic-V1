package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Thông tin người bán. Mỗi user có tối đa một bản ghi:
 * PENDING = đang chờ admin duyệt, APPROVED = đã là SELLER, REJECTED = bị từ chối (được gửi lại).
 */
@Entity
@Table(name = "info_sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoSeller {
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    @Column(length = 500)
    private String rejectReason;

    @Column(length = 64)
    private String reviewedBy;

    private Instant reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
