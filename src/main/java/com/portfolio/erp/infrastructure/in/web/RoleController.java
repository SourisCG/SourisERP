package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.ports.out.RoleRepositoryPort;
import com.portfolio.erp.infrastructure.in.web.api.RolesApi;
import com.portfolio.erp.infrastructure.in.web.dto.RoleResponse;
import com.portfolio.erp.infrastructure.out.mapper.RoleMapper;

@RestController
public class RoleController implements RolesApi {

    private final RoleRepositoryPort roles;
    private final RoleMapper roleMapper;

    public RoleController(RoleRepositoryPort roles, RoleMapper roleMapper) {
        this.roles = roles;
        this.roleMapper = roleMapper;
    }

    @Override
    public ResponseEntity<List<RoleResponse>> listRoles() {
        return ResponseEntity.ok(roles.findAll().stream().map(roleMapper::toResponse).toList());
    }
}
