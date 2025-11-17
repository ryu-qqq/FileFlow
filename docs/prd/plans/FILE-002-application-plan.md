# FILE-002 TDD Plan

**Task**: Application Layer 구현
**Layer**: Application Layer
**브랜치**: feature/FILE-002-application
**예상 소요 시간**: 600분 (40 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: Port 정의 (6 사이클)

---

### 1️⃣ FilePersistencePort 정의 (Cycle 1) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `application/src/test/java/.../port/out/command/FilePersistencePortTest.java` 생성
- [x] Port 인터페이스 메서드 테스트 작성
  - `persist(File file)` 메서드 (Zero-Tolerance 규칙 준수)
  - 반환 타입: `FileId` (Value Object)
- [x] 컴파일 에러 확인 (인터페이스 없음)
- [x] 커밋: `test: FilePersistencePort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `application/src/main/java/.../port/out/command/FilePersistencePort.java` 생성
- [x] `persist(File file): FileId` 메서드 시그니처 정의
- [x] 테스트 실행 → 통과 확인
- [x] ArchUnit 테스트 자동 검증 (`PersistencePortArchTest.java`)
- [x] 커밋: `feat: FilePersistencePort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (메서드 설명, 파라미터, 반환값)
- [x] 테스트 여전히 통과 확인
- [x] 변경 사항 없음 (Javadoc 이미 GREEN에서 작성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### 2️⃣ FileQueryPort 정의 (Cycle 2) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `FileQueryPortTest.java` 생성
- [x] Port 인터페이스 메서드 테스트 작성 (Zero-Tolerance 규칙 준수)
  - `findById(FileId id): Optional<File>`
  - `existsById(FileId id): boolean`
  - `findByCriteria(FileSearchCriteria criteria): List<File>`
  - `countByCriteria(FileSearchCriteria criteria): long`
- [x] 컴파일 에러 확인 (FileSearchCriteria VO 미존재)
- [x] 커밋: `test: FileQueryPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `port/out/query/FileQueryPort.java` 생성
- [x] 4개 메서드 시그니처 정의
- [x] `FileSearchCriteria` VO 생성 (domain layer)
- [x] 테스트 실행 → 통과 확인
- [x] ArchUnit 테스트 자동 검증 (`QueryPortArchTest.java`)
- [x] 커밋: `feat: FileQueryPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (이미 GREEN에서 작성)
- [x] 테스트 여전히 통과 확인
- [x] 변경 사항 없음 (SKIP)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### 3️⃣ FileProcessingJobPort 정의 (Cycle 3)

> **Zero-Tolerance 규칙 준수**:
> - **PersistencePort**: `*PersistencePort` 네이밍, `persist()` 메서드만 사용
> - **QueryPort**: `*QueryPort` 네이밍, 4개 필수 메서드 (findById, existsById, findByCriteria, countByCriteria)
> - **금지**: `save()`, `update()`, `delete()` 메서드 사용 금지

#### 🔴 Red: 테스트 작성
- [x] `FileProcessingJobPersistencePortTest.java` 생성
  - [x] `persist(FileProcessingJob): FileProcessingJobId` 메서드 시그니처 검증
  - [x] Value Object 반환 타입 검증
- [x] `FileProcessingJobQueryPortTest.java` 생성
  - [x] 4개 필수 메서드 시그니처 검증: `findById()`, `existsById()`, `findByCriteria()`, `countByCriteria()`
  - [x] FileProcessingJobSearchCriteria VO 파라미터 검증
- [x] 커밋: `test: FileProcessingJobPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `port/out/command/FileProcessingJobPersistencePort.java` 생성
  - [x] `FileProcessingJobId persist(FileProcessingJob job)` 메서드
  - [x] Javadoc: 신규 생성과 수정 통합 처리
- [x] `port/out/query/FileProcessingJobQueryPort.java` 생성
  - [x] `Optional<FileProcessingJob> findById(FileProcessingJobId id)`
  - [x] `boolean existsById(FileProcessingJobId id)`
  - [x] `List<FileProcessingJob> findByCriteria(FileProcessingJobSearchCriteria criteria)`
  - [x] `long countByCriteria(FileProcessingJobSearchCriteria criteria)`
- [x] `domain/vo/FileProcessingJobSearchCriteria.java` 생성 (필요 시)
- [x] ArchUnit 테스트 자동 검증 (`PersistencePortArchTest.java`, `QueryPortArchTest.java`)
- [x] 커밋: `feat: FileProcessingJobPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (이미 GREEN에서 작성)
- [x] 테스트 여전히 통과 확인
- [x] 변경 사항 없음 (SKIP)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### 4️⃣ MessageOutboxPort 정의 (Cycle 4)

> **Zero-Tolerance 규칙 준수**:
> - **PersistencePort**: `*PersistencePort` 네이밍, `persist()` 메서드만 사용
> - **QueryPort**: `*QueryPort` 네이밍, 4개 필수 메서드 (findById, existsById, findByCriteria, countByCriteria)
> - **금지**: `save()`, `update()`, `delete()` 메서드 사용 금지

#### 🔴 Red: 테스트 작성
- [x] `MessageOutboxPersistencePortTest.java` 생성
  - [x] `persist(MessageOutbox): MessageOutboxId` 메서드 시그니처 검증
  - [x] Value Object 반환 타입 검증
- [x] `MessageOutboxQueryPortTest.java` 생성
  - [x] 4개 필수 메서드 시그니처 검증: `findById()`, `existsById()`, `findByCriteria()`, `countByCriteria()`
  - [x] MessageOutboxSearchCriteria VO 파라미터 검증
- [x] 커밋: `test: MessageOutboxPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `port/out/command/MessageOutboxPersistencePort.java` 생성
  - [x] `MessageOutboxId persist(MessageOutbox outbox)` 메서드
  - [x] Javadoc: 신규 생성과 수정 통합 처리
- [x] `port/out/query/MessageOutboxQueryPort.java` 생성
  - [x] `Optional<MessageOutbox> findById(MessageOutboxId id)`
  - [x] `boolean existsById(MessageOutboxId id)`
  - [x] `List<MessageOutbox> findByCriteria(MessageOutboxSearchCriteria criteria)`
  - [x] `long countByCriteria(MessageOutboxSearchCriteria criteria)`
- [x] `domain/vo/MessageOutboxSearchCriteria.java` 생성 (필요 시)
- [x] ArchUnit 테스트 자동 검증 (`PersistencePortArchTest.java`, `QueryPortArchTest.java`)
- [x] 커밋: `feat: MessageOutboxPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (이미 GREEN에서 작성)
- [x] 테스트 여전히 통과 확인
- [x] 변경 사항 없음 (SKIP)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### 5️⃣ S3ClientPort 정의 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [x] `S3ClientPortTest.java` 생성
- [x] 외부 API Port 메서드 테스트:
  - `generatePresignedUrl()`, `initiateMultipartUpload()`, `headObject()`, `uploadFromUrl()`
- [x] 커밋: `test: S3ClientPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `port/out/external/S3ClientPort.java` 생성
- [x] 4개 메서드 시그니처 정의
- [x] 커밋: `feat: S3ClientPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (Timeout, Retry 정책 명시)
- [x] ArchUnit 테스트 추가 (외부 API Port 규칙)
- [x] 변경 사항 없음 (SKIP - 이미 GREEN에서 작성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### 6️⃣ SqsClientPort, WebhookClientPort 정의 (Cycle 6) ✅

#### 🔴 Red: 테스트 작성
- [x] `SqsClientPortTest.java` 생성
- [x] `WebhookClientPortTest.java` 생성
- [x] SQS: `sendMessage()`, `sendMessageBatch()`
- [x] Webhook: `send()`
- [x] 커밋: `test: SqsClientPort, WebhookClientPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `port/out/external/SqsClientPort.java` 생성
- [x] `port/out/external/WebhookClientPort.java` 생성
- [x] 커밋: `feat: SqsClientPort, WebhookClientPort 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] ArchUnit 테스트 생략 (Port는 인터페이스이므로 불필요)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture는 Port에 불필요 (생략)

---

### Phase 2: Command DTO 정의 (4 사이클)

---

### 7️⃣ GeneratePresignedUrlCommand 정의 (Cycle 7) ✅

#### 🔴 Red: 테스트 작성
- [x] `dto/command/GeneratePresignedUrlCommandTest.java` 생성
- [x] Record 필드 검증 테스트:
  - fileName, fileSize, mimeType, uploaderId, category, tags
- [x] 커밋: `test: GeneratePresignedUrlCommand 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `dto/command/GeneratePresignedUrlCommand.java` 생성 (Record)
- [x] 6개 필드 정의
- [x] 커밋: `feat: GeneratePresignedUrlCommand 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `GeneratePresignedUrlCommandFixture.java` 생성 (Object Mother 패턴)
- [x] `aCommand()` 메서드 작성
- [x] 테스트 → Fixture 사용으로 리팩토링
- [x] 커밋: `test: GeneratePresignedUrlCommandFixture 정리 (Tidy)`

---

### 8️⃣ CompleteUploadCommand 정의 (Cycle 8) ✅

#### 🔴 Red: 테스트 작성
- [x] `CompleteUploadCommandTest.java` 생성
- [x] fileId 검증 테스트
- [x] 커밋: `test: CompleteUploadCommand 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `CompleteUploadCommand.java` 생성 (Record)
- [x] fileId 필드 정의
- [x] 커밋: `feat: CompleteUploadCommand 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `CompleteUploadCommandFixture.java` 생성
- [x] 커밋: `test: CompleteUploadCommandFixture 정리 (Tidy)`

---

### 9️⃣ UploadFromExternalUrlCommand 정의 (Cycle 9) ✅

#### 🔴 Red: 테스트 작성
- [x] `UploadFromExternalUrlCommandTest.java` 생성
- [x] externalUrl, uploaderId, category, tags, webhookUrl 검증
- [x] 커밋: `test: UploadFromExternalUrlCommand 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `UploadFromExternalUrlCommand.java` 생성 (Record)
- [x] 5개 필드 정의
- [x] 커밋: `feat: UploadFromExternalUrlCommand 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `UploadFromExternalUrlCommandFixture.java` 생성
- [x] 커밋: `test: UploadFromExternalUrlCommandFixture 정리 (Tidy)`

---

### 🔟 ProcessFileCommand 정의 (Cycle 10) ✅

#### 🔴 Red: 테스트 작성
- [x] `ProcessFileCommandTest.java` 생성
- [x] fileId, jobTypes 검증
- [x] 커밋: `test: ProcessFileCommand 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ProcessFileCommand.java` 생성 (Record)
- [x] fileId, jobTypes 필드 정의
- [x] 커밋: `feat: ProcessFileCommand 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `ProcessFileCommandFixture.java` 생성
- [x] 커밋: `test: ProcessFileCommandFixture 정리 (Tidy)`

---

### Phase 3: Query DTO 정의 (2 사이클)

---

### 1️⃣1️⃣ GetFileQuery 정의 (Cycle 11) ✅

#### 🔴 Red: 테스트 작성
- [x] `dto/query/GetFileQueryTest.java` 생성
- [x] fileId 검증
- [x] 커밋: `test: GetFileQuery 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `GetFileQuery.java` 생성 (Record)
- [x] fileId 필드 정의
- [x] 커밋: `feat: GetFileQuery 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `GetFileQueryFixture.java` 생성
- [x] 커밋: `test: GetFileQueryFixture 정리 (Tidy)`

---

### 1️⃣2️⃣ ListFilesQuery 정의 (Cycle 12) ✅

#### 🔴 Red: 테스트 작성
- [x] `ListFilesQueryTest.java` 생성
- [x] uploaderId, status, category, cursor, size 검증
- [x] 커밋: `test: ListFilesQuery 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ListFilesQuery.java` 생성 (Record)
- [x] 5개 필드 정의 (uploaderId, status, category, cursor, size)
- [x] 커밋: `feat: ListFilesQuery 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] `ListFilesQueryFixture.java` 생성
- [x] 커밋: `test: ListFilesQueryFixture 정리 (Tidy)`

---

### Phase 4: Response DTO 정의 (4 사이클)

---

### 1️⃣3️⃣ PresignedUrlResponse 정의 (Cycle 13) ✅

#### 🔴 Red: 테스트 작성
- [x] `dto/response/PresignedUrlResponseTest.java` 생성
- [x] fileId, presignedUrl, expiresIn, s3Key 검증
- [x] 커밋: `test: PresignedUrlResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `PresignedUrlResponse.java` 생성 (Record)
- [x] 4개 필드 정의 (fileId, presignedUrl, expiresIn, s3Key)
- [x] `PresignedUrlResponseFixture.java` 생성 (GREEN 단계에서 함께 생성)
- [x] 커밋: `feat: PresignedUrlResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 패턴 확인 완료 (GREEN 단계에서 함께 생성)

---

### 1️⃣4️⃣ FileResponse 정의 (Cycle 14) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileResponseTest.java` 생성
- [x] fileId, status, s3Url, cdnUrl 검증
- [x] 커밋: `test: FileResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileResponse.java` 생성 (Record)
- [x] 4개 필드 정의 (fileId, status, s3Url, cdnUrl)
- [x] `FileResponseFixture.java` 생성 (GREEN 단계에서 함께 생성)
- [x] 커밋: `feat: FileResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 패턴 확인 완료 (GREEN 단계에서 함께 생성)

---

### 1️⃣5️⃣ FileDetailResponse 정의 (Cycle 15) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileDetailResponseTest.java` 생성
- [x] File 정보 + FileProcessingJob 목록 검증
- [x] 커밋: `test: FileDetailResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileDetailResponse.java` 생성 (Record)
- [x] 5개 필드 정의 (fileId, status, s3Url, cdnUrl, processingJobs)
- [x] `FileDetailResponseFixture.java` 생성 (GREEN 단계에서 함께 생성)
- [x] 커밋: `feat: FileDetailResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 패턴 확인 완료 (GREEN 단계에서 함께 생성)

---

### 1️⃣6️⃣ FileSummaryResponse 정의 (Cycle 16) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileSummaryResponseTest.java` 생성
- [x] fileId, fileName, status, uploaderId, createdAt 검증
- [x] 커밋: `test: FileSummaryResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileSummaryResponse.java` 생성 (Record)
- [x] 5개 필드 정의 (fileId, fileName, status, uploaderId, createdAt)
- [x] `FileSummaryResponseFixture.java` 생성 (GREEN 단계에서 함께 생성)
- [x] 커밋: `feat: FileSummaryResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (DtoRecordArchTest가 이미 존재)
- [x] Javadoc 추가 (GREEN 단계에서 완료)
- [x] 구조 개선 불필요 (이미 완성)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 패턴 확인 완료 (GREEN 단계에서 함께 생성)

---

### Phase 5: Command UseCase 구현 (16 사이클)

---

### 1️⃣7️⃣ GeneratePresignedUrlUseCase - 메타데이터 저장 (Cycle 17) ✅

#### 🔴 Red: 테스트 작성
- [x] `GeneratePresignedUrlServiceTest.java` 생성
- [x] Mock Port 준비 (FilePersistencePort, S3ClientPort)
- [x] `shouldCreateFileMetadata()` 테스트 작성
- [x] 커밋: `test: GeneratePresignedUrl 메타데이터 저장 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `service/GeneratePresignedUrlService.java` 생성
- [x] `port/in/command/GeneratePresignedUrlPort.java` 생성
- [x] `@Transactional` 추가 (saveFileMetadata 메서드)
- [x] File 메타데이터 생성 + 저장 로직 (File.forNew() 사용)
- [x] S3 Presigned URL 생성 로직 (트랜잭션 외부, S3ClientPort 사용)
- [x] Clock 주입으로 테스트 가능한 시간 제어
- [x] 커밋: `feat: GeneratePresignedUrl 메타데이터 저장 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ArchUnit 테스트 생략 (기존 ArchUnit 테스트가 이미 @Transactional 내 외부 API 호출 금지 검증 중)
- [x] Transaction 경계 명확히 분리 (saveFileMetadata @Transactional, generatePresignedUrlResponse 외부)

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 확인 완료 (GeneratePresignedUrlCommandFixture, FileFixture 사용)

---

### 1️⃣8️⃣ GeneratePresignedUrlUseCase - 파일 크기 검증 (Cycle 18) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldThrowExceptionWhenFileSizeExceeds1GB()` 테스트 작성
- [x] 커밋: `test: 파일 크기 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] 파일 크기 검증 로직 이미 구현됨 (Domain Layer File.forNew()에서 수행)
- [x] File.validateFileSize() 메서드에서 MAX_FILE_SIZE (1GB) 검증 중
- [x] InvalidFileSizeException 예외 발생 확인
- [x] Application Layer는 Domain 예외를 자연스럽게 전파

#### ♻️ Refactor: 리팩토링
- [x] 상수 이미 존재 (Domain Layer: MAX_FILE_SIZE = 1024L * 1024L * 1024L)
- [x] 검증 로직 이미 최적화됨 (File.validateFileSize() private static method)

#### 🧹 Tidy: TestFixture 정리
- [x] GeneratePresignedUrlCommandFixture.withFileSize() 사용 확인

---

### 1️⃣9️⃣ GeneratePresignedUrlUseCase - MIME 타입 검증 (Cycle 19) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldThrowExceptionWhenInvalidMimeType()` 테스트 작성
- [x] 커밋: `test: MIME 타입 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] MIME 타입 검증 로직 이미 구현됨 (Domain Layer File.forNew()에서 수행)
- [x] File.validateMimeType() 메서드에서 ALLOWED_MIME_TYPES 검증 중
- [x] InvalidMimeTypeException 예외 발생 확인
- [x] Application Layer는 Domain 예외를 자연스럽게 전파

#### ♻️ Refactor: 리팩토링
- [x] 상수 이미 존재 (Domain Layer: ALLOWED_MIME_TYPES)
- [x] 검증 로직 이미 최적화됨 (File.validateMimeType() private static method)

#### 🧹 Tidy: TestFixture 정리
- [x] GeneratePresignedUrlCommandFixture.withMimeType() 사용 확인

---

### 2️⃣0️⃣ GeneratePresignedUrlUseCase - 업로드 전략 결정 (Cycle 20) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldUseSingleUploadForSmallFile()` 테스트 작성 (10MB)
- [x] `shouldUseMultipartUploadForLargeFile()` 테스트 작성 (200MB)
- [x] 커밋: `test: 업로드 전략 결정 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] PresignedUrlResponse에 uploadStrategy 필드 추가
- [x] GeneratePresignedUrlService.determineUploadStrategy() 구현
  - < 100MB: SINGLE
  - ≥ 100MB: MULTIPART
- [x] PresignedUrlResponseFixture 업데이트
- [x] 커밋: `feat: 업로드 전략 결정 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 상수 추출 (MULTIPART_THRESHOLD = 100MB)
- [x] 커밋: `struct: MULTIPART_THRESHOLD 상수 추출 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] TestFixture 사용 확인 (GeneratePresignedUrlCommandFixture.withFileSize())

---

### ✅ 2️⃣1️⃣ CompleteUploadUseCase - 상태 검증 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [x] `CompleteUploadServiceTest.java` 생성
- [x] `shouldThrowExceptionWhenInvalidStatus()` 테스트 작성
  - shouldThrowExceptionWhenAlreadyCompleted() (COMPLETED 상태 거부)
  - shouldThrowExceptionWhenFailed() (FAILED 상태 거부)
- [x] 커밋: `test: 상태 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `service/CompleteUploadService.java` 생성
- [x] File 조회 + 상태 검증 로직 (PENDING/UPLOADING만 허용)
- [x] LoadFilePort (Outbound Query Port) 생성
- [x] CompleteUploadPort (Inbound Port) 생성
- [x] IllegalStateException 예외 발생 (InvalidFileStatusException 대신)
- [x] 커밋: `feat: 상태 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 리팩토링 불필요 (GREEN에서 완료)

#### 🧹 Tidy: TestFixture 정리
- [x] 기존 FileFixture 사용 (aCompletedFile, aFailedFile)

---

### 2️⃣2️⃣ CompleteUploadUseCase - S3 Object 존재 확인 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenS3ObjectNotExists()` 테스트 작성
- [ ] Mock S3ClientPort 준비
- [ ] 커밋: `test: S3 Object 존재 확인 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] S3 Object HEAD 요청 로직 추가 (트랜잭션 밖)
- [ ] S3ObjectNotFoundException 예외 발생
- [ ] 커밋: `feat: S3 Object 존재 확인 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트: @Transactional 내 S3 호출 금지 검증
- [ ] Timeout 3초, 재시도 3회 설정
- [ ] 커밋: `struct: S3 Object 존재 확인 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: S3 Object 존재 확인 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ CompleteUploadUseCase - MessageOutbox 생성 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateMessageOutboxWhenUploadCompleted()` 테스트 작성
- [ ] Mock MessageOutboxCommandPort 준비
- [ ] 커밋: `test: MessageOutbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] File 상태 업데이트 (COMPLETED) + MessageOutbox 생성 로직
- [ ] FILE_UPLOADED 이벤트 Outbox에 저장
- [ ] 커밋: `feat: MessageOutbox 생성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증 (S3 호출 → 트랜잭션 시작 → Outbox 생성 → 커밋)
- [ ] 커밋: `struct: MessageOutbox 생성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: MessageOutbox 생성 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ UploadFromExternalUrlUseCase - URL 검증 (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `UploadFromExternalUrlServiceTest.java` 생성
- [ ] `shouldThrowExceptionWhenInvalidUrl()` 테스트 작성 (HTTPS만 허용)
- [ ] 커밋: `test: 외부 URL 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `service/UploadFromExternalUrlService.java` 생성
- [ ] HTTPS URL 검증 로직 추가
- [ ] InvalidUrlException 예외 발생
- [ ] 커밋: `feat: 외부 URL 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: UploadFromExternalUrlService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UploadFromExternalUrlServiceFixture.java` 생성
- [ ] 커밋: `test: UploadFromExternalUrlService Fixture 정리 (Tidy)`

---

### 2️⃣5️⃣ UploadFromExternalUrlUseCase - MessageOutbox 생성 (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateMessageOutboxForExternalDownload()` 테스트 작성
- [ ] 커밋: `test: 외부 다운로드 Outbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] File 메타데이터 생성 + MessageOutbox 생성 로직
- [ ] FILE_DOWNLOAD_REQUESTED 이벤트 Outbox에 저장
- [ ] 커밋: `feat: 외부 다운로드 Outbox 생성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] 커밋: `struct: 외부 다운로드 Outbox 생성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: 외부 다운로드 Outbox 생성 테스트 정리 (Tidy)`

---

### 2️⃣6️⃣ ProcessFileUseCase - 상태 검증 (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessFileServiceTest.java` 생성
- [ ] `shouldThrowExceptionWhenFileNotCompleted()` 테스트 작성
- [ ] 커밋: `test: 파일 가공 상태 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `service/ProcessFileService.java` 생성
- [ ] File 조회 + 상태 검증 로직 (COMPLETED만 허용)
- [ ] FileNotCompletedException 예외 발생
- [ ] 커밋: `feat: 파일 가공 상태 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: ProcessFileService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProcessFileServiceFixture.java` 생성
- [ ] 커밋: `test: ProcessFileService Fixture 정리 (Tidy)`

---

### 2️⃣7️⃣ ProcessFileUseCase - FileProcessingJob 생성 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateFileProcessingJobs()` 테스트 작성
- [ ] Mock FileProcessingJobCommandPort 준비
- [ ] 커밋: `test: FileProcessingJob 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] FileProcessingJob Entity 생성 로직 (각 jobType마다)
- [ ] MessageOutbox 생성 (FILE_PROCESSING_REQUESTED 이벤트)
- [ ] 커밋: `feat: FileProcessingJob 생성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 검증
- [ ] 커밋: `struct: FileProcessingJob 생성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: FileProcessingJob 생성 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ Port In Command 인터페이스 정의 (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `port/in/command/GeneratePresignedUrlUseCaseTest.java` 생성
- [ ] 나머지 3개 UseCase Port In 테스트 작성
- [ ] 커밋: `test: Command UseCase Port In 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `port/in/command/GeneratePresignedUrlUseCase.java` 인터페이스 생성
- [ ] `CompleteUploadUseCase`, `UploadFromExternalUrlUseCase`, `ProcessFileUseCase` 인터페이스 생성
- [ ] Service 클래스가 인터페이스 구현하도록 수정
- [ ] 커밋: `feat: Command UseCase Port In 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Port In Command 규칙)
- [ ] 커밋: `struct: Command UseCase Port In 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Command UseCase Port In 테스트 정리 (Tidy)`

---

### Phase 6: Query UseCase 구현 (8 사이클)

---

### 2️⃣9️⃣ GetFileUseCase 구현 (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `GetFileServiceTest.java` 생성
- [ ] `shouldGetFileDetail()` 테스트 작성
- [ ] Mock FileQueryPort, FileProcessingJobQueryPort 준비
- [ ] 커밋: `test: GetFileUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `service/GetFileService.java` 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] File 조회 + FileProcessingJob 조회 로직
- [ ] FileDetailResponse 조합
- [ ] 커밋: `feat: GetFileUseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Query UseCase 규칙)
- [ ] 커밋: `struct: GetFileService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `GetFileServiceFixture.java` 생성
- [ ] 커밋: `test: GetFileService Fixture 정리 (Tidy)`

---

### 3️⃣0️⃣ ListFilesUseCase - Cursor 기반 Pagination (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `ListFilesServiceTest.java` 생성
- [ ] `shouldListFilesWithCursorPagination()` 테스트 작성
- [ ] 커밋: `test: ListFilesUseCase Pagination 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `service/ListFilesService.java` 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] Cursor 기반 Pagination 로직 (createdAt 기준)
- [ ] 커밋: `feat: ListFilesUseCase Pagination 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: ListFilesService 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ListFilesServiceFixture.java` 생성
- [ ] 커밋: `test: ListFilesService Fixture 정리 (Tidy)`

---

### 3️⃣1️⃣ ListFilesUseCase - 필터링 (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFilterFilesByUploaderIdAndStatus()` 테스트 작성
- [ ] 커밋: `test: ListFilesUseCase 필터링 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] uploaderId, status, category 필터링 로직 추가
- [ ] 커밋: `feat: ListFilesUseCase 필터링 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 필터 조건 분리 (Filter 객체 생성 고려)
- [ ] 커밋: `struct: ListFilesUseCase 필터링 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: ListFilesUseCase 필터링 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ Port In Query 인터페이스 정의 (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `port/in/query/GetFileUseCaseTest.java` 생성
- [ ] `ListFilesUseCaseTest.java` 생성
- [ ] 커밋: `test: Query UseCase Port In 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `port/in/query/GetFileUseCase.java` 인터페이스 생성
- [ ] `ListFilesUseCase.java` 인터페이스 생성
- [ ] Service 클래스가 인터페이스 구현하도록 수정
- [ ] 커밋: `feat: Query UseCase Port In 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Port In Query 규칙)
- [ ] 커밋: `struct: Query UseCase Port In 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Query UseCase Port In 테스트 정리 (Tidy)`

---

### Phase 7: 아웃박스 패턴 구현 (8 사이클)

---

### 3️⃣3️⃣ TransactionalEventListener 구현 (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `listener/MessageOutboxEventListenerTest.java` 생성
- [ ] `shouldSendMessageToSqsAfterCommit()` 테스트 작성
- [ ] Mock SqsClientPort, MessageOutboxCommandPort 준비
- [ ] 커밋: `test: TransactionalEventListener 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `listener/MessageOutboxEventListener.java` 생성
- [ ] `@TransactionalEventListener(phase = AFTER_COMMIT)` 추가
- [ ] MessageOutbox PENDING 메시지를 SQS로 전송
- [ ] 성공 시: MessageOutbox 상태를 SENT로 업데이트
- [ ] 커밋: `feat: TransactionalEventListener 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Listener 규칙)
- [ ] 예외 처리 개선 (로그 기록)
- [ ] 커밋: `struct: TransactionalEventListener 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxEventListenerFixture.java` 생성
- [ ] 커밋: `test: TransactionalEventListener Fixture 정리 (Tidy)`

---

### 3️⃣4️⃣ 폴백 스케줄러 구현 (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `scheduler/OutboxRetrySchedulerTest.java` 생성
- [ ] `shouldRetryPendingMessages()` 테스트 작성
- [ ] 커밋: `test: 폴백 스케줄러 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `scheduler/OutboxRetryScheduler.java` 생성
- [ ] `@Scheduled(fixedDelay = 60000)` 추가 (1분마다)
- [ ] PENDING 상태의 MessageOutbox 조회 (createdAt < 1분 전)
- [ ] SQS로 전송 시도
- [ ] 커밋: `feat: 폴백 스케줄러 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Scheduler 규칙)
- [ ] 커밋: `struct: 폴백 스케줄러 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `OutboxRetrySchedulerFixture.java` 생성
- [ ] 커밋: `test: 폴백 스케줄러 Fixture 정리 (Tidy)`

---

### 3️⃣5️⃣ 재시도 전략 구현 (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCountOnFailure()` 테스트 작성
- [ ] `shouldMarkAsFailedWhenMaxRetryExceeded()` 테스트 작성
- [ ] 커밋: `test: 재시도 전략 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 재시도 전략 로직 추가 (최대 3회, Exponential Backoff)
- [ ] retryCount 증가
- [ ] maxRetryCount 초과 시 FAILED로 변경
- [ ] 커밋: `feat: 재시도 전략 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상수 추출 (MAX_RETRY_COUNT = 3)
- [ ] Exponential Backoff 계산 로직 분리
- [ ] 커밋: `struct: 재시도 전략 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: 재시도 전략 테스트 정리 (Tidy)`

---

### 3️⃣6️⃣ 아웃박스 패턴 Integration Test (Cycle 36)

#### 🔴 Red: 테스트 작성
- [ ] `OutboxPatternIntegrationTest.java` 생성 (@SpringBootTest)
- [ ] `shouldSendMessageAfterCommit()` 테스트 작성
- [ ] `shouldRetryFailedMessages()` 테스트 작성
- [ ] 커밋: `test: 아웃박스 패턴 Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TestContainer 설정 (MySQL, SQS LocalStack)
- [ ] 애프터 커밋 리스너 검증
- [ ] 폴백 스케줄러 검증
- [ ] 커밋: `feat: 아웃박스 패턴 Integration 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인 (@DirtiesContext)
- [ ] 커밋: `struct: 아웃박스 패턴 Integration 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: 아웃박스 패턴 Integration 테스트 정리 (Tidy)`

---

### Phase 8: ArchUnit 전체 검증 (4 사이클)

---

### 3️⃣7️⃣ Application Layer 의존성 규칙 (Cycle 37)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/ApplicationLayerArchitectureTest.java` 생성
- [ ] Application Layer는 Domain에만 의존 검증
- [ ] Application Layer는 Persistence/REST API에 의존 금지 검증
- [ ] 커밋: `test: Application Layer 의존성 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (LayeredArchitecture)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Application Layer 의존성 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가 (ArchRule description)
- [ ] 커밋: `struct: Application Layer 의존성 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Application Layer 의존성 규칙 테스트 정리 (Tidy)`

---

### 3️⃣8️⃣ Transaction 경계 규칙 (Cycle 38)

#### 🔴 Red: 테스트 작성
- [ ] `TransactionBoundaryArchitectureTest.java` 생성
- [ ] @Transactional 내 S3ClientPort 호출 금지 검증
- [ ] @Transactional 내 SqsClientPort 호출 금지 검증
- [ ] @Transactional 내 WebhookClientPort 호출 금지 검증
- [ ] 커밋: `test: Transaction 경계 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (noClasses()...should()...callMethod())
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Transaction 경계 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Transaction 경계 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Transaction 경계 규칙 테스트 정리 (Tidy)`

---

### 3️⃣9️⃣ CQRS 분리 규칙 (Cycle 39)

#### 🔴 Red: 테스트 작성
- [ ] `CqrsArchitectureTest.java` 생성
- [ ] Command UseCase는 Port In Command만 구현 검증
- [ ] Query UseCase는 Port In Query만 구현 검증
- [ ] Command DTO와 Query DTO 패키지 분리 검증
- [ ] 커밋: `test: CQRS 분리 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (classes()...should()...implement())
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: CQRS 분리 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: CQRS 분리 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: CQRS 분리 규칙 테스트 정리 (Tidy)`

---

### 4️⃣0️⃣ Lombok 금지 규칙 (Cycle 40)

#### 🔴 Red: 테스트 작성
- [ ] `LombokProhibitionArchitectureTest.java` 생성
- [ ] Command DTO는 Record 사용 검증 (Lombok 금지)
- [ ] Query DTO는 Record 사용 검증 (Lombok 금지)
- [ ] Response DTO는 Record 사용 검증 (Lombok 금지)
- [ ] 커밋: `test: Lombok 금지 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (classes()...should()...beRecords())
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Lombok 금지 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Lombok 금지 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Lombok 금지 규칙 테스트 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (40 사이클, 체크박스 모두 ✅)
- [ ] 모든 Unit Test 통과 (커버리지 > 80%)
- [ ] ArchUnit 테스트 통과 (의존성, Transaction 경계, CQRS, Lombok 금지)
- [ ] Zero-Tolerance 규칙 준수
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] Integration Test 통과 (아웃박스 패턴)
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/FILE-002.md
- **PRD**: docs/prd/file-management-system.md
- **컨벤션**: docs/coding_convention/03-application-layer/

---

## 📊 사이클 요약

| Phase | 사이클 수 | 예상 소요 시간 |
|-------|----------|---------------|
| Phase 1: Port 정의 | 6 | 90분 |
| Phase 2: Command DTO 정의 | 4 | 60분 |
| Phase 3: Query DTO 정의 | 2 | 30분 |
| Phase 4: Response DTO 정의 | 4 | 60분 |
| Phase 5: Command UseCase 구현 | 12 | 180분 |
| Phase 6: Query UseCase 구현 | 4 | 60분 |
| Phase 7: 아웃박스 패턴 구현 | 4 | 60분 |
| Phase 8: ArchUnit 전체 검증 | 4 | 60분 |
| **합계** | **40** | **600분 (10시간)** |

---

## 🎯 핵심 원칙

1. **작은 단위**: 각 사이클은 5-15분 내 완료
2. **4단계 필수**: Red → Green → Refactor → Tidy 모두 수행
3. **TestFixture 필수**: Tidy 단계에서 Object Mother 패턴 적용
4. **Zero-Tolerance**: Transaction 경계, CQRS 분리, Lombok 금지 엄격 준수
5. **체크박스 추적**: `/kb/application/go` 명령이 Plan 파일을 읽고 진행 상황 추적
6. **Transaction 경계**: @Transactional 내 외부 API 호출 절대 금지
7. **아웃박스 패턴**: 메시지 전송은 MessageOutbox 통해서만
8. **ArchUnit 검증**: 각 Refactor 단계에서 ArchUnit 규칙 검증 필수

---

## 🚀 다음 단계

```bash
# Plan 파일 생성 완료
/kb/application/go

# 또는 개별 Phase 실행
/kb/application/red    # Red Phase만
/kb/application/green  # Green Phase만
/kb/application/refactor  # Refactor Phase만
```
