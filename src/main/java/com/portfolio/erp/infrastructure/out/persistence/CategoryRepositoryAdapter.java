package com.portfolio.erp.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.Category;
import com.portfolio.erp.domain.ports.out.CategoryRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.CategoryEntity;
import com.portfolio.erp.infrastructure.out.mapper.CategoryMapper;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository repository;
    private final CategoryMapper mapper;

    public CategoryRepositoryAdapter(CategoryJpaRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Category> findAll(String search) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        return repository.search(normalized).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return repository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity;
        if (category.getId() == null) {
            entity = mapper.toEntity(category);
        } else {
            entity = repository.findById(category.getId()).orElseThrow();
            mapper.updateEntity(category, entity);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public long countByParentId(Long parentId) {
        return repository.countByParentId(parentId);
    }
}
