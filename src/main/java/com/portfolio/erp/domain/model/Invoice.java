package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

/**
 * Invoice aggregate. Created from a confirmed sales order; payments are the
 * only allowed mutation and drive the status.
 */
public class Invoice {

    private final Long id;
    private final String number;
    private final Long salesOrderId;
    private final String salesOrderNumber;
    private final Long customerId;
    private final String customerName;
    private InvoiceStatus status;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final BigDecimal subtotal;
    private final BigDecimal taxTotal;
    private final BigDecimal total;
    private BigDecimal paidAmount;
    private final List<InvoiceLine> lines;
    private final List<InvoicePayment> payments;
    private final Instant createdAt;

    public Invoice(Long id,
                   String number,
                   Long salesOrderId,
                   String salesOrderNumber,
                   Long customerId,
                   String customerName,
                   InvoiceStatus status,
                   LocalDate issueDate,
                   LocalDate dueDate,
                   BigDecimal subtotal,
                   BigDecimal taxTotal,
                   BigDecimal total,
                   BigDecimal paidAmount,
                   List<InvoiceLine> lines,
                   List<InvoicePayment> payments,
                   Instant createdAt) {
        this.id = id;
        this.number = number;
        this.salesOrderId = salesOrderId;
        this.salesOrderNumber = salesOrderNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status == null ? InvoiceStatus.ISSUED : status;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
        this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        this.payments = payments == null ? new ArrayList<>() : new ArrayList<>(payments);
        this.createdAt = createdAt;
    }

    public static Invoice fromOrder(String number, SalesOrder order, LocalDate issueDate, LocalDate dueDate) {
        List<InvoiceLine> lines = order.getLines().stream()
                .map(line -> new InvoiceLine(null, line.getProductId(), line.getSku(), line.getProductName(),
                        line.getQuantity(), line.getUnitPrice(), line.getTaxRate()))
                .toList();
        return new Invoice(null, number, order.getId(), order.getNumber(), order.getCustomerId(),
                order.getCustomerName(), InvoiceStatus.ISSUED, issueDate, dueDate,
                order.subtotal(), order.taxTotal(), order.total(), BigDecimal.ZERO,
                lines, new ArrayList<>(), null);
    }

    public void addPayment(BigDecimal amount, String method, String reference) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("error.invoice.invalidPaymentAmount");
        }
        if (balance().signum() <= 0) {
            throw new ConflictException("error.invoice.alreadyPaid", number);
        }
        if (amount.compareTo(balance()) > 0) {
            throw new ConflictException("error.invoice.paymentExceedsBalance", balance());
        }
        payments.add(InvoicePayment.of(amount, method, reference));
        paidAmount = paidAmount.add(amount);
        status = paidAmount.compareTo(total) >= 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
    }

    public BigDecimal balance() {
        return total.subtract(paidAmount);
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public List<InvoicePayment> getPayments() {
        return payments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
