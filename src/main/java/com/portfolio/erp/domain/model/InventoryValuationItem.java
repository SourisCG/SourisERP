package com.portfolio.erp.domain.model;

import java.math.BigDecimal;

public record InventoryValuationItem(Long warehouseId, String warehouseCode, long totalQuantity,
                                     BigDecimal totalCost, BigDecimal totalRetail) {
}
