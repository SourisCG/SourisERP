package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.portfolio.erp.domain.exception.ConflictException;

/**
 * Sales order aggregate. Status transitions are guarded here; stock
 * reservation is orchestrated by the application service.
 */
public class SalesOrder {

    private final Long id;
    private final String number;
    private final Long customerId;
    private final String customerName;
    private SalesOrderStatus status;
    private LocalDate orderDate;
    private String notes;
    private List<SalesOrderLine> lines;
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public SalesOrder(Long id,
                      String number,
                      Long customerId,
                      String customerName,
                      SalesOrderStatus status,
                      LocalDate orderDate,
                      String notes,
                      List<SalesOrderLine> lines,
                      Long version,
                      Instant createdAt,
                      Instant updatedAt) {
        this.id = id;
        this.number = number;
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.customerName = customerName;
        this.status = status == null ? SalesOrderStatus.DRAFT : status;
        this.orderDate = orderDate == null ? LocalDate.now() : orderDate;
        this.notes = notes;
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SalesOrder draft(String number, Long customerId, LocalDate orderDate, String notes,
                                   List<SalesOrderLine> lines) {
        return new SalesOrder(null, number, customerId, null, SalesOrderStatus.DRAFT, orderDate, notes, lines, null, null, null);
    }

    public void replaceLines(List<SalesOrderLine> newLines) {
        requireDraft("edit");
        if (newLines == null || newLines.isEmpty()) {
            throw new ConflictException("error.salesOrder.noLines");
        }
        this.lines = new ArrayList<>(newLines);
    }

    public void updateHeader(LocalDate orderDate, String notes) {
        requireDraft("edit");
        if (orderDate != null) {
            this.orderDate = orderDate;
        }
        this.notes = notes;
    }

    public void confirm() {
        requireDraft("confirm");
        if (lines.isEmpty()) {
            throw new ConflictException("error.salesOrder.noLines");
        }
        this.status = SalesOrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == SalesOrderStatus.INVOICED) {
            throw new ConflictException("error.salesOrder.invalidStatus", "cancel", status);
        }
        if (status == SalesOrderStatus.CANCELLED) {
            throw new ConflictException("error.salesOrder.invalidStatus", "cancel", status);
        }
        this.status = SalesOrderStatus.CANCELLED;
    }

    public void markInvoiced() {
        if (status != SalesOrderStatus.CONFIRMED) {
            throw new ConflictException("error.salesOrder.invalidStatus", "invoice", status);
        }
        this.status = SalesOrderStatus.INVOICED;
    }

    private void requireDraft(String action) {
        if (status != SalesOrderStatus.DRAFT) {
            throw new ConflictException("error.salesOrder.invalidStatus", action, status);
        }
    }

    public BigDecimal subtotal() {
        return lines.stream().map(SalesOrderLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal taxTotal() {
        return lines.stream().map(SalesOrderLine::lineTax).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal total() {
        return subtotal().add(taxTotal());
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getNotes() {
        return notes;
    }

    public List<SalesOrderLine> getLines() {
        return lines;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
