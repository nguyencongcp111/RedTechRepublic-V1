package com.example.inventory_service.service;

import com.example.inventory_service.dto.request.CreateInventoryItemRequest;
import com.example.inventory_service.dto.request.AdjustInventoryRequest;
import com.example.inventory_service.dto.response.InventoryItemResponse;

public interface InventoryItemService {
    InventoryItemResponse addInventoryItem(CreateInventoryItemRequest request);

    InventoryItemResponse getInventoryItemBySku(String sku);

    InventoryItemResponse adjustInventory(String sku, AdjustInventoryRequest request);
}
