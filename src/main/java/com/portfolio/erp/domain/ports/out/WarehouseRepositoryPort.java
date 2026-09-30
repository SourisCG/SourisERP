package com.portfolio.erp.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.portfolio.erp.domain.model.Warehouse;

public interface WarehouseRepositoryPort {

    List<Warehouse> findAll();

    Optional<Warehouse> findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Warehouse save(Warehouse warehouse);

    void delete(Long id);
}
