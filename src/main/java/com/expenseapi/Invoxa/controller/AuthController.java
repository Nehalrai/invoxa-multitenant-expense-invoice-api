package com.expenseapi.Invoxa.controller;

import com.expenseapi.Invoxa.dto.AuthResponse;
import com.expenseapi.Invoxa.dto.LoginRequest;
import com.expenseapi.Invoxa.dto.SignupRequest;
import com.expenseapi.Invoxa.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUser> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(user);
    }
}