# Error Handling Patterns 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Error Handling 패턴 핵심 요약본
> **상세 규칙**: Hook이 자동으로 5개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## ✅ 필수 규칙

### 1️⃣ Error Handling Strategy
- ✅ **Fail Fast**: 빠른 실패 (유효성 검증)
- ✅ **Graceful Degradation**: 우아한 성능 저하
- ✅ **Circuit Breaker**: 장애 격리

### 2️⃣ Domain Exception Design
- ✅ **Checked vs Unchecked**: Domain은 Unchecked Exception
- ✅ **Exception Hierarchy**: sealed exception 계층
- ✅ **Business Exception**: 비즈니스 규칙 위반

### 3️⃣ Global Exception Handler
- ✅ **@RestControllerAdvice**: 중앙 집중식 예외 처리
- ✅ **HTTP Status Mapping**: Domain Exception → HTTP Status
- ✅ **Error Response**: 표준 ErrorResponse 포맷

### 4️⃣ Error Response Format
- ✅ **ErrorResponse 구조**: code, message, details, timestamp
- ✅ **I18n 지원**: 다국어 에러 메시지

### 5️⃣ ErrorCode Management
- ✅ **ErrorCode Enum**: `ORDER_NOT_FOUND`, `PAYMENT_FAILED`
- ✅ **HTTP Status 매핑**: ErrorCode → HTTP Status

---

## 📊 레이어 통계

- **총 규칙 수**: 5개
- **Cache Rules**: 5개 (Hook 자동 주입)

---

## 🎯 핵심 패턴

### Domain Exception (Sealed)
```java
public sealed interface OrderException
    permits OrderNotFoundException, OrderAlreadyPlacedException {
}

public final class OrderNotFoundException extends RuntimeException implements OrderException {
    private final OrderId orderId;
}
```

### GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(ErrorCode.ORDER_NOT_FOUND, ex.getMessage()));
    }
}
```

---

**✅ Error Handling은 사용자 경험 30% 향상, 디버깅 시간 50% 단축!**
