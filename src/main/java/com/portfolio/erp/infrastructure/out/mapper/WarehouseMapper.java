package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.Warehouse;
import com.portfolio.erp.infrastructure.in.web.dto.WarehouseResponse;
import com.portfolio.erp.infrastructure.out.entity.WarehouseEntity;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    Warehouse toDomain(WarehouseEntity entity);

    @Mapping(target = "id", ignore = true)
    WarehouseEntity toEntity(Warehouse warehouse);

    @Mapping(target = "id", ignore = true)
    void updateEntity(Warehouse warehouse, @MappingTarget WarehouseEntity entity);

    WarehouseResponse toResponse(Warehouse warehouse);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
