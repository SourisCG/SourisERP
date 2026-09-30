package com.portfolio.erp.domain.model;

import java.time.Instant;

public record AuditEntry(Long id,
                         String entityType,
                         String action,
                         String entityRef,
                         String username,
                         Long userId,
                         String afterJson,
                         Instant createdAt) {
}
