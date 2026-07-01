package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.model.AuditLog;
import com.expenseapi.Invoxa.repository.AuditLogRepository;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            AuthenticatedUser actor,
            String action,
            String entityType,
            UUID entityId,
            String metadata
    ) {
        AuditLog log = new AuditLog();
        log.setTenantId(actor.getTenantId());
        log.setActorUserId(actor.getUserId());
        log.setActorEmail(actor.getEmail());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadata(metadata);

        auditLogRepository.save(log);
    }
}