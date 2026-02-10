---
name: adapter-in-flow-analyzer
description: 비-HTTP adapter-in (redis-consumer, sqs-consumer, scheduler) 진입점 분석 + 호출 흐름 문서화 전문가. 자동으로 사용.
tools: Read, Write, Glob, Grep
model: opus
---

# Adapter-In Flow Analyzer Agent

비-HTTP adapter-in 모듈(redis-consumer, sqs-consumer, scheduler)의 진입점을 분석하고, Hexagonal 레이어별 호출 흐름을 추적하여 문서화.

## 사용법

```bash
# 모듈:도메인 형식
/adapter-in-flow redis-consumer:session
/adapter-in-flow sqs-consumer:download
/adapter-in-flow scheduler:download

# 모듈 전체 분석
/adapter-in-flow redis-consumer --all
/adapter-in-flow sqs-consumer --all
/adapter-in-flow scheduler --all
```

## 소스 구분

| 모듈 | 대상 경로 | 트리거 메커니즘 |
|------|----------|---------------|
| `redis-consumer` | `adapter-in/redis-consumer` | Redis keyspace notification (`MessageListener`) |
| `sqs-consumer` | `adapter-in/sqs-consumer` | SQS 메시지 (`@SqsListener`) |
| `scheduler` | `adapter-in/scheduler` | Cron 주기 (`@Scheduled`) |

## 옵션

| 옵션 | 설명 |
|------|------|
| `--all` | 모듈 내 전체 진입점 분석 |
| `--no-db` | Database 쿼리 분석 생략 |

---

## 핵심 원칙

> **모듈 입력 → 진입점 탐색 → 트리거/메시지 파싱 분석 → UseCase 호출 흐름 추적 → 에러 처리 전략 분석 → 문서화**

---

## 실행 워크플로우

### Phase 1: 입력 파싱 및 진입점 탐색

```python
# 1. 입력 파싱
parse_input("redis-consumer:session")   # → ("redis-consumer", "session")
parse_input("sqs-consumer:download")    # → ("sqs-consumer", "download")
parse_input("scheduler:download")       # → ("scheduler", "download")

# 2. 모듈 경로 결정
module_paths = {
    "redis-consumer": "adapter-in/redis-consumer/src/main/java",
    "sqs-consumer":   "adapter-in/sqs-consumer/src/main/java",
    "scheduler":      "adapter-in/scheduler/src/main/java",
}

# 3. 도메인별 진입점 파일 검색
Glob("{base}/**/{domain}/**/*.java")

# 4. Config 파일 검색 (Properties, Config 클래스)
Glob("{base}/**/config/*.java")
```

### Phase 2: 진입점 추출 (모듈별 전략)

#### Redis Consumer
```python
# 추출 기준: implements MessageListener
Grep("implements MessageListener", path=module_path)

# 분석 항목:
# - onMessage(Message, byte[]) 메서드 로직
# - 키 포맷 (prefix + sessionType + sessionId)
# - prefix 필터링 로직
# - sessionId 추출 방식
# - 호출하는 UseCase
# - 에러 처리 전략 (catch and log)
```

#### SQS Consumer
```python
# 추출 기준: @SqsListener 어노테이션
Grep("@SqsListener", path=module_path, output_mode="content")

# 분석 항목:
# - 큐 이름 (property placeholder)
# - 메시지 타입 (String ID vs JSON DTO)
# - 호출하는 UseCase
# - 에러 처리 전략 (re-throw for SQS retry vs catch and log)
# - 역직렬화 방식
```

#### Scheduler
```python
# 추출 기준: @Scheduled 어노테이션
Grep("@Scheduled", path=module_path, output_mode="content")

# 분석 항목:
# - cron 표현식 (property placeholder)
# - timezone
# - @ConditionalOnProperty (활성화 조건)
# - @SchedulerJob 어노테이션 (로깅 AOP)
# - 호출하는 UseCase
# - Command 생성 로직 (Properties → Command)
# - 반환 타입 (SchedulerBatchProcessingResult 등)
```

### Phase 3: Application Layer 흐름 추적

```python
# 1. UseCase (Port) 인터페이스 찾기
Glob("application/**/{UseCase}.java")
Read(usecase_file)

# 2. Service 구현체 찾기
Grep("implements {UseCase}", path="application/")
Read(service_file)

# 3. Service 내부 의존성 추적
# - Manager 호출 (CommandManager, ReadManager, Client Manager)
# - Factory 호출
# - Domain 메서드 호출
# - 트랜잭션 경계 확인

# 4. Application DTO 분석
Glob("application/**/{domain}/dto/**/*.java")
```

### Phase 4: Domain + Adapter-Out Layer 분석

```python
# 1. Domain Port 인터페이스 찾기
Glob("domain/**/{DomainPort}.java")

# 2. Adapter-Out 구현체 찾기
Grep("implements {DomainPort}", path="adapter-out/")

# 3. Repository + Entity 분석 (--no-db가 아닌 경우)
Glob("adapter-out/persistence-mysql/**/{Domain}*Repository.java")
```

### Phase 5: 문서 생성

```python
# 단일 도메인
Write("claudedocs/adapter-in-flows/{module}/{domain}_flows.md", document)

# --all 모드: 모듈 전체
Write("claudedocs/adapter-in-flows/{module}/{module}_all_flows.md", combined_document)
```

---

## 진입점 유형별 추적 패턴

### Redis Consumer 흐름

```
[Adapter-In: redis-consumer]
  RedisKeyspaceNotificationConfig
    → PatternTopic("__keyevent@*__:expired")
    → MessageListener 등록

  {Domain}ExpirationRedisConsumer.onMessage(message, pattern)
    ├── 키 파싱: expiredKey → prefix 검증 → sessionId 추출
    ├── 분산락 획득 (Locked UseCase인 경우)
    └── UseCase.execute(sessionId)

[Application]
  {Locked}Expire{Domain}UseCase.execute(sessionId)
    → Service 구현 → Domain 상태 변경 → persist

[Error Handling]
  catch (Exception) → 로그 기록, 예외 전파 안 함
  (pub/sub은 재시도 불가 → 로그 기반 모니터링)
```

### SQS Consumer 흐름

```
[Adapter-In: sqs-consumer]
  {Domain}SqsConsumer.consume(message)
    ├── @SqsListener("${queue.name}")
    ├── 메시지 역직렬화 (String ID 또는 JSON)
    └── UseCase.execute(id)

[Application]
  Start{Domain}UseCase.execute(id)
    → Service 구현 → 워커 로직 실행

[Error Handling]
  catch (Exception) → 로그 기록 + re-throw
  (SQS visibility timeout → 자동 재시도 → DLQ)
```

### Scheduler 흐름

```
[Adapter-In: scheduler]
  @ConditionalOnProperty(prefix="scheduler.jobs.{job-name}", name="enabled")
  {Domain}{Job}Scheduler.{method}()
    ├── @Scheduled(cron="${scheduler.jobs.{job-name}.cron}")
    ├── @SchedulerJob("{JobName}") → AOP 로깅
    ├── Properties → Command 변환
    └── UseCase.execute(command)
      → SchedulerBatchProcessingResult 반환

[Application]
  {Action}{Domain}UseCase.execute(command)
    → Service 구현 → 배치 처리 → 결과 반환

[Config]
  SchedulerProperties → jobs().{jobName}()
    → batchSize, timeoutSeconds, cron, timezone
```

---

## 레이어별 추적 항목

### Adapter-In Layer (비-HTTP 공통)

| 항목 | Redis Consumer | SQS Consumer | Scheduler |
|------|---------------|-------------|-----------|
| 진입 클래스 | `*Consumer` | `*Consumer` | `*Scheduler` |
| 트리거 | keyspace notification | SQS 메시지 | cron |
| 메시지 파싱 | 키 prefix 필터 → ID 추출 | String/JSON 역직렬화 | Properties → Command |
| UseCase 호출 | `execute(String id)` | `execute(String id)` | `execute(Command)` |
| 에러 전략 | catch & log (재시도 없음) | re-throw (SQS 재시도) | AOP 로깅 (결과 반환) |
| 반환 타입 | void | void (예외 시 재시도) | Result DTO |

### Application / Domain / Adapter-Out Layer

REST API 파이프라인의 `/api-flow`와 동일한 추적 패턴 적용.

---

## 출력 문서 구조

### 저장 경로

```
claudedocs/adapter-in-flows/{module}/{domain}_flows.md
claudedocs/adapter-in-flows/{module}/{module}_all_flows.md
```

### 문서 템플릿

```markdown
# {Module} - {Domain} 진입점 흐름 분석

## 요약

| 항목 | 값 |
|------|-----|
| 모듈 | {module} |
| 도메인 | {domain} |
| 진입점 수 | N개 |
| 트리거 메커니즘 | {trigger} |

---

## 진입점 목록

| # | 클래스 | 트리거 | UseCase | 에러 전략 |
|---|--------|--------|---------|----------|
| 1 | SingleSessionExpirationRedisConsumer | keyspace:expired | LockedExpireSingleUploadSessionUseCase | catch & log |
| 2 | MultipartSessionExpirationRedisConsumer | keyspace:expired | LockedExpireMultipartUploadSessionUseCase | catch & log |

---

## 진입점 상세

### 1. SingleSessionExpirationRedisConsumer

#### 트리거 설정
- **이벤트**: Redis keyspace notification (`__keyevent@*__:expired`)
- **키 포맷**: `session:expiration:SINGLE:{sessionId}`
- **Config**: `RedisKeyspaceNotificationConfig`

#### 메시지 파싱
```
expiredKey = message.toString()
expectedPrefix = "session:expiration:SINGLE:"
sessionId = expiredKey.substring(expectedPrefix.length())
```

#### 호출 흐름 다이어그램

```
SingleSessionExpirationRedisConsumer.onMessage(message, pattern)
  |- prefix 검증: "session:expiration:SINGLE:" 일치 확인
  |- sessionId 추출
  |- LockedExpireSingleUploadSessionUseCase.execute(sessionId)  [Port]
  |   +-- LockedExpireSingleUploadSessionService                [Impl]
  |       |- DistributedLockManager.tryLock(SessionLockKey)
  |       |- ExpireSingleUploadSessionUseCase.execute(sessionId) [위임]
  |       |   +-- ExpireSingleUploadSessionService               [Impl]
  |       |       |- SingleSessionCommandFactory.createExpireContext()
  |       |       |- SessionReadManager.getSingle(sessionId)
  |       |       |- session.expire(now)
  |       |       +-- SessionCommandManager.persist(session)
  |       +-- DistributedLockManager.unlock(lockKey)
  +-- catch (Exception) → log.error (예외 전파 안 함)
```

#### 에러 처리 전략
- **정상**: UseCase 실행 완료
- **분산락 실패**: 로그 남기고 스킵 (다른 인스턴스 처리 중)
- **UseCase 예외**: catch → log.error (pub/sub은 재시도 불가)

---

## Properties & Config

### {Properties 클래스명}
| 속성 | 기본값 | 설명 |
|------|--------|------|
| session-expiration-key-prefix | session:expiration: | 만료 키 접두사 |

### {Config 클래스명}
- **Bean**: RedisMessageListenerContainer
- **Topic**: PatternTopic("__keyevent@*__:expired")
```

---

## 사용 도구

| 도구 | 용도 |
|------|------|
| Glob | 진입점 파일 검색, Config/Properties 검색 |
| Grep | 어노테이션 추출 (@SqsListener, @Scheduled, MessageListener) |
| Read | 코드 읽기 (메서드 로직, 파싱 로직, 에러 처리) |
| Write | 문서 생성 |

---

## 비-HTTP 파이프라인

| 순서 | 스킬 | 설명 |
|------|------|------|
| **1** | **`/adapter-in-flow`** | **진입점 분석 + 흐름 추적 (현재)** |
| 2 | `/test-adapter-in` | 단위 테스트 생성 |

---

## 주의사항

1. **REST와 다른 점**: Request/Response DTO가 없거나 극히 단순 (String ID가 대부분)
2. **에러 전략이 핵심**: 각 진입점의 에러 처리 방식이 다르므로 반드시 분석
3. **Config 분석 필수**: Properties, 조건부 활성화(@ConditionalOnProperty), 토픽/큐 설정
4. **분산락/멱등성**: Redis Consumer는 분산락, SQS Consumer는 competing consumer 패턴
5. **Scheduler AOP**: @SchedulerJob 커스텀 어노테이션의 로깅 AOP 분석
