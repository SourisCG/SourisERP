package com.portfolio.erp.domain.ports.in;

import com.portfolio.erp.domain.model.User;

/**
 * Input port: authentication use cases.
 */
public interface AuthUseCase {

    AuthResult login(String username, String password);

    User currentUser(Long userId);

    record AuthResult(String accessToken, long expiresInSeconds, User user) {
    }
}
