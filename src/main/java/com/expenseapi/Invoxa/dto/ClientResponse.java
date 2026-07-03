package com.expenseapi.Invoxa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClientResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Instant createdAt;
}