package com.portfolio.erp.infrastructure.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("""
            select c from CategoryEntity c
            where lower(c.name) like lower(concat('%', coalesce(:search, ''), '%'))
            order by c.name
            """)
    List<CategoryEntity> search(@Param("search") String search);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    long countByParentId(Long parentId);
}
