package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.Role;
import com.portfolio.erp.domain.ports.out.RoleRepositoryPort;
import com.portfolio.erp.infrastructure.out.mapper.RoleMapper;

@Repository
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository repository;
    private final RoleMapper mapper;

    public RoleRepositoryAdapter(RoleJpaRepository repository, RoleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Role> findAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Set<Role> findByNames(Collection<String> names) {
        return repository.findAllByNameIn(names).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }
}
