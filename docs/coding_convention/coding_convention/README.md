# Coding Convention

Spring Boot 3.5.x + Java 21 기반 헥사고날 아키텍처 코딩 규칙 (98개 규칙)

---

## 📚 규칙 구조 (Layer별)

### 01. Adapter-Rest-API Layer (18개 규칙)

**위치**: `01-adapter-rest-api-layer/`

**주요 규칙**:
- Controller Thin (비즈니스 로직 금지)
- GlobalExceptionHandler (통합 예외 처리)
- ApiResponse 표준화
- DTO Validation (@Valid, @NotNull 등)

**하위 디렉토리**:
- `controller-design/` - Controller 설계 원칙
- `dto-patterns/` - Request/Response DTO 패턴
- `exception-handling/` - 예외 처리 전략
- `mapper-patterns/` - Controller ↔ UseCase 매퍼
- `package-guide/` - 패키지 구조
- `testing/` - Controller 테스트

---

### 02. Domain Layer (15개 규칙)

**위치**: `02-domain-layer/`

**Zero-Tolerance 규칙**:
- ❌ **Lombok 금지**: @Data, @Builder 등 모두 금지
- ❌ **Law of Demeter**: Getter 체이닝 금지 (`order.getCustomer().getAddress()`)
- ✅ **Tell, Don't Ask**: `order.getCustomerZipCode()`

**하위 디렉토리**:
- `aggregate-design/` - Aggregate Root 설계
- `law-of-demeter/` - Law of Demeter 원칙
- `package-guide/` - 패키지 구조
- `testing/` - Domain 테스트

---

### 03. Application Layer (18개 규칙)

**위치**: `03-application-layer/`

**Zero-Tolerance 규칙**:
- ❌ **Transaction 경계**: `@Transactional` 내 외부 API 호출 금지
- ⚠️ **Spring 프록시 제약**:
  - Private 메서드에 `@Transactional` 무효
  - Final 클래스/메서드에 `@Transactional` 무효
  - 같은 클래스 내부 호출 (`this.method()`) 무효

**하위 디렉토리**:
- `assembler-pattern/` - Domain ↔ DTO 변환
- `dto-patterns/` - Command/Query DTO
- `package-guide/` - 패키지 구조
- `testing/` - UseCase 테스트
- `transaction-management/` - Transaction 경계 관리
- `usecase-design/` - UseCase 설계

---

### 04. Persistence Layer (10개 규칙)

**위치**: `04-persistence-layer/`

**Zero-Tolerance 규칙**:
- ❌ **JPA 관계 금지**: @ManyToOne, @OneToMany, @OneToOne, @ManyToMany
- ✅ **Long FK 전략**: `private Long userId;` (Long FK 사용)

**하위 디렉토리**:
- `jpa-entity-design/` - Entity 설계 (Long FK 전략)
- `package-guide/` - 패키지 구조
- `querydsl-optimization/` - QueryDSL 최적화
- `repository-patterns/` - Repository 패턴
- `testing/` - Persistence 테스트

---

### 05. Testing (12개 규칙)

**위치**: `05-testing/`

**주요 규칙**:
- ArchUnit 아키텍처 테스트
- Integration 테스트 전략
- Test Fixture 관리

**하위 디렉토리**:
- `archunit-rules/` - ArchUnit 테스트
- `integration-testing/` - 통합 테스트

---

### 06. Java 21 Patterns (8개 규칙)

**위치**: `06-java21-patterns/`

**주요 규칙**:
- Record 패턴
- Sealed Classes
- Virtual Threads

**하위 디렉토리**:
- `record-patterns/` - Record 패턴
- `sealed-classes/` - Sealed Classes
- `virtual-threads/` - Virtual Threads

---

### 07. Enterprise Patterns (5개 규칙)

**위치**: `07-enterprise-patterns/`

**주요 규칙**:
- Caching 전략
- Event-Driven Architecture
- Resilience 패턴

**하위 디렉토리**:
- `caching/` - 캐싱 전략
- `event-driven/` - 이벤트 기반 아키텍처
- `resilience/` - 복원력 패턴

---

### 08. Error Handling (5개 규칙)

**위치**: `08-error-handling/`

**주요 규칙**:
- Domain Exception 설계
- GlobalExceptionHandler
- ErrorCode 관리

**하위 디렉토리**:
- `error-handling-strategy/` - 예외 처리 전략
- `domain-exception-design/` - Domain 예외 설계
- `global-exception-handler/` - 전역 예외 핸들러
- `error-response-format/` - 에러 응답 포맷
- `errorcode-management/` - ErrorCode 관리

---

### 09. Orchestration Patterns (8개 규칙) ⭐ NEW

**위치**: `09-orchestration-patterns/`

**목적**: 외부 API 호출의 안전한 멱등성 보장 및 크래시 복구

**핵심 개념**:
- **3-Phase Lifecycle**: Accept → Execute → Finalize
- **Idempotency**: IdemKey + DB Unique 제약으로 중복 방지
- **Write-Ahead Log (WAL)**: 크래시 복구 (Finalizer/Reaper)
- **Outcome Modeling**: Sealed interface (Ok/Retry/Fail)

**Zero-Tolerance 규칙**:
- ❌ `executeInternal()`에 `@Transactional` 사용
- ✅ `executeInternal()`에 `@Async` 필수 (외부 API는 트랜잭션 밖에서)
- ❌ Command에 Lombok 사용
- ✅ Command는 Record 패턴 (`public record XxxCommand`)
- ❌ Operation Entity에 IdemKey Unique 제약 없음
- ✅ `@UniqueConstraint(columnNames = {"idem_key"})` 필수
- ❌ Orchestrator가 `boolean`/`void` 반환 또는 Exception throw
- ✅ Orchestrator는 `Outcome` (Ok/Retry/Fail) 반환

**하위 디렉토리**:
- `overview/` - 3-Phase Lifecycle, Idempotency, WAL 개요
- `command-pattern/` - Record 패턴, Compact Constructor
- `idempotency-handling/` - IdemKey, Race Condition 방지
- `write-ahead-log/` - 크래시 복구, Finalizer/Reaper
- `outcome-modeling/` - Sealed interface, Pattern matching
- `quick-start-guide/` - 10분 실습
- `security-guide/` - Rate Limiting, DoS 방지
- `automation-analysis/` - 80-85% 자동화 분석

**자동 생성 Command**:
```bash
/code-gen-orchestrator <Domain> <EventType>

# 예시:
/code-gen-orchestrator Order PlacementConfirmed
```

**자동 생성 파일 (10개, 80-85% 완성)**:
1. `OrderPlacementConfirmedOrchestrator.java` - 3-Phase Lifecycle 관리 (@Async)
2. `OrderPlacementConfirmedCommand.java` - Immutable Command (Record)
3. `OrderPlacementConfirmedOperationEntity.java` - WAL Entity (@UniqueConstraint)
4. `OrderPlacementConfirmedFinalizer.java` - PENDING 처리 (@Scheduled)
5. `OrderPlacementConfirmedReaper.java` - TIMEOUT 처리 (@Scheduled)
6. `OrderPlacementConfirmedOutcome.java` - 결과 모델링 (Sealed)
7. `OrderPlacementConfirmedMapper.java` - Command → Domain 변환
8. `OrderPlacementConfirmedOperationRepository.java` - JPA Repository
9. `OrderPlacementConfirmedOperationStatus.java` - 상태 Enum
10. `OrderPlacementConfirmedWriteAheadLog.java` - WAL 인터페이스

**개발자 작업 (15-20%)**:
- `executeInternal()`: 외부 API 호출 비즈니스 로직
- `Mapper`: Command → Domain Entity 변환 로직
- `Outcome`: 성공/재시도/실패 판단 조건

**자동 검증 (3-Tier)**:
1. **Real-time** (validation-helper.py): 코드 생성 직후 즉시 검증
2. **Commit-time** (Git pre-commit hook): 커밋 시 차단
3. **Build-time** (ArchUnit): 빌드 시 강제 실패 (12개 규칙)

**성능 메트릭**:
- 생성 시간: 8분 → 2분 (75% 단축)
- 컨벤션 위반: 평균 12회 → 0-2회 (83-100% 감소)
- 개발자 집중: Boilerplate → 비즈니스 로직

---

## 📊 전체 통계

| Layer | 규칙 수 | Zero-Tolerance | 검증 도구 |
|-------|---------|----------------|----------|
| Adapter-Rest-API | 18 | GlobalException | ArchUnit, Cache |
| Domain | 15 | Lombok, Law of Demeter | ArchUnit, Cache |
| Application | 18 | Transaction 경계 | Git Hook, ArchUnit |
| Persistence | 10 | Long FK | ArchUnit, Cache |
| Testing | 12 | - | ArchUnit |
| Java 21 Patterns | 8 | - | Cache |
| Enterprise | 5 | - | Cache |
| Error Handling | 5 | - | Cache |
| **Orchestration** ⭐ | **8** | **@Async, Record, Outcome** | **Git Hook, ArchUnit (12), Cache** |
| **총합** | **98** | - | - |

---

## 🔧 자동화 시스템

### 1. Cache 시스템

**목적**: 98개 규칙 → JSON → O(1) 검색 (90% 토큰 절감)

**작동**:
```bash
# Cache 빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 결과:
# .claude/cache/rules/
#   ├── index.json (마스터 인덱스)
#   ├── domain-layer-*.json (15개)
#   ├── application-layer-*.json (18개)
#   ├── orchestration-patterns-*.json (8개) ⭐ NEW
#   └── ...
```

### 2. Serena Memory

**목적**: 코딩 컨벤션을 Serena MCP 메모리에 저장 → 세션 간 컨텍스트 유지

**설정**:
```bash
# 1. 메모리 생성 (1회 실행)
bash .claude/hooks/scripts/setup-serena-conventions.sh

# 2. Claude Code 세션 시작 시
/cc:load  # 코딩 컨벤션 자동 로드
```

### 3. 3-Tier 검증 시스템

**Tier 1: Real-time (validation-helper.py)**
- 코드 생성 직후 즉시 검증
- 148ms (73.6% 향상)

**Tier 2: Commit-time (Git pre-commit hook)**
- 커밋 시 자동 차단
- Transaction 경계 + Orchestration 검증

**Tier 3: Build-time (ArchUnit)**
- 빌드 시 강제 실패
- 아키텍처 규칙 + Orchestration 규칙 (12개)

---

## 📖 참고 문서

- [Dynamic Hooks Guide](../DYNAMIC_HOOKS_GUIDE.md) - 전체 자동화 시스템
- [LangFuse Integration](../LANGFUSE_INTEGRATION_GUIDE.md) - 효율 측정 및 A/B 테스트
- [Orchestration Quick Start](./09-orchestration-patterns/quick-start-guide/01_10-minute-tutorial.md) - 10분 실습

---

**✅ 모든 코드는 위 98개 규칙을 준수해야 합니다.**

**💡 핵심**: Dynamic Hooks + Cache + Serena Memory 시스템이 자동으로 규칙을 주입하고 검증하므로, 개발자는 비즈니스 로직에 집중할 수 있습니다!
