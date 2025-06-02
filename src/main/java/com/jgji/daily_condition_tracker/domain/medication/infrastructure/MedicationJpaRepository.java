package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationJpaRepository extends JpaRepository<MedicationEntity, Long> {
} 