package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.infrastructure.out.entity.StockMovementEntity;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {

    @Query("""
            select m from StockMovementEntity m
            where (:productId is null or m.product.id = :productId)
              and (:warehouseId is null or m.warehouse.id = :warehouseId)
              and (:type is null or m.type = :type)
            """)
    Page<StockMovementEntity> search(@Param("productId") Long productId,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("type") MovementType type,
                                     Pageable pageable);
}
