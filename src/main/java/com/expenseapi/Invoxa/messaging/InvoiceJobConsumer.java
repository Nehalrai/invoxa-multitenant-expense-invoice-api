package com.expenseapi.Invoxa.messaging;

import com.expenseapi.Invoxa.model.Invoice;
import com.expenseapi.Invoxa.repository.InvoiceRepository;
import com.expenseapi.Invoxa.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j

public class InvoiceJobConsumer {

    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;

    @Value("${invoxa.rabbitmq.queues.invoice-jobs}")
    private String invoiceJobsQueue;

    @RabbitListener(queues = "${invoxa.rabbitmq.queues.invoice-jobs}")
    public void handleInvoiceJob(InvoiceMessage message) {
        log.info("Received {} event for invoice: {}", message.getEventType(), message.getInvoiceId());

        try {
            Invoice invoice = invoiceRepository.findByIdWithDetails(message.getInvoiceId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found: " + message.getInvoiceId()));

            switch (message.getEventType()) {
                case "INVOICE_CREATED" -> handleInvoiceCreated(invoice);
                case "INVOICE_SENT" -> handleInvoiceSent(invoice);
                default -> log.warn("Unknown event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process {} event for invoice: {}",
                    message.getEventType(), message.getInvoiceId(), e);
        }
    }
    @Transactional
    private void handleInvoiceCreated(Invoice invoice) {
        log.info("Processing INVOICE_CREATED for: {}", invoice.getInvoiceNumber());
        String pdfPath = pdfGenerationService.generateInvoicePdf(invoice);
        log.info("PDF generated at: {}", pdfPath);
    }

    private void handleInvoiceSent(Invoice invoice) {
        log.info("Processing INVOICE_SENT for: {}", invoice.getInvoiceNumber());
        String pdfPath = pdfGenerationService.generateInvoicePdf(invoice);
        log.info("PDF generated at: {} — would now send email to: {}",
                pdfPath, invoice.getClient().getEmail());
        simulateEmailSend(invoice);
    }

    private void simulateEmailSend(Invoice invoice) {
        log.info("📧 [SIMULATED EMAIL] To: {} | Subject: Invoice {} | Amount: {}",
                invoice.getClient().getEmail(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount());
    }
}