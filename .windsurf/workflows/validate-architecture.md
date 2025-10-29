---
description: ArchUnit을 통한 헥사고날 아키텍처 검증 (Wrapper)
---

# Validate Architecture

**🎯 역할**: 헥사고날 아키텍처 규칙 검증 (ArchUnit)

**📋 로직**: Gradle 직접 호출 (ArchUnit 테스트는 코드에 내장)

## What It Does

이 워크플로우는 다음을 **자동으로** 검증합니다:

1. ✅ **Hexagonal Architecture** - 레이어 의존성 방향
2. ✅ **Domain Independence** - Domain의 외부 의존성 없음
3. ✅ **Port-Adapter Pattern** - Adapter는 Port에만 의존
4. ✅ **Naming Conventions** - 클래스/패키지 네이밍 규칙

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

**실패 시**:
```
❌ Architecture validation failed

Violations:
  - Domain class depends on Spring Framework
    Location: Order.java:15
  - Adapter depends on another Adapter
    Location: OrderRestAdapter.java:42
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

## Related

- **Tests**: `bootstrap-web-api/src/test/java/.../architecture/`
- **Rules**: `docs/coding_convention/*/package-guide/`
- **Pipeline**: `tools/pipeline/pr_gate.sh`
