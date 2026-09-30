package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Sales order line. Monetary math lives here so the order can aggregate its
 * totals without leaking calculation everywhere.
 */
public class SalesOrderLine {

    private final Long id;
    private final Long productId;
    private final String sku;
    private final String productName;
    private final Long warehouseId;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal discount;
    private final BigDecimal taxRate;

    public SalesOrderLine(Long id,
                          Long productId,
                          String sku,
                          String productName,
                          Long warehouseId,
                          int quantity,
                          BigDecimal unitPrice,
                          BigDecimal discount,
                          BigDecimal taxRate) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount == null ? BigDecimal.ZERO : discount;
        this.taxRate = taxRate;
    }

    public BigDecimal grossAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal discountAmount() {
        return grossAmount()
                .multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal lineTotal() {
        return grossAmount().subtract(discountAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal lineTax() {
        return lineTotal()
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
}
