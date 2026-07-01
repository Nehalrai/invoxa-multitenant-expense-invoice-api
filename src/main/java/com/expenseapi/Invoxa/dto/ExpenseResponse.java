package com.expenseapi.Invoxa.dto;

import com.expenseapi.Invoxa.model.ExpenseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private BigDecimal amount;
    private String category;
    private String description;
    private ExpenseStatus status;
    private UUID submittedById;
    private String submittedByEmail;
    private Instant createdAt;
    private Instant updatedAt;
}