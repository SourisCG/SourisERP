package com.portfolio.erp.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Warehouse {

    private final Long id;
    private String code;
    private String name;
    private String address;
    private boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Warehouse(Long id, String code, String name, String address, boolean active,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Warehouse newWarehouse(String code, String name, String address, boolean active) {
        return new Warehouse(null, code, name, address, active, null, null);
    }

    public void updateDetails(String code, String name, String address) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.address = address;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
