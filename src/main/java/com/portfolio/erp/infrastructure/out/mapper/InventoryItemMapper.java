package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.infrastructure.in.web.dto.InventoryItemResponse;
import com.portfolio.erp.infrastructure.out.entity.InventoryItemEntity;

@Mapper(componentModel = "spring")
public interface InventoryItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseCode", source = "warehouse.code")
    InventoryItem toDomain(InventoryItemEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    InventoryItemEntity toEntity(InventoryItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    void updateEntity(InventoryItem item, @MappingTarget InventoryItemEntity entity);

    @Mapping(target = "availableQuantity", expression = "java(item.available())")
    InventoryItemResponse toResponse(InventoryItem item);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
