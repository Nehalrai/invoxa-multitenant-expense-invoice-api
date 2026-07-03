package com.expenseapi.Invoxa.controller;

import com.expenseapi.Invoxa.dto.ClientResponse;
import com.expenseapi.Invoxa.dto.CreateClientRequest;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import com.expenseapi.Invoxa.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('ACCOUNTANT')")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody CreateClientRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(clientService.createClient(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(clientService.getAllClients(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getOne(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(clientService.getClient(id, currentUser));
    }
}