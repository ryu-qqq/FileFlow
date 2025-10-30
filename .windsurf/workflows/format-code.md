---
description: Spotless 코드 포맷팅 + Pre-commit Hook 자동 설치
---

# Format Code

**🎯 역할**: 코드 포맷팅 + Pre-commit Hook 자동화

**📋 도구**: Gradle Spotless + Git Hooks

## What It Does

코드 포맷팅 자동화 및 커밋 전 강제 검증:

1. ✅ **Google Java Format** - Google 스타일 가이드 적용
2. ✅ **Import 정리** - 불필요한 import 제거 및 정렬
3. ✅ **들여쓰기 통일** - 4 spaces, 일관된 줄바꿈
4. ✅ **자동 수정** - 포맷 위반 사항 자동 수정
5. 🆕 **Pre-commit Hook** - 커밋 전 자동 포맷팅 검증

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

## Pre-commit Hook 자동 설치

### 자동 설치 (권장)

```bash
# Windsurf에서
/format-code --setup-hook

# Claude Code에서
"Pre-commit Hook 설치해줘"
```

**설치 내용**:
```bash
# .git/hooks/pre-commit (자동 생성)
#!/bin/bash

echo "🔍 Checking code format..."

# Spotless format check
./gradlew spotlessCheck --quiet

if [ $? -ne 0 ]; then
  echo ""
  echo "❌ Code format check failed"
  echo ""
  echo "💡 Fix it automatically:"
  echo "   ./gradlew spotlessApply"
  echo ""
  echo "Or bypass (not recommended):"
  echo "   git commit --no-verify"
  exit 1
fi

echo "✅ Code format check passed"
```

### 수동 설치

```bash
# 1. Hook 파일 생성
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
./gradlew spotlessCheck
if [ $? -ne 0 ]; then
  echo "❌ Code format check failed"
  echo "Run: ./gradlew spotlessApply"
  exit 1
fi
EOF

# 2. 실행 권한 부여
chmod +x .git/hooks/pre-commit

# 3. 테스트
git commit -m "test"
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

## Benefits

### 1. 자동 검증 (커밋 전)
- 포맷되지 않은 코드 커밋 방지
- 팀 전체 코드 스타일 통일

### 2. 빠른 피드백
- 커밋 전 즉시 확인
- PR 후 발견보다 10배 빠름

### 3. 제로 설정
- `/format-code --setup-hook` 한 번만
- 팀원 전체 자동 적용

## Workflow Example

```bash
# 1. 코드 작성
vim domain/src/.../OrderDomain.java

# 2. 커밋 시도
git add .
git commit -m "feat(domain): Order Aggregate"

# 3. Pre-commit Hook 자동 실행
🔍 Checking code format...
❌ Code format check failed

💡 Fix it automatically:
   ./gradlew spotlessApply

# 4. 포맷 적용
./gradlew spotlessApply

# 5. 재커밋
git commit -m "feat(domain): Order Aggregate"
🔍 Checking code format...
✅ Code format check passed

[feature/order 1a2b3c4] feat(domain): Order Aggregate
```

## Related

- **Gradle**: `./gradlew spotlessApply`
- **Config**: `build.gradle.kts` (spotless section)
- **Pipeline**: `tools/pipeline/validate_conventions.sh`
- **Style Guide**: Google Java Format
- **Claude Code**: `/format-code` command
- **Pre-commit Hook**: `.git/hooks/pre-commit` (자동 생성)
