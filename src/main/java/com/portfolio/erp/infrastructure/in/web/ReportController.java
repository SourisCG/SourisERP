package com.portfolio.erp.infrastructure.in.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.application.service.ReportService;
import com.portfolio.erp.infrastructure.in.web.api.ReportsApi;
import com.portfolio.erp.infrastructure.out.mapper.ReportMapper;

@RestController
public class ReportController implements ReportsApi {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    public ReportController(ReportService reportService, ReportMapper reportMapper) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
    }

    @Override
    public ResponseEntity<List<com.portfolio.erp.infrastructure.in.web.dto.SalesSummaryItem>> salesSummary(
            LocalDate from, LocalDate to) {
        return ResponseEntity.ok(reportService.salesSummary(from, to).stream()
                .map(reportMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<List<com.portfolio.erp.infrastructure.in.web.dto.InventoryValuationItem>> inventoryValuation() {
        return ResponseEntity.ok(reportService.inventoryValuation().stream()
                .map(reportMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<List<com.portfolio.erp.infrastructure.in.web.dto.ReceivableItem>> receivables() {
        return ResponseEntity.ok(reportService.receivables().stream()
                .map(reportMapper::toResponse)
                .toList());
    }

    @Override
    public ResponseEntity<String> exportReportCsv(String report, LocalDate from, LocalDate to) {
        String csv = switch (report) {
            case "sales-summary" -> reportService.salesSummaryCsv(from, to);
            case "inventory-valuation" -> reportService.inventoryValuationCsv();
            case "receivables" -> reportService.receivablesCsv();
            default -> throw new IllegalArgumentException("Unknown report: " + report);
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report + ".csv\"")
                .body("\uFEFF" + csv);
    }
}
