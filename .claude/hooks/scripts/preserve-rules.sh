#!/bin/bash

# preserve-rules.sh - Claude Code PreCompact Hook
# 컨텍스트 압축 전 핵심 규칙을 보존하여 압축 후에도 규칙이 유지되도록 합니다.

set -euo pipefail

cat <<'CRITICAL_RULES'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔒 CRITICAL ARCHITECTURE RULES - NEVER FORGET
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 🏗️ Hexagonal Architecture 계층별 규칙

### 📦 Domain Layer (순수성 절대 엄수)
❌ ABSOLUTELY FORBIDDEN:
  - org.springframework.* (Spring Framework)
  - jakarta.persistence.* (JPA/Hibernate)
  - lombok.* (Lombok 전체)
  - Mutable fields (setter 사용 금지)

✅ ALLOWED ONLY:
  - java.util.*, java.time.*, java.math.*
  - org.apache.commons.lang3.*
  - Pure domain logic

✅ REQUIRED:
  - private final fields (불변성)
  - Factory methods (정적 팩토리 메서드)
  - Javadoc + @author tag
  - 90%+ test coverage

---

### ⚙️ Application Layer (Use Case)
❌ ABSOLUTELY FORBIDDEN:
  - Adapter 직접 참조 (Controller, Repository 구현체)
  - lombok.*
  - JPA entities in use case logic

✅ ALLOWED:
  - Domain 참조
  - Port interfaces (의존성 역전)
  - @Service, @Transactional

✅ REQUIRED:
  - UseCase suffix
  - Port interfaces 통해서만 Adapter 통신
  - 80%+ test coverage

---

### 🔌 Adapter Layer (Infrastructure)
❌ ABSOLUTELY FORBIDDEN:
  - lombok.*
  - Business logic (도메인 로직 포함 금지)
  - Direct domain manipulation

✅ ALLOWED:
  - Spring Framework
  - JPA, AWS SDK, HTTP clients
  - Infrastructure code

✅ REQUIRED:
  - Controller/Repository suffix
  - Testcontainers for integration tests
  - 70%+ test coverage

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 🚨 금지어 (즉시 거부해야 하는 표현)
  ❌ "일단", "나중에", "TODO로 남기고"
  ❌ "임시로", "테스트는 나중에"
  ❌ "Lombok으로 간단하게", "setter 추가"
  ❌ "우선 동작하게만", "리팩토링은 나중에"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 📋 품질 게이트 (Quality Gates)
  ✅ ArchUnit 테스트 통과 (HexagonalArchitectureTest.java)
  ✅ Checkstyle 검증 (Javadoc + @author 필수)
  ✅ Git Pre-commit Hooks (8개 validator 자동 실행)
  ✅ 커버리지: Domain 90%, Application 80%, Adapter 70%

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 📚 참고 문서 (컨텍스트 압축 후 읽기)

### 요약본 (빠른 참조용 - 경량)
  - 엔터프라이즈 표준: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md
  - 코딩 표준: docs/CODING_STANDARDS_SUMMARY.md

### 전체 문서 (상세 참조용)
  - 엔터프라이즈 표준: docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md (96개 규칙)
  - 코딩 표준: docs/CODING_STANDARDS.md (87개 규칙)

### 특화 가이드
  - DDD Aggregate: docs/DDD_AGGREGATE_MIGRATION_GUIDE.md
  - DTO 패턴: docs/DTO_PATTERNS_GUIDE.md
  - 예외 처리: docs/EXCEPTION_HANDLING_GUIDE.md
  - Java Record: docs/JAVA_RECORD_GUIDE.md
  - Gemini 리뷰: docs/GEMINI_REVIEW_GUIDE.md

### 시스템 문서
  - Dynamic Hooks: .claude/hooks/README.md
  - Git Hooks: hooks/README.md
  - 작업 추적: TODO_IMPLEMENTATION.md

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚡ 이 규칙들은 절대 타협할 수 없습니다.
📍 의심스러울 때는 항상 문서를 다시 확인하세요.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CRITICAL_RULES

exit 0
