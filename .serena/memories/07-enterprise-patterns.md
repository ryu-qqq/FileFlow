# Enterprise Patterns 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Enterprise 패턴 핵심 요약본
> **상세 규칙**: Hook이 자동으로 10개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## ✅ 필수 규칙

### 1️⃣ Caching (성능 최적화)
- ✅ **Cache Strategies**: Look-Aside, Write-Through, Write-Behind
- ✅ **Distributed Cache**: Redis 기반 분산 캐시
- ✅ **Cache Consistency**: Eventual Consistency vs Strong Consistency

### 2️⃣ Event-Driven (비동기 처리)
- ✅ **Domain Events**: `AbstractAggregateRoot.registerEvent()`
- ✅ **Event Sourcing**: Event Store 기반 상태 복원
- ✅ **Saga Pattern**: 분산 트랜잭션 조율
- ✅ **Multi-Module Event Design**: 모듈 간 이벤트 통신

### 3️⃣ Resilience (장애 대응)
- ✅ **Circuit Breaker**: 외부 API 장애 격리
- ✅ **Retry & Timeout**: 재시도 + 타임아웃 설정
- ✅ **Bulkhead Pattern**: 리소스 격리

---

## 📊 레이어 통계

- **총 규칙 수**: 10개
- **Cache Rules**: 10개 (Hook 자동 주입)

---

## 🎯 핵심 패턴

### Circuit Breaker (Resilience4j)
```java
@CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
public PaymentResult processPayment(PaymentCommand command) {
    return paymentClient.process(command);
}

private PaymentResult paymentFallback(PaymentCommand command, Exception ex) {
    return PaymentResult.retry("Payment service unavailable");
}
```

### Domain Events (Spring Data)
```java
public class Order extends AbstractAggregateRoot<Order> {
    public void place() {
        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlaced(this.id, Instant.now())); // 이벤트 등록
    }
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleOrderPlaced(OrderPlaced event) {
    // 이벤트 처리 (트랜잭션 커밋 후!)
}
```

---

**✅ Enterprise 패턴은 시스템 안정성 50% 향상, 장애 복구 시간 70% 단축!**
