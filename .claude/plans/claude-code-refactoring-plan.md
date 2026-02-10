# Claude Code Skills/Commands/Agents 리팩토링 계획

> 생성일: 2026-02-04
> 참조: Anthropic 공식 문서, Serena Memory 분석 결과

---

## 1. 핵심 개념 정리 (Anthropic 공식 문서 기준)

### 1.1 Commands vs Skills vs Subagents

| 속성 | **Commands** | **Skills** | **Subagents** |
|------|-------------|-----------|---------------|
| **파일 위치** | `.claude/commands/<name>.md` | `.claude/skills/<name>/SKILL.md` | `.claude/agents/<name>.md` |
| **호출 방식** | `/command-name` | `/skill-name` + 자동 호출 | Claude가 Task tool로 위임 |
| **컨텍스트** | 메인 세션에 주입 | 메인 세션에 주입 (기본) | **독립된 새 컨텍스트 (200K)** |
| **격리 실행** | ❌ 불가 | ✅ `context: fork` 옵션 | ✅ 기본 동작 |
| **도구 제한** | ❌ 불가 | ✅ `allowed-tools` | ✅ `tools` 필드 |
| **frontmatter** | 선택 | **필수** | **필수** |

### 1.2 공식 발표: Commands → Skills 통합

> **"Custom slash commands have been merged into skills."**
> `.claude/commands/review.md`와 `.claude/skills/review/SKILL.md`는 동일하게 `/review`를 생성합니다.
> 기존 commands 파일은 계속 작동하지만, **Skills가 상위 버전**입니다.

### 1.3 컨텍스트 동작 방식

```
┌─────────────────────────────────────────────────────────────────┐
│  Skills/Commands (메인 컨텍스트 주입)                             │
│  ─────────────────────────────────────────                      │
│  /skill 호출 → SKILL.md 내용이 메인 컨텍스트에 추가됨              │
│  → MCP 호출하면 결과도 메인 컨텍스트에 쌓임                        │
│  → ⚠️ 컨텍스트 폭발 위험!                                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Subagents (독립 컨텍스트)                                       │
│  ─────────────────────────                                      │
│  Task tool로 spawn → 새로운 200K 컨텍스트 생성 (메인과 별개)       │
│  → MCP 호출 결과는 subagent 컨텍스트에만 쌓임                     │
│  → 완료 후 요약(3-5K)만 메인으로 반환                             │
│  → ✅ 메인 컨텍스트 보호!                                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Skills + context: fork (격리 실행)                              │
│  ────────────────────────────────                               │
│  /skill 호출 → 독립 컨텍스트에서 실행                             │
│  → Subagent와 동일한 격리 효과                                   │
│  → ✅ 메인 컨텍스트 보호!                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 현재 프로젝트 문제점 분석

### 2.1 파일 구조 현황

#### Commands (6개) - frontmatter ✅ 있음
| 파일 | 줄 수 | 역할 |
|------|------|------|
| `legacy-endpoints.md` | ~260줄 | 엔드포인트 분석 (Query/Command 분류) |
| `legacy-flow.md` | ~330줄 | API 흐름 분석 (Controller→Service→Repository) |
| `legacy-convert.md` | ~260줄 | DTO 변환 (Request/Response 생성) |
| `legacy-query.md` | ~480줄 | Persistence Layer 생성 |
| `legacy-service.md` | ~244줄 | Application Layer 생성 |
| `legacy-controller.md` | ~370줄 | Adapter-In Layer 생성 |

**문제**: 200-480줄의 상세 워크플로우가 메인 컨텍스트에 주입됨

#### Agents (6개) - frontmatter ❌ **없음**
| 파일 | 줄 수 | 문제점 |
|------|------|--------|
| `legacy-endpoints-analyzer.md` | ~314줄 | frontmatter 없음 → **작동 안 함** |
| `legacy-flow-analyzer.md` | ~309줄 | frontmatter 없음 → **작동 안 함** |
| `legacy-dto-converter.md` | ~437줄 | frontmatter 없음 → **작동 안 함** |
| `legacy-query-generator.md` | ~479줄 | frontmatter 없음 → **작동 안 함** |
| `legacy-service-generator.md` | ~244줄 | frontmatter 없음 → **작동 안 함** |
| `legacy-controller-generator.md` | ~264줄 | frontmatter 없음 → **작동 안 함** |

**문제**: frontmatter가 없어서 Subagent로 인식되지 않음 (죽은 파일)

### 2.2 핵심 문제 요약

```
❌ 문제 1: Commands와 Agents 내용 중복
   legacy-endpoints.md ≈ legacy-endpoints-analyzer.md
   legacy-flow.md ≈ legacy-flow-analyzer.md
   ... (6쌍 모두 중복)

❌ 문제 2: Agents frontmatter 누락
   현재: # Legacy Endpoints Analyzer Agent (그냥 제목)
   필요: ---
         name: legacy-endpoints-analyzer
         tools: Glob, Grep, Read, Write
         ---

❌ 문제 3: Commands가 너무 무거움
   200-480줄이 메인 컨텍스트에 주입됨
   → MCP 호출 결과도 메인에 쌓임
   → 컨텍스트 폭발 위험
```

---

## 3. 리팩토링 전략

### 3.1 최종 목표 구조

```
.claude/
├── commands/           # ❌ 삭제 또는 최소화 (Skills로 대체)
│
├── skills/             # ✅ 가벼운 진입점 + context: fork
│   ├── legacy-endpoints/
│   │   └── SKILL.md    # ~30줄, context: fork, agent: legacy-endpoints-analyzer
│   ├── legacy-flow/
│   │   └── SKILL.md
│   ├── legacy-convert/
│   │   └── SKILL.md
│   ├── legacy-query/
│   │   └── SKILL.md
│   ├── legacy-service/
│   │   └── SKILL.md
│   └── legacy-controller/
│       └── SKILL.md
│
└── agents/             # ✅ 상세 워크플로우 + frontmatter 필수
    ├── legacy-endpoints-analyzer.md    # frontmatter + 상세 내용
    ├── legacy-flow-analyzer.md
    ├── legacy-dto-converter.md
    ├── legacy-query-generator.md
    ├── legacy-service-generator.md
    └── legacy-controller-generator.md
```

### 3.2 역할 분담

| 컴포넌트 | 역할 | 크기 | 컨텍스트 |
|---------|------|------|----------|
| **Skills** | 가벼운 진입점, 사용법 안내 | ~30줄 | `context: fork` → 격리 |
| **Agents** | 상세 워크플로우, MCP 호출 | 200-480줄 | 독립 200K |

### 3.3 흐름도

```
사용자: /legacy-endpoints admin:brand
         │
         ▼
┌─────────────────────────────────────┐
│  Skill: legacy-endpoints/SKILL.md   │  (~30줄)
│  - context: fork                    │
│  - agent: legacy-endpoints-analyzer │
└─────────────────────────────────────┘
         │
         ▼ (Task tool로 위임)
┌─────────────────────────────────────┐
│  Agent: legacy-endpoints-analyzer   │  (독립 200K 컨텍스트)
│  - 상세 워크플로우 실행              │
│  - MCP 호출 (spring-standards 등)   │
│  - 결과 생성                        │
└─────────────────────────────────────┘
         │
         ▼ (요약만 반환)
┌─────────────────────────────────────┐
│  메인 컨텍스트                       │
│  - 3-5K 요약 결과만 수신             │
│  - 컨텍스트 보호됨 ✅                │
└─────────────────────────────────────┘
```

---

## 4. 리팩토링 상세 작업

### Phase 1: Agents frontmatter 추가 (우선순위: 높음)

**현재 Agents가 작동하지 않는 근본 원인 해결**

| Agent 파일 | name | description | tools |
|-----------|------|-------------|-------|
| `legacy-endpoints-analyzer.md` | legacy-endpoints-analyzer | 레거시 엔드포인트 분석 | Glob, Grep, Read, Write |
| `legacy-flow-analyzer.md` | legacy-flow-analyzer | 레거시 API 흐름 분석 | Glob, Grep, Read, Write |
| `legacy-dto-converter.md` | legacy-dto-converter | 레거시 DTO 변환 | Read, Write |
| `legacy-query-generator.md` | legacy-query-generator | Persistence Layer 생성 | Read, Write, Edit, Bash |
| `legacy-service-generator.md` | legacy-service-generator | Application Layer 생성 | Read, Write, Edit |
| `legacy-controller-generator.md` | legacy-controller-generator | Adapter-In Layer 생성 | Read, Write, Edit |

**frontmatter 템플릿:**
```yaml
---
name: legacy-endpoints-analyzer
description: 레거시 패키지 엔드포인트를 분석하여 Query/Command로 분류. 마이그레이션 계획 수립의 첫 단계.
tools: Glob, Grep, Read, Write
model: sonnet
---
```

### Phase 2: Skills를 가벼운 진입점으로 수정

**Commands 내용을 Skills로 이관하되, 상세 내용은 Agent로 위임**

**Skill 템플릿 (~30줄):**
```yaml
---
name: legacy-endpoints
description: 레거시 엔드포인트 분석. 마이그레이션 계획 수립의 첫 단계.
context: fork
agent: legacy-endpoints-analyzer
allowed-tools: Glob, Grep, Read, Write
---

# /legacy-endpoints

레거시 모듈의 엔드포인트를 Query(조회)와 Command(명령)로 분류합니다.

## 사용법

```bash
/legacy-endpoints admin:brand
/legacy-endpoints web:product
```

## 인자

- `$ARGUMENTS[0]`: 분석 대상 (예: admin:brand, web:product)

## 실행

이 Skill은 **legacy-endpoints-analyzer** agent에게 작업을 위임합니다.
상세 워크플로우는 agent가 독립 컨텍스트에서 실행합니다.
```

### Phase 3: Commands 폴더 정리

**Options:**

| 방안 | 설명 | 장점 | 단점 |
|------|------|------|------|
| A. 삭제 | commands/ 폴더 완전 삭제 | 깔끔함 | 호환성 문제 가능 |
| B. 유지 | 기존 파일 그대로 유지 | 안전함 | 중복 유지 |
| C. 심볼릭 | Skills로 리다이렉트 | 호환성 유지 | 복잡함 |

**권장: 방안 B (당분간 유지)**
- Skills가 안정화될 때까지 Commands 유지
- 점진적으로 Skills 사용으로 전환
- 이후 Commands 삭제

---

## 5. 예상 효과

| 항목 | Before | After |
|------|--------|-------|
| Command/Skill 크기 | 200-480줄 | ~30줄 |
| Agent 작동 | ❌ (frontmatter 없음) | ✅ |
| 메인 컨텍스트 소비 | 전체 내용 + MCP 결과 | 요약만 (3-5K) |
| MCP 호출 위치 | 메인 컨텍스트 | 독립 컨텍스트 |
| 컨텍스트 폭발 위험 | ⚠️ 높음 | ✅ 낮음 |

---

## 6. 마이그레이션 워크플로우

리팩토링 후 전체 레거시 마이그레이션 사이클:

```bash
# 1. 엔드포인트 분석
/legacy-endpoints admin:brand
    → legacy-endpoints-analyzer agent (독립 컨텍스트)
    → claudedocs/legacy-endpoints/admin-brand.md 생성

# 2. API 흐름 분석
/legacy-flow admin:BrandController.fetchBrands
    → legacy-flow-analyzer agent (독립 컨텍스트)
    → claudedocs/legacy-flows/admin-brand-fetchBrands.md 생성

# 3. DTO 변환
/legacy-convert admin:BrandController.fetchBrands
    → legacy-dto-converter agent (독립 컨텍스트)
    → Request/Response DTO 생성

# 4. Persistence Layer 생성
/legacy-query admin:BrandController.fetchBrands
    → legacy-query-generator agent (독립 컨텍스트)
    → QueryDSL Repository 생성

# 5. Application Layer 생성
/legacy-service admin:BrandController.fetchBrands
    → legacy-service-generator agent (독립 컨텍스트)
    → Port, Service, Manager 생성

# 6. Adapter-In Layer 생성
/legacy-controller admin:BrandController.fetchBrands
    → legacy-controller-generator agent (독립 컨텍스트)
    → Controller, ApiMapper 생성
```

---

## 7. 체크리스트

### Phase 1: Agents frontmatter 추가
- [ ] `legacy-endpoints-analyzer.md` frontmatter 추가
- [ ] `legacy-flow-analyzer.md` frontmatter 추가
- [ ] `legacy-dto-converter.md` frontmatter 추가
- [ ] `legacy-query-generator.md` frontmatter 추가
- [ ] `legacy-service-generator.md` frontmatter 추가
- [ ] `legacy-controller-generator.md` frontmatter 추가
- [ ] 각 Agent 작동 테스트

### Phase 2: Skills 수정
- [ ] `legacy-endpoints/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] `legacy-flow/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] `legacy-convert/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] `legacy-query/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] `legacy-service/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] `legacy-controller/SKILL.md` 가볍게 수정 + `context: fork`
- [ ] 각 Skill → Agent 위임 테스트

### Phase 3: 정리
- [ ] Commands 폴더 결정 (삭제/유지)
- [ ] 중복 내용 제거
- [ ] 문서 업데이트

---

## 8. 참고 자료

### Anthropic 공식 문서
- [Extend Claude with skills](https://code.claude.com/docs/en/skills)
- [Create custom subagents](https://code.claude.com/docs/en/sub-agents)

### Serena Memory
- `claude-code-skills-agents-architecture`
- `legacy-files-refactoring-analysis`

---

## 9. 주의사항

1. **Agents frontmatter 필수**: 없으면 작동 안 함
2. **포어그라운드 실행**: MCP 도구 사용하려면 `run_in_background: false`
3. **tools 필드 정확히 명시**: 필요한 도구만 (Glob, Grep, Read, Write, Edit, Bash)
4. **model 선택**: sonnet (기본) 또는 haiku (빠른 작업)
5. **context: fork**: Skills에서 격리 실행 원하면 필수
