package com.portfolio.erp.infrastructure.out.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderLine;
import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.domain.ports.out.SalesOrderRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.SalesOrderEntity;
import com.portfolio.erp.infrastructure.out.entity.SalesOrderLineEntity;
import com.portfolio.erp.infrastructure.out.mapper.SalesOrderMapper;

@Repository
public class SalesOrderRepositoryAdapter implements SalesOrderRepositoryPort {

    private final SalesOrderJpaRepository repository;
    private final CustomerJpaRepository customerRepository;
    private final ProductJpaRepository productRepository;
    private final SalesOrderMapper mapper;

    public SalesOrderRepositoryAdapter(SalesOrderJpaRepository repository,
                                       CustomerJpaRepository customerRepository,
                                       ProductJpaRepository productRepository,
                                       SalesOrderMapper mapper) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<SalesOrder> search(String search, SalesOrderStatus status, Long customerId,
                                         int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<SalesOrderEntity> result = repository.search(normalized, status, customerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public Optional<SalesOrder> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public SalesOrder save(SalesOrder order) {
        SalesOrderEntity entity;
        if (order.getId() == null) {
            entity = mapper.toEntity(order);
        } else {
            entity = repository.findById(order.getId()).orElseThrow();
            mapper.updateEntity(order, entity);
        }
        entity.setCustomer(customerRepository.getReferenceById(order.getCustomerId()));

        entity.getLines().clear();
        for (SalesOrderLine line : order.getLines()) {
            SalesOrderLineEntity lineEntity = mapper.toLineEntity(line);
            lineEntity.setProduct(productRepository.getReferenceById(line.getProductId()));
            lineEntity.setOrder(entity);
            entity.getLines().add(lineEntity);
        }

        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public long nextOrderNumber() {
        return repository.nextOrderNumber();
    }
}
