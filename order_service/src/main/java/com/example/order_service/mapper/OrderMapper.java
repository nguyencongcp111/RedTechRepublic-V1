package com.example.order_service.mapper;

import com.example.order_service.dto.request.CreateOrderRequest;
import com.example.order_service.dto.response.OrderItemResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class OrderMapper {
    public Order addToEntity(
            CreateOrderRequest request,
            String idempotencyKey,
            String requestHash
    ) {
        if (request == null) {
            return null;
        }

        return Order.builder()
                .userId(request.userId())
                .currency(
                        request.currency()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .totalQuantity(0)
                .totalAmount(0)
                .build();
    }

    public OrderResponse toDTO(
            Order order

    ) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalQuantity(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getFailureReason(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getInventoryReservationId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
