package com.expenseapi.Invoxa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {

    @NotBlank(message = "Client name is required")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String address;
}