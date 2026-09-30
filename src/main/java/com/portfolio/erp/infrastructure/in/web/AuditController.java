package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.in.AuditUseCase;
import com.portfolio.erp.infrastructure.in.web.api.AuditApi;
import com.portfolio.erp.infrastructure.in.web.dto.AuditEntryResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PageAuditEntry;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class AuditController implements AuditApi {

    private final AuditUseCase auditUseCase;

    public AuditController(AuditUseCase auditUseCase) {
        this.auditUseCase = auditUseCase;
    }

    @Override
    public ResponseEntity<PageAuditEntry> listAuditEntries(Integer page, Integer size,
                                                           String entityType, String action) {
        PageResult<AuditEntry> result = auditUseCase.list(entityType, action,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageAuditEntry response = new PageAuditEntry();
        response.setContent(result.content().stream().map(this::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    private AuditEntryResponse toResponse(AuditEntry entry) {
        AuditEntryResponse response = new AuditEntryResponse();
        response.setId(entry.id());
        response.setEntityType(entry.entityType());
        response.setAction(entry.action());
        response.setEntityRef(entry.entityRef());
        response.setUsername(entry.username());
        response.setUserId(entry.userId());
        response.setAfterJson(entry.afterJson());
        response.setCreatedAt(entry.createdAt() == null ? null : entry.createdAt().atOffset(java.time.ZoneOffset.UTC));
        return response;
    }
}
