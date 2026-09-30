package com.portfolio.erp.domain.ports.in;

import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;

public interface CustomerUseCase {

    PageResult<Customer> list(String search, Boolean active, int page, int size);

    Customer get(Long id);

    Customer create(CustomerCommand command);

    Customer update(Long id, CustomerCommand command);

    void deactivate(Long id);

    record CustomerCommand(String code, String name, String taxId, String email, String phone,
                           String address, boolean active) {
    }
}
