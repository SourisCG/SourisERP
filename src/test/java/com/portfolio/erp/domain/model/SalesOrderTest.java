package com.portfolio.erp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

class SalesOrderTest {

    private SalesOrderLine line(long productId, int quantity, String price, String discount, String tax) {
        return new SalesOrderLine(1L, productId, "SKU-" + productId, "Product " + productId, 1L,
                quantity, new BigDecimal(price), new BigDecimal(discount), new BigDecimal(tax));
    }

    private SalesOrder order(SalesOrderStatus status, SalesOrderLine... lines) {
        return new SalesOrder(1L, "SO-2026-00001", 1L, "Customer", status,
                LocalDate.now(), null, List.of(lines), 0L, null, null);
    }

    @Test
    void totals_aggregate_lines_discounts_and_taxes() {
        SalesOrder order = order(SalesOrderStatus.DRAFT,
                line(1L, 2, "100.00", "10", "21"),   // 180.00 + 37.80
                line(2L, 1, "50.00", "0", "10"));    //  50.00 +  5.00

        assertThat(order.subtotal()).isEqualByComparingTo("230.00");
        assertThat(order.taxTotal()).isEqualByComparingTo("42.80");
        assertThat(order.total()).isEqualByComparingTo("272.80");
    }

    @Test
    void monetary_math_is_consistent() {
        SalesOrderLine discounted = line(1L, 2, "100.00", "10", "21");

        assertThat(discounted.grossAmount()).isEqualByComparingTo("200.00");
        assertThat(discounted.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(discounted.lineTotal()).isEqualByComparingTo("180.00");
        assertThat(discounted.lineTax()).isEqualByComparingTo("37.80");
    }

    @Test
    void confirm_requires_draft_status_and_at_least_one_line() {
        SalesOrder empty = new SalesOrder(1L, "SO-1", 1L, "Customer", SalesOrderStatus.DRAFT,
                LocalDate.now(), null, List.of(), 0L, null, null);
        assertThatThrownBy(empty::confirm)
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.salesOrder.noLines");

        SalesOrder order = order(SalesOrderStatus.DRAFT, line(1L, 1, "10.00", "0", "21"));
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(SalesOrderStatus.CONFIRMED);

        assertThatThrownBy(order::confirm)
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.salesOrder.invalidStatus");
    }

    @Test
    void confirmed_orders_cannot_be_edited() {
        SalesOrder order = order(SalesOrderStatus.CONFIRMED, line(1L, 1, "10.00", "0", "21"));

        assertThatThrownBy(() -> order.updateHeader(LocalDate.now(), "new notes"))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> order.replaceLines(List.of(line(2L, 1, "5.00", "0", "21"))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void invoicing_only_allowed_from_confirmed_state() {
        SalesOrder draft = order(SalesOrderStatus.DRAFT, line(1L, 1, "10.00", "0", "21"));
        assertThatThrownBy(draft::markInvoiced).isInstanceOf(ConflictException.class);

        SalesOrder confirmed = order(SalesOrderStatus.CONFIRMED, line(1L, 1, "10.00", "0", "21"));
        confirmed.markInvoiced();
        assertThat(confirmed.getStatus()).isEqualTo(SalesOrderStatus.INVOICED);
    }

    @Test
    void cancelled_orders_cannot_be_cancelled_twice_or_invoiced() {
        SalesOrder order = order(SalesOrderStatus.CANCELLED, line(1L, 1, "10.00", "0", "21"));

        assertThatThrownBy(order::cancel).isInstanceOf(ConflictException.class);
        assertThatThrownBy(order::markInvoiced).isInstanceOf(ConflictException.class);
    }

    @Test
    void invoiced_orders_cannot_be_cancelled() {
        SalesOrder order = order(SalesOrderStatus.INVOICED, line(1L, 1, "10.00", "0", "21"));

        assertThatThrownBy(order::cancel)
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.salesOrder.invalidStatus");
    }
}
