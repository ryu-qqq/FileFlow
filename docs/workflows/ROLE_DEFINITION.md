# Claude Code vs Cursor IDE - 역할 정의

## 🎯 핵심 원칙

**Cursor IDE**: 빠른 보일러플레이트 생성 (Template-driven)
**Claude Code**: 고품질 검증 및 테스트 (Quality-driven)

---

## 📊 역할 분담표

| 작업 | Cursor IDE | Claude Code | 이유 |
|------|-----------|-------------|------|
| **Boilerplate 생성** | ✅ 주도 | ❌ 보조 | Cursor IDE가 템플릿 기반으로 빠름 |
| **비즈니스 로직** | ⚠️ 초안 | ✅ 주도 | Claude가 컨텍스트 이해 우수 |
| **단위 테스트** | ❌ 없음 | ✅ 전담 | 고품질 테스트 작성 능력 |
| **통합 테스트** | ❌ 없음 | ✅ 전담 | 복잡한 시나리오 이해 |
| **컨벤션 검증** | ✅ 자동화 | ✅ 수정 | Cursor IDE 검증 → Claude 수정 |
| **코드 리뷰** | ❌ 없음 | ✅ 전담 | PR 리뷰 및 개선 제안 |
| **리팩토링** | ❌ 없음 | ✅ 전담 | 아키텍처 이해 필요 |
| **문서화** | ⚠️ 초안 | ✅ 주도 | 상세한 Javadoc 작성 |

---

## 🚀 권장 워크플로우

### 1. 새로운 기능 개발 (Feature Development)

```
1️⃣ Cursor IDE: Boilerplate 생성
   /cc:domain Order
   → OrderDomain.java, OrderId.java, OrderStatus.java

2️⃣ Claude Code: 비즈니스 로직 구현
   "Order Domain에 다음 비즈니스 규칙 구현:
   - placeOrder(): 주문 생성 (재고 확인 필수)
   - cancelOrder(): PLACED 상태만 취소 가능
   - confirmOrder(): 결제 완료 후 확인"

3️⃣ Claude Code: 단위 테스트 자동 생성 ⭐
   /test-gen-domain Order
   → OrderDomainTest.java (15개 테스트 케이스)

4️⃣ Cursor IDE: 컨벤션 검증
   /validate-conventions
   → 실패 시 TODO 주석 자동 추가

5️⃣ Claude Code: TODO 수정 및 테스트 보강
   "Fix all TODOs and add edge case tests"

6️⃣ Cursor IDE: 최종 검증 파이프라인
   /pipeline-pr
```

### 2. 기존 코드 개선 (Code Improvement)

```
1️⃣ Claude Code: 코드 리뷰
   /code-review domain/Order.java
   → 개선 제안 5가지 (Law of Demeter 위반, 테스트 누락 등)

2️⃣ Claude Code: 리팩토링
   "Refactor Order.java to follow Law of Demeter"

3️⃣ Claude Code: 테스트 보강
   /test-enhance Order
   → 누락된 엣지 케이스 추가

4️⃣ Cursor IDE: 검증
   /validate-conventions
```

### 3. 버그 수정 (Bug Fix)

```
1️⃣ Claude Code: 근본 원인 분석
   /root-cause "Order cancel fails with NPE"
   → Sequential thinking으로 원인 파악

2️⃣ Claude Code: 수정 및 회귀 테스트
   "Fix the NPE and add regression tests"

3️⃣ Cursor IDE: 검증
   /run-unit-tests
```

---

## 💡 Claude Code의 핵심 강점

### 1. 테스트 자동 생성 (New Role)

**기존 문제**:
- Cursor IDE는 템플릿 기반이라 복잡한 테스트 시나리오 작성 어려움
- 개발자가 수동으로 테스트 작성 → 시간 소모

**Claude 해결**:
- 비즈니스 로직 이해 → 자동 테스트 생성
- 엣지 케이스, 경계값, 예외 처리 자동 커버
- **3가지 테스트 유형 지원**:
  1. **단위 테스트**: 순수 로직 (Domain, Application)
  2. **통합 테스트**: DB, API 연동 (Persistence, REST)
  3. **아키텍처 테스트**: ArchUnit 규칙

### 2. 코드 리뷰 및 개선 (New Role)

**기존 문제**:
- PR 리뷰 시간 소모
- 컨벤션 위반 수동 확인

**Claude 해결**:
- `/ai-review` 명령어로 AI 리뷰 자동화
- 컨벤션 위반, 성능 이슈, 보안 취약점 자동 탐지
- **구체적인 개선 코드 제시**

### 3. 복잡한 비즈니스 로직 구현 (Existing Role)

**Claude 강점**:
- Serena Memory로 프로젝트 컨텍스트 유지
- DDD 패턴 이해 (Aggregate, Value Object, Domain Event)
- Law of Demeter, SOLID 자동 준수

---

## ❌ 삭제 대상 Commands

### 1. Code Generation 명령어 (Cursor IDE로 이관)

삭제 파일:
- `/code-gen-domain` → Cursor IDE `/cc:domain`으로 대체
- `/code-gen-usecase` → Cursor IDE `/cc:application`으로 대체
- `/code-gen-controller` → Cursor IDE `/cc:rest`으로 대체

**이유**:
- Cursor IDE가 템플릿 기반으로 더 빠름
- Serena Memory로 컨벤션 자동 주입 가능
- Claude는 복잡한 로직에 집중

### 2. 컨벤션 주입 명령어 (Serena로 통합)

삭제 파일:
- `/domain` → `/cc:load`로 통합
- `/application` → `/cc:load`로 통합
- `/persistence` → `/cc:load`로 통합
- `/rest` → `/cc:load`로 통합

**이유**:
- Serena Memory가 컨벤션 자동 로드
- 중복 제거

---

## ✅ 유지/추가 대상 Commands

### 유지 (검증 & 자동화)

1. `/validate-domain` - Domain layer 검증
2. `/validate-architecture` - ArchUnit 검증
3. `/jira-task` - Jira 태스크 분석
4. `/ai-review` - AI 리뷰
5. `/cc:load` - 컨벤션 로드

### 신규 추가 (테스트 자동화) ⭐

1. **`/test-gen-domain <name>`** - Domain 단위 테스트 자동 생성
   - 예시: `/test-gen-domain Order` → OrderDomainTest.java
   - 생성 내용:
     - Happy path (성공 케이스)
     - Edge cases (경계값)
     - Exception cases (예외 처리)
     - Invariant validation (불변식 검증)

2. **`/test-gen-usecase <name>`** - UseCase 단위 테스트 자동 생성
   - 예시: `/test-gen-usecase PlaceOrder` → PlaceOrderUseCaseTest.java
   - 생성 내용:
     - Transaction 경계 검증
     - Port 인터페이스 Mock
     - Exception handling
     - Command validation

3. **`/test-gen-integration <layer>`** - 통합 테스트 자동 생성
   - 예시: `/test-gen-integration persistence` → OrderRepositoryTest.java
   - 생성 내용:
     - Testcontainers 설정
     - DB CRUD 검증
     - N+1 문제 확인
     - Transaction rollback

4. **`/test-enhance <file>`** - 기존 테스트 보강
   - 예시: `/test-enhance OrderDomainTest.java`
   - 분석 내용:
     - 커버리지 분석 (누락된 케이스)
     - 엣지 케이스 추가
     - Assertion 강화

5. **`/code-review <file>`** - 코드 리뷰 및 개선 제안
   - 예시: `/code-review domain/Order.java`
   - 리뷰 항목:
     - 컨벤션 위반 (Lombok, Law of Demeter 등)
     - 성능 이슈 (N+1, 불필요한 조회)
     - 보안 취약점
     - 테스트 누락
     - 개선 코드 제시

6. **`/root-cause <issue>`** - 버그 근본 원인 분석
   - 예시: `/root-cause "Order cancel fails with NPE"`
   - Sequential thinking으로 분석:
     - 로그 분석
     - 코드 흐름 추적
     - 가설 검증
     - 수정 방안 제시

---

## 🎯 최종 권장 사항

### Cursor IDE 역할
- ✅ Boilerplate 생성 (cc-domain, cc-application, cc-rest 등)
- ✅ 컨벤션 자동 검증 (/validate-conventions)
- ✅ 테스트 실행 (/run-unit-tests)
- ✅ PR 게이트 파이프라인 (/pipeline-pr)

### Claude Code 역할
- ✅ 비즈니스 로직 구현 (복잡한 도메인 규칙)
- ✅ **테스트 자동 생성** (단위/통합/아키텍처) ⭐ NEW
- ✅ **코드 리뷰 및 개선** (/code-review, /ai-review) ⭐ NEW
- ✅ **버그 근본 원인 분석** (/root-cause) ⭐ NEW
- ✅ 리팩토링 (Law of Demeter, SOLID 준수)
- ✅ 상세 Javadoc 작성

---

**핵심**: Cursor IDE는 "빠른 생성", Claude는 "고품질 검증 및 테스트"
