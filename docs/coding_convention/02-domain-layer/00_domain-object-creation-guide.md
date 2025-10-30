# 도메인 객체 생성 가이드

**목적**: 일관된 도메인 객체 생성을 위한 완전 자동화 시스템

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/`

---

## 🎯 3단계 자동화 시스템

### 1️⃣ Windsurf Cascade (자동 생성)
- PRD 문서 기반 도메인 객체 자동 생성
- 모범 사례 100% 준수
- 보일러플레이트 코드 완전 자동화

### 2️⃣ ArchUnit (빌드 시 검증)
- 컨벤션 강제 (Lombok 금지, Law of Demeter 등)
- 빌드 실패 → 수정 → 재빌드

### 3️⃣ Git Pre-commit Hook (최종 안전망)
- 커밋 시 ArchUnit 재검증
- 위반 시 커밋 차단

---

## 📋 사용법

### 빠른 시작

```bash
# 1. Windsurf Cascade로 자동 생성
/cc-domain prd/your-feature.md

# 2. 자동 검증 (빌드 시)
./gradlew build  # ArchUnit 자동 실행

# 3. 수동 검증 (필요시)
./gradlew test --tests DomainObjectConventionTest
```

### 수동 생성 후 검증

```bash
# 1. 도메인 객체 수동 작성
vim domain/src/main/java/com/ryuqq/fileflow/domain/crawling/Crawling.java

# 2. ArchUnit 검증
./gradlew test --tests DomainObjectConventionTest

# 3. 실패 시 수정
# ❌ entityShouldNotUseLombok: Crawling.java에서 @Data 발견
#    → Pure Java로 수정

# 4. 재검증
./gradlew test --tests DomainObjectConventionTest
```

---

## 🏗️ 도메인 객체 타입별 규칙

### 1. Entity (Aggregate Root)

**파일명**: `{AggregateRoot}.java`
**모범 사례**: `Setting.java`

**필수 구조**:
```java
public class {AggregateRoot} {

    // 1. 필드 (final vs mutable 구분)
    private final {AggregateRoot}Id id;              // 식별자 (final)
    private final {Field1} field1;                   // 불변 필드 (final)
    private {Field2} field2;                         // 가변 필드 (비즈니스 로직으로 변경)
    private final Clock clock;                       // 시간 제공자 (DI)
    private final LocalDateTime createdAt;           // 생성 시각 (final)
    private LocalDateTime updatedAt;                 // 수정 시각 (mutable)

    // 2. Private 전체 생성자 (reconstitute 전용)
    private {AggregateRoot}(
        {AggregateRoot}Id id,
        {Field1} field1,
        {Field2} field2,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.field1 = field1;
        this.field2 = field2;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 3. Package-private 주요 생성자 (검증 포함)
    {AggregateRoot}(
        {AggregateRoot}Id id,
        {Field1} field1,
        {Field2} field2,
        Clock clock
    ) {
        validateRequiredFields(field1, field2);

        this.id = id;
        this.field1 = field1;
        this.field2 = field2;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    // 4. Static Factory Methods
    public static {AggregateRoot} forNew({Field1} field1, {Field2} field2) {
        return new {AggregateRoot}(null, field1, field2, Clock.systemDefaultZone());
    }

    public static {AggregateRoot} of(
        {AggregateRoot}Id id, {Field1} field1, {Field2} field2
    ) {
        if (id == null) {
            throw new IllegalArgumentException("{AggregateRoot} ID는 필수입니다");
        }
        return new {AggregateRoot}(id, field1, field2, Clock.systemDefaultZone());
    }

    public static {AggregateRoot} reconstitute(
        {AggregateRoot}Id id,
        {Field1} field1,
        {Field2} field2,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new {AggregateRoot}(
            id, field1, field2,
            Clock.systemDefaultZone(), createdAt, updatedAt
        );
    }

    // 5. Private static 검증 메서드
    private static void validateRequiredFields({Field1} field1, {Field2} field2) {
        if (field1 == null) {
            throw new IllegalArgumentException("{Field1}은(는) 필수입니다");
        }
        if (field2 == null) {
            throw new IllegalArgumentException("{Field2}은(는) 필수입니다");
        }
    }

    // 6. Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getField1Value() {
        return field1.getValue();
    }

    // 7. Tell Don't Ask 비즈니스 로직
    public void updateField2({Field2} newField2) {
        if (newField2 == null) {
            throw new IllegalArgumentException("{Field2}은(는) null일 수 없습니다");
        }

        this.field2 = newField2;
        this.updatedAt = LocalDateTime.now(clock);
    }

    public boolean hasField1({Field1} targetField1) {
        return this.field1.isSameAs(targetField1);
    }

    // 8. equals/hashCode (ID로만 비교)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        {AggregateRoot} that = ({AggregateRoot}) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // 9. toString()
    @Override
    public String toString() {
        return "{AggregateRoot}{" +
            "id=" + id +
            ", field1=" + field1 +
            ", field2=" + field2 +
            '}';
    }
}
```

**ArchUnit 검증 항목**:
- ✅ Lombok 금지
- ✅ ID 필드 final
- ✅ reconstitute() 메서드 존재
- ✅ getIdValue() 메서드 존재

---

### 2. Value Object

**파일명**: `{ValueObject}Key.java` 또는 `{ValueObject}Value.java`
**모범 사례**: `SettingKey.java`, `SettingValue.java`

**필수 구조**:
```java
public class {ValueObject} {

    // 1. 모든 필드 final (완전 불변)
    private final String value;
    private final boolean isSpecial;

    // 2. Private 생성자
    private {ValueObject}(String value, boolean isSpecial) {
        validateValue(value);

        this.value = value;
        this.isSpecial = isSpecial;
    }

    // 3. Static Factory Method
    public static {ValueObject} of(String value) {
        return new {ValueObject}(value, false);
    }

    public static {ValueObject} special(String value) {
        return new {ValueObject}(value, true);
    }

    // 4. Private static 검증 메서드
    private static void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("값은 필수입니다");
        }
    }

    // 5. Getter
    public String getValue() {
        return value;
    }

    // 6. Law of Demeter 메서드
    public boolean isSameAs({ValueObject} other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    // 7. 불변성 유지 메서드
    public {ValueObject} withNewValue(String newValue) {
        return new {ValueObject}(newValue, this.isSpecial);
    }

    // 8. equals/hashCode (모든 필드 비교)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        {ValueObject} that = ({ValueObject}) o;
        return isSpecial == that.isSpecial &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isSpecial);
    }

    // 9. toString()
    @Override
    public String toString() {
        return value;
    }
}
```

**ArchUnit 검증 항목**:
- ✅ 모든 필드 final
- ✅ of() 메서드 존재

---

### 3. Record (Java 21)

**파일명**: `{AggregateRoot}Id.java`
**모범 사례**: `SettingId.java`

**필수 구조**:
```java
/**
 * {AggregateRoot} 식별자
 */
public record {AggregateRoot}Id(Long value) {

    // Compact 생성자 (검증)
    public {AggregateRoot}Id {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("{AggregateRoot} ID는 양수여야 합니다");
        }
        // null 허용: 새로운 엔티티를 의미 (save 전)
    }

    // Static Factory Method
    public static {AggregateRoot}Id of(Long value) {
        return new {AggregateRoot}Id(value);
    }
}
```

**ArchUnit 검증 항목**:
- ✅ Record는 Value Object에만 사용 (주로 ID)
- ✅ of() 메서드 존재

---

### 4. Enum

**파일명**: `{Enum}.java`
**모범 사례**: `SettingLevel.java`, `SettingType.java`

**필수 구조**:
```java
public enum {Enum} {
    VALUE1(1),
    VALUE2(2),
    VALUE3(3);

    // 1. Private final 필드
    private final int priority;

    // 2. Package-private 생성자
    {Enum}(int priority) {
        this.priority = priority;
    }

    // 3. Getter
    public int getPriority() {
        return priority;
    }

    // 4. Law of Demeter 메서드
    public static {Enum} higherPriority({Enum} enum1, {Enum} enum2) {
        if (enum1 == null) {
            return enum2;
        }
        if (enum2 == null) {
            return enum1;
        }
        return enum1.priority < enum2.priority ? enum1 : enum2;
    }

    public boolean hasHigherPriorityThan({Enum} other) {
        if (other == null) {
            return true;
        }
        return this.priority < other.priority;
    }

    // 5. Static Factory Method
    public static {Enum} fromString(String enumStr) {
        if (enumStr == null || enumStr.isBlank()) {
            throw new IllegalArgumentException("{Enum}은(는) 필수입니다");
        }

        try {
            return {Enum}.valueOf(enumStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 {Enum}입니다: " + enumStr);
        }
    }

    // 6. Switch Expression (Java 21)
    public boolean isCompatibleWith(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return switch (this) {
            case VALUE1 -> checkValue1(value);
            case VALUE2 -> checkValue2(value);
            case VALUE3 -> checkValue3(value);
        };
    }
}
```

---

### 5. Utility Class

**파일명**: `{Util}Merger.java`, `{Util}Util.java`, `{Util}Helper.java`
**모범 사례**: `SettingMerger.java`

**필수 구조**:
```java
public final class {Util}Merger {

    // 1. Private 생성자 (인스턴스화 방지)
    private {Util}Merger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // 2. Static 메서드만 (Pure Functions)
    public static Map<Key, Value> merge(
        List<Value> list1,
        List<Value> list2,
        List<Value> list3
    ) {
        // ... 병합 로직 (원본 데이터 변경 없음)

        return Collections.unmodifiableMap(result); // 불변 반환
    }

    // 3. Private Helper Methods
    private static void addToMap(Map<Key, Value> map, List<Value> list) {
        // ... 로직 분리 및 재사용
    }
}
```

**ArchUnit 검증 항목**:
- ✅ final 클래스
- ✅ private 생성자만

---

## 🚨 Zero-Tolerance 규칙

### 1. Lombok 절대 금지

```java
// ❌ 금지
@Data
@Builder
@Getter
@Setter
public class Setting { ... }

// ✅ 권장
public class Setting {
    private final SettingId id;

    public SettingId getId() {
        return id;
    }
}
```

**ArchUnit 검증**: `domainObjectShouldNotUseLombok*`

---

### 2. Law of Demeter 엄격 준수

```java
// ❌ Bad: Getter 체이닝
String zip = order.getCustomer().getAddress().getZip();
String displayValue = setting.getValue().getDisplayValue();

// ✅ Good: 캡슐화된 메서드
String zip = order.getCustomerZipCode();
String displayValue = setting.getDisplayValue();
```

**검증 방법**: Grep 또는 수동 코드 리뷰
```bash
grep -r "\.get.*()\.get" domain/src/main/java
```

---

### 3. equals/hashCode 전략

```java
// Entity: ID로만 비교
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Setting setting = (Setting) o;
    return Objects.equals(id, setting.id); // ✅ ID로만
}

// Value Object: 모든 필드 비교
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SettingKey that = (SettingKey) o;
    return Objects.equals(value, that.value); // ✅ 모든 필드
}
```

---

## 📊 완성된 시스템 흐름

```
PRD 작성
    ↓
/cc-domain prd/your-feature.md (Windsurf Cascade)
    ↓
PRD 분석 → 도메인 모델 추출
    ↓
모범 사례 기반 코드 생성
    ├─ Entity (Setting.java 참고)
    ├─ Value Objects (SettingKey.java, SettingValue.java 참고)
    ├─ Record (SettingId.java 참고)
    └─ Enum (SettingLevel.java, SettingType.java 참고)
    ↓
ArchUnit 자동 검증
    ├─ ✅ 통과 → 성공 보고
    └─ ❌ 실패 → 수정 안내 → 재검증
    ↓
Git Pre-commit Hook (최종 안전망)
    └─ ArchUnit 재검증
    ↓
커밋 완료 🎉
```

---

## 📖 참고 문서

- **모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/`
- **ArchUnit 테스트**: `application/src/test/java/com/ryuqq/fileflow/architecture/DomainObjectConventionTest.java`
- **Windsurf Cascade**: `.cascade/cc-domain.md`
- **다른 레이어 규칙**: `docs/coding_convention/02-domain-layer/`
