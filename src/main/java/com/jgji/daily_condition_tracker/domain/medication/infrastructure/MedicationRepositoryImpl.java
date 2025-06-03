package com.jgji.daily_condition_tracker.domain.medication.infrastructure;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
class MedicationRepositoryImpl implements MedicationRepository {

    private final MedicationJpaRepository medicationJpaRepository;

    @Override
    public Medication save(Medication medication) {
        MedicationEntity entity = MedicationEntity.fromDomain(medication);
        MedicationEntity savedEntity = medicationJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Page<Medication> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable) {
        Page<MedicationEntity> entityPage = medicationJpaRepository.findByUserIdAndIsActiveAndIsDeleted(userId, isActive, false, pageable);
        return entityPage.map(MedicationEntity::toDomain);
    }

    @Override
    public Page<Medication> findByUserId(long userId, Pageable pageable) {
        Page<MedicationEntity> entityPage = medicationJpaRepository.findByUserIdAndIsDeleted(userId, false, pageable);
        return entityPage.map(MedicationEntity::toDomain);
    }

    @Override
    public Optional<Medication> findByIdAndUserId(long medicationId, long userId) {
        Optional<MedicationEntity> entityOptional = medicationJpaRepository.findByMedicationIdAndUserIdAndIsDeleted(medicationId, userId, false);
        return entityOptional.map(MedicationEntity::toDomain);
    }

    @Override
    public boolean existsByNameAndUserIdAndIdNot(String name, long userId, long medicationId) {
        return medicationJpaRepository.existsByNameAndUserIdAndMedicationIdNotAndIsDeleted(name, userId, medicationId, false);
    }
} 