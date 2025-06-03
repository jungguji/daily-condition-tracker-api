package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.shared.domain.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "medications")
public class MedicationEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_id")
    private Long medicationId;

    @Comment("해당 약을 등록한 사용자 ID")
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Comment("약 이름")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("용량 (e.g., 50mg, 1정)")
    @Column(name = "dosage", columnDefinition = "SMALLINT UNSIGNED")
    private Integer dosage;

    @Comment("단위 (e.g., mg, 정, ml)")
    @Column(name = "unit", length = 50)
    private String unit;

    @Comment("약에 대한 추가 설명/메모")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Comment("현재 복용중인 약 여부 (1: 활성, 0: 비활성)")
    @Column(name = "is_active", columnDefinition = "DEFAULT 1", nullable = false)
    private boolean isActive;

    // `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '약 소프트삭제 여부 (1: 삭제됨, 0: 활성)',
    @Comment("약 소프트삭제 여부 (1: 삭제됨, 0: 활성")
    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isDeleted;

    @Builder(access = AccessLevel.PRIVATE)
    private MedicationEntity(Long medicationId, long userId, String name, Integer dosage, String unit,
                           String description, boolean isActive, boolean isDeleted) {
        this.medicationId = medicationId;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.unit = unit;
        this.description = description;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
    }

    static MedicationEntity fromDomain(Medication medication) {
        return MedicationEntity.builder()
                .medicationId(medication.getMedicationId())
                .userId(medication.getUserId())
                .name(medication.getName())
                .dosage(medication.getDosage())
                .unit(medication.getUnit())
                .description(medication.getDescription())
                .isActive(medication.isActive())
                .isDeleted(medication.isDeleted())
                .build();
    }

    Medication toDomain() {
        return Medication.of(this);
    }
} 