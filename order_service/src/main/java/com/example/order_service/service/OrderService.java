package com.example.order_service.service;

import com.example.order_service.dto.request.CreateOrderItemRequest;
import com.example.order_service.dto.request.CreateOrderRequest;
import com.example.order_service.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(
            String idempotencyKey,
            CreateOrderRequest request
    );

    OrderResponse getOrderById(UUID id);

    OrderResponse addItem(
            UUID orderId,
            CreateOrderItemRequest request
    );

    OrderResponse removeItem(
            UUID orderId,
            UUID orderItemId
    );
}
