package com.portfolio.erp.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;

/**
 * Stock of one product in one warehouse. The version field drives optimistic
 * locking; movements are recorded by the application service.
 */
public class InventoryItem {

    private final Long id;
    private final Long productId;
    private final String sku;
    private final String productName;
    private final Long warehouseId;
    private final String warehouseCode;
    private int quantity;
    private int reservedQuantity;
    private final Long version;
    private final Instant updatedAt;

    public InventoryItem(Long id,
                         Long productId,
                         String sku,
                         String productName,
                         Long warehouseId,
                         String warehouseCode,
                         int quantity,
                         int reservedQuantity,
                         Long version,
                         Instant updatedAt) {
        this.id = id;
        this.productId = Objects.requireNonNull(productId, "productId");
        this.sku = sku;
        this.productName = productName;
        this.warehouseId = Objects.requireNonNull(warehouseId, "warehouseId");
        this.warehouseCode = warehouseCode;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    public static InventoryItem empty(Long productId, Long warehouseId) {
        return new InventoryItem(null, productId, null, null, warehouseId, null, 0, 0, null, null);
    }

    public int available() {
        return quantity - reservedQuantity;
    }

    public void adjustTo(int newQuantity) {
        if (newQuantity < 0) {
            throw new DomainException("error.inventory.negativeQuantity");
        }
        if (newQuantity < reservedQuantity) {
            throw new ConflictException("error.inventory.belowReserved", reservedQuantity);
        }
        this.quantity = newQuantity;
    }

    public void receive(int amount) {
        requirePositive(amount);
        this.quantity += amount;
    }

    public void remove(int amount) {
        requirePositive(amount);
        if (available() < amount) {
            throw new ConflictException("error.inventory.insufficientStock",
                    sku != null ? sku : productId, warehouseCode != null ? warehouseCode : warehouseId, available());
        }
        this.quantity -= amount;
    }

    public void reserve(int amount) {
        requirePositive(amount);
        if (available() < amount) {
            throw new ConflictException("error.inventory.insufficientStock",
                    sku != null ? sku : productId, warehouseCode != null ? warehouseCode : warehouseId, available());
        }
        this.reservedQuantity += amount;
    }

    public void release(int amount) {
        requirePositive(amount);
        if (reservedQuantity < amount) {
            throw new DomainException("error.inventory.overRelease");
        }
        this.reservedQuantity -= amount;
    }

    public void ship(int amount) {
        requirePositive(amount);
        if (reservedQuantity < amount || quantity < amount) {
            throw new DomainException("error.inventory.overShip");
        }
        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }

    private void requirePositive(int amount) {
        if (amount <= 0) {
            throw new DomainException("error.inventory.invalidQuantity");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
