# Daily Condition Tracker MVP 개발 계획 문서

**Version:** 1.0
**Date:** 2025-05-07

## 1. 서론

PRD, DB 스키마, API 명세서, 디자인-API 매핑 문서를 기반으로 작성되었으며, Spring Boot 개발 가이드라인을 준수하여 개발합니다.

개발 순서는 사용자 인증, 핵심 데이터 관리(약, 기록), 알림, 그리고 조회 기능 순으로 진행되며, 각 단계는 독립적으로 테스트 가능하도록 구성하는 것을 목표로 합니다.

## 2. 단계별 개발 계획

### **1단계: 사용자 인증 (User Authentication)**

-   **목표:** 사용자 계정 생성, 로그인, 로그아웃, 현재 사용자 정보 조회 등 기본적인 인증 기능을 구현합니다.
-   **주요 DB 테이블:** `users`
-   **주요 API 엔드포인트:**
    -   `POST /users/signup`
    -   `POST /auth/login`
    -   `POST /auth/logout` (선택적)
    -   `POST /users/password-reset/request`
    -   `POST /users/password-reset/confirm`
    -   `GET /users/me`
-   **관련 PRD 기능 요구사항:** FR-ACC-001, FR-ACC-002, FR-ACC-003

### **2단계: 약 관리 (Medication Management)**

-   **목표:** 사용자가 복용 중인 약 정보를 등록, 조회, 수정, 삭제할 수 있는 기능을 구현합니다.
-   **주요 DB 테이블:** `medications`
-   **주요 API 엔드포인트:**
    -   `GET /medications`
    -   `GET /medications/{medication_id}`
    -   `POST /medications`
    -   `PATCH /medications/{medication_id}`
    -   `DELETE /medications/{medication_id}`
-   **관련 PRD 기능 요구사항:** FR-MED-001

### **3단계: 약 복용 알림 설정 (Medication Reminders Setup)**

-   **목표:** 등록된 약에 대해 복용 알림 시간을 설정, 조회, 수정, 삭제할 수 있는 기능을 구현합니다. (실제 푸시 알림 발송 로직은 후순위 또는 별도 모듈로 분리 가능)
-   **주요 DB 테이블:** `medication_reminders`
-   **주요 API 엔드포인트:**
    -   `GET /medications/{medication_id}/reminders`
    -   `GET /medication-reminders` (전체 알림 목록)
    -   `POST /medications/{medication_id}/reminders`
    -   `PATCH /medication-reminders/{reminder_id}`
    -   `DELETE /medication-reminders/{reminder_id}`
-   **관련 PRD 기능 요구사항:** FR-MED-002, FR-MED-004

### **4단계: 기록 입력 - 기본 틀 및 공통 로직 (Data Entry - Base & Common Logic)**

-   **목표:** 모든 기록 유형(식단, 증상, 약 복용 등)의 기본 정보를 저장하는 `records` 테이블 및 관련 공통 로직을 구현합니다. 각 기록 생성 시 `record_time`, `record_type` 등을 처리합니다.
-   **주요 DB 테이블:** `records`
-   **공통 로직:**
    -   모든 기록 생성 요청 시 `record_time` (미제공 시 서버 현재 시간) 처리
    -   `records` 테이블에 기본 정보 저장 후, 각 상세 테이블에 데이터 저장하는 트랜잭션 관리
-   **관련 PRD 기능 요구사항:** FR-ENT-001, FR-ENT-002, FR-ENT-007 (공통 부분)

### **5단계: 개별 기록 유형 개발 (Data Entry - Specific Types)**

-   **목표:** 각 유형별 상세 기록을 입력받아 저장하는 기능을 구현합니다.
-   **5.1. 식단 기록 (Diet Record)**
    -   **주요 DB 테이블:** `diet_records`
    -   **주요 API 엔드포인트:** `POST /records/diet`
    -   **관련 PRD:** FR-ENT-003
-   **5.2. 증상 기록 (Symptom Record)**
    -   **주요 DB 테이블:** `symptom_records`
    -   **주요 API 엔드포인트:** `POST /records/symptom`
    -   **관련 PRD:** FR-ENT-004
-   **5.3. 약 복용 기록 (Medication Taken Record)**
    -   **주요 DB 테이블:** `medication_taken_records`
    -   **주요 API 엔드포인트:** `POST /records/medication-taken`
    -   **관련 PRD:** FR-ENT-005
-   **5.4. 수면 기록 (Sleep Record)**
    -   **주요 DB 테이블:** `sleep_records`
    -   **주요 API 엔드포인트:** `POST /records/sleep`
    -   **관련 PRD:** FR-ENT-006 (수면)
-   **5.5. 메모 기록 (Note Record)**
    -   **주요 DB 테이블:** `note_records`
    -   **주요 API 엔드포인트:** `POST /records/note`
    -   **관련 PRD:** FR-ENT-006 (메모)

### **6단계: 기록 조회 및 관리 (Record Viewing & Management)**

-   **목표:** 사용자가 입력한 모든 기록을 목록 형태로 조회하고, 특정 기록을 상세 조회, 수정, 삭제할 수 있는 기능을 구현합니다.
-   **주요 API 엔드포인트:**
    -   `GET /records` (목록 조회, 필터링, 페이징 포함)
    -   `GET /records/{record_id}` (상세 조회)
    -   `PATCH /records/{record_id}` (수정)
    -   `DELETE /records/{record_id}` (삭제)
-   **관련 PRD 기능 요구사항:** FR-VIEW-001, FR-VIEW-002, FR-VIEW-003, FR-VIEW-004, FR-VIEW-005, FR-ENT-002 (시간 수정 부분)

### **7단계: 기타 사용자 기능 및 마무리 (Miscellaneous & Wrap-up)**

-   **목표:** 회원 탈퇴 등 나머지 사용자 관련 기능 및 전체 시스템 안정화, 테스트.
-   **주요 DB 테이블:** `users` (deleted_at 업데이트 등)
-   **주요 API 엔드포인트:** (API 명세서에 명시되지 않은 경우 추가 정의 필요)
    -   회원 탈퇴 API (e.g., `DELETE /users/me`)
-   **관련 PRD 기능 요구사항:** FR-ACC-004
-   **푸시 알림 발송 로직 구현 (FR-MED-003):** 이 단계 또는 별도의 백그라운드 작업 모듈로 구현합니다. `medication_reminders` 테이블을 주기적으로 스캔하여 알림을 발송합니다.

## 3. 공통 개발 지침

-   모든 API 개발 시 `spring-boot.mdc`에 명시된 Spring Boot 개발 가이드라인을 철저히 준수합니다.
-   API 명세서(`API-명세서.md`)를 기준으로 요청/응답 형식을 일관되게 유지합니다.
-   단위 테스트 및 통합 테스트를 각 기능 개발 후 작성하여 코드 품질을 확보합니다.
-   주요 컴포넌트 개발 전 `/docs/[component].md` 개발 설계를 진행합니다.

## 4. 디자인 연동 참고

-   `docs/design-to-API.md` 문서는 각 화면 디자인과 관련된 API 엔드포인트를 매핑하고 있으므로, 피그마 MCP를 이용해 필요 시 참고하여 개발합니다.

---

본 개발 계획은 프로젝트 진행 상황 및 우선순위 변경에 따라 조정될 수 있습니다.
