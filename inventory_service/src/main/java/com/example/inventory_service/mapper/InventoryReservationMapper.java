package com.example.inventory_service.mapper;

import com.example.inventory_service.dto.request.CreateInventoryReservationRequest;
import com.example.inventory_service.dto.response.InventoryReservationResponse;
import com.example.inventory_service.entity.InventoryItem;
import com.example.inventory_service.entity.InventoryReservation;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class InventoryReservationMapper {
    public InventoryReservation addToEntity(
            CreateInventoryReservationRequest request,
            InventoryItem inventoryItem,
            String idempotencyKey,
            Instant expiresAt
    ) {
        if (request == null) {
            return null;
        }

        return InventoryReservation.builder()
                .orderId(request.orderId())
                .inventoryItem(inventoryItem)
                .quantity(request.quantity())
                .idempotencyKey(idempotencyKey)
                .expiresAt(expiresAt)
                .build();
    }

    public InventoryReservationResponse toDTO(InventoryReservation inventoryReservation) {
        if (inventoryReservation == null) {
            return null;
        }

        InventoryItem item = inventoryReservation.getInventoryItem();


        return new InventoryReservationResponse(
                inventoryReservation.getId(),
                inventoryReservation.getOrderId(),
                item.getId(),
                item.getSku(),
                inventoryReservation.getQuantity(),
                inventoryReservation.getStatus(),
                inventoryReservation.getExpiresAt(),
                inventoryReservation.getCreatedAt());
    }
}
