package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.AuditLogEntity;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {

    @Query("""
            select a from AuditLogEntity a
            where (:entityType is null or a.entityType = :entityType)
              and (:action is null or a.action = :action)
            """)
    Page<AuditLogEntity> search(@Param("entityType") String entityType,
                                @Param("action") String action,
                                Pageable pageable);
}
