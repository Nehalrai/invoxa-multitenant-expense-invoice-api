package com.expenseapi.Invoxa.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private UUID userId;
    private UUID tenantId;
    private String email;
    private String role;
}