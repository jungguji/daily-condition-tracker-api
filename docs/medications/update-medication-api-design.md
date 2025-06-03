# API 설계: 약물 정보 수정 (PATCH /medications/{medication_id})

## 1. 개요

사용자가 기존에 등록한 특정 약물의 정보를 부분적으로 수정하는 API입니다. 사용자는 본인이 등록한 약물에 대해서만 수정 권한을 가집니다.

## 2. API 엔드포인트

- **HTTP Method**: `PATCH`
- **Path**: `/api/v1/medications/{medication_id}`
- **인증**: 필요 (OAuth 2.0 Bearer Token)

## 3. 요청 (Request)

### 3.1. Path Parameters

| 이름             | 타입   | 필수 | 설명         |
| ---------------- | ------ | ---- | ------------ |
| `medication_id` | `Long` | Y    | 약물 ID (PK) |

### 3.2. Request Body

`application/json` 형식의 `MedicationUpdateRequest` DTO를 사용합니다. 모든 필드는 선택적으로 제공될 수 있으며, 제공된 필드만 업데이트됩니다.

**`MedicationUpdateRequest.java` (DTO)**

```java
public record MedicationUpdateRequest(
    @Size(max = 100, message = "약물 이름은 100자를 초과할 수 없습니다.") // API 명세서 상 medications.name VARCHAR(255)지만, 일반적으론 100자 내외로 제한. DB 스키마에 맞게 조정 필요.
    String name, // 약물 이름

    @Min(value = 1, message = "복용량은 1 이상이어야 합니다.") // DB medications.dosage SMALLINT UNSIGNED
    Integer dosage, // 1회 복용량

    @Size(max = 50, message = "단위는 50자를 초과할 수 없습니다.") // DB medications.unit VARCHAR(50)
    String unit, // 복용량 단위 (예: "정", "ml", "mg")

    @Size(max = 65535, message = "설명은 65535자를 초과할 수 없습니다.") // DB medications.description TEXT
    String description, // 약물 설명 (선택 사항)

    Boolean isActive // 약물 활성 상태 (현재 복용중인지 여부 등)
) {
    // 모든 필드가 null일 수 있도록 허용
}
```

### 3.3. 유효성 검사

- `medication_id`: 양의 정수여야 합니다.
- `MedicationUpdateRequest` DTO 필드 (JSR 303/380 Bean Validation):
    - `name`: 제공될 경우, 최대 100자 (DB 스키마에 따라 조정).
    - `dosage`: 제공될 경우, 1 이상의 정수.
    - `unit`: 제공될 경우, 최대 50자.
    - `description`: 제공될 경우, 최대 65535자 (TEXT 타입).
    - `isActive`: 제공될 경우, boolean 값.
- 서비스 계층에서 요청 바디의 모든 필드가 `null`인지 검사합니다. (모두 `null`이면 `BusinessRuleViolationException` 발생)

## 4. 응답 (Response)

### 4.1. 성공

- **HTTP Status**: `200 OK`
- **Body**: `ApiResponse<MedicationResponse>`

**`MedicationResponse.java` (DTO) - (기존 DTO 재활용 및 타입 일치화)**

```java
import java.time.OffsetDateTime;

public record MedicationResponse(
    Long id,
    String name,
    Integer dosage,
    String unit,
    String description,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    // 기존 from 메소드 활용 또는 필요시 생성자/빌더 추가
    public static MedicationResponse from(Medication medication) { // 예시, 실제로는 필드 직접 매핑
        return new MedicationResponse(
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
}
```

### 4.2. 실패

| HTTP Status | `ApiResponse.code` | `ApiResponse.status` | `ApiResponse.message` (예시)                        | 설명                                                                |
| ----------- | ------------------ | -------------------- | ----------------------------------------------------- | ------------------------------------------------------------------- |
| 400         | 400                | `VALIDATION_ERROR`   | "입력 데이터 검증에 실패했습니다." (상세 오류는 `data` 필드에) | 요청 DTO 유효성 검사 실패 (`@Valid`에 의해)                        |
| 400         | 400                | `BAD_REQUEST`        | "수정할 내용이 없습니다."                                 | (BusinessRuleViolationException) Request Body의 모든 필드가 `null`인 경우 |
| 400         | 400                | `BAD_REQUEST`        | (BusinessRuleViolationException에 따름)                 | (BusinessRuleViolationException) 기타 비즈니스 규칙 위반 (예: 약물 이름 중복) |
| 401         | 401                | `UNAUTHORIZED`       | "인증되지 않은 사용자입니다." / (토큰 관련 메시지)          | 유효한 인증 토큰이 없는 경우 / 토큰 만료 등 (`AuthenticationException`, `InvalidTokenException`) |
| 403         | 403                | `FORBIDDEN`          | "해당 약물을 수정할 권한이 없습니다."                       | 다른 사용자의 약물을 수정하려는 경우 (`AuthorizationException`)     |
| 404         | 404                | `NOT_FOUND`          | "약 ID '{medication_id}'을(를) 찾을 수 없습니다."        | (ResourceNotFoundException) 해당 ID의 약물이 존재하지 않는 경우       |
| 500         | 500                | `INTERNAL_SERVER_ERROR` | "서버 내부 오류가 발생했습니다."                         | 그 외 서버 오류 (`Exception`)                                     |

## 5. 처리 로직 (Layer별 상세)

### 5.1. Presentation Layer (`MedicationController`)

- **메소드 시그니처**:
  ```java
  @PatchMapping("/{medicationId}")
  public ResponseEntity<ApiResponse<MedicationResponse>> updateMedication(
      @AuthenticationPrincipal UserPrincipal userPrincipal, // Spring Security 통해 주입 (커스텀 Principal 가정)
      @PathVariable Long medicationId,
      @Valid @RequestBody MedicationUpdateRequest requestDto
  )
  ```
- **주요 로직**:
    1.  `userPrincipal`에서 `userId`를 추출합니다. (null 체크 및 예외 처리, 인증되지 않은 접근은 Spring Security에서 처리)
    2.  `@Valid`를 통해 `MedicationUpdateRequest` DTO의 필드 레벨 유효성 검사를 수행합니다. (`MethodArgumentNotValidException` 발생 가능, `GlobalExceptionHandler`에서 처리)
    3.  `MedicationService.updateMedication(userId, medicationId, requestDto)`를 호출합니다.
    4.  성공 시, 반환된 `MedicationResponse`를 `ApiResponse.success()`로 감싸 `200 OK` 응답을 반환합니다.
    5.  서비스 계층에서 발생한 예외는 `GlobalExceptionHandler`에 의해 처리됩니다.

### 5.2. Application Layer (`MedicationService`)

- **메소드 시그니처**:
  ```java
  @Transactional
  public MedicationResponse updateMedication(Long userId, Long medicationId, MedicationUpdateRequest dto)
  ```
- **주요 로직**:
    1.  `dto`의 모든 필드가 `null`인지 검사합니다.
        ```java
        // 예시: 모든 필드가 null인지 확인하는 로직
        if (Stream.of(dto.name(), dto.dosage(), dto.unit(), dto.description(), dto.isActive())
            .allMatch(Objects::isNull)) {
            throw new BusinessRuleViolationException("수정할 내용이 없습니다.");
        }
        ```
    2.  `MedicationRepository.findByIdAndUserId(medicationId, userId)`를 호출하여 `Medication` 도메인 객체를 조회합니다.
        - 엔티티가 없거나 `userId`가 일치하지 않으면 `ResourceNotFoundException("약", "ID", medicationId)`를 발생시킵니다. (기존 `findMedicationById`와 일관된 예외 메시지 사용)
    3.  조회된 `Medication` 도메인 객체의 `withUpdates()` 메소드를 호출하여 새로운 `Medication` 인스턴스를 생성합니다. 이 메소드는 DTO로부터 받은 값으로 엔티티를 업데이트하며, 유효성 검사를 포함합니다.
        ```java
        Medication originalMedication = medicationRepository.findByIdAndUserId(medicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("약", "ID", medicationId));

        // 이름 중복 검사 (이름이 변경된 경우에만 수행)
        if (dto.name() != null && !dto.name().equals(originalMedication.getName())) {
            if (medicationRepository.existsByNameAndUserIdAndIdNot(dto.name(), userId, medicationId)) {
                throw new BusinessRuleViolationException("이미 동일한 이름의 약물이 존재합니다: " + dto.name());
            }
        }

        Medication updatedMedication = originalMedication.withUpdates(
            dto.name(),
            dto.dosage(),
            dto.unit(),
            dto.description(),
            dto.isActive()
        );
        ```
    4.  업데이트된 `Medication` 도메인 객체를 `MedicationRepository.save(updatedMedication)`을 통해 저장합니다. (Repository는 내부적으로 Domain -> Entity 변환 후 저장)
    5.  저장된 `Medication` 도메인 객체를 `MedicationResponse.from(savedMedication)` (또는 유사한 매퍼)을 통해 `MedicationResponse` DTO로 변환하여 반환합니다.

### 5.3. Domain Layer (`Medication`)

- **`withUpdates` 메소드 추가**:
  ```java
  // Medication.java
  public Medication withUpdates(String name, Integer dosage, String unit, String description, Boolean isActive) {
      // 각 필드에 대한 유효성 검사 수행
      // 예: 이름이 제공되었지만 비어있는 경우, 길이 초과 등
      if (name != null) {
          validateName(name); // 기존 static validateName(name) 사용 또는 내용 통합
      }
      if (dosage != null && dosage < 1) {
          throw new IllegalArgumentException("복용량은 1 이상이어야 합니다."); // 또는 BusinessRuleViolationException
      }
      // unit, description, isActive에 대한 유효성 검사 추가 (필요시)

      return Medication.builder()
              .medicationId(this.medicationId)
              .userId(this.userId)
              .name(name != null ? name : this.name)
              .dosage(dosage != null ? dosage : this.dosage)
              .unit(unit != null ? unit : this.unit)
              .description(description != null ? description : this.description) // DTO에서 null이 오면 기존값 유지, 빈 문자열 오면 빈 문자열로 업데이트
              .isActive(isActive != null ? isActive : this.isActive)
              .createdAt(this.createdAt) // 생성일은 변경하지 않음
              .updatedAt(OffsetDateTime.now()) // 수정일은 현재 시간으로 업데이트
              .build();
  }

  // 기존 validateName은 static이므로, withUpdates 내부에서 필요시 호출하거나 로직을 가져와 사용.
  // private static void validateName(String name) {
  //     if (name == null || name.trim().isEmpty()) {
  //         throw new IllegalArgumentException("약 이름은 필수값입니다."); // BusinessRuleViolationException 사용 권장
  //     }
  //     if (name.length() > 255) { // MedicationUpdateRequest DTO의 @Size 와 일치시킬 것
  //         throw new IllegalArgumentException("약 이름은 최대 255자까지 입력 가능합니다.");
  //     }
  // }
  ```
- 도메인 유효성 검사 실패 시 `IllegalArgumentException` 또는 가급적 `BusinessRuleViolationException` 발생.

### 5.4. Infrastructure Layer (`MedicationRepository` / `MedicationRepositoryImpl`)

- `findByIdAndUserId(Long medicationId, Long userId)`: `Optional<Medication>` 반환 (기존 정의 활용).
- `save(Medication medication)`: `Medication` 도메인 객체를 받아 저장 후, 저장된 `Medication` 도메인 객체 반환 (내부적으로 Entity 변환).
- **신규 메소드 추가 (MedicationRepository 인터페이스)**:
  ```java
  // MedicationRepository.java (Spring Data JPA 인터페이스)
  Optional<Medication> findByIdAndUserId(Long id, Long userId);
  boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id); // 약물 이름 중복 검사용 (동일 사용자의 다른 약물)
  ```

## 6. DTO 설계 (요약)

- **`MedicationUpdateRequest`**: 위 3.2. 참고. 모든 필드는 `null` 허용 (Wrapper 타입 사용).
- **`MedicationResponse`**: 위 4.1. 참고. 수정된 약물의 전체 정보를 포함. 타입 `OffsetDateTime`으로 통일.

## 7. 예외 처리 (상세)

- **`ResourceNotFoundException`**: 서비스 계층에서 약물을 찾을 수 없거나 사용자 ID가 일치하지 않을 때 발생. `GlobalExceptionHandler`에서 `404 Not Found`로 처리.
- **`MethodArgumentNotValidException`**: 컨트롤러에서 `@Valid` 어노테이션에 의해 DTO 유효성 검사 실패 시 발생. `GlobalExceptionHandler`에서 `400 Bad Request` (status: `VALIDATION_ERROR`)로 처리.
- **`BusinessRuleViolationException`**: 서비스 또는 도메인 계층에서 비즈니스 규칙 위반 시 발생.
    - 예: "수정할 내용이 없습니다."
    - 예: "이미 동일한 이름의 약물이 존재합니다."
    - `GlobalExceptionHandler`에서 `400 Bad Request`로 처리.
- **`IllegalArgumentException`**: 도메인 객체 내 유효성 검사 등에서 발생 가능. `GlobalExceptionHandler`에서 `400 Bad Request`로 처리. (가급적 `BusinessRuleViolationException` 사용 권장)
- **`AuthenticationException`, `InvalidTokenException`**: 인증 실패 또는 유효하지 않은 토큰. `GlobalExceptionHandler`에서 `401 Unauthorized`로 처리.
- **`AuthorizationException`**: 권한 부족. `GlobalExceptionHandler`에서 `403 Forbidden`으로 처리. (Spring Security의 `AccessDeniedException`도 유사하게 처리될 수 있음)
- **`DataIntegrityViolationException`**: (필요시) DB 제약 조건 위반 시 발생 가능. `GlobalExceptionHandler`에서 `409 Conflict` 또는 `400 Bad Request` 등으로 상황에 맞게 처리 (현재 `GlobalExceptionHandler`에는 명시적 핸들러 없음).

## 8. 데이터베이스 상호작용

- **조회**: `SELECT * FROM medications WHERE medication_id = ? AND user_id = ?`
- **중복 이름 검사**: `SELECT COUNT(*) FROM medications WHERE name = ? AND user_id = ? AND medication_id <> ?`
- **업데이트**: `UPDATE medications SET name = ?, dosage = ?, unit = ?, description = ?, is_active = ?, updated_at = ? WHERE medication_id = ?`
    - JPA 사용 시, 변경 감지(dirty checking) 및 명시적 `save` 호출을 통해 업데이트 SQL 자동 생성.

## 9. 기타 고려사항

- **동시성 제어**: 여러 사용자가 동시에 같은 약물 정보를 수정하려는 경우 (일반적으로 이 애플리케이션에서는 드문 시나리오). 필요시 Optimistic Locking (`@Version` 어노테이션) 고려.
- **로깅**: 주요 단계 (요청 수신, 서비스 호출, DB 작업, 예외 발생 등)에서 SLF4J를 이용한 로깅 추가.
- **보안**: `userId`는 반드시 인증된 사용자 정보(`@AuthenticationPrincipal`)에서 가져와야 하며, 다른 사용자의 정보를 수정할 수 없도록 서비스 계층에서 `medicationId`와 `userId`를 함께 사용하여 소유권을 검증합니다. 