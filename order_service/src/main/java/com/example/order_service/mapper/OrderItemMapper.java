package com.example.order_service.mapper;

import com.example.order_service.dto.request.CreateOrderItemRequest;
import com.example.order_service.client.dto.ProductCatalogResponse;
import com.example.order_service.dto.response.OrderItemResponse;
import com.example.order_service.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(CreateOrderItemRequest request, ProductCatalogResponse product) {
        if (request == null || product == null) {
            return null;
        }

        long lineTotal = Math.multiplyExact(
                product.price(),
                request.quantity()
        );

        return OrderItem.builder()
                .productId(request.productId())
                .sku(product.sku())
                .productName(product.name())
                .quantity(request.quantity())
                .unitPrice(product.price())
                .lineTotal(lineTotal)
                .build();
    }

    public OrderItemResponse toDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

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
