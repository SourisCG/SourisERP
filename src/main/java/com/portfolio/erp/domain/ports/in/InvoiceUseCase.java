package com.portfolio.erp.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.domain.model.PageResult;

public interface InvoiceUseCase {

    PageResult<Invoice> list(String search, InvoiceStatus status, Long customerId, int page, int size);

    Invoice get(Long id);

    Invoice createFromOrder(Long salesOrderId, LocalDate dueDate);

    Invoice registerPayment(Long id, BigDecimal amount, String method, String reference);
}
