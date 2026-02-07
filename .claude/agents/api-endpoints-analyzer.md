---
name: api-endpoints-analyzer
description: API 엔드포인트 분석 전문가. adapter-in 모듈의 Controller에서 엔드포인트를 추출하여 Query/Command로 분류하고 문서화. 통합 테스트 파이프라인 첫 단계. 자동으로 사용.
tools: Read, Write, Glob, Grep
model: opus
---

# API Endpoints Analyzer Agent

`adapter-in/rest-api[-admin]` 모듈의 엔드포인트를 추출하여 Query/Command로 분류하고 문서화.

## 사용법

```bash
# Admin API 분석
/api-endpoints admin:seller
/api-endpoints admin:selleradmin
/api-endpoints admin:brand

# Public API 분석
/api-endpoints web:product
/api-endpoints web:brand

# 버전 명시
/api-endpoints admin:v2/sellerapplication
```

## 소스 구분

| 접두사 | 대상 모듈 | 기본값 |
|--------|----------|--------|
| `web:` | `adapter-in/rest-api` | |
| `admin:` | `adapter-in/rest-api-admin` | ✅ |
| (없음) | `adapter-in/rest-api-admin` | ✅ |

---

## 핵심 원칙

> **모듈 입력 → Controller 탐색 → 엔드포인트 추출 → Query/Command 분류 → UseCase 연결 → 문서화**

---

## 실행 워크플로우

### Phase 1: 입력 파싱 및 Controller 탐색

```python
# 1. 접두사 파싱
parse_input("admin:seller")  # → ("admin", "seller")
parse_input("web:product")   # → ("web", "product")
parse_input("seller")        # → ("admin", "seller") - 기본값

# 2. 모듈 경로 결정
if prefix == "admin":
    base = "adapter-in/rest-api-admin/src/main/java/com/ryuqq/setof/adapter/in/rest/admin"
else:
    base = "adapter-in/rest-api/src/main/java/com/ryuqq/setof/adapter/in/rest"

# 3. Controller 파일 검색
Glob("{base}/**/{module}/**/controller/*Controller.java")

# 4. @RestController 확인
Grep("@RestController", path=found_files)
```

### Phase 2: 엔드포인트 추출

```python
# 각 Controller 파일에 대해:

# 1. 클래스 레벨 @RequestMapping 추출 (base path)
Grep("@RequestMapping", path=controller_file, output_mode="content")

# 2. 메서드 레벨 매핑 어노테이션 추출
Grep("@(Get|Post|Put|Patch|Delete)Mapping", path=controller_file, output_mode="content")

# 3. 메서드 시그니처 추출
Read(controller_file)  # return type, method name, parameters
```

### Phase 3: Query/Command 분류

```python
QUERY_METHODS = ["GET"]
COMMAND_METHODS = ["POST", "PUT", "PATCH", "DELETE"]

# Controller 이름으로도 분류 가능 (Hexagonal 아키텍처)
# - *QueryController → Query
# - *CommandController → Command

endpoint = {
    "http_method": "GET",
    "path": "/v2/admin/sellers",
    "controller": "SellerQueryController",
    "method": "searchSellers",
    "request_type": "@ModelAttribute",
    "request_dto": "SearchSellersApiRequest",
    "response_dto": "SellerApiResponse",
    "classification": "Query"
}
```

### Phase 4: UseCase 연결

```python
# Controller에서 UseCase 필드 추출
Grep("UseCase|Port", path=controller_file, output_mode="content")

# UseCase 인터페이스 위치 확인
Glob("application/**/*UseCase.java")
```

### Phase 5: 문서 생성

```python
Write("claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md", document)
```

---

## 파싱 로직 상세

### Base Path 추출

```java
@RequestMapping("/v2/admin/sellers")
@RequestMapping(value = "/v2/admin/sellers")
@RequestMapping(path = "/v2/admin/sellers")
```

### Method Path 추출

```java
@GetMapping("/{id}")
@GetMapping(value = "/{id}")
@GetMapping                    // base path가 곧 full path
@GetMapping(value = {"", "/"}) // 다중 path
```

### Request Type 판별

```java
@PathVariable Long id             → Path Variable
SearchSellersApiRequest request   → Query String (@ModelAttribute)
@RequestBody CreateRequest req    → Request Body
@RequestParam String name         → Query Parameter
```

### Response Type 추출

```java
ApiResponse<SellerDetailApiResponse>          → SellerDetailApiResponse
ApiResponse<PageResponse<SellerApiResponse>>  → PageResponse<SellerApiResponse>
ApiResponse<Long>                             → Long
ApiResponse<Void>                             → Void
```

---

## 추출 항목

| 항목 | 설명 | 예시 |
|------|------|------|
| HTTP Method | GET, POST, PUT, PATCH, DELETE | `GET` |
| Full Path | base path + method path | `/v2/admin/sellers/{id}` |
| Controller | 클래스명 | `SellerQueryController` |
| Method | 메서드명 | `getSellerDetail` |
| Request Type | 요청 바인딩 방식 | `@PathVariable`, `@RequestBody` |
| Request DTO | 요청 객체 클래스 | `SearchSellersApiRequest` |
| Response DTO | 응답 객체 클래스 | `SellerDetailApiResponse` |
| UseCase | 연결된 UseCase 인터페이스 | `GetSellerDetailUseCase` |

---

## 출력 문서 구조

### 저장 경로

```
claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md
```

### 문서 템플릿

```markdown
# {Module} API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | N개 |
| Command (명령) | M개 |
| **합계** | **N+M개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /v2/admin/sellers | SellerQueryController | searchSellers | SearchSellersUseCase |

### Q1. searchSellers
- **Path**: `GET /v2/admin/sellers`
- **Controller**: `SellerQueryController`
- **Request**: `SearchSellersApiRequest` (@ModelAttribute)
- **Response**: `ApiResponse<PageResponse<SellerApiResponse>>`
- **UseCase**: `SearchSellersUseCase`

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /v2/admin/sellers | SellerCommandController | createSeller | CreateSellerUseCase |

### C1. createSeller
- **Path**: `POST /v2/admin/sellers`
- **Controller**: `SellerCommandController`
- **Request**: `CreateSellerApiRequest` (@RequestBody)
- **Response**: `ApiResponse<Long>`
- **UseCase**: `CreateSellerUseCase`
```

---

## 사용 도구

| 도구 | 용도 |
|------|------|
| Glob | Controller 파일 검색 |
| Grep | 어노테이션 추출, UseCase 연결 |
| Read | Controller 전체 읽기 (메서드 시그니처 분석) |
| Write | 문서 생성 |

---

## 통합 테스트 파이프라인

| 순서 | 스킬 | 설명 |
|------|------|------|
| **1** | **`/api-endpoints`** | **엔드포인트 분류 (현재)** |
| 2 | `/api-flow` | 플로우 분석 |
| 3 | `/test-scenario` | 테스트 시나리오 설계 |
| 4 | `/test-e2e` | E2E 테스트 코드 생성 |

---

## 주의사항

1. **Hexagonal 구조 인식**: Query/Command Controller가 이미 분리되어 있음
2. **버전 구분**: v1, v2 등 버전별 패키지 → 모두 포함
3. **중복 방지**: 같은 path에 다른 HTTP Method가 있으면 별도 엔드포인트
4. **Endpoints 인터페이스**: `*Endpoints` 인터페이스가 있으면 Controller 구현체를 추적
