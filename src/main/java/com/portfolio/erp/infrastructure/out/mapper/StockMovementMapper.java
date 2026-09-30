package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

import com.portfolio.erp.domain.model.StockMovement;
import com.portfolio.erp.infrastructure.in.web.dto.StockMovementResponse;
import com.portfolio.erp.infrastructure.out.entity.StockMovementEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockMovementMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseCode", source = "warehouse.code")
    StockMovement toDomain(StockMovementEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    StockMovementEntity toEntity(StockMovement movement);

    StockMovementResponse toResponse(StockMovement movement);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
