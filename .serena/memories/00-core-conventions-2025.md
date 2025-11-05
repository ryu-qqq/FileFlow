# Spring Standards 핵심 컨벤션 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 핵심 Zero-Tolerance 규칙 및 전체 개요
> **상세 규칙**: Hook이 자동으로 146개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance 규칙 (절대 위반 금지)

### 1. Lombok 금지 (모든 레이어)
- ❌ `@Data`, `@Builder`, `@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor` 전부 금지
- ✅ **Pure Java getter/setter 직접 작성** (특히 Domain Layer에서 엄격)
- **검증**: validation-helper.py가 자동 감지

### 2. Law of Demeter (Domain Layer)
- ❌ Getter 체이닝: `order.getCustomer().getAddress().getZipCode()`
- ✅ **Tell, Don't Ask**: `order.getCustomerZipCode()`
- **검증**: Anti-pattern 정규식 매칭

### 3. Long FK 전략 (Persistence Layer)
- ❌ JPA 관계 어노테이션: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
- ✅ **Long FK 사용**: `private Long userId;`
- **검증**: JPA 관계 어노테이션 감지

### 4. Transaction 경계 (Application Layer)
- ❌ `@Transactional` 내 외부 API 호출 (RestTemplate, WebClient 등)
- ✅ **트랜잭션은 짧게 유지, 외부 호출은 트랜잭션 밖에서**
- ✅ **외부 API 호출 시 Transactional Outbox Pattern 사용 (Pattern B 권장)**
- **검증**: Git pre-commit hook

### 5. Spring Proxy 제약사항 (Application Layer)
- ❌ Private 메서드에 `@Transactional`
- ❌ Final 클래스/메서드에 `@Transactional`
- ❌ 같은 클래스 내부 호출 (`this.method()`)
- ✅ **Public 메서드만 `@Transactional` 적용**
- **검증**: Git pre-commit hook

### 6. Orchestration Pattern (Application Layer - Orchestration)
- ❌ `executeInternal()`에 `@Transactional` 사용
- ✅ **`executeInternal()`에 `@Async` 필수**, 트랜잭션 밖에서 외부 API 호출
- ❌ Command에 Lombok (`@Data`, `@Builder` 등)
- ✅ **Command는 Record 패턴 사용** (`public record XxxCommand`)
- ❌ Operation Entity에 IdemKey Unique 제약 없음
- ✅ **`@UniqueConstraint(columnNames = {"idem_key"})` 필수**
- ❌ Orchestrator가 `boolean`/`void` 반환 또는 Exception throw
- ✅ **Orchestrator는 `Outcome` (Ok/Retry/Fail) 반환**
- **검증**: validation-helper.py, ArchUnit, Git pre-commit hook

### 7. Javadoc 필수 (모든 Public 클래스/메서드)
- ❌ `@author`, `@since` 없는 public 클래스/메서드
- ✅ **모든 public 클래스/메서드에 Javadoc 포함**
- **검증**: Checkstyle

### 8. Scope 준수 (요청된 코드만 작성)
- ❌ 요청하지 않은 추가 기능 구현
- ✅ **요청된 코드만 정확히 작성** (MVP First)
- **검증**: 수동 코드 리뷰

---

## 📊 전체 레이어 개요

| 레이어 | 파일 수 | Cache Rules | 핵심 카테고리 |
|--------|---------|-------------|--------------|
| **01-adapter-rest-api** | 28 | 27개 | Controller, DTO, Exception, Mapper, Testing |
| **02-domain** | 17 | 17개 | Aggregate, Law of Demeter, Testing |
| **03-application** | 20 | 20개 | Assembler, UseCase, Transaction, Facade, Testing |
| **04-persistence** | 30 | 27개 | JPA Entity, QueryDSL, Repository, Command/Query Adapter |
| **05-testing** | 14 | 14개 | ArchUnit, Integration, Multi-Module Testing |
| **06-java21-patterns** | 15 | 15개 | Record, Sealed Classes, Virtual Threads |
| **07-enterprise-patterns** | 10 | 10개 | Caching, Event-Driven, Resilience |
| **08-error-handling** | 5 | 5개 | Strategy, Domain Exception, Global Handler |
| **09-orchestration** | 11 | 11개 | Command, Idempotency, WAL, Outcome |
| **TOTAL** | **150** | **146** | **98개 규칙 → JSON Cache 변환** |

---

## 🎯 아키텍처 핵심 원칙

### 1. 헥사고날 아키텍처 (Ports & Adapters)
- **의존성 역전**: Domain → Application → Adapter
- **Port 인터페이스**: In Port (UseCase), Out Port (Repository, External API)
- **Adapter 구현**: REST API, Persistence, External API

### 2. 도메인 주도 설계 (DDD)
- **Aggregate 중심 설계**: 비즈니스 불변식 보호
- **Bounded Context**: 명확한 경계 설정
- **Entity vs Value Object**: 식별성 vs 값 동등성

### 3. CQRS (Command/Query 분리)
- **Command**: 상태 변경 (Write)
- **Query**: 상태 조회 (Read)
- **분리 이유**: 성능 최적화, 확장성

---

## 🔥 Dynamic Hooks + Cache 시스템

### 실시간 규칙 자동 주입 (A/B 테스트 검증 완료)

```
사용자: "domain aggregate 작업"
    ↓
Hook: "domain" 키워드 감지 (30점)
    ↓
Cache: Domain Layer 규칙 17개 자동 주입 (O(1) 검색)
    ↓
Claude: Domain Layer 규칙 100% 준수 코드 생성
```

### 성능 메트릭 (A/B 테스트 검증)
- **컨벤션 위반**: 40회 → 0회 (100% 제거) ✅
- **Zero-Tolerance 준수율**: 0% → 100% ✅
- 토큰 사용량: 90% 절감 (50,000 → 500-1,000)
- 검증 속도: 73.6% 향상 (561ms → 148ms)
- Orchestration 생성: 75% 시간 단축 (8분 → 2분)

### Hook 시스템 통합
- **user-prompt-submit.sh**: 키워드 감지 → Layer 매핑 → 규칙 자동 주입
- **after-tool-use.sh**: 코드 생성 직후 실시간 검증
- **validation-helper.py**: Cache 기반 고속 검증 (148ms)

---

## 📚 레이어별 Memory 파일

1. **01-adapter-rest-api-rules.md** (27개 규칙)
2. **02-domain-layer-rules.md** (17개 규칙)
3. **03-application-layer-rules.md** (20개 규칙)
4. **04-persistence-layer-rules.md** (27개 규칙)
5. **05-testing-rules.md** (14개 규칙)
6. **06-java21-patterns.md** (15개 규칙)
7. **07-enterprise-patterns.md** (10개 규칙)
8. **08-error-handling-patterns.md** (5개 규칙)
9. **09-orchestration-patterns.md** (11개 규칙)

**총 10개 Memory 파일 (이 파일 포함) → 146개 Cache Rules 실시간 주입!**

---

## 🔗 참고 문서

### 튜토리얼
- [Getting Started](docs/tutorials/01-getting-started.md) - 시작 가이드 (5분)

### Dynamic Hooks 시스템
- [DYNAMIC_HOOKS_GUIDE.md](docs/DYNAMIC_HOOKS_GUIDE.md) - 전체 시스템 가이드
- [Cache README](.claude/cache/rules/README.md) - Cache 시스템 상세

### Slash Commands
- [Commands README](.claude/commands/README.md) - 모든 명령어 설명
- `/validate-architecture` - 전체 아키텍처 검증 (ArchUnit)
- `/ai-review [pr-number]` - 통합 AI 리뷰 (Gemini + CodeRabbit + Codex)
- `/jira-task` - Jira Task 분석 및 브랜치 생성

### 코딩 규칙
- [Coding Convention](docs/coding_convention/) - 98개 규칙 (Layer별)

---

**✅ 이 파일은 2025-11-05 기준 최신 컨벤션을 요약합니다.**

**🔥 전체 146개 규칙은 Hook이 실시간으로 자동 주입하므로 수동 로드 불필요!**
