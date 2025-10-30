---
description: persistence layer Entity 보일러 템플릿 를 CC에 준수하여 만든다
---

# JPA Entity Generator (Coding Convention)
**목적**: PRD 문서 기반으로 JPA 엔티티를 자동 생성하며, 모든 코딩 컨벤션을 준수합니다.

**모범 사례**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/tenant/entity/TenantJpaEntity.java`

**검증**: ArchUnit (`JpaEntityConventionTest.java`)
---

## 사용법

```bash
# Windsurf에서 실행
/cc-entity prd/your-feature.md
```

---

## Step 1: PRD 문서 분석

**입력**:
- PRD 문서 경로 (예: `prd/crawling.md`)
- 코딩 컨벤션 문서 (`docs/coding_convention/04-persistence-layer/**/*.md`)

**출력**:
- JPA Entity Specification (JSON)

**프롬프트**:
```
PRD 문서를 분석하여 다음을 추출하세요:

1. **JPA 엔티티 후보**
   - 데이터베이스 테이블과 매핑되는 객체
   - 예: Tenant, Example, Settings

2. **필드 목록**
   - 필드명, 타입, 제약 조건
   - 예: name (String, NOT NULL, 200자), status (Enum, NOT NULL)

3. **Enum 타입**
   - 상태, 타입, 레벨 등
   - 예: TenantStatus (ACTIVE, SUSPENDED), ExampleStatus (ACTIVE, INACTIVE, DELETED)

4. **외래키 (Long FK 전략)**
   - JPA 관계 어노테이션 사용 안 함
   - 예: userId (Long), tenantId (Long)

5. **테이블 정보**
   - 테이블명
   - 인덱스 정보 (선택)

6. **ID 생성 전략**
   - GenerationType.IDENTITY (MySQL AUTO_INCREMENT)

출력 형식:
{
  "package": "crawling",
  "entities": [
    {
      "name": "Crawling",
      "tableName": "crawling",
      "description": "크롤링 JPA Entity",
      "fields": [
        {
          "name": "id",
          "type": "Long",
          "columnName": "id",
          "nullable": false,
          "isPrimaryKey": true
        },
        {
          "name": "url",
          "type": "String",
          "columnName": "url",
          "nullable": false,
          "length": 500
        },
        {
          "name": "status",
          "type": "CrawlingStatus",
          "columnName": "status",
          "nullable": false,
          "isEnum": true,
          "enumType": "STRING"
        },
        {
          "name": "userId",
          "type": "Long",
          "columnName": "user_id",
          "nullable": true,
          "isFk": true,
          "fkReference": "User"
        }
      ],
      "enums": [
        {
          "name": "CrawlingStatus",
          "values": ["PENDING", "RUNNING", "COMPLETED", "FAILED"]
        }
      ]
    }
  ]
}
```

---

## Step 2: JPA Entity 생성

**모범 사례**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/tenant/entity/TenantJpaEntity.java`

**출력 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/adapter/out/persistence/{package}/entity/{Entity}JpaEntity.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {Entity}JpaEntity를 생성하세요:

**필수 구조** (TenantJpaEntity.java 참고):

1. **Package 및 Import**:
   ```java
   package com.ryuqq.adapter.out.persistence.{package}.entity;

   import jakarta.persistence.*;
   import com.ryuqq.adapter.out.persistence.common.entity.BaseAuditEntity;
   import com.ryuqq.domain.{package}.{Enum};
   import java.time.LocalDateTime;
   ```

2. **Class Javadoc**:
   ```java
   /**
    * {Entity} JPA Entity (Persistence Layer)
    *
    * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
    * <p><strong>위치</strong>: adapter-out/persistence-mysql/{package}/entity/</p>
    * <p><strong>변환</strong>: {@code {Entity}Mapper}를 통해 Domain {@code {Entity}}와 상호 변환</p>
    *
    * <h3>설계 원칙</h3>
    * <ul>
    *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
    *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
    *   <li>✅ Getter만 제공 (Setter 금지)</li>
    *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
    *   <li>❌ Lombok 금지</li>
    *   <li>❌ JPA 관계 어노테이션 금지</li>
    * </ul>
    *
    * @see com.ryuqq.domain.{package}.{Entity} Domain Model
    * @since 1.0.0
    */
   ```

3. **@Entity, @Table 어노테이션**:
   ```java
   @Entity
   @Table(name = "{table_name}")
   public class {Entity}JpaEntity extends BaseAuditEntity {
   ```

4. **필드 선언**:
   ```java
   // ID 필드 (Primary Key)
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Long id;

   // 일반 필드
   @Column(name = "{column_name}", nullable = false, length = {length})
   private String {fieldName};

   // Enum 필드
   @Enumerated(EnumType.STRING)
   @Column(name = "{column_name}", nullable = false, length = 20)
   private {Enum} {enumField};

   // 외래키 필드 (Long FK 전략)
   @Column(name = "{fk_column_name}")
   private Long {fkField}Id;
   ```

5. **Protected 기본 생성자** (JPA 전용):
   ```java
   /**
    * JPA 전용 기본 생성자 (Protected)
    *
    * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
    */
   protected {Entity}JpaEntity() {
       super();
   }
   ```

6. **Protected 신규 생성용 생성자** (PK 없음):
   ```java
   /**
    * 신규 생성용 생성자 (Protected - PK 없음)
    *
    * <p>새로운 Entity 생성 시 사용합니다. ID는 DB에서 자동 생성됩니다.</p>
    */
   protected {Entity}JpaEntity(
       String {fieldName},
       {Enum} {enumField},
       Long {fkField}Id,
       LocalDateTime createdAt,
       LocalDateTime updatedAt
   ) {
       super(createdAt, updatedAt);
       this.{fieldName} = {fieldName};
       this.{enumField} = {enumField};
       this.{fkField}Id = {fkField}Id;
   }
   ```

7. **Private 재구성용 생성자** (PK 포함):
   ```java
   /**
    * 재구성용 생성자 (Private - PK 포함)
    *
    * <p>DB 조회 결과를 Entity로 재구성할 때 사용합니다.</p>
    */
   private {Entity}JpaEntity(
       Long id,
       String {fieldName},
       {Enum} {enumField},
       Long {fkField}Id,
       LocalDateTime createdAt,
       LocalDateTime updatedAt
   ) {
       super(createdAt, updatedAt);
       this.id = id;
       this.{fieldName} = {fieldName};
       this.{enumField} = {enumField};
       this.{fkField}Id = {fkField}Id;
   }
   ```

8. **Public Static Factory Method: create()** (신규 생성):
   ```java
   /**
    * 새로운 {Entity} Entity 생성 (Static Factory Method)
    *
    * <p>신규 {Entity} 생성 시 사용합니다. 초기 상태는 {DEFAULT_STATUS}입니다.</p>
    *
    * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
    *
    * @param {fieldName} {필드 설명}
    * @param {fkField}Id {FK 설명}
    * @param createdAt 생성 일시
    * @return 새로운 {Entity}JpaEntity (id는 null, save 후 자동 할당)
    * @throws IllegalArgumentException 필수 필드가 null인 경우
    */
   public static {Entity}JpaEntity create(
       String {fieldName},
       Long {fkField}Id,
       LocalDateTime createdAt
   ) {
       if ({fieldName} == null || createdAt == null) {
           throw new IllegalArgumentException("Required fields must not be null");
       }

       return new {Entity}JpaEntity(
           {fieldName},
           {Enum}.{DEFAULT_VALUE},  // 기본 상태
           {fkField}Id,
           createdAt,
           createdAt  // updatedAt = createdAt (초기값)
       );
   }
   ```

9. **Public Static Factory Method: reconstitute()** (DB 재구성):
   ```java
   /**
    * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
    *
    * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
    *
    * @param id {Entity} ID (Long - AUTO_INCREMENT)
    * @param {fieldName} {필드 설명}
    * @param {enumField} {Enum 설명}
    * @param {fkField}Id {FK 설명}
    * @param createdAt 생성 일시
    * @param updatedAt 최종 수정 일시
    * @return 재구성된 {Entity}JpaEntity
    */
   public static {Entity}JpaEntity reconstitute(
       Long id,
       String {fieldName},
       {Enum} {enumField},
       Long {fkField}Id,
       LocalDateTime createdAt,
       LocalDateTime updatedAt
   ) {
       return new {Entity}JpaEntity(
           id, {fieldName}, {enumField}, {fkField}Id, createdAt, updatedAt
       );
   }
   ```

10. **Getters** (Public, 비즈니스 메서드 없음):
    ```java
    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public Long getId() {
        return id;
    }

    public String get{FieldName}() {
        return {fieldName};
    }

    public {Enum} get{EnumField}() {
        return {enumField};
    }

    public Long get{FkField}Id() {
        return {fkField}Id;
    }
    ```

**절대 금지**:
- ❌ Lombok 사용 (@Data, @Getter, @Setter, @Builder)
- ❌ Public 생성자 (protected 또는 private만 허용)
- ❌ JPA 관계 어노테이션 (@ManyToOne, @OneToMany, @OneToOne, @ManyToMany)
- ❌ Setter 메서드
- ❌ 비즈니스 로직 메서드
- ❌ final 필드 (JPA 프록시 제약)
- ❌ EnumType.ORDINAL (EnumType.STRING만 사용)

**모범 사례 파일을 참고하여 동일한 스타일로 작성하세요.**
```

---

## Step 3: Fixture 클래스 생성

**모범 사례**: `adapter-out/persistence-mysql/src/testFixtures/java/com/ryuqq/adapter/out/persistence/example/fixture/ExampleJpaEntityFixture.java`

**출력 경로**: `adapter-out/persistence-mysql/src/testFixtures/java/com/ryuqq/adapter/out/persistence/{package}/fixture/{Entity}JpaEntityFixture.java`

**프롬프트**:
```
다음 모범 사례를 100% 따라서 {Entity}JpaEntityFixture를 생성하세요:

**필수 구조** (ExampleJpaEntityFixture.java 참고):

1. **Class Javadoc**:
   ```java
   /**
    * {Entity}JpaEntity 테스트 Fixture
    *
    * <p>테스트에서 {Entity}JpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
    *
    * <h3>네이밍 규칙:</h3>
    * <ul>
    *   <li>클래스명: {@code *Fixture} 접미사 필수</li>
    *   <li>기본 생성 메서드: {@code create*()} - 기본값으로 객체 생성</li>
    *   <li>커스터마이징 메서드: {@code create*With*()} - 특정 값 지정하여 생성</li>
    * </ul>
    *
    * <h3>사용 예시:</h3>
    * <pre>{@code
    * // 기본값으로 생성 (ID 없음 - 저장 전)
    * {Entity}JpaEntity entity = {Entity}JpaEntityFixture.create();
    *
    * // 특정 값으로 생성
    * {Entity}JpaEntity entity = {Entity}JpaEntityFixture.createWith{Field}("Custom Value");
    *
    * // ID 포함하여 생성 (저장 후 시나리오)
    * {Entity}JpaEntity entity = {Entity}JpaEntityFixture.createWithId(123L, "Value");
    * }</pre>
    *
    * @author Claude Code
    * @since 1.0.0
    * @see {Entity}JpaEntity
    */
   ```

2. **기본 생성 메서드** (ID 없음):
   ```java
   public static {Entity}JpaEntity create() {
       return createWith{PrimaryField}("Test {Field}");
   }
   ```

3. **특정 필드로 생성**:
   ```java
   public static {Entity}JpaEntity createWith{Field}(String {field}) {
       LocalDateTime now = LocalDateTime.now();
       return new {Entity}JpaEntity(
           null,  // ID는 JPA가 자동 생성
           {field},
           {Enum}.{DEFAULT_VALUE},
           null,  // FK는 null 가능
           now,
           now
       );
   }
   ```

4. **ID 포함 생성** (저장 후 시나리오):
   ```java
   public static {Entity}JpaEntity createWithId(Long id, String {field}) {
       LocalDateTime now = LocalDateTime.now();
       return new {Entity}JpaEntity(
           id,
           {field},
           {Enum}.{DEFAULT_VALUE},
           null,
           now,
           now
       );
   }
   ```

5. **ID와 상태 지정 생성**:
   ```java
   public static {Entity}JpaEntity createWithIdAnd{Enum}(
       Long id,
       String {field},
       {Enum} {enum}
   ) {
       LocalDateTime now = LocalDateTime.now();
       return new {Entity}JpaEntity(
           id,
           {field},
           {enum},
           null,
           now,
           now
       );
   }
   ```

6. **대량 생성 메서드** (테스트용):
   ```java
   public static List<{Entity}JpaEntity> createMultiple(int count) {
       List<{Entity}JpaEntity> entities = new ArrayList<>();
       for (int i = 0; i < count; i++) {
           entities.add(createWith{Field}("Test {Field} " + (i + 1)));
       }
       return entities;
   }

   public static List<{Entity}JpaEntity> createMultipleWithId(long startId, int count) {
       List<{Entity}JpaEntity> entities = new ArrayList<>();
       for (int i = 0; i < count; i++) {
           entities.add(createWithId(startId + i, "Test {Field} " + (i + 1)));
       }
       return entities;
   }
   ```

7. **Private 생성자** (Utility 클래스):
   ```java
   // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
   private {Entity}JpaEntityFixture() {
       throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
   }
   ```

**모범 사례 파일을 참고하여 동일한 스타일로 작성하세요.**
```

---

## Step 4: ArchUnit 검증 실행

**검증 클래스**: `bootstrap/bootstrap-web-api/src/test/java/com/ryuqq/bootstrap/architecture/JpaEntityConventionTest.java`

**프롬프트**:
```bash
# Gradle 테스트 실행
./gradlew :bootstrap:bootstrap-web-api:test --tests "com.ryuqq.bootstrap.architecture.JpaEntityConventionTest"

# 출력 예시:
# ✅ JpaEntityConventionTest > Lombok 금지 규칙 > JPA 엔티티는 Lombok @Data를 사용하지 않아야 함 PASSED
# ✅ JpaEntityConventionTest > BaseAuditEntity 상속 규칙 > 모든 JPA 엔티티는 BaseAuditEntity를 상속해야 함 PASSED
# ✅ JpaEntityConventionTest > Long FK