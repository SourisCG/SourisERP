package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.infrastructure.out.entity.SalesOrderEntity;

public interface SalesOrderJpaRepository extends JpaRepository<SalesOrderEntity, Long> {

    @Query("""
            select o from SalesOrderEntity o
            where (lower(o.number) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(o.customer.name) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:status is null or o.status = :status)
              and (:customerId is null or o.customer.id = :customerId)
            """)
    Page<SalesOrderEntity> search(@Param("search") String search,
                                  @Param("status") SalesOrderStatus status,
                                  @Param("customerId") Long customerId,
                                  Pageable pageable);

    @Query(value = "select nextval('sales_order_number_seq')", nativeQuery = true)
    Long nextOrderNumber();
}
