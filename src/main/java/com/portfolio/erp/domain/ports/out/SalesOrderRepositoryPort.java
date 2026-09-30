package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderStatus;

public interface SalesOrderRepositoryPort {

    PageResult<SalesOrder> search(String search, SalesOrderStatus status, Long customerId, int page, int size);

    Optional<SalesOrder> findById(Long id);

    SalesOrder save(SalesOrder order);

    long nextOrderNumber();
}
