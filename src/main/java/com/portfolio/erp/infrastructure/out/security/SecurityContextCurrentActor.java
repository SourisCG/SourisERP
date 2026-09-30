package com.portfolio.erp.infrastructure.out.security;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.portfolio.erp.domain.ports.out.CurrentActorPort;

@Component
public class SecurityContextCurrentActor implements CurrentActorPort {

    @Override
    public Optional<Long> currentActorId() {
        return currentAuthentication()
                .map(Authentication::getName)
                .flatMap(this::parseLong);
    }

    @Override
    public Optional<String> currentActorName() {
        return currentAuthentication().map(authentication -> {
            if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                String username = jwtAuthentication.getToken().getClaimAsString("username");
                return username != null ? username : authentication.getName();
            }
            return authentication.getName();
        });
    }

    private Optional<Authentication> currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    private Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
