package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Supplier;
import com.portfolio.erp.domain.ports.out.SupplierRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.SupplierEntity;
import com.portfolio.erp.infrastructure.out.mapper.PurchaseOrderMapper;

@Repository
public class SupplierRepositoryAdapter implements SupplierRepositoryPort {

    private final SupplierJpaRepository repository;
    private final PurchaseOrderMapper mapper;

    public SupplierRepositoryAdapter(SupplierJpaRepository repository, PurchaseOrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Supplier> search(String search, Boolean active, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<SupplierEntity> result = repository.search(normalized, active,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<Supplier> findById(Long id) {
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
    public Supplier save(Supplier supplier) {
        SupplierEntity entity;
        if (supplier.getId() == null) {
            entity = mapper.toEntity(supplier);
        } else {
            entity = repository.findById(supplier.getId()).orElseThrow();
            mapper.updateEntity(supplier, entity);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
