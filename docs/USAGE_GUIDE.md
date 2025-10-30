# Claude Code + Cascade 통합 사용 가이드

이 가이드는 **Spring Standards 프로젝트**에서 Claude Code와 IntelliJ Cascade를 함께 사용하는 방법을 단계별로 설명합니다.

---

## 📋 목차

1. [시작하기 전에](#시작하기-전에)
2. [초기 설정](#초기-설정)
3. [일반적인 개발 워크플로우](#일반적인-개발-워크플로우)
4. [실제 사용 예시](#실제-사용-예시)
5. [도구별 역할 이해하기](#도구별-역할-이해하기)
6. [트러블슈팅](#트러블슈팅)

---

## 시작하기 전에

### 필요한 도구

1. **Claude Code** (CLI)
   - 설치: [Claude Code 공식 가이드](https://docs.claude.com/)
   - 역할: 설계, 분석, 비즈니스 로직 구현

2. **IntelliJ IDEA** + **Windsurf/Cascade Plugin**
   - 설치: IntelliJ IDEA → Settings → Plugins → "Windsurf" 검색
   - 역할: Boilerplate 생성, 자동 검증, 테스트 실행

3. **Java 21** + **Gradle 8.x**
   ```bash
   java -version  # Java 21 확인
   gradle -v      # Gradle 8.x 확인
   ```

### 프로젝트 구조 이해

```
claude-spring-standards/
├── .claude/                    # Claude Code 설정
│   ├── commands/               # Slash Commands
│   │   ├── cc/load.md         # /cc:load - 코딩 컨벤션 로드
│   │   ├── code-gen-domain.md # /code-gen-domain
│   │   └── ...
│   ├── hooks/                  # Dynamic Hooks
│   │   ├── user-prompt-submit.sh  # 키워드 감지 → 규칙 주입
│   │   └── after-tool-use.sh      # 코드 생성 후 즉시 검증
│   └── cache/rules/            # 90개 규칙 JSON (고속 검색)
│
├── .windsurf/                  # Cascade 설정
│   ├── rules.md               # Cascade 자동 로드 규칙 (7,000자)
│   └── workflows/             # 12개 Markdown Workflows
│       ├── validate-conventions.md  # /validate-conventions
│       ├── run-unit-tests.md        # /run-unit-tests
│       └── pipeline-pr.md           # /pipeline-pr
│
├── tools/pipeline/             # SSOT (실제 작업 스크립트)
│   ├── common.sh              # 공통 헬퍼
│   ├── validate_conventions.sh # 컨벤션 검증
│   ├── test_unit.sh           # 유닛 테스트
│   └── pr_gate.sh             # PR 파이프라인
│
├── docs/coding_convention/     # 90개 코딩 컨벤션 문서
│   ├── 01-adapter-rest-api-layer/
│   ├── 02-domain-layer/
│   ├── 03-application-layer/
│   └── 04-persistence-layer/
│
└── domain/                     # 실제 프로젝트 코드
    ├── src/main/java/com/ryuqq/domain/
    └── ...
```

---

## 초기 설정

### 1. 프로젝트 클론 및 설정

```bash
# 1. 프로젝트 클론
git clone https://github.com/your-org/claude-spring-standards.git
cd claude-spring-standards

# 2. Gradle 빌드 (초기 의존성 다운로드)
./gradlew clean build

# 3. Serena 메모리 준비 (1회만 실행)
bash .claude/hooks/scripts/setup-serena-conventions.sh
```

**출력 예시**:
```
🚀 Serena Conventions Setup
✅ Python 3 확인 완료
📋 코딩 컨벤션 메모리 생성 시작...
📝 생성할 메모리:
   - coding_convention_domain_layer
   - coding_convention_application_layer
   - coding_convention_persistence_layer
   - coding_convention_rest_api_layer
   - coding_convention_index
✅ 메모리 생성 준비 완료
```

### 2. IntelliJ에서 Cascade 활성화

1. **IntelliJ IDEA 실행**
2. **Settings** → **Plugins** → "Windsurf" 검색 및 설치
3. **프로젝트 열기**: `claude-spring-standards` 디렉토리
4. **Cascade 확인**: `.windsurf/workflows/` 디렉토리가 인식되는지 확인

### 3. Claude Code 세션 시작

```bash
# Claude Code CLI 실행
claude code

# 코딩 컨벤션 로드 (세션 시작 시 필수!)
/cc:load
```

**출력 예시**:
```
✅ Project activated: claude-spring-standards
✅ Memory loaded: coding_convention_index
✅ Memory loaded: coding_convention_domain_layer

📋 Available conventions:
   - coding_convention_domain_layer (Domain Layer 규칙)
   - coding_convention_application_layer (Application Layer 규칙)
   - coding_convention_persistence_layer (Persistence Layer 규칙)
   - coding_convention_rest_api_layer (REST API Layer 규칙)

🎯 Zero-Tolerance Rules:
   1. Lombok 금지 (Domain)
   2. Law of Demeter (Domain)
   3. Long FK Strategy (Persistence)
   4. Transaction Boundary (Application)
   5. Spring 프록시 제약사항 (Application)
   6. Javadoc 필수 (All Layers)

🚀 Ready for development!
```

---

## 일반적인 개발 워크플로우

### 워크플로우 개요

```
┌─────────────────────────────────────────────────────────────┐
│ 1️⃣ Claude Code: 분석 & 설계                                   │
│    - PRD 작성, Jira Task 분석, Technical Spec               │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2️⃣ Claude Code: 코딩 컨벤션 로드                              │
│    - /cc:load (Serena Memory 활성화)                        │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3️⃣ Claude Code: 비즈니스 로직 구현                            │
│    - /code-gen-domain, /code-gen-usecase                    │
│    - Dynamic Hooks 자동 규칙 주입                            │
│    - After-tool-use 자동 검증                                │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 4️⃣ IntelliJ Cascade: 자동 검증 & 테스트                       │
│    - /validate-conventions (컨벤션 검증)                     │
│    - /run-unit-tests (유닛 테스트)                           │
│    - /validate-architecture (ArchUnit)                      │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 5️⃣ IntelliJ Cascade: PR 파이프라인                            │
│    - /pipeline-pr (통합 검증)                                │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 6️⃣ Claude Code: PR 생성 & 머지                                │
│    - "PR을 생성해줘" → gh pr create                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 실제 사용 예시

### 예시 1: Order Aggregate 개발 (전체 프로세스)

#### Step 1: Claude Code에서 PRD 작성

**터미널 (Claude Code)**:
```bash
claude code
```

**Claude Code 대화**:
```
사용자: "Order Aggregate PRD를 작성해줘.
        주문 생성, 취소, 상태 변경이 필요해."

Claude: PRD 문서를 작성하겠습니다.

[문서 생성]
✅ Created: docs/prd/order-aggregate.md

📋 PRD 요약:
- 기능: 주문 생성, 취소, 상태 변경
- Aggregate: OrderDomain
- Value Objects: OrderId, OrderStatus, OrderContent
- 비즈니스 규칙:
  1. PENDING 상태에서만 취소 가능
  2. 금액은 항상 양수
  3. 취소 시 환불 프로세스 필요
```

#### Step 2: Jira Task 분석 및 브랜치 생성

**Claude Code 대화**:
```
사용자: /jira-task

Claude: Jira Task를 분석하겠습니다.

[Jira API 조회]
✅ Jira Task: PROJ-123 "주문 관리 기능 구현"
✅ Branch created: feature/PROJ-123-order

📋 Task 정보:
- Type: Feature
- Priority: High
- Acceptance Criteria:
  1. 주문 생성 API
  2. 주문 취소 API
  3. 주문 상태 조회 API
```

#### Step 3: 코딩 컨벤션 로드 (필수!)

**Claude Code 대화**:
```
사용자: /cc:load

Claude: 코딩 컨벤션을 로드하겠습니다.

[Serena Memory 로드]
✅ Project activated: claude-spring-standards
✅ Memory loaded: coding_convention_index
✅ Memory loaded: coding_convention_domain_layer

🎯 Zero-Tolerance Rules (Domain):
   - Lombok 금지
   - Law of Demeter (Getter 체이닝 금지)
   - Tell, Don't Ask 패턴
   - Pure Java (프레임워크 의존성 없음)

🚀 Ready for Domain code generation!
```

#### Step 4: Domain Aggregate 생성

**Claude Code 대화**:
```
사용자: /code-gen-domain Order

Claude: Order Aggregate를 생성하겠습니다.

[자동 실행 흐름]
1. user-prompt-submit.sh 트리거
   - 키워드 감지: "domain", "aggregate"
   - Layer 매핑: domain

2. inject-rules.py 실행
   - .claude/cache/rules/index.json 조회
   - domain layer 규칙 5개 주입:
     * Lombok 금지
     * Law of Demeter
     * Tell, Don't Ask
     * Pure Java
     * Javadoc 필수

3. Serena Memory 참조
   - coding_convention_domain_layer 우선 로드

4. 코드 생성
   ✅ Created: domain/src/main/java/com/ryuqq/domain/order/OrderDomain.java
   ✅ Created: domain/src/main/java/com/ryuqq/domain/order/OrderId.java
   ✅ Created: domain/src/main/java/com/ryuqq/domain/order/OrderStatus.java
   ✅ Created: domain/src/main/java/com/ryuqq/domain/order/OrderContent.java
   ✅ Created: domain/src/main/java/com/ryuqq/domain/order/exception/OrderException.java

5. after-tool-use.sh 자동 실행
   - Write/Edit 도구 사용 감지
   - validation-helper.py 호출

6. Cache 기반 실시간 검증 (148ms)
   ✅ Lombok 검사: Pass
   ✅ Law of Demeter 검사: Pass
   ✅ Getter 체이닝 검사: Pass
   ✅ Pure Java 검사: Pass
   ✅ Javadoc 검사: Pass

✅ All Zero-Tolerance rules passed!
```

**생성된 코드 (OrderDomain.java)**:
```java
package com.ryuqq.domain.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order Aggregate Root
 *
 * <p>주문의 생명주기를 관리하는 Aggregate Root입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * @author Claude
 * @since 2025-10-29
 */
public class OrderDomain {

    // 1. 불변 필드 (final)
    private final OrderId id;
    private final String customerId;

    // 2. 가변 필드
    private OrderStatus status;
    private BigDecimal totalAmount;

    // 3. 내부 컬렉션 (private)
    private final List<OrderLineItem> lineItems;

    /**
     * Order를 생성합니다.
     *
     * @param id Order ID
     * @param customerId 고객 ID
     * @author Claude
     * @since 2025-10-29
     */
    public OrderDomain(OrderId id, String customerId) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID는 필수입니다");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID는 필수입니다");
        }

        this.id = id;
        this.customerId = customerId;
        this.lineItems = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
    }

    /**
     * 주문을 확정합니다.
     *
     * <p>Law of Demeter 준수: 내부 로직을 캡슐화</p>
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     * @author Claude
     * @since 2025-10-29
     */
    public void confirmOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 확정 가능합니다");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * 주문을 취소합니다.
     *
     * <p>Law of Demeter 준수: 내부 로직을 캡슐화</p>
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     * @author Claude
     * @since 2025-10-29
     */
    public void cancelOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 취소 가능합니다");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 주문이 확정되었는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태를 묻는 메서드</p>
     * <p>❌ Bad: order.getStatus().equals(CONFIRMED)</p>
     * <p>✅ Good: order.isConfirmed()</p>
     *
     * @return 확정 여부
     * @author Claude
     * @since 2025-10-29
     */
    public boolean isConfirmed() {
        return this.status == OrderStatus.CONFIRMED;
    }

    /**
     * 주문이 취소되었는지 확인합니다.
     *
     * @return 취소 여부
     * @author Claude
     * @since 2025-10-29
     */
    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }

    /**
     * Order ID를 반환합니다.
     *
     * @return Order ID
     * @author Claude
     * @since 2025-10-29
     */
    public OrderId getId() {
        return id;
    }

    /**
     * Customer ID를 반환합니다.
     *
     * @return Customer ID
     * @author Claude
     * @since 2025-10-29
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * 총 금액을 반환합니다.
     *
     * @return 총 금액
     * @author Claude
     * @since 2025-10-29
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * 주문 항목 목록을 반환합니다 (불변).
     *
     * <p>Law of Demeter: 내부 컬렉션의 직접 수정 방지</p>
     *
     * @return 읽기 전용 컬렉션
     * @author Claude
     * @since 2025-10-29
     */
    public List<OrderLineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
}
```

#### Step 5: IntelliJ Cascade에서 컨벤션 검증

**IntelliJ IDE**:
1. **Cascade 활성화**: `Cmd+K` (Mac) 또는 `Ctrl+K` (Windows)
2. **명령어 입력**: `/validate-conventions`

**Cascade 실행 흐름**:
```
IntelliJ Cascade
    ↓
.windsurf/workflows/validate-conventions.md 읽기
    ↓
Markdown 파싱
    ↓
실제 스크립트 실행: bash ./tools/pipeline/validate_conventions.sh
    ↓
validate_conventions.sh:
  1. Lombok 검사 (grep)
  2. Law of Demeter 검사 (regex)
  3. JPA 관계 어노테이션 검사
  4. Setter 검사
  5. Transaction 경계 검사
  6. Spring 프록시 검사
    ↓
메트릭 기록: .cascade/metrics.jsonl
    ↓
결과 출력
```

**IntelliJ IDE 출력**:
```
✅ Convention Validation PASSED!
Duration: 5s

All Zero-Tolerance rules are followed:
  ✅ No Lombok annotations
  ✅ No Law of Demeter violations
  ✅ No JPA relationship annotations
  ✅ No public setters in Domain/Entity
  ✅ Transaction boundaries respected
  ✅ Spring proxy compatible

📊 Metrics saved to .cascade/metrics.jsonl
```

#### Step 6: UseCase 구현

**Claude Code 대화**:
```
사용자: /code-gen-usecase PlaceOrder

Claude: PlaceOrder UseCase를 생성하겠습니다.

[자동 실행 흐름]
1. application layer 규칙 로드
   - Serena Memory: coding_convention_application_layer
   - Cache: application-layer-*.json 규칙 5개

2. 코드 생성
   ✅ Created: application/src/main/java/com/ryuqq/application/order/service/PlaceOrderUseCase.java
   ✅ Created: application/src/main/java/com/ryuqq/application/order/port/in/PlaceOrderPort.java
   ✅ Created: application/src/main/java/com/ryuqq/application/order/dto/command/PlaceOrderCommand.java
   ✅ Created: application/src/main/java/com/ryuqq/application/order/dto/response/OrderResponse.java

3. Transaction 경계 자동 검증
   ✅ executeInTransaction(): DB 작업만
   ✅ callExternalApi(): 트랜잭션 밖 외부 호출
   ✅ Spring Proxy: public 메서드 (프록시 가능)
```

**생성된 코드 (PlaceOrderUseCase.java)**:
```java
package com.ryuqq.application.order.service;

import com.ryuqq.application.order.dto.command.PlaceOrderCommand;
import com.ryuqq.application.order.dto.response.OrderResponse;
import com.ryuqq.application.order.port.in.PlaceOrderPort;
import com.ryuqq.domain.order.OrderDomain;
import com.ryuqq.domain.order.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 주문 생성 UseCase
 *
 * <p><strong>Transaction 경계 관리:</strong></p>
 * <ul>
 *   <li>✅ executeInTransaction(): 트랜잭션 내 로직</li>
 *   <li>✅ callExternalApi(): 트랜잭션 밖 외부 호출</li>
 *   <li>❌ @Transactional 내 외부 API 호출 금지</li>
 * </ul>
 *
 * <p><strong>Spring 프록시 제약사항 준수:</strong></p>
 * <ul>
 *   <li>✅ public 메서드 - 프록시 가능</li>
 *   <li>❌ private/final 메서드 - 프록시 불가</li>
 *   <li>❌ 같은 클래스 내부 호출 - 프록시 우회</li>
 * </ul>
 *
 * @author Claude
 * @since 2025-10-29
 */
@Service
public class PlaceOrderUseCase implements PlaceOrderPort {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public PlaceOrderUseCase(OrderRepository orderRepository,
                             PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    /**
     * 주문을 생성합니다.
     *
     * <p>Transaction 경계 준수:</p>
     * <ol>
     *   <li>외부 API 호출 (트랜잭션 밖)</li>
     *   <li>DB 작업 (트랜잭션 내)</li>
     * </ol>
     *
     * @param command 주문 생성 명령
     * @return 주문 응답
     * @author Claude
     * @since 2025-10-29
     */
    @Override
    public OrderResponse execute(PlaceOrderCommand command) {
        // 1. 외부 API 호출 (트랜잭션 밖)
        PaymentResult paymentResult = callExternalApi(command);

        // 2. 트랜잭션 내 로직 (DB 작업만)
        OrderDomain order = executeInTransaction(command, paymentResult);

        return OrderResponse.from(order);
    }

    /**
     * 트랜잭션 내 로직 (DB 작업만)
     *
     * <p>Transaction 경계 준수:</p>
     * <ul>
     *   <li>✅ @Transactional 내 DB 작업만</li>
     *   <li>❌ 외부 API 호출 없음</li>
     * </ul>
     *
     * @param command 주문 생성 명령
     * @param paymentResult 결제 결과
     * @return 생성된 주문
     * @author Claude
     * @since 2025-10-29
     */
    @Transactional
    public OrderDomain executeInTransaction(PlaceOrderCommand command,
                                            PaymentResult paymentResult) {
        OrderDomain order = new OrderDomain(
            new OrderId(UUID.randomUUID().toString()),
            command.customerId()
        );

        order.confirmOrder();  // Tell, Don't Ask

        return orderRepository.save(order);
    }

    /**
     * 외부 API 호출 (트랜잭션 밖)
     *
     * <p>Transaction 경계 준수:</p>
     * <ul>
     *   <li>✅ @Transactional 없음</li>
     *   <li>✅ 외부 API 호출 가능</li>
     * </ul>
     *
     * @param command 주문 생성 명령
     * @return 결제 결과
     * @author Claude
     * @since 2025-10-29
     */
    private PaymentResult callExternalApi(PlaceOrderCommand command) {
        return paymentClient.processPayment(command.amount());
    }
}
```

#### Step 7: IntelliJ Cascade에서 유닛 테스트 실행

**IntelliJ IDE**:
```
Cascade: /run-unit-tests --impacted
```

**Cascade 실행 흐름**:
```
bash ./tools/pipeline/test_unit.sh --impacted

test_unit.sh:
  1. 변경된 파일 감지 (git diff)
  2. 관련 테스트만 실행 (Fast Lane)
  3. ./gradlew test --tests "*Order*"
  4. 메트릭 기록
```

**IntelliJ IDE 출력**:
```
✅ Unit Tests PASSED (Fast Lane)
Duration: 15s

Tests run: 23
  Passed: 23
  Failed: 0
  Skipped: 0

Coverage: 89% (line), 85% (branch)

📊 Metrics saved to .cascade/metrics.jsonl
```

#### Step 8: IntelliJ Cascade에서 PR 파이프라인 실행

**IntelliJ IDE**:
```
Cascade: /pipeline-pr
```

**Cascade 실행 흐름**:
```
bash ./tools/pipeline/pr_gate.sh

pr_gate.sh:
  1. Code Formatting (Spotless)
  2. Convention Validation
  3. Unit Tests
  4. Architecture Validation (ArchUnit)
  5. Coverage Check (JaCoCo 80%)
  6. 메트릭 기록
```

**IntelliJ IDE 출력**:
```
✅ PR Gate Pipeline PASSED!
Duration: 45s

✅ Code Formatting (5s)
✅ Convention Validation (5s)
✅ Unit Tests (15s)
✅ Architecture Validation (8s)
✅ Coverage Check (12s) - 89% > 80%

🎉 Ready to merge!

📊 Metrics saved to .cascade/metrics.jsonl
```

#### Step 9: Claude Code에서 PR 생성

**Claude Code 대화**:
```
사용자: "PR을 생성해줘"

Claude: GitHub PR을 생성하겠습니다.

[자동 실행 흐름]
1. Git commit
   git add .
   git commit -m "feat(order): Order Aggregate 구현 (#PROJ-123)"

2. Git push
   git push -u origin feature/PROJ-123-order

3. GitHub PR 생성
   gh pr create \
     --title "feat(order): Order Aggregate 구현" \
     --body "..."

✅ Commit: feat(order): Order Aggregate 구현 (#PROJ-123)
✅ Push: feature/PROJ-123-order
✅ PR Created: #42 "feat(order): Order Aggregate 구현"
🔗 https://github.com/user/repo/pull/42

🎉 Ready for code review!
```

---

### 예시 2: UseCase만 빠르게 구현

**시나리오**: Domain은 이미 있고, UseCase만 추가하는 경우

**Claude Code**:
```
# 1. 세션 시작 및 컨벤션 로드
claude code
/cc:load

# 2. UseCase 생성
사용자: /code-gen-usecase CancelOrder

Claude:
✅ Created: CancelOrderUseCase.java
✅ Created: CancelOrderCommand.java
✅ Created: CancelOrderPort.java
✅ Validation: All rules passed (148ms)
```

**IntelliJ Cascade**:
```
# 3. 검증 및 테스트
Cascade: /validate-conventions
Cascade: /run-unit-tests --impacted

✅ All checks passed!
```

---

### 예시 3: 기존 코드 검증만 수행

**시나리오**: 수동으로 코드를 작성했거나, 다른 사람의 코드를 검증하는 경우

**Claude Code**:
```
사용자: /validate-domain domain/src/main/java/com/ryuqq/domain/payment/PaymentDomain.java

Claude: Payment Domain을 검증하겠습니다.

[validation-helper.py 실행]
❌ Validation FAILED!

Violations found:
  ❌ Lombok annotation detected: @Getter (line 15)
  ❌ Law of Demeter violation: getCustomer().getAddress().getZip() (line 45)

📋 수정 방법:
  1. @Getter 제거 → public String getId() { return id; } 직접 작성
  2. getCustomer().getAddress().getZip()
     → order.getCustomerZipCode() 메서드 추가

참고: docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md
```

---

## 도구별 역할 이해하기

### Claude Code의 역할

| 작업 | Slash Command | 설명 |
|------|---------------|------|
| **세션 시작** | `/cc:load` | Serena 메모리에서 코딩 컨벤션 로드 (필수) |
| **Domain 생성** | `/code-gen-domain <name>` | Aggregate Root + Value Objects + Exception |
| **UseCase 생성** | `/code-gen-usecase <name>` | UseCase + Command + Response + Port |
| **Controller 생성** | `/code-gen-controller <name>` | REST Controller + DTO + Mapper |
| **검증** | `/validate-domain <file>` | Domain layer 파일 검증 |
| **아키텍처 검증** | `/validate-architecture` | 전체 아키텍처 ArchUnit 테스트 |
| **Jira 연동** | `/jira-task` | Jira Task 분석 및 브랜치 생성 |

**Claude Code 특징**:
- ✅ **컨텍스트 유지**: Serena Memory로 세션 간 컨텍스트 유지
- ✅ **복잡한 로직**: 비즈니스 로직, Transaction 경계, 상태 관리
- ✅ **자동 규칙 주입**: Dynamic Hooks로 키워드 감지 시 자동 규칙 주입
- ✅ **즉시 검증**: After-tool-use Hook으로 코드 생성 직후 검증

### Cascade의 역할

| 작업 | Cascade Command | 설명 |
|------|-----------------|------|
| **컨벤션 검증** | `/validate-conventions` | Zero-Tolerance 규칙 자동 검증 |
| **유닛 테스트** | `/run-unit-tests` | Fast/Full Lane 선택 가능 |
| **통합 테스트** | `/run-integration-tests` | Testcontainers 기반 |
| **E2E 테스트** | `/run-e2e-tests` | RestAssured 기반 |
| **아키텍처 검증** | `/validate-architecture` | ArchUnit 테스트 |
| **PR 파이프라인** | `/pipeline-pr` | Format → Conventions → Tests → Architecture → Coverage |
| **코드 포맷팅** | `/format-code` | Spotless (Google Java Format) |
| **커버리지 검증** | `/validate-tests` | JaCoCo 80% 이상 |

**Cascade 특징**:
- ✅ **빠른 자동화**: Markdown Workflow → Bash Script 실행
- ✅ **IDE 통합**: IntelliJ에서 바로 실행, 결과 확인
- ✅ **메트릭 수집**: 모든 실행 결과 `.cascade/metrics.jsonl`에 기록
- ✅ **SSOT 패턴**: `tools/pipeline/*.sh`를 Cascade와 CI가 공유

### tools/pipeline/ 스크립트 (SSOT)

| 스크립트 | 역할 | Cascade | CI/CD |
|---------|------|---------|-------|
| `validate_conventions.sh` | Zero-Tolerance 규칙 검증 | ✅ | ✅ |
| `test_unit.sh` | 유닛 테스트 (Fast/Full Lane) | ✅ | ✅ |
| `pr_gate.sh` | PR 파이프라인 (통합 검증) | ✅ | ✅ |
| `common.sh` | 공통 헬퍼 (로깅, 메트릭) | ✅ | ✅ |

**SSOT (Single Source of Truth) 패턴**:
```
실제 로직: tools/pipeline/*.sh
    ↓
Cascade: .windsurf/workflows/*.md (얇은 래퍼)
    ↓
CI/CD: .github/workflows/*.yml (동일한 스크립트 재사용)
```

**장점**:
- ✅ **No Drift**: Cascade와 CI가 동일한 로직 실행
- ✅ **중복 제거**: 로직 한 곳에만 존재
- ✅ **유지보수**: 스크립트 한 번만 수정

---

## 트러블슈팅

### 문제 1: `/cc:load` 실행 시 에러

**증상**:
```
❌ Error: Memory not found: coding_convention_domain_layer
```

**원인**: Serena 메모리가 생성되지 않음

**해결**:
```bash
# 1. Serena 메모리 생성 스크립트 실행
bash .claude/hooks/scripts/setup-serena-conventions.sh

# 2. Claude Code 재시작
claude code
/cc:load
```

### 문제 2: Cascade 워크플로우가 인식되지 않음

**증상**: IntelliJ에서 `/validate-conventions` 입력 시 아무 반응 없음

**원인**: Windsurf 플러그인이 설치되지 않았거나 `.windsurf/` 디렉토리 권한 문제

**해결**:
```bash
# 1. IntelliJ Windsurf 플러그인 설치 확인
# Settings → Plugins → "Windsurf" 검색

# 2. 디렉토리 권한 확인
chmod -R 755 .windsurf/

# 3. IntelliJ 재시작
```

### 문제 3: 검증이 실패하는데 원인을 모르겠어요

**증상**:
```
❌ Convention Validation FAILED
```

**해결**:
```bash
# 1. 상세 리포트 확인
cat .cascade/report.md

# 2. 특정 규칙 확인
grep -r "@Data\|@Builder" domain/src/  # Lombok 검사
grep -r "\.get[A-Z].*()\.get[A-Z].*()\.get" domain/src/  # Law of Demeter

# 3. 관련 문서 읽기
cat docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md
```

### 문제 4: Cache 시스템이 작동하지 않음

**증상**: 규칙이 자동 주입되지 않음

**원인**: Cache가 빌드되지 않았거나 손상됨

**해결**:
```bash
# 1. Cache 재빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 2. Cache 확인
ls -la .claude/cache/rules/
cat .claude/cache/rules/index.json

# 3. Claude Code 재시작
```

### 문제 5: Git Pre-commit Hook이 너무 느려요

**증상**: 커밋 시 5초 이상 소요

**원인**: 전체 프로젝트 스캔

**해결**:
```bash
# 1. Fast Lane 모드 활성화 (변경된 파일만 검사)
export COMMIT_FAST_LANE=1
git commit -m "..."

# 2. 일시적으로 Hook 비활성화 (권장하지 않음)
git commit --no-verify -m "..."
```

---

## 더 알아보기

### 문서
- [튜토리얼 (5분)](./tutorials/01-getting-started.md)
- [Dynamic Hooks 가이드](./DYNAMIC_HOOKS_GUIDE.md)
- [LangFuse 통합 가이드](./LANGFUSE_INTEGRATION_GUIDE.md)
- [Windsurf 가이드](../.windsurf/README.md)

### 코딩 컨벤션
- [Domain Layer 규칙](./coding_convention/02-domain-layer/)
- [Application Layer 규칙](./coding_convention/03-application-layer/)
- [Persistence Layer 규칙](./coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](./coding_convention/01-adapter-rest-api-layer/)

### Slash Commands
- [Commands README](./.claude/commands/README.md)

---

**생성일**: 2025-10-29
**버전**: 1.0.0
**작성자**: Claude Code + Human
