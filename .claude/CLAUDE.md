# Spring Standards Project - Claude Code Configuration

이 프로젝트는 **Spring Boot 3.3.x + Java 21** 기반의 헥사고날 아키텍처 엔터프라이즈 표준 프로젝트입니다.

---

## 📚 필수 표준 문서 (코드 작성 전 반드시 참조)

### 🎯 핵심 표준 (요약본 - 자동 로드)

**시작 시 먼저 읽을 문서:**
@../docs/CODING_STANDARDS_SUMMARY.md
@../docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md

### 📖 상세 가이드 (필요 시 참조 - 링크만)

**아키텍처 및 설계 패턴:**
- [DDD Aggregate Migration Guide](../docs/DDD_AGGREGATE_MIGRATION_GUIDE.md)
- [DTO Patterns Guide](../docs/DTO_PATTERNS_GUIDE.md)

**구현 가이드:**
- [Exception Handling Guide](../docs/EXCEPTION_HANDLING_GUIDE.md)
- [Java Record Guide](../docs/JAVA_RECORD_GUIDE.md)

**코드 리뷰:**
- [Gemini Review Guide](../docs/GEMINI_REVIEW_GUIDE.md)

---

## 🏗️ 프로젝트 핵심 원칙

### 1. 아키텍처 패턴
- **헥사고날 아키텍처** (Ports & Adapters) - 의존성 역전
- **도메인 주도 설계** (DDD) - Aggregate 중심 설계
- **CQRS** - Command/Query 분리

### 2. 코드 품질 규칙
- **SOLID 원칙** 준수 (특히 SRP, DIP)
- **Law of Demeter** - Getter 체이닝 금지 (`order.getCustomer().getAddress()` ❌)
- **Lombok 금지** - Plain Java 사용 (Zero-tolerance 규칙)

### 3. Spring 프록시 제약사항 (중요!)
⚠️ **다음 경우 `@Transactional`이 작동하지 않습니다:**
- Private 메서드
- Final 클래스/메서드
- 같은 클래스 내부 호출 (`this.method()`)

### 4. 트랜잭션 관리 규칙
- ❌ `@Transactional` 메서드 내에서 외부 API 호출 금지
- ❌ Private/Final 메서드에 `@Transactional` 사용 금지
- ✅ 트랜잭션은 짧게 유지, 외부 호출은 트랜잭션 밖에서

---

## 🎯 코드 작성 체크리스트

### 코드 생성 전
1. **모듈 파악**: Domain/Application/Adapter 중 어디에 속하는가?
2. **요약본 확인**: 위 핵심 표준 문서에서 해당 모듈 규칙 확인
3. **상세 가이드**: 복잡한 경우 하단 "상세 문서" 섹션 참조

### 아키텍처 결정 시
- **DDD 설계**: `DDD_AGGREGATE_MIGRATION_GUIDE.md` 참조
- **DTO 변환**: `DTO_PATTERNS_GUIDE.md`의 `from()`/`toXxx()` 패턴 준수
- **예외 처리**: `EXCEPTION_HANDLING_GUIDE.md`의 Domain Exception 패턴

### 코드 리뷰 후
- **Gemini 리뷰**: `/gemini-review` 실행하여 체계적 개선

---

## 🔧 자동화 도구 (코드 품질 보장)

### Claude Code Dynamic Hooks (최적화됨)
- **SessionStart Hook**: 프로젝트 컨텍스트 로딩 (요약본 우선)
- **UserPromptSubmit Hook**: 모듈별 가이드라인 자동 주입
- **위치**: `.claude/hooks/scripts/`

### Git Pre-commit Hooks
- **트랜잭션 경계 검증**: `@Transactional` 내 외부 API 호출 차단
- **프록시 제약사항 검증**: Private/Final 메서드 `@Transactional` 차단
- **위치**: `hooks/pre-commit`, `hooks/validators/`

### ArchUnit Tests
- **아키텍처 규칙 자동 검증**: 레이어 의존성, 네이밍 규칙
- **위치**: `application/src/test/java/com/company/template/architecture/`

### Slash Commands
- `/gemini-review [pr-number]`: Gemini 코드 리뷰 분석 및 리팩토링 전략 생성
- `/jira-task`: Jira 태스크 분석 및 브랜치 생성

---

## 📖 전체 문서 (상세 규칙 - 링크만)

### 전체 버전 코딩 표준 (상세 규칙)
- [CODING_STANDARDS.md](../docs/CODING_STANDARDS.md) - 2,676줄, 87개 규칙
- [ENTERPRISE_SPRING_STANDARDS_PROMPT.md](../docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md) - 3,361줄, 엔터프라이즈 패턴

### 프로젝트 설정 및 관리
- [CUSTOMIZATION_GUIDE.md](../docs/CUSTOMIZATION_GUIDE.md) - 프로젝트 커스터마이징
- [SETUP_SUMMARY.md](../docs/SETUP_SUMMARY.md) - 초기 설정 가이드
- [VERSION_MANAGEMENT_GUIDE.md](../docs/VERSION_MANAGEMENT_GUIDE.md) - 버전 관리 전략
- [DYNAMIC_HOOKS_GUIDE.md](../docs/DYNAMIC_HOOKS_GUIDE.md) - Claude Code 훅 시스템

---

## 🚨 Zero-Tolerance 규칙

다음 규칙은 **예외 없이** 반드시 준수해야 합니다:

1. **Lombok 금지** - `@Data`, `@Builder`, `@Getter`, `@Setter` 등 모두 금지
2. **Javadoc 필수** - 모든 public 클래스/메서드에 `@author`, `@since` 포함
3. **Scope 준수** - 요청된 코드만 작성, 추가 기능 제안 금지
4. **트랜잭션 경계** - `@Transactional` 내 외부 API 호출 절대 금지

---

**✅ 이 프로젝트의 모든 코드는 위 표준을 따라야 합니다.**
