package com.example.payment_service.messaging;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryReservedConsumer {

    private final PaymentService paymentService;

    @RabbitListener(
            queues = RabbitMessagingConfiguration.INVENTORY_RESERVED_QUEUE
    )
    public void handleInventoryReserved(
            InventoryReservedEvent event
    ) {

        System.out.println(
                "PAYMENT SERVICE RECEIVED InventoryReservedEvent"
        );

        System.out.println("orderId = " + event.orderId());
        System.out.println("userId = " + event.userId());
        System.out.println("totalAmount = " + event.totalAmount());
        System.out.println("currency = " + event.currency());
        System.out.println("paymentMethod = " + event.paymentMethod());

        CreatePaymentRequest request = new CreatePaymentRequest(
                event.orderId(),
                event.totalAmount(),
                event.currency(),
                event.paymentMethod()
        );

        String idempotencyKey =
                "order-" + event.orderId();

        paymentService.createPayment(
                idempotencyKey,
                request
        );

        System.out.println(
                "PAYMENT CREATED: orderId=" + event.orderId()
        );
    }
}