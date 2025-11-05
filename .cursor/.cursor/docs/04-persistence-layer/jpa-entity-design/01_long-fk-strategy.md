# Long FK Strategy (Long 외래키 전략)

**목적**: JPA 관계 어노테이션 금지 및 Long FK 사용 전략

**위치**: `adapter-persistence/[module]/entity/`

**필수 버전**: Java 21+, Spring Boot 3.0+

---

## 🎯 핵심 원칙

**Zero-Tolerance 규칙**: JPA 관계 어노테이션 사용 금지

```java
// ❌ 금지
@OneToMany
@ManyToOne
@OneToOne
@ManyToMany
```

**해결책**: `Long` 타입 외래키 필드 사용

---

## 🚨 JPA 관계 어노테이션의 문제점

### 1. N+1 쿼리 문제

```java
// ❌ Bad - N+1 쿼리 발생
@Entity
public class OrderJpaEntity {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;  // Entity 참조
}

// 100개 주문 조회 시
List<OrderJpaEntity> orders = orderRepository.findAll();  // 쿼리 1개

// 각 주문의 사용자 정보 접근 시
for (OrderJpaEntity order : orders) {
    String userName = order.getUser().getName();  // 쿼리 100개 추가!
}
// 결과: 1 + 100 = 101개 쿼리 (N+1 문제)
```

### 2. Law of Demeter 위반

```java
// ❌ Bad - Getter 체이닝
String zip = order.getUser().getAddress().getZip();
// 3단계 체이닝 → Law of Demeter 위반
```

### 3. 순환 참조 및 복잡도 증가

```java
// ❌ Bad - 양방향 관계
@Entity
public class UserJpaEntity {
    @OneToMany(mappedBy = "user")
    private List<OrderJpaEntity> orders;  // 순환 참조 위험
}
```

---

## ✅ Long FK 전략

### 규칙: Entity 간 참조는 Long 타입 외래키만 사용

```java
// ✅ Good - Long FK
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Long FK (Entity 참조 없음)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    // Protected no-args constructor
    protected OrderJpaEntity() {
        super();
    }

    // Private constructor
    private OrderJpaEntity(Long userId, BigDecimal totalAmount, LocalDateTime createdAt) {
        super(createdAt, createdAt);
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    // Static factory
    public static OrderJpaEntity create(Long userId, BigDecimal totalAmount) {
        return new OrderJpaEntity(userId, totalAmount, LocalDateTime.now());
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }  // ✅ Long FK 반환
    public BigDecimal getTotalAmount() { return totalAmount; }
}
```

---

## 🔄 Application Layer에서 명시적 조회

### 패턴: UseCase에서 필요한 데이터만 명시적 로드

```java
/**
 * Order와 User를 함께 조회하는 UseCase
 */
@Service
@Transactional(readOnly = true)
public class GetOrderWithUserService implements GetOrderWithUserUseCase {

    private final LoadOrderPort loadOrderPort;
    private final LoadUserPort loadUserPort;
    private final OrderAssembler orderAssembler;

    @Override
    public OrderWithUserResponse execute(GetOrderQuery query) {
        // 1. Order 조회 (쿼리 1개)
        Order order = loadOrderPort.load(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));

        // 2. User 조회 (쿼리 1개, Long FK 사용)
        User user = loadUserPort.load(order.getUserId())
            .orElseThrow(() -> new UserNotFoundException(order.getUserId()));

        // 3. 조합 (메모리)
        return orderAssembler.toOrderWithUserResponse(order, user);
    }
}
// 총 2개 쿼리 (N+1 없음, 예측 가능)
```

---

## 📊 1:N 관계 처리

### 패턴: IN 절로 일괄 조회

```java
/**
 * 여러 Order와 각각의 User를 함께 조회
 */
@Service
@Transactional(readOnly = true)
public class GetOrdersWithUsersService implements GetOrdersWithUsersUseCase {

    @Override
    public List<OrderWithUserResponse> execute(GetOrdersQuery query) {
        // 1. Order 목록 조회 (쿼리 1개)
        List<Order> orders = loadOrdersPort.loadAll();

        // 2. User ID 추출
        List<UserId> userIds = orders.stream()
            .map(Order::getUserId)
            .distinct()
            .toList();

        // 3. User 일괄 조회 (쿼리 1개 - IN 절)
        List<User> users = loadUsersPort.loadByIds(userIds);

        // 4. Map으로 변환 (메모리)
        Map<UserId, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        // 5. 조합 (추가 쿼리 없음)
        return orders.stream()
            .map(order -> OrderWithUserResponse.of(
                order,
                userMap.get(order.getUserId())
            ))
            .toList();
    }
}
// 총 2개 쿼리 (100개 Order도 2개 쿼리로 해결)
```

### Repository 구현 (IN 절)

```java
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    // ✅ IN 절로 일괄 조회
    @Query("SELECT u FROM UserJpaEntity u WHERE u.id IN :ids AND u.deletedAt IS NULL")
    List<UserJpaEntity> findAllByIdIn(@Param("ids") List<Long> ids);
}

@Component
public class UserJpaAdapter implements LoadUsersPort {

    @Override
    public List<User> loadByIds(List<UserId> userIds) {
        List<Long> ids = userIds.stream()
            .map(UserId::getValue)
            .toList();

        // ✅ IN 절로 한 번에 조회
        List<UserJpaEntity> entities = userRepository.findAllByIdIn(ids);

        return entities.stream()
            .map(UserMapper::toDomain)
            .toList();
    }
}
```

---

## ⚖️ 성능 비교

| 전략 | 쿼리 수 (100개 Order) | N+1 문제 | Law of Demeter | 예측 가능성 |
|------|----------------------|----------|----------------|-------------|
| **JPA 관계** | 1 + 100 = 101개 | ❌ 발생 | ❌ 위반 | ❌ 불가 |
| **Long FK + IN** | 1 + 1 = 2개 | ✅ 차단 | ✅ 준수 | ✅ 가능 |

**성능 개선**: 98% 쿼리 감소 (101 → 2)

---

## 📋 체크리스트

Entity 작성 시:
- [ ] JPA 관계 어노테이션 없음 (`@OneToMany`, `@ManyToOne` 등)
- [ ] Long FK 사용 (`private Long userId;`)
- [ ] Application Layer에서 명시적 조회
- [ ] IN 절로 일괄 조회 구현
- [ ] N+1 쿼리 없음 확인
- [ ] Law of Demeter 준수 확인
- [ ] 쿼리 수 예측 가능 (2-3개 이내)

---

## 📖 관련 문서

- **[Core Rules](./00_jpa-entity-core-rules.md)** - JPA Entity 핵심 설계 규칙
- **[Constructor Pattern](./02_constructor-pattern.md)** - 3-Tier Constructor 패턴

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
