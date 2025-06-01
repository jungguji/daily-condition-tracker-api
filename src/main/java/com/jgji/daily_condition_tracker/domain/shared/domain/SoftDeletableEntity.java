package com.jgji.daily_condition_tracker.domain.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * 소프트 삭제가 필요한 엔티티
 * 사용자 데이터, 중요한 비즈니스 데이터에서 사용
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // 소프트 삭제 메서드
    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 삭제 복구
    public void restore() {
        this.deletedAt = null;
    }
} 