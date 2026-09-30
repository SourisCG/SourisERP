package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.Category;
import com.portfolio.erp.domain.ports.in.CategoryUseCase;
import com.portfolio.erp.infrastructure.in.web.api.CategoriesApi;
import com.portfolio.erp.infrastructure.in.web.dto.CategoryRequest;
import com.portfolio.erp.infrastructure.in.web.dto.CategoryResponse;
import com.portfolio.erp.infrastructure.out.mapper.CategoryMapper;

@RestController
public class CategoryController implements CategoriesApi {

    private final CategoryUseCase categoryUseCase;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryUseCase categoryUseCase, CategoryMapper categoryMapper) {
        this.categoryUseCase = categoryUseCase;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> listCategories(String search) {
        return ResponseEntity.ok(categoryUseCase.list(search).stream()
                .map(categoryMapper::toResponse)
                .toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        Category created = categoryUseCase.create(new CategoryUseCase.CategoryCommand(
                categoryRequest.getName(),
                categoryRequest.getDescription(),
                categoryRequest.getParentId()));
        return ResponseEntity.status(201).body(categoryMapper.toResponse(created));
    }

    @Override
    public ResponseEntity<CategoryResponse> getCategory(Long id) {
        return ResponseEntity.ok(categoryMapper.toResponse(categoryUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<CategoryResponse> updateCategory(Long id, CategoryRequest categoryRequest) {
        Category updated = categoryUseCase.update(id, new CategoryUseCase.CategoryCommand(
                categoryRequest.getName(),
                categoryRequest.getDescription(),
                categoryRequest.getParentId()));
        return ResponseEntity.ok(categoryMapper.toResponse(updated));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> deleteCategory(Long id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
