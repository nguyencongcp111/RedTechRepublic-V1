package com.example.inventory_service.repository;

import com.example.inventory_service.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    @Query("select coalesce(sum(i.onHand),0) from InventoryItem i")
    long totalOnHand();
    @Query("select coalesce(sum(i.reserved),0) from InventoryItem i")
    long totalReserved();
    @Query("select count(i) from InventoryItem i where i.onHand - i.reserved <= 5")
    long lowStockCount();
    Optional<InventoryItem> findByProductId(UUID productId);

    Optional<InventoryItem> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from InventoryItem item where item.sku = :sku")
    Optional<InventoryItem> findBySkuForUpdate(@Param("sku") String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from InventoryItem item where item.id = :id")
    Optional<InventoryItem> findByIdForUpdate(@Param("id") UUID id);
}
