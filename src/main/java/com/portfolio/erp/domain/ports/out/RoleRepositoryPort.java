package com.portfolio.erp.domain.ports.out;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.portfolio.erp.domain.model.Role;

/**
 * Output port: role persistence.
 */
public interface RoleRepositoryPort {

    List<Role> findAll();

    Set<Role> findByNames(Collection<String> names);
}
