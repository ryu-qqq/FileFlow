# Spring Standards Project - Claude Code Configuration

이 프로젝트는 Spring Boot 헥사고날 아키텍처 기반의 엔터프라이즈 표준 프로젝트입니다.

## 프로젝트 컨텍스트

이 프로젝트의 모든 작업 시 아래 문서들을 참조하여 코딩 표준과 아키텍처 가이드를 준수해주세요.

### 핵심 표준 문서

**코딩 표준 및 아키텍처**
@../docs/CODING_STANDARDS.md
@../docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md

**도메인 주도 설계 (DDD)**
@../docs/DDD_AGGREGATE_MIGRATION_GUIDE.md
@../docs/DTO_PATTERNS_GUIDE.md

**예외 처리 및 Java 패턴**
@../docs/EXCEPTION_HANDLING_GUIDE.md
@../docs/JAVA_RECORD_GUIDE.md

**프로젝트 관리 및 설정**
@../docs/CUSTOMIZATION_GUIDE.md
@../docs/SETUP_SUMMARY.md
@../docs/VERSION_MANAGEMENT_GUIDE.md

**코드 리뷰 및 검증**
@../docs/GEMINI_REVIEW_GUIDE.md
@../docs/DYNAMIC_HOOKS_GUIDE.md

---

## 프로젝트 원칙

### 아키텍처
- **헥사고날 아키텍처** (Ports & Adapters)
- **도메인 주도 설계** (DDD)
- **CQRS** 패턴 적용

### 코드 품질
- **SOLID 원칙** 준수
- **Law of Demeter** (데미터의 법칙)
- **Spring Proxy Limitations** 인지

### 트랜잭션 관리
- `@Transactional` 메서드는 외부 API 호출 금지
- Private/Final 메서드에 `@Transactional` 사용 금지
- 내부 메서드 호출 시 프록시 우회 주의

---

## 작업 시 주의사항

1. **코드 생성 전**: `CODING_STANDARDS.md`와 `ENTERPRISE_SPRING_STANDARDS_PROMPT.md` 확인
2. **아키텍처 결정 시**: DDD 가이드 및 DTO 패턴 가이드 참조
3. **예외 처리**: `EXCEPTION_HANDLING_GUIDE.md` 패턴 준수
4. **코드 리뷰 후**: Gemini 리뷰 가이드에 따라 체계적 개선

---

## 자동화 도구

### Git Hooks
- **Pre-commit**: 트랜잭션 경계 검증, 프록시 제약사항 검증
- 위치: `hooks/pre-commit`, `hooks/validators/`

### ArchUnit Tests
- 아키텍처 규칙 자동 검증
- 위치: `application/src/test/java/com/company/template/architecture/`

### Slash Commands
- `/gemini-review`: Gemini 코드 리뷰 분석
- `/jira-task`: Jira 태스크 분석 및 브랜치 생성

---

**이 프로젝트의 모든 코드는 위 문서들의 표준을 따라야 합니다.**
