package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Product aggregate. Prices and tax rate change through intention-revealing
 * methods; the version field supports optimistic locking.
 */
public class Product {

    private final Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private UnitOfMeasure unit;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private BigDecimal taxRate;
    private boolean active;
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Product(Long id,
                   String sku,
                   String name,
                   String description,
                   Long categoryId,
                   String categoryName,
                   UnitOfMeasure unit,
                   BigDecimal salePrice,
                   BigDecimal costPrice,
                   BigDecimal taxRate,
                   boolean active,
                   Long version,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
        this.categoryName = categoryName;
        this.unit = Objects.requireNonNull(unit, "unit");
        this.salePrice = Objects.requireNonNull(salePrice, "salePrice");
        this.costPrice = Objects.requireNonNull(costPrice, "costPrice");
        this.taxRate = Objects.requireNonNull(taxRate, "taxRate");
        this.active = active;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product newProduct(String sku, String name, String description, Long categoryId,
                                     UnitOfMeasure unit, BigDecimal salePrice, BigDecimal costPrice,
                                     BigDecimal taxRate, boolean active) {
        return new Product(null, sku, name, description, categoryId, null, unit,
                salePrice, costPrice, taxRate, active, null, null, null);
    }

    public void updateDetails(String sku, String name, String description, UnitOfMeasure unit, BigDecimal taxRate) {
        this.sku = Objects.requireNonNull(sku, "sku");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.unit = Objects.requireNonNull(unit, "unit");
        this.taxRate = Objects.requireNonNull(taxRate, "taxRate");
    }

    public void changePrices(BigDecimal salePrice, BigDecimal costPrice) {
        this.salePrice = Objects.requireNonNull(salePrice, "salePrice");
        this.costPrice = Objects.requireNonNull(costPrice, "costPrice");
    }

    public void changeCategory(Long categoryId) {
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public UnitOfMeasure getUnit() {
        return unit;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public boolean isActive() {
        return active;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
