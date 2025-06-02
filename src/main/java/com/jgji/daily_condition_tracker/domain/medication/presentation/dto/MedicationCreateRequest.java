package com.jgji.daily_condition_tracker.domain.medication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicationCreateRequest(
        @NotBlank(message = "약 이름은 필수입니다.")
        @Size(max = 255, message = "약 이름은 최대 255자까지 입력 가능합니다.")
        String name,

        Integer dosage,

        @Size(max = 50, message = "단위 정보는 최대 50자까지 입력 가능합니다.")
        String unit,

        @Size(max = 65535, message = "설명은 최대 65535자까지 입력 가능합니다.")
        String description,

        boolean isActive
) {
} 