# PRD: File Upload Presigned URL System

**작성일**: 2025-01-18
**작성자**: sangwon-ryu
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
S3 직접 업로드를 위한 Presigned URL 생성 및 업로드 세션 관리 시스템 구축

**핵심 가치**:
- 서버 부하 감소: 파일 업로드를 S3로 직접 처리하여 서버 리소스 절약
- 확장성: 대용량 파일(최대 1GB) 업로드 지원
- 안정성: 세션 기반 멱등성 보장 및 다중 완료 처리 메커니즘

### 주요 사용자
- **ADMIN**: 시스템 관리자 (connectly 네임스페이스 사용)
- **SELLER**: 판매자 (setof/{sellerName} 네임스페이스 사용)
- **DEFAULT**: 일반 사용자 (setof/default 네임스페이스 사용)

### 성공 기준
1. Presigned URL 생성 성공률 > 99.9%
2. 세션 기반 멱등성 100% 보장
3. 업로드 완료 처리 정확도 100% (중복 방지)
4. Redis TTL 리스너 + 스케줄러 조합으로 세션 만료 누락 0건

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### 1.1 Aggregate: UploadSession

**속성**:
- `sessionId`: String (UUID v4, 클라이언트 제공)
- `userId`: Long (JWT에서 추출)
- `tenantId`: Long (JWT에서 추출, 기본값 1)
- `role`: UserRole (Enum: ADMIN, SELLER, DEFAULT)
- `sellerName`: String (SELLER인 경우만, Nullable)
- `uploadType`: UploadType (Enum: SINGLE, MULTIPART)
- `customPath`: String (클라이언트 제공 경로, 예: "uploads/documents")
- `fileName`: String (원본 파일명)
- `fileSize`: Long (바이트 단위)
- `mimeType`: String (예: "image/jpeg", "text/html")
- `status`: SessionStatus (Enum: PREPARING, ACTIVE, COMPLETED, EXPIRED, FAILED)
- `createdAt`: LocalDateTime
- `expiresAt`: LocalDateTime (생성 시각 + 15분)
- `completedAt`: LocalDateTime (Nullable)

**비즈니스 규칙** (⭐ 구체화):

1. **세션 생성 (Prepare Upload)**:
   - ❓ **멱등성 보장**: 동일 `sessionId`로 재요청 시 기존 세션 반환
   - ❓ **파일 크기 검증**:
     - SINGLE: 최대 100MB
     - MULTIPART: 최대 1GB
     - 크기 초과 시 `FileSizeExceededException` 발생
   - ❓ **파일 타입 검증**:
     - 허용: `image/*`, `text/html`
     - 거부 시 `UnsupportedFileTypeException` 발생
   - ❓ **세션 유효시간**: 15분 (Redis TTL)
   - ❓ **S3 경로 생성 규칙**:
     ```
     {tenantId}/{namespace}/{customPath}/{fileId}.{ext}

     - ADMIN: 1/connectly/{customPath}/{fileId}.{ext}
     - SELLER: 1/setof/{sellerName}/{customPath}/{fileId}.{ext}
     - DEFAULT: 1/setof/default/{customPath}/{fileId}.{ext}
     ```
   - ❓ **확장자 추출**: MIME 타입에서 추출 (예: `image/jpeg` → `.jpg`)

2. **Presigned URL 생성**:
   - ❓ **단일 업로드**: 1개의 PUT URL 생성
   - ❓ **멀티파트 업로드**:
     - Initiate Multipart Upload
     - Part 크기: 5MB 고정
     - 최대 Part 개수: 10개 (= 50MB)
     - Part 업로드 실패 시: 클라이언트가 재시도 (에러 반환)
   - ❓ **URL 만료시간**: 15분 (세션과 동일)

3. **세션 만료 (Expire Session)**:
   - ❓ **만료 조건**: 생성 후 15분 경과
   - ❓ **만료 처리**:
     - 상태 변경: ACTIVE → EXPIRED
     - 멀티파트인 경우: S3 Abort Multipart Upload 호출
   - ❓ **만료 감지**:
     - Primary: Redis Keyspace Notification (TTL 만료 이벤트)
     - Fallback: 스케줄러 (5분마다 실행, `expiresAt < now()` 조회)

4. **업로드 완료 (Complete Upload)**:
   - ❓ **완료 트리거** (2가지):
     1. 클라이언트 명시적 호출: `POST /api/v1/upload-sessions/{sessionId}/complete`
     2. S3 Event Notification: S3 → SQS → Lambda/Spring (미정)
   - ❓ **중복 처리 방지**:
     - Optimistic Lock (`@Version`)
     - 상태 전환: ACTIVE → COMPLETED (한 번만 가능)
   - ❓ **완료 처리 로직**:
     1. 세션 상태 검증 (ACTIVE만 완료 가능)
     2. 멀티파트인 경우: S3 Complete Multipart Upload 호출
     3. File 엔티티 생성 (RDB 저장)
     4. 세션 상태 변경 (ACTIVE → COMPLETED)
     5. Redis 세션 삭제

5. **동시성 제어**:
   - ❓ **동일 sessionId 동시 요청**: 첫 요청만 처리, 나머지는 기존 세션 반환
   - ❓ **멀티파트 Part 동시 업로드**: 허용 (S3가 처리)
   - ❓ **완료 처리 동시 요청**: Optimistic Lock으로 한 번만 처리

**상태 전환 다이어그램**:
```
PREPARING → ACTIVE → COMPLETED
                ↓
           EXPIRED / FAILED
```

**Value Objects**:
- **UploadType**: Enum (SINGLE, MULTIPART)
- **SessionStatus**: Enum (PREPARING, ACTIVE, COMPLETED, EXPIRED, FAILED)
- **UserRole**: Enum (ADMIN, SELLER, DEFAULT)
- **S3Path**: Value Object (tenantId, namespace, customPath, fileId, extension 조합)

**Domain Events** (선택적):
- `UploadSessionCreated`: 세션 생성 시
- `UploadSessionExpired`: 세션 만료 시
- `UploadCompleted`: 업로드 완료 시

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter (Getter 체이닝 금지)
  - `session.getS3Path()` (O)
  - `session.getUser().getTenantId()` (X, JWT에서 직접 추출)
- ✅ Lombok 금지 (Pure Java 또는 Record 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

---

#### 1.2 Aggregate: File

**속성**:
- `fileId`: String (UUID v4)
- `userId`: Long (업로드한 사용자)
- `tenantId`: Long
- `role`: UserRole
- `fileName`: String (원본 파일명)
- `fileSize`: Long
- `mimeType`: String
- `s3Path`: String (전체 S3 경로)
- `uploadType`: UploadType
- `uploadedAt`: LocalDateTime
- `deletedAt`: LocalDateTime (논리 삭제, Nullable)
- `deleted`: Boolean (기본값: false)

**비즈니스 규칙**:
1. **파일 생성**: 업로드 완료 시에만 생성
2. **논리 삭제**: `deleted = true`, `deletedAt` 설정
3. **조회**: `deleted = false`만 조회

**Zero-Tolerance 규칙 준수**:
- ✅ Long FK 전략 (`userId`, `tenantId`)

---

### 2. Application Layer

#### 2.1 Command UseCase

**PrepareUploadUseCase** (세션 생성 + Presigned URL 생성):
- **Input**: `PrepareUploadCommand`
  ```java
  public record PrepareUploadCommand(
      String sessionId,        // UUID v4 (클라이언트 제공)
      UploadType uploadType,   // SINGLE | MULTIPART
      String customPath,       // "uploads/documents"
      String fileName,         // "document.pdf"
      Long fileSize,           // bytes
      String mimeType,         // "application/pdf"
      UserContext userContext  // JWT 파싱 결과
  ) {}
  ```
- **Output**: `PrepareUploadResponse`
  ```java
  public record PrepareUploadResponse(
      String sessionId,
      String fileId,
      UploadType uploadType,
      String uploadUrl,           // SINGLE인 경우
      List<String> partUploadUrls, // MULTIPART인 경우 (최대 10개)
      LocalDateTime expiresAt
  ) {}
  ```
- **Transaction**: Yes (세션 생성만, Redis 저장 포함)
  - ⚠️ **S3 API 호출은 트랜잭션 밖** (Presigned URL 생성은 조회 성격)
- **비즈니스 로직**:
  1. **멱등성 체크**: Redis에서 sessionId 존재 여부 확인
     - 존재하면 기존 세션 반환
  2. **파일 검증**: 크기, 타입 검증
  3. **S3 경로 생성**: JWT role 기반 경로 생성
  4. **세션 생성**: UploadSession Aggregate
  5. **Redis 저장**: TTL 15분
  6. **트랜잭션 커밋**
  7. **Presigned URL 생성** (트랜잭션 밖):
     - SINGLE: `s3.generatePresignedUrl(PUT, path, 15min)`
     - MULTIPART: `s3.initiateMultipartUpload()` + Part URL 생성

**CompleteUploadUseCase** (업로드 완료 처리):
- **Input**: `CompleteUploadCommand`
  ```java
  public record CompleteUploadCommand(
      String sessionId,
      UserContext userContext  // 권한 체크용
  ) {}
  ```
- **Output**: `FileResponse`
  ```java
  public record FileResponse(
      String fileId,
      String fileName,
      Long fileSize,
      String s3Path,
      LocalDateTime uploadedAt
  ) {}
  ```
- **Transaction**: Yes (File 생성 + 세션 상태 변경)
  - ⚠️ **S3 Complete Multipart Upload는 트랜잭션 밖**
- **비즈니스 로직**:
  1. 세션 조회 (Redis)
  2. 권한 체크 (userId 일치 여부)
  3. 상태 검증 (ACTIVE만 완료 가능)
  4. **멀티파트인 경우**: S3 Complete Multipart Upload (트랜잭션 밖)
  5. **트랜잭션 시작**
  6. File 엔티티 생성 (Optimistic Lock)
  7. 세션 상태 변경 (ACTIVE → COMPLETED)
  8. **트랜잭션 커밋**
  9. Redis 세션 삭제

**AbortUploadUseCase** (업로드 취소):
- **Input**: `AbortUploadCommand(sessionId, userContext)`
- **Output**: `void`
- **Transaction**: Yes (세션 상태만 변경)
  - ⚠️ **S3 Abort Multipart Upload는 트랜잭션 밖**
- **비즈니스 로직**:
  1. 세션 조회
  2. 권한 체크
  3. **멀티파트인 경우**: S3 Abort Multipart Upload (트랜잭션 밖)
  4. 세션 상태 변경 (ACTIVE → FAILED)
  5. Redis 세션 삭제

#### 2.2 Query UseCase

**GetUploadSessionUseCase**:
- **Input**: `GetUploadSessionQuery(sessionId, userContext)`
- **Output**: `UploadSessionResponse`
- **Transaction**: ReadOnly (Redis 조회)

**GetFileUseCase**:
- **Input**: `GetFileQuery(fileId, userContext)`
- **Output**: `FileDetailResponse`
- **Transaction**: ReadOnly
- **권한 체크**: 본인 파일만 조회 가능

**ListFilesUseCase**:
- **Input**: `ListFilesQuery(userId, page, size)`
- **Output**: `PageResponse<FileSummaryResponse>`
- **Transaction**: ReadOnly
- **페이징**: Cursor-based Pagination

#### 2.3 Event Listener

**UploadSessionExpiredListener** (Redis Keyspace Notification):
- **Trigger**: Redis TTL 만료 이벤트
- **처리**:
  1. 세션 조회 (Redis에서 이미 삭제됨, DB에서 조회)
  2. 상태가 ACTIVE면 EXPIRED로 변경
  3. 멀티파트인 경우: S3 Abort Multipart Upload

**S3UploadCompletedListener** (선택적, S3 Event):
- **Trigger**: S3 ObjectCreated 이벤트 (S3 → SQS → Lambda/Spring)
- **처리**: `CompleteUploadUseCase` 호출

#### 2.4 Scheduler

**ExpiredSessionCleanupScheduler**:
- **실행 주기**: 5분마다 (Cron: `0 */5 * * * *`)
- **처리**:
  1. `expiresAt < now() AND status = ACTIVE` 조회
  2. 각 세션에 대해 `AbortUploadUseCase` 호출

#### Zero-Tolerance 규칙 준수
- ✅ Command/Query 분리 (CQRS)
- ✅ **Transaction 경계 엄격 관리** (S3 API 호출은 트랜잭션 밖)
- ✅ **Orchestration Pattern**: S3 API 호출 후 보상 트랜잭션 처리

---

### 3. Persistence Layer

#### 3.1 Redis (세션 저장소)

**RedisUploadSession**:
```java
public record RedisUploadSession(
    String sessionId,
    Long userId,
    Long tenantId,
    String role,
    String sellerName,
    String uploadType,
    String customPath,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,
    String fileId,
    String s3Path,
    String uploadId,  // MULTIPART인 경우 S3 uploadId
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {}
```

**Redis Key 구조**:
- Key: `upload:session:{sessionId}`
- TTL: 15분
- Value: JSON (RedisUploadSession)

**Redis Keyspace Notification 설정**:
```properties
# redis.conf
notify-keyspace-events Ex  # Expired events
```

#### 3.2 MySQL (파일 메타데이터)

**FileJpaEntity**:
- **테이블**: `files`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `file_id`: String (UUID, Unique, Not Null, Index)
  - `user_id`: Long (FK, Not Null, Index)
  - `tenant_id`: Long (Not Null, Index)
  - `role`: String (Not Null)
  - `file_name`: String (Not Null)
  - `file_size`: Long (Not Null)
  - `mime_type`: String (Not Null)
  - `s3_path`: String (Not Null, Index)
  - `upload_type`: String (Not Null)
  - `uploaded_at`: LocalDateTime (Not Null, Index)
  - `deleted`: Boolean (Not Null, Default: false, Index)
  - `deleted_at`: LocalDateTime (Nullable)
  - `version`: Long (Optimistic Lock, Not Null)
- **인덱스**:
  - `idx_file_id` (file_id) - 파일 조회
  - `idx_user_uploaded` (user_id, uploaded_at DESC, deleted) - 사용자별 파일 목록
  - `idx_s3_path` (s3_path, deleted) - S3 경로 조회
- **Unique Constraint**:
  - `file_id` (UUID 중복 방지)

#### 3.3 Repository

**FileJpaRepository**:
```java
public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    Optional<FileJpaEntity> findByFileIdAndDeletedFalse(String fileId);
    List<FileJpaEntity> findByUserIdAndDeletedFalseOrderByUploadedAtDesc(Long userId);
}
```

**FileQueryDslRepository**:
- **메서드**: `findByUserIdWithPagination(userId, Pageable)`
- **최적화**: DTO Projection (N+1 방지)

#### Zero-Tolerance 규칙 준수
- ✅ Long FK 전략 (관계 어노테이션 금지)
- ✅ Optimistic Lock (`@Version`)
- ✅ QueryDSL 최적화 (N+1 방지)

---

### 4. REST API Layer

#### 4.1 API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/upload-sessions | Presigned URL 생성 | PrepareUploadRequest | PrepareUploadResponse | 200 OK |
| POST | /api/v1/upload-sessions/{sessionId}/complete | 업로드 완료 | - | FileResponse | 200 OK |
| POST | /api/v1/upload-sessions/{sessionId}/abort | 업로드 취소 | - | - | 204 No Content |
| GET | /api/v1/upload-sessions/{sessionId} | 세션 조회 | - | UploadSessionResponse | 200 OK |
| GET | /api/v1/files/{fileId} | 파일 조회 | - | FileDetailResponse | 200 OK |
| GET | /api/v1/files | 파일 목록 조회 | - | PageResponse<FileSummaryResponse> | 200 OK |
| DELETE | /api/v1/files/{fileId} | 파일 삭제 (논리) | - | - | 204 No Content |

#### 4.2 Request/Response DTO

**PrepareUploadRequest**:
```java
public record PrepareUploadRequest(
    @NotBlank String sessionId,          // UUID v4
    @NotNull UploadType uploadType,      // SINGLE | MULTIPART
    @NotBlank String customPath,         // "uploads/documents"
    @NotBlank String fileName,           // "document.pdf"
    @Min(1) @Max(1073741824) Long fileSize,  // 1B ~ 1GB
    @NotBlank String mimeType            // "image/jpeg" | "text/html"
) {}
```

**PrepareUploadResponse**:
```java
public record PrepareUploadResponse(
    String sessionId,
    String fileId,
    UploadType uploadType,
    String uploadUrl,               // SINGLE인 경우
    List<PartUploadUrl> partUploadUrls,  // MULTIPART인 경우
    LocalDateTime expiresAt
) {
    public record PartUploadUrl(
        int partNumber,   // 1 ~ 10
        String uploadUrl
    ) {}
}
```

**FileResponse**:
```java
public record FileResponse(
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String s3Path,
    LocalDateTime uploadedAt
) {}
```

**Error Response**:
```json
{
  "errorCode": "FILE_SIZE_EXCEEDED",
  "message": "파일 크기가 최대 허용 크기를 초과했습니다. (최대: 1GB)",
  "timestamp": "2025-01-18T12:34:56Z",
  "path": "/api/v1/upload-sessions"
}
```

#### 4.3 인증/인가

- **인증**: JWT (Access Token)
- **권한**: 모든 엔드포인트 로그인 필수
- **본인 확인**: 세션/파일 조회 시 `userId` 일치 여부 체크

#### 4.4 Validation

**파일 크기 제한**:
- SINGLE: 최대 100MB (104,857,600 bytes)
- MULTIPART: 최대 1GB (1,073,741,824 bytes)

**파일 타입 제한**:
- 허용: `image/*`, `text/html`
- 검증: MIME 타입 Prefix 체크

#### 4.5 Error Handling

| Error Code | HTTP Status | 설명 |
|------------|-------------|------|
| FILE_SIZE_EXCEEDED | 400 Bad Request | 파일 크기 초과 |
| UNSUPPORTED_FILE_TYPE | 400 Bad Request | 지원하지 않는 파일 타입 |
| SESSION_NOT_FOUND | 404 Not Found | 세션 없음 |
| SESSION_EXPIRED | 410 Gone | 세션 만료 |
| ALREADY_COMPLETED | 409 Conflict | 이미 완료된 세션 |
| UNAUTHORIZED_ACCESS | 403 Forbidden | 권한 없음 |
| REDIS_UNAVAILABLE | 503 Service Unavailable | Redis 장애 |

#### Zero-Tolerance 규칙 준수
- ✅ RESTful 설계 원칙
- ✅ 일관된 Error Response 형식

---

### 5. Infrastructure Layer

#### 5.1 S3 Client (AWS SDK)

**S3PresignedUrlGenerator**:
```java
public interface S3PresignedUrlGenerator {
    // 단일 업로드 URL 생성
    String generatePutUrl(String s3Path, Duration expiration);

    // 멀티파트 업로드 시작
    String initiateMultipartUpload(String s3Path);

    // Part URL 생성 (1 ~ 10)
    List<PartUploadUrl> generatePartUploadUrls(
        String s3Path,
        String uploadId,
        int partCount,
        Duration expiration
    );

    // 멀티파트 업로드 완료
    void completeMultipartUpload(String s3Path, String uploadId, List<String> eTags);

    // 멀티파트 업로드 취소
    void abortMultipartUpload(String s3Path, String uploadId);
}
```

#### 5.2 Redis Client (Lettuce)

**RedisUploadSessionRepository**:
```java
public interface RedisUploadSessionRepository {
    void save(RedisUploadSession session, Duration ttl);
    Optional<RedisUploadSession> findById(String sessionId);
    void deleteById(String sessionId);
}
```

**RedisKeyspaceEventListener**:
```java
@RedisListener
public class RedisKeyspaceEventListener {
    @EventListener
    public void onExpired(RedisKeyExpiredEvent<String> event) {
        String sessionId = event.getValue();
        // UploadSessionExpiredListener 호출
    }
}
```

#### 5.3 SQS Client (선택적, S3 Event)

**S3EventListener**:
```java
@SqsListener(queues = "upload-completed-queue")
public void onS3UploadCompleted(S3Event event) {
    String s3Path = event.getRecords().get(0).getS3().getObject().getKey();
    // sessionId 추출 → CompleteUploadUseCase 호출
}
```

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- Presigned URL 생성 응답 시간: < 500ms (P95)
- 파일 조회 응답 시간: < 100ms (P95)
- 동시 업로드 사용자: 낮음 (예상)

**보안**:
- JWT 인증 필수
- HTTPS 통신 (TLS 1.2+)
- Presigned URL 15분 만료
- S3 버킷 퍼블릭 액세스 차단

**확장성**:
- Redis Cluster 구성 (고가용성)
- S3 무제한 저장 (AWS 관리)

**가용성**:
- Redis 다운 시: 503 Service Unavailable (업로드 차단)
- S3 다운 시: AWS 장애 (클라이언트 재시도)

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- UploadSession Aggregate (세션 생성, 만료, 완료)
- File Aggregate (파일 생성, 논리 삭제)
- S3Path Value Object (경로 생성 규칙)
- UserRole Enum (ADMIN/SELLER/DEFAULT 경로 생성)

**Application**:
- PrepareUploadUseCase (Mock Redis, S3)
- CompleteUploadUseCase (Mock Redis, S3, FileRepository)
- Optimistic Lock 동시성 테스트

### Integration Test

**Persistence**:
- FileJpaRepository CRUD 테스트 (TestContainers MySQL)
- RedisUploadSessionRepository 테스트 (Embedded Redis)
- Optimistic Lock 실패 시나리오

**REST API**:
- UploadApiController (MockMvc)
- Validation 테스트 (400 Bad Request)
- 인증/인가 테스트 (401 Unauthorized, 403 Forbidden)

**Infrastructure**:
- S3 Presigned URL 생성 테스트 (LocalStack)
- Redis Keyspace Notification 테스트 (Embedded Redis)

### E2E Test

- 단일 업로드 플로우: 세션 생성 → URL 생성 → 업로드 → 완료
- 멀티파트 업로드 플로우: 세션 생성 → Part URL 생성 → Part 업로드 → Complete
- 세션 만료 플로우: 세션 생성 → 15분 대기 → 만료 확인
- 동시 세션 생성 테스트 (멱등성)

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 3일)
- [ ] UploadSession Aggregate 구현
- [ ] File Aggregate 구현
- [ ] S3Path, UserRole Value Object 구현
- [ ] Domain Unit Test (TestFixture 패턴)

### Phase 2: Application Layer (예상: 5일)
- [ ] PrepareUploadUseCase 구현
- [ ] CompleteUploadUseCase 구현
- [ ] AbortUploadUseCase 구현
- [ ] Query UseCase 구현
- [ ] ExpiredSessionCleanupScheduler 구현
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 3일)
- [ ] FileJpaEntity 구현
- [ ] FileJpaRepository 구현
- [ ] RedisUploadSessionRepository 구현
- [ ] QueryDSL 쿼리 구현
- [ ] Integration Test (TestContainers, Embedded Redis)

### Phase 4: Infrastructure Layer (예상: 3일)
- [ ] S3PresignedUrlGenerator 구현 (AWS SDK)
- [ ] RedisKeyspaceEventListener 구현
- [ ] S3EventListener 구현 (선택적)
- [ ] LocalStack 테스트

### Phase 5: REST API Layer (예상: 3일)
- [ ] UploadApiController 구현
- [ ] Request/Response DTO 구현
- [ ] Exception Handling 구현
- [ ] REST API Integration Test (MockMvc)

### Phase 6: Integration Test (예상: 2일)
- [ ] End-to-End Test 작성
- [ ] 동시성 테스트 (멱등성, Optimistic Lock)
- [ ] 세션 만료 테스트

---

## 📚 참고 문서

- [Domain Layer 규칙](../../docs/coding_convention/02-domain-layer/)
- [Application Layer 규칙](../../docs/coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../../docs/coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../../docs/coding_convention/01-adapter-in-layer/rest-api/)
- [AWS S3 Presigned URL](https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html)
- [Redis Keyspace Notifications](https://redis.io/docs/manual/keyspace-notifications/)

---

## 🔍 추가 고려사항

### 1. Redis 장애 시 Fallback (미결정)
- **현재**: Redis 다운 시 503 에러 (업로드 차단)
- **대안1**: DB로 임시 세션 관리 (성능 저하 감수)
- **대안2**: In-Memory Cache (애플리케이션 재시작 시 세션 소실)

### 2. S3 Event Notification 처리 방식 (미결정)
- **옵션1**: S3 → SQS → Spring `@SqsListener`
- **옵션2**: S3 → Lambda → Spring REST API 호출
- **옵션3**: 클라이언트 완료 호출만 사용 (S3 Event 미사용)

### 3. 멀티파트 Part 업로드 실패 시 재시도 (미결정)
- **현재**: 에러만 반환, 클라이언트가 재시도
- **대안**: 서버에서 자동 재시도 (Exponential Backoff)

### 4. 파일 삭제 정책 (미결정)
- **현재**: 논리 삭제만
- **추가 고려**: S3 파일도 삭제? (물리 삭제 스케줄러)

---

**다음 단계**:
1. PRD 검토 및 수정
2. `/jira-from-prd docs/prd/presigned-url-upload.md` - Jira 티켓 생성
3. Layer별 TDD 사이클 시작 (`/kb/domain/go`, `/kb/application/go` 등)
