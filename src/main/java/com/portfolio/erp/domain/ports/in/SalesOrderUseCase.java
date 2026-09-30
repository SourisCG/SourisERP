package com.portfolio.erp.domain.ports.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderStatus;

public interface SalesOrderUseCase {

    PageResult<SalesOrder> list(String search, SalesOrderStatus status, Long customerId, int page, int size);

    SalesOrder get(Long id);

    SalesOrder create(SalesOrderCommand command);

    SalesOrder update(Long id, SalesOrderCommand command);

    SalesOrder confirm(Long id);

    SalesOrder cancel(Long id);

    SalesOrder markInvoiced(Long id);

    record SalesOrderCommand(Long customerId, LocalDate orderDate, String notes, List<LineCommand> lines) {
    }

    record LineCommand(Long productId, Long warehouseId, int quantity, BigDecimal unitPrice, BigDecimal discount) {
    }
}
