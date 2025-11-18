# File Retention Bounded Context

**Bounded Context**: `file/retention`
**Dependencies**: `session/single` (File Aggregate)
**예상 기간**: 1일
**우선순위**: Level 4 (Level 3 완료 후)

---

## 📋 개요

**목적**: 파일 보관 정책(Retention Policy)에 따라 자동으로 파일을 삭제하거나 아카이빙합니다.

**핵심 문제 해결**:
- **스토리지 비용**: 불필요한 오래된 파일 자동 삭제
- **규정 준수**: 개인정보 보관 기간 준수 (GDPR, CCPA)
- **비즈니스 정책**: UploaderType별 보관 정책 차별화

**보관 정책 예시**:
- **Customer 파일**: 90일 후 자동 삭제
- **Seller 상품 이미지**: 1년 보관 후 Glacier 이동
- **Admin 배너**: 무제한 보관

---

## 🎯 주요 기능

### In Scope
1. **RetentionPolicy Aggregate** - 파일 보관 정책 관리
2. **UploaderType별 정책** - ADMIN, SELLER, CUSTOMER 차별화
3. **자동 만료 처리** - 보관 기간 경과 파일 삭제
4. **스케줄러** - 매일 자정 실행

### Out of Scope (Future)
- S3 Glacier 아카이빙
- 파일 복원 (Restore)
- 보관 정책 변경 이력

---

## 🏗️ Domain Layer

### Aggregates

#### 1. RetentionPolicy
**책임**: 파일 보관 정책 관리

**주요 메서드**:
```java
public class RetentionPolicy {
    private PolicyId policyId;
    private UploaderType uploaderType;
    private FileCategory category;              // null이면 전체 카테고리 적용
    private RetentionPeriod retentionPeriod;    // 보관 기간
    private RetentionAction action;             // DELETE, ARCHIVE

    public static RetentionPolicy create(
        UploaderType uploaderType,
        FileCategory category,
        RetentionPeriod retentionPeriod,
        RetentionAction action
    );

    public boolean isExpired(File file, Clock clock);
    public LocalDateTime calculateExpirationDate(LocalDateTime uploadedAt);
}
```

### Value Objects

#### RetentionPeriod
```java
public record RetentionPeriod(int days) {
    public static final RetentionPeriod FOREVER = new RetentionPeriod(-1);
    public static final RetentionPeriod NINETY_DAYS = new RetentionPeriod(90);
    public static final RetentionPeriod ONE_YEAR = new RetentionPeriod(365);

    public RetentionPeriod {
        if (days < -1 || days == 0) {
            throw new IllegalArgumentException("보관 기간은 -1(무제한) 또는 양수여야 합니다.");
        }
    }

    public boolean isForever() {
        return days == -1;
    }

    public static RetentionPeriod ofDays(int days) {
        return new RetentionPeriod(days);
    }
}
```

### Enums

#### RetentionAction
- `DELETE`: 삭제
- `ARCHIVE`: 아카이빙 (Glacier 이동, Future)

---

## 📦 Application Layer

### Use Cases

#### 1. ExpireFilesUseCase (Scheduler)
**책임**: 보관 기간 경과 파일 삭제

```java
@Component
public class FileRetentionScheduler {

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void expireFiles() {
        // 1. 모든 보관 정책 조회
        List<RetentionPolicy> policies = retentionPolicyQueryPort.findAll();

        for (RetentionPolicy policy : policies) {
            // 2. 정책별 만료된 파일 조회
            LocalDateTime threshold = LocalDateTime.now(clock)
                .minusDays(policy.retentionPeriod().days());

            List<File> expiredFiles = fileQueryPort.findExpiredFiles(
                policy.uploaderType(),
                policy.category(),
                threshold
            );

            for (File file : expiredFiles) {
                // 3. S3에서 파일 삭제
                s3ClientPort.deleteObject(file.s3Bucket(), file.s3Key());

                // 4. DB에서 파일 삭제
                filePersistencePort.delete(file.fileId());
            }
        }
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V10__create_retention_policies_table.sql
```sql
CREATE TABLE retention_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id VARCHAR(36) NOT NULL UNIQUE,
    uploader_type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    retention_days INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    UNIQUE KEY uk_uploader_category (uploader_type, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 초기 데이터 (V11__insert_default_retention_policies.sql)
```sql
INSERT INTO retention_policies (policy_id, uploader_type, category, retention_days, action, created_at, updated_at)
VALUES
    (UUID(), 'CUSTOMER', NULL, 90, 'DELETE', NOW(), NOW()),
    (UUID(), 'SELLER', NULL, 365, 'DELETE', NOW(), NOW()),
    (UUID(), 'ADMIN', NULL, -1, 'DELETE', NOW(), NOW());
```

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] UploaderType별 보관 정책 설정
- [ ] 보관 기간 경과 파일 자동 삭제
- [ ] 스케줄러 (매일 자정)

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (file/retention Bounded Context)
