package com.portfolio.erp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

class PurchaseOrderTest {

    private PurchaseOrderLine line(int quantity, String unitCost) {
        return new PurchaseOrderLine(1L, 1L, "SKU-1", "Product", 1L, quantity, new BigDecimal(unitCost));
    }

    private PurchaseOrder order(PurchaseOrderStatus status, PurchaseOrderLine... lines) {
        return new PurchaseOrder(1L, "PO-2026-00001", 1L, "Supplier", status,
                LocalDate.now(), null, List.of(lines), 0L, null, null);
    }

    @Test
    void subtotal_multiplies_quantity_by_unit_cost() {
        PurchaseOrder order = order(PurchaseOrderStatus.DRAFT, line(3, "7.50"), line(1, "0.99"));

        assertThat(order.subtotal()).isEqualByComparingTo("23.49");
    }

    @Test
    void approve_requires_draft_status_and_at_least_one_line() {
        PurchaseOrder empty = new PurchaseOrder(1L, "PO-1", 1L, "Supplier", PurchaseOrderStatus.DRAFT,
                LocalDate.now(), null, List.of(), 0L, null, null);
        assertThatThrownBy(empty::approve)
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.purchaseOrder.noLines");

        PurchaseOrder order = order(PurchaseOrderStatus.DRAFT, line(1, "1.00"));
        order.approve();
        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);

        assertThatThrownBy(order::approve).isInstanceOf(ConflictException.class);
    }

    @Test
    void receive_only_allowed_from_approved_state() {
        assertThatThrownBy(() -> order(PurchaseOrderStatus.DRAFT, line(1, "1.00")).receive())
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.purchaseOrder.invalidStatus");

        PurchaseOrder approved = order(PurchaseOrderStatus.APPROVED, line(1, "1.00"));
        approved.receive();
        assertThat(approved.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
    }

    @Test
    void received_orders_cannot_be_cancelled() {
        PurchaseOrder received = order(PurchaseOrderStatus.RECEIVED, line(1, "1.00"));

        assertThatThrownBy(received::cancel).isInstanceOf(ConflictException.class);
    }

    @Test
    void draft_and_approved_orders_can_be_cancelled_once() {
        PurchaseOrder draft = order(PurchaseOrderStatus.DRAFT, line(1, "1.00"));
        draft.cancel();
        assertThat(draft.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        assertThatThrownBy(draft::cancel).isInstanceOf(ConflictException.class);

        PurchaseOrder approved = order(PurchaseOrderStatus.APPROVED, line(1, "1.00"));
        approved.cancel();
        assertThat(approved.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
    }
}
