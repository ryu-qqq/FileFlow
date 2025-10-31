# Cache 시스템 완전 설명 (FAQ)

## ❓ 자주 묻는 질문

### Q1: "cache_index_loaded에 total_rules: 0은 정상인가요?"

**✅ 예, 정상입니다!**

```json
// 로그를 보면:
{"event": "cache_index_loaded", "total_rules": 0}  // ← 인덱스 파일만 로드
{"event": "cache_injection", "rules_loaded": 14}   // ← 실제 규칙 주입
```

**이유**:
- `cache_index_loaded`: "index.json 파일을 읽었다" (메타데이터만)
- `cache_injection`: "실제 규칙 JSON 파일들을 읽어서 Claude에게 주입"

### Q2: "그럼 규칙은 매번 새로 넣는 건가요?"

**✅ 예, 세션마다 Layer별로 자동 주입됩니다!**

```
세션 1:
사용자: "Order entity 만들어줘"
  → 키워드 "entity" 감지 → Layer: domain
  → cache_injection: domain 규칙 14개 주입
  → Claude가 이 규칙들을 기반으로 코드 생성

세션 2:
사용자: "UseCase 만들어줘"
  → 키워드 "usecase" 감지 → Layer: application
  → cache_injection: application 규칙 14개 주입
  → Claude가 application 규칙을 기반으로 코드 생성
```

**핵심**: 매 프롬프트마다 **필요한 Layer 규칙만** 자동 주입

### Q3: "다른 프로젝트에 복사했는데 작동 안 하는 이유는?"

**❌ 복사만으로는 부족합니다!**

## 🔧 시스템 구조

### 1️⃣ index.json (규칙 인덱스)

**파일**: `.claude/cache/rules/index.json`

```json
{
  "version": "1.0.0",
  "totalRules": 113,        // ← 전체 규칙 개수 (메타데이터)
  "keywordIndex": {
    "entity": ["domain-layer-...", "persistence-layer-..."],
    "usecase": ["application-layer-..."]
  },
  "layerIndex": {
    "domain": ["domain-layer-01.json", "domain-layer-02.json", ...],
    "application": ["application-layer-01.json", ...]
  }
}
```

**역할**:
- 키워드 → 규칙 파일 매핑
- Layer → 규칙 파일 목록
- **규칙 내용은 없음** (파일 목록만)

### 2️⃣ 규칙 JSON 파일들 (실제 규칙 내용)

**위치**: `.claude/cache/rules/*.json` (113개 파일)

```json
// domain-layer-law-of-demeter-01_getter-chaining-prohibition.json
{
  "id": "domain-layer-law-of-demeter-01_getter-chaining-prohibition",
  "title": "Getter Chaining 금지",
  "layer": "domain",
  "priority": "zero-tolerance",
  "content": "❌ order.getCustomer().getAddress().getZip() 금지...",
  "keywords": ["getter", "chaining", "law of demeter"]
}
```

**역할**:
- 실제 규칙 내용 담김
- Claude에게 주입되는 텍스트

### 3️⃣ Hook 로직 (자동 주입)

**파일**: `.claude/hooks/user-prompt-submit.sh`

```bash
# 1단계: 키워드 분석
detect_keywords() {
  # "entity" 키워드 감지 → domain layer
  # "usecase" 키워드 감지 → application layer
}

# 2단계: index.json 로드
cat .claude/cache/rules/index.json
# 로그: {"event": "cache_index_loaded", "total_rules": 0}
# ↑ index.json은 파일 목록만, 규칙 내용 없음

# 3단계: Layer별 규칙 주입
for layer in "${DETECTED_LAYERS[@]}"; do
  # domain layer 규칙 14개 파일 읽기
  for rule_file in domain-layer-*.json; do
    cat "$rule_file"  # ← 실제 규칙 내용 출력 (Claude에게 주입)
  done
  # 로그: {"event": "cache_injection", "layer": "domain", "rules_loaded": 14}
done
```

## 📊 로그 흐름 완전 분석

```json
// 1️⃣ 세션 시작
{"event": "session_start", "session_id": "1761877404-88242"}

// 2️⃣ 키워드 분석
{"event": "keyword_analysis",
 "context_score": 60,        // ← 키워드 점수
 "detected_layers": ["application", "enterprise"],  // ← 감지된 Layer
 "detected_keywords": ["spring", "event"]}

// 3️⃣ index.json 로드 (파일 목록만)
{"event": "cache_index_loaded",
 "index_file": ".../index.json",
 "total_rules": 0}  // ← 인덱스 자체에는 규칙 내용 없음 (정상!)

// 4️⃣ application layer 규칙 주입
{"event": "cache_injection",
 "layer": "application",
 "total_rules_available": 14,  // ← application layer에 14개 규칙 파일
 "rules_loaded": 14,            // ← 14개 모두 읽어서 Claude에게 주입
 "cache_files": [
   "application-layer-assembler-pattern-01_assembler-responsibility.json",
   "application-layer-dto-patterns-01_request-response-dto.json",
   ...
 ],
 "estimated_tokens": 2505}  // ← 주입된 규칙의 토큰 수

// 5️⃣ enterprise layer 규칙 주입
{"event": "cache_injection",
 "layer": "enterprise",
 "total_rules_available": 10,
 "rules_loaded": 10,
 "estimated_tokens": 2752}

// 6️⃣ 주입 완료
{"event": "cache_injection_complete",
 "layers_count": 2}  // ← 2개 Layer (application + enterprise)
```

## 💡 핵심 이해

### index.json vs 규칙 파일

| 항목 | index.json | 규칙 JSON 파일 (113개) |
|------|-----------|----------------------|
| **역할** | 파일 목록 + 키워드 매핑 | 실제 규칙 내용 |
| **크기** | 작음 (목록만) | 큼 (규칙 전문) |
| **로그** | cache_index_loaded (total_rules: 0) | cache_injection (rules_loaded: 14) |
| **Claude 주입** | ❌ 주입 안 됨 | ✅ 주입됨 |

### 로그 해석

```
"total_rules": 0     // ← 인덱스 로드 (파일 목록)
"rules_loaded": 14   // ← 실제 규칙 주입 (내용)
```

**이해하기 쉽게**:
- `cache_index_loaded`: "전화번호부 열었다" (전화번호만 봄)
- `cache_injection`: "실제로 14명에게 전화했다" (내용 전달)

## 🚀 다른 프로젝트에 적용하기

### ❌ 잘못된 방법 (작동 안 함)

```bash
# .claude/ 디렉토리만 복사
cp -r project-A/.claude project-B/
# → Cache 파일이 없어서 작동 안 함!
```

**문제**:
1. `.claude/cache/rules/` 디렉토리가 비어있음
2. `docs/coding_convention/` 디렉토리가 없음
3. Cache 빌드가 안 됨

### ✅ 올바른 방법 (3단계)

#### 1단계: 전체 구조 복사

```bash
cd /path/to/new-project

# 필수 디렉토리 복사
cp -r /path/to/claude-spring-standards/docs/coding_convention/ ./docs/
cp -r /path/to/claude-spring-standards/.claude/ ./

# 확인
ls docs/coding_convention/  # ← 90개 마크다운 규칙
ls .claude/cache/rules/     # ← 아직 비어있음 (정상)
```

#### 2단계: Cache 빌드

```bash
# Cache 생성
python3 .claude/hooks/scripts/build-rule-cache.py

# 확인
ls .claude/cache/rules/*.json
# ✅ 114개 파일 (113개 규칙 + 1개 index.json)

cat .claude/cache/rules/index.json
# ✅ totalRules: 113
```

#### 3단계: Hook 설정 확인

```bash
# Hook 설정 확인
cat .claude/settings.local.json

# 출력:
# {
#   "hooks": {
#     "UserPromptSubmit": [
#       {"matcher": "", "hooks": [{"command": ".claude/hooks/user-prompt-submit.sh"}]}
#     ],
#     "PostToolUse": [...]
#   }
# }

# Hook 실행 권한
chmod +x .claude/hooks/*.sh
chmod +x .claude/hooks/scripts/*.sh
chmod +x .claude/hooks/scripts/*.py
```

#### 4단계: 테스트

```bash
# Claude Code 실행
claude code

# 테스트 프롬프트
"Order entity 만들어줘"

# 로그 확인
tail -f .claude/hooks/logs/hook-execution.jsonl

# ✅ 정상 작동 확인:
# {"event": "keyword_analysis", "detected_layers": ["domain"]}
# {"event": "cache_index_loaded", "total_rules": 0}  ← 정상!
# {"event": "cache_injection", "layer": "domain", "rules_loaded": 14}
```

## 📋 체크리스트

### 초기 설정
- [ ] `docs/coding_convention/` 복사됨 (90개 마크다운)
- [ ] `.claude/` 복사됨
- [ ] `python3 .claude/hooks/scripts/build-rule-cache.py` 실행
- [ ] `.claude/cache/rules/*.json` 114개 파일 생성 확인
- [ ] `.claude/settings.local.json` Hook 설정 확인
- [ ] Hook 스크립트 실행 권한 부여

### 작동 검증
- [ ] `claude code` 실행
- [ ] 테스트 프롬프트 입력
- [ ] `tail -f .claude/hooks/logs/hook-execution.jsonl` 로그 확인
- [ ] `cache_index_loaded` (total_rules: 0) ← 정상!
- [ ] `cache_injection` (rules_loaded: N) ← 규칙 주입 확인

## 🔍 문제 해결

### 문제 1: "cache_injection 이벤트가 안 나타남"

**원인**: Cache 빌드 안 됨

**해결**:
```bash
python3 .claude/hooks/scripts/build-rule-cache.py
ls .claude/cache/rules/*.json  # 114개 확인
```

### 문제 2: "total_rules가 0이에요!"

**답변**: ✅ **정상입니다!**

```
cache_index_loaded의 total_rules: 0 = index.json은 파일 목록만
cache_injection의 rules_loaded: 14 = 실제 규칙 주입됨
```

### 문제 3: "다른 프로젝트에서 작동 안 해요"

**체크**:
```bash
# 1. Cache 파일 확인
ls .claude/cache/rules/*.json
# → 114개 있어야 함

# 2. Hook 설정 확인
cat .claude/settings.local.json
# → UserPromptSubmit 있어야 함

# 3. 로그 확인
tail .claude/hooks/logs/hook-execution.jsonl
# → cache_injection 이벤트 있어야 함
```

## 💡 요약

### 시스템 동작

```
1. 사용자 프롬프트
   ↓
2. 키워드 감지 (entity, usecase, ...)
   ↓
3. Layer 매핑 (domain, application, ...)
   ↓
4. index.json 로드 (파일 목록 확인)
   → 로그: cache_index_loaded (total_rules: 0) ← 정상!
   ↓
5. Layer별 JSON 파일 읽기
   → domain-layer-01.json, domain-layer-02.json, ...
   ↓
6. Claude에게 규칙 주입
   → 로그: cache_injection (rules_loaded: 14)
   ↓
7. Claude가 규칙 기반 코드 생성
```

### 핵심 답변

1. **total_rules: 0은 정상**: index.json은 목록만, 내용 없음
2. **매번 규칙 주입**: Layer별로 필요한 규칙만 자동 주입
3. **다른 프로젝트 적용**: 복사 + Cache 빌드 + Hook 설정

## 🎯 다음 단계

이제 시스템을 이해했으니:

1. **다른 프로젝트에 적용**: 위 3단계 가이드 참고
2. **규칙 커스터마이징**: `docs/coding_convention/` 수정 → Cache 재빌드
3. **LangFuse 통합**: 효율 측정 (선택 사항)

**궁금한 점이 있으면 언제든 물어보세요!** 🚀
