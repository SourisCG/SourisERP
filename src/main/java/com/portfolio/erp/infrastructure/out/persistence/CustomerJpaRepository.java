package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.CustomerEntity;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            select c from CustomerEntity c
            where (lower(c.name) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(c.code) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:active is null or c.active = :active)
            """)
    Page<CustomerEntity> search(@Param("search") String search,
                                @Param("active") Boolean active,
                                Pageable pageable);
}
