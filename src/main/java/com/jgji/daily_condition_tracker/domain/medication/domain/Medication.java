package com.jgji.daily_condition_tracker.domain.medication.domain;

import com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public final class Medication {

    private final Long medicationId;
    private final long userId;
    private final String name;
    private final Integer dosage;
    private final String unit;
    private final String description;
    private final boolean isActive;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Medication(Long medicationId, long userId, String name, Integer dosage, String unit,
                       String description, boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.medicationId = medicationId;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.unit = unit;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Medication create(long userId, String name, Integer dosage, String unit, String description, boolean isActive) {
        validateName(name);

        return Medication.builder()
                .userId(userId)
                .name(name)
                .dosage(dosage)
                .unit(unit)
                .description(description)
                .isActive(isActive)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static Medication of(MedicationEntity entity) {
        return Medication.builder()
                .medicationId(entity.getMedicationId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .dosage(entity.getDosage())
                .unit(entity.getUnit())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("약 이름은 필수값입니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("약 이름은 최대 255자까지 입력 가능합니다.");
        }
    }
} 