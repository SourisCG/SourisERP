package com.portfolio.erp.domain.ports.out;

/**
 * Output port for demo data management (portfolio mode).
 */
public interface DemoDataPort {

    void clearBusinessData();

    boolean isSeeded();

    void seed();
}
