package com.portfolio.erp.infrastructure.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.Warehouse;
import com.portfolio.erp.domain.ports.in.WarehouseUseCase;
import com.portfolio.erp.infrastructure.in.web.api.WarehousesApi;
import com.portfolio.erp.infrastructure.in.web.dto.WarehouseRequest;
import com.portfolio.erp.infrastructure.in.web.dto.WarehouseResponse;
import com.portfolio.erp.infrastructure.out.mapper.WarehouseMapper;

@RestController
public class WarehouseController implements WarehousesApi {

    private final WarehouseUseCase warehouseUseCase;
    private final WarehouseMapper warehouseMapper;

    public WarehouseController(WarehouseUseCase warehouseUseCase, WarehouseMapper warehouseMapper) {
        this.warehouseUseCase = warehouseUseCase;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    public ResponseEntity<List<WarehouseResponse>> listWarehouses() {
        return ResponseEntity.ok(warehouseUseCase.list().stream().map(warehouseMapper::toResponse).toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<WarehouseResponse> createWarehouse(WarehouseRequest warehouseRequest) {
        Warehouse created = warehouseUseCase.create(toCommand(warehouseRequest));
        return ResponseEntity.status(201).body(warehouseMapper.toResponse(created));
    }

    @Override
    public ResponseEntity<WarehouseResponse> getWarehouse(Long id) {
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouseUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<WarehouseResponse> updateWarehouse(Long id, WarehouseRequest warehouseRequest) {
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouseUseCase.update(id, toCommand(warehouseRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> deleteWarehouse(Long id) {
        warehouseUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private WarehouseUseCase.WarehouseCommand toCommand(WarehouseRequest request) {
        return new WarehouseUseCase.WarehouseCommand(
                request.getCode(),
                request.getName(),
                request.getAddress(),
                !Boolean.FALSE.equals(request.getActive()));
    }
}
