package com.example.inventory_service.service.impl;

import com.example.inventory_service.dto.request.CreateInventoryItemRequest;
import com.example.inventory_service.dto.request.AdjustInventoryRequest;
import com.example.inventory_service.dto.response.InventoryItemResponse;
import com.example.inventory_service.entity.InventoryItem;
import com.example.inventory_service.exception.ResourceNotFoundException;
import com.example.inventory_service.mapper.InventoryItemMapper;
import com.example.inventory_service.repository.InventoryItemRepository;
import com.example.inventory_service.service.InventoryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl implements InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemMapper inventoryItemMapper;

    @Override
    @Transactional
    public InventoryItemResponse addInventoryItem(CreateInventoryItemRequest request) {
        InventoryItem inventoryItem = inventoryItemMapper.addToEntity(request);

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        return inventoryItemMapper.toDTO(savedInventoryItem);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItemBySku(String sku) {
        return inventoryItemRepository.findBySku(sku.trim().toUpperCase(Locale.ROOT))
                .map(inventoryItemMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không thể tìm thấy sản phẩm")
                );
    }

    @Override
    @Transactional
    public InventoryItemResponse adjustInventory(String sku, AdjustInventoryRequest request) {
        InventoryItem item = inventoryItemRepository
                .findBySkuForUpdate(sku.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Không thể tìm thấy sản phẩm"));
        item.adjustOnHand(request.quantityDelta());
        return inventoryItemMapper.toDTO(item);
    }
}
