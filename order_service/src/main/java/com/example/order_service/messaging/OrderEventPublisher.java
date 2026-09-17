package com.example.order_service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInventoryReserved(
            InventoryReservedEvent event
    ) {
        System.out.println("========== PUBLISH INVENTORY RESERVED ==========");
        System.out.println("Event = " + event);
        System.out.println(
                "Converter = "
                        + rabbitTemplate.getMessageConverter().getClass().getName()
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMessagingConfiguration.EXCHANGE,
                    "inventory.reserved",
                    event
            );

            System.out.println("========== PUBLISH SUCCESS ==========");

        } catch (Exception e) {
            System.out.println("========== PUBLISH FAILED ==========");
            e.printStackTrace();
            throw e;
        }
    }
}