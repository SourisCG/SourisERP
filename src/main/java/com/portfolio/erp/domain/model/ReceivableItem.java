package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceivableItem(Long invoiceId, String invoiceNumber, Long customerId, String customerName,
                             LocalDate dueDate, BigDecimal total, BigDecimal paidAmount,
                             BigDecimal balance, int daysOverdue) {
}
