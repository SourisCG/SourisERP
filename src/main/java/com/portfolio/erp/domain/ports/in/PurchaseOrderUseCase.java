package com.portfolio.erp.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;

public interface PurchaseOrderUseCase {

    PageResult<PurchaseOrder> list(String search, PurchaseOrderStatus status, Long supplierId, int page, int size);

    PurchaseOrder get(Long id);

    PurchaseOrder create(PurchaseOrderCommand command);

    PurchaseOrder update(Long id, PurchaseOrderCommand command);

    PurchaseOrder approve(Long id);

    PurchaseOrder receive(Long id);

    PurchaseOrder cancel(Long id);

    record PurchaseOrderCommand(Long supplierId, LocalDate orderDate, String notes, List<LineCommand> lines) {
    }

    record LineCommand(Long productId, Long warehouseId, int quantity, BigDecimal unitCost) {
    }
}
