package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.erp.infrastructure.out.entity.WarehouseEntity;

public interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
