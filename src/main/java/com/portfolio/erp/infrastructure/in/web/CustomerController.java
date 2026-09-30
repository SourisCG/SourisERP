package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.ports.in.CustomerUseCase;
import com.portfolio.erp.infrastructure.in.web.api.CustomersApi;
import com.portfolio.erp.infrastructure.in.web.dto.CustomerRequest;
import com.portfolio.erp.infrastructure.in.web.dto.CustomerResponse;
import com.portfolio.erp.infrastructure.in.web.dto.PageCustomer;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.out.mapper.SalesOrderMapper;

@RestController
public class CustomerController implements CustomersApi {

    private final CustomerUseCase customerUseCase;
    private final SalesOrderMapper mapper;

    public CustomerController(CustomerUseCase customerUseCase, SalesOrderMapper mapper) {
        this.customerUseCase = customerUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PageCustomer> listCustomers(Integer page, Integer size, String search, Boolean active) {
        PageResult<Customer> result = customerUseCase.list(search, active,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageCustomer response = new PageCustomer();
        response.setContent(result.content().stream().map(mapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<CustomerResponse> createCustomer(CustomerRequest customerRequest) {
        Customer created = customerUseCase.create(toCommand(customerRequest));
        return ResponseEntity.status(201).body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomer(Long id) {
        return ResponseEntity.ok(mapper.toResponse(customerUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<CustomerResponse> updateCustomer(Long id, CustomerRequest customerRequest) {
        return ResponseEntity.ok(mapper.toResponse(customerUseCase.update(id, toCommand(customerRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<Void> deactivateCustomer(Long id) {
        customerUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerUseCase.CustomerCommand toCommand(CustomerRequest request) {
        return new CustomerUseCase.CustomerCommand(
                request.getCode(),
                request.getName(),
                request.getTaxId(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                !Boolean.FALSE.equals(request.getActive()));
    }
}
