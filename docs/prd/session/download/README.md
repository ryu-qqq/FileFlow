# External URL Download Bounded Context

**Bounded Context**: `session/download`
**Dependencies**: `session/single` (File Aggregate), `session/multi` (대용량 파일)
**예상 기간**: 3일
**우선순위**: Level 2 (session/single 완료 후)

---

## 📋 개요

**목적**: 외부 URL에서 파일을 다운로드하여 S3에 저장합니다. 클라이언트가 직접 업로드하는 대신, 서버가 외부 URL에서 다운로드하여 S3에 업로드합니다.

**핵심 문제 해결**:
- **클라이언트 부담 감소**: 외부 URL → 클라이언트 → S3 대신, 외부 URL → 서버 → S3로 직접 전송
- **대역폭 최적화**: 서버 간 고속 네트워크 활용
- **보안**: 클라이언트에 외부 URL 노출 방지

**사용 사례**:
- 외부 이미지 URL을 S3로 복사
- 크롤링한 파일을 S3에 저장
- 타 시스템에서 파일 마이그레이션

---

## 🎯 주요 기능

### In Scope
1. **DownloadSession Aggregate** - 다운로드 세션 생명주기 관리
2. **URL 검증** - 허용된 도메인, 파일 크기 제한 검증
3. **비동기 다운로드** - 백그라운드 작업으로 다운로드 처리
4. **진행률 추적** - 다운로드 진행률 실시간 조회 (API)
5. **S3 업로드** - 다운로드 완료 후 S3 업로드

### Out of Scope (Future)
- 외부 URL 인증 (OAuth, API Key)
- 파일 포맷 변환 (다운로드 후 이미지 리사이징 등)
- 다운로드 속도 제한 (Throttling)
- Torrent/P2P 프로토콜

---

## 🏗️ Domain Layer

### Aggregates

#### 1. DownloadSession
**책임**: 외부 URL 다운로드 세션 생명주기 관리

**주요 메서드**:
```java
public class DownloadSession {
    private SessionId sessionId;            // UUID v7
    private TenantId tenantId;
    private ExternalUrl sourceUrl;          // 외부 URL
    private FileName fileName;              // 저장할 파일명
    private FileSize estimatedSize;         // 예상 파일 크기 (Content-Length)
    private MimeType mimeType;
    private DownloadStatus status;          // INITIATED, DOWNLOADING, COMPLETED, FAILED
    private int progressPercentage;         // 0-100
    private String errorMessage;            // 실패 시 에러 메시지
    private LocalDateTime expiresAt;        // 세션 만료 시각 (30분)

    public static DownloadSession initiate(
        SessionId sessionId,
        TenantId tenantId,
        ExternalUrl sourceUrl,
        FileName fileName,
        Clock clock
    );

    public void startDownload(FileSize actualSize, MimeType mimeType, Clock clock);
    public void updateProgress(int percentage, Clock clock);
    public void markAsCompleted(FileId fileId, Clock clock);
    public void markAsFailed(String errorMessage, Clock clock);
    public void ensureNotExpired(Clock clock);
}
```

### Value Objects

#### ExternalUrl
```java
public record ExternalUrl(String value) {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final List<String> ALLOWED_DOMAINS = List.of(
        "example.com",
        "cdn.example.com",
        "images.example.com"
    );

    public ExternalUrl {
        validateUrl(value);
        validateScheme(value);
        validateDomain(value);
    }

    private void validateUrl(String value) {
        try {
            new URL(value);
        } catch (MalformedURLException e) {
            throw new InvalidExternalUrlException(value, "잘못된 URL 형식입니다.");
        }
    }

    private void validateScheme(String value) {
        URL url = parseUrl(value);
        if (!ALLOWED_SCHEMES.contains(url.getProtocol())) {
            throw new InvalidExternalUrlException(value, "허용되지 않은 프로토콜입니다.");
        }
    }

    private void validateDomain(String value) {
        URL url = parseUrl(value);
        boolean allowed = ALLOWED_DOMAINS.stream()
            .anyMatch(domain -> url.getHost().equals(domain) || url.getHost().endsWith("." + domain));

        if (!allowed) {
            throw new InvalidExternalUrlException(value, "허용되지 않은 도메인입니다.");
        }
    }

    public static ExternalUrl of(String value) {
        return new ExternalUrl(value);
    }
}
```

### Enums

#### DownloadStatus
- `INITIATED`: 다운로드 요청 접수
- `DOWNLOADING`: 다운로드 진행 중
- `COMPLETED`: 다운로드 완료 및 S3 업로드 완료
- `FAILED`: 다운로드 실패

---

## 📦 Application Layer

### Use Cases

#### 1. InitiateDownloadUseCase (Command)
**책임**: 다운로드 세션 시작 및 백그라운드 작업 예약

```java
@Component
public class InitiateDownloadFacade implements InitiateDownloadUseCase {

    @Override
    public DownloadSessionResponse execute(InitiateDownloadCommand cmd) {
        // 1. ExternalUrl 검증 (도메인, 스킴)
        ExternalUrl externalUrl = ExternalUrl.of(cmd.sourceUrl());

        // 2. 트랜잭션: DownloadSession 생성
        DownloadSession session = downloadSessionManager.initiateSession(
            cmd.sessionId(),
            cmd.tenantId(),
            externalUrl,
            cmd.fileName()
        );

        // 3. 트랜잭션 밖: 백그라운드 다운로드 작업 예약
        downloadJobScheduler.scheduleDownload(session.sessionId());

        return DownloadSessionResponse.from(session);
    }
}
```

#### 2. ExecuteDownloadJob (Background Worker)
**책임**: 실제 다운로드 및 S3 업로드 수행

```java
@Component
public class DownloadJobExecutor {

    @Async
    public void executeDownload(SessionId sessionId) {
        try {
            // 1. 트랜잭션: DownloadSession 조회
            DownloadSession session = downloadSessionQueryPort.findBySessionId(sessionId);

            // 2. 트랜잭션 밖: HTTP HEAD 요청으로 파일 정보 조회
            FileMetadata metadata = httpClient.fetchMetadata(session.sourceUrl());

            // 3. 파일 크기 검증 (1GB 제한)
            if (metadata.fileSize().bytes() > 1_073_741_824L) {
                throw new FileSizeExceededException(metadata.fileSize());
            }

            // 4. 트랜잭션: 다운로드 시작 상태로 변경
            downloadSessionManager.startDownload(
                sessionId,
                metadata.fileSize(),
                metadata.mimeType()
            );

            // 5. 트랜잭션 밖: 외부 URL에서 다운로드 (스트리밍)
            InputStream inputStream = httpClient.download(session.sourceUrl(),
                progress -> updateProgress(sessionId, progress));

            // 6. 트랜잭션 밖: S3 업로드
            S3Key s3Key = s3KeyGenerator.generate(...);
            s3ClientPort.uploadStream(s3Bucket, s3Key, inputStream, metadata.mimeType());

            // 7. 트랜잭션: File Aggregate 생성 + DownloadSession 완료
            File file = downloadSessionManager.completeDownload(
                sessionId,
                s3Key,
                metadata
            );

        } catch (Exception e) {
            // 8. 트랜잭션: 실패 처리
            downloadSessionManager.markAsFailed(sessionId, e.getMessage());
        }
    }

    private void updateProgress(SessionId sessionId, int percentage) {
        downloadSessionManager.updateProgress(sessionId, percentage);
    }
}
```

#### 3. GetDownloadProgressUseCase (Query)
**책임**: 다운로드 진행률 조회

```java
@Component
public class GetDownloadProgressService implements GetDownloadProgressUseCase {

    @Override
    public DownloadProgressResponse execute(GetDownloadProgressQuery query) {
        DownloadSession session = downloadSessionQueryPort.findBySessionId(query.sessionId());
        session.ensureNotExpired(clock);

        return DownloadProgressResponse.from(session);
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V6__create_download_sessions_table.sql
```sql
CREATE TABLE download_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    source_url TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    estimated_size BIGINT,
    mime_type VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    progress_percentage INT NOT NULL DEFAULT 0,
    error_message TEXT,
    file_id VARCHAR(36),
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| POST | /api/v1/files/download/initiate | 다운로드 세션 시작 | 202 Accepted |
| GET | /api/v1/files/download/progress | 다운로드 진행률 조회 | 200 OK |
| DELETE | /api/v1/files/download/cancel | 다운로드 취소 | 204 No Content |

### Request Example

**POST /api/v1/files/download/initiate**:
```json
{
  "sessionId": "01JDA000-1234-5678-9abc-def012345678",
  "sourceUrl": "https://cdn.example.com/images/sample.jpg",
  "fileName": "다운로드된파일.jpg"
}
```

### Response Example

**POST /api/v1/files/download/initiate (202 Accepted)**:
```json
{
  "sessionId": "01JDA000-1234-5678-9abc-def012345678",
  "status": "INITIATED",
  "estimatedCompletionTime": "2025-11-18T10:35:00Z",
  "message": "다운로드가 예약되었습니다. 진행률 조회 API를 통해 상태를 확인하세요."
}
```

**GET /api/v1/files/download/progress (200 OK)**:
```json
{
  "sessionId": "01JDA000-1234-5678-9abc-def012345678",
  "status": "DOWNLOADING",
  "progressPercentage": 45,
  "downloadedBytes": 471859200,
  "totalBytes": 1048576000,
  "estimatedTimeRemaining": "00:02:30"
}
```

**GET /api/v1/files/download/progress - Completed (200 OK)**:
```json
{
  "sessionId": "01JDA000-1234-5678-9abc-def012345678",
  "status": "COMPLETED",
  "progressPercentage": 100,
  "fileId": "01JDA001-1234-5678-9abc-def012345678",
  "s3Key": "uploads/1/admin/connectly/download/01JDA001_다운로드된파일.jpg",
  "completedAt": "2025-11-18T10:33:45Z"
}
```

---

## 📊 Integration Points

### session/single 재사용
- `File` Aggregate 생성 로직 재사용
- `S3ClientPort` 확장 (InputStream 기반 업로드 추가)

### session/multi 연동
- 100MB 이상 파일은 Multipart Upload 사용
- `FileSize` 기반 자동 라우팅

### 차이점
| 항목 | session/single | session/download |
|------|----------------|------------------|
| 업로드 주체 | 클라이언트 | 서버 |
| 소스 | 클라이언트 로컬 파일 | 외부 URL |
| 처리 방식 | 동기 (Presigned URL) | 비동기 (백그라운드 작업) |
| 진행률 | 없음 | 실시간 추적 |
| 응답 | 201 Created | 202 Accepted |

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] ExternalUrl 도메인 화이트리스트 검증
- [ ] 백그라운드 다운로드 작업 (@Async)
- [ ] 진행률 실시간 추적 (0-100%)
- [ ] 다운로드 완료 후 S3 업로드
- [ ] 100MB 이상 파일은 Multipart Upload 자동 사용
- [ ] 다운로드 취소 기능

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (WireMock + LocalStack S3)
- [ ] ArchUnit Test 통과

### 성능 요구사항
- [ ] 10MB 파일 다운로드 + S3 업로드 < 30초 (P95)
- [ ] 동시 다운로드 최대 10개 (Thread Pool 제한)

### 보안 요구사항
- [ ] 허용된 도메인만 다운로드 가능
- [ ] HTTPS만 허용 (HTTP 차단)
- [ ] SSRF (Server-Side Request Forgery) 방지

---

## 🔗 의존성

### Upstream
- `session/single` - File Aggregate 생성
- `session/multi` - 대용량 파일 처리

### Downstream
- HTTP Client (외부 URL)
- S3 Upload API

---

## 🚨 보안 고려사항

### SSRF 방지
```java
private static final List<String> BLOCKED_PRIVATE_RANGES = List.of(
    "127.0.0.0/8",      // Loopback
    "10.0.0.0/8",       // Private
    "172.16.0.0/12",    // Private
    "192.168.0.0/16",   // Private
    "169.254.0.0/16"    // Link-local
);

private void validateNotPrivateIp(String url) {
    InetAddress address = InetAddress.getByName(new URL(url).getHost());
    if (isPrivateIp(address)) {
        throw new InvalidExternalUrlException(url, "내부 IP 주소는 허용되지 않습니다.");
    }
}
```

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/download Bounded Context)
