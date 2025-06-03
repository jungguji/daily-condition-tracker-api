package com.jgji.daily_condition_tracker.domain.medication.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MedicationRepository {
    Medication save(Medication medication);
    
    Optional<Medication> findByIdAndUserId(long medicationId, long userId);

    Page<Medication> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable);
    Page<Medication> findByUserId(long userId, Pageable pageable);
    boolean existsByNameAndUserIdAndIdNot(String name, long userId, long medicationId);
}