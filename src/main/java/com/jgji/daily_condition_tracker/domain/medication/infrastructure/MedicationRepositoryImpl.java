package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class MedicationRepositoryImpl implements MedicationRepository {

    private final MedicationJpaRepository medicationJpaRepository;

    @Override
    public Medication save(Medication medication) {
        MedicationEntity entity = MedicationEntity.fromDomain(medication);
        MedicationEntity savedEntity = medicationJpaRepository.save(entity);
        return savedEntity.toDomain();
    }
} 