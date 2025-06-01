# **기본 규칙**

- **Base URL:** https://your-api-domain.com/v1 (예시)
- **인증 (Authentication):** 로그인이 필요한 모든 요청은 HTTP 헤더에 Authorization: Bearer <JWT_TOKEN> 형식으로 유효한 JWT 토큰을 포함해야 합니다.
- **데이터 형식:** 요청 및 응답 본문은 기본적으로 application/json 형식을 사용합니다. (로그인 제외 가능성 있음)
- **날짜/시간 형식:** ISO 8601 형식을 사용합니다. (예: YYYY-MM-DDTHH:MM:SSZ 또는 YYYY-MM-DD)
- **성공 응답:**
    - 200 OK: 요청 성공 (조회, 수정 성공 등)
    - 201 Created: 리소스 생성 성공
    - 204 No Content: 요청 성공했으나 반환할 콘텐츠 없음 (삭제 성공 등)

- 400 Bad Request: 잘못된 요청 형식 (예: 필수 필드 누락, 잘못된 데이터 타입)
- 401 Unauthorized: 인증 실패 (유효한 토큰 없음 또는 토큰 만료)
- 403 Forbidden: 인가 실패 (해당 리소스에 접근할 권한 없음)
- 404 Not Found: 요청한 리소스를 찾을 수 없음
- 409 Conflict: 리소스 충돌 (예: 이미 존재하는 이메일로 가입 시도)
- 422 Unprocessable Entity: 요청 형식은 맞으나 의미론적으로 오류가 있음 (FastAPI 유효성 검사 실패 시 자주 사용됨)
- 500 Internal Server Error: 서버 내부 오류

# **1단계: 사용자 인증 (Authentication)**

| 기능 | HTTP Method | URL Path | 인증 | 설명 | 요청 본문 (Request Body) | 성공 응답 (Success Response) | 주요 오류 응답 | 관련 FR |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **회원 가입** | POST | /auth/signup | 불필요 | 이메일/비밀번호로 신규 사용자 생성 | { "email": "user@example.com", "password": "securepassword123" } | 201 Created { "user_id": 1, "email": "user@example.com" } | 400, 409, 422 | FR-ACC-001 |
| **로그인** | POST | /auth/login | 불필요 | 이메일/비밀번호로 로그인, JWT 토큰 발급 | application/x-www-form-urlencoded: username=user@example.com&password=securepassword123 (OAuth2 표준) 또는 application/json: { "email": "user@example.com", "password": "securepassword123" } (FastAPI 조정 가능) | 200 OK { "access_token": "eyJhbGci...", "token_type": "bearer" } | 400, 401 | FR-ACC-002 |
| **로그아웃** (선택) | POST | /auth/logout | 필요 | (서버 측 토큰 무효화 시) | 없음 | 200 OK or 204 No Content | 401 | FR-ACC-002 |
| **비밀번호 재설정 요청** | POST | /auth/password-reset/request | 불필요 | 비밀번호 재설정 토큰 이메일 발송 요청 | { "email": "user@example.com" } | 200 OK { "message": "Password reset email sent if user exists." } (사용자 존재 여부 노출 X) | 422 | FR-ACC-003 |
| **비밀번호 재설정 확인** | POST | /auth/password-reset/confirm | 불필요 | 토큰과 새 비밀번호로 비밀번호 변경 | { "token": "reset_token_from_email", "new_password": "new_secure_password" } | 200 OK { "message": "Password updated successfully." } | 400 (Invalid/Expired Token), 422 | FR-ACC-003 |
| **현재 사용자 정보 조회** | GET | /users/me | 필요 | 현재 로그인된 사용자 정보 확인 | 없음 | 200 OK { "user_id": 1, "email": "user@example.com", "nickname": null } |  |  |

# **2단계: 기록 입력 (Data Entry)**

| 기능 | HTTP Method | URL Path | 인증 | 설명 | 요청 본문 (Request Body) Example | 성공 응답 (Success Response) Example | 주요 오류 응답 | 관련 FR |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **식단 기록 추가** | POST | /records/diet | 필요 | 새로운 식단 기록을 생성합니다. record_time 미제공 시 서버 현재 시간 사용. | { "record_time": "2024-10-27T08:30:00Z", "food_item": "사과 1개, 요거트" } | 201 Created {
 "record_id": 101, "user_id": 1, "record_time": "2024-10-27T08:30:00Z", 
"record_type": "DIET", "details": { "diet_record_id": 51, "food_item": 
"사과 1개, 요거트", "photo_url": null } } | 400, 401, 422 | FR-ENT-001, FR-ENT-002, FR-ENT-003, FR-ENT-007 |
| **증상 기록 추가** | POST | /records/symptom | 필요 | 새로운 증상 기록을 생성합니다. 모든 증상 필드는 선택적이지만, 최소 하나 이상은 제공해야 의미가 있습니다. | {
 "record_time": "2024-10-27T09:00:00Z", "bowel_movement_type": "LOOSE", 
"abdominal_pain_present": true, "abdominal_pain_scale": 3, 
"fatigue_scale": 2, "other_symptoms": "복부 팽만감 약간 있음", 
"overall_condition_scale": 3, "mood_type": "NEUTRAL" } | 201 Created {
 "record_id": 102, "user_id": 1, "record_time": "2024-10-27T09:00:00Z", 
"record_type": "SYMPTOM", "details": { "symptom_record_id": 62, 
"bowel_movement_type": "LOOSE", "bowel_movement_custom": null, 
"abdominal_pain_present": true, "abdominal_pain_scale": 3, 
"fatigue_scale": 2, "other_symptoms": "복부 팽만감 약간 있음", 
"overall_condition_scale": 3, "mood_type": "NEUTRAL", "mood_custom": 
null } } | 400, 401, 422 | FR-ENT-001, FR-ENT-002, FR-ENT-004, FR-ENT-007 |
| **약 복용 기록 추가** | POST | /records/medication-taken | 필요 | 특정 약의 복용 기록을 생성합니다. medication_id는 '내 약 관리'에 등록된 약의 ID여야 합니다. | { "record_time": "2024-10-27T08:05:00Z", "medication_id": 5, "taken_from_reminder_id": 12 } <br/> *(taken_from_reminder_id는 알림 통해 복용시 전달, 선택 사항)* | 201 Created {
 "record_id": 103, "user_id": 1, "record_time": "2024-10-27T08:05:00Z", 
"record_type": "MED_TAKEN", "details": { "med_taken_record_id": 73, 
"medication_id": 5, "taken_from_reminder_id": 12 } } | 400, 401, 404, 422 | FR-ENT-001, FR-ENT-002, FR-ENT-005, FR-ENT-007 |
| **수면 기록 추가** | POST | /records/sleep | 필요 | 수면 시간을 기록합니다. | { "record_time": "2024-10-27T07:00:00Z", "sleep_duration_minutes": 450 } <br/> *(기상 시간 기준 record_time 권장)* | 201 Created {
 "record_id": 104, "user_id": 1, "record_time": "2024-10-27T07:00:00Z", 
"record_type": "SLEEP", "details": { "sleep_record_id": 84, 
"sleep_duration_minutes": 450, "sleep_quality_scale": null } } | 400, 401, 422 | FR-ENT-001, FR-ENT-002, FR-ENT-006 (수면), FR-ENT-007 |
| **메모 기록 추가** | POST | /records/note | 필요 | 자유 형식의 메모를 기록합니다. | { "record_time": "2024-10-27T10:00:00Z", "note_content": "오늘 병원 방문 예정. 의사에게 물어볼 것 정리." } | 201 Created {
 "record_id": 105, "user_id": 1, "record_time": "2024-10-27T10:00:00Z", 
"record_type": "NOTE", "details": { "note_record_id": 95, 
"note_content": "오늘 병원 방문 예정. 의사에게 물어볼 것 정리." } } | 400, 401, 422 | FR-ENT-001, FR-ENT-002, FR-ENT-006 (메모), FR-ENT-007 |

**참고 사항:**

1. **record_time:** 모든 기록 추가 요청 시 record_time (ISO 8601 형식)을 포함하여 과거 기록을 입력할 수 있습니다. 포함되지 않으면 서버에서 요청 처리 시점의 시간을 사용합니다.
2. **증상 기록 (/records/symptom) 상세:**
    - bowel_movement_type: ENUM 값 (DIARRHEA, LOOSE, NORMAL, HARD, CONSTIPATION, BLOODY, OTHER) 중 하나. OTHER 선택 시 bowel_movement_custom에 상세 내용 입력.
    - abdominal_pain_scale: 1 (약함) ~ 5 (매우 심함)
    - fatigue_scale: 1 (약간) ~ 5 (매우 심함) 또는 0 (없음) ~ 3 (심함). (FR 상 예시가 다르므로 프로젝트 내에서 통일 필요. 여기서는 1-5 척도로 가정)
    - overall_condition_scale: 1 (매우 나쁨) ~ 5 (매우 좋음)
    - mood_type: ENUM 값 (GOOD, NEUTRAL, BAD, ANXIOUS, IRRITABLE, OTHER) 중 하나. OTHER 선택 시 mood_custom에 상세 내용 입력.
3. **약 복용 기록 (/records/medication-taken)**:
    - medication_id: /medications (약 관리) API를 통해 얻은 사용자의 약 ID입니다. 존재하지 않는 ID 요청 시 404 Not Found 또는 422 Unprocessable Entity 오류가 발생할 수 있습니다.
    - taken_from_reminder_id: (선택적) 약 복용 알림(medication_reminders 테이블의 ID)을 통해 복용 기록이 발생한 경우 해당 알림 ID를 포함할 수 있습니다 (FR-MED-005 Nice-to-have 구현 시).
4. **수면 기록 (/records/sleep)**:
    - sleep_duration_minutes: 분 단위의 정수 값입니다 (예: 7시간 30분 = 450).
    - sleep_quality_scale: ERD에는 있지만 FR에는 명시되지 않은 필드입니다. 필요시 1-5점 척도로 추가 구현 가능합니다. API 응답 예시에는 포함시켰습니다.
5. **응답 구조:** 각 기록 생성 성공 시, records 테이블의 기본 정보와 해당 상세 테이블(diet_records, symptom_records 등)의 정보가 포함된 details 객체를 반환하여 생성된 리소스를 명확히 보여줍니다.

# **3단계: 약 관리 및 복용 알림 (Medication Management & Reminders)**

| 기능 | HTTP Method | URL Path | 인증 | 설명 | 요청 본문 (Request Body) Example | 성공 응답 (Success Response) Example | 주요 오류 응답 | 관련 FR |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **내 약 목록 조회** | GET | /medications | 필요 | 사용자가 등록한 약 목록을 조회합니다. 기본적으로 활성(is_active=true) 상태인 약만 반환하거나, 쿼리 파라미터로 제어할 수 있습니다. | *(없음 - 쿼리 파라미터 사용 가능: ?is_active=true/false)* | 200 OK [
 { "medication_id": 5, "user_id": 1, "name": "메디락디에스", "dosage": "1포", 
"unit": null, "description": "식후 30분", "is_active": true, "created_at": 
"...", "updated_at": "..." }, ... ] | 401 | FR-MED-001 |
| **특정 약 상세 조회** | GET | /medications/{medication_id} | 필요 | 특정 약의 상세 정보를 조회합니다. | *(없음)* | 200 OK {
 "medication_id": 5, "user_id": 1, "name": "메디락디에스", "dosage": "1포", 
"unit": null, "description": "식후 30분", "is_active": true, "created_at": 
"...", "updated_at": "..." } | 401, 403, 404 | FR-MED-001 |
| **새 약 등록** | POST | /medications | 필요 | 사용자가 복용하는 새로운 약 정보를 등록합니다. | { "name": "타이레놀", "dosage": "500", "unit": "mg", "description": "필요시 복용", "is_active": true } | 201 Created {
 "medication_id": 6, "user_id": 1, "name": "타이레놀", "dosage": "500", 
"unit": "mg", "description": "필요시 복용", "is_active": true, "created_at": 
"...", "updated_at": "..." } | 400, 401, 422 | FR-MED-001 |
| **약 정보 수정** | PATCH | /medications/{medication_id} | 필요 | 등록된 약의 정보를 수정합니다. (PATCH는 부분 수정 지원) | { "description": "두통 심할 때만 복용", "is_active": false } | 200 OK {
 "medication_id": 6, "user_id": 1, "name": "타이레놀", "dosage": "500", 
"unit": "mg", "description": "두통 심할 때만 복용", "is_active": false, 
"created_at": "...", "updated_at": "..." } | 400, 401, 403, 404, 422 | FR-MED-001 |
| **약 삭제** | DELETE | /medications/{medication_id} | 필요 | 등록된 약 정보를 삭제합니다. (DB 제약 조건에 따라 관련 알림도 삭제됨) | *(없음)* | 204 No Content | 401, 403, 404 | FR-MED-001 |
| **특정 약의 알림 목록 조회** | GET | /medications/{medication_id}/reminders | 필요 | 특정 약에 설정된 모든 복용 알림 시간 목록을 조회합니다. | *(없음)* | 200 OK [
 { "reminder_id": 12, "medication_id": 5, "user_id": 1, "reminder_time":
 "08:00:00", "is_enabled": true, "created_at": "...", "updated_at": 
"..." }, { "reminder_id": 15, "medication_id": 5, "user_id": 1, 
"reminder_time": "18:00:00", "is_enabled": true, "created_at": "...", 
"updated_at": "..." } ] | 401, 403, 404 | FR-MED-002 |
| **(전체) 알림 목록 조회** | GET | /medication-reminders | 필요 | 사용자가 설정한 모든 약의 모든 알림 목록을 조회합니다. (알림 설정 화면 등에서 사용) | *(없음)* | 200 OK [
 { "reminder_id": 12, "medication_id": 5, ... }, { "reminder_id": 15, 
"medication_id": 5, ... }, { "reminder_id": 20, "medication_id": 6, ... }
 ] *(위와 유사한 형식)* | 401 | FR-MED-002, FR-MED-004 |
| **새 알림 설정** | POST | /medications/{medication_id}/reminders | 필요 | 특정 약에 대한 새로운 복용 알림 시간을 설정합니다. | { "reminder_time": "09:00:00", "is_enabled": true } *(시간은 HH:MM 또는 HH:MM:SS 형식)* | 201 Created {
 "reminder_id": 21, "medication_id": 6, "user_id": 1, "reminder_time": 
"09:00:00", "is_enabled": true, "created_at": "...", "updated_at": "..."
 } | 400, 401, 403, 404, 409 (중복 시간), 422 | FR-MED-002 |
| **알림 정보 수정** | PATCH | /medication-reminders/{reminder_id} | 필요 | 특정 알림의 시간 또는 활성화 여부를 수정합니다. (알림 켜고 끄기 기능 포함) | { "reminder_time": "08:30:00" } 또는 { "is_enabled": false } | 200 OK {
 "reminder_id": 12, "medication_id": 5, "user_id": 1, "reminder_time": 
"08:30:00", "is_enabled": true, "created_at": "...", "updated_at": "..."
 } | 400, 401, 403, 404, 422 | FR-MED-002, FR-MED-004 |
| **알림 삭제** | DELETE | /medication-reminders/{reminder_id} | 필요 | 설정된 특정 알림을 삭제합니다. | *(없음)* | 204 No Content | 401, 403, 404 | FR-MED-002 |

**참고 사항:**

1. **FR-MED-003 (푸시 알림)**: 푸시 알림 발송 자체는 서버의 백그라운드 작업(e.g., 스케줄러)에 의해 처리됩니다. 이 API 명세는 알림을 설정하고 관리하기 위한 인터페이스를 정의합니다. 백엔드는 medication_reminders 테이블의 is_enabled = true 인 알림들을 주기적으로 확인하여 해당 시간에 푸시 알림을 발송해야 합니다.
2. **FR-MED-004 (알림 켜고 끄기)**:
    - **개별 알림 켜고 끄기:** PATCH /medication-reminders/{reminder_id} API를 사용하여 is_enabled 값을 true 또는 false로 업데이트하여 구현합니다.
    - **전체 알림 켜고 끄기:** 별도 API를 만들기보다는, 클라이언트 측에서 GET /medication-reminders로 모든 알림 ID를 가져온 후, 각 알림에 대해 PATCH /medication-reminders/{reminder_id}를 호출하여 is_enabled 상태를 변경하는 방식으로 구현하거나, 필요하다면 PATCH /medication-reminders/toggle-all 같은 bulk 업데이트 API를 추가할 수 있습니다. (이 명세에는 포함하지 않음)
3. **FR-MED-005 (알림 탭 시 빠른 기록)**: 클라이언트가 푸시 알림을 수신하고 탭했을 때 처리하는 로직입니다. 알림 페이로드에 medication_id와 reminder_id를 포함시켜, 클라이언트가 이를 이용해 기록 입력 화면으로 이동하거나, "2단계: 기록 입력"의 POST /records/medication-taken API를 taken_from_reminder_id와 함께 호출하여 바로 복용 기록을 남길 수 있습니다.
4. **PATCH vs PUT:** 약 정보 수정(PATCH /medications/{medication_id})과 알림 정보 수정(PATCH /medication-reminders/{reminder_id})에 PATCH를 사용하여 부분 업데이트를 허용하는 것이 클라이언트 입장에서 더 유연합니다. PUT을 사용한다면 요청 본문에 리소스의 모든 필드를 포함해야 합니다.
5. **Consistency:** user_id는 요청 본문에 포함하지 않고, 인증된 JWT 토큰에서 추출하여 서버에서 사용합니다. 응답에는 포함될 수 있습니다.

# **4단계: 기록 조회 및 관리 (Record Viewing & Management)**

| 기능 | HTTP Method | URL Path | 인증 | 설명 | 요청 본문 (Request Body) Example | 성공 응답 (Success Response) Example | 주요 오류 응답 | 관련 FR |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **기록 목록 조회** | GET | /records | 필요 | 사용자가 입력한 모든 기록을 조회합니다. 시간 역순(최신순)으로 기본 정렬됩니다. 페이징, 날짜 필터링, 타입 필터링을 지원합니다.<br/>**Query Params:**<br/>- page (int, optional, default=1)<br/>- size (int, optional, default=20)<br/>- date (string, optional, YYYY-MM-DD)<br/>- type (string, optional, e.g., DIET, SYMPTOM) | *(없음)* | 200 OK {
 "total_count": 150, "page": 1, "size": 20, "items": [ { "record_id": 
105, "user_id": 1, "record_time": "2024-10-27T10:00:00Z", "record_type":
 "NOTE", "details": { "note_record_id": 95, "note_content": "..." } }, {
 "record_id": 104, "user_id": 1, "record_time": "2024-10-27T07:00:00Z", 
"record_type": "SLEEP", "details": { ... } }, ... ] } | 400 (Invalid params), 401 | FR-VIEW-001, FR-VIEW-002, FR-VIEW-003, FR-VIEW-004 |
| **특정 기록 상세 조회** | GET | /records/{record_id} | 필요 | 특정 record_id에 해당하는 기록의 상세 정보를 조회합니다. | *(없음)* | 200 OK {
 "record_id": 102, "user_id": 1, "record_time": "2024-10-27T09:00:00Z", 
"record_type": "SYMPTOM", "details": { "symptom_record_id": 62, 
"bowel_movement_type": "LOOSE", ..., "mood_custom": null } } | 401, 403, 404 | FR-VIEW-003 |
| **기록 수정** | PATCH | /records/{record_id} | 필요 | 특정 기록의 record_time 또는 상세 내용(details)을 수정합니다. 요청 본문에는 변경할 필드만 포함합니다. 서버는 record_id로 타입을 식별하여 처리합니다. | **시간만 수정:**<br/>{ "record_time": "2024-10-27T09:05:00Z" }<br/><br/>**식단 내용만 수정 (record_id가 식단 기록일 때):**<br/>{ "details": { "food_item": "바나나 1개, 수정된 요거트" } }<br/><br/>**증상 내용 수정 (record_id가 증상 기록일 때):**<br/>{ "details": { "abdominal_pain_scale": 2, "other_symptoms": "복부 팽만감 거의 없음" } } | 200 OK *(수정된 전체 기록 객체 반환, 위 '특정 기록 상세 조회' 응답 형식과 유사)* | 400, 401, 403, 404, 422 | FR-ENT-002 (시간 수정), FR-VIEW-005 |
| **기록 삭제** | DELETE | /records/{record_id} | 필요 | 특정 record_id에 해당하는 기록을 삭제합니다. (DB의 ON DELETE CASCADE 설정에 따라 상세 정보도 함께 삭제됩니다) | *(없음)* | 204 No Content | 401, 403, 404 | FR-VIEW-005 |

**참고 사항:**

1. **기록 목록 조회 (GET /records)**:
    - **응답 구조**: 페이징 정보를 포함하여 (total_count, page, size), 실제 기록 목록은 items 배열로 반환합니다.
    - **details 객체**: 각 기록 항목(item)에는 해당 기록 타입에 맞는 상세 정보가 details 객체 안에 포함됩니다. 클라이언트는 record_type을 보고 details 객체의 내용을 해석합니다.
    - **날짜 필터링 (date 파라미터)**: YYYY-MM-DD 형식으로 특정 날짜를 지정하면, 해당 날짜의 00:00:00부터 23:59:59 사이의 record_time을 가진 기록들을 필터링합니다. (서버 구현 시 타임존 고려 필요)
    - **타입 필터링 (type 파라미터)**: 특정 record_type (e.g., DIET)만 필터링하여 조회할 수 있습니다.
2. **기록 수정 (PATCH /records/{record_id})**:
    - 이 API는 기록의 **시간(record_time)** 과 **내용(details)** 수정을 모두 처리합니다.
    - 요청 본문에 record_time만 보내면 시간만 업데이트됩니다.
    - 요청 본문에 details 객체만 보내면 해당 기록 타입의 상세 내용만 업데이트됩니다. details 객체 안에는 **수정하려는 필드만** 포함합니다. (예: 증상 기록에서 복통 강도만 바꾸고 싶으면 {"details": {"abdominal_pain_scale": 2}} 와 같이 보냄)
    - record_time과 details를 모두 보내면 둘 다 업데이트됩니다.
    - 서버는 해당 record_id에 연결된 record_type을 먼저 조회하고, 요청으로 들어온 details 객체의 필드가 해당 타입에 유효한지 검증한 후, records 테이블과 적절한 상세 테이블(diet_records, symptom_records 등)을 업데이트해야 합니다.
3. **기록 삭제 (DELETE /records/{record_id})**:
    - 요청 성공 시 별도의 본문을 반환하지 않고 204 No Content 상태 코드를 반환하는 것이 일반적입니다.
    - ERD에서 records 테이블과 상세 테이블 간의 외래 키 제약 조건이 ON DELETE CASCADE로 설정되어 있으므로, records 테이블에서 항목이 삭제되면 연결된 상세 테이블의 항목도 자동으로 삭제됩니다.