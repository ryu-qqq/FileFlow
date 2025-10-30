---
description: persistence layer repository 보일러 템플릿 를 CC에 준수하여 만든다
---

# /cc-repository - Repository Adapter 자동 생성 워크플로우

**명령어**: `/cc-repository prd/your-feature.md` 또는 `/cc-repository "Feature 설명"`

**목적**: Hexagonal Architecture의 Persistence Adapter (Repository Adapter) 자동 생성

---

## 📋 STEP 1: PRD 문서 분석

### 입력
- PRD 문서 경로 (예: `prd/order-management.md`)
- 또는 Feature 설명 텍스트

### 분석 항목
1. **Aggregate 정보**
   - Aggregate Root 이름 (예: Order)
   - Value Objects (OrderId, OrderName 등)
   - Domain Events (주문 생성 이벤트 등)

2. **Port 인터페이스 정보**
   - Port 이름 (예: OrderRepositoryPort)
   - 필수 메서드:
     - `save()`: Aggregate 저장
     - `findById()`: ID로 조회
     - `findAll()`: 목록 조회
     - `deleteById()`: 삭제 (Hard Delete)
     - `count()`: 개수 조회
     - 기타 Custom Query

3. **CQRS 패턴 적용 여부**
   - Command Adapter (저장, 수정, 삭제)
   - Query Adapter (조회, 목록, 집계)
   - 통합 Adapter (Command + Query)

### 출력
```json
{
  "aggregateName": "Order",
  "adapterType": "integrated",  // integrated | command | query
  "adapters": [
    {
      "className": "OrderPersistenceAdapter",
      "portInterface": "OrderRepositoryPort",
      "methods": [
        {"name": "save", "returnType": "Order", "params": ["Order order"]},
        {"name": "findById", "returnType": "Optional<Order>", "params": ["OrderId id"]},
        {"name": "findAll", "returnType": "List<Order>", "params": []},
        {"name": "deleteById", "returnType": "void", "params": ["OrderId id"]},
        {"name": "count", "returnType": "long", "params": []}
      ]
    }
  ],
  "jpaRepository": "OrderJpaRepository",
  "entityMapper": "OrderEntityMapper",
  "dependencies": ["OrderJpaRepository", "OrderEntityMapper"]
}
```

---

## 📦 STEP 2: Repository Adapter 생성

### 2-1. 통합 Adapter (Command + Query) 생성

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{aggregate}/adapter/{Aggregate}PersistenceAdapter.java`

**컨벤션 체크리스트**:
- ✅ `@Component` 어노테이션 사용
- ✅ Port 인터페이스 구현 (`implements {Aggregate}RepositoryPort`)
- ✅ Constructor Injection (final 필드, 단일 public 생성자)
- ✅ Mapper를 통한 Domain ↔ Entity 변환
- ✅ JpaRepository 위임
- ❌ `@Repository` 어노테이션 금지
- ❌ `@Transactional` 어노테이션 금지 (Application Layer에서만)
- ❌ 비즈니스 로직 금지 (단순 변환 및 위임만)

**템플릿**:
```java
package com.ryuqq.adapter.out.persistence.{aggregate}.adapter;

import com.ryuqq.adapter.out.persistence.{aggregate}.entity.{Aggregate}JpaEntity;
import com.ryuqq.adapter.out.persistence.{aggregate}.mapper.{Aggregate}EntityMapper;
import com.ryuqq.adapter.out.persistence.{aggregate}.repository.{Aggregate}JpaRepository;
import com.ryuqq.application.{aggregate}.out.{Aggregate}RepositoryPort;
import com.ryuqq.domain.{aggregate}.{Aggregate};
import com.ryuqq.domain.{aggregate}.{Aggregate}Id;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * {Aggregate} Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link {Aggregate}RepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/{aggregate}/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code {Aggregate}RepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
 *   <li>✅ JpaRepository 사용하여 실제 DB 작업 수행</li>
 *   <li>❌ {@code @Repository} 사용 금지 ({@code @Component} 사용)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @see {Aggregate}RepositoryPort Application Layer Port
 * @see {Aggregate}JpaRepository Spring Data JPA Repository
 * @see {Aggregate}EntityMapper Domain ↔ Entity Mapper
 * @author ryu-qqq
 * @since {날짜}
 */
@Component
public class {Aggregate}PersistenceAdapter implements {Aggregate}RepositoryPort {

    private final {Aggregate}JpaRepository {aggregate}JpaRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param {aggregate}JpaRepository Spring Data JPA Repository
     * @author ryu-qqq
     * @since {날짜}
     */
    public {Aggregate}PersistenceAdapter({Aggregate}JpaRepository {aggregate}JpaRepository) {
        this.{aggregate}JpaRepository = {aggregate}JpaRepository;
    }

    /**
     * {Aggregate} 저장 (생성 또는 수정)
     *
     * <p>Domain {@code {Aggregate}}를 JPA Entity로 변환한 후 저장하고,
     * 저장된 Entity를 다시 Domain으로 변환하여 반환합니다.</p>
     *
     * <h4>처리 흐름</h4>
     * <ol>
     *   <li>Domain → Entity 변환 (Mapper)</li>
     *   <li>JPA Repository로 저장</li>
     *   <li>Entity → Domain 변환 (Mapper)</li>
     *   <li>Domain 반환</li>
     * </ol>
     *
     * @param {aggregate} 저장할 {Aggregate} Domain
     * @return 저장된 {Aggregate} Domain
     * @throws IllegalArgumentException {aggregate}가 null인 경우
     * @author ryu-qqq
     * @since {날짜}
     */
    @Override
    public {Aggregate} save({Aggregate} {aggregate}) {
        if ({aggregate} == null) {
            throw new IllegalArgumentException("{Aggregate} must not be null");
        }

        // Domain → Entity
        {Aggregate}JpaEntity entity = {Aggregate}EntityMapper.toEntity({aggregate});

        // JPA 저장
        {Aggregate}JpaEntity savedEntity = {aggregate}JpaRepository.save(entity);

        // Entity → Domain
        return {Aggregate}EntityMapper.toDomain(savedEntity);
    }

    /**
     * ID로 {Aggregate} 조회
     *
     * @param id 조회할 {Aggregate} ID
     * @return {Aggregate} Domain (존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since {날짜}
     */
    @Override
    public Optional<{Aggregate}> findById({Aggregate}Id id) {
        if (id == null) {
            throw new IllegalArgumentException("{Aggregate}Id must not be null");
        }

        Long idValue = id.value();

        return {aggregate}JpaRepository.findById(idValue)
            .map({Aggregate}EntityMapper::toDomain);
    }

    /**
     * 모든 {Aggregate} 조회
     *
     * @return {Aggregate} Domain 목록 (빈 리스트 가능)
     * @author ryu-qqq
     * @since {날짜}
     */
    @Override
    public List<{Aggregate}> findAll() {
        return {aggregate}JpaRepository.findAll()
            .stream()
            .map({Aggregate}EntityMapper::toDomain)
            .toList();
    }

    /**
     * ID로 {Aggregate} 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.
     * 일반적으로 소프트 삭제를 권장합니다.</p>
     *
     * @param id 삭제할 {Aggregate} ID
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since {날짜}
     */
    @Override
    public void deleteById({Aggregate}Id id) {
        if (id == null) {
            throw new IllegalArgumentException("{Aggregate}Id must not be null");
        }

        Long idValue = id.value();

        {aggregate}JpaRepository.deleteById(idValue);
    }

    /**
     * {Aggregate} 개수 조회
     *
     * @return 전체 {Aggregate} 개수
     * @author ryu-qqq
     * @since {날짜}
     */
    @Override
    public long count() {
        return {aggregate}JpaRepository.count();
    }
}
```

### 2-2. Query Adapter (QueryDSL) 생성

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{aggregate}/adapter/{Aggregate}QueryRepositoryAdapter.java`

**추가 컨벤션**:
- ✅ QueryDSL JPAQueryFactory 사용
- ✅ 동적 쿼리 (BooleanExpression)
- ✅ Pagination 지원 (Offset-based, Cursor-based)
- ✅ Projection 최적화

**템플릿**: (TenantQueryRepositoryAdapter 참조)

---

## 📦 STEP 3: Mapper 생성 (함께 생성)

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{aggregate}/mapper/{Aggregate}EntityMapper.java`

**컨벤션 체크리스트**:
- ✅ `final` 클래스
- ✅ `private` 생성자 (Utility class)
- ✅ `toDomain()` static method (Entity → Domain)
- ✅ `toEntity()` static method (Domain → Entity)
- ❌ `@Component` 어노테이션 금지
- ❌ 비즈니스 로직 금지 (순수 변환만)
- ❌ Law of Demeter 위반 금지 (Getter 체이닝 금지)

**템플릿**:
```java
package com.ryuqq.adapter.out.persistence.{aggregate}.mapper;

import com.ryuqq.adapter.out.persistence.{aggregate}.entity.{Aggregate}JpaEntity;
import com.ryuqq.domain.{aggregate}.{Aggregate};
import com.ryuqq.domain.{aggregate}.{Aggregate}Id;

/**
 * {Aggregate} Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code {Aggregate}} ↔ JPA Entity {@code {Aggregate}JpaEntity} 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since {날짜}
 */
public final class {Aggregate}EntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private {Aggregate}EntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * @param entity JPA Entity
     * @return Domain {Aggregate}
     * @throws IllegalArgumentException entity가 null인 경우
     * @author ryu-qqq
     * @since {날짜}
     */
    public static {Aggregate} toDomain({Aggregate}JpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("{Aggregate}JpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        {Aggregate}Id {aggregate}Id = {Aggregate}Id.of(entity.getId());

        // Domain Aggregate 재구성
        return {Aggregate}.reconstitute(
            {aggregate}Id,
            // ... 다른 필드들
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * @param {aggregate} Domain {Aggregate}
     * @return JPA Entity
     * @throws IllegalArgumentException {aggregate}가 null인 경우
     * @author ryu-qqq
     * @since {날짜}
     */
    public static {Aggregate}JpaEntity toEntity({Aggregate} {aggregate}) {
        if ({aggregate} == null) {
            throw new IllegalArgumentException("{Aggregate} must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        Long id = {aggregate}.getIdValue();

        // Entity 생성 (reconstitute)
        return {Aggregate}JpaEntity.reconstitute(
            id,
            // ... 다른 필드들
            {aggregate}.getCreatedAt(),
            {aggregate}.getUpdatedAt()
        );
    }
}
```
---

## ✅ STEP 4: ArchUnit 자동 검증

### 실행 명령
```bash
./gradlew :bootstrap:bootstrap-web-api:test \
  --tests "com.ryuqq.bootstrap.architecture.RepositoryAdapterConventionTest" \
  --tests "com.ryuqq.bootstrap.architecture.MapperConventionTest"
```

### 검증 항목
**RepositoryAdapterConventionTest** (18개 테스트):
- [x] @Component 사용 (not @Repository)
- [x] @Transactional 금지 (클래스/메서드)
- [x] Constructor Injection (단일 public 생성자)
- [x] Port 인터페이스 구현
- [x] Naming Convention
- [x] 비즈니스 로직 금지 (가이드라인)

**MapperConventionTest** (18개 테스트):
- [x] final 클래스
- [x] private 생성자
- [x] @Component 금지
- [x] toDomain() static method
- [x] toEntity() static method
- [x] Naming Convention
- [x] Law of Demeter 준수 (가이드라인)

---

## 📋 STEP 5: 검증 결과 출력

### 성공 시
```
✅ Repository Adapter 생성 완료!

생성된 파일:
- OrderPersistenceAdapter.java (17개 테스트 통과)
- OrderEntityMapper.java (18개 테스트 통과)

다음 단계:
1. Port 인터페이스 생성: /cc-port Order
2. UseCase 구현: /cc-usecase CreateOrder
3. 전체 검증: /validate-architecture
```

### 실패 시
```
❌ Repository Adapter 생성 실패

실패 원인:
- [@Component 누락] OrderPersistenceAdapter.java:10
  수정: 클래스에 @Component 추가

- [Port 미구현] OrderPersistenceAdapter.java:15
  수정: implements OrderRepositoryPort 추가

- [Mapper final 누락] OrderEntityMapper.java:8
  수정: public final class OrderEntityMapper

자동 수정 옵션:
- /cc-repository Order --fix-violations
```

---

## 🔧 고급 옵션

### CQRS 분리
```bash
# Command Adapter만 생성
/cc-repository Order --command-only

# Query Adapter만 생성
/cc-repository Order --query-only
```

### QueryDSL 동적 쿼리 추가
```bash
/cc-repository Order --querydsl \
  --filters "name,status,deleted" \
  --pagination "offset,cursor"
```

### Soft Delete 지원
```bash
/cc-repository Order --soft-delete \
  --deleted-field "deleted"
```

---

## 📚 참고 자료

- [Repository Patterns Guide](../../d