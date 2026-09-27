package com.example.order_service.repository;

import com.example.order_service.entity.Order;
import com.example.order_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    long countByStatus(OrderStatus status);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(UUID id);

    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Order> findAllByStatus(OrderStatus status);
}
