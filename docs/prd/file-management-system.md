# PRD: File Management System (파일 관리 시스템)

**작성일**: 2025-01-14
**작성자**: ryu-qqq
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
S3 기반 파일 저장소 및 CDN을 활용한 파일 관리 시스템 구축으로 **파일 처리 자동화**와 **CDN 성능 개선**을 달성합니다.

### 주요 사용자
- **내부 서비스**: 상품 관리, 전시 영역 관리, 외부몰 연동 서비스
- **외부 고객**: 파일 업로드/조회 API 사용자
- **관리자**: 파일 가공 작업 모니터링 및 재시도

### 성공 기준
1. **파일 크기가 커도 누락 없이 S3 업로드 성공** (최우선)
2. 업로드 성공률 > 99.9%
3. Presigned URL 발급 응답 시간 < 200ms (P95)
4. 파일 가공 완료율 > 95%
5. CDN Hit Rate > 90% (상품 이미지)

### 기술 스택
- **Storage**: AWS S3
- **CDN**: CloudFront (또는 AWS CloudFront)
- **Message Queue**: AWS SQS (Standard Queue)
- **Database**: MySQL (JPA + QueryDSL)
- **File Processing**: 백그라운드 비동기 처리

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### Aggregate: File

**핵심 개념**: 파일 메타데이터 및 업로드 상태 관리

**속성**:
- `fileId`: String (UUID v7 - 날짜 포함, 시간 순서 정렬 가능)
- `fileName`: String (원본 파일명)
- `fileSize`: Long (바이트 단위)
- `mimeType`: String (예: `image/jpeg`, `text/html`)
- `status`: FileStatus (Enum)
- `s3Key`: String (S3 Object Key)
- `s3Bucket`: String (S3 Bucket Name)
- `cdnUrl`: String (Nullable, CDN URL)
- `uploaderId`: Long (업로더 User ID)
- `category`: String (상품, 전시영역, 외부몰 연동 문서 등)
- `tags`: List<String> (파일 태그, 예: #이미지, #문서)
- `version`: Integer (파일 버전, 같은 파일명 재업로드 시 증가)
- `deletedAt`: LocalDateTime (Nullable, Soft Delete)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙** (구체화):

1. **파일 ID 생성**:
   - UUID v7 사용 (날짜 포함, 시간 순서 정렬 가능)
   - S3 Key와 동일하게 사용 (예: `{fileId}.jpg`)

2. **파일 크기 제한**:
   - 최대 파일 크기: **1GB**
   - 파일 크기별 업로드 전략:
     - **< 100MB**: 단일 업로드 (Single PUT)
     - **≥ 100MB**: Multipart Upload (청크 크기: 5MB 또는 10MB)

3. **Presigned URL 직접 업로드**:
   - Presigned URL 유효 시간: **5분**
   - 업로드 완료 검증: **S3 Event Notification + 클라이언트 명시적 API 호출** (둘 다)
   - 업로드 상태 추적: 성공 여부만 추적 (Progress는 추후 고려)

4. **외부 링크 다운로드 후 업로드**:
   - 외부 URL 검증: **HTTPS만 허용**, 모든 도메인 허용 (추후 차단 리스트 추가)
   - 다운로드 타임아웃: **60초**
   - 다운로드 재시도: **3회** (Exponential Backoff)
   - 파일 크기 사전 체크: HEAD 요청으로 Content-Length 확인, 1GB 초과 시 에러

5. **파일 상태 전환**:
   ```
   PENDING → UPLOADING → COMPLETED
                ↓
            FAILED, RETRY_PENDING
                ↓
           PROCESSING (파일 가공 중)
   ```
   - **PENDING**: Presigned URL 발급 완료, 업로드 대기
   - **UPLOADING**: 클라이언트가 S3에 업로드 중
   - **COMPLETED**: 업로드 완료, S3 Object 존재 확인
   - **FAILED**: 업로드 실패 (Presigned URL 만료, S3 Object 없음)
   - **RETRY_PENDING**: 재시도 대기 (최대 3회)
   - **PROCESSING**: 파일 가공 중 (썸네일 생성, OCR 등)

6. **파일 버전 관리**:
   - 같은 파일명 재업로드 시 **새로운 File Entity 생성** (version 증가)
   - 예: `example.jpg` (version 1) → `example.jpg` (version 2)
   - 이전 버전은 Soft Delete (`deletedAt` 설정)

7. **Soft Delete**:
   - 파일 삭제 시 `deletedAt` 설정 (물리적 삭제 아님)
   - S3 Object는 유지 (추후 Lifecycle Policy로 자동 삭제)

**Value Objects**:
- **FileStatus**: Enum (PENDING, UPLOADING, COMPLETED, FAILED, RETRY_PENDING, PROCESSING)
- **MimeType**: String (Validation: `image/*`, `text/html`, `application/pdf`, `application/vnd.ms-excel` 등)

**Zero-Tolerance 규칙 준수**:
- ✅ **Lombok 금지**: Pure Java 또는 Record 사용
- ✅ **Law of Demeter**: Getter 체이닝 금지
  - `file.getS3Url()` (O)
  - `file.getS3().getUrl()` (X)
- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지, Long uploaderId 사용

---

#### Aggregate: FileProcessingJob

**핵심 개념**: 파일 타입별 가공 작업 관리

**속성**:
- `jobId`: String (UUID v7)
- `fileId`: String (FK, File UUID)
- `jobType`: JobType (Enum)
- `status`: JobStatus (Enum)
- `retryCount`: Integer (현재 재시도 횟수)
- `maxRetryCount`: Integer (최대 재시도 횟수: 2회)
- `inputS3Key`: String (원본 파일 S3 Key)
- `outputS3Key`: String (Nullable, 가공된 파일 S3 Key)
- `errorMessage`: String (Nullable, 에러 메시지)
- `createdAt`: LocalDateTime
- `processedAt`: LocalDateTime (Nullable)

**비즈니스 규칙** (구체화):

1. **가공 유형** (JobType Enum):
   - **이미지**:
     - `THUMBNAIL_GENERATION`: 썸네일 생성 (예: 200x200)
     - `IMAGE_RESIZE`: 리사이징 (예: 1920x1080)
     - `IMAGE_FORMAT_CONVERSION`: 포맷 변환 (JPEG → WebP)
     - `OCR`: OCR 텍스트 추출
   - **HTML**:
     - `HTML_PARSING`: HTML 파싱
     - `HTML_IMAGE_UPLOAD`: HTML 내부 이미지 업로드
     - `HTML_TEXT_ANALYSIS`: 글자 분석
   - **문서**:
     - `DOCUMENT_TEXT_EXTRACTION`: 텍스트 추출 (PDF, Word)
     - `DOCUMENT_FORMAT_CONVERSION`: 포맷 변환 (Word → PDF)
   - **엑셀**:
     - `EXCEL_CSV_CONVERSION`: CSV 변환
     - `EXCEL_DATA_EXTRACTION`: 데이터 추출

2. **가공 시점**:
   - 파일 업로드 완료 (`COMPLETED`) 후 **백그라운드 큐**에 가공 작업 등록
   - **비동기 처리** (AWS SQS)

3. **가공 실패 처리**:
   - 원본 파일은 **유지** (삭제하지 않음)
   - 자동 재시도: **최대 2회**
   - 2회 재시도 후 실패 시: 상태를 `FAILED`로 변경, 관리자 수동 재시도 API 제공

4. **가공 상태 전환**:
   ```
   PENDING → PROCESSING → COMPLETED
                ↓
            FAILED, RETRY_PENDING
   ```

5. **CDN 연동**:
   - 가공된 파일 중 **커머스 노출 상품 이미지 및 HTML만** CDN에 업로드
   - 조건: `category == "상품"` && (`jobType == THUMBNAIL_GENERATION` || `jobType == HTML_PARSING`)

**Value Objects**:
- **JobType**: Enum (위 가공 유형)
- **JobStatus**: Enum (PENDING, PROCESSING, COMPLETED, FAILED, RETRY_PENDING)

---

#### Aggregate: MessageOutbox

**핵심 개념**: 아웃박스 패턴을 통한 메시지 전송 신뢰성 보장

**속성**:
- `id`: Long (PK, Auto Increment)
- `eventType`: String (이벤트 타입)
- `aggregateId`: String (File UUID 또는 FileProcessingJob UUID)
- `payload`: String (JSON, 메시지 페이로드)
- `status`: OutboxStatus (Enum)
- `retryCount`: Integer (현재 재시도 횟수)
- `maxRetryCount`: Integer (최대 재시도 횟수: 3회)
- `createdAt`: LocalDateTime
- `processedAt`: LocalDateTime (Nullable)

**비즈니스 규칙** (구체화):

1. **이벤트 타입**:
   - `FILE_UPLOADED`: Presigned URL 업로드 완료
   - `FILE_DOWNLOAD_COMPLETED`: 외부 URL 다운로드 완료
   - `FILE_PROCESSING_COMPLETED`: 파일 가공 완료

2. **아웃박스 패턴 플로우**:
   ```
   UseCase 트랜잭션 안:
   1. File 또는 FileProcessingJob Entity 저장
   2. MessageOutbox Entity 저장 (PENDING 상태)
   3. 커밋

   애프터 커밋 리스너 (해피 패스):
   4. @TransactionalEventListener(phase = AFTER_COMMIT)
   5. SQS에 메시지 전송
   6. MessageOutbox 상태를 SENT로 업데이트

   폴백 스케줄러 (장애 복구):
   7. 주기적으로 (예: 1분마다) PENDING 상태의 MessageOutbox 조회
   8. SQS에 메시지 전송
   9. MessageOutbox 상태를 SENT로 업데이트
   ```

3. **재시도 전략**:
   - 최대 재시도 횟수: **3회**
   - Exponential Backoff: 1초, 2초, 4초
   - 3회 재시도 후 실패 시: Dead Letter Queue (DLQ)로 이동

4. **메시지 TTL (Time To Live)**:
   - 성공한 메시지 (`SENT`): **7일 후 삭제**
   - 실패한 메시지 (`FAILED`): **30일 후 삭제**

**Value Objects**:
- **OutboxStatus**: Enum (PENDING, SENT, FAILED)

---

### 2. Application Layer

#### Command UseCase

**A. GeneratePresignedUrlUseCase** (Presigned URL 발급):

**Input**: `GeneratePresignedUrlCommand(fileName, fileSize, mimeType, uploaderId, category, tags)`

**Output**: `PresignedUrlResponse(fileId, presignedUrl, expiresIn, s3Key)`

**Transaction 경계**:
1. File 메타데이터 생성 (DB 저장, PENDING 상태) ← **트랜잭션 안**
2. **트랜잭션 커밋**
3. S3 Presigned URL 발급 (AWS SDK 호출) ← **트랜잭션 밖**
4. S3 API 실패 시: File 상태를 `FAILED`로 변경 (보상 트랜잭션)

**비즈니스 로직**:
1. 파일 크기 검증 (최대 1GB)
2. MIME 타입 검증 (허용 목록: 이미지, HTML, 문서, 엑셀)
3. File Entity 생성 (UUID v7, PENDING 상태)
4. S3 Presigned URL 발급 (유효 시간: 5분)
5. 파일 크기별 업로드 전략 결정:
   - < 100MB: 단일 업로드 URL
   - ≥ 100MB: Multipart Upload Initiate URL

**Timeout & Retry**:
- S3 Presigned URL 발급 Timeout: **3초**
- 재시도: **3회**

---

**B. CompleteUploadUseCase** (업로드 완료 처리):

**Input**: `CompleteUploadCommand(fileId)`

**Output**: `FileResponse(fileId, status, s3Url, cdnUrl)`

**Transaction 경계**:
1. S3 Object 존재 여부 확인 (S3 HEAD 요청) ← **트랜잭션 밖**
2. S3 Object 존재 확인 → **트랜잭션 시작**
3. File 상태를 `UPLOADING` → `COMPLETED`로 업데이트
4. **트랜잭션 커밋**
5. S3 Object 없으면: 예외 발생 + File 상태를 `FAILED`로 변경

**비즈니스 로직**:
1. File 조회 (fileId로)
2. 현재 상태 검증 (PENDING 또는 UPLOADING만 허용)
3. S3 Object 존재 여부 확인 (HEAD 요청)
4. 존재하면: 상태를 `COMPLETED`로 변경
5. 파일 가공 작업 등록 (MessageOutbox 생성, FILE_UPLOADED 이벤트)

**Timeout & Retry**:
- S3 Object HEAD 요청 Timeout: **3초**
- 재시도: **3회**

---

**C. UploadFromExternalUrlUseCase** (외부 URL 다운로드 후 업로드):

**Input**: `UploadFromExternalUrlCommand(externalUrl, uploaderId, category, tags, webhookUrl)`

**Output**: `FileResponse(fileId, status)` (비동기, 즉시 반환)

**Transaction 경계**:
1. 외부 URL 검증 (HTTPS만 허용)
2. File 메타데이터 생성 (DB 저장, PENDING 상태) ← **트랜잭션 안**
3. MessageOutbox 생성 (FILE_DOWNLOAD_REQUESTED 이벤트) ← **트랜잭션 안**
4. **트랜잭션 커밋**
5. 애프터 커밋 리스너: SQS에 메시지 전송 ← **트랜잭션 밖**

**백그라운드 작업 (SQS Consumer)**:
1. 외부 URL에서 파일 다운로드 (스트리밍, 메모리 직접 업로드) ← **트랜잭션 밖**
2. 파일 크기 체크 (1GB 초과 시 에러)
3. S3에 업로드 (Multipart Upload 사용) ← **트랜잭션 밖**
4. **트랜잭션 시작**
5. File 상태를 `COMPLETED`로 업데이트
6. **트랜잭션 커밋**
7. Webhook 전송 ← **트랜잭션 밖**

**비즈니스 로직**:
1. 외부 URL 검증 (HTTPS 체크)
2. File Entity 생성 (UUID v7, PENDING 상태)
3. MessageOutbox 생성 + 애프터 커밋 리스너 → SQS 전송
4. 백그라운드에서 다운로드 + S3 업로드
5. 성공 시: Webhook 전송 (webhookUrl로)

**Timeout & Retry**:
- 외부 URL 다운로드 Timeout: **60초**
- Webhook 전송 Timeout: **3초**
- 재시도: **3회** (Exponential Backoff)

**Webhook Payload**:
```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "fileName": "example.jpg",
  "fileSize": 1048576,
  "s3Url": "https://s3.amazonaws.com/bucket/550e8400-e29b-41d4-a716-446655440000.jpg",
  "cdnUrl": "https://cdn.example.com/files/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

**Webhook 인증**: HMAC 서명 (SHA256)
```
X-Webhook-Signature: sha256=<HMAC-SHA256(payload, secret)>
```

---

**D. ProcessFileUseCase** (파일 가공 요청):

**Input**: `ProcessFileCommand(fileId, jobTypes)`

**Output**: `List<FileProcessingJobResponse>`

**Transaction 경계**:
1. File 조회 (상태가 COMPLETED인지 확인) ← **트랜잭션 안**
2. FileProcessingJob Entity 생성 (PENDING 상태) ← **트랜잭션 안**
3. MessageOutbox 생성 (FILE_PROCESSING_REQUESTED 이벤트) ← **트랜잭션 안**
4. **트랜잭션 커밋**
5. 애프터 커밋 리스너: SQS에 메시지 전송 ← **트랜잭션 밖**

**백그라운드 작업 (SQS Consumer)**:
1. S3에서 원본 파일 다운로드 ← **트랜잭션 밖**
2. 파일 가공 (썸네일, OCR, 변환 등) ← **트랜잭션 밖**
3. 가공된 파일 S3에 업로드 ← **트랜잭션 밖**
4. **트랜잭션 시작**
5. FileProcessingJob 상태를 `COMPLETED`로 업데이트, outputS3Key 저장
6. File 상태를 `PROCESSING` → `COMPLETED`로 업데이트
7. **트랜잭션 커밋**
8. CDN 조건 체크: 상품 이미지/HTML이면 CDN 업로드

**비즈니스 로직**:
1. File 조회 (COMPLETED 상태만 가공 가능)
2. FileProcessingJob Entity 생성 (각 jobType마다)
3. MessageOutbox 생성 + 애프터 커밋 리스너 → SQS 전송
4. 백그라운드에서 파일 가공 + S3 업로드
5. CDN 조건 충족 시: CloudFront Invalidation 요청

**가공 실패 처리**:
- 자동 재시도: **최대 2회**
- 2회 재시도 후 실패: 상태를 `FAILED`로 변경, 관리자 수동 재시도 API 제공

---

#### Query UseCase

**E. GetFileUseCase** (파일 조회):

**Input**: `GetFileQuery(fileId)`

**Output**: `FileDetailResponse(fileId, fileName, fileSize, status, s3Url, cdnUrl, processingJobs, ...)`

**Transaction**: ReadOnly

**비즈니스 로직**:
1. File 조회 (Soft Delete 제외)
2. FileProcessingJob 목록 조회 (fileId로)
3. Response DTO 조합

---

**F. ListFilesUseCase** (파일 목록 조회):

**Input**: `ListFilesQuery(uploaderId, status, category, cursor, size)`

**Output**: `CursorPageResponse<FileSummaryResponse>`

**Transaction**: ReadOnly

**페이징**: Cursor-based Pagination (createdAt 기준)
```sql
SELECT * FROM files
WHERE uploader_id = ?
  AND status = ?
  AND created_at < ? -- cursor
ORDER BY created_at DESC
LIMIT ?;
```

---

#### Zero-Tolerance 규칙 준수
- ✅ **Command/Query 분리** (CQRS)
- ✅ **Transaction 경계 엄격 관리** (외부 API 호출은 트랜잭션 밖)
- ✅ **아웃박스 패턴 필수** (메시지 전송 신뢰성 보장)

---

### 3. Persistence Layer

#### A. JPA Entity

**FileJpaEntity**:
- **테이블**: `files`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `file_id`: String (UUID v7, Unique, Not Null)
  - `file_name`: String (Not Null)
  - `file_size`: Long (Not Null, CHECK > 0)
  - `mime_type`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `s3_key`: String (Not Null)
  - `s3_bucket`: String (Not Null)
  - `cdn_url`: String (Nullable)
  - `uploader_id`: Long (FK, Not Null, Index)
  - `category`: String (Nullable, Index)
  - `tags`: String (JSON, Nullable)
  - `version`: Integer (Not Null, Default: 1)
  - `deleted_at`: LocalDateTime (Nullable)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `updated_at`: LocalDateTime (Not Null)
- **인덱스**:
  - **Primary Key**: `id`
  - **Unique**: `file_id`
  - **복합 인덱스**: `(uploader_id, status, created_at DESC)` - 사용자별 상태 필터링 + 정렬 최적화
  - **단일 인덱스**: `category` (카테고리별 조회)
- **Optimistic Lock**: `@Version` 필드 추가 (동시성 제어)

---

**FileProcessingJobJpaEntity**:
- **테이블**: `file_processing_jobs`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `job_id`: String (UUID v7, Unique, Not Null)
  - `file_id`: String (FK, Not Null, Index)
  - `job_type`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default: 0)
  - `max_retry_count`: Integer (Not Null, Default: 2)
  - `input_s3_key`: String (Not Null)
  - `output_s3_key`: String (Nullable)
  - `error_message`: String (Nullable)
  - `created_at`: LocalDateTime (Not Null)
  - `processed_at`: LocalDateTime (Nullable)
- **인덱스**:
  - **Primary Key**: `id`
  - **Unique**: `job_id`
  - **복합 인덱스**: `(file_id, status)` - 파일별 상태 필터링

---

**MessageOutboxJpaEntity**:
- **테이블**: `message_outbox`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `event_type`: String (Not Null)
  - `aggregate_id`: String (Not Null)
  - `payload`: String (JSON, Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default: 0)
  - `max_retry_count`: Integer (Not Null, Default: 3)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `processed_at`: LocalDateTime (Nullable)
- **인덱스**:
  - **Primary Key**: `id`
  - **복합 인덱스**: `(status, created_at)` - 스케줄러 성능 최적화 (PENDING 메시지 조회)

---

#### B. Repository

**FileJpaRepository**:
```java
public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    Optional<FileJpaEntity> findByFileId(String fileId);

    @Query("SELECT f FROM FileJpaEntity f WHERE f.uploaderId = :uploaderId " +
           "AND f.status = :status AND f.createdAt < :cursor " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.createdAt DESC")
    List<FileJpaEntity> findByUploaderIdAndStatusWithCursor(
        Long uploaderId, String status, LocalDateTime cursor, Pageable pageable);
}
```

**FileQueryDslRepository** (복잡한 쿼리):
- **메서드**: `findByUploaderIdAndStatusAndCategoryWithCursor(...)`
- **최적화**: DTO Projection (N+1 방지)

**MessageOutboxJpaRepository**:
```java
public interface MessageOutboxJpaRepository extends JpaRepository<MessageOutboxJpaEntity, Long> {
    @Query("SELECT m FROM MessageOutboxJpaEntity m WHERE m.status = 'PENDING' " +
           "AND m.createdAt < :threshold ORDER BY m.createdAt ASC")
    List<MessageOutboxJpaEntity> findPendingMessages(LocalDateTime threshold, Pageable pageable);
}
```

---

#### C. Flyway Migration

**V1__create_files_table.sql**:
```sql
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(100) NOT NULL,
    cdn_url VARCHAR(500),
    uploader_id BIGINT NOT NULL,
    category VARCHAR(100),
    tags JSON,
    version INT NOT NULL DEFAULT 1,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_uploader_status_created (uploader_id, status, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

#### D. 동시성 제어

**Optimistic Lock** (`@Version`):
- File Entity에 `@Version` 필드 추가
- 동시 업로드 완료 API 호출 시 충돌 감지 → 예외 발생 → 클라이언트 재시도

**예시**:
```java
@Entity
public class FileJpaEntity {
    @Version
    private Long version;

    // ...
}
```

---

#### Zero-Tolerance 규칙 준수
- ✅ **Long FK 전략** (관계 어노테이션 금지)
  - `private Long uploaderId;` (O)
  - `@ManyToOne private User user;` (X)
- ✅ **QueryDSL 최적화** (N+1 방지, DTO Projection)
- ✅ **Lombok 금지** (Pure Java 또는 Record)

---

### 4. REST API Layer

#### A. API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/files/presigned-url | Presigned URL 발급 | GeneratePresignedUrlRequest | PresignedUrlResponse | 201 Created |
| POST | /api/v1/files/upload-complete | 업로드 완료 알림 | CompleteUploadRequest | FileResponse | 200 OK |
| POST | /api/v1/files/from-external-url | 외부 URL 다운로드 요청 | UploadFromExternalUrlRequest | FileResponse | 202 Accepted |
| GET | /api/v1/files/{fileId} | 파일 조회 | - | FileDetailResponse | 200 OK |
| GET | /api/v1/files | 파일 목록 조회 | ListFilesRequest (Query Params) | CursorPageResponse<FileSummaryResponse> | 200 OK |
| DELETE | /api/v1/files/{fileId} | 파일 삭제 (Soft Delete) | - | ApiResponse<Void> | 204 No Content |
| POST | /api/v1/files/{fileId}/process | 파일 가공 요청 | ProcessFileRequest | List<FileProcessingJobResponse> | 202 Accepted |
| GET | /api/v1/files/{fileId}/processing-jobs | 파일 가공 작업 조회 | - | List<FileProcessingJobResponse> | 200 OK |

---

#### B. Request/Response DTO

**GeneratePresignedUrlRequest**:
```java
public record GeneratePresignedUrlRequest(
    @NotBlank String fileName,
    @NotNull @Min(1) @Max(1073741824) Long fileSize, // 최대 1GB
    @NotBlank String mimeType,
    @NotNull Long uploaderId,
    String category,
    List<String> tags
) {}
```

**PresignedUrlResponse**:
```java
public record PresignedUrlResponse(
    String fileId,
    String presignedUrl,
    int expiresIn, // 초 단위 (300초 = 5분)
    String s3Key
) {}
```

**CompleteUploadRequest**:
```java
public record CompleteUploadRequest(
    @NotBlank String fileId
) {}
```

**UploadFromExternalUrlRequest**:
```java
public record UploadFromExternalUrlRequest(
    @NotBlank @Pattern(regexp = "^https://.*") String externalUrl,
    @NotNull Long uploaderId,
    String category,
    List<String> tags,
    String webhookUrl // Webhook URL (Nullable)
) {}
```

**FileResponse**:
```java
public record FileResponse(
    String fileId,
    String fileName,
    Long fileSize,
    String status,
    String s3Url,
    String cdnUrl
) {}
```

**FileDetailResponse**:
```java
public record FileDetailResponse(
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,
    String s3Url,
    String cdnUrl,
    Long uploaderId,
    String category,
    List<String> tags,
    Integer version,
    List<FileProcessingJobResponse> processingJobs,
    LocalDateTime createdAt
) {}
```

**ProcessFileRequest**:
```java
public record ProcessFileRequest(
    @NotEmpty List<String> jobTypes // ["THUMBNAIL_GENERATION", "OCR"]
) {}
```

**FileProcessingJobResponse**:
```java
public record FileProcessingJobResponse(
    String jobId,
    String jobType,
    String status,
    String outputS3Key,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {}
```

---

#### C. Error Handling

**ApiResponse<T> 사용** (프로젝트 표준):
```json
{
  "success": false,
  "data": null,
  "error": {
    "errorCode": "FILE_NOT_FOUND",
    "message": "파일을 찾을 수 없습니다."
  },
  "timestamp": "2025-01-14T12:34:56",
  "requestId": "req-123456"
}
```

**Error Code 규칙** (대문자 스네이크 케이스):
- `FILE_NOT_FOUND`: 파일 없음
- `FILE_SIZE_EXCEEDED`: 파일 크기 초과 (> 1GB)
- `FILE_TYPE_NOT_SUPPORTED`: 지원하지 않는 파일 타입
- `PRESIGNED_URL_EXPIRED`: Presigned URL 만료
- `UPLOAD_FAILED`: 업로드 실패
- `EXTERNAL_URL_DOWNLOAD_FAILED`: 외부 URL 다운로드 실패
- `FILE_PROCESSING_FAILED`: 파일 가공 실패
- `S3_SERVICE_UNAVAILABLE`: S3 서비스 장애

**HTTP Status Code 전략**:
- **200 OK**: 성공 (조회, 업데이트)
- **201 Created**: 리소스 생성 (Presigned URL 발급)
- **202 Accepted**: 비동기 작업 수락 (외부 URL 다운로드, 파일 가공)
- **204 No Content**: 성공 (삭제)
- **400 Bad Request**: Validation 실패 (파일 크기 초과, 잘못된 파일 타입)
- **401 Unauthorized**: 인증 실패 (추후 인증 추가 시)
- **403 Forbidden**: 권한 없음 (타인 파일 접근)
- **404 Not Found**: 파일 없음
- **409 Conflict**: 비즈니스 규칙 위반 (이미 완료된 파일 재업로드)
- **413 Payload Too Large**: 파일 크기 초과 (> 1GB)
- **500 Internal Server Error**: 서버 오류
- **503 Service Unavailable**: S3 장애

---

#### D. 인증/인가

**현재 (Phase 1)**: 인증 없음, 모든 API 접근 가능

**추후 (Phase 2)**: JWT 인증 추가
- Access Token 만료: 1시간
- Refresh Token 만료: 7일
- 업로드/조회 모두 로그인 필수
- Public URL 별도 API: `/api/v1/files/{fileId}/public-url` (인증 불필요)

---

#### Zero-Tolerance 규칙 준수
- ✅ **RESTful 설계 원칙**
- ✅ **일관된 Error Response 형식** (ApiResponse<T>)
- ✅ **Validation 필수** (@NotNull, @NotBlank, @Min, @Max, @Pattern)

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- Presigned URL 발급 응답 시간: < 200ms (P95)
- 파일 조회 응답 시간: < 100ms (P95)
- 업로드 성공률: > 99.9%
- 파일 가공 완료율: > 95%

**보안**:
- HTTPS 통신 필수 (TLS 1.2+)
- Presigned URL 유효 시간 제한 (5분)
- Webhook HMAC 서명 검증
- S3 Bucket Policy: 특정 IP만 접근 허용 (추후)

**확장성**:
- 동시 사용자: 20명 내외 (현재)
- 예상 트래픽: 낮음 (일일 업로드 수: 100-500건)
- S3 Bucket: 1TB 용량 (1년)

**안정성**:
- 아웃박스 패턴 + 애프터 커밋 리스너 + 폴백 스케줄러 (메시지 전송 신뢰성 보장)
- Multipart Upload 실패 시 Part만 재시도
- 전체 업로드 실패 시 최대 3회 재시도
- Dead Letter Queue (DLQ) 활용

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- File Aggregate 비즈니스 로직 (상태 전환, 파일 크기 검증)
- FileProcessingJob Aggregate (가공 타입별 로직)
- FileStatus Enum 상태 전환 로직

**Application**:
- GeneratePresignedUrlUseCase (Mock S3 Client)
- CompleteUploadUseCase (Mock S3 Client)
- UploadFromExternalUrlUseCase (Mock SQS Client)
- ProcessFileUseCase (Mock SQS Client)

### Integration Test

**Persistence**:
- FileJpaRepository CRUD 테스트 (TestContainers MySQL)
- FileQueryDslRepository 복잡한 쿼리 테스트 (Cursor Pagination)
- MessageOutboxJpaRepository 스케줄러 쿼리 테스트

**REST API**:
- FileApiController (TestRestTemplate)
- Validation 테스트 (400 Bad Request)
- Error Handling 테스트 (404 Not Found, 413 Payload Too Large)

### E2E Test

- Presigned URL 발급 → 클라이언트 S3 업로드 → 업로드 완료 API 호출 → 파일 조회 플로우
- 외부 URL 다운로드 → S3 업로드 → Webhook 전송 플로우
- 파일 가공 요청 → 백그라운드 가공 → 가공 완료 확인 플로우
- Multipart Upload 실패 → Part 재시도 → 최종 성공 플로우

---

## 🚀 개발 계획

### Phase 1: 기본 업로드 기능 (예상: 10일)

**Week 1 (Domain + Application)**:
- [ ] Domain Layer 구현 (File, FileProcessingJob, MessageOutbox Aggregate)
- [ ] Application Layer 구현 (GeneratePresignedUrlUseCase, CompleteUploadUseCase)
- [ ] Domain Unit Test (TestFixture 패턴)
- [ ] Application Unit Test (Mock 사용)

**Week 2 (Persistence + REST API)**:
- [ ] Persistence Layer 구현 (JPA Entity, Repository, Flyway Migration)
- [ ] REST API Layer 구현 (FileApiController, Request/Response DTO)
- [ ] Integration Test (TestContainers MySQL, TestRestTemplate)
- [ ] E2E Test (Presigned URL 발급 → 업로드 완료 플로우)

---

### Phase 2: 외부 URL 다운로드 + 아웃박스 패턴 (예상: 7일)

**Week 3 (비동기 처리)**:
- [ ] UploadFromExternalUrlUseCase 구현 (비동기)
- [ ] MessageOutbox 아웃박스 패턴 구현 (애프터 커밋 리스너 + 폴백 스케줄러)
- [ ] SQS Consumer 구현 (외부 URL 다운로드 + S3 업로드)
- [ ] Webhook 전송 구현 (HMAC 서명)
- [ ] Integration Test (아웃박스 패턴 검증)

---

### Phase 3: 파일 가공 파이프라인 (예상: 10일)

**Week 4-5 (파일 가공)**:
- [ ] ProcessFileUseCase 구현 (비동기)
- [ ] 파일 가공 Worker 구현:
  - [ ] 이미지 가공 (썸네일, 리사이징, JPEG→WebP, OCR)
  - [ ] HTML 가공 (파싱, 내부 이미지 업로드, 글자 분석)
  - [ ] 문서 가공 (텍스트 추출, 변환)
  - [ ] 엑셀 가공 (CSV 변환, 데이터 추출)
- [ ] CDN 연동 (CloudFront Invalidation)
- [ ] Integration Test (파일 가공 플로우)

---

### Phase 4: 운영 최적화 (예상: 3일)

**Week 6 (모니터링 + 최적화)**:
- [ ] 모니터링 대시보드 구축 (CloudWatch)
- [ ] 알람 설정 (업로드 실패율, 가공 실패율)
- [ ] 성능 최적화 (쿼리 튜닝, 인덱스 최적화)
- [ ] Dead Letter Queue (DLQ) 관리자 API

---

## 📚 참고 문서

### 프로젝트 컨벤션
- [Domain Layer 규칙](../coding_convention/02-domain-layer/)
- [Application Layer 규칙](../coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../coding_convention/01-adapter-in-layer/rest-api/)

### 외부 문서
- [AWS S3 Presigned URL](https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html)
- [AWS S3 Multipart Upload](https://docs.aws.amazon.com/AmazonS3/latest/userguide/mpuoverview.html)
- [AWS SQS](https://docs.aws.amazon.com/sqs/index.html)
- [UUID v7 Specification](https://datatracker.ietf.org/doc/html/draft-peabody-dispatch-new-uuid-format-04)
- [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)

---

**다음 단계**:
1. PRD 검토 및 수정
2. `/jira-from-prd docs/prd/file-management-system.md` - Jira 티켓 생성 (선택)
3. Layer별 TDD 사이클 시작 (`/kb/domain/go`, `/kb/application/go` 등)

---

**변경 이력**:
- 2025-01-14: 초안 작성 (ryu-qqq)
