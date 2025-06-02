package com.jgji.daily_condition_tracker.domain.medication.application;

import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationCreateRequest;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    @Transactional
    public MedicationResponse createMedication(long userId, MedicationCreateRequest request) {
        Medication medication = Medication.create(
                userId,
                request.name(),
                request.dosage(),
                request.unit(),
                request.description(),
                request.isActive()
        );

        Medication savedMedication = medicationRepository.save(medication);

        return MedicationResponse.from(
                savedMedication.getMedicationId(),
                savedMedication.getName(),
                savedMedication.getDosage(),
                savedMedication.getUnit(),
                savedMedication.getDescription(),
                savedMedication.isActive(),
                savedMedication.getCreatedAt(),
                savedMedication.getUpdatedAt()
        );
    }
} 