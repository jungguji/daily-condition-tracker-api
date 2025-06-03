package com.jgji.daily_condition_tracker.domain.medication.application;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationCreateRequest;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationSummaryResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationUpdateRequest;
import com.jgji.daily_condition_tracker.global.common.PageRequest;
import com.jgji.daily_condition_tracker.global.common.PageResponse;
import com.jgji.daily_condition_tracker.global.exception.BusinessRuleViolationException;
import com.jgji.daily_condition_tracker.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(readOnly = true)
    public MedicationResponse findMedicationById(long medicationId, long userId) {
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("약", "ID", medicationId));
        
        return MedicationResponse.from(
                medication.getMedicationId(),
                medication.getName(),
                medication.getDosage(),
                medication.getUnit(),
                medication.getDescription(),
                medication.isActive(),
                medication.getCreatedAt(),
                medication.getUpdatedAt()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public MedicationResponse updateMedication(long userId, long medicationId, MedicationUpdateRequest dto) {
        if (isAllFieldsUndefined(dto)) {
            throw new BusinessRuleViolationException("수정할 내용이 없습니다.");
        }

        Medication originalMedication = medicationRepository.findByIdAndUserId(medicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("약", "ID", medicationId));

        String newName = extractValueOrNull(dto.name(), originalMedication.getName());
        Integer newDosage = extractValueOrNull(dto.dosage(), originalMedication.getDosage());
        String newUnit = extractValueOrNull(dto.unit(), originalMedication.getUnit());
        String newDescription = extractValueOrNull(dto.description(), originalMedication.getDescription());
        Boolean newIsActive = extractValueOrNull(dto.isActive(), originalMedication.isActive());

        if (newName != null && !newName.equals(originalMedication.getName())) {
            if (medicationRepository.existsByNameAndUserIdAndIdNot(newName, userId, medicationId)) {
                throw new BusinessRuleViolationException("이미 동일한 이름의 약물이 존재합니다: " + newName);
            }
        }

        Medication updatedMedication = originalMedication.withUpdates(
                newName,
                newDosage,
                newUnit,
                newDescription,
                newIsActive
        );

        Medication savedMedication = medicationRepository.save(updatedMedication);

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

    @Transactional(rollbackFor = Exception.class)
    public void deleteMedication(long userId, long medicationId) {
        log.debug("약 삭제 요청: userId={}, medicationId={}", userId, medicationId);
        
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, userId)
                .orElseThrow(() -> {
                    log.warn("삭제할 약을 찾을 수 없음: userId={}, medicationId={}", userId, medicationId);
                    return new ResourceNotFoundException("약", "ID", medicationId);
                });
        
        Medication deletedMedication = medication.delete();
        medicationRepository.save(deletedMedication);
        
        log.debug("약 소프트 삭제 성공: userId={}, medicationId={}, name={}",
                userId, medicationId, medication.getName());
    }

    private boolean isAllFieldsUndefined(MedicationUpdateRequest dto) {
        return !dto.name().isPresent() && 
               !dto.dosage().isPresent() && 
               !dto.unit().isPresent() && 
               !dto.description().isPresent() && 
               !dto.isActive().isPresent();
    }

    private <T> T extractValueOrNull(JsonNullable<T> jsonNullable, T originalValue) {
        return jsonNullable.isPresent() ? jsonNullable.get() : originalValue;
    }
} 