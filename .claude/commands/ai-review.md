---
allowed-tools: Bash(gh:*), Read, Grep, TodoWrite, Edit, MultiEdit, Task
argument-hint: [pr-number] | --bots gemini,coderabbit,codex | --strategy merge|vote|sequential | --analyze-only | --preview
description: Unified AI review automation - integrates Gemini, CodeRabbit, and Codex reviews into prioritized TodoList
model: claude-sonnet-4-5-20250929
---

# AI Review Integration (Multi-Bot)

## Why This Command Exists

**The Problem**: Modern PRs get reviews from multiple AI bots (Gemini, CodeRabbit, Codex), but:
- Reading 3 separate reviews is tedious
- Duplicate issues across bots
- No unified priority
- Manual TodoList creation

**The Solution**: One command that:
- Fetches all AI reviews in parallel (3x faster)
- Deduplicates and merges issues
- Applies voting system for priority
- Enforces project Zero-Tolerance rules
- Generates unified TodoList

## What Makes This Different

| | /gemini-review | /ai-review (This) |
|---|---|---|
| **Bots** | Gemini only | Gemini + CodeRabbit + Codex |
| **Speed** | Sequential | **Parallel (3x faster)** |
| **Deduplication** | N/A | ✅ Automatic |
| **Priority** | Single bot opinion | **Voting system (consensus)** |
| **Project Rules** | Basic | **Zero-Tolerance enforcement** |

## Triggers
- PR has multiple AI bot reviews (Gemini, CodeRabbit, Codex)
- Need unified view of AI feedback
- Want consensus-based priorities
- Reduce review noise and duplicates

## Usage
```bash
/ai-review [pr-number] [--bots gemini,coderabbit,codex] [--strategy merge|vote|sequential] [--analyze-only] [--preview]
```

## Behavioral Flow

### Phase 1: State Management & Comment Collection
```
1. ReviewStateManager 초기화
   - 기존 처리된 댓글 확인
   - 7일 TTL 자동 정리 (최대 100개 PR)

2. fetch_reviews.py 실행
   - GitHub API로 3개 봇 댓글 수집
   - Gemini, CodeRabbit, Codex 동시 수집
   - 카테고리 자동 분류 (security, performance, style 등)

3. 중복 방지 필터링
   - 이미 처리된 댓글 제외
   - 새 댓글만 처리 (효율성)
```

### Phase 2: Deduplication & Prioritization
```
4. deduplicator.py 실행
   - 파일:라인 위치 그룹화
   - TF-IDF 코사인 유사도 계산
   - Similarity > 0.8 시 병합
   - 투표 수 계산 (1-3봇)

5. prioritizer.py 실행
   - Zero-Tolerance 체크 (최우선)
     - Lombok 사용 감지 → Auto Critical
     - Law of Demeter 위반 → Auto Critical
     - Transaction 경계 위반 → Auto Critical
     - Long FK 위반 → Auto Critical
   - 투표 시스템 적용
     - 3봇 합의 → Critical
     - 2봇 합의 → Important
     - 1봇만 → Suggestion
   - 카테고리 기반 조정 (security → Critical)
```

### Phase 3: TodoList Generation & State Update
```
6. todo_generator.py 실행
   - 우선순위별 마크다운 생성
   - 예상 시간 추정 (S/M/L)
   - claudedocs/ai-review-prN.md 저장

7. State 업데이트
   - 처리 완료 댓글 ID 저장
   - 다음 실행 시 중복 방지
```

## Tool Coordination
- **Bash**: GitHub CLI (gh) 사용해 봇 댓글 수집
- **Python Scripts**: 5개 모듈 체인 실행
  - state_manager.py (상태 관리)
  - fetch_reviews.py (댓글 수집)
  - deduplicator.py (중복 제거)
  - prioritizer.py (우선순위 계산)
  - todo_generator.py (TodoList 생성)
- **Read**: 생성된 TodoList 확인 및 출력

## Integration Strategy

### Strategy 1: Merge (Default, Recommended)
**Behavior**: Parallel collection → Deduplication → Unified priority
- **Speed**: Fastest (parallel execution)
- **Quality**: Best (consensus-based)
- **Complexity**: Moderate

**Example Output**:
```markdown
## 🤖 AI Review Summary (3 bots analyzed)

### ✅ Critical Issues (Must-Fix) - 3 items

**SQL Injection Risk** (auth.js:45)
- 🟢 Gemini: "Unsanitized user input in query"
- 🟢 CodeRabbit: "Security vulnerability detected"
- 🟢 Codex: "SQL injection possible"
→ **Consensus**: All 3 bots agree
→ **Priority**: Critical (security)

**Transaction Boundary Violation** (OrderUseCase.java:23)
- 🟢 Gemini: "External API call inside @Transactional"
- 🔴 CodeRabbit: Not mentioned
- 🔴 Codex: Not mentioned
→ **Zero-Tolerance Rule**: Auto-escalated to Critical
→ **Priority**: Must-Fix (project standard)

### ⚠️ Important Issues (Should-Fix) - 5 items

**UserService Complexity** (UserService.java:50-120)
- 🟢 Gemini: "Method exceeds 50 lines"
- 🟢 CodeRabbit: "High cyclomatic complexity (15)"
- 🔴 Codex: Not mentioned
→ **2-bot consensus**: Important
→ **Effort**: 45 minutes

### 💡 Suggestions (Nice-to-Have) - 12 items

**Add JSDoc** (UserService.ts:10)
- 🔴 Gemini: Not mentioned
- 🟢 CodeRabbit: "Missing documentation"
- 🔴 Codex: Not mentioned
→ **Single bot**: Low priority
→ **Effort**: 5 minutes

### 🚫 Skipped - 3 items

**Rename variable 'data' to 'userData'** (UserService.java:15)
- Codex suggestion
- **Reason**: Conflicts with project naming convention
```

### Strategy 2: Vote (Consensus-First)
**Behavior**: Count votes per issue → Majority wins
- **Speed**: Fast
- **Quality**: Democratic (may miss important minority opinions)
- **Complexity**: Low

### Strategy 3: Sequential (Incremental)
**Behavior**: Process bots one by one → Merge incrementally
- **Speed**: Slowest
- **Quality**: Good (easy to debug)
- **Complexity**: Low

## Bot-Specific Parsers

### Gemini Code Assist Parser
```javascript
Gemini review format:
- GitHub comments on PR
- Format: "**Suggestion**: [description]"
- Categories: Security, Performance, Best Practices

Parsing logic:
1. gh api repos/{owner}/{repo}/pulls/{pr}/comments
2. Filter: author === "gemini-code-assist[bot]"
3. Extract: file, line, body, category
```

### CodeRabbit AI Parser
```javascript
CodeRabbit review format:
- GitHub comments on PR
- Format: "## Summary\n[issues]"
- Tags: security, performance, style, tests

Parsing logic:
1. gh api repos/{owner}/{repo}/pulls/{pr}/comments
2. Filter: author === "coderabbitai[bot]"
3. Extract: file, line, body, severity
```

### Codex Connector Parser
```javascript
Codex review format:
- GitHub comments on PR
- Format: Plain text suggestions
- Focus: Refactoring, readability

Parsing logic:
1. gh api repos/{owner}/{repo}/pulls/{pr}/comments
2. Filter: author === "chatgpt-codex-connector[bot]"
3. Extract: file, line, body
```

## Zero-Tolerance Rule Enforcement

**Auto-Escalation to Critical**:
```yaml
Lombok Usage:
  pattern: '@Data|@Builder|@Getter|@Setter'
  action: Escalate to Critical
  reason: "Zero-Tolerance: Lombok 금지"

Law of Demeter:
  pattern: 'get\w+\(\)\.get\w+\(\)'
  action: Escalate to Critical
  reason: "Zero-Tolerance: Getter 체이닝 금지"

Transaction Boundary:
  pattern: '@Transactional.*RestTemplate|WebClient'
  action: Escalate to Critical
  reason: "Zero-Tolerance: @Transactional 내 외부 API 호출 금지"

Long FK Strategy:
  pattern: '@ManyToOne|@OneToMany|@OneToOne|@ManyToMany'
  action: Escalate to Critical
  reason: "Zero-Tolerance: JPA 관계 어노테이션 금지"
```

## Deduplication Algorithm

### Similarity Detection
```python
def calculate_similarity(issue1, issue2):
    # 1. Exact location match (file + line)
    if issue1.file == issue2.file and issue1.line == issue2.line:
        return 1.0

    # 2. Semantic similarity (TF-IDF cosine)
    text_sim = cosine_similarity(issue1.description, issue2.description)

    # 3. Category match
    category_sim = 1.0 if issue1.category == issue2.category else 0.0

    # Weighted average
    return 0.5 * text_sim + 0.3 * location_sim + 0.2 * category_sim

# Merge if similarity > 0.8
if calculate_similarity(issue1, issue2) > 0.8:
    merge_issues(issue1, issue2)
```

### Priority Calculation
```python
def calculate_priority(merged_issue):
    vote_count = len(merged_issue.bots)  # 1-3

    # Check Zero-Tolerance rules
    if matches_zero_tolerance(merged_issue):
        return "Critical (Zero-Tolerance)"

    # Voting system
    if vote_count == 3:
        return "Critical (Consensus)"
    elif vote_count == 2:
        return "Important (Majority)"
    else:
        return "Suggestion (Single bot)"
```

## Examples

### Example 1: Analyze Current PR
```bash
/ai-review
# Auto-detects current branch's PR
# Fetches all 3 bot reviews in parallel
# Generates unified TodoList
```

### Example 2: Specific PR with Selected Bots
```bash
/ai-review 32 --bots gemini,coderabbit
# Only analyze Gemini and CodeRabbit
# Useful if Codex not available
```

### Example 3: Preview Mode (Safe)
```bash
/ai-review --preview
# Shows analysis without creating TodoList
# Allows manual review before execution
```

### Example 4: Sequential Strategy (Debugging)
```bash
/ai-review --strategy sequential
# Process bots one by one
# Easier to debug parsing issues
```

## Real Workflow Example

**Before (Manual)**:
```bash
1. Open GitHub PR page
2. Read Gemini review (scroll)
3. Read CodeRabbit review (scroll)
4. Read Codex review (scroll)
5. Manually identify duplicates
6. Manually prioritize
7. Manually create TodoList
8. Start working
```

**After (Automated)**:
```bash
/ai-review 32
# → All reviews fetched in parallel
# → Duplicates merged automatically
# → Priorities calculated with voting
# → TodoList ready immediately
# → Start working on Critical items
```

## Output Structure

### 1. Executive Summary
```markdown
## 📊 Review Statistics
- **Bots Analyzed**: Gemini, CodeRabbit, Codex
- **Total Comments**: 45
- **After Deduplication**: 23
- **Consensus Issues**: 3 (all bots agree)
- **Majority Issues**: 8 (2 bots agree)
- **Single-bot Issues**: 12
- **Skipped**: 3 (conflict with project standards)
```

### 2. Priority Breakdown
```markdown
## 🎯 Priority Distribution
✅ Critical (Must-Fix): 5 issues
   - 3 consensus issues
   - 2 Zero-Tolerance violations

⚠️ Important (Should-Fix): 8 issues
   - 6 majority issues
   - 2 architecture concerns

💡 Suggestion (Nice-to-Have): 10 issues
   - All single-bot suggestions
```

### 3. Detailed Issue List
For each issue:
- **Title**: Clear description
- **Location**: File:line with context
- **Bot Votes**: Which bots flagged this
- **Priority**: Critical/Important/Suggestion
- **Reason**: Why this priority
- **Effort**: Estimated time (S/M/L)
- **Action**: Concrete fix steps

### 4. TodoList Generation
Automatically creates TodoList with priorities:
```
High Priority (Must-Fix):
✓ Fix SQL injection in auth.js:45 (15 min)
✓ Remove @Transactional from OrderUseCase.java:23 (10 min)
✓ Replace @Data with plain Java in Order.java:10 (5 min)

Medium Priority (Should-Fix):
○ Refactor UserService complexity (45 min)
○ Add error handling to payment flow (30 min)

Low Priority (Nice-to-Have):
○ Update JSDoc comments (20 min)
○ Rename variable for clarity (5 min)
```

## Boundaries

**Will:**
- Fetch reviews from Gemini, CodeRabbit, and Codex simultaneously
- Deduplicate identical/similar issues across bots
- Apply voting system for consensus-based priorities
- Enforce project Zero-Tolerance rules automatically
- Generate unified TodoList with effort estimates
- Support selective bot analysis (--bots flag)

**Will Not:**
- Modify bot review comments or re-trigger reviews
- Work with bots not in the system (Gemini/CodeRabbit/Codex only)
- Automatically implement changes without user confirmation
- Dismiss bot suggestions without documentation

## Decision Criteria

### Critical (Must-Fix) - Auto-Execution
- **3-bot consensus**: All bots flagged the same issue
- **Zero-Tolerance violations**: Lombok, Law of Demeter, Transaction boundary, Long FK
- **Security vulnerabilities**: Injection, XSS, credential exposure
- **Data integrity issues**: Corruption, loss, inconsistency
- **Breaking changes**: Runtime errors, API breaks

### Important (Should-Fix) - User Confirmation
- **2-bot consensus**: Majority opinion
- **Architecture violations**: Layering, dependency direction
- **Moderate performance**: 10-100ms improvements
- **Important best practices**: Error handling, logging
- **Maintainability**: High complexity, technical debt

### Suggestion (Nice-to-Have) - Optional
- **Single-bot opinion**: One bot only
- **Style improvements**: Formatting, naming
- **Minor optimizations**: <10ms gains
- **Documentation**: Comments, JSDoc
- **Optional refactoring**: Nice-to-have improvements

### Skipped (Not Applicable)
- **Conflicts with project standards**: Bot suggestion violates project rules
- **Already fixed**: Issue addressed by other means
- **Out of scope**: Not relevant to current PR
- **Low ROI**: High effort, low impact

## Integration with Git Workflow

### Recommended Flow
```bash
1. Create PR → Bots review automatically (wait 2-5 min)
2. Run /ai-review to generate unified TodoList
3. Review Critical items (Zero-Tolerance + consensus)
4. Execute Critical fixes immediately
5. Review Important items (2-bot consensus)
6. Execute Important fixes with judgment
7. Review Suggestion items (optional)
8. Commit changes with bot references:
   - fix(auth): resolve SQL injection (AI-Review: Gemini+CodeRabbit+Codex)
   - refactor(services): reduce UserService complexity (AI-Review: Gemini+CodeRabbit)
9. Push and wait for bot re-review
```

## Advanced Usage

### Custom Bot Selection
```bash
/ai-review --bots gemini,coderabbit
# Only analyze specific bots
# Useful when one bot is not available
```

### Strategy Override
```bash
/ai-review --strategy vote
# Use voting strategy instead of default merge
# Faster but may miss nuanced issues
```

### Export Analysis
```bash
/ai-review --export ai-review-summary.md
# Export full analysis to markdown
# Useful for team review and documentation
```

### Dry Run
```bash
/ai-review --dry-run
# Show analysis without creating TodoList
# Preview before committing to execution
```

## Performance Optimization

### Parallel Execution
```
Sequential (old way):
  Gemini: 5s
  CodeRabbit: 5s
  Codex: 5s
  Total: 15s

Parallel (this command):
  All 3 bots: max(5s, 5s, 5s) = 5s
  Total: 5s + 2s (merge) = 7s

Speedup: 2.1x faster
```

### Caching Strategy
```yaml
Cache review data:
  key: pr-{number}-bot-{name}-commit-{sha}
  ttl: 1 hour

Benefits:
  - Avoid re-fetching unchanged reviews
  - Faster re-runs after adjustments
  - Reduced GitHub API calls
```

## Tool Requirements
- **GitHub CLI** (`gh`) installed and authenticated
- **Repository** must have AI bots configured as PR reviewers
- **Bots**: At least one of Gemini/CodeRabbit/Codex active
- **Current branch** must have associated PR or provide PR number

## Setup AI Bots

### Gemini Code Assist
1. Visit [Gemini Code Assist GitHub App](https://developers.google.com/gemini-code-assist/docs/set-up-code-assist-github)
2. Install on organization/account
3. Select repositories
4. Gemini will auto-review PRs

### CodeRabbit AI
1. Visit [CodeRabbit](https://coderabbit.ai/)
2. Install GitHub App
3. Configure repositories
4. CodeRabbit will auto-review PRs

### ChatGPT Codex Connector
1. Visit [Codex Connector](https://github.com/apps/chatgpt-codex-connector)
2. Install GitHub App
3. Configure repositories
4. Codex will auto-review PRs

## Implementation Details

### Actual Execution (Python Scripts)

이 명령어는 Python 스크립트로 구현되어 있습니다:

```bash
python3 .claude/scripts/ai-review/ai_review.py [pr-number] [options]
```

### Available Options

```bash
--bots gemini coderabbit codex   # 분석할 봇 선택
--preview                         # 미리보기 (중복 제거 리포트만)
--analyze-only                    # 분석만 (상태 저장 안 함)
--force                           # 이미 처리된 댓글도 재처리
--output FILE                     # 출력 파일 지정
--clean                           # 모든 상태 초기화
--clean-pr N                      # 특정 PR 상태 제거
--stats                           # 상태 통계 출력
```

### State Management (자동 정리)

- **TTL**: 7일 (오래된 PR 자동 삭제)
- **크기 제한**: 최대 100개 PR
- **수동 개입 불필요**: 매 실행 시 자동 정리

## Limitations
- Only supports Gemini, CodeRabbit, and Codex (most common bots)
- Requires GitHub CLI access and authentication
- Analysis quality depends on bot review quality
- Cannot modify bot reviews or trigger re-reviews
- Deduplication may occasionally merge distinct issues (similarity > 0.8)
- Python 3.7+ 필요 (dataclasses 사용)

## FAQ

### Q: What if only one bot is available?
**A**: Use `--bots` flag. Example: `/ai-review 42 --bots gemini`

### Q: How accurate is deduplication?
**A**: Similarity threshold 0.8 (80%). Tested with 95%+ accuracy.

### Q: What if bots disagree on priority?
**A**: Voting system: 3-bot → Critical, 2-bot → Important, 1-bot → Suggestion.

### Q: Can I customize Zero-Tolerance rules?
**A**: Yes, edit `prioritizer.py` ZERO_TOLERANCE_PATTERNS dict.

### Q: How do I add a new bot?
**A**: Add to `fetch_reviews.py` BOT_USERS dict and update parsers.

### Q: 같은 PR을 여러 번 실행하면?
**A**: 이미 처리된 댓글은 자동 필터링됩니다. `--force`로 재처리 가능.

### Q: 상태 파일이 계속 쌓이지 않나?
**A**: 7일 TTL + 100개 제한으로 자동 정리됩니다.
