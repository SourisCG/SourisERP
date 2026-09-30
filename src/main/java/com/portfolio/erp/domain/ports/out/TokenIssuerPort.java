package com.portfolio.erp.domain.ports.out;

import com.portfolio.erp.domain.model.User;

/**
 * Output port: issue access tokens.
 */
public interface TokenIssuerPort {

    IssuedToken issue(User user);

    record IssuedToken(String token, long expiresInSeconds) {
    }
}
