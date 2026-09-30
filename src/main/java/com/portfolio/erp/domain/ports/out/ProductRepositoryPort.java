package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;

public interface ProductRepositoryPort {

    PageResult<Product> search(String search, Long categoryId, Boolean active, int page, int size);

    Optional<Product> findById(Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    Product save(Product product);
}
