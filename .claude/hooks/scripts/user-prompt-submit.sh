#!/bin/bash
# ========================================
# Claude Code Dynamic Hook
# user-prompt-submit: 사용자 요청 제출 시 실행
# ========================================
# Claude가 코드를 생성하기 BEFORE 실행
# 요청 분석 및 모듈 컨텍스트 주입
# ========================================

set -e

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$HOOK_DIR/../.." && pwd)"

# Colors
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}🤖 [Dynamic Hook] $1${NC}" >&2
}

log_warning() {
    echo -e "${YELLOW}⚠️  [Dynamic Hook] $1${NC}" >&2
}

# ========================================
# 사용자 요청 분석
# ========================================

USER_PROMPT="$1"

log_info "Analyzing user request..."

# ========================================
# 모듈 컨텍스트 감지
# ========================================

MODULE_CONTEXT=""

if echo "$USER_PROMPT" | grep -qi "domain\|도메인\|비즈니스 로직"; then
    MODULE_CONTEXT="domain"
elif echo "$USER_PROMPT" | grep -qi "usecase\|application\|서비스\|유즈케이스"; then
    MODULE_CONTEXT="application"
elif echo "$USER_PROMPT" | grep -qi "controller\|rest\|api\|어댑터"; then
    MODULE_CONTEXT="adapter"
elif echo "$USER_PROMPT" | grep -qi "repository\|jpa\|database\|persistence\|entity"; then
    MODULE_CONTEXT="adapter-out-persistence"
elif echo "$USER_PROMPT" | grep -qi "request\|response\|dto\|컨트롤러"; then
    MODULE_CONTEXT="adapter-in-web"
fi

# ========================================
# 컨텍스트 기반 가이드라인 주입
# ========================================

if [ -n "$MODULE_CONTEXT" ]; then
    log_info "Module context detected: $MODULE_CONTEXT"

    case $MODULE_CONTEXT in
        domain)
            cat << 'EOF'

# 🏛️ DOMAIN MODULE GUIDELINES

You are working in the **DOMAIN** module. STRICT RULES:

## ❌ ABSOLUTELY FORBIDDEN
- NO Spring Framework imports (org.springframework.*)
- NO JPA/Hibernate imports (jakarta.persistence.*, org.hibernate.*)
- NO Lombok imports or annotations
- NO Jackson annotations
- NO infrastructure concerns

## ✅ ALLOWED
- Pure Java (java.*, javax.validation.*)
- Apache Commons Lang3
- Domain-specific value objects and entities
- Business logic only

## 📝 REQUIRED PATTERNS

### 1. Complete Purity
- NO Spring, NO JPA, NO Lombok, NO infrastructure dependencies
- Pure Java business logic only
- All business rules MUST be in Domain objects

### 2. Immutability
- All fields: `private final` (NO mutable state)
- NO setter methods (state changes return new objects)
- Example:
  public class Order {
      private final OrderId id;
      private final OrderStatus status;

      // ✅ State change returns new object
      public Order confirm() {
          return new Order(this.id, OrderStatus.CONFIRMED);
      }
  }

### 3. Creation Rules
- NO public constructors
- Use static factory methods: create(), of(), from()
- Example:
  private Order(OrderId id, OrderStatus status) { ... }
  public static Order create(OrderId id, List<OrderItem> items) { ... }
  public static Order reconstitute(OrderId id, ...) { ... }

### 4. Business Logic Location
- All business rules in Domain objects
- Calculations, validations, state transitions as methods
- Domain services for multi-aggregate logic only

### 5. Value Objects
- Prefer Java records for value objects
- Example:
  public record OrderId(Long value) {
      public OrderId {
          if (value == null || value <= 0) {
              throw new IllegalArgumentException("Order ID must be positive");
          }
      }
  }

### 6. Exceptions
- Use Domain-specific exceptions extending DomainException
- Example:
  public class OrderNotFoundException extends DomainException { ... }

## 🧪 TEST COVERAGE
- Target: 90%+ coverage required

REMEMBER: Domain must be framework-independent!
EOF
            ;;

        application)
            cat << 'EOF'

# 🔧 APPLICATION MODULE GUIDELINES

You are working in the **APPLICATION** module. STRICT RULES:

## ❌ ABSOLUTELY FORBIDDEN
- NO Adapter imports (com.company.template.adapter.*)
- NO Lombok imports or annotations
- NO direct JPA usage (belongs in adapter-out-persistence)

## ✅ ALLOWED
- Domain imports (com.company.template.domain.*)
- Spring DI (@Service, @Transactional)
- Port interfaces (in/out)

## 📝 REQUIRED PATTERNS

### 1. Transaction Management
- @Transactional ONLY in this layer, NEVER in adapters
- All UseCase implementations must have @Transactional
- Read operations: @Transactional(readOnly = true)
- Example:
  @UseCase
  @Transactional
  public class CreateOrderService implements CreateOrderUseCase { ... }

### 2. UseCase Pattern
- Define Input/Output ports as interfaces
- Implementation in adapters, declaration here
- Single responsibility per UseCase

### 3. Port Interfaces
- Input ports: UseCase interfaces in port/in/
- Output ports: Repository/External abstractions in port/out/
- Example:
  public interface SaveOrderPort {
      Order save(Order order);
  }

### 4. Dependencies
- Depend ONLY on Domain layer
- NO adapters, NO repositories, NO entities
- Use Port abstractions only

### 5. DTO Pattern
- Use Command/Query/Result pattern
- Prefer records for DTOs
- Example:
  public record CreateOrderCommand(
      UserId userId,
      List<OrderItem> items
  ) {
      public CreateOrderCommand {
          Objects.requireNonNull(userId, "User ID required");
      }
  }

### 6. Domain Objects Only
- Work with Domain models, NOT JPA entities
- Convert at adapter boundaries

## 🧪 TEST COVERAGE
- Target: 80%+ coverage required

REMEMBER: Application orchestrates domain, never accesses adapters directly!
EOF
            ;;

        adapter)
            cat << 'EOF'

# 📡 ADAPTER MODULE GUIDELINES

You are working in an **ADAPTER** module. STRICT RULES:

## ❌ ABSOLUTELY FORBIDDEN
- NO Lombok imports or annotations
- NO business logic (belongs in domain)

## ✅ ALLOWED
- Domain and Application imports
- Spring Framework (Web, JPA, etc.)
- Infrastructure code (HTTP, DB, AWS SDK)

## 📝 REQUIRED
- Controllers MUST end with "Controller" suffix
- Repositories MUST end with "Repository" suffix
- Public methods MUST have Javadoc
- MUST include @author tag
- Use pure Java (no Lombok)

## 🧪 TEST COVERAGE
- Target: 70%+ coverage required
- Use Testcontainers for integration tests

REMEMBER: Adapters implement ports defined in application layer!
EOF
            ;;

        adapter-out-persistence)
            cat << 'EOF'

# 💾 PERSISTENCE ADAPTER GUIDELINES

You are working in **ADAPTER-OUT-PERSISTENCE** module. STRICT RULES:

## ❌ ABSOLUTELY FORBIDDEN
- NO Lombok imports or annotations
- NO business logic (belongs in domain)
- NO domain entities with JPA annotations (use separate JPA entities)

## ✅ ALLOWED
- Spring Data JPA
- QueryDSL for complex queries
- JPA entities (separate from domain entities)
- Mappers between JPA entities and domain entities

## 📝 REQUIRED PATTERNS

### 1. NO @Transactional
- Transactions managed by Application layer ONLY
- Adapters are stateless, transaction-free

### 2. NO JPA Relationships
- NO @OneToMany, @ManyToOne, @OneToOne, @ManyToMany
- Use Long foreign key fields only (userId, orderId, etc.)
- Example:
  @Entity
  public class OrderEntity {
      private Long userId;  // ✅ FK as Long
      // ❌ NOT: @ManyToOne private UserEntity user;
  }

### 3. Entity Creation
- NO public constructors (protected for JPA, private for logic)
- Use static factory methods: create(), reconstitute()
- Example:
  protected OrderEntity() {}  // JPA only
  private OrderEntity(...) {}  // Private constructor
  public static OrderEntity create(...) { }  // Factory for new
  public static OrderEntity reconstitute(...) { }  // Factory for DB load

### 4. NO Setter Methods
- Entities must be immutable after creation
- Provide getters only
- Example:
  public Long getUserId() { return userId; }  // ✅ Getter only
  // ❌ NO: public void setUserId(Long id) { }

### 5. NO Business Logic
- Business logic belongs in Domain layer
- Entities are data structures only
- Use Mapper classes for conversion

### 6. Mapper Pattern
- Dedicated Mapper classes for Entity ↔ Domain conversion
- Example:
  @Component
  class OrderEntityMapper {
      public Order toDomain(OrderEntity entity) { ... }
      public OrderEntity toEntity(Order domain) { ... }
  }

### 7. Example Entity Structure
  @Entity
  @Table(name = "orders")
  public class OrderEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      private Long userId;  // ✅ FK as Long

      protected OrderEntity() {}  // ✅ JPA only
      private OrderEntity(Long userId, ...) { }  // ✅ Private

      public static OrderEntity create(Long userId, ...) {
          return new OrderEntity(userId, ...);
      }

      public Long getUserId() { return userId; }  // ✅ Getter only
  }

## 🧪 TEST COVERAGE
- Target: 70%+ coverage
- MUST use Testcontainers with PostgreSQL

REMEMBER: Map JPA entities to domain entities, keep persistence concerns isolated!
EOF
            ;;

        adapter-in-web)
            cat << 'EOF'

# 🌐 CONTROLLER ADAPTER GUIDELINES

You are working in **ADAPTER-IN-WEB** module. STRICT RULES:

## ❌ ABSOLUTELY FORBIDDEN
- NO Lombok imports or annotations
- NO business logic (belongs in domain)
- NO domain entities exposure

## ✅ ALLOWED
- Spring Web (@RestController, @RequestMapping)
- Request/Response DTOs as records
- UseCase dependencies only

## 📝 REQUIRED PATTERNS

### 1. NO Inner Classes
- Request/Response DTOs MUST be separate files
- Example:
  ✅ CreateOrderRequest.java (separate file)
  ✅ CreateOrderResponse.java (separate file)
  ❌ OrderController with inner class OrderRequest

### 2. DTOs as Records
- All Request/Response MUST be Java records
- Example:
  public record CreateOrderRequest(
      @NotNull Long userId,
      @NotEmpty List<OrderItem> items
  ) {
      // ✅ Constructor validation
      public CreateOrderRequest {
          if (userId <= 0) throw new IllegalArgumentException();
      }
  }

### 3. Record Validation
- Compact constructor must include basic validation
- Use Bean Validation annotations
- Example:
  public record CreateOrderRequest(
      @NotNull(message = "User ID required") Long userId
  ) {
      public CreateOrderRequest {
          if (userId != null && userId <= 0) {
              throw new IllegalArgumentException("User ID must be positive");
          }
      }
  }

### 4. UseCase Only Dependencies
- Depend on UseCase interfaces ONLY
- NO repositories, NO entities, NO adapters
- Example:
  private final CreateOrderUseCase createOrderUseCase;  // ✅
  // ❌ NO: private final OrderRepository orderRepository;

### 5. NO Business Logic
- Controller should be thin, orchestration only
- Convert DTO → Command → UseCase → Result → Response
- Example:
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
      CreateOrderCommand command = request.toCommand();
      CreateOrderResult result = useCase.execute(command);
      return CreateOrderResponse.from(result);
  }

### 6. Example Request/Response Structure
  // CreateOrderRequest.java
  public record CreateOrderRequest(
      @NotNull Long userId,
      @NotEmpty List<OrderItemRequest> items
  ) {
      public CreateOrderRequest {
          if (userId <= 0) throw new IllegalArgumentException();
      }

      public CreateOrderCommand toCommand() {
          return new CreateOrderCommand(
              UserId.of(userId),
              items.stream().map(OrderItemRequest::toDomain).toList()
          );
      }
  }

  // CreateOrderResponse.java
  public record CreateOrderResponse(
      Long orderId,
      String status,
      LocalDateTime createdAt
  ) {
      public static CreateOrderResponse from(CreateOrderResult result) {
          return new CreateOrderResponse(
              result.orderId().value(),
              result.status().name(),
              result.createdAt()
          );
      }
  }

## 🧪 TEST COVERAGE
- Target: 70%+ coverage

REMEMBER: Keep controllers thin, DTOs as separate record files!
EOF
            ;;
    esac
fi

# ========================================
# 글로벌 리마인더 (모든 모듈)
# ========================================

cat << 'EOF'

# 🚨 GLOBAL ENTERPRISE STANDARDS

## 🚫 LOMBOK IS STRICTLY PROHIBITED
- NO @Data, @Builder, @Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor
- Use plain Java with manual getters/setters/constructors
- This is a ZERO TOLERANCE rule

## 📝 DOCUMENTATION REQUIREMENTS
- All public classes MUST have Javadoc
- MUST include: @author Name (email@company.com)
- MUST include: @since YYYY-MM-DD
- Public methods MUST have parameter/return documentation

## 🎯 SCOPE DISCIPLINE
- ONLY write code that is EXPLICITLY requested
- NO additional helper classes unless asked
- NO speculative features or "nice to have" additions
- If you add Utils/Helper/Manager classes, justify why

## ✅ VALIDATION
- Your code will be validated by:
  - ArchUnit tests (architecture rules)
  - Checkstyle (code style)
  - SpotBugs (bug detection)
  - Git pre-commit hooks
  - Dead code detector

## 💡 BEFORE WRITING CODE
1. Identify which module this belongs to (domain/application/adapter)
2. Follow module-specific rules above
3. Use pure Java (no Lombok)
4. Write tests (TDD preferred)
5. Add Javadoc with @author tag

Good luck! 🚀
EOF

# ========================================
# Exit Successfully (allow request)
# ========================================

exit 0
