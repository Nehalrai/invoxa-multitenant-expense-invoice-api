package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByTenantId(UUID tenantId);
    Optional<Client> findByIdAndTenantId(UUID id, UUID tenantId);
}