# REST API Layer - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: REST API
**작성일**: 2025-11-18

---

## 📋 목차

1. [API 엔드포인트](#api-엔드포인트)
2. [Controller](#controller)
3. [Request DTOs](#request-dttos)
4. [Response DTOs](#response-dttos)
5. [GlobalExceptionHandler](#globalexceptionhandler)

---

## API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/files/presigned-url | Presigned URL 발급 (세션 기반) | GeneratePresignedUrlRequest | PresignedUrlResponse | 201 Created |
| POST | /api/v1/files/upload-complete | 업로드 완료 알림 (세션 검증) | CompleteUploadRequest | FileResponse | 200 OK |

---

## Controller

### FileApiController

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/controller/FileApiController.java`

```java
/**
 * 파일 API Controller
 * <p>
 * - POST /api/v1/files/presigned-url: Presigned URL 발급
 * - POST /api/v1/files/upload-complete: 업로드 완료 처리
 * </p>
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileApiController {

    private final GeneratePresignedUrlUseCase generatePresignedUrlUseCase;
    private final CompleteUploadUseCase completeUploadUseCase;

    public FileApiController(
        GeneratePresignedUrlUseCase generatePresignedUrlUseCase,
        CompleteUploadUseCase completeUploadUseCase
    ) {
        this.generatePresignedUrlUseCase = generatePresignedUrlUseCase;
        this.completeUploadUseCase = completeUploadUseCase;
    }

    /**
     * Presigned URL 발급
     *
     * @param request GeneratePresignedUrlRequest
     * @return PresignedUrlResponse (201 Created)
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
        @Valid @RequestBody GeneratePresignedUrlRequest request
    ) {
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.of(request.sessionId()),
            FileName.of(request.fileName()),
            FileSize.of(request.fileSize()),
            MimeType.of(request.mimeType()),
            request.category() != null
                ? FileCategory.of(request.category(), getCurrentUploaderType())
                : null
        );

        PresignedUrlResponse response = generatePresignedUrlUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 업로드 완료 처리
     *
     * @param request CompleteUploadRequest
     * @return FileResponse (200 OK)
     */
    @PostMapping("/upload-complete")
    public ResponseEntity<FileResponse> completeUpload(
        @Valid @RequestBody CompleteUploadRequest request
    ) {
        CompleteUploadCommand command = new CompleteUploadCommand(
            SessionId.of(request.sessionId())
        );

        FileResponse response = completeUploadUseCase.execute(command);

        return ResponseEntity.ok(response);
    }

    private UploaderType getCurrentUploaderType() {
        UserContext userContext = (UserContext) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
        return userContext.uploaderType();
    }
}
```

---

## Request DTOs

### 1. GeneratePresignedUrlRequest

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/dto/request/GeneratePresignedUrlRequest.java`

```java
/**
 * Presigned URL 발급 요청 DTO
 * <p>
 * - sessionId: 멱등키 (UUID v7, 클라이언트 생성)
 * - category: Admin, Seller만 사용 (Customer는 null)
 * </p>
 */
public record GeneratePresignedUrlRequest(
    @NotBlank String sessionId,
    @NotBlank String fileName,
    @NotNull @Min(1) @Max(1073741824) Long fileSize,  // 1GB
    @NotBlank String mimeType,
    String category  // Nullable (Admin, Seller만 사용)
) {}
```

**유효성 검증**:
- `sessionId`: NotBlank (UUID v7 형식, 클라이언트 생성)
- `fileName`: NotBlank (1-255자)
- `fileSize`: 1 ~ 1,073,741,824 bytes (1GB)
- `mimeType`: NotBlank (허용 목록 검증은 Domain Layer에서)
- `category`: Nullable (Admin/Seller만 사용)

---

### 2. CompleteUploadRequest

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/dto/request/CompleteUploadRequest.java`

```java
/**
 * 업로드 완료 요청 DTO
 * <p>
 * - sessionId: 세션 식별자
 * </p>
 */
public record CompleteUploadRequest(
    @NotBlank String sessionId
) {}
```

---

## Response DTOs

### 1. PresignedUrlResponse

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/dto/response/PresignedUrlResponse.java`

```java
/**
 * Presigned URL 발급 응답 DTO
 * <p>
 * - expiresIn: 초 단위 (300초 = 5분)
 * - uploadType: MVP에서는 항상 "SINGLE"
 * </p>
 */
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,  // 300
    String uploadType  // "SINGLE"
) {}
```

**예시 응답**:
```json
{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "presignedUrl": "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg?X-Amz-Algorithm=...",
  "expiresIn": 300,
  "uploadType": "SINGLE"
}
```

---

### 2. FileResponse

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/dto/response/FileResponse.java`

```java
/**
 * 파일 응답 DTO
 * <p>
 * - 업로드 완료 후 반환
 * </p>
 */
public record FileResponse(
    String sessionId,
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,
    String s3Key,
    String s3Bucket,
    LocalDateTime createdAt
) {}
```

**예시 응답**:
```json
{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "fileName": "메인배너.jpg",
  "fileSize": 1048576,
  "mimeType": "image/jpeg",
  "status": "COMPLETED",
  "s3Key": "uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg",
  "s3Bucket": "fileflow-uploads-1",
  "createdAt": "2025-11-18T10:30:00"
}
```

---

## GlobalExceptionHandler

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/error/GlobalExceptionHandler.java`

```java
/**
 * 전역 예외 처리기
 * <p>
 * - Domain Exception → HTTP Status Code 매핑
 * - 표준 ErrorResponse 반환
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 세션 만료 예외 처리
     * <p>
     * - HTTP Status: 410 GONE
     * </p>
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(SessionExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)  // 410
            .body(new ErrorResponse(
                "SESSION_EXPIRED",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * 세션 이미 완료 예외 처리
     * <p>
     * - HTTP Status: 409 CONFLICT
     * </p>
     */
    @ExceptionHandler(SessionAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyCompleted(
        SessionAlreadyCompletedException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)  // 409
            .body(new ErrorResponse(
                "SESSION_ALREADY_COMPLETED",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * 파일 크기 초과 예외 처리
     * <p>
     * - HTTP Status: 400 BAD REQUEST
     * </p>
     */
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleSizeExceeded(
        FileSizeExceededException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)  // 400
            .body(new ErrorResponse(
                "FILE_SIZE_EXCEEDED",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * 지원하지 않는 MIME 타입 예외 처리
     * <p>
     * - HTTP Status: 400 BAD REQUEST
     * </p>
     */
    @ExceptionHandler(UnsupportedMimeTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMimeType(
        UnsupportedMimeTypeException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)  // 400
            .body(new ErrorResponse(
                "UNSUPPORTED_MIME_TYPE",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * 잘못된 세션 상태 예외 처리
     * <p>
     * - HTTP Status: 400 BAD REQUEST
     * </p>
     */
    @ExceptionHandler(InvalidSessionStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
        InvalidSessionStatusException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)  // 400
            .body(new ErrorResponse(
                "INVALID_SESSION_STATUS",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * 세션 없음 예외 처리
     * <p>
     * - HTTP Status: 404 NOT FOUND
     * </p>
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        SessionNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)  // 404
            .body(new ErrorResponse(
                "SESSION_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }
}
```

---

### ErrorResponse

**위치**: `rest-api/src/main/java/com/ryuqq/fileflow/restapi/error/ErrorResponse.java`

```java
/**
 * 표준 에러 응답 DTO
 * <p>
 * - code: 에러 코드 (대문자 스네이크 케이스)
 * - message: 에러 메시지 (사용자 친화적)
 * - timestamp: 에러 발생 시각
 * </p>
 */
public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp
) {}
```

**예시 응답**:
```json
{
  "code": "SESSION_EXPIRED",
  "message": "세션이 만료되었습니다: 01JD8000-1234-5678-9abc-def012345678",
  "timestamp": "2025-11-18T10:30:00"
}
```

---

## HTTP Status Code 매핑

| Domain Exception | HTTP Status | Code | 설명 |
|------------------|-------------|------|------|
| SessionExpiredException | 410 GONE | SESSION_EXPIRED | 세션 만료 (5분 초과) |
| SessionAlreadyCompletedException | 409 CONFLICT | SESSION_ALREADY_COMPLETED | 이미 완료된 세션 |
| FileSizeExceededException | 400 BAD REQUEST | FILE_SIZE_EXCEEDED | 파일 크기 초과 (1GB) |
| UnsupportedMimeTypeException | 400 BAD REQUEST | UNSUPPORTED_MIME_TYPE | 지원하지 않는 MIME 타입 |
| InvalidSessionStatusException | 400 BAD REQUEST | INVALID_SESSION_STATUS | 잘못된 세션 상태 전환 |
| SessionNotFoundException | 404 NOT FOUND | SESSION_NOT_FOUND | 세션 없음 |

---

## API 사용 예시

### 1. Presigned URL 발급 (Admin)

**Request**:
```bash
POST /api/v1/files/presigned-url
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678",
  "fileName": "메인배너.jpg",
  "fileSize": 1048576,
  "mimeType": "image/jpeg",
  "category": "banner"
}
```

**Response (201 Created)**:
```json
{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "presignedUrl": "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg?...",
  "expiresIn": 300,
  "uploadType": "SINGLE"
}
```

---

### 2. S3로 직접 업로드 (클라이언트)

```bash
PUT {presignedUrl}
Content-Type: image/jpeg

{binary data}
```

---

### 3. 업로드 완료 알림

**Request**:
```bash
POST /api/v1/files/upload-complete
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678"
}
```

**Response (200 OK)**:
```json
{
  "sessionId": "01JD8000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "fileName": "메인배너.jpg",
  "fileSize": 1048576,
  "mimeType": "image/jpeg",
  "status": "COMPLETED",
  "s3Key": "uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg",
  "s3Bucket": "fileflow-uploads-1",
  "createdAt": "2025-11-18T10:30:00"
}
```

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/single REST API Layer)
