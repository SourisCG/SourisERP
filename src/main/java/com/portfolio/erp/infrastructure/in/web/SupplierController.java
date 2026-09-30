package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Supplier;
import com.portfolio.erp.domain.ports.in.SupplierUseCase;
import com.portfolio.erp.infrastructure.in.web.api.SuppliersApi;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PageSupplier;
import com.portfolio.erp.infrastructure.in.web.dto.SupplierRequest;
import com.portfolio.erp.infrastructure.in.web.dto.SupplierResponse;
import com.portfolio.erp.infrastructure.out.mapper.PurchaseOrderMapper;

@RestController
public class SupplierController implements SuppliersApi {

    private final SupplierUseCase supplierUseCase;
    private final PurchaseOrderMapper mapper;

    public SupplierController(SupplierUseCase supplierUseCase, PurchaseOrderMapper mapper) {
        this.supplierUseCase = supplierUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PageSupplier> listSuppliers(Integer page, Integer size, String search, Boolean active) {
        PageResult<Supplier> result = supplierUseCase.list(search, active,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageSupplier response = new PageSupplier();
        response.setContent(result.content().stream().map(mapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<SupplierResponse> createSupplier(SupplierRequest supplierRequest) {
        Supplier created = supplierUseCase.create(toCommand(supplierRequest));
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<SupplierResponse> getSupplier(Long id) {
        return ResponseEntity.ok(mapper.toResponse(supplierUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<SupplierResponse> updateSupplier(Long id, SupplierRequest supplierRequest) {
        return ResponseEntity.ok(mapper.toResponse(supplierUseCase.update(id, toCommand(supplierRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> deactivateSupplier(Long id) {
        supplierUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private SupplierUseCase.SupplierCommand toCommand(SupplierRequest request) {
        return new SupplierUseCase.SupplierCommand(
                request.getCode(),
                request.getName(),
                request.getTaxId(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                !Boolean.FALSE.equals(request.getActive()));
    }
}
