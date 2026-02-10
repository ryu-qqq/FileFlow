# Category API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 0개 |
| **합계** | **1개** |

**Base Path**: `/api/v1/market/admin/categories`

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/categories | CategoryQueryController | searchCategoriesByOffset | SearchCategoryByOffsetUseCase |

---

### Q1. searchCategoriesByOffset

**카테고리 목록 조회 (Offset 기반 페이징)**

- **Path**: `GET /api/v1/market/admin/categories`
- **Controller**: `CategoryQueryController`
- **Request**: `SearchCategoriesApiRequest` (@ParameterObject)
- **Response**: `ApiResponse<PageApiResponse<CategoryApiResponse>>`
- **UseCase**: `SearchCategoryByOffsetUseCase`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| parentId | Long | ❌ | 부모 카테고리 ID |
| depth | Integer | ❌ | 계층 깊이 |
| leaf | Boolean | ❌ | 리프 노드 여부 |
| statuses | List\<String\> | ❌ | 상태 필터 (ACTIVE, INACTIVE) |
| departments | List\<String\> | ❌ | 부문 필터 (FASHION, BEAUTY, LIVING 등) |
| categoryGroups | List\<String\> | ❌ | 카테고리 그룹 필터 (CLOTHING, SHOES, DIGITAL 등 - 고시정보 연결용) |
| searchField | String | ❌ | 검색 필드 (code, nameKo, nameEn) |
| searchWord | String | ❌ | 검색어 |
| sortKey | String | ❌ | 정렬 키 (sortOrder, createdAt, nameKo, code) |
| sortDirection | String | ❌ | 정렬 방향 (ASC, DESC) |
| page | Integer | ❌ | 페이지 번호 (0부터) |
| size | Integer | ❌ | 페이지 크기 |

#### Response Fields

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "CATEGORY_CODE",
        "nameKo": "한글명",
        "nameEn": "English Name",
        "parentId": null,
        "depth": 1,
        "path": "/1",
        "sortOrder": 1,
        "leaf": false,
        "status": "ACTIVE",
        "department": "FASHION",
        "categoryGroup": "CLOTHING",
        "createdAt": "2026-02-06T12:00:00Z",
        "updatedAt": "2026-02-06T12:00:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

#### 주요 특징

1. **계층 구조 지원**: `parentId`, `depth`, `path`로 트리 구조 표현
2. **고시정보 연결**: `categoryGroup` 필드로 고시정보 템플릿 매핑
3. **다양한 필터**: 상태, 부문, 카테고리 그룹, 리프 노드 여부 등
4. **유연한 검색**: 코드, 한글명, 영문명 검색 지원
5. **정렬 옵션**: sortOrder, createdAt, nameKo, code 기준 정렬

#### 사용 예시

**1. 최상위 카테고리 조회**
```http
GET /api/v1/market/admin/categories?depth=1&page=0&size=20
```

**2. 특정 부모의 하위 카테고리 조회**
```http
GET /api/v1/market/admin/categories?parentId=1&page=0&size=20
```

**3. 리프 노드만 조회 (상품 등록 가능한 카테고리)**
```http
GET /api/v1/market/admin/categories?leaf=true&page=0&size=50
```

**4. 카테고리 그룹별 조회 (고시정보 관리)**
```http
GET /api/v1/market/admin/categories?categoryGroups=CLOTHING,SHOES&page=0&size=20
```

**5. 카테고리 검색**
```http
GET /api/v1/market/admin/categories?searchField=nameKo&searchWord=의류&page=0&size=20
```

**6. 부문별 활성 카테고리 조회**
```http
GET /api/v1/market/admin/categories?departments=FASHION&statuses=ACTIVE&sortKey=sortOrder&sortDirection=ASC
```

---

## 아키텍처 플로우

```
CategoryQueryController
  ↓ (CategoryQueryApiMapper)
SearchCategoryByOffsetUseCase
  ↓
SearchCategoryByOffsetService
  ↓
CategoryQueryPort
  ↓
CategoryQueryAdapter
  ↓
CategoryQueryDslRepository
```

---

## 데이터 모델 관계

```
Category (JpaEntity)
├─ id: Long (PK)
├─ code: String (UK)
├─ nameKo: String
├─ nameEn: String
├─ parentId: Long (FK to Category)
├─ depth: Integer (1~4)
├─ path: String (/1/2/3)
├─ sortOrder: Integer
├─ leaf: Boolean
├─ status: CategoryStatus (ACTIVE/INACTIVE)
├─ department: Department (FASHION/BEAUTY/LIVING/DIGITAL/ETC)
├─ categoryGroup: CategoryGroup (CLOTHING/SHOES/BAGS/...)
└─ BaseAuditEntity (createdAt, updatedAt)
```

---

## 향후 확장 가능성

### Command Endpoints (예상)

1. **POST /api/v1/market/admin/categories**
   - 카테고리 생성

2. **PUT /api/v1/market/admin/categories/{categoryId}**
   - 카테고리 수정

3. **PATCH /api/v1/market/admin/categories/{categoryId}/status**
   - 카테고리 상태 변경 (활성화/비활성화)

4. **PATCH /api/v1/market/admin/categories/{categoryId}/move**
   - 카테고리 이동 (부모 변경)

5. **DELETE /api/v1/market/admin/categories/{categoryId}**
   - 카테고리 삭제 (Soft Delete)

### 추가 Query Endpoints (예상)

1. **GET /api/v1/market/admin/categories/{categoryId}**
   - 카테고리 상세 조회

2. **GET /api/v1/market/admin/categories/{categoryId}/path**
   - 카테고리 경로 조회 (breadcrumb용)

3. **GET /api/v1/market/admin/categories/tree**
   - 카테고리 트리 전체 조회

---

## 개선 제안

### 1. 성능 최적화
- **인덱스**: `(parentId, sortOrder)`, `(status, department)`, `(categoryGroup)`
- **캐싱**: 카테고리 트리는 변경 빈도가 낮아 Redis 캐싱 적합
- **쿼리 최적화**: N+1 문제 방지 (현재 QueryDSL로 해결됨)

### 2. API 기능 확장
- **Public API**: 고객용 카테고리 조회 API 분리 (`/api/v1/market/public/categories`)
- **Bulk Operations**: 대량 카테고리 상태 변경, 순서 조정
- **Tree API**: 전체 트리 구조를 한 번에 조회하는 엔드포인트

### 3. 보안 및 권한
- 관리자 권한 검증 (Spring Security)
- Audit Log (카테고리 변경 이력 추적)

### 4. 문서화
- OpenAPI/Swagger 자동 생성 (이미 `@Parameter`, `@Schema` 적용됨)
- Spring REST Docs 테스트 작성
- 카테고리 계층 구조 다이어그램

---

## 테스트 전략

### Unit Tests
- `CategoryQueryController`: @WebMvcTest
- `CategoryQueryApiMapper`: Mapping 로직 검증
- `SearchCategoryByOffsetService`: UseCase 비즈니스 로직

### Integration Tests
- `CategoryQueryDslRepository`: 실제 DB 쿼리 검증
- 페이징, 정렬, 필터링 조합 테스트

### API Tests (RestDocs)
- 요청/응답 문서화
- 다양한 필터 조합 시나리오

---

## 참고 자료

- **Controller**: `adapter-in/rest-api/.../category/controller/CategoryQueryController.java`
- **DTO**: `adapter-in/rest-api/.../category/dto/`
- **UseCase**: `application/.../category/port/in/query/SearchCategoryByOffsetUseCase.java`
- **Service**: `application/.../category/service/query/SearchCategoryByOffsetService.java`
- **Repository**: `adapter-out/persistence-mysql/.../category/repository/CategoryQueryDslRepository.java`
