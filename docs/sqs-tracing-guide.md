# SQS 메시지 traceId 전파 가이드

## 개요

Transactional Outbox 패턴에서 Scheduler → SQS → Consumer 간 traceId를 전파하여
분산 환경에서 요청 추적을 가능하게 합니다.

## traceId 흐름

```
Scheduler (SchedulerLoggingAspect)
  │ MDC.put("traceId", "scheduler-{UUID 8자리}")
  ↓
Outbox Polling
  ↓
SQS Publisher
  │ MDC.get("traceId") → SQS message header에 포함
  ↓
SQS Queue (message attribute: traceId)
  ↓
SQS Consumer
  │ @Header("traceId") → MDC.put("traceId", ...)
  ↓
UseCase 실행 (모든 로그에 traceId 포함)
  ↓
finally: MDC.remove("traceId")
```

## Publisher 구현

### Spring Cloud AWS SQS v3 `SqsTemplate.send()` Header API

```java
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.MDC;

public void enqueue(String taskId) {
    String queueName = properties.downloadQueue();
    String traceId = MDC.get("traceId");

    sqsTemplate.send(to -> to
            .queue(queueName)
            .payload(taskId)
            .header("traceId", traceId != null ? traceId : ""));
}
```

### 핵심 포인트

- `SqsTemplate.send(Consumer<SqsSendOptions>)` API 사용
- `header()` 메서드로 SQS message attributes에 커스텀 헤더 추가
- traceId가 null일 경우 빈 문자열 전달 (NPE 방지)
- SQS message attributes는 최대 10개까지 지원

## Consumer 구현

### `@SqsListener` + `@Header` + `@Payload` 수신 패턴

```java
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.slf4j.MDC;

@SqsListener("${fileflow.sqs.download-queue}")
public void consume(
        @Payload String downloadTaskId,
        @Header(name = "traceId", required = false) String traceId) {
    if (traceId != null && !traceId.isBlank()) {
        MDC.put("traceId", traceId);
    }

    try {
        log.info("메시지 수신: id={}", downloadTaskId);
        // 비즈니스 로직 실행
    } finally {
        MDC.remove("traceId");
    }
}
```

### 핵심 포인트

- `@Payload`: 메시지 본문 (기존 String 파라미터와 동일)
- `@Header(name = "traceId", required = false)`: SQS message attribute에서 traceId 추출
- `required = false`: traceId가 없는 기존 메시지와 하위 호환성 보장
- **MDC cleanup 필수**: `finally` 블록에서 `MDC.remove("traceId")` 호출

## MDC 기반 분산 추적

### MDC (Mapped Diagnostic Context)란?

SLF4J/Logback에서 제공하는 스레드 로컬 키-값 저장소.
`MDC.put("traceId", value)`로 설정하면 이후 해당 스레드의 모든 로그에 traceId가 자동 포함됩니다.

### logback 패턴 설정

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
```

`%X{traceId}` 패턴이 MDC에서 traceId를 가져옵니다.

### 로그 출력 예시

```
2025-02-21 10:30:00.123 [sqs-listener-1] [scheduler-abc12345] INFO  DownloadTaskSqsConsumer - 다운로드 작업 메시지 수신: downloadTaskId=task-001
2025-02-21 10:30:01.456 [sqs-listener-1] [scheduler-abc12345] INFO  DownloadTaskService - 다운로드 시작: url=https://example.com/file.zip
2025-02-21 10:30:05.789 [sqs-listener-1] [scheduler-abc12345] INFO  S3Uploader - S3 업로드 완료: key=downloads/task-001/file.zip
```

동일한 `scheduler-abc12345`로 전체 처리 흐름을 추적할 수 있습니다.

## MDC Cleanup 필수 사항

### 왜 MDC.remove()가 필요한가?

SQS Consumer는 스레드 풀을 사용합니다. 하나의 메시지 처리가 끝난 후
MDC를 정리하지 않으면, 같은 스레드로 다음 메시지를 처리할 때
이전 메시지의 traceId가 남아있어 로그 오염이 발생합니다.

### 올바른 패턴

```java
// try-finally로 반드시 정리
try {
    MDC.put("traceId", traceId);
    // 비즈니스 로직
} finally {
    MDC.remove("traceId");  // 필수!
}
```

### 잘못된 패턴

```java
// finally 없이 MDC 설정 → 스레드 오염!
MDC.put("traceId", traceId);
// 비즈니스 로직 (예외 발생 시 MDC 정리 안됨)
MDC.remove("traceId");
```

## 테스트 작성 가이드

### Publisher 테스트

```java
@Test
void shouldPublishWithTraceIdHeader() {
    MDC.put("traceId", "scheduler-abc12345");

    sut.enqueue("task-001");

    // Consumer<SqsSendOptions> 파라미터로 send가 호출되었는지 검증
    verify(sqsTemplate).send(any(Consumer.class));
}

@AfterEach
void tearDown() {
    MDC.clear();  // 테스트 간 MDC 오염 방지
}
```

### Consumer 테스트

```java
@Test
void consume_WithTraceId_ExecutesUseCase() {
    // consume 시그니처: (String payload, String traceId)
    sut.consume("task-001", "scheduler-abc12345");

    then(useCase).should().execute("task-001");
}

@Test
void consume_NullTraceId_ProcessesSuccessfully() {
    sut.consume("task-001", null);

    then(useCase).should().execute("task-001");
}

@Test
void consume_ExceptionThrown_ClearsMdc() {
    willThrow(new RuntimeException("fail"))
            .given(useCase).execute("task-001");

    assertThatThrownBy(() -> sut.consume("task-001", "scheduler-abc12345"))
            .isInstanceOf(RuntimeException.class);

    // 예외 후에도 MDC가 정리되었는지 확인
    assertThat(MDC.get("traceId")).isNull();
}
```
