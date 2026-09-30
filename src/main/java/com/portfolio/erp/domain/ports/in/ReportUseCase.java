package com.portfolio.erp.domain.ports.in;

import java.time.LocalDate;
import java.util.List;

import com.portfolio.erp.domain.model.InventoryValuationItem;
import com.portfolio.erp.domain.model.ReceivableItem;
import com.portfolio.erp.domain.model.SalesSummaryItem;

public interface ReportUseCase {

    List<SalesSummaryItem> salesSummary(LocalDate from, LocalDate to);

    List<InventoryValuationItem> inventoryValuation();

    List<ReceivableItem> receivables();
}
