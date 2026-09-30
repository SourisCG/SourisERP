package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.Category;
import com.portfolio.erp.infrastructure.in.web.dto.CategoryResponse;
import com.portfolio.erp.infrastructure.out.entity.CategoryEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toDomain(CategoryEntity entity);

    @Mapping(target = "id", ignore = true)
    CategoryEntity toEntity(Category category);

    @Mapping(target = "id", ignore = true)
    void updateEntity(Category category, @MappingTarget CategoryEntity entity);

    CategoryResponse toResponse(Category category);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
