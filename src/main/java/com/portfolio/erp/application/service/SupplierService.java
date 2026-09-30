package com.portfolio.erp.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Supplier;
import com.portfolio.erp.domain.ports.in.SupplierUseCase;
import com.portfolio.erp.domain.ports.out.SupplierRepositoryPort;

@Service
@Transactional(readOnly = true)
public class SupplierService implements SupplierUseCase {

    private final SupplierRepositoryPort suppliers;

    public SupplierService(SupplierRepositoryPort suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public PageResult<Supplier> list(String search, Boolean active, int page, int size) {
        return suppliers.search(search, active, page, size);
    }

    @Override
    public Supplier get(Long id) {
        return requireSupplier(id);
    }

    @Override
    @Transactional
    public Supplier create(SupplierCommand command) {
        if (suppliers.existsByCode(command.code())) {
            throw new ConflictException("error.supplier.codeExists", command.code());
        }
        return suppliers.save(Supplier.newSupplier(command.code(), command.name(), command.taxId(),
                command.email(), command.phone(), command.address(), command.active()));
    }

    @Override
    @Transactional
    public Supplier update(Long id, SupplierCommand command) {
        Supplier supplier = requireSupplier(id);
        if (!supplier.getCode().equalsIgnoreCase(command.code()) && suppliers.existsByCode(command.code())) {
            throw new ConflictException("error.supplier.codeExists", command.code());
        }
        supplier.updateDetails(command.code(), command.name(), command.taxId(),
                command.email(), command.phone(), command.address());
        supplier.setActive(command.active());
        return suppliers.save(supplier);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Supplier supplier = requireSupplier(id);
        supplier.setActive(false);
        suppliers.save(supplier);
    }

    private Supplier requireSupplier(Long id) {
        return suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.supplier.notFound"));
    }
}
