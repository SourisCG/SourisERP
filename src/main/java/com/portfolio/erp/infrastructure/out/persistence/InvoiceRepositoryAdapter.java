package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceLine;
import com.portfolio.erp.domain.model.InvoicePayment;
import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.out.InvoiceRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.InvoiceEntity;
import com.portfolio.erp.infrastructure.out.entity.InvoiceLineEntity;
import com.portfolio.erp.infrastructure.out.entity.InvoicePaymentEntity;
import com.portfolio.erp.infrastructure.out.mapper.InvoiceMapper;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final InvoiceJpaRepository repository;
    private final SalesOrderJpaRepository salesOrderRepository;
    private final CustomerJpaRepository customerRepository;
    private final ProductJpaRepository productRepository;
    private final InvoiceMapper mapper;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository repository,
                                    SalesOrderJpaRepository salesOrderRepository,
                                    CustomerJpaRepository customerRepository,
                                    ProductJpaRepository productRepository,
                                    InvoiceMapper mapper) {
        this.repository = repository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Invoice> search(String search, InvoiceStatus status, Long customerId, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<InvoiceEntity> result = repository.search(normalized, status, customerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity;
        if (invoice.getId() == null) {
            entity = mapper.toEntity(invoice);
        } else {
            entity = repository.findById(invoice.getId()).orElseThrow();
            mapper.updateEntity(invoice, entity);
        }
        entity.setSalesOrder(salesOrderRepository.getReferenceById(invoice.getSalesOrderId()));
        entity.setCustomer(customerRepository.getReferenceById(invoice.getCustomerId()));

        if (entity.getLines().isEmpty()) {
            for (InvoiceLine line : invoice.getLines()) {
                InvoiceLineEntity lineEntity = mapper.toLineEntity(line);
                lineEntity.setProduct(productRepository.getReferenceById(line.getProductId()));
                lineEntity.setInvoice(entity);
                entity.getLines().add(lineEntity);
            }
        }

        entity.getPayments().clear();
        for (InvoicePayment payment : invoice.getPayments()) {
            InvoicePaymentEntity paymentEntity = mapper.toPaymentEntity(payment);
            paymentEntity.setInvoice(entity);
            entity.getPayments().add(paymentEntity);
        }

        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public long nextInvoiceNumber() {
        return repository.nextInvoiceNumber();
    }
}
