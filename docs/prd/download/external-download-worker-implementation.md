# ExternalDownload Worker 구현 계획

## 개요

ExternalDownload SQS 메시지를 처리하는 Worker 구현 계획서.
분산락 기반 중복 방지 + HTTP 다운로드 + S3 업로드 처리.

---

## 아키텍처

```
[SQS: external-download-queue]
         │
         ▼
[ExternalDownloadSqsListener] ──분산락──▶ [Redis Lock (Redisson)]
         │                                    │
         │ 락 획득 성공                         │ 락 획득 실패
         ▼                                    ▼
[ExecuteExternalDownloadUseCase]          ACK (skip)
         │
         ├─▶ [HttpDownloadPort] ──▶ HTTP GET (외부 URL)
         │
         └─▶ [S3StoragePort] ──▶ S3 PUT
         │
         ▼
    성공: ACK + 상태 COMPLETED
    실패 (3회): DLQ
         │
         ▼
[ExternalDownloadDlqListener]
         │
         ▼
    Outbox FAILED 마킹
```

---

## 작업 목록

### 0. Domain & Persistence Layer (S3 경로 정보 저장)

> **핵심 개념**: API 요청 시 `UserContextHolder`에서 S3 정보를 추출하여 `ExternalDownload`에 저장.
> Worker에서는 저장된 정보로 S3 경로 생성 (세션과 동일한 패턴).

#### 0.1 Domain Layer

- [ ] `ExternalDownload.java` 수정 - S3 정보 필드 추가
  - 위치: `domain/src/main/java/com/ryuqq/fileflow/domain/download/aggregate/`
  ```java
  // 추가할 필드
  private final S3Bucket s3Bucket;        // 업로드 대상 버킷
  private final String s3PathPrefix;      // "admin/", "seller-123/", "customer/"
  ```

- [ ] `ExternalDownload.forNew()` 수정
  ```java
  public static ExternalDownload forNew(
          SourceUrl sourceUrl,
          long tenantId,
          long organizationId,
          S3Bucket s3Bucket,        // 추가
          String s3PathPrefix,      // 추가
          WebhookUrl webhookUrl,
          Clock clock) {
      // ...
  }
  ```

- [ ] `ExternalDownload.of()` 수정 (재구성용)
  ```java
  public static ExternalDownload of(
          ExternalDownloadId id,
          SourceUrl sourceUrl,
          long tenantId,
          long organizationId,
          S3Bucket s3Bucket,        // 추가
          String s3PathPrefix,      // 추가
          // ... 나머지 파라미터
  ) {
      // ...
  }
  ```

- [ ] Getter 추가
  ```java
  public S3Bucket getS3Bucket() { return s3Bucket; }
  public String getS3PathPrefix() { return s3PathPrefix; }
  public String getS3BucketValue() { return s3Bucket.bucketName(); }
  ```

#### 0.2 Application Layer - Assembler 수정

- [ ] `ExternalDownloadAssembler.java` 수정
  - 위치: `application/src/main/java/com/ryuqq/fileflow/application/download/assembler/`
  ```java
  @Component
  public class ExternalDownloadAssembler {

      private final Supplier<UserContext> userContextSupplier;

      public ExternalDownloadAssembler(Supplier<UserContext> userContextSupplier) {
          this.userContextSupplier = userContextSupplier;
      }

      public ExternalDownload toDomain(RegisterExternalDownloadCommand command, Clock clock) {
          UserContext userContext = userContextSupplier.get();

          return ExternalDownload.forNew(
              SourceUrl.of(command.sourceUrl()),
              userContext.tenant().id(),
              userContext.getOrganizationId(),
              userContext.getS3Bucket(),                        // S3 버킷
              userContext.organization().getS3PathPrefix(),     // S3 경로 prefix
              command.webhookUrl() != null ? WebhookUrl.of(command.webhookUrl()) : null,
              clock
          );
      }
  }
  ```

#### 0.3 Persistence Layer - Entity & Mapper 수정

- [ ] Flyway 마이그레이션 추가
  - 위치: `adapter-out/persistence-mysql/src/main/resources/db/migration/`
  ```sql
  -- V{N}__add_s3_info_to_external_download.sql
  ALTER TABLE external_download
      ADD COLUMN s3_bucket VARCHAR(255) NOT NULL AFTER organization_id,
      ADD COLUMN s3_path_prefix VARCHAR(255) NOT NULL AFTER s3_bucket;
  ```

- [ ] `ExternalDownloadJpaEntity.java` 수정
  ```java
  @Column(name = "s3_bucket", nullable = false)
  private String s3Bucket;

  @Column(name = "s3_path_prefix", nullable = false)
  private String s3PathPrefix;
  ```

- [ ] `ExternalDownloadEntityMapper.java` 수정
  - `toDomain()`: Entity → Domain 매핑에 S3 정보 추가
  - `toEntity()`: Domain → Entity 매핑에 S3 정보 추가

#### 0.4 S3ClientPort 확장 (직접 업로드용)

- [ ] `S3ClientPort.java` 수정 - `putObject()` 추가
  - 위치: `application/src/main/java/com/ryuqq/fileflow/application/session/port/out/client/`
  ```java
  /**
   * 파일을 S3에 직접 업로드합니다.
   *
   * @param bucket S3 버킷
   * @param s3Key S3 객체 키
   * @param contentType Content-Type
   * @param content 파일 내용
   * @return 업로드된 파일의 ETag
   */
  ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] content);
  ```

- [ ] `S3ClientAdapter.java` 수정 - `putObject()` 구현
  - 위치: `adapter-out/aws-s3/src/main/java/com/ryuqq/fileflow/adapter/out/aws/s3/adapter/`
  ```java
  @Override
  public ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] content) {
      PutObjectRequest request = PutObjectRequest.builder()
              .bucket(bucket.bucketName())
              .key(s3Key.key())
              .contentType(contentType.type())
              .contentLength((long) content.length)
              .build();

      PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(content));
      String etag = response.eTag().replace("\"", "");
      return ETag.of(etag);
  }
  ```

---

### 1. Application Layer

#### 1.1 Lock 관련 수정

- [ ] `DistributedLockPort.java` 생성
  - 위치: `application/src/main/java/com/ryuqq/fileflow/application/common/port/out/lock/`
  ```java
  public interface DistributedLockPort {
      <T> T executeWithLock(String key, long waitTime, long leaseTime,
                            TimeUnit unit, Supplier<T> action);
      boolean executeWithLock(String key, long waitTime, long leaseTime,
                              TimeUnit unit, Runnable action);
      boolean isLocked(String key);
  }
  ```

- [ ] `LockType.java` 수정 - EXTERNAL_DOWNLOAD 추가
  - 위치: `application/src/main/java/com/ryuqq/fileflow/application/common/lock/`
  ```java
  EXTERNAL_DOWNLOAD("external-download:", 0L, 300000L);
  // 락 키: external-download:{externalDownloadId}
  // 대기: 0ms (즉시 반환)
  // 유지: 300초 (5분)
  ```

- [ ] `DistributedLockExecutor.java` import 수정
  ```java
  // 변경 전
  import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
  // 변경 후
  import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
  ```

#### 1.2 UseCase 생성

- [ ] `ExecuteExternalDownloadUseCase.java` (port/in/command)
  ```java
  public interface ExecuteExternalDownloadUseCase {
      void execute(ExecuteExternalDownloadCommand command);
  }
  ```

- [ ] `ExecuteExternalDownloadCommand.java` (dto/command)
  ```java
  public record ExecuteExternalDownloadCommand(
      Long externalDownloadId,
      String sourceUrl,
      Long tenantId,
      Long organizationId
  ) {}
  ```

- [ ] `ExecuteExternalDownloadService.java` (service)
  - HTTP 다운로드 실행
  - S3 업로드
  - ExternalDownload 상태 COMPLETED 변경

#### 1.3 HttpDownload 포트

- [ ] `HttpDownloadPort.java` (port/out/client)
  ```java
  public interface HttpDownloadPort {
      DownloadResult download(String sourceUrl);
  }
  ```

- [ ] `DownloadResult.java` (dto)
  ```java
  public record DownloadResult(
      byte[] content,
      String contentType,
      long contentLength
  ) {}
  ```

---

### 2. Adapter-out/http-client

- [ ] `build.gradle` 생성
  ```groovy
  plugins {
      id 'java-library'
  }

  dependencies {
      api project(':application')
      implementation 'org.springframework.boot:spring-boot-starter-webflux'
  }
  ```

- [ ] `HttpDownloadAdapter.java` 구현
  - WebClient 사용
  - HTTP GET → byte[] 반환
  - Content-Type, Content-Length 추출

- [ ] `HttpClientConfig.java`
  - WebClient Bean 설정
  - 타임아웃 설정

---

### 3. Adapter-out/persistence-redis (분산락)

- [ ] `build.gradle` 수정 - Redisson 추가
  ```groovy
  implementation libs.redisson
  ```

- [ ] `RedissonConfig.java` 생성
  ```java
  @Configuration
  public class RedissonConfig {
      @Bean
      public RedissonClient redissonClient() {
          Config config = new Config();
          config.useSingleServer()
                .setAddress("redis://${host}:${port}");
          return Redisson.create(config);
      }
  }
  ```

- [ ] `DistributedLockAdapter.java` 구현
  ```java
  @Component
  public class DistributedLockAdapter implements DistributedLockPort {
      private final RedissonClient redissonClient;

      @Override
      public <T> T executeWithLock(String key, long waitTime, long leaseTime,
                                    TimeUnit unit, Supplier<T> action) {
          RLock lock = redissonClient.getLock(key);
          try {
              if (lock.tryLock(waitTime, leaseTime, unit)) {
                  try {
                      return action.get();
                  } finally {
                      if (lock.isHeldByCurrentThread()) {
                          lock.unlock();
                      }
                  }
              }
              return null;
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return null;
          }
      }
  }
  ```

---

### 4. Adapter-in/sqs-listener

#### 4.1 설정 파일 수정

- [ ] `sqs-listener.yml` 수정
  ```yaml
  aws:
    sqs:
      listener:
        external-download-queue-url: ${SQS_EXTERNAL_DOWNLOAD_QUEUE_URL:}
        external-download-dlq-url: ${SQS_EXTERNAL_DOWNLOAD_DLQ_URL:}
        external-download-listener-enabled: true
        external-download-dlq-listener-enabled: true

  logging:
    level:
      com.ryuqq.fileflow.adapter.in.sqs: INFO
  ```

- [ ] `sqs-listener-local.yml` 수정
  ```yaml
  aws:
    sqs:
      listener:
        external-download-queue-url: http://localhost:4566/000000000000/external-download-queue
        external-download-dlq-url: http://localhost:4566/000000000000/external-download-dlq
  ```

- [ ] `SqsListenerProperties.java` 수정
  ```java
  private String externalDownloadQueueUrl;
  private String externalDownloadDlqUrl;
  private boolean externalDownloadListenerEnabled = true;
  private boolean externalDownloadDlqListenerEnabled = true;
  // getter/setter
  ```

#### 4.2 Listener 클래스

- [ ] `CrawlTaskSqsListener.java` 삭제

- [ ] `CrawlTaskDlqListener.java` 삭제

- [ ] `ExternalDownloadSqsListener.java` 생성
  ```java
  @Component
  @ConditionalOnProperty(
      name = "aws.sqs.listener.external-download-listener-enabled",
      havingValue = "true",
      matchIfMissing = true)
  public class ExternalDownloadSqsListener {

      private final DistributedLockExecutor lockExecutor;
      private final ExecuteExternalDownloadUseCase downloadUseCase;

      @SqsListener(
          value = "${aws.sqs.listener.external-download-queue-url}",
          acknowledgementMode = "MANUAL")
      public void handleMessage(
          @Payload ExternalDownloadMessage payload,
          Acknowledgement acknowledgement) {

          Long downloadId = payload.externalDownloadId();

          boolean executed = lockExecutor.tryExecuteWithLock(
              LockType.EXTERNAL_DOWNLOAD,
              downloadId,
              () -> executeDownload(payload)
          );

          if (!executed) {
              acknowledgement.acknowledge();
              log.info("Download skip (다른 워커 처리 중): id={}", downloadId);
              return;
          }

          acknowledgement.acknowledge();
      }

      private void executeDownload(ExternalDownloadMessage payload) {
          ExecuteExternalDownloadCommand command = new ExecuteExternalDownloadCommand(
              payload.externalDownloadId(),
              payload.sourceUrl(),
              payload.tenantId(),
              payload.organizationId()
          );
          downloadUseCase.execute(command);
      }
  }
  ```

- [ ] `ExternalDownloadDlqListener.java` 생성
  ```java
  @Component
  @ConditionalOnProperty(
      name = "aws.sqs.listener.external-download-dlq-listener-enabled",
      havingValue = "true",
      matchIfMissing = true)
  public class ExternalDownloadDlqListener {

      private final ExternalDownloadOutboxQueryPort outboxQueryPort;
      private final ExternalDownloadOutboxManager outboxManager;

      @SqsListener(
          value = "${aws.sqs.listener.external-download-dlq-url}",
          acknowledgementMode = "MANUAL")
      public void handleMessage(
          @Payload ExternalDownloadMessage payload,
          Acknowledgement acknowledgement) {

          // Outbox 조회 후 FAILED 마킹
          markOutboxAsFailed(payload.externalDownloadId());
          acknowledgement.acknowledge();
      }
  }
  ```

---

### 5. Bootstrap/bootstrap-download-worker

- [ ] 디렉토리 구조 생성
  ```
  bootstrap/bootstrap-download-worker/
  ├── build.gradle
  └── src/
      ├── main/
      │   ├── java/.../FileFlowDownloadWorkerApplication.java
      │   └── resources/
      │       └── application.yml
      └── test/
          ├── java/.../DownloadWorkerApplicationContextTest.java
          └── resources/
              └── application-test.yml
  ```

- [ ] `build.gradle`
  ```groovy
  dependencies {
      implementation project(':application')
      implementation project(':adapter-in:sqs-listener')
      implementation project(':adapter-out:persistence-mysql')
      implementation project(':adapter-out:persistence-redis')
      implementation project(':adapter-out:aws-s3')
      implementation project(':adapter-out:http-client')

      implementation libs.spring.cloud.aws.sqs
  }
  ```

- [ ] `FileFlowDownloadWorkerApplication.java`
  ```java
  @SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
  public class FileFlowDownloadWorkerApplication {
      public static void main(String[] args) {
          SpringApplication.run(FileFlowDownloadWorkerApplication.class, args);
      }
  }
  ```

- [ ] `application.yml`
  ```yaml
  spring:
    application:
      name: fileflow-download-worker
    config:
      import:
        - classpath:persistence.yml
        - classpath:redis.yml
        - classpath:s3.yml
        - classpath:sqs-listener.yml

  server:
    port: ${SERVER_PORT:8082}
  ```

---

### 6. Terraform SQS

- [ ] `terraform/sqs/provider.tf` 수정
  ```hcl
  # backend key 변경
  key = "fileflow/sqs/terraform.tfstate"

  # project_name 변경
  variable "project_name" {
    default = "fileflow"
  }
  ```

- [ ] `terraform/sqs/main.tf` 수정
  - crawling_task_queue → external_download_queue
  - eventbridge_trigger_queue 삭제
  - visibility_timeout_seconds = 360 (6분)
  - SSM Parameter 경로 변경: /fileflow/sqs/*

- [ ] `terraform/sqs/outputs.tf` 수정
  - external_download_queue_* outputs

---

## 시간 설정

| 구성 요소 | 값 | 설명 |
|----------|-----|------|
| Lock WaitTime | 0ms | 즉시 반환 (대기 안 함) |
| Lock LeaseTime | 300초 (5분) | 다운로드 최대 시간 |
| SQS Visibility Timeout | 360초 (6분) | LeaseTime + 1분 |
| SQS maxReceiveCount | 3 | 3회 실패 후 DLQ |
| DLQ Retention | 14일 | 실패 메시지 보관 |

**Visibility Timeout > Lock LeaseTime 이유**:
- 락이 해제되어도 메시지가 다시 보이지 않도록
- 원래 워커가 ACK 전송할 시간 확보
- 중복 처리 방지

---

## 작업 순서

1. **Domain Layer** (ExternalDownload S3 필드 추가)
2. **Persistence Layer** (Entity, Mapper, Migration)
3. **Application Layer - Assembler** (UserContext → S3 정보 추출)
4. **Application Layer - S3ClientPort** (putObject 추가)
5. **Adapter-out/aws-s3** (S3ClientAdapter putObject 구현)
6. **Application Layer - Lock** (Port/UseCase)
7. **Adapter-out/persistence-redis** (분산락)
8. **Adapter-out/http-client** (HTTP 다운로드)
9. **Adapter-in/sqs-listener** (Listener)
10. **Bootstrap/bootstrap-download-worker** (부트스트랩)
11. **Terraform SQS** (인프라)
12. **통합 테스트**

---

## 참고

- ExternalDownloadMessage: `application/src/main/java/.../download/dto/ExternalDownloadMessage.java`
- SqsPublishPort: `application/src/main/java/.../download/port/out/client/SqsPublishPort.java`
- 기존 스케줄러 패턴: `SingleUploadSessionExpirationScheduler.java`
