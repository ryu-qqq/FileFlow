# FILE-005 TDD Plan

**Task**: Integration Test 구현 (E2E)
**Layer**: Integration Test
**브랜치**: feature/FILE-005-integration-test
**예상 소요 시간**: 1,560분 (104 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: Test Infrastructure 구성 (12 사이클)

#### 1️⃣ TestContainers MySQL 설정 (Cycle 1)

##### 🔴 Red: 테스트 작성
- [ ] `BaseIntegrationTest.java` 파일 생성
- [ ] MySQL Container 설정 코드 작성
- [ ] @DynamicPropertySource로 DataSource 설정
- [ ] 테스트 실행 → Container 실행 확인
- [ ] 커밋: `test: TestContainers MySQL 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] TestContainers dependency 추가 (build.gradle)
- [ ] MySQL 8.0 Container 설정
- [ ] JdbcUrl, Username, Password 동적 설정
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TestContainers MySQL 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Container 재사용 최적화 (@Container static)
- [ ] Connection Pool 설정 검증
- [ ] Integration Test ArchUnit 테스트 추가
- [ ] 커밋: `refactor: TestContainers MySQL 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] `BaseIntegrationTest` 추상 클래스로 분리
- [ ] 모든 E2E Test가 상속받도록 구조화
- [ ] 커밋: `test: BaseIntegrationTest 정리 (Tidy)`

---

#### 2️⃣ TestContainers LocalStack S3 설정 (Cycle 2)

##### 🔴 Red: 테스트 작성
- [ ] `S3IntegrationTest.java` 생성
- [ ] LocalStack Container 설정 테스트
- [ ] S3 Bucket 생성 테스트
- [ ] 커밋: `test: LocalStack S3 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] LocalStack Container 추가 (S3 Service)
- [ ] S3Client Bean 설정 (TestConfig)
- [ ] Bucket 자동 생성 로직
- [ ] 테스트 통과 확인
- [ ] 커밋: `impl: LocalStack S3 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] S3Client Builder 설정 개선
- [ ] Region, Endpoint 설정 검증
- [ ] 커밋: `refactor: LocalStack S3 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] S3TestConfig 별도 클래스로 분리
- [ ] BaseIntegrationTest에 S3Client 주입
- [ ] 커밋: `test: S3TestConfig 정리 (Tidy)`

---

#### 3️⃣ TestContainers Redis 설정 (Cycle 3, 선택)

##### 🔴 Red: 테스트 작성
- [ ] `RedisIntegrationTest.java` 생성
- [ ] Redis Container 설정 테스트
- [ ] 커밋: `test: Redis Container 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] Redis Container 추가 (redis:7-alpine)
- [ ] RedisTemplate Bean 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: Redis Container 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Redis 설정 최적화
- [ ] 커밋: `refactor: Redis Container 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] RedisTestConfig 분리
- [ ] 커밋: `test: RedisTestConfig 정리 (Tidy)`

---

#### 4️⃣ application-test.yml 설정 (Cycle 4)

##### 🔴 Red: 테스트 작성
- [ ] `ApplicationTestConfigTest.java` 생성
- [ ] application-test.yml 프로퍼티 로드 테스트
- [ ] 커밋: `test: application-test.yml 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `application-test.yml` 파일 생성
- [ ] DataSource, JPA, Flyway 설정
- [ ] AWS S3 설정 (LocalStack 연동)
- [ ] 테스트 통과
- [ ] 커밋: `impl: application-test.yml 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 환경 변수 설정 최적화
- [ ] Profile 분리 검증
- [ ] 커밋: `refactor: application-test.yml 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] Test Profile 설정 문서화
- [ ] 커밋: `test: Test Profile 문서 정리 (Tidy)`

---

#### 5️⃣ TestConfig 클래스 구현 (Cycle 5)

##### 🔴 Red: 테스트 작성
- [ ] `TestConfigTest.java` 생성
- [ ] S3Client Bean 생성 테스트
- [ ] Bucket 자동 생성 검증
- [ ] 커밋: `test: TestConfig 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `TestConfig.java` 생성
- [ ] @TestConfiguration 설정
- [ ] S3Client Bean 정의
- [ ] @PostConstruct로 Bucket 생성
- [ ] 커밋: `impl: TestConfig 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Bean Lifecycle 최적화
- [ ] 커밋: `refactor: TestConfig 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] TestConfig 재사용 구조화
- [ ] 커밋: `test: TestConfig 정리 (Tidy)`

---

#### 6️⃣ WireMock 외부 URL Mock 설정 (Cycle 6)

##### 🔴 Red: 테스트 작성
- [ ] `WireMockSetupTest.java` 생성
- [ ] WireMock 서버 실행 테스트
- [ ] GET /image.jpg Mock 응답 테스트
- [ ] 커밋: `test: WireMock 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] WireMock dependency 추가
- [ ] @RegisterExtension으로 WireMock 설정
- [ ] 외부 URL Stub 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: WireMock 설정 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] WireMock 설정 재사용 구조화
- [ ] 커밋: `refactor: WireMock 설정 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] WireMockTestSupport 추상 클래스 생성
- [ ] 커밋: `test: WireMockTestSupport 정리 (Tidy)`

---

#### 7️⃣ WireMock Webhook Mock 설정 (Cycle 7)

##### 🔴 Red: 테스트 작성
- [ ] Webhook POST 요청 Mock 테스트
- [ ] Webhook 전송 검증 테스트 (wireMock.verify)
- [ ] 커밋: `test: Webhook Mock 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] POST /webhook Stub 설정
- [ ] 200 OK 응답 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: Webhook Mock 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Webhook Signature 검증 로직 추가
- [ ] 커밋: `refactor: Webhook Mock 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] Webhook Stub 메서드 분리
- [ ] 커밋: `test: Webhook Stub 정리 (Tidy)`

---

#### 8️⃣ TestRestTemplate 설정 (Cycle 8)

##### 🔴 Red: 테스트 작성
- [ ] `RestTemplateSetupTest.java` 생성
- [ ] TestRestTemplate Bean 주입 테스트
- [ ] HTTP GET 요청 테스트
- [ ] 커밋: `test: TestRestTemplate 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] @SpringBootTest(webEnvironment = RANDOM_PORT)
- [ ] TestRestTemplate 자동 주입
- [ ] 기본 GET 요청 테스트 통과
- [ ] 커밋: `impl: TestRestTemplate 설정 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] TestRestTemplate 설정 최적화
- [ ] ArchUnit: MockMvc 금지 규칙 추가
- [ ] 커밋: `refactor: TestRestTemplate 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] BaseIntegrationTest에 TestRestTemplate 주입
- [ ] 커밋: `test: TestRestTemplate 정리 (Tidy)`

---

#### 9️⃣ Flyway 마이그레이션 설정 (Cycle 9)

##### 🔴 Red: 테스트 작성
- [ ] Flyway 마이그레이션 실행 테스트
- [ ] files 테이블 생성 검증
- [ ] 커밋: `test: Flyway 마이그레이션 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] application-test.yml에 Flyway 설정
- [ ] db/migration 디렉토리 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: Flyway 설정 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Flyway vs @Sql 분리 검증
- [ ] ArchUnit: @Sql로 스키마 생성 금지 규칙
- [ ] 커밋: `refactor: Flyway 설정 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] Flyway 설정 문서화
- [ ] 커밋: `test: Flyway 문서 정리 (Tidy)`

---

#### 🔟 Transaction Isolation 설정 (Cycle 10)

##### 🔴 Red: 테스트 작성
- [ ] @Transactional 금지 검증 테스트
- [ ] 테스트 독립성 검증
- [ ] 커밋: `test: Transaction Isolation 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] @AfterEach로 데이터 정리 로직 추가
- [ ] JdbcTemplate DELETE 쿼리
- [ ] 테스트 통과
- [ ] 커밋: `impl: Transaction Isolation 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] ArchUnit: Integration Test에서 @Transactional 금지
- [ ] 커밋: `refactor: Transaction Isolation 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] BaseIntegrationTest에 cleanup 메서드 추가
- [ ] 커밋: `test: cleanup 메서드 정리 (Tidy)`

---

#### 1️⃣1️⃣ SQS Consumer 수동 트리거 설정 (Cycle 11)

##### 🔴 Red: 테스트 작성
- [ ] SQS Consumer 수동 실행 테스트
- [ ] MessageOutbox → SQS 전송 검증
- [ ] 커밋: `test: SQS Consumer 트리거 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `triggerSqsConsumer()` 메서드 작성
- [ ] @Autowired로 SQS Consumer Bean 주입
- [ ] 수동 실행 로직
- [ ] 커밋: `impl: SQS Consumer 트리거 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] SQS Consumer 설정 최적화
- [ ] 커밋: `refactor: SQS Consumer 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] SQS Consumer Helper 메서드 분리
- [ ] 커밋: `test: SQS Helper 정리 (Tidy)`

---

#### 1️⃣2️⃣ Awaitility 설정 (비동기 검증) (Cycle 12)

##### 🔴 Red: 테스트 작성
- [ ] Awaitility를 사용한 비동기 검증 테스트
- [ ] await().atMost(5, SECONDS) 테스트
- [ ] 커밋: `test: Awaitility 설정 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] Awaitility dependency 추가
- [ ] await() 로직 작성
- [ ] 테스트 통과
- [ ] 커밋: `impl: Awaitility 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Timeout 설정 최적화
- [ ] 커밋: `refactor: Awaitility 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] Awaitility Helper 메서드 생성
- [ ] 커밋: `test: Awaitility Helper 정리 (Tidy)`

---

### Phase 2: Test Fixtures 구현 (12 사이클)

#### 1️⃣3️⃣ FileTestFixture - aFile() (Cycle 13)

##### 🔴 Red: 테스트 작성
- [ ] `FileTestFixtureTest.java` 생성
- [ ] `aFile()` 메서드 호출 테스트
- [ ] 커밋: `test: FileTestFixture aFile() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `FileTestFixture.java` 생성
- [ ] `aFile()` 메서드 구현 (기본 File 객체)
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileTestFixture aFile() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Builder 패턴 적용 검토
- [ ] 커밋: `refactor: FileTestFixture 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] Object Mother 패턴 적용
- [ ] 커밋: `test: FileTestFixture 정리 (Tidy)`

---

#### 1️⃣4️⃣ FileTestFixture - aCompletedFile() (Cycle 14)

##### 🔴 Red: 테스트 작성
- [ ] `aCompletedFile()` 호출 테스트
- [ ] status=COMPLETED 검증
- [ ] 커밋: `test: aCompletedFile() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `aCompletedFile()` 메서드 작성
- [ ] FileStatus.COMPLETED 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: aCompletedFile() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] aFile()을 base로 재사용
- [ ] 커밋: `refactor: aCompletedFile() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화 주석 추가
- [ ] 커밋: `test: aCompletedFile() 정리 (Tidy)`

---

#### 1️⃣5️⃣ FileTestFixture - aFileWithCategory() (Cycle 15)

##### 🔴 Red: 테스트 작성
- [ ] `aFileWithCategory("상품")` 테스트
- [ ] category 검증
- [ ] 커밋: `test: aFileWithCategory() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `aFileWithCategory(String)` 메서드 작성
- [ ] 테스트 통과
- [ ] 커밋: `impl: aFileWithCategory() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 파라미터 검증 추가
- [ ] 커밋: `refactor: aFileWithCategory() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aFileWithCategory() 정리 (Tidy)`

---

#### 1️⃣6️⃣ FileTestFixture - createFiles() (Cycle 16)

##### 🔴 Red: 테스트 작성
- [ ] `createFiles(10, 1L)` 테스트
- [ ] 10개 File 생성 검증
- [ ] 커밋: `test: createFiles() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `createFiles(int, Long)` 메서드 작성
- [ ] 반복문으로 File 생성
- [ ] 테스트 통과
- [ ] 커밋: `impl: createFiles() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Stream API 사용 검토
- [ ] 커밋: `refactor: createFiles() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: createFiles() 정리 (Tidy)`

---

#### 1️⃣7️⃣ GeneratePresignedUrlRequestFixture - aRequest() (Cycle 17)

##### 🔴 Red: 테스트 작성
- [ ] `GeneratePresignedUrlRequestFixtureTest.java` 생성
- [ ] `aRequest()` 테스트
- [ ] 커밋: `test: GeneratePresignedUrlRequestFixture 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `GeneratePresignedUrlRequestFixture.java` 생성
- [ ] `aRequest()` 메서드 작성
- [ ] 기본값 설정
- [ ] 커밋: `impl: aRequest() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 상수로 기본값 추출
- [ ] 커밋: `refactor: aRequest() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aRequest() 정리 (Tidy)`

---

#### 1️⃣8️⃣ GeneratePresignedUrlRequestFixture - aLargeFileRequest() (Cycle 18)

##### 🔴 Red: 테스트 작성
- [ ] `aLargeFileRequest()` 테스트 (>= 100MB)
- [ ] fileSize 검증
- [ ] 커밋: `test: aLargeFileRequest() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `aLargeFileRequest()` 메서드 작성
- [ ] fileSize = 100MB 설정
- [ ] 커밋: `impl: aLargeFileRequest() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 파일 크기 상수화
- [ ] 커밋: `refactor: aLargeFileRequest() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aLargeFileRequest() 정리 (Tidy)`

---

#### 1️⃣9️⃣ GeneratePresignedUrlRequestFixture - anInvalidMimeTypeRequest() (Cycle 19)

##### 🔴 Red: 테스트 작성
- [ ] `anInvalidMimeTypeRequest()` 테스트
- [ ] 잘못된 MIME 타입 검증
- [ ] 커밋: `test: anInvalidMimeTypeRequest() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `anInvalidMimeTypeRequest()` 메서드 작성
- [ ] mimeType = "invalid/type" 설정
- [ ] 커밋: `impl: anInvalidMimeTypeRequest() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 다양한 잘못된 MIME 타입 추가
- [ ] 커밋: `refactor: anInvalidMimeTypeRequest() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: anInvalidMimeTypeRequest() 정리 (Tidy)`

---

#### 2️⃣0️⃣ UploadFromExternalUrlRequestFixture - aRequest() (Cycle 20)

##### 🔴 Red: 테스트 작성
- [ ] `UploadFromExternalUrlRequestFixtureTest.java` 생성
- [ ] `aRequest()` 테스트
- [ ] 커밋: `test: UploadFromExternalUrlRequestFixture 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `UploadFromExternalUrlRequestFixture.java` 생성
- [ ] `aRequest()` 메서드 작성 (HTTPS URL)
- [ ] 커밋: `impl: aRequest() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] URL 상수화
- [ ] 커밋: `refactor: aRequest() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aRequest() 정리 (Tidy)`

---

#### 2️⃣1️⃣ UploadFromExternalUrlRequestFixture - aHttpUrlRequest() (Cycle 21)

##### 🔴 Red: 테스트 작성
- [ ] `aHttpUrlRequest()` 테스트 (HTTP, 실패 케이스)
- [ ] 커밋: `test: aHttpUrlRequest() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `aHttpUrlRequest()` 메서드 작성
- [ ] externalUrl = "http://..." 설정
- [ ] 커밋: `impl: aHttpUrlRequest() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] HTTP/HTTPS 검증 로직 추가
- [ ] 커밋: `refactor: aHttpUrlRequest() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aHttpUrlRequest() 정리 (Tidy)`

---

#### 2️⃣2️⃣ UploadFromExternalUrlRequestFixture - aRequestWithWebhook() (Cycle 22)

##### 🔴 Red: 테스트 작성
- [ ] `aRequestWithWebhook()` 테스트
- [ ] webhookUrl 포함 검증
- [ ] 커밋: `test: aRequestWithWebhook() 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `aRequestWithWebhook()` 메서드 작성
- [ ] webhookUrl 설정
- [ ] 커밋: `impl: aRequestWithWebhook() 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Webhook URL 상수화
- [ ] 커밋: `refactor: aRequestWithWebhook() 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: aRequestWithWebhook() 정리 (Tidy)`

---

#### 2️⃣3️⃣ ProcessFileRequestFixture (Cycle 23)

##### 🔴 Red: 테스트 작성
- [ ] `ProcessFileRequestFixtureTest.java` 생성
- [ ] `aRequest()` 테스트
- [ ] 커밋: `test: ProcessFileRequestFixture 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] `ProcessFileRequestFixture.java` 생성
- [ ] `aRequest()` 메서드 작성
- [ ] jobTypes 설정
- [ ] 커밋: `impl: ProcessFileRequestFixture 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] JobType 리스트 다양화
- [ ] 커밋: `refactor: ProcessFileRequestFixture 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: ProcessFileRequestFixture 정리 (Tidy)`

---

#### 2️⃣4️⃣ Test Fixtures ArchUnit 검증 (Cycle 24)

##### 🔴 Red: 테스트 작성
- [ ] `TestFixtureArchUnitTest.java` 생성
- [ ] Fixture 네이밍 규칙 테스트 (*Fixture.java)
- [ ] 커밋: `test: TestFixture ArchUnit 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] ArchUnit 테스트 통과
- [ ] 모든 Fixture가 규칙 준수 확인
- [ ] 커밋: `impl: TestFixture ArchUnit 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 강화 (Fixture는 테스트 패키지 내)
- [ ] 커밋: `refactor: TestFixture ArchUnit 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 문서화
- [ ] 커밋: `test: TestFixture ArchUnit 정리 (Tidy)`

---

### Phase 3: Presigned URL 직접 업로드 E2E (20 사이클)

#### 2️⃣5️⃣ 시나리오 1-1: Presigned URL 발급 (Cycle 25)

##### 🔴 Red: 테스트 작성
- [ ] `PresignedUrlUploadE2ETest.java` 생성
- [ ] `presignedUrl_발급_성공()` 메서드 작성
- [ ] POST /api/v1/files/presigned-url 요청
- [ ] 커밋: `test: Presigned URL 발급 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] TestRestTemplate.postForEntity() 호출
- [ ] 응답 200 OK 검증
- [ ] presignedUrl 필드 존재 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: Presigned URL 발급 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Given-When-Then 패턴 적용
- [ ] AssertJ 사용
- [ ] 커밋: `refactor: Presigned URL 발급 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] GeneratePresignedUrlRequestFixture 사용
- [ ] 커밋: `test: Presigned URL 발급 정리 (Tidy)`

---

#### 2️⃣6️⃣ 시나리오 1-2: S3 업로드 시뮬레이션 (Cycle 26)

##### 🔴 Red: 테스트 작성
- [ ] S3 PUT 요청 테스트
- [ ] LocalStack S3 업로드 검증
- [ ] 커밋: `test: S3 업로드 시뮬레이션 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.put(presignedUrl, fileContent)
- [ ] S3Client.headObject() 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: S3 업로드 시뮬레이션 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] S3 업로드 Helper 메서드 분리
- [ ] 커밋: `refactor: S3 업로드 시뮬레이션 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] S3UploadHelper 클래스 생성
- [ ] 커밋: `test: S3 업로드 Helper 정리 (Tidy)`

---

#### 2️⃣7️⃣ 시나리오 1-3: 업로드 완료 신호 (Cycle 27)

##### 🔴 Red: 테스트 작성
- [ ] POST /api/v1/files/{fileId}/complete 테스트
- [ ] 응답 200 OK 검증
- [ ] 커밋: `test: 업로드 완료 신호 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.postForEntity() 호출
- [ ] 응답 status=COMPLETED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 업로드 완료 신호 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 응답 검증 메서드 분리
- [ ] 커밋: `refactor: 업로드 완료 신호 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 업로드 완료 신호 정리 (Tidy)`

---

#### 2️⃣8️⃣ 시나리오 1-4: 파일 조회 (Cycle 28)

##### 🔴 Red: 테스트 작성
- [ ] GET /api/v1/files/{fileId} 테스트
- [ ] FileDetailResponse 검증
- [ ] 커밋: `test: 파일 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET) 호출
- [ ] status=COMPLETED 검증
- [ ] s3Key 필드 존재 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: 파일 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] ParameterizedTypeReference 재사용
- [ ] 커밋: `refactor: 파일 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 파일 조회 정리 (Tidy)`

---

#### 2️⃣9️⃣ 시나리오 1-5: DB 검증 (Cycle 29)

##### 🔴 Red: 테스트 작성
- [ ] JdbcTemplate 쿼리로 DB 검증
- [ ] files 테이블 데이터 확인
- [ ] 커밋: `test: DB 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] jdbcTemplate.queryForObject() 호출
- [ ] COUNT(*) = 1 검증
- [ ] status=COMPLETED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: DB 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] DB 검증 Helper 메서드 생성
- [ ] 커밋: `refactor: DB 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] DbVerificationHelper 클래스 생성
- [ ] 커밋: `test: DB 검증 Helper 정리 (Tidy)`

---

#### 3️⃣0️⃣ 시나리오 2: Multipart Upload 성공 (Cycle 30)

##### 🔴 Red: 테스트 작성
- [ ] `presignedUrl_Multipart업로드_성공()` 테스트
- [ ] fileSize >= 100MB 요청
- [ ] 커밋: `test: Multipart Upload 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] aLargeFileRequest() Fixture 사용
- [ ] uploadId 필드 존재 검증
- [ ] S3 Multipart 시뮬레이션
- [ ] 테스트 통과
- [ ] 커밋: `impl: Multipart Upload 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Multipart Upload Helper 메서드 분리
- [ ] 커밋: `refactor: Multipart Upload 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] MultipartUploadHelper 생성
- [ ] 커밋: `test: Multipart Upload Helper 정리 (Tidy)`

---

#### 3️⃣1️⃣ 시나리오 3: 파일 크기 초과 실패 (Cycle 31)

##### 🔴 Red: 테스트 작성
- [ ] `presignedUrl_파일크기초과_실패()` 테스트
- [ ] fileSize > 1GB 요청
- [ ] 커밋: `test: 파일 크기 초과 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] GeneratePresignedUrlRequest(fileSize = 1GB + 1) 생성
- [ ] 응답 400 Bad Request 검증
- [ ] errorCode=FILE_SIZE_EXCEEDED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 파일 크기 초과 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Error 응답 검증 메서드 분리
- [ ] 커밋: `refactor: 파일 크기 초과 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] ErrorResponseVerifier 생성
- [ ] 커밋: `test: Error 검증 Helper 정리 (Tidy)`

---

#### 3️⃣2️⃣ 시나리오 4: 업로드 완료 전 조회 (Cycle 32)

##### 🔴 Red: 테스트 작성
- [ ] `파일조회_업로드완료전_PENDING()` 테스트
- [ ] Presigned URL 발급만 하고 조회
- [ ] 커밋: `test: 업로드 완료 전 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] POST /presigned-url만 실행
- [ ] GET /files/{fileId} 호출
- [ ] status=PENDING 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 업로드 완료 전 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Status 검증 메서드 분리
- [ ] 커밋: `refactor: 업로드 완료 전 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] StatusVerifier 생성
- [ ] 커밋: `test: Status 검증 Helper 정리 (Tidy)`

---

#### 3️⃣3️⃣ 시나리오 5: S3 업로드 없이 완료 시도 (Cycle 33)

##### 🔴 Red: 테스트 작성
- [ ] `업로드완료_S3미업로드_실패()` 테스트
- [ ] S3 PUT 없이 /complete 호출
- [ ] 커밋: `test: S3 미업로드 실패 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] POST /presigned-url
- [ ] POST /complete (S3 업로드 생략)
- [ ] 응답 500 Internal Server Error 검증
- [ ] errorCode=UPLOAD_VERIFICATION_FAILED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: S3 미업로드 실패 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] S3 검증 실패 시나리오 강화
- [ ] 커밋: `refactor: S3 미업로드 실패 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: S3 미업로드 실패 정리 (Tidy)`

---

#### 3️⃣4️⃣ 시나리오 5-2: DB 상태 FAILED 검증 (Cycle 34)

##### 🔴 Red: 테스트 작성
- [ ] DB에서 status=FAILED 검증
- [ ] 커밋: `test: FAILED 상태 DB 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] JdbcTemplate 쿼리
- [ ] status='FAILED' 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: FAILED 상태 DB 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] DB 검증 재사용
- [ ] 커밋: `refactor: FAILED 상태 DB 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: FAILED 상태 DB 검증 정리 (Tidy)`

---

#### 3️⃣5️⃣ 테스트 정리 (cleanup 메서드) (Cycle 35)

##### 🔴 Red: 테스트 작성
- [ ] @AfterEach cleanup 메서드 테스트
- [ ] 커밋: `test: cleanup 메서드 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] @AfterEach cleanup() 메서드 작성
- [ ] DELETE FROM files
- [ ] DELETE FROM message_outbox
- [ ] 커밋: `impl: cleanup 메서드 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] BaseIntegrationTest로 이동
- [ ] 커밋: `refactor: cleanup 메서드 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: cleanup 메서드 정리 (Tidy)`

---

#### 3️⃣6️⃣~4️⃣4️⃣ (9 사이클 예비)
- 추가 Edge Case 시나리오 (MIME 타입 검증 실패, 파일명 특수문자 처리 등)

---

### Phase 4: 외부 URL 업로드 E2E (16 사이클)

#### 4️⃣5️⃣ 시나리오 1-1: WireMock 외부 URL Mock 설정 (Cycle 45)

##### 🔴 Red: 테스트 작성
- [ ] `ExternalUrlUploadE2ETest.java` 생성
- [ ] WireMock GET /image.jpg Stub 테스트
- [ ] 커밋: `test: WireMock 외부 URL Mock 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] @RegisterExtension WireMock 설정
- [ ] wireMock.stubFor(get("/image.jpg")) 작성
- [ ] 테스트 통과
- [ ] 커밋: `impl: WireMock 외부 URL Mock 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] WireMock Stub 메서드 분리
- [ ] 커밋: `refactor: WireMock Mock 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] WireMockStubHelper 생성
- [ ] 커밋: `test: WireMock Stub Helper 정리 (Tidy)`

---

#### 4️⃣6️⃣ 시나리오 1-2: 외부 URL 업로드 요청 (Cycle 46)

##### 🔴 Red: 테스트 작성
- [ ] `외부URL_다운로드_성공()` 테스트
- [ ] POST /api/v1/files/from-url 요청
- [ ] 커밋: `test: 외부 URL 업로드 요청 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] UploadFromExternalUrlRequestFixture 사용
- [ ] restTemplate.postForEntity() 호출
- [ ] 응답 202 Accepted 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 외부 URL 업로드 요청 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 202 Accepted 검증 메서드 분리
- [ ] 커밋: `refactor: 외부 URL 업로드 요청 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 외부 URL 업로드 요청 정리 (Tidy)`

---

#### 4️⃣7️⃣ 시나리오 1-3: MessageOutbox 생성 검증 (Cycle 47)

##### 🔴 Red: 테스트 작성
- [ ] DB에서 message_outbox 레코드 확인
- [ ] 커밋: `test: MessageOutbox 생성 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] JdbcTemplate 쿼리
- [ ] COUNT(*) = 1 검증
- [ ] status=PENDING 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: MessageOutbox 생성 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] MessageOutbox 검증 Helper 생성
- [ ] 커밋: `refactor: MessageOutbox 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] MessageOutboxVerifier 생성
- [ ] 커밋: `test: MessageOutbox Verifier 정리 (Tidy)`

---

#### 4️⃣8️⃣ 시나리오 1-4: SQS Consumer 수동 트리거 (Cycle 48)

##### 🔴 Red: 테스트 작성
- [ ] triggerSqsConsumer() 호출 테스트
- [ ] 백그라운드 작업 실행 검증
- [ ] 커밋: `test: SQS Consumer 트리거 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] triggerSqsConsumer() 메서드 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: SQS Consumer 트리거 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Consumer 트리거 로직 최적화
- [ ] 커밋: `refactor: SQS Consumer 트리거 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: SQS Consumer 트리거 정리 (Tidy)`

---

#### 4️⃣9️⃣ 시나리오 1-5: 파일 상태 COMPLETED 검증 (Cycle 49)

##### 🔴 Red: 테스트 작성
- [ ] GET /files/{fileId} 호출
- [ ] status=COMPLETED 검증
- [ ] 커밋: `test: 파일 COMPLETED 상태 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET) 호출
- [ ] status=COMPLETED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 파일 COMPLETED 상태 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 상태 검증 재사용
- [ ] 커밋: `refactor: COMPLETED 상태 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: COMPLETED 상태 검증 정리 (Tidy)`

---

#### 5️⃣0️⃣ 시나리오 1-6: S3 업로드 검증 (Cycle 50)

##### 🔴 Red: 테스트 작성
- [ ] S3Client.headObject() 호출
- [ ] Object 존재 확인
- [ ] 커밋: `test: S3 업로드 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] S3Client.headObject() 호출
- [ ] contentLength 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: S3 업로드 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] S3 검증 Helper 재사용
- [ ] 커밋: `refactor: S3 업로드 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: S3 업로드 검증 정리 (Tidy)`

---

#### 5️⃣1️⃣ 시나리오 2: HTTP URL 거부 (Cycle 51)

##### 🔴 Red: 테스트 작성
- [ ] `외부URL_HTTP거부_실패()` 테스트
- [ ] aHttpUrlRequest() Fixture 사용
- [ ] 커밋: `test: HTTP URL 거부 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] POST /files/from-url (http://)
- [ ] 응답 400 Bad Request 검증
- [ ] errorCode=INVALID_URL 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: HTTP URL 거부 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] URL 검증 로직 강화
- [ ] 커밋: `refactor: HTTP URL 거부 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: HTTP URL 거부 정리 (Tidy)`

---

#### 5️⃣2️⃣ 시나리오 3: 외부 URL 다운로드 실패 (404) (Cycle 52)

##### 🔴 Red: 테스트 작성
- [ ] `외부URL_다운로드_404실패()` 테스트
- [ ] WireMock 404 응답 설정
- [ ] 커밋: `test: 외부 URL 404 실패 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] wireMock.stubFor(404)
- [ ] SQS Consumer 실행
- [ ] GET /files/{fileId} → status=FAILED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 외부 URL 404 실패 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 404 처리 로직 강화
- [ ] 커밋: `refactor: 외부 URL 404 실패 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 외부 URL 404 실패 정리 (Tidy)`

---

#### 5️⃣3️⃣ 시나리오 3-2: 재시도 3회 검증 (Cycle 53)

##### 🔴 Red: 테스트 작성
- [ ] DB에서 retryCount=3 검증
- [ ] errorMessage 존재 확인
- [ ] 커밋: `test: 재시도 3회 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] JdbcTemplate 쿼리
- [ ] retryCount=3 검증
- [ ] errorMessage NOT NULL 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 재시도 3회 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Retry 검증 Helper 생성
- [ ] 커밋: `refactor: 재시도 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] RetryVerifier 생성
- [ ] 커밋: `test: Retry Verifier 정리 (Tidy)`

---

#### 5️⃣4️⃣ 시나리오 4-1: Webhook Mock 설정 (Cycle 54)

##### 🔴 Red: 테스트 작성
- [ ] `Webhook_전송_성공()` 테스트
- [ ] WireMock POST /webhook Stub
- [ ] 커밋: `test: Webhook Mock 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] wireMock.stubFor(post("/webhook"))
- [ ] 200 OK 응답 설정
- [ ] 테스트 통과
- [ ] 커밋: `impl: Webhook Mock 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Webhook Stub 메서드 분리
- [ ] 커밋: `refactor: Webhook Mock 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] WebhookStubHelper 생성
- [ ] 커밋: `test: Webhook Stub Helper 정리 (Tidy)`

---

#### 5️⃣5️⃣ 시나리오 4-2: Webhook 전송 검증 (Cycle 55)

##### 🔴 Red: 테스트 작성
- [ ] wireMock.verify() 호출 테스트
- [ ] Webhook Payload 검증
- [ ] 커밋: `test: Webhook 전송 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] aRequestWithWebhook() Fixture 사용
- [ ] SQS Consumer 실행
- [ ] wireMock.verify(postRequestedFor("/webhook"))
- [ ] 테스트 통과
- [ ] 커밋: `impl: Webhook 전송 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Webhook 검증 메서드 분리
- [ ] 커밋: `refactor: Webhook 전송 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] WebhookVerifier 생성
- [ ] 커밋: `test: Webhook Verifier 정리 (Tidy)`

---

#### 5️⃣6️⃣ 시나리오 4-3: Webhook Payload 검증 (Cycle 56)

##### 🔴 Red: 테스트 작성
- [ ] Webhook Body에 fileId, status, s3Url 포함 검증
- [ ] 커밋: `test: Webhook Payload 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] withRequestBody(matchingJsonPath("$.fileId"))
- [ ] matchingJsonPath("$.status")
- [ ] 테스트 통과
- [ ] 커밋: `impl: Webhook Payload 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] JsonPath 검증 메서드 분리
- [ ] 커밋: `refactor: Webhook Payload 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: Webhook Payload 검증 정리 (Tidy)`

---

#### 5️⃣7️⃣ 시나리오 4-4: HMAC 서명 검증 (Cycle 57)

##### 🔴 Red: 테스트 작성
- [ ] X-Signature 헤더 존재 검증
- [ ] HMAC SHA256 서명 검증
- [ ] 커밋: `test: HMAC 서명 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] withHeader("X-Signature", matching(".*"))
- [ ] HMAC 검증 로직
- [ ] 테스트 통과
- [ ] 커밋: `impl: HMAC 서명 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] HMAC 검증 Helper 생성
- [ ] 커밋: `refactor: HMAC 서명 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] HmacVerifier 생성
- [ ] 커밋: `test: HMAC Verifier 정리 (Tidy)`

---

#### 5️⃣8️⃣~6️⃣0️⃣ (3 사이클 예비)
- 추가 Edge Case (Timeout, Retry 실패 등)

---

### Phase 5: 파일 가공 파이프라인 E2E (16 사이클)

#### 6️⃣1️⃣ 시나리오 1-1: 썸네일 생성 요청 (Cycle 61)

##### 🔴 Red: 테스트 작성
- [ ] `FileProcessingPipelineE2ETest.java` 생성
- [ ] `썸네일생성_성공()` 테스트
- [ ] POST /files/{fileId}/process 요청
- [ ] 커밋: `test: 썸네일 생성 요청 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] ProcessFileRequestFixture 사용
- [ ] restTemplate.postForEntity() 호출
- [ ] 응답 202 Accepted 검증
- [ ] jobId 수신 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: 썸네일 생성 요청 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 가공 요청 메서드 분리
- [ ] 커밋: `refactor: 썸네일 생성 요청 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 썸네일 생성 요청 정리 (Tidy)`

---

#### 6️⃣2️⃣ 시나리오 1-2: SQS Consumer 가공 작업 실행 (Cycle 62)

##### 🔴 Red: 테스트 작성
- [ ] triggerSqsConsumer() 호출
- [ ] 가공 작업 실행 검증
- [ ] 커밋: `test: 가공 작업 실행 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] triggerSqsConsumer() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: 가공 작업 실행 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Consumer 트리거 재사용
- [ ] 커밋: `refactor: 가공 작업 실행 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 가공 작업 실행 정리 (Tidy)`

---

#### 6️⃣3️⃣ 시나리오 1-3: Job 상태 COMPLETED 검증 (Cycle 63)

##### 🔴 Red: 테스트 작성
- [ ] GET /files/{fileId}/jobs 호출
- [ ] status=COMPLETED 검증
- [ ] 커밋: `test: Job COMPLETED 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET) 호출
- [ ] status=COMPLETED 검증
- [ ] outputS3Key 존재 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: Job COMPLETED 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Job 검증 Helper 생성
- [ ] 커밋: `refactor: Job COMPLETED 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] JobVerifier 생성
- [ ] 커밋: `test: Job Verifier 정리 (Tidy)`

---

#### 6️⃣4️⃣ 시나리오 1-4: S3 썸네일 검증 (Cycle 64)

##### 🔴 Red: 테스트 작성
- [ ] S3Client.headObject() 호출
- [ ] 썸네일 Object 존재 확인
- [ ] 커밋: `test: S3 썸네일 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] S3Client.headObject(outputS3Key)
- [ ] 존재 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: S3 썸네일 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] S3 검증 재사용
- [ ] 커밋: `refactor: S3 썸네일 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: S3 썸네일 검증 정리 (Tidy)`

---

#### 6️⃣5️⃣ 시나리오 2: 여러 가공 작업 동시 실행 (Cycle 65)

##### 🔴 Red: 테스트 작성
- [ ] `다중가공작업_동시실행_성공()` 테스트
- [ ] jobTypes=[THUMBNAIL, RESIZE, OCR] 요청
- [ ] 커밋: `test: 다중 가공 작업 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] ProcessFileRequest(jobTypes=3개)
- [ ] 응답에서 3개 jobId 수신 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: 다중 가공 작업 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] jobId 리스트 검증 메서드 분리
- [ ] 커밋: `refactor: 다중 가공 작업 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 다중 가공 작업 정리 (Tidy)`

---

#### 6️⃣6️⃣ 시나리오 2-2: 3개 Job 모두 PENDING 확인 (Cycle 66)

##### 🔴 Red: 테스트 작성
- [ ] GET /files/{fileId}/jobs
- [ ] 3개 Job 모두 PENDING 검증
- [ ] 커밋: `test: 3개 Job PENDING 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET)
- [ ] jobs.size() = 3 검증
- [ ] 모든 status=PENDING 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 3개 Job PENDING 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Job 리스트 검증 Helper 생성
- [ ] 커밋: `refactor: Job PENDING 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] JobListVerifier 생성
- [ ] 커밋: `test: Job List Verifier 정리 (Tidy)`

---

#### 6️⃣7️⃣ 시나리오 2-3: 3개 Job 모두 COMPLETED 확인 (Cycle 67)

##### 🔴 Red: 테스트 작성
- [ ] SQS Consumer 실행 후 3개 Job COMPLETED 검증
- [ ] 커밋: `test: 3개 Job COMPLETED 검증 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] triggerSqsConsumer()
- [ ] GET /files/{fileId}/jobs
- [ ] 모든 status=COMPLETED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 3개 Job COMPLETED 검증 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Job 상태 검증 재사용
- [ ] 커밋: `refactor: 3개 Job COMPLETED 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 3개 Job COMPLETED 정리 (Tidy)`

---

#### 6️⃣8️⃣ 시나리오 2-4: FileDetailResponse에 Job 포함 검증 (Cycle 68)

##### 🔴 Red: 테스트 작성
- [ ] GET /files/{fileId}
- [ ] jobs 필드에 3개 Job 포함 검증
- [ ] 커밋: `test: FileDetailResponse Job 포함 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET /files/{fileId})
- [ ] response.jobs.size() = 3 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileDetailResponse Job 포함 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] FileDetailResponse 검증 Helper 생성
- [ ] 커밋: `refactor: FileDetailResponse 검증 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] FileDetailResponseVerifier 생성
- [ ] 커밋: `test: FileDetailResponse Verifier 정리 (Tidy)`

---

#### 6️⃣9️⃣ 시나리오 3: 가공 실패 후 재시도 (Cycle 69)

##### 🔴 Red: 테스트 작성
- [ ] `가공실패_재시도_성공()` 테스트
- [ ] 첫 번째 시도 실패 시뮬레이션
- [ ] 커밋: `test: 가공 재시도 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] 실패 시뮬레이션 설정
- [ ] GET /jobs/{jobId} → retryCount=1 검증
- [ ] 두 번째 시도 성공
- [ ] status=COMPLETED 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 가공 재시도 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 재시도 로직 강화
- [ ] 커밋: `refactor: 가공 재시도 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 가공 재시도 정리 (Tidy)`

---

#### 7️⃣0️⃣ 시나리오 4: 최대 재시도 초과 (Cycle 70)

##### 🔴 Red: 테스트 작성
- [ ] `가공실패_최대재시도초과_FAILED()` 테스트
- [ ] 2회 실패 시뮬레이션
- [ ] 커밋: `test: 최대 재시도 초과 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] 2회 실패 설정
- [ ] GET /jobs/{jobId}
- [ ] status=FAILED, retryCount=2 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 최대 재시도 초과 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 재시도 한계 검증 강화
- [ ] 커밋: `refactor: 최대 재시도 초과 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 최대 재시도 초과 정리 (Tidy)`

---

#### 7️⃣1️⃣~7️⃣6️⃣ (6 사이클 예비)
- 추가 Edge Case (가공 타입별 검증, 동시성 테스트 등)

---

### Phase 6: 파일 조회 E2E (16 사이클)

#### 7️⃣7️⃣ 시나리오 1-1: 30개 파일 생성 (Cycle 77)

##### 🔴 Red: 테스트 작성
- [ ] `FileQueryE2ETest.java` 생성
- [ ] `cursorPagination_정상동작()` 테스트
- [ ] createFiles(30, 1L) 호출
- [ ] 커밋: `test: Cursor Pagination 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] FileTestFixture.createFiles(30, 1L)
- [ ] DB에 30개 파일 생성 확인
- [ ] 테스트 통과
- [ ] 커밋: `impl: 30개 파일 생성 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Bulk Insert 최적화
- [ ] 커밋: `refactor: 파일 생성 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 파일 생성 정리 (Tidy)`

---

#### 7️⃣8️⃣ 시나리오 1-2: 첫 페이지 조회 (Cycle 78)

##### 🔴 Red: 테스트 작성
- [ ] GET /files?uploaderId=1&size=10
- [ ] 10개 파일, hasNext=true 검증
- [ ] 커밋: `test: 첫 페이지 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] restTemplate.exchange(GET)
- [ ] content.size() = 10 검증
- [ ] hasNext = true 검증
- [ ] nextCursor != null 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 첫 페이지 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Pagination 검증 Helper 생성
- [ ] 커밋: `refactor: 첫 페이지 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] PaginationVerifier 생성
- [ ] 커밋: `test: Pagination Verifier 정리 (Tidy)`

---

#### 7️⃣9️⃣ 시나리오 1-3: 두 번째 페이지 조회 (Cycle 79)

##### 🔴 Red: 테스트 작성
- [ ] GET /files?uploaderId=1&size=10&cursor={cursor}
- [ ] 다음 10개 파일, hasNext=true 검증
- [ ] 커밋: `test: 두 번째 페이지 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] nextCursor를 query parameter로 전달
- [ ] content.size() = 10 검증
- [ ] hasNext = true 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 두 번째 페이지 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Cursor 전달 로직 메서드화
- [ ] 커밋: `refactor: 두 번째 페이지 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 두 번째 페이지 조회 정리 (Tidy)`

---

#### 8️⃣0️⃣ 시나리오 1-4: 마지막 페이지 조회 (Cycle 80)

##### 🔴 Red: 테스트 작성
- [ ] GET /files?uploaderId=1&size=10&cursor={cursor}
- [ ] 마지막 10개 파일, hasNext=false 검증
- [ ] 커밋: `test: 마지막 페이지 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] nextCursor 전달
- [ ] content.size() = 10 검증
- [ ] hasNext = false 검증
- [ ] nextCursor = null 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 마지막 페이지 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 마지막 페이지 검증 메서드 분리
- [ ] 커밋: `refactor: 마지막 페이지 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 마지막 페이지 조회 정리 (Tidy)`

---

#### 8️⃣1️⃣ 시나리오 2: 상태별 필터링 (COMPLETED) (Cycle 81)

##### 🔴 Red: 테스트 작성
- [ ] `상태필터링_COMPLETED_성공()` 테스트
- [ ] 5개 COMPLETED, 5개 PENDING 생성
- [ ] 커밋: `test: 상태 필터링 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] createFiles(5, COMPLETED) + createFiles(5, PENDING)
- [ ] GET /files?status=COMPLETED
- [ ] content.size() = 5 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 상태 필터링 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 상태별 필터링 검증 메서드 분리
- [ ] 커밋: `refactor: 상태 필터링 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 상태 필터링 정리 (Tidy)`

---

#### 8️⃣2️⃣ 시나리오 3: 카테고리별 필터링 (Cycle 82)

##### 🔴 Red: 테스트 작성
- [ ] `카테고리필터링_성공()` 테스트
- [ ] 5개 "상품", 5개 "전시영역" 생성
- [ ] 커밋: `test: 카테고리 필터링 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] createFiles(5, "상품") + createFiles(5, "전시영역")
- [ ] GET /files?category=상품
- [ ] content.size() = 5 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 카테고리 필터링 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 카테고리 필터링 검증 메서드 분리
- [ ] 커밋: `refactor: 카테고리 필터링 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 카테고리 필터링 정리 (Tidy)`

---

#### 8️⃣3️⃣ 시나리오 4: 파일 상세 조회 (Job 포함) (Cycle 83)

##### 🔴 Red: 테스트 작성
- [ ] `파일상세조회_Job포함_성공()` 테스트
- [ ] 파일 1개 + Job 2개 생성
- [ ] 커밋: `test: 파일 상세 조회 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] 파일 업로드 + 가공 작업 2개 완료
- [ ] GET /files/{fileId}
- [ ] jobs 필드에 2개 Job 포함 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: 파일 상세 조회 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] FileDetailResponse 검증 재사용
- [ ] 커밋: `refactor: 파일 상세 조회 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: 파일 상세 조회 정리 (Tidy)`

---

#### 8️⃣4️⃣~9️⃣2️⃣ (9 사이클 예비)
- 추가 필터링 조합 (status + category), 정렬 테스트 등

---

### Phase 7: Outbox Pattern E2E (12 사이클)

#### 9️⃣3️⃣ 시나리오 1-1: MessageOutbox PENDING 레코드 생성 (Cycle 93)

##### 🔴 Red: 테스트 작성
- [ ] `OutboxPatternE2ETest.java` 생성
- [ ] `afterCommitListener_정상동작()` 테스트
- [ ] POST /files/from-url 후 MessageOutbox 확인
- [ ] 커밋: `test: MessageOutbox PENDING 생성 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] POST /files/from-url
- [ ] JdbcTemplate 쿼리
- [ ] status=PENDING 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: MessageOutbox PENDING 생성 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] MessageOutbox 검증 재사용
- [ ] 커밋: `refactor: MessageOutbox PENDING 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: MessageOutbox PENDING 정리 (Tidy)`

---

#### 9️⃣4️⃣ 시나리오 1-2: After-Commit Listener 실행 (Cycle 94)

##### 🔴 Red: 테스트 작성
- [ ] Awaitility로 비동기 검증
- [ ] status=SENT 확인
- [ ] 커밋: `test: After-Commit Listener 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] await().atMost(5, SECONDS)
- [ ] status=SENT 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: After-Commit Listener 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Awaitility 검증 재사용
- [ ] 커밋: `refactor: After-Commit Listener 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: After-Commit Listener 정리 (Tidy)`

---

#### 9️⃣5️⃣ 시나리오 1-3: SQS 메시지 전송 확인 (Cycle 95)

##### 🔴 Red: 테스트 작성
- [ ] LocalStack SQS 메시지 조회
- [ ] 메시지 존재 확인
- [ ] 커밋: `test: SQS 메시지 전송 확인 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] SQS Client로 메시지 조회
- [ ] 메시지 존재 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: SQS 메시지 전송 확인 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] SQS 검증 Helper 생성
- [ ] 커밋: `refactor: SQS 메시지 확인 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] SqsMessageVerifier 생성
- [ ] 커밋: `test: SQS Verifier 정리 (Tidy)`

---

#### 9️⃣6️⃣ 시나리오 2-1: Fallback Scheduler 동작 확인 (Cycle 96)

##### 🔴 Red: 테스트 작성
- [ ] `fallbackScheduler_정상동작()` 테스트
- [ ] MessageOutbox 직접 생성 (createdAt < 1분 전)
- [ ] 커밋: `test: Fallback Scheduler 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] MessageOutbox 레코드 직접 생성
- [ ] Fallback Scheduler 수동 트리거
- [ ] status=SENT 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: Fallback Scheduler 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] Scheduler 트리거 메서드 분리
- [ ] 커밋: `refactor: Fallback Scheduler 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerHelper 생성
- [ ] 커밋: `test: Scheduler Helper 정리 (Tidy)`

---

#### 9️⃣7️⃣ 시나리오 2-2: SQS 메시지 전송 확인 (Cycle 97)

##### 🔴 Red: 테스트 작성
- [ ] Fallback Scheduler 후 SQS 메시지 확인
- [ ] 커밋: `test: Fallback SQS 전송 확인 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] SQS Client로 메시지 조회
- [ ] 메시지 존재 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: Fallback SQS 전송 확인 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] SQS 검증 재사용
- [ ] 커밋: `refactor: Fallback SQS 확인 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: Fallback SQS 확인 정리 (Tidy)`

---

#### 9️⃣8️⃣ 시나리오 3: 재시도 실패 시 FAILED 상태 (Cycle 98)

##### 🔴 Red: 테스트 작성
- [ ] `재시도실패_FAILED상태_전환()` 테스트
- [ ] retryCount=2 설정 후 실패 시뮬레이션
- [ ] 커밋: `test: FAILED 상태 전환 테스트 추가 (Red)`

##### 🟢 Green: 최소 구현
- [ ] MessageOutbox(retryCount=2) 생성
- [ ] SQS 전송 실패 시뮬레이션
- [ ] Fallback Scheduler 실행
- [ ] status=FAILED, retryCount=3 검증
- [ ] 테스트 통과
- [ ] 커밋: `impl: FAILED 상태 전환 구현 (Green)`

##### ♻️ Refactor: 리팩토링
- [ ] 재시도 한계 검증 강화
- [ ] 커밋: `refactor: FAILED 상태 전환 개선 (Refactor)`

##### 🧹 Tidy: TestFixture 정리
- [ ] 문서화
- [ ] 커밋: `test: FAILED 상태 전환 정리 (Tidy)`

---

#### 9️⃣9️⃣~1️⃣0️⃣4️⃣ (6 사이클 예비)
- Outbox Pattern 추가 시나리오

---

### Phase 8: Performance Test (4 사이클)

#### 1️⃣0️⃣5️⃣ 대용량 파일 업로드 성능 테스트 (100MB) (Cycle 105)

(생략 - 동일 패턴 반복)

---

#### 1️⃣0️⃣6️⃣ 대용량 파일 업로드 성능 테스트 (1GB) (Cycle 106)

(생략)

---

#### 1️⃣0️⃣7️⃣ 동시 업로드 성능 테스트 (10개 파일) (Cycle 107)

(생략)

---

#### 1️⃣0️⃣8️⃣ Cursor Pagination 성능 테스트 (10,000개 파일) (Cycle 108)

(생략)

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (104 사이클)
- [ ] TestContainers 설정 완료 (MySQL, LocalStack S3, Redis)
- [ ] 5개 E2E Test 클래스 구현 완료
  - PresignedUrlUploadE2ETest (5개 시나리오)
  - ExternalUrlUploadE2ETest (4개 시나리오)
  - FileProcessingPipelineE2ETest (4개 시나리오)
  - FileQueryE2ETest (4개 시나리오)
  - OutboxPatternE2ETest (3개 시나리오)
- [ ] 3개 Test Fixture 구현 완료
  - FileTestFixture
  - GeneratePresignedUrlRequestFixture
  - UploadFromExternalUrlRequestFixture
- [ ] WireMock 설정 완료 (외부 URL, Webhook)
- [ ] Performance Test 3개 완료
- [ ] 모든 E2E 테스트 통과 (총 20+ 시나리오)
- [ ] 테스트 커버리지 > 80%
- [ ] Zero-Tolerance 규칙 준수 검증
  - TestRestTemplate 필수 (MockMvc 금지)
  - TestContainers 필수 (H2 금지)
  - Flyway vs @Sql 분리
  - Transaction Isolation (@Transactional 금지)
- [ ] Integration Test ArchUnit 테스트 통과
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/FILE-005.md
- **PRD**: docs/prd/file-management-system.md
- **컨벤션**: docs/coding_convention/05-testing/integration-testing/

---

## 📊 Phase별 사이클 요약

| Phase | 내용 | 사이클 수 | 예상 시간 |
|-------|------|----------|----------|
| 1 | Test Infrastructure 구성 | 12 | 180분 |
| 2 | Test Fixtures 구현 | 12 | 180분 |
| 3 | Presigned URL 직접 업로드 E2E | 20 | 300분 |
| 4 | 외부 URL 업로드 E2E | 16 | 240분 |
| 5 | 파일 가공 파이프라인 E2E | 16 | 240분 |
| 6 | 파일 조회 E2E | 16 | 240분 |
| 7 | Outbox Pattern E2E | 12 | 180분 |
| 8 | Performance Test | 4 | 60분 |
| **총계** | | **104** | **1,560분** |

---

## 🎯 핵심 원칙

1. **작은 단위**: 각 사이클은 5-15분 내 완료
2. **4단계 필수**: Red → Green → Refactor → Tidy 모두 수행
3. **TestFixture 필수**: Tidy 단계에서 Object Mother 패턴 적용
4. **Zero-Tolerance**: Refactor 단계에서 ArchUnit 검증
5. **실제 환경 시뮬레이션**: TestContainers + TestRestTemplate + WireMock
6. **테스트 독립성**: @Transactional 금지, @AfterEach cleanup 필수
7. **Given-When-Then**: 모든 테스트는 명확한 시나리오 패턴
8. **AssertJ 사용**: 가독성 높은 Assertion
