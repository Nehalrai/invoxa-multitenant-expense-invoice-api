package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.dto.AuthResponse;
import com.expenseapi.Invoxa.dto.LoginRequest;
import com.expenseapi.Invoxa.dto.SignupRequest;
import com.expenseapi.Invoxa.model.Role;
import com.expenseapi.Invoxa.model.Tenant;
import com.expenseapi.Invoxa.model.User;
import com.expenseapi.Invoxa.repository.TenantRepository;
import com.expenseapi.Invoxa.repository.UserRepository;
import com.expenseapi.Invoxa.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getCompanyName());
        tenant = tenantRepository.save(tenant);

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                tenant.getId(),
                user.getRole().name()
        );

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name()
        );

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}