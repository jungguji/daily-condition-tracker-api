# API 설계: 로그인 (POST /auth/login)

## 1. 개요

-   **목표**: 사용자가 이메일과 비밀번호를 사용하여 로그인하고 JWT 액세스 토큰과 리프레시 토큰을 발급받을 수 있도록 합니다.
-   **엔드포인트**: `POST /api/v1/auth/login`
-   **HTTP 메소드**: `POST`
-   **인증**: 불필요
-   **관련 PRD**: FR-ACC-002

## 2. 요청 DTO (Request DTO)

**파일명**: `com.jgji.daily_condition_tracker.auth.presentation.dto.LoginRequest.java`

```java
package com.jgji.daily_condition_tracker.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        String password
) {
}
```

## 3. 응답 DTO (Response DTO)

**파일명**: `com.jgji.daily_condition_tracker.auth.presentation.dto.LoginResponse.java`

```java
package com.jgji.daily_condition_tracker.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("refresh_token")
        String refreshToken
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, "bearer", refreshToken);
    }
}
```

## 4. 토큰 갱신 DTO

**파일명**: `com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshRequest.java`

```java
package com.jgji.daily_condition_tracker.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @JsonProperty("refresh_token")
        String refreshToken
) {
}
```

**파일명**: `com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshResponse.java`

```java
package com.jgji.daily_condition_tracker.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRefreshResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("refresh_token")
        String refreshToken
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, "bearer", refreshToken);
    }
}
```

## 5. Controller (`AuthController.java`)

**경로**: `com.jgji.daily_condition_tracker.auth.presentation.AuthController.java`

```java
package com.jgji.daily_condition_tracker.auth.presentation;

import com.jgji.daily_condition_tracker.auth.application.AuthService;
import com.jgji.daily_condition_tracker.auth.presentation.dto.LoginRequest;
import com.jgji.daily_condition_tracker.auth.presentation.dto.LoginResponse;
import com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshRequest;
import com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshResponse;
import com.jgji.daily_condition_tracker.global.infra.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

## 6. Service (`AuthService.java`)

**경로**: `com.jgji.daily_condition_tracker.auth.application.AuthService.java`

```java
package com.jgji.daily_condition_tracker.auth.application;

import com.jgji.daily_condition_tracker.auth.infrastructure.AuthTokenProvider;
import com.jgji.daily_condition_tracker.auth.infrastructure.TokenBlacklistService;
import com.jgji.daily_condition_tracker.auth.presentation.dto.LoginRequest;
import com.jgji.daily_condition_tracker.auth.presentation.dto.LoginResponse;
import com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshRequest;
import com.jgji.daily_condition_tracker.auth.presentation.dto.TokenRefreshResponse;
import com.jgji.daily_condition_tracker.domain.user.User;
import com.jgji.daily_condition_tracker.domain.user.UserRepository;
import com.jgji.daily_condition_tracker.global.exception.InvalidCredentialsException;
import com.jgji.daily_condition_tracker.global.exception.InvalidTokenException;
import com.jgji.daily_condition_tracker.global.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthTokenProvider authTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 스프링 시큐리티를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // 2. 인증된 사용자 정보 조회
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new InvalidCredentialsException("인증에 실패했습니다."));

            // 3. JWT 토큰 생성 (액세스 토큰 + 리프레시 토큰)
            String accessToken = authTokenProvider.generateAccessToken(authentication);
            String refreshToken = authTokenProvider.generateRefreshToken(authentication);

            // 4. 응답 DTO 생성 및 반환
            return LoginResponse.of(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // 1. 리프레시 토큰 유효성 검증
        if (!authTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. 블랙리스트 검증
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException("블랙리스트에 등록된 토큰입니다.");
        }

        // 3. 사용자 정보 조회
        String username = authTokenProvider.getUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다."));

        // 4. 새로운 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, Collections.emptyList());
        String newAccessToken = authTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = authTokenProvider.generateRefreshToken(authentication);

        // 5. 기존 리프레시 토큰 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(refreshToken);

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("유효하지 않은 Authorization 헤더입니다.");
        }

        String accessToken = authorizationHeader.substring(7);

        // 액세스 토큰을 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(accessToken);
    }
}
```

## 7. 확장된 토큰 제공자 (`AuthTokenProvider.java`)

**경로**: `com.jgji.daily_condition_tracker.auth.infrastructure.AuthTokenProvider.java`

```java
package com.jgji.daily_condition_tracker.auth.infrastructure;

import com.jgji.daily_condition_tracker.global.security.filter.JwtTokenProvider;
import com.jgji.daily_condition_tracker.global.security.value.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class AuthTokenProvider {

    private final JwtTokenProvider jwtTokenProvider; // 기존 토큰 제공자 활용
    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = jwtProperties.getExpirationMs();
        this.refreshTokenExpirationMs = jwtProperties.getExpirationMs() * 24 * 7; // 7일 (액세스 토큰의 7배)
    }

    /**
     * 액세스 토큰 생성 (기존 JwtTokenProvider 활용)
     */
    public String generateAccessToken(Authentication authentication) {
        return jwtTokenProvider.generateToken(authentication);
    }

    /**
     * 리프레시 토큰 생성
     */
    public String generateRefreshToken(Authentication authentication) {
        long now = new Date().getTime();
        Date expiryDate = new Date(now + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim("type", "refresh")
                .issuedAt(new Date(now))
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 액세스 토큰 유효성 검증 (기존 JwtTokenProvider 활용)
     */
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // refresh 타입인지 확인
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 리프레시 토큰에서 사용자명 추출
     */
    public String getUsernameFromRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 액세스 토큰에서 사용자명 추출 (기존 JwtTokenProvider 활용)
     */
    public String getUsernameFromAccessToken(String token) {
        return jwtTokenProvider.getUsername(token);
    }
}
```

## 8. 토큰 블랙리스트 서비스 인터페이스

**파일명**: `com/jgji/daily_condition_tracker/domain/auth/infrastructure/TokenRepository.java`

```java
package com.jgji.daily_condition_tracker.auth.infrastructure;

/**
 * JWT 토큰 블랙리스트 관리 저장소
 * 현재는 메모리 저장소를 사용하고, 추후 Redis로 확장 가능
 */
public interface TokenRepository {

    /**
     * 토큰을 블랙리스트에 추가
     */
    void addToBlacklist(String token);

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    boolean isBlacklisted(String token);

    /**
     * 만료된 토큰들을 블랙리스트에서 제거 (정리 작업)
     */
    void cleanExpiredTokens();
}
```

## 9. 메모리 기반 토큰 블랙리스트 구현체

**파일명**: `com/jgji/daily_condition_tracker/domain/auth/infrastructure/TokenRepositoryImpl.java`
**파일명**: `com/jgji/daily_condition_tracker/domain/auth/infrastructure/LocalTokenRepository.java`

```java
package com.jgji.daily_condition_tracker.auth.infrastructure;

import com.jgji.daily_condition_tracker.global.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final JwtTokenProvider jwtTokenProvider;
    private final LocalTokenRepository localTokenRepository;

    @Override
    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
        log.debug("토큰이 블랙리스트에 추가되었습니다. 현재 블랙리스트 크기: {}", blacklistedTokens.size());
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * 매일 자정에 만료된 토큰들을 정리
     */
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanExpiredTokens() {
        int initialSize = blacklistedTokens.size();
        blacklistedTokens.removeIf(token -> !jwtTokenProvider.validateToken(token));
        int finalSize = blacklistedTokens.size();

        log.info("만료된 토큰 정리 완료. 제거된 토큰 수: {}, 남은 토큰 수: {}",
                initialSize - finalSize, finalSize);
    }
}
```

## 10. 기존 SecurityConfig 수정사항

**파일명**: `src/main/java/com/jgji/daily_condition_tracker/global/security/config/SecurityConfig.java`

```java
// 기존 파일에 다음 내용 추가/수정

@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}

// permitAll 경로에 추가
.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
```

## 11. 기존 JwtAuthenticationFilter 수정사항

**파일명**: `src/main/java/com/jgji/daily_condition_tracker/global/security/filter/JwtAuthenticationFilter.java`

```java
// doFilterInternal 메소드에 블랙리스트 검증 로직 추가

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService; // 추가

    // 생성자에 TokenBlacklistService 추가
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                 UserDetailsService userDetailsService,
                                 TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
            String username = jwtTokenProvider.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
    
    // resolveToken 메소드는 기존과 동일
}
```

## 12. 예외 처리 확장

**사용자 정의 예외**: `com.jgji.daily_condition_tracker.global.exception.InvalidTokenException.java`

```java
package com.jgji.daily_condition_tracker.global.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
```

**GlobalExceptionHandler에 추가**:

```java
@ExceptionHandler(InvalidCredentialsException.class)
public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(401, ex.getMessage()));
}

@ExceptionHandler(InvalidTokenException.class)
public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(401, ex.getMessage()));
}
```

## 13. 설정 파일 확장 (application.yml)

```yaml
jwt:
  secret: "your-secret-key-here-must-be-at-least-256-bits-long-for-hs256-algorithm"
  expiration-ms: 3600000 # 1시간 (액세스 토큰)
  # 리프레시 토큰은 액세스 토큰의 7배 (7일)로 코드에서 설정

spring:
  task:
    scheduling:
      enabled: true # 스케줄링 활성화 (토큰 정리용)
```

## 14. API 엔드포인트 추가

### 14.1. 토큰 갱신 (POST /auth/refresh)

**요청**:
```json
{
  "refresh_token": "eyJhbGci..."
}
```

**응답**:
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "bearer",
  "refresh_token": "eyJhbGci..."
}
```

### 14.2. 로그아웃 (POST /auth/logout)

**요청 헤더**:
```
Authorization: Bearer eyJhbGci...
```

**응답**:
```json
{
  "code": 200,
  "status": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": null
}
```

## 15. 보안 강화 사항

1.  **토큰 만료 시간**: 액세스 토큰 1시간, 리프레시 토큰 7일
2.  **토큰 타입 검증**: 리프레시 토큰에 `type: refresh` 클레임 추가
3.  **블랙리스트 관리**: 로그아웃 시 토큰 무효화, 스케줄링을 통한 만료 토큰 정리
4.  **토큰 갱신 보안**: 기존 리프레시 토큰 즉시 블랙리스트 등록
5.  **인터페이스 설계**: Redis 등 외부 저장소로 확장 가능한 구조

## 16. 추후 개선 사항

- Redis 기반 토큰 블랙리스트 구현체 추가
- 토큰 갱신 시 리프레시 토큰 로테이션 정책 강화
- 로그인 시도 횟수 제한 및 계정 잠금 기능
- 다중 디바이스 로그인 관리
- 소셜 로그인 (Google, Kakao) 통합
- 토큰 발급 로깅 및 모니터링 