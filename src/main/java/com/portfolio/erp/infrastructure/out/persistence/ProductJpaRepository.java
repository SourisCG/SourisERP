package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.infrastructure.out.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select p from ProductEntity p
            where (lower(p.name) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(p.sku) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:categoryId is null or p.category.id = :categoryId)
              and (:active is null or p.active = :active)
            """)
    Page<ProductEntity> search(@Param("search") String search,
                               @Param("categoryId") Long categoryId,
                               @Param("active") Boolean active,
                               Pageable pageable);
}
