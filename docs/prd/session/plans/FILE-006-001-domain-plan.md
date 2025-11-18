# FILE-006-001 TDD Plan

**Task**: Domain Layer - 파일 업로드 세션 및 파일 메타데이터 도메인 모델 구현
**Layer**: Domain Layer
**브랜치**: feature/FILE-006-001-domain
**예상 소요 시간**: 300분 (20 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ SessionId VO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [x] `SessionIdTest.java` 파일 생성
- [x] `shouldCreateNewSessionId()` 테스트 작성 (forNew() 메서드)
- [x] `shouldCreateFromValidUUID()` 테스트 작성 (from() 메서드)
- [x] `shouldThrowExceptionWhenInvalidUUID()` 테스트 작성
- [x] `shouldReturnTrueWhenIsNew()` 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: SessionId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SessionId.java` 생성 (Record)
- [x] `forNew()` 메서드 구현 (UUID.randomUUID())
- [x] `from(String value)` 메서드 구현
- [x] `isNew()` 메서드 구현
- [x] UUID 형식 검증 로직 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `impl: SessionId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] UUID 검증 로직 메서드 추출
- [x] Javadoc 추가
- [x] ArchUnit VO 테스트 통과 확인
- [x] 커밋: `refactor: SessionId VO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `SessionIdFixture.java` 생성 (Object Mother 패턴)
- [x] `SessionIdFixture.forNew()` 메서드 작성
- [x] `SessionIdFixture.from(String value)` 메서드 작성
- [x] `SessionIdTest` → Fixture 사용으로 리팩토링
- [x] 커밋: `test: SessionIdFixture 정리 (Tidy)`

---

### 2️⃣ FileName VO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [x] `FileNameTest.java` 생성
- [x] `shouldCreateFileNameWithExtension()` 테스트 작성
- [x] `shouldExtractExtensionCorrectly()` 테스트 작성
- [x] `shouldReturnWithoutExtension()` 테스트 작성
- [x] `shouldThrowExceptionWhenNull()` 테스트 작성
- [x] `shouldThrowExceptionWhenTooLong()` 테스트 작성 (>255자)
- [x] 커밋: `test: FileName VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileName.java` 생성 (Record)
- [x] `from(String value)` 메서드 구현
- [x] 확장자 추출 로직 구현
- [x] `withoutExtension()` 메서드 구현
- [x] null, 빈 문자열, 길이 검증
- [x] 테스트 통과
- [x] 커밋: `impl: FileName VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 확장자 추출 로직 메서드 분리
- [x] Javadoc 추가
- [x] ArchUnit VO 테스트 통과
- [x] 커밋: `refactor: FileName VO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `FileNameFixture.java` 생성
- [x] `FileNameFixture.from(String value)` 메서드 작성
- [x] 다양한 파일 이름 Fixture 메서드 추가 (image, html)
- [x] 커밋: `test: FileNameFixture 정리 (Tidy)`

---

### 3️⃣ FileSize VO 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `FileSizeTest.java` 생성
- [ ] `shouldCreateFileSize()` 테스트 작성
- [ ] `shouldThrowExceptionWhenZeroOrNegative()` 테스트 작성
- [ ] `shouldValidateForUploadType()` 테스트 작성 (SINGLE 5GB, MULTIPART 5TB)
- [ ] `shouldCompareSizeCorrectly()` 테스트 작성 (isLargerThan)
- [ ] `shouldConvertToMBAndGB()` 테스트 작성
- [ ] 커밋: `test: FileSize VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileSize.java` 생성 (Record)
- [ ] `of(long bytes)` 메서드 구현
- [ ] `validateForUploadType(UploadType)` 메서드 구현
- [ ] `isLargerThan(long threshold)` 메서드 구현
- [ ] `toMB()`, `toGB()` 메서드 구현
- [ ] 크기 검증 로직 추가
- [ ] 커밋: `impl: FileSize VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 단위 변환 상수 추출 (MB, GB)
- [ ] Javadoc 추가
- [ ] ArchUnit VO 테스트 통과
- [ ] 커밋: `refactor: FileSize VO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileSizeFixture.java` 생성
- [ ] `FileSizeFixture.of(long bytes)` 메서드 작성
- [ ] 다양한 크기 Fixture 메서드 추가 (1MB, 100MB, 1GB)
- [ ] 커밋: `test: FileSizeFixture 정리 (Tidy)`

---

### 4️⃣ MimeType VO 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `MimeTypeTest.java` 생성
- [ ] `shouldCreateAllowedMimeTypes()` 테스트 작성 (image/*, text/html)
- [ ] `shouldThrowExceptionForUnsupportedType()` 테스트 작성
- [ ] `shouldExtractExtensionCorrectly()` 테스트 작성
- [ ] `shouldCheckIsImage()` 테스트 작성
- [ ] `shouldCheckIsHtml()` 테스트 작성
- [ ] 커밋: `test: MimeType VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `MimeType.java` 생성 (Record)
- [ ] `of(String value)` 메서드 구현
- [ ] 허용 타입 검증 로직 구현
- [ ] `extractExtension()` 메서드 구현
- [ ] `isImage()`, `isHtml()` 메서드 구현
- [ ] `UnsupportedFileTypeException` 예외 던지기
- [ ] 커밋: `impl: MimeType VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 허용 타입 상수 추출 (ALLOWED_PATTERNS)
- [ ] 패턴 매칭 로직 메서드 분리
- [ ] Javadoc 추가
- [ ] ArchUnit VO 테스트 통과
- [ ] 커밋: `refactor: MimeType VO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MimeTypeFixture.java` 생성
- [ ] `MimeTypeFixture.of(String value)` 메서드 작성
- [ ] 다양한 MIME 타입 Fixture 메서드 추가 (jpeg, png, html)
- [ ] 커밋: `test: MimeTypeFixture 정리 (Tidy)`

---

### 5️⃣ UserRole Enum 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `UserRoleTest.java` 생성
- [ ] `shouldReturnCorrectNamespace()` 테스트 작성
- [ ] 각 Role별 네임스페이스 검증 테스트 작성
- [ ] 커밋: `test: UserRole Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserRole.java` 생성 (Enum)
- [ ] ADMIN("connectly"), SELLER("setof"), DEFAULT("setof") 정의
- [ ] `getNamespace()` 메서드 구현
- [ ] 커밋: `impl: UserRole Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Enum 테스트 통과
- [ ] 커밋: `refactor: UserRole Enum 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Enum은 자체적으로 상수)
- [ ] 테스트 코드 간소화
- [ ] 커밋: `test: UserRole 테스트 정리 (Tidy)`

---

### 6️⃣ UploadType Enum 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `UploadTypeTest.java` 생성
- [ ] `shouldReturnCorrectMaxSize()` 테스트 작성
- [ ] SINGLE(5GB), MULTIPART(5TB) 검증 테스트 작성
- [ ] 커밋: `test: UploadType Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadType.java` 생성 (Enum)
- [ ] SINGLE(5GB), MULTIPART(5TB) 정의
- [ ] `getMaxSize()` 메서드 구현
- [ ] 커밋: `impl: UploadType Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 바이트 계산 상수화
- [ ] 커밋: `refactor: UploadType Enum 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Enum은 자체적으로 상수)
- [ ] 커밋: `test: UploadType 테스트 정리 (Tidy)`

---

### 7️⃣ SessionStatus Enum 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `SessionStatusTest.java` 생성
- [ ] `shouldTransitionCorrectly()` 테스트 작성
- [ ] 상태 전환 규칙 검증 (PREPARING → ACTIVE → {COMPLETED, EXPIRED, FAILED})
- [ ] 불가능한 전환 테스트 작성
- [ ] 커밋: `test: SessionStatus Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SessionStatus.java` 생성 (Enum)
- [ ] PREPARING, ACTIVE, COMPLETED, EXPIRED, FAILED 정의
- [ ] `canTransitionTo(SessionStatus next)` 메서드 구현
- [ ] 상태 전환 규칙 구현 (switch 표현식)
- [ ] 커밋: `impl: SessionStatus Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] switch 표현식 최적화
- [ ] 커밋: `refactor: SessionStatus Enum 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Enum은 자체적으로 상수)
- [ ] 커밋: `test: SessionStatus 테스트 정리 (Tidy)`

---

### 8️⃣ S3Path VO 구현 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `S3PathTest.java` 생성
- [ ] `shouldCreateAdminPath()` 테스트 작성
- [ ] `shouldCreateSellerPath()` 테스트 작성
- [ ] `shouldCreateDefaultPath()` 테스트 작성
- [ ] `shouldExtractExtensionFromMimeType()` 테스트 작성
- [ ] `shouldGenerateFullPath()` 테스트 작성
- [ ] 커밋: `test: S3Path VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `S3Path.java` 생성 (Record)
- [ ] `from(UserRole, Long, String, String, String, String)` 메서드 구현
- [ ] `getFullPath()` 메서드 구현
- [ ] `extractExtension(String mimeType)` 메서드 구현
- [ ] Role별 네임스페이스 로직 구현
- [ ] 커밋: `impl: S3Path VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 경로 생성 로직 명확화
- [ ] Javadoc 추가
- [ ] ArchUnit VO 테스트 통과
- [ ] 커밋: `refactor: S3Path VO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `S3PathFixture.java` 생성
- [ ] `S3PathFixture.from(UserRole, ...)` 메서드 작성
- [ ] Role별 Fixture 메서드 추가
- [ ] 커밋: `test: S3PathFixture 정리 (Tidy)`

---

### 9️⃣ Domain Exception: SessionErrorCode Enum (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `SessionErrorCodeTest.java` 생성
- [ ] 각 ErrorCode의 code, message, httpStatus 검증 테스트 작성
- [ ] 커밋: `test: SessionErrorCode Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SessionErrorCode.java` 생성 (Enum)
- [ ] FILE_SIZE_EXCEEDED, UNSUPPORTED_FILE_TYPE, INVALID_SESSION_STATUS, SESSION_EXPIRED 정의
- [ ] Getter 메서드 구현
- [ ] 커밋: `impl: SessionErrorCode Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: SessionErrorCode Enum 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Enum)
- [ ] 커밋: `test: SessionErrorCode 테스트 정리 (Tidy)`

---

### 🔟 Domain Exception: DomainException 기본 클래스 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `DomainExceptionTest.java` 생성
- [ ] `shouldCreateExceptionWithErrorCode()` 테스트 작성
- [ ] `shouldReturnCorrectHttpStatus()` 테스트 작성
- [ ] 커밋: `test: DomainException 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `DomainException.java` 생성 (abstract class extends RuntimeException)
- [ ] errorCode, httpStatus 필드 추가
- [ ] protected 생성자 구현
- [ ] Getter 메서드 구현
- [ ] 커밋: `impl: DomainException 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Exception 테스트 통과
- [ ] 커밋: `refactor: DomainException 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (구체적인 예외 클래스에서 사용)
- [ ] 커밋: `test: DomainException 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ Domain Exception: FileSizeExceededException (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `FileSizeExceededExceptionTest.java` 생성
- [ ] `shouldCreateExceptionWithCorrectMessage()` 테스트 작성
- [ ] `shouldReturnHttpStatus400()` 테스트 작성
- [ ] 커밋: `test: FileSizeExceededException 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileSizeExceededException.java` 생성 (extends DomainException)
- [ ] 생성자 구현 (actualSize, maxSize 파라미터)
- [ ] 메시지 포맷팅
- [ ] 커밋: `impl: FileSizeExceededException 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: FileSizeExceededException 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (예외는 테스트에서 직접 생성)
- [ ] 커밋: `test: FileSizeExceededException 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ Domain Exception: 나머지 3종 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `UnsupportedFileTypeExceptionTest.java` 생성
- [ ] `InvalidSessionStatusExceptionTest.java` 생성
- [ ] `SessionExpiredExceptionTest.java` 생성
- [ ] 각 예외의 메시지, HTTP Status 검증 테스트 작성
- [ ] 커밋: `test: 나머지 Domain Exception 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UnsupportedFileTypeException.java` 생성
- [ ] `InvalidSessionStatusException.java` 생성
- [ ] `SessionExpiredException.java` 생성
- [ ] 각 예외의 생성자 및 메시지 포맷팅 구현
- [ ] 커밋: `impl: 나머지 Domain Exception 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Exception 테스트 통과
- [ ] 커밋: `refactor: Domain Exception 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Domain Exception 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ UploadSession Aggregate: 생성자 및 forNew() (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionTest.java` 생성
- [ ] `shouldCreateNewSessionWithForNew()` 테스트 작성
- [ ] `shouldValidateFileSizeForUploadType()` 테스트 작성
- [ ] `shouldValidateMimeType()` 테스트 작성
- [ ] `shouldSetExpiresAt15Minutes()` 테스트 작성
- [ ] `shouldInitializeStatusAsPreparing()` 테스트 작성
- [ ] 커밋: `test: UploadSession forNew() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSession.java` 생성 (Plain Java Class)
- [ ] private 생성자 구현
- [ ] `forNew(...)` 정적 메서드 구현
- [ ] Clock 필드 추가 및 주입
- [ ] createdAt, updatedAt = LocalDateTime.now(clock)
- [ ] expiresAt = createdAt + 15분
- [ ] 파일 크기, 타입 검증 로직
- [ ] 커밋: `impl: UploadSession forNew() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 검증 로직 메서드 추출
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과 (private 생성자, forNew() 필수)
- [ ] 커밋: `refactor: UploadSession forNew() 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UploadSessionFixture.java` 생성
- [ ] `UploadSessionFixture.forNew()` 메서드 작성
- [ ] `UploadSessionTest` → Fixture 사용
- [ ] 커밋: `test: UploadSessionFixture 정리 (Tidy)`

---

### 1️⃣4️⃣ UploadSession Aggregate: of() 및 reconstitute() (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateSessionWithOf()` 테스트 작성
- [ ] `shouldReconstituteSession()` 테스트 작성
- [ ] reconstitute()는 검증 로직 실행하지 않음 확인
- [ ] 커밋: `test: UploadSession of(), reconstitute() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `of(SessionId sessionId, ...)` 정적 메서드 구현
- [ ] `reconstitute(...)` 정적 메서드 구현
- [ ] 모든 필드 파라미터로 받기 (createdAt, updatedAt 포함)
- [ ] 커밋: `impl: UploadSession of(), reconstitute() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과 (of(), reconstitute() 필수)
- [ ] 커밋: `refactor: UploadSession 정적 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UploadSessionFixture.of(SessionId)` 메서드 추가
- [ ] `UploadSessionFixture.reconstitute(...)` 메서드 추가
- [ ] 커밋: `test: UploadSessionFixture 정적 메서드 추가 (Tidy)`

---

### 1️⃣5️⃣ UploadSession Aggregate: 상태 전환 메서드 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldActivateSession()` 테스트 작성 (PREPARING → ACTIVE)
- [ ] `shouldCompleteSession()` 테스트 작성 (ACTIVE → COMPLETED)
- [ ] `shouldExpireSession()` 테스트 작성 (ACTIVE → EXPIRED)
- [ ] `shouldFailSession()` 테스트 작성 (ACTIVE → FAILED)
- [ ] `shouldThrowExceptionWhenInvalidTransition()` 테스트 작성
- [ ] updatedAt 자동 갱신 확인 테스트
- [ ] 커밋: `test: UploadSession 상태 전환 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `activate()` 메서드 구현
- [ ] `complete()` 메서드 구현
- [ ] `expire()` 메서드 구현
- [ ] `fail()` 메서드 구현
- [ ] 각 메서드에서 `this.updatedAt = LocalDateTime.now(clock)` 필수
- [ ] 상태 전환 가능 여부 검증
- [ ] 커밋: `impl: UploadSession 상태 전환 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상태 전환 검증 로직 메서드 추출
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: UploadSession 상태 전환 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 다양한 상태의 UploadSession Fixture 메서드 추가
- [ ] `withStatusActive()`, `withStatusCompleted()` 등
- [ ] 커밋: `test: UploadSessionFixture 상태 메서드 추가 (Tidy)`

---

### 1️⃣6️⃣ UploadSession Aggregate: Tell Don't Ask 메서드 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCheckIsExpired()` 테스트 작성
- [ ] `shouldCheckCanComplete()` 테스트 작성
- [ ] `shouldCheckIsActive()` 테스트 작성
- [ ] `shouldCheckIsPreparing()` 테스트 작성
- [ ] `shouldCheckCanActivate()` 테스트 작성
- [ ] 커밋: `test: UploadSession Tell Don't Ask 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `isExpired()` 메서드 구현
- [ ] `canComplete()` 메서드 구현
- [ ] `isActive()` 메서드 구현
- [ ] `isPreparing()` 메서드 구현
- [ ] `canActivate()` 메서드 구현
- [ ] 커밋: `impl: UploadSession Tell Don't Ask 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과 (is*, can* 메서드 확인)
- [ ] 커밋: `refactor: UploadSession Tell Don't Ask 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 간소화
- [ ] 커밋: `test: UploadSession Tell Don't Ask 테스트 정리 (Tidy)`

---

### 1️⃣7️⃣ UploadSession Aggregate: Law of Demeter 메서드 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGetSessionIdValue()` 테스트 작성
- [ ] `getSessionIdValue()` 반환값 검증 (String)
- [ ] 커밋: `test: UploadSession Law of Demeter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `getSessionIdValue()` 메서드 구현 (return sessionId.value())
- [ ] 커밋: `impl: UploadSession Law of Demeter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과 (getIdValue() 필수)
- [ ] 커밋: `refactor: UploadSession Law of Demeter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 코드 간소화
- [ ] 커밋: `test: UploadSession Law of Demeter 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ File Aggregate: forNew(), of(), reconstitute() (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `FileTest.java` 생성
- [ ] `shouldCreateNewFileWithForNew()` 테스트 작성
- [ ] `shouldCreateFileWithOf()` 테스트 작성
- [ ] `shouldReconstituteFile()` 테스트 작성
- [ ] uploadedAt, updatedAt 자동 설정 확인
- [ ] 커밋: `test: File Aggregate 정적 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `File.java` 생성 (Plain Java Class)
- [ ] private 생성자 구현
- [ ] `forNew(...)` 정적 메서드 구현
- [ ] `of(SessionId fileId, ...)` 정적 메서드 구현
- [ ] `reconstitute(...)` 정적 메서드 구현
- [ ] Clock 주입 및 uploadedAt, updatedAt 설정
- [ ] 커밋: `impl: File Aggregate 정적 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과 (private 생성자, forNew(), of(), reconstitute() 필수)
- [ ] 커밋: `refactor: File Aggregate 정적 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileFixture.java` 생성
- [ ] `FileFixture.forNew()` 메서드 작성
- [ ] `FileFixture.of(SessionId)` 메서드 작성
- [ ] `FileFixture.reconstitute(...)` 메서드 작성
- [ ] 커밋: `test: FileFixture 정리 (Tidy)`

---

### 1️⃣9️⃣ File Aggregate: delete() 메서드 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDeleteFile()` 테스트 작성
- [ ] deleted = true, deletedAt 설정 확인
- [ ] updatedAt 자동 갱신 확인
- [ ] 커밋: `test: File delete() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `delete()` 메서드 구현
- [ ] deleted = true
- [ ] deletedAt = LocalDateTime.now(clock)
- [ ] updatedAt = LocalDateTime.now(clock)
- [ ] 커밋: `impl: File delete() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: File delete() 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileFixture.deleted()` 메서드 추가 (삭제된 파일 Fixture)
- [ ] 커밋: `test: FileFixture delete 메서드 추가 (Tidy)`

---

### 2️⃣0️⃣ File Aggregate: Tell Don't Ask 및 Law of Demeter (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCheckIsDeleted()` 테스트 작성
- [ ] `shouldCheckCanDelete()` 테스트 작성
- [ ] `shouldGetFileIdValue()` 테스트 작성
- [ ] 커밋: `test: File Tell Don't Ask 및 Law of Demeter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `isDeleted()` 메서드 구현
- [ ] `canDelete()` 메서드 구현 (이미 삭제된 경우 false)
- [ ] `getFileIdValue()` 메서드 구현 (return fileId.value())
- [ ] 커밋: `impl: File Tell Don't Ask 및 Law of Demeter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Aggregate ArchUnit 테스트 통과
- [ ] 커밋: `refactor: File 메서드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 정리
- [ ] 커밋: `test: File Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (20 사이클 × 4단계 = 80 체크박스)
- [ ] 모든 테스트 통과
- [ ] ArchUnit 테스트 통과 (32개 규칙)
  - Aggregate 규칙 (24개)
  - Value Object 규칙 (8개)
- [ ] Zero-Tolerance 규칙 준수
  - Lombok 금지
  - Long FK 전략
  - Law of Demeter
  - Tell Don't Ask
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 테스트 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/session/FILE-006-001.md
- PRD: /Users/sangwon-ryu/fileflow/docs/prd/presigned-url-upload.md
- Domain Layer 규칙: docs/coding_convention/02-domain-layer/

---

## 📊 사이클 요약

**총 사이클 수**: 20
**예상 소요 시간**: 300분 (5시간)
**Red 단계**: 20개
**Green 단계**: 20개
**Refactor 단계**: 20개
**Tidy 단계**: 20개

**레이어별 분류**:
- Value Objects: 8 사이클
- Enums: 4 사이클
- Domain Exceptions: 4 사이클
- UploadSession Aggregate: 5 사이클
- File Aggregate: 3 사이클

---

## 🔧 다음 단계

1. `/kb/domain/go` - TDD 사이클 시작 (자동으로 다음 체크박스 진행)
2. 각 사이클마다 4단계 커밋 (test: → impl: → refactor: → test:)
3. 모든 사이클 완료 후 FILE-006-002 (Application Layer) 시작
