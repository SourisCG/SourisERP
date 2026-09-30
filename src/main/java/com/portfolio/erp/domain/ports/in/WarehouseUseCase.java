package com.portfolio.erp.domain.ports.in;

import java.util.List;

import com.portfolio.erp.domain.model.Warehouse;

public interface WarehouseUseCase {

    List<Warehouse> list();

    Warehouse get(Long id);

    Warehouse create(WarehouseCommand command);

    Warehouse update(Long id, WarehouseCommand command);

    void delete(Long id);

    record WarehouseCommand(String code, String name, String address, boolean active) {
    }
}
