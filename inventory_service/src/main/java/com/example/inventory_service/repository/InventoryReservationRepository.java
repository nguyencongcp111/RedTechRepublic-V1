package com.example.inventory_service.repository;

import com.example.inventory_service.entity.InventoryReservation;
import com.example.inventory_service.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    @EntityGraph(attributePaths = "inventoryItem")
    Optional<InventoryReservation> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "inventoryItem")
    List<InventoryReservation> findAllByOrderId(UUID orderId);

    List<InventoryReservation> findAllByStatusAndExpiresAtBefore(ReservationStatus status, Instant expiresAt);

    @EntityGraph(attributePaths = "inventoryItem")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from InventoryReservation reservation where reservation.id = :id")
    Optional<InventoryReservation> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = "select * from inventory_reservations " +
            "where status = 'ACTIVE' and expires_at < :now " +
            "order by expires_at for update skip locked limit :batchSize", nativeQuery = true)
    List<InventoryReservation> findExpiredForUpdate(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );
}
