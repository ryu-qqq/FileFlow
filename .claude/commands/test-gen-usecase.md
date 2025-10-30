---
description: Application UseCase 단위 테스트 자동 생성 (Transaction, Port Mock, Exception)
---

# Application UseCase 단위 테스트 자동 생성

**목적**: Application Layer UseCase에 대한 고품질 단위 테스트 자동 생성

**타겟**: Application Layer - UseCase Transaction Boundary Tests

**생성 테스트**: Transaction 경계, Port Mock, Command Validation, Exception Handling

---

## 🎯 사용법

```bash
# UseCase 테스트 생성
/test-gen-usecase PlaceOrder

# QueryService 테스트 생성
/test-gen-usecase GetOrderDetails
```

---

## ✅ 자동 생성되는 테스트 케이스

### 1. Happy Path (성공 케이스)

```java
@Test
@DisplayName("유효한 Command로 주문 생성 성공")
void shouldPlaceOrderWithValidCommand() {
    // Given
    PlaceOrderCommand command = new PlaceOrderCommand(
        100L,  // customerId
        List.of(new OrderItemRequest(1L, 2))
    );

    Order expectedOrder = Order.create(/*...*/);
    given(orderCommandPort.save(any(Order.class)))
        .willReturn(expectedOrder);

    // When
    OrderResponse response = placeOrderUseCase.execute(command);

    // Then
    assertThat(response.orderId()).isEqualTo(1L);
    assertThat(response.status()).isEqualTo("PLACED");

    verify(orderCommandPort).save(any(Order.class));
    verify(eventPublisher).publish(any(OrderPlacedEvent.class));
}
```

### 2. Transaction Boundary 검증

```java
@Test
@DisplayName("UseCase 메서드는 @Transactional이 Public이어야 함")
void useCaseMethodShouldBePublicForTransactional() {
    // Given
    Method executeMethod = PlaceOrderUseCaseImpl.class
        .getDeclaredMethod("execute", PlaceOrderCommand.class);

    // Then
    assertThat(Modifier.isPublic(executeMethod.getModifiers())).isTrue();
    assertThat(executeMethod.isAnnotationPresent(Transactional.class)).isTrue();
}

@Test
@DisplayName("외부 API 호출은 트랜잭션 밖에서 실행")
void externalApiCallShouldBeOutsideTransaction() {
    // Given
    PlaceOrderCommand command = new PlaceOrderCommand(/*...*/);

    // Mock external API
    given(paymentClient.processPayment(anyLong())).willReturn(true);

    // When
    placeOrderUseCase.execute(command);

    // Then
    // executeInTransaction() 호출 확인
    verify(transactionTemplate).execute(any());
    // 외부 API는 트랜잭션 밖에서 호출
    verify(paymentClient).processPayment(anyLong());
}
```

### 3. Port Interface Mock

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceOrderUseCase 단위 테스트")
class PlaceOrderUseCaseTest {

    @Mock
    private OrderCommandPort orderCommandPort;

    @Mock
    private InventoryQueryPort inventoryQueryPort;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PlaceOrderUseCaseImpl placeOrderUseCase;

    @Test
    @DisplayName("재고 부족 시 주문 실패")
    void shouldFailWhenInventoryInsufficient() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(/*...*/);

        given(inventoryQueryPort.checkStock(anyLong()))
            .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> placeOrderUseCase.execute(command))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");

        verify(orderCommandPort, never()).save(any());
    }
}
```

### 4. Command Validation

```java
@Test
@DisplayName("Command의 customerId가 null이면 예외 발생")
void shouldThrowExceptionWhenCustomerIdIsNull() {
    // Given
    PlaceOrderCommand command = new PlaceOrderCommand(
        null,  // customerId
        List.of(new OrderItemRequest(1L, 2))
    );

    // When & Then
    assertThatThrownBy(() -> placeOrderUseCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CustomerId must not be null");
}

@Test
@DisplayName("Command의 items가 비어있으면 예외 발생")
void shouldThrowExceptionWhenItemsAreEmpty() {
    // Given
    PlaceOrderCommand command = new PlaceOrderCommand(
        100L,
        List.of()  // empty items
    );

    // When & Then
    assertThatThrownBy(() -> placeOrderUseCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Order items must not be empty");
}
```

### 5. Exception Handling

```java
@Test
@DisplayName("Port에서 예외 발생 시 적절히 처리")
void shouldHandlePortException() {
    // Given
    PlaceOrderCommand command = new PlaceOrderCommand(/*...*/);

    given(orderCommandPort.save(any(Order.class)))
        .willThrow(new DataAccessException("DB connection failed"));

    // When & Then
    assertThatThrownBy(() -> placeOrderUseCase.execute(command))
        .isInstanceOf(OrderCreationException.class)
        .hasCauseInstanceOf(DataAccessException.class);
}
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
application/src/test/java/com/ryuqq/application/{usecase}/
└── {UseCase}ImplTest.java
```

### 2. 테스트 클래스 템플릿
```java
package com.ryuqq.application.order.usecase;

import com.ryuqq.application.order.port.in.PlaceOrderUseCase;
import com.ryuqq.application.order.port.out.OrderCommandPort;
import com.ryuqq.application.order.dto.command.PlaceOrderCommand;
import com.ryuqq.application.order.dto.response.OrderResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * PlaceOrderUseCase 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 주문 생성</li>
 *   <li>Transaction Boundary: @Transactional 경계 검증</li>
 *   <li>Port Mock: 의존성 Port 모킹</li>
 *   <li>Command Validation: 입력 검증</li>
 *   <li>Exception Handling: 예외 처리</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceOrderUseCase 단위 테스트")
class PlaceOrderUseCaseTest {

    @Mock
    private OrderCommandPort orderCommandPort;

    @InjectMocks
    private PlaceOrderUseCaseImpl placeOrderUseCase;

    // Test methods...
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **MockitoExtension 사용**: `@ExtendWith(MockitoExtension.class)`
- ✅ **Port Interface Mock**: `@Mock private OrderCommandPort`
- ✅ **BDD 스타일 Mockito**: `given()` / `willReturn()` / `verify()`
- ✅ **Transaction 경계 검증**: `@Transactional` Public 메서드 확인
- ✅ **외부 API 분리 검증**: 트랜잭션 밖에서 호출 확인

---

## 📊 테스트 커버리지 목표

| 항목 | 목표 | 설명 |
|------|------|------|
| Line Coverage | 100% | 모든 UseCase 로직 실행 |
| Branch Coverage | 100% | 모든 조건문 분기 |
| Port Interaction | 100% | 모든 Port 호출 검증 |
| Exception Path | 100% | 모든 예외 경로 |
| Transaction Boundary | 100% | @Transactional 경계 검증 |

---

## 🚀 실행 예시

### Input (UseCase Implementation)
```java
@Service
@RequiredArgsConstructor
public class PlaceOrderUseCaseImpl implements PlaceOrderUseCase {

    private final OrderCommandPort orderCommandPort;
    private final InventoryQueryPort inventoryQueryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse execute(PlaceOrderCommand command) {
        validateCommand(command);

        if (!inventoryQueryPort.checkStock(command.productId())) {
            throw new InsufficientStockException("Insufficient stock");
        }

        Order order = Order.create(
            OrderId.generate(),
            CustomerId.of(command.customerId())
        );

        Order savedOrder = orderCommandPort.save(order);

        eventPublisher.publishEvent(
            new OrderPlacedEvent(savedOrder.getIdValue())
        );

        return OrderResponse.from(savedOrder);
    }

    private void validateCommand(PlaceOrderCommand command) {
        if (command.customerId() == null) {
            throw new IllegalArgumentException("CustomerId must not be null");
        }
        if (command.items().isEmpty()) {
            throw new IllegalArgumentException("Order items must not be empty");
        }
    }
}
```

### Output (Auto-generated Test)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaceOrderUseCase 단위 테스트")
class PlaceOrderUseCaseTest {

    @Mock
    private OrderCommandPort orderCommandPort;

    @Mock
    private InventoryQueryPort inventoryQueryPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PlaceOrderUseCaseImpl placeOrderUseCase;

    @Test
    @DisplayName("유효한 Command로 주문 생성 성공")
    void shouldPlaceOrderWithValidCommand() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(
            100L,
            1L,
            List.of(new OrderItemRequest(1L, 2))
        );

        given(inventoryQueryPort.checkStock(1L)).willReturn(true);

        Order expectedOrder = Order.create(
            OrderId.of(1L),
            CustomerId.of(100L)
        );
        given(orderCommandPort.save(any(Order.class)))
            .willReturn(expectedOrder);

        // When
        OrderResponse response = placeOrderUseCase.execute(command);

        // Then
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("PLACED");

        verify(inventoryQueryPort).checkStock(1L);
        verify(orderCommandPort).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
    }

    @Test
    @DisplayName("재고 부족 시 주문 실패")
    void shouldFailWhenInventoryInsufficient() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(/*...*/);
        given(inventoryQueryPort.checkStock(anyLong())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> placeOrderUseCase.execute(command))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");

        verify(orderCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Command의 customerId가 null이면 예외 발생")
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(null, 1L, List.of());

        // When & Then
        assertThatThrownBy(() -> placeOrderUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CustomerId must not be null");
    }

    // ... (12개 테스트 케이스 자동 생성)
}
```

---

## 💡 Claude Code 활용 팁

### 1. 기존 UseCase 분석
```
"Analyze PlaceOrderUseCaseImpl.java and generate comprehensive unit tests with port mocking"
```

### 2. Transaction 경계 집중 테스트
```
"Generate tests focused on @Transactional boundary validation for PlaceOrderUseCase"
```

### 3. 외부 API 호출 분리 테스트
```
"Add tests to verify external API calls are outside transaction boundary"
```

### 4. Port 인터렉션 검증
```
"Add verify() assertions for all port interactions in PlaceOrderUseCaseTest"
```

---

## 🎯 기대 효과

1. **Transaction 안정성**: @Transactional 경계 자동 검증
2. **Port 격리**: Mock을 통한 완벽한 Port 격리 테스트
3. **빠른 실행**: 외부 의존성 없이 밀리초 단위 실행
4. **Command 검증**: 입력 유효성 검사 자동 테스트

---

**✅ 이 명령어는 Claude Code가 Application UseCase의 고품질 단위 테스트를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 UseCase를 생성하면, Claude Code가 Transaction 경계 및 Port Mock 테스트를 자동 생성!
