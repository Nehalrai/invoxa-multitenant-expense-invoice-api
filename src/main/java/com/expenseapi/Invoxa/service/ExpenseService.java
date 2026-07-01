package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.dto.CreateExpenseRequest;
import com.expenseapi.Invoxa.dto.ExpenseResponse;
import com.expenseapi.Invoxa.model.Expense;
import com.expenseapi.Invoxa.model.ExpenseStatus;
import com.expenseapi.Invoxa.model.Tenant;
import com.expenseapi.Invoxa.model.User;
import com.expenseapi.Invoxa.repository.ExpenseRepository;
import com.expenseapi.Invoxa.repository.UserRepository;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final AuditService auditService;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseResponse createExpense(CreateExpenseRequest request, AuthenticatedUser currentUser) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Expense expense = new Expense();
        expense.setTenant(user.getTenant());
        expense.setSubmittedBy(user);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());

        expense = expenseRepository.save(expense);
        auditService.log(currentUser, "EXPENSE_CREATED", "EXPENSE", expense.getId(),
                "amount=" + expense.getAmount() + ", category=" + expense.getCategory());

        return toResponse(expense);
    }

    public List<ExpenseResponse> getMyExpenses(AuthenticatedUser currentUser) {
        return expenseRepository
                .findByTenantIdAndSubmittedById(currentUser.getTenantId(), currentUser.getUserId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getAllTenantExpenses(AuthenticatedUser currentUser) {
        return expenseRepository
                .findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse approveExpense(UUID expenseId, AuthenticatedUser currentUser) {
        return updateStatus(expenseId, currentUser, ExpenseStatus.APPROVED);
    }

    public ExpenseResponse rejectExpense(UUID expenseId, AuthenticatedUser currentUser) {
        return updateStatus(expenseId, currentUser, ExpenseStatus.REJECTED);
    }

    private ExpenseResponse updateStatus(UUID expenseId, AuthenticatedUser currentUser, ExpenseStatus newStatus) {
        Expense expense = expenseRepository
                .findByIdAndTenantId(expenseId, currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        expense.setStatus(newStatus);
        expense = expenseRepository.save(expense);
        auditService.log(currentUser, "EXPENSE_" + newStatus.name(), "EXPENSE", expense.getId(),
                "previous_status=PENDING, new_status=" + newStatus.name());
        return toResponse(expense);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getStatus(),
                expense.getSubmittedBy().getId(),
                expense.getSubmittedBy().getEmail(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}