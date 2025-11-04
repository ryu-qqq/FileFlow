# DTO 네이밍 컨벤션 (Command/Query/Response)

**목적**: Application Layer DTO 네이밍 규칙 및 패키지 구조 표준화

**위치**: `application/[context]/dto/`

**관련 문서**:
- [Application Package Guide](../package-guide/01_application_package_guide.md)
- [Command/Query DTO](./02_command-query-dto.md)
- [UseCase Inner DTO (DEPRECATED)](../assembler-pattern/02_usecase-inner-dto.md)

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

### DTO 네이밍 3대 원칙

1. **의도 표현**: 네이밍만으로 DTO의 역할이 명확해야 함
2. **접미사 일치**: 패키지명과 클래스 접미사가 일치해야 함
3. **Record 사용**: 모든 DTO는 Java Record로 정의

---

## 📁 패키지 구조

```
application/
└─ [context]/
   └─ dto/
      ├─ command/           # 쓰기 작업 DTO
      │  ├─ CreateOrderCommand.java
      │  ├─ UpdateOrderStatusCommand.java
      │  └─ CancelOrderCommand.java
      ├─ query/             # 읽기 조건 DTO
      │  ├─ GetOrderQuery.java
      │  ├─ SearchOrdersQuery.java
      │  └─ FindOrdersByCustomerQuery.java
      └─ response/          # 응답 DTO
         ├─ OrderResponse.java
         ├─ OrderSummaryResponse.java
         └─ OrderDetailResponse.java
```

---

## 📌 Command (쓰기 DTO) 네이밍

### 규칙: `{Verb}{Aggregate}Command`

**패턴**:
- **동사**: Create, Update, Delete, Cancel, Confirm, Approve 등
- **집합체**: Order, Payment, Product 등
- **접미사**: `Command` (필수)

### 예시

```java
// ✅ Good
public record CreateOrderCommand(...)      // 주문 생성
public record UpdateOrderStatusCommand(...) // 주문 상태 변경
public record CancelOrderCommand(...)       // 주문 취소
public record DeleteProductCommand(...)     // 상품 삭제
public record ApprovePaymentCommand(...)    // 결제 승인
public record ConfirmShipmentCommand(...)   // 배송 확인
```

```java
// ❌ Bad
public record OrderCreate(...)              // 동사-명사 순서 잘못됨
public record OrderCreateCommand(...)       // 명사-동사 순서 잘못됨
public record CreateOrder(...)              // 접미사 누락
public record CreateOrderDTO(...)           // DTO 대신 Command 사용
public record CreateOrderRequest(...)       // Request는 Adapter Layer용
```

### 동사 선택 가이드

| 동작 | 동사 | 예시 |
|------|------|------|
| 생성 | Create | `CreateOrderCommand` |
| 수정 | Update | `UpdateOrderStatusCommand` |
| 삭제 | Delete | `DeleteProductCommand` |
| 취소 | Cancel | `CancelOrderCommand` |
| 확인 | Confirm | `ConfirmOrderCommand` |
| 승인 | Approve | `ApprovePaymentCommand` |
| 거부 | Reject | `RejectOrderCommand` |
| 발행 | Issue | `IssueRefundCommand` |
| 활성화 | Activate | `ActivateAccountCommand` |
| 비활성화 | Deactivate | `DeactivateAccountCommand` |

---

## 🔍 Query (읽기 조건 DTO) 네이밍

### 규칙: `{Verb}{Aggregate}Query` 또는 `{Verb}{Aggregate}By{Condition}Query`

**패턴**:
- **동사**: Get, Find, Search, List 등
- **집합체**: Order, Payment, Product 등
- **조건** (선택): ById, ByCustomer, ByStatus 등
- **접미사**: `Query` (필수)

### 예시

```java
// ✅ Good - 단순 조회
public record GetOrderQuery(Long orderId)           // ID로 단건 조회
public record GetProductQuery(Long productId)       // ID로 단건 조회

// ✅ Good - 조건부 조회
public record FindOrdersByCustomerQuery(...)        // 고객별 주문 조회
public record FindProductsByStatusQuery(...)        // 상태별 상품 조회

// ✅ Good - 검색
public record SearchOrdersQuery(...)                // 주문 검색 (복잡한 조건)
public record SearchProductsQuery(...)              // 상품 검색

// ✅ Good - 목록 조회
public record ListOrdersQuery(...)                  // 주문 목록
public record ListProductsQuery(...)                // 상품 목록
```

```java
// ❌ Bad
public record OrderQuery(...)                       // 동사 누락
public record GetOrder(...)                         // 접미사 누락
public record OrderGetQuery(...)                    // 명사-동사 순서 잘못됨
public record GetOrderRequest(...)                  // Request는 Adapter Layer용
```

### 동사 선택 가이드

| 목적 | 동사 | 설명 | 예시 |
|------|------|------|------|
| 단건 조회 | Get | ID로 단일 엔티티 조회 | `GetOrderQuery` |
| 조건 조회 | Find | 특정 조건으로 조회 | `FindOrdersByCustomerQuery` |
| 검색 | Search | 복잡한 검색 조건 | `SearchOrdersQuery` |
| 목록 | List | 전체 또는 페이징 목록 | `ListOrdersQuery` |
| 개수 | Count | 집계 쿼리 | `CountOrdersByStatusQuery` |
| 존재 여부 | Exists | 존재 확인 | `ExistsOrderQuery` |

---

## 📤 Response (응답 DTO) 네이밍

### 규칙: `{Aggregate}Response` 또는 `{Aggregate}{Detail}Response`

**패턴**:
- **집합체**: Order, Payment, Product 등
- **상세도** (선택): Summary, Detail, Info 등
- **접미사**: `Response` (필수)

### 예시

```java
// ✅ Good - 기본 응답
public record OrderResponse(...)                    // 주문 기본 정보
public record PaymentResponse(...)                  // 결제 기본 정보

// ✅ Good - 상세도 명시
public record OrderSummaryResponse(...)             // 주문 요약 정보
public record OrderDetailResponse(...)              // 주문 상세 정보
public record ProductInfoResponse(...)              // 상품 정보
public record CustomerProfileResponse(...)          // 고객 프로필

// ✅ Good - 목록 응답
public record OrderListResponse(...)                // 주문 목록 (페이징 포함)
public record ProductPageResponse(...)              // 상품 페이지 (페이징 포함)
```

```java
// ❌ Bad
public record Order(...)                            // 접미사 누락 (Domain과 충돌)
public record OrderDTO(...)                         // DTO 대신 Response 사용
public record OrderResult(...)                      // Result 대신 Response 사용
public record GetOrderResponse(...)                 // 동사 포함 금지
public record OrderApiResponse(...)                 // Api는 Adapter Layer용
```

### 상세도 선택 가이드

| 상세도 | 접미사 | 설명 | 예시 |
|--------|--------|------|------|
| 기본 | `Response` | 일반적인 응답 | `OrderResponse` |
| 요약 | `SummaryResponse` | 최소 정보만 | `OrderSummaryResponse` |
| 상세 | `DetailResponse` | 전체 정보 포함 | `OrderDetailResponse` |
| 정보 | `InfoResponse` | 특정 정보 집합 | `CustomerInfoResponse` |
| 목록 | `ListResponse` | 목록 응답 (페이징 포함) | `OrderListResponse` |
| 페이지 | `PageResponse` | 페이징 정보 포함 | `ProductPageResponse` |

---

## 📋 전체 예시: Order Context

```
application/order/dto/
├─ command/
│  ├─ CreateOrderCommand.java
│  ├─ UpdateOrderStatusCommand.java
│  ├─ CancelOrderCommand.java
│  └─ AddOrderItemCommand.java
├─ query/
│  ├─ GetOrderQuery.java
│  ├─ FindOrdersByCustomerQuery.java
│  ├─ SearchOrdersQuery.java
│  └─ ListOrdersQuery.java
└─ response/
   ├─ OrderResponse.java
   ├─ OrderSummaryResponse.java
   ├─ OrderDetailResponse.java
   └─ OrderListResponse.java
```

---

## ✅ Command DTO 예제

```java
package com.company.application.order.dto.command;

import java.util.List;

/**
 * 주문 생성 Command
 *
 * @author development-team
 * @since 1.0.0
 */
public record CreateOrderCommand(
    Long customerId,
    List<OrderItem> items,
    String notes
) {
    /**
     * Compact Constructor - 필수 검증
     */
    public CreateOrderCommand {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        // 불변 리스트로 방어적 복사
        items = List.copyOf(items);
    }

    /**
     * ✅ Nested Record - OrderItem
     */
    public record OrderItem(
        Long productId,
        Integer quantity,
        Long unitPrice
    ) {
        public OrderItem {
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID");
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            if (unitPrice == null || unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative");
            }
        }
    }
}
```

---

## ✅ Query DTO 예제

```java
package com.company.application.order.dto.query;

import java.time.LocalDate;

/**
 * 주문 검색 Query
 *
 * @author development-team
 * @since 1.0.0
 */
public record SearchOrdersQuery(
    Long customerId,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    Long minAmount,
    Long maxAmount,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
    /**
     * Compact Constructor - 검증
     */
    public SearchOrdersQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            throw new IllegalArgumentException("Min amount must be less than max amount");
        }
    }

    /**
     * ✅ Builder 패턴 (Optional 파라미터)
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long customerId;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long minAmount;
        private Long maxAmount;
        private int page = 0;
        private int size = 20;
        private String sortBy = "createdAt";
        private String sortDirection = "DESC";

        public Builder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder dateRange(LocalDate start, LocalDate end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }

        public Builder amountRange(Long min, Long max) {
            this.minAmount = min;
            this.maxAmount = max;
            return this;
        }

        public Builder pagination(int page, int size) {
            this.page = page;
            this.size = size;
            return this;
        }

        public Builder sort(String by, String direction) {
            this.sortBy = by;
            this.sortDirection = direction;
            return this;
        }

        public SearchOrdersQuery build() {
            return new SearchOrdersQuery(
                customerId, status, startDate, endDate,
                minAmount, maxAmount, page, size, sortBy, sortDirection
            );
        }
    }
}
```

---

## ✅ Response DTO 예제

```java
package com.company.application.order.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * 주문 상세 Response
 *
 * @author development-team
 * @since 1.0.0
 */
public record OrderDetailResponse(
    Long orderId,
    CustomerInfo customer,
    List<LineItem> items,
    Long totalAmount,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * ✅ Nested Record - CustomerInfo
     */
    public record CustomerInfo(
        Long customerId,
        String customerName,
        String email
    ) {}

    /**
     * ✅ Nested Record - LineItem
     */
    public record LineItem(
        Long productId,
        String productName,
        Integer quantity,
        Long unitPrice,
        Long subtotal
    ) {}
}
```

---

## 🔄 UseCase에서의 사용

```java
package com.company.application.order.port.in;

import com.company.application.order.dto.command.CreateOrderCommand;
import com.company.application.order.dto.response.OrderResponse;

/**
 * 주문 생성 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CreateOrderUseCase {

    /**
     * ✅ Command → Response
     */
    OrderResponse createOrder(CreateOrderCommand command);
}
```

```java
package com.company.application.order.port.in;

import com.company.application.order.dto.query.GetOrderQuery;
import com.company.application.order.dto.response.OrderDetailResponse;

/**
 * 주문 조회 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetOrderUseCase {

    /**
     * ✅ Query → Response
     */
    OrderDetailResponse getOrder(GetOrderQuery query);
}
```

---

## 🚨 Do / Don't

### Do ✅

- **Command**: `{Verb}{Aggregate}Command` 패턴 준수
  - `CreateOrderCommand`, `UpdateOrderStatusCommand`
- **Query**: `{Verb}{Aggregate}Query` 패턴 준수
  - `GetOrderQuery`, `SearchOrdersQuery`
- **Response**: `{Aggregate}Response` 패턴 준수
  - `OrderResponse`, `OrderDetailResponse`
- **패키지 위치**: `dto/command/`, `dto/query/`, `dto/response/`
- **Record 사용**: 모든 DTO는 Java Record로 정의
- **Compact Constructor**: 필수 검증 로직 포함
- **방어적 복사**: `List.copyOf()` 사용

### Don't ❌

- **순서 오류**: `OrderCreateCommand` (명사-동사 순서 잘못됨)
- **접미사 누락**: `CreateOrder` (Command 접미사 없음)
- **잘못된 접미사**: `CreateOrderDTO`, `CreateOrderRequest` (Adapter Layer용)
- **동사 누락**: `OrderQuery` (Get/Find/Search 등 동사 필요)
- **Response에 동사**: `GetOrderResponse` (동사 포함 금지)
- **UseCase 내부**: UseCase 내부 Record로 정의 금지 (별도 파일로)

---

## 🧪 ArchUnit 검증 규칙

```java
// Command DTO는 Command로 끝나야 함
classes().that().resideInAPackage("..application..dto.command..")
    .should().haveSimpleNameEndingWith("Command")
    .andShould().beRecords();

// Query DTO는 Query로 끝나야 함
classes().that().resideInAPackage("..application..dto.query..")
    .should().haveSimpleNameEndingWith("Query")
    .andShould().beRecords();

// Response DTO는 Response로 끝나야 함
classes().that().resideInAPackage("..application..dto.response..")
    .should().haveSimpleNameEndingWith("Response")
    .andShould().beRecords();

// DTO는 dto/ 패키지에만 위치 (UseCase 내부 Record 금지)
classes().that().areRecords()
    .and().haveSimpleNameMatching(".*Command|.*Query|.*Response")
    .and().resideInAPackage("..application..")
    .should().resideInAnyPackage("..application..dto..")
    .because("Command/Query/Response DTOs must be in dto/ package (not inside UseCase)");
```

---

## 📊 네이밍 체크리스트

### Command
- [ ] `{Verb}{Aggregate}Command` 패턴 준수
- [ ] 동사는 Create/Update/Delete/Cancel 등 명확한 의도 표현
- [ ] `dto/command/` 패키지에 위치
- [ ] Java Record로 정의
- [ ] Compact Constructor로 검증

### Query
- [ ] `{Verb}{Aggregate}Query` 패턴 준수
- [ ] 동사는 Get/Find/Search/List 등 조회 의도 표현
- [ ] `dto/query/` 패키지에 위치
- [ ] Java Record로 정의
- [ ] Compact Constructor로 검증

### Response
- [ ] `{Aggregate}Response` 패턴 준수
- [ ] 동사 포함 금지
- [ ] `dto/response/` 패키지에 위치
- [ ] Java Record로 정의
- [ ] 최소한의 정보만 포함

---

## 📖 관련 문서

- **[Application Package Guide](../package-guide/01_application_package_guide.md)** - 전체 패키지 구조
- **[Command UseCase](../usecase-design/01_command-usecase.md)** - Command UseCase 설계
- **[Query UseCase](../usecase-design/02_query-usecase.md)** - Query UseCase 설계
- **[DTO Validation](./03_dto-validation.md)** - DTO 검증 전략
- **[Command/Query DTO](./02_command-query-dto.md)** - CQRS DTO 패턴

---

**작성자**: Development Team
**최종 수정일**: 2025-11-03
**버전**: 1.0.0
