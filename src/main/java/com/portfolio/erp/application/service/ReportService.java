package com.portfolio.erp.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.DomainException;
import com.portfolio.erp.domain.model.InventoryValuationItem;
import com.portfolio.erp.domain.model.ReceivableItem;
import com.portfolio.erp.domain.model.SalesSummaryItem;
import com.portfolio.erp.domain.ports.in.ReportUseCase;
import com.portfolio.erp.domain.ports.out.ReportQueryPort;

@Service
@Transactional(readOnly = true)
public class ReportService implements ReportUseCase {

    private final ReportQueryPort queries;
    private final MessageSource messageSource;

    public ReportService(ReportQueryPort queries, MessageSource messageSource) {
        this.queries = queries;
        this.messageSource = messageSource;
    }

    @Override
    public List<SalesSummaryItem> salesSummary(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new DomainException("error.report.missingRange");
        }
        return queries.salesSummary(from, to);
    }

    @Override
    public List<InventoryValuationItem> inventoryValuation() {
        return queries.inventoryValuation();
    }

    @Override
    public List<ReceivableItem> receivables() {
        return queries.receivables();
    }

    public String salesSummaryCsv(LocalDate from, LocalDate to) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvHeader("report.period", "report.invoices", "report.subtotal", "report.tax", "report.total"));
        for (SalesSummaryItem item : salesSummary(from, to)) {
            csv.append(item.period()).append(';')
                    .append(item.invoiceCount()).append(';')
                    .append(item.subtotal()).append(';')
                    .append(item.taxTotal()).append(';')
                    .append(item.total()).append('\n');
        }
        return csv.toString();
    }

    public String inventoryValuationCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(csvHeader("report.warehouse", "report.quantity", "report.costValue", "report.retailValue"));
        for (InventoryValuationItem item : inventoryValuation()) {
            csv.append(item.warehouseCode()).append(';')
                    .append(item.totalQuantity()).append(';')
                    .append(item.totalCost()).append(';')
                    .append(item.totalRetail()).append('\n');
        }
        return csv.toString();
    }

    public String receivablesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(csvHeader("report.invoice", "report.customer", "report.dueDate", "report.total",
                "report.paid", "report.balance", "report.daysOverdue"));
        for (ReceivableItem item : receivables()) {
            csv.append(item.invoiceNumber()).append(';')
                    .append(item.customerName()).append(';')
                    .append(item.dueDate()).append(';')
                    .append(item.total()).append(';')
                    .append(item.paidAmount()).append(';')
                    .append(item.balance()).append(';')
                    .append(item.daysOverdue()).append('\n');
        }
        return csv.toString();
    }

    private String csvHeader(String... keys) {
        Locale locale = LocaleContextHolder.getLocale();
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            header.append(messageSource.getMessage(keys[i], null, keys[i], locale));
            header.append(i == keys.length - 1 ? '\n' : ';');
        }
        return header.toString();
    }
}
