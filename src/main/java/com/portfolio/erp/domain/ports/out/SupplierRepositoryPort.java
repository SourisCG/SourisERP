package com.portfolio.erp.domain.ports.out;

import java.util.Optional;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Supplier;

public interface SupplierRepositoryPort {

    PageResult<Supplier> search(String search, Boolean active, int page, int size);

    Optional<Supplier> findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Supplier save(Supplier supplier);
}
