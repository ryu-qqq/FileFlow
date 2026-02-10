---
name: test-application
description: Application 레이어 테스트 자동 생성. testFixtures + Service/Factory/Assembler/Manager Mockito 기반 단위 테스트.
context: fork
agent: application-tester
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-application

Application 레이어의 테스트를 자동 생성합니다. Mockito 기반 Service, Factory, Assembler, Manager 테스트.

## 사용법

```bash
/test-application seller
/test-application brand --fixtures-only
/test-application category --service-only
/test-application order --factory-only
/test-application payment --no-run
```

## 입력

- `$ARGUMENTS[0]`: 패키지명 (예: seller, brand, category, order)
- `$ARGUMENTS[1]`: (선택) `--fixtures-only`, `--service-only`, `--factory-only`, `--assembler-only`, `--no-run`

## 생성 파일

```
testFixtures/ → {Domain}CommandFixtures.java, {Domain}QueryFixtures.java
test/service/command/ → {Action}{Domain}ServiceTest.java
test/service/query/   → Get{Domain}ServiceTest.java
test/factory/         → {Domain}CommandFactoryTest.java
test/assembler/       → {Domain}AssemblerTest.java
test/manager/         → {Domain}CommandManagerTest.java, {Domain}ReadManagerTest.java
test/internal/        → {Domain}CoordinatorTest.java
test/validator/       → {Domain}ValidatorTest.java
```

## 테스트 실행

```bash
./gradlew :application:test --tests "*{Domain}*"
```
