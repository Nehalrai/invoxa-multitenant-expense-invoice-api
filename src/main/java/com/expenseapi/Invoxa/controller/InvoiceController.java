package com.expenseapi.Invoxa.controller;

import com.expenseapi.Invoxa.dto.CreateInvoiceRequest;
import com.expenseapi.Invoxa.dto.InvoiceResponse;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import com.expenseapi.Invoxa.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(invoiceService.createInvoice(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getOne(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(invoiceService.getInvoice(id, currentUser));
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<InvoiceResponse> send(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(invoiceService.markAsSent(id, currentUser));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> pay(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id, currentUser, "manual"));
    }
}