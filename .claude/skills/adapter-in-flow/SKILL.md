---
name: adapter-in-flow
description: 비-HTTP adapter-in (redis-consumer, sqs-consumer, scheduler) 진입점 분석 + 호출 흐름 문서화. 비-HTTP 테스트 파이프라인 첫 단계.
context: fork
agent: adapter-in-flow-analyzer
allowed-tools: Read, Write, Glob, Grep
---

# /adapter-in-flow

비-HTTP adapter-in 모듈의 진입점을 분석하고 Hexagonal 레이어별 호출 흐름을 추적하여 문서화합니다.

## 사용법

```bash
# 모듈:도메인 형식
/adapter-in-flow redis-consumer:session
/adapter-in-flow sqs-consumer:download
/adapter-in-flow scheduler:download
/adapter-in-flow scheduler:transform

# 모듈 전체 분석
/adapter-in-flow redis-consumer --all
/adapter-in-flow sqs-consumer --all
/adapter-in-flow scheduler --all
```

## 입력

- `$ARGUMENTS[0]`: `{module}:{domain}` 또는 `{module}`
  - module: `redis-consumer`, `sqs-consumer`, `scheduler`
  - domain: `session`, `download`, `transform` 등
- `$ARGUMENTS[1]`: (선택) `--all`, `--no-db`

## 지원 모듈

| 모듈 | 트리거 | 추출 기준 |
|------|--------|----------|
| `redis-consumer` | keyspace notification | `implements MessageListener` |
| `sqs-consumer` | SQS 메시지 | `@SqsListener` |
| `scheduler` | cron 주기 | `@Scheduled` |

## 출력

```
claudedocs/adapter-in-flows/{module}/{domain}_flows.md      # 단일 도메인
claudedocs/adapter-in-flows/{module}/{module}_all_flows.md   # --all
```

## 다음 단계

흐름 분석 완료 후:
```bash
/test-adapter-in redis-consumer:session
```

## 비-HTTP 테스트 파이프라인

```
/adapter-in-flow → /test-adapter-in
```
