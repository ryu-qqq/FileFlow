---
description: domain layer Domain 보일러 템플릿 를 CC에 준수하여 만든다
---

# Domain Object Generator (Coding Convention)
**목적**: PRD 문서 기반으로 도메인 객체를 자동 생성하며, 모든 코딩 컨벤션을 준수합니다.

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/`

**검증**: ArchUnit (`DomainObjectConventionTest.java`)

---

## 사용법

```bash
# Windsurf에서 실행
/cc-domain prd/your-feature.md
```

---

## Step 1: PRD 문서 분석

**입력**:
- PRD 문서 경로 (예: `prd/crawling.md`)
- 코딩 컨벤션 문서 (`docs/coding_convention/02-domain-layer/**/*.md`)

**출력**:
- Domain Model Specification (JSON)

**프롬프트**:
```
PRD 문서를 분석하여 다음을 추출하세요:

1. **Aggregate Root 후보**
   - 도메인의 핵심 엔티티
   - 생명주기를 독립적으로 관리하는 객체
   - 예: Order, User, Setting

2. **Value Object 후보**
   - 불변 값 객체
   - 예: Email, Address, Money, SettingKey, SettingValue

3. **Enum 후보**
   - 상태, 타입, 레벨 등
   - 예: OrderStatus, UserRole, SettingLevel

4. **도메인 규칙 (검증 로직)**
   - 비즈니스 규칙
   - 필수 필드 검증
   - 값 범위 제한

5. **비즈니스 로직 (Tell Don't Ask 메서드)**
   - has*, is*, can* 메서드
   - 상태 변경 메서드
   - 예: hasKey(), isLevel(), updateValue()

출력 형식:
{
  "package": "crawling",
  "aggregates": [
    {
      "name": "Crawling",
      "description": "크롤링 Aggregate Root",
      "fields": [
        {"name": "id", "type": "CrawlingId", "final": true},
        {"name": "url", "type": "String", "final": true},
        {"name": "status", "type": "CrawlingStatus", "final": false}
      ],
      "valueObjects": ["CrawlingId"],
      "enums": ["CrawlingStatus"],
      "validationRules": [
        "URL은 필수입니다",
        "URL은 유효한 형식이어야 합니다"
      ],
      "businessLogic": [
        {"name": "start", "description": "크롤링 시작"},
        {"name": "complete", "description": "크롤링 완료"},
        {"name": "fail", "description": "크롤링 실패"},
        {"name": "isCompleted", "returnType": "boolean"},
        {"name": "isFailed", "returnType": "boolean"}
      ]
    }
  ],
  "valueObjects": [
    {
      "name": "CrawlingId",
      "type": "Record",
      "field": {"name": "value", "type": "Long"}
    }
  ],
  "enums": [
    {
      "name": "CrawlingStatus",
      "values": ["PENDING", "RUNNING", "COMPLETED", "FAILED"]
    }
  ]
}
```

---

## Step 2: Entity (Aggregate Root) 생성

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/Setting.java`

**출력 경로**: `domain/src/main/java/com/ryuqq/fileflow/domain/{package}/{AggregateRoot}.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {AggregateRoot} Entity를 생성하세요:

**필수 구조** (Setting.java 참고):

1. **Private 전체 생성자** (reconstitute 전용)
   ```java
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
   ```

2. **Package-private 주요 생성자** (검증 포함)
   ```java
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
   ```

3. **Static Factory Methods**:
   ```java
   // 신규 생성 (ID 없음)
   public static {AggregateRoot} forNew({Field1} field1, {Field2} field2) {
       return new {AggregateRoot}(null, field1, field2, Clock.systemDefaultZone());
   }

   // ID 있는 생성 (테스트용)
   public static {AggregateRoot} of({AggregateRoot}Id id, {Field1} field1, {Field2} field2) {
       if (id == null) {
           throw new IllegalArgumentException("{AggregateRoot} ID는 필수입니다");
       }
       return new {AggregateRoot}(id, field1, field2, Clock.systemDefaultZone());
   }

   // DB 재구성
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
   ```

4. **Private static 검증 메서드**:
   ```java
   private static void validateRequiredFields({Field1} field1, {Field2} field2) {
       if (field1 == null) {
           throw new IllegalArgumentException("{Field1}은(는) 필수입니다");
       }
       if (field2 == null) {
           throw new IllegalArgumentException("{Field2}은(는) 필수입니다");
       }
       // 도메인 규칙 검증
   }
   ```

5. **Law of Demeter 준수 메서드**:
   ```java
   // ❌ Bad: entity.getId().value()
   // ✅ Good: entity.getIdValue()
   public Long getIdValue() {
       return id != null ? id.value() : null;
   }

   // ❌ Bad: entity.getField1().getValue()
   // ✅ Good: entity.getField1Value()
   public String getField1Value() {
       return field1.getValue();
   }
   ```

6. **Tell Don't Ask 비즈니스 로직**:
   ```java
   // ❌ Bad: entity.setValue(newValue); entity.setUpdatedAt(LocalDateTime.now());
   // ✅ Good: entity.updateValue(newValue);
   public void updateValue(NewValue newValue) {
       if (newValue == null) {
           throw new IllegalArgumentException("값은 null일 수 없습니다");
       }

       this.value = newValue;
       this.updatedAt = LocalDateTime.now(clock);
   }

   // ❌ Bad: entity.getStatus() == Status.COMPLETED
   // ✅ Good: entity.isCompleted()
   public boolean isCompleted() {
       return this.status == Status.COMPLETED;
   }
   ```

7. **equals/hashCode** (ID로만 비교):
   ```java
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
   ```

8. **toString()**:
   ```java
   @Override
   public String toString() {
       return "{AggregateRoot}{" +
           "id=" + id +
           ", field1=" + field1 +
           ", field2=" + field2 +
           '}';
   }
   ```

9. **Javadoc**:
   ```java
   /**
    * {AggregateRoot} Aggregate Root
    *
    * <p>{설명}</p>
    *
    * <p><strong>규칙 준수:</strong></p>
    * <ul>
    *   <li>❌ Lombok 사용 안함 - Pure Java</li>
    *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
    *   <li>✅ Tell, Don't Ask 패턴 적용</li>
    *   <li>✅ Long FK 전략 - JPA 관계 어노테이션 금지</li>
    *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
    * </ul>
    *
    * @author ryu-qqq
    * @since 2025-10-30
    */
   ```

**절대 금지**:
- ❌ Lombok 사용 (@Data, @Getter, @Setter, @Builder)
- ❌ Public 생성자
- ❌ Getter 체이닝
- ❌ JPA 관계 어노테이션 (@ManyToOne, @OneToMany 등)

**모범 사례 파일을 참고하여 동일한 스타일로 작성하세요.**
```

---

## Step 3: Value Object 생성

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/SettingKey.java`, `SettingValue.java`

**출력 경로**: `domain/src/main/java/com/ryuqq/fileflow/domain/{package}/{ValueObject}.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {ValueObject} Value Object를 생성하세요:

**필수 구조** (SettingKey.java, SettingValue.java 참고):

1. **Private 생성자**:
   ```java
   private {ValueObject}(String value) {
       validateValue(value);
       this.value = value;
   }
   ```

2. **Static Factory Method**:
   ```java
   public static {ValueObject} of(String value) {
       return new {ValueObject}(value);
   }

   // 특수 케이스 (SettingValue.secret() 참고)
   public static {ValueObject} special(String value) {
       return new {ValueObject}(value, true);
   }
   ```

3. **모든 필드 final** (완전 불변):
   ```java
   private final String value;
   private final boolean isSpecial;
   ```

4. **Private static 검증 메서드**:
   ```java
   private static void validateValue(String value) {
       if (value == null || value.isBlank()) {
           throw new IllegalArgumentException("값은 필수입니다");
       }
       // 도메인 규칙 검증
   }
   ```

5. **Law of Demeter 메서드**:
   ```java
   // ❌ Bad: vo1.getValue().equals(vo2.getValue())
   // ✅ Good: vo1.isSameAs(vo2)
   public boolean isSameAs({ValueObject} other) {
       if (other == null) {
           return false;
       }
       return this.value.equals(other.value);
   }
   ```

6. **불변성 유지 메서드** (withNewValue):
   ```java
   // ❌ Bad: setValue(newValue) - 기존 객체 변경
   // ✅ Good: withNewValue(newValue) - 새 객체 반환
   public {ValueObject} withNewValue(String newValue) {
       return new {ValueObject}(newValue, this.isSpecial);
   }
   ```

7. **equals/hashCode** (모든 필드 비교):
   ```java
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
   ```

8. **toString()**:
   ```java
   @Override
   public String toString() {
       return value;
   }
   ```

9. **Javadoc**:
   ```java
   /**
    * {ValueObject} Value Object
    *
    * <p>{설명}</p>
    *
    * <p><strong>규칙 준수:</strong></p>
    * <ul>
    *   <li>❌ Lombok 사용 안함 - Pure Java</li>
    *   <li>✅ Value Object 불변성 - final 필드</li>
    *   <li>✅ Law of Demeter - 캡슐화된 행동</li>
    *   <li>✅ Domain 규칙 검증 - 생성 시 유효성 검증</li>
    * </ul>
    *
    * @author ryu-qqq
    * @since 2025-10-30
    */
   ```

**모범 사례 파일을 참고하여 동일한 스타일로 작성하세요.**
```

---

## Step 4: Record (ID) 생성

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/SettingId.java`

**출력 경로**: `domain/src/main/java/com/ryuqq/fileflow/domain/{package}/{AggregateRoot}Id.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {AggregateRoot}Id Record를 생성하세요:

**필수 구조** (SettingId.java 참고):

1. **Java 21 Record**:
   ```java
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

2. **Javadoc**:
   ```java
   /**
    * {AggregateRoot} 식별자
    *
    * <p>{AggregateRoot}의 고유 식별자를 나타내는 Value Object입니다.
    * Java 21 Record를 사용하여 불변성을 보장합니다.</p>
    *
    * <p><strong>설계 원칙:</strong></p>
    * <ul>
    *   <li>✅ Value Object 패턴 적용</li>
    *   <li>✅ 불변성 보장 (Java Record)</li>
    *   <li>✅ null 허용 (신규 엔티티 생성 시)</li>
    *   <li>✅ 양수 검증 (기존 엔티티)</li>
    * </ul>
    *
    * @param value {AggregateRoot} ID 값 (Long - AUTO_INCREMENT)
    * @author ryu-qqq
    * @since 2025-10-30
    */
   ```

**모범 사례 파일을 참고하여 동일한 스타일로 작성하세요.**
```

---

## Step 5: Enum 생성

**모범 사례**: `domain/src/main/java/com/ryuqq/fileflow/domain/settings/SettingLevel.java`, `SettingType.java`

**출력 경로**: `domain/src/main/java/com/ryuqq/fileflow/domain/{package}/{Enum}.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {Enum} Enum을 생성하세요:

**필수 구조** (SettingLevel.java, SettingType.java 참고):

1. **Private final 필드 + Package-private 생성자**:
   ```java
   public enum {Enum} {
       VALUE1(param1),
       VALUE2(param2),
       VALUE3(param3);

       private final int priority; // 또는 다른 필드

       {Enum}(int priority) {
           this.priority = priority;
       }

       public int getPriority() {
           return priority;
       }
   }
   ```

2. **Law of Demeter 메서드**:
   ```java
   // ❌ Bad: enum1.getPriority() < enum2.getPriority() ? enum1 : enum2
   // ✅ Good: {Enum}.higherPriority(enum1, enum2)
   public static {Enum} higherPriority({Enum} enum1, {Enum} enum2) {
       if (enum1 == null) {
           return enum2;
       }
       if (enum2 == null) {
           return enum1;
       }
       return enum1.