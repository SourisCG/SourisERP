package com.portfolio.erp.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.portfolio.erp.domain.model.Category;

public interface CategoryRepositoryPort {

    List<Category> findAll(String search);

    Optional<Category> findById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Category save(Category category);

    void delete(Long id);

    long countByParentId(Long parentId);
}
