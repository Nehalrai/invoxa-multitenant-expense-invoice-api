package com.expenseapi.Invoxa.repository;

import com.expenseapi.Invoxa.model.ProcessedStripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEvent, String> {
}