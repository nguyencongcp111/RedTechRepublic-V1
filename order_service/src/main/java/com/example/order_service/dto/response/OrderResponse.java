package com.example.order_service.dto.response;

import com.example.order_service.entity.OrderItem;
import com.example.order_service.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        int totalQuantity,
        long totalAmount,
        String currency,
        String failureReason,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
