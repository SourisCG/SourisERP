package com.portfolio.erp.domain.model;

import java.time.Instant;

/**
 * Immutable stock ledger entry.
 */
public record StockMovement(Long id,
                            Long productId,
                            String sku,
                            String productName,
                            Long warehouseId,
                            String warehouseCode,
                            MovementType type,
                            int quantity,
                            String referenceType,
                            Long referenceId,
                            String notes,
                            Instant createdAt,
                            Long createdBy) {

    public static StockMovement of(Long productId, Long warehouseId, MovementType type, int quantity,
                                   String referenceType, Long referenceId, String notes) {
        return new StockMovement(null, productId, null, null, warehouseId, null, type, quantity,
                referenceType, referenceId, notes, null, null);
    }
}
