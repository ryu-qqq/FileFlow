# Java 21 Patterns 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Java 21 패턴 핵심 요약본
> **상세 규칙**: Hook이 자동으로 15개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## ✅ 필수 규칙

### 1️⃣ Record Patterns (Value Object, DTO)
- ✅ **DTO with Records**: API Request/Response DTO를 Record로
- ✅ **Value Objects with Records**: Immutable Value Object
- ✅ **Pattern Matching**: `switch` 표현식 활용
- ✅ **UseCase Inner Record**: UseCase 내부 DTO
- ✅ **Entity vs Value Object**: Record는 Value Object에만

### 2️⃣ Sealed Classes (Domain Modeling)
- ✅ **Domain Modeling**: 제한된 타입 계층 구조
- ✅ **Event Modeling**: Domain Event sealed interface
- ✅ **Result Types**: `sealed interface Result<T>`
- ✅ **Aggregate State Modeling**: 상태 Enum 대체
- ✅ **Exception Hierarchy**: sealed exception 계층

### 3️⃣ Virtual Threads (비동기 처리)
- ✅ **Virtual Threads Basics**: 경량 스레드
- ✅ **Async Processing**: `@Async` + Virtual Threads
- ✅ **Performance Tuning**: Thread Pool 설정
- ✅ **Spring Integration**: `spring.threads.virtual.enabled=true`
- ✅ **Outbound Port Async**: 외부 API 호출 비동기화

---

## 📊 레이어 통계

- **총 규칙 수**: 15개
- **Cache Rules**: 15개 (Hook 자동 주입)

---

## 🎯 핵심 패턴 (Java 21 활용)

### Record (Value Object)
```java
// ✅ Record로 Value Object
public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
```

### Sealed Class (Domain Event)
```java
// ✅ Sealed Interface로 Domain Event
public sealed interface OrderEvent
    permits OrderPlaced, OrderCancelled, OrderCompleted {
}

public record OrderPlaced(OrderId orderId, Instant placedAt) implements OrderEvent {}
public record OrderCancelled(OrderId orderId, Instant cancelledAt) implements OrderEvent {}
```

### Virtual Threads (비동기 처리)
```java
// application.yml
spring:
  threads:
    virtual:
      enabled: true

// ✅ @Async + Virtual Threads
@Async
public CompletableFuture<PaymentResult> processPayment(PaymentCommand command) {
    // Virtual Thread로 실행됨!
    return CompletableFuture.completedFuture(paymentClient.process(command));
}
```

---

**✅ Java 21 패턴은 코드 간결성 30% 향상, 성능 20% 개선!**
