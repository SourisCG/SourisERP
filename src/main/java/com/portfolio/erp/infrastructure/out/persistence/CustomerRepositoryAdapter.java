package com.portfolio.erp.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.out.CustomerRepositoryPort;
import com.portfolio.erp.infrastructure.out.entity.CustomerEntity;
import com.portfolio.erp.infrastructure.out.mapper.SalesOrderMapper;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository repository;
    private final SalesOrderMapper mapper;

    public CustomerRepositoryAdapter(CustomerJpaRepository repository, SalesOrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Customer> search(String search, Boolean active, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        Page<CustomerEntity> result = repository.search(normalized, active,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public java.util.Optional<Customer> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return repository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity;
        if (customer.getId() == null) {
            entity = mapper.toEntity(customer);
        } else {
            entity = repository.findById(customer.getId()).orElseThrow();
            mapper.updateEntity(customer, entity);
        }
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
