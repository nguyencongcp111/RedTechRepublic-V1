package com.example.order_service.service;

import com.example.order_service.dto.response.OrderItemResponse;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {
    List<OrderItemResponse> getItemsByOrderId(UUID orderId);
}
