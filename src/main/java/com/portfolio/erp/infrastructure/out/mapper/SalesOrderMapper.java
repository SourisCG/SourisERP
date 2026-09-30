package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderLine;
import com.portfolio.erp.infrastructure.in.web.dto.CustomerResponse;
import com.portfolio.erp.infrastructure.in.web.dto.SalesOrderLineResponse;
import com.portfolio.erp.infrastructure.in.web.dto.SalesOrderResponse;
import com.portfolio.erp.infrastructure.out.entity.CustomerEntity;
import com.portfolio.erp.infrastructure.out.entity.SalesOrderEntity;
import com.portfolio.erp.infrastructure.out.entity.SalesOrderLineEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesOrderMapper {

    Customer toDomain(CustomerEntity entity);

    @Mapping(target = "id", ignore = true)
    CustomerEntity toEntity(Customer customer);

    @Mapping(target = "id", ignore = true)
    void updateEntity(Customer customer, @MappingTarget CustomerEntity entity);

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "subtotal", expression = "java(order.subtotal())")
    @Mapping(target = "taxTotal", expression = "java(order.taxTotal())")
    @Mapping(target = "total", expression = "java(order.total())")
    SalesOrderResponse toResponse(SalesOrder order);

    @Mapping(target = "lineTotal", expression = "java(line.lineTotal())")
    SalesOrderLineResponse toLineResponse(SalesOrderLine line);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    SalesOrderLine toLineDomain(SalesOrderLineEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lines", ignore = true)
    SalesOrderEntity toEntity(SalesOrder order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lines", ignore = true)
    void updateEntity(SalesOrder order, @MappingTarget SalesOrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    SalesOrderLineEntity toLineEntity(SalesOrderLine line);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    SalesOrder toDomain(SalesOrderEntity entity);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
