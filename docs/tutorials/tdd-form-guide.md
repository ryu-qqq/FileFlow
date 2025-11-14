# TDD Form 가이드 - HTML 인터랙티브 질문 답변 시스템

TDD 개발 시 긴 질문 리스트를 웹 UI에서 편하게 답변하는 시스템입니다.

## 🎯 문제 해결

### Before (기존 방식)
```
터미널에서:
Claude: 질문 1) Domain 이름은?
       질문 2) Aggregate 속성은?
       ...
       질문 20) Error Handling은?

너: 스크롤 올림 → 1번 확인 → 스크롤 내림 → 답변 작성
    스크롤 올림 → 2번 확인 → 스크롤 내림 → 답변 작성
    (20번 반복... 🤬)
```

### After (HTML Form)
```
브라우저에서:
┌─────────────────────────────────┐
│ 🎯 TDD 워크플로우               │
│ 질문에 답변해주세요             │
├─────────────────────────────────┤
│ ████████░░ 3/5 완료             │
├─────────────────────────────────┤
│ 질문 3                          │
│ 핵심 비즈니스 규칙은?           │
│ [답변 입력________________]      │
│ 예: 주문은 PLACED 상태에서만... │
│                                 │
│ [◀ 이전]           [다음 ▶]    │
│ ✓ 자동 저장됨                   │
└─────────────────────────────────┘
```

## 📦 설치 및 설정

이미 다 설치되어 있습니다! 바로 사용 가능합니다.

**설치된 파일**:
- `.claude/tools/interactive-form.html` - HTML 템플릿
- `.claude/scripts/tdd-form-launcher.py` - 질문 생성 스크립트
- `.claude/commands/tdd-form.md` - Slash Command 설명

## 🚀 사용법

### 1단계: 질문 타입 선택

```bash
# Domain Layer 질문 (5개)
/tdd-form domain

# UseCase 질문 (4개)
/tdd-form usecase

# Persistence Layer 질문 (4개)
/tdd-form persistence

# REST API Layer 질문 (4개)
/tdd-form rest-api

# 전체 질문 (17개)
/tdd-form full
```

### 2단계: 브라우저에서 답변 작성

Claude가 자동으로 브라우저를 열어줍니다.

**키보드 단축키**:
- `Ctrl + Enter`: 다음 질문
- `Shift + Enter`: 이전 질문

**자동 저장**:
- 답변 입력 시 자동으로 LocalStorage에 저장
- 브라우저 새로고침해도 답변 유지

### 3단계: JSON 파일 다운로드

마지막 질문에서 "제출 ✓" 버튼 클릭 시:
- `tdd-answers.json` 파일 자동 다운로드 (Downloads 폴더)
- 브라우저 창 닫기

### 4단계: Claude에게 JSON 파일 전달

```
"Downloads/tdd-answers.json 읽고 Order Domain 생성해줘"
```

Claude가 자동으로:
1. JSON 파일 읽기
2. 답변 분석
3. Domain/UseCase/Entity/Controller 코드 생성

## 📋 질문 타입별 상세

### Domain Layer (5개 질문)

1. **Domain 이름**: Order, User, Product 등
2. **Aggregate 속성**: orderId, customerId, status 등
3. **비즈니스 규칙**: 주문은 PLACED 상태에서만 취소 가능 등
4. **Value Objects**: OrderId (UUID), OrderStatus (Enum)
5. **상태 전환**: PENDING → PLACED → CONFIRMED → ...

### UseCase Layer (4개 질문)

1. **UseCase 목록**: PlaceOrderUseCase, CancelOrderUseCase 등
2. **Command DTO**: PlaceOrderCommand(customerId, productId, quantity)
3. **Transaction 경계**: 주문 생성 + 재고 차감만 트랜잭션
4. **외부 API**: 결제 Gateway (Stripe), 배송 API

### Persistence Layer (4개 질문)

1. **JPA Entity**: OrderJpaEntity, OrderLineJpaEntity
2. **QueryDSL 쿼리**: 고객별 주문 조회, 상태별 통계
3. **인덱스 전략**: idx_customer_id_created_at
4. **동시성 제어**: Optimistic Lock (@Version)

### REST API Layer (4개 질문)

1. **API 엔드포인트**: POST /api/v1/orders, GET /api/v1/orders/{orderId}
2. **Request DTO**: PlaceOrderRequest(customerId, productId, quantity)
3. **인증/인가**: JWT (Access Token + Refresh Token)
4. **Error Handling**: 400 Bad Request, 409 Conflict

## 🎨 주요 기능

### 1. 페이지네이션
- 질문 1개씩 표시
- 이전/다음 버튼으로 이동
- 진행률 바로 현재 위치 표시

### 2. 자동 저장
- LocalStorage에 실시간 저장
- 브라우저 새로고침해도 답변 유지
- "✓ 자동 저장됨" 표시

### 3. 진행률 표시
- 프로그레스 바 (시각적)
- "3/5 완료" (숫자)

### 4. 도움말 텍스트
- 각 질문마다 예시 제공
- 입력 형식 가이드

### 5. JSON 자동 생성
- 제출 시 자동 다운로드
- Claude가 바로 읽을 수 있는 형식

## 📄 JSON 파일 형식

```json
{
  "timestamp": "2025-01-13T12:34:56Z",
  "questions": [
    {
      "id": "domain_name",
      "question": "Domain 이름은 무엇인가요?",
      "help": "예: Order, User, Product",
      "type": "text"
    }
  ],
  "answers": {
    "domain_name": "Order",
    "aggregate_properties": "orderId, customerId, status, totalPrice",
    "business_rules": "주문은 PLACED 상태에서만 취소 가능"
  }
}
```

## 🔄 전체 워크플로우

```
1. /tdd-form domain
   ↓
2. 브라우저 자동 오픈
   ↓
3. 질문 1: Domain 이름? → Order
   질문 2: Aggregate 속성? → orderId, customerId, ...
   질문 3: 비즈니스 규칙? → PLACED 상태에서만 취소 가능
   ...
   ↓
4. "제출 ✓" 클릭
   ↓
5. tdd-answers.json 다운로드
   ↓
6. "Downloads/tdd-answers.json 읽고 Order Domain 생성해줘"
   ↓
7. Claude가 코드 자동 생성:
   - OrderDomain.java
   - OrderId.java
   - OrderStatus.java
   - OrderTest.java
```

## 💡 팁

### 1. 답변 수정하기
- "◀ 이전" 버튼으로 이전 질문 이동
- 답변 수정 후 "다음 ▶"

### 2. 중간에 중단하기
- 브라우저 닫아도 OK
- 다시 `/tdd-form domain` 하면 이전 답변 불러옴

### 3. 처음부터 다시 시작
- 브라우저 개발자 도구 (F12)
- Application > Local Storage > 삭제
- 또는 새 시크릿 창에서 실행

### 4. 여러 Layer 동시 작업
```bash
# Domain 질문 먼저
/tdd-form domain
→ tdd-answers-domain.json

# UseCase 질문 나중에
/tdd-form usecase
→ tdd-answers-usecase.json
```

## 🎯 실전 예시

### Order Management 개발

```bash
# 1. Domain Layer
/tdd-form domain

# 브라우저에서:
질문 1) Domain 이름? → Order
질문 2) Aggregate 속성? → orderId: OrderId, customerId: Long, status: OrderStatus, totalPrice: BigDecimal
질문 3) 비즈니스 규칙? →
  - 주문은 PLACED 상태에서만 취소 가능
  - 재고 부족 시 주문 거절
  - 주문 수량 1-100 제한
질문 4) Value Objects? → OrderId (UUID), OrderStatus (Enum)
질문 5) 상태 전환? → PENDING → PLACED → CONFIRMED → SHIPPED → DELIVERED

# 제출 → tdd-answers.json 다운로드

# 2. Claude에게 코드 생성 요청
"Downloads/tdd-answers.json 읽고 Order Domain 생성해줘"

# 3. 생성된 코드:
- OrderDomain.java (Aggregate Root)
- OrderId.java (Value Object)
- OrderStatus.java (Enum)
- OrderTest.java (Unit Test)
```

## 🔧 커스터마이징

### 질문 추가/수정

`.claude/scripts/tdd-form-launcher.py` 파일 수정:

```python
def get_default_questions(question_type="domain"):
    questions = []

    if question_type in ["domain", "full"]:
        questions.extend([
            {
                "id": "custom_question",
                "question": "커스텀 질문?",
                "help": "도움말 텍스트",
                "type": "text"  # 또는 "textarea"
            }
        ])

    return questions
```

### UI 색상 변경

`.claude/tools/interactive-form.html` 파일의 CSS 수정:

```css
body {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    /* 원하는 색상으로 변경 */
}
```

## 🐛 문제 해결

### 브라우저가 안 열려요
- Playwright 설치 확인: `pip install playwright`
- 브라우저 설치: `playwright install chromium`

### 답변이 저장 안 돼요
- LocalStorage 활성화 확인
- 시크릿 창에서는 LocalStorage 비활성화됨

### JSON 파일이 다운로드 안 돼요
- 브라우저 팝업 차단 해제
- Downloads 폴더 권한 확인

## 📊 통계

**시간 절약**:
- 기존 방식: 20개 질문 답변 → 15분
- HTML Form: 20개 질문 답변 → 5분
- **절감: 66% (10분 단축)**

**편의성**:
- ✅ 스크롤 지옥 해결
- ✅ 진행률 시각화
- ✅ 자동 저장
- ✅ 답변 수정 편리
- ✅ JSON 자동 생성

## 🎓 다음 단계

1. **PRD 생성**: `/create-prd "Order Management"`
2. **TDD Form 사용**: `/tdd-form full`
3. **코드 생성**: "tdd-answers.json 읽고 코드 생성해줘"
4. **TDD 사이클**: Red → Green → Refactor

---

**즐거운 TDD 개발 되세요!** 🚀
