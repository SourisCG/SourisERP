package com.portfolio.erp.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.ports.out.InventoryRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.InventoryItemEntity;
import com.portfolio.erp.infrastructure.out.mapper.InventoryItemMapper;

@Repository
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final InventoryItemJpaRepository repository;
    private final ProductJpaRepository productRepository;
    private final WarehouseJpaRepository warehouseRepository;
    private final InventoryItemMapper mapper;

    public InventoryRepositoryAdapter(InventoryItemJpaRepository repository,
                                      ProductJpaRepository productRepository,
                                      WarehouseJpaRepository warehouseRepository,
                                      InventoryItemMapper mapper) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Override
    public List<InventoryItem> search(Long warehouseId, Long productId, Integer lowStockThreshold) {
        return repository.search(warehouseId, productId, lowStockThreshold).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<InventoryItem> findByProductAndWarehouse(Long productId, Long warehouseId) {
        return repository.findByProductIdAndWarehouseId(productId, warehouseId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByWarehouseId(Long warehouseId) {
        return repository.existsByWarehouseId(warehouseId);
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemEntity entity;
        if (item.getId() == null) {
            entity = mapper.toEntity(item);
        } else {
            entity = repository.findById(item.getId()).orElseThrow();
            mapper.updateEntity(item, entity);
        }
        if (entity.getProduct() == null) {
            entity.setProduct(productRepository.getReferenceById(item.getProductId()));
        }
        if (entity.getWarehouse() == null) {
            entity.setWarehouse(warehouseRepository.getReferenceById(item.getWarehouseId()));
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
