# 🚀 Claude Code + Cursor AI 통합 개발 가이드

> **단일 진실 공급원 (Single Source of Truth)**: `.claude/cache/rules/` (98개 규칙 JSON Cache)

---

## 📋 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [전체 워크플로우](#전체-워크플로우)
3. [구현 로드맵](#구현-로드맵)
4. [즉시 시작 가능한 작업](#즉시-시작-가능한-작업)
5. [커맨드 가이드](#커맨드-가이드)
6. [Git Worktree 전략](#git-worktree-전략)

---

## 🎯 아키텍처 개요

### 역할 분리

```
┌─────────────────────────────────────────────────────────┐
│ Claude Code: 분석, 설계, 비즈니스 로직, 검증            │
├─────────────────────────────────────────────────────────┤
│ - PRD 작성                                               │
│ - Technical Spec 작성                                    │
│ - 복잡한 비즈니스 로직 구현                              │
│ - 테스트 작성 및 검증                                    │
│ - 코드 컨벤션 검증                                       │
└─────────────────────────────────────────────────────────┘
           ↕ (작업지시서)
┌─────────────────────────────────────────────────────────┐
│ Cursor AI: Boilerplate 빠른 생성                         │
├─────────────────────────────────────────────────────────┤
│ - Domain Aggregate 스켈레톤                              │
│ - UseCase 스켈레톤                                       │
│ - Controller 스켈레톤                                    │
│ - Entity, Repository 스켈레톤                            │
└─────────────────────────────────────────────────────────┘
```

### 단일 진실 공급원

**❌ 제거:**
- Serena Memory (`.serena/memories/`) - 캐시와 중복

**✅ 유지:**
- `.claude/cache/rules/` - 98개 규칙 JSON Cache (O(1) 검색)
- `docs/coding_convention/` - 원본 마크다운 (참조용)

**이유:**
- Serena Memory와 Cache가 중복되어 **단일 진실 공급원** 원칙 위반
- Cache만으로도 90% 토큰 절감 + 73.6% 속도 향상 달성
- Cursor AI는 `docs/` 마크다운 직접 참조 가능

---

## 🔄 전체 워크플로우

### 1. Claude Code: 설계 및 작업지시서 생성

```bash
# Main 디렉토리 (~/claude-spring-standards)
cd ~/claude-spring-standards

# Jira Task 분석
/jira-task PROJ-123

# 설계 분석 및 작업지시서 생성
/design-analysis Order

# 출력:
# → .claude/work-orders/order-aggregate.md (Cursor 작업지시서)
# → .claude/work-queue.json 업데이트 (작업 큐)
```

### 2. Git Worktree: 독립 작업 환경 생성

```bash
# Worktree 생성 (자동)
/queue-start task-001

# 실행 내역:
# 1. git worktree add ../wt-order feature/order-aggregate
# 2. cp .claude/work-orders/order-aggregate.md ../wt-order/
# 3. Cursor AI에게 알림
```

### 3. Cursor AI: Boilerplate 빠른 생성

```bash
# Worktree 디렉토리 (~/wt-order)
cd ~/wt-order

# Cursor AI 작업:
# 1. .cursorrules 자동 로드 (Zero-Tolerance 규칙)
# 2. docs/coding_convention/ 참조
# 3. work-orders/order-aggregate.md 읽기
# 4. Boilerplate 생성:
#    - OrderDomain.java
#    - OrderId.java
#    - OrderStatus.java
# 5. Git Commit
#    → Git Hook 실행 (.claude/cursor-changes.md 자동 생성)
```

### 4. Claude Code: 검증 및 비즈니스 로직

```bash
# Main 디렉토리로 복귀
cd ~/claude-spring-standards

# Cursor 변경 검증
/validate-cursor-changes

# 실행 내역:
# 1. .claude/cursor-changes.md 읽기
# 2. validation-helper.py 실행
# 3. ArchUnit 테스트 실행
# 4. 위반 시 리포트 생성

# 검증 통과 후 비즈니스 로직 구현
/implement-logic OrderDomain.java

# 테스트 자동 생성
/generate-tests OrderDomain.java --with-states --vip

# Fixture 자동 생성
/generate-fixtures Order --all
```

### 5. Git Merge: Worktree → Main

```bash
# Worktree 제거 및 Merge
/queue-complete task-001

# 실행 내역:
# 1. git worktree remove ../wt-order
# 2. git merge feature/order-aggregate
# 3. gh pr create (자동 PR 생성)
```

---

## 🗓️ 구현 로드맵

### Phase 1: 기본 인프라 (1-2일) ✅ 우선순위 높음

| 작업 | 설명 | 상태 |
|------|------|------|
| 1. Serena 메모리 제거 | `.serena/`, `user-prompt-submit.sh` 수정 | ⏳ 진행 중 |
| 2. `.cursorrules` 작성 | Zero-Tolerance + 문서 참조 | ⏳ 진행 중 |
| 3. Claude Skill 작성 | `design-analysis.md`, `business-logic.md` | ⏳ 진행 중 |

### Phase 2: 자동화 커맨드 (2-3일) ✅ 완료

| 작업 | 설명 | 상태 |
|------|------|------|
| 4. `/design-analysis` | 설계 분석 + 작업지시서 생성 (129 lines) | ✅ 완료 |
| 5. `/generate-fixtures` | 템플릿 기반 Fixture 자동 생성 (178 lines) | ✅ 완료 |
| 6. `/validate-cursor-changes` | Cursor 변경 자동 검증 (196 lines) | ✅ 완료 |

### Phase 3: Worktree + 큐 시스템 (3-5일) ✅ 완료

| 작업 | 설명 | 상태 |
|------|------|------|
| 7. Worktree 자동화 | `.claude/scripts/worktree-manager.sh` (220 lines) | ✅ 완료 |
| 8. 작업 큐 시스템 | `.claude/work-queue.json` + `queue-manager.py` (280 lines) | ✅ 완료 |
| 9. 큐 커맨드 (5개) | `/queue-add`, `/queue-start`, `/queue-complete`, `/queue-list`, `/queue-status` | ✅ 완료 |

### Phase 4: Git Hook 통합 (1-2일)

| 작업 | 설명 | 상태 |
|------|------|------|
| 10. Cursor 변경 추적 Hook | Git post-commit hook | 📋 대기 |
| 11. 자동 검증 리포트 | 컨벤션 위반 자동 리포트 | 📋 대기 |

---

## ⚡ 즉시 시작 가능한 작업

### 작업 1: Serena 메모리 제거 (15분)

**목표**: 단일 진실 공급원 (Cache만 사용)

**실행:**
```bash
# 1. Serena 메모리 디렉토리 삭제
rm -rf .serena/memories/

# 2. Hook 스크립트 수정
vim .claude/hooks/user-prompt-submit.sh
# → Serena 로드 부분 제거

# 3. setup 스크립트 제거
rm .claude/hooks/scripts/setup-serena-conventions.sh

# 4. /cc:load 커맨드 제거 또는 수정
rm .claude/commands/cc/load.md
# 또는 Cache 전용으로 수정
```

**검증:**
```bash
# Hook 로그 확인 (Serena 로드 없어야 함)
tail -f .claude/hooks/logs/hook-execution.jsonl
```

---

### 작업 2: `.cursorrules` 정적 파일 작성 (20분)

**목표**: Cursor AI가 자동으로 읽는 Zero-Tolerance 규칙

**실행:**
```bash
# 루트에 .cursorrules 파일 생성
vim .cursorrules
```

**내용:**
```markdown
# Spring DDD Standards - Cursor AI Rules

> **컨벤션 원본**: `docs/coding_convention/` (98개 규칙)
> **Cache**: `.claude/cache/rules/` (JSON 검색용)

---

## 🚨 Zero-Tolerance (절대 금지)

### 1. Lombok 금지
- ❌ `@Data`, `@Builder`, `@Getter`, `@Setter`, `@AllArgsConstructor`
- ✅ Pure Java getter/setter 직접 작성

### 2. Law of Demeter (Getter 체이닝 금지)
- ❌ `order.getCustomer().getAddress().getZipCode()`
- ✅ `order.getCustomerZipCode()` (Tell, Don't Ask)

### 3. Long FK Strategy (JPA 관계 금지)
- ❌ `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
- ✅ `private Long userId;` (Long FK 사용)

### 4. Transaction 경계
- ❌ `@Transactional` 내 외부 API 호출 (RestTemplate, WebClient)
- ✅ 트랜잭션은 짧게 유지, 외부 호출은 밖에서

### 5. Javadoc 필수
- ❌ `@author`, `@since` 없는 public 클래스/메서드
- ✅ 모든 public 클래스/메서드에 Javadoc

---

## 📋 필수 규칙

### Domain Layer
- ✅ Aggregate Root 패턴
- ✅ Value Object (Immutable)
- ✅ Domain Event (`AbstractAggregateRoot`)
- ✅ Factory Pattern (복잡한 생성 로직)

### Application Layer
- ✅ UseCase Single Responsibility
- ✅ Command/Query 분리 (CQRS)
- ✅ `@Transactional` 경계 명확히
- ✅ Assembler (Domain ↔ DTO 변환)

### Persistence Layer
- ✅ CQRS (Command/Query Repository 분리)
- ✅ QueryDSL (복잡한 조회)
- ✅ N+1 방지 (Fetch Join, Batch Size)

### REST API Layer
- ✅ Controller Thin (비즈니스 로직 없음)
- ✅ GlobalExceptionHandler
- ✅ ApiResponse 표준화

---

## 🔗 상세 규칙 참조

Cursor AI는 아래 디렉토리의 마크다운 파일을 자동으로 읽을 수 있습니다:

- **Domain**: `docs/coding_convention/02-domain-layer/`
- **Application**: `docs/coding_convention/03-application-layer/`
- **Persistence**: `docs/coding_convention/04-persistence-layer/`
- **REST API**: `docs/coding_convention/01-adapter-rest-api-layer/`
- **Testing**: `docs/coding_convention/05-testing/`
- **Java 21**: `docs/coding_convention/06-java21-patterns/`
- **Enterprise**: `docs/coding_convention/07-enterprise-patterns/`
- **Orchestration**: `docs/coding_convention/09-orchestration-patterns/`

작업 중 궁금한 규칙이 있으면 해당 디렉토리 파일을 참조하세요.

---

## 💡 작업 패턴

### Aggregate 생성 시
1. `XxxDomain.java` (Aggregate Root)
2. `XxxId.java` (Value Object)
3. `XxxStatus.java` (Enum)
4. Factory 메서드 (복잡한 생성 로직)
5. Domain Event 등록 (`registerEvent()`)

### UseCase 생성 시
1. `XxxUseCase.java` (port/in/)
2. `XxxCommand.java` (dto/command/)
3. `XxxResponse.java` (dto/response/)
4. `@Transactional` 경계 명확히

### Repository 생성 시
1. Command: `XxxCommandRepository.java`
2. Query: `XxxQueryRepository.java`
3. QueryDSL: `XxxQueryRepositoryImpl.java`

---

## 🎯 Claude Code 검증

Cursor AI로 코드 생성 후, Claude Code가 다음을 검증합니다:

1. **validation-helper.py**: Cache 기반 컨벤션 검증
2. **ArchUnit**: 레이어 의존성, 네이밍 규칙
3. **Git Pre-commit Hook**: Transaction 경계 검증

**위반 시 자동 리포트 생성**
```

**검증:**
```bash
# Cursor IDE에서 확인
# → .cursorrules가 자동으로 로드됨
```

---

### 작업 3: 첫 번째 Claude Skill 작성 (30분)

**목표**: 설계 분석 스킬 (`design-analysis.md`)

**실행:**
```bash
# Skills 디렉토리 생성
mkdir -p .claude/skills

# 스킬 파일 작성
vim .claude/skills/design-analysis.md
```

**내용:**
```markdown
# Design Analysis Skill

당신은 **Spring DDD 설계 전문가**입니다.

## 역할

- PRD 기반 Technical Spec 생성
- Domain 모델 설계 (Aggregate, Value Object, Domain Event)
- UseCase 경계 정의 (Command/Query 분리)
- API 명세 설계 (Request/Response DTO)

## 자동 로드 규칙

- `docs/coding_convention/` 참조 (98개 규칙)
- `.claude/cache/rules/` 활용 (O(1) 검색)

## 출력 형식: Cursor AI 작업지시서

설계 완료 후, Cursor AI가 바로 사용할 수 있는 작업지시서를 생성합니다.

### 작업지시서 구조

```markdown
# 작업지시서: {Feature Name}

## 📋 생성할 파일

- `domain/XxxDomain.java` (Aggregate Root)
- `domain/XxxId.java` (Value Object)
- `domain/XxxStatus.java` (Enum)
- `application/port/in/XxxUseCase.java`
- `application/dto/command/XxxCommand.java`
- `application/dto/response/XxxResponse.java`

## ✅ 필수 규칙 (Zero-Tolerance)

- ❌ Lombok 금지 → Pure Java
- ❌ Getter 체이닝 금지 → Tell, Don't Ask
- ❌ JPA 관계 어노테이션 금지 → Long FK
- ✅ Javadoc 필수 (`@author`, `@since`)

## 🎯 Domain 스켈레톤

```java
/**
 * {Aggregate} Domain Aggregate
 *
 * @author {Your Name}
 * @since 1.0
 */
public class XxxDomain extends AbstractAggregateRoot<XxxDomain> {
    private final XxxId id;
    private XxxStatus status;

    // Factory Method
    public static XxxDomain create(...) {
        // TODO: 생성 로직
    }

    // Business Methods (스켈레톤만, 로직은 Claude Code가 작성)
    public void doSomething() {
        // TODO: 비즈니스 로직 (Claude Code 작업)
    }
}
```

## 🎯 UseCase 스켈레톤

```java
/**
 * {UseCase} UseCase
 *
 * @author {Your Name}
 * @since 1.0
 */
@UseCase
public class XxxUseCase implements XxxPort {

    @Transactional
    public XxxResponse execute(XxxCommand command) {
        // TODO: UseCase 로직 (Claude Code 작업)
    }
}
```

## 📝 다음 단계

1. Cursor AI가 위 스켈레톤 코드 생성
2. Git Commit → Hook 실행 (변경 파일 추적)
3. Claude Code가 검증 (`/validate-cursor-changes`)
4. Claude Code가 비즈니스 로직 구현 (`/implement-logic`)
5. Claude Code가 테스트 생성 (`/generate-tests`)
```

## 사용 예시

```bash
# Claude Code에서 실행
/design-analysis Order

# 출력:
# → .claude/work-orders/order-aggregate.md (작업지시서)
# → Cursor AI가 읽고 Boilerplate 생성
```
```

**검증:**
```bash
# 스킬 사용 테스트
# Claude Code에서: /design-analysis Order
# → 작업지시서가 생성되는지 확인
```

---

## 📌 커맨드 가이드

### 현재 사용 가능한 커맨드

#### Phase 2: 설계 & 검증 ✅
```bash
# 설계 분석 및 작업지시서 생성
/design-analysis <feature-name>
/design-analysis Order --prd docs/prd/order.md

# Fixture 자동 생성
/generate-fixtures <aggregate> [--without-id] [--with-states] [--vip] [--all]
/generate-fixtures Order --all

# Cursor 변경 검증
/validate-cursor-changes
/validate-cursor-changes --layer domain
```

#### Phase 3: 작업 큐 시스템 ✅
```bash
# 작업 큐에 추가
/queue-add <feature> [work-order] [--priority high|normal]
/queue-add order order-aggregate.md
/queue-add payment payment-aggregate.md --priority high

# 작업 시작 (Worktree 자동 생성)
/queue-start <feature>
/queue-start order
# → git worktree add ../wt-order feature/order
# → 작업지시서 자동 복사
# → .cursorrules 자동 복사

# 작업 완료 (통계 표시)
/queue-complete <feature>
/queue-complete order
# → 소요 시간 계산
# → 남은 작업 수 표시

# 큐 목록 확인
/queue-list
# → ⏳ 대기 중 작업
# → 🔄 진행 중 작업

# 큐 상태 요약
/queue-status
# → 대기 중: N개
# → 진행 중: N개
# → 완료됨: N개
```

#### Jira & AI Review
```bash
# Jira Task 분석
/jira-analyze <issue-key>
/jira-create
/jira-update <issue-key>

# AI 리뷰
/ai-review [pr-number]
/ai-review 123 --bots gemini,coderabbit
```

#### 검증
```bash
# 아키텍처 검증
/validate-domain <file>
/validate-architecture [dir]
```

### 추가 예정 커맨드 (Phase 4)

```bash
# 비즈니스 로직 구현
/implement-logic <file>

# 테스트 생성
/generate-tests <file> [--with-states] [--vip]
```

---

## 🌲 Phase 3: 작업 큐 시스템 상세 가이드

### 시스템 아키텍처

```
.claude/
├── work-queue.json                 # 큐 데이터 (JSON)
├── work-orders/                     # 작업지시서 저장소
│   └── order-aggregate.md
└── scripts/
    ├── worktree-manager.sh          # Worktree 자동화 (220 lines)
    └── queue-manager.py             # 큐 관리 엔진 (280 lines)

commands/
├── queue-add.md                     # 작업 추가
├── queue-start.md                   # 작업 시작 + Worktree 생성
├── queue-complete.md                # 작업 완료 + 통계
├── queue-list.md                    # 목록 확인
└── queue-status.md                  # 상태 요약
```

### 큐 데이터 구조

**`.claude/work-queue.json`:**
```json
{
  "queue": [
    {
      "id": 1,
      "feature": "order",
      "work_order": "order-aggregate.md",
      "priority": "normal",
      "status": "in_progress",
      "created_at": "2024-11-04T17:00:00Z",
      "started_at": "2024-11-04T17:05:00Z",
      "completed_at": null
    },
    {
      "id": 2,
      "feature": "payment",
      "work_order": "payment-aggregate.md",
      "priority": "high",
      "status": "pending",
      "created_at": "2024-11-04T17:10:00Z",
      "started_at": null,
      "completed_at": null
    }
  ],
  "completed": [
    {
      "id": 0,
      "feature": "product",
      "status": "completed",
      "completed_at": "2024-11-04T16:50:00Z"
    }
  ],
  "metadata": {
    "version": "1.0",
    "created_at": "2024-11-04T17:00:00Z",
    "last_updated": "2024-11-04T17:10:00Z"
  }
}
```

### 완전한 워크플로우 예시

```bash
# ========================================
# Step 1: Jira 분석 및 작업지시서 생성
# ========================================
/jira-analyze PROJ-123
# → Jira 내용 분석
# → TodoList 생성

/design-analysis Order
# → .claude/work-orders/order-aggregate.md 생성
# → 18개 파일 스켈레톤 코드 포함
# → Zero-Tolerance 규칙 명시

# ========================================
# Step 2: 큐에 작업 추가
# ========================================
/queue-add order order-aggregate.md
# 출력:
#   ✅ 작업 추가됨: order
#   ID: 1
#   작업지시서: order-aggregate.md
#   우선순위: normal

/queue-add payment payment-aggregate.md --priority high
# 출력:
#   ✅ 작업 추가됨: payment
#   ID: 2
#   우선순위: high

# ========================================
# Step 3: 큐 상태 확인
# ========================================
/queue-status
# 출력:
#   📊 큐 상태
#   ⏳ 대기 중: 2개
#   🔄 진행 중: 0개
#   ✅ 완료됨: 0개
#   📝 총 작업: 2개

/queue-list
# 출력:
#   📋 작업 큐
#   ⏳ 📌 order
#      ID: 1 | 상태: pending
#      작업지시서: order-aggregate.md
#
#   ⏳ 🔥 payment
#      ID: 2 | 상태: pending
#      작업지시서: payment-aggregate.md

# ========================================
# Step 4: 작업 시작 (Worktree 자동 생성)
# ========================================
/queue-start order
# 출력:
#   ✅ 작업 시작됨: order
#
#   📝 다음 단계:
#     1. bash .claude/scripts/worktree-manager.sh create order order-aggregate.md
#     2. Cursor AI로 Boilerplate 생성
#     3. Git Commit
#     4. python3 .claude/scripts/queue-manager.py complete order

# 자동 실행 (worktree-manager.sh):
bash .claude/scripts/worktree-manager.sh create order order-aggregate.md
# 실행 내역:
#   1. git branch feature/order
#   2. git worktree add ../wt-order feature/order
#   3. cp .claude/work-orders/order-aggregate.md ../wt-order/
#   4. cp .cursorrules ../wt-order/

# 출력:
#   ✅ Worktree 생성 완료!
#   📂 Worktree 경로: ../wt-order
#   🌿 브랜치: feature/order
#   📋 작업지시서: order-aggregate.md

# ========================================
# Step 5: Cursor AI로 Boilerplate 생성
# ========================================
cd ../wt-order

# Cursor IDE에서:
# 1. .cursorrules 자동 로드
# 2. order-aggregate.md 참조
# 3. 18개 파일 Boilerplate 생성:
#    - OrderDomain.java
#    - OrderId.java
#    - OrderStatus.java
#    - CreateOrderUseCase.java
#    - OrderController.java
#    - ... (13개 더)

git add .
git commit -m "feat: Order Aggregate Boilerplate"

# ========================================
# Step 6: Claude Code로 검증
# ========================================
cd ~/claude-spring-standards

/validate-cursor-changes
# 실행 내역:
#   1. Git Hook이 생성한 cursor-changes.md 읽기
#   2. validation-helper.py 실행 (Cache 기반)
#   3. ArchUnit 테스트 실행
#   4. 위반 사항 리포트 생성

# 출력 (통과):
#   ✅ Validation Passed
#   모든 파일이 컨벤션을 준수합니다

# 출력 (위반 시):
#   ❌ Validation Failed
#
#   위반 사항:
#   - OrderDomain.java:45 - Lombok 금지
#   - OrderDomain.java:78 - Law of Demeter 위반
#
#   수정 가이드: .claude/validation-report.md

# ========================================
# Step 7: Claude Code로 비즈니스 로직 구현
# ========================================
# Claude Code가 Worktree 코드를 읽고 비즈니스 메서드 구현

# OrderDomain.java:
# - placeOrder() 메서드 구현
# - cancelOrder() 메서드 구현
# - confirmOrder() 메서드 구현

# CreateOrderUseCase.java:
# - Transaction 경계 관리
# - Domain 메서드 호출

# ========================================
# Step 8: Fixture 생성
# ========================================
/generate-fixtures Order --all
# 생성:
#   - OrderTestFixtures.java
#   - OrderObjectMother.java
#   - OrderCommandFixtures.java
#   - OrderEntityFixtures.java

# ========================================
# Step 9: 작업 완료
# ========================================
/queue-complete order
# 출력:
#   ✅ 작업 완료됨: order
#
#   📊 통계:
#     소요 시간: 45분
#     남은 작업: 1개
#     완료된 작업: 1개
#
#   📝 다음 단계:
#     1. cd ../wt-order
#     2. git log (커밋 확인)
#     3. cd ~/claude-spring-standards
#     4. git merge feature/order
#     5. bash .claude/scripts/worktree-manager.sh remove order

# ========================================
# Step 10: Worktree 제거 및 Merge
# ========================================
bash .claude/scripts/worktree-manager.sh remove order
# 실행 내역:
#   1. 변경사항 확인 (있으면 경고)
#   2. git worktree remove ../wt-order --force
#   3. 브랜치 유지 (feature/order)

git merge feature/order
gh pr create
/ai-review {pr-number}

# ========================================
# Step 11: 다음 작업 진행
# ========================================
/queue-start payment
# 위 과정 반복...
```

### Worktree Manager 스크립트

**`.claude/scripts/worktree-manager.sh`** (220 lines)

**주요 기능:**
1. **create**: Worktree 생성 + 작업지시서 복사 + .cursorrules 복사
2. **remove**: Worktree 제거 + 변경사항 확인
3. **list**: 활성 Worktree 목록
4. **status**: Worktree 상태 요약

**사용법:**
```bash
# Worktree 생성
bash .claude/scripts/worktree-manager.sh create order order-aggregate.md

# Worktree 제거
bash .claude/scripts/worktree-manager.sh remove order

# 목록 확인
bash .claude/scripts/worktree-manager.sh list

# 상태 확인
bash .claude/scripts/worktree-manager.sh status
```

### Queue Manager 스크립트

**`.claude/scripts/queue-manager.py`** (280 lines)

**주요 기능:**
1. **add**: 작업 추가 (중복 방지, 우선순위 설정)
2. **start**: 작업 시작 (상태 변경, 시작 시간 기록)
3. **complete**: 작업 완료 (소요 시간 계산, Completed 이동)
4. **list**: 큐 목록 (색상 아이콘, 상태별 필터링)
5. **status**: 큐 상태 (통계 요약, 진행 시간 계산)

**사용법:**
```bash
# 작업 추가
python3 .claude/scripts/queue-manager.py add order order-aggregate.md
python3 .claude/scripts/queue-manager.py add payment --priority high

# 작업 시작
python3 .claude/scripts/queue-manager.py start order

# 작업 완료
python3 .claude/scripts/queue-manager.py complete order

# 목록 확인
python3 .claude/scripts/queue-manager.py list

# 상태 확인
python3 .claude/scripts/queue-manager.py status
```

### 성능 메트릭

| 항목 | 수동 작업 | 큐 시스템 | 개선율 |
|------|----------|----------|--------|
| Worktree 생성 | 5분 | 10초 | 97% ↓ |
| 작업지시서 복사 | 1분 | 자동 | 100% ↓ |
| 상태 추적 | 수동 메모 | 자동 JSON | 100% ↓ |
| 통계 계산 | 수동 계산 | 자동 | 100% ↓ |
| **총 시간 절감** | - | - | **80% ↓** |

### 장점

1. **자동화**: Worktree 생성/제거 자동화
2. **추적**: 작업 상태 자동 추적 (pending → in_progress → completed)
3. **통계**: 소요 시간 자동 계산
4. **우선순위**: High/Normal 우선순위 관리
5. **병렬 작업**: 여러 Feature 동시 개발 가능
6. **Git 통합**: Worktree + 브랜치 자동 관리

---

## 🌲 Git Worktree 전략

### 개념

Git Worktree를 사용하면 **하나의 저장소**에서 **여러 브랜치를 동시에** 작업할 수 있습니다.

```
~/claude-spring-standards/          (main 브랜치, Claude Code 전용)
~/wt-order/                          (feature/order, Cursor AI 전용)
~/wt-product/                        (feature/product, Cursor AI 전용)
```

### 사용법

```bash
# Worktree 생성
git worktree add ../wt-order feature/order-aggregate

# Cursor AI 작업 (Worktree 디렉토리)
cd ../wt-order
# Cursor에서 코드 생성

# Claude Code 검증 (Main 디렉토리)
cd ~/claude-spring-standards
/validate-worktree feature/order-aggregate

# Worktree 제거 및 Merge
git worktree remove ../wt-order
git merge feature/order-aggregate
```

### 장점

1. **독립 환경**: Claude와 Cursor가 서로 다른 디렉토리에서 작업
2. **충돌 방지**: `.cursorrules` 동시 수정 문제 없음
3. **병렬 작업**: 여러 Feature 동시 개발 가능
4. **명확한 역할**: Main (Claude), Worktree (Cursor)

---

## 🎯 현재 상태 및 다음 액션

### ✅ 완료된 Phase

#### Phase 1: 기본 인프라 ✅
- Serena 메모리 제거 완료
- `.cursorrules` 작성 완료
- Claude Skills 작성 완료

#### Phase 2: 자동화 커맨드 ✅
- `/design-analysis` (129 lines) ✅
- `/generate-fixtures` (178 lines) ✅
- `/validate-cursor-changes` (196 lines) ✅

#### Phase 3: 작업 큐 시스템 ✅
- `worktree-manager.sh` (220 lines) ✅
- `queue-manager.py` (280 lines) ✅
- `/queue-add`, `/queue-start`, `/queue-complete`, `/queue-list`, `/queue-status` ✅

**총 구현 라인 수: 1,000+ lines**

### 🚀 즉시 사용 가능

```bash
# 완전한 워크플로우
/jira-analyze PROJ-123                      # Jira 분석
/design-analysis Order                       # 설계 + 작업지시서
/queue-add order order-aggregate.md          # 큐에 추가
/queue-start order                           # Worktree 자동 생성
# Cursor AI 작업 (Worktree)
/validate-cursor-changes                     # 검증
/generate-fixtures Order --all               # Fixture 생성
/queue-complete order                        # 완료 + 통계
```

### 📋 Phase 4: Git Hook 통합 (다음 단계)

1. **Cursor 변경 추적 Hook** (1일)
   - Git post-commit hook 작성
   - `.claude/cursor-changes.md` 자동 생성
   - 변경 파일 목록 추적

2. **자동 검증 리포트** (1일)
   - 컨벤션 위반 자동 리포트
   - 수정 가이드 자동 생성
   - GitHub PR 코멘트 자동 작성

---

## 📚 참고 문서

- [Hook System Analysis](claudedocs/hook-system-analysis-report.md)
- [Coding Convention](docs/coding_convention/)
- [Cache System](.claude/cache/rules/README.md)
- [Commands](.claude/commands/README.md)

---

**✅ 이 가이드를 따라 Phase 1부터 시작하세요!**
