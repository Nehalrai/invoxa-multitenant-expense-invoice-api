package com.expenseapi.Invoxa.controller;

import com.expenseapi.Invoxa.dto.InviteUserRequest;
import com.expenseapi.Invoxa.dto.UserResponse;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import com.expenseapi.Invoxa.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> inviteUser(
            @Valid @RequestBody InviteUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(userService.inviteUser(request, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getTeamMembers(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(userService.getTeamMembers(currentUser));
    }
}