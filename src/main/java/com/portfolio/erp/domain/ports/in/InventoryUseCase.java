package com.portfolio.erp.domain.ports.in;

import java.util.List;

import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.StockMovement;

public interface InventoryUseCase {

    List<InventoryItem> list(Long warehouseId, Long productId, Integer lowStockThreshold);

    PageResult<StockMovement> listMovements(Long productId, Long warehouseId, MovementType type, int page, int size);

    InventoryItem adjust(Long productId, Long warehouseId, int newQuantity, String reason);

    void transfer(Long productId, Long fromWarehouseId, Long toWarehouseId, int quantity, String notes);

    InventoryItem reserve(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId);

    InventoryItem release(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId);

    InventoryItem ship(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId);

    InventoryItem receive(Long productId, Long warehouseId, int quantity, String referenceType, Long referenceId);
}
