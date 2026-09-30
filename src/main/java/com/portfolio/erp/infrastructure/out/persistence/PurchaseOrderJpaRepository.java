package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.domain.model.PurchaseOrderStatus;
import com.portfolio.erp.infrastructure.out.entity.PurchaseOrderEntity;

public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    @Query("""
            select o from PurchaseOrderEntity o
            where (lower(o.number) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(o.supplier.name) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:status is null or o.status = :status)
              and (:supplierId is null or o.supplier.id = :supplierId)
            """)
    Page<PurchaseOrderEntity> search(@Param("search") String search,
                                     @Param("status") PurchaseOrderStatus status,
                                     @Param("supplierId") Long supplierId,
                                     Pageable pageable);

    @Query(value = "select nextval('purchase_order_number_seq')", nativeQuery = true)
    Long nextOrderNumber();
}
