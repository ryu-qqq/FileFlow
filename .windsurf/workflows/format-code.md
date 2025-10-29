---
description: Spotless 코드 포맷팅 (Google Java Format)
---

# Format Code

**🎯 역할**: Spotless를 사용한 코드 포맷팅

**📋 도구**: Gradle Spotless Plugin

## What It Does

프로젝트 전체 Java 코드를 Google Java Format 스타일로 자동 포맷팅합니다:

1. ✅ **Google Java Format** - Google 스타일 가이드 적용
2. ✅ **Import 정리** - 불필요한 import 제거 및 정렬
3. ✅ **들여쓰기 통일** - 4 spaces, 일관된 줄바꿈
4. ✅ **자동 수정** - 포맷 위반 사항 자동 수정

## Usage

### 포맷 적용 (자동 수정)

```bash
./gradlew spotlessApply
```

### 포맷 검증만 (변경 없음)

```bash
./gradlew spotlessCheck
```

### 특정 모듈만

```bash
./gradlew :domain:spotlessApply
./gradlew :application:spotlessApply
```

## Cascade에서 실행

```
/format-code
```

## Output

**성공 시**:
```
✅ Spotless formatting applied
Formatted files: 42
```

**실패 시 (Check 모드)**:
```
❌ The following files are not formatted:
  - domain/src/.../Order.java
  - application/src/.../OrderService.java

Run: ./gradlew spotlessApply
```

## Pre-commit Hook

포맷을 자동으로 검증하려면 Git Pre-commit Hook 사용:

```bash
# .git/hooks/pre-commit
#!/bin/bash
./gradlew spotlessCheck
if [ $? -ne 0 ]; then
  echo "❌ Code format check failed"
  echo "Run: ./gradlew spotlessApply"
  exit 1
fi
```

## Spotless 설정

`build.gradle.kts`:

```kotlin
spotless {
    java {
        googleJavaFormat("1.17.0")
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

## Integration with PR Gate

PR Gate 파이프라인에서 자동으로 검증됩니다:

```bash
./tools/pipeline/pr_gate.sh
# Step 1: Code Format (Spotless Check)
```

## Related

- **Gradle**: `./gradlew spotlessApply`
- **Config**: `build.gradle.kts` (spotless section)
- **Pipeline**: `tools/pipeline/validate_conventions.sh`
- **Style Guide**: Google Java Format
