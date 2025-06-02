package com.jgji.daily_condition_tracker.domain.medication.presentation.dto;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;

import java.time.OffsetDateTime;

public record MedicationSummaryResponse(
        Long medicationId,
        String name,
        Integer dosage,
        String unit,
        boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MedicationSummaryResponse from(Medication medication) {
        return new MedicationSummaryResponse(
                medication.getMedicationId(),
                medication.getName(),
                medication.getDosage(),
                medication.getUnit(),
                medication.isActive(),
                medication.getCreatedAt(),
                medication.getUpdatedAt()
        );
    }
} 