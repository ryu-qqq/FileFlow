---
name: test-domain
description: Domain 레이어 테스트 자동 생성. testFixtures + Aggregate/VO/Entity/Event 단위 테스트.
context: fork
agent: domain-tester
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-domain

Domain 레이어의 테스트를 자동 생성합니다. Aggregate, VO, Entity, Domain Event 테스트.

## 사용법

```bash
/test-domain seller
/test-domain brand --fixtures-only
/test-domain category --aggregate-only
/test-domain order --vo-only
/test-domain payment --no-run
```

## 입력

- `$ARGUMENTS[0]`: 패키지명 (예: seller, brand, category, order)
- `$ARGUMENTS[1]`: (선택) `--fixtures-only`, `--aggregate-only`, `--vo-only`, `--no-run`

## 생성 파일

```
testFixtures/ → {Domain}Fixtures.java
test/aggregate/ → {Domain}Test.java, {SubEntity}Test.java
test/vo/        → {VoName}Test.java
test/id/        → {DomainId}Test.java
test/event/     → {Domain}EventTest.java
test/exception/ → {Domain}ErrorCodeTest.java
```

## 테스트 실행

```bash
./gradlew :domain:test --tests "*{Domain}*"
```
