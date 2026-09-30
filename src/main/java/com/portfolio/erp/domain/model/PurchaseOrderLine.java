package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PurchaseOrderLine {

    private final Long id;
    private final Long productId;
    private final String sku;
    private final String productName;
    private final Long warehouseId;
    private final int quantity;
    private final BigDecimal unitCost;

    public PurchaseOrderLine(Long id, Long productId, String sku, String productName,
                             Long warehouseId, int quantity, BigDecimal unitCost) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public BigDecimal lineTotal() {
        return unitCost.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }
}
