package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.dto.CreateInvoiceRequest;
import com.expenseapi.Invoxa.dto.InvoiceResponse;
import com.expenseapi.Invoxa.dto.LineItemResponse;
import com.expenseapi.Invoxa.model.*;
import com.expenseapi.Invoxa.messaging.InvoiceJobPublisher;
import com.expenseapi.Invoxa.repository.ClientRepository;
import com.expenseapi.Invoxa.repository.InvoiceRepository;
import com.expenseapi.Invoxa.repository.TenantRepository;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final StripeService stripeService;
    private final OutboundWebhookService outboundWebhookService;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    private final InvoiceJobPublisher invoiceJobPublisher;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request, AuthenticatedUser currentUser) {
        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Client client = clientRepository.findByIdAndTenantId(request.getClientId(), currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Invoice invoice = new Invoice();
        invoice.setTenant(tenant);
        invoice.setClient(client);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemRequest : request.getLineItems()) {
            InvoiceLineItem lineItem = new InvoiceLineItem();
            lineItem.setInvoice(invoice);
            lineItem.setDescription(itemRequest.getDescription());
            lineItem.setQuantity(itemRequest.getQuantity());
            lineItem.setUnitPrice(itemRequest.getUnitPrice());
            BigDecimal amount = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            lineItem.setAmount(amount);
            invoice.getLineItems().add(lineItem);
            total = total.add(amount);
        }

        invoice.setTotalAmount(total);
        invoice = invoiceRepository.save(invoice);
        outboundWebhookService.sendInvoicePaidWebhook(invoice);

        auditService.log(currentUser, "INVOICE_CREATED", "INVOICE", invoice.getId(),
                "total=" + invoice.getTotalAmount() + ", client=" + client.getName());

        UUID invoiceId = invoice.getId();
        UUID tenantId = currentUser.getTenantId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invoiceJobPublisher.publishInvoiceCreated(invoiceId, tenantId);
            }
        });

        return toResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices(AuthenticatedUser currentUser) {
        return invoiceRepository.findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoice(UUID invoiceId, AuthenticatedUser currentUser) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markAsSent(UUID invoiceId, AuthenticatedUser currentUser) {
        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (!invoice.getTenant().getId().equals(currentUser.getTenantId())) {
            throw new IllegalArgumentException("Invoice not found");
        }

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT invoices can be marked as sent");
        }

        String paymentLink = stripeService.createCheckoutSession(invoice);
        invoice.setStripePaymentLink(paymentLink);
        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);

        auditService.log(currentUser, "INVOICE_SENT", "INVOICE", invoice.getId(),
                "stripe_payment_link=" + paymentLink);

        UUID tenantIdForMsg = currentUser.getTenantId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invoiceJobPublisher.publishInvoiceSent(invoiceId, tenantIdForMsg);
            }
        });

        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markAsPaid(UUID invoiceId, AuthenticatedUser currentUser, String source) {
        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        auditService.log(currentUser, "INVOICE_PAID", "INVOICE", invoice.getId(),
                "source=" + source);
        return toResponse(invoice);
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<LineItemResponse> lineItems = invoice.getLineItems().stream()
                .map(item -> new LineItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getAmount()
                ))
                .collect(Collectors.toList());

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTotalAmount(),
                invoice.getDueDate(),
                invoice.getNotes(),
                invoice.getStripePaymentLink(),
                invoice.getClient().getId(),
                invoice.getClient().getName(),
                lineItems,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}