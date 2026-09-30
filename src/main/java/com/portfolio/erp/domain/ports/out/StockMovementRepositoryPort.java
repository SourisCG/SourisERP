package com.portfolio.erp.domain.ports.out;

import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.StockMovement;

public interface StockMovementRepositoryPort {

    StockMovement save(StockMovement movement);

    PageResult<StockMovement> search(Long productId, Long warehouseId, MovementType type, int page, int size);
}
