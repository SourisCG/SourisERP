package com.portfolio.erp.domain.ports.in;

import java.util.Set;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.User;

/**
 * Input port: user management use cases.
 */
public interface UserUseCase {

    PageResult<User> list(String search, int page, int size);

    User get(Long id);

    User create(CreateUserCommand command);

    User update(Long id, UpdateUserCommand command);

    void disable(Long id);

    User assignRoles(Long id, Set<String> roleNames);

    record CreateUserCommand(String username,
                             String email,
                             String firstName,
                             String lastName,
                             String password,
                             Set<String> roles,
                             boolean enabled) {
    }

    record UpdateUserCommand(String email, String firstName, String lastName, boolean enabled) {
    }
}
