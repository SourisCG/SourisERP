package com.portfolio.erp.application.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.DomainException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Role;
import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.domain.ports.in.UserUseCase;
import com.portfolio.erp.domain.ports.out.CurrentActorPort;
import com.portfolio.erp.domain.ports.out.PasswordHasherPort;
import com.portfolio.erp.domain.ports.out.RoleRepositoryPort;
import com.portfolio.erp.domain.ports.out.UserRepositoryPort;

@Service
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final PasswordHasherPort passwordHasher;
    private final CurrentActorPort currentActor;

    public UserService(UserRepositoryPort users,
                       RoleRepositoryPort roles,
                       PasswordHasherPort passwordHasher,
                       CurrentActorPort currentActor) {
        this.users = users;
        this.roles = roles;
        this.passwordHasher = passwordHasher;
        this.currentActor = currentActor;
    }

    @Override
    public PageResult<User> list(String search, int page, int size) {
        return users.findAll(search, page, size);
    }

    @Override
    public User get(Long id) {
        return requireUser(id);
    }

    @Override
    @Transactional
    public User create(CreateUserCommand command) {
        if (users.existsByUsername(command.username())) {
            throw new ConflictException("error.user.usernameExists", command.username());
        }
        if (users.existsByEmail(command.email())) {
            throw new ConflictException("error.user.emailExists", command.email());
        }
        Set<Role> roles = resolveRoles(command.roles());
        User user = User.newUser(
                command.username(),
                command.email(),
                command.firstName(),
                command.lastName(),
                passwordHasher.hash(command.password()),
                roles);
        user.setEnabled(command.enabled());
        return users.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, UpdateUserCommand command) {
        User user = requireUser(id);
        if (!user.getEmail().equalsIgnoreCase(command.email()) && users.existsByEmail(command.email())) {
            throw new ConflictException("error.user.emailExists", command.email());
        }
        user.updateProfile(command.email(), command.firstName(), command.lastName());
        user.setEnabled(command.enabled());
        return users.save(user);
    }

    @Override
    @Transactional
    public void disable(Long id) {
        if (currentActor.currentActorId().filter(actorId -> actorId.equals(id)).isPresent()) {
            throw new DomainException("error.user.cannotDisableSelf");
        }
        User user = requireUser(id);
        user.setEnabled(false);
        users.save(user);
    }

    @Override
    @Transactional
    public User assignRoles(Long id, Set<String> roleNames) {
        User user = requireUser(id);
        user.assignRoles(resolveRoles(roleNames));
        return users.save(user);
    }

    private User requireUser(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.notFound"));
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<String> normalized = roleNames.stream()
                .map(name -> name.trim().toUpperCase())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        Set<Role> found = roles.findByNames(normalized);
        if (found.size() != normalized.size()) {
            Set<String> missing = new HashSet<>(normalized);
            found.forEach(role -> missing.remove(role.name()));
            throw new DomainException("error.role.notFound", String.join(", ", missing));
        }
        return found;
    }
}
