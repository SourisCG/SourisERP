package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

/**
 * Output port: resolve the currently authenticated actor.
 */
public interface CurrentActorPort {

    Optional<Long> currentActorId();

    Optional<String> currentActorName();
}
