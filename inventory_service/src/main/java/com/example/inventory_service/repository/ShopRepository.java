package com.example.inventory_service.repository;

import com.example.inventory_service.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    List<Shop> findByOwnerUsernameOrderByCreatedAtAsc(String ownerUsername);

    Page<Shop> findByNameContainingIgnoreCaseOrOwnerUsernameContainingIgnoreCase(String name, String owner, Pageable pageable);
}
