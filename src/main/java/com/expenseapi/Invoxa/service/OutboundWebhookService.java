package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.model.Invoice;
import com.expenseapi.Invoxa.model.Tenant;
import com.expenseapi.Invoxa.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundWebhookService {

    private final TenantRepository tenantRepository;
    private final RestTemplate restTemplate;

    public void sendInvoicePaidWebhook(Invoice invoice) {
        Tenant tenant = tenantRepository.findById(invoice.getTenant().getId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (tenant.getWebhookUrl() == null || tenant.getWebhookUrl().isBlank()) {
            log.info("No webhook URL registered for tenant: {}", tenant.getId());
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "invoice.paid");
        payload.put("invoiceId", invoice.getId().toString());
        payload.put("invoiceNumber", invoice.getInvoiceNumber());
        payload.put("totalAmount", invoice.getTotalAmount());
        payload.put("clientName", invoice.getClient().getName());
        payload.put("paidAt", Instant.now().toString());
        payload.put("tenantId", tenant.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            log.info("Sending invoice.paid webhook to: {} for invoice: {}",
                    tenant.getWebhookUrl(), invoice.getInvoiceNumber());
            restTemplate.postForEntity(tenant.getWebhookUrl(), request, String.class);
            log.info("Webhook delivered successfully to: {}", tenant.getWebhookUrl());
        } catch (Exception e) {
            log.error("Webhook delivery failed for invoice: {}. Error: {}",
                    invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    public void registerWebhookUrl(UUID tenantId, String webhookUrl) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setWebhookUrl(webhookUrl);
        tenantRepository.save(tenant);

        log.info("Webhook URL registered for tenant: {}", tenantId);
    }
}