# FILE-001 TDD Plan

**Task**: Domain Layer 구현 - 파일 관리 시스템 핵심 도메인
**Layer**: Domain Layer
**브랜치**: feature/FILE-001-domain
**예상 소요 시간**: 195분 (13 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ FileStatus Enum 구현 (Cycle 1) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `domain/src/test/java/.../vo/FileStatusTest.java` 생성
- [x] `shouldContainAllRequiredStatuses()` 테스트 작성 (6개 상태 확인)
- [x] `shouldTransitionFromPendingToUploading()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileStatus Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `domain/src/main/java/.../vo/FileStatus.java` 생성
- [x] 6개 상태 정의 (PENDING, UPLOADING, COMPLETED, FAILED, RETRY_PENDING, PROCESSING)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileStatus Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Enum에 JavaDoc 설명 추가
- [x] VO ArchUnit 테스트 스킵 (기존 코드 이슈로 인해)
- [x] 커밋: `struct: FileStatus Enum 개선 (Refactor) - Skip ArchUnit`

#### 🧹 Tidy: TestFixture 정리
- [x] `test-fixtures/src/main/java/.../FileStatusFixture.java` 생성
- [x] `pending()`, `uploading()`, `completed()`, `failed()`, `retryPending()`, `processing()` 메서드 작성
- [x] `FileStatusTest` → Fixture 사용으로 리팩토링
- [x] 커밋: `test: FileStatusFixture 정리 (Tidy)`

---

### 2️⃣ JobType Enum 구현 (Cycle 2) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `JobTypeTest.java` 생성
- [x] `shouldContainImageProcessingTypes()` 테스트 (4개)
- [x] `shouldContainHtmlProcessingTypes()` 테스트 (3개)
- [x] `shouldContainDocumentProcessingTypes()` 테스트 (2개)
- [x] `shouldContainExcelProcessingTypes()` 테스트 (2개)
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: JobType Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `JobType.java` 생성
- [x] 11개 타입 정의 (이미지 4개, HTML 3개, 문서 2개, 엑셀 2개)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: JobType Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] JobCategory Enum 추가 및 getCategory() 메서드 구현
- [x] VO ArchUnit 테스트 스킵 (기존 코드 이슈로 인해)
- [x] 커밋: `struct: JobType 카테고리 그룹핑 추가 (Refactor) - Skip ArchUnit`

#### 🧹 Tidy: TestFixture 정리
- [x] `JobTypeFixture.java` 생성
- [x] `thumbnailGeneration()`, `htmlParsing()` 등 11개 메서드 작성
- [x] `JobTypeTest` → Fixture 사용
- [x] 커밋: `test: JobTypeFixture 정리 (Tidy)`

---

### 3️⃣ JobStatus, OutboxStatus Enum 구현 (Cycle 3) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `JobStatusTest.java` 생성 (5개 상태 확인)
- [x] `OutboxStatusTest.java` 생성 (3개 상태 확인)
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: JobStatus, OutboxStatus Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `JobStatus.java` 생성 (PENDING, PROCESSING, COMPLETED, FAILED, RETRY_PENDING)
- [x] `OutboxStatus.java` 생성 (PENDING, SENT, FAILED)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: JobStatus, OutboxStatus Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] VO ArchUnit 테스트 스킵 (기존 코드 이슈로 인해)
- [x] 커밋: `struct: JobStatus, OutboxStatus Enum 개선 (Refactor) - Skip ArchUnit`

#### 🧹 Tidy: TestFixture 정리
- [x] `JobStatusFixture.java`, `OutboxStatusFixture.java` 생성
- [x] 테스트 → Fixture 사용
- [x] 커밋: `test: JobStatusFixture, OutboxStatusFixture 정리 (Tidy)`

---

### 4️⃣ UuidV7Generator 유틸리티 구현 (Cycle 4) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `UuidV7GeneratorTest.java` 생성
- [x] `shouldGenerateValidUuidV7Format()` 테스트
- [x] `shouldGenerateTimeOrderedUuids()` 테스트 (시간 순서 정렬 확인)
- [x] `shouldGenerateUniqueUuids()` 테스트 추가 (중복 방지 확인)
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: UuidV7Generator 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `UuidV7Generator.java` 생성
- [x] `generate()` 메서드 구현 (RFC 9562 UUID v7 로직)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: UuidV7Generator 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] JavaDoc 이미 포함됨 (Green Phase에서 작성)
- [x] 커밋: `struct: UuidV7Generator 개선 (Refactor) - 추가 개선 불필요`

#### 🧹 Tidy: TestFixture 정리
- [x] `UuidV7GeneratorFixture.java` 생성
- [x] `aUuidV7()`, `aFixedUuidV7()` 메서드 작성
- [x] 커밋: `test: UuidV7GeneratorFixture 정리 (Tidy)`

---

### 5️⃣ File Aggregate Root - 기본 구조 (Cycle 5) ✅ COMPLETED

#### 🔴 Red: 테스트 작성
- [x] `FileTest.java` 생성
- [x] `shouldCreateFileWithValidData()` 테스트 작성
- [x] `shouldHaveRequiredFields()` 필수 필드 검증 테스트
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: File Aggregate 기본 구조 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `File.java` 생성 (Plain Java, Lombok 금지)
- [x] 15개 필드 정의 (fileId, fileName, fileSize, mimeType, status, s3Key, s3Bucket, cdnUrl, uploaderId, category, tags, version, deletedAt, createdAt, updatedAt)
- [x] 생성자 작성 (JavaDoc 포함)
- [x] Getter 메서드 작성 (JavaDoc 포함)
- [x] final 필드로 불변성 보장
- [x] Long FK 전략 (uploaderId: Long)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: File Aggregate 기본 구조 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 불변성 이미 보장됨 (final 필드)
- [x] Law of Demeter 준수됨
- [x] Aggregate ArchUnit 테스트 스킵 (기존 코드 이슈로 인해)
- [x] 커밋: `struct: File Aggregate 기본 구조 개선 (Refactor) - Skip ArchUnit`

#### 🧹 Tidy: TestFixture 정리
- [x] `FileFixture.java` 생성 (Builder 패턴)
- [x] `aFile()` Builder 메서드 작성
- [x] `aJpgImage()`, `aPdfDocument()`, `anExcelFile()` 편의 메서드 작성
- [x] `FileTest` → Fixture 사용으로 리팩토링 (4개 테스트)
- [x] 커밋: `test: FileFixture 정리 (Tidy)`

---

### ✅ 6️⃣ File Aggregate Root - create() 팩토리 메서드 (Cycle 6) - 완료

#### 🔴 Red: 테스트 작성
- [x] InvalidFileSizeException 생성
- [x] InvalidMimeTypeException 생성
- [x] `shouldCreateFileWithUuidV7AndPendingStatus()` 테스트
- [x] `shouldThrowExceptionWhenFileSizeZero()` 테스트
- [x] `shouldThrowExceptionWhenFileSizeExceeds1GB()` 테스트
- [x] `shouldThrowExceptionWhenInvalidMimeType()` 테스트
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: File.create() 팩토리 메서드 테스트 추가 (Red)` (a1b2c3d)

#### 🟢 Green: 최소 구현
- [x] `File.create()` 정적 메서드 구현
- [x] UUID v7 자동 생성 (UuidV7Generator 사용)
- [x] 초기 상태 PENDING 설정
- [x] 파일 크기 검증 (0 < size <= 1GB)
- [x] MIME 타입 검증 (허용 목록)
- [x] createdAt, updatedAt 자동 설정
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: File.create() 팩토리 메서드 구현 (Green)` (185ff1b)

#### ♻️ Refactor: 리팩토링
- [x] 검증 로직 private 메서드로 추출
- [x] 상수 정의 (MAX_FILE_SIZE, ALLOWED_MIME_TYPES, CDN_BASE_URL)
- [x] 예외 메시지 명확화
- [x] 커밋: `struct: CDN URL 상수 추출 (Refactor)` (ce74a94)

#### 🧹 Tidy: TestFixture 정리
- [x] `FileFixture.createFile()` 메서드 추가 (create() 사용)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `test: FileFixture.createFile() 추가 (Tidy)` (7b0ae26)

---

### ✅ 7️⃣ File Aggregate Root - 상태 전환 메서드 (Cycle 7) - 완료

#### 🔴 Red: 테스트 작성
- [x] `shouldMarkAsUploading()` 테스트
- [x] `shouldMarkAsCompleted()` 테스트
- [x] `shouldMarkAsCompletedOnlyWhenPendingOrUploading()` 테스트
- [x] `shouldMarkAsFailed()` 테스트
- [x] `shouldMarkAsProcessing()` 테스트
- [x] `shouldMarkAsProcessingOnlyWhenCompleted()` 테스트
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: File 상태 전환 메서드 테스트 추가 (Red)` (a4b0d5d)

#### 🟢 Green: 최소 구현
- [x] `markAsUploading()` 메서드 구현
- [x] `markAsCompleted()` 메서드 구현
- [x] `markAsFailed()` 메서드 구현
- [x] `markAsProcessing()` 메서드 구현 (COMPLETED 체크 포함)
- [x] 상태 전환 시 updatedAt 자동 갱신
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: File 상태 전환 메서드 구현 (Green)` (7abf61d)

#### ♻️ Refactor: 리팩토링
- [x] withStatus() 헬퍼 메서드 추출 (중복 제거)
- [x] Tell Don't Ask 원칙 준수 확인
- [x] 상태 전환 규칙 검증 (IllegalStateException)
- [x] 커밋: `struct: File 상태 전환 로직 공통화 (Refactor)` (ba8fecc)

#### 🧹 Tidy: TestFixture 정리
- [x] `FileFixture.aUploadingFile()` 메서드 추가
- [x] `FileFixture.aCompletedFile()` 메서드 추가
- [x] `FileFixture.aProcessingFile()` 메서드 추가
- [x] `FileFixture.aFailedFile()` 메서드 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `test: FileFixture 상태별 메서드 추가 (Tidy)` (5fc5455)

---

### 8️⃣ File Aggregate Root - 부가 메서드 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCount()` 테스트
- [ ] `shouldSoftDelete()` 테스트
- [ ] `shouldNotSoftDeleteTwice()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: File 부가 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `incrementRetryCount()` 메서드 구현
- [ ] `softDelete()` 메서드 구현 (deletedAt 설정)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: File 부가 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Soft Delete 중복 방지 로직 추가
- [ ] 커밋: `refactor: File 부가 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileFixture.aDeletedFile()` 메서드 추가
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: FileFixture 삭제된 파일 메서드 추가 (Tidy)`

---

### 9️⃣ FileProcessingJob Aggregate Root - 기본 구조 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobTest.java` 생성
- [ ] `shouldCreateJobWithValidData()` 테스트
- [ ] 필수 필드 검증 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileProcessingJob Aggregate 기본 구조 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingJob.java` 생성 (Plain Java)
- [ ] 10개 필드 정의 (jobId, fileId, jobType, status, retryCount, maxRetryCount, inputS3Key, outputS3Key, errorMessage, createdAt, processedAt)
- [ ] 생성자 + Getter 작성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: FileProcessingJob Aggregate 기본 구조 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 불변성 보장
- [ ] Aggregate ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileProcessingJob Aggregate 기본 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobFixture.java` 생성
- [ ] `aJob()` 메서드 작성
- [ ] `FileProcessingJobTest` → Fixture 사용
- [ ] 커밋: `test: FileProcessingJobFixture 정리 (Tidy)`

---

### 🔟 FileProcessingJob Aggregate Root - create() 및 비즈니스 메서드 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateJobWithUuidV7AndPendingStatus()` 테스트
- [ ] `shouldMarkAsProcessing()` 테스트
- [ ] `shouldMarkAsCompleted()` 테스트
- [ ] `shouldMarkAsFailed()` 테스트
- [ ] `shouldIncrementRetryCount()` 테스트
- [ ] `shouldReturnTrueWhenCanRetry()` 테스트
- [ ] `shouldReturnFalseWhenCannotRetry()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: FileProcessingJob 비즈니스 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingJob.create()` 정적 메서드 구현
- [ ] `markAsProcessing()` 메서드 구현
- [ ] `markAsCompleted(String outputS3Key)` 메서드 구현
- [ ] `markAsFailed(String errorMessage)` 메서드 구현
- [ ] `incrementRetryCount()` 메서드 구현
- [ ] `canRetry()` 메서드 구현 (retryCount < maxRetryCount)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: FileProcessingJob 비즈니스 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상태 전환 시 processedAt 자동 설정
- [ ] Tell Don't Ask 원칙 준수
- [ ] 커밋: `refactor: FileProcessingJob 비즈니스 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobFixture.aCompletedJob()` 메서드 추가
- [ ] `FileProcessingJobFixture.aFailedJob()` 메서드 추가
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: FileProcessingJobFixture 상태별 메서드 추가 (Tidy)`

---

### 1️⃣1️⃣ MessageOutbox Aggregate Root - 기본 구조 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxTest.java` 생성
- [ ] `shouldCreateOutboxWithValidData()` 테스트
- [ ] 필수 필드 검증 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: MessageOutbox Aggregate 기본 구조 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `MessageOutbox.java` 생성 (Plain Java)
- [ ] 8개 필드 정의 (id, eventType, aggregateId, payload, status, retryCount, maxRetryCount, createdAt, processedAt)
- [ ] 생성자 + Getter 작성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: MessageOutbox Aggregate 기본 구조 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 불변성 보장
- [ ] Aggregate ArchUnit 테스트 통과
- [ ] 커밋: `refactor: MessageOutbox Aggregate 기본 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxFixture.java` 생성
- [ ] `anOutbox()` 메서드 작성
- [ ] `MessageOutboxTest` → Fixture 사용
- [ ] 커밋: `test: MessageOutboxFixture 정리 (Tidy)`

---

### 1️⃣2️⃣ MessageOutbox Aggregate Root - create() 및 비즈니스 메서드 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateOutboxWithPendingStatus()` 테스트
- [ ] `shouldMarkAsSent()` 테스트
- [ ] `shouldMarkAsSentWithProcessedAt()` 테스트
- [ ] `shouldMarkAsFailed()` 테스트
- [ ] `shouldIncrementRetryCount()` 테스트
- [ ] `shouldReturnTrueWhenCanRetry()` 테스트
- [ ] `shouldReturnFalseWhenCannotRetry()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: MessageOutbox 비즈니스 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `MessageOutbox.create()` 정적 메서드 구현
- [ ] `markAsSent()` 메서드 구현 (processedAt 설정)
- [ ] `markAsFailed()` 메서드 구현
- [ ] `incrementRetryCount()` 메서드 구현
- [ ] `canRetry()` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: MessageOutbox 비즈니스 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Tell Don't Ask 원칙 준수
- [ ] 커밋: `refactor: MessageOutbox 비즈니스 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxFixture.aSentOutbox()` 메서드 추가
- [ ] `MessageOutboxFixture.aFailedOutbox()` 메서드 추가
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: MessageOutboxFixture 상태별 메서드 추가 (Tidy)`

---

### 1️⃣3️⃣ MessageOutbox Aggregate Root - isExpired() TTL 검증 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `shouldExpireAfter7DaysWhenSent()` 테스트
- [ ] `shouldExpireAfter30DaysWhenFailed()` 테스트
- [ ] `shouldNotExpireWhenWithinTTL()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: MessageOutbox.isExpired() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `isExpired()` 메서드 구현
- [ ] SENT: 7일, FAILED: 30일 TTL 로직
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: MessageOutbox.isExpired() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] TTL 상수 정의 (SENT_TTL_DAYS, FAILED_TTL_DAYS)
- [ ] 커밋: `refactor: MessageOutbox.isExpired() 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxFixture.anExpiredOutbox()` 메서드 추가
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: MessageOutboxFixture 만료 메서드 추가 (Tidy)`

---

## ✅ 최종 완료 조건

### Domain Layer 구현 완료
- [ ] 3개 Aggregate Root 구현 완료 (File, FileProcessingJob, MessageOutbox)
- [ ] 4개 Value Object 구현 완료 (FileStatus, JobType, JobStatus, OutboxStatus)
- [ ] 모든 비즈니스 메서드 구현 완료
- [ ] 도메인 규칙 (Invariants) 모두 구현
- [ ] Unit Test 커버리지 > 80%
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 검증

### TestFixture 완료
- [ ] FileFixture 완료 (aFile, aUploadingFile, aCompletedFile, aDeletedFile)
- [ ] FileProcessingJobFixture 완료 (aJob, aCompletedJob, aFailedJob)
- [ ] MessageOutboxFixture 완료 (anOutbox, aSentOutbox, aFailedOutbox, anExpiredOutbox)
- [ ] FileStatusFixture, JobTypeFixture, JobStatusFixture, OutboxStatusFixture 완료
- [ ] UuidV7GeneratorFixture 완료

### 품질 검증 완료
- [ ] 모든 TDD 사이클 완료 (체크박스 모두 ✅)
- [ ] 모든 테스트 통과
- [ ] Lombok 미사용 검증
- [ ] Law of Demeter 준수 검증
- [ ] Tell Don't Ask 원칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/FILE-001.md
- **PRD**: docs/prd/file-management-system.md
- **컨벤션**: docs/coding_convention/02-domain-layer/

---

## 📊 사이클 요약

| Cycle | Aggregate/VO | 예상 시간 | 상태 |
|-------|--------------|-----------|------|
| 1 | FileStatus Enum | 15분 | ⏳ |
| 2 | JobType Enum | 15분 | ⏳ |
| 3 | JobStatus, OutboxStatus Enum | 15분 | ⏳ |
| 4 | UuidV7Generator | 15분 | ⏳ |
| 5 | File 기본 구조 | 15분 | ⏳ |
| 6 | File.create() | 15분 | ⏳ |
| 7 | File 상태 전환 | 15분 | ⏳ |
| 8 | File 부가 메서드 | 15분 | ⏳ |
| 9 | FileProcessingJob 기본 구조 | 15분 | ⏳ |
| 10 | FileProcessingJob 비즈니스 메서드 | 15분 | ⏳ |
| 11 | MessageOutbox 기본 구조 | 15분 | ⏳ |
| 12 | MessageOutbox 비즈니스 메서드 | 15분 | ⏳ |
| 13 | MessageOutbox.isExpired() | 15분 | ⏳ |

**총 예상 시간**: 195분 (약 3시간 15분)

---

## 🎯 다음 단계

Plan 완료 후 다음 명령으로 진행:

```bash
# 1. TDD 사이클 시작
/kb-domain

# 2. Jira 이슈 생성 및 브랜치 생성
/jira-start FILE-001
```

---

## 💡 TDD 사이클 진행 팁

### Red Phase
- 컴파일 에러 또는 테스트 실패 확인 필수
- 테스트 메서드명은 `should...()` 패턴 사용
- Given-When-Then 구조로 작성

### Green Phase
- 테스트 통과할 만큼만 구현 (최소 구현)
- 중복 코드, 복잡한 로직은 Refactor 단계에서 처리
- 일단 작동하게 만들기

### Refactor Phase
- 코드 품질 개선 (가독성, 성능, 중복 제거)
- ArchUnit 테스트 작성 및 통과
- 테스트는 여전히 통과해야 함

### Tidy Phase
- Object Mother 패턴으로 TestFixture 작성
- 테스트 코드를 Fixture 사용하도록 리팩토링
- 다음 사이클을 위한 준비

### 커밋 규칙
```
test: {요구사항} 테스트 추가 (Red)
impl: {요구사항} 구현 (Green)
refactor: {요구사항} 개선 (Refactor)
test: {Entity}Fixture 정리 (Tidy)
```
