package com.jgji.daily_condition_tracker.domain.medication.presentation;

import com.jgji.daily_condition_tracker.domain.auth.application.CustomUserPrincipal;
import com.jgji.daily_condition_tracker.domain.medication.application.MedicationService;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationSummaryResponse;
import com.jgji.daily_condition_tracker.global.common.PageRequest;
import com.jgji.daily_condition_tracker.global.common.PageResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationCreateRequest;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse;
import com.jgji.daily_condition_tracker.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<MedicationResponse>> createMedication(
            @Valid @RequestBody MedicationCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userDetails) {
        
        long userId = userDetails.getUser().getUserId();
        
        MedicationResponse response = medicationService.createMedication(userId, request);
        
        log.debug("약 등록 성공: userId={}, medicationId={}, name={}", 
                userId, response.medicationId(), response.name());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MedicationSummaryResponse>>> getMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "medicationId") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal CustomUserPrincipal userDetails) {
        
        long userId = userDetails.getUser().getUserId();
        
        PageRequest pageRequest = PageRequest.of(page, size, sortBy, direction);
        PageResponse<MedicationSummaryResponse> response = medicationService.findMedicationsByUserId(userId, pageRequest, isActive);
        
        log.debug("약 목록 조회 성공: userId={}, page={}, size={}, totalElements={}", 
                userId, page, size, response.totalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
} 