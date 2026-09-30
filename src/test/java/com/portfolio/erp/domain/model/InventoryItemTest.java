package com.portfolio.erp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

class InventoryItemTest {

    private InventoryItem item(int quantity, int reserved) {
        return new InventoryItem(1L, 10L, "SKU-1", "Product", 20L, "WH-A", quantity, reserved, 0L, null);
    }

    @Test
    void reserve_reduces_available_and_accumulates_reservations() {
        InventoryItem item = item(10, 0);

        item.reserve(4);

        assertThat(item.getQuantity()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isEqualTo(4);
        assertThat(item.available()).isEqualTo(6);
    }

    @Test
    void reserve_fails_when_there_is_not_enough_available_stock() {
        InventoryItem item = item(5, 3);

        assertThatThrownBy(() -> item.reserve(3))
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.inventory.insufficientStock");
    }

    @Test
    void release_frees_reserved_stock_but_never_goes_below_zero() {
        InventoryItem item = item(10, 5);

        item.release(5);
        assertThat(item.getReservedQuantity()).isZero();
        assertThat(item.available()).isEqualTo(10);

        assertThatThrownBy(() -> item.release(1))
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.inventory.overRelease");
    }

    @Test
    void ship_deducts_both_physical_and_reserved_quantity() {
        InventoryItem item = item(10, 4);

        item.ship(4);

        assertThat(item.getQuantity()).isEqualTo(6);
        assertThat(item.getReservedQuantity()).isZero();
        assertThat(item.available()).isEqualTo(6);
    }

    @Test
    void adjustTo_rejects_negative_values_and_values_below_reserved() {
        InventoryItem item = item(10, 6);

        assertThatThrownBy(() -> item.adjustTo(-1))
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.inventory.negativeQuantity");

        assertThatThrownBy(() -> item.adjustTo(5))
                .isInstanceOf(ConflictException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.inventory.belowReserved");

        item.adjustTo(8);
        assertThat(item.getQuantity()).isEqualTo(8);
    }

    @Test
    void remove_validates_positive_amount_and_availability() {
        InventoryItem item = item(4, 1);

        assertThatThrownBy(() -> item.remove(0))
                .isInstanceOf(DomainException.class)
                .extracting(exception -> ((DomainException) exception).getCode())
                .isEqualTo("error.inventory.invalidQuantity");

        assertThatThrownBy(() -> item.remove(4))
                .isInstanceOf(ConflictException.class);

        item.remove(3);
        assertThat(item.getQuantity()).isEqualTo(1);
    }
}
