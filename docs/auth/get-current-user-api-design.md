# API 설계: 현재 사용자 정보 조회 (GET /users/me)

## 1. 개요

- **목표**: 현재 인증된 사용자의 기본 정보를 조회할 수 있도록 합니다.
- **엔드포인트**: `GET /api/v1/users/me`
- **HTTP 메소드**: `GET`
- **인증**: 필요 (JWT 토큰)
- **관련 PRD**: 사용자 계정 관련 기능, 인증된 사용자 정보 확인

## 2. 기능 요구사항

### 2.1. 핵심 기능
- JWT 토큰을 통해 인증된 사용자의 정보를 조회
- 사용자의 기본 정보 (ID, 이메일, 닉네임, 계정 상태 등) 반환
- 보안상 민감한 정보 (비밀번호 해시 등)는 제외

### 2.2. 비기능 요구사항
- 인증되지 않은 요청에 대한 적절한 오류 응답
- 탈퇴하거나 비활성화된 계정에 대한 처리
- 응답 시간 최적화 (단순 조회)

## 3. API 설계

### 3.1. 요청 사양
```
GET /api/v1/users/me
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**요청 본문**: 없음
**쿼리 파라미터**: 없음

### 3.2. 응답 사양

#### 성공 응답 (200 OK)
```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "user_id": 1,
    "email": "user@example.com",
    "nickname": "사용자닉네임",
    "social_provider": null,
    "is_active": true,
    "is_verified": true,
    "is_superuser": false,
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

#### 오류 응답
- **401 Unauthorized**: 유효하지 않은 토큰
- **403 Forbidden**: 비활성화된 계정
- **500 Internal Server Error**: 서버 내부 오류

## 4. 응답 DTO 설계

### 4.1. CurrentUserResponse
```java
public record CurrentUserResponse(
    @JsonProperty("user_id")
    Long userId,
    
    String email,
    
    String nickname,
    
    @JsonProperty("social_provider")
    String socialProvider,
    
    @JsonProperty("is_active")
    boolean isActive,
    
    @JsonProperty("is_verified")
    boolean isVerified,
    
    @JsonProperty("is_superuser")
    boolean isSuperuser,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
)
```

### 4.2. DTO 변환 로직
- User 도메인 객체에서 응답 DTO로 변환하는 정적 팩토리 메소드 제공
- 민감한 정보 (비밀번호 해시, 소셜 ID 등) 제외
- 타임스탬프는 ISO 8601 형식으로 변환

## 5. Controller 설계

### 5.1. 위치 및 네이밍
- **패키지**: `com.jgji.daily_condition_tracker.domain.user.presentation`
- **클래스명**: `UserController`
- **메소드명**: `getCurrentUser()`

### 5.2. 핵심 로직
```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
        @AuthenticationPrincipal CustomUserPrincipal userDetails) {
    
    User currentUser = userDetails.getUser();
    CurrentUserResponse response = userService.getCurrentUserInfo(currentUser);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 5.3. 설계 고려사항
- `@AuthenticationPrincipal`을 통해 인증된 사용자 정보 직접 획득
- UserService에 비즈니스 로직 위임
- 일관된 ApiResponse 형식으로 응답

## 6. Service 설계

### 6.1. UserService 메소드
- **메소드명**: `getCurrentUserInfo(User user)`
- **역할**: User 도메인 객체를 CurrentUserResponse DTO로 변환
- **트랜잭션**: `@Transactional(readOnly = true)`

### 6.2. 비즈니스 로직
```java
@Transactional(readOnly = true)
public CurrentUserResponse getCurrentUserInfo(User user) {
    // 1. 사용자 상태 검증
    validateUserStatus(user);
    
    // 2. DTO 변환 및 반환
    return CurrentUserResponse.from(user);
}
```

### 6.3. 검증 로직
- 계정 활성화 상태 확인
- 탈퇴 상태 확인
- 필요시 추가 비즈니스 규칙 적용

## 7. 보안 설계

### 7.1. 인증 방식
- JWT 토큰 기반 인증
- Spring Security의 `@AuthenticationPrincipal` 활용
- 토큰 유효성은 JwtAuthenticationFilter에서 사전 검증

### 7.2. 정보 보안
- 비밀번호 해시 제외
- 소셜 로그인 ID 제외 (보안상 민감)
- 내부 시스템 정보 제외

### 7.3. 접근 제어
- 본인의 정보만 조회 가능 (토큰 기반 자동 제한)
- 관리자 권한 별도 처리 불필요 (개인 정보 조회)

## 8. 예외 처리 설계

### 8.1. 예상 예외 상황
- 토큰 만료 또는 무효
- 계정 비활성화
- 계정 삭제
- 사용자 정보 조회 실패

### 8.2. 예외 처리 전략
```java
private void validateUserStatus(User user) {
    if (user.isNotActive()) {
        throw new UserNotActiveException("비활성화된 계정입니다.");
    }
    
    if (user.isDeleted()) {
        throw new UserDeletedException("삭제된 계정입니다.");
    }
}
```

### 8.3. GlobalExceptionHandler 확장
- `UserNotActiveException` 처리 (403 Forbidden)
- `UserDeletedException` 처리 (403 Forbidden)

## 9. 데이터 흐름

### 9.1. 요청 흐름
```
1. Client -> GET /api/v1/users/me (with JWT token)
2. JwtAuthenticationFilter -> 토큰 검증 및 사용자 인증
3. UserController -> @AuthenticationPrincipal로 User 객체 획득
4. UserService -> 사용자 정보 조회 및 DTO 변환
5. Controller -> ApiResponse로 응답 포장
6. Client <- JSON 응답
```

### 9.2. 데이터 변환
```
JWT Token -> CustomUserPrincipal -> User Domain Object -> CurrentUserResponse DTO -> ApiResponse<CurrentUserResponse>
```

## 10. 테스트 설계

### 10.1. 단위 테스트
- UserService의 getCurrentUserInfo() 메소드 테스트
- CurrentUserResponse DTO 변환 테스트
- 예외 상황별 처리 테스트

### 10.2. 통합 테스트
- 인증된 요청에 대한 정상 응답 테스트
- 무효한 토큰에 대한 401 응답 테스트
- 비활성화된 계정에 대한 403 응답 테스트

### 10.3. 보안 테스트
- 토큰 없는 요청 차단 테스트
- 만료된 토큰 요청 차단 테스트
- 민감한 정보 노출 방지 테스트

## 11. 성능 고려사항

### 11.1. 최적화 포인트
- 단순 조회이므로 캐싱 불필요
- 데이터베이스 조회 없이 토큰에서 추출된 정보 활용
- 응답 크기 최소화 (필요한 정보만 포함)

### 11.2. 모니터링
- API 호출 빈도 모니터링
- 응답 시간 측정
- 오류율 추적

## 12. 확장성 고려사항

### 12.1. 추후 확장 가능 사항
- 사용자 프로필 이미지 URL 추가
- 사용자 설정 정보 포함
- 마지막 로그인 시간 정보 추가
- 구독 상태 등 부가 정보 포함

### 12.2. API 버전 관리
- 응답 필드 추가 시 하위 호환성 유지
- 필요시 v2 API 별도 제공 고려

## 13. 기존 코드와의 통합

### 13.1. 기존 구조 활용
- AuthController 패턴과 일관성 유지
- CustomUserPrincipal 기반 사용자 인증
- ApiResponse 응답 형식 준수
- 기존 예외 처리 체계 활용

### 13.2. 코드 재사용
- User 도메인 객체 재사용
- 기존 보안 설정 그대로 활용
- GlobalExceptionHandler 확장

## 14. 배포 및 운영

### 14.1. 환경별 고려사항
- 개발/스테이징/프로덕션 환경 동일 동작
- 로그 레벨 설정 (INFO 레벨로 호출 로그)

### 14.2. 모니터링 및 로깅
```java
log.info("현재 사용자 정보 조회 요청: userId={}", user.getUserId());
log.debug("현재 사용자 정보 조회 성공: userId={}, email={}", user.getUserId(), user.getEmail().getValue());
```

---

**참고사항**: 
- 이 API는 사용자 인증이 전제되므로 보안이 중요합니다.
- 응답에 포함되는 정보는 클라이언트에서 사용자 인터페이스 구성에 필요한 최소한의 정보로 제한합니다.
- 추후 사용자 정보 수정 API와 연계하여 일관된 사용자 경험을 제공할 수 있도록 설계되었습니다. 