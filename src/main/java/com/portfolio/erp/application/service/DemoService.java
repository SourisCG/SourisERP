package com.portfolio.erp.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.portfolio.erp.application.config.DemoProperties;
import com.portfolio.erp.domain.exception.ConflictException;
import com.portfolio.erp.domain.ports.in.DemoUseCase;
import com.portfolio.erp.domain.ports.out.DemoDataPort;

/**
 * Demo mode orchestration: resets the database to its seeded state on demand
 * (POST /api/v1/demo/reset) and every night, so recruiters always find fresh,
 * coherent data.
 */
@Service
public class DemoService implements DemoUseCase {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final DemoProperties properties;
    private final DemoDataPort demoData;

    public DemoService(DemoProperties properties, DemoDataPort demoData) {
        this.properties = properties;
        this.demoData = demoData;
    }

    @Override
    public void reset() {
        if (!properties.mode()) {
            throw new ConflictException("error.demo.disabled");
        }
        log.info("Resetting demo data...");
        demoData.clearBusinessData();
        demoData.seed();
        log.info("Demo data reset completed");
    }

    @Scheduled(cron = "${app.demo.nightly-reset-cron:0 0 4 * * *}")
    public void nightlyReset() {
        if (properties.mode()) {
            reset();
        }
    }
}
