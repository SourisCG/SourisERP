package com.portfolio.erp.domain.ports.in;

import java.util.List;

import com.portfolio.erp.domain.model.Category;

public interface CategoryUseCase {

    List<Category> list(String search);

    Category get(Long id);

    Category create(CategoryCommand command);

    Category update(Long id, CategoryCommand command);

    void delete(Long id);

    record CategoryCommand(String name, String description, Long parentId) {
    }
}
