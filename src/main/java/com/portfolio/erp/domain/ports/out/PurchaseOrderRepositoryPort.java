package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;

public interface PurchaseOrderRepositoryPort {

    PageResult<PurchaseOrder> search(String search, PurchaseOrderStatus status, Long supplierId, int page, int size);

    Optional<PurchaseOrder> findById(Long id);

    PurchaseOrder save(PurchaseOrder order);

    long nextOrderNumber();
}
