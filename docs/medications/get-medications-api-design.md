# API 설계: 약 목록 조회 (GET /medications)

## 1. 개요

-   **목표**: 사용자가 등록한 자신의 약물 목록을 조회할 수 있는 API를 제공합니다. 페이지네이션 및 정렬 기능을 지원합니다.
-   **엔드포인트**: `GET /api/v1/medications` (기존 `MedicationController`의 `@RequestMapping` 참고)
-   **HTTP 메소드**: `GET`
-   **인증**: 필요 (Bearer Token, `@AuthenticationPrincipal CustomUserPrincipal userDetails` 활용)
-   **관련 PRD**: `FR-MED-001`
-   **관련 API 명세서**: `3단계: 약 관리 및 복용 알림` - `내 약 목록 조회`
-   **관련 DB 스키마**: `medications` 테이블 (`MedicationEntity` 참고)

## 2. 요청 (Request)

### 2.1. Query Parameters

| 파라미터명 | 타입    | 필수 여부 | 기본값  | 설명                                                                  | API 명세서 참고 필드 |
|------------|---------|-----------|---------|-----------------------------------------------------------------------|--------------------|
| `page`     | Integer | 아니오    | 0       | 조회할 페이지 번호 (0-based index, Spring Pageable 기본값)             | `page` (조정 제안) |
| `size`     | Integer | 아니오    | 10      | 한 페이지에 표시할 약물 수 (Spring Pageable 기본값 또는 커스텀 가능)      | `size` (조정 제안) |
| `sort`     | String  | 아니오    | `medicationId,desc` | 정렬 기준. 형식: `속성명,(asc|desc)`. 예: `name,asc`, `createdAt,desc`   | -                  |
| `isActive`| Boolean | 아니오    | `true`  | 약물 활성 상태 필터링 (DB 스키마 `is_active` 필드 활용)                 | `is_active` (추론) |

*참고: API 명세서의 `/records` 조회에는 `page` 기본값이 1, `size` 기본값이 20으로 되어 있습니다. Spring Data JPA의 `Pageable` 기본값은 page 0, size 20입니다. 일관성을 위해 프로젝트 전반의 페이징 기본값을 통일하거나, API별로 명확히 정의해야 합니다. 본 설계에서는 `Pageable` 기본값을 따르되, 클라이언트가 값을 지정할 수 있도록 합니다. `sort` 기본값은 `medicationId,desc`로 제안합니다.*

## 3. 응답 DTO (Response DTO)

목록 조회 시에는 각 약물의 전체 정보보다는 요약된 정보가 적합하므로, 새로운 DTO를 정의합니다.

**파일명**: `com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationSummaryResponse.java` (신규 정의)

```java
// package com.jgji.daily_condition_tracker.domain.medication.presentation.dto;

// import java.time.OffsetDateTime;

// 개별 약물 요약 정보 DTO (페이징된 목록의 content 내부 요소)
public record MedicationSummaryDto(
    Long medicationId,
    String name,
    Integer dosage, // Medication.java, MedicationEntity.java 참조
    String unit,   // Medication.java, MedicationEntity.java 참조
    boolean isActive, // Medication.java, MedicationEntity.java 참조
    OffsetDateTime createdAt, // Medication.java, MedicationEntity.java 참조
    OffsetDateTime updatedAt // Medication.java, MedicationEntity.java 참조
) {
    // 정적 팩토리 메소드 (Service에서 Medication 도메인 객체를 DTO로 변환 시 사용)
    // public static MedicationSummaryDto from(Medication medication) {
    //     return new MedicationSummaryDto(
    //         medication.getMedicationId(),
    //         medication.getName(),
    //         medication.getDosage(),
    //         medication.getUnit(),
    //         medication.isActive(),
    //         medication.getCreatedAt(),
    //         medication.getUpdatedAt()
    //     );
    // }
}
```

-   기존 `MedicationResponse`는 생성/상세 조회 시 사용하고, 목록 조회에는 위 `MedicationSummaryDto`를 사용합니다.
-   API 응답은 `ApiResponse<Page<MedicationSummaryDto>>` 형태를 따릅니다. Spring의 `Page` 객체를 그대로 사용하거나, `Page` 객체의 주요 필드(content, totalPages, totalElements, pageNumber, pageSize 등)를 포함하는 커스텀 페이징 DTO를 `ApiResponse`의 `data` 필드에 담아 반환할 수 있습니다.

## 4. Controller (`MedicationController.java`)

**경로**: `com.jgji.daily_condition_tracker.domain.medication.presentation.MedicationController.java`

-   **주요 역할**:
    -   `@GetMapping` 엔드포인트 매핑 (기존 `@RequestMapping("/api/v1/medications")` 하위)
    -   요청 파라미터(`page`, `size`, `sort`, `is_active`)를 `Pageable` 객체 및 `isActive` 필터 값으로 바인딩.
        -   `@PageableDefault(size = 10, sort = "medicationId", direction = Sort.Direction.DESC)` 등으로 기본값 설정 가능.
    -   `@AuthenticationPrincipal CustomUserPrincipal userDetails`를 통해 현재 인증된 사용자 ID (`long userId = userDetails.getUser().getUserId();`)를 가져옴.
    -   `MedicationService`의 `findMedicationsByUserId` (또는 유사한 이름) 메소드 호출 (사용자 ID, `Pageable`, `isActive` 전달).
    -   결과(`Page<MedicationSummaryDto>`)를 `ApiResponse.success()`로 래핑하여 `ResponseEntity<ApiResponse<Page<MedicationSummaryDto>>>` 형태로 반환 (`HttpStatus.OK`).
    -   `GlobalExceptionHandler`를 통해 예외 처리 위임.

## 5. Service (`MedicationService.java`)

**경로**: `com.jgji.daily_condition_tracker.domain.medication.application.MedicationService.java`

-   **주요 역할**:
    -   `findMedicationsByUserId(long userId, Pageable pageable, Boolean isActive)` 메소드 신규 정의.
    -   `@Transactional(readOnly = true)` 어노테이션 적용.
    -   `MedicationRepository`를 사용하여 `userId`와 `isActive` 조건에 맞는 `Medication` 도메인 객체 목록을 `Page` 형태로 조회.
        -   `isActive`가 `null`일 경우, 전체 상태를 조회하거나 기본값(예: `true`)을 적용하는 정책 필요. 여기서는 `true`를 기본으로 간주.
    -   조회된 `Page<Medication>` 도메인 객체들을 `MedicationSummaryDto`로 변환 (예: `medicationsPage.map(MedicationSummaryDto::from)` 또는 `medicationsPage.map(medication -> new MedicationSummaryDto(...))`).
    -   변환된 `Page<MedicationSummaryDto>`를 반환.

## 6. Domain (`Medication.java`, `MedicationRepository.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.domain.medication.domain.Medication.java` (기존 파일 활용)
-   `com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository.java` (기존 파일 활용, 메소드 추가 필요)

-   **`Medication.java` 주요 역할**:
    -   `medications` 테이블의 데이터와 비즈니스 로직을 포함하는 도메인 객체.
    -   필드: `medicationId`, `userId`, `name`, `dosage` (Integer), `unit`, `description`, `isActive`, `createdAt` (OffsetDateTime), `updatedAt` (OffsetDateTime). (기존 코드 확인 완료)
    -   `MedicationSummaryDto`로 변환하기 위한 getter 메소드 제공.

-   **`MedicationRepository.java` 주요 역할**:
    -   `Medication` 도메인 객체의 영속성을 처리하는 인터페이스.
    -   기존 `save(Medication medication)` 외에 다음 메소드 추가 정의 필요:
        -   `Page<Medication> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable);`
        -   (선택적) `Page<Medication> findByUserId(long userId, Pageable pageable);` (isActive 필터 없이 조회)

## 7. Infrastructure (`MedicationEntity.java`, `MedicationRepositoryImpl.java`, `MedicationJpaRepository.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationEntity.java`
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationRepositoryImpl.java`
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationJpaRepository.java`

-   **`MedicationEntity.java` 주요 역할**: (기존 파일 활용)
    -   JPA 엔티티. `medications` DB 테이블과 매핑.
    -   `BaseEntity`를 상속하여 `createdAt`, `updatedAt` 관리. (실제로는 `MedicationEntity`가 `BaseEntity`를 상속하며, `Medication` 도메인 객체는 `OffsetDateTime` 타입의 `createdAt`, `updatedAt`을 직접 가짐).
    -   `fromDomain(Medication medication)`: 도메인 객체를 엔티티로 변환.
    -   `toDomain()`: 엔티티를 도메인 객체로 변환.

-   **`MedicationRepositoryImpl.java` 주요 역할**: (기존 파일 활용, 메소드 추가 필요)
    -   `MedicationRepository` 인터페이스 구현체.
    -   `MedicationJpaRepository`를 주입받아 사용.
    -   `save` 메소드는 이미 구현됨.
    -   `findByUserIdAndIsActive` (및 `findByUserId`) 메소드 구현:
        -   `MedicationJpaRepository`에 정의된 해당 JpaRepository 메소드를 호출.
        -   조회된 `Page<MedicationEntity>`를 `Page<Medication>` (도메인 객체)로 변환하여 반환 (예: `entityPage.map(MedicationEntity::toDomain)`).

-   **`MedicationJpaRepository.java` 주요 역할**: (기존 파일 활용, 메소드 추가 필요)
    -   `JpaRepository<MedicationEntity, Long>` 인터페이스 확장.
    -   Spring Data JPA가 프록시 객체를 통해 구현체를 자동 생성.
    -   다음과 같은 쿼리 메소드 정의 필요:
        -   `Page<MedicationEntity> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable);`
        -   (선택적) `Page<MedicationEntity> findByUserId(long userId, Pageable pageable);`

## 8. 주요 로직 흐름

1.  **Client**: `GET /api/v1/medications?page=0&size=10&sort=name,asc&isActive=true` 요청 (Authorization 헤더에 JWT 포함).
2.  **JwtAuthenticationFilter**: JWT 토큰 유효성 검사 및 SecurityContext에 인증 정보 설정.
3.  **MedicationController**:
    a.  `@AuthenticationPrincipal CustomUserPrincipal userDetails`로부터 `userId` 획득.
    b.  `Pageable` 객체 (page:0, size:10, sort:name ASC) 및 `isActive=true` 필터 조건 생성.
    c.  `MedicationService.findMedicationsByUserId(userId, pageable, isActive)` 호출.
4.  **MedicationService** (`findMedicationsByUserId`):
    a.  `@Transactional(readOnly = true)` 시작.
    b.  `MedicationRepository.findByUserIdAndIsActive(userId, isActive, pageable)` 호출.
5.  **MedicationRepositoryImpl** (`findByUserIdAndIsActive`):
    a.  `MedicationJpaRepository.findByUserIdAndIsActive(userId, isActive, pageable)` 호출하여 `Page<MedicationEntity>` 조회.
    b.  조회된 `Page<MedicationEntity>`를 `page.map(MedicationEntity::toDomain)`을 사용하여 `Page<Medication>` (도메인 객체)로 변환하여 반환.
6.  **MedicationService**:
    a.  Repository로부터 받은 `Page<Medication>`을 `page.map(medication -> new MedicationSummaryDto(...))` (또는 `page.map(MedicationSummaryDto::from)`)을 사용하여 `Page<MedicationSummaryDto>`로 변환.
    b.  `@Transactional` 종료.
    c.  `Page<MedicationSummaryDto>` 반환.
7.  **MedicationController**:
    a.  `MedicationService`로부터 받은 `Page<MedicationSummaryDto>`를 `ApiResponse.success()`로 래핑.
    b.  `ResponseEntity` (HTTP 200 OK) 반환.
8.  **Client**: 페이징된 약물 요약 목록 데이터 수신.

## 9. 예외 처리

-   **인증 실패 (`AuthenticationException` 등)**: Spring Security에서 처리 (`401 Unauthorized`).
-   **잘못된 요청 파라미터 (예: `page`나 `size`가 음수, `sort` 형식이 잘못됨)**: Spring MVC에서 처리 후 `GlobalExceptionHandler`가 `400 Bad Request`로 응답.
-   **기타 서버 오류**: `GlobalExceptionHandler`에서 `500 Internal Server Error` 처리.

## 10. 보안 고려 사항 (OWASP Top 10 등)

-   **A01: Broken Access Control**:
    -   인증된 사용자만 API 접근 가능.
    -   Service 계층 및 Repository 조회 시 `userId`를 기준으로 데이터를 필터링하여 다른 사용자의 정보 노출 방지.
-   **A03: Injection**: Spring Data JPA 사용으로 SQL Injection 위험 감소. `sort` 파라미터 값은 `Pageable` 객체 생성 시 Spring에서 처리하며, 허용된 엔티티 속성인지 검증됨.
-   **A05: Security Misconfiguration**: `SecurityConfig` 및 관련 보안 설정 준수.
-   **민감 데이터 노출**: 응답 DTO (`MedicationSummaryDto`)에 필요한 정보만 포함. (현재 `userId`는 포함하지 않음)

## 11. 추후 고려 사항

-   **검색 기능**: 약 이름 등으로 검색하는 기능 추가 (Repository에 `Specification` 또는 QueryDSL 활용).
-   **API 응답 형식 통일**: 페이징을 사용하는 모든 목록 조회 API가 일관된 페이징 메타데이터 구조를 갖도록 `ApiResponse`의 `data` 필드 타입을 표준화하는 것을 고려 (예: `CustomPageDto<T>`).

--- 