package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.infrastructure.out.entity.InvoiceEntity;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, Long> {

    @Query("""
            select i from InvoiceEntity i
            where (lower(i.number) like lower(concat('%', coalesce(:search, ''), '%'))
                    or lower(i.customer.name) like lower(concat('%', coalesce(:search, ''), '%')))
              and (:status is null or i.status = :status)
              and (:customerId is null or i.customer.id = :customerId)
            """)
    Page<InvoiceEntity> search(@Param("search") String search,
                               @Param("status") InvoiceStatus status,
                               @Param("customerId") Long customerId,
                               Pageable pageable);

    @Query(value = "select nextval('invoice_number_seq')", nativeQuery = true)
    Long nextInvoiceNumber();
}
