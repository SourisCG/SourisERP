package com.portfolio.erp.domain.ports.in;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Supplier;

public interface SupplierUseCase {

    PageResult<Supplier> list(String search, Boolean active, int page, int size);

    Supplier get(Long id);

    Supplier create(SupplierCommand command);

    Supplier update(Long id, SupplierCommand command);

    void deactivate(Long id);

    record SupplierCommand(String code, String name, String taxId, String email, String phone,
                           String address, boolean active) {
    }
}
