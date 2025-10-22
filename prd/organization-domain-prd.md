# Organization Domain Aggregate PRD

## 목표
Organization 도메인 모델을 Pure Java로 구현하여 멀티테넌시 시스템의 조직 관리 Aggregate를 완성합니다.

## 비즈니스 컨텍스트
FileFlow 시스템에서 Organization은 Tenant 하위의 논리적 조직 단위를 담당합니다.
각 Organization은 Tenant에 종속되며, 여러 User를 포함할 수 있습니다.
Long FK 전략을 사용하여 Tenant와의 관계를 표현합니다.

## 구현 클래스

### 1. Organization.java (Aggregate Root)
**역할**: Organization 집합 루트
**책임**:
- Organization의 생명주기 관리
- 이름 변경 (updateName)
- 상태 관리 (deactivate)
- 소프트 삭제 (softDelete)
- 활성 상태 조회 (isActive)

**필수 필드**:
- `OrganizationId id` (final) - Organization 식별자
- `Long tenantId` (final) - Tenant 참조 (Long FK 전략)
- `OrgCode orgCode` - 조직 코드 (Value Object)
- `String name` - 조직 이름
- `OrganizationStatus status` - 현재 상태 (ACTIVE/INACTIVE)
- `LocalDateTime createdAt` (final) - 생성 시각
- `LocalDateTime updatedAt` - 수정 시각
- `boolean deleted` - 소프트 삭제 플래그

### 2. OrganizationId.java (Value Object, Record)
**역할**: Organization 식별자
**타입**: Java 21 Record
**필드**: `Long value`
**검증**: null 및 음수 방지

### 3. OrgCode.java (Value Object)
**역할**: 조직 코드 캡슐화
**비즈니스 규칙**:
- 최소 길이: 2자
- 최대 길이: 20자
- 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)만 허용
- null 및 공백 불가
- 자동 대문자 변환

**필드**:
- `String value` (final)

**메서드**:
- 생성자: 유효성 검증 및 정규화
- `getValue()`: 값 반환
- `equals()`, `hashCode()`: 동등성 비교

### 4. OrganizationStatus.java (Enum)
**역할**: Organization 상태 정의
**값**:
- `ACTIVE` - 활성 상태 (정상 운영)
- `INACTIVE` - 비활성 상태 (사용 중지)

## 핵심 비즈니스 메서드

### updateName(String newName)
**목적**: Organization 이름 변경
**선행조건**: deleted == false
**후행조건**: name 변경, updatedAt 갱신
**예외**: Organization이 삭제된 경우 IllegalStateException

### deactivate()
**목적**: Organization 비활성화
**선행조건**: status == ACTIVE, deleted == false
**후행조건**: status = INACTIVE, updatedAt 갱신
**예외**: 이미 INACTIVE이거나 삭제된 경우 IllegalStateException

### softDelete()
**목적**: Organization 소프트 삭제
**선행조건**: deleted == false
**후행조건**: deleted = true, status = INACTIVE, updatedAt 갱신
**예외**: 이미 삭제된 경우 IllegalStateException

### isActive()
**목적**: 활성 상태 확인 (Law of Demeter 준수)
**반환**: deleted == false && status == ACTIVE

### belongsToTenant(Long tenantId)
**목적**: 특정 Tenant 소속 여부 확인 (Law of Demeter 준수)
**반환**: this.tenantId.equals(tenantId)

## Definition of Done (DoD)

### 필수 규칙
- ✅ **Lombok 절대 금지**: Pure Java getter/setter 직접 작성
- ✅ **Law of Demeter 준수**: Getter 체이닝 방지, Tell Don't Ask 패턴
- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지 (`@ManyToOne` 등)
- ✅ **Javadoc 완전성**: 모든 public 클래스/메서드에 `@author`, `@since` 포함
- ✅ **불변성**: 중요 필드는 `final` 선언
- ✅ **캡슐화**: 내부 상태를 직접 노출하지 않고 메서드로 제공

### 테스트 요구사항
- OrganizationId 유효성 검증 테스트
- OrgCode 유효성 검증 테스트 (길이, 형식, 정규화)
- Organization 생명주기 테스트 (생성, 이름 변경, 비활성화, 삭제)
- Tenant 소속 확인 테스트 (belongsToTenant)
- 예외 시나리오 테스트 (중복 상태 전환, 삭제된 Organization 접근)

## 패키지 구조
```
domain/src/main/java/com/ryuqq/fileflow/domain/iam/organization/
├── Organization.java           # Aggregate Root
├── OrganizationId.java         # Value Object (Record)
├── OrgCode.java                # Value Object
└── OrganizationStatus.java     # Enum
```

## 사용 예시
```java
// Organization 생성
OrganizationId id = new OrganizationId(1L);
Long tenantId = 100L;  // Long FK 전략
OrgCode orgCode = new OrgCode("SALES-KR");
Organization org = new Organization(id, tenantId, orgCode, "Sales Korea");

// 이름 변경
org.updateName("Sales Korea Division");

// Tenant 소속 확인 (Law of Demeter)
if (org.belongsToTenant(100L)) {
    // 비즈니스 로직
}

// 활성 상태 확인 (Law of Demeter)
if (org.isActive()) {
    // 비즈니스 로직
}

// 비활성화
org.deactivate();

// 소프트 삭제
org.softDelete();
```

## Long FK 전략 중요 사항

**❌ 금지**:
```java
@ManyToOne
@JoinColumn(name = "tenant_id")
private Tenant tenant;  // JPA 관계 어노테이션 사용 금지
```

**✅ 올바른 방식**:
```java
private final Long tenantId;  // Long FK만 저장

public boolean belongsToTenant(Long tenantId) {
    return this.tenantId.equals(tenantId);
}
```

## 검증 포인트

1. **JPA 관계 어노테이션 미사용**: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany` 절대 금지
2. **Long FK 전략**: 다른 Aggregate 참조는 Long 타입 ID만 사용
3. **Lombok 미사용**: `@Data`, `@Getter`, `@Setter`, `@Builder` 등 금지
4. **Law of Demeter**: `organization.getTenant().getName()` 형태 금지
5. **Tell, Don't Ask**: `belongsToTenant()`, `isActive()` 같은 질의 메서드 제공
