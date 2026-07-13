package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.dto.InviteUserRequest;
import com.expenseapi.Invoxa.dto.UserResponse;
import com.expenseapi.Invoxa.model.Tenant;
import com.expenseapi.Invoxa.model.User;
import com.expenseapi.Invoxa.repository.TenantRepository;
import com.expenseapi.Invoxa.repository.UserRepository;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse inviteUser(InviteUserRequest request, AuthenticatedUser currentUser) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        user = userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> getTeamMembers(AuthenticatedUser currentUser) {
        return userRepository.findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}