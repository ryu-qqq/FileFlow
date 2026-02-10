---
name: test-adapter-in
description: 비-HTTP adapter-in (redis-consumer, sqs-consumer, scheduler) Mockito 기반 단위 테스트 자동 생성. 비-HTTP 테스트 파이프라인 두 번째 단계.
context: fork
agent: adapter-in-tester
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-adapter-in

비-HTTP adapter-in 모듈의 Mockito 기반 단위 테스트를 자동 생성합니다.

## 사용법

```bash
# 모듈:도메인 형식
/test-adapter-in redis-consumer:session
/test-adapter-in sqs-consumer:download
/test-adapter-in scheduler:download
/test-adapter-in scheduler:transform

# 모듈 전체
/test-adapter-in redis-consumer --all
/test-adapter-in sqs-consumer --all
/test-adapter-in scheduler --all

# 옵션
/test-adapter-in scheduler:download --no-run
```

## 입력

- `$ARGUMENTS[0]`: `{module}:{domain}` 또는 `{module}`
  - module: `redis-consumer`, `sqs-consumer`, `scheduler`
  - domain: `session`, `download`, `transform` 등
- `$ARGUMENTS[1]`: (선택) `--all`, `--no-run`

## 전제조건

`/adapter-in-flow` 분석 문서 (권장, 필수 아님):
- `claudedocs/adapter-in-flows/{module}/{domain}_flows.md`

## 생성 파일

```
{module}/src/test/java/
  com/ryuqq/fileflow/adapter/in/{type}/{domain}/
    └── {ClassName}Test.java
```

## 모듈별 테스트 전략

| 모듈 | 핵심 검증 | 에러 검증 |
|------|----------|----------|
| `redis-consumer` | prefix 필터 + UseCase 호출 | catch & log (예외 전파 안 됨) |
| `sqs-consumer` | 메시지 수신 + UseCase 호출 | re-throw (SQS 재시도) |
| `scheduler` | Properties → Command + UseCase 호출 | AOP 로깅 범위 |

## 테스트 실행

```bash
./gradlew :adapter-in:{module}:test --tests "*{Domain}*"
```

## 비-HTTP 테스트 파이프라인

```
/adapter-in-flow → /test-adapter-in
```
