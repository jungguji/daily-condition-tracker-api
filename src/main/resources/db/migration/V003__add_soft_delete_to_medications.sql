-- V003: medications 테이블에 소프트 삭제 컬럼 추가
-- 약 삭제 시 물리적 삭제 대신 논리적 삭제를 지원하기 위함

-- 1. 소프트 삭제 컬럼 추가
ALTER TABLE medications 
ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '약 소프트삭제 여부 (1: 삭제됨, 0: 활성)';

ALTER TABLE medications 
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '삭제 일시 (소프트 삭제)';

-- 2. 사용자별 활성 약 조회 성능을 위한 복합 인덱스 추가
CREATE INDEX idx_user_not_deleted ON medications (user_id ASC, is_deleted ASC);

-- 3. medication_taken_records 테이블의 외래키 제약조건 수정
-- 기존 제약조건 삭제
ALTER TABLE medication_taken_records 
DROP FOREIGN KEY fk_medication_taken_records_medications;

-- 새로운 제약조건 추가 (ON DELETE CASCADE로 변경)
ALTER TABLE medication_taken_records 
ADD CONSTRAINT fk_medication_taken_records_medications
    FOREIGN KEY (medication_id) 
    REFERENCES medications (medication_id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE; 