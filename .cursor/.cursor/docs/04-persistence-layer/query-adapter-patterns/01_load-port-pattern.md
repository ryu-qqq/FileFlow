# Load Port Pattern (조회 포트 패턴 - Pure CQRS)

**목적**: CQRS Query 패턴에서 DTO 직접 조회를 위한 Port 인터페이스 정의

**위치**: `application/[module]/port/out/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### Pure CQRS Query 패턴

Load Port는 **Query(읽기) 전용** Port이며, **DTO를 직접 반환**합니다:

```
Application Layer (UseCase)
    ↓ Query
LoadOrderPort (Interface)
    ↓ 구현
OrderQueryAdapter
    ↓ 호출
QueryDSL DTO Projection
    ↓ 반환
OrderDetailResponse (DTO)
```

**규칙**:
- ✅ **DTO 직접 반환** (Domain Model 거치지 않음)
- ✅ Query(조회)만 담당
- ✅ QueryDSL `Projections.constructor()` 사용
- ❌ Domain Model 반환 금지 (Load Command는 별도 Port)
- ❌ Command(저장/삭제) 메서드 금지

---

## 📦 Load Port 인터페이스 (Query)

### 기본 패턴

```java
package com.company.application.order.port.out;

import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.dto.response.OrderSummaryResponse;
import com.company.domain.order.OrderId;
import com.company.domain.order.CustomerId;
import java.util.List;
import java.util.Optional;

/**
 * Order 조회 Port (Query - DTO 직접 반환)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface LoadOrderPort {

    /**
     * Order 상세 정보를 조회합니다.
     *
     * @param orderId Order ID
     * @return Order 상세 DTO
     */
    Optional<OrderDetailResponse> loadById(OrderId orderId);

    /**
     * Customer의 Order 목록을 조회합니다.
     *
     * @param customerId Customer ID
     * @return Order 요약 DTO 목록
     */
    List<OrderSummaryResponse> loadByCustomerId(CustomerId customerId);

    /**
     * Order 목록을 페이징 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return Order 요약 DTO 목록
     */
    Page<OrderSummaryResponse> loadAll(Pageable pageable);
}
```

**핵심**:
- **입력**: `OrderId`, `CustomerId` (Domain Value Object)
- **출력**: `OrderDetailResponse`, `OrderSummaryResponse` (DTO)
- **책임**: 조회만 담당, 저장은 Command Port로 분리

---

## 📋 Response DTO 정의

### DTO 위치

```
application/
└── order/
    └── dto/
        └── response/
            ├── OrderDetailResponse.java      ← 상세 조회용
            └── OrderSummaryResponse.java     ← 목록 조회용
```

**💡 포인트**: DTO는 **Application Layer**에 위치 (Persistence Layer 아님!)

### OrderDetailResponse (상세 조회용)

```java
package com.company.application.order.dto.response;

import com.company.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order 상세 조회 Response DTO
 *
 * @author development-team
 * @since 1.0.0
 */
public record OrderDetailResponse(
    Long id,
    Long userId,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // QueryDSL Projections.constructor()가 사용할 생성자
    // Record는 자동으로 제공
}
```

### OrderSummaryResponse (목록 조회용)

```java
package com.company.application.order.dto.response;

import com.company.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order 요약 조회 Response DTO (목록용)
 *
 * @author development-team
 * @since 1.0.0
 */
public record OrderSummaryResponse(
    Long id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {
    // 목록 조회는 필요한 필드만 포함 (성능 최적화)
}
```

**💡 포인트**:
- **상세 조회**: 모든 필드 포함
- **목록 조회**: 필요한 필드만 포함 (성능)

---

## 🔄 UseCase에서 사용

### Query UseCase (DTO 직접 사용)

```java
package com.company.application.order.usecase;

import com.company.application.order.dto.response.OrderDetailResponse;
import com.company.application.order.port.in.GetOrderDetailUseCase;
import com.company.application.order.port.out.LoadOrderPort;
import com.company.domain.order.OrderId;
import com.company.domain.order.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order 상세 조회 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)  // ✅ Query는 readOnly
public class GetOrderDetailService implements GetOrderDetailUseCase {

    private final LoadOrderPort loadOrderPort;

    @Override
    public OrderDetailResponse execute(GetOrderQuery query) {
        // DTO를 그대로 사용 (Domain 변환 없음)
        return loadOrderPort.loadById(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
}
```

**💡 포인트**:
- `@Transactional(readOnly = true)` (Query 최적화)
- DTO 직접 반환 (Domain Model 거치지 않음)
- 비즈니스 로직 없음 (Domain 불필요)

### 목록 조회 UseCase

```java
@Service
@Transactional(readOnly = true)
public class GetCustomerOrdersService implements GetCustomerOrdersUseCase {

    private final LoadOrderPort loadOrderPort;

    @Override
    public List<OrderSummaryResponse> execute(GetCustomerOrdersQuery query) {
        // DTO 목록 직접 반환
        return loadOrderPort.loadByCustomerId(query.customerId());
    }
}
```

---

## 📊 Load Port vs Load Command Port 비교

| 구분 | Load Port (Query) | Load Command Port |
|-----|------------------|-------------------|
| **목적** | DTO 조회 (View 전용) | Domain 조회 (수정 목적) |
| **입력** | `OrderId` | `OrderId` |
| **출력** | `OrderDetailResponse` (DTO) | `Order` (Domain) |
| **사용처** | Query UseCase | Command UseCase |
| **예시** | `GetOrderDetailService` | `ConfirmOrderService` |
| **Transaction** | `@Transactional(readOnly = true)` | `@Transactional` |
| **비즈니스 로직** | ❌ 없음 | ✅ 있음 |

---

## 🚨 Do / Don't

### ❌ Bad Examples

```java
// ❌ Domain Model 반환 (Query는 DTO만!)
public interface LoadOrderPort {
    Optional<Order> loadById(OrderId id);  // Domain 반환 금지!
}

// ❌ Command 메서드 포함 (CQRS 위반)
public interface LoadOrderPort {
    Optional<OrderDetailResponse> loadById(OrderId id);
    Order save(Order order);  // Command는 별도 Port로!
}

// ❌ DTO가 Persistence Layer에 위치
// adapter-persistence/order/dto/OrderDetailResponse.java  // 금지!
// 올바른 위치: application/order/dto/response/OrderDetailResponse.java

// ❌ Entity 직접 노출
public interface LoadOrderPort {
    Optional<OrderJpaEntity> loadById(OrderId id);  // Entity 노출 금지!
}
```

### ✅ Good Examples

```java
// ✅ Query만 담당 (DTO 반환)
public interface LoadOrderPort {
    Optional<OrderDetailResponse> loadById(OrderId id);
    List<OrderSummaryResponse> loadByCustomerId(CustomerId id);
}

// ✅ Load Command는 별도 Port (Domain 반환)
public interface LoadOrderForUpdatePort {
    Optional<Order> loadById(OrderId id);
}

// ✅ DTO 직접 사용
OrderDetailResponse response = loadOrderPort.loadById(orderId)
    .orElseThrow(() -> new OrderNotFoundException(orderId));

// ✅ @Transactional(readOnly = true)
@Transactional(readOnly = true)
public OrderDetailResponse execute(GetOrderQuery query) {
    return loadOrderPort.loadById(query.orderId())
        .orElseThrow();
}
```

---

## 📐 Load Port 설계 규칙

### 1. 단일 책임 원칙 (SRP)

```java
// ✅ Good - Query만 담당
public interface LoadOrderPort {
    Optional<OrderDetailResponse> loadById(OrderId id);
    List<OrderSummaryResponse> loadByCustomerId(CustomerId id);
}

// ❌ Bad - Query + Command 혼재
public interface OrderPort {
    Optional<OrderDetailResponse> loadById(OrderId id);
    Order save(Order order);  // CQRS 위반!
}
```

### 2. DTO 명명 규칙

```java
// ✅ Good - Response 접미사
public record OrderDetailResponse(...) {}
public record OrderSummaryResponse(...) {}

// ❌ Bad - Response 접미사 없음
public record OrderDetail(...) {}  // DTO인지 Domain인지 모호!
```

### 3. 목적별 DTO 분리

```java
// ✅ Good - 상세/목록 DTO 분리
Optional<OrderDetailResponse> loadById(OrderId id);  // 상세 조회
List<OrderSummaryResponse> loadAll();                // 목록 조회

// ❌ Bad - 동일 DTO 사용 (비효율)
Optional<OrderDetailResponse> loadById(OrderId id);  // 상세 조회
List<OrderDetailResponse> loadAll();  // 목록 조회 (불필요한 필드 포함!)
```

---

## 🔍 Query vs Command 사용 예시

### Query UseCase (Load Port 사용)

```java
// ✅ 단순 조회 → DTO 직접 반환
@Service
@Transactional(readOnly = true)
public class GetOrderDetailService {
    private final LoadOrderPort loadOrderPort;

    public OrderDetailResponse execute(GetOrderQuery query) {
        return loadOrderPort.loadById(query.orderId())
            .orElseThrow();
    }
}
```

### Command UseCase (Load Command Port 사용)

```java
// ✅ 상태 변경 → Domain 조회 + 비즈니스 로직
@Service
@Transactional
public class ConfirmOrderService {
    private final LoadOrderForUpdatePort loadOrderPort;  // Domain 조회
    private final SaveOrderPort saveOrderPort;           // Domain 저장

    public OrderResponse execute(ConfirmOrderCommand command) {
        // 1. Domain 조회
        Order order = loadOrderPort.loadById(command.orderId())
            .orElseThrow();

        // 2. 비즈니스 로직 실행
        order.confirm();

        // 3. Domain 저장
        Order savedOrder = saveOrderPort.save(order);

        return OrderResponse.from(savedOrder);
    }
}
```

---

## 📋 체크리스트

Load Port 작성 시:
- [ ] Query(조회)만 담당
- [ ] DTO 직접 반환 (`OrderDetailResponse`)
- [ ] DTO는 Application Layer에 위치
- [ ] Domain Model 반환 금지
- [ ] Command 메서드 없음
- [ ] `@Transactional(readOnly = true)` 사용
- [ ] 상세/목록 DTO 분리
- [ ] Record 패턴 사용

---

## 📖 관련 문서

- **[QueryDSL DTO Projection](./02_querydsl-dto-projection.md)** - QueryDSL DTO 조회 패턴
- **[Query Adapter Implementation](./03_query-adapter-implementation.md)** - Load Port 구현
- **[Save Port Pattern](../command-adapter-patterns/01_save-port-pattern.md)** - Command Port 비교
- **[Query Performance Optimization](./04_query-performance-optimization.md)** - N+1 문제 해결

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
