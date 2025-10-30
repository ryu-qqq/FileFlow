# Phase 3A: 핵심 파일 관리 기능 구현 가이드

## 📋 Phase 3A 개요
- **목표**: 파일 생성/조회/수정/삭제 기본 기능 구현
- **기간**: 3일 (Day 1-3)
- **태스크 수**: 7개

---

## 🎯 KAN-293: FileManagement REST Controller 구현

### 작업 내용
FileAsset 관련 REST API 엔드포인트를 구현합니다.

### 구현 체크리스트

#### 1. Controller 클래스 생성
```java
package com.ryuqq.fileflow.adapter.rest.file.controller;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "파일 관리 API")
public class FileManagementController {
    private final CreateFileAssetUseCase createFileAssetUseCase;
    private final UpdateFileAssetUseCase updateFileAssetUseCase;
    private final DeleteFileAssetUseCase deleteFileAssetUseCase;
    private final QueryFileAssetUseCase queryFileAssetUseCase;
    private final FileApiMapper mapper;

    // 구현할 엔드포인트들...
}
```

#### 2. 엔드포인트 구현

##### 2.1 파일 생성
```java
@PostMapping
@Operation(summary = "파일 생성", description = "업로드 완료 후 파일 메타데이터 생성")
public ResponseEntity<FileAssetResponse> createFile(
    @Valid @RequestBody CreateFileRequest request,
    @RequestHeader("X-Tenant-Id") Long tenantId,
    @RequestHeader("X-Organization-Id") Long organizationId
) {
    // 1. Command 변환
    CreateFileAssetCommand command = mapper.toCommand(request, tenantId, organizationId);

    // 2. UseCase 실행
    FileAssetResponse response = createFileAssetUseCase.execute(command);

    // 3. Response 반환
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

##### 2.2 파일 조회
```java
@GetMapping("/{fileAssetId}")
@Operation(summary = "파일 조회", description = "파일 메타데이터 조회")
public ResponseEntity<FileAssetResponse> getFile(
    @PathVariable String fileAssetId,
    @RequestHeader("X-Tenant-Id") Long tenantId
) {
    QueryFileAssetCommand command = new QueryFileAssetCommand(fileAssetId, tenantId);
    FileAssetResponse response = queryFileAssetUseCase.execute(command);
    return ResponseEntity.ok(response);
}
```

##### 2.3 파일 목록 조회
```java
@GetMapping
@Operation(summary = "파일 목록 조회", description = "페이징 처리된 파일 목록")
public ResponseEntity<Page<FileAssetResponse>> getFiles(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String status,
    @RequestHeader("X-Tenant-Id") Long tenantId,
    @RequestHeader("X-Organization-Id") Long organizationId
) {
    // Pageable 생성
    Pageable pageable = PageRequest.of(page, size);

    // Query 실행
    Page<FileAssetResponse> files = queryFileAssetUseCase.findAll(
        tenantId, organizationId, status, pageable
    );

    return ResponseEntity.ok(files);
}
```

##### 2.4 파일 수정
```java
@PutMapping("/{fileAssetId}")
@Operation(summary = "파일 메타데이터 수정")
public ResponseEntity<FileAssetResponse> updateFile(
    @PathVariable String fileAssetId,
    @Valid @RequestBody UpdateFileRequest request,
    @RequestHeader("X-Tenant-Id") Long tenantId
) {
    UpdateFileAssetCommand command = mapper.toUpdateCommand(
        fileAssetId, request, tenantId
    );
    FileAssetResponse response = updateFileAssetUseCase.execute(command);
    return ResponseEntity.ok(response);
}
```

##### 2.5 파일 삭제 (Soft Delete)
```java
@DeleteMapping("/{fileAssetId}")
@Operation(summary = "파일 삭제", description = "논리 삭제 처리")
public ResponseEntity<Void> deleteFile(
    @PathVariable String fileAssetId,
    @RequestHeader("X-Tenant-Id") Long tenantId
) {
    DeleteFileAssetCommand command = new DeleteFileAssetCommand(fileAssetId, tenantId);
    deleteFileAssetUseCase.execute(command);
    return ResponseEntity.noContent().build();
}
```

#### 3. Request/Response DTO

##### CreateFileRequest.java
```java
package com.ryuqq.fileflow.adapter.rest.file.dto;

public class CreateFileRequest {
    @NotBlank(message = "uploadSessionId는 필수입니다")
    private String uploadSessionId;

    @NotBlank(message = "파일명은 필수입니다")
    private String fileName;

    @NotNull(message = "파일 크기는 필수입니다")
    @Min(0)
    private Long fileSize;

    @NotBlank(message = "MIME 타입은 필수입니다")
    private String mimeType;

    private String bucketName;
    private String objectKey;

    // 수동 Getter (NO Lombok!)
    public String getUploadSessionId() {
        return uploadSessionId;
    }

    public void setUploadSessionId(String uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }

    // ... 나머지 getter/setter
}
```

##### FileAssetResponse.java
```java
public class FileAssetResponse {
    private String fileAssetId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String status;
    private String fileUrl;
    private Instant createdAt;
    private Instant updatedAt;

    // Static Factory Method
    public static FileAssetResponse of(FileAsset domain) {
        FileAssetResponse response = new FileAssetResponse();
        response.fileAssetId = domain.getFileAssetId();
        response.fileName = domain.getFileName();
        response.fileSize = domain.getFileSize();
        response.mimeType = domain.getMimeType();
        response.status = domain.getStatus().name();
        response.createdAt = domain.getCreatedAt();
        response.updatedAt = domain.getUpdatedAt();
        return response;
    }

    // Getter만 (NO Setter for Response)
    public String getFileAssetId() {
        return fileAssetId;
    }

    // ... 나머지 getter
}
```

#### 4. Exception Handler
```java
@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException e) {
        ErrorResponse error = ErrorResponse.of(
            "FILE_NOT_FOUND",
            e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateFile(DuplicateFileException e) {
        ErrorResponse error = ErrorResponse.of(
            "DUPLICATE_FILE",
            e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

### 주의사항
1. **NO Lombok** - 모든 getter/setter 수동 작성
2. **Validation** - @Valid로 입력값 검증
3. **Error Handling** - 적절한 HTTP 상태 코드 반환
4. **Swagger 문서화** - @Operation 어노테이션 필수
5. **Header 처리** - Tenant/Organization 정보는 Header로

### 테스트 작성
```java
@WebMvcTest(FileManagementController.class)
class FileManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateFileAssetUseCase createFileAssetUseCase;

    @Test
    void 파일_생성_성공() throws Exception {
        // Given
        CreateFileRequest request = new CreateFileRequest();
        request.setUploadSessionId("upload-123");
        request.setFileName("test.pdf");
        request.setFileSize(1024L);
        request.setMimeType("application/pdf");

        FileAssetResponse response = new FileAssetResponse();
        // ... response 설정

        when(createFileAssetUseCase.execute(any()))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/files")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Tenant-Id", "1")
                .header("X-Organization-Id", "1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fileAssetId").exists());
    }
}
```

---

## 🎯 KAN-292: UploadCompletedEventListener 구현

### 작업 내용
업로드 완료 이벤트를 수신하여 FileAsset을 자동 생성하는 Event Adapter를 구현합니다.

### 구현 체크리스트

#### 1. Event Listener 구현
```java
package com.ryuqq.fileflow.adapter.event;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCompletedEventListener {
    private final CreateFileAssetUseCase createFileAssetUseCase;
    private final UploadSessionQueryPort uploadSessionQueryPort;

    @EventListener
    @Async  // 비동기 처리
    public void handleUploadCompleted(UploadCompletedEvent event) {
        log.info("업로드 완료 이벤트 수신: uploadSessionId={}",
            event.getUploadSessionId());

        try {
            // 1. UploadSession 조회
            UploadSession session = uploadSessionQueryPort
                .findById(event.getUploadSessionId())
                .orElseThrow(() -> new UploadSessionNotFoundException(
                    event.getUploadSessionId()
                ));

            // 2. FileAsset 생성 Command
            CreateFileAssetCommand command = CreateFileAssetCommand.builder()
                .uploadSessionId(session.getId())
                .fileAssetId(generateFileAssetId())
                .fileName(session.getFileName())
                .fileSize(session.getFileSize())
                .mimeType(session.getMimeType())
                .bucketName(session.getBucketName())
                .objectKey(session.getObjectKey())
                .tenantId(session.getTenantId())
                .organizationId(session.getOrganizationId())
                .build();

            // 3. FileAsset 생성
            FileAssetResponse created = createFileAssetUseCase.execute(command);

            log.info("FileAsset 생성 완료: fileAssetId={}",
                created.getFileAssetId());

        } catch (Exception e) {
            log.error("FileAsset 생성 실패: uploadSessionId={}",
                event.getUploadSessionId(), e);
            // 실패 시 재처리 또는 DLQ로 전송
            handleFailure(event, e);
        }
    }

    private String generateFileAssetId() {
        return "FILE-" + UUID.randomUUID().toString();
    }

    private void handleFailure(UploadCompletedEvent event, Exception e) {
        // 재시도 로직 또는 Dead Letter Queue 처리
        // 예: SQS DLQ로 전송
    }
}
```

#### 2. 비동기 설정
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public TaskExecutor fileEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("file-event-");
        executor.setRejectedExecutionHandler(
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.initialize();
        return executor;
    }
}
```

#### 3. 재시도 메커니즘
```java
@Component
@RequiredArgsConstructor
public class FileAssetCreationRetryHandler {
    private final CreateFileAssetUseCase createFileAssetUseCase;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Retryable(
        value = {TransientException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public FileAssetResponse createWithRetry(CreateFileAssetCommand command) {
        return createFileAssetUseCase.execute(command);
    }

    @Recover
    public FileAssetResponse recover(TransientException e,
                                    CreateFileAssetCommand command) {
        log.error("FileAsset 생성 최종 실패: command={}", command, e);
        // DLQ로 전송 또는 알림
        sendToDeadLetterQueue(command);
        throw new FileAssetCreationFailedException(
            "최대 재시도 횟수 초과", e
        );
    }
}
```

### 주의사항
1. **비동기 처리** - 이벤트 처리는 비동기로
2. **에러 핸들링** - 실패 시 재시도 또는 DLQ
3. **멱등성** - 중복 이벤트 처리 방지
4. **로깅** - 충분한 로깅으로 추적 가능하게

---

## 🎯 KAN-291: FilePermissionEvaluation Adapter 구현

### 작업 내용
IAM 시스템과 연동하여 파일 접근 권한을 평가하는 Adapter를 구현합니다.

### 구현 체크리스트

#### 1. Permission Evaluator 구현
```java
package com.ryuqq.fileflow.adapter.security;

@Component
@RequiredArgsConstructor
public class FilePermissionEvaluator {
    private final IamServiceClient iamClient;
    private final FileAssetQueryPort fileAssetQueryPort;

    /**
     * 파일 접근 권한 평가
     * @param userId 사용자 ID
     * @param fileAssetId 파일 ID
     * @param permission 필요한 권한 (READ, WRITE, DELETE)
     * @return 권한 여부
     */
    public boolean hasPermission(Long userId, String fileAssetId,
                                FilePermission permission) {
        // 1. 파일 정보 조회
        FileAsset fileAsset = fileAssetQueryPort
            .findByFileAssetId(fileAssetId)
            .orElseThrow(() -> new FileNotFoundException(fileAssetId));

        // 2. 사용자 권한 조회 (IAM 연동)
        UserPermissions userPermissions = iamClient.getUserPermissions(
            userId,
            fileAsset.getTenantId(),
            fileAsset.getOrganizationId()
        );

        // 3. 권한 평가
        return evaluatePermission(fileAsset, userPermissions, permission);
    }

    private boolean evaluatePermission(FileAsset fileAsset,
                                      UserPermissions userPermissions,
                                      FilePermission permission) {
        // 소유자 확인
        if (fileAsset.getCreatedBy().equals(userPermissions.getUserId())) {
            return true;  // 소유자는 모든 권한
        }

        // 조직 권한 확인
        if (hasOrganizationPermission(userPermissions, permission)) {
            return true;
        }

        // 파일별 개별 권한 확인
        return hasFileSpecificPermission(fileAsset, userPermissions, permission);
    }

    private boolean hasOrganizationPermission(UserPermissions permissions,
                                             FilePermission required) {
        return permissions.getOrganizationPermissions()
            .contains(required.toOrganizationPermission());
    }

    private boolean hasFileSpecificPermission(FileAsset fileAsset,
                                             UserPermissions permissions,
                                             FilePermission required) {
        // 파일별 세부 권한 로직
        return false;
    }
}
```

#### 2. IAM Service Client
```java
@Component
@RequiredArgsConstructor
public class IamServiceClient {
    private final RestTemplate restTemplate;

    @Value("${iam.service.url}")
    private String iamServiceUrl;

    public UserPermissions getUserPermissions(Long userId,
                                             Long tenantId,
                                             Long organizationId) {
        String url = String.format(
            "%s/api/v1/permissions/user/%d?tenantId=%d&organizationId=%d",
            iamServiceUrl, userId, tenantId, organizationId
        );

        try {
            return restTemplate.getForObject(url, UserPermissions.class);
        } catch (RestClientException e) {
            log.error("IAM 권한 조회 실패: userId={}", userId, e);
            // 기본 권한 반환 또는 예외 처리
            return UserPermissions.empty();
        }
    }
}
```

#### 3. Security Interceptor
```java
@Component
@RequiredArgsConstructor
public class FileAccessInterceptor implements HandlerInterceptor {
    private final FilePermissionEvaluator permissionEvaluator;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {
        // 파일 관련 API인 경우만 체크
        if (!isFileApi(request.getRequestURI())) {
            return true;
        }

        // 사용자 정보 추출
        Long userId = extractUserId(request);
        String fileAssetId = extractFileAssetId(request);
        FilePermission required = determineRequiredPermission(request.getMethod());

        // 권한 체크
        if (!permissionEvaluator.hasPermission(userId, fileAssetId, required)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "파일 접근 권한이 없습니다");
            return false;
        }

        return true;
    }

    private FilePermission determineRequiredPermission(String method) {
        return switch (method) {
            case "GET" -> FilePermission.READ;
            case "PUT", "PATCH" -> FilePermission.WRITE;
            case "DELETE" -> FilePermission.DELETE;
            default -> FilePermission.READ;
        };
    }
}
```

### 주의사항
1. **캐싱** - 권한 정보는 적절히 캐싱
2. **Circuit Breaker** - IAM 서비스 장애 대응
3. **기본 권한** - IAM 조회 실패 시 안전한 기본값
4. **성능** - 권한 체크가 병목이 되지 않도록

---

## 🎯 KAN-290: S3 Download/Variant Adapter 구현

### 작업 내용
S3에서 파일 다운로드 및 이미지 변환(썸네일 등)을 처리하는 Adapter를 구현합니다.

### 구현 체크리스트

#### 1. S3 Download Adapter
```java
package com.ryuqq.fileflow.adapter.storage;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3DownloadAdapter implements FileDownloadPort {
    private final S3Client s3Client;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public PreSignedUrlResponse generateDownloadUrl(String bucketName,
                                                   String objectKey,
                                                   Duration expiration) {
        try {
            // Pre-signed URL 생성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest =
                s3Client.presigner().presignGetObject(presignRequest);

            return PreSignedUrlResponse.of(
                presignedRequest.url().toString(),
                Instant.now().plus(expiration)
            );

        } catch (S3Exception e) {
            log.error("S3 다운로드 URL 생성 실패: bucket={}, key={}",
                bucketName, objectKey, e);
            throw new FileDownloadException("다운로드 URL 생성 실패", e);
        }
    }

    @Override
    public byte[] downloadFile(String bucketName, String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            ResponseBytes<GetObjectResponse> responseBytes =
                s3Client.getObjectAsBytes(request);

            return responseBytes.asByteArray();

        } catch (S3Exception e) {
            log.error("S3 파일 다운로드 실패: bucket={}, key={}",
                bucketName, objectKey, e);
            throw new FileDownloadException("파일 다운로드 실패", e);
        }
    }
}
```

#### 2. Image Variant Adapter
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageVariantAdapter implements ImageVariantPort {
    private final S3Client s3Client;
    private final ImageProcessor imageProcessor;

    @Override
    public ImageVariant createThumbnail(String sourceBucket,
                                       String sourceKey,
                                       ThumbnailSpec spec) {
        try {
            // 1. 원본 이미지 다운로드
            byte[] originalImage = downloadImage(sourceBucket, sourceKey);

            // 2. 썸네일 생성
            byte[] thumbnail = imageProcessor.createThumbnail(
                originalImage,
                spec.getWidth(),
                spec.getHeight(),
                spec.getQuality()
            );

            // 3. 썸네일 S3 업로드
            String thumbnailKey = generateThumbnailKey(sourceKey, spec);
            uploadImage(sourceBucket, thumbnailKey, thumbnail);

            // 4. Variant 정보 반환
            return ImageVariant.of(
                thumbnailKey,
                spec.getWidth(),
                spec.getHeight(),
                thumbnail.length
            );

        } catch (Exception e) {
            log.error("썸네일 생성 실패: sourceKey={}", sourceKey, e);
            throw new ImageProcessingException("썸네일 생성 실패", e);
        }
    }

    @Override
    @Cacheable(value = "image-variants", key = "#sourceKey + '-' + #spec")
    public String getOrCreateVariant(String sourceBucket,
                                   String sourceKey,
                                   ImageVariantSpec spec) {
        // 캐시된 variant URL 반환 또는 새로 생성
        String variantKey = generateVariantKey(sourceKey, spec);

        if (existsInS3(sourceBucket, variantKey)) {
            return generateUrl(sourceBucket, variantKey);
        }

        // 새로 생성
        createVariant(sourceBucket, sourceKey, spec);
        return generateUrl(sourceBucket, variantKey);
    }

    private String generateThumbnailKey(String originalKey, ThumbnailSpec spec) {
        String baseName = FilenameUtils.getBaseName(originalKey);
        String extension = FilenameUtils.getExtension(originalKey);
        return String.format("thumbnails/%s_%dx%d.%s",
            baseName, spec.getWidth(), spec.getHeight(), extension);
    }
}
```

#### 3. Image Processor
```java
@Component
@Slf4j
public class ImageProcessor {

    public byte[] createThumbnail(byte[] original,
                                 int width,
                                 int height,
                                 int quality) {
        try {
            BufferedImage originalImage = ImageIO.read(
                new ByteArrayInputStream(original)
            );

            // 비율 유지하면서 리사이즈
            BufferedImage thumbnail = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC,
                width,
                height
            );

            // byte array로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("이미지 처리 실패", e);
            throw new ImageProcessingException("이미지 처리 실패", e);
        }
    }

    public ImageMetadata extractMetadata(byte[] image) {
        try {
            BufferedImage img = ImageIO.read(
                new ByteArrayInputStream(image)
            );

            return ImageMetadata.of(
                img.getWidth(),
                img.getHeight(),
                img.getType(),
                image.length
            );

        } catch (IOException e) {
            log.error("메타데이터 추출 실패", e);
            throw new ImageProcessingException("메타데이터 추출 실패", e);
        }
    }
}
```

### 주의사항
1. **캐싱** - 변환된 이미지는 캐싱
2. **비동기 처리** - 이미지 변환은 비동기로
3. **에러 처리** - 변환 실패 시 원본 반환
4. **성능** - 대용량 이미지 처리 시 메모리 관리

---

## 🎯 KAN-294: Phase 3A 통합 테스트 작성

### 작업 내용
Phase 3A에서 구현한 기능들의 통합 테스트를 작성합니다.

### 구현 체크리스트

#### 1. Controller 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class FileManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileAssetRepository repository;

    @Test
    @Sql("/test-data/file-assets.sql")
    void 파일_생성_및_조회_통합_테스트() throws Exception {
        // Given
        CreateFileRequest request = new CreateFileRequest();
        request.setUploadSessionId("upload-123");
        request.setFileName("test.pdf");
        request.setFileSize(1024L);
        request.setMimeType("application/pdf");

        // When - 파일 생성
        MvcResult createResult = mockMvc.perform(post("/api/v1/files")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Tenant-Id", "1")
                .header("X-Organization-Id", "1"))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        FileAssetResponse created = objectMapper.readValue(
            responseBody, FileAssetResponse.class
        );

        // Then - DB 검증
        FileAssetEntity entity = repository.findByFileAssetId(
            created.getFileAssetId()
        ).orElseThrow();

        assertThat(entity.getFileName()).isEqualTo("test.pdf");
        assertThat(entity.getFileSize()).isEqualTo(1024L);

        // When - 파일 조회
        mockMvc.perform(get("/api/v1/files/" + created.getFileAssetId())
                .header("X-Tenant-Id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("test.pdf"));
    }

    @Test
    void 파일_삭제_통합_테스트() throws Exception {
        // Given - 파일 생성
        FileAssetEntity entity = FileAssetEntity.create(
            "FILE-001", "test.pdf", 1024L
        );
        repository.save(entity);

        // When - 삭제
        mockMvc.perform(delete("/api/v1/files/FILE-001")
                .header("X-Tenant-Id", "1"))
            .andExpect(status().isNoContent());

        // Then - Soft Delete 확인
        FileAssetEntity deleted = repository.findByFileAssetId("FILE-001")
            .orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(FileStatus.DELETED);
        assertThat(deleted.getDeletedAt()).isNotNull();
    }
}
```

#### 2. Event Listener 통합 테스트
```java
@SpringBootTest
@DirtiesContext
class UploadCompletedEventIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private FileAssetRepository repository;

    @MockBean
    private UploadSessionQueryPort uploadSessionQueryPort;

    @Test
    void 업로드_완료_이벤트_처리_테스트() {
        // Given
        UploadSession mockSession = UploadSession.builder()
            .id(1L)
            .sessionId("upload-123")
            .fileName("test.pdf")
            .fileSize(1024L)
            .mimeType("application/pdf")
            .bucketName("test-bucket")
            .objectKey("files/test.pdf")
            .tenantId(1L)
            .organizationId(1L)
            .build();

        when(uploadSessionQueryPort.findById(1L))
            .thenReturn(Optional.of(mockSession));

        // When - 이벤트 발행
        UploadCompletedEvent event = UploadCompletedEvent.of(
            1L, "upload-123", Instant.now()
        );
        eventPublisher.publishEvent(event);

        // Then - FileAsset 생성 확인 (비동기 처리 대기)
        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<FileAssetEntity> files = repository.findAll();
                assertThat(files).hasSize(1);
                assertThat(files.get(0).getFileName()).isEqualTo("test.pdf");
            });
    }
}
```

#### 3. Permission 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
class FilePermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IamServiceClient iamClient;

    @Test
    void 권한_없는_사용자_접근_차단_테스트() throws Exception {
        // Given
        when(iamClient.getUserPermissions(2L, 1L, 1L))
            .thenReturn(UserPermissions.empty());  // 권한 없음

        // When & Then
        mockMvc.perform(get("/api/v1/files/FILE-001")
                .header("X-User-Id", "2")
                .header("X-Tenant-Id", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void 소유자_접근_허용_테스트() throws Exception {
        // Given
        UserPermissions ownerPermissions = UserPermissions.builder()
            .userId(1L)
            .isOwner(true)
            .build();

        when(iamClient.getUserPermissions(1L, 1L, 1L))
            .thenReturn(ownerPermissions);

        // When & Then
        mockMvc.perform(get("/api/v1/files/FILE-001")
                .header("X-User-Id", "1")
                .header("X-Tenant-Id", "1"))
            .andExpect(status().isOk());
    }
}
```

### 주의사항
1. **테스트 격리** - @DirtiesContext로 테스트 간 영향 차단
2. **비동기 처리** - Awaitility로 비동기 이벤트 대기
3. **Mock 최소화** - 가능한 실제 구현 사용
4. **데이터 준비** - @Sql로 테스트 데이터 준비

---

## 📝 체크리스트 총정리

### 개발 전
- [ ] Jira 태스크 "진행 중" 변경
- [ ] 관련 문서 및 스키마 확인
- [ ] 의존 모듈 확인

### 개발 중
- [ ] **NO Lombok** 준수
- [ ] **Law of Demeter** 준수
- [ ] **Long FK 전략** 준수
- [ ] **Transaction 경계** 준수
- [ ] Static Factory Method 사용
- [ ] Javadoc 작성

### 개발 후
- [ ] 단위 테스트 작성 및 통과
- [ ] 통합 테스트 작성 및 통과
- [ ] 코드 리뷰 요청
- [ ] Jira 태스크 "완료" 변경

## 다음 단계
Phase 3B 태스크는 `prd/KAN-260-phase-3b-tasks.md` 참조