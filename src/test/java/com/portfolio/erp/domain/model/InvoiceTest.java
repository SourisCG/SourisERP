package com.portfolio.erp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

class InvoiceTest {

    private SalesOrderLine orderLine(int quantity, String price, String tax) {
        return new SalesOrderLine(1L, 1L, "SKU-1", "Product", 1L, quantity,
                new BigDecimal(price), BigDecimal.ZERO, new BigDecimal(tax));
    }

    private SalesOrder confirmedOrder(SalesOrderLine... lines) {
        return new SalesOrder(1L, "SO-2026-00001", 1L, "Customer", SalesOrderStatus.CONFIRMED,
                LocalDate.now(), null, List.of(lines), 0L, null, null);
    }

    private Invoice invoice(String total) {
        BigDecimal totalValue = new BigDecimal(total);
        return new Invoice(1L, "INV-1", 1L, "SO-1", 1L, "Customer", InvoiceStatus.ISSUED,
                LocalDate.now(), LocalDate.now().plusDays(30), totalValue, BigDecimal.ZERO, totalValue,
                BigDecimal.ZERO, List.of(), new java.util.ArrayList<>(), null);
    }

    @Test
    void fromOrder_copies_lines_and_totals_and_starts_issued() {
        Invoice invoice = Invoice.fromOrder("INV-2026-00001",
                confirmedOrder(orderLine(2, "100.00", "21")),
                LocalDate.now(), LocalDate.now().plusDays(30));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getLines()).hasSize(1);
        assertThat(invoice.getLines().get(0).lineTotal()).isEqualByComparingTo("200.00");
        assertThat(invoice.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(invoice.getTaxTotal()).isEqualByComparingTo("42.00");
        assertThat(invoice.getTotal()).isEqualByComparingTo("242.00");
        assertThat(invoice.balance()).isEqualByComparingTo("242.00");
    }

    @Test
    void partial_payments_move_the_invoice_to_partially_paid() {
        Invoice invoice = invoice("242.00");

        invoice.addPayment(new BigDecimal("100.00"), "TRANSFER", "SEPA-1");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("100.00");
        assertThat(invoice.balance()).isEqualByComparingTo("142.00");
        assertThat(invoice.getPayments()).hasSize(1);
    }

    @Test
    void paying_the_balance_marks_the_invoice_as_paid() {
        Invoice invoice = invoice("242.00");

        invoice.addPayment(new BigDecimal("100.00"), "CASH", null);
        invoice.addPayment(new BigDecimal("142.00"), "CARD", "POS-9");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void payments_cannot_exceed_the_balance() {
        Invoice invoice = invoice("100.00");

        assertThatThrownBy(() -> invoice.addPayment(new BigDecimal("100.01"), "CASH", null))
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.invoice.paymentExceedsBalance");
    }

    @Test
    void paid_invoices_reject_new_payments() {
        Invoice invoice = invoice("50.00");
        invoice.addPayment(new BigDecimal("50.00"), "CASH", null);

        assertThatThrownBy(() -> invoice.addPayment(new BigDecimal("10.00"), "CASH", null))
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.invoice.alreadyPaid");
    }

    @Test
    void non_positive_payments_are_rejected() {
        Invoice invoice = invoice("50.00");

        assertThatThrownBy(() -> invoice.addPayment(BigDecimal.ZERO, "CASH", null))
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.invoice.invalidPaymentAmount");
        assertThatThrownBy(() -> invoice.addPayment(new BigDecimal("-1"), "CASH", null))
                .isInstanceOf(DomainException.class);
    }
}
