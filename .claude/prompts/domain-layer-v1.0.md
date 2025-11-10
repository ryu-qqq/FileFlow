# Domain Layer Code Generation Prompt (v1.0)

당신은 Spring DDD Domain Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **Lombok 금지**: Pure Java 또는 Record 패턴 사용
- ✅ **Law of Demeter**: Getter 체이닝 금지, Delegation 패턴 사용
- ✅ **Tell, Don't Ask**: 비즈니스 메서드로 상태 변경, 새 인스턴스 반환
- ✅ **완전 불변성**: Record 패턴, 상태 변경 시 새 인스턴스 반환
- ✅ **Value Object 조합**: 작은 Value Object로 분해 (Id, Content, Status, Audit)
- ✅ **Javadoc 필수**: HTML 포맷 (`<p>`, `<ul>`, `<li>`, `{@link}`)
- ✅ **Domain Purity**: JPA, DB, 외부 의존성 완전히 배제

## 코드 생성 템플릿

### Aggregate (Record 패턴 - 완전 불변)

```java
/**
 * {Aggregate} Domain Aggregate Root
 *
 * <p>DDD 패턴의 Aggregate Root로서 {Aggregate} 도메인을 표현합니다.</p>
 *
 * <p><strong>Aggregate Root 책임:</strong></p>
 * <ul>
 *   <li>도메인 불변성 유지</li>
 *   <li>비즈니스 규칙 검증</li>
 *   <li>상태 변경 관리</li>
 *   <li>Value Object 조합</li>
 * </ul>
 *
 * <p><strong>Value Object 구성:</strong></p>
 * <ul>
 *   <li>{@link {Aggregate}Id} - 식별자</li>
 *   <li>{@link {Aggregate}Content} - 핵심 내용</li>
 *   <li>{@link {Aggregate}Status} - 상태</li>
 *   <li>{@link {Aggregate}Audit} - 감사 정보</li>
 * </ul>
 *
 * <p><strong>불변성 원칙:</strong></p>
 * <ul>
 *   <li>모든 필드는 record로 불변</li>
 *   <li>상태 변경 시 새로운 인스턴스 반환</li>
 *   <li>비즈니스 로직은 도메인 메서드로 캡슐화</li>
 * </ul>
 *
 * @param {aggregate}Id 식별자 Value Object
 * @param content 핵심 내용 Value Object
 * @param status 상태 Value Object
 * @param audit 감사 정보 Value Object
 * @author Claude Code
 * @since 1.0
 */
public record {Aggregate}Domain(
    {Aggregate}Id {aggregate}Id,
    {Aggregate}Content content,
    {Aggregate}Status status,
    {Aggregate}Audit audit
) {

    /**
     * 새로운 {Aggregate} 생성 (ID 없음)
     *
     * <p>신규 생성 시 사용하며, ID는 Persistence Layer에서 할당됩니다.</p>
     *
     * @param name {Aggregate} 이름
     * @return ID가 없는 새로운 {Aggregate}Domain
     */
    public static {Aggregate}Domain create(String name) {
        return new {Aggregate}Domain(
            null,  // ID는 저장 시 할당
            {Aggregate}Content.of(name),
            {Aggregate}Status.createDefault(),  // ACTIVE
            {Aggregate}Audit.createNew()  // 현재 시각
        );
    }

    /**
     * 기존 {Aggregate} 재구성 (ID 있음)
     *
     * <p>Persistence Layer에서 로드 시 사용합니다.</p>
     *
     * @param id {Aggregate} ID
     * @param name 이름
     * @param status 상태 문자열
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 기존 {Aggregate}Domain
     */
    public static {Aggregate}Domain of(
        Long id,
        String name,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new {Aggregate}Domain(
            {Aggregate}Id.of(id),
            {Aggregate}Content.of(name),
            {Aggregate}Status.fromString(status),
            {Aggregate}Audit.of(createdAt, updatedAt)
        );
    }

    /**
     * ID 조회
     *
     * @return {Aggregate} ID (신규 생성 시 null 가능)
     */
    public Long getId() {
        return {aggregate}Id != null ? {aggregate}Id.id() : null;
    }

    /**
     * 이름 조회 (Law of Demeter 준수 - Delegation)
     *
     * @return 이름 문자열
     */
    public String getName() {
        return content.name();
    }

    /**
     * 상태 문자열 조회 (Law of Demeter 준수 - Delegation)
     *
     * @return 상태 문자열
     */
    public String getStatus() {
        return status.asString();
    }

    /**
     * 생성 일시 조회
     *
     * @return 생성 일시
     */
    public LocalDateTime getCreatedAt() {
        return audit.createdAt();
    }

    /**
     * 수정 일시 조회
     *
     * @return 수정 일시
     */
    public LocalDateTime getUpdatedAt() {
        return audit.updatedAt();
    }

    /**
     * 이름 변경 (Tell, Don't Ask - 새 인스턴스 반환)
     *
     * <p>이름을 변경하고 updatedAt을 갱신한 새로운 {Aggregate}Domain을 반환합니다.</p>
     *
     * @param newName 새로운 이름
     * @return 이름이 변경된 새로운 {Aggregate}Domain
     */
    public {Aggregate}Domain changeName(String newName) {
        return new {Aggregate}Domain(
            this.{aggregate}Id,
            {Aggregate}Content.of(newName),
            this.status,
            this.audit.updateNow()
        );
    }

    /**
     * 활성화 (Tell, Don't Ask - 새 인스턴스 반환)
     *
     * @return ACTIVE 상태로 변경된 새로운 {Aggregate}Domain
     * @throws {Aggregate}InvalidStatusException DELETED 상태에서 호출 시
     */
    public {Aggregate}Domain activate() {
        return new {Aggregate}Domain(
            this.{aggregate}Id,
            this.content,
            this.status.activate(),
            this.audit.updateNow()
        );
    }

    /**
     * 비활성화 (Tell, Don't Ask - 새 인스턴스 반환)
     *
     * @return INACTIVE 상태로 변경된 새로운 {Aggregate}Domain
     * @throws {Aggregate}InvalidStatusException DELETED 상태에서 호출 시
     */
    public {Aggregate}Domain deactivate() {
        return new {Aggregate}Domain(
            this.{aggregate}Id,
            this.content,
            this.status.deactivate(),
            this.audit.updateNow()
        );
    }

    /**
     * 삭제 (논리적 삭제 - 새 인스턴스 반환)
     *
     * @return DELETED 상태로 변경된 새로운 {Aggregate}Domain
     */
    public {Aggregate}Domain delete() {
        return new {Aggregate}Domain(
            this.{aggregate}Id,
            this.content,
            this.status.delete(),
            this.audit.updateNow()
        );
    }

    /**
     * 활성 상태 여부
     *
     * @return ACTIVE면 true
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * 삭제 상태 여부
     *
     * @return DELETED면 true
     */
    public boolean isDeleted() {
        return status.isDeleted();
    }

    /**
     * ID 할당 (Persistence Layer에서 저장 후 호출)
     *
     * @param id 할당된 ID
     * @return ID가 설정된 새로운 {Aggregate}Domain
     */
    public {Aggregate}Domain withId(Long id) {
        return new {Aggregate}Domain(
            {Aggregate}Id.of(id),
            this.content,
            this.status,
            this.audit
        );
    }
}
```

### Value Object - ID (Simple Record)

```java
/**
 * {Aggregate} ID Value Object
 *
 * <p>{Aggregate} 식별자를 나타내는 불변 Value Object</p>
 *
 * @param id {Aggregate} ID
 * @author Claude Code
 * @since 1.0
 */
public record {Aggregate}Id(
    Long id
) {

    /**
     * 기존 ID로 생성 (Static Factory)
     *
     * @param id ID 값
     * @return 생성된 {Aggregate}Id
     */
    public static {Aggregate}Id of(Long id) {
        return new {Aggregate}Id(id);
    }
}
```

### Value Object - ID (Class with Validation)

```java
/**
 * {Aggregate} ID Value Object (UUID)
 *
 * <p>{Aggregate} 식별자를 나타내는 불변 Value Object</p>
 *
 * @author Claude Code
 * @since 1.0
 */
public class {Aggregate}Id {

    private final String value;

    /**
     * {Aggregate}Id 생성자 (외부 직접 호출 금지)
     */
    protected {Aggregate}Id(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("{Aggregate} ID는 필수입니다.");
        }
        this.value = value;
    }

    /**
     * 새로운 {Aggregate}Id 생성 (Static Factory)
     *
     * @return 생성된 {Aggregate}Id
     */
    public static {Aggregate}Id new{Aggregate}Id() {
        return new {Aggregate}Id(UUID.randomUUID().toString());
    }

    /**
     * 기존 값으로 {Aggregate}Id 생성 (Static Factory)
     *
     * @param value {Aggregate} ID 값
     * @return 생성된 {Aggregate}Id
     */
    public static {Aggregate}Id of(String value) {
        return new {Aggregate}Id(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {Aggregate}Id that = ({Aggregate}Id) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
```

### Value Object - Content

```java
/**
 * {Aggregate} Content Value Object
 *
 * <p>{Aggregate} 핵심 내용을 나타내는 불변 Value Object</p>
 *
 * @param name {Aggregate} 이름
 * @author Claude Code
 * @since 1.0
 */
public record {Aggregate}Content(
    String name
) {

    /**
     * Compact Constructor - Validation
     */
    public {Aggregate}Content {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("{Aggregate} name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("{Aggregate} name must be <= 100 characters");
        }
    }

    /**
     * Static Factory
     *
     * @param name {Aggregate} 이름
     * @return 생성된 {Aggregate}Content
     */
    public static {Aggregate}Content of(String name) {
        return new {Aggregate}Content(name);
    }

    /**
     * Business Method - 대문자 변환
     *
     * @return 대문자로 변환된 이름
     */
    public String nameInUpperCase() {
        return name.toUpperCase();
    }
}
```

### Value Object - Status (Sealed + Pattern Matching)

```java
/**
 * {Aggregate} Status Value Object
 *
 * <p>{Aggregate} 상태를 나타내는 불변 Value Object</p>
 * <p>Sealed interface + Pattern Matching으로 상태 안전성 보장</p>
 *
 * @author Claude Code
 * @since 1.0
 */
public sealed interface {Aggregate}Status
    permits {Aggregate}Status.Active, {Aggregate}Status.Inactive, {Aggregate}Status.Deleted {

    /**
     * 기본 상태 생성 (ACTIVE)
     */
    static {Aggregate}Status createDefault() {
        return new Active();
    }

    /**
     * 문자열로부터 상태 생성
     */
    static {Aggregate}Status fromString(String status) {
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> new Active();
            case "INACTIVE" -> new Inactive();
            case "DELETED" -> new Deleted();
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

    /**
     * 문자열 변환
     */
    String asString();

    /**
     * 활성화
     */
    {Aggregate}Status activate();

    /**
     * 비활성화
     */
    {Aggregate}Status deactivate();

    /**
     * 삭제
     */
    {Aggregate}Status delete();

    /**
     * 활성 상태 여부
     */
    boolean isActive();

    /**
     * 삭제 상태 여부
     */
    boolean isDeleted();

    record Active() implements {Aggregate}Status {
        @Override public String asString() { return "ACTIVE"; }
        @Override public {Aggregate}Status activate() { return this; }
        @Override public {Aggregate}Status deactivate() { return new Inactive(); }
        @Override public {Aggregate}Status delete() { return new Deleted(); }
        @Override public boolean isActive() { return true; }
        @Override public boolean isDeleted() { return false; }
    }

    record Inactive() implements {Aggregate}Status {
        @Override public String asString() { return "INACTIVE"; }
        @Override public {Aggregate}Status activate() { return new Active(); }
        @Override public {Aggregate}Status deactivate() { return this; }
        @Override public {Aggregate}Status delete() { return new Deleted(); }
        @Override public boolean isActive() { return false; }
        @Override public boolean isDeleted() { return false; }
    }

    record Deleted() implements {Aggregate}Status {
        @Override public String asString() { return "DELETED"; }
        @Override public {Aggregate}Status activate() {
            throw new {Aggregate}InvalidStatusException("Cannot activate deleted {aggregate}");
        }
        @Override public {Aggregate}Status deactivate() {
            throw new {Aggregate}InvalidStatusException("Cannot deactivate deleted {aggregate}");
        }
        @Override public {Aggregate}Status delete() { return this; }
        @Override public boolean isActive() { return false; }
        @Override public boolean isDeleted() { return true; }
    }
}
```

### Value Object - Audit

```java
/**
 * {Aggregate} Audit Value Object
 *
 * <p>{Aggregate} 감사 정보를 나타내는 불변 Value Object</p>
 *
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 * @author Claude Code
 * @since 1.0
 */
public record {Aggregate}Audit(
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * 신규 생성 (현재 시각)
     */
    public static {Aggregate}Audit createNew() {
        LocalDateTime now = LocalDateTime.now();
        return new {Aggregate}Audit(now, now);
    }

    /**
     * 기존 데이터 복원
     */
    public static {Aggregate}Audit of(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new {Aggregate}Audit(createdAt, updatedAt);
    }

    /**
     * updatedAt 갱신 (새 인스턴스 반환)
     */
    public {Aggregate}Audit updateNow() {
        return new {Aggregate}Audit(this.createdAt, LocalDateTime.now());
    }
}
```

## 검증 체크리스트

- [ ] Lombok 어노테이션 미사용
- [ ] Getter 체이닝 없음, Delegation 패턴 사용
- [ ] 비즈니스 메서드는 새 인스턴스 반환 (완전 불변)
- [ ] Aggregate는 Record 패턴
- [ ] Value Object 조합 (Id, Content, Status, Audit)
- [ ] HTML 포맷 Javadoc (`<p>`, `<ul>`, `<li>`, `{@link}`)
- [ ] Factory Method 패턴 (`create()`, `of()`)
- [ ] Compact Constructor로 Validation
- [ ] Domain Purity (JPA, DB 완전히 배제)

## 안티패턴 (피해야 할 것)

### ❌ Getter 체이닝 (Law of Demeter 위반)

```java
// ❌ Bad - Getter 체이닝
String message = example.getContent().getMessage().toUpperCase();

// ✅ Good - Delegation 패턴
String message = example.getMessageInUpperCase();

// Domain 내부 구현:
public String getMessageInUpperCase() {
    return content.messageInUpperCase();
}
```

### ❌ Lombok 사용

```java
// ❌ Bad
@Data
@Builder
public class {Aggregate}Domain {
    private Long {aggregate}Id;
    private String name;
}

// ✅ Good - Record 패턴
public record {Aggregate}Domain(
    {Aggregate}Id {aggregate}Id,
    {Aggregate}Content content,
    {Aggregate}Status status,
    {Aggregate}Audit audit
) {
    // Factory Methods, Business Methods...
}
```

### ❌ 내부 상태 변경 (가변 객체)

```java
// ❌ Bad - void 메서드, 내부 상태 변경
public void activate() {
    this.status = Status.ACTIVE;
}

// ✅ Good - 새 인스턴스 반환 (완전 불변)
public {Aggregate}Domain activate() {
    return new {Aggregate}Domain(
        this.{aggregate}Id,
        this.content,
        this.status.activate(),
        this.audit.updateNow()
    );
}
```

### ❌ Ask, Don't Tell

```java
// ❌ Bad (Application Layer)
if ({aggregate}.getStatus() == Status.ACTIVE) {
    {aggregate}.setStatus(Status.INACTIVE);
}

// ✅ Good (Application Layer)
{Aggregate}Domain updated{Aggregate} = {aggregate}.deactivate();

// Domain Layer (새 인스턴스 반환)
public {Aggregate}Domain deactivate() {
    return new {Aggregate}Domain(
        this.{aggregate}Id,
        this.content,
        this.status.deactivate(),
        this.audit.updateNow()
    );
}
```

### ❌ Domain Purity 위반 (JPA 의존)

```java
// ❌ Bad - Domain이 JPA에 의존
public class {Aggregate}Domain {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}

// ✅ Good - Domain은 순수 Java (JPA 몰라야 함)
public record {Aggregate}Domain(
    {Aggregate}Id {aggregate}Id,
    {Aggregate}Content content,
    {Aggregate}Status status,
    {Aggregate}Audit audit
) {
    // Pure Java, No JPA
}

// JPA는 Persistence Layer에서만 사용
// {Aggregate}Entity.java (별도 파일, Persistence Layer)
@Entity
@Table(name = "{aggregate}")
public class {Aggregate}Entity {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    // Entity → Domain 변환
    public {Aggregate}Domain toDomain() { ... }

    // Domain → Entity 변환
    public static {Aggregate}Entity from({Aggregate}Domain domain) { ... }
}
```

## 참고 문서

- [Domain Layer 규칙](../../docs/coding_convention/02-domain-layer/)
- [Law of Demeter](../../docs/coding_convention/02-domain-layer/law-of-demeter/)
- [Java 21 Record 패턴](../../docs/coding_convention/06-java21-patterns/record-patterns/)
- [Sealed Classes](../../docs/coding_convention/06-java21-patterns/sealed-classes/)
