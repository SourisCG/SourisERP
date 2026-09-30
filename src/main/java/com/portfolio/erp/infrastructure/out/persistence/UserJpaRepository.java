package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            select u from UserEntity u
            where lower(u.username) like lower(concat('%', coalesce(:search, ''), '%'))
               or lower(u.email) like lower(concat('%', coalesce(:search, ''), '%'))
               or lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', coalesce(:search, ''), '%'))
            """)
    Page<UserEntity> search(@Param("search") String search, Pageable pageable);
}
