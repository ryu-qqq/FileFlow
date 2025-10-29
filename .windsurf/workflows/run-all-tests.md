---
description: 전체 테스트 실행 (Unit → Integration → E2E)
---

# Run All Tests

**🎯 역할**: 전체 테스트 스위트 순차 실행

**📋 도구**: Gradle Test Suites

## What It Does

모든 테스트를 단계별로 실행합니다:

1. ✅ **Unit Tests** - 단위 테스트 (빠름, ~1분)
2. ✅ **Integration Tests** - 통합 테스트 (보통, ~3분)
3. ✅ **E2E Tests** - E2E 테스트 (느림, ~5분)
4. ✅ **Coverage Report** - 전체 커버리지 리포트

## Usage

### 전체 테스트 (순차 실행)

```bash
./gradlew test integrationTest e2eTest
```

### 커버리지 포함

```bash
./gradlew test integrationTest e2eTest jacocoTestReport
```

### 실패 시 중단

```bash
./gradlew test integrationTest e2eTest --fail-fast
```

## Cascade에서 실행

```
/run-all-tests
```

## Output

**성공 시**:
```
✅ All tests passed!

Summary:
  Unit Tests:        120 passed (1m 15s)
  Integration Tests:  42 passed (2m 30s)
  E2E Tests:          15 passed (4m 45s)

Total: 177 tests in 8m 30s

Coverage: 85%
Report: build/reports/jacoco/test/html/index.html
```

**실패 시**:
```
❌ Test suite failed

Unit Tests: ✅ 120 passed
Integration Tests: ❌ 2 failed, 40 passed
E2E Tests: ⏭️  Skipped (previous failure)

Failed tests:
  - OrderRepositoryTest.shouldFindByStatus
  - PaymentServiceTest.shouldProcessRefund

Check: build/reports/tests/test/index.html
```

## Test Execution Order

```
1. Unit Tests (fast)
   ├─ Domain tests
   ├─ Application tests
   └─ Adapter tests (mocked)

2. Integration Tests (medium)
   ├─ Repository tests (real DB)
   ├─ Service tests (with DB)
   └─ API tests (without server)

3. E2E Tests (slow)
   ├─ Full stack tests
   ├─ Business scenarios
   └─ API contracts
```

## Best Practices

### 1. Local Development (Fast Feedback)

```bash
# 변경된 코드만 테스트
./gradlew test --tests "*Order*"

# Unit tests만 (빠른 피드백)
./gradlew test
```

### 2. Pre-commit (Medium Coverage)

```bash
# Unit + Integration
./gradlew test integrationTest
```

### 3. PR Gate (Full Coverage)

```bash
# All tests + Coverage
./tools/pipeline/pr_gate.sh
```

## Gradle Configuration

`build.gradle.kts`:

```kotlin
tasks {
    // Test order
    test {
        useJUnitPlatform()
    }

    val integrationTest by registering(Test::class) {
        useJUnitPlatform()
        testClassesDirs = sourceSets["integration"].output.classesDirs
        classpath = sourceSets["integration"].runtimeClasspath
        shouldRunAfter(test)
    }

    val e2eTest by registering(Test::class) {
        useJUnitPlatform()
        testClassesDirs = sourceSets["e2e"].output.classesDirs
        classpath = sourceSets["e2e"].runtimeClasspath
        shouldRunAfter(integrationTest)
    }
}
```

## Related

- **Unit Tests**: `run-unit-tests.md`
- **Integration Tests**: `run-integration-tests.md`
- **E2E Tests**: `run-e2e-tests.md`
- **Coverage**: `validate-tests.md`
- **Pipeline**: `tools/pipeline/pr_gate.sh`
