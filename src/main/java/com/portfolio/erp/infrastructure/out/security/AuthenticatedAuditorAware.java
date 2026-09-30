package com.portfolio.erp.infrastructure.out.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.portfolio.erp.domain.ports.out.CurrentActorPort;

/**
 * Feeds JPA auditing columns (created_by / updated_by) with the authenticated
 * actor id taken from the JWT subject.
 */
@Component("auditorAware")
public class AuthenticatedAuditorAware implements AuditorAware<Long> {

    private final CurrentActorPort currentActor;

    public AuthenticatedAuditorAware(CurrentActorPort currentActor) {
        this.currentActor = currentActor;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        return currentActor.currentActorId();
    }
}
