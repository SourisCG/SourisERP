package com.portfolio.erp.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Customer {

    private final Long id;
    private String code;
    private String name;
    private String taxId;
    private String email;
    private String phone;
    private String address;
    private boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Customer(Long id, String code, String name, String taxId, String email, String phone,
                    String address, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer newCustomer(String code, String name, String taxId, String email,
                                       String phone, String address, boolean active) {
        return new Customer(null, code, name, taxId, email, phone, address, active, null, null);
    }

    public void updateDetails(String code, String name, String taxId, String email, String phone, String address) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
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

    public String getTaxId() {
        return taxId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
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
