package com.acme.salary.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Pagination envelope matching the shape documented in ARCHITECTURE.md's API design section. */
public record PageResponse<T>(
        List<T> items,
        int page,
        int limit,
        long total,
        int totalPages,
        boolean hasNext,
        boolean hasPrev
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
