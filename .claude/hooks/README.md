# 🔧 Dynamic Hooks 시스템

**Claude Code가 코드 생성 시 자동으로 실행되는 Hook 시스템**

> ⚠️ **중요**: 이것은 **Claude Code Dynamic Hooks**입니다. **Git Hooks**와는 다릅니다.
> - **Claude Hooks** (`.claude/hooks/`): Claude가 코드 생성/수정 시 실행 (이 문서)
> - **Git Hooks** (`hooks/`): `git commit` 실행 시 검증

---

## 📋 목차

- [개요](#개요)
- [Hook 파일 설명](#hook-파일-설명)
- [실행 흐름](#실행-흐름)
- [키워드 매핑](#키워드-매핑)
- [검증 규칙](#검증-규칙)
- [로그 및 디버깅](#로그-및-디버깅)
- [커스터마이징](#커스터마이징)

---

## 🎯 개요

### 목적

Claude가 **코드를 생성하는 시점**에 규칙을 주입하고, **코드 생성 직후** 즉시 검증하여 처음부터 올바른 코드를 생성하도록 유도합니다.

### 2개의 Hook

| Hook | 실행 시점 | 역할 |
|------|----------|------|
| `user-prompt-submit.sh` | 사용자 요청 제출 시 (코드 생성 **전**) | 키워드 감지 → Layer 매핑 → 규칙 주입 |
| `after-tool-use.sh` | Write/Edit 도구 사용 직후 (코드 생성 **후**) | 파일 경로 분석 → 검증 실행 |

### 핵심 스크립트

| 스크립트 | 역할 | 사용 위치 |
|----------|------|----------|
| `build-rule-cache.py` | 90개 마크다운 → JSON Cache | Cache 빌드 |
| `validation-helper.py` | Cache 기반 고속 검증 (148ms) | after-tool-use.sh |

### 보조 라이브러리

| 파일 | 역할 |
|------|------|
| `../commands/lib/inject-rules.py` | index.json → Layer 규칙 추출 → Markdown 출력 |

---

## 📦 Hook 파일 설명

### 1. user-prompt-submit.sh

**실행 시점**: 사용자가 Claude에게 요청을 제출할 때 (코드 생성 **전**)

**목적**: 사용자 입력을 분석하여 해당 Layer의 규칙을 자동으로 주입

**처리 과정**:

```bash
1. 사용자 입력 수신
   "Order 엔티티를 만들어줘"

2. 키워드 분석 및 점수 계산
   "entity" → adapter-persistence (+30점)
   "도메인" → domain (+25점)
   ...

3. 임계값 확인 (25점 이상)
   Context Score: 30 → 규칙 주입 실행

4. inject-rules.py 호출
   python3 ../commands/lib/inject-rules.py adapter-persistence

5. Markdown 규칙 출력
   ## 🎯 ADAPTER-PERSISTENCE 레이어 규칙
   ### ❌ 금지 규칙
   - @ManyToOne, @OneToMany 사용 금지
   - Setter 메서드 금지
   ...
```

**로그 예시**:

```
[2025-10-17 12:34:56] user-prompt-submit triggered
User Input: Order 엔티티를 만들어줘
  → Detected: entity → adapter-persistence (+30 score)
  → Context Score: 30
  → Detected Layers: adapter-persistence
  → Strategy: CACHE_BASED (inject-rules.py)
  → Injecting rules for layer: adapter-persistence
```

---

### 2. after-tool-use.sh

**실행 시점**: Claude가 Write/Edit 도구를 사용한 직후 (코드 생성 **후**)

**목적**: 생성된 코드를 즉시 검증하고 규칙 위반 시 경고

**처리 과정**:

```bash
1. 도구 사용 감지
   Tool: Write
   File: domain/src/main/java/.../OrderEntity.java

2. 파일 경로 기반 Layer 감지
   "adapter/out/persistence" → adapter-persistence

3. validation-helper.py 호출
   python3 validation-helper.py "$FILE_PATH" "$LAYER"

4. 검증 결과 출력
   ✅ Validation Passed
   또는
   ❌ Validation Failed: @ManyToOne detected
```

**로그 예시**:

```
[2025-10-17 12:35:10] after-tool-use triggered
Tool: Write
File: adapter/out/persistence/jpa/OrderEntity.java
  → Detected Layer: ADAPTER-PERSISTENCE
  → Running cache-based validation for layer: adapter-persistence
  → Validation: ✅ PASSED
```

---

## 🔄 실행 흐름

### 전체 흐름도

```
┌──────────────────────────────────────────────────────────┐
│  사용자: "Order 엔티티를 만들어줘"                          │
└────────────────────┬─────────────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────────────┐
│  user-prompt-submit.sh                                   │
│  1. 키워드 분석: "entity" → 30점                          │
│  2. Layer 매핑: adapter-persistence                       │
│  3. inject-rules.py adapter-persistence                  │
│  4. Markdown 규칙 출력:                                   │
│     - ❌ @ManyToOne 금지                                 │
│     - ✅ Long FK 사용                                     │
└────────────────────┬─────────────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────────────┐
│  Claude Code                                             │
│  - 주입된 규칙 읽기                                        │
│  - OrderEntity.java 생성                                 │
│  - Long userId 사용 (NOT @ManyToOne)                     │
└────────────────────┬─────────────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────────────┐
│  after-tool-use.sh                                       │
│  1. Write 도구 사용 감지                                  │
│  2. 파일 경로: adapter/out/persistence/.../OrderEntity.java│
│  3. Layer 감지: adapter-persistence                       │
│  4. validation-helper.py 호출                             │
└────────────────────┬─────────────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────────────┐
│  validation-helper.py                                    │
│  1. index.json 로드 (O(1))                               │
│  2. layerIndex["adapter-persistence"] 추출                │
│  3. Anti-pattern 검사:                                    │
│     - @ManyToOne: ❌ Not found                           │
│     - Setter: ❌ Not found                                │
│  4. 결과: ✅ Validation Passed (148ms)                   │
└──────────────────────────────────────────────────────────┘
```

---

## 🔍 키워드 매핑

### Layer 감지 키워드 (user-prompt-submit.sh)

| Layer | 키워드 | 점수 |
|-------|--------|------|
| **domain** | aggregate, domain, 도메인, 비즈니스 로직 | 30점 |
| **application** | usecase, service, command, query, 유즈케이스 | 30점 |
| **adapter-rest** | controller, rest, api, request, response | 30점 |
| **adapter-persistence** | repository, jpa, database, persistence, entity | 30점 |
| **testing** | test, 테스트, junit, mockito | 25점 |

**임계값**: 25점 이상이면 규칙 주입 실행

**예시**:

```bash
# "Order aggregate를 만들어줘"
# → "aggregate" (30점) → domain layer
# → inject-rules.py domain

# "CreateOrderUseCase를 만들어줘"
# → "usecase" (30점) → application layer
# → inject-rules.py application

# "OrderController를 만들어줘"
# → "controller" (30점) → adapter-rest layer
# → inject-rules.py adapter-rest

# "OrderRepository를 만들어줘"
# → "repository" (30점) → adapter-persistence layer
# → inject-rules.py adapter-persistence
```

---

## ✅ 검증 규칙

### validation-helper.py가 검증하는 항목

| Layer | 검증 항목 | Anti-Pattern |
|-------|----------|--------------|
| **domain** | Spring/JPA 의존 금지 | `org.springframework`, `jakarta.persistence` |
| **domain** | Lombok 금지 | `@Data`, `@Builder`, `@Getter`, `@Setter` |
| **domain** | Law of Demeter | `\.get\w+\(\)\.get\w+\(\)` (Getter 체이닝) |
| **application** | Adapter 직접 참조 금지 | `import.*adapter.*` |
| **adapter-persistence** | JPA 관계 금지 | `@ManyToOne`, `@OneToMany`, `@OneToOne` |
| **adapter-persistence** | Setter 금지 | `public void set\w+\(` |
| **adapter-persistence** | Public constructor 금지 | `public \w+Entity\(` |
| **adapter-rest** | Inner class DTO 금지 | `public class.*Request.*{` (Controller 내부) |
| **adapter-rest** | Non-record DTO | `public class.*Request` (record여야 함) |

### 검증 결과 형식

**✅ 성공**:

```
---

✅ **Validation Passed**

파일: `domain/src/main/java/.../Order.java`

모든 규칙을 준수합니다!

---
```

**❌ 실패**:

```
---

⚠️ **Validation Failed**

**파일**: `adapter/.../OrderEntity.java`

**규칙 위반**: Long FK 전략 (JPA 관계 어노테이션 금지)

**문제**: Anti-pattern detected: @ManyToOne

**금지 사항**:
- ❌ @ManyToOne, @OneToMany, @OneToOne, @ManyToMany
- ❌ 엔티티 간 직접 참조

**참고**: `docs/coding_convention/04-persistence-layer/jpa-entity-design/01_long-fk-strategy.md`

💡 코드를 수정한 후 다시 시도하세요.

---
```

---

## 📊 로그 및 디버깅

### 로그 위치

```bash
.claude/hooks/logs/hook-execution.jsonl
```

### 로그 확인

```bash
# 전체 로그
cat .claude/hooks/logs/hook-execution.jsonl

# 최근 50줄
tail -50 .claude/hooks/logs/hook-execution.jsonl

# 실시간 로그 (새 터미널에서)
tail -f .claude/hooks/logs/hook-execution.jsonl
```

### 로그 예시

```
[2025-10-17 12:34:56] user-prompt-submit triggered
User Input: Order 엔티티를 만들어줘
Keyword Analysis:
  - "entity": adapter-persistence (+30 score)
Context Score: 30
Detected Layers: adapter-persistence
Strategy: CACHE_BASED
Command: python3 /path/to/inject-rules.py adapter-persistence
Exit Code: 0

[2025-10-17 12:35:10] after-tool-use triggered
Tool: Write
File Path: adapter/out/persistence/jpa/OrderEntity.java
Detected Layer: ADAPTER-PERSISTENCE
Validation Command: python3 /path/to/validation-helper.py "adapter/out/persistence/jpa/OrderEntity.java" "adapter-persistence"
Validation Result: PASSED
Exit Code: 0
```

### 수동 테스트

```bash
# user-prompt-submit.sh 테스트
echo "Order entity 만들어줘" | bash .claude/hooks/user-prompt-submit.sh

# after-tool-use.sh 테스트 (수동 트리거 어려움, 로그로 확인)
cat .claude/hooks/logs/hook-execution.jsonl | grep "after-tool-use"

# validation-helper.py 직접 테스트
python3 .claude/hooks/scripts/validation-helper.py \
  domain/src/main/java/.../Order.java \
  domain
```

---

## ⚙️ 커스터마이징

### 새로운 Layer 추가

**user-prompt-submit.sh 수정**:

```bash
# 키워드 매핑 추가
declare -A LAYER_KEYWORDS=(
    ["batch"]="batch scheduler 스케줄러"  # 새로 추가
)

# 점수 계산 로직은 자동으로 처리됨
```

**inject-rules.py 지원 (자동)**:

- index.json에 `layerIndex["batch"]`가 있으면 자동으로 작동
- 없으면 경고 메시지 출력

### 새로운 키워드 추가

**user-prompt-submit.sh 수정**:

```bash
# 기존 Layer에 키워드 추가
declare -A LAYER_KEYWORDS=(
    ["domain"]="aggregate domain 도메인 비즈니스로직 aggregate-root"  # aggregate-root 추가
)
```

### 검증 규칙 추가

**validation-helper.py는 Cache 기반이므로 코드 수정 불필요**:

```bash
# 1. 규칙 문서 작성
vim docs/coding_convention/02-domain-layer/new-rule/01_new-validation.md

# 2. validation.antiPatterns 섹션에 정규식 추가
# validation:
#   antiPatterns:
#     - "new-anti-pattern-regex"

# 3. Cache 재빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 4. 자동으로 검증에 반영됨
```

---

## 🔗 관련 문서

### 상위 문서

- **[.claude/README.md](../README.md)** - Dynamic Hooks + Cache 시스템 전체 가이드
- **[.claude/CLAUDE.md](../CLAUDE.md)** - 프로젝트 중앙 설정 파일

### Cache 시스템

- **[Cache README](../cache/rules/README.md)** - Cache 시스템 상세 문서
- **[index.json](../cache/rules/index.json)** - 메타 인덱스

### 스크립트

- **[build-rule-cache.py](./scripts/build-rule-cache.py)** - Cache 빌드 스크립트
- **[validation-helper.py](./scripts/validation-helper.py)** - 검증 엔진
- **[inject-rules.py](../commands/lib/inject-rules.py)** - 규칙 주입 엔진

### 규칙 문서

- **[Coding Convention](../../docs/coding_convention/)** - 90개 규칙 (Layer별)

---

## 🎯 효과

### Before (Hook 없이)

```
Claude: "Order 엔티티를 만들어줘"
  → 일반 지식 기반 코드 생성
  → @ManyToOne 사용 (JPA 표준)
  → Setter 메서드 포함
  → 커밋 시 Git Hook에서 차단
  → 수정 필요
```

### After (Dynamic Hooks)

```
사용자: "Order 엔티티를 만들어줘"
  ↓
user-prompt-submit.sh
  → "entity" 키워드 감지
  → adapter-persistence 규칙 주입
  ↓
Claude
  → 규칙 읽기
  → Long FK 사용
  → NO Setter
  → Protected constructor + static factory
  ↓
after-tool-use.sh
  → 즉시 검증
  → ✅ Pass
  ↓
커밋 성공 (Git Hook도 통과)
```

**결과**:
- 처음부터 올바른 코드 생성
- 재작업 불필요
- 개발 속도 향상

---

**🎯 목표**: Claude가 코드 생성 시점부터 프로젝트 규칙을 준수하도록 자동 가이드

---

© 2025 Ryu-qqq. All Rights Reserved.
