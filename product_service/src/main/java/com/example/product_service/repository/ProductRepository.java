package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findAllByStatus(ProductStatus status);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
