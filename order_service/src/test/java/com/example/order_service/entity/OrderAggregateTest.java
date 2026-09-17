package com.example.order_service.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderAggregateTest {
    @Test
    void totalsFollowItems() {
        Order order = Order.builder().totalAmount(0).totalQuantity(0).build();
        OrderItem first = OrderItem.builder().quantity(2).lineTotal(300).build();
        OrderItem second = OrderItem.builder().quantity(1).lineTotal(250).build();

        order.addItem(first);
        order.addItem(second);
        assertEquals(3, order.getTotalQuantity());
        assertEquals(550, order.getTotalAmount());

        order.removeItem(first);
        assertEquals(1, order.getTotalQuantity());
        assertEquals(250, order.getTotalAmount());
    }
}
