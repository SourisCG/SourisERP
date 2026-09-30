package com.portfolio.erp.domain.model;

import java.math.BigDecimal;

public record SalesSummaryItem(String period, long invoiceCount, BigDecimal subtotal,
                               BigDecimal taxTotal, BigDecimal total) {
}
