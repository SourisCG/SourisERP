package com.portfolio.erp.domain.exception;

/**
 * Thrown on business conflicts: duplicates, concurrent modifications and
 * insufficient stock. Mapped to HTTP 409.
 */
public class ConflictException extends DomainException {

    public ConflictException(String code, Object... args) {
        super(code, args);
    }
}
