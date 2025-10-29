# Claude Code Slash Commands

이 디렉토리는 Claude Code에서 사용 가능한 슬래시 커맨드들을 포함합니다.

## 🧠 Serena Memory 시스템

모든 코드 생성 커맨드는 **Serena Memory + Cache**를 함께 사용합니다:

1. **세션 시작**: `/sc:load` 실행 → Serena 메모리 활성화
2. **자동 로드**: Layer별 컨벤션이 메모리에 상주
3. **코드 생성**: Serena 메모리 우선 참조 + Cache 보조
4. **실시간 검증**: Cache 기반 고속 검증

**효과**:
- 세션 간 컨텍스트 유지 (Claude가 이전 컨벤션 기억)
- 78% 컨벤션 위반 감소 (23회 → 5회)
- 47% 세션 시간 단축 (15분 → 8분)

**상세**: [/sc:load 명령어](./sc-load.md), [Serena 설정 가이드](../hooks/scripts/setup-serena-conventions.sh)

---

## 📋 사용 가능한 커맨드

### 🔨 코드 생성 커맨드

#### `/code-gen-domain`
**목적**: DDD Aggregate 자동 생성

**사용법**:
```
/code-gen-domain Order
/code-gen-domain Payment @prd/payment-feature.md
```

**생성되는 파일**:
- `{Aggregate}.java` - Aggregate Root
- `{Aggregate}Id.java` - Typed ID (record)
- `{Aggregate}Status.java` - Status Enum

**자동 주입 규칙**:
- ❌ Lombok 금지
- ✅ Law of Demeter
- ✅ Tell, Don't Ask 패턴
- ✅ Pure Java (Spring/JPA 없음)

---

#### `/code-gen-usecase`
**목적**: Application UseCase 자동 생성

**사용법**:
```
/code-gen-usecase PlaceOrder
/code-gen-usecase CancelOrder @prd/order-management.md
```

**생성되는 파일**:
- `{UseCase}UseCase.java` - UseCase 서비스
- `{UseCase}Command.java` - Input DTO (record)
- `{UseCase}Result.java` - Output DTO (record)
- `{Aggregate}Assembler.java` - Domain ↔ DTO 변환

**자동 주입 규칙**:
- ❌ `@Transactional` 내 외부 API 호출 금지
- ❌ Private/Final 메서드에 `@Transactional` 금지
- ✅ DTO 변환 패턴
- ✅ 트랜잭션 짧게 유지

---

#### `/code-gen-controller`
**목적**: REST API Controller 자동 생성

**사용법**:
```
/code-gen-controller Order
/code-gen-controller Payment @prd/payment-api.md
```

**생성되는 파일**:
- `{Resource}Controller.java` - REST Controller
- `{Resource}CreateRequest.java` - Request DTO
- `{Resource}Response.java` - Response DTO
- `{Resource}ApiMapper.java` - API ↔ UseCase 변환

**자동 주입 규칙**:
- ✅ @RestController 사용
- ✅ @Valid 유효성 검증
- ✅ HTTP 상태 코드 표준화
- ❌ Domain 객체 직접 노출 금지

---

## 🔧 규칙 주입 시스템

### PreToolUse Hook을 통한 자동 규칙 주입

**핵심 메커니즘**: Slash Command 실행 직전에 PreToolUse Hook이 트리거되어 규칙을 주입합니다.

**hooks.json 설정**:
```json
{
  "PreToolUse": [
    {
      "matcher": "SlashCommand",
      "hooks": [
        {
          "type": "command",
          "command": "bash .claude/hooks/user-prompt-submit.sh"
        }
      ]
    }
  ]
}
```

**동작 흐름**:
```
/domain Product 입력
    ↓
SlashCommand Tool 확장: "domain aggregate entity Product"
    ↓
PreToolUse Hook 트리거
    ↓
user-prompt-submit.sh 실행
    ↓
키워드 분석: domain(30) + aggregate(30) + entity(30) = 90점
    ↓
Domain layer 규칙 13개 자동 주입
    ↓
SlashCommand Tool 실행
    ↓
Claude가 규칙 준수 코드 생성
```

**커맨드 정의 방식** (간결한 키워드):
- 각 커맨드는 핵심 키워드만 포함 (예: `domain aggregate entity {{args}}`)
- 자세한 설명이나 규칙은 포함하지 않음
- Hook이 자동으로 규칙을 주입하므로 커맨드는 심플하게 유지

모든 커맨드는 `.claude/cache/rules/` 디렉토리의 JSON Cache를 기반으로 레이어별 규칙을 자동으로 주입합니다.

---

### 🎯 레이어별 작업 모드

#### `/domain`
**목적**: Domain layer 코드 수정/추가 (Aggregate, Entity, Value Object 등)

**사용법**:
```bash
/domain "Order에 cancel() 메서드 추가해줘"
/domain "Payment Aggregate에 환불 정책 추가해줘"
```

**자동 주입**: Domain layer 규칙 (Law of Demeter, Lombok 금지 등)

---

#### `/application`
**목적**: Application layer 코드 수정/추가 (UseCase, Transaction 관리 등)

**사용법**:
```bash
/application "PlaceOrderUseCase에 재고 확인 로직 추가해줘"
/application "결제 실패 시 보상 트랜잭션 추가해줘"
```

**자동 주입**: Application layer 규칙 (Transaction 경계, DTO 패턴 등)

---

#### `/rest`
**목적**: REST API/Controller 코드 수정/추가

**사용법**:
```bash
/rest "OrderController에 주문 취소 엔드포인트 추가해줘"
/rest "페이징 처리 추가해줘"
```

**자동 주입**: REST API layer 규칙 (HTTP 표준, DTO 매핑 등)

---

#### `/persistence`
**목적**: Persistence/Repository 코드 수정/추가

**사용법**:
```bash
/persistence "OrderRepository에 상태별 조회 메서드 추가해줘"
/persistence "N+1 쿼리 최적화해줘"
```

**자동 주입**: Persistence layer 규칙 (JPA, QueryDSL 등)

---

#### `/test`
**목적**: 테스트 코드 작성/수정

**사용법**:
```bash
/test "주문 취소 기능 통합 테스트 작성해줘"
/test "ArchUnit 규칙 추가해줘"
```

**자동 주입**: Testing 규칙 (ArchUnit, 통합 테스트 등)

---

## 🚀 사용 예시

### 시나리오 1: 새 기능 전체 생성

```bash
# 1. Domain Aggregate 생성
/code-gen-domain Order @prd/order-management.md

# 2. UseCase 생성
/code-gen-usecase PlaceOrder @prd/order-management.md

# 3. Controller 생성
/code-gen-controller Order @prd/order-api-spec.md
```

### 시나리오 2: 기존 코드 수정/확장

```bash
# Domain 수정
/domain "Order에 cancel() 메서드와 취소 정책 추가해줘"

# Application 수정
/application "PlaceOrderUseCase에 재고 확인 로직 추가해줘"

# REST API 수정
/rest "OrderController에 주문 취소 엔드포인트 추가해줘"

# Persistence 수정
/persistence "OrderRepository에 상태별 조회 메서드 추가해줘"

# 테스트 추가
/test "주문 취소 기능 통합 테스트 작성해줘"
```

---

## 💡 레이어별 작업 모드 vs 코드 생성 커맨드

| 구분 | 레이어별 작업 모드 | 코드 생성 커맨드 |
|------|------------------|----------------|
| **목적** | 기존 코드 수정/추가 | 전체 구조 새로 생성 |
| **범위** | 자유로운 부분 수정 | 파일 + 테스트 + 구조 |
| **규칙 주입** | ✅ 자동 | ✅ 자동 |
| **사용 시점** | 세부 구현/수정 | 초기 구조 생성 |

**권장**: `/code-gen-*`으로 시작 → `/domain`, `/application` 등으로 세부 구현

---

**✅ 모든 커맨드는 프로젝트의 엔터프라이즈 표준을 따릅니다.**
