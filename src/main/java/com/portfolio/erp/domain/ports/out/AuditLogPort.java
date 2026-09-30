package com.portfolio.erp.domain.ports.out;

import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.model.PageResult;

public interface AuditLogPort {

    void record(AuditEntry entry);

    PageResult<AuditEntry> search(String entityType, String action, int page, int size);
}
