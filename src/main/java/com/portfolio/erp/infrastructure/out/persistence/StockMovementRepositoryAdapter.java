package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.StockMovement;
import com.portfolio.erp.domain.ports.out.StockMovementRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.StockMovementEntity;
import com.portfolio.erp.infrastructure.out.mapper.StockMovementMapper;

@Repository
public class StockMovementRepositoryAdapter implements StockMovementRepositoryPort {

    private final StockMovementJpaRepository repository;
    private final ProductJpaRepository productRepository;
    private final WarehouseJpaRepository warehouseRepository;
    private final StockMovementMapper mapper;

    public StockMovementRepositoryAdapter(StockMovementJpaRepository repository,
                                          ProductJpaRepository productRepository,
                                          WarehouseJpaRepository warehouseRepository,
                                          StockMovementMapper mapper) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementEntity entity = mapper.toEntity(movement);
        entity.setProduct(productRepository.getReferenceById(movement.productId()));
        entity.setWarehouse(warehouseRepository.getReferenceById(movement.warehouseId()));
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public PageResult<StockMovement> search(Long productId, Long warehouseId, MovementType type, int page, int size) {
        Page<StockMovementEntity> result = repository.search(productId, warehouseId, type,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }
}
