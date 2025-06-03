# Daily Condition Tracker API

개인의 건강 상태(식단, 증상, 약 복용, 수면 등)를 매일 기록하고 관리하여 건강 증진 및 자기 관리에 도움을 주는 것을 목표로 하는 API 서비스입니다.

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술-스택](#기술-스택)
- [패키지-구조](#패키지-구조)
- [주요-기능-및-api-엔드포인트](#주요-기능-및-api-엔드포인트)
- [데이터베이스-스키마](#데이터베이스-스키마)
- [애플리케이션-실행-방법](#애플리케이션-실행-방법)
- [개발-가이드라인](#개발-가이드라인)
- [상세-문서](#상세-문서)

## 프로젝트 개요

본 프로젝트는 사용자가 자신의 식단, 증상, 약 복용, 수면 등 건강 관련 정보를 간편하게 기록하고, 약 복용 알림을 통해 복약 순응도를 높이며, 꾸준한 건강 기록 습관 형성을 돕는 것을 주요 목표로 합니다.

자세한 내용은 [제품 요구사항 문서 (PRD)](./docs/PRD.md)를 참고해주세요.

## 기술 스택

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.4.5
- **빌드 도구**: Gradle
- **주요 도구**:
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - OpenAPI (Swagger UI) - API 문서화
- **데이터베이스**: MySQL
- **형상 관리**: Git

## 패키지 구조

본 프로젝트는 프레젠테이션(Presentation), 애플리케이션(Application), 도메인(Domain), 인프라스트럭처(Infrastructure)의 4개 계층으로 구성된 레이어드 아키텍처를 따릅니다.

- **`presentation`**: API 엔드포인트(RestController), DTO 담당
- **`application`**: 비즈니스 로직 및 비즈니스 워크플로우 오케스트레이션
- **`domain`**: 핵심 도메인 로직, 도메인 이벤트 담당 
    - 이 패키지에 순수 도메인 객체가 위치하며, DB와의 매핑을 위한 JPA 엔티티는 모듈의 `infrastructure` 패키지 내에 정의됩니다.
- **`infrastructure`**: 엔티티, 리포지토리 구현체, 외부 서비스 연동, DB 설정 담당
- **`global`**: 전역 설정(보안, 예외 처리, OpenAPI 등) 담당

계층 간 의존성은 단방향(Presentation -> Application -> Domain <- Infrastructure)을 따릅니다.

## 주요 기능 및 API 엔드포인트

### 주요 기능

- 사용자 인증 (회원가입, 로그인, 비밀번호 재설정)
- 건강 기록 입력 (식단, 증상, 약 복용, 수면, 메모)
- 약 관리 (등록, 조회, 수정, 삭제)
- 약 복용 알림 설정
- 기록 조회 및 관리 (목록, 상세, 수정, 삭제)

### 핵심 API 엔드포인트 예시

- **인증**:
    - `POST /api/auth/signup`: 회원 가입
    - `POST /api/auth/login`: 로그인
    - `GET /api/users/me`: 현재 사용자 정보 조회
- **기록**:
    - `POST /api/records/diet`: 식단 기록 추가
    - `POST /api/records/symptom`: 증상 기록 추가
    - `GET /api/records`: 기록 목록 조회
    - `GET /api/records/{recordId}`: 특정 기록 상세 조회
- **약 관리**:
    - `POST /api/medications`: 새 약 등록
    - `GET /api/medications`: 내 약 목록 조회
    - `PATCH /api/medications/{medicationId}`: 약 정보 수정
- **헬스 체크**:
    - `GET /api/health`: 서버 상태 확인

전체 API 명세는 [API 명세서](./docs/API-명세서.md) 및 [화면 디자인-API 매핑 문서](./docs/design-to-API.md)를 참고해주세요.
또한, 각 기능별 상세 API 디자인 문서는 `docs/auth` 및 `docs/medications` 디렉토리에서 확인할 수 있습니다.

## 데이터베이스 스키마

주요 테이블은 다음과 같습니다:

- `users`: 사용자 계정 정보
- `medications`: 사용자 등록 약 목록
- `medication_reminders`: 약 복용 알림 설정
- `records`: 모든 기록의 기본 정보 (마스터 테이블)
- `diet_records`, `symptom_records`, `medication_taken_records`, `sleep_records`, `note_records`: 각 기록 유형별 상세 정보

상세한 DDL 및 관계는 [DB 스키마 문서](./docs/db-schema.md)를 참고해주세요.

## 애플리케이션 실행 방법

1.  **데이터베이스 설정**: `application.yml` (또는 `application.properties`) 파일에 올바른 MySQL 접속 정보를 입력합니다.
2.  **애플리케이션 실행**: Gradle을 사용하여 빌드 및 실행합니다.
    ```bash
    ./gradlew bootRun
    ```
    또는 IDE에서 Spring Boot 애플리케이션을 직접 실행합니다.

3.  **API 문서 확인**: 애플리케이션 실행 후, 브라우저에서 다음 URL로 접속하여 Swagger API 문서를 확인할 수 있습니다 (포트 및 경로 설정에 따라 다를 수 있음):
    `http://localhost:8080/swagger-ui.html` (기본값 예시)

## 개발 가이드라인

본 프로젝트는 다음과 같은 원칙과 가이드라인을 준수합니다:

- **계층형 아키텍처**: 명확한 책임 분리를 위해 레이어드 아키텍처를 적용합니다.
- **도메인 객체 중심 로직**: Application 서비스 계층은 순수 도메인 객체를 사용하여 비즈니스 로직을 처리합니다.
- **JPA 엔티티**: DB 데이터 운반 및 매핑 용도로 주로 사용됩니다.
- **생성자 주입**: 의존성은 생성자 주입을 통해 관리합니다.
- **예외 처리**:
    - 특정 비즈니스 예외 상황에 대해 커스텀 예외 클래스(예: `ResourceNotFoundException`, `BusinessRuleViolationException`)를 정의하여 사용합니다.
    - 전역 예외 처리는 `GlobalExceptionHandler` 클래스에서 담당하며, 모든 API 응답은 일관된 `ApiResponse` 형식으로 반환됩니다.
- **로깅**: 주요 비즈니스 로직, 예외 발생 시점에 SLF4J를 사용하여 로그를 기록합니다.

## 상세 문서

프로젝트와 관련된 더 자세한 정보는 다음 문서들을 참고해주십시오:

- [제품 요구사항 문서 (PRD)](./docs/PRD.md)
- [API 명세서](./docs/API-명세서.md)
- [DB 스키마](./docs/db-schema.md)
- [개발 계획 문서](./docs/DEVELOPMENT_PLAN.md)
- [화면 디자인 - API 매핑](./docs/design-to-API.md)
- API 디자인
    - [인증 관련 API 디자인](./docs/auth/)
    - [약물 관리 관련 API 디자인](./docs/medications/)

---

*본 README 문서는 프로젝트 초기 단계의 정보를 기반으로 작성되었으며, 프로젝트 진행에 따라 내용이 업데이트될 수 있습니다.* 