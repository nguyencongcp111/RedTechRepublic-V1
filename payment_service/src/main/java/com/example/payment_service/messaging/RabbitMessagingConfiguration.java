package com.example.payment_service.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMessagingConfiguration {

    public static final String EXCHANGE = "ecommerce.events";

    public static final String INVENTORY_RESERVED_QUEUE =
            "payment.inventory-reserved.v1";

    @Bean
    TopicExchange ecommerceEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue inventoryReservedQueue() {
        return QueueBuilder
                .durable(INVENTORY_RESERVED_QUEUE)
                .build();
    }

    @Bean
    Binding inventoryReservedBinding(
            @Qualifier("inventoryReservedQueue") Queue inventoryReservedQueue,
            @Qualifier("ecommerceEventsExchange") TopicExchange ecommerceEventsExchange
    ) {
        return BindingBuilder
                .bind(inventoryReservedQueue)
                .to(ecommerceEventsExchange)
                .with("inventory.reserved");
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new JacksonJsonMessageConverter((JsonMapper) objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }
}