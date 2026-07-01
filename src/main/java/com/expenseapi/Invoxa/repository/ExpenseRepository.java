package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByTenantId(UUID tenantId);

    List<Expense> findByTenantIdAndSubmittedById(UUID tenantId, UUID userId);

    Optional<Expense> findByIdAndTenantId(UUID id, UUID tenantId);
}