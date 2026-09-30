package com.portfolio.erp.domain.exception;

/**
 * Thrown when a requested resource does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String code, Object... args) {
        super(code, args);
    }
}
