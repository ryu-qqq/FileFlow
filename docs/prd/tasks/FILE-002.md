# FILE-002: Application Layer 구현

**Epic**: File Management System
**Layer**: Application Layer
**브랜치**: feature/FILE-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 관리 시스템의 UseCase를 구현합니다. Transaction 경계를 엄격히 관리하고, 외부 API 호출은 트랜잭션 밖에서 처리합니다. 아웃박스 패턴을 통해 메시지 전송 신뢰성을 보장합니다.

---

## 🎯 요구사항

### Command UseCase

#### A. GeneratePresignedUrlUseCase

- [ ] **Input/Output 정의**
  - Input: `GeneratePresignedUrlCommand(fileName, fileSize, mimeType, uploaderId, category, tags)`
  - Output: `PresignedUrlResponse(fileId, presignedUrl, expiresIn, s3Key)`

- [ ] **비즈니스 로직**
  - 파일 크기 검증 (최대 1GB)
  - MIME 타입 검증 (허용 목록)
  - File Entity 생성 (UUID v7, PENDING 상태)
  - 파일 크기별 업로드 전략 결정:
    - < 100MB: 단일 업로드 URL
    - ≥ 100MB: Multipart Upload Initiate URL

- [ ] **Transaction 경계**
  - File 메타데이터 DB 저장 (트랜잭션 안)
  - 트랜잭션 커밋
  - S3 Presigned URL 발급 (트랜잭션 밖, Timeout 3초, 재시도 3회)
  - S3 API 실패 시: File 상태를 FAILED로 변경 (보상 트랜잭션)

#### B. CompleteUploadUseCase

- [ ] **Input/Output 정의**
  - Input: `CompleteUploadCommand(fileId)`
  - Output: `FileResponse(fileId, status, s3Url, cdnUrl)`

- [ ] **비즈니스 로직**
  - File 조회 (fileId로)
  - 현재 상태 검증 (PENDING 또는 UPLOADING만 허용)
  - S3 Object 존재 여부 확인 (HEAD 요청)
  - 존재하면: 상태를 COMPLETED로 변경
  - 파일 가공 작업 등록 (MessageOutbox 생성, FILE_UPLOADED 이벤트)

- [ ] **Transaction 경계**
  - S3 Object HEAD 요청 (트랜잭션 밖, Timeout 3초, 재시도 3회)
  - S3 Object 존재 확인 → 트랜잭션 시작
  - File 상태 업데이트 + MessageOutbox 생성
  - 트랜잭션 커밋
  - S3 Object 없으면: 예외 발생 + File 상태를 FAILED로 변경

#### C. UploadFromExternalUrlUseCase

- [ ] **Input/Output 정의**
  - Input: `UploadFromExternalUrlCommand(externalUrl, uploaderId, category, tags, webhookUrl)`
  - Output: `FileResponse(fileId, status)` (비동기, 즉시 반환)

- [ ] **비즈니스 로직**
  - 외부 URL 검증 (HTTPS만 허용)
  - File Entity 생성 (UUID v7, PENDING 상태)
  - MessageOutbox 생성 (FILE_DOWNLOAD_REQUESTED 이벤트)
  - 애프터 커밋 리스너 → SQS 메시지 전송

- [ ] **Transaction 경계**
  - File 메타데이터 + MessageOutbox 생성 (트랜잭션 안)
  - 트랜잭션 커밋
  - 애프터 커밋 리스너: SQS 전송 (트랜잭션 밖)

- [ ] **백그라운드 작업 (SQS Consumer)**
  - 외부 URL 다운로드 (Timeout 60초, 재시도 3회, 트랜잭션 밖)
  - 파일 크기 체크 (1GB 초과 시 에러)
  - S3 업로드 (Multipart Upload, 트랜잭션 밖)
  - File 상태 업데이트 (COMPLETED, 트랜잭션 안)
  - Webhook 전송 (HMAC 서명, Timeout 3초, 재시도 3회, 트랜잭션 밖)

#### D. ProcessFileUseCase

- [ ] **Input/Output 정의**
  - Input: `ProcessFileCommand(fileId, jobTypes)`
  - Output: `List<FileProcessingJobResponse>`

- [ ] **비즈니스 로직**
  - File 조회 (COMPLETED 상태만 가공 가능)
  - FileProcessingJob Entity 생성 (각 jobType마다)
  - MessageOutbox 생성 (FILE_PROCESSING_REQUESTED 이벤트)
  - 애프터 커밋 리스너 → SQS 메시지 전송

- [ ] **Transaction 경계**
  - File 조회 + FileProcessingJob 생성 + MessageOutbox 생성 (트랜잭션 안)
  - 트랜잭션 커밋
  - 애프터 커밋 리스너: SQS 전송 (트랜잭션 밖)

- [ ] **백그라운드 작업 (SQS Consumer)**
  - S3 원본 파일 다운로드 (트랜잭션 밖)
  - 파일 가공 (썸네일, OCR, 변환 등, 트랜잭션 밖)
  - 가공 파일 S3 업로드 (트랜잭션 밖)
  - FileProcessingJob 상태 업데이트 (COMPLETED, outputS3Key 저장, 트랜잭션 안)
  - CDN 조건 체크: 상품 이미지/HTML이면 CDN 업로드 (트랜잭션 밖)

### Query UseCase

#### E. GetFileUseCase

- [ ] **Input/Output 정의**
  - Input: `GetFileQuery(fileId)`
  - Output: `FileDetailResponse`

- [ ] **비즈니스 로직**
  - File 조회 (Soft Delete 제외)
  - FileProcessingJob 목록 조회 (fileId로)
  - Response DTO 조합

- [ ] **Transaction**: ReadOnly

#### F. ListFilesUseCase

- [ ] **Input/Output 정의**
  - Input: `ListFilesQuery(uploaderId, status, category, cursor, size)`
  - Output: `CursorPageResponse<FileSummaryResponse>`

- [ ] **비즈니스 로직**
  - Cursor-based Pagination (createdAt 기준)
  - 사용자별, 상태별, 카테고리별 필터링

- [ ] **Transaction**: ReadOnly

### Port 정의 (Out)

#### Command Port

- [ ] **FileCommandPort**
  - `save(File file): File`
  - `saveAll(List<File> files): List<File>`
  - `updateStatus(String fileId, FileStatus status): void`
  - `softDelete(String fileId): void`

- [ ] **FileProcessingJobCommandPort**
  - `save(FileProcessingJob job): FileProcessingJob`
  - `saveAll(List<FileProcessingJob> jobs): List<FileProcessingJob>`
  - `updateStatus(String jobId, JobStatus status): void`

- [ ] **MessageOutboxCommandPort**
  - `save(MessageOutbox outbox): MessageOutbox`

#### Query Port

- [ ] **FileQueryPort**
  - `findById(String fileId): Optional<File>`
  - `findByIdWithLock(String fileId): Optional<File>` (Optimistic Lock)
  - `findByUploaderIdAndStatusWithCursor(...)`: `CursorPageResponse<File>`
  - `existsByFileId(String fileId): boolean`

- [ ] **FileProcessingJobQueryPort**
  - `findByFileId(String fileId): List<FileProcessingJob>`
  - `findById(String jobId): Optional<FileProcessingJob>`

- [ ] **MessageOutboxQueryPort**
  - `findPendingMessages(int limit): List<MessageOutbox>`

#### 외부 API Port

- [ ] **S3ClientPort**
  - `generatePresignedUrl(String s3Key, int expiresIn): String`
  - `initiateMultipartUpload(String s3Key): String` (Upload ID 반환)
  - `headObject(String s3Key): boolean` (존재 여부)
  - `uploadFromUrl(String externalUrl, String s3Key): void`

- [ ] **SqsClientPort**
  - `sendMessage(String queueUrl, String message): void`
  - `sendMessageBatch(String queueUrl, List<String> messages): void`

- [ ] **WebhookClientPort**
  - `send(String webhookUrl, String payload, String signature): void`

### 아웃박스 패턴 구현

- [ ] **TransactionalEventListener 구현**
  - `@TransactionalEventListener(phase = AFTER_COMMIT)`
  - MessageOutbox PENDING 메시지를 SQS로 전송
  - 성공 시: MessageOutbox 상태를 SENT로 업데이트
  - 실패 시: 로그 기록 (폴백 스케줄러가 재시도)

- [ ] **폴백 스케줄러 구현**
  - `@Scheduled(fixedDelay = 60000)` (1분마다)
  - PENDING 상태의 MessageOutbox 조회 (createdAt < 1분 전)
  - SQS로 전송 시도
  - 성공 시: MessageOutbox 상태를 SENT로 업데이트
  - 실패 시: retryCount 증가, maxRetryCount 초과 시 FAILED로 변경

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Command/Query 분리 (CQRS)**
  - Command UseCase는 Port In Command 구현
  - Query UseCase는 Port In Query 구현
  - Command와 Query DTO 분리

- [ ] **Transaction 경계 엄격 관리**
  - @Transactional 내 외부 API 호출 절대 금지
  - S3, SQS, Webhook 호출은 트랜잭션 밖
  - 외부 API 호출 전/후 트랜잭션 분리

- [ ] **아웃박스 패턴 필수**
  - 메시지 전송은 MessageOutbox 통해서만
  - 애프터 커밋 리스너 + 폴백 스케줄러 필수
  - 재시도 전략 (최대 3회, Exponential Backoff)

- [ ] **Lombok 금지**
  - Command/Query DTO는 Record 사용
  - Response DTO는 Record 사용

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Application Layer는 Domain에만 의존
  - @Transactional 내 외부 API 호출 금지 검증
  - Command/Query 분리 검증

- [ ] **Unit Test (Mock 사용)**
  - Port는 Mock 처리
  - 비즈니스 로직만 테스트
  - 테스트 커버리지 > 80%

- [ ] **Integration Test**
  - 아웃박스 패턴 검증 (애프터 커밋 리스너)
  - 폴백 스케줄러 검증

---

## ✅ 완료 조건

- [ ] 4개 Command UseCase 구현 완료
- [ ] 2개 Query UseCase 구현 완료
- [ ] 3개 Command Port 정의 (File, FileProcessingJob, MessageOutbox)
- [ ] 3개 Query Port 정의
- [ ] 3개 외부 API Port 정의 (S3, SQS, Webhook)
- [ ] 아웃박스 패턴 구현 (애프터 커밋 리스너 + 폴백 스케줄러)
- [ ] Unit Test 커버리지 > 80%
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/file-management-system.md
- **Plan**: docs/prd/plans/FILE-002-application-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **컨벤션**: docs/coding_convention/03-application-layer/

---

## 📝 참고사항

### Transaction 경계 예시
```java
@Service
public class GeneratePresignedUrlService implements GeneratePresignedUrlUseCase {

    @Transactional
    public PresignedUrlResponse execute(GeneratePresignedUrlCommand command) {
        // 1. 트랜잭션 안: File 메타데이터 생성
        File file = File.create(...);
        fileCommandPort.save(file);
        // 2. 트랜잭션 커밋 (자동)
    }

    // 3. 트랜잭션 밖: S3 API 호출 (별도 메서드)
    private String generatePresignedUrlFromS3(String s3Key) {
        try {
            return s3ClientPort.generatePresignedUrl(s3Key, 300); // 5분
        } catch (S3Exception e) {
            // 4. 보상 트랜잭션: File 상태를 FAILED로 변경
            updateFileStatusToFailed(file.getFileId());
            throw new PresignedUrlGenerationException(e);
        }
    }
}
```

### 아웃박스 패턴 예시
```java
// 1. UseCase에서 MessageOutbox 생성
@Transactional
public FileResponse execute(CompleteUploadCommand command) {
    File file = fileQueryPort.findById(command.fileId()).orElseThrow();
    file.markAsCompleted();

    MessageOutbox outbox = MessageOutbox.create(
        "FILE_UPLOADED",
        file.getFileId(),
        toJson(file)
    );
    messageOutboxCommandPort.save(outbox);

    return FileResponse.from(file);
}

// 2. 애프터 커밋 리스너
@TransactionalEventListener(phase = AFTER_COMMIT)
public void handleFileUploaded(FileUploadedEvent event) {
    try {
        sqsClientPort.sendMessage(queueUrl, event.toJson());
        messageOutboxCommandPort.updateStatus(event.outboxId(), SENT);
    } catch (Exception e) {
        // 로그만 기록, 폴백 스케줄러가 재시도
        log.error("Failed to send message", e);
    }
}

// 3. 폴백 스케줄러
@Scheduled(fixedDelay = 60000)
public void retryPendingMessages() {
    List<MessageOutbox> pendingMessages =
        messageOutboxQueryPort.findPendingMessages(100);

    for (MessageOutbox outbox : pendingMessages) {
        try {
            sqsClientPort.sendMessage(queueUrl, outbox.getPayload());
            outbox.markAsSent();
        } catch (Exception e) {
            outbox.incrementRetryCount();
            if (!outbox.canRetry()) {
                outbox.markAsFailed();
            }
        }
        messageOutboxCommandPort.save(outbox);
    }
}
```
