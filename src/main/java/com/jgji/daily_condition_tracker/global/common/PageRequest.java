package com.jgji.daily_condition_tracker.global.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequest(
        int page,
        int size,
        String sortBy,
        String direction
) {
    public static PageRequest of(int page, int size, String sortBy, String direction) {
        assert sortBy != null && !sortBy.isBlank() : "정렬 기준은 null이거나 비어있을 수 없습니다.";

        return new PageRequest(
                Math.max(0, page),
                Math.max(1, size),
                sortBy,
                direction != null ? direction : "desc"
        );
    }
    
    public Pageable toPageable() {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );
    }
} 