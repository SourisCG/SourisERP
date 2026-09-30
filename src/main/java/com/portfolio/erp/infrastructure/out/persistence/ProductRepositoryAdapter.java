package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.CategoryEntity;
import com.portfolio.erp.infrastructure.out.entity.ProductEntity;
import com.portfolio.erp.infrastructure.out.mapper.ProductMapper;

@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private final CategoryJpaRepository categoryRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository repository,
                                    CategoryJpaRepository categoryRepository,
                                    ProductMapper mapper) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Product> search(String search, Long categoryId, Boolean active, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<ProductEntity> result = repository.search(normalized, categoryId, active,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public boolean existsBySkuAndIdNot(String sku, Long id) {
        return repository.existsBySkuAndIdNot(sku, id);
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return repository.existsByCategoryId(categoryId);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity;
        if (product.getId() == null) {
            entity = mapper.toEntity(product);
        } else {
            entity = repository.findById(product.getId()).orElseThrow();
            mapper.updateEntity(product, entity);
        }
        if (entity.getCategory() == null || !entity.getCategory().getId().equals(product.getCategoryId())) {
            CategoryEntity category = categoryRepository.getReferenceById(product.getCategoryId());
            entity.setCategory(category);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
