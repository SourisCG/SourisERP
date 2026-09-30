package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderLine;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;
import com.portfolio.erp.domain.ports.out.PurchaseOrderRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.PurchaseOrderEntity;
import com.portfolio.erp.infrastructure.out.entity.PurchaseOrderLineEntity;
import com.portfolio.erp.infrastructure.out.mapper.PurchaseOrderMapper;

@Repository
public class PurchaseOrderRepositoryAdapter implements PurchaseOrderRepositoryPort {

    private final PurchaseOrderJpaRepository repository;
    private final SupplierJpaRepository supplierRepository;
    private final ProductJpaRepository productRepository;
    private final PurchaseOrderMapper mapper;

    public PurchaseOrderRepositoryAdapter(PurchaseOrderJpaRepository repository,
                                          SupplierJpaRepository supplierRepository,
                                          ProductJpaRepository productRepository,
                                          PurchaseOrderMapper mapper) {
        this.repository = repository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<PurchaseOrder> search(String search, PurchaseOrderStatus status, Long supplierId,
                                            int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<PurchaseOrderEntity> result = repository.search(normalized, status, supplierId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PurchaseOrder save(PurchaseOrder order) {
        PurchaseOrderEntity entity;
        if (order.getId() == null) {
            entity = mapper.toEntity(order);
        } else {
            entity = repository.findById(order.getId()).orElseThrow();
            mapper.updateEntity(order, entity);
        }
        entity.setSupplier(supplierRepository.getReferenceById(order.getSupplierId()));

        entity.getLines().clear();
        for (PurchaseOrderLine line : order.getLines()) {
            PurchaseOrderLineEntity lineEntity = mapper.toLineEntity(line);
            lineEntity.setProduct(productRepository.getReferenceById(line.getProductId()));
            lineEntity.setOrder(entity);
            entity.getLines().add(lineEntity);
        }

        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public long nextOrderNumber() {
        return repository.nextOrderNumber();
    }
}
