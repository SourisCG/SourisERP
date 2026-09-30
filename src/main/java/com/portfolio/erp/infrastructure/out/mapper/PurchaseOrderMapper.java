package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderLine;
import com.portfolio.erp.domain.model.Supplier;
import com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderLineResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PurchaseOrderResponse;
import com.portfolio.erp.infrastructure.in.web.dto.SupplierResponse;
import com.portfolio.erp.infrastructure.out.entity.PurchaseOrderEntity;
import com.portfolio.erp.infrastructure.out.entity.PurchaseOrderLineEntity;
import com.portfolio.erp.infrastructure.out.entity.SupplierEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseOrderMapper {

    Supplier toDomain(SupplierEntity entity);

    @Mapping(target = "id", ignore = true)
    SupplierEntity toEntity(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    void updateEntity(Supplier supplier, @MappingTarget SupplierEntity entity);

    SupplierResponse toResponse(Supplier supplier);

    @Mapping(target = "subtotal", expression = "java(order.subtotal())")
    PurchaseOrderResponse toResponse(PurchaseOrder order);

    @Mapping(target = "lineTotal", expression = "java(line.lineTotal())")
    PurchaseOrderLineResponse toLineResponse(PurchaseOrderLine line);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    PurchaseOrderLine toLineDomain(PurchaseOrderLineEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "lines", ignore = true)
    PurchaseOrderEntity toEntity(PurchaseOrder order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "lines", ignore = true)
    void updateEntity(PurchaseOrder order, @MappingTarget PurchaseOrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    PurchaseOrderLineEntity toLineEntity(PurchaseOrderLine line);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    PurchaseOrder toDomain(PurchaseOrderEntity entity);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
