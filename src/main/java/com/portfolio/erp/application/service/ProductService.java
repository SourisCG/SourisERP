package com.portfolio.erp.application.service;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.audit.Audited;
import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.ports.in.ProductUseCase;
import com.portfolio.erp.domain.ports.out.CategoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;

@Service
@Transactional(readOnly = true)
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort products;
    private final CategoryRepositoryPort categories;

    public ProductService(ProductRepositoryPort products, CategoryRepositoryPort categories) {
        this.products = products;
        this.categories = categories;
    }

    @Override
    public PageResult<Product> list(String search, Long categoryId, Boolean active, int page, int size) {
        return products.search(search, categoryId, active, page, size);
    }

    @Override
    public Product get(Long id) {
        return requireProduct(id);
    }

    @Override
    @Transactional
    @Audited(action = "PRODUCT_CREATE", entityType = "Product")
    public Product create(ProductCommand command) {
        validateCategory(command.categoryId());
        if (products.existsBySku(command.sku())) {
            throw new ConflictException("error.product.skuExists", command.sku());
        }
        Product product = Product.newProduct(
                command.sku(),
                command.name(),
                command.description(),
                command.categoryId(),
                command.unit(),
                command.salePrice(),
                command.costPrice(),
                defaultTaxRate(command.taxRate()),
                command.active());
        return products.save(product);
    }

    @Override
    @Transactional
    @Audited(action = "PRODUCT_UPDATE", entityType = "Product")
    public Product update(Long id, ProductCommand command) {
        Product product = requireProduct(id);
        validateCategory(command.categoryId());
        if (!product.getSku().equalsIgnoreCase(command.sku()) && products.existsBySku(command.sku())) {
            throw new ConflictException("error.product.skuExists", command.sku());
        }
        product.updateDetails(command.sku(), command.name(), command.description(),
                command.unit(), defaultTaxRate(command.taxRate()));
        product.changeCategory(command.categoryId());
        product.changePrices(command.salePrice(), command.costPrice());
        product.setActive(command.active());
        return products.save(product);
    }

    @Override
    @Transactional
    @Audited(action = "PRODUCT_DEACTIVATE", entityType = "Product")
    public void deactivate(Long id) {
        Product product = requireProduct(id);
        product.setActive(false);
        products.save(product);
    }

    private Product requireProduct(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.product.notFound"));
    }

    private void validateCategory(Long categoryId) {
        if (categories.findById(categoryId).isEmpty()) {
            throw new ResourceNotFoundException("error.product.categoryNotFound", categoryId);
        }
    }

    private BigDecimal defaultTaxRate(BigDecimal taxRate) {
        return Objects.requireNonNullElse(taxRate, new BigDecimal("21.00"));
    }
}
