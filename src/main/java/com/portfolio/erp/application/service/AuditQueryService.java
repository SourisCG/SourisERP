package com.portfolio.erp.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.in.AuditUseCase;
import com.portfolio.erp.domain.ports.out.AuditLogPort;

@Service
@Transactional(readOnly = true)
public class AuditQueryService implements AuditUseCase {

    private final AuditLogPort auditLog;

    public AuditQueryService(AuditLogPort auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public PageResult<AuditEntry> list(String entityType, String action, int page, int size) {
        return auditLog.search(entityType, action, page, size);
    }
}
