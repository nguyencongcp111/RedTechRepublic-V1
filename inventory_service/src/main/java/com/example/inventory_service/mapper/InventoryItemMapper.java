package com.example.inventory_service.mapper;

import com.example.inventory_service.dto.request.CreateInventoryItemRequest;
import com.example.inventory_service.dto.response.InventoryItemResponse;
import com.example.inventory_service.entity.InventoryItem;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class InventoryItemMapper {
    public InventoryItem addToEntity(CreateInventoryItemRequest request) {
        if (request == null) {
            return null;
        }

        return InventoryItem.builder()
                .productId(request.productId())
                .sku(request.sku().trim().toUpperCase(Locale.ROOT))
                .onHand(request.onHand())
                .reserved(0)
                .build();
    }

    public InventoryItemResponse toDTO (InventoryItem inventoryItem) {
        if (inventoryItem == null) {
            return null;
        }

        return new InventoryItemResponse(inventoryItem.getId(), inventoryItem.getProductId(), inventoryItem.getSku(), inventoryItem.getOnHand(), inventoryItem.getReserved(), inventoryItem.getVersion());
    }
}
