# JPA Entity 생성자 및 팩토리 메서드 패턴

## 목차
1. [개요](#개요)
2. [Three-Tier Constructor 전략](#three-tier-constructor-전략)
3. [Static Factory Method 패턴](#static-factory-method-패턴)
4. [ArchUnit 자동 검증](#archunit-자동-검증)
5. [실전 예제](#실전-예제)

---

## 개요

### 핵심 원칙
JPA Entity는 **Three-Tier Constructor + Static Factory Method** 패턴을 사용하여:
1. **의도를 명확히 표현** (create vs reconstitute)
2. **JPA 요구사항 충족** (no-args constructor)
3. **불변성 보장** (private reconstitute constructor)

### Zero-Tolerance 규칙
- ✅ **MUST**: Protected no-args constructor (JPA)
- ✅ **MUST**: Protected create constructor (without ID)
- ✅ **MUST**: Private reconstitute constructor (with ID)
- ✅ **MUST**: `create()` static factory method
- ✅ **MUST**: `reconstitute()` static factory method
- ❌ **NEVER**: Public constructors
- ❌ **NEVER**: Setter methods
- ❌ **NEVER**: Lombok `@Builder`, `@AllArgsConstructor`

---

## Three-Tier Constructor 전략

### 1단계: Protected No-Args Constructor (JPA 요구사항)

**목적**: JPA가 리플렉션으로 객체를 생성할 수 있도록 허용

```java
/**
 * JPA 전용 기본 생성자
 *
 * <p><strong>주의:</strong></p>
 * <ul>
 *   <li>JPA가 리플렉션으로 호출하므로 {@code protected}로 선언</li>
 *   <li>애플리케이션 코드에서 직접 사용 금지</li>
 *   <li>반드시 {@code super()} 호출 필요 (BaseAuditEntity)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
protected TenantJpaEntity() {
    super();  // BaseAuditEntity() 호출
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 protected no-args 생성자를 가져야 함")
void jpaEntityShouldHaveProtectedNoArgsConstructor() {
    noClasses()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should(new ArchCondition<JavaClass>("have protected no-args constructor") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasProtectedNoArgsConstructor = javaClass.getConstructors().stream()
                    .anyMatch(constructor ->
                        constructor.getModifiers().contains(JavaModifier.PROTECTED)
                        && constructor.getParameters().isEmpty()
                    );

                if (!hasProtectedNoArgsConstructor) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("Class %s does not have protected no-args constructor",
                            javaClass.getName())
                    ));
                }
            }
        })
        .check(persistenceClasses);
}
```

---

### 2단계: Protected Create Constructor (ID 없음)

**목적**: 새로운 엔티티 생성 시 사용 (ID는 DB가 자동 생성)

```java
/**
 * Create용 생성자 - 새로운 Tenant 생성 시 사용
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>ID는 포함하지 않음 (DB가 {@code @GeneratedValue}로 생성)</li>
 *   <li>{@code protected} 접근 제어 (static factory method만 사용)</li>
 *   <li>필수 필드만 파라미터로 받음</li>
 *   <li>기본값은 생성자 내부에서 설정 (예: {@code deleted = false})</li>
 * </ul>
 *
 * @param name 테넌트 이름 (필수)
 * @param status 테넌트 상태 (필수)
 * @param createdAt 생성 시각 (필수)
 * @param updatedAt 수정 시각 (필수)
 * @param deleted 삭제 여부 (필수)
 * @author ryu-qqq
 * @since 2025-10-23
 */
protected TenantJpaEntity(
    String name,
    TenantStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted
) {
    super(createdAt, updatedAt);  // BaseAuditEntity 초기화
    this.name = name;
    this.status = status;
    this.deleted = deleted;
}
```

**핵심 포인트**:
- **ID 제외**: 새 엔티티는 ID가 없어야 함 (DB가 생성)
- **필수 필드만**: Optional 필드는 기본값 설정
- **불변성**: 생성 후 수정 불가 (setter 없음)

---

### 3단계: Private Reconstitute Constructor (ID 포함)

**목적**: DB에서 로드한 엔티티를 재구성 시 사용

```java
/**
 * Reconstitute용 생성자 - DB에서 로드한 데이터로 객체 재구성 시 사용
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>ID를 포함한 모든 필드를 파라미터로 받음</li>
 *   <li>{@code private} 접근 제어 (reconstitute() static method만 사용)</li>
 *   <li>DB 상태를 그대로 복원 (validation 없음)</li>
 *   <li>Domain layer로 변환 시 사용 (Mapper에서 호출)</li>
 * </ul>
 *
 * @param id 테넌트 ID (DB PK)
 * @param name 테넌트 이름
 * @param status 테넌트 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param deleted 삭제 여부
 * @author ryu-qqq
 * @since 2025-10-23
 */
private TenantJpaEntity(
    Long id,
    String name,
    TenantStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted
) {
    super(createdAt, updatedAt);
    this.id = id;
    this.name = name;
    this.status = status;
    this.deleted = deleted;
}
```

**핵심 포인트**:
- **ID 포함**: DB에서 로드한 엔티티는 반드시 ID 보유
- **Private 접근**: 외부에서 직접 호출 불가 (reconstitute() 통해서만)
- **Validation 없음**: DB 상태를 그대로 신뢰
- **Mapper 전용**: EntityMapper에서 Domain 변환 시 사용

---

## Static Factory Method 패턴

### 1. create() - 새로운 엔티티 생성

**목적**: 애플리케이션에서 새 엔티티를 생성할 때 사용

```java
/**
 * 새로운 Tenant 생성 (Static Factory Method)
 *
 * <p><strong>사용 시점:</strong></p>
 * <ul>
 *   <li>애플리케이션에서 새로운 Tenant를 생성할 때</li>
 *   <li>Command 처리 중 신규 엔티티 생성 시</li>
 *   <li>Test Fixture에서 신규 엔티티 생성 시</li>
 * </ul>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>ID는 {@code null} (DB가 {@code @GeneratedValue}로 생성)</li>
 *   <li>필수 필드만 파라미터로 받음</li>
 *   <li>기본값 자동 설정 (예: {@code status = ACTIVE}, {@code deleted = false})</li>
 *   <li>Validation 수행 (null check, business rule)</li>
 * </ul>
 *
 * <p><strong>예외:</strong></p>
 * <ul>
 *   <li>{@link IllegalArgumentException} - 필수 필드가 null인 경우</li>
 * </ul>
 *
 * @param name 테넌트 이름 (필수, null 불가)
 * @param createdAt 생성 시각 (필수, null 불가)
 * @return 새로운 TenantJpaEntity 인스턴스
 * @throws IllegalArgumentException 필수 필드가 null인 경우
 * @author ryu-qqq
 * @since 2025-10-23
 */
public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
    // 1. Validation: 필수 필드 null check
    if (name == null || createdAt == null) {
        throw new IllegalArgumentException("Required fields must not be null");
    }

    // 2. 기본값 설정
    TenantStatus defaultStatus = TenantStatus.ACTIVE;
    boolean defaultDeleted = false;

    // 3. Protected constructor 호출
    return new TenantJpaEntity(
        name,
        defaultStatus,
        createdAt,
        createdAt,  // updatedAt = createdAt (최초 생성)
        defaultDeleted
    );
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 create() static factory method를 가져야 함")
void jpaEntityShouldHaveCreateStaticFactoryMethod() {
    classes()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should(ArchCondition.from(new ArchCondition<JavaClass>("have create() static factory method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasCreateMethod = javaClass.getMethods().stream()
                    .anyMatch(method ->
                        method.getName().equals("create")
                        && method.getModifiers().contains(JavaModifier.STATIC)
                        && method.getModifiers().contains(JavaModifier.PUBLIC)
                    );

                if (!hasCreateMethod) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("Class %s does not have create() static factory method",
                            javaClass.getName())
                    ));
                }
            }
        }))
        .check(persistenceClasses);
}
```

---

### 2. reconstitute() - DB 엔티티 재구성

**목적**: DB에서 로드한 데이터로 엔티티를 재구성할 때 사용

```java
/**
 * DB에서 로드한 데이터로 Tenant 재구성 (Static Factory Method)
 *
 * <p><strong>사용 시점:</strong></p>
 * <ul>
 *   <li>QueryDSL 조회 결과를 Entity로 변환할 때</li>
 *   <li>Mapper에서 JPA Entity → Domain 변환 시</li>
 *   <li>Test Fixture에서 DB 로드 상태 재현 시</li>
 * </ul>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>ID를 포함한 모든 필드를 파라미터로 받음</li>
 *   <li>Validation 수행하지 않음 (DB 상태를 신뢰)</li>
 *   <li>Private reconstitute constructor 호출</li>
 *   <li>불변 객체 생성 (setter 없음)</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>애플리케이션 코드에서 직접 사용 금지</li>
 *   <li>Mapper layer에서만 사용</li>
 *   <li>Test Fixture에서는 사용 가능</li>
 * </ul>
 *
 * @param id 테넌트 ID (DB PK, null 가능 - JPA가 관리)
 * @param name 테넌트 이름
 * @param status 테넌트 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param deleted 삭제 여부
 * @return DB 상태로 재구성된 TenantJpaEntity 인스턴스
 * @author ryu-qqq
 * @since 2025-10-23
 */
public static TenantJpaEntity reconstitute(
    Long id,
    String name,
    TenantStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted
) {
    // Private reconstitute constructor 호출
    // Validation 없음 (DB 상태를 그대로 신뢰)
    return new TenantJpaEntity(id, name, status, createdAt, updatedAt, deleted);
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 reconstitute() static factory method를 가져야 함")
void jpaEntityShouldHaveReconstituteStaticFactoryMethod() {
    classes()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should(ArchCondition.from(new ArchCondition<JavaClass>("have reconstitute() static factory method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasReconstituteMethod = javaClass.getMethods().stream()
                    .anyMatch(method ->
                        method.getName().equals("reconstitute")
                        && method.getModifiers().contains(JavaModifier.STATIC)
                        && method.getModifiers().contains(JavaModifier.PUBLIC)
                    );

                if (!hasReconstituteMethod) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("Class %s does not have reconstitute() static factory method",
                            javaClass.getName())
                    ));
                }
            }
        }))
        .check(persistenceClasses);
}
```

---

## ArchUnit 자동 검증

### 전체 컨벤션 검증 테스트

`JpaEntityConventionTest.java`에서 모든 생성자 및 팩토리 메서드 규칙을 자동 검증합니다:

```java
@DisplayName("JPA 엔티티 컨벤션 검증")
class JpaEntityConventionTest {

    @Nested
    @DisplayName("생성자 및 팩토리 메서드 규칙")
    class ConstructorAndFactoryMethodRules {

        @Test
        @DisplayName("JPA Entity는 protected no-args 생성자를 가져야 함")
        void jpaEntityShouldHaveProtectedNoArgsConstructor() {
            // ... (위 코드 참조)
        }

        @Test
        @DisplayName("JPA Entity는 create() static factory method를 가져야 함")
        void jpaEntityShouldHaveCreateStaticFactoryMethod() {
            // ... (위 코드 참조)
        }

        @Test
        @DisplayName("JPA Entity는 reconstitute() static factory method를 가져야 함")
        void jpaEntityShouldHaveReconstituteStaticFactoryMethod() {
            // ... (위 코드 참조)
        }

        @Test
        @DisplayName("JPA Entity는 public 생성자를 가지면 안 됨")
        void jpaEntityShouldNotHavePublicConstructor() {
            noClasses()
                .that().resideInAPackage("..persistence..")
                .and().areAnnotatedWith(Entity.class)
                .should(new ArchCondition<JavaClass>("not have public constructor") {
                    @Override
                    public void check(JavaClass javaClass, ConditionEvents events) {
                        boolean hasPublicConstructor = javaClass.getConstructors().stream()
                            .anyMatch(constructor ->
                                constructor.getModifiers().contains(JavaModifier.PUBLIC)
                            );

                        if (hasPublicConstructor) {
                            events.add(SimpleConditionEvent.violated(
                                javaClass,
                                String.format("Class %s has public constructor (use static factory methods instead)",
                                    javaClass.getName())
                            ));
                        }
                    }
                })
                .check(persistenceClasses);
        }
    }
}
```

### 실행 방법

```bash
# JPA Entity 컨벤션 검증
./gradlew :bootstrap:bootstrap-web-api:test --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest"

# 전체 아키텍처 검증
./gradlew :bootstrap:bootstrap-web-api:test --tests "com.ryuqq.bootstrap.architecture.*"
```

---

## 실전 예제

### 예제 1: 올바른 JPA Entity 구조

```java
package com.ryuqq.adapter.out.persistence.tenant.entity;

import com.ryuqq.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TenantJpaEntity - Tenant 정보를 저장하는 JPA Entity
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Three-Tier Constructor Pattern (protected no-args, protected create, private reconstitute)</li>
 *   <li>✅ Static Factory Methods (create, reconstitute)</li>
 *   <li>✅ BaseAuditEntity 상속 (createdAt, updatedAt)</li>
 *   <li>✅ Long FK Strategy (관계 어노테이션 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TenantStatus status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    // ========================================
    // Constructors (Three-Tier Pattern)
    // ========================================

    /**
     * JPA 전용 기본 생성자
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    protected TenantJpaEntity() {
        super();
    }

    /**
     * Create용 생성자 (ID 없음)
     *
     * @param name 테넌트 이름
     * @param status 테넌트 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-23
     */
    protected TenantJpaEntity(
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.name = name;
        this.status = status;
        this.deleted = deleted;
    }

    /**
     * Reconstitute용 생성자 (ID 포함)
     *
     * @param id 테넌트 ID
     * @param name 테넌트 이름
     * @param status 테넌트 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private TenantJpaEntity(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.name = name;
        this.status = status;
        this.deleted = deleted;
    }

    // ========================================
    // Static Factory Methods
    // ========================================

    /**
     * 새로운 Tenant 생성
     *
     * @param name 테넌트 이름
     * @param createdAt 생성 시각
     * @return 새로운 TenantJpaEntity 인스턴스
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
        if (name == null || createdAt == null) {
            throw new IllegalArgumentException("Required fields must not be null");
        }

        return new TenantJpaEntity(
            name,
            TenantStatus.ACTIVE,
            createdAt,
            createdAt,
            false
        );
    }

    /**
     * DB에서 로드한 데이터로 Tenant 재구성
     *
     * @param id 테넌트 ID
     * @param name 테넌트 이름
     * @param status 테넌트 상태
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param deleted 삭제 여부
     * @return DB 상태로 재구성된 TenantJpaEntity 인스턴스
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity reconstitute(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new TenantJpaEntity(id, name, status, createdAt, updatedAt, deleted);
    }

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Tenant 활성화
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();
    }

    /**
     * Tenant 비활성화
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public void deactivate() {
        this.status = TenantStatus.INACTIVE;
        markAsUpdated();
    }

    /**
     * Tenant 소프트 삭제
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public void softDelete() {
        this.deleted = true;
        markAsUpdated();
    }

    // ========================================
    // Getters (No Setters)
    // ========================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
```

---

### 예제 2: 잘못된 Entity 구조 (Anti-Pattern)

```java
// ❌ WRONG: Lombok 사용
@Entity
@Table(name = "tenant")
@Data  // ❌ Lombok 금지!
@Builder  // ❌ Builder 금지!
@NoArgsConstructor  // ❌ 접근 제어 명시 불가
@AllArgsConstructor  // ❌ Public constructor 생성됨
public class TenantJpaEntity extends BaseAuditEntity {
    // ...
}

// ❌ WRONG: Public constructor
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    // ❌ Public constructor (외부에서 직접 생성 가능)
    public TenantJpaEntity(String name, TenantStatus status) {
        this.name = name;
        this.status = status;
    }
}

// ❌ WRONG: Static factory method 없음
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    protected TenantJpaEntity() {
        super();
    }

    // ❌ Static factory method 없음 (create, reconstitute)
    // 의도가 불명확함
}

// ❌ WRONG: Setter 사용
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    private String name;

    // ❌ Setter 금지! (불변성 위반)
    public void setName(String name) {
        this.name = name;
    }
}
```

---

## 체크리스트

생성자 및 팩토리 메서드 패턴을 올바르게 구현했는지 확인하세요:

- [ ] ✅ Protected no-args constructor (JPA)
- [ ] ✅ Protected create constructor (ID 없음)
- [ ] ✅ Private reconstitute constructor (ID 포함)
- [ ] ✅ `create()` static factory method
- [ ] ✅ `reconstitute()` static factory method
- [ ] ❌ Public constructor 없음
- [ ] ❌ Setter 메서드 없음
- [ ] ❌ Lombok `@Builder`, `@AllArgsConstructor` 없음
- [ ] ✅ BaseAuditEntity 상속
- [ ] ✅ 모든 public 메서드에 Javadoc 포함

---

## 참고 자료

- [Long FK Strategy](./01_long-fk-strategy.md) - JPA 관계 어노테이션 금지
- [Entity Immutability](./02_entity-immutability.md) - Setter 금지 원칙
- [Audit Entity Pattern](./05_audit-entity-pattern.md) - BaseAuditEntity 설계
- [JpaEntityConventionTest.java](../../../../bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/JpaEntityConventionTest.java) - ArchUnit 자동 검증

---

**✅ 이 패턴을 따르면 JpaEntityConventionTest가 자동으로 통과합니다!**
