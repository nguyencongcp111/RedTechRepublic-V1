package com.example.order_service.service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.messaging.PaymentStatusEvent;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.dto.InventoryReservationResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentWorkflowService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PlatformTransactionManager transactionManager;

    public void handle(PaymentStatusEvent event) {
        switch (event.status()) {
            case "PENDING" -> markPaymentProcessing(event.orderId());
            case "SUCCEEDED" -> confirmOrder(event.orderId());
            case "FAILED" -> failOrder(event.orderId());
            case "REFUNDED" -> {
                // Refund does not imply a physical return, so stock is not changed automatically.
            }
            default -> throw new IllegalArgumentException("Payment status không được hỗ trợ: " + event.status());
        }
    }

    private void markPaymentProcessing(java.util.UUID orderId) {
        transactionTemplate().executeWithoutResult(status -> {
            Order order = findOrder(orderId);
            if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
                order.markPaymentProcessing();
            }
        });
    }

    private void confirmOrder(java.util.UUID orderId) {
        Order order = findOrderWithItems(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return;
        }
        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
            markPaymentProcessing(orderId);
            order = findOrderWithItems(orderId);
        }
        if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING) {
            throw new IllegalStateException("Order không thể confirm ở trạng thái " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            requireReservation(item);
            inventoryClient.transition(item.getInventoryReservationId(), "CONFIRMED");
        }

        transactionTemplate().executeWithoutResult(status -> findOrder(orderId).confirm());
    }

    private void failOrder(UUID orderId) {
        Order order = findOrderWithItems(orderId);

        if (order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        List<InventoryReservationResponse> reservations =
                inventoryClient.getReservationsByOrder(orderId);

        for (OrderItem item : order.getItems()) {
            requireReservation(item);

            InventoryReservationResponse reservation = reservations.stream()
                    .filter(r -> r.id().equals(item.getInventoryReservationId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy inventory reservation: "
                                    + item.getInventoryReservationId()
                    ));

            switch (reservation.status()) {

                case "ACTIVE" -> inventoryClient.transition(
                        reservation.id(),
                        "RELEASED"
                );

                case "EXPIRED", "RELEASED" -> {
                    // Reservation đã được giải phóng.
                }

                case "CONFIRMED" -> throw new IllegalStateException(
                        "Không thể fail order vì reservation đã CONFIRMED: "
                                + reservation.id()
                );

                default -> throw new IllegalStateException(
                        "Reservation status không hỗ trợ: "
                                + reservation.status()
                );
            }
        }

        transactionTemplate().executeWithoutResult(status ->
                findOrder(orderId).fail("Thanh toán thất bại")
        );
    }

    private void requireReservation(OrderItem item) {
        if (item.getInventoryReservationId() == null) {
            throw new IllegalStateException("Order item chưa có inventory reservation: " + item.getId());
        }
    }

    private Order findOrder(java.util.UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + orderId));
    }

    private Order findOrderWithItems(java.util.UUID orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + orderId));
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
}
