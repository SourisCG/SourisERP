package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoicePayment(Long id, BigDecimal amount, String method, String reference, Instant paidAt) {

    public static InvoicePayment of(BigDecimal amount, String method, String reference) {
        return new InvoicePayment(null, amount, method, reference, Instant.now());
    }
}
