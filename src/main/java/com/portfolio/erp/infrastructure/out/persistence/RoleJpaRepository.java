package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.erp.infrastructure.out.entity.RoleEntity;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findAllByOrderByNameAsc();

    Set<RoleEntity> findAllByNameIn(Collection<String> names);
}
