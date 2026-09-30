package com.portfolio.erp.infrastructure.out.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceLine;
import com.portfolio.erp.domain.model.InvoicePayment;
import com.portfolio.erp.infrastructure.in.web.dto.InvoiceLineResponse;
import com.portfolio.erp.infrastructure.in.web.dto.InvoiceResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PaymentResponse;
import com.portfolio.erp.infrastructure.out.entity.InvoiceEntity;
import com.portfolio.erp.infrastructure.out.entity.InvoiceLineEntity;
import com.portfolio.erp.infrastructure.out.entity.InvoicePaymentEntity;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "salesOrderNumber", source = "salesOrder.number")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    Invoice toDomain(InvoiceEntity entity);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    InvoiceLine toLineDomain(InvoiceLineEntity entity);

    InvoicePayment toPaymentDomain(InvoicePaymentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "payments", ignore = true)
    InvoiceEntity toEntity(Invoice invoice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(Invoice invoice, @MappingTarget InvoiceEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "product", ignore = true)
    InvoiceLineEntity toLineEntity(InvoiceLine line);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    InvoicePaymentEntity toPaymentEntity(InvoicePayment payment);

    @Mapping(target = "balance", expression = "java(invoice.balance())")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(target = "lineTotal", expression = "java(line.lineTotal())")
    @Mapping(target = "lineTax", expression = "java(line.lineTax())")
    InvoiceLineResponse toLineResponse(InvoiceLine line);

    PaymentResponse toPaymentResponse(InvoicePayment payment);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
