package com.portfolio.erp.application.service;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.DomainException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.StockMovement;
import com.portfolio.erp.domain.ports.in.InventoryUseCase;
import com.portfolio.erp.domain.ports.out.InventoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;
import com.portfolio.erp.domain.ports.out.StockMovementRepositoryPort;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;

/**
 * Stock operations. Mutating methods are transactional AND retried when an
 * optimistic lock conflict occurs, so concurrent requests lose gracefully
 * instead of over-selling.
 */
@Service
@Transactional(readOnly = true)
public class InventoryService implements InventoryUseCase {

    private final InventoryRepositoryPort inventory;
    private final StockMovementRepositoryPort movements;
    private final ProductRepositoryPort products;
    private final WarehouseRepositoryPort warehouses;

    public InventoryService(InventoryRepositoryPort inventory,
                            StockMovementRepositoryPort movements,
                            ProductRepositoryPort products,
                            WarehouseRepositoryPort warehouses) {
        this.inventory = inventory;
        this.movements = movements;
        this.products = products;
        this.warehouses = warehouses;
    }

    @Override
    public List<InventoryItem> list(Long warehouseId, Long productId, Integer lowStockThreshold) {
        return inventory.search(warehouseId, productId, lowStockThreshold);
    }

    @Override
    public PageResult<StockMovement> listMovements(Long productId, Long warehouseId, MovementType type,
                                                   int page, int size) {
        return movements.search(productId, warehouseId, type, page, size);
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public InventoryItem adjust(Long productId, Long warehouseId, int newQuantity, String reason) {
        Product product = requireProduct(productId);
        requireWarehouse(warehouseId);
        InventoryItem item = inventory.findByProductAndWarehouse(productId, warehouseId)
                .orElseGet(() -> InventoryItem.empty(productId, warehouseId));

        int delta = newQuantity - item.getQuantity();
        item.adjustTo(newQuantity);
        InventoryItem saved = inventory.save(item);
        movements.save(StockMovement.of(productId, warehouseId, MovementType.ADJUSTMENT, delta,
                "ADJUSTMENT", saved.getId(), reason != null ? reason : "Manual adjustment for " + product.getSku()));
        return saved;
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public void transfer(Long productId, Long fromWarehouseId, Long toWarehouseId, int quantity, String notes) {
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new DomainException("error.inventory.sameWarehouse");
        }
        requireProduct(productId);
        requireWarehouse(fromWarehouseId);
        requireWarehouse(toWarehouseId);

        InventoryItem from = requireItem(productId, fromWarehouseId);
        InventoryItem to = inventory.findByProductAndWarehouse(productId, toWarehouseId)
                .orElseGet(() -> InventoryItem.empty(productId, toWarehouseId));

        from.remove(quantity);
        to.receive(quantity);

        inventory.save(from);
        inventory.save(to);
        movements.save(StockMovement.of(productId, fromWarehouseId, MovementType.TRANSFER_OUT, quantity,
                "TRANSFER", null, notes));
        movements.save(StockMovement.of(productId, toWarehouseId, MovementType.TRANSFER_IN, quantity,
                "TRANSFER", null, notes));
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public InventoryItem reserve(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId) {
        InventoryItem item = requireItem(productId, warehouseId);
        item.reserve(quantity);
        return inventory.save(item);
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public InventoryItem release(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId) {
        InventoryItem item = requireItem(productId, warehouseId);
        item.release(quantity);
        return inventory.save(item);
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public InventoryItem ship(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId) {
        InventoryItem item = requireItem(productId, warehouseId);
        item.ship(quantity);
        InventoryItem saved = inventory.save(item);
        movements.save(StockMovement.of(productId, warehouseId, MovementType.SALE, quantity,
                referenceType, referenceId, null));
        return saved;
    }

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 40))
    public InventoryItem receive(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId) {
        requireProduct(productId);
        requireWarehouse(warehouseId);
        InventoryItem item = inventory.findByProductAndWarehouse(productId, warehouseId)
                .orElseGet(() -> InventoryItem.empty(productId, warehouseId));
        boolean isNewItem = item.getId() == null;

        item.receive(quantity);
        InventoryItem saved = inventory.save(item);
        movements.save(StockMovement.of(productId, warehouseId,
                isNewItem ? MovementType.INITIAL : MovementType.PURCHASE_RECEIPT, quantity,
                referenceType, referenceId, null));
        return saved;
    }

    private InventoryItem requireItem(Long productId, Long warehouseId) {
        return inventory.findByProductAndWarehouse(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("error.inventory.notFound",
                        productId, warehouseId));
    }

    private Product requireProduct(Long productId) {
        return products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("error.product.notFound"));
    }

    private void requireWarehouse(Long warehouseId) {
        warehouses.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("error.warehouse.notFound"));
    }
}
