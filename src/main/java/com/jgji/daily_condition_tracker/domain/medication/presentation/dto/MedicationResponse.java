package com.jgji.daily_condition_tracker.domain.medication.presentation.dto;

import java.time.OffsetDateTime;

public record MedicationResponse(
        long medicationId,
        String name,
        Integer dosage,
        String unit,
        String description,
        boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MedicationResponse from(long medicationId, String name, Integer dosage, String unit,
                                        String description, boolean isActive, OffsetDateTime createdAt, 
                                        OffsetDateTime updatedAt) {
        return new MedicationResponse(medicationId, name, dosage, unit, description, isActive, createdAt, updatedAt);
    }
} 