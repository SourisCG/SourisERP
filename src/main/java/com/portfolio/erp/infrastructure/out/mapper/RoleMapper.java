package com.portfolio.erp.infrastructure.out.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.portfolio.erp.domain.model.Role;
import com.portfolio.erp.infrastructure.in.web.dto.RoleResponse;
import com.portfolio.erp.infrastructure.out.entity.RoleEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    Role toDomain(RoleEntity entity);

    RoleEntity toEntity(Role role);

    RoleResponse toResponse(Role role);
}
