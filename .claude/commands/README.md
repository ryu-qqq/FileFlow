# Claude Code Slash Commands

**Spring DDD Standards 프로젝트 전용 커맨드 (v1.0)**

---

## 🎯 v1.0 시스템 워크플로우

```
/create-prd "Order Management"  ✅ 완료
    ↓
PRD 문서 생성 (docs/prd/*.md)
    ↓
/jira-from-prd  ✅ 완료
    ↓
Jira 티켓 (Layer 태그 포함)
    ↓
/create-tdd-plan SC-57  ⭐ NEW
    ↓
docs/prd/SC-57-tdd-plan.md 생성
    ↓
/update-tdd-plan SC-57 "피드백..."  ⭐ NEW (선택)
    ↓
TDD Plan 피드백 반영
    ↓
/kb/go  ✅ 완료
    ↓
kentback TDD 개발 (RED → GREEN → REFACTOR)
    ↓
/langfuse-register-prompt  (개발 예정)
    ↓
LangFuse 프롬프트 등록
    ↓
/abcd-test  (개발 예정)
    ↓
A/B/C/D 테스트 실행 + 메트릭 수집
    ↓
/langfuse-analyze  (개발 예정)
    ↓
프롬프트 효과 분석 + v1.1 개선안
```

---

## 📋 커맨드 목록

### 🆕 Phase 1: PRD → Jira → TDD Plan ✅ 개발 완료

| 순위 | 커맨드 | 상태 | 설명 | 실제 시간 |
|------|--------|------|------|----------|
| 1 | `/create-prd` | ✅ 개발 완료 | 대화형 PRD 생성 | ~1시간 |
| 2 | `/jira-from-prd` | ✅ 개발 완료 | PRD → 레이어별 Jira 티켓 | ~1시간 |
| 3 | `/create-tdd-plan` | ✅ 개발 완료 | Jira Story → TDD Plan 자동 생성 | ~2시간 |
| 4 | `/update-tdd-plan` | ✅ 개발 완료 | TDD Plan 피드백 반영 및 수정 | ~1시간 |

**⭐ NEW (v2.6)**: `/create-tdd-plan`, `/update-tdd-plan` 추가
- **Gap 해결**: Jira Story → TDD Plan 파일 생성 워크플로우 완성
- **통합**: `/kb/go`가 기대하는 `docs/prd/{STORY-KEY}-tdd-plan.md` 자동 생성
- **피드백 반영**: 생성된 TDD Plan을 사용자 피드백으로 반복 개선

### 📊 Phase 2: LangFuse 통합 (개발 예정)

| 순위 | 커맨드 | 상태 | 설명 | 예상 시간 |
|------|--------|------|------|----------|
| 4 | `/langfuse-register-prompt` | ❌ 미개발 | 프롬프트 LangFuse 등록 | 2-3시간 |
| 5 | `/abcd-test` | ❌ 미개발 | A/B/C/D 테스트 실행 | 8-10시간 |
| 6 | `/langfuse-analyze` | ❌ 미개발 | 결과 분석 및 v1.1 생성 | 5-7시간 |

**총 예상 개발 시간**: 25-34시간 (5-7주, part-time)

### 🔍 Jira 통합 (기존 커맨드)

| 커맨드 | 상태 | 설명 |
|--------|------|------|
| `/jira-analyze` | ✅ 사용 가능 | Jira 태스크 분석 및 TodoList 생성 (→ `/jira-task`로 개선 예정) |
| `/jira-create` | ✅ 사용 가능 | Jira 이슈 생성 |
| `/jira-update` | ✅ 사용 가능 | Jira 이슈 업데이트 |
| `/jira-transition` | ✅ 사용 가능 | Jira 이슈 상태 변경 |
| `/jira-comment` | ✅ 사용 가능 | Jira 이슈에 코멘트 추가 |
| `/jira-link-pr` | ✅ 사용 가능 | GitHub PR과 Jira 연동 |

### 🤖 AI 리뷰 (기존 커맨드)

| 커맨드 | 상태 | 설명 |
|--------|------|------|
| `/ai-review` | ✅ 사용 가능 | 통합 AI 리뷰 (Gemini + CodeRabbit + Codex) |

**옵션**:
- `--bots gemini,coderabbit`: 특정 봇만 실행
- `--strategy merge`: 병합 전략 (기본)
- `--analyze-only`: 분석만 (실행 안함)

### ✅ 검증 (기존 커맨드)

| 커맨드 | 상태 | 설명 |
|--------|------|------|
| `/validate-architecture` | ✅ 사용 가능 | 전체 아키텍처 검증 (ArchUnit) |
| `/validate-domain` | ✅ 사용 가능 | Domain 파일 검증 |

---

## 🚀 Cursor TDD (Domain Layer 전용) ⭐ NEW

**설계 철학**: Domain Layer는 의존성이 적고 단위 테스트가 간단 → Cursor로 빠르게 생성, Claude로 검증

### 워크플로우

```
Claude Code: PRD 생성
    ↓
/cr/domain-prd "Order Management"
    → docs/prd/domain/order-domain-prd.md
    ↓
Cursor Composer: TDD 사이클
    ↓
1. /cr/red (TestFixture + Tests)
2. /cr/green (Minimal implementation)
3. /cr/refactor (Convention application)
    ↓
Claude Code: 검증 + 효율 측정
    ↓
/cr/validate
    → validation-helper.py
    → LangFuse 자동 업로드
    ↓
Claude Code: Git Workflow 자동화 ⭐ NEW
    ↓
/cr/commit-and-pr
    → git commit (보기 좋은 템플릿)
    → git push
    → gh pr create
```

### 커맨드 목록

| 커맨드 | 위치 | 설명 | 실행 환경 |
|--------|------|------|----------|
| `/cr/domain-prd` | Claude Code | Domain Layer PRD 생성 (TDD Plan 포함) | Claude |
| `/cr/red` | Cursor | RED Phase - 실패하는 테스트 작성 | Cursor |
| `/cr/green` | Cursor | GREEN Phase - 최소 구현으로 테스트 통과 | Cursor |
| `/cr/refactor` | Cursor | REFACTOR Phase - 컨벤션 100% 적용 | Cursor |
| `/cr/validate` | Claude Code | 검증 + LangFuse 업로드 | Claude |
| `/cr/commit-and-pr` | Claude Code | Git Workflow 자동화 (Commit → Push → PR) ⭐ NEW | Claude |

### Kent Beck TDD 사이클

```
🔴 RED Phase (Cursor):
   → TestFixture 먼저 생성 (FIRST STEP)
   → Given-When-Then 구조
   → 비즈니스 규칙을 테스트로 표현

🟢 GREEN Phase (Cursor):
   → 테스트 통과만 목표
   → 하드코딩 허용 (빠르게!)
   → Lombok 금지만 필수 준수

🔧 REFACTOR Phase (Cursor):
   → Law of Demeter 적용
   → ValueObject 패턴 완성
   → Tell, Don't Ask 원칙
   → Javadoc 추가
```

### 효율 메트릭 (예상)

| 메트릭 | Claude 단독 | Cursor TDD | 개선율 |
|--------|------------|------------|--------|
| **개발 시간** | 20분 | 7분 | **65% 단축** |
| **컨벤션 위반** | 5-10건 | 0-1건 | **90% 감소** |
| **반복 수정** | 2-3회 | 0-1회 | **90% 감소** |
| **토큰 사용량** | 50,000 | 5,000 | **90% 절감** |

### 적용 범위

**✅ Domain Layer만 적용**:
- Aggregate Root (e.g., Order)
- Value Object (e.g., OrderId, Money)
- Enum (e.g., OrderStatus)
- Domain Event

**❌ 다른 Layer는 기존 명령어 사용**:
- Application Layer → `/code-gen-usecase`
- Persistence Layer → 기존 방식
- REST API Layer → `/code-gen-controller`

### 상세 문서

- `.claude/commands/cr/README.md` - 전체 워크플로우 가이드
- `.claude/commands/cr/domain-prd.md` - PRD 생성 가이드
- `.claude/commands/cr/red.md` - RED Phase 가이드
- `.claude/commands/cr/green.md` - GREEN Phase 가이드
- `.claude/commands/cr/refactor.md` - REFACTOR Phase 가이드
- `.claude/commands/cr/validate.md` - 검증 + LangFuse 가이드
- `.claude/commands/cr/commit-and-pr.md` - Git Workflow 자동화 가이드 ⭐ NEW

---

## 🚀 현재 사용 가능한 워크플로우

### Workflow 1: Jira Task 기반 개발 (현재)

```bash
# 1. Jira Task 분석 및 브랜치 생성
/jira-task

# 2. Kent Beck TDD 개발 (kb/ 디렉토리)
# 📁 .claude/commands/kb/ 파일을 직접 참조
# ⚠️ 참고: /kb:* slash command는 현재 등록되지 않음
# 아래 명령어들은 .claude/commands/kb/*.md 파일의 내용을 따릅니다

# kb/go.md: TDD 사이클 시작
# kb/red.md: RED Phase (실패하는 테스트 작성)
# kb/green.md: GREEN Phase (최소 코드로 테스트 통과)
# kb/refactor.md: REFACTOR Phase (코드 개선)
# kb/next-test.md: 다음 테스트로 이동
# kb/check-tests.md: 테스트 실행
# kb/commit-tdd.md: TDD Commit
# kb/tidy.md: 정리

# 3. 최종 검증
/validate-architecture

# 4. PR 생성 및 AI 리뷰
gh pr create
/ai-review {pr-number}

# 5. Jira 연동
/jira-link-pr PROJ-123 {pr-number}
/jira-transition PROJ-123 Done
```

### Workflow 2: v1.0 완전 워크플로우 (현재 사용 가능) ⭐

```bash
# 1. PRD 생성
/create-prd "Order Management"
→ docs/prd/order-management.md 생성

# 2. Jira 티켓 생성
/jira-from-prd docs/prd/order-management.md
→ Epic SC-56 + Stories (SC-57, SC-58, SC-59, SC-60)

# 3. TDD Plan 자동 생성 ⭐ NEW
/create-tdd-plan SC-57
→ docs/prd/SC-57-tdd-plan.md 생성

# 4. TDD Plan 피드백 반영 (선택) ⭐ NEW
/update-tdd-plan SC-57 "Email 국제 도메인 테스트 추가"
→ docs/prd/SC-57-tdd-plan.md 업데이트

# 5. Kent Beck TDD 개발
# 📁 .claude/commands/kb/go.md를 참조하여 TDD 사이클 수행
/kb/go
→ RED → GREEN → REFACTOR 사이클

# 6. 최종 검증
/validate-architecture

# 7. PR 생성 및 AI 리뷰
gh pr create
/ai-review {pr-number}

# ────────────────────────────────────────
# Phase 2: LangFuse 통합 (개발 예정)
# ────────────────────────────────────────

# 8. 프롬프트 등록
/langfuse-register-prompt domain v1.0

# 9. A/B/C/D 테스트
/abcd-test PROJ-123 all

# 10. 결과 분석
/langfuse-analyze domain v1.0
```

---

## 📚 커맨드 상세 가이드

### `/jira-analyze` (기존)

**목적**: Jira Task 분석 및 TodoList 생성

**사용법**:
```bash
/jira-analyze PROJ-123
/jira-analyze https://your-domain.atlassian.net/browse/PROJ-123
```

**기능**:
- Jira 이슈 조회 (summary, description, status, Epic)
- TodoList 자동 생성
- Feature 브랜치 생성 안내

**향후 개선** (`/jira-task`):
- kentback plan.md 자동 생성 (RED → GREEN → REFACTOR 계획)
- Layer 태그 활용 (domain, application, persistence, rest-api)

---

### `/ai-review` (기존)

**목적**: 통합 AI 리뷰 (병렬 실행)

**사용법**:
```bash
/ai-review 123
/ai-review 123 --bots gemini,coderabbit
/ai-review 123 --analyze-only
```

**지원 봇**:
- Gemini Code Assist
- CodeRabbit
- Amazon CodeWhisperer

---

### `/validate-architecture` (기존)

**목적**: ArchUnit 기반 아키텍처 규칙 검증

**사용법**:
```bash
/validate-architecture
/validate-architecture domain
```

**검증 항목**:
- Layer 의존성
- Naming 규칙
- Zero-Tolerance 규칙

---

### `/create-tdd-plan` ⭐ NEW (v2.6)

**목적**: Jira Story에서 TDD Plan 문서 자동 생성

**사용법**:
```bash
/create-tdd-plan SC-57
/create-tdd-plan SC-57 --prd docs/prd/user-authentication.md
/create-tdd-plan SC-57 --layer domain
/create-tdd-plan SC-57 --force
```

**생성 파일**: `docs/prd/SC-57-tdd-plan.md`

**주요 기능**:
- Jira Story 정보 자동 수집 (제목, Epic, Layer)
- PRD에서 요구사항 자동 추출
- TestFixture 템플릿 자동 생성
- RED/GREEN/REFACTOR Phase 가이드 생성
- Zero-Tolerance 체크리스트 포함
- Layer별 커스터마이징 (domain, application, persistence, adapter-rest)

**워크플로우**:
```
/create-prd → /jira-from-prd → /create-tdd-plan → /kb/go
```

**참조**: [create-tdd-plan.md](create-tdd-plan.md)

---

### `/update-tdd-plan` ⭐ NEW (v2.6)

**목적**: 생성된 TDD Plan에 피드백 반영 및 수정

**사용법**:
```bash
/update-tdd-plan SC-57 "Email 국제 도메인 테스트 추가"
/update-tdd-plan SC-57 "UserDomainFixture에 createAdmin() 메서드 추가"
/update-tdd-plan SC-57 "경계값 테스트 추가 (4회, 5회, 6회)"
/update-tdd-plan SC-57  # 대화형 모드
```

**주요 기능**:
- TestFixture 메서드 추가
- 테스트 케이스 추가/수정
- 구현 가이드 추가 (Builder 패턴, Record 패턴 등)
- 비즈니스 규칙 추가
- 복합 피드백 처리 (여러 섹션 동시 수정)

**피드백 템플릿**:
- TestFixture: `"{FixtureName}에 {methodName}() 메서드 추가"`
- 테스트: `"{테스트명}에 {케이스 설명} 테스트 추가"`
- 구현: `"{구현명}에 {패턴명} 적용 예시 추가"`

**참조**: [update-tdd-plan.md](update-tdd-plan.md)

---

## 🔧 환경 설정

### 필수 환경 변수

```bash
# Jira
export JIRA_API_TOKEN="your-token"
export JIRA_BASE_URL="https://your-domain.atlassian.net"
export JIRA_USER_EMAIL="your-email@example.com"

# GitHub
export GITHUB_TOKEN="your-token"

# AI Review (optional)
export GEMINI_API_KEY="your-key"
export CODERABBIT_API_KEY="your-key"

# LangFuse (Phase 2에서 필요)
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"
```

---

## 📖 참고 문서

### 시스템 설계
- [TDD_LANGFUSE_SYSTEM_DESIGN.md](../../langfuse/TDD_LANGFUSE_SYSTEM_DESIGN.md) - v1.0 시스템 전체 설계
- [COMMAND_PRIORITY.md](../../langfuse/COMMAND_PRIORITY.md) - 6개 커맨드 우선순위

### 코딩 규칙
- [docs/coding_convention/](../../docs/coding_convention/) - 98개 규칙 (Layer별)

### Cache 시스템
- [.claude/cache/rules/](../cache/rules/) - JSON Cache (O(1) 검색, 90% 토큰 절감)

### Kent Beck TDD
- [kb/](kb/) - TDD 사이클 커맨드 (8개 파일)
  - `go.md` - TDD 사이클 시작
  - `red.md` - RED Phase (실패하는 테스트 작성)
  - `green.md` - GREEN Phase (최소 코드로 테스트 통과)
  - `refactor.md` - REFACTOR Phase (코드 개선)
  - `next-test.md` - 다음 테스트로 이동
  - `check-tests.md` - 테스트 실행
  - `commit-tdd.md` - TDD Commit
  - `tidy.md` - 정리

⚠️ **참고**: `/kb:*` slash command는 현재 등록되지 않음. Claude에게 "kb/go.md를 따라서 TDD를 시작해줘" 형식으로 요청하세요.

---

## 📊 개발 진행 상황

### Phase 0: 시스템 설계 (완료)
- ✅ TDD + LangFuse 시스템 설계 완료
- ✅ 6개 커맨드 우선순위 정의
- ✅ 불필요한 커맨드 정리 (큐 시스템 6개, Cursor 통합 3개 삭제)

### Phase 1: PRD → Jira → TDD Plan (완료) ✅
- ✅ `/create-prd` 구현 (~1시간)
- ✅ `/jira-from-prd` 구현 (~1시간)
- ✅ `/create-tdd-plan` 구현 (~2시간) ⭐ NEW (v2.6)
- ✅ `/update-tdd-plan` 구현 (~1시간) ⭐ NEW (v2.6)

**실제 기간**: 5시간 (예상: 10-14시간 → 64% 시간 단축)

**v2.6 업데이트**:
- ✅ TDD Plan 자동 생성 워크플로우 완성
- ✅ `/kb/go` 통합 (`docs/prd/{STORY-KEY}-tdd-plan.md`)
- ✅ 피드백 반영 시스템 구축

### Phase 2: LangFuse 통합 (예정)
- ❌ `/langfuse-register-prompt` 구현 (2-3시간)
- ❌ `/abcd-test` 구현 (8-10시간)

**예상 기간**: 2주 (10-13시간, part-time)

### Phase 3: 분석 및 개선 (예정)
- ❌ `/langfuse-analyze` 구현 (5-7시간)

**예상 기간**: 1주 (5-7시간, part-time)

---

## ⚙️ Cache 시스템

**위치**: `.claude/cache/rules/`

**성능**:
- O(1) 검색 (index.json 기반)
- 90% 토큰 절감 (50,000 → 500-1,000)
- 73.6% 속도 향상 (561ms → 148ms)

**빌드**:
```bash
python3 .claude/hooks/scripts/build-rule-cache.py
```

---

**✅ v1.0 시스템: 기능 개발 + 컨벤션 자동 검증 + 프롬프트 효과 측정**
