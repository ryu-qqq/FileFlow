# File Processing Bounded Context

**Bounded Context**: `file/processing`
**Dependencies**: `session/single` (File Aggregate), `messaging/outbox` (이벤트 발행)
**예상 기간**: 3일
**우선순위**: Level 3 (Level 2 완료 후)

---

## 📋 개요

**목적**: 업로드된 파일에 대한 비동기 후처리(이미지 리사이징, 썸네일 생성, 메타데이터 추출 등)를 수행합니다.

**핵심 문제 해결**:
- **성능 최적화**: 업로드 응답 속도와 무관하게 비동기 처리
- **확장성**: 다양한 파일 타입별 처리 로직 추가 가능
- **안정성**: 처리 실패 시 재시도 및 에러 추적

**사용 사례**:
- 이미지 썸네일 자동 생성 (원본, 중간, 작은 크기)
- 이미지 메타데이터 추출 (크기, 해상도, EXIF)
- 이미지 포맷 변환 (WebP, AVIF)
- 동영상 썸네일 추출 (Future)

---

## 🎯 주요 기능

### In Scope
1. **FileProcessingJob Aggregate** - 파일 처리 작업 생명주기 관리
2. **이미지 리사이징** - 3가지 크기 (원본, 중간 512px, 썸네일 128px)
3. **메타데이터 추출** - 이미지 크기, 해상도, EXIF 정보
4. **S3 업로드** - 처리된 파일 S3 저장 (원본과 별도 경로)
5. **재시도 로직** - 처리 실패 시 exponential backoff (최대 3회)

### Out of Scope (Future)
- 동영상 처리 (썸네일 추출, 인코딩)
- AI 기반 처리 (객체 인식, 얼굴 감지)
- 워터마크 추가
- PDF 변환

---

## 🏗️ Domain Layer

### Aggregates

#### 1. FileProcessingJob
**책임**: 파일 처리 작업 생명주기 관리

**주요 메서드**:
```java
public class FileProcessingJob {
    private JobId jobId;                    // UUID v7
    private FileId fileId;                  // 처리할 파일
    private ProcessingType processingType;  // IMAGE_RESIZE, THUMBNAIL, METADATA_EXTRACT
    private JobStatus status;               // PENDING, PROCESSING, COMPLETED, FAILED
    private int retryCount;                 // 재시도 횟수
    private String errorMessage;            // 실패 시 에러 메시지
    private ProcessingResult result;        // 처리 결과 (JSON)
    private LocalDateTime scheduledAt;      // 처리 예정 시각
    private LocalDateTime completedAt;      // 처리 완료 시각

    public static FileProcessingJob create(
        FileId fileId,
        ProcessingType processingType,
        Clock clock
    );

    public void startProcessing(Clock clock);
    public void markAsCompleted(ProcessingResult result, Clock clock);
    public void markAsFailed(String errorMessage, Clock clock);
    public void scheduleRetry(Clock clock);  // Exponential backoff
    public boolean isRetryable();            // 최대 3회 재시도
}
```

### Value Objects

#### ProcessingResult
```java
public record ProcessingResult(
    Map<String, String> processedFiles,     // "thumbnail" -> "s3://..."
    ImageMetadata metadata,
    LocalDateTime processedAt
) {
    public static ProcessingResult from(
        Map<String, String> processedFiles,
        ImageMetadata metadata,
        Clock clock
    ) {
        return new ProcessingResult(
            processedFiles,
            metadata,
            LocalDateTime.now(clock)
        );
    }
}
```

#### ImageMetadata
```java
public record ImageMetadata(
    int width,
    int height,
    String format,                          // JPEG, PNG, WebP
    long fileSize,
    int dpi,
    String colorSpace,                      // RGB, CMYK
    Map<String, String> exif                // EXIF 정보
) {
    public static ImageMetadata extract(BufferedImage image) {
        // 이미지 메타데이터 추출 로직
    }
}
```

### Enums

#### ProcessingType
- `IMAGE_RESIZE`: 이미지 리사이징 (3가지 크기)
- `THUMBNAIL`: 썸네일만 생성
- `METADATA_EXTRACT`: 메타데이터만 추출

#### JobStatus
- `PENDING`: 처리 대기
- `PROCESSING`: 처리 중
- `COMPLETED`: 처리 완료
- `FAILED`: 처리 실패 (최종)

---

## 📦 Application Layer

### Use Cases

#### 1. ScheduleFileProcessingUseCase (Event Listener)
**책임**: FILE_UPLOADED 이벤트 수신 시 처리 작업 생성

```java
@Component
public class FileUploadedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onFileUploaded(FileUploadedEvent event) {
        // 1. 파일 타입 확인
        File file = fileQueryPort.findById(event.fileId());
        if (!isImageFile(file.mimeType())) {
            return; // 이미지가 아니면 처리 안 함
        }

        // 2. 처리 작업 생성
        FileProcessingJob job = FileProcessingJob.create(
            event.fileId(),
            ProcessingType.IMAGE_RESIZE,
            clock
        );
        fileProcessingJobPersistencePort.save(job);

        // 3. 백그라운드 작업 예약
        fileProcessingExecutor.scheduleProcessing(job.jobId());
    }

    private boolean isImageFile(MimeType mimeType) {
        return mimeType.value().startsWith("image/");
    }
}
```

#### 2. ExecuteFileProcessingJob (Background Worker)
**책임**: 실제 파일 처리 수행

```java
@Component
public class FileProcessingExecutor {

    @Async
    public void executeProcessing(JobId jobId) {
        try {
            // 1. 트랜잭션: Job 조회 및 상태 변경
            FileProcessingJob job = fileProcessingJobQueryPort.findById(jobId);
            job.startProcessing(clock);
            fileProcessingJobPersistencePort.update(job);

            // 2. 트랜잭션 밖: 원본 파일 다운로드 (S3)
            File file = fileQueryPort.findById(job.fileId());
            InputStream originalStream = s3ClientPort.download(file.s3Bucket(), file.s3Key());
            BufferedImage originalImage = ImageIO.read(originalStream);

            // 3. 트랜잭션 밖: 이미지 처리
            Map<String, BufferedImage> processedImages = processImage(originalImage);
            //   - "original": 원본 (변환 없음)
            //   - "medium": 512px
            //   - "thumbnail": 128px

            // 4. 트랜잭션 밖: 처리된 이미지 S3 업로드
            Map<String, String> s3Keys = new HashMap<>();
            for (Map.Entry<String, BufferedImage> entry : processedImages.entrySet()) {
                S3Key s3Key = generateProcessedS3Key(file.fileId(), entry.getKey());
                s3ClientPort.uploadImage(s3Bucket, s3Key, entry.getValue());
                s3Keys.put(entry.getKey(), s3Key.value());
            }

            // 5. 메타데이터 추출
            ImageMetadata metadata = ImageMetadata.extract(originalImage);

            // 6. 트랜잭션: Job 완료 처리
            ProcessingResult result = ProcessingResult.from(s3Keys, metadata, clock);
            job.markAsCompleted(result, clock);
            fileProcessingJobPersistencePort.update(job);

        } catch (Exception e) {
            // 7. 트랜잭션: 실패 처리 및 재시도
            FileProcessingJob job = fileProcessingJobQueryPort.findById(jobId);
            job.markAsFailed(e.getMessage(), clock);

            if (job.isRetryable()) {
                job.scheduleRetry(clock);
                fileProcessingExecutor.scheduleProcessing(jobId, job.scheduledAt());
            }

            fileProcessingJobPersistencePort.update(job);
        }
    }

    private Map<String, BufferedImage> processImage(BufferedImage original) {
        Map<String, BufferedImage> result = new HashMap<>();
        result.put("original", original);
        result.put("medium", resizeImage(original, 512));
        result.put("thumbnail", resizeImage(original, 128));
        return result;
    }

    private BufferedImage resizeImage(BufferedImage original, int targetSize) {
        // 이미지 리사이징 로직 (Thumbnailator 라이브러리 사용)
        return Thumbnails.of(original)
            .size(targetSize, targetSize)
            .keepAspectRatio(true)
            .asBufferedImage();
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V7__create_file_processing_jobs_table.sql
```sql
CREATE TABLE file_processing_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL UNIQUE,
    file_id VARCHAR(36) NOT NULL,
    processing_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    result JSON,
    scheduled_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_job_id (job_id),
    INDEX idx_file_id (file_id),
    INDEX idx_status_scheduled (status, scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📊 Storage Strategy

### S3 Key Pattern

**원본 파일**:
```
uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg
```

**처리된 파일**:
```
processed/1/admin/connectly/banner/01JD8001/original.jpg
processed/1/admin/connectly/banner/01JD8001/medium.jpg
processed/1/admin/connectly/banner/01JD8001/thumbnail.jpg
```

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| GET | /api/v1/files/{fileId}/processing/status | 처리 작업 상태 조회 | 200 OK |
| GET | /api/v1/files/{fileId}/processed | 처리된 파일 목록 조회 | 200 OK |

### Response Example

**GET /api/v1/files/{fileId}/processing/status (200 OK)**:
```json
{
  "jobId": "01JDB000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "processingType": "IMAGE_RESIZE",
  "status": "COMPLETED",
  "result": {
    "processedFiles": {
      "original": "processed/.../original.jpg",
      "medium": "processed/.../medium.jpg",
      "thumbnail": "processed/.../thumbnail.jpg"
    },
    "metadata": {
      "width": 1920,
      "height": 1080,
      "format": "JPEG",
      "fileSize": 1048576,
      "dpi": 72,
      "colorSpace": "RGB"
    }
  },
  "completedAt": "2025-11-18T10:31:30Z"
}
```

---

## 📊 Integration Points

### session/single 연동
- FILE_UPLOADED 이벤트 수신 (Domain Event)
- File Aggregate 조회하여 처리

### messaging/outbox 연동
- FILE_PROCESSED 이벤트 발행 (처리 완료 시)
- SQS로 외부 시스템에 알림

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] FILE_UPLOADED 이벤트 리스너 구현
- [ ] 이미지 리사이징 (3가지 크기)
- [ ] 메타데이터 추출 (크기, 해상도, EXIF)
- [ ] 처리된 파일 S3 업로드
- [ ] 재시도 로직 (exponential backoff, 최대 3회)
- [ ] 처리 상태 조회 API

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (TestContainers + LocalStack S3)
- [ ] ArchUnit Test 통과

### 성능 요구사항
- [ ] 1MB 이미지 처리 시간 < 5초 (P95)
- [ ] 동시 처리 최대 5개 (Thread Pool 제한)

---

## 🔗 의존성

### Upstream
- `session/single` - FILE_UPLOADED 이벤트 수신

### Downstream
- `messaging/outbox` - FILE_PROCESSED 이벤트 발행
- S3 Download/Upload API
- Thumbnailator (이미지 처리 라이브러리)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (file/processing Bounded Context)
