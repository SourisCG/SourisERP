package com.portfolio.erp.domain.ports.in;

import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.model.PageResult;

public interface AuditUseCase {

    PageResult<AuditEntry> list(String entityType, String action, int page, int size);
}
