package com.portfolio.erp.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.AuthenticationFailedException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.domain.ports.in.AuthUseCase;
import com.portfolio.erp.domain.ports.out.PasswordHasherPort;
import com.portfolio.erp.domain.ports.out.TokenIssuerPort;
import com.portfolio.erp.domain.ports.out.UserRepositoryPort;

@Service
@Transactional(readOnly = true)
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuer;

    public AuthService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenIssuerPort tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public AuthResult login(String username, String password) {
        User user = users.findByUsername(username)
                .filter(User::isEnabled)
                .orElseThrow(() -> new AuthenticationFailedException("error.badCredentials"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("error.badCredentials");
        }

        TokenIssuerPort.IssuedToken token = tokenIssuer.issue(user);
        return new AuthResult(token.token(), token.expiresInSeconds(), user);
    }

    @Override
    public User currentUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.notFound"));
    }
}
