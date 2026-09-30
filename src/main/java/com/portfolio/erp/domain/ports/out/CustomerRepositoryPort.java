package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;

public interface CustomerRepositoryPort {

    PageResult<Customer> search(String search, Boolean active, int page, int size);

    Optional<Customer> findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Customer save(Customer customer);
}
