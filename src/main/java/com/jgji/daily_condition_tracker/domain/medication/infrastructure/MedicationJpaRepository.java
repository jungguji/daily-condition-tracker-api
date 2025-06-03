package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MedicationJpaRepository extends JpaRepository<MedicationEntity, Long> {
    Optional<MedicationEntity> findByMedicationIdAndUserId(long medicationId, long userId);
    Page<MedicationEntity> findByUserIdAndIsActiveAndIsDeleted(long userId, boolean isActive, boolean isDeleted, Pageable pageable);
    Page<MedicationEntity> findByUserIdAndIsDeleted(long userId, boolean isDeleted, Pageable pageable);
    Optional<MedicationEntity> findByMedicationIdAndUserIdAndIsDeleted(long medicationId, long userId, boolean isDeleted);
    boolean existsByNameAndUserIdAndMedicationIdNotAndIsDeleted(String name, long userId, long medicationId, boolean isDeleted);
}