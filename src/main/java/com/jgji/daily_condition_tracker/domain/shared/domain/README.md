# Base Entity 계층 구조

## 개요
엔티티의 필요에 따라 적절한 Base 클래스를 선택하여 상속받습니다.

## 계층 구조

```
BaseEntity (기본 시간 정보)
    ↓
SoftDeletableEntity (소프트 삭제 기능 추가)
```

## 사용 가이드

### 1. BaseEntity
**사용 대상**: 대부분의 기본 엔티티
**포함 필드**: `created_at`, `updated_at`
**사용 예시**:
- 로그 테이블
- 설정 테이블  
- 단순 매핑 테이블
- 게시글, 댓글 등

### 2. SoftDeletableEntity
**사용 대상**: 소프트 삭제가 필요한 중요한 비즈니스 엔티티
**포함 필드**: BaseEntity + `deleted_at`
**사용 예시**:
- 사용자 정보
- 주문 정보
- 결제 정보

```java
@Entity
public class User extends SoftDeletableEntity {
    // 사용자는 완전 삭제하지 않고 비활성화만 함
}
```

## 선택 기준

| 요구사항 | 선택할 Base 클래스 |
|---------|------------------|
| 기본 시간 정보만 | BaseEntity |
| + 소프트 삭제 | SoftDeletableEntity |