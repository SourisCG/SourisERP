package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.out.AuditLogPort;
import com.portfolio.erp.infrastructure.out.entity.AuditLogEntity;

@Repository
public class AuditLogAdapter implements AuditLogPort {

    private final AuditLogJpaRepository repository;

    public AuditLogAdapter(AuditLogJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void record(AuditEntry entry) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setEntityType(entry.entityType());
        entity.setAction(entry.action());
        entity.setEntityRef(entry.entityRef());
        entity.setUsername(entry.username());
        entity.setUserId(entry.userId());
        entity.setAfterJson(entry.afterJson());
        entity.setCreatedAt(entry.createdAt());
        repository.save(entity);
    }

    @Override
    public PageResult<AuditEntry> search(String entityType, String action, int page, int size) {
        String normalizedType = (entityType == null || entityType.isBlank()) ? null : entityType.trim();
        String normalizedAction = (action == null || action.isBlank()) ? null : action.trim();
        Page<AuditLogEntity> result = repository.search(normalizedType, normalizedAction,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(
                result.getContent().stream()
                        .map(entity -> new AuditEntry(entity.getId(), entity.getEntityType(), entity.getAction(),
                                entity.getEntityRef(), entity.getUsername(), entity.getUserId(),
                                entity.getAfterJson(), entity.getCreatedAt()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }
}
