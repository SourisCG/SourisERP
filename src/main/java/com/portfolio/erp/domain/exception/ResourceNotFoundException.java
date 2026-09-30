package com.portfolio.erp.domain.exception;

/**
 * Thrown when a requested resource does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String code, Object... args) {
        super(code, args);
    }
}
