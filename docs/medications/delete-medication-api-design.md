# API 설계: 약 삭제 (DELETE /medications/{medication_id})

## 1. 개요

-   **목표**: 사용자가 등록한 특정 약 정보를 시스템에서 삭제할 수 있도록 API를 제공합니다.
-   **엔드포인트**: `DELETE /api/v1/medications/{medication_id}`
-   **HTTP 메소드**: `DELETE`
-   **인증**: 필요 (Bearer Token)
-   **관련 PRD**: `FR-MED-001` (사용자는 자신이 복용하는 약의 이름, (선택적) 용량/단위 정보를 등록/수정/삭제할 수 있는 '내 약 관리' 메뉴가 있어야 한다.)
-   **관련 API 명세서**: `3단계: 약 관리 및 복용 알림` - `약 삭제`
-   **관련 DB 스키마**: `medications` 테이블

## 2. 요청 (Request)

### 2.1. Path Parameters

| 이름             | 타입   | 필수 | 설명                |
| ---------------- | ------ | ---- | ------------------- |
| `medication_id` | `Long` | Y    | 삭제할 약의 고유 ID |

### 2.2. Request Body

없음 (DELETE 요청은 Request Body가 필요하지 않음)

### 2.3. 유효성 검사

- `medication_id`: 양의 정수여야 합니다.
- 인증된 사용자만 본인의 약을 삭제할 수 있습니다.

## 3. 응답 (Response)

### 3.1. 성공

- **HTTP Status**: `204 No Content`
- **Body**: 없음 (성공적인 DELETE 요청의 표준 응답)

### 3.2. 실패

| HTTP Status | `ApiResponse.code` | `ApiResponse.status` | `ApiResponse.message` (예시)                        | 설명                                                                |
| ----------- | ------------------ | -------------------- | ----------------------------------------------------- | ------------------------------------------------------------------- |
| 400         | 400                | `BAD_REQUEST`        | "잘못된 약 ID 형식입니다."                            | Path Parameter 형식 오류 (`PathVariable` 바인딩 실패)             |
| 401         | 401                | `UNAUTHORIZED`       | "인증되지 않은 사용자입니다."                         | 유효한 인증 토큰이 없는 경우 (`AuthenticationException`)           |
| 403         | 403                | `FORBIDDEN`          | "해당 약을 삭제할 권한이 없습니다."                   | 다른 사용자의 약을 삭제하려는 경우                                 |
| 404         | 404                | `NOT_FOUND`          | "약 ID '{medication_id}'을(를) 찾을 수 없습니다."   | 해당 ID의 약이 존재하지 않는 경우 (`ResourceNotFoundException`)     |
| 409         | 409                | `CONFLICT`           | "복용 기록이 있는 약은 삭제할 수 없습니다."           | 약 복용 기록이 있어 DB 제약조건 위반 시 (`DataIntegrityViolationException`) |
| 500         | 500                | `INTERNAL_SERVER_ERROR` | "서버 내부 오류가 발생했습니다."                   | 그 외 서버 오류 (`Exception`)                                     |

## 4. Controller (`MedicationController.java`)

**경로**: `com.jgji.daily_condition_tracker.medication.presentation.MedicationController.java`

-   **주요 역할**:
    -   `@DeleteMapping("/medications/{medicationId}")` 엔드포인트 매핑
    -   `@PathVariable`로 `medicationId` 추출 및 유효성 검사
    -   `AuthService` 또는 `SecurityContextHolder`를 통해 현재 인증된 사용자 정보(ID)를 가져옴
    -   `MedicationService`의 `deleteMedication` 메소드 호출 (사용자 ID와 약 ID 전달)
    -   성공 시 `ResponseEntity<Void>` 형태로 `HttpStatus.NO_CONTENT` 반환
    -   `GlobalExceptionHandler`를 통해 예외 처리 위임

```java
// 예시 메소드 시그니처
@DeleteMapping("/{medicationId}")
public ResponseEntity<Void> deleteMedication(
    @AuthenticationPrincipal UserPrincipal userPrincipal,
    @PathVariable Long medicationId
) {
    Long userId = userPrincipal.getUserId();
    medicationService.deleteMedication(userId, medicationId);
    return ResponseEntity.noContent().build();
}
```

## 5. Service (`MedicationService.java`)

**경로**: `com.jgji.daily_condition_tracker.medication.application.MedicationService.java`

-   **주요 역할**:
    -   `deleteMedication(Long userId, Long medicationId)` 메소드 정의
    -   `@Transactional` 어노테이션 적용 (쓰기 작업이므로)
    -   삭제 권한 검증: 해당 약이 존재하고 현재 사용자의 소유인지 확인
    -   `MedicationRepository`를 호출하여 약 삭제 실행
    -   DB 제약조건 위반 시 적절한 예외 변환 및 처리

```java
// 예시 메소드
@Transactional
public void deleteMedication(Long userId, Long medicationId) {
    // 1. 약 존재 및 소유권 확인
    Medication medication = medicationRepository.findByIdAndUserId(medicationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("약", "ID", medicationId));
    
    try {
        // 2. 약 삭제 실행
        medicationRepository.deleteByIdAndUserId(medicationId, userId);
    } catch (DataIntegrityViolationException e) {
        // 3. DB 제약조건 위반 시 적절한 예외 변환
        throw new BusinessRuleViolationException("복용 기록이 있는 약은 삭제할 수 없습니다.", e);
    }
}
```

## 6. Repository (`MedicationRepository.java` / `MedicationRepositoryImpl.java`)

**경로**:
-   `com.jgji.daily_condition_tracker.medication.domain.MedicationRepository.java` (Interface)
-   `com.jgji.daily_condition_tracker.medication.infrastructure.MedicationRepositoryImpl.java` (Implementation)

-   **주요 역할**:
    -   `deleteByIdAndUserId(Long medicationId, Long userId)` 메소드 추가
    -   사용자 소유권 검증과 함께 안전한 삭제 수행
    -   Spring Data JPA 또는 QueryDSL을 통한 삭제 쿼리 실행

```java
// Repository Interface에 추가할 메소드
public interface MedicationRepository {
    // 기존 메소드들...
    void deleteByIdAndUserId(Long medicationId, Long userId);
}
```

## 7. 주요 로직 흐름

1.  **Client**: `DELETE /api/v1/medications/{medication_id}` 요청 (Authorization 헤더에 JWT 포함)
2.  **JwtAuthenticationFilter**: JWT 토큰 유효성 검사 및 SecurityContext에 인증 정보 설정
3.  **MedicationController**:
    a.  `@PathVariable`로 `medicationId` 추출 및 타입 검증
    b.  현재 인증된 사용자 ID 획득
    c.  `MedicationService.deleteMedication(userId, medicationId)` 호출
4.  **MedicationService**:
    a.  `@Transactional` 시작
    b.  `MedicationRepository.findByIdAndUserId(medicationId, userId)` 호출하여 약 존재 및 소유권 확인
    c.  약이 존재하지 않거나 다른 사용자 소유인 경우 `ResourceNotFoundException` 발생
    d.  `MedicationRepository.deleteByIdAndUserId(medicationId, userId)` 호출하여 삭제 실행
    e.  DB 제약조건 위반 시 `DataIntegrityViolationException` catch하여 `BusinessRuleViolationException`로 변환
    f.  `@Transactional` 커밋
5.  **MedicationController**:
    a.  성공 시 `ResponseEntity.noContent().build()` 반환 (HTTP 204 No Content)
6.  **Client**: 성공 응답 수신

## 8. 예외 처리

-   **`PathVariableTypeMismatchException`**: `medication_id`가 Long 타입으로 변환되지 않는 경우. `GlobalExceptionHandler`에서 `400 Bad Request` 처리.
-   **`ResourceNotFoundException`**: 해당 ID의 약이 존재하지 않거나 다른 사용자 소유인 경우. `GlobalExceptionHandler`에서 `404 Not Found` 처리.
-   **`DataIntegrityViolationException`**: 복용 기록이 있는 약을 삭제하려 할 때 DB 제약조건 위반. 서비스에서 `BusinessRuleViolationException`로 변환하여 `GlobalExceptionHandler`에서 `409 Conflict` 처리.
-   **인증 실패 (`AuthenticationException` 등)**: JWT 토큰이 유효하지 않거나 없는 경우. Spring Security 필터 체인에서 `401 Unauthorized` 처리.
-   **기타 서버 오류**: `GlobalExceptionHandler`에서 `500 Internal Server Error` 처리.

## 9. 데이터베이스 영향 분석

### 9.1. CASCADE 관계 (자동 삭제)
```sql
-- medications 삭제 시 자동 삭제되는 테이블
medication_reminders (ON DELETE CASCADE)
```

### 9.2. RESTRICT 관계 (삭제 제한)
```sql
-- medications 삭제 시 제약조건으로 보호되는 테이블
medication_taken_records (ON DELETE RESTRICT)
```

### 9.3. 삭제 시나리오
1. **정상 삭제**: 복용 기록이 없는 약 → 관련 알림과 함께 성공적으로 삭제
2. **제약조건 위반**: 복용 기록이 있는 약 → `DataIntegrityViolationException` 발생, `409 Conflict` 응답

## 10. 보안 고려 사항 (OWASP Top 10 등)

-   **A01: Broken Access Control**: 
    -   인증된 사용자만 약을 삭제할 수 있도록 JWT 토큰 검증
    -   본인 소유의 약만 삭제 가능하도록 `userId` 기반 소유권 검증
-   **A03: Injection**: 
    -   Spring Data JPA 사용으로 SQL Injection 위험 감소
    -   Path Parameter의 타입 안전성 보장
-   **A04: Insecure Design**: 
    -   중요한 기록 데이터 보존을 위한 RESTRICT 제약조건 적용
    -   CASCADE 삭제 범위를 적절히 제한
-   **A05: Security Misconfiguration**: 
    -   적절한 HTTP 상태 코드 사용으로 정보 노출 최소화
-   **A10: Server-Side Request Forgery (SSRF)**: 
    -   입력값 검증을 통한 악의적 요청 방지

## 11. 로깅 및 모니터링

### 11.1. 로깅 포인트
- 삭제 요청 수신 시점 (INFO 레벨)
- 삭제 성공 시점 (INFO 레벨)
- 권한 없는 삭제 시도 (WARN 레벨)
- 제약조건 위반으로 인한 삭제 실패 (WARN 레벨)
- 시스템 오류 (ERROR 레벨)

### 11.2. 메트릭스
- 삭제 요청 수
- 삭제 성공률
- 권한 위반 시도 횟수
- 제약조건 위반 발생 횟수

## 12. 추후 고려 사항

-   **소프트 삭제**: 물리적 삭제 대신 `is_deleted` 플래그를 사용한 논리적 삭제 방식 검토
-   **삭제 확인**: 중요한 데이터 삭제 시 추가 확인 단계 도입 (2단계 인증 등)
-   **감사 로그**: 삭제 작업에 대한 상세한 감사 로그 기록
-   **복구 기능**: 실수로 삭제된 약 정보의 복구 기능 제공

--- 