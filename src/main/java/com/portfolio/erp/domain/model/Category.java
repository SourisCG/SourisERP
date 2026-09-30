package com.portfolio.erp.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Category {

    private final Long id;
    private final String name;
    private final String description;
    private final Long parentId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Category(Long id, String name, String description, Long parentId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Category newCategory(String name, String description, Long parentId) {
        return new Category(null, name, description, parentId, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getParentId() {
        return parentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
