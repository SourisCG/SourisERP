package com.portfolio.erp.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.audit.Audited;
import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.domain.ports.in.InvoiceUseCase;
import com.portfolio.erp.domain.ports.in.SalesOrderUseCase;
import com.portfolio.erp.domain.ports.out.InvoiceRepositoryPort;
import com.portfolio.erp.domain.ports.out.SalesOrderRepositoryPort;

@Service
@Transactional(readOnly = true)
public class InvoiceService implements InvoiceUseCase {

    private final InvoiceRepositoryPort invoices;
    private final SalesOrderRepositoryPort salesOrders;
    private final SalesOrderUseCase salesOrderUseCase;

    public InvoiceService(InvoiceRepositoryPort invoices,
                          SalesOrderRepositoryPort salesOrders,
                          SalesOrderUseCase salesOrderUseCase) {
        this.invoices = invoices;
        this.salesOrders = salesOrders;
        this.salesOrderUseCase = salesOrderUseCase;
    }

    @Override
    public PageResult<Invoice> list(String search, InvoiceStatus status, Long customerId, int page, int size) {
        return invoices.search(search, status, customerId, page, size);
    }

    @Override
    public Invoice get(Long id) {
        return requireInvoice(id);
    }

    @Override
    @Transactional
    @Audited(action = "INVOICE_CREATE", entityType = "Invoice")
    public Invoice createFromOrder(Long salesOrderId, LocalDate dueDate) {
        SalesOrder order = salesOrders.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("error.salesOrder.notFound"));
        if (order.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new ConflictException("error.invoice.orderNotConfirmed");
        }

        LocalDate issueDate = LocalDate.now();
        Invoice invoice = Invoice.fromOrder(nextNumber(), order, issueDate,
                dueDate != null ? dueDate : issueDate.plusDays(30));
        Invoice saved = invoices.save(invoice);

        salesOrderUseCase.markInvoiced(order.getId());
        return saved;
    }

    @Override
    @Transactional
    @Audited(action = "INVOICE_PAYMENT", entityType = "Invoice")
    public Invoice registerPayment(Long id, BigDecimal amount, String method, String reference) {
        Invoice invoice = requireInvoice(id);
        invoice.addPayment(amount, method, reference);
        return invoices.save(invoice);
    }

    private Invoice requireInvoice(Long id) {
        return invoices.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.invoice.notFound"));
    }

    private String nextNumber() {
        return "INV-%d-%05d".formatted(LocalDate.now().getYear(), invoices.nextInvoiceNumber());
    }
}
