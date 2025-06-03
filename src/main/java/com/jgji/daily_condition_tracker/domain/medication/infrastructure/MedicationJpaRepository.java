package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MedicationJpaRepository extends JpaRepository<MedicationEntity, Long> {
    Page<MedicationEntity> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable);
    Page<MedicationEntity> findByUserId(long userId, Pageable pageable);
    Optional<MedicationEntity> findByMedicationIdAndUserId(long medicationId, long userId);
}