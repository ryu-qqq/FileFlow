# Security Visibility Bounded Context

**Bounded Context**: `security/visibility`
**Dependencies**: `session/single` (File Aggregate)
**예상 기간**: 2일
**우선순위**: Level 4 (Level 3 완료 후)

---

## 📋 개요

**목적**: 파일 접근 제어 및 다운로드 권한 관리를 제공합니다.

**핵심 문제 해결**:
- **보안**: 권한 없는 사용자의 파일 접근 차단
- **규정 준수**: GDPR, CCPA 개인정보 접근 제어
- **비즈니스 정책**: Tenant 간 파일 격리

**접근 제어 정책**:
- **Customer 파일**: 본인만 접근 가능
- **Seller 상품 이미지**: 같은 Tenant 내 접근 가능
- **Admin 배너**: 모든 사용자 접근 가능 (Public)

---

## 🎯 주요 기능

### In Scope
1. **FileAccessControl Aggregate** - 파일 접근 권한 관리
2. **Presigned Download URL** - 임시 다운로드 링크 발급 (1시간)
3. **접근 로그** - 파일 다운로드 이력 추적
4. **권한 검증** - Tenant/Uploader 기반 접근 제어

### Out of Scope (Future)
- 세밀한 권한 관리 (Role-Based Access Control)
- 파일 공유 링크 (Share Link)
- 만료 가능한 다운로드 링크

---

## 🏗️ Domain Layer

### Aggregates

#### 1. FileAccessControl
**책임**: 파일 접근 권한 검증

**주요 메서드**:
```java
public class FileAccessControl {
    private FileId fileId;
    private TenantId tenantId;
    private UploaderId uploaderId;
    private UploaderType uploaderType;
    private AccessPolicy accessPolicy;

    public static FileAccessControl from(File file);

    public void ensureCanAccess(UserContext userContext);
    public boolean canAccess(UserContext userContext);
}
```

### Value Objects

#### AccessPolicy
```java
public record AccessPolicy(AccessLevel level) {
    public enum AccessLevel {
        OWNER_ONLY,      // Customer: 본인만
        TENANT_ONLY,     // Seller: 같은 Tenant
        PUBLIC           // Admin: 모두
    }

    public static AccessPolicy from(UploaderType uploaderType) {
        return switch (uploaderType) {
            case CUSTOMER -> new AccessPolicy(AccessLevel.OWNER_ONLY);
            case SELLER -> new AccessPolicy(AccessLevel.TENANT_ONLY);
            case ADMIN -> new AccessPolicy(AccessLevel.PUBLIC);
        };
    }
}
```

---

## 📦 Application Layer

### Use Cases

#### 1. GenerateDownloadUrlUseCase (Query)
**책임**: 권한 검증 후 다운로드 URL 발급

```java
@Component
public class GenerateDownloadUrlService implements GenerateDownloadUrlUseCase {

    @Override
    public DownloadUrlResponse execute(GenerateDownloadUrlQuery query) {
        // 1. 파일 조회
        File file = fileQueryPort.findById(query.fileId());

        // 2. 접근 권한 검증
        FileAccessControl accessControl = FileAccessControl.from(file);
        UserContext userContext = extractUserContext();
        accessControl.ensureCanAccess(userContext);

        // 3. S3 Presigned Download URL 발급 (1시간)
        PresignedUrl downloadUrl = s3ClientPort.generatePresignedGetUrl(
            file.s3Bucket(),
            file.s3Key(),
            Duration.ofHours(1)
        );

        // 4. 접근 로그 기록
        fileAccessLogPersistencePort.save(FileAccessLog.create(
            file.fileId(),
            userContext.userId(),
            AccessType.DOWNLOAD,
            clock
        ));

        return DownloadUrlResponse.from(downloadUrl);
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V12__create_file_access_logs_table.sql
```sql
CREATE TABLE file_access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    access_type VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    INDEX idx_file_id (file_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| GET | /api/v1/files/{fileId}/download-url | 다운로드 URL 발급 | 200 OK |
| GET | /api/v1/files/{fileId}/access-logs | 접근 로그 조회 | 200 OK |

### Response Example

**GET /api/v1/files/{fileId}/download-url (200 OK)**:
```json
{
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "downloadUrl": "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/...",
  "expiresIn": 3600,
  "expiresAt": "2025-11-18T11:30:00Z"
}
```

**403 Forbidden (권한 없음)**:
```json
{
  "code": "ACCESS_DENIED",
  "message": "해당 파일에 대한 접근 권한이 없습니다.",
  "timestamp": "2025-11-18T10:30:00Z"
}
```

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] UploaderType별 접근 정책 (OWNER_ONLY, TENANT_ONLY, PUBLIC)
- [ ] 권한 검증 (Tenant/Uploader 기반)
- [ ] Presigned Download URL 발급 (1시간)
- [ ] 접근 로그 기록

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test

### 보안 요구사항
- [ ] 권한 없는 사용자 403 Forbidden
- [ ] Presigned URL 만료 시 401 Unauthorized

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (security/visibility Bounded Context)
