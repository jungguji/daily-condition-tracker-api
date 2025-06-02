package com.jgji.daily_condition_tracker.domain.medication.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicationRepository {
    Medication save(Medication medication);
    Page<Medication> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable);
    Page<Medication> findByUserId(long userId, Pageable pageable);
} 