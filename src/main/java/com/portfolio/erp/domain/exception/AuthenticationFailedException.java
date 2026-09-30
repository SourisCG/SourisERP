package com.portfolio.erp.domain.exception;

/**
 * Thrown when credentials are invalid. Mapped to HTTP 401.
 */
public class AuthenticationFailedException extends DomainException {

    public AuthenticationFailedException(String code, Object... args) {
        super(code, args);
    }
}
