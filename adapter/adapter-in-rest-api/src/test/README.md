# FileFlow REST API Adapter - 테스트 가이드

이 문서는 `adapter-in-rest-api` 모듈의 테스트 실행 및 구성 방법을 안내합니다.

## 📋 목차

- [테스트 구조](#-테스트-구조)
- [단위 테스트 실행](#-단위-테스트-실행)
- [통합 테스트 실행](#-통합-테스트-실행)
- [테스트 데이터 관리](#-테스트-데이터-관리)
- [테스트 커버리지](#-테스트-커버리지)
- [알려진 제한사항](#-알려진-제한사항)

---

## 🏗️ 테스트 구조

```
src/test/java/
├── com.ryuqq.fileflow.adapter.rest/
│   ├── controller/              # REST 컨트롤러 단위 테스트
│   │   ├── UploadSessionControllerTest.java   (✅ 9 tests)
│   │   └── PolicyControllerTest.java
│   ├── integration/             # E2E 통합 테스트
│   │   ├── UploadSessionIntegrationTest.java  (⏳ 4 tests)
│   │   ├── UploadSessionExceptionIntegrationTest.java  (⏳ 12 tests)
│   │   └── UploadSessionPerformanceTest.java  (⏳ 6 tests)
│   ├── exception/               # 예외 핸들러 테스트
│   │   ├── GlobalExceptionHandlerTest.java
│   │   └── MissingHeaderExceptionTest.java
│   └── interceptor/             # 인터셉터 테스트
│       └── PolicyMatchingInterceptorTest.java
└── resources/
    └── sql/                     # 테스트 데이터 스크립트
        ├── cleanup-upload-test-data.sql
        └── insert-upload-test-data.sql
```

**범례:**
- ✅ = 실행 완료 (Pass)
- ⏳ = 작성 완료 (전체 애플리케이션 컨텍스트 필요)

---

## ✅ 단위 테스트 실행

### 전제 조건
- Java 21+
- Gradle 8.x+

### 실행 방법

**특정 컨트롤러 테스트 실행:**
```bash
./gradlew :adapter:adapter-in-rest-api:test --tests "UploadSessionControllerTest"
```

**모든 단위 테스트 실행 (컨트롤러, 예외, 인터셉터):**
```bash
./gradlew :adapter:adapter-in-rest-api:test
```

### 테스트 결과 확인

**HTML 리포트:**
```bash
open adapter/adapter-in-rest-api/build/reports/tests/test/index.html
```

**JaCoCo 커버리지 리포트:**
```bash
open adapter/adapter-in-rest-api/build/reports/jacoco/test/html/index.html
```

### UploadSessionControllerTest 상세

이 테스트는 `@WebMvcTest`를 사용하여 컨트롤러 계층만 격리하여 테스트합니다.

**테스트 케이스 (총 9개):**
1. ✅ `createUploadSession_Success()` - 정상 세션 생성
2. ✅ `createUploadSession_IdempotencyKey_ReturnsExistingSession()` - 멱등성 키 검증
3. ✅ `createUploadSession_PolicyNotFound()` - 존재하지 않는 정책
4. ✅ `createUploadSession_PolicyViolation()` - 정책 위반 (파일 크기)
5. ✅ `createUploadSession_ValidationFailed_EmptyFileName()` - Validation: 빈 파일명
6. ✅ `createUploadSession_ValidationFailed_NegativeFileSize()` - Validation: 음수 파일 크기
7. ✅ `createUploadSession_ValidationFailed_InvalidExpirationMinutes()` - Validation: 만료 시간 0
8. ✅ `createUploadSession_ValidationFailed_EmptyUploaderId()` - Validation: 빈 업로더 ID
9. ✅ `createUploadSession_DefaultExpirationMinutes()` - 기본 만료 시간 사용

**실행 시간:** ~0.4초
**상태:** 전체 통과 (9/9)

---

## 🔄 통합 테스트 실행

### 전제 조건

**1. Docker 및 Testcontainers 환경**
```bash
# Docker가 실행 중이어야 합니다
docker --version

# Testcontainers는 LocalStack 이미지를 자동으로 pull합니다
# - localstack/localstack:3.0.2
```

**2. 전체 Spring Boot 애플리케이션 컨텍스트**

통합 테스트는 `@SpringBootTest`를 사용하여 전체 애플리케이션 컨텍스트를 로드합니다.
**필수 Bean 의존성:**
- `CreateUploadSessionUseCase` (application 계층)
- `GetUploadPolicyUseCase` (application 계층)
- `S3Service` (adapter-out-aws-s3)
- JPA Repositories (adapter-out-persistence-jpa)

**⚠️ 현재 제한사항:**
- 통합 테스트는 `adapter-in-rest-api` 모듈 단독으로 실행할 수 없습니다
- 전체 애플리케이션 부트스트랩 (예: `bootstrap` 또는 `application` 모듈)이 필요합니다

### 실행 방법

**애플리케이션 루트에서 통합 테스트 실행:**
```bash
# 애플리케이션 전체 테스트 (통합 테스트 포함)
./gradlew clean test --tests "*IntegrationTest"

# 또는 특정 통합 테스트 클래스 실행
./gradlew test --tests "UploadSessionIntegrationTest"
```

### 통합 테스트 상세

#### 1. UploadSessionIntegrationTest (E2E 정상 플로우)

**테스트 환경:**
- LocalStack S3/SQS 컨테이너 사용
- `@ActiveProfiles("integration-test")`
- `@Sql` 스크립트로 테스트 데이터 자동 로드

**테스트 케이스 (총 4개):**
1. ⏳ `endToEndUploadFlow_Success()` - 전체 업로드 플로우 (Presigned URL → S3 업로드)
2. ⏳ `endToEndUploadFlow_IdempotencyKey_PreventsDuplicates()` - 멱등성 키 중복 방지
3. ⏳ `endToEndUploadFlow_LargeFile_Success()` - 대용량 파일 (20MB)
4. ⏳ `endToEndUploadFlow_MultipleContentTypes_Success()` - 다양한 Content-Type (JPEG, PNG, PDF)

#### 2. UploadSessionExceptionIntegrationTest (예외 시나리오)

**테스트 케이스 (총 12개):**
1. ⏳ `createUploadSession_FileSizeExceeded_PolicyViolation()` - 파일 크기 초과
2. ⏳ `createUploadSession_UnsupportedFormat_PolicyViolation()` - 허용되지 않은 포맷
3. ⏳ `createUploadSession_PolicyNotFound()` - 존재하지 않는 정책
4. ⏳ `createUploadSession_EmptyFileName_ValidationFailed()` - 빈 파일명
5. ⏳ `createUploadSession_NegativeFileSize_ValidationFailed()` - 음수 파일 크기
6. ⏳ `createUploadSession_ZeroExpirationMinutes_ValidationFailed()` - 0 이하 만료 시간
7. ⏳ `createUploadSession_EmptyUploaderId_ValidationFailed()` - 빈 업로더 ID
8. ⏳ `createUploadSession_EmptyRequestBody_ValidationFailed()` - 빈 Request Body
9. ⏳ `createUploadSession_MalformedJson_BadRequest()` - 잘못된 JSON
10. ⏳ `createUploadSession_ExactMaxFileSize_Success()` - 경계값 테스트 (정확히 10MB)
11. ⏳ `createUploadSession_ExceedMaxFileSizeByOneByte_PolicyViolation()` - 경계값 테스트 (10MB + 1 byte)
12. ⏳ (추가 예외 시나리오 - Rate Limiting, 이미지 해상도 등은 Skip)

#### 3. UploadSessionPerformanceTest (성능 테스트)

**테스트 케이스 (총 6개):**
1. ⏳ `concurrentUploads_10Parallel_AllSuccess()` - 동시 업로드 10개 (5초 이내)
2. ⏳ `largeFileUpload_50MB_ResponseWithin3Seconds()` - 대용량 파일 처리 (3초 이내)
3. ⏳ `sequentialRequests_100Requests_AverageResponseTimeUnder500ms()` - 연속 요청 100개 (평균 500ms 이하)
4. ⏳ `concurrentIdempotencyRequests_NoDuplicates_PerformanceVerified()` - 멱등성 동시 요청 (10초 이내, 중복 방지)
5. ⏳ `presignedUrlGeneration_SingleRequest_Under200ms()` - 단일 요청 (200ms 이하)
6. ⏳ (추가 성능 시나리오)

**성능 SLA:**
- 단일 Presigned URL 발급: ≤ 200ms
- 대용량 파일 (50MB): ≤ 3초
- 동시 업로드 10개: ≤ 5초
- 연속 요청 평균 응답 시간: ≤ 500ms

**⚠️ 참고:**
성능 SLA는 CI/CD 환경, 네트워크 상태, 하드웨어 성능에 따라 조정이 필요할 수 있습니다.

---

## 📊 테스트 데이터 관리

### SQL 스크립트 구조

테스트 데이터는 `@Sql` 어노테이션으로 자동 로드됩니다:

```java
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
```

### 1. cleanup-upload-test-data.sql

테스트 실행 전 기존 데이터를 정리합니다:

```sql
DELETE FROM upload_session WHERE uploader_id LIKE 'user-%' OR uploader_id LIKE 'seller-%';
DELETE FROM upload_policy WHERE policy_key IN ('b2c:CONSUMER:REVIEW', 'b2c:SELLER:PRODUCT');
```

### 2. insert-upload-test-data.sql

테스트에 필요한 정책 데이터를 삽입합니다:

**b2c:CONSUMER:REVIEW 정책:**
- 최대 파일 크기: 10MB
- 허용 포맷: image/jpeg, image/png, image/webp
- 최대 이미지 해상도: 2048x2048
- 시간당 요청 제한: 100회
- 최대 파일 개수: 5개

**b2c:SELLER:PRODUCT 정책:**
- 최대 파일 크기: 50MB
- 허용 포맷: image/jpeg, image/png, image/webp, application/pdf
- 최대 이미지 해상도: 4096x4096
- 시간당 요청 제한: 500회
- 최대 파일 개수: 20개

### 데이터 격리

각 테스트 메서드는 독립적으로 실행되며, `@Sql` 스크립트가 매번 실행되어 데이터 격리를 보장합니다.

---

## 📈 테스트 커버리지

### 현재 커버리지 (단위 테스트만 실행 시)

```
Total Coverage: 33%
├── controller:        16% (UploadSessionController만 테스트됨)
├── dto.request:       23%
├── dto.response:      59%
├── exception:         38%
└── interceptor:        0% (통합 테스트 필요)
```

### 목표 커버리지

- **Adapter 전체:** ≥ 70%
- **개별 클래스:** ≥ 50%

### 커버리지 부족 원인

**PolicyMatchingInterceptor (0% coverage):**
- 인터셉터는 전체 Spring MVC 파이프라인에서 동작
- `@WebMvcTest`는 인터셉터를 자동 등록하지 않음
- 통합 테스트 (`@SpringBootTest`)에서 검증 필요

**통합 테스트 미실행:**
- 통합 테스트는 전체 애플리케이션 컨텍스트가 필요
- `adapter-in-rest-api` 모듈 단독으로는 실행 불가
- 애플리케이션 부트스트랩 모듈에서 실행 시 커버리지 70% 달성 예상

---

## ⚠️ 알려진 제한사항

### 1. 통합 테스트 실행 제약

**문제:**
통합 테스트 (`*IntegrationTest`, `*PerformanceTest`)는 `@SpringBootTest`를 사용하여 전체 애플리케이션 컨텍스트를 로드합니다.

**필요 Bean:**
- `CreateUploadSessionUseCase` (application 계층)
- `GetUploadPolicyUseCase` (application 계층)
- S3 관련 Bean (adapter-out-aws-s3)
- JPA 관련 Bean (adapter-out-persistence-jpa)

**현재 상태:**
`adapter-in-rest-api` 모듈은 **Hexagonal Architecture**를 따라 application 계층 및 다른 adapter를 **직접 의존하지 않습니다** (`implementation` 의존성 없음).

테스트 의존성만 추가된 상태:
```kotlin
// build.gradle.kts
testImplementation(project(":adapter:adapter-out-persistence-jpa"))
testImplementation(project(":adapter:adapter-out-aws-s3"))
```

**해결 방법:**

**Option 1. 애플리케이션 루트에서 실행 (권장)**
```bash
# 전체 애플리케이션 컨텍스트로 통합 테스트 실행
./gradlew clean test --tests "*IntegrationTest"
```

**Option 2. Bootstrap/Application 모듈에서 실행**
- 애플리케이션 부트스트랩 모듈이 모든 adapter와 application을 의존
- 해당 모듈에서 통합 테스트 실행 시 정상 작동

**Option 3. 통합 테스트 전용 TestConfiguration 생성 (미래 개선 사항)**
- `src/test/java/com/ryuqq/fileflow/adapter/rest/FullTestConfiguration.java` 생성
- `@ComponentScan`으로 모든 필요 Bean 로드
- 통합 테스트 전용 설정 분리

### 2. LocalStack 환경 의존성

**Docker 필수:**
통합 테스트는 Testcontainers를 통해 LocalStack을 실행하므로 Docker가 필수입니다.

```bash
# Docker가 실행 중인지 확인
docker ps
```

**네트워크 이슈:**
CI/CD 환경에서 Docker 네트워크 설정이 필요할 수 있습니다.

### 3. 성능 테스트 SLA 조정 필요

성능 테스트의 시간 제약 (예: 200ms, 500ms)은 로컬 개발 환경 기준입니다.
**CI/CD 환경에서는 SLA 조정이 필요할 수 있습니다:**

```java
// 로컬 환경
assertThat(duration.toMillis()).isLessThanOrEqualTo(200);

// CI/CD 환경 (예시)
assertThat(duration.toMillis()).isLessThanOrEqualTo(500);
```

---

## 🔧 트러블슈팅

### 문제 1: ApplicationContext Loading 실패

**에러:**
```
java.lang.IllegalStateException: Failed to load ApplicationContext
Caused by: NoSuchBeanDefinitionException: No qualifying bean of type 'CreateUploadSessionUseCase'
```

**원인:**
단위 테스트 환경에서 통합 테스트를 실행하려고 시도

**해결:**
애플리케이션 루트 또는 부트스트랩 모듈에서 통합 테스트 실행

### 문제 2: Docker 연결 실패

**에러:**
```
Could not find a valid Docker environment
```

**원인:**
Docker가 실행 중이지 않거나 접근 권한 문제

**해결:**
```bash
# Docker 시작
docker info

# 권한 확인 (Linux)
sudo usermod -aG docker $USER
```

### 문제 3: LocalStack 이미지 Pull 실패

**에러:**
```
Unable to pull image: localstack/localstack:3.0.2
```

**원인:**
네트워크 연결 문제 또는 Docker Hub 접근 제한

**해결:**
```bash
# 수동으로 이미지 Pull
docker pull localstack/localstack:3.0.2

# 또는 다른 버전 사용
# @Container 어노테이션에서 버전 변경
DockerImageName.parse("localstack/localstack:latest")
```

---

## 📚 참고 자료

- [Spring Boot Testing 공식 문서](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Testcontainers LocalStack 모듈](https://java.testcontainers.org/modules/localstack/)
- [JaCoCo Gradle Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [MockMvc 테스트 가이드](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)

---

## ✅ 체크리스트

테스트 실행 전 확인사항:

- [ ] Java 21+ 설치 확인
- [ ] Docker 실행 확인 (`docker ps`)
- [ ] Gradle 빌드 성공 (`./gradlew clean build`)
- [ ] 단위 테스트 먼저 실행 (`./gradlew :adapter:adapter-in-rest-api:test`)
- [ ] 통합 테스트는 애플리케이션 루트에서 실행
- [ ] 테스트 결과 리포트 확인 (`build/reports/tests/test/index.html`)
- [ ] JaCoCo 커버리지 리포트 확인 (`build/reports/jacoco/test/html/index.html`)

---

**작성자:** sangwon-ryu
**최종 수정일:** 2025-10-10
**버전:** 1.0.0
