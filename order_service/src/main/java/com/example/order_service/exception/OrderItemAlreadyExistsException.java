package com.example.order_service.exception;

public class OrderItemAlreadyExistsException extends RuntimeException {
    public OrderItemAlreadyExistsException(String message) {
        super(message);
    }
}
