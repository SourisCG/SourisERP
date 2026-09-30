package com.portfolio.erp.domain.ports.out;

/**
 * Output port: password hashing.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
