# FILE-004 TDD Plan

**Task**: REST API Layer 구현
**Layer**: Adapter-In Layer (REST API)
**브랜치**: feature/FILE-004-rest-api
**예상 소요 시간**: 900분 (60 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: Request DTO 구현 (12 사이클)

---

### 1️⃣ GeneratePresignedUrlRequest - 기본 필드 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `adapter-in-rest-api/src/test/java/.../dto/request/GeneratePresignedUrlRequestTest.java` 생성
- [ ] `shouldCreateRequestWithValidData()` 테스트 작성
- [ ] fileName, fileSize, mimeType, uploaderId 검증
- [ ] 커밋: `test: GeneratePresignedUrlRequest 기본 필드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/request/GeneratePresignedUrlRequest.java` 생성 (Record)
- [ ] 6개 필드 정의 (fileName, fileSize, mimeType, uploaderId, category, tags)
- [ ] 테스트 통과
- [ ] 커밋: `feat: GeneratePresignedUrlRequest 기본 필드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Request DTO는 Record, Lombok 금지)
- [ ] 커밋: `struct: GeneratePresignedUrlRequest 기본 필드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `GeneratePresignedUrlRequestFixture.java` 생성 (Object Mother 패턴)
- [ ] `aRequest()` 메서드 작성
- [ ] 커밋: `test: GeneratePresignedUrlRequest Fixture 정리 (Tidy)`

---

### 2️⃣ GeneratePresignedUrlRequest - Validation (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenFileNameIsBlank()` 테스트 작성
- [ ] `shouldFailWhenFileSizeIsNull()` 테스트 작성
- [ ] `shouldFailWhenMimeTypeIsInvalid()` 테스트 작성
- [ ] 커밋: `test: GeneratePresignedUrlRequest Validation 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@NotBlank` fileName 추가
- [ ] `@NotNull @Min(1)` fileSize 추가
- [ ] `@NotBlank @Pattern(regexp="^(image|text|application)/.*")` mimeType 추가
- [ ] `@NotNull` uploaderId 추가
- [ ] 커밋: `feat: GeneratePresignedUrlRequest Validation 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 메시지 개선
- [ ] ArchUnit 테스트: Request DTO는 Validation 필수
- [ ] 커밋: `struct: GeneratePresignedUrlRequest Validation 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] GeneratePresignedUrlRequestFixture 업데이트 (invalid 케이스 추가)
- [ ] 커밋: `test: GeneratePresignedUrlRequest Validation Fixture 정리 (Tidy)`

---

### 3️⃣ UploadFromExternalUrlRequest 구현 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `UploadFromExternalUrlRequestTest.java` 생성
- [ ] `shouldCreateRequestWithValidData()` 테스트 작성
- [ ] `shouldFailWhenUrlIsNotHttps()` 테스트 작성
- [ ] 커밋: `test: UploadFromExternalUrlRequest 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/request/UploadFromExternalUrlRequest.java` 생성 (Record)
- [ ] 5개 필드 정의 (externalUrl, uploaderId, category, tags, webhookUrl)
- [ ] `@NotBlank @Pattern(regexp="^https://.*")` externalUrl 추가
- [ ] `@NotNull` uploaderId 추가
- [ ] 커밋: `feat: UploadFromExternalUrlRequest 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: UploadFromExternalUrlRequest 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UploadFromExternalUrlRequestFixture.java` 생성
- [ ] 커밋: `test: UploadFromExternalUrlRequest Fixture 정리 (Tidy)`

---

### 4️⃣ ProcessFileRequest 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessFileRequestTest.java` 생성
- [ ] `shouldCreateRequestWithValidJobTypes()` 테스트 작성
- [ ] `shouldFailWhenJobTypesIsEmpty()` 테스트 작성
- [ ] 커밋: `test: ProcessFileRequest 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/request/ProcessFileRequest.java` 생성 (Record)
- [ ] `@NotEmpty List<JobType> jobTypes` 필드 정의
- [ ] 커밋: `feat: ProcessFileRequest 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: ProcessFileRequest 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProcessFileRequestFixture.java` 생성
- [ ] 커밋: `test: ProcessFileRequest Fixture 정리 (Tidy)`

---

### Phase 2: Response DTO 구현 (24 사이클)

---

### 5️⃣ PresignedUrlResponse 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `dto/response/PresignedUrlResponseTest.java` 생성
- [ ] `shouldCreateResponseWithValidData()` 테스트 작성
- [ ] fileId, presignedUrl, expiresIn, s3Key 검증
- [ ] 커밋: `test: PresignedUrlResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/PresignedUrlResponse.java` 생성 (Record)
- [ ] 4개 필드 정의
- [ ] 커밋: `feat: PresignedUrlResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Response DTO는 Record)
- [ ] 커밋: `struct: PresignedUrlResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `PresignedUrlResponseFixture.java` 생성
- [ ] 커밋: `test: PresignedUrlResponse Fixture 정리 (Tidy)`

---

### 6️⃣ FileResponse 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `FileResponseTest.java` 생성
- [ ] `shouldCreateResponseWithValidData()` 테스트 작성
- [ ] 커밋: `test: FileResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/FileResponse.java` 생성 (Record)
- [ ] 6개 필드 정의 (fileId, fileName, status, s3Url, cdnUrl, createdAt)
- [ ] 커밋: `feat: FileResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileResponseFixture.java` 생성
- [ ] 커밋: `test: FileResponse Fixture 정리 (Tidy)`

---

### 7️⃣ FileDetailResponse 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `FileDetailResponseTest.java` 생성
- [ ] `shouldCreateResponseWithAllFields()` 테스트 작성
- [ ] 커밋: `test: FileDetailResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/FileDetailResponse.java` 생성 (Record)
- [ ] 14개 필드 정의 (fileId, fileName, fileSize, mimeType, status, s3Key, s3Bucket, cdnUrl, uploaderId, category, tags, version, jobs, createdAt, updatedAt)
- [ ] 커밋: `feat: FileDetailResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileDetailResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileDetailResponseFixture.java` 생성
- [ ] 커밋: `test: FileDetailResponse Fixture 정리 (Tidy)`

---

### 8️⃣ FileSummaryResponse 구현 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `FileSummaryResponseTest.java` 생성
- [ ] `shouldCreateSummaryResponse()` 테스트 작성
- [ ] 커밋: `test: FileSummaryResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/FileSummaryResponse.java` 생성 (Record)
- [ ] 8개 필드 정의 (fileId, fileName, fileSize, mimeType, status, cdnUrl, category, createdAt)
- [ ] 커밋: `feat: FileSummaryResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileSummaryResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileSummaryResponseFixture.java` 생성
- [ ] 커밋: `test: FileSummaryResponse Fixture 정리 (Tidy)`

---

### 9️⃣ FileProcessingJobResponse 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobResponseTest.java` 생성
- [ ] `shouldCreateJobResponse()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/FileProcessingJobResponse.java` 생성 (Record)
- [ ] 8개 필드 정의 (jobId, fileId, jobType, status, outputS3Key, errorMessage, createdAt, processedAt)
- [ ] 커밋: `feat: FileProcessingJobResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobResponseFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobResponse Fixture 정리 (Tidy)`

---

### 🔟 CursorPageResponse<T> 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `CursorPageResponseTest.java` 생성
- [ ] `shouldCreateCursorPageResponse()` 테스트 작성
- [ ] content, nextCursor, hasNext, size 검증
- [ ] 커밋: `test: CursorPageResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `dto/response/CursorPageResponse.java` 생성 (Record)
- [ ] 4개 필드 정의 (content, nextCursor, hasNext, size)
- [ ] 커밋: `feat: CursorPageResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: CursorPageResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CursorPageResponseFixture.java` 생성
- [ ] 커밋: `test: CursorPageResponse Fixture 정리 (Tidy)`

---

### Phase 3: Mapper 구현 (12 사이클)

---

### 1️⃣1️⃣ FileRequestMapper - toCommand (GeneratePresignedUrl) (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `mapper/FileRequestMapperTest.java` 생성
- [ ] `shouldConvertToGeneratePresignedUrlCommand()` 테스트 작성
- [ ] 모든 필드 매핑 검증
- [ ] 커밋: `test: FileRequestMapper toCommand (GeneratePresignedUrl) 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/FileRequestMapper.java` 생성
- [ ] `toCommand(GeneratePresignedUrlRequest)` 메서드 구현
- [ ] 커밋: `feat: FileRequestMapper toCommand (GeneratePresignedUrl) 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Mapper는 Lombok 금지)
- [ ] 커밋: `struct: FileRequestMapper toCommand (GeneratePresignedUrl) 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileRequestMapperFixture.java` 생성
- [ ] 커밋: `test: FileRequestMapper Fixture 정리 (Tidy)`

---

### 1️⃣2️⃣ FileRequestMapper - toCommand (UploadFromExternalUrl) (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToUploadFromExternalUrlCommand()` 테스트 작성
- [ ] 커밋: `test: FileRequestMapper toCommand (UploadFromExternalUrl) 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toCommand(UploadFromExternalUrlRequest)` 메서드 구현
- [ ] 커밋: `feat: FileRequestMapper toCommand (UploadFromExternalUrl) 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileRequestMapper toCommand (UploadFromExternalUrl) 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileRequestMapperFixture 업데이트
- [ ] 커밋: `test: FileRequestMapper toCommand (UploadFromExternalUrl) Fixture 정리 (Tidy)`

---

### 1️⃣3️⃣ FileRequestMapper - toCommand (ProcessFile) (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToProcessFileCommand()` 테스트 작성
- [ ] 커밋: `test: FileRequestMapper toCommand (ProcessFile) 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toCommand(ProcessFileRequest, String fileId)` 메서드 구현
- [ ] 커밋: `feat: FileRequestMapper toCommand (ProcessFile) 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileRequestMapper toCommand (ProcessFile) 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileRequestMapperFixture 업데이트
- [ ] 커밋: `test: FileRequestMapper toCommand (ProcessFile) Fixture 정리 (Tidy)`

---

### 1️⃣4️⃣ FileResponseMapper - toPresignedUrlResponse (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `mapper/FileResponseMapperTest.java` 생성
- [ ] `shouldConvertToPresignedUrlResponse()` 테스트 작성
- [ ] 커밋: `test: FileResponseMapper toPresignedUrlResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/FileResponseMapper.java` 생성
- [ ] `toPresignedUrlResponse(PresignedUrlInfo)` 메서드 구현
- [ ] 커밋: `feat: FileResponseMapper toPresignedUrlResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileResponseMapper toPresignedUrlResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileResponseMapperFixture.java` 생성
- [ ] 커밋: `test: FileResponseMapper Fixture 정리 (Tidy)`

---

### 1️⃣5️⃣ FileResponseMapper - toFileResponse (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToFileResponse()` 테스트 작성
- [ ] 커밋: `test: FileResponseMapper toFileResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toFileResponse(File)` 메서드 구현
- [ ] 커밋: `feat: FileResponseMapper toFileResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileResponseMapper toFileResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileResponseMapperFixture 업데이트
- [ ] 커밋: `test: FileResponseMapper toFileResponse Fixture 정리 (Tidy)`

---

### 1️⃣6️⃣ FileResponseMapper - toFileDetailResponse (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToFileDetailResponse()` 테스트 작성
- [ ] 커밋: `test: FileResponseMapper toFileDetailResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toFileDetailResponse(File, List<FileProcessingJob>)` 메서드 구현
- [ ] 커밋: `feat: FileResponseMapper toFileDetailResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileResponseMapper toFileDetailResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileResponseMapperFixture 업데이트
- [ ] 커밋: `test: FileResponseMapper toFileDetailResponse Fixture 정리 (Tidy)`

---

### 1️⃣7️⃣ FileResponseMapper - toFileSummaryResponse (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToFileSummaryResponse()` 테스트 작성
- [ ] 커밋: `test: FileResponseMapper toFileSummaryResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toFileSummaryResponse(File)` 메서드 구현
- [ ] 커밋: `feat: FileResponseMapper toFileSummaryResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileResponseMapper toFileSummaryResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileResponseMapperFixture 업데이트
- [ ] 커밋: `test: FileResponseMapper toFileSummaryResponse Fixture 정리 (Tidy)`

---

### 1️⃣8️⃣ FileResponseMapper - toCursorPageResponse (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertToCursorPageResponse()` 테스트 작성
- [ ] content, nextCursor, hasNext, size 검증
- [ ] 커밋: `test: FileResponseMapper toCursorPageResponse 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toCursorPageResponse(CursorPage<File>)` 메서드 구현
- [ ] List<File> → List<FileSummaryResponse> 변환
- [ ] 커밋: `feat: FileResponseMapper toCursorPageResponse 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Stream API 사용 개선
- [ ] 커밋: `struct: FileResponseMapper toCursorPageResponse 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileResponseMapperFixture 업데이트
- [ ] 커밋: `test: FileResponseMapper toCursorPageResponse Fixture 정리 (Tidy)`

---

### 1️⃣9️⃣ FileProcessingJobResponseMapper 구현 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `mapper/FileProcessingJobResponseMapperTest.java` 생성
- [ ] `shouldConvertToFileProcessingJobResponse()` 테스트 작성
- [ ] `shouldConvertToFileProcessingJobResponses()` 테스트 작성 (List)
- [ ] 커밋: `test: FileProcessingJobResponseMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/FileProcessingJobResponseMapper.java` 생성
- [ ] `toFileProcessingJobResponse(FileProcessingJob)` 메서드 구현
- [ ] `toFileProcessingJobResponses(List<FileProcessingJob>)` 메서드 구현
- [ ] 커밋: `feat: FileProcessingJobResponseMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobResponseMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobResponseMapperFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobResponseMapper Fixture 정리 (Tidy)`

---

### Phase 4: Error Code 구현 (4 사이클)

---

### 2️⃣0️⃣ FileErrorCode Enum 구현 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `error/FileErrorCodeTest.java` 생성
- [ ] `shouldHaveAllErrorCodes()` 테스트 작성
- [ ] 8개 에러 코드 검증
- [ ] 커밋: `test: FileErrorCode Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `error/FileErrorCode.java` Enum 생성
- [ ] 8개 에러 코드 정의:
  - FILE_NOT_FOUND
  - FILE_SIZE_EXCEEDED
  - INVALID_MIME_TYPE
  - INVALID_FILE_STATUS
  - INVALID_URL
  - PRESIGNED_URL_GENERATION_FAILED
  - UPLOAD_VERIFICATION_FAILED
  - JOB_NOT_FOUND
- [ ] 각 코드별 메시지 정의
- [ ] 커밋: `feat: FileErrorCode Enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Error Code는 UPPER_SNAKE_CASE)
- [ ] 커밋: `struct: FileErrorCode Enum 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileErrorCodeFixture.java` 생성
- [ ] 커밋: `test: FileErrorCode Enum Fixture 정리 (Tidy)`

---

### 2️⃣1️⃣ Global Exception Handler 구현 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `error/FileExceptionHandlerTest.java` 생성
- [ ] `shouldHandleFileNotFoundException()` 테스트 작성
- [ ] 커밋: `test: FileExceptionHandler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `error/FileExceptionHandler.java` 생성 (@RestControllerAdvice)
- [ ] `handleFileNotFoundException()` 메서드 구현
  - HTTP 404 반환
  - ApiResponse.ofFailure(ErrorInfo) 사용
- [ ] 커밋: `feat: FileExceptionHandler 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (ExceptionHandler 규칙)
- [ ] 커밋: `struct: FileExceptionHandler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileExceptionHandlerFixture.java` 생성
- [ ] 커밋: `test: FileExceptionHandler Fixture 정리 (Tidy)`

---

### 2️⃣2️⃣ FileExceptionHandler - 비즈니스 예외 처리 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandleFileSizeExceededException()` 테스트 작성
- [ ] `shouldHandleInvalidMimeTypeException()` 테스트 작성
- [ ] `shouldHandleInvalidFileStatusException()` 테스트 작성
- [ ] 커밋: `test: FileExceptionHandler 비즈니스 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `handleFileSizeExceededException()` 메서드 구현 (HTTP 400)
- [ ] `handleInvalidMimeTypeException()` 메서드 구현 (HTTP 400)
- [ ] `handleInvalidFileStatusException()` 메서드 구현 (HTTP 409)
- [ ] 커밋: `feat: FileExceptionHandler 비즈니스 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 로직 중복 제거
- [ ] 커밋: `struct: FileExceptionHandler 비즈니스 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileExceptionHandlerFixture 업데이트
- [ ] 커밋: `test: FileExceptionHandler 비즈니스 예외 Fixture 정리 (Tidy)`

---

### 2️⃣3️⃣ FileExceptionHandler - Validation 예외 처리 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandleMethodArgumentNotValidException()` 테스트 작성
- [ ] 필드 에러 메시지 조합 검증
- [ ] 커밋: `test: FileExceptionHandler Validation 예외 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `handleMethodArgumentNotValidException()` 메서드 구현 (HTTP 400)
- [ ] BindingResult 에러 메시지 추출 로직
- [ ] 커밋: `feat: FileExceptionHandler Validation 예외 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 메시지 포맷 개선
- [ ] 커밋: `struct: FileExceptionHandler Validation 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileExceptionHandlerFixture 업데이트
- [ ] 커밋: `test: FileExceptionHandler Validation 예외 Fixture 정리 (Tidy)`

---

### Phase 5: Controller 구현 (24 사이클)

---

### 2️⃣4️⃣ FileUploadController - POST /presigned-url (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `controller/FileUploadControllerTest.java` 생성 (@SpringBootTest, TestRestTemplate)
- [ ] `shouldGeneratePresignedUrl()` 테스트 작성
- [ ] HTTP 200 OK 검증
- [ ] 커밋: `test: FileUploadController POST /presigned-url 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `controller/FileUploadController.java` 생성
- [ ] `@RestController`, `@RequestMapping("/api/v1/files")` 추가
- [ ] `POST /presigned-url` 엔드포인트 구현
  - `@Valid @RequestBody GeneratePresignedUrlRequest` 파라미터
  - UseCase 호출 → Mapper 변환 → ApiResponse 반환
- [ ] 커밋: `feat: FileUploadController POST /presigned-url 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Controller는 UseCase만 의존)
- [ ] RESTful 설계 검증
- [ ] 커밋: `struct: FileUploadController POST /presigned-url 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileUploadControllerFixture.java` 생성
- [ ] 커밋: `test: FileUploadController POST /presigned-url Fixture 정리 (Tidy)`

---

### 2️⃣5️⃣ FileUploadController - POST /presigned-url Validation (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenFileNameIsBlank()` 테스트 작성
- [ ] `shouldFailWhenFileSizeExceeds1GB()` 테스트 작성
- [ ] `shouldFailWhenMimeTypeIsInvalid()` 테스트 작성
- [ ] HTTP 400 Bad Request 검증
- [ ] 커밋: `test: POST /presigned-url Validation 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Validation 에러 시 HTTP 400 반환 확인
- [ ] ExceptionHandler 연동 확인
- [ ] 커밋: `feat: POST /presigned-url Validation 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Validation 테스트 개선
- [ ] 커밋: `struct: POST /presigned-url Validation 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /presigned-url Validation Fixture 정리 (Tidy)`

---

### 2️⃣6️⃣ FileUploadController - POST /{fileId}/complete (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCompleteUpload()` 테스트 작성
- [ ] HTTP 200 OK 검증
- [ ] 커밋: `test: FileUploadController POST /{fileId}/complete 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `POST /{fileId}/complete` 엔드포인트 구현
  - `@PathVariable String fileId` 파라미터
  - UUID v7 검증 (`@Pattern`)
  - UseCase 호출 → ApiResponse 반환
- [ ] 커밋: `feat: FileUploadController POST /{fileId}/complete 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileUploadController POST /{fileId}/complete 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /{fileId}/complete Fixture 정리 (Tidy)`

---

### 2️⃣7️⃣ FileUploadController - POST /{fileId}/complete Error (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenFileNotFound()` 테스트 작성 (HTTP 404)
- [ ] `shouldFailWhenInvalidStatus()` 테스트 작성 (HTTP 409)
- [ ] `shouldFailWhenS3ObjectNotExists()` 테스트 작성 (HTTP 500)
- [ ] 커밋: `test: POST /{fileId}/complete Error 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExceptionHandler 연동 확인
- [ ] 각 예외별 HTTP Status 검증
- [ ] 커밋: `feat: POST /{fileId}/complete Error 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 테스트 개선
- [ ] 커밋: `struct: POST /{fileId}/complete Error 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /{fileId}/complete Error Fixture 정리 (Tidy)`

---

### 2️⃣8️⃣ FileUploadController - POST /from-url (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUploadFromExternalUrl()` 테스트 작성
- [ ] HTTP 202 Accepted 검증
- [ ] 커밋: `test: FileUploadController POST /from-url 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `POST /from-url` 엔드포인트 구현
  - `@Valid @RequestBody UploadFromExternalUrlRequest` 파라미터
  - UseCase 호출 → HTTP 202 반환
- [ ] 커밋: `feat: FileUploadController POST /from-url 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileUploadController POST /from-url 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /from-url Fixture 정리 (Tidy)`

---

### 2️⃣9️⃣ FileUploadController - POST /from-url Validation (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenUrlIsNotHttps()` 테스트 작성
- [ ] HTTP 400 Bad Request 검증
- [ ] 커밋: `test: POST /from-url Validation 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] HTTPS 검증 확인 (Validation)
- [ ] ExceptionHandler 연동 확인
- [ ] 커밋: `feat: POST /from-url Validation 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: POST /from-url Validation 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /from-url Validation Fixture 정리 (Tidy)`

---

### 3️⃣0️⃣ FileUploadController - POST /{fileId}/process (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `shouldProcessFile()` 테스트 작성
- [ ] HTTP 202 Accepted 검증
- [ ] 커밋: `test: FileUploadController POST /{fileId}/process 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `POST /{fileId}/process` 엔드포인트 구현
  - `@PathVariable String fileId` 파라미터
  - `@Valid @RequestBody ProcessFileRequest` 파라미터
  - UseCase 호출 → HTTP 202 반환
- [ ] 커밋: `feat: FileUploadController POST /{fileId}/process 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileUploadController POST /{fileId}/process 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /{fileId}/process Fixture 정리 (Tidy)`

---

### 3️⃣1️⃣ FileUploadController - POST /{fileId}/process Error (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenFileNotCompleted()` 테스트 작성 (HTTP 409)
- [ ] `shouldFailWhenJobTypesIsEmpty()` 테스트 작성 (HTTP 400)
- [ ] 커밋: `test: POST /{fileId}/process Error 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExceptionHandler 연동 확인
- [ ] 커밋: `feat: POST /{fileId}/process Error 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: POST /{fileId}/process Error 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadControllerFixture 업데이트
- [ ] 커밋: `test: POST /{fileId}/process Error Fixture 정리 (Tidy)`

---

### 3️⃣2️⃣ FileQueryController - GET /{fileId} (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `controller/FileQueryControllerTest.java` 생성
- [ ] `shouldGetFileDetail()` 테스트 작성
- [ ] HTTP 200 OK 검증
- [ ] 커밋: `test: FileQueryController GET /{fileId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `controller/FileQueryController.java` 생성
- [ ] `@RestController`, `@RequestMapping("/api/v1/files")` 추가
- [ ] `GET /{fileId}` 엔드포인트 구현
  - `@PathVariable String fileId` 파라미터
  - UseCase 호출 → Mapper 변환 → ApiResponse 반환
- [ ] 커밋: `feat: FileQueryController GET /{fileId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileQueryController GET /{fileId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileQueryControllerFixture.java` 생성
- [ ] 커밋: `test: FileQueryController GET /{fileId} Fixture 정리 (Tidy)`

---

### 3️⃣3️⃣ FileQueryController - GET /{fileId} Error (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenFileNotFound()` 테스트 작성 (HTTP 404)
- [ ] 커밋: `test: GET /{fileId} Error 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExceptionHandler 연동 확인
- [ ] 커밋: `feat: GET /{fileId} Error 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: GET /{fileId} Error 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryControllerFixture 업데이트
- [ ] 커밋: `test: GET /{fileId} Error Fixture 정리 (Tidy)`

---

### 3️⃣4️⃣ FileQueryController - GET /files (Cursor Pagination) (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `shouldListFilesWithCursorPagination()` 테스트 작성
- [ ] Query Params 검증 (uploaderId, status, category, cursor, size)
- [ ] HTTP 200 OK, hasNext, nextCursor 검증
- [ ] 커밋: `test: FileQueryController GET /files 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GET /files` 엔드포인트 구현
  - `@RequestParam @NotNull Long uploaderId` 파라미터
  - `@RequestParam(required = false) String status` 파라미터
  - `@RequestParam(required = false) String category` 파라미터
  - `@RequestParam(required = false) LocalDateTime cursor` 파라미터
  - `@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size` 파라미터
  - UseCase 호출 → Mapper 변환 → ApiResponse 반환
- [ ] 커밋: `feat: FileQueryController GET /files 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] Query Params Validation 개선
- [ ] 커밋: `struct: FileQueryController GET /files 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryControllerFixture 업데이트
- [ ] 커밋: `test: GET /files Fixture 정리 (Tidy)`

---

### 3️⃣5️⃣ FileQueryController - GET /files Pagination (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldPaginateCorrectly()` 테스트 작성
- [ ] 첫 페이지 → 두 번째 페이지 → hasNext=false 검증
- [ ] 커밋: `test: GET /files Pagination 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Cursor Pagination 동작 확인
- [ ] 커밋: `feat: GET /files Pagination 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 페이지네이션 테스트 개선
- [ ] 커밋: `struct: GET /files Pagination 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryControllerFixture 업데이트
- [ ] 커밋: `test: GET /files Pagination Fixture 정리 (Tidy)`

---

### 3️⃣6️⃣ FileProcessingJobController - GET /{fileId}/jobs (Cycle 36)

#### 🔴 Red: 테스트 작성
- [ ] `controller/FileProcessingJobControllerTest.java` 생성
- [ ] `shouldGetFileProcessingJobs()` 테스트 작성
- [ ] HTTP 200 OK 검증
- [ ] 커밋: `test: FileProcessingJobController GET /{fileId}/jobs 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `controller/FileProcessingJobController.java` 생성
- [ ] `@RestController`, `@RequestMapping("/api/v1")` 추가
- [ ] `GET /files/{fileId}/jobs` 엔드포인트 구현
  - `@PathVariable String fileId` 파라미터
  - UseCase 호출 → Mapper 변환 → ApiResponse 반환
- [ ] 커밋: `feat: FileProcessingJobController GET /{fileId}/jobs 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobController GET /{fileId}/jobs 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobControllerFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobController GET /{fileId}/jobs Fixture 정리 (Tidy)`

---

### 3️⃣7️⃣ FileProcessingJobController - GET /jobs/{jobId} (Cycle 37)

#### 🔴 Red: 테스트 작성
- [ ] `shouldGetJobDetail()` 테스트 작성
- [ ] HTTP 200 OK 검증
- [ ] 커밋: `test: FileProcessingJobController GET /jobs/{jobId} 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GET /jobs/{jobId}` 엔드포인트 구현
  - `@PathVariable String jobId` 파라미터
  - UseCase 호출 → Mapper 변환 → ApiResponse 반환
- [ ] 커밋: `feat: FileProcessingJobController GET /jobs/{jobId} 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobController GET /jobs/{jobId} 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileProcessingJobControllerFixture 업데이트
- [ ] 커밋: `test: GET /jobs/{jobId} Fixture 정리 (Tidy)`

---

### 3️⃣8️⃣ FileProcessingJobController - GET /jobs/{jobId} Error (Cycle 38)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFailWhenJobNotFound()` 테스트 작성 (HTTP 404)
- [ ] 커밋: `test: GET /jobs/{jobId} Error 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExceptionHandler 연동 확인
- [ ] 커밋: `feat: GET /jobs/{jobId} Error 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: GET /jobs/{jobId} Error 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileProcessingJobControllerFixture 업데이트
- [ ] 커밋: `test: GET /jobs/{jobId} Error Fixture 정리 (Tidy)`

---

### Phase 6: ArchUnit 검증 (8 사이클)

---

### 3️⃣9️⃣ Controller ArchUnit - UseCase 의존성 (Cycle 39)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/ControllerArchitectureTest.java` 생성
- [ ] `controllersShouldOnlyDependOnUseCases()` 테스트 작성
- [ ] Controller는 UseCase만 의존 검증
- [ ] 커밋: `test: Controller UseCase 의존성 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Controller UseCase 의존성 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Controller UseCase 의존성 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Controller UseCase 의존성 ArchUnit Fixture 정리 (Tidy)`

---

### 4️⃣0️⃣ DTO ArchUnit - Record 검증 (Cycle 40)

#### 🔴 Red: 테스트 작성
- [ ] `dtosShouldBeRecords()` 테스트 작성
- [ ] Request/Response DTO는 Record 검증
- [ ] 커밋: `test: DTO Record ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: DTO Record ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: DTO Record ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: DTO Record ArchUnit Fixture 정리 (Tidy)`

---

### 4️⃣1️⃣ DTO ArchUnit - Lombok 금지 (Cycle 41)

#### 🔴 Red: 테스트 작성
- [ ] `dtosShouldNotUseLombok()` 테스트 작성
- [ ] @Data, @Getter, @Setter 등 금지 검증
- [ ] 커밋: `test: DTO Lombok 금지 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: DTO Lombok 금지 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: DTO Lombok 금지 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: DTO Lombok 금지 ArchUnit Fixture 정리 (Tidy)`

---

### 4️⃣2️⃣ Request DTO ArchUnit - Validation 필수 (Cycle 42)

#### 🔴 Red: 테스트 작성
- [ ] `requestDtosShouldHaveValidation()` 테스트 작성
- [ ] @Valid, @NotNull, @NotBlank 등 필수 검증
- [ ] 커밋: `test: Request DTO Validation 필수 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Request DTO Validation 필수 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Request DTO Validation 필수 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Request DTO Validation 필수 ArchUnit Fixture 정리 (Tidy)`

---

### 4️⃣3️⃣ Controller ArchUnit - RESTful 네이밍 (Cycle 43)

#### 🔴 Red: 테스트 작성
- [ ] `controllersShouldFollowRestfulNaming()` 테스트 작성
- [ ] URI는 명사, HTTP Method로 동작 표현 검증
- [ ] 커밋: `test: Controller RESTful 네이밍 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (URI 패턴 검증)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Controller RESTful 네이밍 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Controller RESTful 네이밍 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Controller RESTful 네이밍 ArchUnit Fixture 정리 (Tidy)`

---

### 4️⃣4️⃣ Mapper ArchUnit - Lombok 금지 (Cycle 44)

#### 🔴 Red: 테스트 작성
- [ ] `mappersShouldNotUseLombok()` 테스트 작성
- [ ] 커밋: `test: Mapper Lombok 금지 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Mapper Lombok 금지 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Mapper Lombok 금지 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Mapper Lombok 금지 ArchUnit Fixture 정리 (Tidy)`

---

### Phase 7: Integration Test E2E (12 사이클)

---

### 4️⃣5️⃣ E2E: Presigned URL 발급 → 업로드 완료 (Cycle 45)

#### 🔴 Red: 테스트 작성
- [ ] `integration/FileUploadE2ETest.java` 생성 (@SpringBootTest, TestContainers)
- [ ] `shouldGeneratePresignedUrlAndCompleteUpload()` E2E 테스트 작성
- [ ] POST /presigned-url → POST /{fileId}/complete → GET /{fileId} 시나리오
- [ ] 커밋: `test: Presigned URL 발급 → 업로드 완료 E2E 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TestContainers MySQL, Redis 설정
- [ ] Flyway 마이그레이션 자동 실행
- [ ] E2E 시나리오 통과
- [ ] 커밋: `feat: Presigned URL 발급 → 업로드 완료 E2E 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인 (@DirtiesContext)
- [ ] 커밋: `struct: Presigned URL 발급 → 업로드 완료 E2E 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileUploadE2EFixture.java` 생성
- [ ] 커밋: `test: Presigned URL 발급 → 업로드 완료 E2E Fixture 정리 (Tidy)`

---

### 4️⃣6️⃣ E2E: 외부 URL 업로드 → 파일 가공 (Cycle 46)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUploadFromExternalUrlAndProcessFile()` E2E 테스트 작성
- [ ] POST /from-url → (비동기 대기) → POST /{fileId}/process → GET /{fileId}/jobs 시나리오
- [ ] 커밋: `test: 외부 URL 업로드 → 파일 가공 E2E 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 비동기 작업 대기 로직 (폴링 또는 @Async)
- [ ] E2E 시나리오 통과
- [ ] 커밋: `feat: 외부 URL 업로드 → 파일 가공 E2E 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: 외부 URL 업로드 → 파일 가공 E2E 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadE2EFixture 업데이트
- [ ] 커밋: `test: 외부 URL 업로드 → 파일 가공 E2E Fixture 정리 (Tidy)`

---

### 4️⃣7️⃣ E2E: Cursor Pagination 전체 흐름 (Cycle 47)

#### 🔴 Red: 테스트 작성
- [ ] `shouldPaginateThroughAllFiles()` E2E 테스트 작성
- [ ] 10개 파일 생성 → GET /files (size=3) → 첫 페이지 → 두 번째 페이지 → ... → hasNext=false 검증
- [ ] 커밋: `test: Cursor Pagination 전체 흐름 E2E 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] E2E 시나리오 통과
- [ ] 커밋: `feat: Cursor Pagination 전체 흐름 E2E 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: Cursor Pagination 전체 흐름 E2E 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileUploadE2EFixture 업데이트
- [ ] 커밋: `test: Cursor Pagination 전체 흐름 E2E Fixture 정리 (Tidy)`

---

### 4️⃣8️⃣ 테스트 커버리지 검증 (Cycle 48)

#### 🔴 Red: 테스트 작성
- [ ] JaCoCo 플러그인 설정
- [ ] `shouldHaveTestCoverageAbove80Percent()` 테스트 작성
- [ ] 커밋: `test: 테스트 커버리지 검증 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 커버리지 > 80% 확인
- [ ] 커밋: `feat: 테스트 커버리지 검증 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커버리지 리포트 생성 설정
- [ ] 커밋: `struct: 테스트 커버리지 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 검토
- [ ] 커밋: `test: 모든 Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (48 사이클, 체크박스 모두 ✅)
- [ ] 3개 Controller 구현 완료 (FileUpload, FileQuery, FileProcessingJob)
- [ ] 3개 Request DTO 구현 완료 (Record)
- [ ] 6개 Response DTO 구현 완료 (Record)
- [ ] 3개 Mapper 구현 완료
- [ ] 8개 Error Code 정의 완료
- [ ] Global Exception Handler 구현 완료
- [ ] Integration Test (TestRestTemplate) 통과
- [ ] ArchUnit 테스트 통과 (UseCase 의존성, Record, Lombok 금지, Validation 필수, RESTful 네이밍)
- [ ] 테스트 커버리지 > 80%
- [ ] Zero-Tolerance 규칙 준수
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/FILE-004.md
- **PRD**: docs/prd/file-management-system.md
- **컨벤션**: docs/coding_convention/01-adapter-in-layer/rest-api/

---

## 📊 사이클 요약

| Phase | 사이클 수 | 예상 소요 시간 |
|-------|----------|---------------|
| Phase 1: Request DTO 구현 | 4 | 60분 |
| Phase 2: Response DTO 구현 | 6 | 90분 |
| Phase 3: Mapper 구현 | 9 | 135분 |
| Phase 4: Error Code 구현 | 4 | 60분 |
| Phase 5: Controller 구현 | 14 | 210분 |
| Phase 6: ArchUnit 검증 | 6 | 90분 |
| Phase 7: Integration Test E2E | 5 | 75분 |
| **합계** | **48** | **720분 (12시간)** |

---

## 🎯 핵심 원칙

1. **작은 단위**: 각 사이클은 5-15분 내 완료
2. **4단계 필수**: Red → Green → Refactor → Tidy 모두 수행
3. **TestFixture 필수**: Tidy 단계에서 Object Mother 패턴 적용
4. **Zero-Tolerance**: RESTful 설계, Record, Lombok 금지, Validation 필수 엄격 준수
5. **체크박스 추적**: `/kb/rest-api/go` 명령이 Plan 파일을 읽고 진행 상황 추적
6. **RESTful 설계**: URI는 명사, HTTP Method로 동작 표현
7. **ApiResponse<T> 표준 포맷**: 모든 API는 ApiResponse 반환
8. **Integration Test**: TestRestTemplate 필수, MockMvc 금지
9. **ArchUnit 검증**: 각 Refactor 단계에서 ArchUnit 규칙 검증 필수

---

## 🚀 다음 단계

```bash
# Plan 파일 생성 완료
/kb/rest-api/go

# 또는 개별 Phase 실행
/kb/rest-api/red      # Red Phase만
/kb/rest-api/green    # Green Phase만
/kb/rest-api/refactor # Refactor Phase만
```
