# setof-commerce - Claude Code Configuration

이 프로젝트는 **SPRING_BOOT 3.5.x + JAVA 21** 기반의 **hexagonal-multimodule** 프로젝트입니다.

---

## 🏗️ 아키텍처 개요

```text
│  DOMAIN          │  Domain Layer                    │
│  APPLICATION     │  Application Layer               │
│  ADAPTER_OUT     │  Adapter-Out Layer               │
│  ADAPTER_IN      │  Adapter-In Layer                │
│  BOOTSTRAP       │  Bootstrap Layer                 │
```

**아키텍처 원칙**: DIP, SRP, OCP, ISP, CQRS, DDD

---

## 🧰 MCP 도구 사용법

이 프로젝트의 코딩 컨벤션은 **Convention Hub DB**에서 관리됩니다.
코드 작성 시 반드시 MCP 도구를 사용하여 규칙을 조회하세요.

### 3-Phase 워크플로우

```text
┌─────────────────────────────────────────────────────────────┐
│  1️⃣ PLANNING PHASE                                          │
│     planning_context(layers=[...])                          │
│     → 레이어는 list_tech_stacks()로 먼저 조회                 │
├─────────────────────────────────────────────────────────────┤
│  2️⃣ EXECUTION PHASE                                         │
│     module_context(module_id=N, class_type="...")           │
│     → 템플릿 + 규칙 기반 코드 생성                            │
├─────────────────────────────────────────────────────────────┤
│  3️⃣ VALIDATION PHASE                                        │
│     validation_context(layers=[...])                        │
│     → Zero-Tolerance + Checklist 검증                       │
└─────────────────────────────────────────────────────────────┘
```

### 사용 예시

```python
# 0. 먼저 레이어 목록 조회 (하드코딩 금지!)
list_tech_stacks()
# → layers: ["DOMAIN", "APPLICATION", "ADAPTER_OUT", "ADAPTER_IN", "BOOTSTRAP"]

# 1. 개발 계획 수립
planning_context(layers=["DOMAIN", "APPLICATION"])

# 2. 코드 생성
module_context(module_id=1, class_type="AGGREGATE")

# 3. 코드 검증
validation_context(layers=["DOMAIN"])
```

---

## 🚨 Zero-Tolerance 규칙

> ⚠️ **중요**: 규칙은 DB에서 조회하세요.

```python
# Zero-Tolerance 규칙 조회 (레이어는 동적으로!)
validation_context(layers=["DOMAIN", "APPLICATION", "ADAPTER_OUT", "ADAPTER_IN", "BOOTSTRAP"])
```

### 주요 규칙 (요약)

> 상세 규칙은 MCP `validation_context()` 또는 `get_rule()` 로 조회

MCP를 통해 최신 규칙을 동적으로 조회하세요.
하드코딩된 규칙은 DB 변경 시 outdated 될 수 있습니다.

---

## 📚 MCP Tools 목록

| 분류 | Tool | 용도 |
|------|------|------|
| **워크플로우** | planning_context | 개발 계획 수립 |
|  | module_context | 코드 생성 (템플릿 + 규칙) |
|  | validation_context | 코드 검증 (Zero-Tolerance) |
| **컨텍스트** | get_context | 빠른 컨텍스트 조회 |
|  | get_rule | 규칙 상세 + 예시 |
| **계층** | list_tech_stacks | 기술 스택 + 레이어 목록 |
|  | get_architecture | 아키텍처 상세 |
|  | get_layer_detail | 레이어 상세 |

---

## 🔧 설계 원칙

MCP 서버는 **순수 정보 브릿지**로 설계되었습니다:

- MCP = 규칙/템플릿 전달 (Spring API → LLM)
- **LLM은 규칙을 반드시 준수**하며 코드 생성
- 규칙을 "판단"하지 않고 **100% 준수**

---

## ⚡ 빠른 시작

```python
# 1. 레이어 목록 조회
layers = list_tech_stacks()  # → ["DOMAIN", "APPLICATION", ...]

# 2. Aggregate 생성 시
planning_context(layers=["DOMAIN"])
module_context(module_id=1, class_type="AGGREGATE")
validation_context(layers=["DOMAIN"])

# 3. UseCase 생성 시
module_context(module_id=2, class_type="USE_CASE")
```
