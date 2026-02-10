---
name: legacy-migrate
description: 레거시 API 전체 마이그레이션 워크플로우. 6단계 자동화 파이프라인.
disable-model-invocation: true
---

# /legacy-migrate

레거시 API를 새 아키텍처로 **완전 마이그레이션**하는 자동화 워크플로우입니다.

## 사용법

```bash
/legacy-migrate admin:brand           # Admin API brand 모듈 전체 마이그레이션
/legacy-migrate web:product           # Web API product 모듈 전체 마이그레이션
/legacy-migrate admin:brand --step 3  # 3단계(legacy-convert)부터 재개
```

## 입력

- `$ARGUMENTS[0]`: 대상 모듈 (예: `admin:brand`, `web:product`)
- `$ARGUMENTS[1]`: (선택) `--step N` - N단계부터 시작

---

## 워크플로우 (6단계 순차 실행)

다음 순서로 각 **Agent를 순차적으로 호출**하세요.
각 단계 완료 후 결과를 확인하고, 다음 Agent에 필요한 정보를 전달합니다.

### Step 1: 엔드포인트 분석

```
Agent: legacy-endpoints-analyzer
입력: $ARGUMENTS[0]의 모듈 부분 (예: admin:brand → admin:brand)
출력: claudedocs/legacy-endpoints/{prefix}/{module}_endpoints.md
```

**확인사항**: Query 엔드포인트 목록 추출

---

### Step 2: API 흐름 분석

```
Agent: legacy-flow-analyzer
입력: Step 1에서 추출한 각 Query 엔드포인트
      예: admin:BrandController.fetchBrands
출력: claudedocs/legacy-flows/{prefix}/{Controller}_{method}.md
```

**확인사항**: Request/Response 구조, 호출 스택, QueryDSL 쿼리

---

### Step 3: DTO 변환

```
Agent: legacy-dto-converter
입력: Step 2의 분석 결과 기반
출력: adapter-in/rest-api-{admin|web}/.../dto/request/, response/
```

**확인사항**: record 타입, @Schema/@Parameter 어노테이션, Validation

---

### Step 4: Persistence Layer 생성

```
Agent: legacy-query-generator
입력: Step 2의 QueryDSL 분석 결과 기반
출력:
  - domain/.../legacy/{prefix}/{domain}/dto/query/
  - application/.../legacy/{prefix}/{domain}/dto/response/
  - adapter-out/persistence-mysql-legacy/.../composite/{prefix}/{domain}/
```

**확인사항**: SearchCondition, Result DTO, Repository, Adapter

---

### Step 5: Application Layer 생성

```
Agent: legacy-service-generator
입력: Step 4의 Persistence Layer 기반
출력: application/.../legacy/{prefix}/{domain}/
      - port/in/, port/out/
      - service/, manager/, assembler/
```

**확인사항**: UseCase, Port, Service, Manager, Assembler

---

### Step 6: Controller 생성

```
Agent: legacy-controller-generator
입력: Step 3 DTO + Step 5 UseCase
출력: adapter-in/rest-api-{admin|web}/.../v1/{domain}/
      - controller/, mapper/
```

**확인사항**: Controller, ApiMapper

---

## 실행 지침

1. **순차 실행**: 각 Agent 완료 후 다음 Agent 호출
2. **결과 전달**: 이전 Agent의 출력을 다음 Agent에 전달
3. **에러 처리**: 실패 시 해당 단계에서 중단하고 사용자에게 보고
4. **진행 상황 보고**: 각 단계 완료 시 결과 요약 출력

## 진행 상황 보고 형식

```
✅ Step 1/6: 엔드포인트 분석 완료
   - Query: 5개, Command: 3개
   - 출력: claudedocs/legacy-endpoints/admin/brand_endpoints.md

🔄 Step 2/6: API 흐름 분석 중...
   - 대상: BrandController.fetchBrands
```

## 마이그레이션 완료 후

```
✅ 마이그레이션 완료: admin:brand

📁 생성된 파일:
- DTO: 2개
- Repository: 2개
- Service: 5개
- Controller: 2개

🔗 다음 단계:
- 테스트 작성: /test-repository, /test-api
- 코드 리뷰: /review
```
