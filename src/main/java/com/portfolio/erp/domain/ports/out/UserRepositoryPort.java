package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.User;

/**
 * Output port: user persistence.
 */
public interface UserRepositoryPort {

    PageResult<User> findAll(String search, int page, int size);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);
}
