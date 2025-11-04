# Claude Code Slash Commands

**Spring DDD Standards 프로젝트 전용 커맨드**

---

## 📋 커맨드 목록

### 🎯 Phase 2: 설계 & 검증

| 커맨드 | 설명 | 사용법 |
|--------|------|--------|
| `/design-analysis` | 설계 분석 및 작업지시서 생성 | `/design-analysis Order` |
| `/generate-fixtures` | 테스트 픽스처 자동 생성 | `/generate-fixtures Order --all` |
| `/validate-cursor-changes` | Cursor 코드 자동 검증 | `/validate-cursor-changes` |

### 🌲 Phase 3: 작업 큐 시스템 (NEW)

| 커맨드 | 설명 | 사용법 |
|--------|------|--------|
| `/queue-add` | 작업 큐에 추가 | `/queue-add order order-aggregate.md` |
| `/queue-start` | 작업 시작 및 Worktree 생성 | `/queue-start order` |
| `/queue-complete` | 작업 완료 및 통계 표시 | `/queue-complete order` |
| `/queue-list` | 큐 목록 확인 | `/queue-list` |
| `/queue-status` | 큐 상태 요약 | `/queue-status` |

### 🔍 Jira 통합

| 커맨드 | 설명 | 사용법 |
|--------|------|--------|
| `/jira-analyze` | Jira 태스크 분석 및 TodoList 생성 | `/jira-analyze PROJ-123` |
| `/jira-create` | Jira 이슈 생성 | `/jira-create` |
| `/jira-update` | Jira 이슈 업데이트 | `/jira-update PROJ-123` |
| `/jira-transition` | Jira 이슈 상태 변경 | `/jira-transition PROJ-123 Done` |
| `/jira-comment` | Jira 이슈에 코멘트 추가 | `/jira-comment PROJ-123` |
| `/jira-link-pr` | GitHub PR과 Jira 연동 | `/jira-link-pr PROJ-123 123` |

### 🤖 AI 리뷰

| 커맨드 | 설명 | 사용법 |
|--------|------|--------|
| `/ai-review` | 통합 AI 리뷰 (Gemini + CodeRabbit + Codex) | `/ai-review 123` |
| `/ai-review` | 특정 봇만 실행 | `/ai-review 123 --bots gemini` |
| `/ai-review` | 분석만 (실행 안함) | `/ai-review 123 --analyze-only` |

### ✅ 검증

| 커맨드 | 설명 | 사용법 |
|--------|------|--------|
| `/validate-architecture` | 전체 아키텍처 검증 | `/validate-architecture` |
| `/validate-domain` | Domain 파일 검증 | `/validate-domain {file}` |

---

## 🚀 통합 워크플로우

### Workflow 1: 새로운 Aggregate 개발

```bash
# 1. Jira Task 분석
/jira-analyze PROJ-123

# 2. 설계 분석 및 작업지시서 생성
/design-analysis Order

# 3. Git Worktree 생성 (수동)
git worktree add ../wt-order feature/order

# 4. Cursor AI로 Boilerplate 생성 (Worktree)
# → .cursorrules 자동 로드
# → 작업지시서 참조
# → 코드 생성

# 5. Git Commit (Cursor)
git add .
git commit -m "feat: Order Aggregate 생성"

# 6. Claude Code로 검증 (Main)
/validate-cursor-changes

# 7. 통과 시 Fixture 생성
/generate-fixtures Order --all

# 8. 비즈니스 로직 구현 (Claude Code)
# → Domain 메서드 구현
# → UseCase Transaction 관리

# 9. 최종 검증
/validate-architecture

# 10. PR 생성 및 AI 리뷰
gh pr create
/ai-review {pr-number}
```

### Workflow 2: 큐 시스템 활용 (NEW)

```bash
# 1. Jira Task 분석
/jira-analyze PROJ-123

# 2. 작업 큐에 추가
/queue-add order order-aggregate.md
/queue-add payment payment-aggregate.md --priority high

# 3. 큐 상태 확인
/queue-status
# 출력:
#   ⏳ 대기 중: 2개
#   📝 총 작업: 2개

# 4. 작업 시작 (Worktree 자동 생성)
/queue-start order
# 자동 실행:
#   - 작업 상태 → in_progress
#   - git worktree add ../wt-order feature/order
#   - 작업지시서 복사: order-aggregate.md
#   - .cursorrules 복사

# 5. Cursor AI로 코드 생성 (Worktree)
cd ../wt-order
# Cursor IDE에서 작업지시서 참조하여 코드 생성
git commit -m "feat: Order Aggregate 생성"

# 6. Claude Code로 검증 (Main)
cd ~/claude-spring-standards
/validate-cursor-changes

# 7. 비즈니스 로직 구현

# 8. 작업 완료
/queue-complete order
# 출력:
#   ✅ 작업 완료됨: order
#   📊 소요 시간: 25분
#   📝 남은 작업: 1개

# 9. 다음 작업 진행
/queue-start payment

# 10. 전체 진행 상황 확인
/queue-list
```

### Workflow 3: 기존 코드 리뷰

```bash
# 1. PR 생성
gh pr create

# 2. AI 리뷰 실행
/ai-review {pr-number}

# 3. Jira 연동
/jira-link-pr PROJ-123 {pr-number}

# 4. 리뷰 반영 후 상태 변경
/jira-transition PROJ-123 Done
```

---

## 📚 커맨드 상세 가이드

### `/design-analysis`

**목적**: Spring DDD 설계 분석 및 Cursor 작업지시서 생성

**옵션:**
- `--prd {file}`: PRD 파일 참조
- `--jira {ticket}`: Jira 티켓 참조

**출력:**
- `.claude/work-orders/{aggregate}-aggregate.md`
- Domain/UseCase/Controller 스켈레톤 코드

**예시:**
```bash
/design-analysis Order --prd docs/prd/order.md
```

---

### `/generate-fixtures`

**목적**: Layer별 테스트 픽스처 자동 생성

**옵션:**
- `--without-id`: ID 없는 Fixture
- `--with-states`: 상태별 Fixture
- `--vip`: Object Mother 패턴
- `--all`: 모든 패턴 조합

**출력:**
- `{Layer}TestFixtures.java`
- `{Aggregate}ObjectMother.java`

**예시:**
```bash
/generate-fixtures Order --all
```

---

### `/validate-cursor-changes`

**목적**: Cursor AI 생성 코드 자동 검증

**검증 항목:**
- Lombok 금지
- Law of Demeter
- Transaction 경계
- Long FK Strategy
- Javadoc 필수

**출력:**
- `.claude/validation-report.md`
- 위반 사항 + 수정 가이드

**예시:**
```bash
/validate-cursor-changes
/validate-cursor-changes --layer domain
```

---

### `/queue-add`

**목적**: 작업을 큐에 추가하여 체계적으로 관리

**옵션:**
- `--priority high`: 높은 우선순위 설정

**기능:**
- 작업 ID 자동 생성
- 작업지시서 연결
- 우선순위 설정 (high/normal)
- 중복 방지

**예시:**
```bash
/queue-add order order-aggregate.md
/queue-add payment payment-aggregate.md --priority high
```

---

### `/queue-start`

**목적**: 작업 시작 및 Worktree 자동 생성

**자동 실행:**
- 작업 상태 → `in_progress`
- 시작 시간 기록
- Worktree 생성 안내
- 다음 단계 가이드

**Worktree 구조:**
```
../wt-{feature}/
├── order-aggregate.md  # 작업지시서 (자동 복사)
├── .cursorrules        # 컨벤션 (자동 복사)
└── (프로젝트 전체)
```

**예시:**
```bash
/queue-start order
# 출력:
#   bash .claude/scripts/worktree-manager.sh create order order-aggregate.md
```

---

### `/queue-complete`

**목적**: 작업 완료 및 통계 표시

**자동 처리:**
- 작업 상태 → `completed`
- 완료 시간 기록
- 소요 시간 계산
- Completed 목록으로 이동
- 통계 표시

**출력 정보:**
- 소요 시간 (started_at → completed_at)
- 남은 작업 수
- 완료된 작업 수

**예시:**
```bash
/queue-complete order
# 출력:
#   ✅ 작업 완료됨: order
#   📊 소요 시간: 25분
#   📝 남은 작업: 2개
```

---

### `/queue-list`

**목적**: 큐 목록 확인 (대기 중 + 진행 중)

**표시 정보:**
- 작업 ID
- 상태 (pending/in_progress)
- 우선순위 (high/normal)
- 작업지시서
- 시작 시간 (진행 중인 경우)

**아이콘:**
- ⏳ 대기 중 (pending)
- 🔄 진행 중 (in_progress)
- 🔥 높은 우선순위 (high)
- 📌 일반 우선순위 (normal)

**예시:**
```bash
/queue-list
```

---

### `/queue-status`

**목적**: 큐 전체 상태 요약

**표시 통계:**
- ⏳ 대기 중: N개
- 🔄 진행 중: N개
- ✅ 완료됨: N개
- 📝 총 작업: N개
- 현재 진행 중인 작업 상세 (진행 시간)

**예시:**
```bash
/queue-status
```

---

### `/ai-review`

**목적**: 통합 AI 리뷰 (병렬 실행)

**지원 봇:**
- Gemini Code Assist
- CodeRabbit
- Amazon CodeWhisperer

**전략:**
- `--strategy merge`: 병합 (기본)
- `--strategy vote`: 투표
- `--strategy sequential`: 순차

**예시:**
```bash
/ai-review 123
/ai-review 123 --bots gemini,coderabbit
/ai-review 123 --analyze-only
```

---

### `/jira-analyze`

**목적**: Jira Task 분석 및 브랜치 생성

**기능:**
- Task 내용 분석
- TodoList 자동 생성
- Feature 브랜치 생성

**예시:**
```bash
/jira-analyze PROJ-123
```

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
```

---

## 📖 참고 문서

- [DEVELOPMENT_GUIDE.md](../../DEVELOPMENT_GUIDE.md) - 전체 워크플로우
- [.claude/skills/](../skills/) - Claude Skills 정의
- [docs/coding_convention/](../../docs/coding_convention/) - 코딩 규칙 (98개)

---

## ⚙️ Cache 시스템

**위치**: `.claude/cache/rules/`

**성능:**
- O(1) 검색
- 90% 토큰 절감
- 73.6% 속도 향상

**빌드:**
```bash
python3 .claude/hooks/scripts/build-rule-cache.py
```

---

**✅ 이 커맨드들은 Claude Code + Cursor AI 통합 워크플로우를 지원합니다!**
