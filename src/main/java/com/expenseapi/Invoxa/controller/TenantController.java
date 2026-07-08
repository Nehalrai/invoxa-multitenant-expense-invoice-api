package com.expenseapi.Invoxa.controller;

import com.expenseapi.Invoxa.dto.WebhookRegistrationRequest;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import com.expenseapi.Invoxa.service.OutboundWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final OutboundWebhookService outboundWebhookService;

    @PutMapping("/webhook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerWebhook(
            @Valid @RequestBody WebhookRegistrationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        outboundWebhookService.registerWebhookUrl(
                currentUser.getTenantId(),
                request.getWebhookUrl()
        );
        return ResponseEntity.ok("Webhook URL registered successfully");
    }
}