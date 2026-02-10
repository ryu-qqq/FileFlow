---
name: test-repository
description: persistence-mysql 모듈 Repository 테스트 자동 생성. testFixtures + 단위 테스트 + 통합 테스트.
context: fork
agent: repository-tester
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-repository

persistence-mysql 모듈의 Repository 테스트를 자동 생성합니다.

## 사용법

```bash
/test-repository selleradmin
/test-repository brand --unit-only
/test-repository category --integration-only
/test-repository seller --fixtures-only
```

## 입력

- `$ARGUMENTS[0]`: 패키지명 (예: selleradmin, brand, category)
- `$ARGUMENTS[1]`: (선택) `--unit-only`, `--integration-only`, `--fixtures-only`, `--no-run`

## 생성 파일

```
testFixtures/ → {Domain}JpaEntityFixtures.java
test/         → MapperTest, ConditionBuilderTest, AdapterTest
integration/  → RepositoryTest, QueryDslRepositoryTest
```
