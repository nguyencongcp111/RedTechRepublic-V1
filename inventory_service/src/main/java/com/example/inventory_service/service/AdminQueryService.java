package com.example.inventory_service.service;
import com.example.inventory_service.repository.InventoryItemRepository;
import com.example.inventory_service.mapper.InventoryItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AdminQueryService {
    private final InventoryItemRepository repository;
    private final InventoryItemMapper mapper;
    public Map<String,Object> list(int page, int size) {
        var result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sku"))).map(mapper::toDTO);
        return Map.of("items", result.getContent(), "page", page, "size", size, "totalItems", result.getTotalElements(), "totalPages", result.getTotalPages(), "isLast", result.isLast());
    }
    public Map<String,Object> stats() {
        long onHand = repository.totalOnHand(), reserved = repository.totalReserved();
        return Map.of("total", repository.count(), "onHand", onHand, "reserved", reserved, "available", onHand-reserved, "lowStock", repository.lowStockCount()); }
}
