# API 설계: 회원 가입 (POST /auth/signup)

## 1. 개요

-   **목표**: 사용자가 이메일과 비밀번호를 사용하여 신규 계정을 생성할 수 있도록 합니다.
-   **엔드포인트**: `POST /api/v1/auth/signup`
-   **HTTP 메소드**: `POST`
-   **인증**: 불필요
-   **관련 PRD**: FR-ACC-001

## 2. 요청 DTO (Request DTO)

**파일명**: `com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpRequest.java`

```java
package com.jgji.daily_condition_tracker.presentation.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        // 비밀번호 정책 관련 추가 어노테이션 고려 (예: 정규식)
        String password
) {
}
```

## 3. 응답 DTO (Response DTO)

**파일명**: `com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpResponse.java`

```java
package com.jgji.daily_condition_tracker.presentation.auth.dto;

public record SignUpResponse(
        Long userId,
        String email
) {
}
```
-   `API-명세서.md` 에는 `user_id` 로 되어 있으나, Java에서는 camelCase를 따릅니다.

## 4. Controller (`AuthController.java`)

**경로**: `com.jgji.daily_condition_tracker.presentation.auth.AuthController.java`

```java
package com.jgji.daily_condition_tracker.presentation.auth;

import com.jgji.daily_condition_tracker.application.auth.AuthService;
import com.jgji.daily_condition_tracker.global.infra.ApiResponse;
import com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpRequest;
import com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
```

**처리 흐름**:
1.  `@Valid` 어노테이션을 통해 `SignUpRequest` DTO의 유효성을 검사합니다. (실패 시 `MethodArgumentNotValidException` 발생, `GlobalExceptionHandler`에서 처리)
2.  `AuthService` 클래스의 `signUp` 메소드를 호출하여 비즈니스 로직을 수행합니다.
3.  성공 시, 서비스로부터 `SignUpResponse` DTO를 반환받습니다.
4.  `ApiResponse.success()`를 사용하여 표준 응답 형식으로 감싸고, HTTP 상태 코드 `201 Created`와 함께 응답합니다.

## 5. Service (`AuthService.java`)

**경로**: `com.jgji.daily_condition_tracker.application.auth.AuthService.java`

```java
package com.jgji.daily_condition_tracker.application.auth;

import com.jgji.daily_condition_tracker.domain.users.User;
import com.jgji.daily_condition_tracker.domain.users.UserRepository;
// import com.jgji.daily_condition_tracker.global.exception.EmailAlreadyExistsException; // 사용자 정의 예외
import com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpRequest;
import com.jgji.daily_condition_tracker.presentation.auth.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            // throw new EmailAlreadyExistsException("이미 사용 중인 이메일입니다: " + request.email());
            // 임시로 RuntimeException 사용, 추후 변경
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + request.email());
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. User 엔티티 생성 및 저장
        User newUser = User.builder()
                .email(request.email())
                .password(encodedPassword)
                // .role(UserRole.USER) // 역할(Role)이 있다면 설정
                .build();

        User savedUser = userRepository.save(newUser);

        // 4. 응답 DTO 생성 및 반환
        return new SignUpResponse(savedUser.getId(), savedUser.getEmail());
    }
}
```

**주요 로직**:
1.  `UserRepository`를 사용하여 요청된 이메일이 이미 존재하는지 확인합니다. 존재하면 `EmailAlreadyExistsException` (또는 유사한 예외)을 발생시켜 409 Conflict 응답을 유도합니다.
2.  `PasswordEncoder`를 사용하여 사용자의 비밀번호를 안전하게 암호화합니다.
3.  `User` 엔티티를 생성하고, 암호화된 비밀번호와 이메일을 설정합니다. (필요시 사용자 역할(Role) 등 초기값 설정)
4.  `UserRepository`를 통해 `User` 엔티티를 데이터베이스에 저장합니다.
5.  저장된 사용자 정보(ID, 이메일)를 사용하여 `SignUpResponse` DTO를 생성하여 반환합니다.
6.  전체 과정은 `@Transactional` 어노테이션을 통해 단일 트랜잭션으로 관리됩니다.

## 6. Repository (`UserRepository.java`)

**경로**: `com.jgji.daily_condition_tracker.domain.user.infrastructure.UserRepository.java`
```
- `findByEmail`: 로그인 및 기타 이메일 기반 사용자 조회 시 사용.
- `existsByEmail`: 회원 가입 시 이메일 중복 검사에 사용.

```
- `db-schema.md` 또는 ERD를 참고하여 `users` 테이블의 정확한 컬럼명과 제약조건을 반영해야 합니다. (예: `role`, `created_at`, `updated_at` 등)
- 비밀번호는 암호화되어 저장되므로 길이를 충분히 확보해야 합니다. (e.g., `length = 255` 또는 `TEXT` 타입)
- `UserRole`과 같은 Enum 타입은 별도로 정의하거나 내부에 정의할 수 있습니다.
- UserRepository는 인터페이스며, 실제 구현은 UserRepositoryImpl에서 처리됩니다.
- UserRepositoryImpl은 내부에서 UserJPARepository를 주입받아서 해당 클래스의 메서드를 사용합니다.

```java

## 8. 예외 처리 (Exception Handling)

-   **이메일 중복 (`EmailAlreadyExistsException` - Custom Exception)**
    -   HTTP 상태 코드: `409 Conflict`
    -   `GlobalExceptionHandler` 에서 처리하여 `ApiResponse.fail(409, ex.getMessage())` 형태로 응답합니다.
    -   (예시) `GlobalExceptionHandler.java` 에 추가:
        ```java
        // @ExceptionHandler(EmailAlreadyExistsException.class)
        // public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        //     return ResponseEntity
        //             .status(HttpStatus.CONFLICT)
        //             .body(ApiResponse.fail(HttpStatus.CONFLICT.value(), ex.getMessage()));
        // }
        ```
-   **요청 DTO 유효성 검사 실패 (`MethodArgumentNotValidException`)**
    -   HTTP 상태 코드: `400 Bad Request`
    -   `GlobalExceptionHandler` 에서 이미 처리 로직이 존재할 것으로 예상됩니다. (제공된 `GlobalExceptionHandler.java` 내용 참고)

## 9. 보안 고려 사항

1.  **비밀번호 암호화**: `PasswordEncoder` 를 사용하여 단방향 암호화를 적용합니다.
2.  **입력값 검증**:
    -   Controller DTO 레벨에서 JSR 303/380 Bean Validation 어노테이션을 사용하여 이메일 형식, 길이 등을 검증합니다.
    -   Service 레벨에서 비즈니스 규칙(예: 이메일 중복)을 검증합니다.
3.  **HTTPS**: 프로덕션 환경에서는 통신 전 과정에 HTTPS를 적용하여 데이터 전송을 암호화해야 합니다.
4.  **민감 정보 로깅**: 로그에 비밀번호와 같은 민감 정보가 직접 노출되지 않도록 주의합니다.
5.  **의존성 관리**: Spring Security 등 보안 관련 라이브러리를 최신 상태로 유지하여 알려진 취약점에 대응합니다.

## 10. 흐름도 (Sequence Diagram - PlantUML)

```plantuml
@startuml
actor Client
participant AuthController
participant AuthService
participant UserRepository
participant PasswordEncoder
database Database

Client -> AuthController: POST /api/v1/auth/signup (SignUpRequest)
activate AuthController

AuthController -> AuthService: signUp(request)
activate AuthService

AuthService -> UserRepo
sitory: existsByEmail(email)
activate UserRepository
UserRepository -> Database: SELECT COUNT(*) FROM users WHERE email = ?
Database --> UserRepository: count
UserRepository --> AuthService: boolean (isExist)
deactivate UserRepository

alt Email Exists
    AuthService --> AuthController: EmailAlreadyExistsException
    deactivate AuthService
    AuthController --> Client: 409 Conflict (ApiResponse)
    deactivate AuthController
else Email Not Exists
    AuthService -> PasswordEncoder: encode(rawPassword)
    activate PasswordEncoder
    PasswordEncoder --> AuthService: encodedPassword
    deactivate PasswordEncoder

    AuthService -> UserRepository: save(User.builder().build())
    activate UserRepository
    UserRepository -> Database: INSERT INTO users (...) VALUES (...)
    Database --> UserRepository: Saved User (with ID)
    UserRepository --> AuthService: savedUser
    deactivate UserRepository

    AuthService --> AuthController: SignUpResponse
    deactivate AuthService

    AuthController --> Client: 201 Created (ApiResponse<SignUpResponse>)
    deactivate AuthController
end

@enduml
```

## 11. 추후 개선 사항
- `UserRole` Enum 정의 및 적용
- 사용자 정의 예외 클래스 (`EmailAlreadyExistsException`) 생성 및 `GlobalExceptionHandler`에 등록
- 비밀번호 정책 강화 (예: 특수문자, 대소문자, 숫자 조합) 및 관련 유효성 검사 추가 