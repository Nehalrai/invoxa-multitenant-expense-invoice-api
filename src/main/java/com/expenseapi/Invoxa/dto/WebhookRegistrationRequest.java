package com.expenseapi.Invoxa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookRegistrationRequest {

    @NotBlank(message = "Webhook URL is required")
    private String webhookUrl;
}