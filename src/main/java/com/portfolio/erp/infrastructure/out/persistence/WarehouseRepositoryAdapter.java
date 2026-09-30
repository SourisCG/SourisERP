package com.portfolio.erp.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.Warehouse;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.WarehouseEntity;
import com.portfolio.erp.infrastructure.out.mapper.WarehouseMapper;

@Repository
public class WarehouseRepositoryAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository repository;
    private final WarehouseMapper mapper;

    public WarehouseRepositoryAdapter(WarehouseJpaRepository repository, WarehouseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Warehouse> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return repository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseEntity entity;
        if (warehouse.getId() == null) {
            entity = mapper.toEntity(warehouse);
        } else {
            entity = repository.findById(warehouse.getId()).orElseThrow();
            mapper.updateEntity(warehouse, entity);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
