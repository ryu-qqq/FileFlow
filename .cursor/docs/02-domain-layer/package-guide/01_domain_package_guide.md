# DOMAIN 패키지 가이드

> 순수 도메인 모델 레이어. **프레임워크 의존 금지**, **불변성·명세 중심**.
> **중요**: Domain 모듈에는 **순수 도메인 객체만** 포함됩니다. Port(인터페이스), Service, Repository는 **Application 모듈**에 위치합니다.

## 🎯 핵심 원칙

> **비즈니스 로직과 데이터 변경은 무조건 Domain Layer에서만!**
>
> Application은 흐름 연결, Persistence는 저장/조회만.
> 상세한 Layer별 책임은 [비즈니스 로직 배치 원칙](../_shared/business-logic-placement.md) 참조.

## 디렉터리 구조

### ✅ 권장 구조 (심플)
```
domain/
├─ common/
│  ├─ DomainEvent.java       # 인터페이스
│  ├─ ErrorCode.java          # 에러 코드 enum
│  ├─ DomainException.java    # 기본 예외 클래스
│  └─ exception/              # 공통 예외 (여러 Aggregate에서 사용)
│     ├─ InvalidValueException.java
│     └─ ResourceNotFoundException.java
└─ [boundedContext]/
   └─ [aggregateName]/
      ├─ [AggregateRoot].java      # 예: Order.java
      ├─ [Entity].java              # 예: OrderLineItem.java
      ├─ [Id].java                  # 예: OrderId.java (Value Object)
      ├─ [ValueObject].java         # 예: Money.java, Address.java
      ├─ [DomainEvent].java         # 예: OrderCreatedEvent.java
      └─ exception/                 # Aggregate 전용 예외
         ├─ OrderNotFoundException.java
         └─ InvalidOrderStateException.java
```

### 📝 설계 원칙
1. **ID는 Entity 옆에**: `OrderId.java`는 `Order.java` 옆에 위치
2. **VO는 Entity 옆에**: `Money.java`, `Address.java` 등 모두 Aggregate 디렉터리에
3. **Event는 Entity 옆에**: `OrderCreatedEvent.java`는 `Order.java` 옆에
4. **Exception만 패키지 분리**: 여러 클래스가 생길 수 있으므로 `exception/` 디렉터리
5. **Factory 불필요**: `forNew()`, `of()`, `create()` static method로 대체

## 포함할 객체 & 역할
- **Aggregate Root / Entity**: 트랜잭션 경계, 상태 전이, 불변성 보장
- **Value Object(VO)**: 값 동등성/불변/검증/행동 포함(가능하면 `record`)
  - **ID (식별자)**: `OrderId`, `UserId` 등 - Value Object의 일종
  - **일반 VO**: `Money`, `Address`, `Email` 등 - Entity 옆에 배치
- **Domain Event**: 상태 전이 결과(발생 시각, 관련 식별자 포함)
  - `common/DomainEvent.java`: 인터페이스
  - Aggregate별 Event: Entity 옆에 배치
- **Exception**: 도메인 규칙 위반 시 발생
  - `common/exception/`: 공통 예외 (여러 Aggregate에서 사용)
  - `[aggregate]/exception/`: Aggregate 전용 예외

### ❌ 제거된 패키지
- **~~Policy~~**: Entity 메서드 또는 Domain Service로 대체
- **~~Factory~~**: `forNew()`, `of()`, `create()` static method로 대체

## Application 모듈에 위치하는 것들
- **Port (인터페이스)**: Inbound Port (UseCase), Outbound Port (Repository, External API)
- **Application Service**: UseCase 구현, 트랜잭션 경계, 오케스트레이션
- **DTO/Mapper**: 계층 간 데이터 변환

## 허용/금지 의존
- **허용**: `java.*`, 내부 도메인 코드
- **금지**: `org.springframework.*`, `jakarta.persistence.*`, `com.fasterxml.jackson.*`, `lombok.*`
- 외부 시스템(I/O) 접근 금지

## 네이밍 규약
- **Aggregate Root / Entity**: 단수 명사 (`Order`, `Session`)
- **ID (Value Object)**: `<Entity>Id` (예: `OrderId`, `UserId`)
- **Value Object**: 의미 있는 명사 (`Money`, `Address`, `Email`)
- **Domain Event**: `<Aggregate><PastTense>Event` (예: `OrderCreatedEvent`, `PaymentCompletedEvent`)
- **Exception**: `<Condition>Exception` (예: `OrderNotFoundException`, `InvalidOrderStateException`)
- **Static Factory Method**: `forNew()`, `of()`, `create()`, `reconstitute()`

## Do / Don't

### ✅ Do
- **Static Factory Method 사용**: `forNew()`, `of()`, `create()`, `reconstitute()`
  ```java
  // ✅ Good: VO 자체에 static factory method
  public record OrderId(Long value) {
      public static OrderId of(Long value) { ... }
  }

  public class Order {
      public static Order forNew(OrderId id, ...) { ... }
      public static Order reconstitute(OrderId id, ...) { ... }
  }
  ```
- **생성 시 검증**: 불변성 위반 시 도메인 예외 throw
- **값 기반 equals/hashCode**: `record` 사용 권장

### ❌ Don't
- **별도 Factory 클래스 생성 금지**: `OrderFactory.java` 대신 `Order` 클래스의 static method 사용
- **Policy 패키지 생성 금지**: Entity 메서드 또는 Domain Service로 처리
- **public setter 금지**: 불변성 보장
- **가변 컬렉션 노출 금지**: `List.copyOf()` 또는 `Collections.unmodifiableList()` 사용
- **프레임워크 어노테이션 금지**: JPA/Jackson/Lombok 의존 금지
- **외부 API 호출 금지**: I/O 작업 금지

## ArchUnit 룰 스니펫
```java
noClasses().that().resideInAPackage("..domain..")
  .should().dependOnClassesThat().resideInAnyPackage(
    "org.springframework..","jakarta.persistence..","com.fasterxml.jackson..","lombok..");
fields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
  .and().areNotStatic().should().bePrivate().andShould().beFinal();
noMethods().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
  .and().arePublic().and().haveNameMatching("set[A-Z].*").should().beDeclared();
```
