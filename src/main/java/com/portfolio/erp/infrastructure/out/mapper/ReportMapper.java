package com.portfolio.erp.infrastructure.out.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Maps report query results (domain records) to generated API DTOs.
 * Fully-qualified names avoid clashes between domain and DTO classes.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportMapper {

    com.portfolio.erp.infrastructure.in.web.dto.SalesSummaryItem toResponse(
            com.portfolio.erp.domain.model.SalesSummaryItem item);

    com.portfolio.erp.infrastructure.in.web.dto.InventoryValuationItem toResponse(
            com.portfolio.erp.domain.model.InventoryValuationItem item);

    com.portfolio.erp.infrastructure.in.web.dto.ReceivableItem toResponse(
            com.portfolio.erp.domain.model.ReceivableItem item);
}
