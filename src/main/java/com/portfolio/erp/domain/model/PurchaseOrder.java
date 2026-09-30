package com.portfolio.erp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.portfolio.erp.domain.exception.ConflictException;

/**
 * Purchase order aggregate: DRAFT -> APPROVED -> RECEIVED, or CANCELLED
 * before receiving. Receiving side effects (stock, cost) are orchestrated by
 * the application service.
 */
public class PurchaseOrder {

    private final Long id;
    private final String number;
    private final Long supplierId;
    private final String supplierName;
    private PurchaseOrderStatus status;
    private LocalDate orderDate;
    private String notes;
    private List<PurchaseOrderLine> lines;
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PurchaseOrder(Long id,
                         String number,
                         Long supplierId,
                         String supplierName,
                         PurchaseOrderStatus status,
                         LocalDate orderDate,
                         String notes,
                         List<PurchaseOrderLine> lines,
                         Long version,
                         Instant createdAt,
                         Instant updatedAt) {
        this.id = id;
        this.number = number;
        this.supplierId = Objects.requireNonNull(supplierId, "supplierId");
        this.supplierName = supplierName;
        this.status = status == null ? PurchaseOrderStatus.DRAFT : status;
        this.orderDate = orderDate == null ? LocalDate.now() : orderDate;
        this.notes = notes;
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PurchaseOrder draft(String number, Long supplierId, LocalDate orderDate, String notes,
                                      List<PurchaseOrderLine> lines) {
        return new PurchaseOrder(null, number, supplierId, null, PurchaseOrderStatus.DRAFT,
                orderDate, notes, lines, null, null, null);
    }

    public void replaceLines(List<PurchaseOrderLine> newLines) {
        requireDraft("edit");
        if (newLines == null || newLines.isEmpty()) {
            throw new ConflictException("error.purchaseOrder.noLines");
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

    public void approve() {
        requireDraft("approve");
        if (lines.isEmpty()) {
            throw new ConflictException("error.purchaseOrder.noLines");
        }
        this.status = PurchaseOrderStatus.APPROVED;
    }

    public void receive() {
        if (status != PurchaseOrderStatus.APPROVED) {
            throw new ConflictException("error.purchaseOrder.invalidStatus", "receive", status);
        }
        this.status = PurchaseOrderStatus.RECEIVED;
    }

    public void cancel() {
        if (status == PurchaseOrderStatus.RECEIVED) {
            throw new ConflictException("error.purchaseOrder.invalidStatus", "cancel", status);
        }
        if (status == PurchaseOrderStatus.CANCELLED) {
            throw new ConflictException("error.purchaseOrder.invalidStatus", "cancel", status);
        }
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    private void requireDraft(String action) {
        if (status != PurchaseOrderStatus.DRAFT) {
            throw new ConflictException("error.purchaseOrder.invalidStatus", action, status);
        }
    }

    public BigDecimal subtotal() {
        return lines.stream().map(PurchaseOrderLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getNotes() {
        return notes;
    }

    public List<PurchaseOrderLine> getLines() {
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
