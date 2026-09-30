package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.SupplierEntity;

public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select s from SupplierEntity s
            where (lower(s.name) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(s.code) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:active is null or s.active = :active)
            """)
    Page<SupplierEntity> search(@Param("search") String search,
                                @Param("active") Boolean active,
                                Pageable pageable);
}
