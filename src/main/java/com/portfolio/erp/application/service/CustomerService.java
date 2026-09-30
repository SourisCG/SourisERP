package com.portfolio.erp.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.in.CustomerUseCase;
import com.portfolio.erp.domain.ports.out.CustomerRepositoryPort;

@Service
@Transactional(readOnly = true)
public class CustomerService implements CustomerUseCase {

    private final CustomerRepositoryPort customers;

    public CustomerService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    public PageResult<Customer> list(String search, Boolean active, int page, int size) {
        return customers.search(search, active, page, size);
    }

    @Override
    public Customer get(Long id) {
        return requireCustomer(id);
    }

    @Override
    @Transactional
    public Customer create(CustomerCommand command) {
        if (customers.existsByCode(command.code())) {
            throw new ConflictException("error.customer.codeExists", command.code());
        }
        return customers.save(Customer.newCustomer(command.code(), command.name(), command.taxId(),
                command.email(), command.phone(), command.address(), command.active()));
    }

    @Override
    @Transactional
    public Customer update(Long id, CustomerCommand command) {
        Customer customer = requireCustomer(id);
        if (!customer.getCode().equalsIgnoreCase(command.code()) && customers.existsByCode(command.code())) {
            throw new ConflictException("error.customer.codeExists", command.code());
        }
        customer.updateDetails(command.code(), command.name(), command.taxId(),
                command.email(), command.phone(), command.address());
        customer.setActive(command.active());
        return customers.save(customer);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Customer customer = requireCustomer(id);
        customer.setActive(false);
        customers.save(customer);
    }

    private Customer requireCustomer(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.customer.notFound"));
    }
}
