# users

```sql
-- -----------------------------------------------------
-- Table `users`
-- 사용자 계정 정보
-- -----------------------------------------------------
CREATE TABLE `users` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 ID',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이메일 주소 (로그인 ID)',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '해시된 비밀번호',
  `social_provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '소셜 로그인 제공자 (e.g., google, kakao)',
  `social_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '소셜 로그인 고유 ID',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '닉네임 (커뮤니티 기능 대비)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 정보 수정 일시',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '탈퇴(소프트 삭제) 일시',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '계정 활성화 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '계정 소프트삭제 여부',
  `is_superuser` tinyint(1) NOT NULL DEFAULT '0' COMMENT '관리자 여부',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '계정 인증 여부',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uq_social_provider_id` (`social_provider`,`social_id`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 계정 정보'
```

# password_reset_tokens

```sql
CREATE TABLE `password_reset_tokens` (
    `id` BIGINT unsigned AUTO_INCREMENT PRIMARY KEY COMMENT '토큰 ID',
    `token` VARCHAR(255) NOT NULL UNIQUE COMMENT '비밀번호 재설정 토큰',
    `user_id` BIGINT unsigned NOT NULL COMMENT '사용자 ID (users 테이블 참조)',
    `is_used` BOOLEAN DEFAULT FALSE COMMENT '사용 여부',
    `expiry_date` TIMESTAMP NOT NULL COMMENT '토큰 만료 일시',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    INDEX `idx_token` (`token`),
    INDEX `idx_user_id_created` (`user_id`, `created_at`),
    CONSTRAINT `fk_password_reset_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES users(`user_id`) ON DELETE CASCADE
) COMMENT '비밀번호 재설정 토큰 테이블';
```

---

# medications

```sql
-- -----------------------------------------------------
-- Table `medications`
-- 사용자가 관리하는 약 목록
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medications` (
  `medication_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '약 고유 ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '해당 약을 등록한 사용자 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '약 이름',
  `dosage` SMALLINT UNSIGNED NULL COMMENT '용량 (e.g., 50mg, 1정)',
  `unit` VARCHAR(50) NULL COMMENT '단위 (e.g., mg, 정, ml)',
  `description` TEXT NULL COMMENT '약에 대한 추가 설명/메모',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '현재 복용중인 약 여부 (1: 활성, 0: 비활성)',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '약 소프트삭제 여부 (1: 삭제됨, 0: 활성)',
  `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '삭제 일시 (소프트 삭제)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정 일시',
  PRIMARY KEY (`medication_id`),
  INDEX `fk_medications_users_idx` (`user_id` ASC) VISIBLE,
  INDEX `idx_user_not_deleted` (`user_id` ASC, `is_deleted` ASC) VISIBLE, -- 사용자별 활성 약 조회 성능
  CONSTRAINT `fk_medications_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE -- 사용자가 삭제되면 관련 약 정보도 삭제
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '사용자 등록 약 목록 (소프트 삭제 지원)';
```

# medication_reminders

```sql
-- -----------------------------------------------------
-- Table `medication_reminders`
-- 약 복용 알림 설정
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medication_reminders` (
  `reminder_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '알림 고유 ID',
  `medication_id` BIGINT UNSIGNED NOT NULL COMMENT '알림 대상 약 ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '알림 설정 사용자 ID',
  `reminder_time` TIME NOT NULL COMMENT '알림 시간 (HH:MM:SS)',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '알림 활성화 여부 (1: 활성, 0: 비활성)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '설정 일시',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정 일시',
  PRIMARY KEY (`reminder_id`),
  INDEX `fk_medication_reminders_medications_idx` (`medication_id` ASC) VISIBLE,
  INDEX `fk_medication_reminders_users_idx` (`user_id` ASC) VISIBLE,
  INDEX `idx_reminder_time` (`reminder_time` ASC), -- 알림 시간 기반 조회 성능
  CONSTRAINT `fk_medication_reminders_medications`
    FOREIGN KEY (`medication_id`)
    REFERENCES `medications` (`medication_id`)
    ON DELETE CASCADE -- 약 정보가 삭제되면 관련 알림도 삭제
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_reminders_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE -- 사용자가 삭제되면 관련 알림도 삭제
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '약 복용 알림 설정';
```

# records

```sql
-- -----------------------------------------------------
-- Table `records`
-- 모든 기록의 기본 정보 (마스터 테이블 역할)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `records` (
  `record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '기록 고유 ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '기록 작성 사용자 ID',
  `record_time` DATETIME NOT NULL COMMENT '기록 시점 (사용자가 수정 가능)',
  `record_type` ENUM('DIET', 'SYMPTOM', 'MED_TAKEN', 'SLEEP', 'NOTE') NOT NULL COMMENT '기록 종류',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 생성 일시',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '기록 수정 일시',
  PRIMARY KEY (`record_id`),
  INDEX `fk_records_users_idx` (`user_id` ASC) VISIBLE,
  INDEX `idx_record_time` (`record_time` DESC), -- 시간순 조회를 위한 인덱스
  INDEX `idx_user_record_time` (`user_id` ASC, `record_time` DESC), -- 특정 사용자의 시간순 조회를 위한 복합 인덱스
  CONSTRAINT `fk_records_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`user_id`)
    ON DELETE CASCADE -- 사용자가 삭제되면 관련 기록도 모두 삭제
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '모든 기록의 기본 정보';
```

# diet_records

```sql
-- -----------------------------------------------------
-- Table `diet_records`
-- 식단 기록 상세 정보
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `diet_records` (
  `diet_record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '기본 기록 ID',
  `food_item` TEXT NOT NULL COMMENT '섭취 음식/음료 (텍스트)',
  `photo_url` VARCHAR(1024) NULL COMMENT '음식 사진 URL (향후 기능)',
  `ocr_text` TEXT NULL COMMENT '사진 OCR 결과 텍스트 (향후 기능)',
  PRIMARY KEY (`diet_record_id`),
  UNIQUE INDEX `uq_record_id` (`record_id` ASC) VISIBLE, -- records 테이블과 1:1 관계 보장
  CONSTRAINT `fk_diet_records_records`
    FOREIGN KEY (`record_id`)
    REFERENCES `records` (`record_id`)
    ON DELETE CASCADE -- 기본 기록 삭제 시 상세 정보도 삭제
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '식단 기록 상세';
```

# symptom_records

```sql
-- -----------------------------------------------------
-- Table `symptom_records`
-- 증상 기록 상세 정보
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `symptom_records` (
  `symptom_record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '기본 기록 ID',
  `bowel_movement_type` ENUM('DIARRHEA', 'LOOSE', 'NORMAL', 'HARD', 'CONSTIPATION', 'BLOODY', 'OTHER') NULL COMMENT '변 상태 (선택형)',
  `bowel_movement_custom` VARCHAR(255) NULL COMMENT '변 상태 (직접 입력)',
  `abdominal_pain_present` TINYINT(1) NULL COMMENT '복통 유무 (1: 있음, 0: 없음)',
  `abdominal_pain_scale` TINYINT UNSIGNED NULL COMMENT '복통 강도 (1-5점)',
  `fatigue_scale` TINYINT UNSIGNED NULL COMMENT '피로도 (0: 없음, 1: 약간, 2: 보통, 3: 심함)',
  `other_symptoms` TEXT NULL COMMENT '기타 특이 증상 (텍스트)',
  `overall_condition_scale` TINYINT UNSIGNED NULL COMMENT '전반적 컨디션 (1: 매우 나쁨 - 5: 매우 좋음)',
  `mood_type` ENUM('GOOD', 'NEUTRAL', 'BAD', 'ANXIOUS', 'IRRITABLE', 'OTHER') NULL COMMENT '기분 상태 (선택형)',
  `mood_custom` VARCHAR(255) NULL COMMENT '기분 상태 (직접 입력)',
  PRIMARY KEY (`symptom_record_id`),
  UNIQUE INDEX `uq_record_id` (`record_id` ASC) VISIBLE, -- records 테이블과 1:1 관계 보장
  CONSTRAINT `fk_symptom_records_records`
    FOREIGN KEY (`record_id`)
    REFERENCES `records` (`record_id`)
    ON DELETE CASCADE -- 기본 기록 삭제 시 상세 정보도 삭제
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '증상 기록 상세';
```

# medication_taken_records

```sql
-- -----------------------------------------------------
-- Table `medication_taken_records`
-- 약 복용 실행 기록 상세 정보
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `medication_taken_records` (
  `med_taken_record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '기본 기록 ID',
  `medication_id` BIGINT UNSIGNED NOT NULL COMMENT '복용한 약 ID',
  `taken_from_reminder_id` BIGINT UNSIGNED NULL COMMENT '어떤 알림을 통해 복용했는지 (null 가능)',
  PRIMARY KEY (`med_taken_record_id`),
  UNIQUE INDEX `uq_record_id` (`record_id` ASC) VISIBLE, -- records 테이블과 1:1 관계 보장
  INDEX `fk_medication_taken_records_medications_idx` (`medication_id` ASC) VISIBLE,
  INDEX `fk_medication_taken_records_reminders_idx` (`taken_from_reminder_id` ASC) VISIBLE,
  CONSTRAINT `fk_medication_taken_records_records`
    FOREIGN KEY (`record_id`)
    REFERENCES `records` (`record_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_taken_records_medications`
    FOREIGN KEY (`medication_id`)
    REFERENCES `medications` (`medication_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medication_taken_records_reminders`
    FOREIGN KEY (`taken_from_reminder_id`)
    REFERENCES `medication_reminders` (`reminder_id`)
    ON DELETE SET NULL -- 알림이 삭제되더라도 복용 기록은 유지
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '약 복용 실행 기록 상세';
```

# sleep_records

```sql
-- -----------------------------------------------------
-- Table `sleep_records`
-- 수면 기록 상세 정보
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sleep_records` (
  `sleep_record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '기본 기록 ID',
  `sleep_duration_minutes` INT UNSIGNED NULL COMMENT '수면 시간 (분 단위)',
  `sleep_quality_scale` TINYINT UNSIGNED NULL COMMENT '수면 만족도 (1-5점, 향후 확장)',
  PRIMARY KEY (`sleep_record_id`),
  UNIQUE INDEX `uq_record_id` (`record_id` ASC) VISIBLE, -- records 테이블과 1:1 관계 보장
  CONSTRAINT `fk_sleep_records_records`
    FOREIGN KEY (`record_id`)
    REFERENCES `records` (`record_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '수면 기록 상세';
```

# note_records

```sql
-- -----------------------------------------------------
-- Table `note_records`
-- 기타 메모 기록 상세 정보 (단순 텍스트 메모)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `note_records` (
  `note_record_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '기본 기록 ID',
  `note_content` TEXT NOT NULL COMMENT '메모 내용',
  PRIMARY KEY (`note_record_id`),
  UNIQUE INDEX `uq_record_id` (`record_id` ASC) VISIBLE, -- records 테이블과 1:1 관계 보장
  CONSTRAINT `fk_note_records_records`
    FOREIGN KEY (`record_id`)
    REFERENCES `records` (`record_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = '기타 메모 기록 상세';
```

# etc

```sql
-- -----------------------------------------------------
-- Table `community_posts` (향후 확장용 예시)
-- 커뮤니티 게시글
-- -----------------------------------------------------
-- CREATE TABLE IF NOT EXISTS `community_posts` (
--   `post_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
--   `user_id` BIGINT UNSIGNED NOT NULL,
--   `title` VARCHAR(255) NOT NULL,
--   `content` LONGTEXT NOT NULL,
--   `category` VARCHAR(50) NULL, -- e.g., '식단 공유', '증상 질문'
--   `view_count` INT UNSIGNED NOT NULL DEFAULT 0,
--   `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--   PRIMARY KEY (`post_id`),
--   INDEX `fk_community_posts_users_idx` (`user_id` ASC) VISIBLE,
--   CONSTRAINT `fk_community_posts_users`
--     FOREIGN KEY (`user_id`)
--     REFERENCES `users` (`user_id`)
--     ON DELETE -- 사용자 탈퇴 시 게시글 처리 정책 필요 (e.g., SET NULL, CASCADE, RESTRICT)
--     ON UPDATE CASCADE
-- )
-- ENGINE = InnoDB
-- COMMENT = '커뮤니티 게시글 (향후 기능)';
```