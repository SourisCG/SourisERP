package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.StockMovement;
import com.portfolio.erp.domain.ports.in.InventoryUseCase;
import com.portfolio.erp.infrastructure.in.web.api.InventoryApi;
import com.portfolio.erp.infrastructure.in.web.dto.AdjustmentRequest;
import com.portfolio.erp.infrastructure.in.web.dto.InventoryItemResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PageStockMovement;
import com.portfolio.erp.infrastructure.in.web.dto.TransferRequest;
import com.portfolio.erp.infrastructure.out.mapper.InventoryItemMapper;
import com.portfolio.erp.infrastructure.out.mapper.StockMovementMapper;

@RestController
public class InventoryController implements InventoryApi {

    private final InventoryUseCase inventoryUseCase;
    private final InventoryItemMapper itemMapper;
    private final StockMovementMapper movementMapper;

    public InventoryController(InventoryUseCase inventoryUseCase,
                               InventoryItemMapper itemMapper,
                               StockMovementMapper movementMapper) {
        this.inventoryUseCase = inventoryUseCase;
        this.itemMapper = itemMapper;
        this.movementMapper = movementMapper;
    }

    @Override
    public ResponseEntity<List<InventoryItemResponse>> listInventory(Long warehouseId, Long productId,
                                                                     Integer lowStockThreshold) {
        return ResponseEntity.ok(inventoryUseCase.list(warehouseId, productId, lowStockThreshold).stream()
                .map(itemMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<PageStockMovement> listStockMovements(Integer page, Integer size,
                                                                Long productId, Long warehouseId,
                                                                com.portfolio.erp.infrastructure.in.web.dto.MovementType type) {
        MovementType movementType = type == null ? null : MovementType.valueOf(type.getValue());
        PageResult<StockMovement> result = inventoryUseCase.listMovements(productId, warehouseId, movementType,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageStockMovement response = new PageStockMovement();
        response.setContent(result.content().stream().map(movementMapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<InventoryItemResponse> adjustStock(AdjustmentRequest adjustmentRequest) {
        InventoryItem item = inventoryUseCase.adjust(
                adjustmentRequest.getProductId(),
                adjustmentRequest.getWarehouseId(),
                adjustmentRequest.getNewQuantity(),
                adjustmentRequest.getReason());
        return ResponseEntity.ok(itemMapper.toResponse(item));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> transferStock(TransferRequest transferRequest) {
        inventoryUseCase.transfer(
                transferRequest.getProductId(),
                transferRequest.getFromWarehouseId(),
                transferRequest.getToWarehouseId(),
                transferRequest.getQuantity(),
                transferRequest.getNotes());
        return ResponseEntity.noContent().build();
    }
}
