package com.example.order_service.service.impl;

import com.example.order_service.dto.response.OrderItemResponse;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getItemsByOrderId(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy order: " + orderId
            );
        }

        return orderItemRepository.findAllByOrder_Id(orderId)
                .stream()
                .map(orderItemMapper::toDTO)
                .toList();
    }
}
