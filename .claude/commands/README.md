# Claude Code Slash Commands

**Spring DDD Standards 프로젝트 전용 커맨드 (v1.0)**

---

## 🎯 v1.0 시스템 워크플로우

```
/create-prd "Order Management"  (개발 예정)
    ↓
PRD 문서 생성 (docs/prd/*.md)
    ↓
/jira-from-prd  (개발 예정)
    ↓
Jira 티켓 (Layer 태그 포함)
    ↓
/jira-task  (기존 /jira-analyze 개선 예정)
    ↓
kentback plan.md + 브랜치 생성
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

### 🆕 Phase 1: PRD → Jira → Plan ✅ 개발 완료

| 순위 | 커맨드 | 상태 | 설명 | 실제 시간 |
|------|--------|------|------|----------|
| 1 | `/create-prd` | ✅ 개발 완료 | 대화형 PRD 생성 | ~1시간 |
| 2 | `/jira-from-prd` | ✅ 개발 완료 | PRD → 레이어별 Jira 티켓 | ~1시간 |
| 3 | `/jira-task` | ✅ 개발 완료 | Jira → kentback plan + 브랜치 | ~1시간 |

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

### Workflow 2: v1.0 시스템 완성 후 (향후)

```bash
# 1. PRD 생성
/create-prd "Order Management"

# 2. Jira 티켓 생성
/jira-from-prd docs/prd/order-management.md

# 3. Kent Beck TDD plan 생성
/jira-task

# 4. Kent Beck TDD 개발
# 📁 .claude/commands/kb/ 파일 참조하여 TDD 사이클 수행

# 5. 프롬프트 등록
/langfuse-register-prompt domain v1.0

# 6. A/B/C/D 테스트
/abcd-test PROJ-123 all

# 7. 결과 분석
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

### Phase 1: PRD → Jira → Plan (완료) ✅
- ✅ `/create-prd` 구현 (~1시간)
- ✅ `/jira-from-prd` 구현 (~1시간)
- ✅ `/jira-task` 구현 (~1시간)

**실제 기간**: 3시간 (예상: 10-14시간 → 78% 시간 단축)

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
