package com.portfolio.erp.infrastructure.in.web;

import java.net.URI;
import java.util.LinkedHashSet;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.domain.ports.in.UserUseCase;
import com.portfolio.erp.infrastructure.in.web.api.UsersApi;
import com.portfolio.erp.infrastructure.in.web.dto.AssignRolesRequest;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PageUser;
import com.portfolio.erp.infrastructure.in.web.dto.UserCreateRequest;
import com.portfolio.erp.infrastructure.in.web.dto.UserDetail;
import com.portfolio.erp.infrastructure.in.web.dto.UserUpdateRequest;
import com.portfolio.erp.infrastructure.out.mapper.UserMapper;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class UserController implements UsersApi {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;

    public UserController(UserUseCase userUseCase, UserMapper userMapper) {
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<PageUser> listUsers(Integer page, Integer size, String search) {
        PageResult<User> result = userUseCase.list(search, page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageUser response = new PageUser();
        response.setContent(result.content().stream().map(userMapper::toSummary).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserDetail> createUser(UserCreateRequest request) {
        User created = userUseCase.create(new UserUseCase.CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                new LinkedHashSet<>(request.getRoles()),
                !Boolean.FALSE.equals(request.getEnabled())));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(userMapper.toDetail(created));
    }

    @Override
    public ResponseEntity<UserDetail> getUser(Long id) {
        return ResponseEntity.ok(userMapper.toDetail(userUseCase.get(id)));
    }

    @Override
    public ResponseEntity<UserDetail> updateUser(Long id, UserUpdateRequest request) {
        User updated = userUseCase.update(id, new UserUseCase.UpdateUserCommand(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                !Boolean.FALSE.equals(request.getEnabled())));
        return ResponseEntity.ok(userMapper.toDetail(updated));
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserDetail> assignRoles(Long id, AssignRolesRequest request) {
        User updated = userUseCase.assignRoles(id, new LinkedHashSet<>(request.getRoles()));
        return ResponseEntity.ok(userMapper.toDetail(updated));
    }
}
