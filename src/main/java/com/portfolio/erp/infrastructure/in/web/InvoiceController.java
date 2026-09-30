package com.portfolio.erp.infrastructure.in.web;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.in.InvoiceUseCase;
import com.portfolio.erp.infrastructure.in.web.api.InvoicesApi;
import com.portfolio.erp.infrastructure.in.web.dto.InvoiceRequest;
import com.portfolio.erp.infrastructure.in.web.dto.InvoiceResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PageInvoice;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PaymentRequest;
import com.portfolio.erp.infrastructure.out.mapper.InvoiceMapper;
import com.portfolio.erp.infrastructure.out.report.InvoicePdfRenderer;

@RestController
public class InvoiceController implements InvoicesApi {

    private final InvoiceUseCase invoiceUseCase;
    private final InvoiceMapper mapper;
    private final InvoicePdfRenderer pdfRenderer;

    public InvoiceController(InvoiceUseCase invoiceUseCase, InvoiceMapper mapper, InvoicePdfRenderer pdfRenderer) {
        this.invoiceUseCase = invoiceUseCase;
        this.mapper = mapper;
        this.pdfRenderer = pdfRenderer;
    }

    @Override
    public ResponseEntity<PageInvoice> listInvoices(Integer page, Integer size, String search,
                                                    com.portfolio.erp.infrastructure.in.web.dto.InvoiceStatus status,
                                                    Long customerId) {
        com.portfolio.erp.domain.model.InvoiceStatus domainStatus =
                status == null ? null : com.portfolio.erp.domain.model.InvoiceStatus.valueOf(status.getValue());
        PageResult<Invoice> result = invoiceUseCase.list(search, domainStatus, customerId,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageInvoice response = new PageInvoice();
        response.setContent(result.content().stream().map(mapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<InvoiceResponse> createInvoice(InvoiceRequest invoiceRequest) {
        Invoice created = invoiceUseCase.createFromOrder(invoiceRequest.getSalesOrderId(), invoiceRequest.getDueDate());
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<InvoiceResponse> getInvoice(Long id) {
        return ResponseEntity.ok(mapper.toResponse(invoiceUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public ResponseEntity<InvoiceResponse> registerPayment(Long id, PaymentRequest paymentRequest) {
        Invoice updated = invoiceUseCase.registerPayment(id, paymentRequest.getAmount(),
                paymentRequest.getMethod() == null ? "OTHER" : paymentRequest.getMethod().getValue(),
                paymentRequest.getReference());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoicePdf(Long id) {
        Invoice invoice = invoiceUseCase.get(id);
        Locale locale = LocaleContextHolder.getLocale();
        byte[] pdf = pdfRenderer.render(invoice, locale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoice.getNumber() + ".pdf\"")
                .body(new org.springframework.core.io.ByteArrayResource(pdf));
    }
}
