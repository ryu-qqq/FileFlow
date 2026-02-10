# Test Audit: adapter-out persistence-mysql refundpolicy

**레이어**: adapter-out (persistence-mysql)  
**패키지**: refundpolicy  
**감사 일자**: 2025-02-06

---

## 1. 요약

| 항목 | 수 | 비고 |
|------|-----|------|
| 소스 클래스 | 7 | Adapter 2 (Query, Command), Condition 1, Entity 1, Mapper 1, Repository 2 |
| 테스트 클래스 | 4 | QueryAdapter, CommandAdapter, ConditionBuilder, Mapper |
| testFixtures | 1 | RefundPolicyJpaEntityFixtures (존재) |
| MISSING_TEST | 2 | RefundPolicyJpaEntity, RefundPolicyQueryDslRepository |
| MISSING_EDGE_CASE | 1 | Mapper toDomain(null id) |
| PATTERN_VIOLATION | 0 | - |

---

## 2. 소스 vs 테스트 매트릭스

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| RefundPolicyQueryAdapter | RefundPolicyQueryAdapterTest | ✅ RefundPolicyJpaEntityFixtures | - |
| RefundPolicyCommandAdapter | RefundPolicyCommandAdapterTest | ✅ RefundPolicyJpaEntityFixtures | - |
| RefundPolicyConditionBuilder | RefundPolicyConditionBuilderTest | - (Mock/직접 인자) | - |
| RefundPolicyJpaEntityMapper | RefundPolicyJpaEntityMapperTest | ✅ RefundPolicyJpaEntityFixtures + Domain Fixtures | - |
| RefundPolicyJpaEntity | **없음** | Fixtures에 생성 로직 있음 | MED |
| RefundPolicyJpaRepository | 없음 (interface) | - | LOW |
| RefundPolicyQueryDslRepository | **없음** | - | HIGH |

---

## 3. 항목별 상세

### 3.1 MISSING_TEST

#### RefundPolicyJpaEntity (MED)

- **위치**: `adapter/out/persistence/refundpolicy/entity/RefundPolicyJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter (sellerId, policyName, defaultPolicy, active, returnPeriodDays, exchangePeriodDays, nonReturnableConditions, partialRefundEnabled, inspectionRequired, inspectionPeriodDays, additionalInfo 등).
- **갭**: Entity 단위 테스트 파일 없음. brand/category/commoncode/commoncodetype와 동일하게 Mapper/Adapter 위주로만 검증 중.
- **우선순위**: MED (역할·복잡도는 create/getter 위주. Fixtures·Mapper 테스트로 간접 커버 가능)
- **권장**: 선택 사항. `RefundPolicyJpaEntity.create()` 및 getter 일치 검증용 단위 테스트 추가 시 Fixtures와의 일관성 검증에 유리.

#### RefundPolicyQueryDslRepository (HIGH)

- **위치**: `adapter/out/persistence/refundpolicy/repository/RefundPolicyQueryDslRepository.java`
- **역할**: QueryDSL 조회 (findById, findByIds, findDefaultBySellerId, findBySellerIdAndId, findByCriteria, countByCriteria, countActiveBySellerId, createOrderSpecifier).
- **갭**: persistence-mysql 모듈 전역에 QueryDslRepository 단위/통합 테스트 없음.
- **우선순위**: HIGH (조회·조건·정렬 로직이 핵심, 메서드 7개)
- **권장**:
  1. **통합 테스트**로 DB 부담하여 `RefundPolicyQueryDslRepository` + `RefundPolicyConditionBuilder` 조합 검증 (권장).
  2. 또는 **단위 테스트**: `JPAQueryFactory`·`RefundPolicyConditionBuilder` Mock하여 호출 경로·반환값만 검증.

---

### 3.2 MISSING_FIXTURES

- **갭 없음.** `RefundPolicyJpaEntityFixtures` 존재하며 Adapter/Mapper 테스트에서 사용 중. `newEntity()` 등 id=null Fixture 포함.

---

### 3.3 MISSING_METHOD

- **RefundPolicyQueryAdapter**: findById, findByIds, findDefaultBySellerId, findBySellerIdAndId, findByCriteria, countByCriteria, countActiveBySellerId 모두 테스트됨.
- **RefundPolicyCommandAdapter**: persist, persistAll 모두 테스트됨.
- **RefundPolicyConditionBuilder**: idEq, idIn, sellerIdEq, defaultPolicyEq, activeEq, notDeleted 모두 테스트됨.
- **RefundPolicyJpaEntityMapper**: toEntity, toDomain 모두 테스트됨.
- **RefundPolicyQueryDslRepository**: public 메서드 7개에 대한 단위/통합 테스트 없음 (위 MISSING_TEST 참고).

---

### 3.4 MISSING_EDGE_CASE

#### RefundPolicyJpaEntityMapper.toDomain (MED)

- **갭**: `entity.getId() == null` 인 경우 처리 없음. `RefundPolicyId.of(entity.getId())` 호출 시 `RefundPolicyId`가 null을 허용하지 않아 `IllegalArgumentException` 발생.
- **현재**: toDomain 테스트는 activeEntity, inactiveEntity, deletedEntity 등 non-null id Fixture만 사용. Fixtures에는 `newEntity()` 등 id=null Entity가 있으나 toDomain 테스트에서 미사용.
- **권장**:
  - **옵션 A**: Brand/Category/CommonCode/CommonCodeType/SellerApplication 매퍼처럼 `entity.getId() != null ? RefundPolicyId.of(entity.getId()) : RefundPolicyId.forNew()` 로 null id 허용 후, `toDomain_WithNewEntity_ConvertsCorrectly()` 테스트 추가 (RefundPolicyJpaEntityFixtures.newEntity() 사용).
  - **옵션 B**: "저장된 Entity만 toDomain" 전제로 두고 null id는 불가로 유지. 이 경우 `toDomain_WithNullId_ThrowsIllegalArgumentException()` 등 명시적 테스트 1개 추가 권장.

---

### 3.5 MISSING_STATE_TRANSITION

- 해당 없음 (상태 기계가 있는 Aggregate가 persistence refundpolicy 패키지에 없음).

---

### 3.6 PATTERN_VIOLATION

- **갭 없음.** `RefundPolicyQueryDslRepository.findById()`는 `conditionBuilder.notDeleted()` 조건을 사용하여 다른 패키지(brand, category, commoncode)와 일관성 유지.

---

## 4. 우선순위 요약 (3축)

| 클래스 | 커버리지 갭 | 역할 | 복잡도 | 종합 |
|--------|-------------|------|--------|------|
| RefundPolicyQueryDslRepository | HIGH (테스트 없음) | Repository (조회) | 메서드 7개 | **HIGH** |
| RefundPolicyJpaEntity | MED (테스트 없음) | Entity | 낮음 | **MED** |
| RefundPolicyJpaEntityMapper | LOW (null id 엣지) | Mapper | 낮음 | **MED** |
| RefundPolicyQueryAdapter | 없음 | Adapter | 메서드 7개 | - |
| RefundPolicyCommandAdapter | 없음 | Adapter | 메서드 2개 | - |
| RefundPolicyConditionBuilder | 없음 | ConditionBuilder | 메서드 6개 | - |
| RefundPolicyJpaRepository | LOW | JPA Interface | - | LOW |

---

## 5. 권장 조치

1. **RefundPolicyQueryDslRepository (HIGH)**  
   - persistence-mysql 통합 테스트에 `RefundPolicyQueryDslRepository` 시나리오 추가 (또는 전용 통합 테스트 클래스 추가).  
   - findById, findByIds, findDefaultBySellerId, findBySellerIdAndId, findByCriteria, countByCriteria, countActiveBySellerId 및 정렬·페이징 동작 검증.

2. **RefundPolicyJpaEntityMapper (MED)**  
   - null id 대응 정책 결정 후,  
     - null 허용 시: 매퍼에 `RefundPolicyId.forNew()` 분기 추가 + `toDomain_WithNewEntity_*` 테스트 추가 (RefundPolicyJpaEntityFixtures.newEntity() 사용).  
     - null 비허용 시: `toDomain_WithNullId_ThrowsIllegalArgumentException()` 테스트 추가.

3. **RefundPolicyJpaEntity (MED, 선택)**  
   - `RefundPolicyJpaEntityTest` 추가 시 `create()` 인자와 getter 일치, Fixtures와의 일관성만 검증해도 충분.

4. **RefundPolicyJpaRepository**  
   - JPA interface라 단위 테스트 생략 가능. 통합/슬라이스 테스트에서 save/saveAll 사용 시 간접 검증.

---

## 6. 출력 경로

```
.claude/docs/test-audit/adapter-out-persistence-mysql-refundpolicy-audit.md
```
