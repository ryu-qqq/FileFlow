---
description: JaCoCo 테스트 커버리지 검증
---

# Validate Test Coverage

**🎯 역할**: JaCoCo 테스트 커버리지 검증

**📋 도구**: Gradle JaCoCo Plugin

## What It Does

테스트 커버리지를 측정하고 최소 임계값을 검증합니다:

1. ✅ **Coverage Report** - JaCoCo HTML 리포트 생성
2. ✅ **Threshold Check** - 최소 커버리지 검증 (80%)
3. ✅ **Line Coverage** - 라인 커버리지 측정
4. ✅ **Branch Coverage** - 분기 커버리지 측정

## Usage

### 커버리지 리포트 생성

```bash
./gradlew test jacocoTestReport
```

### 커버리지 검증

```bash
./gradlew jacocoTestCoverageVerification
```

### 리포트 확인

```bash
# HTML 리포트
open build/reports/jacoco/test/html/index.html

# XML 리포트 (CI용)
cat build/reports/jacoco/test/jacocoTestReport.xml
```

## Cascade에서 실행

```
/validate-tests
```

## Coverage Rules

`build.gradle.kts`:

```kotlin
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80.toBigDecimal()  // 80%
            }
        }
        rule {
            element = "CLASS"
            limit {
                minimum = 0.70.toBigDecimal()  // 70%
            }
        }
    }
}
```

## Output

**성공 시**:
```
✅ Test coverage verification passed
Overall coverage: 85%
  Line: 87%
  Branch: 82%

Report: build/reports/jacoco/test/html/index.html
```

**실패 시**:
```
❌ Test coverage below threshold
Overall coverage: 75% (required: 80%)

Missing coverage:
  OrderService: 60%
  PaymentService: 70%

Add tests for uncovered code.
```

## Integration with PR Gate

PR Gate 파이프라인에서 자동으로 검증됩니다:

```bash
./tools/pipeline/pr_gate.sh
# Step 5: Test Coverage Verification
```

## Related

- **Gradle**: `./gradlew jacocoTestReport`
- **Config**: `build.gradle.kts` (jacoco section)
- **Reports**: `build/reports/jacoco/test/html/index.html`
- **Pipeline**: `tools/pipeline/pr_gate.sh`
