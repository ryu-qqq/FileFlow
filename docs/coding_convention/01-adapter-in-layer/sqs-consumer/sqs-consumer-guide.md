# SQS Consumer Layer — **비동기 메시지 수신 & 안전한 처리**

> 이 문서는 `adapter-in/sqs-consumer` 레이어의 **설계 가이드**입니다.
>
> 현재 구현 분석, 핵심 원칙, 그리고 **안전한 SQS Consumer 작성을 위한 체크리스트**를 제공합니다.

---

## 1) 핵심 원칙 (한눈에)

* **멱등성 보장**: 동일 메시지가 2번 이상 수신될 수 있다. 비즈니스 로직이 이를 견뎌야 한다.
* **Visibility Timeout 연계**: Consumer의 처리 시간 제한과 큐의 visibility timeout을 반드시 일치시킨다.
* **조건부 활성화**: `@ConditionalOnProperty`로 bootstrap별 리스너를 선택적으로 활성화한다.
* **에러 분류**: 일시적 오류(재시도 가능)와 영구적 오류(즉시 DLQ)를 구분한다.
* **DLQ 소비**: DLQ에 쌓인 메시지를 방치하지 않는다. 알림 또는 재처리 로직을 구현한다.
* **Graceful Shutdown**: 처리 중인 메시지가 완료될 때까지 대기한다.

### 금지사항

* **Lombok 전면 금지**: 모든 어노테이션 사용 금지
* **@SqsListener 무조건 활성화 금지**: 반드시 `@ConditionalOnProperty`와 함께 사용
* **Consumer 내 비즈니스 로직 금지**: UseCase에 위임만 하고, 직접 DB/외부 API 호출하지 않는다
* **예외 삼키기 금지**: catch 후 로깅만 하고 예외를 삼키면 SQS가 ACK로 간주한다

---

## 2) 아키텍처 개요

```
                    ┌─────────────────────────────────────────┐
                    │            AWS SQS Queue                │
                    │  ┌─────────┐       ┌─────────┐         │
                    │  │  Main   │       │   DLQ   │         │
                    │  │  Queue  │──3x──→│  Queue  │         │
                    │  └────┬────┘       └────┬────┘         │
                    └───────┼─────────────────┼──────────────┘
                            │                 │
                 ReceiveMessage          ReceiveMessage
                            │                 │
┌───────────────────────────┼─────────────────┼──────────────────────┐
│  adapter-in/sqs-consumer  │                 │                      │
│                           ▼                 ▼                      │
│  ┌────────────────────────────┐  ┌──────────────────────────┐     │
│  │  @SqsListener Consumer    │  │  DLQ Consumer (미구현)    │     │
│  │  @ConditionalOnProperty   │  │  - 알림 발송              │     │
│  │                           │  │  - 원인 분석 로깅         │     │
│  │  1. 메시지 수신            │  │  - 수동 재처리 트리거     │     │
│  │  2. UseCase.execute()     │  │                           │     │
│  │  3. 성공 → ACK (자동)      │  └──────────────────────────┘     │
│  │  4. 실패 → throw (NACK)   │                                   │
│  └────────────┬───────────────┘                                   │
└───────────────┼───────────────────────────────────────────────────┘
                │
                ▼
┌──────────────────────────┐
│  application layer       │
│  UseCase.execute(id)     │
│  - 멱등성 보장 (상태 체크) │
│  - 분산 락 (필요 시)      │
└──────────────────────────┘
```

---

## 3) 패키징 구조

```
adapter-in/sqs-consumer/
├─ src/main/java/com/ryuqq/fileflow/adapter/in/sqs/
│  ├─ config/
│  │  └─ SqsConsumerProperties.java     ← 큐 이름 설정 (record)
│  │
│  ├─ [boundedContext]/                  ← 예: download, transform
│  │  └─ {Action}SqsConsumer.java       ← @SqsListener + @ConditionalOnProperty
│  │
│  └─ dlq/                              ← DLQ Consumer (향후)
│     └─ {Action}DlqConsumer.java
│
├─ src/main/resources/
│  ├─ sqs-consumer.yml                  ← 공통 설정
│  ├─ sqs-consumer-local.yml            ← LocalStack 설정
│  ├─ sqs-consumer-stage.yml            ← 스테이징 설정
│  └─ sqs-consumer-prod.yml             ← 운영 설정
│
└─ src/test/java/.../
   └─ [boundedContext]/
      └─ {Action}SqsConsumerTest.java   ← Mockito 단위 테스트
```

---

## 4) Consumer 작성 규칙

### 4-1. 필수 어노테이션

| 어노테이션 | 필수 | 설명 |
|-----------|------|------|
| `@Component` | O | Bean 등록 |
| `@ConditionalOnProperty` | O | bootstrap별 선택적 활성화 |
| `@SqsListener` | O | 큐 리스닝 (SpEL로 큐 이름 주입) |

### 4-2. 표준 Consumer 템플릿

```java
package com.ryuqq.fileflow.adapter.in.sqs.download;

import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.external-download-listener-enabled",
        havingValue = "true")
public class DownloadTaskSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DownloadTaskSqsConsumer.class);

    private final StartDownloadTaskUseCase startDownloadTaskUseCase;

    public DownloadTaskSqsConsumer(StartDownloadTaskUseCase startDownloadTaskUseCase) {
        this.startDownloadTaskUseCase = startDownloadTaskUseCase;
    }

    @SqsListener("${fileflow.sqs.download-queue}")
    public void consume(String downloadTaskId) {
        log.info("다운로드 작업 메시지 수신: downloadTaskId={}", downloadTaskId);

        try {
            startDownloadTaskUseCase.execute(downloadTaskId);
            log.info("다운로드 작업 시작 완료: downloadTaskId={}", downloadTaskId);
        } catch (Exception e) {
            log.error("다운로드 작업 처리 실패: downloadTaskId={}", downloadTaskId, e);
            throw e; // SQS 재시도 메커니즘에 위임
        }
    }
}
```

### 4-3. 핵심 패턴

**예외 재전파 (throw e)**

```
메시지 수신 → UseCase 실행 → 성공 → ACK (Spring Cloud AWS가 자동 처리)
                            → 실패 → throw → NACK → visibility timeout 후 재수신
                                                   → maxReceiveCount 초과 시 DLQ로 이동
```

- `throw e`: SQS 라이브러리가 NACK으로 처리하여 재시도 가능
- 예외를 삼키면 ACK로 간주되어 메시지가 유실됨

**조건부 활성화**

```yaml
# bootstrap-download-worker/application.yml
aws:
  sqs:
    listener:
      external-download-listener-enabled: true    # 이 워커에서 활성화
      file-processing-listener-enabled: false     # 이 워커에서 비활성화

# bootstrap-resizing-worker/application.yml
aws:
  sqs:
    listener:
      external-download-listener-enabled: false   # 이 워커에서 비활성화
      file-processing-listener-enabled: true      # 이 워커에서 활성화
```

> **교훈 (KAN-344)**: `@ConditionalOnProperty` 없이 `@SqsListener`만 사용하면,
> 해당 Consumer가 포함된 모든 bootstrap에서 리스닝을 시도한다.
> IAM 권한이 없는 큐에 접근하면 `SqsException: 403` 에러가 발생한다.

---

## 5) 설정 가이드

### 5-1. 환경별 설정 매트릭스

| 설정 | Local | Stage | Prod |
|------|-------|-------|------|
| **Endpoint** | `http://localhost:4566` (LocalStack) | AWS 기본 | AWS 기본 |
| **max-concurrent-messages** | 5 | 10 | 10 |
| **max-messages-per-poll** | 10 | 10 | 10 |
| **SQS 로그 레벨** | DEBUG | INFO | WARN |
| **큐 이름** | 고정값 | 환경변수 (기본값 있음) | 환경변수 (필수) |

### 5-2. 큐 이름 공유

`SqsConsumerProperties`와 `SqsPublisherProperties`는 동일한 prefix `fileflow.sqs`를 사용한다.

```yaml
# sqs-publish-stage.yml (Publisher)
fileflow:
  sqs:
    download-queue: ${SQS_DOWNLOAD_QUEUE:fileflow-stage-download-queue}
    transform-queue: ${SQS_TRANSFORM_QUEUE:fileflow-stage-transform-queue}
```

Consumer의 `@SqsListener("${fileflow.sqs.download-queue}")`가 동일한 프로퍼티를 참조하므로,
Publisher와 Consumer가 항상 같은 큐를 바라보게 된다.

---

## 6) Terraform (IAM) 연계

SQS Consumer가 정상 동작하려면 ECS Task Role에 적절한 IAM 권한이 필요하다.

### 6-1. Consumer (ReceiveMessage) 권한

```hcl
# 큐에서 메시지를 수신하는 워커에 필요
Action = [
  "sqs:ReceiveMessage",
  "sqs:DeleteMessage",
  "sqs:GetQueueAttributes",
  "sqs:GetQueueUrl",
  "sqs:ChangeMessageVisibility"
]
Resource = [queue_arn, "${queue_arn}-dlq"]
```

### 6-2. Publisher (SendMessage) 권한

```hcl
# 큐에 메시지를 발행하는 워커에 필요
Action = [
  "sqs:SendMessage",
  "sqs:GetQueueUrl",
  "sqs:GetQueueAttributes"
]
Resource = [target_queue_arn]
```

### 6-3. KMS 권한 (암호화된 큐)

```hcl
# SQS 큐가 KMS로 암호화된 경우 추가 필요
Action = [
  "kms:Decrypt",
  "kms:GenerateDataKey"
]
Resource = [sqs_kms_key_arn]
```

### 6-4. 권한 체크리스트

새로운 SQS Consumer/Publisher를 추가할 때 반드시 확인:

- [ ] Consumer 워커의 Task Role에 ReceiveMessage 권한 추가
- [ ] Publisher 워커의 Task Role에 SendMessage 권한 추가
- [ ] KMS 암호화 큐인 경우 Decrypt/GenerateDataKey 권한 추가
- [ ] DLQ에 대한 권한도 별도로 추가
- [ ] SSM Parameter로 큐 ARN을 참조하는 경우 data source 선언 확인

---

## 7) 테스트 가이드

### 7-1. 단위 테스트 (필수)

Mockito 기반으로 UseCase 호출을 검증한다. `@ConditionalOnProperty`는 Spring Context 없이 동작하므로 영향 없음.

```java
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadTaskSqsConsumer 단위 테스트")
class DownloadTaskSqsConsumerTest {

    @InjectMocks private DownloadTaskSqsConsumer sut;
    @Mock private StartDownloadTaskUseCase startDownloadTaskUseCase;

    @Test
    @DisplayName("유효한 ID를 수신하면 UseCase를 실행한다")
    void consume_ValidId_ExecutesUseCase() {
        String id = "download-task-001";
        sut.consume(id);
        then(startDownloadTaskUseCase).should().execute(id);
    }

    @Test
    @DisplayName("UseCase 실패 시 SQS 재시도를 위해 예외를 재전파한다")
    void consume_UseCaseThrows_RethrowsForSqsRetry() {
        String id = "download-task-002";
        willThrow(new RuntimeException("처리 실패"))
                .given(startDownloadTaskUseCase).execute(id);

        assertThatThrownBy(() -> sut.consume(id))
                .isInstanceOf(RuntimeException.class);
    }
}
```

### 7-2. 검증 항목

| 항목 | 검증 내용 |
|------|----------|
| 정상 처리 | UseCase가 정확히 1번 호출되는가 |
| 예외 재전파 | RuntimeException이 재전파되는가 (SQS NACK을 위해) |
| 인자 전달 | 메시지 ID가 UseCase에 정확히 전달되는가 |

---

## 8) Observability

### 8-1. 로깅 전략

| 시점 | 레벨 | 메시지 |
|------|------|--------|
| 메시지 수신 | INFO | `"다운로드 작업 메시지 수신: downloadTaskId={}"` |
| 처리 완료 | INFO | `"다운로드 작업 시작 완료: downloadTaskId={}"` |
| 처리 실패 | ERROR | `"다운로드 작업 처리 실패: downloadTaskId={}"` + 스택트레이스 |

### 8-2. 에러 추적 (Sentry)

- Local/Stage: DSN 비활성화 (`dsn: ""`)
- Prod: 활성화 (`dsn: ${SENTRY_DSN}`)
- 최소 이벤트 레벨: `error`
- 최소 breadcrumb 레벨: `info`

### 8-3. 메트릭 (Prometheus)

Spring Cloud AWS SQS는 Micrometer를 통해 자동으로 메트릭을 수집한다:
- `spring.cloud.aws.sqs.listener.*` 관련 메트릭
- JVM, 프로세스, DB 연결 풀 메트릭 (Actuator 기반)

---

## 9) Graceful Shutdown

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: ${SHUTDOWN_TIMEOUT:30s}

management:
  server:
    shutdown: graceful
```

**종료 흐름:**

```
SIGTERM 수신
  → 새로운 메시지 폴링 중단
  → 처리 중인 메시지 완료 대기 (최대 30초)
  → 30초 초과 시 강제 종료
  → 미완료 메시지는 visibility timeout 후 큐에 다시 노출
  → 다른 워커가 재처리
```
