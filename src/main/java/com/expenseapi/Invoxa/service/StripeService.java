package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.model.Invoice;
import com.expenseapi.Invoxa.model.InvoiceStatus;
import com.expenseapi.Invoxa.model.ProcessedStripeEvent;
import com.expenseapi.Invoxa.repository.InvoiceRepository;
import com.expenseapi.Invoxa.repository.ProcessedStripeEventRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final InvoiceRepository invoiceRepository;
    private final OutboundWebhookService outboundWebhookService;
    private final ProcessedStripeEventRepository processedStripeEventRepository;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public String createCheckoutSession(Invoice invoice) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/invoices/" + invoice.getId() + "?payment=success")
                    .setCancelUrl("http://localhost:8080/invoices/" + invoice.getId() + "?payment=cancelled")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(invoice.getTotalAmount()
                                                            .multiply(java.math.BigDecimal.valueOf(100))
                                                            .longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Invoice " + invoice.getInvoiceNumber())
                                                                    .setDescription("Payment for " + invoice.getClient().getName())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("invoiceId", invoice.getId().toString())
                    .putMetadata("tenantId", invoice.getTenant().getId().toString())
                    .build();

            Session session = Session.create(params);
            log.info("Created Stripe checkout session: {} for invoice: {}",
                    session.getId(), invoice.getInvoiceNumber());
            return session.getUrl();

        } catch (Exception e) {
            log.error("Failed to create Stripe checkout session for invoice: {}",
                    invoice.getInvoiceNumber(), e);
            throw new RuntimeException("Failed to create payment link", e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            throw new RuntimeException("Invalid signature", e);
        }

        if (processedStripeEventRepository.existsById(event.getId())) {
            log.info("Skipping already processed Stripe event: {}", event.getId());
            return;
        }

        try {
            if ("checkout.session.completed".equals(event.getType())) {

                Gson gson = new Gson();
                JsonObject eventJson = gson.fromJson(payload, JsonObject.class);
                JsonObject sessionData = eventJson
                        .getAsJsonObject("data")
                        .getAsJsonObject("object");

                JsonObject metadata = sessionData.getAsJsonObject("metadata");
                if (metadata == null || !metadata.has("invoiceId")) {
                    log.error("No invoiceId in metadata: {}", metadata);
                    throw new RuntimeException("No invoiceId in metadata");
                }

                String invoiceIdStr = metadata.get("invoiceId").getAsString();
                UUID invoiceId = UUID.fromString(invoiceIdStr);

                Invoice invoice = invoiceRepository.findById(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

                invoice.setStatus(InvoiceStatus.PAID);
                invoiceRepository.save(invoice);

                processedStripeEventRepository.save(new ProcessedStripeEvent(event.getId()));
                outboundWebhookService.sendInvoicePaidWebhook(invoice);

                log.info("Invoice {} marked as PAID via Stripe webhook event: {}",
                        invoice.getInvoiceNumber(), event.getId());
            }

        } catch (Exception e) {
            log.error("Failed to process Stripe event: {}", event.getId(), e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }
}