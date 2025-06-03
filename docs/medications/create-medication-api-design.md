# API 설계: 새 약 등록 (POST /medications)

## 1. 개요

-   **목표**: 사용자가 복용 중인 새로운 약 정보를 시스템에 등록할 수 있도록 API를 제공합니다.
-   **엔드포인트**: `POST /api/v1/medications`
-   **HTTP 메소드**: `POST`
-   **인증**: 필요 (Bearer Token)
-   **관련 PRD**: `FR-MED-001` (사용자는 자신이 복용하는 약의 이름, (선택적) 용량/단위 정보를 등록/수정/삭제할 수 있는 '내 약 관리' 메뉴가 있어야 한다.)
-   **관련 API 명세서**: `3단계: 약 관리 및 복용 알림` - `새 약 등록`
-   **관련 DB 스키마**: `medications` 테이블

## 2. 요청 DTO (Request DTO)

**파일명**: `com.jgji.daily_condition_tracker.medication.presentation.dto.MedicationCreateRequest.java`

```java
// 예시 (실제 코드는 아님)
package com.jgji.daily_condition_tracker.medication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// 필요한 경우 추가적인 Bean Validation 어노테이션 사용

public record MedicationCreateRequest(
    @NotBlank(message = "약 이름은 필수입니다.")
    @Size(max = 255, message = "약 이름은 최대 255자까지 입력 가능합니다.")
    String name,

    @Size(max = 100, message = "용량 정보는 최대 100자까지 입력 가능합니다.")
    String dosage, // 예: "50mg", "1정"

    @Size(max = 50, message = "단위 정보는 최대 50자까지 입력 가능합니다.")
    String unit, // 예: "mg", "정", "ml"

    @Size(max = 65535, message = "설명은 최대 65535자까지 입력 가능합니다.") // TEXT 타입 고려
    String description, // 약에 대한 추가 설명/메모

    Boolean isActive // 현재 복용중인 약 여부 (기본값 true)
) {
}
```

-   `name`: 약 이름 (필수)
-   `dosage`: 용량 (선택)
-   `unit`: 단위 (선택)
-   `description`: 설명 (선택)
-   `isActive`: 활성 여부 (선택, 기본값: `true`). API 명세서에는 없지만 DB 스키마에 `is_active` 필드가 있으므로 DTO에 포함하는 것을 고려합니다. 클라이언트에서 명시적으로 비활성 상태로 약을 등록할 케이스가 없다면 서비스 레이어에서 기본값 `true`로 처리할 수도 있습니다.

## 3. 응답 DTO (Response DTO)

**파일명**: `com.jgji.daily_condition_tracker.medication.presentation.dto.MedicationResponse.java`

```java
// 예시 (실제 코드는 아님)
package com.jgji.daily_condition_tracker.medication.presentation.dto;

import java.time.LocalDateTime;

public record MedicationResponse(
    Long medicationId,
    String name,
    String dosage,
    String unit,
    String description,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // 정적 팩토리 메소드 (Service에서 Domain 객체를 DTO로 변환 시 사용)
    // public static MedicationResponse from(Medication medication) { ... }
}
```
-   API 명세서의 성공 응답 예시를 따릅니다.

## 4. Controller (`MedicationController.java`)

**경로**: `com.jgji.daily_condition_tracker.medication.presentation.MedicationController.java`

-   **주요 역할**:
    -   `@PostMapping("/medications")` 엔드포인트 매핑
    -   `@RequestBody`로 `MedicationCreateRequest` DTO를 받아 `@Valid` 어노테이션으로 유효성 검사 수행
    -   `AuthService` 또는 `SecurityContextHolder`를 통해 현재 인증된 사용자 정보(ID)를 가져옴
    -   `MedicationService`의 `createMedication` 메소드 호출 (사용자 ID와 요청 DTO 전달)
    -   결과를 `ResponseEntity<ApiResponse<MedicationResponse>>` 형태로 래핑하여 반환 (`HttpStatus.CREATED` 및 `ApiResponse.success()`)
    -   `GlobalExceptionHandler`를 통해 예외 처리 위임

## 5. Service (`MedicationService.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.medication.application.MedicationService.java` (Implementation)

-   **주요 역할**:
    -   `createMedication(Long userId, MedicationCreateRequest request)` 메소드 정의
    -   `@Transactional` 어노테이션 적용 (쓰기 작업이므로)
    -   요청 DTO를 기반으로 `Medication` 도메인 객체 생성 (빌더 패턴 또는 정적 팩토리 메소드 사용)
        -   `userId`는 인증된 사용자의 ID로 설정
        -   `isActive` 필드는 요청 DTO에 값이 없거나 `null`일 경우 `true`로 기본 설정
    -   `MedicationRepository`를 호출하여 `Medication` 도메인 객체 저장
    -   저장된 `Medication` 도메인 객체를 `MedicationResponse` DTO로 변환하여 반환

## 6. Domain (`Medication.java`)

**경로**: `com.jgji.daily_condition_tracker.medication.domain.Medication.java`

-   **주요 역할**:
    -   `medications` 테이블의 필드들을 나타내는 멤버 변수 정의 (DDD의 Entity 역할)
    -   `name`은 필수값, `dosage`, `unit`, `description` 등은 선택적일 수 있음 (DB 스키마 참조)
    -   `user_id` 외래 키 참조
    -   생성자 또는 정적 팩토리 메소드를 통해 객체 생성 로직 관리 (유효성 검증 포함 가능)
    -   비즈니스 로직 (예: 상태 변경 메소드) 포함 가능 (본 API에서는 주로 생성)

## 7. Repository (`MedicationRepository.java` / `MedicationRepositoryImpl.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.medication.domain.MedicationRepository.java` (Interface)
-   `com.jgji.daily_condition_tracker.medication.infrastructure.MedicationRepositoryImpl.java` (Implementation)

-   **주요 역할**:
    -   `MedicationRepositoryImpl`은 `MedicationRepository` 인터페이스를 구현. Spring Data JPA를 사용한다면 별도의 구현체가 필요 없을 수 있으나, QueryDSL 등 사용 시 구현
    -  `MedicationJpaRepository` 인터페이스는 `JpaRepository<MedicationEntity, Long>`를 상속받아 기본 CRUD 메소드 제공
    -  `MedicationRepositoryImpl`에서 MedicationJpaRepository를 주입받아서 사용
    -   `save(MedicationEntity entity)` 메소드를 통해 `MedicationEntity`를 DB에 저장

### 7.1. Entity (`MedicationEntity.java`)

**경로**: `com.jgji.daily_condition_tracker.medication.infrastructure.MedicationEntity.java`

-   **주요 역할**:
    -   `@Entity`, `@Table(name = "medications")` 어노테이션 사용
    -   `medications` DB 테이블의 컬럼과 매핑 (JPA 규칙 준수)
    -   `UserEntity`와의 연관 관계를 JPA 기능을 통해 매핑하지 않고, `user_id` 외래 키를 직접 관리
    -   도메인 객체(`Medication`)와 Entity 간의 변환 로직 (e.g., `Medication.of(MedicationEntity entity)`, `MedicationEntity.toDomain()`) 포함

## 8. 주요 로직 흐름

1.  **Client**: `POST /api/v1/medications` 요청 (Authorization 헤더에 JWT 포함, Request Body에 `MedicationCreateRequest` DTO 포함)
2.  **JwtAuthenticationFilter**: JWT 토큰 유효성 검사 및 SecurityContext에 인증 정보 설정
3.  **MedicationController**:
    a.  `@Valid` 어노테이션으로 `MedicationCreateRequest` DTO 유효성 검사 (실패 시 `GlobalExceptionHandler`가 `400 Bad Request` 응답)
    b.  현재 인증된 사용자 ID 획득
    c.  `MedicationService.createMedication(userId, requestDto)` 호출
4.  **MedicationService**:
    a.  `@Transactional` 시작
    b.  `requestDto`와 `userId`를 사용하여 `Medication` 도메인 객체 생성
        - `Medication.create(userId, requestDto.name(), requestDto.dosage(), ...)` 형태
    c.  `Medication` 도메인 객체를 `MedicationEntity`로 변환
    d.  `MedicationRepository.save(medicationEntity)` 호출하여 DB에 저장
    e.  저장된 `MedicationEntity`를 다시 `Medication` 도메인 객체로 변환 (ID 등 자동 생성 값 포함)
    f.  `Medication` 도메인 객체를 `MedicationResponse` DTO로 변환
    g.  `@Transactional` 커밋
    h.  `MedicationResponse` DTO 반환
5.  **MedicationController**:
    a.  `MedicationService`로부터 받은 `MedicationResponse` DTO를 `ApiResponse.success()`로 래핑
    b.  `ResponseEntity` (HTTP 201 Created) 반환
6.  **Client**: 성공 응답 수신

## 9. 예외 처리

-   **`MethodArgumentNotValidException`**: DTO 유효성 검사 실패 시. `GlobalExceptionHandler`에서 `400 Bad Request` (VALIDATION_ERROR)로 처리.
-   **인증 실패 (`AuthenticationException` 등)**: JWT 토큰이 유효하지 않거나 없는 경우. Spring Security 필터 체인 및 `CustomAuthenticationEntryPoint`에서 `401 Unauthorized` 처리.
-   **인가 실패 (`AccessDeniedException` 등)**: (본 API에서는 특정 리소스 접근 권한 체크는 없으나, 일반적인 경우) `CustomAccessDeniedHandler`에서 `403 Forbidden` 처리.
-   **DB 제약 조건 위반 (예: `DataIntegrityViolationException`)**: 중복된 약 이름 (사용자별로 유니크해야 하는 경우, 현재 스키마에는 없음) 등. `GlobalExceptionHandler`에서 `500 Internal Server Error` 또는 상황에 맞는 커스텀 예외로 변환하여 `409 Conflict` 등으로 처리 가능성 검토. (현재 약 이름은 사용자별로 중복 가능하므로 해당 없음)
-   **기타 서버 오류**: `GlobalExceptionHandler`에서 `500 Internal Server Error` 처리.

## 10. 보안 고려 사항 (OWASP Top 10 등)

-   **A01: Broken Access Control**: 인증된 사용자만 자신의 약을 등록할 수 있도록 `userId`를 JWT 토큰에서 추출하여 사용.
-   **A03: Injection**: Spring Data JPA 사용으로 SQL Injection 위험 감소. 입력값에 대한 추가적인 정제(Sanitization)는 필요시 고려.
-   **A05: Security Misconfiguration**: `SecurityConfig`를 통해 적절한 보안 헤더, 세션 관리, CSRF 보호(stateless에서는 비활성화) 설정.
-   **입력값 검증 (Input Validation)**: JSR 303/380 Bean Validation을 통해 DTO 레벨에서 강력한 입력값 검증 수행.
-   **민감 데이터 노출**: 응답 DTO에 불필요한 민감 정보가 포함되지 않도록 주의.

## 11. 추후 고려 사항

-   사용자별로 동일한 이름의 약을 중복 등록하지 못하게 하는 비즈니스 규칙 추가 여부 검토. (현재 스키마 및 요구사항에서는 허용)
-   약 이름 자동 완성 또는 표준 약물 데이터베이스 연동. (MVP 범위 아님)

--- 