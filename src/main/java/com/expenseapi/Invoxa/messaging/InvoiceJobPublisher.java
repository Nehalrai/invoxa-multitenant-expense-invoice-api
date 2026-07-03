package com.expenseapi.Invoxa.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceJobPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${invoxa.rabbitmq.queues.invoice-jobs}")
    private String invoiceJobsQueue;

    public void publishInvoiceCreated(UUID invoiceId, UUID tenantId) {
        InvoiceMessage message = new InvoiceMessage(invoiceId, tenantId, "INVOICE_CREATED");
        rabbitTemplate.convertAndSend(invoiceJobsQueue, message);
        log.info("Published INVOICE_CREATED event for invoice: {}", invoiceId);
    }

    public void publishInvoiceSent(UUID invoiceId, UUID tenantId) {
        InvoiceMessage message = new InvoiceMessage(invoiceId, tenantId, "INVOICE_SENT");
        rabbitTemplate.convertAndSend(invoiceJobsQueue, message);
        log.info("Published INVOICE_SENT event for invoice: {}", invoiceId);
    }
}