package com.portfolio.erp.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.portfolio.erp.domain.model.InventoryItem;

public interface InventoryRepositoryPort {

    List<InventoryItem> search(Long warehouseId, Long productId, Integer lowStockThreshold);

    Optional<InventoryItem> findByProductAndWarehouse(Long productId, Long warehouseId);

    boolean existsByWarehouseId(Long warehouseId);

    InventoryItem save(InventoryItem item);
}
