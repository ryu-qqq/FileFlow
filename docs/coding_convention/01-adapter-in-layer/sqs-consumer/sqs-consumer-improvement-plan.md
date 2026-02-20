# SQS Consumer 개선 로드맵 — **안전한 비동기 메시지 처리를 위한 분석과 계획**

> 이 문서는 채널톡 기술 블로그 ["AWS SQS를 도입하면서 했던 고민들"](https://docs.channel.io/team-blog/ko/articles/AWS-SQS%EB%A5%BC-%EB%8F%84%EC%9E%85%ED%95%98%EB%A9%B4%EC%84%9C-%ED%96%88%EB%8D%98-%EA%B3%A0%EB%AF%BC%EB%93%A4-9706d147)을 기반으로
> FileFlow SQS Consumer의 현재 상태를 분석하고, 개선 방향을 정리한 문서입니다.
>
> 작성일: 2026-02-20

---

## 1) 현재 상태 요약

### 모듈 구성

| 모듈 | 역할 | 큐 |
|------|------|-----|
| `adapter-in/sqs-consumer` | 메시지 수신 | download-queue, transform-queue |
| `adapter-out/client/sqs-publisher` | 메시지 발행 | download-queue, transform-queue |

### 워커별 리스너 활성화

| 워커 | download-queue | transform-queue |
|------|---------------|-----------------|
| download-worker | **수신 (Consumer)** + 발행 (Publisher → transform) | 비활성화 |
| resizing-worker | 비활성화 | **수신 (Consumer)** |

### 메시지 흐름

```
[API Server]
    │
    ▼ (SQS Publisher)
[download-queue] ──Consumer──→ [download-worker] ──SQS Publisher──→ [transform-queue]
                                                                         │
                                                              Consumer───┘
                                                                         │
                                                                         ▼
                                                                  [resizing-worker]
```

---

## 2) 블로그 핵심 교훈 vs 현재 구현 비교

### 2-1. Visibility Timeout — 이중 처리의 위험

> **블로그 교훈**: process() 실행 시간이 visibility timeout을 초과하면 동일 메시지가
> 다른 워커에게 다시 노출된다. **양쪽에 동일한 timeout을 설정해야 한다.**

**현재 상태:**

| 구분 | Terraform (큐 설정) | Spring Cloud AWS (Consumer) | 일치 여부 |
|------|---------------------|-----------------------------|----------|
| download-queue | `visibility_timeout = 360s` (6분) | 명시적 설정 없음 (큐 기본값 사용) | 큐 설정에 의존 |
| transform-queue | `visibility_timeout = 600s` (10분) | 명시적 설정 없음 (큐 기본값 사용) | 큐 설정에 의존 |

**위험 시나리오:**

```
[Worker A] 메시지 수신 → 대용량 파일 다운로드 시작 (7분 소요)
                                    │
                          ┌─────────┴─────────┐
                          │ 6분 경과            │
                          │ visibility timeout │
                          │ 메시지 다시 visible │
                          └─────────┬─────────┘
                                    │
[Worker B] 같은 메시지 수신 → 동일 파일 다운로드 시작 (중복!)
                                    │
[Worker A] 다운로드 완료 → ACK     │
[Worker B] 다운로드 완료 → ACK (중복 처리 완료)
```

**문제점:**
- Consumer 측에서 처리 시간 제한(process timeout)이 없음
- visibility timeout을 초과하는 처리가 이중 실행될 수 있음

**개선 방향:**
- Consumer 측에서 `@SqsListener`의 `acknowledgementMode`와 visibility timeout extension 전략 검토
- 또는 Application 레이어에서 분산 락으로 이중 처리 방지

---

### 2-2. 멱등성 — 가장 큰 갭

> **블로그 교훈**: 비즈니스 로직은 **중복 처리를 견디도록** 설계해야 한다.
> DB upsert는 멱등성이 있지만, 푸시 알림은 중복 제거 필터가 필요하다.

**현재 상태:**

```java
// Consumer: 단순 String ID 전달
@SqsListener("${fileflow.sqs.download-queue}")
public void consume(String downloadTaskId) {
    startDownloadTaskUseCase.execute(downloadTaskId);
}
```

| 검토 항목 | 현재 상태 | 비고 |
|----------|----------|------|
| Consumer 레벨 중복 방지 | 없음 | 동일 메시지 2번 수신 시 UseCase 2번 실행 |
| Application 레벨 상태 체크 | 있을 수 있음 (UseCase 내부) | 확인 필요 |
| 분산 락 | 미사용 | Redis 분산 락 인프라는 존재 |
| MessageDeduplicationId | 미사용 | Standard Queue이므로 FIFO dedup 불가 |

**중복 실행 시 영향 분석:**

| 작업 | 멱등성 | 중복 시 영향 |
|------|--------|-------------|
| 파일 다운로드 + S3 업로드 | 부분적 (같은 키에 덮어쓰기) | 시간/비용 낭비 |
| DB 상태 PROCESSING → COMPLETED | 위험 | 이미 COMPLETED인 상태를 다시 PROCESSING으로 변경 가능 |
| transform-queue 메시지 발행 | 없음 | 동일 변환 요청이 2번 발행됨 |

**개선 방향:**

```
방안 A: Application 레이어 상태 체크 강화
  - UseCase 시작 시 현재 상태가 PENDING인지 확인
  - 이미 PROCESSING/COMPLETED면 조기 리턴 (skip)
  - 장점: 단순, 추가 인프라 불필요
  - 단점: DB 조회와 상태 변경 사이 race condition 존재

방안 B: 분산 락 (Redis)
  - UseCase 시작 시 Redis 분산 락 획득 시도
  - 이미 락이 잡혀있으면 조기 리턴
  - 장점: race condition 방지
  - 단점: Redis 의존성 증가, 락 해제 실패 시 처리 지연

방안 C: DB 낙관적 락 (version 필드)
  - 상태 변경 시 version 체크
  - 동시 업데이트 시 한쪽만 성공
  - 장점: 별도 인프라 불필요
  - 단점: 실패한 쪽이 재시도 필요

권장: 방안 A + B 조합
  - 상태 체크로 대부분의 중복 차단 (가벼움)
  - 분산 락으로 race condition 방지 (안전장치)
```

---

### 2-3. Publisher 안정성

> **블로그 교훈**: 네트워크 오류 시 "전달됐는지" vs "응답만 유실됐는지"를 구별할 수 없다.
> publisher는 재시도하고, `MessageDeduplicationId`로 중복 발행을 방지한다.

**현재 상태:**

```java
// fire-and-forget 방식
public void enqueue(String downloadTaskId) {
    sqsTemplate.send(queueName, downloadTaskId);
}
```

| 검토 항목 | 현재 상태 |
|----------|----------|
| 발행 실패 시 재시도 | 없음 (SqsTemplate 기본 동작에 의존) |
| MessageDeduplicationId | 미사용 (Standard Queue) |
| 발행 실패 로깅 | sqsTemplate.send() 예외 발생 시 상위 전파 |
| 발행 확인 (응답 검증) | 없음 |

**개선 방향:**
- `sqsTemplate.send()` 실패 시 재시도 래퍼 또는 Spring Retry 도입 검토
- 중요 메시지는 Outbox 패턴으로 DB에 먼저 저장 후 발행 고려

---

### 2-4. DLQ 처리 — 설정만 있고 소비 로직 없음

> **블로그 교훈**: DLQ로 이동시켜 뒤의 메시지 처리 중단을 방지한다.

**현재 상태:**

```hcl
# Terraform - DLQ 설정은 완료
enable_dlq        = true
max_receive_count = 3     # 3번 실패 시 DLQ로 이동
dlq_message_retention_seconds = 1209600  # 14일 보관
```

```yaml
# Bootstrap - DLQ 리스너 프로퍼티 정의됨 (Consumer 미구현)
aws.sqs.listener:
  external-download-dlq-listener-enabled: true
  file-processing-dlq-listener-enabled: true
```

**문제점:**
- DLQ Consumer 클래스가 존재하지 않음
- DLQ에 쌓인 메시지가 14일 후 조용히 사라짐
- 실패 원인 분석 불가

**개선 방향:**

```java
// 향후 구현 예시
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.external-download-dlq-listener-enabled",
        havingValue = "true")
public class DownloadTaskDlqConsumer {

    @SqsListener("${fileflow.sqs.download-queue}-dlq")
    public void consume(String downloadTaskId) {
        // 1. 실패 원인 로깅 (ERROR)
        log.error("DLQ 메시지 수신 - 3회 재시도 실패: downloadTaskId={}", downloadTaskId);

        // 2. DB 상태를 FAILED로 업데이트
        markDownloadTaskFailed(downloadTaskId);

        // 3. 알림 발송 (Slack, Sentry 등)
        notifyFailure(downloadTaskId);

        // 4. ACK (DLQ에서 제거)
    }
}
```

---

### 2-5. 에러 분류 — 일시적 vs 영구적

> **블로그 교훈**: 안전한 Consumer 작성을 위해 실패 유형을 구분해야 한다.

**현재 상태:**

```java
catch (Exception e) {
    log.error("처리 실패: id={}", id, e);
    throw e;  // 모든 에러를 동일하게 처리
}
```

**문제점:**
- 모든 예외가 동일하게 재시도됨
- 영구적 오류(잘못된 URL, 파싱 불가)도 3번 재시도 후에야 DLQ로 이동
- 불필요한 재시도로 큐 처리 지연

**개선 방향:**

```
에러 분류 전략:

┌─────────────────────────┐     ┌─────────────────────────┐
│ 일시적 오류 (Retryable)  │     │ 영구적 오류 (Fatal)      │
│                         │     │                         │
│ - 네트워크 타임아웃       │     │ - 잘못된 메시지 형식      │
│ - 외부 서버 503          │     │ - 존재하지 않는 리소스 ID │
│ - DB 커넥션 풀 소진      │     │ - 파싱/변환 불가 데이터   │
│ - Redis 일시 장애        │     │ - 비즈니스 규칙 위반      │
│                         │     │                         │
│ → throw e (재시도)       │     │ → 즉시 DLQ 또는 FAILED   │
│ → visibility timeout 후 │     │ → 로깅 + 알림            │
│   자동 재시도            │     │ → 불필요한 재시도 방지    │
└─────────────────────────┘     └─────────────────────────┘
```

---

### 2-6. Graceful Shutdown과 Visibility Timeout 연계

> **블로그 교훈**: Spot Instance 종료 시 SQS visibility timeout으로 자동 재시도된다.

**현재 상태:**

| 설정 | 값 | 비고 |
|------|-----|------|
| Graceful Shutdown 대기 시간 | 30초 | `spring.lifecycle.timeout-per-shutdown-phase` |
| download-queue visibility timeout | 360초 (6분) | Terraform |
| transform-queue visibility timeout | 600초 (10분) | Terraform |
| ECS 태스크 종료 유예 시간 | 기본 30초 | ECS stopTimeout |

**잠재적 문제:**
- 대용량 파일 다운로드가 30초 안에 끝나지 않으면 강제 종료
- 미완료 메시지는 visibility timeout(6분) 후에야 다시 visible
- 최대 6분 동안 해당 메시지 처리가 지연됨

**개선 방향:**
- 프로덕션 환경에서 shutdown timeout을 45~60초로 상향 검토
- ECS stopTimeout도 동일하게 조정 필요

---

### 2-7. 동시성 제어와 배압

> **블로그 교훈**: 워커당 약 300 TPS. 트래픽 3배 증가 시 워커를 8배 추가.

**현재 상태:**

| 설정 | 값 | 비고 |
|------|-----|------|
| max-concurrent-messages | 10 (local: 5) | 동시 처리 메시지 수 |
| max-messages-per-poll | 10 | 한 번의 폴링당 최대 수신 |
| ECS Task CPU | 512 (0.5 vCPU) | |
| ECS Task Memory | 1024 MB | |
| desired_count | 1 | stage/prod 동일 |

**검토 필요:**
- download-worker는 I/O 바운드 (외부 다운로드 + S3 업로드)
- 동시 10개 대용량 파일 다운로드 시 메모리/대역폭 영향 측정 필요
- 512 CPU / 1024 MB에서 동시 10개 처리가 적절한지 부하 테스트 필요

---

## 3) 개선 우선순위

### Phase 1: 즉시 (안정성 확보)

| 항목 | 위험도 | 난이도 | 설명 |
|------|--------|--------|------|
| ~~@ConditionalOnProperty 추가~~ | 높음 | 낮음 | ~~KAN-344로 완료~~ |
| ~~IAM 권한 보완~~ | 높음 | 낮음 | ~~KAN-344로 완료~~ |

### Phase 2: 단기 (안전망 구축)

| 항목 | 위험도 | 난이도 | 설명 |
|------|--------|--------|------|
| UseCase 멱등성 강화 | **높음** | 중간 | 상태 체크 + 분산 락 조합 |
| DLQ Consumer 구현 | **높음** | 중간 | 실패 메시지 알림 + 상태 업데이트 |
| 에러 분류 도입 | 중간 | 중간 | Retryable vs Fatal 예외 구분 |

### Phase 3: 중기 (운영 고도화)

| 항목 | 위험도 | 난이도 | 설명 |
|------|--------|--------|------|
| Visibility Timeout 연계 | 중간 | 중간 | Consumer 측 timeout 명시 설정 |
| Graceful Shutdown 조정 | 낮음 | 낮음 | prod 45~60초로 상향 |
| SQS 메트릭 대시보드 | 낮음 | 낮음 | 큐 깊이, 처리 지연, DLQ 모니터링 |
| 동시성 부하 테스트 | 낮음 | 중간 | 적정 concurrent messages 측정 |

### Phase 4: 장기 (아키텍처 개선)

| 항목 | 위험도 | 난이도 | 설명 |
|------|--------|--------|------|
| Publisher 재시도 전략 | 낮음 | 중간 | Spring Retry 또는 Outbox 패턴 |
| FIFO Queue 전환 검토 | 낮음 | 높음 | 순서 보장이 필요한 경우 |
| SQS 기반 Auto Scaling | 낮음 | 높음 | 큐 깊이 기반 ECS 태스크 스케일링 |

---

## 4) 참고 자료

- [AWS SQS를 도입하면서 했던 고민들 — 채널톡 기술 블로그](https://docs.channel.io/team-blog/ko/articles/AWS-SQS%EB%A5%BC-%EB%8F%84%EC%9E%85%ED%95%98%EB%A9%B4%EC%84%9C-%ED%96%88%EB%8D%98-%EA%B3%A0%EB%AF%BC%EB%93%A4-9706d147)
- [Spring Cloud AWS SQS 공식 문서](https://docs.awspring.io/spring-cloud-aws/docs/current/reference/html/index.html#sqs-integration)
- [AWS SQS Visibility Timeout](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-visibility-timeout.html)
- [AWS SQS Dead Letter Queue](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html)

---

## 5) KAN-344 사후 분석 (Post-Mortem)

### 사건 요약

| 항목 | 내용 |
|------|------|
| 발생일 | 2026-02-20 |
| 환경 | stage |
| 에러 | `SqsException: User is not authorized to perform: sqs:receivemessage` |
| 영향 | download-worker 기동 시 file-processing 큐 접근 실패 |

### 근본 원인

`TransformRequestSqsConsumer`에 `@ConditionalOnProperty`가 없어서,
download-worker bootstrap에서도 해당 Consumer가 활성화됨.
download-worker의 IAM Task Role에는 file-processing 큐 접근 권한이 없어 403 에러 발생.

### 수정 내용

1. `DownloadTaskSqsConsumer`, `TransformRequestSqsConsumer`에 `@ConditionalOnProperty` 추가
2. download-worker Task Role에 file-processing 큐 SendMessage + KMS 권한 추가

### 교훈

- **SQS Consumer는 반드시 조건부 활성화해야 한다**
- 하나의 `sqs-consumer` 모듈을 여러 bootstrap이 공유하는 구조에서,
  `@ConditionalOnProperty` 없이 `@SqsListener`를 사용하면
  의도하지 않은 큐에 접근을 시도하게 된다
- **새로운 Consumer 추가 시 체크리스트 필수 확인** (본 문서 6장 참조)
