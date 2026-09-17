package com.example.payment_service.outbox;

import com.example.payment_service.messaging.RabbitMessagingConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPending() {

        for (OutboxEvent event :
                repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding("UTF-8");

            Message message = new Message(
                    event.getPayload().getBytes(StandardCharsets.UTF_8),
                    properties
            );

            rabbitTemplate.send(
                    RabbitMessagingConfiguration.EXCHANGE,
                    event.getRoutingKey(),
                    message
            );

            event.markPublished(Instant.now());
        }
    }
}