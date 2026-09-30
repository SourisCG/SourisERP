package com.portfolio.erp.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.InventoryItemEntity;

public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, Long> {

    Optional<InventoryItemEntity> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    boolean existsByWarehouseId(Long warehouseId);

    @Query("""
            select i from InventoryItemEntity i
            where (:warehouseId is null or i.warehouse.id = :warehouseId)
              and (:productId is null or i.product.id = :productId)
              and (:lowStockThreshold is null or (i.quantity - i.reservedQuantity) <= :lowStockThreshold)
            order by i.product.name
            """)
    List<InventoryItemEntity> search(@Param("warehouseId") Long warehouseId,
                                     @Param("productId") Long productId,
                                     @Param("lowStockThreshold") Integer lowStockThreshold);
}
