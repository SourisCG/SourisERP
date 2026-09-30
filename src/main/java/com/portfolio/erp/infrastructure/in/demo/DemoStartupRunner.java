package com.portfolio.erp.infrastructure.in.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.portfolio.erp.application.config.DemoProperties;
import com.portfolio.erp.domain.ports.out.DemoDataPort;

/**
 * Seeds the demo dataset on startup when the database is empty. Kept separate
 * from the seeder itself so the transactional proxy is applied.
 */
@Component
public class DemoStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoStartupRunner.class);

    private final DemoProperties properties;
    private final DemoDataPort demoData;

    public DemoStartupRunner(DemoProperties properties, DemoDataPort demoData) {
        this.properties = properties;
        this.demoData = demoData;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.mode() && !demoData.isSeeded()) {
            log.info("Demo mode: seeding initial business data...");
            demoData.seed();
            log.info("Demo data ready");
        }
    }
}
