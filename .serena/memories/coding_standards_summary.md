# FileFlow Coding Standards Summary

## Zero-Tolerance Rules (MUST Follow)

### 1. NO Lombok - Pure Java Only
- ❌ PROHIBITED: `@Data`, `@Builder`, `@Getter`, `@Setter`, `@AllArgsConstructor`, etc.
- ✅ REQUIRED: Write all getters/setters/constructors manually
- **Why**: Explicit code, better debugging, framework independence (especially in Domain layer)
- **Enforcement**: Build task `checkNoLombok` fails build if Lombok detected

### 2. Law of Demeter - No Getter Chaining
- ❌ PROHIBITED: `order.getCustomer().getAddress().getZip()`
- ✅ REQUIRED: `order.getCustomerZipCode()` or `order.isShippingToRegion("prefix")`
- **Principle**: "Tell, Don't Ask"
- **Scope**: Strictly enforced in Domain and Application layers
- **Allowed Exceptions**: Java Streams, Optional, Builder/Fluent APIs, BigDecimal operations

### 3. Long FK Strategy - No JPA Relationships
- ❌ PROHIBITED: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
- ✅ REQUIRED: `private Long userId;` (use Long foreign key IDs)
- **Why**: Avoid lazy loading issues, N+1 queries, circular dependencies
- **Scope**: All JPA Entity classes

### 4. Transaction Boundaries
- ❌ PROHIBITED: External API calls (RestTemplate, WebClient, etc.) inside `@Transactional` methods
- ✅ REQUIRED: Keep transactions short, external calls outside transaction boundaries
- **Why**: Long-running transactions block resources and cause deadlocks
- **Enforcement**: Git pre-commit hook validates this

### 5. Spring Proxy Constraints
`@Transactional` WILL NOT WORK on:
- Private methods
- Final classes or methods  
- Internal method calls within same class (`this.method()`)
**Solution**: Create separate service classes or use programmatic transactions

### 6. Javadoc Required
- ✅ REQUIRED: All public classes and methods must have Javadoc with `@author`, `@since`
- **Enforcement**: Checkstyle validates this

### 7. Scope Discipline
- ❌ PROHIBITED: Implementing features not explicitly requested
- ✅ REQUIRED: Build ONLY what was asked for (MVP approach)

## Code Style Principles

### Immutability Preference
```java
// ✅ Preferred
public class Order {
    private final Long id;
    private final String orderNumber;
    
    private Order(Long id, String orderNumber) {
        this.id = id;
        this.orderNumber = orderNumber;
    }
    
    public static Order of(Long id, String orderNumber) {
        return new Order(id, orderNumber);
    }
    
    // Manual getters
    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
}
```

### Static Factory Methods
- Prefer static factory methods over public constructors
- Use descriptive names: `Order.of()`, `Money.fromAmount()`, `Address.create()`

### Package Structure
Follow hexagonal architecture package organization:
```
com.ryuqq.fileflow/
├── domain/           # Pure business logic, no external dependencies
├── application/      # Use case implementations, transaction boundaries
└── adapter/
    ├── in.web/      # REST controllers
    └── out/
        ├── persistence/ # JPA repositories
        ├── redis/       # Cache
        └── aws/         # S3, SQS, etc.
```

## Testing Standards

### Coverage Requirements (enforced by JaCoCo)
- **Domain Layer**: 90% minimum
- **Application Layer**: 80% minimum  
- **Adapter Layers**: 70% minimum
- **Per-class minimum**: 50% line coverage

### Required Test Types
1. **Unit Tests**: All business logic in domain/application
2. **Integration Tests**: Adapter layers with real infrastructure (using Testcontainers)
3. **ArchUnit Tests**: Architecture validation (dependency rules, naming conventions)

### ArchUnit Validation
- Layer dependency rules (Domain → Application → Adapter)
- Package naming conventions
- No cyclic dependencies
- Runs automatically during build

## Quality Tools Configuration

### Checkstyle
- Config: `config/checkstyle/checkstyle.xml`
- Max warnings: 0 (strict enforcement)
- Validates: Javadoc, naming, code formatting

### SpotBugs
- Effort: MAX
- Report Level: LOW (most strict)
- Excludes: `config/spotbugs/spotbugs-exclude.xml`

### JaCoCo
- Reports: XML + HTML
- Verification runs on every build
- Fails build if coverage thresholds not met
