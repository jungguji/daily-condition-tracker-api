package com.jgji.daily_condition_tracker.domain.medication.application;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationCreateRequest;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationSummaryResponse;
import com.jgji.daily_condition_tracker.global.common.PageRequest;
import com.jgji.daily_condition_tracker.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public PageResponse<MedicationSummaryResponse> findMedicationsByUserId(long userId, PageRequest pageRequest, Boolean isActive) {
        Pageable pageable = pageRequest.toPageable();
        
        Page<Medication> medicationsPage;
        if (isActive != null) {
            medicationsPage = medicationRepository.findByUserIdAndIsActive(userId, isActive, pageable);
        } else {
            medicationsPage = medicationRepository.findByUserId(userId, pageable);
        }

        List<MedicationSummaryResponse> medicationSummaryResponses = medicationsPage.getContent()
                .stream()
                .map(MedicationSummaryResponse::from)
                .toList();

        return PageResponse.of(
                medicationSummaryResponses,
                pageRequest.page(),
                pageRequest.size(),
                medicationsPage.getTotalElements()
        );
    }
} 