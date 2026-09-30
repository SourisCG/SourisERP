package com.portfolio.erp.domain.exception;

/**
 * Base class for domain exceptions. Carries a stable, machine-readable error
 * code that doubles as an i18n message key plus optional message arguments.
 */
public class DomainException extends RuntimeException {

    private final String code;
    private final transient Object[] args;

    public DomainException(String code, Object... args) {
        super(code);
        this.code = code;
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}
