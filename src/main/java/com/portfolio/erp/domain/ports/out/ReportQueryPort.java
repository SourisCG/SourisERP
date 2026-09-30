package com.portfolio.erp.domain.ports.out;

import java.time.LocalDate;
import java.util.List;

import com.portfolio.erp.domain.model.InventoryValuationItem;
import com.portfolio.erp.domain.model.ReceivableItem;
import com.portfolio.erp.domain.model.SalesSummaryItem;

public interface ReportQueryPort {

    List<SalesSummaryItem> salesSummary(LocalDate from, LocalDate to);

    List<InventoryValuationItem> inventoryValuation();

    List<ReceivableItem> receivables();
}
