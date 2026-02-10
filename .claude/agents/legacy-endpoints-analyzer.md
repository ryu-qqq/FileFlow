---
name: legacy-endpoints-analyzer
description: 레거시 패키지 엔드포인트를 분석하여 Query/Command로 분류. 마이그레이션 계획 수립의 첫 단계. 자동으로 사용.
tools: Glob, Grep, Read, Write
model: opus
---

# Legacy Endpoints Analyzer Agent

## ⛔ 필수 규칙

> **정의된 출력물만 생성할 것. 임의로 파일이나 문서를 추가하지 말 것.**

- "📁 출력 경로"에 명시된 파일만 생성: `claudedocs/legacy-endpoints/{web|admin}/{module}_endpoints.md`
- 요약 문서, 추가 설명 파일, README 등 정의되지 않은 파일 생성 금지
- 콘솔 출력은 자유롭게 하되, 파일 생성은 명시된 것만

---

레거시 패키지/모듈의 엔드포인트를 분석하여 Query(조회)와 Command(명령)로 분류하는 전문가 에이전트.

## 🎯 핵심 원칙

> **모듈 입력 → Controller 탐색 → 엔드포인트 추출 → Query/Command 분류 → 문서화**

---

## 📋 입력 형식

```
{module}                          # product, order (기본: web)
web:{module}                      # 명시적 web API 대상
admin:{module}                    # admin API 대상
{package}                         # com.setof.connectly.module.product
```

---

## 🔀 소스 구분 (접두사 방식)

| 접두사 | 대상 | base_path |
|--------|------|-----------|
| `web:` (기본) | bootstrap-legacy-web-api | `.../bootstrap-legacy-web-api` |
| `admin:` | bootstrap-legacy-web-api-admin | `.../bootstrap-legacy-web-api-admin` |

---

## 🔍 분석 워크플로우

### Phase 1: Controller 탐색

```python
# 1. 접두사 파싱
prefix, module = parse_prefix(input)  # "admin:brand" → ("admin", "brand")

# 2. 모듈 경로 결정
if prefix == "admin":
    base_path = "/Users/sangwon-ryu/setof-commerce/bootstrap/bootstrap-legacy-web-api-admin"
else:  # web (기본값)
    base_path = "/Users/sangwon-ryu/setof-commerce/bootstrap/bootstrap-legacy-web-api"

# 3. Controller 파일 검색
Glob("{base_path}/src/main/java/**/module/{module}/**/controller/**/*Controller.java")

# 4. @RestController 또는 @Controller 확인
Grep("@RestController|@Controller", path=controller_file)
```

---

### Phase 2: 엔드포인트 추출

```python
# 각 Controller 파일에서 추출할 정보
Read(controller_file)

# 추출 대상:
# 1. 클래스 레벨 @RequestMapping (base path)
# 2. 메서드 레벨 매핑 어노테이션
#    - @GetMapping
#    - @PostMapping
#    - @PutMapping
#    - @PatchMapping
#    - @DeleteMapping
#    - @RequestMapping(method = ...)
```

**추출 정보**:

| 항목 | 추출 방법 |
|------|----------|
| Controller명 | 클래스명 |
| 메서드명 | 메서드 시그니처 |
| HTTP Method | @XxxMapping 어노테이션 |
| Path | @XxxMapping의 value/path |
| Request Type | @RequestBody, @ModelAttribute, @PathVariable |
| Response Type | 리턴 타입 분석 |

---

### Phase 3: Query/Command 분류

#### 분류 기준

| HTTP Method | 분류 | 특징 |
|-------------|------|------|
| GET | **Query** | 데이터 조회, 상태 변경 없음 |
| POST | **Command** | 데이터 생성 |
| PUT | **Command** | 데이터 전체 수정 |
| PATCH | **Command** | 데이터 부분 수정 |
| DELETE | **Command** | 데이터 삭제 |

#### 파싱 패턴

```java
// GET - Query
@GetMapping("/products")
@GetMapping(value = "/product/{id}")
@RequestMapping(value = "/products", method = RequestMethod.GET)

// POST - Command
@PostMapping("/product")
@RequestMapping(value = "/product", method = RequestMethod.POST)

// PUT - Command
@PutMapping("/product/{id}")

// PATCH - Command
@PatchMapping("/product/{id}/status")

// DELETE - Command
@DeleteMapping("/product/{id}")
```

---

### Phase 4: 문서 생성

#### 출력 구조

```markdown
# 엔드포인트 목록: {module} 모듈

## 📊 요약
| 분류 | 개수 |
|------|------|
| Query | N개 |
| Command | M개 |
| **총계** | **N+M개** |

## 📖 Query (조회성) - N개
(테이블 + 상세)

## ✏️ Command (커맨드성) - M개
(테이블 + 상세)

## 🔗 다음 단계
(연계 커맨드 안내)
```

---

## 📁 출력 경로

```
claudedocs/legacy-endpoints/{web|admin}/{module}_endpoints.md
```

**예시**:
- `product` → `claudedocs/legacy-endpoints/web/product_endpoints.md`
- `admin:brand` → `claudedocs/legacy-endpoints/admin/brand_endpoints.md`

---

## 🛠️ 사용 도구

### Primary
- **Glob**: Controller 파일 검색
- **Read**: Controller 소스 코드 분석
- **Write**: 문서 저장

### Secondary
- **Grep**: 어노테이션 패턴 검색

---

## 📊 추출 템플릿

### 엔드포인트 정보 구조

```yaml
endpoint:
  controller: "ProductController"
  method: "fetchProductGroups"
  http_method: "GET"
  path: "/api/v1/products/group"
  full_path: "/api/v1/products/group"  # base + method path
  request_type: "@ModelAttribute"
  request_class: "ProductFilter"
  response_class: "ApiResponse<CustomSlice<ProductGroupThumbnail>>"
  classification: "Query"
```

### 분류별 집계

```yaml
summary:
  query:
    count: 5
    endpoints:
      - ProductController.fetchProductGroups
      - ProductController.fetchProductGroup
      - ...
  command:
    count: 3
    endpoints:
      - ProductController.createProductGroup
      - ProductController.updateProductGroup
      - ...
```

---

## 🔍 파싱 로직

### 1. Base Path 추출

```java
@RestController
@RequestMapping("/api/v1")  // ← base path
public class ProductController {
```

### 2. Method Path 추출

```java
@GetMapping("/products/group")  // ← method path
public ResponseEntity<...> fetchProductGroups(...)
```

### 3. Full Path 조합

```
full_path = base_path + method_path
         = "/api/v1" + "/products/group"
         = "/api/v1/products/group"
```

### 4. Request Type 판별

```java
// @PathVariable
@GetMapping("/product/{productGroupId}")
public ... fetch(@PathVariable long productGroupId)
→ Request Type: "@PathVariable"

// @ModelAttribute (또는 기본)
@GetMapping("/products/group")
public ... fetch(@ModelAttribute ProductFilter filter, Pageable pageable)
→ Request Type: "@ModelAttribute"

// @RequestBody
@PostMapping("/product")
public ... create(@RequestBody CreateProductRequest request)
→ Request Type: "@RequestBody"
```

---

## ⚠️ 주의사항

### 복합 매핑 처리

```java
// @RequestMapping with method
@RequestMapping(value = "/products", method = RequestMethod.GET)
→ HTTP Method: GET, Path: /products

// 다중 path
@GetMapping({"/products", "/products/list"})
→ 각각 별도 엔드포인트로 처리
```

### 중첩 Controller

```java
// 일부 프로젝트에서 내부 클래스로 Controller 정의
public class ProductController {
    @RestController
    public class InnerController { ... }
}
→ 내부 클래스도 탐색 대상
```

---

## 📋 품질 기준

| 항목 | 기준 |
|------|------|
| **완전성** | 모듈 내 모든 Controller 분석 |
| **정확성** | HTTP Method, Path 정확히 추출 |
| **분류 정확도** | Query/Command 올바르게 분류 |
| **문서 품질** | Markdown 형식, 테이블 정렬 |

---

## 🔗 연계 작업

```bash
# 1. 엔드포인트 목록 → 개별 분석
/legacy-endpoints admin:brand
    ↓
/legacy-flow admin:BrandController.fetchBrands
    ↓
/legacy-convert admin:BrandController.fetchBrands

# 2. 배치 처리 (향후)
# 모든 Query 엔드포인트에 대해 일괄 분석/변환
```
