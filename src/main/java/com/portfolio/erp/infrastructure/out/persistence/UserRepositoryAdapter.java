package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.domain.ports.out.UserRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.UserEntity;
import com.portfolio.erp.infrastructure.out.mapper.UserMapper;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<User> findAll(String search, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<UserEntity> result = repository.search(normalized,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity;
        if (user.getId() == null) {
            entity = mapper.toEntity(user);
        } else {
            entity = repository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.user.notFound"));
            mapper.updateEntity(user, entity);
        }
        return mapper.toDomain(repository.save(entity));
    }
}
