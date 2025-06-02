# API 설계: 특정 약 정보 조회 (GET /medications/{medication_id})

## 1. 개요

-   **목표**: 사용자가 등록한 특정 약물의 상세 정보를 조회할 수 있는 API를 제공합니다. 접근 권한 검증을 통해 본인의 약물만 조회 가능하도록 보장합니다.
-   **엔드포인트**: `GET /api/v1/medications/{medication_id}` (기존 `MedicationController`의 `@RequestMapping` 참고)
-   **HTTP 메소드**: `GET`
-   **인증**: 필요 (Bearer Token, `@AuthenticationPrincipal CustomUserPrincipal userDetails` 활용)
-   **관련 PRD**: `FR-MED-001`
-   **관련 API 명세서**: `3단계: 약 관리 및 복용 알림` - `특정 약 상세 조회`
-   **관련 DB 스키마**: `medications` 테이블 (`MedicationEntity` 참고)

## 2. 요청 (Request)

### 2.1. Path Parameters

| 파라미터명 | 타입 | 필수 여부 | 설명 | 유효성 검증 |
|------------|------|-----------|------|-------------|
| `medication_id` | Long | 필수 | 조회할 약의 고유 ID | 양수, null이 아님 |

### 2.2. Query Parameters

이 API는 특정 리소스 조회이므로 추가 쿼리 파라미터는 없습니다.

## 3. 응답 DTO (Response DTO)

기존 `MedicationResponse.java`를 재사용합니다. 이는 약 생성 시에도 사용되며, 약의 모든 상세 정보를 포함하므로 특정 약 조회에 적합합니다.

**파일명**: `com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse.java` (기존 파일 활용)

-   **포함 필드**:
    -   `medicationId` (Long): 약 고유 ID
    -   `name` (String): 약 이름
    -   `dosage` (Integer): 용량
    -   `unit` (String): 단위 (예: mg, 정, ml)
    -   `description` (String): 약에 대한 추가 설명/메모
    -   `isActive` (boolean): 현재 복용중인 약 여부
    -   `createdAt` (OffsetDateTime): 등록 일시
    -   `updatedAt` (OffsetDateTime): 마지막 수정 일시

-   기존 `from()` 정적 팩토리 메소드를 그대로 활용하여 `Medication` 도메인 객체를 DTO로 변환합니다.

## 4. Controller (`MedicationController.java`)

**경로**: `com.jgji.daily_condition_tracker.domain.medication.presentation.MedicationController.java`

-   **주요 역할**:
    -   `@GetMapping("/{medicationId}")` 엔드포인트 매핑 추가
    -   `@PathVariable Long medicationId`를 통해 URL에서 약 ID 추출
    -   `@AuthenticationPrincipal CustomUserPrincipal userDetails`를 통해 현재 인증된 사용자 ID (`long userId = userDetails.getUser().getUserId();`)를 가져옴
    -   `MedicationService`의 `findMedicationById` 메소드 호출 (약 ID와 사용자 ID 전달)
    -   결과(`MedicationResponse`)를 `ApiResponse.success()`로 래핑하여 `ResponseEntity<ApiResponse<MedicationResponse>>` 형태로 반환 (`HttpStatus.OK`)
    -   존재하지 않는 약 ID 또는 권한 없는 접근 시 예외 발생을 Service 계층에 위임

## 5. Service (`MedicationService.java`)

**경로**: `com.jgji.daily_condition_tracker.domain.medication.application.MedicationService.java`

-   **주요 역할**:
    -   `findMedicationById(long medicationId, long userId)` 메소드 신규 정의
    -   `@Transactional(readOnly = true)` 어노테이션 적용
    -   `MedicationRepository`를 사용하여 `medicationId`로 `Medication` 도메인 객체 조회
    -   조회된 약의 `userId`와 요청자의 `userId`가 일치하는지 검증 (접근 권한 확인)
    -   권한이 없는 경우 `AccessDeniedException` 또는 `ForbiddenException` 등 적절한 예외 발생
    -   약이 존재하지 않는 경우 `ResourceNotFoundException` 등 적절한 예외 발생
    -   조회된 `Medication` 도메인 객체를 `MedicationResponse`로 변환하여 반환

## 6. Domain (`Medication.java`, `MedicationRepository.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.domain.medication.domain.Medication.java` (기존 파일 활용)
-   `com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository.java` (기존 파일 활용, 메소드 추가 필요)

-   **`Medication.java` 주요 역할**:
    -   기존 도메인 객체 그대로 활용
    -   `MedicationResponse`로 변환하기 위한 getter 메소드 제공 (이미 구현됨)

-   **`MedicationRepository.java` 주요 역할**:
    -   기존 `save`, `findByUserIdAndIsActive`, `findByUserId` 메소드 외에 다음 메소드 추가:
        -   `Optional<Medication> findById(long medicationId);` - 약 ID로 특정 약 조회

## 7. Infrastructure (`MedicationEntity.java`, `MedicationRepositoryImpl.java`, `MedicationJpaRepository.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationEntity.java` (기존 파일 활용)
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationRepositoryImpl.java` (기존 파일 활용, 메소드 추가 필요)
-   `com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationJpaRepository.java` (기존 파일 활용, 메소드 추가 필요)

-   **`MedicationEntity.java` 주요 역할**: (기존 파일 활용)
    -   기존 JPA 엔티티 그대로 활용
    -   `fromDomain(Medication medication)`: 도메인 객체를 엔티티로 변환 (기존)
    -   `toDomain()`: 엔티티를 도메인 객체로 변환 (기존)

-   **`MedicationRepositoryImpl.java` 주요 역할**: (기존 파일 활용, 메소드 추가)
    -   `MedicationRepository` 인터페이스 구현체
    -   `findById` 메소드 구현:
        -   `MedicationJpaRepository.findById(medicationId)` 호출
        -   조회된 `Optional<MedicationEntity>`를 `Optional<Medication>` (도메인 객체)로 변환하여 반환 (예: `entityOptional.map(MedicationEntity::toDomain)`)

-   **`MedicationJpaRepository.java` 주요 역할**: (기존 파일 활용)
    -   `JpaRepository<MedicationEntity, Long>` 인터페이스 확장
    -   `findById(Long id)` 메소드는 JpaRepository에서 기본 제공되므로 별도 정의 불필요
    -   추가적으로 사용자 권한 검증을 위해 `Optional<MedicationEntity> findByMedicationIdAndUserId(long medicationId, long userId)` 메소드 정의 고려 (성능 최적화 및 보안 강화)

## 8. 주요 로직 흐름

1.  **Client**: `GET /api/v1/medications/5` 요청 (Authorization 헤더에 JWT 포함)
2.  **JwtAuthenticationFilter**: JWT 토큰 유효성 검사 및 SecurityContext에 인증 정보 설정
3.  **MedicationController**:
    a.  `@PathVariable Long medicationId`로부터 약 ID (5) 획득
    b.  `@AuthenticationPrincipal CustomUserPrincipal userDetails`로부터 `userId` 획득
    c.  `MedicationService.findMedicationById(medicationId, userId)` 호출
4.  **MedicationService** (`findMedicationById`):
    a.  `@Transactional(readOnly = true)` 시작
    b.  `MedicationRepository.findById(medicationId)` 호출
    c.  조회된 `Optional<Medication>`이 비어있으면 `ResourceNotFoundException` 발생
    d.  조회된 `Medication`의 `userId`와 요청자의 `userId` 비교
    e.  일치하지 않으면 `AccessDeniedException` 발생
    f.  유효한 경우 `MedicationResponse.from()` 메소드로 DTO 변환
5.  **MedicationRepositoryImpl** (`findById`):
    a.  `MedicationJpaRepository.findById(medicationId)` 호출하여 `Optional<MedicationEntity>` 조회
    b.  조회된 `Optional<MedicationEntity>`를 `optional.map(MedicationEntity::toDomain)`을 사용하여 `Optional<Medication>` (도메인 객체)로 변환하여 반환
6.  **MedicationService**:
    a.  Repository로부터 받은 `Medication` 도메인 객체를 `MedicationResponse`로 변환
    b.  `@Transactional` 종료
    c.  `MedicationResponse` 반환
7.  **MedicationController**:
    a.  `MedicationService`로부터 받은 `MedicationResponse`를 `ApiResponse.success()`로 래핑
    b.  `ResponseEntity` (HTTP 200 OK) 반환
8.  **Client**: 약 상세 정보 데이터 수신

## 9. 예외 처리

-   **인증 실패**: Spring Security에서 처리 (`401 Unauthorized`)
-   **약이 존재하지 않음**: `ResourceNotFoundException` 발생 → `GlobalExceptionHandler`에서 `404 Not Found` 응답
-   **접근 권한 없음** (다른 사용자의 약 조회 시도): `AccessDeniedException` 발생 → `GlobalExceptionHandler`에서 `403 Forbidden` 응답
-   **잘못된 URL 파라미터** (예: medicationId가 음수이거나 유효하지 않은 형식): Spring MVC에서 처리 후 `GlobalExceptionHandler`가 `400 Bad Request`로 응답
-   **기타 서버 오류**: `GlobalExceptionHandler`에서 `500 Internal Server Error` 처리

## 10. 보안 고려 사항 (OWASP Top 10 등)

-   **A01: Broken Access Control**:
    -   인증된 사용자만 API 접근 가능
    -   Service 계층에서 약의 소유자(`userId`)와 요청자가 일치하는지 반드시 검증
    -   Repository 계층에서 `findByMedicationIdAndUserId` 메소드 활용 고려 (DB 레벨에서 권한 검증)
-   **A03: Injection**: Spring Data JPA 사용으로 SQL Injection 위험 감소
-   **A04: Insecure Design**: Path Parameter 유효성 검증 (양수, Long 타입)
-   **A05: Security Misconfiguration**: `SecurityConfig` 및 관련 보안 설정 준수
-   **민감 데이터 노출**: 응답 DTO (`MedicationResponse`)에 필요한 정보만 포함, 다른 사용자 정보 노출 방지

## 11. 성능 최적화 고려사항

-   **Database Query 최적화**:
    -   `findById` 대신 `findByMedicationIdAndUserId`를 사용하여 DB에서 직접 권한 검증 수행 고려
    -   이 경우 애플리케이션 레벨에서의 추가 권한 검증 로직 제거 가능
-   **Caching**: 자주 조회되는 약 정보에 대한 캐싱 전략 고려 (향후 확장)

## 12. 추후 고려 사항

-   **Soft Delete 지원**: 논리적으로 삭제된 약에 대한 조회 처리 방식 정의
-   **Audit Log**: 약 조회 이력 로깅 (보안 감사 목적)
-   **API 응답 최적화**: 클라이언트 요구사항에 따라 필요한 필드만 반환하는 선택적 응답 지원

---

**개발 우선순위**: 본 API는 약 관리 기능의 핵심이므로 높은 우선순위로 개발되어야 하며, 특히 접근 권한 검증 로직이 완전히 구현되어야 합니다. 