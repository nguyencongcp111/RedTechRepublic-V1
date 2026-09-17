package com.example.order_service.messaging;

import com.example.order_service.service.OrderPaymentWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStatusListener {

    private final OrderPaymentWorkflowService workflowService;

    @RabbitListener(queues = RabbitMessagingConfiguration.PAYMENT_STATUS_QUEUE)
    public void onPaymentStatus(PaymentStatusEvent event) {
        workflowService.handle(event);
    }
}
