package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(UUID tenantId);
}