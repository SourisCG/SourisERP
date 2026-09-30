package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.ports.in.DemoUseCase;
import com.portfolio.erp.infrastructure.in.web.api.DemoApi;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class DemoController implements DemoApi {

    private final DemoUseCase demoUseCase;

    public DemoController(DemoUseCase demoUseCase) {
        this.demoUseCase = demoUseCase;
    }

    @Override
    public ResponseEntity<Void> resetDemoData() {
        demoUseCase.reset();
        return ResponseEntity.noContent().build();
    }
}
