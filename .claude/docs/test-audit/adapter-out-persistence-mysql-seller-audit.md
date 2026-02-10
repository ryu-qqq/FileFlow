# Test Audit: adapter-out persistence-mysql seller

**레이어**: adapter-out (persistence-mysql)  
**패키지**: seller  
**감사 일자**: 2025-02-06

---

## 1. 요약

| 항목 | 수 | 비고 |
|------|-----|------|
| 소스 클래스 | 44 | Adapter 13, ConditionBuilder 6, Entity 6, Mapper 6, Repository 13 (JpaRepository 6 + QueryDslRepository 7) |
| 테스트 클래스 | 10 | Adapter 5, ConditionBuilder 2, Mapper 3 |
| testFixtures | 4 | SellerJpaEntityFixtures, SellerAuthOutboxJpaEntityFixtures, SellerCsJpaEntityFixtures, SellerBusinessInfoJpaEntityFixtures |
| MISSING_TEST | 30+ | Adapter 8, ConditionBuilder 4, Entity 6, Mapper 3, QueryDslRepository 7 |
| MISSING_EDGE_CASE | 6 | 모든 Mapper toDomain(null id) |
| PATTERN_VIOLATION | 0 | - |

---

## 2. 소스 vs 테스트 매트릭스

### 2.1 Seller (메인)

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerQueryAdapter | SellerQueryAdapterTest | ✅ SellerJpaEntityFixtures | - |
| SellerCommandAdapter | SellerCommandAdapterTest | ✅ SellerJpaEntityFixtures | - |
| SellerConditionBuilder | SellerConditionBuilderTest | - (Mock/직접 인자) | - |
| SellerJpaEntityMapper | SellerJpaEntityMapperTest | ✅ SellerJpaEntityFixtures + Domain Fixtures | - |
| SellerJpaEntity | **없음** | Fixtures에 생성 로직 있음 | MED |
| SellerJpaRepository | 없음 (interface) | - | LOW |
| SellerQueryDslRepository | **없음** | - | HIGH |

### 2.2 SellerAuthOutbox

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerAuthOutboxQueryAdapter | **없음** | - | MED |
| SellerAuthOutboxCommandAdapter | SellerAuthOutboxCommandAdapterTest | ✅ SellerAuthOutboxJpaEntityFixtures | - |
| SellerAuthOutboxConditionBuilder | **없음** | - | MED |
| SellerAuthOutboxJpaEntityMapper | SellerAuthOutboxJpaEntityMapperTest | ✅ SellerAuthOutboxJpaEntityFixtures | - |
| SellerAuthOutboxJpaEntity | **없음** | Fixtures에 생성 로직 있음 | MED |
| SellerAuthOutboxJpaRepository | 없음 (interface) | - | LOW |
| SellerAuthOutboxQueryDslRepository | **없음** | - | HIGH |

### 2.3 SellerBusinessInfo

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerBusinessInfoQueryAdapter | SellerBusinessInfoQueryAdapterTest | ✅ SellerBusinessInfoJpaEntityFixtures | - |
| SellerBusinessInfoCommandAdapter | SellerBusinessInfoCommandAdapterTest | ✅ SellerBusinessInfoJpaEntityFixtures | - |
| SellerBusinessInfoConditionBuilder | SellerBusinessInfoConditionBuilderTest | - (Mock/직접 인자) | - |
| SellerBusinessInfoJpaEntityMapper | SellerBusinessInfoJpaEntityMapperTest | ✅ SellerBusinessInfoJpaEntityFixtures | - |
| SellerBusinessInfoJpaEntity | **없음** | Fixtures에 생성 로직 있음 | MED |
| SellerBusinessInfoJpaRepository | 없음 (interface) | - | LOW |
| SellerBusinessInfoQueryDslRepository | **없음** | - | HIGH |

### 2.4 SellerContract

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerContractQueryAdapter | **없음** | - | MED |
| SellerContractCommandAdapter | **없음** | - | MED |
| SellerContractConditionBuilder | **없음** | - | MED |
| SellerContractJpaEntityMapper | **없음** | - | MED |
| SellerContractJpaEntity | **없음** | Fixtures 없음 | MED |
| SellerContractJpaRepository | 없음 (interface) | - | LOW |
| SellerContractQueryDslRepository | **없음** | - | HIGH |

### 2.5 SellerCs

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerCsQueryAdapter | **없음** | - | MED |
| SellerCsCommandAdapter | **없음** | - | MED |
| SellerCsConditionBuilder | **없음** | - | MED |
| SellerCsJpaEntityMapper | **없음** | - | MED |
| SellerCsJpaEntity | **없음** | Fixtures 있음 (SellerCsJpaEntityFixtures) | MED |
| SellerCsJpaRepository | 없음 (interface) | - | LOW |
| SellerCsQueryDslRepository | **없음** | - | HIGH |

### 2.6 SellerSettlement

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| SellerSettlementQueryAdapter | **없음** | - | MED |
| SellerSettlementCommandAdapter | **없음** | - | MED |
| SellerSettlementConditionBuilder | **없음** | - | MED |
| SellerSettlementJpaEntityMapper | **없음** | - | MED |
| SellerSettlementJpaEntity | **없음** | Fixtures 없음 | MED |
| SellerSettlementJpaRepository | 없음 (interface) | - | LOW |
| SellerSettlementQueryDslRepository | **없음** | - | HIGH |

---

## 3. 항목별 상세

### 3.1 MISSING_TEST

#### Adapter (8개 누락)

**SellerAuthOutboxQueryAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerAuthOutboxQueryAdapter.java`
- **역할**: SellerAuthOutbox 조회 (findById, findBySellerId 등)
- **갭**: QueryAdapter 테스트 없음
- **우선순위**: MED

**SellerContractQueryAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerContractQueryAdapter.java`
- **역할**: SellerContract 조회 (findById, findBySellerId, existsBySellerId)
- **갭**: QueryAdapter 테스트 없음
- **우선순위**: MED

**SellerContractCommandAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerContractCommandAdapter.java`
- **역할**: SellerContract 저장 (persist, persistAll)
- **갭**: CommandAdapter 테스트 없음
- **우선순위**: MED

**SellerCsQueryAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerCsQueryAdapter.java`
- **역할**: SellerCs 조회
- **갭**: QueryAdapter 테스트 없음
- **우선순위**: MED

**SellerCsCommandAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerCsCommandAdapter.java`
- **역할**: SellerCs 저장
- **갭**: CommandAdapter 테스트 없음
- **우선순위**: MED

**SellerSettlementQueryAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerSettlementQueryAdapter.java`
- **역할**: SellerSettlement 조회
- **갭**: QueryAdapter 테스트 없음
- **우선순위**: MED

**SellerSettlementCommandAdapter (MED)**
- **위치**: `adapter/out/persistence/seller/adapter/SellerSettlementCommandAdapter.java`
- **역할**: SellerSettlement 저장
- **갭**: CommandAdapter 테스트 없음
- **우선순위**: MED

#### ConditionBuilder (4개 누락)

**SellerAuthOutboxConditionBuilder (MED)**
- **위치**: `adapter/out/persistence/seller/condition/SellerAuthOutboxConditionBuilder.java`
- **역할**: QueryDSL 조건 빌더
- **갭**: ConditionBuilder 테스트 없음
- **우선순위**: MED

**SellerContractConditionBuilder (MED)**
- **위치**: `adapter/out/persistence/seller/condition/SellerContractConditionBuilder.java`
- **역할**: QueryDSL 조건 빌더 (idEq, sellerIdEq, statusEq)
- **갭**: ConditionBuilder 테스트 없음
- **우선순위**: MED

**SellerCsConditionBuilder (MED)**
- **위치**: `adapter/out/persistence/seller/condition/SellerCsConditionBuilder.java`
- **역할**: QueryDSL 조건 빌더
- **갭**: ConditionBuilder 테스트 없음
- **우선순위**: MED

**SellerSettlementConditionBuilder (MED)**
- **위치**: `adapter/out/persistence/seller/condition/SellerSettlementConditionBuilder.java`
- **역할**: QueryDSL 조건 빌더
- **갭**: ConditionBuilder 테스트 없음
- **우선순위**: MED

#### Entity (6개 누락)

**SellerJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

**SellerAuthOutboxJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerAuthOutboxJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

**SellerBusinessInfoJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerBusinessInfoJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

**SellerContractJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerContractJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

**SellerCsJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerCsJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

**SellerSettlementJpaEntity (MED)**
- **위치**: `adapter/out/persistence/seller/entity/SellerSettlementJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter
- **갭**: Entity 단위 테스트 파일 없음
- **우선순위**: MED

#### Mapper (3개 누락)

**SellerContractJpaEntityMapper (MED)**
- **위치**: `adapter/out/persistence/seller/mapper/SellerContractJpaEntityMapper.java`
- **역할**: Entity ↔ Domain 변환 (toEntity, toDomain)
- **갭**: Mapper 테스트 없음
- **우선순위**: MED

**SellerCsJpaEntityMapper (MED)**
- **위치**: `adapter/out/persistence/seller/mapper/SellerCsJpaEntityMapper.java`
- **역할**: Entity ↔ Domain 변환 (toEntity, toDomain)
- **갭**: Mapper 테스트 없음
- **우선순위**: MED

**SellerSettlementJpaEntityMapper (MED)**
- **위치**: `adapter/out/persistence/seller/mapper/SellerSettlementJpaEntityMapper.java`
- **역할**: Entity ↔ Domain 변환 (toEntity, toDomain)
- **갭**: Mapper 테스트 없음
- **우선순위**: MED

#### QueryDslRepository (7개 누락, HIGH)

**SellerQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerQueryDslRepository.java`
- **역할**: QueryDSL 조회 (findById, findByCriteria, countByCriteria 등)
- **갭**: persistence-mysql 모듈 전역에 QueryDslRepository 단위/통합 테스트 없음
- **우선순위**: HIGH (조회·조건·정렬 로직이 핵심)

**SellerAuthOutboxQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerAuthOutboxQueryDslRepository.java`
- **역할**: QueryDSL 조회
- **갭**: 테스트 없음
- **우선순위**: HIGH

**SellerBusinessInfoQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerBusinessInfoQueryDslRepository.java`
- **역할**: QueryDSL 조회
- **갭**: 테스트 없음
- **우선순위**: HIGH

**SellerContractQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerContractQueryDslRepository.java`
- **역할**: QueryDSL 조회 (findById, findBySellerId, existsBySellerId)
- **갭**: 테스트 없음
- **우선순위**: HIGH

**SellerCsQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerCsQueryDslRepository.java`
- **역할**: QueryDSL 조회
- **갭**: 테스트 없음
- **우선순위**: HIGH

**SellerSettlementQueryDslRepository (HIGH)**
- **위치**: `adapter/out/persistence/seller/repository/SellerSettlementQueryDslRepository.java`
- **역할**: QueryDSL 조회
- **갭**: 테스트 없음
- **우선순위**: HIGH

**권장**:
1. **통합 테스트**로 DB 부담하여 각 QueryDslRepository + ConditionBuilder 조합 검증 (권장).
2. 또는 **단위 테스트**: `JPAQueryFactory`·`ConditionBuilder` Mock하여 호출 경로·반환값만 검증.

---

### 3.2 MISSING_FIXTURES

**SellerContractJpaEntityFixtures (MED)**
- **갭**: SellerContractJpaEntity용 Fixtures 없음
- **권장**: `SellerContractJpaEntityFixtures` 생성 (activeEntity, newEntity, inactiveEntity 등)

**SellerSettlementJpaEntityFixtures (MED)**
- **갭**: SellerSettlementJpaEntity용 Fixtures 없음
- **권장**: `SellerSettlementJpaEntityFixtures` 생성

---

### 3.3 MISSING_METHOD

- **SellerQueryAdapter**: 모든 메서드 테스트됨
- **SellerCommandAdapter**: 모든 메서드 테스트됨
- **SellerBusinessInfoQueryAdapter**: 모든 메서드 테스트됨
- **SellerBusinessInfoCommandAdapter**: 모든 메서드 테스트됨
- **SellerAuthOutboxCommandAdapter**: 모든 메서드 테스트됨
- **SellerConditionBuilder**: 모든 메서드 테스트됨
- **SellerBusinessInfoConditionBuilder**: 모든 메서드 테스트됨
- **SellerJpaEntityMapper**: 모든 메서드 테스트됨 (단, null id 엣지 케이스 누락)
- **SellerBusinessInfoJpaEntityMapper**: 모든 메서드 테스트됨 (단, null id 엣지 케이스 누락)
- **SellerAuthOutboxJpaEntityMapper**: 모든 메서드 테스트됨 (단, null id 엣지 케이스 누락)

---

### 3.4 MISSING_EDGE_CASE

#### 모든 Mapper toDomain (MED)

**SellerJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerId.of(entity.getId())` 호출 시 `IllegalArgumentException` 발생.
- **현재**: toDomain 테스트는 activeEntity, inactiveEntity 등 non-null id Fixture만 사용.
- **권장**: `entity.getId() != null ? SellerId.of(entity.getId()) : SellerId.forNew()` 로 null id 허용 후, `toDomain_WithNewEntity_ConvertsCorrectly()` 테스트 추가.

**SellerBusinessInfoJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerBusinessInfoId.of(entity.getId())` 호출 시 예외 발생.
- **권장**: null id 처리 추가 + 테스트 추가.

**SellerAuthOutboxJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerAuthOutboxId.of(entity.getId())` 호출 시 예외 발생.
- **권장**: null id 처리 추가 + 테스트 추가.

**SellerContractJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerContractId.of(entity.getId())` 호출 시 예외 발생.
- **권장**: null id 처리 추가 + 테스트 추가.

**SellerCsJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerCsId.of(entity.getId())` 호출 시 예외 발생.
- **권장**: null id 처리 추가 + 테스트 추가.

**SellerSettlementJpaEntityMapper.toDomain**
- **갭**: `entity.getId() == null` 인 경우 처리 없음. `SellerSettlementId.of(entity.getId())` 호출 시 예외 발생.
- **권장**: null id 처리 추가 + 테스트 추가.

---

### 3.5 MISSING_STATE_TRANSITION

- 해당 없음 (상태 기계가 있는 Aggregate가 persistence seller 패키지에 없음).

---

### 3.6 PATTERN_VIOLATION

- **갭 없음.** 모든 QueryDslRepository의 `findById()`는 `notDeleted()` 조건을 사용하여 일관성 유지.

---

## 4. 우선순위 요약 (3축)

| 클래스 | 커버리지 갭 | 역할 | 복잡도 | 종합 |
|--------|-------------|------|--------|------|
| 모든 QueryDslRepository (7개) | HIGH (테스트 없음) | Repository (조회) | 메서드 3~7개 | **HIGH** |
| Adapter (8개) | MED (테스트 없음) | Adapter | 메서드 2~3개 | **MED** |
| ConditionBuilder (4개) | MED (테스트 없음) | ConditionBuilder | 메서드 3~4개 | **MED** |
| Mapper (3개) | MED (테스트 없음) | Mapper | 메서드 2개 | **MED** |
| Entity (6개) | MED (테스트 없음) | Entity | 낮음 | **MED** |
| Mapper null id (6개) | LOW (엣지 케이스) | Mapper | 낮음 | **MED** |

---

## 5. 권장 조치

### 우선순위 HIGH

1. **모든 QueryDslRepository (7개)**  
   - persistence-mysql 통합 테스트에 각 QueryDslRepository 시나리오 추가 (또는 전용 통합 테스트 클래스 추가).  
   - findById, findBySellerId, findByCriteria, countByCriteria 등 및 정렬·페이징 동작 검증.

### 우선순위 MED

2. **Adapter 테스트 (8개)**  
   - SellerAuthOutboxQueryAdapter, SellerContractQueryAdapter, SellerContractCommandAdapter, SellerCsQueryAdapter, SellerCsCommandAdapter, SellerSettlementQueryAdapter, SellerSettlementCommandAdapter 테스트 추가.

3. **ConditionBuilder 테스트 (4개)**  
   - SellerAuthOutboxConditionBuilder, SellerContractConditionBuilder, SellerCsConditionBuilder, SellerSettlementConditionBuilder 테스트 추가.

4. **Mapper 테스트 (3개)**  
   - SellerContractJpaEntityMapper, SellerCsJpaEntityMapper, SellerSettlementJpaEntityMapper 테스트 추가.

5. **Mapper null id 처리 (6개)**  
   - 모든 Mapper의 `toDomain()`에 null id 처리 추가 (`*.forNew()` 사용) + `toDomain_WithNewEntity_*` 테스트 추가.

6. **Entity 테스트 (6개, 선택)**  
   - 각 Entity의 `create()` 및 getter 일치, Fixtures와의 일관성 검증용 단위 테스트 추가.

7. **Fixtures 생성 (2개)**  
   - SellerContractJpaEntityFixtures, SellerSettlementJpaEntityFixtures 생성.

---

## 6. 출력 경로

```
.claude/docs/test-audit/adapter-out-persistence-mysql-seller-audit.md
```
