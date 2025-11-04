# Facade 사용 가이드

## 📋 목차
1. [Facade란?](#facade란)
2. [Facade 위치](#facade-위치)
3. [사용 기준 (Decision Tree)](#사용-기준-decision-tree)
4. [필요한 경우](#필요한-경우)
5. [불필요한 경우](#불필요한-경우)
6. [실전 예시](#실전-예시)
7. [체크리스트](#체크리스트)

---

## Facade란?

**Facade Pattern**은 여러 복잡한 하위 시스템(UseCase)을 **단순한 인터페이스로 통합**하여 제공하는 디자인 패턴입니다.

### 헥사고날 아키텍처에서의 위치
```
adapter-in/rest-api (Controller)
    ↓
application/facade (Facade) ← 여기!
    ↓
application/port/in (UseCase Interface)
    ↓
application/service (UseCase Implementation)
    ↓
domain
```

### 핵심 목적
- ✅ **Controller 의존성 감소**: 여러 UseCase → 하나의 Facade
- ✅ **트랜잭션 조율**: 여러 UseCase를 하나의 트랜잭션으로
- ✅ **논리적 그룹화**: 관련된 기능을 하나의 진입점으로

---

## 🎯 Rule of 3 (Facade 사용 기준)

**Facade는 다음 3가지 조건 중 하나라도 만족하면 사용합니다:**

### 1️⃣ UseCase 3개 이상 호출 필요
- Controller가 3개 이상의 UseCase에 의존하는 경우
- 생성자 의존성이 많아져 복잡도 증가
- **예시**: Create, Update, Delete UseCase를 하나의 Facade로 통합

### 2️⃣ Transaction + 외부 호출 분리 필요
- 트랜잭션 내에서는 DB 작업만 수행
- 외부 API, 메시징, 파일 업로드 등은 트랜잭션 밖에서
- Facade가 트랜잭션 경계를 명확히 분리
- **예시**: 주문 생성(트랜잭션) → 이메일 발송(외부 호출)

### 3️⃣ Controller 의존성 감소 필요
- Controller가 여러 UseCase에 직접 의존하면 결합도 증가
- Facade로 간접 의존하여 결합도 감소
- 테스트 시 Mocking 간소화
- **예시**: 5개 UseCase → 1개 Facade

### ✅ 사용 예시

```java
// ✅ 조건 1: 3개 이상 UseCase → Facade 필수
@Service
public class OrderFacade {
    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    // ... 더 많은 UseCase
}

// ✅ 조건 2: Transaction + 외부 호출 분리 → Facade 권장
@Service
public class OrderFacade {
    private final CreateOrderUseCase createOrderUseCase;
    private final SendEmailPort sendEmailPort;

    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        // ✅ 트랜잭션 내: DB 작업만
        OrderResponse response = createOrderUseCase.execute(command);

        // ✅ 트랜잭션 밖: 외부 호출 (@Transactional 메서드 종료 후 실행)
        sendEmailConfirmation(response.orderId());

        return response;
    }

    // 외부 호출은 별도 메서드로 (트랜잭션 밖)
    private void sendEmailConfirmation(Long orderId) {
        sendEmailPort.send(orderId);
    }
}
```

### ❌ 불필요한 경우

```java
// ❌ UseCase 1-2개만 + 단순 위임 → Facade 불필요
@Service
public class UserFacade {
    private final GetUserUseCase getUserUseCase;  // 1개만

    public UserResponse getUser(GetUserQuery query) {
        return getUserUseCase.execute(query);  // 단순 위임만
    }
}
```

---

## Facade 위치

### 패키지 구조
```
application/
├── {feature}/
│   └── {domain}/
│       ├── facade/
│       │   ├── {Domain}CommandFacade.java  ← Command 작업 통합
│       │   └── {Domain}QueryFacade.java     ← Query 작업 통합
│       ├── port/in/
│       │   ├── {Feature}{Domain}UseCase.java
│       │   └── ...
│       └── service/
│           ├── {Feature}{Domain}Service.java
│           └── ...
```

### 예시
```
application/iam/tenant/
├── facade/
│   ├── TenantCommandFacade.java  ← Create, Update, Delete 통합
│   └── TenantQueryFacade.java     ← Get, Search 통합
├── port/in/
│   ├── CreateTenantUseCase.java
│   ├── UpdateTenantUseCase.java
│   ├── GetTenantUseCase.java
│   └── SearchTenantsUseCase.java
└── service/
    ├── CreateTenantService.java
    └── ...
```

---

## 사용 기준 (Decision Tree)

```
Controller에서 호출할 UseCase가 있음
    ↓
┌───────────────────────────────────────┐
│ Rule of 3 중 하나라도 해당하는가?    │
│ 1. UseCase 3개 이상 호출?            │
│ 2. Transaction + 외부 호출 분리?     │
│ 3. Controller 의존성 감소 필요?      │
└───────────────────────────────────────┘
    ├─ Yes → ✅ Facade 사용
    │         (Rule of 3 조건 만족)
    │
    └─ No → UseCase 1-2개만 + 단순 위임
              ↓
        ┌──────────────────────────────────────┐
        │ 추가 로직이 필요한가?                │
        │ - 트랜잭션 조율                      │
        │ - 데이터 변환/통합                   │
        │ - 여러 UseCase 순차 호출             │
        └──────────────────────────────────────┘
              ├─ Yes → ✅ Facade 권장
              │         (조율 로직 중앙화)
              │
              └─ No → ❌ UseCase 직접 호출
                        (Facade 불필요, YAGNI 원칙)
```

---

## 필요한 경우

### 1. 여러 UseCase 그룹화 (가장 흔한 케이스) ✅

**문제**: Controller가 여러 UseCase에 의존하면 생성자가 복잡해짐

**해결**: Facade로 통합

```java
// ❌ 나쁨 - Controller가 3개 UseCase에 직접 의존
@RestController
public class TenantController {
    private final CreateTenantUseCase createTenantUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final UpdateTenantStatusUseCase updateTenantStatusUseCase;

    public TenantController(
        CreateTenantUseCase createTenantUseCase,
        UpdateTenantUseCase updateTenantUseCase,
        UpdateTenantStatusUseCase updateTenantStatusUseCase  // 생성자가 길어짐
    ) {
        this.createTenantUseCase = createTenantUseCase;
        this.updateTenantUseCase = updateTenantUseCase;
        this.updateTenantStatusUseCase = updateTenantStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<?> create(...) {
        return createTenantUseCase.execute(command);
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<?> update(...) {
        return updateTenantUseCase.execute(command);
    }

    @PutMapping("/{tenantId}/status")
    public ResponseEntity<?> updateStatus(...) {
        return updateTenantStatusUseCase.execute(command);
    }
}

// ✅ 좋음 - Facade로 통합
@RestController
public class TenantController {
    private final TenantCommandFacade tenantCommandFacade;  // 1개 Facade만 의존

    public TenantController(TenantCommandFacade tenantCommandFacade) {
        this.tenantCommandFacade = tenantCommandFacade;
    }

    @PostMapping
    public ResponseEntity<?> create(...) {
        return tenantCommandFacade.createTenant(command);
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<?> update(...) {
        return tenantCommandFacade.updateTenant(command);
    }

    @PutMapping("/{tenantId}/status")
    public ResponseEntity<?> updateStatus(...) {
        return tenantCommandFacade.updateTenantStatus(command);
    }
}

// Facade 구현
@Service
public class TenantCommandFacade {
    private final CreateTenantUseCase createTenantUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final UpdateTenantStatusUseCase updateTenantStatusUseCase;

    public TenantCommandFacade(
        CreateTenantUseCase createTenantUseCase,
        UpdateTenantUseCase updateTenantUseCase,
        UpdateTenantStatusUseCase updateTenantStatusUseCase
    ) {
        this.createTenantUseCase = createTenantUseCase;
        this.updateTenantUseCase = updateTenantUseCase;
        this.updateTenantStatusUseCase = updateTenantStatusUseCase;
    }

    public TenantResponse createTenant(CreateTenantCommand command) {
        return createTenantUseCase.execute(command);
    }

    public TenantResponse updateTenant(UpdateTenantCommand command) {
        return updateTenantUseCase.execute(command);
    }

    public TenantResponse updateTenantStatus(UpdateTenantStatusCommand command) {
        return updateTenantStatusUseCase.execute(command);
    }
}
```

**이점**:
- Controller 생성자 간결화: 3개 의존성 → 1개 의존성
- 관련 기능 논리적 그룹화
- 테스트 시 Mocking 간소화

---

### 2. 트랜잭션 조율 (여러 UseCase를 하나의 트랜잭션으로) ✅

**문제**: 여러 UseCase를 하나의 트랜잭션으로 묶어야 함

**해결**: Facade에서 `@Transactional` 적용

```java
// ✅ Facade에서 트랜잭션 조율
@Service
public class OrderFacade {
    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateInventoryUseCase updateInventoryUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;

    @Transactional  // ← 여러 UseCase를 하나의 트랜잭션으로
    public OrderResponse createOrder(CreateOrderCommand command) {
        // 1. Order 생성
        OrderResponse order = createOrderUseCase.execute(command);

        // 2. 재고 차감
        updateInventoryUseCase.execute(order.orderId());

        // 3. 알림 발송 (비동기)
        sendNotificationUseCase.execute(order.orderId());

        return order;
    }
}
```

**이점**:
- 여러 UseCase를 원자적으로 실행
- 트랜잭션 경계 명확화
- 롤백 시 일관성 보장

---

### 3. 데이터 통합 및 변환 ✅

**문제**: 여러 UseCase 결과를 통합/변환해야 함

**해결**: Facade에서 데이터 통합 로직 구현

```java
// ✅ Facade에서 데이터 통합
@Service
public class ReportFacade {
    private final GetSalesDataUseCase getSalesDataUseCase;
    private final GetInventoryDataUseCase getInventoryDataUseCase;

    public MonthlyReportResponse generateMonthlyReport(Month month) {
        // 1. 여러 UseCase 호출
        SalesData sales = getSalesDataUseCase.execute(month);
        InventoryData inventory = getInventoryDataUseCase.execute(month);

        // 2. 데이터 통합 및 계산
        return MonthlyReportResponse.builder()
            .totalSales(sales.total())
            .totalInventory(inventory.total())
            .profitMargin(calculateProfitMargin(sales, inventory))  // 통합 로직
            .trends(calculateTrends(sales))
            .build();
    }

    private double calculateProfitMargin(SalesData sales, InventoryData inventory) {
        // 복잡한 계산 로직
        return (sales.revenue() - inventory.cost()) / sales.revenue() * 100;
    }
}
```

**이점**:
- 복잡한 데이터 통합 로직을 Facade에 캡슐화
- Controller는 단순하게 유지
- 재사용 가능한 비즈니스 로직

---

## 불필요한 경우

### 1. 단일 UseCase만 호출 (단순 위임) ❌

**문제**: Facade가 단순 위임만 하면 불필요한 레이어

```java
// ❌ 나쁨 - 불필요한 Facade (단순 위임만)
@Service
public class UserContextCommandFacade {
    private final CreateUserContextUseCase createUserContextUseCase;

    public UserContextCommandFacade(CreateUserContextUseCase createUserContextUseCase) {
        this.createUserContextUseCase = createUserContextUseCase;
    }

    public UserContextResponse createUserContext(CreateUserContextCommand command) {
        return createUserContextUseCase.execute(command);  // 단순 위임만 (불필요)
    }
}

// ❌ Controller도 복잡해짐
@RestController
public class UserContextController {
    private final UserContextCommandFacade facade;  // 불필요한 Facade

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserContextApiRequest request) {
        CreateUserContextCommand command = UserContextApiMapper.toCommand(request);
        UserContextResponse response = facade.createUserContext(command);  // 단순 위임
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(UserContextApiMapper.toApiResponse(response)));
    }
}

// ✅ 좋음 - UseCase 직접 호출
@RestController
public class UserContextController {
    private final CreateUserContextUseCase createUserContextUseCase;  // UseCase 직접 의존

    public UserContextController(CreateUserContextUseCase createUserContextUseCase) {
        this.createUserContextUseCase = createUserContextUseCase;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserContextApiRequest request) {
        CreateUserContextCommand command = UserContextApiMapper.toCommand(request);
        UserContextResponse response = createUserContextUseCase.execute(command);  // ✅ 직접 호출
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(UserContextApiMapper.toApiResponse(response)));
    }
}
```

**이점**:
- 불필요한 레이어 제거 (YAGNI 원칙)
- 코드 간결화
- 의존성 체인 단축

---

### 2. 단순 CRUD만 하는 경우 ❌

**문제**: 단순 CRUD는 Facade 없이도 충분히 간단

```java
// ❌ 나쁨 - 단순 조회에 Facade 사용
@Service
public class SettingQueryFacade {
    private final GetSettingUseCase getSettingUseCase;

    public SettingResponse getSetting(GetSettingQuery query) {
        return getSettingUseCase.execute(query);  // 단순 위임만
    }
}

// ✅ 좋음 - UseCase 직접 호출
@RestController
public class SettingController {
    private final GetSettingUseCase getSettingUseCase;  // UseCase 직접

    @GetMapping("/{key}")
    public ResponseEntity<?> getSetting(@PathVariable String key) {
        GetSettingQuery query = new GetSettingQuery(key);
        SettingResponse response = getSettingUseCase.execute(query);  // ✅ 직접 호출
        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }
}
```

**이점**:
- Over-engineering 방지
- 단순한 CRUD는 UseCase만으로 충분

---

## 실전 예시

### ✅ Facade 사용 예시 1: TenantController

**상황**: 3개 Command UseCase + 2개 Query UseCase

```java
// Facade 1: TenantCommandFacade (3개 UseCase 통합)
@Service
public class TenantCommandFacade {
    private final CreateTenantUseCase createTenantUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final UpdateTenantStatusUseCase updateTenantStatusUseCase;

    public TenantResponse createTenant(CreateTenantCommand command) {
        return createTenantUseCase.execute(command);
    }

    public TenantResponse updateTenant(UpdateTenantCommand command) {
        return updateTenantUseCase.execute(command);
    }

    public TenantResponse updateTenantStatus(UpdateTenantStatusCommand command) {
        return updateTenantStatusUseCase.execute(command);
    }
}

// Facade 2: TenantQueryFacade (2개 UseCase 통합)
@Service
public class TenantQueryFacade {
    private final GetTenantUseCase getTenantUseCase;
    private final SearchTenantsUseCase searchTenantsUseCase;

    public TenantResponse getTenant(GetTenantQuery query) {
        return getTenantUseCase.execute(query);
    }

    public List<TenantResponse> searchTenants(SearchTenantsQuery query) {
        return searchTenantsUseCase.execute(query);
    }
}

// Controller: 2개 Facade만 의존
@RestController
public class TenantController {
    private final TenantCommandFacade tenantCommandFacade;
    private final TenantQueryFacade tenantQueryFacade;

    public TenantController(
        TenantCommandFacade tenantCommandFacade,
        TenantQueryFacade tenantQueryFacade
    ) {
        this.tenantCommandFacade = tenantCommandFacade;
        this.tenantQueryFacade = tenantQueryFacade;
    }

    // 3개 Command + 2개 Query 메서드...
}
```

**결과**: Controller 의존성 5개 → 2개로 감소

---

### ❌ Facade 불필요 예시: UserContextController

**상황**: 1개 Command UseCase만 존재

```java
// ❌ 불필요한 Facade
@Service
public class UserContextCommandFacade {
    private final CreateUserContextUseCase createUserContextUseCase;

    public UserContextResponse createUserContext(CreateUserContextCommand command) {
        return createUserContextUseCase.execute(command);  // 단순 위임만
    }
}

// ✅ 올바른 방식 - UseCase 직접 호출
@RestController
public class UserContextController {
    private final CreateUserContextUseCase createUserContextUseCase;  // 직접 의존

    public UserContextController(CreateUserContextUseCase createUserContextUseCase) {
        this.createUserContextUseCase = createUserContextUseCase;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserContextApiRequest request) {
        CreateUserContextCommand command = UserContextApiMapper.toCommand(request);
        UserContextResponse response = createUserContextUseCase.execute(command);  // 직접 호출
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(UserContextApiMapper.toApiResponse(response)));
    }
}
```

**결과**: 불필요한 레이어 제거, 코드 간결화

---

## 체크리스트

### Facade 생성 전 확인사항

#### 1. UseCase 개수 확인
- [ ] UseCase가 2개 이상인가?
  - ✅ Yes → Facade 필수
  - ❌ No → 다음 단계로

#### 2. 추가 로직 필요 여부
- [ ] 트랜잭션 조율이 필요한가?
- [ ] 데이터 통합/변환이 필요한가?
- [ ] 여러 UseCase를 순차적으로 호출해야 하는가?
  - ✅ Yes (하나라도) → Facade 권장
  - ❌ No (모두 아님) → UseCase 직접 호출

#### 3. YAGNI 원칙 확인
- [ ] 단순 위임만 하는 Facade인가?
  - ✅ Yes → Facade 제거, UseCase 직접 호출
  - ❌ No → Facade 유지

---

## 명확한 규칙 정리

### Rule of 3 기준표

| 상황 | Facade | 이유 |
|------|--------|------|
| **UseCase 3개 이상** | ✅ 필수 | Controller 의존성 감소 (Rule of 3 #1) |
| **Transaction + 외부 호출 분리** | ✅ 필수 | 트랜잭션 경계 명확화 (Rule of 3 #2) |
| **Controller 의존성 감소 필요** | ✅ 필수 | 결합도 감소 (Rule of 3 #3) |
| UseCase 1-2개 + 트랜잭션 조율 | ✅ 권장 | 트랜잭션 경계 명확화 |
| UseCase 1-2개 + 데이터 통합 | ✅ 권장 | 비즈니스 로직 캡슐화 |
| UseCase 1-2개 + 단순 위임 | ❌ 불필요 | YAGNI 원칙 위배 |
| 단순 CRUD | ❌ 불필요 | Over-engineering |

---

## 참고 자료

### 관련 문서
- [Application Layer 가이드](../00_application-creation-guide.md)
- [REST API Creation Guide](../../01-adapter-rest-api-layer/00_rest-api-creation-guide.md)
- [헥사고날 아키텍처](../../00-architecture/hexagonal-architecture.md)

### 참조 구현
- **Facade 사용**: `application/iam/tenant/facade/TenantCommandFacade.java`
- **UseCase 직접 호출**: `adapter-in/rest-api/.../usercontext/controller/UserContextController.java`

---

**결론**: **"Facade는 필요할 때만 사용하고, 단순 위임만 하는 경우는 UseCase를 직접 호출하라"** (YAGNI 원칙)
