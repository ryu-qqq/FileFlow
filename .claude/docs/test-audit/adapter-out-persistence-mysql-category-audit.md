# Test Audit: adapter-out persistence-mysql category

**레이어**: adapter-out (persistence-mysql)  
**패키지**: category  
**감사 일자**: 2025-02-06

---

## 1. 요약

| 항목 | 수 | 비고 |
|------|-----|------|
| 소스 클래스 | 6 | Adapter 1, Condition 1, Entity 1, Mapper 1, Repository 2 |
| 테스트 클래스 | 3 | Adapter, ConditionBuilder, Mapper |
| testFixtures | 1 | CategoryJpaEntityFixtures (존재) |
| MISSING_TEST | 2 | CategoryJpaEntity, CategoryQueryDslRepository |
| MISSING_EDGE_CASE | 1 | Mapper toDomain(null id) |
| PATTERN_VIOLATION | 0 | - |

---

## 2. 소스 vs 테스트 매트릭스

| 소스 클래스 | 테스트 파일 | Fixtures 사용 | 우선순위 |
|-------------|-------------|----------------|----------|
| CategoryQueryAdapter | CategoryQueryAdapterTest | ✅ CategoryJpaEntityFixtures | - |
| CategoryConditionBuilder | CategoryConditionBuilderTest | - (Mock criteria) | - |
| CategoryJpaEntityMapper | CategoryJpaEntityMapperTest | ✅ CategoryJpaEntityFixtures | - |
| CategoryJpaEntity | **없음** | Fixtures에 생성 로직 있음 | HIGH |
| CategoryJpaRepository | 없음 (interface) | - | LOW |
| CategoryQueryDslRepository | **없음** | - | HIGH |

---

## 3. 항목별 상세

### 3.1 MISSING_TEST

#### CategoryJpaEntity (HIGH)

- **위치**: `adapter/out/persistence/category/entity/CategoryJpaEntity.java`
- **역할**: JPA Entity, `create()` 정적 팩토리 + getter (필드 다수: code, nameKo, nameEn, parentId, depth, path, sortOrder, leaf, status, department, categoryGroup)
- **갭**: Entity 단위 테스트 파일 없음. brand 패키지와 동일하게 Mapper/Adapter 위주로만 검증 중.
- **우선순위**: MED (역할·복잡도는 create/getter 위주. Fixtures·Mapper 테스트로 간접 커버 가능)
- **권장**: 선택 사항. `CategoryJpaEntity.create()` 및 getter 일치 검증용 단위 테스트 추가 시 Fixtures와의 일관성 검증에 유리.

#### CategoryQueryDslRepository (HIGH)

- **위치**: `adapter/out/persistence/category/repository/CategoryQueryDslRepository.java`
- **역할**: QueryDSL 조회 (findById, findByCriteria, countByCriteria, existsByCode, resolveOrderSpecifier). 조건 조합이 많음 (parentId, depth, leaf, status, department, categoryGroup, searchCondition).
- **갭**: persistence-mysql 모듈 전역에 QueryDslRepository 단위/통합 테스트 없음.
- **우선순위**: HIGH (조회·조건·정렬 로직이 핵심)
- **권장**:
  1. **통합 테스트**로 DB 부담하여 `CategoryQueryDslRepository` + `CategoryConditionBuilder` 조합 검증 (권장).
  2. 또는 **단위 테스트**: `JPAQueryFactory`·`CategoryConditionBuilder` Mock하여 호출 경로·반환값만 검증.

---

### 3.2 MISSING_FIXTURES

- **갭 없음.** `CategoryJpaEntityFixtures` 존재하며 Adapter/Mapper 테스트에서 사용 중.

---

### 3.3 MISSING_METHOD

- **CategoryQueryAdapter**: findById, findByCriteria, countByCriteria, existsByCode 모두 테스트됨.
- **CategoryConditionBuilder**: idEq, parentIdEq, depthEq, leafEq, statusIn, departmentIn, categoryGroupIn, searchCondition, notDeleted 모두 테스트됨.
- **CategoryJpaEntityMapper**: toEntity, toDomain 모두 테스트됨.
- **CategoryQueryDslRepository**: public 메서드 4개에 대한 단위/통합 테스트 없음 (위 MISSING_TEST 참고).

---

### 3.4 MISSING_EDGE_CASE

#### CategoryJpaEntityMapper.toDomain (MED)

- **갭**: `entity.getId() == null` 인 경우 처리 없음. `CategoryId.of(entity.getId())` 호출 시 `CategoryId`가 null을 허용하지 않아 `IllegalArgumentException` 발생.
- **현재**: toDomain 테스트는 activeRootEntity, activeChildEntity 등 non-null id Fixture만 사용.
- **권장**:
  - **옵션 A**: Brand/SellerApplication 매퍼처럼 `entity.getId() != null ? CategoryId.of(entity.getId()) : CategoryId.forNew()` 로 null id 허용 후, Fixtures에 id=null Entity 추가하고 `toDomain_WithNewEntity_ConvertsCorrectly()` 테스트 추가.
  - **옵션 B**: "저장된 Entity만 toDomain" 전제로 두고 null id는 불가로 유지. 이 경우 `toDomain_WithNullId_ThrowsIllegalArgumentException()` 등 명시적 테스트 1개 추가 권장.

---

### 3.5 MISSING_STATE_TRANSITION

- 해당 없음 (상태 기계가 있는 Aggregate가 persistence category 패키지에 없음).

---

### 3.6 PATTERN_VIOLATION

- **@Tag("unit")**, **@DisplayName**, **Nested** 구조, **Fixtures 사용** 등 프로젝트 컨벤션과 일치. 위반 없음.

---

## 4. 우선순위 요약 (3축)

| 클래스 | 커버리지 갭 | 역할 | 복잡도 | 종합 |
|--------|-------------|------|--------|------|
| CategoryQueryDslRepository | HIGH (테스트 없음) | Repository (조회) | 메서드 4+ | **HIGH** |
| CategoryJpaEntity | MED (테스트 없음) | Entity | 낮음 | **MED** |
| CategoryJpaEntityMapper | LOW (null id 엣지) | Mapper | 낮음 | **MED** |
| CategoryQueryAdapter | 없음 | Adapter | 중간 | - |
| CategoryConditionBuilder | 없음 | ConditionBuilder | 메서드 9개 | - |
| CategoryJpaRepository | LOW | JPA Interface | - | LOW |

---

## 5. 권장 조치

1. **CategoryQueryDslRepository (HIGH)**  
   - persistence-mysql 통합 테스트에 `CategoryQueryDslRepository` 시나리오 추가 (또는 전용 통합 테스트 클래스 추가).  
   - 조건/정렬/페이징 조합 및 countByCriteria, existsByCode 동작 검증.

2. **CategoryJpaEntityMapper (MED)**  
   - null id 대응 정책 결정 후,  
     - null 허용 시: 매퍼에 `CategoryId.forNew()` 분기 추가 + Fixtures에 id=null Entity + `toDomain_WithNewEntity_*` 테스트 추가.  
     - null 비허용 시: `toDomain_WithNullId_ThrowsIllegalArgumentException()` 테스트 추가.

3. **CategoryJpaEntity (MED, 선택)**  
   - `CategoryJpaEntityTest` 추가 시 `create()` 인자와 getter 일치, Fixtures와의 일관성만 검증해도 충분.

4. **CategoryJpaRepository**  
   - JPA interface라 단위 테스트 생략 가능. 통합/슬라이스 테스트에서 save 등 사용 시 간접 검증.

---

## 6. 출력 경로

```
.claude/docs/test-audit/adapter-out-persistence-mysql-category-audit.md
```
