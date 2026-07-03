package com.expenseapi.Invoxa.dto;

import com.expenseapi.Invoxa.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private InvoiceStatus status;
    private BigDecimal totalAmount;
    private LocalDate dueDate;
    private String notes;
    private String stripePaymentLink;
    private UUID clientId;
    private String clientName;
    private List<LineItemResponse> lineItems;
    private Instant createdAt;
    private Instant updatedAt;
}