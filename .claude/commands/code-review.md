---
description: 코드 리뷰 및 개선 제안 (Convention, Performance, Security, Testing)
---

# 코드 리뷰 및 개선 제안

**목적**: 작성된 코드에 대한 포괄적인 리뷰 및 구체적인 개선 제안

**검토 항목**: Convention, Performance, Security, Testing, Architecture

**출력**: 개선 제안 + 수정 코드 예시

---

## 🎯 사용법

```bash
# 특정 파일 리뷰
/code-review domain/Order.java

# 특정 디렉토리 리뷰
/code-review application/order/

# 최근 변경 파일 리뷰
/code-review --recent
```

---

## ✅ 리뷰 체크리스트

### 1. Convention Violations (컨벤션 위반)

**검사 항목**:
- ❌ Lombok 사용 (`@Data`, `@Builder`, `@Getter` 등)
- ❌ Law of Demeter 위반 (`order.getCustomer().getAddress()`)
- ❌ JPA 관계 어노테이션 (`@OneToMany`, `@ManyToOne` 등)
- ❌ Setter 사용 (불변성 위반)
- ❌ @Transactional 내 외부 API 호출
- ❌ Private 메서드에 @Transactional
- ❌ Javadoc 누락

**출력 예시**:
```
🚨 Convention Violations (3건)

1. Lombok 사용 금지 위반 (Line 15)
   ❌ 현재 코드:
      @Data
      public class Order {
          private Long id;
      }

   ✅ 수정 제안:
      public class Order {
          private Long id;

          public Long getId() {
              return this.id;
          }
      }

2. Law of Demeter 위반 (Line 42)
   ❌ 현재 코드:
      String zip = order.getCustomer().getAddress().getZip();

   ✅ 수정 제안:
      public String getCustomerZip() {
          return this.customer.getAddressZip();
      }
      // 호출부
      String zip = order.getCustomerZip();

3. Setter 사용 금지 (Line 28)
   ❌ 현재 코드:
      public void setStatus(OrderStatus status) {
          this.status = status;
      }

   ✅ 수정 제안:
      public void confirm() {
          this.status = OrderStatus.CONFIRMED;
      }
```

### 2. Performance Issues (성능 이슈)

**검사 항목**:
- ❌ N+1 쿼리 문제
- ❌ 불필요한 조회 (LazyLoading 남용)
- ❌ 비효율적인 Stream 사용
- ❌ String concatenation in loop
- ❌ 과도한 객체 생성

**출력 예시**:
```
⚡ Performance Issues (2건)

1. N+1 쿼리 문제 발견 (Line 55)
   ❌ 현재 코드:
      List<Order> orders = orderRepository.findAll();
      orders.forEach(order -> {
          Customer customer = customerRepository.findById(order.getCustomerId());
      });

   ✅ 수정 제안:
      // QueryDSL로 Fetch Join 사용
      List<Order> orders = orderQueryDslRepository
          .findAllWithCustomer();

2. 비효율적인 Stream 사용 (Line 78)
   ❌ 현재 코드:
      list.stream()
          .filter(x -> x.getStatus() == ACTIVE)
          .collect(Collectors.toList())
          .size();

   ✅ 수정 제안:
      list.stream()
          .filter(x -> x.getStatus() == ACTIVE)
          .count();
```

### 3. Security Vulnerabilities (보안 취약점)

**검사 항목**:
- ❌ SQL Injection 위험
- ❌ XSS 공격 가능성
- ❌ 민감 정보 로깅
- ❌ 권한 검증 누락
- ❌ 암호화 누락

**출력 예시**:
```
🛡️ Security Vulnerabilities (1건)

1. 민감 정보 로깅 (Line 92)
   ❌ 현재 코드:
      log.info("User login: {}", user);

   ✅ 수정 제안:
      log.info("User login: userId={}", user.getId());
      // 비밀번호, 개인정보 로깅 금지
```

### 4. Testing Gaps (테스트 누락)

**검사 항목**:
- ❌ 테스트 클래스 없음
- ❌ 엣지 케이스 미검증
- ❌ Exception 테스트 누락
- ❌ 커버리지 80% 미만

**출력 예시**:
```
🧪 Testing Gaps (3건)

1. 테스트 클래스 없음
   ❌ OrderDomain.java에 대응하는 OrderDomainTest.java 없음

   ✅ 수정 제안:
      /test-gen-domain Order  # 자동 테스트 생성

2. 엣지 케이스 미검증 (cancel 메서드)
   ❌ CANCELLED 상태 재취소 테스트 없음

   ✅ 수정 제안:
      @Test
      void shouldThrowExceptionWhenCancellingCancelledOrder() {
          // Given
          Order order = Order.create(/*...*/);
          order.cancel();

          // When & Then
          assertThatThrownBy(() -> order.cancel())
              .isInstanceOf(IllegalStateException.class);
      }

3. Exception 메시지 검증 누락
   ❌ 예외 발생만 확인, 메시지 검증 안 함

   ✅ 수정 제안:
      assertThatThrownBy(() -> OrderId.of(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("OrderId must not be null");
```

### 5. Architecture Violations (아키텍처 위반)

**검사 항목**:
- ❌ Layer 의존성 위반
- ❌ Domain이 Infrastructure 의존
- ❌ Controller가 Persistence 직접 호출
- ❌ Aggregate 경계 위반

**출력 예시**:
```
🏗️ Architecture Violations (1건)

1. Layer 의존성 위반 (Line 22)
   ❌ 현재 코드:
      // Domain Layer
      import com.ryuqq.adapter.persistence.OrderRepository;

   ✅ 수정 제안:
      // Domain Layer는 Port Interface만 의존
      import com.ryuqq.application.port.out.OrderCommandPort;
```

---

## 🔧 리뷰 프로세스

### 1. 파일 분석
```
1. 파일 읽기 및 구조 파악
2. Layer 식별 (Domain/Application/Persistence/REST)
3. 관련 컨벤션 규칙 로드 (Serena Memory)
```

### 2. 위반 사항 탐지
```
1. Regex 기반 패턴 매칭 (Lombok, Law of Demeter 등)
2. AST 분석 (메서드 호출 체이닝, Import 검증)
3. ArchUnit 규칙 적용
```

### 3. 개선 제안 생성
```
1. 위반 사항별 구체적인 수정 코드 제시
2. Before/After 비교
3. 이유 설명 (왜 위반인지, 왜 수정해야 하는지)
```

### 4. 우선순위 분류
```
🔴 Critical: 빌드 실패 또는 보안 취약점
🟡 Important: 컨벤션 위반, 성능 이슈
🟢 Nice-to-have: 코드 스타일, 문서화
```

---

## 📊 리뷰 리포트 형식

```markdown
# Code Review Report: Order.java

## 📋 Summary

- **File**: domain/src/main/java/com/ryuqq/domain/order/Order.java
- **Layer**: Domain Layer
- **Lines**: 120
- **Issues Found**: 8건 (🔴 2건, 🟡 4건, 🟢 2건)

---

## 🔴 Critical Issues (즉시 수정 필수)

### 1. Setter 사용 (불변성 위반)
**Line**: 45
**Category**: Convention
**Severity**: Critical

❌ **현재 코드**:
...java
public void setStatus(OrderStatus status) {
    this.status = status;
}
...

✅ **수정 제안**:
...java
public void confirm() {
    if (this.status != OrderStatus.PLACED) {
        throw new IllegalStateException("Only PLACED orders can be confirmed");
    }
    this.status = OrderStatus.CONFIRMED;
}
...

**이유**: Domain 객체는 불변성을 유지해야 하며, 상태 변경은 비즈니스 메서드를 통해서만 가능합니다.

---

## 🟡 Important Issues (우선 수정 권장)

### 2. Law of Demeter 위반
**Line**: 67
**Category**: Convention
**Severity**: Important

❌ **현재 코드**:
...java
String zip = order.getCustomer().getAddress().getZip();
...

✅ **수정 제안**:
...java
// Order.java
public String getCustomerZip() {
    return this.customer.getAddressZip();
}

// Customer.java
public String getAddressZip() {
    return this.address.getZip();
}
...

**이유**: Getter 체이닝은 결합도를 높이고 Tell, Don't Ask 원칙을 위반합니다.

---

## 🟢 Nice-to-have (개선 권장)

### 7. Javadoc 누락
**Line**: 15
**Category**: Documentation
**Severity**: Low

✅ **수정 제안**:
...java
/**
 * Order Aggregate Root
 *
 * <p>주문의 생명주기를 관리하며 다음 상태를 가집니다:</p>
 * <ul>
 *   <li>PLACED: 주문 생성</li>
 *   <li>CONFIRMED: 주문 확인</li>
 *   <li>CANCELLED: 주문 취소</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
public final class Order {
    ...
}
...

---

## 🧪 Testing Recommendations

1. **테스트 클래스 생성**
   ...bash
   /test-gen-domain Order
   ...

2. **엣지 케이스 추가**
   - CANCELLED 상태 재취소 테스트
   - null 입력 검증 테스트
   - 경계값 테스트 (OrderId.of(Long.MAX_VALUE))

3. **커버리지 목표**: 현재 0% → 목표 100%

---

## 📈 Overall Score: 65/100

- Convention: 70/100 (Lombok, Law of Demeter 위반)
- Performance: 80/100 (큰 이슈 없음)
- Security: 90/100 (양호)
- Testing: 0/100 (테스트 없음)
- Architecture: 100/100 (완벽)

**권장 조치**: 🔴 Critical 2건 즉시 수정 → 테스트 자동 생성 → 🟡 Important 4건 수정
```

---

## 💡 Claude Code 활용 팁

### 1. 전체 리뷰
```
"Review all Java files in domain/order/ directory"
```

### 2. 특정 항목 집중 리뷰
```
"Review Order.java focusing on Law of Demeter violations"
```

### 3. 리뷰 후 자동 수정
```
"Review and fix all critical issues in Order.java"
```

### 4. 리뷰 리포트 생성
```
"Generate a detailed code review report for the entire domain layer"
```

---

## 🎯 기대 효과

1. **조기 발견**: PR 전에 컨벤션 위반 자동 탐지
2. **학습 도구**: 왜 위반인지 설명 → 개발자 성장
3. **시간 절약**: 수동 리뷰 대비 **90% 시간 절감**
4. **일관성**: 모든 코드가 동일한 기준으로 검토

---

**✅ 이 명령어는 Claude Code가 작성된 코드를 포괄적으로 리뷰하고 구체적인 개선 제안을 제공하는 데 사용됩니다.**

**💡 핵심**: Windsurf가 코드를 빠르게 생성하면, Claude Code가 품질을 검증하고 개선!
