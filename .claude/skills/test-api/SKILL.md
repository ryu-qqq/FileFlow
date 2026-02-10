---
name: test-api
description: rest-api / rest-api-admin 모듈 테스트 자동 생성. testFixtures + Mapper 테스트 + RestDocs 테스트.
context: fork
agent: api-tester
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-api

rest-api / rest-api-admin 모듈의 테스트를 자동 생성합니다.

## 사용법

```bash
/test-api rest-api-admin selleradmin
/test-api rest-api-admin v2/seller
/test-api rest-api brand
/test-api rest-api-admin auth --mapper-only
/test-api rest-api-admin category --restdocs-only
```

## 입력

- `$ARGUMENTS[0]`: 모듈명 (rest-api-admin 또는 rest-api)
- `$ARGUMENTS[1]`: 패키지명 (예: selleradmin, brand)
- `$ARGUMENTS[2]`: (선택) `--mapper-only`, `--restdocs-only`, `--query-only`, `--command-only`, `--no-run`

## 생성 파일

```
testFixtures/ → {Domain}ApiFixtures.java
test/mapper/  → QueryApiMapperTest, CommandApiMapperTest
test/controller/ → QueryControllerRestDocsTest, CommandControllerRestDocsTest
```
