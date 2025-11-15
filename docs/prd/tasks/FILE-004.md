# FILE-004: REST API Layer 구현

**Epic**: File Management System
**Layer**: Adapter-In Layer (REST API)
**브랜치**: feature/FILE-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 관리 시스템의 REST API를 구현합니다. RESTful 설계 원칙을 준수하고, ApiResponse<T> 표준 포맷을 사용하며, 통합 테스트로 E2E 흐름을 검증합니다.

---

## 🎯 요구사항

### Controller 구현

#### A. FileUploadController

- [ ] **POST /api/v1/files/presigned-url** - Presigned URL 발급
  - Request DTO: `GeneratePresignedUrlRequest`
  - Response DTO: `ApiResponse<PresignedUrlResponse>`
  - Validation:
    - `@NotBlank` fileName
    - `@NotNull @Min(1)` fileSize
    - `@NotBlank @Pattern(regexp="^(image|text|application)/.*")` mimeType
    - `@NotNull` uploaderId
  - HTTP Status:
    - 200 OK: 성공
    - 400 Bad Request: 검증 실패 (FILE_SIZE_EXCEEDED, INVALID_MIME_TYPE)
    - 500 Internal Server Error: S3 API 실패

- [ ] **POST /api/v1/files/{fileId}/complete** - 업로드 완료 처리
  - Path Variable: `fileId` (String, UUID v7)
  - Response DTO: `ApiResponse<FileResponse>`
  - Validation:
    - `@Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")` fileId (UUID v7 검증)
  - HTTP Status:
    - 200 OK: 성공
    - 404 Not Found: FILE_NOT_FOUND
    - 409 Conflict: INVALID_FILE_STATUS (PENDING/UPLOADING 아님)
    - 500 Internal Server Error: S3 Object 없음 (UPLOAD_VERIFICATION_FAILED)

- [ ] **POST /api/v1/files/from-url** - 외부 URL 업로드
  - Request DTO: `UploadFromExternalUrlRequest`
  - Response DTO: `ApiResponse<FileResponse>`
  - Validation:
    - `@NotBlank @Pattern(regexp="^https://.*")` externalUrl (HTTPS만)
    - `@NotNull` uploaderId
    - `@Pattern(regexp="^https://.*")` webhookUrl (선택, HTTPS만)
  - HTTP Status:
    - 202 Accepted: 비동기 작업 등록 성공
    - 400 Bad Request: INVALID_URL (HTTP 프로토콜)

- [ ] **POST /api/v1/files/{fileId}/process** - 파일 가공 요청
  - Path Variable: `fileId` (String, UUID v7)
  - Request DTO: `ProcessFileRequest`
  - Response DTO: `ApiResponse<List<FileProcessingJobResponse>>`
  - Validation:
    - `@NotEmpty` jobTypes (List<JobType>)
  - HTTP Status:
    - 202 Accepted: 비동기 작업 등록 성공
    - 404 Not Found: FILE_NOT_FOUND
    - 409 Conflict: INVALID_FILE_STATUS (COMPLETED 아님)

#### B. FileQueryController

- [ ] **GET /api/v1/files/{fileId}** - 파일 상세 조회
  - Path Variable: `fileId` (String, UUID v7)
  - Response DTO: `ApiResponse<FileDetailResponse>`
  - HTTP Status:
    - 200 OK: 성공
    - 404 Not Found: FILE_NOT_FOUND

- [ ] **GET /api/v1/files** - 파일 목록 조회 (Cursor Pagination)
  - Query Params:
    - `uploaderId` (Long, Required)
    - `status` (String, Optional)
    - `category` (String, Optional)
    - `cursor` (LocalDateTime, Optional)
    - `size` (Integer, Optional, Default: 20, Max: 100)
  - Response DTO: `ApiResponse<CursorPageResponse<FileSummaryResponse>>`
  - Validation:
    - `@NotNull` uploaderId
    - `@Min(1) @Max(100)` size
  - HTTP Status:
    - 200 OK: 성공

#### C. FileProcessingJobController

- [ ] **GET /api/v1/files/{fileId}/jobs** - 파일 가공 작업 목록 조회
  - Path Variable: `fileId` (String, UUID v7)
  - Response DTO: `ApiResponse<List<FileProcessingJobResponse>>`
  - HTTP Status:
    - 200 OK: 성공

- [ ] **GET /api/v1/jobs/{jobId}** - 가공 작업 상세 조회
  - Path Variable: `jobId` (String, UUID v7)
  - Response DTO: `ApiResponse<FileProcessingJobResponse>`
  - HTTP Status:
    - 200 OK: 성공
    - 404 Not Found: JOB_NOT_FOUND

### Request DTO 구현 (Record)

- [ ] **GeneratePresignedUrlRequest**
  ```java
  public record GeneratePresignedUrlRequest(
      @NotBlank String fileName,
      @NotNull @Min(1) Long fileSize,
      @NotBlank @Pattern(regexp = "^(image|text|application)/.*") String mimeType,
      @NotNull Long uploaderId,
      String category,
      List<String> tags
  ) {}
  ```

- [ ] **UploadFromExternalUrlRequest**
  ```java
  public record UploadFromExternalUrlRequest(
      @NotBlank @Pattern(regexp = "^https://.*") String externalUrl,
      @NotNull Long uploaderId,
      String category,
      List<String> tags,
      @Pattern(regexp = "^https://.*") String webhookUrl
  ) {}
  ```

- [ ] **ProcessFileRequest**
  ```java
  public record ProcessFileRequest(
      @NotEmpty List<JobType> jobTypes
  ) {}
  ```

### Response DTO 구현 (Record)

- [ ] **PresignedUrlResponse**
  ```java
  public record PresignedUrlResponse(
      String fileId,
      String presignedUrl,
      int expiresIn,
      String s3Key
  ) {}
  ```

- [ ] **FileResponse**
  ```java
  public record FileResponse(
      String fileId,
      String fileName,
      FileStatus status,
      String s3Url,
      String cdnUrl,
      LocalDateTime createdAt
  ) {}
  ```

- [ ] **FileDetailResponse**
  ```java
  public record FileDetailResponse(
      String fileId,
      String fileName,
      Long fileSize,
      String mimeType,
      FileStatus status,
      String s3Key,
      String s3Bucket,
      String cdnUrl,
      Long uploaderId,
      String category,
      List<String> tags,
      Integer version,
      List<FileProcessingJobResponse> jobs,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
  ```

- [ ] **FileSummaryResponse**
  ```java
  public record FileSummaryResponse(
      String fileId,
      String fileName,
      Long fileSize,
      String mimeType,
      FileStatus status,
      String cdnUrl,
      String category,
      LocalDateTime createdAt
  ) {}
  ```

- [ ] **FileProcessingJobResponse**
  ```java
  public record FileProcessingJobResponse(
      String jobId,
      String fileId,
      JobType jobType,
      JobStatus status,
      String outputS3Key,
      String errorMessage,
      LocalDateTime createdAt,
      LocalDateTime processedAt
  ) {}
  ```

- [ ] **CursorPageResponse<T>**
  ```java
  public record CursorPageResponse<T>(
      List<T> content,
      LocalDateTime nextCursor,
      boolean hasNext,
      int size
  ) {}
  ```

### Mapper 구현

- [ ] **FileRequestMapper**
  - `toCommand(GeneratePresignedUrlRequest): GeneratePresignedUrlCommand`
  - `toCommand(UploadFromExternalUrlRequest): UploadFromExternalUrlCommand`
  - `toCommand(ProcessFileRequest, String fileId): ProcessFileCommand`

- [ ] **FileResponseMapper**
  - `toPresignedUrlResponse(PresignedUrlInfo): PresignedUrlResponse`
  - `toFileResponse(File): FileResponse`
  - `toFileDetailResponse(File, List<FileProcessingJob>): FileDetailResponse`
  - `toFileSummaryResponse(File): FileSummaryResponse`
  - `toCursorPageResponse(CursorPage<File>): CursorPageResponse<FileSummaryResponse>`

- [ ] **FileProcessingJobResponseMapper**
  - `toFileProcessingJobResponse(FileProcessingJob): FileProcessingJobResponse`
  - `toFileProcessingJobResponses(List<FileProcessingJob>): List<FileProcessingJobResponse>`

### Error Code 정의

- [ ] **FileErrorCode** (Enum)
  - `FILE_NOT_FOUND` - "파일을 찾을 수 없습니다"
  - `FILE_SIZE_EXCEEDED` - "파일 크기가 제한을 초과했습니다 (최대 1GB)"
  - `INVALID_MIME_TYPE` - "지원하지 않는 파일 형식입니다"
  - `INVALID_FILE_STATUS` - "파일 상태가 유효하지 않습니다"
  - `INVALID_URL` - "유효하지 않은 URL입니다 (HTTPS만 허용)"
  - `PRESIGNED_URL_GENERATION_FAILED` - "Presigned URL 생성에 실패했습니다"
  - `UPLOAD_VERIFICATION_FAILED` - "업로드 검증에 실패했습니다"
  - `JOB_NOT_FOUND` - "가공 작업을 찾을 수 없습니다"

### Global Exception Handler

- [ ] **FileExceptionHandler** (@RestControllerAdvice)
  - `handleFileNotFoundException(FileNotFoundException): ResponseEntity<ApiResponse<Void>>`
  - `handleFileSizeExceededException(FileSizeExceededException): ResponseEntity<ApiResponse<Void>>`
  - `handleInvalidMimeTypeException(InvalidMimeTypeException): ResponseEntity<ApiResponse<Void>>`
  - `handleInvalidFileStatusException(InvalidFileStatusException): ResponseEntity<ApiResponse<Void>>`
  - `handleMethodArgumentNotValidException(MethodArgumentNotValidException): ResponseEntity<ApiResponse<Void>>`
  - `handleS3Exception(S3Exception): ResponseEntity<ApiResponse<Void>>`

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **RESTful 설계 원칙 준수**
  - URI는 명사, HTTP Method로 동작 표현
  - `/api/v1/files` (O) / `/api/v1/getFile` (X)
  - POST (생성), GET (조회), PUT (전체 수정), PATCH (부분 수정), DELETE (삭제)

- [ ] **ApiResponse<T> 표준 포맷 사용**
  - 모든 API는 `ApiResponse<T>` 반환
  - 성공: `ApiResponse.ofSuccess(data)`
  - 실패: `ApiResponse.ofFailure(ErrorInfo)`
  - Error Code는 UPPER_SNAKE_CASE

- [ ] **DTO는 Record 사용**
  - Request/Response DTO는 Record로 구현
  - Lombok 금지
  - Validation 어노테이션 필수

- [ ] **Validation 필수**
  - `@Valid` 사용 (Controller 파라미터)
  - `@NotNull`, `@NotBlank`, `@Min`, `@Max`, `@Pattern` 적극 활용
  - Custom Validator 필요 시 `ConstraintValidator` 구현

- [ ] **HTTP Status Code 전략**
  - 200 OK: 성공 (GET, POST, PATCH)
  - 202 Accepted: 비동기 작업 등록 성공
  - 400 Bad Request: 검증 실패 (Validation)
  - 404 Not Found: 리소스 없음
  - 409 Conflict: 비즈니스 규칙 위반
  - 500 Internal Server Error: 서버 오류

### 테스트 규칙

- [ ] **Integration Test (TestRestTemplate 사용)**
  - MockMvc 금지 (프록시 제약사항 회피 불가)
  - TestRestTemplate 필수 (실제 HTTP 요청)
  - @SpringBootTest(webEnvironment = RANDOM_PORT)
  - TestContainers (MySQL, Redis)
  - Flyway 마이그레이션 자동 실행

- [ ] **ArchUnit 테스트 필수**
  - Controller는 UseCase만 의존
  - DTO는 Record 검증
  - Lombok 사용 금지 검증
  - Validation 어노테이션 필수 검증

- [ ] **테스트 커버리지 > 80%**
  - Controller 모든 엔드포인트 테스트
  - Validation 테스트 (성공/실패)
  - Error Handling 테스트
  - Mapper 변환 테스트

---

## ✅ 완료 조건

- [ ] 3개 Controller 구현 완료 (FileUpload, FileQuery, FileProcessingJob)
- [ ] 3개 Request DTO 구현 완료 (Record)
- [ ] 6개 Response DTO 구현 완료 (Record)
- [ ] 3개 Mapper 구현 완료
- [ ] 8개 Error Code 정의 완료
- [ ] Global Exception Handler 구현 완료
- [ ] Integration Test (TestRestTemplate) 통과
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/file-management-system.md
- **Plan**: docs/prd/plans/FILE-004-rest-api-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **컨벤션**: docs/coding_convention/01-adapter-in-layer/rest-api/

---

## 📝 참고사항

### ApiResponse<T> 사용 예시
```java
@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generatePresignedUrl(
        @Valid @RequestBody GeneratePresignedUrlRequest request
    ) {
        PresignedUrlInfo info = generatePresignedUrlUseCase.execute(
            fileRequestMapper.toCommand(request)
        );

        PresignedUrlResponse response = fileResponseMapper.toPresignedUrlResponse(info);

        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }
}
```

### Error Handling 예시
```java
@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotFoundException(
        FileNotFoundException e
    ) {
        ErrorInfo error = new ErrorInfo("FILE_NOT_FOUND", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.ofFailure(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorInfo error = new ErrorInfo("VALIDATION_FAILED", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.ofFailure(error));
    }
}
```

### Integration Test 예시 (TestRestTemplate)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class FileUploadControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Test
    void generatePresignedUrl_성공() {
        // Given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            "example.jpg",
            1024L,
            "image/jpeg",
            1L,
            "상품",
            List.of("이미지")
        );

        // When
        ResponseEntity<ApiResponse<PresignedUrlResponse>> response = restTemplate
            .postForEntity(
                "/api/v1/files/presigned-url",
                request,
                new ParameterizedTypeReference<ApiResponse<PresignedUrlResponse>>() {}
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().presignedUrl()).isNotBlank();
        assertThat(response.getBody().data().expiresIn()).isEqualTo(300);
    }

    @Test
    void generatePresignedUrl_파일크기초과_실패() {
        // Given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            "large.jpg",
            1_073_741_825L, // 1GB + 1 byte
            "image/jpeg",
            1L,
            null,
            null
        );

        // When
        ResponseEntity<ApiResponse<Void>> response = restTemplate
            .postForEntity(
                "/api/v1/files/presigned-url",
                request,
                new ParameterizedTypeReference<ApiResponse<Void>>() {}
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().errorCode()).isEqualTo("FILE_SIZE_EXCEEDED");
    }
}
```

### Cursor Pagination 응답 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "fileId": "01234567-89ab-7cde-f012-3456789abcde",
        "fileName": "example.jpg",
        "fileSize": 1024,
        "mimeType": "image/jpeg",
        "status": "COMPLETED",
        "cdnUrl": "https://cdn.example.com/files/01234567-89ab-7cde-f012-3456789abcde.jpg",
        "category": "상품",
        "createdAt": "2025-11-13T12:34:56"
      }
    ],
    "nextCursor": "2025-11-13T12:34:56",
    "hasNext": true,
    "size": 1
  },
  "error": null,
  "timestamp": "2025-11-13T12:35:00",
  "requestId": "abc123"
}
```

### Validation 예시
```java
public record GeneratePresignedUrlRequest(
    @NotBlank(message = "파일명은 필수입니다")
    String fileName,

    @NotNull(message = "파일 크기는 필수입니다")
    @Min(value = 1, message = "파일 크기는 1 바이트 이상이어야 합니다")
    Long fileSize,

    @NotBlank(message = "MIME 타입은 필수입니다")
    @Pattern(
        regexp = "^(image|text|application)/.*",
        message = "지원하지 않는 MIME 타입입니다"
    )
    String mimeType,

    @NotNull(message = "업로더 ID는 필수입니다")
    Long uploaderId,

    String category,
    List<String> tags
) {}
```
