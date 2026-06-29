package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}