---
name: legacy-controller-generator
description: 마이그레이션 사이클의 마지막 단계. Controller + ApiMapper 생성하여 전체 흐름 완성. 자동으로 사용.
tools: Read, Write, Edit, Glob
model: opus
---

# Legacy Controller Generator Agent (Adapter-In Layer)

## ⛔ 필수 규칙

> **정의된 출력물만 생성할 것. 임의로 파일이나 문서를 추가하지 말 것.**

- "📁 저장 경로"에 명시된 파일만 생성:
  - `controller/`, `mapper/`, `request/`, `response/` 디렉토리 내 파일만
- 요약 문서, 추가 설명 파일, README 등 정의되지 않은 파일 생성 금지
- 콘솔 출력은 자유롭게 하되, 파일 생성은 명시된 것만

---

마이그레이션 사이클의 **마지막 단계**. Controller + ApiMapper를 생성하여 전체 흐름을 완성하는 전문가 에이전트.

## 📁 저장 경로

```text
adapter-in/rest-api-{admin|web}/src/main/java/com/ryuqq/setof/adapter/in/rest/{admin|web}/
└── v1/
    └── {domain}/
        ├── controller/
        │   └── Legacy{Domain}QueryV1Controller.java
        ├── mapper/
        │   └── Legacy{Domain}V1ApiMapper.java
        ├── request/
        │   └── Search{Domain}V1ApiRequest.java    # /legacy-convert에서 복사
        └── response/
            └── {Domain}V1ApiResponse.java         # /legacy-convert에서 복사
```

## 🔀 소스 구분 (접두사 방식)

| 접두사 | 대상 모듈 | Convert 경로 |
|--------|----------|-------------|
| `web:` (기본) | adapter-in/rest-api-web | `claudedocs/legacy-converts/web/` |
| `admin:` | adapter-in/rest-api-admin | `claudedocs/legacy-converts/admin/` |

---

## 🎯 핵심 원칙

> **이전 단계 확인 → DTO 복사 → ApiMapper 생성 → Controller 생성**

---

## 📋 생성 파일

| 파일 | 역할 |
|------|------|
| `Legacy{Domain}QueryV1Controller.java` | 조회 Controller - UseCase 호출 |
| `Legacy{Domain}CommandV1Controller.java` | 명령 Controller (필요시) |
| `Legacy{Domain}V1ApiMapper.java` | Request → Command, Result → Response 변환 |
| `Search{Domain}V1ApiRequest.java` | 검색 요청 DTO (복사) |
| `{Domain}V1ApiResponse.java` | 응답 DTO (복사) |

---

## 🔍 생성 워크플로우

### Phase 1: 이전 단계 확인

```python
# 접두사 파싱
prefix, endpoint = parse_prefix(input)  # "admin:BrandController.fetchBrands"

# 1. /legacy-convert 결과물 확인
convert_path = f"claudedocs/legacy-converts/{prefix}/{Controller}_{method}"
Read(f"{convert_path}/request/Search{Domain}V1ApiRequest.java")
Read(f"{convert_path}/response/{Domain}V1ApiResponse.java")

# 2. /legacy-service 결과물 확인
app_path = f"application/.../legacy/{domain}"
Glob(f"{app_path}/port/in/Legacy{Domain}QueryUseCase.java")
Glob(f"{app_path}/dto/composite/Legacy{Domain}CompositeResult.java")
```

### Phase 2: 대상 모듈 결정

```python
# 접두사에 따라 모듈 결정
if prefix == "admin":
    base_path = "adapter-in/rest-api-admin/src/main/java/com/ryuqq/setof/adapter/in/rest/admin"
else:  # web (기본)
    base_path = "adapter-in/rest-api-web/src/main/java/com/ryuqq/setof/adapter/in/rest/web"
```

### Phase 3: DTO 복사 및 패키지 수정

```python
# 1. Request DTO 복사
source = f"claudedocs/legacy-converts/{prefix}/{Controller}_{method}/request/Search{Domain}V1ApiRequest.java"
target = f"{base_path}/v1/{domain}/request/Search{Domain}V1ApiRequest.java"
Read(source) → 패키지 수정 → Write(target)

# 2. Response DTO 복사
source = f"claudedocs/legacy-converts/{prefix}/{Controller}_{method}/response/{Domain}V1ApiResponse.java"
target = f"{base_path}/v1/{domain}/response/{Domain}V1ApiResponse.java"
Read(source) → 패키지 수정 → Write(target)
```

### Phase 4: ApiMapper 생성

```java
// mapper/Legacy{Domain}V1ApiMapper.java
@Component
public class Legacy{Domain}V1ApiMapper {

    /**
     * Request → Command 변환.
     */
    public Legacy{Domain}QueryCommand toCommand(Search{Domain}V1ApiRequest request) {
        return Legacy{Domain}QueryCommand.builder()
            .lastDomainId(request.lastDomainId())
            .categoryId(request.categoryId())
            .brandId(request.brandId())
            .sellerId(request.sellerId())
            .page(request.page() != null ? request.page() : 0)
            .size(request.size() != null ? request.size() : 20)
            .build();
    }

    /**
     * Result → SliceResponse 변환.
     */
    public SliceResponse<{Domain}V1ApiResponse> toSliceResponse(
            Legacy{Domain}CompositeResult result) {
        List<{Domain}V1ApiResponse> items = result.items().stream()
            .map(this::toResponse)
            .toList();

        return SliceResponse.of(
            items,
            result.sliceMeta().cursor(),
            result.sliceMeta().hasNext(),
            result.sliceMeta().count()
        );
    }

    private {Domain}V1ApiResponse toResponse(Legacy{Domain}Detail detail) {
        return new {Domain}V1ApiResponse(
            detail.{domain}Id(),
            detail.name(),
            // ... 필드 매핑
        );
    }
}
```

### Phase 5: Controller 생성

```java
// controller/Legacy{Domain}QueryV1Controller.java
@Tag(name = "Legacy {Domain}", description = "레거시 {Domain} API")
@RestController
@RequestMapping("/api/v1/legacy/{domain}")
public class Legacy{Domain}QueryV1Controller {

    private final Legacy{Domain}QueryUseCase queryUseCase;
    private final Legacy{Domain}V1ApiMapper apiMapper;

    public Legacy{Domain}QueryV1Controller(
            Legacy{Domain}QueryUseCase queryUseCase,
            Legacy{Domain}V1ApiMapper apiMapper) {
        this.queryUseCase = queryUseCase;
        this.apiMapper = apiMapper;
    }

    @Operation(summary = "{Domain} 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<{Domain}V1ApiResponse>>> fetch{Domain}s(
            @ModelAttribute Search{Domain}V1ApiRequest request) {

        var command = apiMapper.toCommand(request);
        var result = queryUseCase.fetch{Domain}s(command);
        var response = apiMapper.toSliceResponse(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### Phase 6: 파일 저장

```python
target_path = f"{base_path}/v1/{domain}"

mkdir -p {target_path}/{controller,mapper,request,response}

Write(f"{target_path}/controller/Legacy{Domain}QueryV1Controller.java")
Write(f"{target_path}/mapper/Legacy{Domain}V1ApiMapper.java")
Write(f"{target_path}/request/Search{Domain}V1ApiRequest.java")   # 패키지 수정 후
Write(f"{target_path}/response/{Domain}V1ApiResponse.java")        # 패키지 수정 후
```

---

## 🛠️ 사용 도구

- **Read**: 이전 단계 결과물 확인, DTO 읽기
- **Write**: Controller, ApiMapper, DTO 생성
- **Edit**: 패키지 선언 수정
- **Glob**: 기존 파일 탐색

---

## 📋 품질 기준

| 항목 | 기준 |
|------|------|
| **이전 단계 의존** | /legacy-convert, /legacy-service 완료 필수 |
| **패키지 일관성** | adapter-in 패키지 구조 준수 |
| **UseCase 호출** | Application Layer UseCase만 호출 |
| **ApiMapper 역할** | Request↔Command, Result↔Response 변환만 담당 |
| **Swagger 어노테이션** | @Tag, @Operation 필수 |

---

## 🔗 전체 흐름 연결

```
┌─────────────────────────────────────────────────────────────┐
│  Controller                                                 │
│    ↓ Request DTO 받음                                       │
│    ↓ ApiMapper.toCommand()                                  │
│    ↓ UseCase 호출                                           │
│    ↓ Result 받음                                            │
│    ↓ ApiMapper.toSliceResponse()                            │
│    ↓ Response 반환                                          │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│  Application Layer (Service → Manager)                      │
│    ↓ Command 받음                                           │
│    ↓ Manager가 Port 호출 (@Transactional)                   │
│    ↓ Assembler가 Result 조립 (SliceMeta 포함)               │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│  Persistence Layer (Adapter → Repository)                   │
│    ↓ SearchCondition 받음                                   │
│    ↓ QueryDSL 실행                                          │
│    ↓ Mapper가 QueryDto → Application DTO 변환               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 연계 작업

```bash
/legacy-endpoints admin:brand                       # 1. 전체 엔드포인트 분석
    ↓
/legacy-flow admin:BrandController.fetchBrands      # 2. 상세 흐름 분석
    ↓
/legacy-convert admin:BrandController.fetchBrands   # 3. DTO 생성
    ↓
/legacy-query admin:BrandController.fetchBrands     # 4. Persistence Layer
    ↓
/legacy-service admin:BrandController.fetchBrands   # 5. Application Layer
    ↓
/legacy-controller admin:BrandController.fetchBrands # 6. Adapter-In Layer ★
```
