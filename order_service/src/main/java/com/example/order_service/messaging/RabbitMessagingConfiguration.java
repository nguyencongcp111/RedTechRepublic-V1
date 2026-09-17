package com.example.order_service.messaging;

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
    public static final String PAYMENT_STATUS_QUEUE = "order.payment-status.v1";
    public static final String DEAD_LETTER_EXCHANGE = "ecommerce.dlx";
    public static final String PAYMENT_STATUS_DLQ = "order.payment-status.v1.dlq";

    @Bean
    TopicExchange ecommerceEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue paymentStatusQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(PAYMENT_STATUS_DLQ)
                .build();
    }

    @Bean
    Binding paymentStatusBinding(
            @Qualifier("paymentStatusQueue") Queue paymentStatusQueue,
            @Qualifier("ecommerceEventsExchange") TopicExchange ecommerceEventsExchange
    ) {
        return BindingBuilder.bind(paymentStatusQueue)
                .to(ecommerceEventsExchange)
                .with("payment.status.#");
    }

    @Bean
    TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue paymentStatusDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_DLQ).build();
    }

    @Bean
    Binding paymentStatusDeadLetterBinding(
            @Qualifier("paymentStatusDeadLetterQueue") Queue paymentStatusDeadLetterQueue,
            @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(paymentStatusDeadLetterQueue)
                .to(deadLetterExchange)
                .with(PAYMENT_STATUS_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(messageConverter);

        System.out.println(
                "========== RABBIT TEMPLATE =========="
        );
        System.out.println(
                "Converter: "
                        + rabbitTemplate.getMessageConverter()
                        .getClass()
                        .getName()
        );
        System.out.println(
                "====================================="
        );

        return rabbitTemplate;
    }
}
