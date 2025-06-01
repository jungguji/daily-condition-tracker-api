**화면 디자인 및 관련 API 요약:**

1. **로그인 화면**
    - 프레임 ID: `5:3`
    - 관련 API:
        - `POST /auth/login`
        - `POST /auth/password-reset/request` (비밀번호 찾기 링크 연결)
        - (회원가입 화면으로 이동)
2. **회원가입 화면**
    - 프레임 ID: `5:13`
    - 관련 API:
        - `POST /auth/signup`
3. **기록 목록 화면 (메인)**
    - 프레임 ID: `5:24`
    - 관련 API:
        - `GET /records` (목록 조회)
        - (FAB 클릭 시 기록 종류 선택 화면 이동)
        - (탭 바: 약 관리, 설정 화면 이동)
4. **기록 종류 선택 화면**
    - 프레임 ID: `5:32`
    - 관련 API: 없음 (화면 전환 로직)
5. **식단 기록 화면**
    - 프레임 ID: `6:45`
    - 관련 API:
        - `POST /records/diet` (새 기록 저장 시)
        - `PATCH /records/{record_id}` (기존 기록 수정 시)
6. **증상 기록 화면 (1/6: 변 상태)**
    - 프레임 ID: `6:54`
    - (Typeform 스타일의 시작점, 이후 단계 포함)
7. **증상 기록 화면 (2/6: 복통)**
    - 프레임 ID: `6:73`
8. **증상 기록 화면 (3/6: 피로도)**
    - 프레임 ID: `6:93`
9. **증상 기록 화면 (4/6: 기타 증상)**
    - 프레임 ID: `6:106`
10. **증상 기록 화면 (5/6: 컨디션)**
    - 프레임 ID: `6:113`
11. **증상 기록 화면 (6/6: 기분)**
    - 프레임 ID: `6:128`
    - 관련 API (증상 기록 전체 흐름):
        - `POST /records/symptom` (새 기록 저장 시)
        - `PATCH /records/{record_id}` (기존 기록 수정 시)
12. **약 복용 기록 화면**
    - 프레임 ID: `6:147`
    - 관련 API:
        - `POST /records/medication-taken` (새 기록 저장 시)
        - `GET /medications` (약 선택 목록 로드 시)
        - `PATCH /records/{record_id}` (기존 기록 수정 시)
13. **수면 기록 화면**
    - 프레임 ID: `7:157`
    - 관련 API:
        - `POST /records/sleep` (새 기록 저장 시)
        - `PATCH /records/{record_id}` (기존 기록 수정 시)
14. **메모 기록 화면**
    - 프레임 ID: `7:178`
    - 관련 API:
        - `POST /records/note` (새 기록 저장 시)
        - `PATCH /records/{record_id}` (기존 기록 수정 시)
15. **약 관리 화면**
    - 프레임 ID: `7:191`
    - 관련 API:
        - `GET /medications` (목록 조회)
        - `DELETE /medications/{medication_id}` (삭제 시)
        - ( '+' 클릭 시 새 약 추가 화면 이동)
        - (항목 탭 시 수정 화면 이동)
16. **새 약 추가/수정 화면**
    - 프레임 ID: `7:204`
    - 관련 API:
        - `POST /medications` (새 약 추가 저장 시)
        - `PATCH /medications/{medication_id}` (기존 약 수정 저장 시)
        - `DELETE /medications/{medication_id}` (삭제 버튼 클릭 시)
        - (알림 설정 버튼 클릭 시 알림 설정 화면 이동)
17. **약 알림 설정 화면**
    - 프레임 ID: `7:220`
    - 관련 API:
        - `GET /medications/{medication_id}/reminders` (목록 조회)
        - `POST /medications/{medication_id}/reminders` (새 알림 추가 시)
        - `PATCH /medication-reminders/{reminder_id}` (알림 토글 시)
        - `DELETE /medication-reminders/{reminder_id}` (알림 삭제 시)
18. **설정 화면**
    - 프레임 ID: `7:240`
    - 관련 API:
        - `POST /auth/logout` (로그아웃 시)
        - (메뉴 클릭 시 각 상세 설정 화면 이동)
19. **프로필 수정 화면**
    - 프레임 ID: `7:257`
    - 관련 API:
        - `GET /users/me` (화면 로드 시)
        - `PATCH /users/me` (저장 시 - API 명세서에 없으나 필요 예상)
20. **비밀번호 재설정 요청 화면**
    - 프레임 ID: `7:265`
    - 관련 API:
        - `POST /auth/password-reset/request`
21. **새 비밀번호 설정 화면**
    - 프레임 ID: `7:273`
    - 관련 API:
        - `POST /auth/password-reset/confirm`
22. **기록 상세 보기 화면 (증상 예시)**
    - 프레임 ID: `7:283`
    - 관련 API (기록 종류별로 상이):
        - `GET /records/{record_id}` (상세 내용 조회)
        - `PATCH /records/{record_id}` ('수정' 버튼 클릭 후 저장 시)
        - `DELETE /records/{record_id}` ('삭제' 버튼 클릭 시)