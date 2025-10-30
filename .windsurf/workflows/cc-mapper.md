---
description: persistence layer mapper 보일러 템플릿 를 CC에 준수하여 만든다
---

# Entity Mapper Generation Workflow

**목적**: Entity Mapper (Domain ↔ JPA Entity 변환) 자동 생성
**타겟**: Persistence Layer - Mapper Pattern
**검증**: MapperConventionTest (ArchUnit)

---

## ✅ STEP 1: PRD 분석 (Entity 매핑 요구사항 추출)

### 1.1 Domain Aggregate 분석

**분석 대상**:
```
domain/src/main/java/com/ryuqq/domain/{aggregate}/
├── {Aggregate}.java           # Aggregate Root
├── {Aggregate}Id.java         # Entity ID (Value Object)
├── {Property}Name.java        # 각 속성의 Value Object
└── {Aggregate}Status.java     # Enum (상태)
```

**추출 정보**:
- Aggregate 이름 (예: Tenant, Order, Product)
- Value Object 목록 (예: TenantId, TenantName, TenantStatus)
- 필수 필드 vs 선택 필드
- reconstitute() 생성자 시그니처

### 1.2 JPA Entity 분석

**분석 대상**:
```
adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{aggregate}/entity/
└── {Aggregate}JpaEntity.java  # JPA Entity
```

**추출 정보**:
- JPA Entity 필드 목록 (원시 타입: Long, String, Enum)
- Nullable vs NotNull 필드
- create() / reconstitute() 팩토리 메서드 시그니처

### 1.3 매핑 전략 결정

**Law of Demeter 준수** (중요!):
- ❌ 금지: `tenant.getId().getValue()` (Getter 체이닝)
- ✅ 권장: `tenant.getIdValue()` (Tell, Don't Ask)

**Domain에서 제공하는 메서드**:
```java
// Domain Aggregate
public Long getIdValue() {
    return this.id.value();
}

public String getNameValue() {
    return this.name.value();
}
```

---

## ✅ STEP 2: Mapper 클래스 생성

### 2.1 파일 위치

```
adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{aggregate}/mapper/
└── {Aggregate}EntityMapper.java
```

### 2.2 Mapper 템플릿

```java
package com.ryuqq.adapter.out.persistence.{aggregate}.mapper;

import com.ryuqq.adapter.out.persistence.{aggregate}.entity.{Aggregate}JpaEntity;
import com.ryuqq.domain.{aggregate}.{Aggregate};
import com.ryuqq.domain.{aggregate}.{Aggregate}Id;
import com.ryuqq.domain.{aggregate}.{Property}Name;
import com.ryuqq.domain.{aggregate}.{Aggregate}Status;

/**
 * {Aggregate} Entity Mapper
 *
 * <p>Domain {@link {Aggregate}} ↔ JPA {@link {Aggregate}JpaEntity} 변환을 담당하는 Stateless Utility 클래스입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>Stateless: 상태를 저장하지 않는 순수 변환 로직</li>
 *   <li>Pure Function: 동일한 입력에 항상 동일한 출력</li>
 *   <li>Law of Demeter 준수: {@code {aggregate}.getIdValue()} 사용</li>
 *   <li>Final 클래스, Private 생성자: 인스턴스화 방지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public final class {Aggregate}EntityMapper {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     */
    private {Aggregate}EntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Entity getter로 원시 타입 추출 (Long, String, Enum)</li>
     *   <li>Value Object Static Factory Method 호출 ({Property}Name.of(String))</li>
     *   <li>Domain reconstitute() 호출 (기존 데이터 복원)</li>
     * </ol>
     *
     * @param entity JPA Entity ({@link {Aggregate}JpaEntity})
     * @return Domain Aggregate ({@link {Aggregate}})
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public static {Aggregate} toDomain({Aggregate}JpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("{Aggregate}JpaEntity must not be null");
        }

        // 1. Value Object 생성 (Static Factory Method)
        {Aggregate}Id {aggregate}Id = {Aggregate}Id.of(entity.getId());
        {Property}Name {property}Name = {Property}Name.of(entity.getName());
        {Aggregate}Status status = entity.getStatus();

        // 2. Domain reconstitute() 호출 (기존 데이터 복원)
        return {Aggregate}.reconstitute(
            {aggregate}Id,
            {property}Name,
            status,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Domain → JPA Entity 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Domain getter로 Value Object 추출</li>
     *   <li>Value Object value() 메서드로 원시 타입 추출 (Law of Demeter 준수)</li>
     *   <li>Entity reconstitute() 호출 (기존 데이터) 또는 create() 호출 (신규 데이터)</li>
     * </ol>
     *
     * @param {aggregate} Domain Aggregate ({@link {Aggregate}})
     * @return JPA Entity ({@link {Aggregate}JpaEntity})
     * @throws IllegalArgumentException {aggregate}가 null인 경우
     */
    public static {Aggregate}JpaEntity toEntity({Aggregate} {aggregate}) {
        if ({aggregate} == null) {
            throw new IllegalArgumentException("{Aggregate} must not be null");
        }

        // 1. Domain에서 원시 타입 추출 (Law of Demeter 준수)
        Long id = {aggregate}.getIdValue();  // ❌ {aggregate}.getId().value() 금지
        String name = {aggregate}.getNameValue();
        {Aggregate}Status status = {aggregate}.getStatus();

        // 2. Entity 생성 또는 복원
        if (id == null) {
            // 신규 데이터: create() 사용 (ID 없음)
            return {Aggregate}JpaEntity.create(name, status);
        } else {
            // 기존 데이터: reconstitute() 사용 (ID 있음)
            return {Aggregate}JpaEntity.reconstitute(
                id,
                name,
                status,
                {aggregate}.getCreatedAt(),
                {aggregate}.getUpdatedAt()
            );
        }
    }
}
```

### 2.3 Zero-Tolerance 규칙 체크리스트

- [x] **Final 클래스**: `public final class {Aggregate}EntityMapper`
- [x] **Private 생성자**: `private {Aggregate}EntityMapper() { throw new AssertionError(...); }`
- [x] **@Component 금지**: 어노테이션 없음
- [x] **Static Methods**: `public static {Aggregate} toDomain(...)`, `public static {Aggregate}JpaEntity toEntity(...)`
- [x] **Law of Demeter**: `{aggregate}.getIdValue()` 사용 (Getter 체이닝 금지)
- [x] **Null 체크**: `if (entity == null) { throw new IllegalArgumentException(...); }`
- [x] **Javadoc**: 클래스, 메서드, @param, @return, @throws 모두 포함

---

## ✅ STEP 3: Domain에 Law of Demeter 메서드 추가 (필요 시)

Domain Aggregate에 `get{Property}Value()` 메서드가 없는 경우 추가합니다.

### 3.1 Domain Aggregate 수정

**파일 위치**: `domain/src/main/java/com/ryuqq/domain/{aggregate}/{Aggregate}.java`

**추가할 메서드**:
```java
/**
 * {Property}Id의 원시 값 반환 (Law of Demeter)
 *
 * <p>Mapper가 {@code {aggregate}.getId().value()} 대신 {@code {aggregate}.getIdValue()}를 사용하도록 지원합니다.</p>
 *
 * @return {Property}Id의 Long 값
 */
public Long getIdValue() {
    return this.id.value();
}

/**
 * {Property}Name의 원시 값 반환 (Law of Demeter)
 *
 * <p>Mapper가 {@code {aggregate}.getName().value()} 대신 {@code {aggregate}.getNameValue()}를 사용하도록 지원합니다.</p>
 *
 * @return {Property}Name의 String 값
 */
public String getNameValue() {
    return this.name.value();
}
```

---

## ✅ STEP 4: ArchUnit 자동 검증

### 4.1 MapperConventionTest 실행

**테스트 파일**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/MapperConventionTest.java`

**검증 항목 (18개 테스트)**:

#### 1️⃣ **Utility Class 규칙** (2개 테스트)
- `mapperShouldBeFinalClass()`: Final 클래스 검증
- `mapperShouldHavePrivateConstructor()`: Private 생성자 검증

#### 2️⃣ **@Component 금지** (1개 테스트)
- `mapperShouldNotUseComponentAnnotation()`: @Component 어노테이션 사용 금지

#### 3️⃣ **Static Method 규칙** (2개 테스트)
- `mapperShouldHaveToDomainStaticMethod()`: `toDomain()` 메서드 존재 검증
- `mapperShouldHaveToEntityStaticMethod()`: `toEntity()` 메서드 존재 검증

#### 4️⃣ **Naming Convention** (2개 테스트)
- `mapperClassShouldHaveEntityMapperSuffix()`: `*EntityMapper` 또는 `*Mapper` suffix
- `mapperShouldResideInMapperPackage()`: `..mapper..` 패키지에 위치

#### 5️⃣ **비즈니스 로직 금지** (3개 가이드라인 테스트)
- `mapperShouldOnlyContainTransformationLogic()`: 순수 변환 로직만 포함
- `toDomainShouldOnlyTransformEntityToDomain()`: toDomain() 규칙
- `toEntityShouldOnlyTransformDomainToEntity()`: toEntity() 규칙

#### 6️⃣ **Law of Demeter** (1개 가이드라인 테스트)
- `mapperShouldFollowLawOfDemeter()`: Getter 체이닝 금지

#### 7️⃣ **Javadoc 가이드라인** (2개 가이드라인 테스트)
- `mapperClassShouldHaveJavadoc()`: 클래스 Javadoc
- `mapperMethodShouldHaveJavadoc()`: 메서드 Javadoc

### 4.2 테스트 실행 명령어

```bash
# Mapper 컨벤션 테스트만 실행
./gradlew test --tests "com.ryuqq.bootstrap.architecture.MapperConventionTest"

# 전체 아키텍처 테스트 실행
./gradlew test --tests "com.ryuqq.bootstrap.architecture.*"
```

---

## ✅ STEP 5: 검증 결과 출력

### 5.1 성공 시 출력

```
✅ Mapper Convention Test: PASSED

[Utility Class 규칙]
✅ Mapper는 final 클래스여야 함
✅ Mapper는 private 생성자를 가져야 함

[@Component 금지]
✅ Mapper는 @Component 어노테이션을 사용하면 안 됨

[Static Method 규칙]
✅ Mapper는 toDomain() static method를 가져야 함
✅ Mapper는 toEntity() static method를 가져야 함

[Naming Convention]
✅ Mapper 클래스는 EntityMapper suffix를 가져야 함
✅ Mapper는 mapper 패키지에 위치해야 함

📁 생성된 파일:
   - adapter-out/persistence-mysql/.../mapper/{Aggregate}EntityMapper.java
```

### 5.2 실패 시 출력

```
❌ Mapper Convention Test: FAILED

[실패 원인]
Rule 'Mapper는 final 클래스여야 함' was violated (1 times):
Class com.ryuqq.adapter.out.persistence.{aggregate}.mapper.{Aggregate}EntityMapper
does not have modifier FINAL

Rule 'Mapper는 @Component 어노테이션을 사용하면 안 됨' was violated (1 times):
Class com.ryuqq.adapter.out.persistence.{aggregate}.mapper.{Aggregate}EntityMapper
is annotated with @Component (Utility class should not be managed by Spring)

[수정 방법]
1. 클래스 선언을 `public final class {Aggregate}EntityMapper`로 수정
2. @Component 어노테이션 제거
3. 테스트 재실행: ./gradlew test --tests "MapperConventionTest"
```

---

## 🎯 고급 옵션

### Option 1: Collection 매핑 (List, Set)

**Domain**:
```java
public class Order {
    private final List<OrderItem> items;

    public List<Long> getItemIdValues() {
        return items.stream()
            .map(OrderItem::getIdValue)
            .collect(Collectors.toList());
    }
}
```

**Mapper**:
```java
public static Order toDomain(OrderJpaEntity entity, List<OrderItemJpaEntity> itemEntities) {
    List<OrderItem> items = itemEntities.stream()
        .map(OrderItemEntityMapper::toDomain)
        .collect(Collectors.toList());

    return Order.reconstitute(
        OrderId.of(entity.getId()),
        items,
        entity.getCreatedAt()
    );
}
```

### Option 2: Embedded Value Object 매핑

**Domain**:
```java
public class Product {
    private final Money price;  // Value Object (amount + currency)
}
```

**Mapper**:
```java
public static Product toDomain(ProductJpaEntity entity) {
    Money price = Money.of(entity.getPriceAmount(), entity.getCurrency());
    return Product.reconstitute(
        ProductId.of(entity.getId()),
        price,
        entity.getCreatedAt()
    );
}

public static ProductJpaEntity toEntity(Product product) {
    return ProductJpaEntity.reconstitute(
        product.getIdValue(),
        product.getPriceAmount(),    // Money.amount()
        product.getPriceCurrency(),  // Money.currency()
        product.getCreatedAt()
    );
}
```

### Option 3: Enum 매핑 (Custom Logic)

**Domain Enum**:
```java
public enum OrderStatus {
    PENDING("대기 중"),
    CONFIRMED("확인 완료"),
    SHIPPED("배송 중");

    private final String description;
}
```

**Mapper** (Enum은 직접 매핑):
```java
public static Order toDomain(OrderJpaEntity entity) {
    OrderStatus status = entity.getStatus();  // JPA Enum → Domain Enum 직접 매핑
    return Order.reconstitute(
        OrderId.of(entity.getId()),
        status,
        entity.getCreatedAt()
    );
}
```

---

## 📚 참고 자료

### 프로젝트 내부 문서
- **Mapper Convention**: `docs/coding_convention/04-persistence-layer/mapper-patterns/`
- **Law of Demeter**: `docs/coding_convention/02-domain-layer/law-of-demeter/`
- **ArchUnit Test**: `bootstrap/bootstrap-web-api/src/test/java/.../MapperConventionTest.java`

### 실제 구현 예제
- **TenantEntityMapper**: `adapter-out/persistence-mysql/.../tenant/mapper/TenantEntityMapper.java`
- **Tenant Domain**: `domain/src/main/java/com/ryuqq/domain/tenant/Tenant.java`

### 관련 워크플로우
- **cc-repository.md**: Repository Adapter 생성 (Mapper 사용)
- **cc-entity.md**: JPA Entity 생성 (Mapper의 변환 대상)
- **cc-domain.md**: Domain Aggregate 생성 (Mapper의 변환 대상)

---

**✅ 이 워크플로우는 Windsurf Cascade가 Entity Mapper를 자동 생성할 때 사용합니다.**

**💡 핵심**: Stateless Utility Class + Law of Demeter + ArchUnit 자동 검증
