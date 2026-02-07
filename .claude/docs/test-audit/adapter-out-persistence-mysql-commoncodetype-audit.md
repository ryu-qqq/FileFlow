# Test Audit: adapter-out persistence-mysql commoncodetype

**레이어**: adapter-out (persistence-mysql)  
**패키지**: commoncodetype  
**감사 일자**: 2025-02-06

---

## 1. 요약

| 항목 | 수 | 비고 |
|------|-----|------|
| 소스 클래스 | 7 | Adapter 2 (Query, Command), Condition 1, Entity 1, Mapper 1, Repository 2 |
| 테스트 클래스 | 4 | QueryAdapter, CommandAdapter, ConditionBuilder, Mapper |
| testFixtures | 1 | CommonCodeTypeJpaEntityFixtures (존재) |
| MISSING_TEST | 2 | CommonCodeTypeJpaEntity, CommonCodeTypeQueryDslRepository |
| MISSING_EDGE_CASE | 1 | Mapper toDomain(null id) |
| PATTERN_VIOLATION | 1 | QueryDslRepository.findById에 notDeleted() 조건 없음 |

---

## 2. 소스 vs 테스트 매트릭스

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| CommonCodeTypeQueryAdapter | CommonCodeTypeQueryAdapterTest | ✅ CommonCodeTypeJpaEntityFixtures | - |
| CommonCodeTypeCommandAdapter | CommonCodeTypeCommandAdapterTest | ✅ CommonCodeTypeJpaEntityFixtures | - |
| CommonCodeTypeConditionBuilder | CommonCodeTypeConditionBuilderTest | - (Mock/직접 인자) | - |
| CommonCodeTypeJpaEntityMapper | CommonCodeTypeJpaEntityMapperTest | ✅ CommonCodeTypeJpaEntityFixtures + Domain Fixtures | - |
| CommonCodeTypeJpaEntity | **없음** | Fixtures에 생성 로직 있음 | HIGH |
| CommonCodeTypeJpaRepository | 없음 (interface) | - | LOW |
| CommonCodeTypeQueryDslRepository | **없음** | - | HIGH |

---

## 3. 항목별 상세

### 3.1 MISSING_TEST

#### CommonCodeTypeJpaEntity (HIGH)

- **위치**: `adapter/out/persistence/commoncodetype/entity/CommonCodeTypeJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter (code, name, description, displayOrder, active 등).
- **갭**: Entity 단위 테스트 파일 없음. brand/category/commoncode와 동일하게 Mapper/Adapter 위주로만 검증 중.
- **우선순위**: MED (역할·복잡도는 create/getter 위주. Fixtures·Mapper 테스트로 간접 커버 가능)
- **권장**: 선택 사항. `CommonCodeTypeJpaEntity.create()` 및 getter 일치 검증용 단위 테스트 추가 시 Fixtures와의 일관성 검증에 유리.

#### CommonCodeTypeQueryDslRepository (HIGH)

- **위치**: `adapter/out/persistence/commoncodetype/repository/CommonCodeTypeQueryDslRepository.java`
- **역할**: QueryDSL 조회 (findById, findByIds, existsByCode, existsByDisplayOrderExcludingId, findByCriteria, countByCriteria, createOrderSpecifier, deletedAtFilter).
- **갭**: persistence-mysql 모듈 전역에 QueryDslRepository 단위/통합 테스트 없음.
- **우선순위**: HIGH (조회·조건·정렬 로직이 핵심)
- **권장**:
  1. **통합 테스트**로 DB 부담하여 `CommonCodeTypeQueryDslRepository` + `CommonCodeTypeConditionBuilder` 조합 검증 (권장).
  2. 또는 **단위 테스트**: `JPAQueryFactory`·`CommonCodeTypeConditionBuilder` Mock하여 호출 경로·반환값만 검증.

---

### 3.2 MISSING_FIXTURES

- **갭 없음.** `CommonCodeTypeJpaEntityFixtures` 존재하며 Adapter/Mapper 테스트에서 사용 중. `newEntity()` 등 id=null Fixture 포함.

---

### 3.3 MISSING_METHOD

- **CommonCodeTypeQueryAdapter**: findById, findByIds, existsByCode, existsByDisplayOrderExcludingId, findByCriteria, countByCriteria 모두 테스트됨.
- **CommonCodeTypeCommandAdapter**: persist, persistAll 모두 테스트됨.
- **CommonCodeTypeConditionBuilder**: idEq, idIn, idNe, codeEq, displayOrderEq, activeEq(Boolean), activeEq(Criteria), searchFieldContains, searchCondition, typeHasCommonCodeValue 모두 테스트됨.
- **CommonCodeTypeJpaEntityMapper**: toEntity, toDomain 모두 테스트됨.
- **CommonCodeTypeQueryDslRepository**: public 메서드 6개에 대한 단위/통합 테스트 없음 (위 MISSING_TEST 참고).

---

### 3.4 MISSING_EDGE_CASE

#### CommonCodeTypeJpaEntityMapper.toDomain (MED)

- **갭**: `entity.getId() == null` 인 경우 처리 없음. `CommonCodeTypeId.of(entity.getId())` 호출 시 `CommonCodeTypeId`가 null을 허용하지 않아 `IllegalArgumentException` 발생.
- **현재**: toDomain 테스트는 activeEntity, inactiveEntity, deletedEntity 등 non-null id Fixture만 사용. Fixtures에는 `newEntity()` 등 id=null Entity가 있으나 toDomain 테스트에서 미사용.
- **권장**:
  - **옵션 A**: Brand/Category/CommonCode/SellerApplication 매퍼처럼 `entity.getId() != null ? CommonCodeTypeId.of(entity.getId()) : CommonCodeTypeId.forNew()` 로 null id 허용 후, `toDomain_WithNewEntity_ConvertsCorrectly()` 테스트 추가 (CommonCodeTypeJpaEntityFixtures.newEntity() 사용).
  - **옵션 B**: "저장된 Entity만 toDomain" 전제로 두고 null id는 불가로 유지. 이 경우 `toDomain_WithNullId_ThrowsIllegalArgumentException()` 등 명시적 테스트 1개 추가 권장.

---

### 3.5 MISSING_STATE_TRANSITION

- 해당 없음 (상태 기계가 있는 Aggregate가 persistence commoncodetype 패키지에 없음).

---

### 3.6 PATTERN_VIOLATION

#### CommonCodeTypeQueryDslRepository.findById (MED)

- **위치**: `adapter/out/persistence/commoncodetype/repository/CommonCodeTypeQueryDslRepository.java:53-59`
- **갭**: `findById()` 메서드에 `notDeleted()` 조건이 없음. 다른 패키지(brand, category, commoncode)의 QueryDslRepository는 모두 `conditionBuilder.notDeleted()` 조건을 사용.
- **현재**: `findById()`는 `conditionBuilder.idEq(id)`만 사용. `findByCriteria()`와 `countByCriteria()`는 `deletedAtFilter(criteria)`를 사용하지만, `findById()`는 삭제된 Entity도 조회 가능.
- **권장**: `findById()`에 `conditionBuilder.notDeleted()` 조건 추가 또는 `deletedAtFilter()` 사용하여 일관성 유지. 또는 의도된 설계라면 명시적 주석 추가.

---

## 4. 우선순위 요약 (3축)

| 클래스 | 커버리지 갭 | 역할 | 복잡도 | 종합 |
|--------|-------------|------|--------|------|
| CommonCodeTypeQueryDslRepository | HIGH (테스트 없음) | Repository (조회) | 메서드 6개 | **HIGH** |
| CommonCodeTypeJpaEntity | MED (테스트 없음) | Entity | 낮음 | **MED** |
| CommonCodeTypeJpaEntityMapper | LOW (null id 엣지) | Mapper | 낮음 | **MED** |
| CommonCodeTypeQueryDslRepository.findById | MED (패턴 위반) | Repository | - | **MED** |
| CommonCodeTypeQueryAdapter | 없음 | Adapter | 메서드 6개 | - |
| CommonCodeTypeCommandAdapter | 없음 | Adapter | 메서드 2개 | - |
| CommonCodeTypeConditionBuilder | 없음 | ConditionBuilder | 메서드 10개 | - |
| CommonCodeTypeJpaRepository | LOW | JPA Interface | - | LOW |

---

## 5. 권장 조치

1. **CommonCodeTypeQueryDslRepository (HIGH)**  
   - persistence-mysql 통합 테스트에 `CommonCodeTypeQueryDslRepository` 시나리오 추가 (또는 전용 통합 테스트 클래스 추가).  
   - findById, findByIds, existsByCode, existsByDisplayOrderExcludingId, findByCriteria, countByCriteria 및 정렬·페이징 동작 검증.

2. **CommonCodeTypeJpaEntityMapper (MED)**  
   - null id 대응 정책 결정 후,  
     - null 허용 시: 매퍼에 `CommonCodeTypeId.forNew()` 분기 추가 + `toDomain_WithNewEntity_*` 테스트 추가 (CommonCodeTypeJpaEntityFixtures.newEntity() 사용).  
     - null 비허용 시: `toDomain_WithNullId_ThrowsIllegalArgumentException()` 테스트 추가.

3. **CommonCodeTypeQueryDslRepository.findById (MED)**  
   - 다른 패키지와 일관성을 위해 `findById()`에 `notDeleted()` 조건 추가 검토. 또는 의도된 설계라면 주석으로 명시.

4. **CommonCodeTypeJpaEntity (MED, 선택)**  
   - `CommonCodeTypeJpaEntityTest` 추가 시 `create()` 인자와 getter 일치, Fixtures와의 일관성만 검증해도 충분.

5. **CommonCodeTypeJpaRepository**  
   - JPA interface라 단위 테스트 생략 가능. 통합/슬라이스 테스트에서 save/saveAll 사용 시 간접 검증.

---

## 6. 출력 경로

```
.claude/docs/test-audit/adapter-out-persistence-mysql-commoncodetype-audit.md
```
