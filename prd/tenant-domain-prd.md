# Tenant Domain Aggregate PRD

## 목표
Tenant 도메인 모델을 Pure Java로 구현하여 멀티테넌시 시스템의 핵심 Aggregate를 완성합니다.

## 비즈니스 컨텍스트
FileFlow 시스템은 멀티테넌트 SaaS 아키텍처로 설계되며, Tenant는 최상위 논리적 경계를 담당합니다.
각 Tenant는 독립된 데이터 공간을 가지며, 조직(Organization)과 사용자(User)를 포함합니다.

## 구현 클래스

### 1. Tenant.java (Aggregate Root)
**역할**: Tenant 집합 루트
**책임**:
- Tenant의 생명주기 관리
- 이름 변경 (updateName)
- 상태 관리 (suspend, activate)
- 소프트 삭제 (softDelete)
- 활성 상태 조회 (isActive)

**필수 필드**:
- `TenantId id` (final) - Tenant 식별자
- `TenantName name` - Tenant 이름 (Value Object)
- `TenantStatus status` - 현재 상태 (ACTIVE/SUSPENDED)
- `LocalDateTime createdAt` (final) - 생성 시각
- `LocalDateTime updatedAt` - 수정 시각
- `boolean deleted` - 소프트 삭제 플래그

### 2. TenantId.java (Value Object, Record)
**역할**: Tenant 식별자
**타입**: Java 21 Record
**검증**: null 및 빈 문자열 방지

### 3. TenantName.java (Value Object)
**역할**: Tenant 이름 캡슐화
**비즈니스 규칙**:
- 최소 길이: 2자
- 최대 길이: 50자
- null 및 공백 불가
- 앞뒤 공백 자동 제거

**필드**:
- `String value` (final)

**메서드**:
- 생성자: 유효성 검증
- `getValue()`: 값 반환
- `equals()`, `hashCode()`: 동등성 비교

### 4. TenantStatus.java (Enum)
**역할**: Tenant 상태 정의
**값**:
- `ACTIVE` - 활성 상태 (정상 운영)
- `SUSPENDED` - 일시 정지 (결제 문제, 정책 위반 등)

## 핵심 비즈니스 메서드

### updateName(TenantName newName)
**목적**: Tenant 이름 변경
**선행조건**: deleted == false
**후행조건**: name 변경, updatedAt 갱신
**예외**: Tenant가 삭제된 경우 IllegalStateException

### suspend()
**목적**: Tenant 일시 정지
**선행조건**: status == ACTIVE, deleted == false
**후행조건**: status = SUSPENDED, updatedAt 갱신
**예외**: 이미 SUSPENDED이거나 삭제된 경우 IllegalStateException

### activate()
**목적**: Tenant 활성화
**선행조건**: status == SUSPENDED, deleted == false
**후행조건**: status = ACTIVE, updatedAt 갱신
**예외**: 이미 ACTIVE이거나 삭제된 경우 IllegalStateException

### softDelete()
**목적**: Tenant 소프트 삭제
**선행조건**: deleted == false
**후행조건**: deleted = true, status = SUSPENDED, updatedAt 갱신
**예외**: 이미 삭제된 경우 IllegalStateException

### isActive()
**목적**: 활성 상태 확인 (Law of Demeter 준수)
**반환**: deleted == false && status == ACTIVE

## Definition of Done (DoD)

### 필수 규칙
- ✅ **Lombok 절대 금지**: Pure Java getter/setter 직접 작성
- ✅ **Law of Demeter 준수**: Getter 체이닝 방지, Tell Don't Ask 패턴
- ✅ **Javadoc 완전성**: 모든 public 클래스/메서드에 `@author`, `@since` 포함
- ✅ **불변성**: 중요 필드는 `final` 선언
- ✅ **캡슐화**: 내부 상태를 직접 노출하지 않고 메서드로 제공

### 테스트 요구사항
- TenantId 유효성 검증 테스트
- TenantName 유효성 검증 테스트 (길이, null, 공백)
- Tenant 생명주기 테스트 (생성, 이름 변경, 상태 전환, 삭제)
- 예외 시나리오 테스트 (중복 상태 전환, 삭제된 Tenant 접근)

## 패키지 구조
```
domain/src/main/java/com/company/template/domain/model/tenant/
├── Tenant.java           # Aggregate Root
├── TenantId.java         # Value Object (Record)
├── TenantName.java       # Value Object
└── TenantStatus.java     # Enum
```

## 사용 예시
```java
// Tenant 생성
TenantId id = new TenantId("tenant-001");
TenantName name = new TenantName("Acme Corporation");
Tenant tenant = new Tenant(id, name);

// 이름 변경
tenant.updateName(new TenantName("Acme Corp"));

// 일시 정지
tenant.suspend();

// 재활성화
tenant.activate();

// 활성 상태 확인 (Law of Demeter)
if (tenant.isActive()) {
    // 비즈니스 로직
}

// 소프트 삭제
tenant.softDelete();
```
