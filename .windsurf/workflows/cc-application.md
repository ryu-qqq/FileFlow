---
description: application layer  보일러 템플릿 를 CC에 준수하여 만든다
---

# Application Layer Workflow - Hexagonal Architecture

**Version**: 1.0.0
**Framework**: Spring Boot 3.5.x + Java 21
**Pattern**: Hexagonal Architecture (Ports & Adapters) + CQRS + DDD
---

## 📚 Overview

Application 레이어는 **비즈니스 로직 조율(Orchestration)**을 담당하는 핵심 레이어입니다.

### 핵심 원칙
- **Port/Adapter 패턴**: 인터페이스(Port)와 구현(Service) 분리
- **CQRS 패턴**: Command와 Query 분리
- **트랜잭션 경계 관리**: `@Transactional` 범위 최적화
- **DTO 패턴**: Command/Query/Response DTO 사용
- **Assembler 패턴**: Domain ↔ DTO 변환 중앙화

---

## 🏗️ Directory Structure

```
application/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/company/application/
│   │           ├── common/
│   │           │   └── dto/
│   │           │       └── response/
│   │           │           ├── PageResponse.java
│   │           │           └── SliceResponse.java
│   │           │
│   │           └── [bounded-context]/  # 예: order, product, user
│   │               ├── port/
│   │               │   ├── in/         # UseCase 인터페이스
│   │               │   │   ├── CreateOrderUseCase.java
│   │               │   │   ├── UpdateOrderUseCase.java
│   │               │   │   └── GetOrderQueryService.java
│   │               │   │
│   │               │   └── out/        # 외부 의존성 인터페이스
│   │               │       ├── OrderCommandOutPort.java
│   │               │       └── OrderQueryOutPort.java
│   │               │
│   │               ├── service/        # UseCase 구현체
│   │               │   ├── CreateOrderService.java
│   │               │   ├── UpdateOrderService.java
│   │               │   └── GetOrderService.java
│   │               │
│   │               ├── dto/
│   │               │   ├── command/    # 쓰기 작업 DTO
│   │               │   │   ├── CreateOrderCommand.java
│   │               │   │   └── UpdateOrderCommand.java
│   │               │   │
│   │               │   ├── query/      # 읽기 작업 DTO
│   │               │   │   ├── GetOrderQuery.java
│   │               │   │   └── SearchOrderQuery.java
│   │               │   │
│   │               │   └── response/   # 응답 DTO
│   │               │       ├── OrderResponse.java
│   │               │       └── OrderDetailResponse.java
│   │               │
│   │               ├── assembler/      # Domain-DTO 변환
│   │               │   └── OrderAssembler.java
│   │               │
│   │               └── facade/         # 여러 UseCase 조합 (선택적)
│   │                   └── OrderFacade.java
│   │
│   └── test/
│       └── java/
│           └── com/company/application/
│               └── [bounded-context]/
│                   ├── service/
│                   │   └── CreateOrderServiceTest.java
│                   └── assembler/
│                       └── OrderAssemblerTest.java
```

---

## 🎯 Component Templates

### 1. Port/In - UseCase Interface (Command)

```java
package com.company.application.order.port.in;

import com.company.application.order.dto.command.CreateOrderCommand;
import com.company.application.order.dto.response.OrderResponse;

/**
 * CreateOrderUseCase - 주문 생성 유스케이스
 *
 * <p>CQRS Command 패턴의 진입점 인터페이스입니다.</p>
 *
 * @author cc-application
 * @since 1.0.0
 */
public interface CreateOrderUseCase {

    /**
     * 주문 생성 실행
     *
     * @param command 주문 생성 명령
     * @return 생성된 주문 응답
     */
    OrderResponse execute(CreateOrderCommand command);
}
```

### 2. Port/In - Query Service Interface

```java
package com.company.application.order.port.in;

import com.company.application.order.dto.query.GetOrderQuery;
import com.company.application.order.dto.response.OrderDetailResponse;

/**
 * GetOrderQueryService - 주문 조회 쿼리 서비스
 *
 * <p>CQRS Query 패턴의 진입점 인터페이스입니다.</p>
 *
 * @author cc-application
 * @since 1.0.0
 */
public interface GetOrderQueryService {

    /**
     * 주문 상세 조회
     *
     * @param query 주문 조회 쿼리
     * @return 주문 상세 응답
     */
    OrderDetailResponse getById(GetOrderQuery query);
}
```

### 3. Port/Out - Command OutPort

```java
package com.company.application.order.port.out;

import com.company.domain.order.OrderDomain;

/**
 * OrderCommandOutPort - 주문 Command 출력 포트
 *
 * <p>Persistence Layer로의 쓰기 작업 인터페이스입니다.</p>
 *
 * @author cc-application
 * @since 1.0.0
 */
public interface OrderCommandOutPort {

    /**
     * 주문 저장
     *
     * @param order 주문 도메인
     * @return 저장된 주문 도메인
     */
    OrderDomain save(OrderDomain order);

    /**
     * 주문 삭제 (Soft Delete)
     *
     * @param orderId 주문 ID
     */
    void delete(Long orderId);
}
```

### 4. Port/Out - Query OutPort

```java
package com.company.application.order.port.out;

import com.company.domain.order.OrderDomain;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * OrderQueryOutPort - 주문 Query 출력 포트
 *
 * <p>Persistence Layer로의 읽기 작업 인터페이스입니다.</p>
 *
 * @author cc-application
 * @since 1.0.0
 */
public interface OrderQueryOutPort {

    /**
     * ID로 주문 조회
     *
     * @param orderId 주문 ID
     * @return 주문 도메인 (Optional)
     */
    Optional<OrderDomain> findById(Long orderId);

    /**
     * 주문 목록 조회 (페이징)
     *
     * @param customerId 고객 ID (Long FK)
     * @param pageable 페이징 정보
     * @return 주문 페이지
     */
    Page<OrderDomain> findAllByCustomerId(Long customerId, Pageable pageable);
}
```

### 5. Service - UseCase Implementation

```java
package com.company.application.order.service;

import com.company.application.order.assembler.OrderAssembler;
import com.company.application.order.dto.command.CreateOrderCommand;
import com.company.application.order.dto.response.OrderResponse;
import com.company.application.order.port.in.CreateOrderUseCase;
import com.company.application.order.port.out.OrderCommandOutPort;
import com.company.domain.order.OrderDomain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateOrderService - 주문 생성 서비스
 *
 * <p>CQRS Command 처리를 담당하는 Application Service입니다.</p>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>Command 작업은 @Transactional 필수</li>
 *   <li>외부 API 호출은 트랜잭션 밖에서 처리</li>
 *   <li>트랜잭션은 짧게 유지</li>
 * </ul>
 *
 * @author cc-application
 * @since 1.0.0
 */
@Service
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderAssembler orderAssembler;
    private final OrderCommandOutPort commandOutPort;

    /**
     * CreateOrderService 생성자
     *
     * <p>Constructor Injection을 통해 의존성을 주입받습니다.</p>
     *
     * @param orderAssembler Domain-DTO 변환 Assembler
     * @param commandOutPort 주문 저장 Command OutPort
     */
    public CreateOrderService(
            OrderAssembler orderAssembler,
            OrderCommandOutPort commandOutPort) {
        this.orderAssembler = orderAssembler;
        this.commandOutPort = commandOutPort;
    }

    /**
     * 주문 생성 실행
     *
     * <p><strong>트랜잭션 범위:</strong></p>
     * <ul>
     *   <li>Command → Domain 변환</li>
     *   <li>Domain 저장 (Database Write)</li>
     *   <li>Domain → Response 변환</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>@Transactional 내에서 외부 API 호출 금지</li>
     *   <li>Long FK 전략 준수 (JPA 관계 어노테이션 사용 금지)</li>
     * </ul>
     *
     * @param command 주문 생성 명령
     * @return 생성된 주문 응답
     */
    @Transactional
    @Override
    public OrderResponse execute(CreateOrderCommand command) {
        // 1. Command → Domain 변환 (Assembler)
        OrderDomain domain = orderAssembler.toDomain(command);

        // 2. Domain 비즈니스 로직 실행
        domain.validateOrder();
        domain.calculateTotalPrice();

        // 3. Domain 저장 (CommandOutPort를 통해 Persistence Layer 호출)
        OrderDomain savedDomain = commandOutPort.save(domain);

        // 4. Domain → Response 변환 (Assembler)
        return orderAssembler.toResponse(savedDomain);
    }
}
```

### 6. DTO - Command (Record Pattern)

```java
package com.company.application.order.dto.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * CreateOrderCommand - 주문 생성 명령
 *
 * <p>Java Record를 사용한 불변 Command 객체입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>customerId: 필수, 양수</li>
 *   <li>items: 필수, 1개 이상</li>
 *   <li>shippingAddress: 필수, 공백 불가</li>
 * </ul>
 *
 * @param customerId 고객 ID (Long FK)
 * @param items 주문 항목 목록
 * @param shippingAddress 배송 주소
 * @param paymentMethod 결제 방법
 *
 * @author cc-application
 * @since 1.0.0
 */
public record CreateOrderCommand(
        Long customerId,
        List<OrderItemCommand> items,
        String shippingAddress,
        String paymentMethod
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 활용한 불변성 보장 및 검증</p>
     */
    public CreateOrderCommand {
        // Null 방어
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // 불변 리스트로 변환
        items = List.copyOf(items);
    }

    /**
     * OrderItemCommand - 주문 항목 명령
     *
     * @param productId 상품 ID (Long FK)
     * @param quantity 수량
     * @param unitPrice 단가
     */
    public record OrderItemCommand(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {
        public OrderItemCommand {
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Product ID must be positive");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
        }
    }
}
```

### 7. DTO - Query (Record Pattern)

```java
package com.company.application.order.dto.query;

/**
 * GetOrderQuery - 주문 조회 쿼리
 *
 * <p>Java Record를 사용한 불변 Query 객체입니다.</p>
 *
 * @param orderId 주문 ID
 * @param customerId 고객 ID (권한 검증용, Long FK)
 *
 * @author cc-application
 * @since 1.0.0
 */
public record GetOrderQuery(
        Long orderId,
        Long customerId
) {
    /**
     * Compact Constructor - 유효성 검증
     */
    public GetOrderQuery {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        // customerId는 선택적 (관리자는 null 가능)
    }
}
```

### 8. DTO - Response (Record Pattern)

```java
package com.company.application.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderDetailResponse - 주문 상세 응답
 *
 * <p>Java Record를 사용한 불변 Response 객체입니다.</p>
 *
 * @param orderId 주문 ID
 * @param customerId 고객 ID (Long FK)
 * @param items 주문 항목 목록
 * @param totalPrice 총 금액
 * @param status 주문 상태
 * @param shippingAddress 배송 주소
 * @param paymentMethod 결제 방법
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 *
 * @author cc-application
 * @since 1.0.0
 */
public record OrderDetailResponse(
        Long orderId,
        Long customerId,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        String status,
        String shippingAddress,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Compact Constructor - 불변성 보장
     */
    public OrderDetailResponse {
        // 불변 리스트로 변환
        items = items != null ? List.copyOf(items) : List.of();
    }

    /**
     * 정적 팩토리 메서드
     */
    public static OrderDetailResponse of(
            Long orderId,
            Long customerId,
