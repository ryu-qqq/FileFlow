# FILE-004: REST API Layer 구현

**Epic**: File Management System (파일 관리 시스템)
**Layer**: REST API Layer (Adapter-In)
**브랜치**: feature/FILE-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 업로드 API 엔드포인트를 RESTful 설계로 구현합니다.
- Controller (2개 엔드포인트)
- Request/Response DTO (4개)
- GlobalExceptionHandler
- API 문서화 (Spring REST Docs)

---

## 🎯 요구사항

### A. API 엔드포인트 (2개)

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | `/api/v1/files/presigned-url` | Presigned URL 발급 | GeneratePresignedUrlRequest | PresignedUrlResponse | 201 Created |
| POST | `/api/v1/files/upload-complete` | 업로드 완료 알림 | CompleteUploadRequest | FileResponse | 200 OK |

---

### B. Controller (1개)

#### FileApiController

**POST /api/v1/files/presigned-url**:
- [ ] Request DTO 검증 (`@Valid`)
- [ ] UserContext 추출 (SecurityContext)
- [ ] DTO → Command 변환
- [ ] UseCase 호출: `GeneratePresignedUrlUseCase`
- [ ] Response 반환: 201 Created

**POST /api/v1/files/upload-complete**:
- [ ] Request DTO 검증 (`@Valid`)
- [ ] UserContext 추출 (SecurityContext)
- [ ] DTO → Command 변환
- [ ] UseCase 호출: `CompleteUploadUseCase`
- [ ] Response 반환: 200 OK

**헬퍼 메서드**:
- [ ] `getCurrentUploaderType()`: UploaderType 추출

---

### C. Request DTOs (2개)

#### 1. GeneratePresignedUrlRequest
```java
public record GeneratePresignedUrlRequest(
    @NotBlank String sessionId,
    @NotBlank String fileName,
    @NotNull @Min(1) @Max(1073741824) Long fileSize,  // 1GB
    @NotBlank String mimeType,
    String category  // Admin, Seller만 사용 (nullable)
) {}
```

**검증 규칙**:
- [ ] sessionId: UUID v7 형식 (커스텀 Validator)
- [ ] fileName: 1-255자
- [ ] fileSize: 1 byte ~ 1GB
- [ ] mimeType: 허용 목록 체크 (커스텀 Validator)
- [ ] category: Admin/Seller 카테고리 검증 (커스텀 Validator)

#### 2. CompleteUploadRequest
```java
public record CompleteUploadRequest(
    @NotBlank String sessionId
) {}
```

**검증 규칙**:
- [ ] sessionId: UUID v7 형식

---

### D. Response DTOs (2개)

#### 1. PresignedUrlResponse
```java
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,  // 초 단위 (300초)
    String uploadType  // "SINGLE"
) {}
```

#### 2. FileResponse
```java
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

---

### E. GlobalExceptionHandler

#### Domain Exceptions 처리
- [ ] `SessionExpiredException` → 410 Gone
- [ ] `SessionAlreadyCompletedException` → 409 Conflict
- [ ] `FileSizeExceededException` → 400 Bad Request
- [ ] `UnsupportedMimeTypeException` → 400 Bad Request
- [ ] `SessionNotFoundException` → 404 Not Found
- [ ] `InvalidSessionStatusException` → 400 Bad Request

#### Validation 에러 처리
- [ ] `MethodArgumentNotValidException` → 400 Bad Request
- [ ] 필드별 에러 메시지 반환

#### 에러 응답 형식
```java
public record ErrorResponse(
    String code,  // "SESSION_EXPIRED"
    String message,  // "세션이 만료되었습니다"
    LocalDateTime timestamp,
    List<FieldError> fieldErrors  // Nullable
) {}

public record FieldError(
    String field,
    String message,
    Object rejectedValue
) {}
```

---

### F. 커스텀 Validators (3개)

#### 1. @UuidV7
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UuidV7Validator.class)
public @interface UuidV7 {
    String message() default "UUID v7 형식이 아닙니다";
}
```

#### 2. @AllowedMimeType
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedMimeTypeValidator.class)
public @interface AllowedMimeType {
    String message() default "지원하지 않는 MIME Type입니다";
}
```

#### 3. @AllowedFileCategory
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedFileCategoryValidator.class)
public @interface AllowedFileCategory {
    String message() default "지원하지 않는 카테고리입니다";
}
```

---

### G. API 문서화 (Spring REST Docs)

#### MockMvc 테스트
- [ ] POST /api/v1/files/presigned-url 성공 케이스
- [ ] POST /api/v1/files/presigned-url 실패 케이스 (파일 크기 초과)
- [ ] POST /api/v1/files/upload-complete 성공 케이스
- [ ] POST /api/v1/files/upload-complete 실패 케이스 (세션 만료)

#### Snippets 생성
- [ ] request-fields.adoc
- [ ] response-fields.adoc
- [ ] curl-request.adoc
- [ ] http-request.adoc
- [ ] http-response.adoc

#### API 문서 구조
```
src/docs/asciidoc/
├── index.adoc
├── file-api.adoc
└── upload-api.adoc
```

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지**: Record 사용 (Request/Response DTO)
- [ ] **Law of Demeter**: DTO는 Flat 구조 (Getter 체이닝 금지)
- [ ] **RESTful 설계**: 명사형 리소스, HTTP 메서드 의미 준수
- [ ] **Validation 필수**: 모든 Request DTO에 `@Valid`

### REST API 규칙
- [ ] **HTTP Status Code 정확히 사용**:
  - 201 Created: Presigned URL 발급
  - 200 OK: 업로드 완료
  - 400 Bad Request: 클라이언트 에러
  - 404 Not Found: 리소스 없음
  - 409 Conflict: 중복 요청
  - 410 Gone: 세션 만료
- [ ] **Content-Type**: `application/json` 필수
- [ ] **API 버전 관리**: `/api/v1/` prefix

### 테스트 규칙
- [ ] **MockMvc 테스트 필수**: TestRestTemplate 금지 (Unit Test)
- [ ] **ArchUnit 테스트 필수**:
  - Controller: `@RestController` 필수
  - Request DTO: Record 타입
  - Response DTO: Record 타입
- [ ] **Spring REST Docs 필수**: 모든 엔드포인트 문서화
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] 1개 Controller 구현 완료 (2개 엔드포인트)
- [ ] 2개 Request DTOs 구현 완료 (Record)
- [ ] 2개 Response DTOs 구현 완료 (Record)
- [ ] 3개 커스텀 Validators 구현 완료
- [ ] GlobalExceptionHandler 구현 완료
- [ ] 모든 MockMvc 테스트 통과
- [ ] ArchUnit 테스트 통과
  - `RestApiLayerDependencyRules`
  - `ControllerNamingRules`
  - `DtoRecordRules`
- [ ] Spring REST Docs 문서 생성 완료
- [ ] Zero-Tolerance 규칙 100% 준수
- [ ] 테스트 커버리지 > 80%
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mvp/file-upload-mvp.md
- **Domain Layer**: docs/prd/tasks/FILE-001.md
- **Application Layer**: docs/prd/tasks/FILE-002.md
- **Persistence Layer**: docs/prd/tasks/FILE-003.md
- **Plan**: docs/prd/plans/FILE-004-rest-api-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: docs/coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md

---

## 📚 참고 규칙

- `docs/coding_convention/01-adapter-in-layer/rest-api/controller/controller-guide.md` (Controller 패턴)
- `docs/coding_convention/01-adapter-in-layer/rest-api/dto/command/guide.md` (Request DTO)
- `docs/coding_convention/01-adapter-in-layer/rest-api/dto/response/guide.md` (Response DTO)
- `docs/coding_convention/01-adapter-in-layer/rest-api/error/error-handling-strategy.md` (에러 핸들링)
- `docs/coding_convention/01-adapter-in-layer/rest-api/controller/controller-test-restdocs-guide.md` (REST Docs)
