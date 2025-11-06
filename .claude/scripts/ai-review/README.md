# AI Review Integration System

여러 AI 봇(Gemini, CodeRabbit, Codex) 리뷰를 통합하여 우선순위별 TodoList 자동 생성

## 🎯 핵심 기능

1. **중복 방지**: 이미 처리된 댓글 자동 필터링 (7일 TTL, 100개 제한)
2. **중복 제거**: 여러 봇의 유사한 댓글 병합 (Similarity > 0.8)
3. **투표 시스템**: 봇 간 합의 기반 우선순위 결정
4. **Zero-Tolerance**: 프로젝트 규칙 위반 자동 Critical 처리
5. **TodoList 생성**: 우선순위별 마크다운 생성

## 📦 모듈 구조

```
.claude/scripts/ai-review/
├── ai_review.py          # 통합 실행 스크립트 (메인)
├── state_manager.py      # 상태 관리 (TTL 7일, 100개 제한)
├── fetch_reviews.py      # GitHub API 봇 댓글 수집
├── deduplicator.py       # 중복 제거 (TF-IDF 코사인 유사도)
├── prioritizer.py        # 우선순위 계산 (투표 + Zero-Tolerance)
├── todo_generator.py     # TodoList 마크다운 생성
└── review-state.json     # 처리 이력 (자동 생성)
```

## 🚀 사용 방법

### Claude Code 명령어

```bash
# 현재 브랜치 PR 분석
/ai-review

# 특정 PR 분석
/ai-review 42

# 특정 봇만 분석
/ai-review 42 --bots gemini coderabbit

# 미리보기 (상태 저장 안 함)
/ai-review 42 --preview

# 강제 재처리
/ai-review 42 --force
```

### 직접 실행

```bash
# 기본 사용
python3 ai_review.py 42

# 옵션
python3 ai_review.py 42 \
  --bots gemini coderabbit \
  --output my-todolist.md \
  --preview

# 상태 관리
python3 ai_review.py --stats      # 통계
python3 ai_review.py --clean      # 전체 초기화
python3 ai_review.py --clean-pr 42 # PR 42 제거
```

## 📊 실행 흐름

```
1. ReviewStateManager
   ↓ 기존 처리 댓글 확인 (7일 TTL 자동 정리)

2. fetch_reviews.py
   ↓ GitHub API로 3개 봇 댓글 수집

3. state_manager.filter_new_comments()
   ↓ 새 댓글만 필터링 (중복 방지)

4. deduplicator.py
   ↓ Similarity > 0.8 병합 (TF-IDF 코사인)

5. prioritizer.py
   ↓ Zero-Tolerance 체크 + 투표 시스템

6. todo_generator.py
   ↓ 우선순위별 마크다운 생성

7. state_manager.mark_as_processed()
   ↓ 처리 완료 마킹
```

## 🎯 우선순위 결정 로직

### 1. Zero-Tolerance (최우선 → Critical)

```python
ZERO_TOLERANCE_PATTERNS = {
    "Lombok 사용": r"@(Data|Builder|Getter|Setter)",
    "Law of Demeter": r"\.get\w+\(\)\.get\w+\(\)",
    "Transaction 경계": r"@Transactional.*?(RestTemplate|WebClient)",
    "Long FK 위반": r"@(ManyToOne|OneToMany|OneToOne|ManyToMany)"
}
```

### 2. 투표 시스템

| 투표 수 | 우선순위 | 설명 |
|---------|----------|------|
| 3봇 합의 | **Critical** | Gemini + CodeRabbit + Codex 모두 동의 |
| 2봇 합의 | **Important** | 2개 봇 동의 |
| 1봇만 | **Suggestion** | 1개 봇만 제기 |

### 3. 카테고리 조정

- `security` → Auto Critical (SQL Injection, XSS 등)
- `performance` → memory leak, deadlock → Critical
- `error-handling` → exception, null pointer → Important

## 📝 생성되는 TodoList 예시

```markdown
# 🤖 AI Review TodoList

## 📊 요약
- ✅ High Priority (Must-Fix): **3개**
  - ⚠️ Zero-Tolerance 위반: **2개**
- ⚠️ Medium Priority (Should-Fix): **5개**
- 💡 Low Priority (Nice-to-Have): **12개**

## ✅ High Priority (Must-Fix)

### ✅ 1. Fix Order.java:45 - Lombok @Data ⚠️ **Zero-Tolerance**

**📍 위치**: `domain/.../Order.java:45`
**🤖 봇**: gemini (투표: 1)
**💡 이유**: Zero-Tolerance: Lombok 금지
**⏱️ 예상 시간**: 5-15분
```

## 🧹 자동 정리 (State Management)

### TTL 기반 정리

- **7일 이상** 된 PR 자동 삭제
- 매 실행 시 자동 체크

### 크기 제한

- 최대 **100개 PR**까지 유지
- 초과 시 오래된 순으로 삭제

### 수동 제어

```bash
# 통계 확인
python3 ai_review.py --stats

# 특정 PR 제거
python3 ai_review.py --clean-pr 42

# 전체 초기화
python3 ai_review.py --clean
```

## 🔧 커스터마이징

### 1. Zero-Tolerance 규칙 추가

`prioritizer.py` 수정:

```python
ZERO_TOLERANCE_PATTERNS = {
    "새 규칙": {
        "pattern": r"your_regex_pattern",
        "reason": "Zero-Tolerance: 설명"
    }
}
```

### 2. 봇 추가

`fetch_reviews.py` 수정:

```python
BOT_USERS = {
    "new-bot[bot]": "newbot"
}
```

### 3. TTL/크기 제한 변경

`state_manager.py` 수정:

```python
self.max_prs = 100  # 최대 PR 개수
self.ttl_days = 7   # TTL (일)
```

## 📋 요구사항

- Python 3.7+ (dataclasses 사용)
- GitHub CLI (`gh`) 설치 및 인증
- AI 봇 설정된 저장소 (Gemini, CodeRabbit, Codex)

## 🐛 문제 해결

### Q: "GitHub CLI 인증 실패"
**A**: `gh auth login` 실행

### Q: "봇 댓글이 수집 안 됨"
**A**: AI 봇이 PR에 댓글을 남겼는지 확인

### Q: "중복 제거가 너무 많이 됨"
**A**: `deduplicator.py` SIMILARITY_THRESHOLD 조정 (기본 0.8)

### Q: "Zero-Tolerance가 작동 안 함"
**A**: `prioritizer.py` 정규식 패턴 확인

## 📚 참고

- [ai-review.md](.claude/commands/ai-review.md) - 전체 문서
- [gemini-review.md](~/.claude/commands/gemini-review.md) - 레거시 (Deprecated)

## 🤝 기여

버그 리포트 및 개선 제안은 Issues에 등록해주세요.
