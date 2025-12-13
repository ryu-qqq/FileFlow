# FILE-ASSET-PROCESSING REST API Layer TDD Plan

> **Jira Issue**: [KAN-342](https://ryuqqq.atlassian.net/browse/KAN-342)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)

## Overview
- **PRD**: `docs/prd/file-asset-processing.md`
- **Layer**: REST API (Adapter-In)
- **Estimated Time**: 240 minutes (4 hours)
- **Total Cycles**: 16 TDD cycles

---

## API Endpoints

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| POST | /api/v1/file-assets/{id}/process | 수동 가공 트리거 | 202 Accepted |
| PATCH | /api/v1/file-assets/{id}/status | 상태 업데이트 (n8n용) | 200 OK |
| GET | /api/v1/file-assets | 파일 목록 조회 (n8n용) | 200 OK |
| GET | /api/v1/file-assets/{id} | 파일 상세 조회 | 200 OK |
| GET | /api/v1/file-assets/{id}/download | 다운로드 URL 조회 | 200 OK |
| GET | /api/v1/file-assets/{id}/processed | 가공된 파일 목록 | 200 OK |

---

## Zero-Tolerance Rules (REST API Layer)

### Must Follow
- [x] **MockMvc 금지** - TestRestTemplate 필수
- [x] **@SpringBootTest(webEnvironment = RANDOM_PORT)** - 실제 서버 구동
- [x] **Lombok 금지** - Plain Java 사용
- [x] **@Valid 검증** - Request DTO 검증 필수
- [x] **RESTful 설계** - 표준 HTTP 상태 코드 사용
- [x] **record DTO** - Request/Response DTO는 record 사용

### Test Requirements
- **테스트 환경**: @SpringBootTest + TestRestTemplate
- **TestContainers**: MySQL 실제 DB 사용
- **인증 Mock**: SecurityContextHolder 또는 @WithMockUser 대체 방식

---

## TDD Cycles

### Cycle 1: UpdateStatusRequest DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/UpdateStatusRequestTest.java
[ ] 테스트: `생성_유효한상태_성공()` - status: RESIZED, message: "완료"
[ ] 테스트: `생성_status_null_검증실패()` - @NotNull 검증
[ ] 테스트: `생성_message_null_허용()` - message는 nullable
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: UpdateStatusRequest DTO 검증 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/UpdateStatusRequest.java
[ ] record 사용 (Lombok 금지!)
[ ] @NotNull FileAssetStatus status
[ ] String message (nullable)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: UpdateStatusRequest DTO 구현"
```

---

### Cycle 2: FileAssetDetailResponse DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/FileAssetDetailResponseTest.java
[ ] 테스트: `생성_모든필드매핑()` - 전체 필드 검증
[ ] 테스트: `생성_processedFiles_빈리스트허용()` - 가공 파일 없음
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetDetailResponse DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/FileAssetDetailResponse.java
[ ] record 사용
[ ] 필드: id, sessionId, fileName, fileSize, contentType, category, status, statusMessage, bucket, s3Key, downloadUrl, userId, organizationId, tenantId, createdAt, processedAt, processedFiles
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetDetailResponse DTO 구현"
```

---

### Cycle 3: ProcessedFileResponse DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/ProcessedFileResponseTest.java
[ ] 테스트: `생성_모든필드매핑()` - id, variant, format, fileName, fileSize, width, height, downloadUrl
[ ] 테스트: `생성_width_height_nullable()` - 이미지 아닌 경우 null
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileResponse DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/ProcessedFileResponse.java
[ ] record 사용
[ ] 필드: id, variant, format, fileName, fileSize, width, height, downloadUrl
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileResponse DTO 구현"
```

---

### Cycle 4: DownloadUrlResponse DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/DownloadUrlResponseTest.java
[ ] 테스트: `생성_모든필드매핑()` - fileAssetId, variant, format, downloadUrl, expiresAt
[ ] 테스트: `생성_expiresAt_미래시간()` - 만료 시간 검증
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: DownloadUrlResponse DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/DownloadUrlResponse.java
[ ] record 사용
[ ] 필드: fileAssetId, variant, format, downloadUrl, expiresAt
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: DownloadUrlResponse DTO 구현"
```

---

### Cycle 5: ListFileAssetsQueryParams DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/ListFileAssetsQueryParamsTest.java
[ ] 테스트: `생성_기본값_page0_size20()` - 기본 페이징
[ ] 테스트: `생성_size_최대100제한()` - @Max(100) 검증
[ ] 테스트: `생성_모든필터_설정()` - status, contentType, category, from, to
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ListFileAssetsQueryParams DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/ListFileAssetsQueryParams.java
[ ] record 또는 class 사용 (Query param binding)
[ ] 필드: status, contentType, category, from, to, page (default 0), size (default 20, max 100)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ListFileAssetsQueryParams DTO 구현"
```

---

### Cycle 6: FileAssetResponseMapper (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/mapper/FileAssetResponseMapperTest.java
[ ] 테스트: `toDetailResponse_Domain에서Response변환()` - FileAsset → FileAssetDetailResponse
[ ] 테스트: `toProcessedFileResponse_Domain에서Response변환()` - ProcessedFileAsset → ProcessedFileResponse
[ ] 테스트: `toDownloadUrlResponse_URL정보변환()` - DownloadUrlResponse 생성
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetResponseMapper Domain-Response 변환 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Mapper 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/mapper/FileAssetResponseMapper.java
[ ] @Component 어노테이션
[ ] toDetailResponse() 메서드
[ ] toProcessedFileResponse() 메서드
[ ] toDownloadUrlResponse() 메서드
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetResponseMapper 구현"
```

---

### Cycle 7: FileAssetController - POST /process (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/FileAssetControllerTest.java
[ ] @SpringBootTest(webEnvironment = RANDOM_PORT) + @Testcontainers
[ ] @Autowired TestRestTemplate
[ ] 테스트: `processFileAsset_성공_202Accepted()` - POST /api/v1/file-assets/{id}/process
[ ] 테스트: `processFileAsset_없는ID_404NotFound()` - FileAssetNotFoundException
[ ] 테스트: `processFileAsset_이미처리중_409Conflict()` - AlreadyProcessingException
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetController 가공 트리거 API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Controller 파일 생성 (또는 확장): adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/FileAssetController.java
[ ] @RestController + @RequestMapping("/api/v1/file-assets")
[ ] @PostMapping("/{id}/process")
  - ProcessFileAssetUseCase 의존성 주입
  - return ResponseEntity.accepted().body(response)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 가공 트리거 API 구현"
```

---

### Cycle 8: FileAssetController - PATCH /status (20분)

**Red Phase** - `test:`
```
[ ] 테스트: `updateStatus_성공_200OK()` - PATCH /api/v1/file-assets/{id}/status
[ ] 테스트: `updateStatus_유효하지않은상태전환_400BadRequest()` - InvalidStatusTransitionException
[ ] 테스트: `updateStatus_status_null_400BadRequest()` - @Valid 검증 실패
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAssetController 상태 업데이트 API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @PatchMapping("/{id}/status")
[ ] @RequestBody @Valid UpdateStatusRequest request
[ ] UpdateFileAssetStatusUseCase 의존성 주입
[ ] return ResponseEntity.ok().body(response)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 상태 업데이트 API 구현"
```

---

### Cycle 9: FileAssetController - GET / (목록 조회) (20분)

**Red Phase** - `test:`
```
[ ] 테스트: `listFileAssets_성공_200OK()` - GET /api/v1/file-assets
[ ] 테스트: `listFileAssets_필터적용_결과반환()` - status=RESIZED&category=PRODUCT_IMAGE
[ ] 테스트: `listFileAssets_페이징_정상동작()` - page=1&size=10
[ ] 테스트: `listFileAssets_size_초과_400BadRequest()` - size=200 (max 100 초과)
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAssetController 목록 조회 API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @GetMapping
[ ] @ModelAttribute @Valid ListFileAssetsQueryParams params
[ ] ListFileAssetsForN8nUseCase 의존성 주입
[ ] PageResponse<FileAssetForN8nResponse> 반환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 목록 조회 API 구현"
```

---

### Cycle 10: FileAssetController - GET /{id} (상세 조회) (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `getFileAsset_성공_200OK()` - GET /api/v1/file-assets/{id}
[ ] 테스트: `getFileAsset_없는ID_404NotFound()` - FileAssetNotFoundException
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAssetController 상세 조회 API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @GetMapping("/{id}")
[ ] FileAssetQueryPort (또는 전용 UseCase) 사용
[ ] FileAssetDetailResponse 반환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 상세 조회 API 구현"
```

---

### Cycle 11: FileAssetController - GET /{id}/download (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `getDownloadUrl_성공_200OK()` - GET /api/v1/file-assets/{id}/download
[ ] 테스트: `getDownloadUrl_variant지정_해당버전URL()` - ?variant=LARGE&format=WEBP
[ ] 테스트: `getDownloadUrl_없는ID_404NotFound()` - FileAssetNotFoundException
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAssetController 다운로드 URL API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @GetMapping("/{id}/download")
[ ] @RequestParam(required = false) String variant
[ ] @RequestParam(required = false) String format
[ ] Presigned URL 생성 (S3StoragePort 사용)
[ ] DownloadUrlResponse 반환 (expiresAt: 15분)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 다운로드 URL API 구현"
```

---

### Cycle 12: FileAssetController - GET /{id}/processed (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `getProcessedFiles_성공_200OK()` - GET /api/v1/file-assets/{id}/processed
[ ] 테스트: `getProcessedFiles_가공파일있음_리스트반환()` - 여러 variant/format
[ ] 테스트: `getProcessedFiles_가공파일없음_빈리스트()` - 아직 가공 안 됨
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAssetController 가공 파일 목록 API 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @GetMapping("/{id}/processed")
[ ] ProcessedFileAssetQueryPort 사용
[ ] List<ProcessedFileResponse> 반환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetController 가공 파일 목록 API 구현"
```

---

### Cycle 13: GlobalExceptionHandler - FileAsset 예외 (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성 (또는 확장): adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/GlobalExceptionHandlerTest.java
[ ] 테스트: `handleFileAssetNotFound_404반환()` - FileAssetNotFoundException
[ ] 테스트: `handleInvalidStatusTransition_400반환()` - InvalidStatusTransitionException
[ ] 테스트: `handleAlreadyProcessing_409반환()` - AlreadyProcessingException
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: GlobalExceptionHandler FileAsset 예외 처리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] GlobalExceptionHandler 확장 (또는 생성)
[ ] @ExceptionHandler(FileAssetNotFoundException.class)
  - return ResponseEntity.notFound().build()
[ ] @ExceptionHandler(InvalidStatusTransitionException.class)
  - return ResponseEntity.badRequest().body(...)
[ ] @ExceptionHandler(AlreadyProcessingException.class)
  - return ResponseEntity.status(CONFLICT).body(...)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: GlobalExceptionHandler FileAsset 예외 처리 구현"
```

---

### Cycle 14: GlobalExceptionHandler - Processing 예외 (15분)

**Red Phase** - `test:`
```
[ ] 테스트: `handleImageProcessingFailed_500반환()` - ImageProcessingException
[ ] 테스트: `handleHtmlParsingFailed_500반환()` - HtmlParsingException
[ ] 테스트: `handleExternalImageDownloadFailed_502반환()` - ImageDownloadException
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: GlobalExceptionHandler Processing 예외 처리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] @ExceptionHandler(ImageProcessingException.class)
  - return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(...)
[ ] @ExceptionHandler(HtmlParsingException.class)
  - return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(...)
[ ] @ExceptionHandler(ImageDownloadException.class)
  - return ResponseEntity.status(BAD_GATEWAY).body(...)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: GlobalExceptionHandler Processing 예외 처리 구현"
```

---

### Cycle 15: ErrorResponse DTO (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/dto/ErrorResponseTest.java
[ ] 테스트: `생성_모든필드매핑()` - errorCode, message, timestamp
[ ] 테스트: `of_팩토리메서드_생성()` - 정적 팩토리
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ErrorResponse DTO 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 파일 생성: adapter-in-rest/src/main/java/com/fileflow/fileasset/adapter/in/rest/dto/ErrorResponse.java
[ ] record 사용
[ ] 필드: errorCode, message, timestamp
[ ] of() 정적 팩토리 메서드
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ErrorResponse DTO 구현"
```

---

### Cycle 16: API 문서화 - REST Docs (20분)

**Red Phase** - `test:`
```
[ ] REST Docs 테스트 파일 생성: adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/docs/FileAssetApiDocsTest.java
[ ] @AutoConfigureRestDocs
[ ] 테스트: `processFileAsset_API문서생성()` - POST /process 문서
[ ] 테스트: `updateStatus_API문서생성()` - PATCH /status 문서
[ ] 테스트: `listFileAssets_API문서생성()` - GET / 문서
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAsset API REST Docs 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] REST Docs 설정 추가 (필요 시)
[ ] 각 API 테스트에 document() 추가
  - requestFields, responseFields, pathParameters, queryParameters
[ ] Asciidoc 파일 생성 확인
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAsset API REST Docs 문서화 구현"
```

---

## Tidy Phase: TestFixture 생성 (전체 완료 후)

**Refactor Phase** - `struct:`
```
[ ] FileAssetControllerTestFixture 생성
  - createFileAsset() - 테스트용 FileAsset
  - createProcessedFileAssets() - 가공된 파일 목록
[ ] UpdateStatusRequestTestFixture 생성
  - withStatus(FileAssetStatus)
  - withMessage(String)
[ ] ListFileAssetsQueryParamsTestFixture 생성
  - withDefaults()
  - withFilters(status, category, ...)
[ ] 기존 테스트에 TestFixture 적용
[ ] Commit: "struct: REST API TestFixture 생성 및 적용"
```

---

## ArchUnit 규칙 검증

**파일**: `adapter-in-rest/src/test/java/com/fileflow/fileasset/adapter/in/rest/architecture/RestApiArchitectureTest.java`

```
[ ] Controller는 UseCase Port만 의존 (Repository 직접 의존 금지)
[ ] DTO는 adapter-in-rest 패키지 내부에만 존재
[ ] Mapper는 Domain ↔ DTO 변환만 담당
[ ] @RestController 클래스 네이밍: *Controller
[ ] Commit: "test: REST API ArchUnit 규칙 테스트 추가"
```

---

## Summary

| Phase | Cycles | Estimated Time |
|-------|--------|----------------|
| Request DTO (1, 5) | 2 | 20분 |
| Response DTO (2-4) | 3 | 30분 |
| Mapper (6) | 1 | 15분 |
| Controller (7-12) | 6 | 105분 |
| ExceptionHandler (13-15) | 3 | 40분 |
| REST Docs (16) | 1 | 20분 |
| TestFixture + ArchUnit | 2 | 30분 |
| **Total** | **16+2** | **260분 (약 4.3시간)** |

---

## Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| FILE_ASSET_NOT_FOUND | 404 | 파일 없음 |
| INVALID_STATUS_TRANSITION | 400 | 잘못된 상태 전환 |
| PROCESSING_IN_PROGRESS | 409 | 이미 가공 중 |
| IMAGE_PROCESSING_FAILED | 500 | 이미지 가공 실패 |
| HTML_PARSING_FAILED | 500 | HTML 파싱 실패 |
| EXTERNAL_IMAGE_DOWNLOAD_FAILED | 502 | 외부 이미지 다운로드 실패 |
| VALIDATION_ERROR | 400 | 요청 검증 실패 |

---

## Checklist Before Starting

- [ ] Domain Layer Plan 완료 확인
- [ ] Application Layer Plan 완료 확인
- [ ] Persistence Layer Plan 완료 확인
- [ ] Infrastructure Layer Plan 완료 확인
- [ ] Application UseCase 인터페이스 및 구현 완료
- [ ] Domain 예외 클래스 정의 완료
- [ ] TestContainers MySQL 설정 확인
- [ ] Spring Security 설정 (테스트용 인증 처리)
