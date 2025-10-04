# 🤖 Claude Code Dynamic Hooks

**Claude가 코드를 생성/수정할 때** 자동으로 실행되는 동적 훅 시스템입니다.

> ⚠️ **중요**: 이것은 **Claude Code 동적 훅**입니다. **Git Hooks**와는 다릅니다.
> - **Claude Hooks** (`.claude/hooks/`): Claude가 코드 생성/수정 시 실행 (이 문서)
> - **Git Hooks** (`hooks/`): `git commit` 실행 시 검증 ([문서](../../hooks/README.md))

---

## 📋 목차

- [개요](#개요)
- [훅 파일 설명](#훅-파일-설명)
- [실행 흐름](#실행-흐름)
- [모듈별 가이드라인](#모듈별-가이드라인)
- [검증 규칙](#검증-규칙)
- [커스터마이징](#커스터마이징)

---

## 🎯 개요

### 목적
Claude가 **코드를 생성하는 시점**에 헥사고날 아키텍처 규칙을 주입하여, 처음부터 올바른 코드를 생성하도록 유도합니다.

### 동작 방식
```
사용자 요청 → user-prompt-submit.sh → Claude 코드 생성 → after-tool-use.sh
     ↓               ↓ 가이드 주입              ↓               ↓ 실시간 검증
  "Order 클래스"   규칙 프롬프트 추가      코드 생성 완료      위반 감지 시 경고
```

### 주요 기능
- ✅ **사전 예방**: 요청 단계에서 규칙 가이드 주입
- ✅ **실시간 검증**: 코드 생성 직후 즉시 검증
- ✅ **모듈별 컨텍스트**: Domain, Application, Adapter 별 맞춤 가이드
- ✅ **경고 시스템**: 위반 발견 시 사용자에게 즉시 알림

---

## 📦 훅 파일 설명

### 1. `user-prompt-submit.sh`

**실행 시점**: 사용자가 Claude에게 요청을 제출할 때 (코드 생성 **전**)

**역할**: 요청을 분석하고 해당 모듈의 아키텍처 규칙을 프롬프트에 주입

**처리 과정**:
```bash
1. 사용자 요청 분석
   "Order 클래스를 domain에 만들어줘"

2. 모듈 컨텍스트 감지
   키워드: "domain" → MODULE_CONTEXT="domain"

3. 해당 모듈 가이드라인 주입
   Domain 규칙 프롬프트 추가

4. Claude에게 전달
   원래 요청 + 주입된 가이드라인
```

**모듈 감지 키워드**:
```bash
# Domain 모듈
"domain", "도메인", "비즈니스 로직"

# Application 모듈
"usecase", "application", "서비스", "유즈케이스"

# Persistence Adapter
"repository", "jpa", "database", "persistence", "entity"

# Controller Adapter
"controller", "rest", "api", "request", "response", "dto"
```

**주입되는 가이드라인 예시** (Domain):
```
# 🏛️ DOMAIN MODULE GUIDELINES

## ❌ ABSOLUTELY FORBIDDEN
- NO Spring Framework imports
- NO JPA/Hibernate imports
- NO Lombok imports
- NO infrastructure concerns

## ✅ REQUIRED
- Pure Java only
- Immutable objects (private final fields)
- Static factory methods (create, of, from)
- NO setters

## 📝 EXAMPLES
[Good vs Bad 코드 예시]
```

---

### 2. `after-tool-use.sh`

**실행 시점**: Claude가 코드를 생성/수정한 직후 (코드 생성 **후**)

**역할**: 생성된 코드를 즉시 검증하고 규칙 위반 시 경고

**처리 과정**:
```bash
1. 도구 사용 감지
   Read, Write, Edit 등 파일 작업 도구 사용 확인

2. 대상 파일 경로 분석
   domain/Order.java → Domain 모듈

3. 모듈별 검증 함수 실행
   validate_domain_layer()

4. 위반 발견 시 경고 출력
   ❌ VIOLATION: domain/Order.java contains Lombok import
```

**검증 레이어별 함수**:
- `validate_domain_layer()` - Domain 순수성 검증
- `validate_application_layer()` - Application 의존성 검증
- `validate_persistence_layer()` - Persistence 규칙 검증
- `validate_controller_layer()` - Controller DTO 규칙 검증

---

## 🔄 실행 흐름

### 전체 흐름도

```
┌──────────────────────────────────────────────────────────┐
│           사용자: "Order 엔티티 만들어줘"                   │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│          user-prompt-submit.sh 실행                       │
│  1. 요청 분석: "entity" 키워드 발견                         │
│  2. 컨텍스트: MODULE_CONTEXT="persistence"                │
│  3. Persistence Adapter 가이드라인 주입:                    │
│     - NO JPA relationships                               │
│     - Use Long foreign keys                              │
│     - NO setters, NO public constructors                 │
│     - Static factory methods required                    │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│              Claude가 코드 생성                             │
│  주입된 가이드라인을 참고하여:                                │
│  - Long userId 필드 사용 (NOT @ManyToOne)                 │
│  - protected 생성자 + static create()                     │
│  - NO setter 메서드                                       │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│           after-tool-use.sh 실행                          │
│  1. Write 도구 사용 감지                                   │
│  2. 파일 경로: adapter-out-persistence-jpa/OrderEntity.java│
│  3. validate_persistence_layer() 실행:                    │
│     ✅ NO JPA relationships                               │
│     ✅ NO setters                                         │
│     ✅ NO public constructor                              │
│     ✅ Static factory method exists                       │
│  4. 검증 통과 → 완료                                       │
└──────────────────────────────────────────────────────────┘
```

### 위반 발견 시 흐름

```
Claude 코드 생성
    ↓
after-tool-use.sh 검증
    ↓
위반 발견!
    ↓
┌─────────────────────────────────────────┐
│ ❌ PERSISTENCE VIOLATION:               │
│ OrderEntity.java contains @ManyToOne    │
│                                         │
│ ⚠️  POLICY:                             │
│ - NO JPA relationships                  │
│ - Use Long foreign keys instead         │
│ - See: CODING_STANDARDS.md              │
└─────────────────────────────────────────┘
    ↓
사용자에게 경고 표시 (코드는 생성됨)
    ↓
사용자가 수정하거나 Claude에게 재생성 요청
```

---

## 📚 모듈별 가이드라인

### Domain 모듈

**주입되는 규칙**:
```markdown
## ❌ ABSOLUTELY FORBIDDEN
- NO Spring Framework (org.springframework.*)
- NO JPA/Hibernate (jakarta.persistence.*)
- NO Lombok (@Data, @Builder, etc.)
- NO infrastructure concerns

## ✅ REQUIRED
- Pure Java only
- Immutable objects (private final fields, NO setters)
- Static factory methods (create, of, from, reconstitute)
- Business logic in domain objects

## 📝 PATTERN
public class Order {
    private final OrderId id;

    private Order(OrderId id) { ... }

    public static Order create(OrderId id) { ... }

    public Order confirm() {  // Returns new instance
        return new Order(this.id, OrderStatus.CONFIRMED);
    }
}
```

**검증 항목**:
- ❌ Spring/JPA/Lombok import 감지
- ❌ `@Component`, `@Entity` 등 어노테이션
- ⚠️ Jackson 직렬화 어노테이션

---

### Application 모듈

**주입되는 규칙**:
```markdown
## ❌ FORBIDDEN
- NO Adapter 직접 참조 (adapter.* import)
- NO Lombok

## ✅ REQUIRED
- Port 인터페이스 정의 (port.in.*, port.out.*)
- UseCase 인터페이스 구현
- @Transactional on UseCase implementations

## 📝 PATTERN
// Port definition
public interface CreateOrderUseCase {
    Order execute(CreateOrderCommand command);
}

// Implementation
@Service
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final LoadOrderPort loadOrderPort;  // Outbound port
    ...
}
```

**검증 항목**:
- ❌ `import com.company.template.adapter.*` 발견
- ❌ Lombok 사용
- ⚠️ UseCase 인터페이스 미구현
- ⚠️ @Transactional 누락

---

### Persistence Adapter

**주입되는 규칙**:
```markdown
## ❌ STRICTLY FORBIDDEN
- NO JPA relationships (@OneToMany, @ManyToOne, @OneToOne, @ManyToMany)
- NO setter methods in entities
- NO public constructors in entities
- NO @Transactional (belongs in Application layer)

## ✅ REQUIRED
- Use Long foreign key fields (userId, orderId, NOT @ManyToOne)
- Entity: protected constructor + static factory methods
- Mapper class for Entity ↔ Domain conversion

## 📝 PATTERN
@Entity
public class OrderEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;  // ✅ Long FK, NOT @ManyToOne User user

    protected OrderEntity() {}  // JPA only

    private OrderEntity(Long userId, ...) { ... }

    public static OrderEntity create(Long userId, ...) { ... }

    // ✅ Getter only, NO setters
    public Long getUserId() { return userId; }
}
```

**검증 항목**:
- ❌ JPA 관계 어노테이션 (`@OneToMany`, `@ManyToOne`)
- ❌ Setter 메서드 (`public void setXxx()`)
- ❌ Public 생성자
- ❌ `@Transactional` 어노테이션

---

### Controller Adapter

**주입되는 규칙**:
```markdown
## ❌ FORBIDDEN
- NO inner classes for Request/Response
- Request/Response must be Java records
- NO Repository/Entity dependencies

## ✅ REQUIRED
- Request/Response as separate record files
- Record compact constructor validation
- UseCase interface dependencies ONLY

## 📝 PATTERN
// Separate file: CreateOrderRequest.java
public record CreateOrderRequest(
    String orderId,
    int amount
) {
    // Compact constructor validation
    public CreateOrderRequest {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(orderId, amount);
    }
}

// Controller
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;  // UseCase only

    @PostMapping("/orders")
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.execute(request.toCommand());
        return OrderResponse.from(order);
    }
}
```

**검증 항목**:
- ❌ Controller 내부 클래스로 Request/Response 정의
- ❌ Request/Response가 class (record여야 함)
- ❌ Repository/Entity 직접 의존
- ⚠️ Request에 `toCommand()` 누락
- ⚠️ Response에 `from()` 누락

---

## 🔍 검증 규칙

### 실시간 검증 (after-tool-use.sh)

| 모듈 | 검증 항목 | 액션 |
|------|----------|------|
| Domain | Spring/JPA/Lombok import | ❌ 경고 |
| Application | Adapter import | ❌ 경고 |
| Persistence | JPA relationships | ❌ 경고 |
| Persistence | Setter methods | ❌ 경고 |
| Persistence | Public constructors | ❌ 경고 |
| Persistence | @Transactional | ❌ 경고 |
| Controller | Inner classes | ❌ 경고 |
| Controller | Non-record DTO | ❌ 경고 |
| Controller | Repository dependency | ❌ 경고 |

### 경고 vs 차단

**Dynamic Hook (이 시스템)**:
- ⚠️ **경고만 제공** (코드는 생성됨)
- 사용자가 수정하거나 Claude에게 재생성 요청 가능

**Git Hook** (`hooks/pre-commit`):
- ❌ **커밋 차단** (강제 수정 필요)
- 최종 안전망 역할

---

## 🛠️ 커스터마이징

### 새로운 모듈 컨텍스트 추가

**`user-prompt-submit.sh` 수정**:
```bash
# 새로운 모듈 감지 추가
elif echo "$USER_PROMPT" | grep -qi "batch\|스케줄러"; then
    MODULE_CONTEXT="batch"
fi

# 가이드라인 케이스 추가
case $MODULE_CONTEXT in
    batch)
        cat << 'EOF'
# 🔄 BATCH MODULE GUIDELINES
...
EOF
        ;;
esac
```

### 새로운 검증 규칙 추가

**`after-tool-use.sh` 수정**:
```bash
# 새로운 검증 함수 정의
validate_batch_layer() {
    local file="$1"

    if grep -q "@Scheduled" "$file"; then
        if ! grep -q "@Transactional" "$file"; then
            log_error "$file: @Scheduled must have @Transactional"
        fi
    fi
}

# 검증 라우팅에 추가
if [[ "$file" == *"batch"* ]]; then
    validate_batch_layer "$file"
fi
```

---

## 📚 관련 문서

- **[CODING_STANDARDS.md](../../docs/CODING_STANDARDS.md)** - 87개 코딩 규칙 전체
- **[DYNAMIC_HOOKS_GUIDE.md](../../docs/DYNAMIC_HOOKS_GUIDE.md)** - Dynamic Hook 상세 가이드
- **[Git Hooks README](../../hooks/README.md)** - Git Pre-commit Hook 문서

---

## 🎯 효과

### Before (동적 훅 없이)
```
사용자: "Order 엔티티 만들어줘"
    ↓
Claude: @OneToMany List<Item> items 생성
    ↓
git commit 시도
    ↓
❌ Pre-commit hook에서 차단
    ↓
사용자가 수동으로 수정 필요
```

### After (동적 훅 사용)
```
사용자: "Order 엔티티 만들어줘"
    ↓
user-prompt-submit.sh: Persistence 규칙 주입
    ↓
Claude: Long foreignKey 필드 생성 (규칙 준수)
    ↓
after-tool-use.sh: ✅ 검증 통과
    ↓
git commit 시도
    ↓
✅ Pre-commit hook 통과
```

**결과**: 처음부터 올바른 코드 생성 → 수정 불필요

---

**🎯 목표**: Claude가 아키텍처 규칙을 이해하고 준수하는 코드를 생성하도록 지속적으로 가이드

© 2024 Company Name. All Rights Reserved.
