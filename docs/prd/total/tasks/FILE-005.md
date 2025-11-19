# FILE-005: Integration Test 구현

**Epic**: File Management System (파일 관리 시스템)
**Layer**: Integration Test (E2E)
**브랜치**: feature/FILE-005-integration
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 업로드 전체 플로우를 E2E 시나리오로 검증합니다.
- Presigned URL 발급 → S3 업로드 → 완료 처리
- 세션 만료 케이스
- 멱등성 검증

---

## 🎯 요구사항

### A. E2E 시나리오 (3개)

#### 시나리오 1: 단일 파일 업로드 (< 1GB)
```
1. POST /api/v1/files/presigned-url
   - sessionId 생성 (UUID v7)
   - fileSize < 1GB
   → 201 Created, uploadType=SINGLE

2. 클라이언트: S3 PUT (실제 파일 업로드)
   - presignedUrl로 업로드
   → 200 OK

3. POST /api/v1/files/upload-complete
   - sessionId 전달
   → 200 OK, status=COMPLETED

4. DB 확인
   - UploadSession 생성 확인 (COMPLETED)
   - File 생성 확인 (COMPLETED)
```

**검증 항목**:
- [ ] UploadSession 생성 확인
- [ ] File 생성 확인 (PENDING → COMPLETED)
- [ ] S3 Object 존재 확인 (LocalStack)
- [ ] S3Key 경로 검증 (Admin/Seller/Customer)
- [ ] FileCategory 검증 (서브카테고리)

#### 시나리오 2: 멱등성 검증
```
1. POST /api/v1/files/presigned-url
   - sessionId: "abc-123"
   → 201 Created

2. POST /api/v1/files/presigned-url (동일 sessionId)
   - sessionId: "abc-123"
   → 200 OK, 기존 URL 반환 (중복 생성 없음)

3. DB 확인
   - UploadSession 1개만 존재
```

**검증 항목**:
- [ ] 동일 sessionId 중복 방지
- [ ] 기존 세션 상태 확인 (INITIATED)
- [ ] DB에 1개만 존재

#### 시나리오 3: 세션 만료 처리
```
1. POST /api/v1/files/presigned-url
   → 201 Created, expiresAt (5분 후)

2. 시간 경과 (5분 초과 시뮬레이션)
   - Clock.fixed() 사용

3. POST /api/v1/files/upload-complete (만료된 세션)
   → 410 Gone, SessionExpiredException
```

**검증 항목**:
- [ ] 세션 만료 자동 처리
- [ ] 만료된 세션 업로드 차단
- [ ] HTTP 410 Gone 반환

---

### B. 에러 케이스 검증 (5개)

#### 1. 파일 크기 초과
- [ ] fileSize > 1GB → 400 Bad Request

#### 2. 잘못된 MIME 타입
- [ ] mimeType: "text/plain" (허용 목록 외) → 400 Bad Request

#### 3. 존재하지 않는 세션 조회
- [ ] sessionId: "invalid-id" → 404 Not Found

#### 4. 중복 완료 요청
- [ ] 이미 완료된 세션에 재요청 → 409 Conflict

#### 5. 잘못된 카테고리
- [ ] Customer에 "product" 카테고리 요청 → 400 Bad Request

---

### C. 테스트 환경 구성

#### TestContainers
- [ ] **MySQL**: TestContainers MySQL 8.0
- [ ] **LocalStack** (S3 Mock): TestContainers LocalStack

#### Flyway Migration
- [ ] **Flyway 사용**: V1-V2 마이그레이션 자동 실행
- [ ] **@Sql 금지**: 테스트 데이터 준비는 `@BeforeEach`에서 생성

#### TestRestTemplate
- [ ] **MockMvc 금지**: TestRestTemplate 사용
- [ ] **실제 HTTP 요청/응답**
- [ ] **전체 Spring Boot 컨텍스트 로딩**

---

### D. 테스트 데이터 준비

#### TestFixture
- [ ] `GeneratePresignedUrlRequestFixture`
  - 단일 업로드용 (fileSize < 1GB)
  - Admin/Seller/Customer 별 카테고리

- [ ] `CompleteUploadRequestFixture`
  - sessionId 포함

- [ ] `UserContextFixture`
  - Admin: tenantId=1, uploaderType=ADMIN, uploaderSlug="connectly"
  - Seller: tenantId=1, uploaderType=SELLER, uploaderSlug="samsung-electronics"
  - Customer: tenantId=1, uploaderType=CUSTOMER, uploaderSlug="default"

---

### E. Integration Test 클래스 구조

#### FileUploadIntegrationTest
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class FileUploadIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest")
    );

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UploadSessionJpaRepository uploadSessionRepository;

    @Autowired
    private FileJpaRepository fileRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        uploadSessionRepository.deleteAll();
        fileRepository.deleteAll();
    }

    @Test
    void 단일_파일_업로드_E2E_시나리오() {
        // Given
        var request = GeneratePresignedUrlRequestFixture.create();

        // When: Presigned URL 발급
        var presignedUrlResponse = restTemplate.postForEntity(
            "/api/v1/files/presigned-url",
            request,
            PresignedUrlResponse.class
        );

        // Then
        assertThat(presignedUrlResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(presignedUrlResponse.getBody().uploadType()).isEqualTo("SINGLE");

        // When: S3 업로드 (LocalStack)
        // ... S3 PUT 시뮬레이션

        // When: 업로드 완료
        var completeRequest = new CompleteUploadRequest(
            presignedUrlResponse.getBody().sessionId()
        );
        var fileResponse = restTemplate.postForEntity(
            "/api/v1/files/upload-complete",
            completeRequest,
            FileResponse.class
        );

        // Then
        assertThat(fileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fileResponse.getBody().status()).isEqualTo("COMPLETED");

        // Then: DB 검증
        var session = uploadSessionRepository.findBySessionId(
            presignedUrlResponse.getBody().sessionId()
        );
        assertThat(session).isPresent();
        assertThat(session.get().getStatus()).isEqualTo("COMPLETED");

        var file = fileRepository.findByFileId(fileResponse.getBody().fileId());
        assertThat(file).isPresent();
        assertThat(file.get().getStatus()).isEqualTo("COMPLETED");
    }
}
```

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **MockMvc 금지**: TestRestTemplate 필수
- [ ] **@Sql 금지**: Flyway Migration 사용
- [ ] **@Mock 금지**: 실제 컴포넌트 사용 (Integration Test)

### Integration Test 규칙
- [ ] **@SpringBootTest(webEnvironment = RANDOM_PORT)**
- [ ] **@AutoConfigureTestRestTemplate**
- [ ] **@Testcontainers**: MySQL, LocalStack
- [ ] **전체 플로우 검증**: API → UseCase → Repository → DB

### 테스트 규칙
- [ ] **Given-When-Then 패턴**:
```java
// Given: 테스트 데이터 준비
var request = GeneratePresignedUrlRequestFixture.create();

// When: API 호출
var response = restTemplate.postForEntity(
    "/api/v1/files/presigned-url",
    request,
    PresignedUrlResponse.class
);

// Then: 검증
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
assertThat(response.getBody().uploadType()).isEqualTo("SINGLE");
```

- [ ] **테스트 격리**: 각 테스트마다 DB 초기화
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] 3개 E2E 시나리오 테스트 통과
- [ ] 5개 에러 케이스 테스트 통과
- [ ] TestContainers 구성 완료 (MySQL, LocalStack)
- [ ] Flyway Migration 테스트 통과
- [ ] TestRestTemplate 테스트 통과
- [ ] TestFixture 구현 완료
- [ ] 테스트 격리 확인 (DB 초기화)
- [ ] 테스트 커버리지 > 80%
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mvp/file-upload-mvp.md
- **Domain Layer**: docs/prd/tasks/FILE-001.md
- **Application Layer**: docs/prd/tasks/FILE-002.md
- **Persistence Layer**: docs/prd/tasks/FILE-003.md
- **REST API Layer**: docs/prd/tasks/FILE-004.md
- **Plan**: docs/prd/plans/FILE-005-integration-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: docs/coding_convention/05-testing/integration-testing/01_integration-testing-overview.md

---

## 📚 참고 규칙

- `docs/coding_convention/05-testing/integration-testing/` (Integration Test 가이드)
- `docs/coding_convention/05-testing/test-fixtures/` (TestFixture 가이드)
- `docs/coding_convention/04-persistence-layer/mysql/config/flyway-testing.md` (Flyway Test 가이드)
