package com.example.order_service.controller;

import com.example.order_service.dto.request.CreateOrderItemRequest;
import com.example.order_service.dto.request.CreateOrderRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> addOrder(
            @RequestHeader("Idempotency-Key") @Size(min = 1, max = 128) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        com.example.order_service.security.Ownership.require(request.userId());
        return new ResponseEntity<>(
                orderService.createOrder(idempotencyKey, request), HttpStatus.CREATED
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        if (!com.example.order_service.security.Ownership.service()) requireOwner(orderId);
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /** List all orders for a given user, newest first */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable UUID userId) {
        com.example.order_service.security.Ownership.require(userId);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateOrderItemRequest request
    ) {
        requireOwner(orderId);
        return ResponseEntity.ok(
                orderService.addItem(orderId, request)
        );
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderResponse> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId
    ) {
        requireOwner(orderId);
        return ResponseEntity.ok(
                orderService.removeItem(orderId, orderItemId)
        );
    }
    private void requireOwner(UUID orderId) {
        com.example.order_service.security.Ownership.require(orderService.getOrderById(orderId).userId());
    }
}
