package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByTenantId(UUID tenantId);

    Optional<Invoice> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.client JOIN FETCH i.lineItems WHERE i.id = :id")
    Optional<Invoice> findByIdWithDetails(@Param("id") UUID id);
}