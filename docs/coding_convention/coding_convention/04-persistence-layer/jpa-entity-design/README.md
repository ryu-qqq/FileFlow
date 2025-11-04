# JPA Entity 설계 가이드

## 개요

이 디렉토리는 **JPA Entity 설계 컨벤션**을 정의합니다. 모든 규칙은 **ArchUnit 자동 검증**으로 강제됩니다.

---

## 핵심 원칙

### Zero-Tolerance 규칙
다음 규칙은 **예외 없이** 반드시 준수해야 합니다:

1. **Lombok 금지** - Pure Java 사용
2. **Three-Tier Constructor Pattern** - protected JPA, protected create, private reconstitute
3. **Static Factory Methods** - `create()`, `reconstitute()`
4. **Long FK Strategy** - JPA 관계 어노테이션 금지
5. **Setter 금지** - 비즈니스 메서드로 상태 변경
6. **BaseAuditEntity 상속** - createdAt, updatedAt 자동 관리
7. **Enum은 STRING 타입** - `@Enumerated(EnumType.STRING)`
8. **ID는 IDENTITY 전략** - `@GeneratedValue(strategy = GenerationType.IDENTITY)`

---

## 문서 구조

### 00. Lombok 금지
- **파일**: [00_lombok-prohibition.md](./00_lombok-prohibition.md)
- **내용**: Lombok 사용 금지 이유 및 대체 방법
- **ArchUnit**: `@Data`, `@Getter`, `@Setter`, `@Builder` 자동 검증

### 01. Long FK Strategy
- **파일**: [01_long-fk-strategy.md](./01_long-fk-strategy.md)
- **내용**: JPA 관계 어노테이션 금지, Long FK 사용
- **ArchUnit**: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany` 검증

### 02. Entity Immutability
- **파일**: [02_entity-immutability.md](./02_entity-immutability.md)
- **내용**: Setter 금지, 불변성 보장
- **ArchUnit**: Setter 메서드 자동 검증

### 03. N+1 Prevention
- **파일**: [03_n-plus-one-prevention.md](./03_n-plus-one-prevention.md)
- **내용**: N+1 문제 방지 전략
- **관련**: QueryDSL, Fetch Join, Projection

### 04. Constructor and Factory Pattern
- **파일**: [04_constructor-and-factory-pattern.md](./04_constructor-and-factory-pattern.md)
- **내용**: Three-Tier Constructor + Static Factory Method 패턴
- **ArchUnit**: Constructor 접근 제어, Static Factory Method 검증

### 05. Audit Entity Pattern
- **파일**: [05_audit-entity-pattern.md](./05_audit-entity-pattern.md)
- **내용**: BaseAuditEntity 상속, createdAt/updatedAt 자동 관리
- **ArchUnit**: BaseAuditEntity 상속 검증

---

## 자동 검증

### ArchUnit 테스트
모든 JPA Entity 컨벤션은 **JpaEntityConventionTest**로 자동 검증됩니다:

```bash
# JPA Entity 컨벤션 검증
./gradlew :bootstrap:bootstrap-web-api:test \
  --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest"
```

**검증 항목** (18개 테스트):
- [x] Lombok 금지 (4개 테스트)
- [x] BaseAuditEntity 상속 (1개 테스트)
- [x] Long FK 전략 (4개 테스트)
- [x] Static Factory Methods (2개 테스트)
- [x] Setter 금지 (1개 테스트)
- [x] ID 생성 전략 (2개 테스트)
- [x] @Table 어노테이션 (1개 테스트)
- [x] Field 어노테이션 (1개 테스트)
- [x] Enum 타입 (1개 테스트)
- [x] Javadoc (1개 가이드)

---

## 자동화 워크플로우

### /cc-entity 명령어

Windsurf Cascade를 통해 자동으로 JPA Entity를 생성할 수 있습니다:

```bash
# PRD 문서 기반 Entity 자동 생성
/cc-entity prd/your-feature.md

# 워크플로우:
# 1. PRD 문서 분석 → JSON 스펙 생성
# 2. JPA Entity 생성 (모든 컨벤션 준수)
# 3. Fixture 클래스 생성 (테스트용)
# 4. ArchUnit 자동 검증 실행
# 5. 검증 결과 출력 (통과/실패)
```

**워크플로우 문서**: [.windsurf/workflows/cc-entity.md](../../../../.windsurf/workflows/cc-entity.md)

---

## 실전 예제

### 올바른 JPA Entity 구조

```java
package com.ryuqq.adapter.out.persistence.tenant.entity;

import com.ryuqq.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TenantJpaEntity - Tenant 정보를 저장하는 JPA Entity
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
    // Three-Tier Constructor Pattern
    // ========================================

    /**
     * JPA 전용 기본 생성자
     */
    protected TenantJpaEntity() {
        super();
    }

    /**
     * Create용 생성자 (ID 없음)
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
    // Business Methods (No Setters)
    // ========================================

    /**
     * Tenant 활성화
     */
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();  // BaseAuditEntity 메서드
    }

    /**
     * Tenant 비활성화
     */
    public void deactivate() {
        this.status = TenantStatus.INACTIVE;
        markAsUpdated();
    }

    /**
     * Tenant 소프트 삭제
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

## Anti-Patterns (잘못된 예제)

### ❌ Lombok 사용

```java
// ❌ WRONG: Lombok 사용
@Entity
@Data  // ❌ Setter 자동 생성, 불변성 파괴
@Builder  // ❌ 의도 불명확
@AllArgsConstructor  // ❌ Public constructor 생성
public class TenantJpaEntity extends BaseAuditEntity {
    // ...
}
```

### ❌ JPA 관계 어노테이션 사용

```java
// ❌ WRONG: JPA 관계 어노테이션
@Entity
public class OrderJpaEntity {

    @ManyToOne  // ❌ 금지!
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;
}

// ✅ CORRECT: Long FK 전략
@Entity
public class OrderJpaEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;  // ✅ Long FK 사용
}
```

### ❌ Setter 사용

```java
// ❌ WRONG: Setter 사용
@Entity
public class TenantJpaEntity {

    private TenantStatus status;

    // ❌ Setter 금지!
    public void setStatus(TenantStatus status) {
        this.status = status;
    }
}

// ✅ CORRECT: 비즈니스 메서드
@Entity
public class TenantJpaEntity {

    private TenantStatus status;

    // ✅ 비즈니스 메서드로 상태 변경
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        markAsUpdated();
    }
}
```

---

## 체크리스트

JPA Entity를 올바르게 설계했는지 확인하세요:

### 필수 항목
- [ ] ✅ BaseAuditEntity 상속
- [ ] ✅ `@Entity` 어노테이션
- [ ] ✅ `@Table(name = "...")` 어노테이션
- [ ] ✅ `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- [ ] ✅ Protected no-args constructor (JPA)
- [ ] ✅ Protected create constructor (ID 없음)
- [ ] ✅ Private reconstitute constructor (ID 포함)
- [ ] ✅ `create()` static factory method
- [ ] ✅ `reconstitute()` static factory method
- [ ] ✅ Getter만 제공 (Setter 없음)
- [ ] ✅ 비즈니스 메서드로 상태 변경
- [ ] ✅ 모든 public 메서드에 Javadoc

### 금지 항목
- [ ] ❌ Lombok 어노테이션 (`@Data`, `@Getter`, `@Setter`, `@Builder`)
- [ ] ❌ JPA 관계 어노테이션 (`@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`)
- [ ] ❌ Setter 메서드
- [ ] ❌ Public constructor
- [ ] ❌ Business logic 없음 (순수 데이터 객체)

---

## 참고 자료

### 프로젝트 문서
- [ArchUnit Tests](../../../../bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/JpaEntityConventionTest.java) - 자동 검증 테스트
- [Windsurf Workflow](../../../../.windsurf/workflows/cc-entity.md) - Entity 자동 생성 워크플로우
- [TenantJpaEntity](../../../../adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/tenant/entity/TenantJpaEntity.java) - 모범 예제

### 관련 문서
- [Repository Patterns](../repository-patterns/) - Repository 설계 가이드
- [QueryDSL Optimization](../querydsl-optimization/) - QueryDSL 최적화
- [Testing Guide](../testing/) - JPA Entity 테스트 가이드

---

## FAQ

### Q1: Lombok을 왜 금지하나요?

**A**: 다음 이유로 금지합니다:
1. **의도 불명확**: create vs reconstitute 구분 불가
2. **불변성 위반**: Setter 자동 생성
3. **JPA 충돌**: Lazy Loading, Proxy 객체 문제
4. **ArchUnit 검증 불가**: 생성자 패턴 검증 어려움

자세한 내용: [00_lombok-prohibition.md](./00_lombok-prohibition.md)

---

### Q2: Static Factory Method를 왜 사용하나요?

**A**: 다음 장점이 있습니다:
1. **의도 명확**: `create()` → 신규 생성, `reconstitute()` → DB 로드
2. **Validation 분리**: create는 검증, reconstitute는 신뢰
3. **가독성 향상**: 메서드 이름으로 용도 표현
4. **테스트 용이**: Test Fixture에서 명확한 사용

자세한 내용: [04_constructor-and-factory-pattern.md](./04_constructor-and-factory-pattern.md)

---

### Q3: JPA 관계 어노테이션을 왜 금지하나요?

**A**: 다음 문제를 방지합니다:
1. **N+1 문제**: Lazy Loading 기본 동작
2. **순환 참조**: 양방향 관계 설정 시 무한 루프
3. **복잡도 증가**: Cascade, Orphan Removal 관리 어려움
4. **DDD 위반**: Aggregate 경계 불명확

자세한 내용: [01_long-fk-strategy.md](./01_long-fk-strategy.md)

---

### Q4: BaseAuditEntity를 왜 상속해야 하나요?

**A**: 다음 이유로 필수입니다:
1. **Audit 자동화**: createdAt, updatedAt 자동 관리
2. **일관성**: 모든 Entity가 동일한 Audit 필드 보유
3. **markAsUpdated()**: updatedAt 업데이트 메서드 제공

자세한 내용: [05_audit-entity-pattern.md](./05_audit-entity-pattern.md)

---

### Q5: ArchUnit 테스트는 언제 실행되나요?

**A**: 다음 시점에 자동 실행됩니다:
1. **Gradle Build**: `./gradlew build` 실행 시
2. **CI/CD Pipeline**: PR 생성 시 자동 검증
3. **수동 실행**: `./gradlew test --tests "*JpaEntityConventionTest"`

---

## 요약

### 핵심 메시지
> **JPA Entity는 Pure Java로 작성하고, Three-Tier Constructor + Static Factory Method 패턴을 사용하세요.**
>
> **모든 규칙은 JpaEntityConventionTest로 자동 검증됩니다.**

### Quick Start
```bash
# 1. 자동 생성 (Windsurf Cascade)
/cc-entity prd/your-feature.md

# 2. 수동 작성 시 참고
# - TenantJpaEntity.java (모범 예제)
# - 04_constructor-and-factory-pattern.md (상세 가이드)

# 3. 검증
./gradlew :bootstrap:bootstrap-web-api:test \
  --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest"
```

---

**✅ 이 가이드를 따르면 일관되고 유지보수 가능한 JPA Entity를 작성할 수 있습니다!**
