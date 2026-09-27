package com.example.order_service.service;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AdminQueryService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    public Map<String,Object> list(int page, int size) {
        var result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(mapper::toDTO);
        return Map.of("items", result.getContent(), "page", page, "size", size, "totalItems", result.getTotalElements(), "totalPages", result.getTotalPages(), "isLast", result.isLast());
    }
    public Map<String,Object> stats() { var byStatus = new LinkedHashMap<String, Long>();
        for (var status : com.example.order_service.enums.OrderStatus.values()) byStatus.put(status.name(), repository.countByStatus(status));
        return Map.of("total", repository.count(), "byStatus", byStatus); }
}
