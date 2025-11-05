# Domain Layer ArchUnit Rules - Domain 규칙 자동 검증

**목적**: ArchUnit을 활용하여 Domain Layer의 코딩 규칙을 빌드 시 자동 검증

**관련 문서**:
- [Domain Object Creation Guide](../aggregate-design/00_domain-object-creation-guide.md)
- [Law of Demeter](../law-of-demeter/01_getter-chaining-prohibition.md)
- [Lombok Prohibition](../../04-persistence-layer/jpa-entity-design/00_lombok-prohibition.md)
- [Aggregate Testing](01_aggregate-testing.md)

**검증 도구**: ArchUnit 1.2.0+

**테스트 위치**: `domain/src/test/java/architecture/DomainLayerArchitectureTest.java`

---

## 📌 핵심 원칙

### Domain Layer Zero-Tolerance 규칙

Domain Layer는 프로젝트의 핵심 비즈니스 로직을 담당하므로 가장 엄격한 규칙이 적용됩니다:

1. **Lombok 절대 금지** - `@Data`, `@Builder`, `@Getter`, `@Setter` 등 모두 금지
2. **Law of Demeter** - Getter 체이닝 금지, `getIdValue()` 메서드 필수
3. **Domain Object Creation Pattern** - `forNew()`, `reconstitute()`, `of()` 패턴 필수
4. **Framework 의존성 금지** - Spring, JPA 등 프레임워크 어노테이션 금지
5. **레이어 의존성** - Application/Adapter Layer에 절대 의존 금지

**ArchUnit이 빌드 시 자동으로 검증하여 위반 시 빌드 실패**

---

## ❌ 금지 패턴 (Anti-Patterns)

### Anti-Pattern 1: Lombok 사용

```java
// ❌ Domain Layer에서 Lombok 사용 금지
package com.ryuqq.domain.order;

import lombok.Data;           // ❌ 금지
import lombok.Builder;        // ❌ 금지
import lombok.Getter;         // ❌ 금지
import lombok.Setter;         // ❌ 금지

@Data    // ❌ ArchUnit 빌드 실패!
@Builder // ❌ ArchUnit 빌드 실패!
public class Order {
    @Getter private OrderId id;    // ❌ ArchUnit 빌드 실패!
    @Setter private OrderStatus status; // ❌ ArchUnit 빌드 실패!
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Domain objects should not use Lombok'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> is annotated with <@Data> in (Order.java:7)
```

**해결책**:
```java
// ✅ Pure Java 사용
package com.ryuqq.domain.order;

public class Order {
    private final OrderId id;
    private OrderStatus status;

    // Package-private 생성자
    Order(OrderId id, OrderStatus status) {
        this.id = id;
        this.status = status;
    }

    // Static Factory Methods
    public static Order forNew(...) { ... }
    public static Order reconstitute(...) { ... }

    // Public Getters
    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }

    // Law of Demeter: ID 값 직접 접근 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }
}
```

---

### Anti-Pattern 2: ID 필드가 final이 아님

```java
// ❌ ID 필드는 반드시 final이어야 함
package com.ryuqq.domain.order;

public class Order {
    private OrderId id;  // ❌ final 없음 → ArchUnit 빌드 실패!

    // ID를 변경하는 Setter
    public void setId(OrderId id) { // ❌ ID는 불변이어야 함
        this.id = id;
    }
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Domain Entity ID field must be final'
was violated (1 times):
Field <com.ryuqq.domain.order.Order.id> is not final in (Order.java:5)
```

**해결책**:
```java
// ✅ ID 필드는 final
public class Order {
    private final OrderId id;  // ✅ final 필수

    Order(OrderId id, ...) {
        this.id = id;
    }

    public OrderId getId() { return id; }  // Getter만 제공
    // ✅ Setter 없음 (ID는 생성 시점에만 설정)
}
```

---

### Anti-Pattern 3: reconstitute() 메서드 누락

```java
// ❌ Domain Entity는 reconstitute() 메서드가 필수
package com.ryuqq.domain.order;

public class Order {
    private final OrderId id;

    // ✅ forNew() 있음
    public static Order forNew(CustomerId customerId) {
        return new Order(null, customerId, ...);
    }

    // ❌ reconstitute() 없음 → ArchUnit 빌드 실패!
    // DB에서 조회한 데이터를 Domain 객체로 복원할 방법이 없음
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Domain Entities must have reconstitute() method'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> does not have 'reconstitute' method in (Order.java:1)
```

**해결책**:
```java
// ✅ reconstitute() 메서드 필수
public class Order {

    // ✅ 신규 생성 (ID = null)
    public static Order forNew(CustomerId customerId) {
        return new Order(null, customerId, OrderStatus.PENDING, ...);
    }

    // ✅ DB 복원 (ID 필수, 전체 상태 복원)
    public static Order reconstitute(
        OrderId id,
        CustomerId customerId,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        if (id == null) {
            throw new IllegalArgumentException("reconstitute는 ID가 필수입니다");
        }
        return new Order(id, customerId, status, createdAt, updatedAt, deleted);
    }
}
```

---

### Anti-Pattern 4: getIdValue() 메서드 누락 (Law of Demeter 위반)

```java
// ❌ Law of Demeter: ID 값 접근을 위한 메서드 필수
package com.ryuqq.domain.order;

public class Order {
    private final OrderId id;

    public OrderId getId() { return id; }
    // ❌ getIdValue() 없음 → ArchUnit 경고!
}

// Application Layer에서:
Long orderIdValue = order.getId().value(); // ❌ Getter 체이닝!
```

**ArchUnit 검증 경고**:
```
Architecture Warning [Priority: MEDIUM] - Rule 'Domain Entities should provide getIdValue() method'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> does not have 'getIdValue' method in (Order.java:1)
```

**해결책**:
```java
// ✅ Law of Demeter: ID 값 직접 접근 메서드 제공
public class Order {
    private final OrderId id;

    public OrderId getId() { return id; }

    // ✅ Law of Demeter: ID 값 직접 접근
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }
}

// Application Layer에서:
Long orderIdValue = order.getIdValue(); // ✅ Getter 체이닝 없음!
```

---

### Anti-Pattern 5: Domain → Framework 의존성

```java
// ❌ Domain Layer에서 Spring Framework 의존
package com.ryuqq.domain.order;

import org.springframework.stereotype.Component;  // ❌ Spring 의존
import org.springframework.stereotype.Service;   // ❌ Spring 의존

@Component // ❌ ArchUnit 빌드 실패!
public class Order {
    // Domain은 순수 Java여야 함
}

@Service // ❌ ArchUnit 빌드 실패!
public class OrderDomainService {
    // Domain Service도 순수 Java
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Domain Layer should not depend on Framework'
was violated (2 times):
Class <com.ryuqq.domain.order.Order> depends on <org.springframework.stereotype.Component>
Class <com.ryuqq.domain.order.OrderDomainService> depends on <org.springframework.stereotype.Service>
```

**해결책**:
```java
// ✅ Domain은 순수 Java (Framework 의존 없음)
package com.ryuqq.domain.order;

// ✅ 어노테이션 없음
public class Order {
    // 순수 비즈니스 로직만
}

// ✅ Domain Service도 순수 Java
public class OrderDomainService {
    // Spring 없이 순수 Java로 비즈니스 로직 구현
}
```

---

### Anti-Pattern 6: Domain → Application/Adapter 의존성

```java
// ❌ Domain Layer에서 상위 레이어 의존
package com.ryuqq.domain.order;

import com.ryuqq.application.in.web.OrderController;      // ❌ Domain → Web
import com.ryuqq.application.out.persistence.OrderJpaEntity; // ❌ Domain → Persistence

public class Order {
    private OrderController controller; // ❌ ArchUnit 빌드 실패!

    public OrderJpaEntity toEntity() { // ❌ ArchUnit 빌드 실패!
        // Domain이 JPA Entity를 알면 안 됨
    }
}
```

**ArchUnit 검증 실패**:
```
Architecture Violation [Priority: HIGH] - Rule 'Domain Layer should not depend on Application/Adapter'
was violated (2 times):
Class <com.ryuqq.domain.order.Order> depends on <com.ryuqq.application.in.web.OrderController>
Class <com.ryuqq.domain.order.Order> depends on <com.ryuqq.application.out.persistence.OrderJpaEntity>
```

**해결책**:
```java
// ✅ Domain은 어떤 레이어에도 의존하지 않음
package com.ryuqq.domain.order;

// ✅ Domain 패키지만 import
import com.ryuqq.domain.customer.CustomerId;
import com.ryuqq.domain.shared.Money;

public class Order {
    // ✅ 순수 Domain 로직만
    // Application/Adapter Layer는 Domain을 호출
}
```

---

## ✅ ArchUnit 검증 규칙 구현

### 테스트 클래스: `DomainLayerArchitectureTest.java`

**위치**: `domain/src/test/java/architecture/DomainLayerArchitectureTest.java`

```java
package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Domain Layer Architecture Test
 *
 * <p>Domain Layer의 코딩 규칙을 ArchUnit으로 자동 검증합니다.</p>
 *
 * <h3>검증 규칙:</h3>
 * <ul>
 *   <li>Lombok 금지 (@Data, @Builder, @Getter, @Setter)</li>
 *   <li>ID 필드 final 검증</li>
 *   <li>reconstitute() 메서드 존재 검증</li>
 *   <li>getIdValue() 메서드 존재 검증 (Law of Demeter)</li>
 *   <li>Framework 의존성 금지 (Spring, JPA)</li>
 *   <li>레이어 의존성 검증 (Domain → Application/Adapter 금지)</li>
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Domain Layer ArchUnit 검증")
class DomainLayerArchitectureTest {

    private JavaClasses domainClasses;

    @BeforeEach
    void setUp() {
        // Domain Layer 클래스만 로드
        domainClasses = new ClassFileImporter()
            .importPackages("com.ryuqq.domain");
    }

    //=================================================
    // 1. Lombok 금지 검증
    //=================================================

    @Test
    @DisplayName("Domain 객체는 @Data 사용 금지")
    void domainObjectShouldNotUseLombokData() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("lombok.Data")
            .because("Domain 객체는 Plain Java를 사용해야 합니다 (Lombok 금지)");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain 객체는 @Builder 사용 금지")
    void domainObjectShouldNotUseLombokBuilder() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("lombok.Builder")
            .because("Domain 객체는 Static Factory Method를 사용해야 합니다");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain 객체는 @Getter 사용 금지")
    void domainObjectShouldNotUseLombokGetter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("lombok.Getter")
            .because("Domain 객체는 명시적 getter 메서드를 작성해야 합니다");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain 객체는 @Setter 사용 금지")
    void domainObjectShouldNotUseLombokSetter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("lombok.Setter")
            .because("Domain 객체는 불변성을 유지해야 합니다 (Setter 금지)");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain 객체는 @AllArgsConstructor/@NoArgsConstructor 사용 금지")
    void domainObjectShouldNotUseLombokConstructor() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().beAnnotatedWith("lombok.AllArgsConstructor")
            .orShould().beAnnotatedWith("lombok.NoArgsConstructor")
            .because("Domain 객체는 명시적 생성자를 작성해야 합니다");

        rule.check(domainClasses);
    }

    //=================================================
    // 2. ID 필드 Final 검증
    //=================================================

    @Test
    @DisplayName("Domain Entity의 ID 필드는 final이어야 함")
    void domainEntityIdFieldShouldBeFinal() {
        ArchRule rule = fields()
            .that().haveName("id")
            .and().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Test")
            .should().beFinal()
            .because("ID는 생성 후 변경되면 안 됩니다 (불변성 보장)");

        rule.check(domainClasses);
    }

    //=================================================
    // 3. reconstitute() 메서드 검증
    //=================================================

    @Test
    @DisplayName("Domain Entity는 reconstitute() 메서드가 필수")
    void domainEntityShouldHaveReconstituteMethod() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("Id")
            .and().haveSimpleNameNotEndingWith("Test")
            .and().haveSimpleNameNotEndingWith("Fixture")
            .and().areNotEnums()
            .and().areNotInterfaces()
            .and().areNotRecords()
            .should().declareMethod("reconstitute")
            .because("Domain Entity는 DB 데이터를 복원하기 위한 reconstitute() 메서드가 필수입니다");

        rule.check(domainClasses);
    }

    //=================================================
    // 4. getIdValue() 메서드 검증 (Law of Demeter)
    //=================================================

    @Test
    @DisplayName("Domain Entity는 getIdValue() 메서드가 권장됨 (Law of Demeter)")
    void domainEntityShouldHaveGetIdValueMethod() {
        // 경고 수준 (빌드 실패 아님)
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("Id")
            .and().haveSimpleNameNotEndingWith("Test")
            .and().haveSimpleNameNotEndingWith("Fixture")
            .and().areNotEnums()
            .and().areNotInterfaces()
            .and().areNotRecords()
            .should().declareMethod("getIdValue")
            .because("Law of Demeter: ID 값 직접 접근을 위한 getIdValue() 메서드를 제공하세요");

        // allowEmptyShould(true)로 경고만 출력
        rule.allowEmptyShould(true).check(domainClasses);
    }

    //=================================================
    // 5. Framework 의존성 금지
    //=================================================

    @Test
    @DisplayName("Domain Layer는 Spring Framework 의존 금지")
    void domainLayerShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "javax.persistence.."
            )
            .because("Domain Layer는 순수 Java여야 합니다 (Framework 의존 금지)");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 JPA 어노테이션 사용 금지")
    void domainLayerShouldNotUseJpaAnnotations() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .orShould().beAnnotatedWith("jakarta.persistence.Table")
            .orShould().beAnnotatedWith("javax.persistence.Entity")
            .orShould().beAnnotatedWith("javax.persistence.Table")
            .because("Domain Layer는 JPA Entity가 아닙니다 (Persistence Layer 전용)");

        rule.check(domainClasses);
    }

    //=================================================
    // 6. 레이어 의존성 검증
    //=================================================

    @Test
    @DisplayName("Domain Layer는 Application/Adapter Layer에 의존 금지")
    void domainLayerShouldNotDependOnApplicationOrAdapter() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..adapter..",
                "..persistence..",
                "..web..",
                "..rest.."
            )
            .because("Domain Layer는 어떤 레이어에도 의존하면 안 됩니다 (헥사고날 아키텍처)");

        rule.check(domainClasses);
    }

    @Test
    @DisplayName("Domain Layer는 Bootstrap/Infrastructure Layer에 의존 금지")
    void domainLayerShouldNotDependOnBootstrapOrInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..bootstrap..",
                "..infrastructure.."
            )
            .because("Domain Layer는 Bootstrap/Infrastructure Layer에 의존하면 안 됩니다");

        rule.check(domainClasses);
    }

    //=================================================
    // 7. Package 구조 검증
    //=================================================

    @Test
    @DisplayName("Domain Layer는 올바른 Package 구조를 따라야 함")
    void domainLayerShouldFollowPackageStructure() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .should().resideInAnyPackage(
                "..domain..",
                "..domain.*.aggregate..",
                "..domain.*.entity..",
                "..domain.*.vo..",
                "..domain.*.service..",
                "..domain.shared.."
            )
            .because("Domain Layer는 정의된 패키지 구조를 따라야 합니다");

        rule.check(domainClasses);
    }
}
```

---

## 🚀 실행 방법

### 1. Gradle 빌드 시 자동 실행

```bash
# 전체 빌드 (ArchUnit 자동 실행)
./gradlew build

# Domain Layer ArchUnit만 실행
./gradlew :domain:test --tests architecture.DomainLayerArchitectureTest

# 출력:
# > Task :domain:test
# DomainLayerArchitectureTest
#   ✓ Domain 객체는 @Data 사용 금지 (0.2s)
#   ✓ Domain 객체는 @Builder 사용 금지 (0.1s)
#   ✓ Domain Entity의 ID 필드는 final이어야 함 (0.3s)
#   ✓ Domain Entity는 reconstitute() 메서드가 필수 (0.4s)
#   ✓ Domain Layer는 Spring Framework 의존 금지 (0.2s)
#
# BUILD SUCCESSFUL in 2s
```

---

### 2. IntelliJ에서 실행

```
1. DomainLayerArchitectureTest.java 열기
2. 클래스 좌측 ▶ 클릭 → "Run 'DomainLayerArchitectureTest'"
3. 또는 Ctrl+Shift+F10 (Windows/Linux) / Cmd+Shift+R (Mac)
```

---

### 3. CI/CD Pipeline에서 실행

```yaml
# .github/workflows/archunit-domain.yml
name: Domain Layer ArchUnit Check

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  archunit-domain:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Domain Layer ArchUnit Tests
        run: ./gradlew :domain:test --tests architecture.DomainLayerArchitectureTest

      - name: Upload Test Results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: archunit-domain-report
          path: domain/build/reports/tests/
```

---

## 🔧 문제 해결

### 문제 1: ArchUnit 테스트 실패 - Lombok 감지

**증상**:
```
Architecture Violation - Rule 'Domain objects should not use Lombok'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> is annotated with <@Data>
```

**원인**: Domain 객체에 Lombok 어노테이션 사용

**해결책**:
```java
// ❌ Before
@Data
public class Order { ... }

// ✅ After
public class Order {
    private final OrderId id;

    // Getter 직접 작성
    public OrderId getId() { return id; }
}
```

---

### 문제 2: ArchUnit 테스트 실패 - ID 필드 final 누락

**증상**:
```
Architecture Violation - Rule 'Domain Entity ID field must be final'
was violated (1 times):
Field <com.ryuqq.domain.order.Order.id> is not final
```

**원인**: ID 필드가 final이 아님

**해결책**:
```java
// ❌ Before
private OrderId id;

// ✅ After
private final OrderId id;
```

---

### 문제 3: ArchUnit 테스트 실패 - reconstitute() 메서드 누락

**증상**:
```
Architecture Violation - Rule 'Domain Entities must have reconstitute() method'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> does not have 'reconstitute' method
```

**원인**: DB 복원을 위한 `reconstitute()` 메서드 누락

**해결책**:
```java
// ✅ reconstitute() 메서드 추가
public static Order reconstitute(
    OrderId id,
    CustomerId customerId,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted
) {
    if (id == null) {
        throw new IllegalArgumentException("reconstitute는 ID가 필수입니다");
    }
    return new Order(id, customerId, status, createdAt, updatedAt, deleted);
}
```

---

### 문제 4: ArchUnit 경고 - getIdValue() 메서드 권장

**증상** (경고, 빌드는 성공):
```
Architecture Warning - Rule 'Domain Entities should provide getIdValue() method'
was violated (1 times):
Class <com.ryuqq.domain.order.Order> does not have 'getIdValue' method
```

**원인**: Law of Demeter를 위한 `getIdValue()` 메서드 누락

**해결책**:
```java
// ✅ getIdValue() 메서드 추가 (Law of Demeter)
public Long getIdValue() {
    return id != null ? id.value() : null;
}
```

---

### 문제 5: ArchUnit 테스트 실패 - Spring 의존성 감지

**증상**:
```
Architecture Violation - Rule 'Domain Layer should not depend on Spring Framework'
was violated (1 times):
Class <com.ryuqq.domain.order.OrderDomainService> depends on
<org.springframework.stereotype.Service>
```

**원인**: Domain Layer에서 Spring Framework 사용

**해결책**:
```java
// ❌ Before
import org.springframework.stereotype.Service;

@Service
public class OrderDomainService { ... }

// ✅ After (어노테이션 제거)
public class OrderDomainService { ... }
```

---

## 📋 체크리스트

### ArchUnit 테스트 작성 체크리스트

- [ ] `DomainLayerArchitectureTest.java` 생성 (`domain/src/test/java/architecture/`)
- [ ] Lombok 금지 규칙 추가 (5개: Data, Builder, Getter, Setter, Constructor)
- [ ] ID 필드 final 검증 규칙 추가
- [ ] reconstitute() 메서드 존재 검증 추가
- [ ] getIdValue() 메서드 권장 검증 추가 (경고)
- [ ] Spring Framework 의존성 금지 규칙 추가
- [ ] JPA 어노테이션 금지 규칙 추가
- [ ] 레이어 의존성 금지 규칙 추가 (Application/Adapter)
- [ ] Package 구조 검증 규칙 추가

---

### Domain 객체 작성 체크리스트

- [ ] Lombok 어노테이션 없음 (`@Data`, `@Builder`, `@Getter`, `@Setter` 등)
- [ ] ID 필드 `final` 선언
- [ ] `reconstitute()` 메서드 구현
- [ ] `getIdValue()` 메서드 구현 (Law of Demeter)
- [ ] Spring/JPA 어노테이션 없음
- [ ] Application/Adapter Layer import 없음
- [ ] Gradle 빌드 시 ArchUnit 테스트 통과
- [ ] CI/CD Pipeline 통과

---

## 📚 관련 문서

**다음 단계**:
- [Test Fixture Pattern](03_test-fixture-pattern.md) - Domain 테스트 객체 생성
- [Object Mother Pattern](04_object-mother-pattern.md) - 비즈니스 시나리오 테스트

**관련 가이드**:
- [Domain Object Creation Guide](../aggregate-design/00_domain-object-creation-guide.md) - Domain 객체 생성 패턴
- [Law of Demeter](../law-of-demeter/01_getter-chaining-prohibition.md) - Getter 체이닝 금지
- [Lombok Prohibition](../../04-persistence-layer/jpa-entity-design/00_lombok-prohibition.md) - Lombok 금지 이유

**전체 ArchUnit 가이드**:
- [Layer Dependency Rules](../../05-testing/archunit-rules/01_layer-dependency-rules.md) - 전체 레이어 의존성
- [Naming Convention Rules](../../05-testing/archunit-rules/02_naming-convention-rules.md) - 네이밍 규칙
- [Annotation Rules](../../05-testing/archunit-rules/03_annotation-rules.md) - 어노테이션 규칙

---

**작성자**: Development Team
**최종 수정일**: 2025-10-16
**버전**: 1.0.0
