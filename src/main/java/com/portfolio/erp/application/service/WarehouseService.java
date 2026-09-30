package com.portfolio.erp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.Warehouse;
import com.portfolio.erp.domain.ports.in.WarehouseUseCase;
import com.portfolio.erp.domain.ports.out.InventoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;

@Service
@Transactional(readOnly = true)
public class WarehouseService implements WarehouseUseCase {

    private final WarehouseRepositoryPort warehouses;
    private final InventoryRepositoryPort inventory;

    public WarehouseService(WarehouseRepositoryPort warehouses, InventoryRepositoryPort inventory) {
        this.warehouses = warehouses;
        this.inventory = inventory;
    }

    @Override
    public List<Warehouse> list() {
        return warehouses.findAll();
    }

    @Override
    public Warehouse get(Long id) {
        return requireWarehouse(id);
    }

    @Override
    @Transactional
    public Warehouse create(WarehouseCommand command) {
        if (warehouses.existsByCode(command.code())) {
            throw new ConflictException("error.warehouse.codeExists", command.code());
        }
        return warehouses.save(Warehouse.newWarehouse(command.code(), command.name(), command.address(), command.active()));
    }

    @Override
    @Transactional
    public Warehouse update(Long id, WarehouseCommand command) {
        Warehouse warehouse = requireWarehouse(id);
        if (!warehouse.getCode().equalsIgnoreCase(command.code()) && warehouses.existsByCode(command.code())) {
            throw new ConflictException("error.warehouse.codeExists", command.code());
        }
        warehouse.updateDetails(command.code(), command.name(), command.address());
        warehouse.setActive(command.active());
        return warehouses.save(warehouse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireWarehouse(id);
        if (inventory.existsByWarehouseId(id)) {
            throw new ConflictException("error.warehouse.hasStock");
        }
        warehouses.delete(id);
    }

    private Warehouse requireWarehouse(Long id) {
        return warehouses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.warehouse.notFound"));
    }
}
