package com.jgji.daily_condition_tracker.domain.medication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record MedicationUpdateRequest(
    JsonNullable<@NotBlank(message = "약물 이름은 필수 입력 필드입니다.") @Size(max = 100, message = "약물 이름은 100자를 초과할 수 없습니다.") String> name,

    JsonNullable<Integer> dosage,

    JsonNullable<@Size(max = 50, message = "단위는 50자를 초과할 수 없습니다.") String> unit,

    JsonNullable<@Size(max = 65535, message = "설명은 65535자를 초과할 수 없습니다.") String> description,

    JsonNullable<Boolean> isActive
) {
} 