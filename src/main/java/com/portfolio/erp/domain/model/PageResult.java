package com.portfolio.erp.domain.model;

import java.util.List;

/**
 * Framework-agnostic page of results.
 */
public record PageResult<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResult<T> of(List<T> content, int number, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, number, size, totalElements, totalPages);
    }
}
