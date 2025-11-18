# Multipart Upload Bounded Context

**Bounded Context**: `session/multi`
**Dependencies**: `session/single` (UploadSession, File Aggregates)
**예상 기간**: 4일
**우선순위**: Level 2 (session/single 완료 후)

---

## 📋 개요

**목적**: 대용량 파일(100MB 이상)을 Multipart Upload로 처리하여 안정적이고 효율적인 업로드를 제공합니다.

**핵심 문제 해결**:
- **네트워크 안정성**: 대용량 파일 전송 중 네트워크 오류 시 전체 재전송 방지
- **병렬 업로드**: 파트별 병렬 업로드로 전송 속도 향상
- **재개 가능성**: 실패한 파트만 재전송 가능

**S3 Multipart Upload 제약사항**:
- 최소 파일 크기: 5MB (마지막 파트 제외)
- 최대 파트 수: 10,000개
- 파트 크기: 5MB ~ 5GB
- 최대 파일 크기: 5TB

---

## 🎯 주요 기능

### In Scope
1. **Multipart Upload Session** - 멀티파트 업로드 세션 관리
2. **Part Upload Tracking** - 파트별 업로드 상태 추적
3. **Upload Completion** - 모든 파트 업로드 완료 후 S3 Complete 호출
4. **Upload Abort** - 실패 시 업로드 취소 및 임시 파일 삭제
5. **Progress Tracking** - 업로드 진행률 실시간 추적 (API)

### Out of Scope (Future)
- Resumable Upload (중단된 업로드 재개)
- Parallel Part Upload (클라이언트 병렬 업로드)
- Upload Speed Throttling (속도 제한)

---

## 🏗️ Domain Layer

### Aggregates

#### 1. MultipartUploadSession (extends UploadSession)
**책임**: 멀티파트 업로드 세션 생명주기 관리

**주요 메서드**:
```java
public class MultipartUploadSession extends UploadSession {
    private S3UploadId s3UploadId;          // S3 Multipart Upload ID
    private int totalParts;                 // 총 파트 수
    private int completedParts;             // 완료된 파트 수
    private MultipartStatus status;         // IN_PROGRESS, COMPLETED, ABORTED

    public static MultipartUploadSession initiate(
        SessionId sessionId,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3UploadId s3UploadId,
        int totalParts,
        Clock clock
    );

    public void markPartCompleted(int partNumber, ETag eTag);
    public void ensureAllPartsCompleted();
    public void abort(Clock clock);
    public int calculateProgressPercentage();
}
```

#### 2. UploadPart
**책임**: 개별 파트 업로드 상태 관리

**주요 메서드**:
```java
public class UploadPart {
    private PartId partId;                  // UUID v7
    private SessionId sessionId;            // MultipartUploadSession 참조
    private int partNumber;                 // 파트 번호 (1-based)
    private PartSize partSize;              // 파트 크기
    private ETag eTag;                      // S3 ETag (완료 시)
    private PartStatus status;              // PENDING, COMPLETED, FAILED
    private PresignedUrl presignedUrl;      // 파트별 Presigned URL

    public static UploadPart create(
        SessionId sessionId,
        int partNumber,
        PartSize partSize,
        PresignedUrl presignedUrl,
        Clock clock
    );

    public void markAsCompleted(ETag eTag, Clock clock);
    public void markAsFailed(Clock clock);
}
```

### Value Objects

#### S3UploadId
```java
public record S3UploadId(String value) {
    public S3UploadId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("S3UploadId는 null이거나 빈 값일 수 없습니다.");
        }
    }

    public static S3UploadId of(String value) {
        return new S3UploadId(value);
    }
}
```

#### ETag
```java
public record ETag(String value) {
    public ETag {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ETag는 null이거나 빈 값일 수 없습니다.");
        }
    }

    public static ETag of(String value) {
        return new ETag(value);
    }
}
```

### Enums

#### MultipartStatus
- `IN_PROGRESS`: 업로드 진행 중
- `COMPLETED`: 모든 파트 업로드 완료
- `ABORTED`: 업로드 취소됨

#### PartStatus
- `PENDING`: 대기 중
- `COMPLETED`: 완료
- `FAILED`: 실패

---

## 📦 Application Layer

### Use Cases

#### 1. InitiateMultipartUploadUseCase (Command)
**책임**: 멀티파트 업로드 세션 시작

**Orchestration Pattern**:
```java
@Component
public class InitiateMultipartUploadFacade implements InitiateMultipartUploadUseCase {

    @Override
    public MultipartUploadResponse execute(InitiateMultipartUploadCommand cmd) {
        // 1. 트랜잭션: 세션 생성
        MultipartSessionResult result = multipartSessionManager.initiateSession(cmd);

        // 2. 트랜잭션 밖: S3 Multipart Upload 시작
        S3UploadId s3UploadId = s3ClientPort.initiateMultipartUpload(
            result.s3Bucket(),
            result.s3Key(),
            cmd.mimeType()
        );

        // 3. 트랜잭션: 세션 업데이트 + 파트 생성
        MultipartUploadSession session = multipartSessionManager.completeInitiation(
            result.sessionId(),
            s3UploadId,
            calculateTotalParts(cmd.fileSize())
        );

        // 4. 트랜잭션 밖: 각 파트별 Presigned URL 발급
        List<PartPresignedUrl> partUrls = generatePartPresignedUrls(
            result.s3Bucket(),
            result.s3Key(),
            s3UploadId,
            session.totalParts()
        );

        return MultipartUploadResponse.from(session, partUrls);
    }

    private int calculateTotalParts(FileSize fileSize) {
        long partSize = 5 * 1024 * 1024; // 5MB
        return (int) Math.ceil((double) fileSize.bytes() / partSize);
    }
}
```

#### 2. CompletePartUploadUseCase (Command)
**책임**: 개별 파트 업로드 완료 처리

```java
@Transactional
public PartUploadResponse completePartUpload(CompletePartUploadCommand cmd) {
    // 1. UploadPart 조회
    UploadPart part = uploadPartQueryPort.findBySessionIdAndPartNumber(
        cmd.sessionId(),
        cmd.partNumber()
    );

    // 2. 파트 완료 처리
    part.markAsCompleted(cmd.eTag(), clock);
    uploadPartPersistencePort.update(part);

    // 3. MultipartUploadSession 진행률 업데이트
    MultipartUploadSession session = multipartSessionQueryPort.findBySessionId(cmd.sessionId());
    session.markPartCompleted(cmd.partNumber(), cmd.eTag());
    multipartSessionPersistencePort.update(session);

    return PartUploadResponse.from(part, session.calculateProgressPercentage());
}
```

#### 3. CompleteMultipartUploadUseCase (Command)
**책임**: 모든 파트 업로드 완료 후 S3 Complete 호출

**Orchestration Pattern**:
```java
@Override
public FileResponse execute(CompleteMultipartUploadCommand cmd) {
    // 1. 트랜잭션: 세션 및 파트 조회
    CompletionPreparationResult result = multipartSessionManager.prepareCompletion(cmd.sessionId());

    // 2. 트랜잭션 밖: S3 Complete Multipart Upload
    s3ClientPort.completeMultipartUpload(
        result.s3Bucket(),
        result.s3Key(),
        result.s3UploadId(),
        result.partETags()
    );

    // 3. 트랜잭션: File Aggregate 생성 + 세션 완료
    File file = multipartSessionManager.finalizeUpload(
        cmd.sessionId(),
        result.fileMetadata()
    );

    return FileResponse.from(file);
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V4__create_multipart_upload_sessions_table.sql
```sql
CREATE TABLE multipart_upload_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    s3_upload_id VARCHAR(255) NOT NULL,
    total_parts INT NOT NULL,
    completed_parts INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    FOREIGN KEY (session_id) REFERENCES upload_sessions(session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### V5__create_upload_parts_table.sql
```sql
CREATE TABLE upload_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id VARCHAR(36) NOT NULL UNIQUE,
    session_id VARCHAR(36) NOT NULL,
    part_number INT NOT NULL,
    part_size BIGINT NOT NULL,
    etag VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    presigned_url TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_part (session_id, part_number),
    INDEX idx_status (status),
    UNIQUE KEY uk_session_part (session_id, part_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| POST | /api/v1/files/multipart/initiate | 멀티파트 업로드 시작 | 201 Created |
| PUT | /api/v1/files/multipart/parts/{partNumber} | 파트 업로드 완료 | 200 OK |
| POST | /api/v1/files/multipart/complete | 멀티파트 업로드 완료 | 200 OK |
| DELETE | /api/v1/files/multipart/abort | 멀티파트 업로드 취소 | 204 No Content |
| GET | /api/v1/files/multipart/progress | 업로드 진행률 조회 | 200 OK |

### Response Example

**POST /api/v1/files/multipart/initiate**:
```json
{
  "sessionId": "01JD9000-1234-5678-9abc-def012345678",
  "s3UploadId": "exampleUploadId",
  "totalParts": 20,
  "parts": [
    {
      "partNumber": 1,
      "presignedUrl": "https://s3.amazonaws.com/...",
      "expiresIn": 300
    },
    {
      "partNumber": 2,
      "presignedUrl": "https://s3.amazonaws.com/...",
      "expiresIn": 300
    }
  ]
}
```

---

## 📊 Integration Points

### session/single 재사용
- `UploadSession` Aggregate 기반 클래스로 활용
- `SessionManager` 트랜잭션 패턴 재사용
- `S3ClientPort` 확장 (Multipart Upload API 추가)

### 차이점
| 항목 | session/single | session/multi |
|------|----------------|---------------|
| 파일 크기 | < 100MB | >= 100MB |
| Presigned URL | 1개 | N개 (파트 수) |
| S3 API | PutObject | InitiateMultipartUpload → UploadPart → CompleteMultipartUpload |
| 진행률 추적 | 없음 | 파트별 진행률 |
| 재시도 | 전체 재업로드 | 실패 파트만 재업로드 |

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] Multipart Upload 세션 시작 (S3 Initiate)
- [ ] 파트별 Presigned URL 발급 (최대 10,000개)
- [ ] 파트 업로드 완료 처리 (ETag 저장)
- [ ] 모든 파트 완료 후 S3 Complete 호출
- [ ] 업로드 취소 (S3 Abort + 임시 파일 삭제)
- [ ] 진행률 조회 API (completedParts / totalParts)

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (TestContainers + LocalStack S3)
- [ ] ArchUnit Test 통과

### 성능 요구사항
- [ ] 100MB 파일 업로드 완료 < 60초 (P95, 10Mbps 기준)
- [ ] 파트별 Presigned URL 발급 < 500ms (100개 파트 기준)

---

## 🔗 의존성

### Upstream
- `session/single` - UploadSession, SessionManager 재사용

### Downstream
- S3 Multipart Upload API

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/multi Bounded Context)
