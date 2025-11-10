# Persistence Layer Code Generation Prompt (v1.0)

당신은 Spring Persistence Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지 (`@ManyToOne`, `@OneToMany` 등)
- ✅ **JpaEntity 네이밍**: `{Entity}JpaEntity` 접미사 사용 (Domain과 명확히 구분)
- ✅ **BaseAuditEntity 상속**: 감사 필드 중복 제거, `markAsUpdated()` 공통 메서드
- ✅ **CQRS Adapter 분리**: `CommandPersistenceAdapter` + `QueryPersistenceAdapter` 완전 분리
- ✅ **@Component Mapper**: MapStruct 금지, `@Component` + 수동 변환
- ✅ **QueryDslRepository 분리**: 복잡한 QueryDSL 로직을 별도 `@Repository` 클래스로
- ✅ **Domain Enum 공유**: Entity에서 Domain Enum 직접 사용 (불필요한 변환 제거)
- ✅ **final 제거 전략**: JPA 프록시 생성을 위해 final 제거, 이유 명시

## 코드 생성 템플릿

### 1. BaseAuditEntity (공통 감사 필드)

```java
/**
 * BaseAuditEntity - 감사 정보 공통 추상 클래스
 *
 * <p>모든 JPA 엔티티의 공통 감사 필드(생성일시, 수정일시)를 제공합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>@MappedSuperclass: 엔티티가 아닌 매핑 정보만 제공</li>
 *   <li>추상 클래스로 직접 인스턴스화 불가</li>
 *   <li>protected 생성자로 상속 클래스만 접근 가능</li>
 * </ul>
 *
 * <p><strong>필드 불변성 전략:</strong></p>
 * <ul>
 *   <li>JPA 프록시 생성을 위해 final 사용 안 함</li>
 *   <li>불변성은 비즈니스 로직에서 보장 (setter 미제공)</li>
 *   <li>수정 일시는 markAsUpdated() 메서드로만 변경</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@MappedSuperclass
public abstract class BaseAuditEntity {

    /**
     * 생성 일시
     *
     * <p>엔티티 최초 생성 시각을 기록합니다.</p>
     * <p>updatable = false로 수정 불가능하게 설정합니다.</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     *
     * <p>엔티티 최종 수정 시각을 기록합니다.</p>
     * <p>markAsUpdated() 메서드로만 변경 가능합니다.</p>
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항 및 상속 클래스 전용 생성자입니다.</p>
     */
    protected BaseAuditEntity() {
    }

    /**
     * 감사 정보 생성자
     *
     * <p>상속 클래스에서 감사 필드를 초기화할 때 사용합니다.</p>
     *
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    protected BaseAuditEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 생성 일시 조회
     *
     * @return 생성 일시
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 일시 조회
     *
     * @return 수정 일시
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 엔티티 수정 시각 갱신
     *
     * <p>엔티티의 상태가 변경될 때 호출하여 수정 일시를 업데이트합니다.</p>
     * <p>도메인 의미를 가진 메서드로 setter를 대체합니다.</p>
     */
    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 2. JPA Entity (BaseAuditEntity 상속)

```java
/**
 * {Entity}JpaEntity - {Entity} JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 데이터베이스 테이블과 매핑됩니다.</p>
 *
 * <p><strong>BaseAuditEntity 상속:</strong></p>
 * <ul>
 *   <li>공통 감사 필드 상속: createdAt, updatedAt</li>
 *   <li>markAsUpdated() 메서드로 수정 일시 자동 갱신</li>
 *   <li>감사 필드 중복 코드 제거</li>
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong></p>
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지 (@ManyToOne, @OneToMany 등)</li>
 *   <li>모든 외래키는 Long 타입으로 직접 관리</li>
 *   <li>연관 관계는 Application Layer에서 조합</li>
 * </ul>
 *
 * <p><strong>Lombok 금지:</strong></p>
 * <ul>
 *   <li>Plain Java getter/setter 사용</li>
 *   <li>명시적 생성자 제공</li>
 *   <li>JPA protected 기본 생성자 필수</li>
 * </ul>
 *
 * <p><strong>필드 불변성 전략 (final 제거):</strong></p>
 * <ul>
 *   <li>JPA 프록시 생성을 위해 final 사용 안 함</li>
 *   <li>불변성은 비즈니스 로직에서 보장 (setter 미제공)</li>
 *   <li>변경이 필요한 경우 명시적 메서드 제공</li>
 * </ul>
 *
 * <p><strong>Domain Enum 공유:</strong></p>
 * <ul>
 *   <li>Domain과 Entity가 동일한 Enum 사용</li>
 *   <li>별도 변환 로직 불필요 (직접 전달)</li>
 *   <li>코드 단순화 및 일관성 유지</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Entity
@Table(
    name = "{table_name}",
    indexes = {
        @Index(name = "idx_{entity}_name", columnList = "name"),
        @Index(name = "idx_{entity}_customer_id", columnList = "customer_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_{entity}_code", columnNames = {"code"})
    }
)
public class {Entity}JpaEntity extends BaseAuditEntity {

    /**
     * 기본 키 - AUTO_INCREMENT
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Long FK (관계 어노테이션 금지)
     *
     * <p>final 제거: JPA 프록시 생성을 위해 필수</p>
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 이름
     *
     * <p>final 제거: JPA 프록시 생성을 위해 필수</p>
     * <p>불변성은 setter 미제공으로 보장</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 코드 (비즈니스 키)
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 상태 (Domain Enum 직접 사용)
     *
     * <p>Domain과 Entity가 동일한 {Entity}Status enum 사용</p>
     * <p>별도 변환 로직 불필요 (직접 전달)</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private {Entity}Status status;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항으로 반드시 필요합니다.</p>
     * <p>BaseAuditEntity의 protected 생성자 호출</p>
     */
    protected {Entity}JpaEntity() {
        super();
    }

    /**
     * 전체 필드 생성자
     *
     * @param id 기본 키
     * @param customerId Long FK
     * @param name 이름
     * @param code 코드
     * @param status 상태 (Domain Enum)
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    public {Entity}JpaEntity(
        Long id,
        Long customerId,
        String name,
        String code,
        {Entity}Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.code = code;
        this.status = status;
    }

    /**
     * 신규 생성용 생성자 (ID 제외)
     *
     * @param customerId Long FK
     * @param name 이름
     * @param code 코드
     * @param status 상태 (Domain Enum)
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    public {Entity}JpaEntity(
        Long customerId,
        String name,
        String code,
        {Entity}Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this(null, customerId, name, code, status, createdAt, updatedAt);
    }

    // Getters (Pure Java)
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public {Entity}Status getStatus() { return status; }
}
```

### 3. Entity Mapper (@Component, 수동 변환)

```java
/**
 * {Entity}EntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.</p>
 *
 * <p><strong>변환 책임:</strong></p>
 * <ul>
 *   <li>{Entity}Domain → {Entity}JpaEntity (저장용)</li>
 *   <li>{Entity}JpaEntity → {Entity}Domain (조회용)</li>
 *   <li>Value Object 추출 및 재구성</li>
 * </ul>
 *
 * <p><strong>Hexagonal Architecture 관점:</strong></p>
 * <ul>
 *   <li>Adapter Layer의 책임</li>
 *   <li>Domain과 Infrastructure 기술 분리</li>
 *   <li>Domain은 JPA 의존성 없음</li>
 * </ul>
 *
 * <p><strong>{Entity}Status Enum 공유:</strong></p>
 * <ul>
 *   <li>Domain과 Entity가 동일한 {Entity}Status enum 사용</li>
 *   <li>별도 변환 로직 불필요 (직접 전달)</li>
 *   <li>코드 단순화 및 일관성 유지</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Component
public class {Entity}EntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>신규 {Entity} 저장 (ID가 null)</li>
     *   <li>기존 {Entity} 수정 (ID가 있음)</li>
     * </ul>
     *
     * @param domain {Entity} 도메인
     * @return {Entity}JpaEntity
     */
    public {Entity}JpaEntity toEntity({Entity}Domain domain) {
        return new {Entity}JpaEntity(
            domain.getId(),
            domain.getCustomerId(),
            domain.getName(),
            domain.getCode(),
            domain.status(),  // Domain Enum 직접 전달
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환</li>
     *   <li>Application Layer로 전달</li>
     * </ul>
     *
     * @param entity {Entity}JpaEntity
     * @return {Entity} 도메인
     */
    public {Entity}Domain toDomain({Entity}JpaEntity entity) {
        return {Entity}Domain.of(
            entity.getId(),
            entity.getCustomerId(),
            entity.getName(),
            entity.getCode(),
            entity.getStatus(),  // Domain Enum 직접 전달
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

### 4. Command Persistence Adapter (CQRS - Command)

```java
/**
 * {Entity}CommandPersistenceAdapter - {Entity} Command Persistence Adapter
 *
 * <p>CQRS 패턴의 Command 작업을 처리하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>Hexagonal Architecture 관점:</strong></p>
 * <ul>
 *   <li>Outbound Adapter (Driven Adapter)</li>
 *   <li>Application Layer의 {Entity}CommandOutPort 구현</li>
 *   <li>Infrastructure 세부사항 캡슐화 (JPA, MySQL)</li>
 * </ul>
 *
 * <p><strong>CQRS 책임:</strong></p>
 * <ul>
 *   <li>Command 작업만 처리 (저장, 수정, 삭제)</li>
 *   <li>데이터 변경 작업 담당</li>
 *   <li>Query 작업은 {Entity}QueryPersistenceAdapter 사용</li>
 * </ul>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <ol>
 *   <li>Domain → Entity 변환 (Mapper)</li>
 *   <li>Repository를 통한 데이터베이스 작업</li>
 *   <li>Entity → Domain 변환 (Mapper)</li>
 *   <li>Application Layer로 반환</li>
 * </ol>
 *
 * @author Claude Code
 * @since 1.0
 */
@Component
public class {Entity}CommandPersistenceAdapter implements {Entity}CommandOutPort {

    private final {Entity}Repository {entity}Repository;
    private final {Entity}EntityMapper entityMapper;

    /**
     * {Entity}CommandPersistenceAdapter 생성자
     *
     * <p>Constructor Injection을 통해 의존성을 주입받습니다.</p>
     *
     * @param {entity}Repository {Entity} JPA Repository
     * @param entityMapper Entity-Domain 변환 Mapper
     */
    public {Entity}CommandPersistenceAdapter(
        {Entity}Repository {entity}Repository,
        {Entity}EntityMapper entityMapper
    ) {
        this.{entity}Repository = {entity}Repository;
        this.entityMapper = entityMapper;
    }

    /**
     * {Entity} 저장 (생성, 수정, 삭제 모두 처리)
     *
     * <p><strong>처리 시나리오:</strong></p>
     * <ul>
     *   <li><strong>생성:</strong> ID가 null → JPA persist (INSERT)</li>
     *   <li><strong>수정:</strong> ID가 있음 → JPA merge (UPDATE)</li>
     * </ul>
     *
     * @param domain 저장할 {Entity} 도메인
     * @return 저장된 {Entity} 도메인 (ID 할당됨)
     */
    @Override
    public {Entity}Domain save({Entity}Domain domain) {
        // 1. Domain → Entity 변환
        {Entity}JpaEntity entity = entityMapper.toEntity(domain);

        // 2. Entity 저장 (신규 생성 또는 수정)
        {Entity}JpaEntity savedEntity = {entity}Repository.save(entity);

        // 3. Entity → Domain 변환하여 반환
        return entityMapper.toDomain(savedEntity);
    }
}
```

### 5. Query Persistence Adapter (CQRS - Query)

```java
/**
 * {Entity}QueryPersistenceAdapter - {Entity} Query Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 작업을 처리하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>CQRS 책임:</strong></p>
 * <ul>
 *   <li>Query 작업만 처리 (단건 조회, 목록 조회, 검색)</li>
 *   <li>읽기 전용 작업 담당</li>
 *   <li>Command 작업은 {Entity}CommandPersistenceAdapter 사용</li>
 * </ul>
 *
 * <p><strong>QueryDSL 활용:</strong></p>
 * <ul>
 *   <li>복잡한 쿼리 로직은 {Entity}QueryDslRepository로 분리</li>
 *   <li>Adapter는 Entity-Domain 변환만 담당</li>
 * </ul>
 *
 * <p><strong>페이지네이션 전략:</strong></p>
 * <ul>
 *   <li>Offset 기반: search() + countBy() 조합</li>
 *   <li>Cursor 기반: searchByCursor() (size+1 조회, COUNT 쿼리 불필요)</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Component
public class {Entity}QueryPersistenceAdapter implements {Entity}QueryOutPort {

    private final {Entity}Repository {entity}Repository;
    private final {Entity}EntityMapper entityMapper;
    private final {Entity}QueryDslRepository queryDslRepository;

    public {Entity}QueryPersistenceAdapter(
        {Entity}Repository {entity}Repository,
        {Entity}EntityMapper entityMapper,
        {Entity}QueryDslRepository queryDslRepository
    ) {
        this.{entity}Repository = {entity}Repository;
        this.entityMapper = entityMapper;
        this.queryDslRepository = queryDslRepository;
    }

    /**
     * {Entity} ID로 단건 조회
     *
     * @param id {Entity} ID
     * @return 조회된 {Entity} 도메인 (Optional)
     */
    @Override
    public Optional<{Entity}Domain> findById(Long id) {
        return {entity}Repository.findById(id)
            .map(entityMapper::toDomain);
    }

    /**
     * {Entity} 목록 조회 (Offset 기반 페이징)
     *
     * @param query 검색 조건 (page, size, sortBy, sortDirection, 검색 필드)
     * @return {Entity} 도메인 목록
     */
    @Override
    public List<{Entity}Domain> search(Search{Entity}Query query) {
        // QueryDslRepository를 통한 Entity 조회
        List<{Entity}JpaEntity> entities = queryDslRepository.search(query);

        // Entity 목록 → Domain 목록 변환
        return entities.stream()
            .map(entityMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * {Entity} 총 개수 조회 (Offset 기반 페이징용)
     *
     * @param query 검색 조건
     * @return 검색 조건에 맞는 총 개수
     */
    @Override
    public long countBy(Search{Entity}Query query) {
        return queryDslRepository.countBy(query);
    }

    /**
     * {Entity} 목록 조회 (Cursor 기반 페이징)
     *
     * <p><strong>Cursor 기반 페이징 전략:</strong></p>
     * <ul>
     *   <li>size+1개 조회 (hasNext 판단용)</li>
     *   <li>COUNT 쿼리 불필요 (성능 향상)</li>
     *   <li>Service Layer에서 hasNext 판단</li>
     * </ul>
     *
     * @param query 검색 조건 (cursor, size, sortBy, sortDirection, 검색 필드)
     * @return {Entity} 도메인 목록 (size+1개 반환, hasNext 판단용)
     */
    @Override
    public List<{Entity}Domain> searchByCursor(Search{Entity}Query query) {
        // QueryDslRepository를 통한 Entity 조회 (size+1개)
        List<{Entity}JpaEntity> entities = queryDslRepository.searchByCursor(query);

        // Entity 목록 → Domain 목록 변환
        return entities.stream()
            .map(entityMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### 6. JPA Repository

```java
/**
 * {Entity}Repository - {Entity} JPA Repository
 *
 * <p>Spring Data JPA Repository로서 {Entity} Entity의 데이터베이스 접근을 담당합니다.</p>
 *
 * <p><strong>QueryDSL 지원:</strong></p>
 * <ul>
 *   <li>QuerydslPredicateExecutor 상속으로 타입 안전 쿼리 지원</li>
 *   <li>복잡한 조회 조건을 타입 안전하게 구성</li>
 *   <li>동적 쿼리 작성 용이</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
public interface {Entity}Repository extends
    JpaRepository<{Entity}JpaEntity, Long>,
    QuerydslPredicateExecutor<{Entity}JpaEntity> {
}
```

### 7. QueryDSL Repository (별도 클래스)

```java
/**
 * {Entity}QueryDslRepository - {Entity} QueryDSL Repository
 *
 * <p>QueryDSL 기반 복잡한 쿼리를 처리하는 전용 Repository입니다.</p>
 *
 * <p><strong>패키지 분리 이유:</strong></p>
 * <ul>
 *   <li>관심사 분리: 복잡한 QueryDSL 로직을 Adapter와 분리</li>
 *   <li>유지보수성: 쿼리 로직만 독립적으로 관리</li>
 *   <li>테스트 용이성: QueryDSL 로직만 단위 테스트 가능</li>
 * </ul>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>동적 쿼리 구성 (BooleanExpression)</li>
 *   <li>정렬 조건 구성 (OrderSpecifier)</li>
 *   <li>Cursor/Offset 기반 페이징</li>
 *   <li>검색 조건 빌더 메서드</li>
 * </ul>
 *
 * @author Claude Code
 * @since 1.0
 */
@Repository
public class {Entity}QueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final Q{Entity}JpaEntity q{Entity} = Q{Entity}JpaEntity.{entity}JpaEntity;

    public {Entity}QueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * {Entity} 목록 조회 (Offset 기반 페이징)
     *
     * @param query 검색 조건 (page, size, sortBy, sortDirection, 검색 필드)
     * @return {Entity}JpaEntity 목록
     */
    public List<{Entity}JpaEntity> search(Search{Entity}Query query) {
        return queryFactory
            .selectFrom(q{Entity})
            .where(buildSearchConditions(query))
            .orderBy(buildOrderSpecifier(query))
            .offset((long) query.page() * query.size())
            .limit(query.size())
            .fetch();
    }

    /**
     * {Entity} 총 개수 조회 (Offset 기반 페이징용)
     *
     * @param query 검색 조건
     * @return 검색 조건에 맞는 총 개수
     */
    public long countBy(Search{Entity}Query query) {
        Long count = queryFactory
            .select(q{Entity}.count())
            .from(q{Entity})
            .where(buildSearchConditions(query))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * {Entity} 목록 조회 (Cursor 기반 페이징)
     *
     * <p><strong>Cursor 기반 페이징 전략:</strong></p>
     * <ol>
     *   <li>size+1개 조회 (hasNext 판단용)</li>
     *   <li>cursor(마지막 ID)보다 큰 ID만 조회</li>
     *   <li>Service Layer에서 hasNext 판단</li>
     * </ol>
     *
     * <p><strong>성능 이점:</strong></p>
     * <ul>
     *   <li>COUNT 쿼리 불필요 (성능 향상)</li>
     *   <li>인덱스 활용 효율적 (ID 기반 조회)</li>
     *   <li>대량 데이터에 유리</li>
     * </ul>
     *
     * @param query 검색 조건 (cursor, size, sortBy, sortDirection, 검색 필드)
     * @return {Entity}JpaEntity 목록 (size+1개 반환)
     */
    public List<{Entity}JpaEntity> searchByCursor(Search{Entity}Query query) {
        return queryFactory
            .selectFrom(q{Entity})
            .where(
                buildSearchConditions(query),
                buildCursorCondition(query)
            )
            .orderBy(buildOrderSpecifier(query))
            .limit(query.size() + 1)  // size+1개 조회 (hasNext 판단용)
            .fetch();
    }

    /**
     * 검색 조건 구성 (WHERE 절)
     *
     * @param query 검색 조건
     * @return BooleanExpression (null 가능)
     */
    private BooleanExpression buildSearchConditions(Search{Entity}Query query) {
        BooleanExpression expression = null;

        // 검색 조건 예시
        if (query.name() != null && !query.name().isBlank()) {
            expression = q{Entity}.name.containsIgnoreCase(query.name());
        }

        if (query.status() != null && !query.status().isBlank()) {
            try {
                {Entity}Status status = {Entity}Status.valueOf(query.status().toUpperCase());
                BooleanExpression statusCondition = q{Entity}.status.eq(status);
                expression = expression != null ? expression.and(statusCondition) : statusCondition;
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 status는 무시
            }
        }

        return expression;
    }

    /**
     * Cursor 조건 구성 (Cursor 기반 페이징용)
     *
     * @param query 검색 조건 (cursor 포함)
     * @return BooleanExpression (cursor가 없으면 null)
     */
    private BooleanExpression buildCursorCondition(Search{Entity}Query query) {
        if (query.cursor() == null || query.cursor().isBlank()) {
            return null;
        }

        try {
            Long cursorId = Long.parseLong(query.cursor());
            return q{Entity}.id.gt(cursorId);  // ID > cursor
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 정렬 조건 구성 (ORDER BY 절)
     *
     * @param query 검색 조건 (sortBy, sortDirection)
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> buildOrderSpecifier(Search{Entity}Query query) {
        String sortBy = query.sortBy() != null ? query.sortBy() : "updatedAt";
        boolean isAsc = "ASC".equalsIgnoreCase(query.sortDirection());

        return switch (sortBy.toLowerCase()) {
            case "id" -> isAsc ? q{Entity}.id.asc() : q{Entity}.id.desc();
            case "name" -> isAsc ? q{Entity}.name.asc() : q{Entity}.name.desc();
            case "createdat" -> isAsc ? q{Entity}.createdAt.asc() : q{Entity}.createdAt.desc();
            default -> isAsc ? q{Entity}.updatedAt.asc() : q{Entity}.updatedAt.desc();
        };
    }
}
```

## 검증 체크리스트

- [ ] **JpaEntity 네이밍**: `{Entity}JpaEntity` 접미사 사용
- [ ] **BaseAuditEntity 상속**: `extends BaseAuditEntity` (감사 필드 중복 제거)
- [ ] **JPA 관계 어노테이션 없음**: `@ManyToOne`, `@OneToMany` 등 금지
- [ ] **Long FK 사용**: `private Long customerId` (관계 어노테이션 대신)
- [ ] **@Component Mapper**: MapStruct 사용 안 함, `@Component` + 수동 변환
- [ ] **CQRS Adapter 분리**: `CommandPersistenceAdapter` + `QueryPersistenceAdapter` 완전 분리
- [ ] **QueryDslRepository 분리**: 복잡한 QueryDSL 로직을 별도 `@Repository` 클래스로
- [ ] **Domain Enum 공유**: Entity에서 Domain Enum 직접 사용 (불필요한 변환 제거)
- [ ] **final 제거 전략**: JPA 프록시 생성을 위해 final 제거, 이유 명시
- [ ] **Cursor 페이징**: `searchByCursor()` size+1 전략 (COUNT 쿼리 불필요)
- [ ] **HTML Javadoc**: `<p>`, `<ul>`, `<li>`, `<strong>`, `{@link}` 사용
- [ ] **3-Constructor 패턴**: protected no-args, 전체 필드, 신규 생성용
- [ ] **`@Index` 설정**: 조회 성능 향상
- [ ] **`@UniqueConstraint` 설정**: 비즈니스 키 유일성 보장

## 안티패턴 (피해야 할 것)

### ❌ JPA 관계 어노테이션

```java
// ❌ Bad - JPA 관계 어노테이션 사용
@ManyToOne
@JoinColumn(name = "customer_id")
private Customer customer;

// ✅ Good - Long FK 사용
@Column(name = "customer_id")
private Long customerId;
```

### ❌ MapStruct 사용

```java
// ❌ Bad - MapStruct 사용
@Mapper(componentModel = "spring")
public interface {Entity}Mapper {
    {Entity}JpaEntity toEntity({Entity}Domain domain);
    {Entity}Domain toDomain({Entity}JpaEntity entity);
}

// ✅ Good - @Component + 수동 변환
@Component
public class {Entity}EntityMapper {
    public {Entity}JpaEntity toEntity({Entity}Domain domain) {
        return new {Entity}JpaEntity(...);
    }
    public {Entity}Domain toDomain({Entity}JpaEntity entity) {
        return {Entity}Domain.of(...);
    }
}
```

### ❌ CQRS Adapter 미분리

```java
// ❌ Bad - Command와 Query가 하나의 Adapter에
@Component
public class {Entity}PersistenceAdapter
    implements Save{Entity}OutPort, Load{Entity}OutPort {
    // Command와 Query 혼재
}

// ✅ Good - Command와 Query 완전 분리
@Component
public class {Entity}CommandPersistenceAdapter implements {Entity}CommandOutPort { ... }

@Component
public class {Entity}QueryPersistenceAdapter implements {Entity}QueryOutPort { ... }
```

### ❌ QueryDSL 로직을 Adapter 내부에

```java
// ❌ Bad - QueryDSL 로직이 Adapter 내부에
@Component
public class {Entity}QueryPersistenceAdapter {
    public List<{Entity}Domain> search(Search{Entity}Query query) {
        // QueryDSL 로직 직접 작성 (Adapter 비대화)
        return queryFactory
            .selectFrom(q{Entity})
            .where(...)
            .fetch()
            .stream()
            .map(entityMapper::toDomain)
            .collect(Collectors.toList());
    }
}

// ✅ Good - QueryDslRepository로 분리
@Repository
public class {Entity}QueryDslRepository {
    public List<{Entity}JpaEntity> search(Search{Entity}Query query) {
        return queryFactory.selectFrom(q{Entity}).where(...).fetch();
    }
}

@Component
public class {Entity}QueryPersistenceAdapter {
    public List<{Entity}Domain> search(Search{Entity}Query query) {
        // QueryDslRepository 호출 → Adapter는 변환만
        List<{Entity}JpaEntity> entities = queryDslRepository.search(query);
        return entities.stream().map(entityMapper::toDomain).collect(Collectors.toList());
    }
}
```

### ❌ Offset 페이징만 사용 (대량 데이터 성능 저하)

```java
// ❌ Bad - Offset 페이징만 사용 (대량 데이터 시 성능 저하)
public Page<{Entity}JpaEntity> search(Search{Entity}Query query) {
    List<{Entity}JpaEntity> content = queryFactory
        .selectFrom(q{Entity})
        .offset((long) query.page() * query.size())
        .limit(query.size())
        .fetch();

    long total = queryFactory
        .select(q{Entity}.count())
        .from(q{Entity})
        .fetchOne();  // ❌ COUNT 쿼리 (성능 비용)

    return new PageImpl<>(content, pageable, total);
}

// ✅ Good - Cursor 페이징 추가 (대량 데이터 성능 향상)
public List<{Entity}JpaEntity> searchByCursor(Search{Entity}Query query) {
    return queryFactory
        .selectFrom(q{Entity})
        .where(q{Entity}.id.gt(cursorId))  // ✅ ID > cursor
        .limit(query.size() + 1)  // ✅ size+1개 조회 (hasNext 판단용)
        .fetch();  // ✅ COUNT 쿼리 불필요
}
```

## 참고 문서

- [Persistence Layer 규칙](../../docs/coding_convention/04-persistence-layer/)
- [JPA Entity 설계](../../docs/coding_convention/04-persistence-layer/jpa-entity-design/)
- [QueryDSL 최적화](../../docs/coding_convention/04-persistence-layer/querydsl-optimization/)
