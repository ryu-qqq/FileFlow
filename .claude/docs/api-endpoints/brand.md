# Brand API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 0개 |
| **합계** | **1개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/admin/brands | BrandQueryController | searchBrandsByOffset | SearchBrandByOffsetUseCase |

---

### Q1. searchBrandsByOffset - 브랜드 목록 조회 (Offset 기반 페이징)

- **Path**: `GET /api/v1/market/admin/brands`
- **Controller**: `BrandQueryController`
- **Request**: `SearchBrandsApiRequest` (@ParameterObject, Query String)
- **Response**: `ApiResponse<PageApiResponse<BrandApiResponse>>`
- **UseCase**: `SearchBrandByOffsetUseCase`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| statuses | List\<String\> | ❌ | 상태 필터 (ACTIVE, INACTIVE) | `statuses=ACTIVE` |
| searchField | String | ❌ | 검색 필드 (code, nameKo, nameEn) | `searchField=nameKo` |
| searchWord | String | ❌ | 검색어 | `searchWord=나이키` |
| sortKey | String | ❌ | 정렬 키 (createdAt, nameKo, updatedAt) | `sortKey=createdAt` |
| sortDirection | String | ❌ | 정렬 방향 (ASC, DESC) | `sortDirection=DESC` |
| page | Integer | ❌ | 페이지 번호 (0부터 시작) | `page=0` |
| size | Integer | ❌ | 페이지 크기 (기본값: 20) | `size=20` |

#### Response Fields

**BrandApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 브랜드 ID |
| code | String | 브랜드 코드 |
| nameKo | String | 한글명 |
| nameEn | String | 영문명 |
| shortName | String | 약칭 |
| status | String | 상태 (ACTIVE, INACTIVE) |
| logoUrl | String | 로고 URL |
| createdAt | String | 생성일시 (ISO-8601) |
| updatedAt | String | 수정일시 (ISO-8601) |

#### Response Structure

```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "code": "NIKE",
        "nameKo": "나이키",
        "nameEn": "Nike",
        "shortName": "NK",
        "status": "ACTIVE",
        "logoUrl": "https://example.com/nike-logo.png",
        "createdAt": "2025-01-01T00:00:00Z",
        "updatedAt": "2025-01-15T12:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. BrandQueryController.searchBrandsByOffset()
   ↓
2. BrandQueryApiMapper.toSearchParams()
   - SearchBrandsApiRequest → BrandSearchParams 변환
   - 기본값 설정: page=0, size=20
   ↓
3. SearchBrandByOffsetUseCase.execute()
   - 비즈니스 로직 실행
   ↓
4. BrandPageResult 반환
   ↓
5. BrandQueryApiMapper.toPageResponse()
   - BrandPageResult → PageApiResponse<BrandApiResponse> 변환
   - Instant → ISO-8601 문자열 변환
   ↓
6. ResponseEntity<ApiResponse<PageApiResponse<BrandApiResponse>>>
```

#### 특이사항

- **검색 조건 조합**: statuses, searchField, searchWord를 조합하여 동적 검색 가능
- **기본 페이징**: page/size 미지정 시 0페이지, 20개 크기로 조회
- **정렬 지원**: createdAt, nameKo, updatedAt 기준 ASC/DESC 정렬 가능
- **날짜 포맷**: DB의 Instant 타입을 ISO-8601 문자열로 변환하여 응답 (DateTimeFormatUtils 사용)

---

## Command Endpoints

현재 Brand 도메인에는 Command 엔드포인트가 구현되어 있지 않습니다.

### 향후 구현 예정 (추정)

| 기능 | HTTP Method | Path | 예상 컨트롤러 |
|------|-------------|------|--------------|
| 브랜드 생성 | POST | /api/v1/market/admin/brands | BrandCommandController |
| 브랜드 수정 | PUT | /api/v1/market/admin/brands/{brandId} | BrandCommandController |
| 브랜드 삭제 | DELETE | /api/v1/market/admin/brands/{brandId} | BrandCommandController |
| 브랜드 상태 변경 | PATCH | /api/v1/market/admin/brands/{brandId}/status | BrandCommandController |

---

## 아키텍처 매핑

### Hexagonal Architecture Layer 흐름

```
[Adapter-In] BrandQueryController
    ↓
[Adapter-In] BrandQueryApiMapper (DTO 변환)
    ↓
[Application] SearchBrandByOffsetUseCase (Port-In)
    ↓
[Application] SearchBrandByOffsetService (구현체)
    ↓
[Application] BrandQueryPort (Port-Out)
    ↓
[Adapter-Out] BrandQueryAdapter
    ↓
[Adapter-Out] BrandQueryDslRepository
    ↓
[Database] brand 테이블
```

### CQRS 패턴 적용

- **Query Side**: 현재 구현된 `BrandQueryController`는 조회 전용
- **Command Side**: 아직 미구현 상태 (향후 `BrandCommandController` 예상)
- **명확한 분리**: 컨트롤러/UseCase/Mapper가 Query/Command로 완전 분리된 구조

---

## 코드 품질 체크

### ✅ 준수 사항

1. **Hexagonal Architecture**: 명확한 Port-In/Port-Out 분리
2. **CQRS 패턴**: Query 전용 컨트롤러/UseCase 분리
3. **DTO 변환**: Mapper를 통한 계층 간 DTO 변환
4. **Validation**: `@Valid` + `@ParameterObject` 사용
5. **API 문서화**: Swagger `@Parameter`, `@Schema` 어노테이션 적용
6. **기본값 처리**: page/size 기본값 설정
7. **Null-Safety**: record 타입 사용으로 null-safe 구조
8. **날짜 포맷 통일**: ISO-8601 표준 사용

### 📋 개선 가능 사항

1. **페이징 최대값 제한**: size에 대한 max 제한 없음 (DoS 방지 필요)
2. **검증 규칙**: searchField 값 enum 제한 없음 (허용되지 않은 필드 검색 가능)
3. **에러 처리**: BrandErrorMapper 존재 여부 확인 필요
4. **API 문서**: OpenAPI 스펙 자동 생성 확인 필요

---

## 테스트 커버리지 확인 사항

### 필요한 테스트

1. **Controller Test** (`BrandQueryControllerTest`)
   - 정상 조회 (200 OK)
   - 페이징 파라미터 검증
   - 정렬 파라미터 검증
   - 검색 조건 조합 테스트

2. **Mapper Test** (`BrandQueryApiMapperTest`)
   - Request → SearchParams 변환
   - Result → Response 변환
   - 기본값 설정 검증
   - 날짜 포맷 변환 검증

3. **Integration Test**
   - E2E 조회 흐름
   - DB 연동 테스트
   - 페이징/정렬 동작 확인

---

## 문서 생성 정보

- **분석 일시**: 2026-02-06
- **대상 모듈**: `adapter-in/rest-api`
- **대상 패키지**: `com.ryuqq.marketplace.adapter.in.rest.brand`
- **컨트롤러 파일**: `BrandQueryController.java`
- **엔드포인트 Base**: `/api/v1/market/admin/brands`
