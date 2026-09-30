package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;
import com.portfolio.erp.domain.ports.in.PurchaseOrderUseCase;
import com.portfolio.erp.infrastructure.in.web.api.PurchaseOrdersApi;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PagePurchaseOrder;
import com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderLineRequest;
import com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderRequest;
import com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderResponse;
import com.portfolio.erp.infrastructure.out.mapper.PurchaseOrderMapper;

@RestController
public class PurchaseOrderController implements PurchaseOrdersApi {

    private final PurchaseOrderUseCase purchaseOrderUseCase;
    private final PurchaseOrderMapper mapper;

    public PurchaseOrderController(PurchaseOrderUseCase purchaseOrderUseCase, PurchaseOrderMapper mapper) {
        this.purchaseOrderUseCase = purchaseOrderUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PagePurchaseOrder> listPurchaseOrders(
            Integer page, Integer size, String search,
            com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderStatus status,
            Long supplierId) {
        PurchaseOrderStatus domainStatus = status == null ? null : PurchaseOrderStatus.valueOf(status.getValue());
        PageResult<PurchaseOrder> result = purchaseOrderUseCase.list(search, domainStatus, supplierId,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PagePurchaseOrder response = new PagePurchaseOrder();
        response.setContent(result.content().stream().map(mapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(PurchaseOrderRequest purchaseOrderRequest) {
        PurchaseOrder created = purchaseOrderUseCase.create(toCommand(purchaseOrderRequest));
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(purchaseOrderUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(Long id,
                                                                     PurchaseOrderRequest purchaseOrderRequest) {
        return ResponseEntity.ok(mapper.toResponse(purchaseOrderUseCase.update(id, toCommand(purchaseOrderRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(purchaseOrderUseCase.approve(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(purchaseOrderUseCase.receive(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(Long id) {
        return ResponseEntity.ok(mapper.toResponse(purchaseOrderUseCase.cancel(id)));
    }

    private PurchaseOrderUseCase.PurchaseOrderCommand toCommand(PurchaseOrderRequest request) {
        List<PurchaseOrderUseCase.LineCommand> lines = request.getLines().stream()
                .map(this::toLineCommand)
                .toList();
        return new PurchaseOrderUseCase.PurchaseOrderCommand(
                request.getSupplierId(),
                request.getOrderDate(),
                request.getNotes(),
                lines);
    }

    private PurchaseOrderUseCase.LineCommand toLineCommand(PurchaseOrderLineRequest line) {
        return new PurchaseOrderUseCase.LineCommand(
                line.getProductId(),
                line.getWarehouseId(),
                line.getQuantity(),
                line.getUnitCost());
    }
}
