package com.expenseapi.Invoxa.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceMessage{
    private UUID invoiceId;
    private UUID tenantId;
    private String eventType;
}