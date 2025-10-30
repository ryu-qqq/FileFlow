---
description: Test Fixture 생성 (Claude Code 위임)
---

# Create Test Fixtures

**🎯 역할**: Test Fixture 생성 안내

**📋 권장**: Claude Code `/test-gen-fixtures` 사용

---

## ⚠️ 변경 사항

이 워크플로우는 **Claude Code 명령어**로 대체되었습니다.

### Before (Windsurf)
```
사용자: "Order Domain Fixture를 생성해줘"

→ Cascade가 494줄 가이드 문서 읽고
→ 수동으로 템플릿 참고하여 생성
```

### After (Claude Code)
```bash
# Claude Code 명령어 사용
/test-gen-fixtures OrderDomain --layer domain

# 자동 생성:
# - OrderDomainFixture.java
# - create(), createWithId(), createMultiple() 메서드
# - Javadoc 포함
# - Gradle 설정 자동 추가
```

---

## 🚀 Claude Code 사용법

### 기본 사용

```bash
# Domain Layer
/test-gen-fixtures OrderDomain --layer domain

# Application Layer
/test-gen-fixtures CreateOrderCommand --layer application

# REST API Layer
/test-gen-fixtures OrderApiRequest --layer rest

# Persistence Layer
/test-gen-fixtures OrderJpaEntity --layer persistence
```

### 고급 사용

```bash
# 테스트와 함께 생성
/test-gen-domain Order --with-fixtures

# 모든 Layer Fixture 일괄 생성
/test-gen-fixtures Order --all-layers
```

---

## 📚 Test Fixture란?

테스트에서 객체를 쉽게 생성하기 위한 **Factory 클래스**입니다.

### 핵심 개념
- **위치**: `src/testFixtures/java/` (별도 source set)
- **공유**: 다른 모듈에서 `testFixtures` 의존성으로 사용 가능
- **네이밍**: `*Fixture` 접미사 필수

### 사용 예시

```java
// Before (Fixture 없이)
@Test
void testOrderLogic() {
    LocalDateTime now = LocalDateTime.now();
    OrderDomain order = OrderDomain.of(
        1L,
        "Test Order",
        "PLACED",
        now,
        now
    );
    // ...
}

// After (Fixture 사용)
@Test
void testOrderLogic() {
    OrderDomain order = OrderDomainFixture.createWithId(1L, "Test Order");
    // ...
}
```

---

## 🔗 Related

- **Claude Code Command**: `/test-gen-fixtures`
- **Command Docs**: `.claude/commands/test-gen-fixtures.md`
- **Gradle Plugin**: `java-test-fixtures`
- **Gradle Docs**: https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures

---

**💡 핵심**: Windsurf는 Boilerplate 생성, Claude Code는 지능형 테스트 자동화!
