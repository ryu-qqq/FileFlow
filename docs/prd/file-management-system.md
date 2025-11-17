# PRD: File Management System (파일 관리 시스템)

**작성일**: 2025-01-14
**최종 수정일**: 2025-01-17
**작성자**: ryu-qqq
**상태**: Draft → **재설계 (VO 확장 + 세션 기반 아키텍처)**

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
2. **멱등성 보장**: 동일 요청에 대해 중복 업로드/다운로드 방지
3. 업로드 성공률 > 99.9%
4. Presigned URL 발급 응답 시간 < 200ms (P95)
5. 파일 가공 완료율 > 95%
6. CDN Hit Rate > 90% (상품 이미지)

### 기술 스택
- **Storage**: AWS S3
- **CDN**: CloudFront
- **Message Queue**: AWS SQS (Standard Queue)
- **Database**: MySQL (JPA + QueryDSL)
- **File Processing**: 백그라운드 비동기 처리

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

---

#### Value Objects (VO 중심 설계)

##### Core File VOs
| VO | 책임 | 검증 규칙 | 예시 |
|----|------|----------|------|
| **FileName** | 파일명 검증 | - 길이: 1-255자<br>- 금지 문자: `/`, `\`, `<`, `>`, `:`, `"`, `|`, `?`, `*`<br>- Null/Empty 불가 | `FileName.of("example.jpg")` |
| **FileSize** | 파일 크기 검증 | - 범위: 1 byte ~ 1GB (1,073,741,824 bytes)<br>- 0 이하 불가 | `FileSize.of(1048576L)` |
| **MimeType** | MIME 타입 검증 | - 허용 목록 체크 (image/*, application/pdf 등)<br>- Null 불가 | `MimeType.of("image/jpeg")` |
| **FileCategory** | 파일 카테고리 검증 | - 허용 목록: "상품", "전시영역", "외부몰연동", "문서"<br>- Null 허용 (기본값: "기타") | `FileCategory.of("상품")` |
| **Tags** | 파일 태그 (복수) | - 콤마 구분 문자열 또는 List<String><br>- 최대 10개 태그<br>- 각 태그 최대 20자 | `Tags.of("이미지,상품,썸네일")` |
| **Checksum** | 체크섬 검증 | - 알고리즘: SHA-256, MD5<br>- 값: Hex String (64자 또는 32자) | `Checksum.sha256("abc123...")` |
| **ETag** | S3 ETag | - S3 반환 값 저장<br>- 체크섬 비교용 | `ETag.of("d41d8cd98f00b204e9800998ecf8427e")` |
| **ExternalUrl** | 외부 URL 검증 | - HTTPS 필수<br>- URL 형식 검증 | `ExternalUrl.of("https://example.com/image.jpg")` |

##### Upload Session VOs
| VO | 책임 | 속성 | 예시 |
|----|------|------|------|
| **SessionId** | 멱등키 (UUID v7) | - value: String (UUID) | `SessionId.generate()` |
| **UploadType** | 업로드 전략 | - Enum: SINGLE, MULTIPART<br>- 파일 크기로 자동 결정 | `UploadType.determineBySize(fileSize)` |
| **SessionStatus** | 세션 상태 | - Enum: INITIATED, IN_PROGRESS, COMPLETED, EXPIRED, FAILED | `SessionStatus.INITIATED` |
| **MultipartUpload** | 멀티파트 업로드 정보 | - uploadId: MultipartUploadId<br>- status: MultipartStatus<br>- totalParts: int<br>- uploadedParts: List<UploadedPart><br>- initiatedAt, completedAt, abortedAt | `MultipartUpload.forNew(...)` |
| **UploadedPart** | 업로드된 파트 | - partNumber: int<br>- etag: ETag<br>- size: long | `UploadedPart.of(1, etag, 5242880L)` |
| **MultipartUploadId** | S3 멀티파트 업로드 ID | - value: String (S3 반환 값) | `MultipartUploadId.of("...")` |
| **MultipartStatus** | 멀티파트 상태 | - Enum: INITIATED, IN_PROGRESS, COMPLETED, ABORTED | `MultipartStatus.INITIATED` |

##### Retry & Quota VOs
| VO | 책임 | 속성 | 예시 |
|----|------|------|------|
| **RetryCount** | 재시도 횟수 관리 | - current: int (현재 횟수)<br>- max: int (최대 횟수)<br>- canRetry(): boolean | `RetryCount.of(0, 3)` |

##### Tenant/Organization VOs (향후 확장)
| VO | 책임 | 비고 |
|----|------|------|
| **TenantId** | 테넌트 식별자 | - value: Long<br>- Long FK 전략 |
| **OrganizationId** | 조직 식별자 | - value: Long<br>- Tenant 하위 조직 |
| **DailyUploadQuota** | 일일 업로드 할당량 | - quota: long (bytes)<br>- 향후 구현 |

**MultipartUpload VO 상세**:
```java
/**
 * 멀티파트 업로드 VO
 * <p>
 * 멀티파트 업로드 관련 모든 정보를 캡슐화합니다.
 * File Aggregate에서 분리하여 단일 책임 원칙을 준수합니다.
 * </p>
 */
public class MultipartUpload {
    private final MultipartUploadId uploadId; // S3 multipart upload ID
    private final MultipartStatus status; // INITIATED, IN_PROGRESS, COMPLETED, ABORTED
    private final int totalParts; // 전체 파트 수
    private final List<UploadedPart> uploadedParts; // 업로드된 파트 목록
    private final LocalDateTime initiatedAt; // 시작 시각
    private final LocalDateTime completedAt; // 완료 시각 (Nullable)
    private final LocalDateTime abortedAt; // 중단 시각 (Nullable)

    // 도메인 메서드
    public void addPart(UploadedPart part) { ... }
    public boolean isAllPartsUploaded() { ... }
    public void markAsCompleted(Clock clock) { ... }
    public void markAsAborted(Clock clock) { ... }
}
```

**RetryCount VO 상세**:
```java
/**
 * 재시도 횟수 VO
 * <p>
 * 재시도 로직을 캡슐화하여 중복 코드를 제거합니다.
 * </p>
 */
public class RetryCount {
    private final int current; // 현재 재시도 횟수
    private final int max; // 최대 재시도 횟수

    public static RetryCount forFile() {
        return new RetryCount(0, 3); // File: 최대 3회
    }

    public static RetryCount forJob() {
        return new RetryCount(0, 2); // FileProcessingJob: 최대 2회
    }

    public static RetryCount forOutbox() {
        return new RetryCount(0, 3); // MessageOutbox: 최대 3회
    }

    public boolean canRetry() {
        return current < max;
    }

    public RetryCount increment() {
        if (!canRetry()) {
            throw new IllegalStateException("최대 재시도 횟수를 초과했습니다");
        }
        return new RetryCount(current + 1, max);
    }
}
```

---

#### Aggregate: File

**핵심 개념**: 파일 메타데이터 및 업로드 상태 관리

**속성** (VO 적용):
- `fileId`: FileId (UUID v7 VO)
- `fileName`: **FileName** (VO)
- `fileSize`: **FileSize** (VO)
- `mimeType`: **MimeType** (VO)
- `status`: FileStatus (Enum)
- `s3Key`: String (S3 Object Key)
- `s3Bucket`: String (S3 Bucket Name)
- `cdnUrl`: String (Nullable, CDN URL)
- `uploaderId`: UploaderId (VO, Long FK 전략)
- `tenantId`: **TenantId** (VO, Nullable, 향후 확장)
- `category`: **FileCategory** (VO)
- `tags`: **Tags** (VO)
- `checksum`: **Checksum** (VO, Optional, 업로드 시 클라이언트 제공)
- `etag`: **ETag** (VO, Nullable, S3 ETag)
- `retryCount`: **RetryCount** (VO)
- `version`: Integer (파일 버전, 같은 파일명 재업로드 시 증가)
- `deletedAt`: LocalDateTime (Nullable, Soft Delete)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:

1. **파일 ID 생성**:
   - UUID v7 사용 (날짜 포함, 시간 순서 정렬 가능)
   - S3 Key와 동일하게 사용 (예: `{fileId}.jpg`)

2. **파일 크기 제한**:
   - 최대 파일 크기: **1GB** (FileSize VO에서 검증)
   - 파일 크기별 업로드 전략:
     - **< 100MB**: 단일 업로드 (Single PUT)
     - **≥ 100MB**: Multipart Upload

3. **파일 상태 전환**:
   ```
   PENDING → UPLOADING → COMPLETED
                ↓
            FAILED, RETRY_PENDING
                ↓
           PROCESSING (파일 가공 중)
   ```

4. **체크섬 검증** (Optional):
   - 클라이언트가 `checksum` 제공 시: 업로드 완료 후 S3 ETag와 비교
   - 불일치 시: `FAILED` 상태 전환

5. **재시도 전략**:
   - `retryCount.canRetry()`: true → 재시도 가능
   - 최대 재시도 횟수: **3회** (RetryCount VO에서 관리)

**도메인 메서드**:
- `markAsCompleted(ETag etag)`: COMPLETED 상태 전환, ETag 저장
- `markAsFailed()`: FAILED 상태 전환
- `validateChecksum(Checksum uploadedChecksum)`: 체크섬 검증
- `incrementRetryCount()`: 재시도 횟수 증가
- `canRetry()`: 재시도 가능 여부 체크

**Zero-Tolerance 규칙 준수**:
- ✅ **Lombok 금지**: Pure Java 또는 Record 사용
- ✅ **Law of Demeter**: `file.getFileNameValue()` (O), `file.getFileName().getValue()` (X)
- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지, TenantId, UploaderId VO 사용

---

#### Aggregate: UploadSession

**핵심 개념**: 프리사인드 URL 발급부터 업로드 완료까지의 세션 추적 및 멱등성 보장

**속성**:
- `sessionId`: **SessionId** (멱등키, UUID v7 VO)
- `tenantId`: **TenantId** (VO, Nullable, 향후 확장)
- `fileName`: **FileName** (VO)
- `fileSize`: **FileSize** (VO)
- `mimeType`: **MimeType** (VO)
- `uploadType`: **UploadType** (VO, SINGLE/MULTIPART)
- `multipartUpload`: **MultipartUpload** (VO, Optional, uploadType=MULTIPART일 때만)
- `checksum`: **Checksum** (VO, Optional)
- `etag`: **ETag** (VO, Nullable, 업로드 완료 후)
- `presignedUrl`: String (Nullable)
- `expiresAt`: LocalDateTime (세션 만료 시각)
- `status`: **SessionStatus** (VO)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:

1. **멱등성 보장**:
   - 동일한 `sessionId`로 중복 발급 방지
   - 기존 세션 조회 시: 상태에 따라 기존 URL 반환 또는 에러

2. **세션 만료**:
   - Presigned URL 유효 시간: **5분**
   - 만료 시 자동으로 `EXPIRED` 상태 전환

3. **업로드 전략 자동 결정**:
   - `fileSize < 100MB` → `uploadType = SINGLE`
   - `fileSize >= 100MB` → `uploadType = MULTIPART`

4. **멀티파트 업로드 추적**:
   - `uploadType = MULTIPART`일 때 `multipartUpload` 필수
   - S3 Initiate Multipart Upload → `MultipartUploadId` 생성
   - 각 파트 업로드 완료 시 → `multipartUpload.addPart(part)`

5. **체크섬 검증** (Optional):
   - 클라이언트가 `checksum` 제공 시: 업로드 완료 후 S3 ETag와 비교

**상태 전환**:
```
INITIATED (URL 발급 완료)
    ↓
IN_PROGRESS (클라이언트 업로드 시작)
    ↓
COMPLETED (업로드 완료) → 24시간 후 삭제
    ↓
EXPIRED (5분 만료) → 24시간 후 삭제
    ↓
FAILED (체크섬 불일치, S3 에러) → 7일 후 삭제
```

**도메인 메서드**:
- `markAsInProgress()`: IN_PROGRESS 상태 전환
- `markAsCompleted(ETag etag)`: COMPLETED 상태 전환, ETag 저장
- `markAsExpired()`: EXPIRED 상태 전환
- `markAsFailed(String reason)`: FAILED 상태 전환
- `addUploadedPart(UploadedPart part)`: 멀티파트 파트 추가
- `isExpired(Clock clock)`: 만료 여부 체크
- `validateChecksum(ETag s3Etag)`: 체크섬 검증

---

#### Aggregate: DownloadSession

**핵심 개념**: 외부 URL 다운로드 요청부터 완료까지의 세션 추적 및 중복 방지

**속성**:
- `sessionId`: **SessionId** (멱등키, UUID v7 VO)
- `externalUrl`: **ExternalUrl** (VO, HTTPS 검증)
- `tenantId`: **TenantId** (VO, Nullable, 향후 확장)
- `uploadSessionId`: **SessionId** (VO, Nullable, 다운로드 후 생성된 UploadSession ID)
- `status`: **SessionStatus** (VO)
- `retryCount`: **RetryCount** (VO)
- `expiresAt`: LocalDateTime (세션 만료 시각)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:

1. **중복 다운로드 방지**:
   - 동일한 `externalUrl`로 24시간 내 다운로드 요청 시: 기존 세션 반환
   - `externalUrl`의 SHA-256 해시로 중복 체크

2. **다운로드 → 업로드 연결**:
   - 외부 URL 다운로드 완료 후 → `UploadSession` 생성
   - `uploadSessionId`에 생성된 UploadSession ID 저장

3. **재시도 전략**:
   - `retryCount.canRetry()`: true → 재시도 가능
   - 최대 재시도 횟수: **3회** (RetryCount VO에서 관리)
   - Exponential Backoff: 1초, 2초, 4초

**상태 전환**:
```
INITIATED (다운로드 요청)
    ↓
IN_PROGRESS (다운로드 중)
    ↓
COMPLETED (다운로드 완료, UploadSession 생성) → 7일 후 삭제
    ↓
EXPIRED (60초 타임아웃) → 7일 후 삭제
    ↓
FAILED (다운로드 실패, 3회 재시도 후) → 7일 후 삭제
```

**도메인 메서드**:
- `markAsInProgress()`: IN_PROGRESS 상태 전환
- `markAsCompleted(SessionId uploadSessionId)`: COMPLETED 상태 전환, UploadSession 연결
- `markAsFailed()`: FAILED 상태 전환
- `incrementRetryCount()`: 재시도 횟수 증가
- `canRetry()`: 재시도 가능 여부 체크 (retryCount.canRetry())

---

#### Aggregate: FileProcessingJob

**핵심 개념**: 파일 타입별 가공 작업 관리

**속성** (VO 적용):
- `jobId`: JobId (UUID v7 VO)
- `fileId`: FileId (FK, File UUID VO)
- `jobType`: JobType (Enum)
- `status`: JobStatus (Enum)
- `retryCount`: **RetryCount** (VO)
- `inputS3Key`: String (원본 파일 S3 Key)
- `outputS3Key`: String (Nullable, 가공된 파일 S3 Key)
- `errorMessage`: String (Nullable, 에러 메시지)
- `createdAt`: LocalDateTime
- `processedAt`: LocalDateTime (Nullable)

**비즈니스 규칙**:

1. **가공 유형** (JobType Enum):
   - **이미지**: `THUMBNAIL_GENERATION`, `IMAGE_RESIZE`, `IMAGE_FORMAT_CONVERSION`, `OCR`
   - **HTML**: `HTML_PARSING`, `HTML_IMAGE_UPLOAD`, `HTML_TEXT_ANALYSIS`
   - **문서**: `DOCUMENT_TEXT_EXTRACTION`, `DOCUMENT_FORMAT_CONVERSION`
   - **엑셀**: `EXCEL_CSV_CONVERSION`, `EXCEL_DATA_EXTRACTION`

2. **가공 실패 처리**:
   - 자동 재시도: **최대 2회** (RetryCount VO에서 관리)
   - `retryCount.canRetry()`: true → 재시도 가능
   - 2회 재시도 후 실패 시: 상태를 `FAILED`로 변경

**도메인 메서드**:
- `markAsProcessing()`: PROCESSING 상태 전환
- `markAsCompleted(String outputS3Key)`: COMPLETED 상태 전환
- `markAsFailed(String errorMessage)`: FAILED 상태 전환
- `incrementRetryCount()`: 재시도 횟수 증가
- `canRetry()`: 재시도 가능 여부 체크 (retryCount.canRetry())

---

#### Aggregate: MessageOutbox

**핵심 개념**: 아웃박스 패턴을 통한 메시지 전송 신뢰성 보장

**속성** (VO 적용):
- `id`: OutboxId (Long PK VO)
- `eventType`: String (이벤트 타입)
- `aggregateId`: AggregateId (VO, File/Session/Job UUID)
- `payload`: String (JSON, 메시지 페이로드)
- `status`: OutboxStatus (Enum)
- `retryCount`: **RetryCount** (VO)
- `createdAt`: LocalDateTime
- `processedAt`: LocalDateTime (Nullable)

**비즈니스 규칙**:

1. **재시도 전략**:
   - 최대 재시도 횟수: **3회** (RetryCount VO에서 관리)
   - `retryCount.canRetry()`: true → 재시도 가능
   - Exponential Backoff: 1초, 2초, 4초

**도메인 메서드**:
- `markAsSent()`: SENT 상태 전환
- `markAsFailed()`: FAILED 상태 전환
- `incrementRetryCount()`: 재시도 횟수 증가
- `canRetry()`: 재시도 가능 여부 체크 (retryCount.canRetry())

---

### 향후 확장: Tenant/Organization 구조

**목적**: 멀티테넌시 지원 및 조직별 할당량 관리

**구조**:
```
Tenant (테넌트)
  ├─ dailyUploadQuota (일일 업로드 할당량)
  ├─ storageQuota (저장소 할당량)
  └─ Organization (조직)
      ├─ permissions (권한 관리)
      └─ File (파일)
```

**향후 추가 예정 Aggregates**:

**Tenant Aggregate**:
- `tenantId`: TenantId
- `tenantName`: String
- `dailyUploadQuota`: DailyUploadQuota (일일 업로드 할당량)
- `storageQuota`: StorageQuota (저장소 할당량)
- `status`: TenantStatus (ACTIVE, SUSPENDED)

**Organization Aggregate** (참고: `legacy/domain/iam/organization/Organization.java`):
- `organizationId`: OrganizationId
- `tenantId`: TenantId (FK, Long FK 전략)
- `orgCode`: OrgCode
- `name`: String
- `permissions`: List<Permission> (권한 목록)
- `status`: OrganizationStatus (ACTIVE, INACTIVE)

**현재 설계 반영**:
- ✅ **UploadSession**에 `tenantId` 필드 포함 (Nullable)
- ✅ **DownloadSession**에 `tenantId` 필드 포함 (Nullable)
- ✅ **File**에 `tenantId` 필드 포함 (Nullable)
- ⏳ **검증 로직 없음** (현재는 null 허용, 추후 FK 제약조건 추가)
- ⏳ **할당량 체크 로직 없음** (추후 확장)

**참고 문서**:
- `legacy/domain/src/main/java/com/ryuqq/fileflow/domain/iam/organization/Organization.java`

---

### 2. Application Layer

#### Command UseCase

**A. GeneratePresignedUrlUseCase** (Presigned URL 발급) - **세션 기반 재설계**

**Input**: `GeneratePresignedUrlCommand(sessionId, fileName, fileSize, mimeType, uploaderId, tenantId, category, tags, checksum)`

**Output**: `PresignedUrlResponse(sessionId, fileId, presignedUrl, expiresIn, uploadType, multipartUploadId)`

**Transaction 경계**:
1. UploadSession 조회 (sessionId로) - **멱등성 체크** ← **트랜잭션 안**
2. 기존 세션 있으면:
   - 상태 확인 → `INITIATED` 또는 `IN_PROGRESS`: 기존 URL 반환 (멱등성 보장)
   - 상태 확인 → `EXPIRED` 또는 `FAILED`: 에러 반환
3. 기존 세션 없으면:
   - UploadSession 생성 (`INITIATED` 상태)
   - File 메타데이터 생성 (`PENDING` 상태)
4. **트랜잭션 커밋**
5. S3 Presigned URL 발급 (단일/멀티파트 분기) ← **트랜잭션 밖**
6. **트랜잭션 시작**
7. UploadSession 업데이트 (`presignedUrl`, `multipartUploadId` 저장)
8. UploadSession 상태를 `IN_PROGRESS`로 변경
9. **트랜잭션 커밋**

**비즈니스 로직**:
1. **멱등성 체크**: `sessionId`로 기존 UploadSession 조회
2. **VO 검증**:
   - `FileName.of(fileName)` → 파일명 검증
   - `FileSize.of(fileSize)` → 파일 크기 검증 (0 < size <= 1GB)
   - `MimeType.of(mimeType)` → MIME 타입 검증 (허용 목록)
   - `FileCategory.of(category)` → 카테고리 검증
   - `Tags.of(tags)` → 태그 검증 (최대 10개)
3. **업로드 전략 자동 결정**:
   - `UploadType.determineBySize(fileSize)` → SINGLE/MULTIPART
4. **UploadSession 생성**:
   - `sessionId`, `fileName`, `fileSize`, `mimeType`, `uploadType`, `expiresAt` (5분 후)
5. **S3 Presigned URL 발급** (트랜잭션 밖):
   - **SINGLE**: `s3Client.generatePresignedUrl(s3Key, 5분)`
   - **MULTIPART**: `s3Client.initiateMultipartUpload(s3Key)` → `MultipartUpload` VO 생성

---

**B. CompleteUploadUseCase** (업로드 완료 처리) - **세션 검증 추가**

**Input**: `CompleteUploadCommand(sessionId, checksum)`

**Output**: `FileResponse(sessionId, fileId, status, s3Url, cdnUrl)`

**Transaction 경계**:
1. UploadSession 조회 (sessionId로) ← **트랜잭션 안**
2. 세션 상태 검증: `IN_PROGRESS`만 허용
3. File 조회 (UploadSession.fileId로)
4. **트랜잭션 커밋**
5. S3 Object 존재 여부 확인 (S3 HEAD 요청) ← **트랜잭션 밖**
6. 멀티파트 업로드인 경우: S3 Complete Multipart Upload ← **트랜잭션 밖**
7. 체크섬 검증 (클라이언트 vs S3 ETag) ← **트랜잭션 밖**
8. **트랜잭션 시작**
9. File 상태를 `COMPLETED`로 업데이트, ETag 저장
10. UploadSession 상태를 `COMPLETED`로 업데이트
11. **트랜잭션 커밋**

---

**C. UploadFromExternalUrlUseCase** (외부 URL 다운로드 후 업로드) - **다운로드 세션 추가**

**Input**: `UploadFromExternalUrlCommand(sessionId, externalUrl, uploaderId, tenantId, category, tags, webhookUrl)`

**Output**: `FileResponse(sessionId, fileId, status)` (비동기, 즉시 반환)

**Transaction 경계**:
1. DownloadSession 조회 (sessionId 또는 externalUrl 해시로) - **중복 다운로드 체크** ← **트랜잭션 안**
2. 기존 세션 있으면: 상태에 따라 기존 File 반환 또는 진행 중 상태 반환
3. 기존 세션 없으면:
   - `ExternalUrl.of(externalUrl)` → HTTPS 검증
   - DownloadSession 생성 (`INITIATED` 상태)
   - MessageOutbox 생성 (`FILE_DOWNLOAD_REQUESTED` 이벤트)
4. **트랜잭션 커밋**
5. 애프터 커밋 리스너: SQS에 메시지 전송 ← **트랜잭션 밖**

**백그라운드 작업 (SQS Consumer)**:
1. 외부 URL에서 파일 다운로드 (60초 타임아웃)
2. UploadSession 생성 (다운로드 파일 → S3 업로드용)
3. S3 Multipart Upload
4. DownloadSession에 `uploadSessionId` 저장
5. Webhook 전송

---

**D. ProcessFileUseCase** (파일 가공 요청):

**Input**: `ProcessFileCommand(fileId, jobTypes)`

**Output**: `List<FileProcessingJobResponse>`

**비즈니스 로직**:
1. File 조회 (COMPLETED 상태만 가공 가능)
2. FileProcessingJob Entity 생성 (각 jobType마다, RetryCount.forJob() 사용)
3. MessageOutbox 생성 + 애프터 커밋 리스너 → SQS 전송

---

#### Query UseCase

**E. GetFileUseCase** (파일 조회):
- File 조회 (Soft Delete 제외)
- FileProcessingJob 목록 조회

**F. ListFilesUseCase** (파일 목록 조회):
- Cursor-based Pagination
- 필터: uploaderId, status, category

---

### 3. Persistence Layer

#### A. JPA Entity

**FileJpaEntity**:
- **필드**:
  - `file_name`: String
  - `file_size`: Long
  - `mime_type`: String
  - `category`: String
  - `tags`: String (JSON)
  - `tenant_id`: Long (Nullable)
  - `checksum_algorithm`: String (Nullable)
  - `checksum_value`: String (Nullable)
  - `etag`: String (Nullable)
  - `retry_count`: Integer
  - `max_retry_count`: Integer

**UploadSessionJpaEntity**:
- **테이블**: `upload_sessions`
- **필드**:
  - `session_id`: String (UUID v7, Unique)
  - `tenant_id`: Long (Nullable)
  - `file_name`: String
  - `file_size`: Long
  - `mime_type`: String
  - `upload_type`: String (SINGLE/MULTIPART)
  - `multipart_upload_id`: String (Nullable)
  - `multipart_status`: String (Nullable)
  - `total_parts`: Integer (Nullable)
  - `uploaded_parts`: String (JSON, Nullable)
  - `checksum_algorithm`: String (Nullable)
  - `checksum_value`: String (Nullable)
  - `etag`: String (Nullable)
  - `presigned_url`: TEXT
  - `expires_at`: LocalDateTime
  - `status`: String
- **인덱스**:
  - `(status, expires_at)` - 만료된 세션 정리
  - `(tenant_id, created_at DESC)` - 테넌트별 세션 조회

**DownloadSessionJpaEntity**:
- **테이블**: `download_sessions`
- **필드**:
  - `session_id`: String (UUID v7, Unique)
  - `external_url`: TEXT
  - `external_url_hash`: VARCHAR(64) - SHA-256 해시 (중복 체크)
  - `tenant_id`: Long (Nullable)
  - `upload_session_id`: String (Nullable)
  - `status`: String
  - `retry_count`: Integer
  - `max_retry_count`: Integer
  - `expires_at`: LocalDateTime
- **인덱스**:
  - `(external_url_hash, created_at DESC)` - 중복 다운로드 체크
  - `(status, expires_at)` - 만료된 세션 정리

**FileProcessingJobJpaEntity**:
- **필드**:
  - `retry_count`: Integer
  - `max_retry_count`: Integer (Default: 2)

**MessageOutboxJpaEntity**:
- **필드**:
  - `retry_count`: Integer
  - `max_retry_count`: Integer (Default: 3)

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
    tenant_id BIGINT,
    category VARCHAR(100),
    tags JSON,
    checksum_algorithm VARCHAR(20),
    checksum_value VARCHAR(100),
    etag VARCHAR(100),
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 3,
    version INT NOT NULL DEFAULT 1,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_uploader_status_created (uploader_id, status, created_at DESC),
    INDEX idx_tenant_created (tenant_id, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V2__create_upload_sessions_table.sql**:
```sql
CREATE TABLE upload_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    tenant_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    upload_type VARCHAR(20) NOT NULL,
    multipart_upload_id VARCHAR(200),
    multipart_status VARCHAR(20),
    total_parts INT,
    uploaded_parts JSON,
    multipart_initiated_at DATETIME(6),
    multipart_completed_at DATETIME(6),
    multipart_aborted_at DATETIME(6),
    checksum_algorithm VARCHAR(20),
    checksum_value VARCHAR(100),
    etag VARCHAR(100),
    presigned_url TEXT,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_status_expires (status, expires_at),
    INDEX idx_tenant_created (tenant_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V3__create_download_sessions_table.sql**:
```sql
CREATE TABLE download_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    external_url TEXT NOT NULL,
    external_url_hash VARCHAR(64) NOT NULL,
    tenant_id BIGINT,
    upload_session_id VARCHAR(36),
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 3,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_url_hash_created (external_url_hash, created_at DESC),
    INDEX idx_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**V4__update_processing_jobs_table.sql**:
```sql
ALTER TABLE file_processing_jobs
ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
ADD COLUMN max_retry_count INT NOT NULL DEFAULT 2;
```

**V5__update_message_outbox_table.sql**:
```sql
ALTER TABLE message_outbox
ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
ADD COLUMN max_retry_count INT NOT NULL DEFAULT 3;
```

---

### 4. REST API Layer

#### A. API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/files/presigned-url | Presigned URL 발급 (세션 기반) | GeneratePresignedUrlRequest | PresignedUrlResponse | 201 Created |
| POST | /api/v1/files/upload-complete | 업로드 완료 알림 (세션 검증) | CompleteUploadRequest | FileResponse | 200 OK |
| POST | /api/v1/files/from-external-url | 외부 URL 다운로드 요청 (세션 기반) | UploadFromExternalUrlRequest | FileResponse | 202 Accepted |
| GET | /api/v1/files/{fileId} | 파일 조회 | - | FileDetailResponse | 200 OK |
| GET | /api/v1/files | 파일 목록 조회 | ListFilesRequest (Query Params) | CursorPageResponse<FileSummaryResponse> | 200 OK |

---

#### B. Request/Response DTO

**GeneratePresignedUrlRequest**:
```java
public record GeneratePresignedUrlRequest(
    @NotBlank String sessionId, // 멱등키 (UUID v7)
    @NotBlank String fileName,
    @NotNull @Min(1) @Max(1073741824) Long fileSize,
    @NotBlank String mimeType,
    @NotNull Long uploaderId,
    Long tenantId, // Nullable
    String category, // Nullable
    List<String> tags, // Nullable
    String checksumAlgorithm, // Nullable (SHA-256, MD5)
    String checksumValue // Nullable
) {}
```

**PresignedUrlResponse**:
```java
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn, // 초 단위 (300초 = 5분)
    String uploadType, // SINGLE, MULTIPART
    String multipartUploadId // Nullable (MULTIPART일 때만)
) {}
```

**CompleteUploadRequest**:
```java
public record CompleteUploadRequest(
    @NotBlank String sessionId,
    String checksumAlgorithm, // Nullable
    String checksumValue // Nullable
) {}
```

---

## 🚀 개발 계획

### Phase 1: VO 중심 Domain Layer 재설계 (예상: 5일)

**Week 1 (Domain Layer)**:
- [ ] Value Objects 구현 (FileName, FileSize, MimeType, FileCategory, Tags, Checksum, ETag, RetryCount 등)
- [ ] UploadSession Aggregate 구현
- [ ] DownloadSession Aggregate 구현
- [ ] File Aggregate 수정 (VO 적용)
- [ ] FileProcessingJob, MessageOutbox 수정 (RetryCount VO 적용)
- [ ] MultipartUpload VO 구현
- [ ] Domain Unit Test (TestFixture 패턴)

---

### Phase 2: 세션 기반 Application Layer (예상: 7일)

**Week 2 (Application Layer)**:
- [ ] GeneratePresignedUrlUseCase 재구현 (세션 기반, 멱등성)
- [ ] CompleteUploadUseCase 재구현 (세션 검증)
- [ ] UploadFromExternalUrlUseCase 재구현 (다운로드 세션)
- [ ] ProcessFileUseCase 재구현 (RetryCount VO)
- [ ] Application Unit Test (Mock 사용)

---

### Phase 3: Persistence Layer (예상: 5일)

**Week 3 (Persistence Layer)**:
- [ ] UploadSessionJpaEntity 구현
- [ ] DownloadSessionJpaEntity 구현
- [ ] FileJpaEntity 수정 (VO 매핑)
- [ ] Flyway Migration (V1-V5)
- [ ] Repository 구현
- [ ] Integration Test (TestContainers MySQL)

---

### Phase 4: REST API Layer (예상: 3일)

**Week 4 (REST API Layer)**:
- [ ] FileApiController 수정 (세션 기반 API)
- [ ] Request/Response DTO 수정
- [ ] Integration Test (TestRestTemplate)
- [ ] E2E Test (세션 기반 플로우)

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

**변경 이력**:
- 2025-01-14: 초안 작성 (ryu-qqq)
- 2025-01-17: **재설계** - VO 확장 (FileName, FileSize, MimeType, FileCategory, Tags, RetryCount 등) + 세션 기반 아키텍처 (UploadSession, DownloadSession) 추가 (ryu-qqq)
