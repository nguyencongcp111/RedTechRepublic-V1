package com.example.payment_service.service;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AdminQueryService {
    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    public Map<String,Object> list(int page, int size) {
        var result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(mapper::toDTO);
        return Map.of("items", result.getContent(), "page", page, "size", size, "totalItems", result.getTotalElements(), "totalPages", result.getTotalPages(), "isLast", result.isLast());
    }
    public Map<String,Object> stats() { var byStatus = new LinkedHashMap<String, Long>();
        for (var status : com.example.payment_service.enums.PaymentStatus.values()) byStatus.put(status.name(), repository.countByStatus(status));
        var revenue = repository.revenueByCurrency().stream().map(r -> Map.of("currency", r[0], "amount", r[1])).toList();
        return Map.of("total", repository.count(), "byStatus", byStatus, "revenue", revenue); }
}
