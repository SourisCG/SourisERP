package com.portfolio.erp.domain.ports.in;

import java.math.BigDecimal;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.UnitOfMeasure;

public interface ProductUseCase {

    PageResult<Product> list(String search, Long categoryId, Boolean active, int page, int size);

    Product get(Long id);

    Product create(ProductCommand command);

    Product update(Long id, ProductCommand command);

    void deactivate(Long id);

    record ProductCommand(String sku,
                          String name,
                          String description,
                          Long categoryId,
                          UnitOfMeasure unit,
                          BigDecimal salePrice,
                          BigDecimal costPrice,
                          BigDecimal taxRate,
                          boolean active) {
    }
}
