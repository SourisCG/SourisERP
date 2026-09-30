package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.domain.model.PageResult;

public interface InvoiceRepositoryPort {

    PageResult<Invoice> search(String search, InvoiceStatus status, Long customerId, int page, int size);

    Optional<Invoice> findById(Long id);

    Invoice save(Invoice invoice);

    long nextInvoiceNumber();
}
