# FILE-006-002 TDD Plan

**Task**: Application Layer - 파일 업로드 세션 및 파일 관리 비즈니스 로직 구현
**Layer**: Application Layer
**브랜치**: feature/FILE-006-002-application
**예상 소요 시간**: 510분 (34 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ UserContext DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `UserContextTest.java` 파일 생성
- [ ] `shouldCreateUserContext()` 테스트 작성
- [ ] `shouldCreateFromJwtUser()` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UserContext DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UserContext.java` 생성 (Record)
- [ ] 4개 필드 정의 (userId, tenantId, role, sellerName)
- [ ] `from(JwtUser)` 정적 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserContext DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit DTO 테스트 통과 확인 (Record 사용)
- [ ] 커밋: `refactor: UserContext DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UserContextFixture.java` 생성
- [ ] `UserContextFixture.admin()` 메서드 작성
- [ ] `UserContextFixture.seller(String sellerName)` 메서드 작성
- [ ] `UserContextFixture.defaultUser()` 메서드 작성
- [ ] 커밋: `test: UserContextFixture 정리 (Tidy)`

---

### 2️⃣ Command DTO 3종 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `PrepareUploadCommandTest.java` 생성
- [ ] `CompleteUploadCommandTest.java` 생성
- [ ] `AbortUploadCommandTest.java` 생성
- [ ] 각 Command 생성 테스트 작성
- [ ] 커밋: `test: Command DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `PrepareUploadCommand.java` 생성 (Record)
- [ ] `CompleteUploadCommand.java` 생성 (Record)
- [ ] `AbortUploadCommand.java` 생성 (Record)
- [ ] 테스트 통과
- [ ] 커밋: `impl: Command DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Command DTO 테스트 통과
- [ ] 커밋: `refactor: Command DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `PrepareUploadCommandFixture.java` 생성
- [ ] `CompleteUploadCommandFixture.java` 생성
- [ ] `AbortUploadCommandFixture.java` 생성
- [ ] 커밋: `test: Command DTO Fixture 정리 (Tidy)`

---

### 3️⃣ Query DTO 3종 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `GetUploadSessionQueryTest.java` 생성
- [ ] `GetFileQueryTest.java` 생성
- [ ] `ListFilesQueryTest.java` 생성
- [ ] 각 Query 생성 테스트 작성
- [ ] 커밋: `test: Query DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetUploadSessionQuery.java` 생성 (Record)
- [ ] `GetFileQuery.java` 생성 (Record)
- [ ] `ListFilesQuery.java` 생성 (Record)
- [ ] 테스트 통과
- [ ] 커밋: `impl: Query DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Query DTO 테스트 통과
- [ ] 커밋: `refactor: Query DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `GetUploadSessionQueryFixture.java` 생성
- [ ] `GetFileQueryFixture.java` 생성
- [ ] `ListFilesQueryFixture.java` 생성
- [ ] 커밋: `test: Query DTO Fixture 정리 (Tidy)`

---

### 4️⃣ SessionPreparationResult DTO 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `SessionPreparationResultTest.java` 생성
- [ ] `shouldCreateForSingle()` 테스트 작성
- [ ] `shouldCreateForMultipart()` 테스트 작성
- [ ] PartUploadUrl 중첩 Record 테스트 작성
- [ ] 커밋: `test: SessionPreparationResult DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SessionPreparationResult.java` 생성 (Record)
- [ ] `PartUploadUrl` 중첩 Record 정의
- [ ] `forSingle()` 정적 메서드 구현
- [ ] `forMultipart()` 정적 메서드 구현
- [ ] 테스트 통과
- [ ] 커밋: `impl: SessionPreparationResult DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Factory Method 패턴 명확화
- [ ] ArchUnit Response DTO 테스트 통과
- [ ] 커밋: `refactor: SessionPreparationResult DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SessionPreparationResultFixture.java` 생성
- [ ] `forSingle()` Fixture 메서드 작성
- [ ] `forMultipart()` Fixture 메서드 작성
- [ ] 커밋: `test: SessionPreparationResult Fixture 정리 (Tidy)`

---

### 5️⃣ Response DTO 5종 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionResponseTest.java` 생성
- [ ] `FileResponseTest.java` 생성
- [ ] `FileDetailResponseTest.java` 생성
- [ ] `FileSummaryResponseTest.java` 생성
- [ ] 각 Response 생성 테스트 작성
- [ ] 커밋: `test: Response DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSessionResponse.java` 생성 (Record)
- [ ] `FileResponse.java` 생성 (Record)
- [ ] `FileDetailResponse.java` 생성 (Record)
- [ ] `FileSummaryResponse.java` 생성 (Record)
- [ ] 테스트 통과
- [ ] 커밋: `impl: Response DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] @Nullable 필드 표시
- [ ] ArchUnit Response DTO 테스트 통과
- [ ] 커밋: `refactor: Response DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UploadSessionResponseFixture.java` 생성
- [ ] `FileResponseFixture.java` 생성
- [ ] `FileDetailResponseFixture.java` 생성
- [ ] `FileSummaryResponseFixture.java` 생성
- [ ] 커밋: `test: Response DTO Fixture 정리 (Tidy)`

---

### 6️⃣ In Port 인터페이스 6종 정의 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] In Port 인터페이스는 테스트 불필요 (구현은 UseCase에서)
- [ ] ArchUnit 테스트만 작성
- [ ] 커밋: `test: In Port 인터페이스 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `PrepareUploadInPort.java` 생성 (인터페이스)
- [ ] `CompleteUploadInPort.java` 생성
- [ ] `AbortUploadInPort.java` 생성
- [ ] `GetUploadSessionInPort.java` 생성
- [ ] `GetFileInPort.java` 생성
- [ ] `ListFilesInPort.java` 생성
- [ ] 각 인터페이스 메서드 정의
- [ ] 커밋: `impl: In Port 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit In Port 테스트 통과
- [ ] 커밋: `refactor: In Port 인터페이스 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (인터페이스)
- [ ] 커밋: `test: In Port 정리 (Tidy)`

---

### 7️⃣ Out Port 인터페이스 8종 정의 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] Out Port 인터페이스는 테스트 불필요
- [ ] ArchUnit 테스트만 작성
- [ ] 커밋: `test: Out Port 인터페이스 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `LoadUploadSessionPort.java` 생성 (인터페이스)
- [ ] `SaveUploadSessionPort.java` 생성
- [ ] `DeleteUploadSessionPort.java` 생성
- [ ] `LoadFilePort.java` 생성
- [ ] `SaveFilePort.java` 생성
- [ ] `S3PresignedUrlPort.java` 생성
- [ ] `S3MultipartPort.java` 생성
- [ ] 각 인터페이스 메서드 정의
- [ ] 커밋: `impl: Out Port 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Out Port 테스트 통과
- [ ] 커밋: `refactor: Out Port 인터페이스 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (인터페이스)
- [ ] 커밋: `test: Out Port 정리 (Tidy)`

---

### 8️⃣ UploadSessionAssembler 구현 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionAssemblerTest.java` 생성
- [ ] `shouldConvertCommandToDomain()` 테스트 작성
- [ ] PrepareUploadCommand → UploadSession 변환 검증
- [ ] VO 변환 검증 (SessionId, FileName, FileSize, MimeType)
- [ ] 커밋: `test: UploadSessionAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSessionAssembler.java` 생성 (@Component)
- [ ] `toDomain(PrepareUploadCommand)` 메서드 구현 (Instance 메서드)
- [ ] 원시 타입 → VO 변환 로직
- [ ] UploadSession.forNew() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: UploadSessionAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Static 메서드 사용하지 않았는지 확인 (assembler-guide.md)
- [ ] ArchUnit Assembler 테스트 통과
- [ ] 커밋: `refactor: UploadSessionAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Assembler는 @Component Bean)
- [ ] 커밋: `test: UploadSessionAssembler 테스트 정리 (Tidy)`

---

### 9️⃣ FileAssembler 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssemblerTest.java` 생성
- [ ] `shouldConvertToResponse()` 테스트 작성
- [ ] `shouldConvertToDetailResponse()` 테스트 작성
- [ ] `shouldConvertToSummaryResponse()` 테스트 작성
- [ ] Law of Demeter 준수 검증 (getFileIdValue() 사용)
- [ ] 커밋: `test: FileAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssembler.java` 생성 (@Component)
- [ ] `toResponse(File)` 메서드 구현 (Instance 메서드)
- [ ] `toDetailResponse(File)` 메서드 구현
- [ ] `toSummaryResponse(File)` 메서드 구현
- [ ] Law of Demeter 준수 (file.getFileIdValue() 사용)
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Static 메서드 사용하지 않았는지 확인
- [ ] ArchUnit Assembler 테스트 통과
- [ ] 커밋: `refactor: FileAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileAssembler 테스트 정리 (Tidy)`

---

### 🔟 UploadSessionResponseAssembler 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionResponseAssemblerTest.java` 생성
- [ ] `shouldConvertToResponse()` 테스트 작성
- [ ] Law of Demeter 준수 검증 (getSessionIdValue() 사용)
- [ ] 커밋: `test: UploadSessionResponseAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSessionResponseAssembler.java` 생성 (@Component)
- [ ] `toResponse(UploadSession)` 메서드 구현 (Instance 메서드)
- [ ] Law of Demeter 준수
- [ ] 테스트 통과
- [ ] 커밋: `impl: UploadSessionResponseAssembler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Assembler 테스트 통과
- [ ] 커밋: `refactor: UploadSessionResponseAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: UploadSessionResponseAssembler 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ GetUploadSessionUseCase 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `GetUploadSessionUseCaseTest.java` 생성
- [ ] `shouldGetSessionSuccessfully()` 테스트 작성
- [ ] `shouldThrowExceptionWhenSessionNotFound()` 테스트 작성
- [ ] `shouldThrowExceptionWhenUnauthorized()` 테스트 작성
- [ ] Mock Port 준비 (LoadUploadSessionPort)
- [ ] 커밋: `test: GetUploadSessionUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetUploadSessionUseCase.java` 생성
- [ ] GetUploadSessionInPort 구현
- [ ] @Transactional(readOnly = true) 추가
- [ ] Redis 조회 로직
- [ ] 권한 체크 로직
- [ ] Assembler 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: GetUploadSessionUseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 예외 처리 개선
- [ ] ArchUnit UseCase 테스트 통과
- [ ] 커밋: `refactor: GetUploadSessionUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (UseCase는 Mock 사용)
- [ ] 커밋: `test: GetUploadSessionUseCase 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ GetFileUseCase 구현 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `GetFileUseCaseTest.java` 생성
- [ ] `shouldGetFileSuccessfully()` 테스트 작성
- [ ] `shouldThrowExceptionWhenFileNotFound()` 테스트 작성
- [ ] `shouldThrowExceptionWhenUnauthorized()` 테스트 작성
- [ ] Mock Port 준비 (LoadFilePort)
- [ ] 커밋: `test: GetFileUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetFileUseCase.java` 생성
- [ ] GetFileInPort 구현
- [ ] @Transactional(readOnly = true) 추가
- [ ] File 조회 로직
- [ ] 권한 체크 로직
- [ ] Assembler 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: GetFileUseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit UseCase 테스트 통과
- [ ] 커밋: `refactor: GetFileUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: GetFileUseCase 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ ListFilesUseCase 구현 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `ListFilesUseCaseTest.java` 생성
- [ ] `shouldListFilesSuccessfully()` 테스트 작성
- [ ] `shouldReturnEmptyListWhenNoFiles()` 테스트 작성
- [ ] Cursor-based 페이징 검증
- [ ] Mock Port 준비 (LoadFilePort)
- [ ] 커밋: `test: ListFilesUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListFilesUseCase.java` 생성
- [ ] ListFilesInPort 구현
- [ ] @Transactional(readOnly = true) 추가
- [ ] File 목록 조회 로직
- [ ] 페이징 처리
- [ ] Assembler 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: ListFilesUseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 페이징 로직 최적화
- [ ] ArchUnit UseCase 테스트 통과
- [ ] 커밋: `refactor: ListFilesUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: ListFilesUseCase 테스트 정리 (Tidy)`

---

### 1️⃣4️⃣ PrepareUploadUseCase 구현 (Part 1: 멱등성 체크) (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `PrepareUploadUseCaseTest.java` 생성
- [ ] `shouldReturnExistingSessionWhenIdempotent()` 테스트 작성
- [ ] Redis 조회 Mock 설정
- [ ] 커밋: `test: PrepareUploadUseCase 멱등성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `PrepareUploadUseCase.java` 생성
- [ ] PrepareUploadInPort 구현
- [ ] @Transactional 추가
- [ ] Redis 멱등성 체크 로직 (LoadUploadSessionPort.findById)
- [ ] 기존 세션 반환 로직
- [ ] 테스트 통과
- [ ] 커밋: `impl: PrepareUploadUseCase 멱등성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 멱등성 로직 메서드 추출
- [ ] 커밋: `refactor: PrepareUploadUseCase 멱등성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: PrepareUploadUseCase 멱등성 테스트 정리 (Tidy)`

---

### 1️⃣5️⃣ PrepareUploadUseCase 구현 (Part 2: 세션 생성) (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateNewSessionSuccessfully()` 테스트 작성
- [ ] UploadSession Aggregate 생성 검증
- [ ] Redis 저장 검증 (TTL 15분)
- [ ] 커밋: `test: PrepareUploadUseCase 세션 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Assembler 사용 (PrepareUploadCommand → UploadSession)
- [ ] UploadSession.forNew() 호출
- [ ] Redis 저장 로직 (SaveUploadSessionPort.save, TTL 15분)
- [ ] 테스트 통과
- [ ] 커밋: `impl: PrepareUploadUseCase 세션 생성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 세션 생성 로직 메서드 추출
- [ ] TTL 상수화 (15분)
- [ ] 커밋: `refactor: PrepareUploadUseCase 세션 생성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: PrepareUploadUseCase 세션 생성 테스트 정리 (Tidy)`

---

### 1️⃣6️⃣ PrepareUploadUseCase 구현 (Part 3: Presigned URL 생성 - SINGLE) (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGenerateSinglePresignedUrl()` 테스트 작성
- [ ] 트랜잭션 커밋 후 S3 API 호출 검증
- [ ] SessionPreparationResult.forSingle() 반환 검증
- [ ] 커밋: `test: PrepareUploadUseCase SINGLE URL 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] S3PresignedUrlPort.generatePutUrl() 호출 (트랜잭션 밖)
- [ ] SessionPreparationResult.forSingle() 반환
- [ ] 테스트 통과
- [ ] 커밋: `impl: PrepareUploadUseCase SINGLE URL 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Presigned URL 생성 로직 메서드 추출
- [ ] Transaction 경계 명확화 (주석 추가)
- [ ] 커밋: `refactor: PrepareUploadUseCase SINGLE URL 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: PrepareUploadUseCase SINGLE URL 테스트 정리 (Tidy)`

---

### 1️⃣7️⃣ PrepareUploadUseCase 구현 (Part 4: Presigned URL 생성 - MULTIPART) (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGenerateMultipartPresignedUrls()` 테스트 작성
- [ ] S3 Multipart Initiate + Part URLs 생성 검증
- [ ] SessionPreparationResult.forMultipart() 반환 검증
- [ ] 커밋: `test: PrepareUploadUseCase MULTIPART URL 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] S3PresignedUrlPort.initiateMultipartUpload() 호출
- [ ] S3PresignedUrlPort.generatePartUploadUrls() 호출 (Part 10개)
- [ ] SessionPreparationResult.forMultipart() 반환
- [ ] 테스트 통과
- [ ] 커밋: `impl: PrepareUploadUseCase MULTIPART URL 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Multipart URL 생성 로직 메서드 추출
- [ ] Part 개수 상수화 (10개)
- [ ] 커밋: `refactor: PrepareUploadUseCase MULTIPART URL 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: PrepareUploadUseCase MULTIPART URL 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ PrepareUploadUseCase 구현 (Part 5: 예외 시나리오) (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenFileSizeExceeded()` 테스트 작성
- [ ] `shouldThrowExceptionWhenUnsupportedFileType()` 테스트 작성
- [ ] `shouldHandleS3ApiFailure()` 테스트 작성
- [ ] 커밋: `test: PrepareUploadUseCase 예외 시나리오 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] FileSizeExceededException 처리
- [ ] UnsupportedFileTypeException 처리
- [ ] S3 API 실패 시 예외 처리
- [ ] 테스트 통과
- [ ] 커밋: `impl: PrepareUploadUseCase 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 통합
- [ ] 로깅 추가
- [ ] 커밋: `refactor: PrepareUploadUseCase 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: PrepareUploadUseCase 예외 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ CompleteUploadUseCase 구현 (Part 1: 세션 조회 및 권한 체크) (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `CompleteUploadUseCaseTest.java` 생성
- [ ] `shouldCompleteUploadSuccessfully()` 테스트 작성
- [ ] `shouldThrowExceptionWhenSessionNotFound()` 테스트 작성
- [ ] `shouldThrowExceptionWhenUnauthorized()` 테스트 작성
- [ ] 커밋: `test: CompleteUploadUseCase 세션 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CompleteUploadUseCase.java` 생성
- [ ] CompleteUploadInPort 구현
- [ ] @Transactional 추가
- [ ] 세션 조회 로직 (LoadUploadSessionPort.findById)
- [ ] 권한 체크 로직
- [ ] 테스트 통과
- [ ] 커밋: `impl: CompleteUploadUseCase 세션 조회 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 권한 체크 메서드 추출
- [ ] 커밋: `refactor: CompleteUploadUseCase 세션 조회 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: CompleteUploadUseCase 세션 조회 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ CompleteUploadUseCase 구현 (Part 2: SINGLE 업로드 완료) (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCompleteSingleUpload()` 테스트 작성
- [ ] File Aggregate 생성 검증
- [ ] 세션 상태 변경 검증 (complete())
- [ ] Redis 세션 삭제 검증
- [ ] 커밋: `test: CompleteUploadUseCase SINGLE 완료 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] File.forNew() 호출
- [ ] SaveFilePort.save() 호출
- [ ] session.complete() 호출
- [ ] DeleteUploadSessionPort.delete() 호출
- [ ] Assembler 사용 (FileAssembler.toResponse)
- [ ] 테스트 통과
- [ ] 커밋: `impl: CompleteUploadUseCase SINGLE 완료 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SINGLE 완료 로직 메서드 추출
- [ ] 커밋: `refactor: CompleteUploadUseCase SINGLE 완료 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: CompleteUploadUseCase SINGLE 완료 테스트 정리 (Tidy)`

---

### 2️⃣1️⃣ CompleteUploadUseCase 구현 (Part 3: MULTIPART 업로드 완료) (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCompleteMultipartUpload()` 테스트 작성
- [ ] S3 Complete Multipart Upload 호출 검증 (트랜잭션 밖)
- [ ] File Aggregate 생성 검증
- [ ] 세션 상태 변경 검증
- [ ] 커밋: `test: CompleteUploadUseCase MULTIPART 완료 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] S3MultipartPort.completeMultipartUpload() 호출 (트랜잭션 밖)
- [ ] File.forNew() 호출
- [ ] SaveFilePort.save() 호출 (Optimistic Lock)
- [ ] session.complete() 호출
- [ ] DeleteUploadSessionPort.delete() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: CompleteUploadUseCase MULTIPART 완료 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] MULTIPART 완료 로직 메서드 추출
- [ ] Transaction 경계 명확화 (주석 추가)
- [ ] 커밋: `refactor: CompleteUploadUseCase MULTIPART 완료 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: CompleteUploadUseCase MULTIPART 완료 테스트 정리 (Tidy)`

---

### 2️⃣2️⃣ CompleteUploadUseCase 구현 (Part 4: 예외 시나리오) (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenCannotComplete()` 테스트 작성
- [ ] `shouldHandleOptimisticLockException()` 테스트 작성
- [ ] `shouldHandleS3CompleteFailure()` 테스트 작성
- [ ] 커밋: `test: CompleteUploadUseCase 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] canComplete() 검증
- [ ] OptimisticLockException 처리
- [ ] S3 Complete Multipart Upload 실패 시 예외 처리
- [ ] 테스트 통과
- [ ] 커밋: `impl: CompleteUploadUseCase 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 통합
- [ ] 로깅 추가
- [ ] 커밋: `refactor: CompleteUploadUseCase 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: CompleteUploadUseCase 예외 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ AbortUploadUseCase 구현 (Part 1: SINGLE 취소) (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `AbortUploadUseCaseTest.java` 생성
- [ ] `shouldAbortSingleUpload()` 테스트 작성
- [ ] 세션 조회 및 권한 체크 검증
- [ ] 세션 상태 변경 검증 (fail())
- [ ] Redis 세션 삭제 검증
- [ ] 커밋: `test: AbortUploadUseCase SINGLE 취소 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `AbortUploadUseCase.java` 생성
- [ ] AbortUploadInPort 구현
- [ ] @Transactional 추가
- [ ] 세션 조회 및 권한 체크
- [ ] session.fail() 호출
- [ ] DeleteUploadSessionPort.delete() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: AbortUploadUseCase SINGLE 취소 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] SINGLE 취소 로직 명확화
- [ ] 커밋: `refactor: AbortUploadUseCase SINGLE 취소 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: AbortUploadUseCase SINGLE 취소 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ AbortUploadUseCase 구현 (Part 2: MULTIPART 취소) (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `shouldAbortMultipartUpload()` 테스트 작성
- [ ] S3 Abort Multipart Upload 호출 검증 (트랜잭션 밖)
- [ ] 세션 상태 변경 검증
- [ ] 커밋: `test: AbortUploadUseCase MULTIPART 취소 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] S3MultipartPort.abortMultipartUpload() 호출 (트랜잭션 밖)
- [ ] session.fail() 호출
- [ ] DeleteUploadSessionPort.delete() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: AbortUploadUseCase MULTIPART 취소 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] MULTIPART 취소 로직 메서드 추출
- [ ] Transaction 경계 명확화
- [ ] 커밋: `refactor: AbortUploadUseCase MULTIPART 취소 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: AbortUploadUseCase MULTIPART 취소 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ AbortUploadUseCase 구현 (Part 3: 예외 시나리오) (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenSessionNotFound()` 테스트 작성
- [ ] `shouldThrowExceptionWhenUnauthorized()` 테스트 작성
- [ ] `shouldHandleS3AbortFailure()` 테스트 작성
- [ ] 커밋: `test: AbortUploadUseCase 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 세션 없음 예외 처리
- [ ] 권한 없음 예외 처리
- [ ] S3 Abort 실패 시 예외 처리
- [ ] 테스트 통과
- [ ] 커밋: `impl: AbortUploadUseCase 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 통합
- [ ] 로깅 추가
- [ ] 커밋: `refactor: AbortUploadUseCase 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: AbortUploadUseCase 예외 테스트 정리 (Tidy)`

---

### 2️⃣6️⃣ UploadSessionExpiredListener 구현 (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionExpiredListenerTest.java` 생성
- [ ] `shouldHandleRedisKeyExpiredEvent()` 테스트 작성
- [ ] sessionId 추출 검증
- [ ] S3 Abort Multipart Upload 호출 검증
- [ ] 로그 기록 검증
- [ ] 커밋: `test: UploadSessionExpiredListener 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSessionExpiredListener.java` 생성
- [ ] @EventListener(RedisKeyExpiredEvent.class) 추가
- [ ] @Async 추가
- [ ] sessionId 추출 로직
- [ ] S3 Abort Multipart Upload 호출
- [ ] 로그 기록
- [ ] 테스트 통과
- [ ] 커밋: `impl: UploadSessionExpiredListener 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 비동기 처리 최적화
- [ ] 커밋: `refactor: UploadSessionExpiredListener 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: UploadSessionExpiredListener 테스트 정리 (Tidy)`

---

### 2️⃣7️⃣ S3UploadCompletedListener 구현 (선택적) (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `S3UploadCompletedListenerTest.java` 생성
- [ ] `shouldHandleS3ObjectCreatedEvent()` 테스트 작성
- [ ] S3 경로에서 sessionId 추출 검증
- [ ] CompleteUploadUseCase 호출 검증
- [ ] 커밋: `test: S3UploadCompletedListener 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `S3UploadCompletedListener.java` 생성
- [ ] @SqsListener(queues = "upload-completed-queue") 추가
- [ ] S3 경로 파싱 로직
- [ ] sessionId 추출
- [ ] CompleteUploadUseCase 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: S3UploadCompletedListener 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] S3 경로 파싱 로직 메서드 추출
- [ ] 커밋: `refactor: S3UploadCompletedListener 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: S3UploadCompletedListener 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ ExpiredSessionCleanupScheduler 구현 (Part 1: Redis SCAN) (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `ExpiredSessionCleanupSchedulerTest.java` 생성
- [ ] `shouldScanRedisForExpiredSessions()` 테스트 작성
- [ ] Redis SCAN 명령 검증 (KEYS 금지)
- [ ] 배치 크기 제한 검증 (최대 100개)
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler SCAN 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ExpiredSessionCleanupScheduler.java` 생성
- [ ] @Scheduled(cron = "0 */5 * * * *") 추가
- [ ] Redis SCAN 명령 사용
- [ ] 배치 크기 제한 (최대 100개)
- [ ] 테스트 통과
- [ ] 커밋: `impl: ExpiredSessionCleanupScheduler SCAN 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Redis SCAN 로직 메서드 추출
- [ ] 배치 크기 상수화
- [ ] 커밋: `refactor: ExpiredSessionCleanupScheduler SCAN 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler SCAN 테스트 정리 (Tidy)`

---

### 2️⃣9️⃣ ExpiredSessionCleanupScheduler 구현 (Part 2: 만료 세션 처리) (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldAbortExpiredSessions()` 테스트 작성
- [ ] 만료 여부 확인 검증 (expiresAt < now())
- [ ] AbortUploadUseCase 호출 검증
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler 만료 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 만료 여부 확인 로직
- [ ] AbortUploadUseCase 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: ExpiredSessionCleanupScheduler 만료 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 만료 처리 로직 메서드 추출
- [ ] 커밋: `refactor: ExpiredSessionCleanupScheduler 만료 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler 만료 처리 테스트 정리 (Tidy)`

---

### 3️⃣0️⃣ ExpiredSessionCleanupScheduler 구현 (Part 3: 예외 처리) (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandleRedisConnectionError()` 테스트 작성
- [ ] `shouldContinueOnAbortFailure()` 테스트 작성
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Redis 연결 오류 처리
- [ ] Abort 실패 시 로그 기록 후 계속 진행
- [ ] 테스트 통과
- [ ] 커밋: `impl: ExpiredSessionCleanupScheduler 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 통합
- [ ] 로깅 추가
- [ ] 커밋: `refactor: ExpiredSessionCleanupScheduler 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler 예외 테스트 정리 (Tidy)`

---

### 3️⃣1️⃣ Application Layer ArchUnit 테스트 (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `ApplicationLayerArchitectureTest.java` 생성
- [ ] Application Layer는 Domain에만 의존 테스트
- [ ] Port(Out) 인터페이스만 의존 테스트
- [ ] Adapter 직접 의존 금지 테스트
- [ ] CQRS 분리 검증 테스트
- [ ] 커밋: `test: Application Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 테스트 구현 완료
- [ ] 테스트 통과
- [ ] 커밋: `impl: Application Layer ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 커밋: `refactor: Application Layer ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Application Layer ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ Transaction 경계 ArchUnit 테스트 (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `TransactionBoundaryArchitectureTest.java` 생성
- [ ] @Transactional 내 S3 API 호출 금지 테스트
- [ ] Command UseCase는 @Transactional 필수 테스트
- [ ] Query UseCase는 @Transactional(readOnly = true) 필수 테스트
- [ ] 커밋: `test: Transaction 경계 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 테스트 구현 완료
- [ ] 테스트 통과
- [ ] 커밋: `impl: Transaction 경계 ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 커밋: `refactor: Transaction 경계 ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Transaction 경계 ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣3️⃣ Assembler ArchUnit 테스트 (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `AssemblerArchitectureTest.java` 생성
- [ ] Assembler는 @Component Bean 필수 테스트
- [ ] Static 메서드 금지 테스트
- [ ] Instance 메서드만 허용 테스트
- [ ] 커밋: `test: Assembler ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 테스트 구현 완료
- [ ] 테스트 통과
- [ ] 커밋: `impl: Assembler ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 커밋: `refactor: Assembler ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Assembler ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣4️⃣ 최종 통합 검증 (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] 모든 UseCase 통합 시나리오 테스트 작성
- [ ] PrepareUpload → CompleteUpload 전체 플로우 테스트
- [ ] PrepareUpload → AbortUpload 전체 플로우 테스트
- [ ] 커밋: `test: 최종 통합 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 통합 테스트 통과
- [ ] 커밋: `impl: 최종 통합 검증 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 전체 코드 리뷰 및 개선
- [ ] 중복 코드 제거
- [ ] Javadoc 보완
- [ ] 커밋: `refactor: Application Layer 최종 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 TestFixture 최종 정리
- [ ] 사용하지 않는 Fixture 메서드 제거
- [ ] 커밋: `test: Application Layer Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (34 사이클 × 4단계 = 136 체크박스)
- [ ] 모든 테스트 통과
- [ ] ArchUnit 테스트 통과
  - Application Layer 의존성 규칙
  - Transaction 경계 규칙
  - CQRS 분리 규칙
  - Assembler 규칙
- [ ] Zero-Tolerance 규칙 준수
  - Command/Query 분리 (CQRS)
  - Transaction 경계 엄격 관리
  - Assembler 사용 (Domain ↔ DTO 변환)
  - Port 의존성 (인터페이스만)
- [ ] 테스트 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/session/FILE-006-002.md
- PRD: /Users/sangwon-ryu/fileflow/docs/prd/presigned-url-upload.md
- Application Layer 규칙: docs/coding_convention/03-application-layer/

---

## 📊 사이클 요약

**총 사이클 수**: 34
**예상 소요 시간**: 510분 (8.5시간)
**Red 단계**: 34개
**Green 단계**: 34개
**Refactor 단계**: 34개
**Tidy 단계**: 34개

**레이어별 분류**:
- DTO (Command/Query/Response): 5 사이클
- Port (In/Out): 2 사이클
- Assembler: 3 사이클
- Query UseCase: 3 사이클
- PrepareUploadUseCase: 5 사이클
- CompleteUploadUseCase: 4 사이클
- AbortUploadUseCase: 3 사이클
- Event Listener: 2 사이클
- Scheduler: 3 사이클
- ArchUnit 테스트: 3 사이클
- 최종 통합 검증: 1 사이클

---

## 🔧 다음 단계

1. `/kb/application/go` - TDD 사이클 시작 (자동으로 다음 체크박스 진행)
2. 각 사이클마다 4단계 커밋 (test: → impl: → refactor: → test:)
3. 모든 사이클 완료 후 FILE-006-003 (Persistence Layer) 시작
