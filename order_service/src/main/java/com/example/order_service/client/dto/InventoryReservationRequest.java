package com.example.order_service.client.dto;

import java.util.UUID;

public record InventoryReservationRequest(UUID orderId, String sku, int quantity) {
}
