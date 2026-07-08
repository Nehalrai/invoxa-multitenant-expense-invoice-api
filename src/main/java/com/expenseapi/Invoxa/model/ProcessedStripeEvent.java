package com.expenseapi.Invoxa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "processed_stripe_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedStripeEvent {

    @Id
    private String id;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedStripeEvent(String id) {
        this.id = id;
        this.processedAt = Instant.now();
    }
}