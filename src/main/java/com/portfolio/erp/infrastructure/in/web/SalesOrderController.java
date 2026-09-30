package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.domain.ports.in.SalesOrderUseCase;
import com.portfolio.erp.infrastructure.in.web.api.SalesOrdersApi;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PageSalesOrder;
import com.portfolio.erp.infrastructure.in.web.dto.SalesOrderLineRequest;
import com.portfolio.erp.infrastructure.in.web.dto.SalesOrderRequest;
import com.portfolio.erp.infrastructure.in.web.dto.SalesOrderResponse;
import com.portfolio.erp.infrastructure.out.mapper.SalesOrderMapper;

@RestController
public class SalesOrderController implements SalesOrdersApi {

    private final SalesOrderUseCase salesOrderUseCase;
    private final SalesOrderMapper mapper;

    public SalesOrderController(SalesOrderUseCase salesOrderUseCase, SalesOrderMapper mapper) {
        this.salesOrderUseCase = salesOrderUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PageSalesOrder> listSalesOrders(Integer page, Integer size, String search,
                                                          com.portfolio.erp.infrastructure.in.web.dto.SalesOrderStatus status,
                                                          Long customerId) {
        SalesOrderStatus domainStatus = status == null ? null : SalesOrderStatus.valueOf(status.getValue());
        PageResult<SalesOrder> result = salesOrderUseCase.list(search, domainStatus, customerId,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageSalesOrder response = new PageSalesOrder();
        response.setContent(result.content().stream().map(mapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<SalesOrderResponse> createSalesOrder(SalesOrderRequest salesOrderRequest) {
        SalesOrder created = salesOrderUseCase.create(toCommand(salesOrderRequest));
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<SalesOrderResponse> getSalesOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(salesOrderUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<SalesOrderResponse> updateSalesOrder(Long id, SalesOrderRequest salesOrderRequest) {
        return ResponseEntity.ok(mapper.toResponse(salesOrderUseCase.update(id, toCommand(salesOrderRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<SalesOrderResponse> confirmSalesOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(salesOrderUseCase.confirm(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<SalesOrderResponse> cancelSalesOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(salesOrderUseCase.cancel(id)));
    }

    private SalesOrderUseCase.SalesOrderCommand toCommand(SalesOrderRequest request) {
        List<SalesOrderUseCase.LineCommand> lines = request.getLines().stream()
                .map(this::toLineCommand)
                .toList();
        return new SalesOrderUseCase.SalesOrderCommand(
                request.getCustomerId(),
                request.getOrderDate(),
                request.getNotes(),
                lines);
    }

    private SalesOrderUseCase.LineCommand toLineCommand(SalesOrderLineRequest line) {
        return new SalesOrderUseCase.LineCommand(
                line.getProductId(),
                line.getWarehouseId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getDiscount());
    }
}
