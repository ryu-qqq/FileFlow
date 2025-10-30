# JPA Entity에서 Lombok 사용 금지

## 목차
1. [개요](#개요)
2. [금지된 Lombok 어노테이션](#금지된-lombok-어노테이션)
3. [Lombok 금지 이유](#lombok-금지-이유)
4. [ArchUnit 자동 검증](#archunit-자동-검증)
5. [대체 방법](#대체-방법)

---

## 개요

### Zero-Tolerance 규칙
JPA Entity에서는 **모든 Lombok 어노테이션이 엄격히 금지**됩니다.

**이유**:
1. **의도 불명확**: 생성자의 목적(create vs reconstitute)을 코드로 명시 불가
2. **불변성 위반**: Setter가 자동 생성되어 Entity 불변성 보장 불가
3. **JPA 충돌**: Lazy Loading, Proxy 객체와 충돌 가능
4. **디버깅 어려움**: 자동 생성 코드로 인한 스택 트레이스 복잡도 증가
5. **ArchUnit 검증 불가**: 생성자 패턴, Setter 금지 등 규칙 검증 어려움

---

## 금지된 Lombok 어노테이션

### 1. @Data (가장 위험)

❌ **절대 사용 금지**

```java
// ❌ WRONG: @Data 사용
@Entity
@Table(name = "tenant")
@Data  // ❌ 다음 항목들을 자동 생성:
       // - @Getter (모든 필드)
       // - @Setter (모든 필드) ← 불변성 위반!
       // - @ToString (순환 참조 가능)
       // - @EqualsAndHashCode (JPA Entity에 부적합)
       // - @RequiredArgsConstructor ← 의도 불명확!
public class TenantJpaEntity extends BaseAuditEntity {
    private Long id;
    private String name;
    private TenantStatus status;
}
```

**문제점**:
- Setter 자동 생성 → Entity 불변성 완전 파괴
- RequiredArgsConstructor → create vs reconstitute 구분 불가
- EqualsAndHashCode → JPA Proxy 객체와 충돌
- ToString → Lazy Loading 필드 접근 시 N+1 문제 발생

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 @Data 어노테이션을 사용하면 안 됨")
void jpaEntityShouldNotUseDataAnnotation() {
    noClasses()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should().beAnnotatedWith(Data.class)
        .check(persistenceClasses);
}
```

---

### 2. @Getter (전역 사용 금지)

❌ **클래스 레벨 사용 금지** (필드 레벨은 허용하지만 권장하지 않음)

```java
// ❌ WRONG: 클래스 레벨 @Getter
@Entity
@Table(name = "tenant")
@Getter  // ❌ 모든 필드에 Getter 자동 생성
public class TenantJpaEntity extends BaseAuditEntity {
    private Long id;
    private String name;
    private TenantStatus status;

    // Lazy Loading 필드도 Getter 생성됨
    // → N+1 문제 유발 가능
}
```

**문제점**:
- 모든 필드에 무차별적으로 Getter 생성
- 내부 필드까지 노출 (캡슐화 위반)
- Lazy Loading 필드 접근 시 의도치 않은 쿼리 발생

**올바른 방법**:
```java
// ✅ CORRECT: 필요한 Getter만 수동 작성
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    private Long id;
    private String name;
    private TenantStatus status;

    // 필요한 Getter만 명시적으로 작성
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    // 내부 필드는 Getter 제공하지 않음 (캡슐화)
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 클래스 레벨 @Getter 어노테이션을 사용하면 안 됨")
void jpaEntityShouldNotUseGetterAnnotation() {
    noClasses()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should().beAnnotatedWith(Getter.class)
        .check(persistenceClasses);
}
```

---

### 3. @Setter (절대 금지)

❌ **클래스/필드 레벨 모두 금지**

```java
// ❌ WRONG: @Setter 사용
@Entity
@Table(name = "tenant")
@Setter  // ❌ 모든 필드에 Setter 자동 생성
public class TenantJpaEntity extends BaseAuditEntity {
    private Long id;
    private String name;
    private TenantStatus status;

    // Setter 자동 생성됨:
    // public void setId(Long id) { this.id = id; }
    // public void setName(String name) { this.name = name; }
    // ← Entity 불변성 완전 파괴!
}
```

**문제점**:
- Entity 불변성 완전 파괴
- 비즈니스 로직 우회 가능 (validation 없이 상태 변경)
- Audit 필드(createdAt, updatedAt) 외부 수정 가능 (심각한 보안 문제)

**올바른 방법**:
```java
// ✅ CORRECT: 비즈니스 메서드로 상태 변경
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    private String name;
    private TenantStatus status;

    // Setter 대신 비즈니스 메서드 제공
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();  // Audit 필드 자동 업데이트
    }

    public void deactivate() {
        this.status = TenantStatus.INACTIVE;
        markAsUpdated();
    }

    // ❌ Setter 없음!
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 클래스 레벨 @Setter 어노테이션을 사용하면 안 됨")
void jpaEntityShouldNotUseSetterAnnotation() {
    noClasses()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should().beAnnotatedWith(Setter.class)
        .check(persistenceClasses);
}
```

---

### 4. @Builder (금지)

❌ **절대 사용 금지**

```java
// ❌ WRONG: @Builder 사용
@Entity
@Table(name = "tenant")
@Builder  // ❌ Builder 패턴 자동 생성
public class TenantJpaEntity extends BaseAuditEntity {
    private Long id;
    private String name;
    private TenantStatus status;
}

// 사용 예시:
TenantJpaEntity tenant = TenantJpaEntity.builder()
    .id(1L)  // ❌ ID를 수동 설정 가능 (DB PK 충돌!)
    .name("test")
    .status(TenantStatus.ACTIVE)
    .build();  // ❌ 의도 불명확 (create? reconstitute?)
```

**문제점**:
- create vs reconstitute 의도 구분 불가
- ID를 수동으로 설정 가능 (DB PK 충돌 위험)
- 필수 필드 누락 가능 (컴파일 타임 검증 불가)
- BaseAuditEntity 필드 초기화 누락 가능

**올바른 방법**:
```java
// ✅ CORRECT: Static Factory Method 사용
@Entity
@Table(name = "tenant")
public class TenantJpaEntity extends BaseAuditEntity {

    // create: ID 없이 생성 (의도 명확)
    public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
        if (name == null || createdAt == null) {
            throw new IllegalArgumentException("Required fields must not be null");
        }
        return new TenantJpaEntity(name, TenantStatus.ACTIVE, createdAt, createdAt, false);
    }

    // reconstitute: ID 포함 재구성 (의도 명확)
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
}
```

**ArchUnit 검증**:
```java
@Test
@DisplayName("JPA Entity는 @Builder 어노테이션을 사용하면 안 됨")
void jpaEntityShouldNotUseBuilderAnnotation() {
    noClasses()
        .that().resideInAPackage("..persistence..")
        .and().areAnnotatedWith(Entity.class)
        .should().beAnnotatedWith(Builder.class)
        .check(persistenceClasses);
}
```

---

### 5. 기타 금지 어노테이션

#### @NoArgsConstructor
❌ **클래스 레벨 금지** (JPA no-args constructor는 수동 작성)

```java
// ❌ WRONG: @NoArgsConstructor 사용
@Entity
@NoArgsConstructor  // ❌ 접근 제어 명시 불가
public class TenantJpaEntity {
    // Public no-args constructor 생성됨
}

// ✅ CORRECT: 수동 작성
@Entity
public class TenantJpaEntity {
    protected TenantJpaEntity() {  // Protected 명시
        super();
    }
}
```

#### @AllArgsConstructor
❌ **절대 금지** (의도 불명확)

```java
// ❌ WRONG: @AllArgsConstructor 사용
@Entity
@AllArgsConstructor  // ❌ 모든 필드를 파라미터로 받는 Public constructor 생성
public class TenantJpaEntity {
    private Long id;
    private String name;
    // ...
}

// ✅ CORRECT: Three-Tier Constructor Pattern
@Entity
public class TenantJpaEntity {
    protected TenantJpaEntity() {}  // JPA
    protected TenantJpaEntity(...) {}  // Create (ID 없음)
    private TenantJpaEntity(...) {}  // Reconstitute (ID 포함)
}
```

#### @RequiredArgsConstructor
❌ **절대 금지** (의도 불명확)

#### @ToString
❌ **절대 금지** (Lazy Loading N+1 문제)

#### @EqualsAndHashCode
❌ **절대 금지** (JPA Proxy 객체 충돌)

---

## Lombok 금지 이유

### 1. 의도 불명확 (Intent Clarity)

**문제**: Lombok 생성자는 용도를 코드로 표현할 수 없음

```java
// ❌ Lombok: 의도 불명확
@AllArgsConstructor
public class TenantJpaEntity {
    // 이 생성자가 create용? reconstitute용?
    // ID를 포함해야 하나? 제외해야 하나?
}

// ✅ Static Factory: 의도 명확
public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
    // "create" → 새로운 엔티티 생성 (ID 없음)
}

public static TenantJpaEntity reconstitute(Long id, String name, ...) {
    // "reconstitute" → DB에서 로드 (ID 포함)
}
```

---

### 2. 불변성 보장 불가 (Immutability)

**문제**: Lombok Setter는 비즈니스 로직을 우회함

```java
// ❌ Lombok Setter: 불변성 파괴
@Setter
public class TenantJpaEntity {
    private TenantStatus status;
    private LocalDateTime updatedAt;
}

// 외부에서 상태 직접 변경 가능
tenant.setStatus(TenantStatus.ACTIVE);  // ❌ Validation 없음!
tenant.setUpdatedAt(LocalDateTime.now());  // ❌ 수동 업데이트!

// ✅ 비즈니스 메서드: 불변성 보장
public class TenantJpaEntity {
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();  // Audit 필드 자동 업데이트
    }
}
```

---

### 3. JPA Proxy 객체 충돌 (JPA Compatibility)

**문제**: Lombok `@ToString`, `@EqualsAndHashCode`는 JPA Proxy와 충돌

```java
// ❌ Lombok: Lazy Loading 필드 접근 시 N+1 문제
@ToString
public class TenantJpaEntity {
    // toString() 호출 시 모든 필드 접근
    // → Lazy Loading 필드까지 쿼리 발생!
}

// ❌ Lombok: Proxy 객체 equals 오작동
@EqualsAndHashCode
public class TenantJpaEntity {
    // Proxy 객체와 실제 객체 비교 시 오작동
}
```

---

### 4. ArchUnit 검증 어려움 (Testing)

**문제**: Lombok 자동 생성 코드는 ArchUnit 검증 불가

```java
// ❌ Lombok: ArchUnit 검증 불가
@AllArgsConstructor
public class TenantJpaEntity {
    // 생성자가 protected인지 private인지 검증 불가!
}

// ✅ 수동 작성: ArchUnit 검증 가능
public class TenantJpaEntity {
    protected TenantJpaEntity() {}  // ← ArchUnit이 접근 제어 검증 가능
    private TenantJpaEntity(...) {}  // ← ArchUnit이 패턴 검증 가능
}
```

---

## ArchUnit 자동 검증

### 전체 Lombok 어노테이션 검증

`JpaEntityConventionTest.java`에서 모든 Lombok 어노테이션을 자동 검증:

```java
@Nested
@DisplayName("Lombok 금지 규칙")
class LombokProhibitionRules {

    @Test
    @DisplayName("JPA Entity는 @Data 어노테이션을 사용하면 안 됨")
    void jpaEntityShouldNotUseDataAnnotation() {
        noClasses()
            .that().resideInAPackage("..persistence..")
            .and().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Data.class)
            .check(persistenceClasses);
    }

    @Test
    @DisplayName("JPA Entity는 클래스 레벨 @Getter 어노테이션을 사용하면 안 됨")
    void jpaEntityShouldNotUseGetterAnnotation() {
        noClasses()
            .that().resideInAPackage("..persistence..")
            .and().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Getter.class)
            .check(persistenceClasses);
    }

    @Test
    @DisplayName("JPA Entity는 클래스 레벨 @Setter 어노테이션을 사용하면 안 됨")
    void jpaEntityShouldNotUseSetterAnnotation() {
        noClasses()
            .that().resideInAPackage("..persistence..")
            .and().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Setter.class)
            .check(persistenceClasses);
    }

    @Test
    @DisplayName("JPA Entity는 @Builder 어노테이션을 사용하면 안 됨")
    void jpaEntityShouldNotUseBuilderAnnotation() {
        noClasses()
            .that().resideInAPackage("..persistence..")
            .and().areAnnotatedWith(Entity.class)
            .should().beAnnotatedWith(Builder.class)
            .check(persistenceClasses);
    }
}
```

### 실행 방법

```bash
# Lombok 금지 규칙 검증
./gradlew :bootstrap:bootstrap-web-api:test \
  --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest.LombokProhibitionRules"

# 전체 JPA Entity 컨벤션 검증
./gradlew :bootstrap:bootstrap-web-api:test \
  --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest"
```

---

## 대체 방법

### Lombok → Pure Java 변환 가이드

#### 1. @Data 제거

```java
// BEFORE: @Data 사용
@Entity
@Data
public class TenantJpaEntity extends BaseAuditEntity {
    private Long id;
    private String name;
    private TenantStatus status;
}

// AFTER: Pure Java
@Entity
public class TenantJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status;

    // Three-Tier Constructor Pattern
    protected TenantJpaEntity() {
        super();
    }

    protected TenantJpaEntity(String name, TenantStatus status, LocalDateTime createdAt) {
        super(createdAt, createdAt);
        this.name = name;
        this.status = status;
    }

    private TenantJpaEntity(Long id, String name, TenantStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.name = name;
        this.status = status;
    }

    // Static Factory Methods
    public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
        return new TenantJpaEntity(name, TenantStatus.ACTIVE, createdAt);
    }

    public static TenantJpaEntity reconstitute(Long id, String name, TenantStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new TenantJpaEntity(id, name, status, createdAt, updatedAt);
    }

    // Getters only (No Setters)
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    // Business Methods (instead of Setters)
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();
    }

    public void deactivate() {
        this.status = TenantStatus.INACTIVE;
        markAsUpdated();
    }
}
```

---

#### 2. @Builder 제거

```java
// BEFORE: @Builder 사용
@Entity
@Builder
@AllArgsConstructor
public class TenantJpaEntity {
    private Long id;
    private String name;
}

// 사용:
TenantJpaEntity tenant = TenantJpaEntity.builder()
    .id(1L)
    .name("test")
    .build();

// AFTER: Static Factory Methods
@Entity
public class TenantJpaEntity {

    private Long id;
    private String name;

    protected TenantJpaEntity() {}

    protected TenantJpaEntity(String name) {
        this.name = name;
    }

    private TenantJpaEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TenantJpaEntity create(String name) {
        return new TenantJpaEntity(name);
    }

    public static TenantJpaEntity reconstitute(Long id, String name) {
        return new TenantJpaEntity(id, name);
    }
}

// 사용:
TenantJpaEntity tenant = TenantJpaEntity.create("test");  // 의도 명확!
```

---

#### 3. @Setter 제거

```java
// BEFORE: @Setter 사용
@Entity
@Setter
public class TenantJpaEntity {
    private String name;
    private TenantStatus status;
}

// 사용:
tenant.setStatus(TenantStatus.ACTIVE);  // ❌ Validation 없음

// AFTER: Business Methods
@Entity
public class TenantJpaEntity {

    private String name;
    private TenantStatus status;

    // Business Method (Validation 포함)
    public void activate() {
        if (this.status == TenantStatus.DELETED) {
            throw new IllegalStateException("Cannot activate deleted tenant");
        }
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();
    }
}

// 사용:
tenant.activate();  // ✅ Validation + Audit 자동 업데이트
```

---

## 체크리스트

Lombok 제거 작업이 완료되었는지 확인하세요:

- [ ] ❌ `@Data` 제거됨
- [ ] ❌ 클래스 레벨 `@Getter` 제거됨
- [ ] ❌ 클래스 레벨 `@Setter` 제거됨
- [ ] ❌ `@Builder` 제거됨
- [ ] ❌ `@NoArgsConstructor` 제거됨
- [ ] ❌ `@AllArgsConstructor` 제거됨
- [ ] ❌ `@RequiredArgsConstructor` 제거됨
- [ ] ❌ `@ToString` 제거됨
- [ ] ❌ `@EqualsAndHashCode` 제거됨
- [ ] ✅ Three-Tier Constructor Pattern 구현됨
- [ ] ✅ Static Factory Methods (`create`, `reconstitute`) 구현됨
- [ ] ✅ 필요한 Getter만 수동 작성됨
- [ ] ✅ Setter 대신 비즈니스 메서드 구현됨
- [ ] ✅ JpaEntityConventionTest 통과

---

## 참고 자료

- [Constructor and Factory Pattern](./04_constructor-and-factory-pattern.md) - Three-Tier Constructor 상세
- [Entity Immutability](./02_entity-immutability.md) - Setter 금지 원칙
- [JpaEntityConventionTest.java](../../../../bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/JpaEntityConventionTest.java) - ArchUnit 자동 검증

---

**✅ Pure Java로 작성하면 코드 의도가 명확해지고 JpaEntityConventionTest가 자동으로 통과합니다!**
