package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceLine {

    private final Long id;
    private final Long productId;
    private final String sku;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal taxRate;

    public InvoiceLine(Long id, Long productId, String sku, String productName,
                       int quantity, BigDecimal unitPrice, BigDecimal taxRate) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal lineTax() {
        return lineTotal().multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
}
