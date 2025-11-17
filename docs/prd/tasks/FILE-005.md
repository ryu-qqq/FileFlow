# FILE-005: Integration Test 구현

**Epic**: File Management System
**Layer**: Integration Test (E2E)
**브랜치**: feature/FILE-005-integration-test
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 관리 시스템의 E2E 통합 테스트를 구현합니다. 실제 환경과 유사한 조건에서 전체 시스템 흐름을 검증하고, TestContainers를 통해 독립적인 테스트 환경을 보장합니다.

---

## 🎯 요구사항

### Test Infrastructure 구성

#### A. TestContainers 설정

- [ ] **MySQL Container**
  ```java
  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("fileflow_test")
      .withUsername("test")
      .withPassword("test");
  ```

- [ ] **Redis Container** (선택, 캐시 사용 시)
  ```java
  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);
  ```

- [ ] **LocalStack Container** (S3 Mock)
  ```java
  @Container
  static LocalStackContainer localstack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:latest")
  )
      .withServices(LocalStackContainer.Service.S3)
      .withEnv("AWS_DEFAULT_REGION", "ap-northeast-2");
  ```

#### B. Test Configuration

- [ ] **application-test.yml** 설정
  ```yaml
  spring:
    datasource:
      url: ${TESTCONTAINERS_MYSQL_URL}
      username: test
      password: test
    jpa:
      hibernate:
        ddl-auto: validate
    flyway:
      enabled: true
      locations: classpath:db/migration

  aws:
    s3:
      endpoint: ${LOCALSTACK_ENDPOINT}
      region: ap-northeast-2
      bucket: test-bucket
  ```

- [ ] **TestConfig** 클래스
  - LocalStack S3 Client Bean 설정
  - Test Bucket 자동 생성
  - Test 환경 Properties 로드

### E2E Test Scenarios

#### A. Presigned URL 직접 업로드 시나리오

- [ ] **PresignedUrlUploadE2ETest**
  - **시나리오 1: 100MB 미만 단일 업로드 성공**
    1. POST /api/v1/files/presigned-url (파일 정보 전달)
    2. 응답에서 presignedUrl 수신
    3. HTTP PUT presignedUrl (실제 S3 업로드 시뮬레이션)
    4. POST /api/v1/files/{fileId}/complete (업로드 완료 신호)
    5. GET /api/v1/files/{fileId} (파일 상태 COMPLETED 확인)
    6. DB 검증: files 테이블에 데이터 존재, status=COMPLETED
    7. S3 검증: Object 존재 확인

  - **시나리오 2: 100MB 이상 Multipart Upload 성공**
    1. POST /api/v1/files/presigned-url (fileSize >= 100MB)
    2. 응답에서 uploadId 수신 (Multipart Upload ID)
    3. Multipart Upload 시뮬레이션 (LocalStack)
    4. POST /api/v1/files/{fileId}/complete
    5. GET /api/v1/files/{fileId} (COMPLETED 확인)

  - **시나리오 3: 파일 크기 초과 실패 (1GB+)**
    1. POST /api/v1/files/presigned-url (fileSize > 1GB)
    2. 응답: 400 Bad Request, errorCode=FILE_SIZE_EXCEEDED

  - **시나리오 4: 업로드 완료 전 조회**
    1. POST /api/v1/files/presigned-url
    2. GET /api/v1/files/{fileId}
    3. 응답: status=PENDING (아직 업로드 안 함)

  - **시나리오 5: S3 업로드 없이 완료 시도**
    1. POST /api/v1/files/presigned-url
    2. POST /api/v1/files/{fileId}/complete (S3 업로드 안 함)
    3. 응답: 500 Internal Server Error, errorCode=UPLOAD_VERIFICATION_FAILED
    4. DB 검증: status=FAILED

#### B. 외부 URL 업로드 시나리오

- [ ] **ExternalUrlUploadE2ETest**
  - **시나리오 1: 외부 URL 다운로드 및 S3 업로드 성공**
    1. WireMock으로 외부 URL Mock (https://example.com/image.jpg)
    2. POST /api/v1/files/from-url (externalUrl 전달)
    3. 응답: 202 Accepted, fileId 수신
    4. SQS 메시지 발송 확인 (MessageOutbox 테이블)
    5. SQS Consumer 시뮬레이션 (백그라운드 작업 수동 트리거)
    6. GET /api/v1/files/{fileId} (status=COMPLETED 확인)
    7. S3 검증: Object 존재 확인

  - **시나리오 2: HTTP URL 거부 (HTTPS만 허용)**
    1. POST /api/v1/files/from-url (http://example.com/image.jpg)
    2. 응답: 400 Bad Request, errorCode=INVALID_URL

  - **시나리오 3: 외부 URL 다운로드 실패 (404)**
    1. WireMock으로 404 응답 설정
    2. POST /api/v1/files/from-url
    3. SQS Consumer 실행 → 3회 재시도 → 실패
    4. GET /api/v1/files/{fileId} (status=FAILED 확인)
    5. DB 검증: retryCount=3, errorMessage 존재

  - **시나리오 4: Webhook 전송 성공**
    1. WireMock으로 Webhook URL Mock
    2. POST /api/v1/files/from-url (webhookUrl 포함)
    3. SQS Consumer 실행 → 업로드 완료
    4. Webhook 전송 확인 (WireMock verify)
    5. Webhook Payload 검증 (fileId, status, s3Url 포함)
    6. HMAC 서명 검증

#### C. 파일 가공 파이프라인 시나리오

- [ ] **FileProcessingPipelineE2ETest**
  - **시나리오 1: 썸네일 생성 성공**
    1. 파일 업로드 완료 상태 (COMPLETED)
    2. POST /api/v1/files/{fileId}/process (jobTypes=[THUMBNAIL_GENERATION])
    3. 응답: 202 Accepted, jobId 수신
    4. SQS Consumer 시뮬레이션 (가공 작업 수동 트리거)
    5. GET /api/v1/files/{fileId}/jobs (status=COMPLETED, outputS3Key 존재)
    6. S3 검증: 썸네일 Object 존재 확인

  - **시나리오 2: 여러 가공 작업 동시 실행**
    1. POST /api/v1/files/{fileId}/process (jobTypes=[THUMBNAIL, IMAGE_RESIZE, OCR])
    2. 응답: 3개 jobId 수신
    3. GET /api/v1/files/{fileId}/jobs (3개 Job 모두 PENDING)
    4. SQS Consumer 실행 → 3개 Job 모두 COMPLETED
    5. GET /api/v1/files/{fileId} (FileDetailResponse에 3개 Job 포함)

  - **시나리오 3: 가공 실패 후 재시도**
    1. POST /api/v1/files/{fileId}/process
    2. 첫 번째 시도 실패 (errorMessage 기록)
    3. retryCount=1 확인
    4. 두 번째 시도 성공
    5. GET /api/v1/jobs/{jobId} (status=COMPLETED, retryCount=1)

  - **시나리오 4: 최대 재시도 초과 (2회)**
    1. 가공 작업 시뮬레이션 (2회 실패)
    2. GET /api/v1/jobs/{jobId} (status=FAILED, retryCount=2)

#### D. 파일 조회 시나리오

- [ ] **FileQueryE2ETest**
  - **시나리오 1: Cursor Pagination 정상 동작**
    1. 파일 30개 생성 (uploaderId=1)
    2. GET /api/v1/files?uploaderId=1&size=10
    3. 응답: 10개 파일, hasNext=true, nextCursor 존재
    4. GET /api/v1/files?uploaderId=1&size=10&cursor={nextCursor}
    5. 응답: 다음 10개 파일, hasNext=true
    6. GET /api/v1/files?uploaderId=1&size=10&cursor={nextCursor}
    7. 응답: 마지막 10개 파일, hasNext=false

  - **시나리오 2: 상태별 필터링 (status=COMPLETED)**
    1. 파일 10개 생성 (5개 COMPLETED, 5개 PENDING)
    2. GET /api/v1/files?uploaderId=1&status=COMPLETED
    3. 응답: 5개 파일만 반환

  - **시나리오 3: 카테고리별 필터링 (category=상품)**
    1. 파일 10개 생성 (5개 "상품", 5개 "전시영역")
    2. GET /api/v1/files?uploaderId=1&category=상품
    3. 응답: 5개 파일만 반환

  - **시나리오 4: 파일 상세 조회 (Job 정보 포함)**
    1. 파일 1개 업로드 + 가공 작업 2개 완료
    2. GET /api/v1/files/{fileId}
    3. 응답: FileDetailResponse (jobs 필드에 2개 Job 포함)

#### E. Outbox Pattern 검증 시나리오

- [ ] **OutboxPatternE2ETest**
  - **시나리오 1: After-Commit Listener 동작 확인**
    1. POST /api/v1/files/from-url
    2. DB 검증: message_outbox 테이블에 PENDING 레코드 생성
    3. After-Commit Listener 실행 (자동)
    4. DB 검증: message_outbox 상태 SENT로 변경
    5. SQS 메시지 전송 확인 (LocalStack SQS)

  - **시나리오 2: Fallback Scheduler 동작 확인**
    1. MessageOutbox 레코드 직접 생성 (PENDING, createdAt < 1분 전)
    2. Fallback Scheduler 수동 트리거 (@Scheduled 메서드 직접 호출)
    3. DB 검증: message_outbox 상태 SENT로 변경
    4. SQS 메시지 전송 확인

  - **시나리오 3: 재시도 실패 시 FAILED 상태 전환**
    1. MessageOutbox 생성 (PENDING, retryCount=2)
    2. SQS 전송 실패 시뮬레이션
    3. Fallback Scheduler 실행
    4. DB 검증: status=FAILED, retryCount=3

### Test Fixtures

- [ ] **FileTestFixture**
  - `aFile()`: 기본 File 객체 생성
  - `aCompletedFile()`: COMPLETED 상태 File 생성
  - `aFileWithCategory(String category)`: 카테고리 지정 File 생성
  - `createFiles(int count, Long uploaderId)`: 여러 File 생성

- [ ] **GeneratePresignedUrlRequestFixture**
  - `aRequest()`: 기본 Request 생성
  - `aLargeFileRequest()`: 100MB 이상 Request
  - `anInvalidMimeTypeRequest()`: 잘못된 MIME 타입

- [ ] **UploadFromExternalUrlRequestFixture**
  - `aRequest()`: 기본 Request
  - `aHttpUrlRequest()`: HTTP URL (실패 케이스)
  - `aRequestWithWebhook()`: Webhook URL 포함

### WireMock 설정

- [ ] **외부 URL Mock 서버**
  ```java
  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  @BeforeEach
  void setupExternalUrlMock() {
      wireMock.stubFor(get(urlEqualTo("/image.jpg"))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "image/jpeg")
              .withBodyFile("test-image.jpg")));
  }
  ```

- [ ] **Webhook Mock 서버**
  ```java
  wireMock.stubFor(post(urlEqualTo("/webhook"))
      .willReturn(aResponse()
          .withStatus(200)));

  // Webhook 전송 검증
  wireMock.verify(postRequestedFor(urlEqualTo("/webhook"))
      .withHeader("X-Signature", matching(".*"))
      .withRequestBody(matchingJsonPath("$.fileId", equalTo(fileId))));
  ```

### Performance Test

- [ ] **대용량 파일 업로드 성능 테스트**
  - 100MB 파일 Multipart Upload 시간 측정 (< 30초 목표)
  - 1GB 파일 Multipart Upload 시간 측정 (< 5분 목표)

- [ ] **동시 업로드 성능 테스트**
  - 10개 파일 동시 업로드 (Thread Pool)
  - 모든 파일 COMPLETED 상태 확인
  - 평균 응답 시간 < 3초 목표

- [ ] **Cursor Pagination 성능 테스트**
  - 10,000개 파일 생성
  - Cursor Pagination 조회 (100 페이지)
  - 각 페이지 조회 시간 < 100ms 목표

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **TestRestTemplate 필수**
  - MockMvc 금지
  - 실제 HTTP 요청으로 E2E 검증
  - @SpringBootTest(webEnvironment = RANDOM_PORT)

- [ ] **TestContainers 필수**
  - 실제 MySQL 사용 (H2 금지)
  - Flyway 마이그레이션 자동 실행
  - 독립적인 테스트 환경 보장

- [ ] **Flyway vs @Sql 분리**
  - 스키마 생성: Flyway (src/main/resources/db/migration)
  - 테스트 데이터: @Sql (src/test/resources/sql)
  - @Sql로 스키마 생성 금지

- [ ] **Transaction Isolation**
  - 각 테스트는 독립적으로 실행
  - @Transactional 사용 금지 (실제 환경과 동일하게)
  - 테스트 후 데이터 정리 (@AfterEach)

### 테스트 규칙

- [ ] **Given-When-Then 패턴**
  ```java
  @Test
  void presignedUrl_발급_성공() {
      // Given
      GeneratePresignedUrlRequest request = ...;

      // When
      ResponseEntity<ApiResponse<PresignedUrlResponse>> response =
          restTemplate.postForEntity(...);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().success()).isTrue();
  }
  ```

- [ ] **테스트 네이밍 규칙**
  - `{메서드명}_{시나리오}_{예상결과}` 형식
  - 예: `generatePresignedUrl_파일크기초과_실패()`

- [ ] **Assertion 명확성**
  - AssertJ 사용 권장
  - 한 테스트에 하나의 시나리오만 검증
  - 실패 시 원인 파악 가능한 메시지

---

## ✅ 완료 조건

- [ ] TestContainers 설정 완료 (MySQL, LocalStack S3)
- [ ] 5개 E2E Test 클래스 구현 완료
  - PresignedUrlUploadE2ETest (5개 시나리오)
  - ExternalUrlUploadE2ETest (4개 시나리오)
  - FileProcessingPipelineE2ETest (4개 시나리오)
  - FileQueryE2ETest (4개 시나리오)
  - OutboxPatternE2ETest (3개 시나리오)
- [ ] 3개 Test Fixture 구현 완료
- [ ] WireMock 설정 완료 (외부 URL, Webhook)
- [ ] Performance Test 3개 완료
- [ ] 모든 E2E 테스트 통과 (총 20+ 시나리오)
- [ ] 테스트 커버리지 > 80%
- [ ] Zero-Tolerance 규칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/file-management-system.md
- **Plan**: docs/prd/plans/FILE-005-integration-test-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **컨벤션**: docs/coding_convention/05-testing/integration-testing/

---

## 📝 참고사항

### TestContainers 설정 예시
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class PresignedUrlUploadE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("fileflow_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest")
    )
        .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("aws.s3.endpoint", localstack::getEndpoint);
    }

    @BeforeEach
    void setup() {
        // S3 Bucket 생성
        S3Client s3Client = S3Client.builder()
            .endpointOverride(localstack.getEndpoint())
            .build();
        s3Client.createBucket(b -> b.bucket("test-bucket"));
    }

    @AfterEach
    void cleanup() {
        // 테스트 데이터 정리
        jdbcTemplate.execute("DELETE FROM files");
        jdbcTemplate.execute("DELETE FROM message_outbox");
    }
}
```

### E2E Test 예시 (Presigned URL 업로드)
```java
@Test
void presignedUrl_단일업로드_성공() {
    // Given
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
        "example.jpg",
        1024L,
        "image/jpeg",
        1L,
        "상품",
        List.of("이미지")
    );

    // When - 1. Presigned URL 발급
    ResponseEntity<ApiResponse<PresignedUrlResponse>> presignedResponse =
        restTemplate.postForEntity(
            "/api/v1/files/presigned-url",
            request,
            new ParameterizedTypeReference<>() {}
        );

    // Then - 1. URL 발급 성공
    assertThat(presignedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    String fileId = presignedResponse.getBody().data().fileId();
    String presignedUrl = presignedResponse.getBody().data().presignedUrl();

    // When - 2. S3 업로드 시뮬레이션
    byte[] fileContent = "test content".getBytes();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    HttpEntity<byte[]> uploadEntity = new HttpEntity<>(fileContent, headers);

    restTemplate.put(presignedUrl, uploadEntity);

    // When - 3. 업로드 완료 신호
    ResponseEntity<ApiResponse<FileResponse>> completeResponse =
        restTemplate.postForEntity(
            "/api/v1/files/" + fileId + "/complete",
            null,
            new ParameterizedTypeReference<>() {}
        );

    // Then - 3. 업로드 완료 성공
    assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(completeResponse.getBody().data().status())
        .isEqualTo(FileStatus.COMPLETED);

    // When - 4. 파일 조회
    ResponseEntity<ApiResponse<FileDetailResponse>> getResponse =
        restTemplate.exchange(
            "/api/v1/files/" + fileId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

    // Then - 4. 파일 상태 COMPLETED
    assertThat(getResponse.getBody().data().status())
        .isEqualTo(FileStatus.COMPLETED);
    assertThat(getResponse.getBody().data().s3Key())
        .isNotBlank();

    // Then - 5. DB 검증
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM files WHERE file_id = ? AND status = 'COMPLETED'",
        Integer.class,
        fileId
    );
    assertThat(count).isEqualTo(1);

    // Then - 6. S3 검증
    S3Client s3Client = S3Client.builder()
        .endpointOverride(localstack.getEndpoint())
        .build();

    HeadObjectResponse headResponse = s3Client.headObject(b -> b
        .bucket("test-bucket")
        .key(fileId + ".jpg")
    );
    assertThat(headResponse.contentLength()).isEqualTo(fileContent.length);
}
```

### WireMock 사용 예시 (외부 URL 다운로드)
```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

@Test
void 외부URL_다운로드_성공() {
    // Given - WireMock 설정
    byte[] imageContent = loadImageFromResource("test-image.jpg");
    wireMock.stubFor(get(urlEqualTo("/image.jpg"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "image/jpeg")
            .withBody(imageContent)));

    String externalUrl = wireMock.baseUrl() + "/image.jpg";
    UploadFromExternalUrlRequest request = new UploadFromExternalUrlRequest(
        externalUrl,
        1L,
        "상품",
        null,
        null
    );

    // When - 외부 URL 업로드 요청
    ResponseEntity<ApiResponse<FileResponse>> response =
        restTemplate.postForEntity(
            "/api/v1/files/from-url",
            request,
            new ParameterizedTypeReference<>() {}
        );

    // Then - 비동기 작업 등록 성공
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    String fileId = response.getBody().data().fileId();

    // When - SQS Consumer 수동 트리거
    triggerSqsConsumer();

    // Then - 파일 상태 COMPLETED
    ResponseEntity<ApiResponse<FileDetailResponse>> getResponse =
        restTemplate.exchange(
            "/api/v1/files/" + fileId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

    assertThat(getResponse.getBody().data().status())
        .isEqualTo(FileStatus.COMPLETED);

    // Then - WireMock 호출 검증
    wireMock.verify(getRequestedFor(urlEqualTo("/image.jpg")));

    // Then - S3 업로드 검증
    S3Client s3Client = S3Client.builder()
        .endpointOverride(localstack.getEndpoint())
        .build();

    HeadObjectResponse headResponse = s3Client.headObject(b -> b
        .bucket("test-bucket")
        .key(fileId + ".jpg")
    );
    assertThat(headResponse.contentLength()).isEqualTo(imageContent.length);
}
```

### Outbox Pattern 검증 예시
```java
@Test
void afterCommitListener_정상동작() {
    // Given
    UploadFromExternalUrlRequest request = ...;

    // When - 외부 URL 업로드 요청
    ResponseEntity<ApiResponse<FileResponse>> response =
        restTemplate.postForEntity("/api/v1/files/from-url", request, ...);

    String fileId = response.getBody().data().fileId();

    // Then - MessageOutbox PENDING 레코드 생성
    Integer pendingCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM message_outbox WHERE aggregate_id = ? AND status = 'PENDING'",
        Integer.class,
        fileId
    );
    assertThat(pendingCount).isEqualTo(1);

    // When - After-Commit Listener 실행 대기 (최대 5초)
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
        // Then - MessageOutbox SENT 상태 변경
        Integer sentCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM message_outbox WHERE aggregate_id = ? AND status = 'SENT'",
            Integer.class,
            fileId
        );
        assertThat(sentCount).isEqualTo(1);
    });

    // Then - SQS 메시지 전송 확인 (LocalStack)
    // SQS 메시지 조회 로직...
}
```

### Cursor Pagination 검증 예시
```java
@Test
void cursorPagination_정상동작() {
    // Given - 30개 파일 생성
    Long uploaderId = 1L;
    for (int i = 0; i < 30; i++) {
        createFile("file-" + i + ".jpg", uploaderId);
    }

    // When - 첫 페이지 조회 (size=10)
    ResponseEntity<ApiResponse<CursorPageResponse<FileSummaryResponse>>> page1 =
        restTemplate.exchange(
            "/api/v1/files?uploaderId={uploaderId}&size=10",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {},
            uploaderId
        );

    // Then - 첫 페이지 검증
    CursorPageResponse<FileSummaryResponse> page1Data = page1.getBody().data();
    assertThat(page1Data.content()).hasSize(10);
    assertThat(page1Data.hasNext()).isTrue();
    assertThat(page1Data.nextCursor()).isNotNull();

    // When - 두 번째 페이지 조회
    LocalDateTime cursor = page1Data.nextCursor();
    ResponseEntity<ApiResponse<CursorPageResponse<FileSummaryResponse>>> page2 =
        restTemplate.exchange(
            "/api/v1/files?uploaderId={uploaderId}&size=10&cursor={cursor}",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {},
            uploaderId,
            cursor
        );

    // Then - 두 번째 페이지 검증
    CursorPageResponse<FileSummaryResponse> page2Data = page2.getBody().data();
    assertThat(page2Data.content()).hasSize(10);
    assertThat(page2Data.hasNext()).isTrue();

    // When - 세 번째 페이지 조회
    cursor = page2Data.nextCursor();
    ResponseEntity<ApiResponse<CursorPageResponse<FileSummaryResponse>>> page3 =
        restTemplate.exchange(
            "/api/v1/files?uploaderId={uploaderId}&size=10&cursor={cursor}",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {},
            uploaderId,
            cursor
        );

    // Then - 마지막 페이지 검증
    CursorPageResponse<FileSummaryResponse> page3Data = page3.getBody().data();
    assertThat(page3Data.content()).hasSize(10);
    assertThat(page3Data.hasNext()).isFalse();
    assertThat(page3Data.nextCursor()).isNull();
}
```
