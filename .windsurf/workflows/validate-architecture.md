---
description: ArchUnit 아키텍처 검증 + Claude Code 자동 수정
---

# Validate Architecture

**🎯 역할**: 헥사고날 아키텍처 검증 + 위반 시 자동 수정 제안

**📋 통합**: ArchUnit + Claude Code Auto-Fix

## What It Does

이 워크플로우는 다음을 **자동으로** 검증하고 수정합니다:

1. ✅ **Hexagonal Architecture** - 레이어 의존성 방향
2. ✅ **Domain Independence** - Domain의 외부 의존성 없음
3. ✅ **Port-Adapter Pattern** - Adapter는 Port에만 의존
4. ✅ **Naming Conventions** - 클래스/패키지 네이밍 규칙
5. 🆕 **Auto-Fix** - 위반 감지 시 Claude Code 자동 수정 제안

## Usage

### 전체 아키텍처 검증

```bash
./gradlew :bootstrap-web-api:test --tests "*ArchitectureTest"
```

### 특정 레이어 검증

```bash
# Domain layer 규칙만
./gradlew :bootstrap-web-api:test --tests "DomainLayerRulesTest"

# Application layer 규칙만
./gradlew :bootstrap-web-api:test --tests "ApplicationLayerRulesTest"

# Hexagonal architecture 전체
./gradlew :bootstrap-web-api:test --tests "HexagonalArchitectureTest"
```

## Architecture Rules Checked

### 1. 레이어 의존성 방향
```
Adapter (REST/Persistence)
        ↓ (의존 가능)
    Application
        ↓ (의존 가능)
      Domain
        ↓ (의존 불가능 - 독립성)
```

### 2. Domain Layer 규칙
- ❌ Spring/JPA 의존성 금지
- ❌ Framework 어노테이션 금지
- ✅ Pure Java만 사용

### 3. Application Layer 규칙
- ✅ Domain만 의존
- ❌ Adapter 직접 의존 금지 (Port 인터페이스 사용)

### 4. Adapter Layer 규칙
- ✅ Port 인터페이스 구현
- ❌ 다른 Adapter 의존 금지

## Output

**성공 시**:
```
✅ Architecture validation passed

Tests:
  HexagonalArchitectureTest ✓
  DomainLayerRulesTest ✓
  ApplicationLayerRulesTest ✓
```

**실패 시 (자동 수정 모드)**:
```
❌ Architecture validation failed

Violations:
  1. Domain class depends on Spring Framework
     Location: Order.java:15
     Issue: import org.springframework.stereotype.Component;

  2. Adapter depends on another Adapter
     Location: OrderRestAdapter.java:42
     Issue: OrderPersistenceAdapter 직접 참조

✨ Claude Code Auto-Fix Suggestions:

1️⃣ Order.java:15
   Problem: Domain이 Spring Framework에 의존
   Solution:
   - Spring 어노테이션 제거
   - Pure Java로 변경
   - Port 인터페이스 사용

   Apply fix? [Y/n]

2️⃣ OrderRestAdapter.java:42
   Problem: Adapter 간 직접 의존
   Solution:
   - OrderPort 인터페이스 사용
   - Application Layer를 통한 간접 참조

   Apply fix? [Y/n]

📝 Serena Memory: 위반 패턴 저장 → 다음 코드 생성 시 자동 예방
```

## ArchUnit Test Locations

```
bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/
├── HexagonalArchitectureTest.java    # 전체 헥사고날 규칙
├── DomainLayerRulesTest.java         # Domain 규칙
├── ApplicationLayerRulesTest.java    # Application 규칙
└── PersistenceLayerRulesTest.java    # Persistence 규칙
```

## Integration with Pipeline

PR Gate 파이프라인에서 자동으로 실행됩니다:

```bash
./tools/pipeline/pr_gate.sh
# Step 4: Architecture Validation (ArchUnit)
```

## Claude Code Integration

### Auto-Fix 모드 활성화

```bash
# Windsurf에서
/validate-architecture --auto-fix

# 또는 Claude Code에서
"아키텍처 검증하고 위반 사항 자동 수정해줘"
```

### 워크플로우

```
1. ArchUnit 테스트 실행
   ↓
2. 위반 감지
   ↓
3. Claude Code 분석
   - 위반 원인 파악
   - 수정 방법 제안
   - Before/After 코드 생성
   ↓
4. 사용자 확인
   ↓
5. 자동 적용 (승인 시)
   ↓
6. Serena Memory 저장
   - 위반 패턴 학습
   - 다음 코드 생성 시 자동 예방
```

### 실행 예시

```bash
# 1. 검증 실행
/validate-architecture --auto-fix

# 2. 위반 감지 및 수정
❌ Domain depends on Spring Framework
   Location: Order.java:15

✨ Claude Code:
   Before:
   ```java
   @Component
   public class OrderDomain { ... }
   ```

   After:
   ```java
   public class OrderDomain { ... }
   ```

   Apply? [Y/n] Y

✅ Fixed: Order.java:15
📝 Serena: Pattern saved

# 3. 재검증
🔄 Re-running ArchUnit tests...
✅ All architecture rules passed
```

## Related

- **Tests**: `bootstrap-web-api/src/test/java/.../architecture/`
- **Rules**: `docs/coding_convention/*/package-guide/`
- **Pipeline**: `tools/pipeline/pr_gate.sh`
- **Claude Code**: `/validate-architecture` command
- **Serena Memory**: 위반 패턴 자동 학습
