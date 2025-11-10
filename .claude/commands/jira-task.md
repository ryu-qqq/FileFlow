---
description: Jira 태스크 분석 + PRD TDD Plan 생성 + 브랜치 생성
tags: [project]
---

# Jira Task Analysis with PRD TDD Plan

당신은 Jira 이슈를 분석하고, Layer별 TDD 계획을 생성하며, 브랜치를 자동으로 설정하는 작업을 수행합니다.

## 목적

Jira 티켓을 분석하여:
1. Layer 정보 추출 (labels 기반)
2. TDD 계획 생성 (docs/prd/{ISSUE-KEY}-tdd-plan.md)
3. 브랜치 자동 체크아웃
4. TodoList 생성

## 입력 형식

사용자는 다음 형식 중 하나로 정보를 제공합니다:
- Jira URL: `https://ryuqqq.atlassian.net/browse/{ISSUE-KEY}`
- 이슈 키만: `{PROJECT}-{NUMBER}` (예: KAN-6)

## 실행 단계

### 1. Cloud ID 확인

먼저 Atlassian Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 2. Jira 이슈 상세 정보 조회

URL 또는 이슈 키에서 추출한 정보로 이슈를 조회합니다:
```
mcp__atlassian__getJiraIssue 도구 사용:
- cloudId: Cloud ID
- issueIdOrKey: 이슈 키
- fields: ["summary", "description", "status", "issuetype", "parent", "labels", "customfield_*"]
```

### 3. Epic 정보 조회 (해당되는 경우)

이슈가 Epic의 하위 태스크인 경우, Epic 정보도 조회합니다:
```
parent 필드에 Epic이 있다면:
- mcp__atlassian__getJiraIssue로 Epic 정보도 조회
```

### 4. Layer 정보 추출

Jira labels에서 Layer 정보를 추출합니다:

**Layer 태그**:
- `domain`: Domain Layer
- `application`: Application Layer
- `persistence`: Persistence Layer
- `adapter-rest`: REST API Layer

**추가 태그**:
- `prd-based`: PRD에서 생성됨
- `tdd`: kentback TDD 적용
- `zero-tolerance`: Zero-Tolerance 규칙 적용

### 5. TDD Plan 생성

Layer 정보를 기반으로 TDD 계획을 생성합니다.

**파일 경로**: `docs/prd/{ISSUE-KEY}-tdd-plan.md`

**TDD Plan 구조**:
```markdown
# TDD Plan: {ISSUE-KEY}

**Jira 이슈**: {ISSUE-KEY} - {summary}
**Epic**: {epic_summary} (있는 경우)
**Layer**: {layer} (domain/application/persistence/adapter-rest)
**상태**: {status}
**브랜치**: {branch_name}

---

## 📋 작업 개요

{description 요약}

---

## 🎯 Layer별 TDD 전략

### {Layer} Layer TDD 계획

#### 🔴 RED Phase: 실패하는 테스트 작성

**목표**: {Layer}의 Zero-Tolerance 규칙을 검증하는 테스트 작성

{Layer별 RED 템플릿}

#### 🟢 GREEN Phase: 최소 구현으로 테스트 통과

**목표**: 테스트를 통과시키는 최소한의 코드 작성

{Layer별 GREEN 템플릿}

#### 🔄 REFACTOR Phase: 코드 개선

**목표**: 코드 품질 향상 (성능, 가독성, 유지보수성)

{Layer별 REFACTOR 템플릿}

---

## ✅ Zero-Tolerance 체크리스트

{Layer별 Zero-Tolerance 규칙 체크리스트}

---

## 📊 예상 작업 시간

- RED Phase: {예상 시간}
- GREEN Phase: {예상 시간}
- REFACTOR Phase: {예상 시간}
- **총 예상 시간**: {총 시간}

---

## 🚀 실행 순서

1. /kb:red - RED Phase 실행
2. /kb:green - GREEN Phase 실행
3. /kb:refactor - REFACTOR Phase 실행
4. /kb:go - 전체 사이클 자동 실행

---

**생성 일시**: {YYYY-MM-DD HH:mm:ss}
```

#### 5.1 Domain Layer TDD Plan 템플릿

```markdown
### Domain Layer TDD 계획

#### 🔴 RED Phase: 실패하는 테스트 작성

**목표**: Domain의 Zero-Tolerance 규칙을 검증하는 테스트 작성

**테스트 케이스**:
1. **Law of Demeter 테스트**
   ```java
   @Test
   void shouldNotUseLawOfDemeterViolation() {
       // Getter 체이닝이 없는지 확인
       // order.getCustomer().getAddress() ❌
       // order.getCustomerAddress() ✅
   }
   ```

2. **Lombok 금지 테스트**
   ```java
   @Test
   void shouldNotUseLombokAnnotations() {
       // Lombok 어노테이션이 없는지 확인
       // @Data, @Builder 등 ❌
   }
   ```

3. **Long FK 전략 테스트**
   ```java
   @Test
   void shouldUseLongFkStrategy() {
       // JPA 관계 어노테이션이 없는지 확인
       // @ManyToOne, @OneToMany 등 ❌
       // private Long customerId; ✅
   }
   ```

**참고 템플릿**:
- `.claude/kentback/templates/domain-layer/law-of-demeter.md`
- `.claude/kentback/templates/domain-layer/lombok-prohibition.md`

#### 🟢 GREEN Phase: 최소 구현으로 테스트 통과

**목표**: 테스트를 통과시키는 최소한의 Domain 코드 작성

**구현 내용**:
1. **Domain Aggregate 생성**
   ```java
   public class OrderDomain {
       private Long orderId;
       private Long customerId;  // Long FK
       private OrderStatus status;

       // Pure Java getter/setter (Lombok 금지)
       public Long getOrderId() {
           return orderId;
       }

       // Tell, Don't Ask (Law of Demeter)
       public String getCustomerAddress() {
           // Getter 체이닝 금지
           return /* 주소 조회 로직 */;
       }
   }
   ```

2. **비즈니스 로직 구현**
   - Aggregate의 핵심 메서드 구현
   - Invariant 검증 로직 추가

#### 🔄 REFACTOR Phase: 코드 개선

**목표**: 코드 품질 향상

**리팩토링 항목**:
1. **Java 21 Record 패턴 적용** (선택)
   ```java
   public record OrderId(Long value) {
       public OrderId {
           if (value == null || value <= 0) {
               throw new IllegalArgumentException();
           }
       }
   }
   ```

2. **Tell, Don't Ask 원칙 강화**
   - Getter 최소화
   - 비즈니스 메서드 중심 설계

3. **Value Object 추출**
   - 원시 타입 포장 (Primitive Obsession 제거)

---

## ✅ Zero-Tolerance 체크리스트

- [ ] Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] Lombok 미사용 (Pure Java/Record)
- [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] Tell, Don't Ask 원칙
- [ ] Invariant 보호
```

#### 5.2 Application Layer TDD Plan 템플릿

```markdown
### Application Layer TDD 계획

#### 🔴 RED Phase: 실패하는 테스트 작성

**목표**: Application의 Zero-Tolerance 규칙을 검증하는 테스트 작성

**테스트 케이스**:
1. **Transaction 경계 테스트**
   ```java
   @Test
   void shouldNotCallExternalApiInsideTransaction() {
       // @Transactional 내 외부 API 호출 금지
       // RestTemplate, WebClient 호출 ❌
   }
   ```

2. **Command/Query 분리 테스트**
   ```java
   @Test
   void shouldSeparateCommandAndQuery() {
       // Command는 void 또는 ID 반환
       // Query는 데이터 반환, 상태 변경 없음
   }
   ```

**참고 템플릿**:
- `.claude/kentback/templates/application-layer/transaction-boundary.md`
- `.claude/kentback/templates/application-layer/command-query-separation.md`

#### 🟢 GREEN Phase: 최소 구현으로 테스트 통과

**목표**: 테스트를 통과시키는 최소한의 UseCase 코드 작성

**구현 내용**:
1. **UseCase 구현**
   ```java
   @UseCase
   @Transactional
   public class PlaceOrderUseCase implements PlaceOrderCommand {
       @Override
       public Long execute(PlaceOrderRequest request) {
           // 1. Domain 로직 (트랜잭션 내)
           OrderDomain order = orderDomain.create(request);

           // 2. 저장 (트랜잭션 내)
           orderRepository.save(order);

           return order.getOrderId();
       }

       // 외부 API 호출은 별도 메서드 (트랜잭션 밖)
       public void notifyExternalSystem(Long orderId) {
           // RestTemplate 호출 (트랜잭션 밖)
       }
   }
   ```

2. **Command/Query DTO 구현**
   - Command: PlaceOrderRequest
   - Response: OrderResponse

#### 🔄 REFACTOR Phase: 코드 개선

**목표**: 코드 품질 향상

**리팩토링 항목**:
1. **Facade 패턴 적용** (선택)
   - 여러 UseCase 조율

2. **Assembler 패턴 적용**
   - Domain ↔ DTO 변환 로직 분리

3. **Component 패턴 적용**
   - 횡단 관심사 처리

---

## ✅ Zero-Tolerance 체크리스트

- [ ] Command/Query 분리 (CQRS)
- [ ] Transaction 경계 엄격 관리 (외부 API 호출 금지)
- [ ] Spring 프록시 제약사항 준수 (private/final 금지)
- [ ] UseCase 단위 테스트
```

#### 5.3 Persistence Layer TDD Plan 템플릿

```markdown
### Persistence Layer TDD 계획

#### 🔴 RED Phase: 실패하는 테스트 작성

**목표**: Persistence의 Zero-Tolerance 규칙을 검증하는 테스트 작성

**테스트 케이스**:
1. **Long FK 전략 테스트**
   ```java
   @Test
   void shouldUseLongFkStrategy() {
       // JPA 관계 어노테이션 금지
       // @ManyToOne, @OneToMany 등 ❌
       // private Long customerId; ✅
   }
   ```

2. **QueryDSL 최적화 테스트**
   ```java
   @Test
   void shouldAvoidNPlusOneQuery() {
       // N+1 쿼리 방지 (QueryDSL fetch join)
   }
   ```

**참고 템플릿**:
- `.claude/kentback/templates/persistence-layer/long-fk-strategy.md`

#### 🟢 GREEN Phase: 최소 구현으로 테스트 통과

**목표**: 테스트를 통과시키는 최소한의 Repository 코드 작성

**구현 내용**:
1. **JPA Entity 구현**
   ```java
   @Entity
   @Table(name = "orders")
   public class OrderEntity {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long orderId;

       // Long FK (관계 어노테이션 금지)
       private Long customerId;

       private OrderStatus status;
   }
   ```

2. **Repository 구현**
   ```java
   public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
       List<OrderEntity> findByCustomerId(Long customerId);
   }
   ```

3. **QueryDSL 구현**
   ```java
   public List<OrderEntity> findOrdersWithCustomer(Long customerId) {
       return queryFactory
           .selectFrom(orderEntity)
           .where(orderEntity.customerId.eq(customerId))
           .fetch();
   }
   ```

#### 🔄 REFACTOR Phase: 코드 개선

**목표**: 코드 품질 향상

**리팩토링 항목**:
1. **인덱스 최적화**
   - 자주 조회되는 필드에 인덱스 추가

2. **QueryDSL fetch join 최적화**
   - N+1 쿼리 제거

3. **Batch Insert/Update**
   - 성능 개선

---

## ✅ Zero-Tolerance 체크리스트

- [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] QueryDSL 최적화 (N+1 방지)
- [ ] 인덱스 전략
```

#### 5.4 REST API Layer TDD Plan 템플릿

```markdown
### REST API Layer TDD 계획

#### 🔴 RED Phase: 실패하는 테스트 작성

**목표**: REST API의 Zero-Tolerance 규칙을 검증하는 테스트 작성

**테스트 케이스**:
1. **RESTful 설계 테스트**
   ```java
   @Test
   void shouldFollowRestfulDesign() {
       // POST /api/v1/orders → 201 Created
       // GET /api/v1/orders/{id} → 200 OK
   }
   ```

2. **Error Response 형식 테스트**
   ```java
   @Test
   void shouldReturnConsistentErrorResponse() {
       // 일관된 Error Response 형식
       // { "code": "ERR_001", "message": "...", "timestamp": "..." }
   }
   ```

**참고 템플릿**:
- `.claude/kentback/templates/adapter-rest-layer/restful-design.md`

#### 🟢 GREEN Phase: 최소 구현으로 테스트 통과

**목표**: 테스트를 통과시키는 최소한의 Controller 코드 작성

**구현 내용**:
1. **Controller 구현**
   ```java
   @RestController
   @RequestMapping("/api/v1/orders")
   public class OrderController {
       @PostMapping
       public ResponseEntity<OrderResponse> createOrder(
           @RequestBody @Valid OrderCreateRequest request
       ) {
           Long orderId = placeOrderUseCase.execute(request);
           return ResponseEntity.status(HttpStatus.CREATED)
               .body(new OrderResponse(orderId));
       }
   }
   ```

2. **Request/Response DTO 구현**
   - OrderCreateRequest
   - OrderResponse

3. **Exception Handling 구현**
   ```java
   @RestControllerAdvice
   public class GlobalExceptionHandler {
       @ExceptionHandler(BusinessException.class)
       public ResponseEntity<ErrorResponse> handleBusinessException(
           BusinessException e
       ) {
           // 일관된 Error Response
       }
   }
   ```

#### 🔄 REFACTOR Phase: 코드 개선

**목표**: 코드 품질 향상

**리팩토링 항목**:
1. **OpenAPI/Swagger 문서화**
   - API 명세 자동 생성

2. **Validation 강화**
   - Bean Validation (javax.validation)

3. **HATEOAS 적용** (선택)
   - Hypermedia As The Engine Of Application State

---

## ✅ Zero-Tolerance 체크리스트

- [ ] RESTful 설계 원칙
- [ ] 일관된 Error Response 형식
- [ ] HTTP 상태 코드 올바른 사용
- [ ] Javadoc 및 OpenAPI 문서
```

### 6. Git 브랜치 처리

브랜치 정보가 있는 경우:
```bash
git fetch origin
if git rev-parse --verify --quiet "origin/{branch-name}"; then
  git checkout {branch-name}
  git pull origin {branch-name}
else
  git checkout -b {branch-name}
fi
```

브랜치 정보가 없는 경우, 이슈 키 기반으로 제안:
```bash
# 제안: feature/{ISSUE-KEY}-{layer}-{요약-kebab-case}
# Layer 태그 포함하여 브랜치명 생성
git checkout -b feature/{ISSUE-KEY}-{layer}-{요약-kebab-case}
```

### 7. TodoList 생성

TodoWrite 도구를 사용하여 구조화된 작업 목록 생성:

**Todo 항목 구조**:
1. 브랜치 체크아웃 (completed 상태로 시작)
2. /kb:red - RED Phase 실행
3. /kb:green - GREEN Phase 실행
4. /kb:refactor - REFACTOR Phase 실행
5. Zero-Tolerance 규칙 검증 (`/validate-{layer}`)
6. 코드 리뷰 준비
7. PR 생성 (마지막 단계)

### 8. 출력 형식

```markdown
## Jira 태스크 분석: {ISSUE-KEY}

**제목**: {summary}
**Epic**: {epic_summary} (있는 경우)
**Layer**: {layer}
**현재 상태**: {status}
**브랜치**: {branch_name}

### 작업 설명
{description 요약}

### TDD Plan 생성 완료
파일: `docs/prd/{ISSUE-KEY}-tdd-plan.md`

### TodoList 생성 완료
{TodoWrite 도구로 생성된 항목 개수}개 작업 항목이 생성되었습니다.

### 다음 단계
1. `docs/prd/{ISSUE-KEY}-tdd-plan.md` 검토
2. `/kb:red` - RED Phase 시작
3. `/kb:green` - GREEN Phase 실행
4. `/kb:refactor` - REFACTOR Phase 실행
5. `/validate-{layer}` - Zero-Tolerance 검증
```

## MCP 도구 사용 순서

1. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
2. `mcp__atlassian__getJiraIssue` → 이슈 상세 정보
3. `mcp__atlassian__getJiraIssue` (선택) → Epic 정보
4. `Write` → TDD Plan 생성 (docs/prd/{ISSUE-KEY}-tdd-plan.md)
5. `Bash` → git 브랜치 체크아웃
6. `TodoWrite` → 구조화된 작업 목록 생성

## 에러 처리

- **Cloud ID 없음**: URL에서 사이트명 추출하여 사용
- **이슈 없음**: 이슈 키 확인 요청
- **Layer 태그 없음**: 사용자에게 Layer 확인 요청 (또는 자동 추론)
- **브랜치 충돌**: 사용자에게 브랜치 전략 확인

## Layer 자동 추론 (태그 없는 경우)

Jira labels에 Layer 태그가 없는 경우, 이슈 설명에서 키워드로 자동 추론:

| Keywords | Layer |
|----------|-------|
| aggregate, entity, domain model, business rule | domain |
| usecase, command, query, transaction | application |
| jpa, entity, repository, querydsl | persistence |
| controller, rest, api, endpoint | adapter-rest |

## 사용 예시

```bash
/jira-task KAN-6
/jira-task https://ryuqqq.atlassian.net/browse/KAN-6
```

## 기존 `/jira-analyze`와의 차이

| 항목 | `/jira-analyze` | `/jira-task` |
|------|-----------------|--------------|
| Jira 이슈 조회 | ✅ | ✅ |
| Layer 정보 추출 | ❌ | ✅ (labels 기반) |
| TDD Plan 생성 | ❌ | ✅ (Layer별 템플릿, docs/prd/) |
| Zero-Tolerance 체크리스트 | ❌ | ✅ (자동 생성) |
| TDD 사이클 가이드 | ❌ | ✅ (RED/GREEN/REFACTOR) |
| 브랜치 자동 생성 | ✅ | ✅ (Layer 포함) |
| TodoList 생성 | ✅ | ✅ (TDD 단계 포함) |

**권장**: `/jira-task`를 기본으로 사용하고, `/jira-analyze`는 Deprecated로 표시
