package com.portfolio.erp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.Category;
import com.portfolio.erp.domain.ports.in.CategoryUseCase;
import com.portfolio.erp.domain.ports.out.CategoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;

@Service
@Transactional(readOnly = true)
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categories;
    private final ProductRepositoryPort products;

    public CategoryService(CategoryRepositoryPort categories, ProductRepositoryPort products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    public List<Category> list(String search) {
        return categories.findAll(search);
    }

    @Override
    public Category get(Long id) {
        return requireCategory(id);
    }

    @Override
    @Transactional
    public Category create(CategoryCommand command) {
        if (categories.existsByName(command.name())) {
            throw new ConflictException("error.category.nameExists", command.name());
        }
        validateParent(command.parentId(), null);
        return categories.save(Category.newCategory(command.name(), command.description(), command.parentId()));
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryCommand command) {
        Category existing = requireCategory(id);
        if (categories.existsByNameAndIdNot(command.name(), id)) {
            throw new ConflictException("error.category.nameExists", command.name());
        }
        validateParent(command.parentId(), id);
        Category updated = new Category(existing.getId(), command.name(), command.description(),
                command.parentId(), existing.getCreatedAt(), existing.getUpdatedAt());
        return categories.save(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireCategory(id);
        if (categories.countByParentId(id) > 0) {
            throw new ConflictException("error.category.hasChildren");
        }
        if (products.existsByCategoryId(id)) {
            throw new ConflictException("error.category.hasProducts");
        }
        categories.delete(id);
    }

    private Category requireCategory(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.category.notFound"));
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw new DomainException("error.category.invalidParent");
        }
        if (categories.findById(parentId).isEmpty()) {
            throw new ResourceNotFoundException("error.category.notFound");
        }
    }
}
