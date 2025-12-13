# FILE-ASSET-PROCESSING Integration Test Plan

> **Jira Issue**: [KAN-343](https://ryuqqq.atlassian.net/browse/KAN-343)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)

## Overview
- **PRD**: `docs/prd/file-asset-processing.md`
- **Layer**: Integration / E2E Tests
- **Estimated Time**: 180 minutes (3 hours)
- **Total Cycles**: 10 TDD cycles

---

## Integration Test Categories

### 1. Component Integration Tests
- Persistence Layer 통합 (JPA + MySQL)
- Infrastructure Layer 통합 (실제 라이브러리)

### 2. E2E Tests
- 전체 파일 처리 흐름 (업로드 → 가공 → 완료)
- n8n API 시뮬레이션 흐름
- Outbox 패턴 동작 검증

### 3. Contract Tests
- API 응답 형식 검증
- 에러 응답 표준화 검증

---

## Zero-Tolerance Rules (Integration Tests)

### Must Follow
- [x] **MockMvc 금지** - TestRestTemplate 필수
- [x] **@SpringBootTest(webEnvironment = RANDOM_PORT)** - 실제 서버 구동
- [x] **TestContainers 사용** - MySQL, LocalStack (S3, SQS)
- [x] **실제 데이터베이스** - H2 금지, MySQL 8.0 사용
- [x] **실제 이미지/HTML 파일** - 테스트 리소스 사용

### Test Environment
```yaml
TestContainers:
  - MySQL 8.0
  - LocalStack (S3, SQS)

Test Data:
  - resources/images/test-image-*.jpg
  - resources/html/test-*.html

Profile:
  - spring.profiles.active=integration-test
```

---

## TDD Cycles

### Cycle 1: 테스트 인프라 설정 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/BaseIntegrationTest.java
[ ] @SpringBootTest(webEnvironment = RANDOM_PORT)
[ ] @Testcontainers
[ ] @ActiveProfiles("integration-test")
[ ] MySQLContainer 설정
[ ] LocalStackContainer 설정 (S3, SQS)
[ ] @DynamicPropertySource로 설정 주입
[ ] 테스트: `컨테이너_정상시작()` - 연결 확인
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: Integration 테스트 기반 인프라 설정"
```

**Green Phase** - `feat:`
```
[ ] BaseIntegrationTest 추상 클래스 생성
[ ] @Container static MySQLContainer<?> mysql
[ ] @Container static LocalStackContainer localstack
[ ] @DynamicPropertySource 설정
  - spring.datasource.url
  - spring.datasource.username
  - spring.datasource.password
  - aws.s3.endpoint
  - aws.sqs.endpoint
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: Integration 테스트 인프라 구현"
```

---

### Cycle 2: FileAsset 생성 → 조회 흐름 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/FileAssetCrudIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] @Autowired TestRestTemplate
[ ] 테스트: `파일에셋_생성후_상세조회_성공()` - POST → GET /{id}
[ ] 테스트: `파일에셋_목록조회_페이징()` - GET /?page=0&size=10
[ ] 테스트: `파일에셋_상태업데이트()` - PATCH /{id}/status
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: FileAsset CRUD 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 테스트 데이터 준비 (TestFixture 또는 @Sql)
[ ] API 엔드포인트 호출 및 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAsset CRUD 통합 테스트 통과"
```

---

### Cycle 3: 이미지 가공 전체 흐름 (25분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/ImageProcessingE2ETest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `이미지업로드_가공요청_완료확인()` - 전체 E2E 흐름
  1. S3에 테스트 이미지 업로드
  2. FileAsset 생성 (상태: UPLOADED)
  3. POST /process 호출
  4. 상태 변경 확인 (UPLOADED → PROCESSING → RESIZED)
  5. 가공된 파일 목록 확인 (LARGE, MEDIUM, THUMBNAIL)
  6. 각 variant의 WebP + JPEG 존재 확인
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: 이미지 가공 E2E 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] S3 테스트 버킷 생성 (LocalStack)
[ ] 테스트 이미지 업로드 헬퍼 메서드
[ ] 비동기 처리 대기 (Awaitility 사용)
[ ] 최종 상태 및 결과 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: 이미지 가공 E2E 테스트 통과"
```

---

### Cycle 4: HTML 이미지 추출 흐름 (25분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/HtmlProcessingE2ETest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `HTML업로드_이미지추출_URL교체()` - HTML 처리 E2E
  1. 테스트 HTML 파일 준비 (외부 이미지 URL 포함)
  2. S3에 HTML 업로드
  3. FileAsset 생성 (category: HTML)
  4. POST /process 호출
  5. 이미지 추출 확인 (ProcessedFileAsset)
  6. HTML 내 URL 교체 확인
  7. 상태: N8N_COMPLETED
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: HTML 이미지 추출 E2E 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 테스트 HTML 파일 준비 (resources/html/test-with-images.html)
[ ] Mock 외부 이미지 서버 (WireMock 또는 실제 파일)
[ ] 비동기 처리 대기
[ ] 교체된 HTML 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: HTML 이미지 추출 E2E 테스트 통과"
```

---

### Cycle 5: Outbox 패턴 동작 검증 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/OutboxPatternIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `Outbox_이벤트저장_SQS전송()` - Outbox → SQS 흐름
  1. FileAsset 생성
  2. 가공 요청 (POST /process)
  3. Outbox 테이블에 PENDING 이벤트 확인
  4. OutboxRelayScheduler 실행 (또는 대기)
  5. SQS 메시지 수신 확인 (LocalStack)
  6. Outbox 상태 SENT 확인
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: Outbox 패턴 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] SQS Queue 생성 (LocalStack)
[ ] Outbox 테이블 직접 조회 (JdbcTemplate 또는 Repository)
[ ] SQS 메시지 폴링
[ ] Outbox 상태 변경 확인
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: Outbox 패턴 통합 테스트 통과"
```

---

### Cycle 6: StatusHistory 추적 검증 (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/StatusHistoryIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `상태변경시_History기록()` - StatusHistory 검증
  1. FileAsset 생성 (UPLOADED)
  2. POST /process → PROCESSING
  3. 가공 완료 → RESIZED
  4. StatusHistory 조회
  5. 변경 이력 순서 및 내용 검증
  6. durationMillis 검증 (이전 상태 체류 시간)
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: StatusHistory 추적 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] StatusHistory 조회 API 또는 Repository 직접 조회
[ ] 이력 순서 검증
[ ] 체류 시간 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: StatusHistory 추적 통합 테스트 통과"
```

---

### Cycle 7: n8n API 시뮬레이션 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/N8nApiSimulationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `n8n_파일목록조회_필터링()` - n8n 워크플로우 시뮬레이션
  1. 여러 FileAsset 생성 (다양한 상태, 카테고리)
  2. GET /?status=RESIZED&category=PRODUCT_IMAGE
  3. 필터 결과 검증
  4. 페이징 동작 검증
[ ] 테스트: `n8n_상태업데이트_N8N_PROCESSING()` - n8n 상태 변경
  1. RESIZED 상태 FileAsset
  2. PATCH /{id}/status → N8N_PROCESSING
  3. 상태 변경 확인
  4. PATCH /{id}/status → N8N_COMPLETED
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: n8n API 시뮬레이션 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 다양한 테스트 데이터 준비
[ ] API 호출 및 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: n8n API 시뮬레이션 테스트 통과"
```

---

### Cycle 8: 에러 시나리오 검증 (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/ErrorScenarioIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `존재하지않는파일_404응답()` - GET /file-assets/invalid-id
[ ] 테스트: `잘못된상태전환_400응답()` - UPLOADED → RESIZED (직접 전환 불가)
[ ] 테스트: `이미가공중_409응답()` - PROCESSING 상태에서 다시 /process
[ ] 테스트: `손상된이미지_500응답()` - 가공 실패 케이스
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: 에러 시나리오 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 각 에러 케이스에 맞는 테스트 데이터 준비
[ ] HTTP 상태 코드 및 에러 응답 본문 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: 에러 시나리오 통합 테스트 통과"
```

---

### Cycle 9: 동시성 테스트 (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/ConcurrencyIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `동시_가공요청_중복방지()` - Race Condition
  1. FileAsset 생성 (UPLOADED)
  2. 동시에 POST /process 2회 호출 (ExecutorService)
  3. 하나만 성공, 하나는 409 응답
  4. 최종 ProcessedFileAsset 중복 없음 확인
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: 동시성 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] ExecutorService + CountDownLatch로 동시 요청
[ ] 응답 코드 분석 (202 vs 409)
[ ] 최종 데이터 일관성 검증
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: 동시성 통합 테스트 통과"
```

---

### Cycle 10: 성능 검증 테스트 (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: application/src/test/java/com/fileflow/fileasset/integration/PerformanceIntegrationTest.java
[ ] extends BaseIntegrationTest
[ ] 테스트: `이미지리사이징_5MB이하_5초내완료()` - P95 기준
  1. 5MB 테스트 이미지 준비
  2. 가공 요청
  3. 완료 시간 측정
  4. 5초 이내 완료 검증
[ ] 테스트: `API응답시간_200ms이내()` - 목록 조회 성능
  1. 100개 FileAsset 생성
  2. GET / 호출 시간 측정
  3. 200ms 이내 응답 검증
[ ] 테스트 실행 → 실패 확인
[ ] Commit: "test: 성능 검증 통합 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] 대용량 테스트 이미지 준비
[ ] 성능 측정 (System.nanoTime 또는 StopWatch)
[ ] Assertion (성능 기준 미달 시 실패)
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: 성능 검증 통합 테스트 통과"
```

---

## Tidy Phase: 테스트 유틸리티 정리 (전체 완료 후)

**Refactor Phase** - `struct:`
```
[ ] IntegrationTestHelper 생성
  - createFileAsset() - 테스트 FileAsset 생성
  - uploadToS3() - S3 업로드 헬퍼
  - waitForStatus() - 상태 변경 대기 (Awaitility)
  - cleanupTestData() - 테스트 후 정리
[ ] TestDataFactory 생성
  - createTestImage(int width, int height)
  - createTestHtml(List<String> imageUrls)
[ ] @BeforeEach / @AfterEach 공통화
[ ] 기존 테스트 리팩토링
[ ] Commit: "struct: Integration 테스트 유틸리티 정리"
```

---

## Test Data Files

### 이미지 파일 (resources/images/)
```
[ ] test-image-1000x800.jpg (가로 이미지, ~500KB)
[ ] test-image-600x1000.jpg (세로 이미지, ~500KB)
[ ] test-image-5mb.jpg (대용량 테스트, 5MB)
[ ] test-image-corrupted.jpg (손상된 이미지)
[ ] test-image-transparent.png (투명도 있는 PNG)
```

### HTML 파일 (resources/html/)
```
[ ] test-with-images.html (3개 img 태그)
[ ] test-with-inline-styles.html (background-image 포함)
[ ] test-with-external-images.html (외부 URL 이미지)
[ ] test-no-images.html (이미지 없음)
```

---

## Summary

| Phase | Cycles | Estimated Time |
|-------|--------|----------------|
| 인프라 설정 (1) | 1 | 20분 |
| CRUD 통합 (2) | 1 | 20분 |
| E2E - 이미지 (3) | 1 | 25분 |
| E2E - HTML (4) | 1 | 25분 |
| Outbox (5) | 1 | 20분 |
| StatusHistory (6) | 1 | 15분 |
| n8n 시뮬레이션 (7) | 1 | 20분 |
| 에러 시나리오 (8) | 1 | 20분 |
| 동시성 (9) | 1 | 15분 |
| 성능 (10) | 1 | 15분 |
| 유틸리티 정리 | 1 | 20분 |
| **Total** | **11** | **215분 (약 3.5시간)** |

---

## Dependencies (build.gradle)

```groovy
// TestContainers
testImplementation 'org.testcontainers:testcontainers:1.19.7'
testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
testImplementation 'org.testcontainers:mysql:1.19.7'
testImplementation 'org.testcontainers:localstack:1.19.7'

// Awaitility (비동기 테스트)
testImplementation 'org.awaitility:awaitility:4.2.0'

// WireMock (외부 API 모킹)
testImplementation 'org.wiremock:wiremock-standalone:3.4.2'
```

---

## Test Profiles

### application-integration-test.yml
```yaml
spring:
  datasource:
    url: # TestContainers에서 동적 설정
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

cloud:
  aws:
    s3:
      endpoint: # LocalStack에서 동적 설정
    sqs:
      endpoint: # LocalStack에서 동적 설정

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.testcontainers: INFO
```

---

## Checklist Before Starting

- [ ] Domain Layer Plan 완료 확인
- [ ] Application Layer Plan 완료 확인
- [ ] Persistence Layer Plan 완료 확인
- [ ] Infrastructure Layer Plan 완료 확인
- [ ] REST API Layer Plan 완료 확인
- [ ] 모든 레이어 구현 완료
- [ ] 테스트 이미지/HTML 파일 준비
- [ ] TestContainers Docker 환경 확인
- [ ] LocalStack 설정 확인
