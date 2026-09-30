package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.infrastructure.in.web.dto.UserDetail;
import com.portfolio.erp.infrastructure.in.web.dto.UserSummary;
import com.portfolio.erp.infrastructure.out.entity.UserEntity;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.fullName())")
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(com.portfolio.erp.domain.model.Role::name).toList())")
    UserSummary toSummary(User user);

    @Mapping(target = "fullName", expression = "java(user.fullName())")
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(com.portfolio.erp.domain.model.Role::name).toList())")
    UserDetail toDetail(User user);

    User toDomain(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    UserEntity toEntity(User user);

    @Mapping(target = "id", ignore = true)
    void updateEntity(User user, @MappingTarget UserEntity entity);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
