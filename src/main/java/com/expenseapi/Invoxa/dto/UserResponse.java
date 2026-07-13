package com.expenseapi.Invoxa.dto;

import com.expenseapi.Invoxa.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private Role role;
    private Instant createdAt;
}