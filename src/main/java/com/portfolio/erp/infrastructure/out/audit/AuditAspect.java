package com.portfolio.erp.infrastructure.out.audit;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.portfolio.erp.domain.audit.Audited;
import com.portfolio.erp.domain.model.AuditEntry;
import com.portfolio.erp.domain.ports.out.AuditLogPort;
import com.portfolio.erp.domain.ports.out.CurrentActorPort;

import tools.jackson.databind.ObjectMapper;

/**
 * Records audit trail entries for methods annotated with {@link Audited}.
 * Runs outside the business transaction: if the operation fails, nothing is
 * recorded; if it commits, the resulting state snapshot is stored as JSONB.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogPort auditLog;
    private final CurrentActorPort currentActor;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogPort auditLog, CurrentActorPort currentActor, ObjectMapper objectMapper) {
        this.auditLog = auditLog;
        this.currentActor = currentActor;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            String afterJson = result == null ? null : objectMapper.writeValueAsString(result);
            Long entityId = extractId(result);
            auditLog.record(new AuditEntry(
                    null,
                    audited.entityType(),
                    audited.action(),
                    entityId == null ? describeArgs(joinPoint) : String.valueOf(entityId),
                    currentActor.currentActorName().orElse("system"),
                    currentActor.currentActorId().orElse(null),
                    afterJson,
                    Instant.now()));
        } catch (Exception ex) {
            log.warn("Could not record audit entry for {}: {}", audited.action(), ex.getMessage());
        }
        return result;
    }

    private Long extractId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Method getId = result.getClass().getMethod("getId");
            Object value = getId.invoke(result);
            return value instanceof Long id ? id : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String describeArgs(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof Number || arg instanceof String)
                .map(String::valueOf)
                .findFirst()
                .orElse(null);
    }
}
