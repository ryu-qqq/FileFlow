# FILE-006-004: REST API Layer TDD Plan

**Task**: FILE-006-004 (REST API Layer 구현)
**Layer**: REST API Layer
**브랜치**: feature/FILE-006-004-rest-api
**예상 소요 시간**: 510분 (34 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: Request/Response DTO (5 사이클)

#### 1️⃣ PrepareUploadRequest DTO 구현 (Cycle 1)

**🔴 Red: 테스트 작성**
- [ ] `PrepareUploadRequestTest.java` 생성
- [ ] `shouldCreateValidRequest()` 테스트 작성 (모든 필드 검증)
- [ ] `shouldValidateSessionIdNotBlank()` 테스트 작성
- [ ] `shouldValidateFileSize_between1And1GB()` 테스트 작성
- [ ] 커밋: `test: PrepareUploadRequest DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `PrepareUploadRequest.java` 생성 (Record)
- [ ] 6개 필드: sessionId, uploadType, customPath, fileName, fileSize, mimeType
- [ ] `@NotBlank`, `@NotNull`, `@Min`, `@Max` 어노테이션 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: PrepareUploadRequest DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인 (dto-record-archunit.md)
- [ ] Validation 메시지 명확화
- [ ] 커밋: `refactor: PrepareUploadRequest DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `PrepareUploadRequestFixture.java` 생성 (Object Mother 패턴)
- [ ] `aValidRequest()`, `anInvalidRequest()` 메서드 작성
- [ ] 커밋: `test: PrepareUploadRequestFixture 정리 (Tidy)`

---

#### 2️⃣ PrepareUploadResponse DTO 구현 (Cycle 2)

**🔴 Red: 테스트 작성**
- [ ] `PrepareUploadResponseTest.java` 생성
- [ ] `shouldCreateFromSessionPreparationResult_forSingle()` 테스트 작성
- [ ] `shouldCreateFromSessionPreparationResult_forMultipart()` 테스트 작성
- [ ] PartUploadUrl 중첩 Record 검증
- [ ] 커밋: `test: PrepareUploadResponse DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `PrepareUploadResponse.java` 생성 (Record)
- [ ] 6개 필드: sessionId, fileId, uploadType, uploadUrl, partUploadUrls, expiresAt
- [ ] 중첩 Record `PartUploadUrl` 정의
- [ ] `from(SessionPreparationResult)` 팩토리 메서드 구현
- [ ] 커밋: `impl: PrepareUploadResponse DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] Stream API 최적화
- [ ] 커밋: `refactor: PrepareUploadResponse DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `PrepareUploadResponseFixture.java` 생성
- [ ] `aValidSingleResponse()`, `aValidMultipartResponse()` 메서드 작성
- [ ] 커밋: `test: PrepareUploadResponseFixture 정리 (Tidy)`

---

#### 3️⃣ UploadSessionResponse DTO 구현 (Cycle 3)

**🔴 Red: 테스트 작성**
- [ ] `UploadSessionResponseTest.java` 생성
- [ ] `shouldCreateFromUploadSession()` 테스트 작성
- [ ] Law of Demeter 준수 검증 (getSessionIdValue(), getFileIdValue())
- [ ] 커밋: `test: UploadSessionResponse DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `UploadSessionResponse.java` 생성 (Record)
- [ ] 6개 필드: sessionId, fileId, uploadType, status, expiresAt, createdAt
- [ ] `from(UploadSession)` 팩토리 메서드 구현
- [ ] 커밋: `impl: UploadSessionResponse DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: UploadSessionResponse DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `UploadSessionResponseFixture.java` 생성
- [ ] 커밋: `test: UploadSessionResponseFixture 정리 (Tidy)`

---

#### 4️⃣ FileDetailResponse & FileSummaryResponse DTO 구현 (Cycle 4)

**🔴 Red: 테스트 작성**
- [ ] `FileDetailResponseTest.java` 생성
- [ ] `shouldCreateFromFile()` 테스트 작성
- [ ] `FileSummaryResponseTest.java` 생성
- [ ] Law of Demeter 준수 검증
- [ ] 커밋: `test: File Response DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `FileDetailResponse.java` 생성 (7개 필드)
- [ ] `FileSummaryResponse.java` 생성 (5개 필드)
- [ ] 각각 `from(File)` 팩토리 메서드 구현
- [ ] 커밋: `impl: File Response DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: File Response DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `FileDetailResponseFixture.java`, `FileSummaryResponseFixture.java` 생성
- [ ] 커밋: `test: File Response Fixture 정리 (Tidy)`

---

#### 5️⃣ PageResponse DTO 구현 (Cycle 5)

**🔴 Red: 테스트 작성**
- [ ] `PageResponseTest.java` 생성
- [ ] `shouldCreatePageResponse()` 테스트 작성
- [ ] totalPages 계산 로직 검증
- [ ] 커밋: `test: PageResponse DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `PageResponse.java` 생성 (Generic Record)
- [ ] 5개 필드: content, page, size, totalElements, totalPages
- [ ] `of()` 팩토리 메서드 구현
- [ ] 커밋: `impl: PageResponse DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: PageResponse DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `PageResponseFixture.java` 생성
- [ ] 커밋: `test: PageResponseFixture 정리 (Tidy)`

---

### Phase 2: Mapper (1 사이클)

#### 6️⃣ PrepareUploadMapper 구현 (Cycle 6)

**🔴 Red: 테스트 작성**
- [ ] `PrepareUploadMapperTest.java` 생성
- [ ] `shouldMapRequestToCommand()` 테스트 작성
- [ ] UserContext 통합 검증
- [ ] 커밋: `test: PrepareUploadMapper 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `PrepareUploadMapper.java` 생성
- [ ] `toCommand(PrepareUploadRequest, UserContext)` 정적 메서드 구현
- [ ] 커밋: `impl: PrepareUploadMapper 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Mapper ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: PrepareUploadMapper 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: PrepareUploadMapper 테스트 정리 (Tidy)`

---

### Phase 3: Error Handling (3 사이클)

#### 7️⃣ ErrorResponse DTO 구현 (Cycle 7)

**🔴 Red: 테스트 작성**
- [ ] `ErrorResponseTest.java` 생성
- [ ] `shouldCreateErrorResponse()` 테스트 작성
- [ ] 커밋: `test: ErrorResponse DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `ErrorResponse.java` 생성 (Record)
- [ ] 4개 필드: errorCode, message, timestamp, path
- [ ] 커밋: `impl: ErrorResponse DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: ErrorResponse DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `ErrorResponseFixture.java` 생성
- [ ] 커밋: `test: ErrorResponseFixture 정리 (Tidy)`

---

#### 8️⃣ ValidationErrorResponse DTO 구현 (Cycle 8)

**🔴 Red: 테스트 작성**
- [ ] `ValidationErrorResponseTest.java` 생성
- [ ] `shouldCreateValidationErrorResponse()` 테스트 작성
- [ ] FieldError 중첩 Record 검증
- [ ] 커밋: `test: ValidationErrorResponse DTO 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `ValidationErrorResponse.java` 생성 (Record)
- [ ] 중첩 Record `FieldError` 정의
- [ ] 5개 필드: errorCode, message, fieldErrors, timestamp, path
- [ ] 커밋: `impl: ValidationErrorResponse DTO 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] DTO ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: ValidationErrorResponse DTO 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `ValidationErrorResponseFixture.java` 생성
- [ ] 커밋: `test: ValidationErrorResponseFixture 정리 (Tidy)`

---

#### 9️⃣ GlobalExceptionHandler 구현 (Part 1: Domain 예외) (Cycle 9)

**🔴 Red: 테스트 작성**
- [ ] `GlobalExceptionHandlerTest.java` 생성 (@WebMvcTest)
- [ ] `shouldHandleFileSizeExceededException()` 테스트 작성
- [ ] `shouldHandleUnsupportedFileTypeException()` 테스트 작성
- [ ] `shouldHandleInvalidSessionStatusException()` 테스트 작성
- [ ] `shouldHandleSessionExpiredException()` 테스트 작성
- [ ] 커밋: `test: GlobalExceptionHandler Domain 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `GlobalExceptionHandler.java` 생성 (@RestControllerAdvice)
- [ ] 4개 Domain 예외 핸들러 구현
- [ ] 커밋: `impl: GlobalExceptionHandler Domain 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 중복 코드 추출 (createErrorResponse 메서드)
- [ ] 커밋: `refactor: GlobalExceptionHandler Domain 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: GlobalExceptionHandler 테스트 정리 (Tidy)`

---

#### 🔟 GlobalExceptionHandler 구현 (Part 2: Application 예외) (Cycle 10)

**🔴 Red: 테스트 작성**
- [ ] `shouldHandleSessionNotFoundException()` 테스트 작성
- [ ] `shouldHandleFileNotFoundException()` 테스트 작성
- [ ] `shouldHandleUnauthorizedAccessException()` 테스트 작성
- [ ] 커밋: `test: GlobalExceptionHandler Application 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] 3개 Application 예외 핸들러 구현
- [ ] 커밋: `impl: GlobalExceptionHandler Application 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 중복 코드 제거
- [ ] 커밋: `refactor: GlobalExceptionHandler Application 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: GlobalExceptionHandler 테스트 정리 (Tidy)`

---

#### 1️⃣1️⃣ GlobalExceptionHandler 구현 (Part 3: Infrastructure & Validation) (Cycle 11)

**🔴 Red: 테스트 작성**
- [ ] `shouldHandleRedisConnectionException()` 테스트 작성
- [ ] `shouldHandleMethodArgumentNotValidException()` 테스트 작성
- [ ] `shouldHandleGenericException()` 테스트 작성
- [ ] 커밋: `test: GlobalExceptionHandler Infrastructure 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] RedisConnectionException 핸들러 구현 (503)
- [ ] MethodArgumentNotValidException 핸들러 구현 (400)
- [ ] Exception 핸들러 구현 (500)
- [ ] 커밋: `impl: GlobalExceptionHandler Infrastructure 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Error Handling ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: GlobalExceptionHandler Infrastructure 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: GlobalExceptionHandler 테스트 정리 (Tidy)`

---

### Phase 4: UploadSessionApiController (7 사이클)

#### 1️⃣2️⃣ UploadSessionApiController 기본 구조 (Cycle 12)

**🔴 Red: 테스트 작성**
- [ ] `UploadSessionApiControllerTest.java` 생성 (@WebMvcTest)
- [ ] MockBean 설정 (PrepareUploadInPort, CompleteUploadInPort 등)
- [ ] `shouldReturnBaseUrl()` 테스트 작성 (GET /api/v1/upload-sessions)
- [ ] 커밋: `test: UploadSessionApiController 기본 구조 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `UploadSessionApiController.java` 생성
- [ ] `@RestController`, `@RequestMapping("/api/v1/upload-sessions")` 설정
- [ ] 4개 Port 필드 선언 (생성자 주입)
- [ ] 커밋: `impl: UploadSessionApiController 기본 구조 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Controller ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: UploadSessionApiController 기본 구조 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UploadSessionApiController 테스트 정리 (Tidy)`

---

#### 1️⃣3️⃣ POST /api/v1/upload-sessions (Part 1: 정상 케이스) (Cycle 13)

**🔴 Red: 테스트 작성**
- [ ] `shouldPrepareUpload_withValidRequest()` 테스트 작성
- [ ] MockMvc로 POST 요청 시뮬레이션
- [ ] JWT 인증 Mock 설정
- [ ] 커밋: `test: prepareUpload 정상 케이스 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `prepareUpload()` 메서드 구현
- [ ] `@Valid @RequestBody PrepareUploadRequest` 파라미터
- [ ] `@AuthenticationPrincipal JwtUser` 파라미터
- [ ] UserContext.from(JwtUser) 호출
- [ ] PrepareUploadMapper.toCommand() 호출
- [ ] PrepareUploadInPort.execute() 호출
- [ ] PrepareUploadResponse.from() 변환
- [ ] ResponseEntity.ok() 반환
- [ ] 커밋: `impl: prepareUpload 정상 케이스 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (POST, 200 OK)
- [ ] 커밋: `refactor: prepareUpload 정상 케이스 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: prepareUpload 테스트 정리 (Tidy)`

---

#### 1️⃣4️⃣ POST /api/v1/upload-sessions (Part 2: Validation & 예외) (Cycle 14)

**🔴 Red: 테스트 작성**
- [ ] `shouldReturnBadRequest_whenValidationFails()` 테스트 작성
- [ ] `shouldReturnUnauthorized_whenNoJwtToken()` 테스트 작성
- [ ] 커밋: `test: prepareUpload Validation 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] 테스트 통과 (@Valid 어노테이션으로 자동 처리)
- [ ] 커밋: `impl: prepareUpload Validation 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Validation 메시지 검증
- [ ] 커밋: `refactor: prepareUpload Validation 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: prepareUpload 테스트 정리 (Tidy)`

---

#### 1️⃣5️⃣ POST /api/v1/upload-sessions/{sessionId}/complete (Cycle 15)

**🔴 Red: 테스트 작성**
- [ ] `shouldCompleteUpload_withValidSessionId()` 테스트 작성
- [ ] `shouldReturnNotFound_whenSessionNotFound()` 테스트 작성
- [ ] `shouldReturnForbidden_whenUnauthorizedAccess()` 테스트 작성
- [ ] 커밋: `test: completeUpload 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `completeUpload()` 메서드 구현
- [ ] `@PathVariable String sessionId` 파라미터
- [ ] CompleteUploadCommand 생성
- [ ] CompleteUploadInPort.execute() 호출
- [ ] FileDetailResponse.from() 변환
- [ ] ResponseEntity.ok() 반환
- [ ] 커밋: `impl: completeUpload 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (POST, 200 OK)
- [ ] 커밋: `refactor: completeUpload 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: completeUpload 테스트 정리 (Tidy)`

---

#### 1️⃣6️⃣ POST /api/v1/upload-sessions/{sessionId}/abort (Cycle 16)

**🔴 Red: 테스트 작성**
- [ ] `shouldAbortUpload_withValidSessionId()` 테스트 작성
- [ ] `shouldReturnNotFound_whenSessionNotFound()` 테스트 작성
- [ ] 커밋: `test: abortUpload 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `abortUpload()` 메서드 구현
- [ ] AbortUploadCommand 생성
- [ ] AbortUploadInPort.execute() 호출
- [ ] ResponseEntity.noContent() 반환 (204)
- [ ] 커밋: `impl: abortUpload 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (POST, 204 No Content)
- [ ] 커밋: `refactor: abortUpload 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: abortUpload 테스트 정리 (Tidy)`

---

#### 1️⃣7️⃣ GET /api/v1/upload-sessions/{sessionId} (Cycle 17)

**🔴 Red: 테스트 작성**
- [ ] `shouldGetUploadSession_withValidSessionId()` 테스트 작성
- [ ] `shouldReturnNotFound_whenSessionNotFound()` 테스트 작성
- [ ] 커밋: `test: getUploadSession 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `getUploadSession()` 메서드 구현
- [ ] GetUploadSessionQuery 생성
- [ ] GetUploadSessionInPort.execute() 호출
- [ ] UploadSessionResponse.from() 변환
- [ ] ResponseEntity.ok() 반환
- [ ] 커밋: `impl: getUploadSession 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (GET, 200 OK)
- [ ] 커밋: `refactor: getUploadSession 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: getUploadSession 테스트 정리 (Tidy)`

---

#### 1️⃣8️⃣ UploadSessionApiController REST Docs 문서화 (Cycle 18)

**🔴 Red: 테스트 작성**
- [ ] `UploadSessionApiControllerDocsTest.java` 생성
- [ ] `shouldDocumentPrepareUpload()` 테스트 작성 (Request/Response fields)
- [ ] `shouldDocumentCompleteUpload()` 테스트 작성
- [ ] `shouldDocumentAbortUpload()` 테스트 작성
- [ ] `shouldDocumentGetUploadSession()` 테스트 작성
- [ ] 커밋: `test: UploadSessionApiController REST Docs 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] MockMvc + REST Docs 설정
- [ ] 4개 API 문서화 (requestFields, responseFields, pathParameters)
- [ ] 커밋: `impl: UploadSessionApiController REST Docs 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 문서 스니펫 정리
- [ ] 커밋: `refactor: UploadSessionApiController REST Docs 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UploadSessionApiController REST Docs 정리 (Tidy)`

---

### Phase 5: FileApiController (5 사이클)

#### 1️⃣9️⃣ FileApiController 기본 구조 (Cycle 19)

**🔴 Red: 테스트 작성**
- [ ] `FileApiControllerTest.java` 생성 (@WebMvcTest)
- [ ] MockBean 설정 (GetFileInPort, ListFilesInPort, DeleteFileInPort)
- [ ] `shouldReturnBaseUrl()` 테스트 작성 (GET /api/v1/files)
- [ ] 커밋: `test: FileApiController 기본 구조 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `FileApiController.java` 생성
- [ ] `@RestController`, `@RequestMapping("/api/v1/files")` 설정
- [ ] 3개 Port 필드 선언 (생성자 주입)
- [ ] 커밋: `impl: FileApiController 기본 구조 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Controller ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: FileApiController 기본 구조 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: FileApiController 테스트 정리 (Tidy)`

---

#### 2️⃣0️⃣ GET /api/v1/files/{fileId} (Cycle 20)

**🔴 Red: 테스트 작성**
- [ ] `shouldGetFile_withValidFileId()` 테스트 작성
- [ ] `shouldReturnNotFound_whenFileNotFound()` 테스트 작성
- [ ] `shouldReturnForbidden_whenUnauthorizedAccess()` 테스트 작성
- [ ] 커밋: `test: getFile 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `getFile()` 메서드 구현
- [ ] GetFileQuery 생성
- [ ] GetFileInPort.execute() 호출
- [ ] FileDetailResponse.from() 변환
- [ ] ResponseEntity.ok() 반환
- [ ] 커밋: `impl: getFile 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (GET, 200 OK)
- [ ] 커밋: `refactor: getFile 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: getFile 테스트 정리 (Tidy)`

---

#### 2️⃣1️⃣ GET /api/v1/files (Cycle 21)

**🔴 Red: 테스트 작성**
- [ ] `shouldListFiles_withPagination()` 테스트 작성
- [ ] `shouldListFiles_withDefaultPagination()` 테스트 작성
- [ ] 커밋: `test: listFiles 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `listFiles()` 메서드 구현
- [ ] `@RequestParam(defaultValue = "0") int page` 파라미터
- [ ] `@RequestParam(defaultValue = "20") int size` 파라미터
- [ ] ListFilesQuery 생성
- [ ] ListFilesInPort.execute() 호출
- [ ] Stream으로 FileSummaryResponse 변환
- [ ] PageResponse.of() 생성
- [ ] ResponseEntity.ok() 반환
- [ ] 커밋: `impl: listFiles 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (GET, 200 OK)
- [ ] 페이징 로직 검증
- [ ] 커밋: `refactor: listFiles 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: listFiles 테스트 정리 (Tidy)`

---

#### 2️⃣2️⃣ PATCH /api/v1/files/{fileId}/delete (Cycle 22)

**🔴 Red: 테스트 작성**
- [ ] `shouldSoftDeleteFile_withValidFileId()` 테스트 작성
- [ ] `shouldReturnNotFound_whenFileNotFound()` 테스트 작성
- [ ] 커밋: `test: softDeleteFile 테스트 추가 (Red)`

**⚠️ PATCH 사용 필수** (controller-guide.md):
- DELETE 메서드 금지 (물리 삭제 의미)
- 논리 삭제는 상태 변경 → PATCH 사용

**🟢 Green: 최소 구현**
- [ ] `softDeleteFile()` 메서드 구현
- [ ] `@PatchMapping("/{fileId}/delete")` 어노테이션
- [ ] DeleteFileCommand 생성
- [ ] DeleteFileInPort.execute() 호출
- [ ] ResponseEntity.noContent() 반환 (204)
- [ ] 커밋: `impl: softDeleteFile 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] RESTful 설계 검증 (PATCH, 204 No Content)
- [ ] 커밋: `refactor: softDeleteFile 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: softDeleteFile 테스트 정리 (Tidy)`

---

#### 2️⃣3️⃣ FileApiController REST Docs 문서화 (Cycle 23)

**🔴 Red: 테스트 작성**
- [ ] `FileApiControllerDocsTest.java` 생성
- [ ] `shouldDocumentGetFile()` 테스트 작성
- [ ] `shouldDocumentListFiles()` 테스트 작성 (Query Parameters)
- [ ] `shouldDocumentSoftDeleteFile()` 테스트 작성
- [ ] 커밋: `test: FileApiController REST Docs 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] MockMvc + REST Docs 설정
- [ ] 3개 API 문서화 (requestParameters, responseFields, pathParameters)
- [ ] 커밋: `impl: FileApiController REST Docs 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 문서 스니펫 정리
- [ ] 커밋: `refactor: FileApiController REST Docs 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: FileApiController REST Docs 정리 (Tidy)`

---

### Phase 6: Security Configuration (3 사이클)

#### 2️⃣4️⃣ JwtUser 구현 (Cycle 24)

**🔴 Red: 테스트 작성**
- [ ] `JwtUserTest.java` 생성
- [ ] `shouldCreateJwtUser()` 테스트 작성
- [ ] `shouldImplementUserDetails()` 테스트 작성
- [ ] 커밋: `test: JwtUser 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `JwtUser.java` 생성 (Record, UserDetails 구현)
- [ ] 필드: userId, tenantId, email, role
- [ ] UserDetails 메서드 구현 (getUsername, getAuthorities 등)
- [ ] 커밋: `impl: JwtUser 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Security ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: JwtUser 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `JwtUserFixture.java` 생성
- [ ] 커밋: `test: JwtUserFixture 정리 (Tidy)`

---

#### 2️⃣5️⃣ JwtTokenProvider 구현 (Cycle 25)

**🔴 Red: 테스트 작성**
- [ ] `JwtTokenProviderTest.java` 생성
- [ ] `shouldValidateToken()` 테스트 작성
- [ ] `shouldGetUserFromToken()` 테스트 작성
- [ ] 커밋: `test: JwtTokenProvider 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `JwtTokenProvider.java` 생성 (@Component)
- [ ] `validateToken(String token)` 메서드 구현
- [ ] `getUser(String token)` 메서드 구현 (JwtUser 반환)
- [ ] 커밋: `impl: JwtTokenProvider 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] JWT 라이브러리 사용 (io.jsonwebtoken)
- [ ] 커밋: `refactor: JwtTokenProvider 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: JwtTokenProvider 테스트 정리 (Tidy)`

---

#### 2️⃣6️⃣ JwtAuthenticationFilter 구현 (Cycle 26)

**🔴 Red: 테스트 작성**
- [ ] `JwtAuthenticationFilterTest.java` 생성
- [ ] `shouldAuthenticateWithValidToken()` 테스트 작성
- [ ] `shouldNotAuthenticateWithInvalidToken()` 테스트 작성
- [ ] 커밋: `test: JwtAuthenticationFilter 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `JwtAuthenticationFilter.java` 생성 (OncePerRequestFilter 상속)
- [ ] `doFilterInternal()` 메서드 구현
- [ ] `extractToken()` 메서드 구현 (Authorization 헤더 파싱)
- [ ] SecurityContextHolder 설정
- [ ] 커밋: `impl: JwtAuthenticationFilter 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Security ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: JwtAuthenticationFilter 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: JwtAuthenticationFilter 테스트 정리 (Tidy)`

---

#### 2️⃣7️⃣ SecurityConfig 구현 (Cycle 27)

**🔴 Red: 테스트 작성**
- [ ] `SecurityConfigTest.java` 생성
- [ ] `shouldConfigureSecurityFilterChain()` 테스트 작성
- [ ] 커밋: `test: SecurityConfig 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `SecurityConfig.java` 생성 (@Configuration)
- [ ] `filterChain()` Bean 정의
- [ ] CSRF 비활성화
- [ ] `/api/v1/**` 인증 필수 설정
- [ ] JwtAuthenticationFilter 추가
- [ ] 커밋: `impl: SecurityConfig 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Security ArchUnit 테스트 통과 확인
- [ ] 커밋: `refactor: SecurityConfig 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: SecurityConfig 테스트 정리 (Tidy)`

---

### Phase 7: ArchUnit Tests (3 사이클)

#### 2️⃣8️⃣ REST API Layer 의존성 규칙 (Cycle 28)

**🔴 Red: 테스트 작성**
- [ ] `RestApiLayerArchitectureTest.java` 생성
- [ ] `shouldDependOnlyOnApplicationLayer()` 테스트 작성
- [ ] `shouldNotDependOnDomainLayer()` 테스트 작성
- [ ] `shouldNotDependOnPersistenceLayer()` 테스트 작성
- [ ] 커밋: `test: REST API Layer 의존성 규칙 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: REST API Layer 의존성 규칙 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 규칙 명확화
- [ ] 커밋: `refactor: REST API Layer 의존성 규칙 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: REST API Layer 의존성 규칙 정리 (Tidy)`

---

#### 2️⃣9️⃣ Controller 네이밍 및 어노테이션 규칙 (Cycle 29)

**🔴 Red: 테스트 작성**
- [ ] `shouldHaveControllerSuffix()` 테스트 작성
- [ ] `shouldBeAnnotatedWithRestController()` 테스트 작성
- [ ] `shouldHaveRequestMapping()` 테스트 작성
- [ ] 커밋: `test: Controller 네이밍 규칙 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: Controller 네이밍 규칙 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 규칙 명확화
- [ ] 커밋: `refactor: Controller 네이밍 규칙 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: Controller 네이밍 규칙 정리 (Tidy)`

---

#### 3️⃣0️⃣ DTO Record 규칙 (Cycle 30)

**🔴 Red: 테스트 작성**
- [ ] `shouldBeRecords()` 테스트 작성 (Request/Response DTO)
- [ ] `shouldHaveRequestOrResponseSuffix()` 테스트 작성
- [ ] `shouldBeImmutable()` 테스트 작성
- [ ] 커밋: `test: DTO Record 규칙 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: DTO Record 규칙 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 규칙 명확화
- [ ] 커밋: `refactor: DTO Record 규칙 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: DTO Record 규칙 정리 (Tidy)`

---

### Phase 8: 통합 검증 (4 사이클)

#### 3️⃣1️⃣ Controller 통합 테스트 (Part 1: UploadSession) (Cycle 31)

**🔴 Red: 테스트 작성**
- [ ] `UploadSessionApiControllerIntegrationTest.java` 생성 (@SpringBootTest)
- [ ] TestRestTemplate 사용
- [ ] `shouldPrepareUpload_endToEnd()` 테스트 작성
- [ ] `shouldCompleteUpload_endToEnd()` 테스트 작성
- [ ] 커밋: `test: UploadSession 통합 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: UploadSession 통합 테스트 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 테스트 격리 확인
- [ ] 커밋: `refactor: UploadSession 통합 테스트 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: UploadSession 통합 테스트 정리 (Tidy)`

---

#### 3️⃣2️⃣ Controller 통합 테스트 (Part 2: File) (Cycle 32)

**🔴 Red: 테스트 작성**
- [ ] `FileApiControllerIntegrationTest.java` 생성 (@SpringBootTest)
- [ ] TestRestTemplate 사용
- [ ] `shouldGetFile_endToEnd()` 테스트 작성
- [ ] `shouldListFiles_endToEnd()` 테스트 작성
- [ ] `shouldSoftDeleteFile_endToEnd()` 테스트 작성
- [ ] 커밋: `test: File 통합 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: File 통합 테스트 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 테스트 격리 확인
- [ ] 커밋: `refactor: File 통합 테스트 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: File 통합 테스트 정리 (Tidy)`

---

#### 3️⃣3️⃣ Security 통합 테스트 (Cycle 33)

**🔴 Red: 테스트 작성**
- [ ] `SecurityIntegrationTest.java` 생성 (@SpringBootTest)
- [ ] `shouldAuthenticate_withValidJwtToken()` 테스트 작성
- [ ] `shouldRejectUnauthorizedRequest()` 테스트 작성
- [ ] 커밋: `test: Security 통합 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: Security 통합 테스트 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 테스트 격리 확인
- [ ] 커밋: `refactor: Security 통합 테스트 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: Security 통합 테스트 정리 (Tidy)`

---

#### 3️⃣4️⃣ 최종 검증 및 문서화 (Cycle 34)

**🔴 Red: 테스트 작성**
- [ ] 모든 ArchUnit 테스트 실행 및 통과 확인
- [ ] 테스트 커버리지 > 80% 확인
- [ ] 커밋: `test: 최종 검증 체크리스트 (Red)`

**🟢 Green: 최소 구현**
- [ ] Spring REST Docs 빌드 확인
- [ ] API 문서 생성 확인 (build/generated-snippets/)
- [ ] 커밋: `impl: 최종 검증 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 코드 리뷰 준비
- [ ] README 업데이트
- [ ] 커밋: `refactor: 최종 검증 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] 모든 Fixture 정리 완료 확인
- [ ] 커밋: `test: 최종 Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

### 구현 완료
- [ ] UploadSessionApiController 구현 (4개 엔드포인트)
- [ ] FileApiController 구현 (3개 엔드포인트)
- [ ] Request/Response DTO 정의 (7개)
- [ ] GlobalExceptionHandler 구현 (10개 예외)
- [ ] Mapper 구현 (1개)
- [ ] JwtAuthenticationFilter 구현
- [ ] SecurityConfig 구현
- [ ] JwtTokenProvider 구현
- [ ] JwtUser 구현

### 테스트 완료
- [ ] UploadSessionApiController Test (15+ 테스트)
- [ ] FileApiController Test (10+ 테스트)
- [ ] GlobalExceptionHandler Test (10+ 테스트)
- [ ] Spring REST Docs (7개 API)
- [ ] ArchUnit Test (3+ 규칙)
- [ ] Security Test (2+ 테스트)
- [ ] Controller 통합 테스트 (5+ 테스트)

### 품질 검증
- [ ] 모든 MockMvc Test 통과
- [ ] 모든 ArchUnit Test 통과
- [ ] 테스트 커버리지 > 80%
- [ ] API 문서 생성 확인
- [ ] 코드 리뷰 승인

---

## 🎯 Zero-Tolerance 규칙 준수

### RESTful 설계
- [ ] 리소스 중심 URL 설계 (/api/v1/upload-sessions, /api/v1/files)
- [ ] HTTP Method 의미론적 사용 (POST/GET/PATCH)
- [ ] 상태 코드 정확한 사용 (200/204/400/403/404/410/503)

### Validation
- [ ] `@Valid` 사용 필수
- [ ] Validation 메시지 명확화

### Error Handling
- [ ] ErrorResponse DTO 통일
- [ ] errorCode 명명 규칙 준수

### Testing
- [ ] MockMvc (Controller 단위 테스트)
- [ ] TestRestTemplate (E2E 통합 테스트)
- [ ] ArchUnit (아키텍처 규칙 검증)

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/fileflow/docs/prd/presigned-url-upload.md`
- **Task**: `/Users/sangwon-ryu/fileflow/docs/prd/session/FILE-006-004.md`
- **REST API Layer 규칙**: `docs/coding_convention/01-adapter-in-layer/rest-api/`

---

## 📝 참고사항

### TDD 진행 순서 (권장)

1. **DTO 먼저** (의존성 없음):
   - Request/Response DTO (Cycle 1-5)
2. **Mapper**:
   - PrepareUploadMapper (Cycle 6)
3. **Error Handling**:
   - ErrorResponse, ValidationErrorResponse, GlobalExceptionHandler (Cycle 7-11)
4. **Controller** (MockMvc):
   - UploadSessionApiController (Cycle 12-18)
   - FileApiController (Cycle 19-23)
5. **Security**:
   - JwtUser, JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig (Cycle 24-27)
6. **ArchUnit**:
   - 아키텍처 규칙 검증 (Cycle 28-30)
7. **통합 테스트**:
   - Controller 통합 테스트 (Cycle 31-33)
8. **최종 검증**:
   - API 문서화, 커버리지 확인 (Cycle 34)

### API 설계 체크리스트

- [ ] RESTful URL 설계 (명사 사용, 동사 금지)
- [ ] HTTP Method 의미론적 사용 (POST/GET/PATCH)
- [ ] 상태 코드 정확한 사용 (200/204/400/403/404/410/503)
- [ ] Validation 메시지 명확화
- [ ] Error Response 일관성
- [ ] PATCH /delete 사용 (DELETE 메서드 금지)

---

**다음 단계**:
1. `/kb/rest-api/go` - TDD 사이클 시작
2. 구현 완료 후 FILE-006-005 (Integration Test) 시작
