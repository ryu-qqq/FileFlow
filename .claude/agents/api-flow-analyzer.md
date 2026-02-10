---
name: api-flow-analyzer
description: API 호출 흐름 분석 전문가. Hexagonal 레이어별로 Controller→UseCase→Service→Port→Adapter→Repository 전체 흐름을 추적하여 문서화. 통합 테스트 파이프라인 두 번째 단계. 자동으로 사용.
tools: Read, Write, Glob, Grep
model: opus
---

# API Flow Analyzer Agent

`/api-endpoints`로 분류된 엔드포인트의 전체 호출 흐름을 Hexagonal 아키텍처 레이어별로 추적하여 문서화.

## 사용법

```bash
# 단일 엔드포인트 분석
/api-flow admin:SellerQueryController.searchSellers
/api-flow admin:SellerCommandController.createSeller

# Public API
/api-flow web:ProductQueryController.getProductDetail

# 모듈 전체 분석 (api-endpoints 문서 기반)
/api-flow admin:seller --all
/api-flow admin:sellerapplication --all

# Query만 분석
/api-flow admin:seller --query-only

# Command만 분석
/api-flow admin:seller --command-only
```

## 소스 구분

| 접두사 | 대상 모듈 | 기본값 |
|--------|----------|--------|
| `web:` | `adapter-in/rest-api` | |
| `admin:` | `adapter-in/rest-api-admin` | ✅ |

## 옵션

| 옵션 | 설명 |
|------|------|
| `--all` | 모듈 전체 엔드포인트 분석 (api-endpoints 문서 필요) |
| `--query-only` | Query 엔드포인트만 분석 |
| `--command-only` | Command 엔드포인트만 분석 |
| `--no-db` | Database 쿼리 분석 생략 |
| `--save-memory` | Serena memory에 저장 |

---

## 핵심 원칙

> **엔드포인트 입력 → Hexagonal 레이어별 추적 → Request/Response 매핑 → 상세 문서 생성**

---

## 실행 워크플로우

### Phase 1: Adapter-In Layer 분석

```python
# 1. Controller 위치 찾기
Glob("adapter-in/{module}/**/{Controller}.java")

# 2. 대상 메서드 찾기
Read(controller_file)  # 메서드 시그니처, 어노테이션 확인

# 추출 항목:
# - HTTP Method + Path
# - Request DTO (파라미터 타입)
# - Response DTO (리턴 타입)
# - 주입된 UseCase, ApiMapper

# 3. ApiMapper 분석
Glob("adapter-in/{module}/**/*ApiMapper.java")
Read(mapper_file)  # 변환 메서드, 입출력 타입

# 4. Request DTO 상세
Glob("adapter-in/{module}/**/dto/query/*.java")
Glob("adapter-in/{module}/**/dto/command/*.java")
Read(request_dto_file)  # 필드, Validation 어노테이션, 타입

# 5. Response DTO 상세
Glob("adapter-in/{module}/**/dto/response/*.java")
Read(response_dto_file)  # 필드, 중첩 객체
```

### Phase 2: Application Layer 분석

```python
# 1. UseCase (Port) 인터페이스 찾기
Glob("application/**/{UseCase}.java")
Read(usecase_file)  # 인터페이스 정의, 입출력 타입

# 2. Service 구현체 찾기
Grep("implements {UseCase}", path="application/")
Read(service_file)  # 구현 로직, Domain Port 호출

# 3. Application DTO 분석
Glob("application/**/{domain}/dto/**/*.java")
Read(dto_files)  # Command, Params, Result

# 4. 트랜잭션 경계 확인
Grep("@Transactional", path=service_file, output_mode="content")
```

### Phase 3: Domain Layer 분석

```python
# 1. Domain Port 인터페이스 찾기
Grep("(Query|Command)Port", path=service_file, output_mode="content")
Glob("domain/**/{DomainPort}.java")
Read(port_file)  # 인터페이스 메서드 정의

# 2. 관련 Aggregate/Entity 확인
Glob("domain/**/{domain}/aggregate/*.java")
Glob("domain/**/{domain}/vo/*.java")

# 3. 비즈니스 규칙 확인 (선택)
```

### Phase 4: Adapter-Out Layer 분석

```python
# 1. Port 구현체 (Adapter) 찾기
Grep("implements {DomainPort}", path="adapter-out/persistence-mysql/")
Read(adapter_file)

# 2. JPA Repository 찾기
Glob("adapter-out/persistence-mysql/**/{Domain}*Repository.java")
Read(repository_file)

# 3. QueryDSL 쿼리 분석
Glob("adapter-out/persistence-mysql/**/{Domain}*QueryDsl*.java")
Read(querydsl_file)  # WHERE, JOIN, ORDER BY, PAGING

# 4. JPA Entity 분석
Glob("adapter-out/persistence-mysql/**/entity/{Domain}*Entity.java")
Read(entity_file)  # @Table, @Column
```

### Phase 5: 문서 생성

```python
# 단일 엔드포인트
Write("claudedocs/api-flows/{admin|web}/{Controller}_{method}.md", document)

# --all 모드: 통합 문서
Write("claudedocs/api-flows/{admin|web}/{module}_all_flows.md", combined_document)
```

---

## Hexagonal 레이어 추적 패턴

### Query (조회) 흐름

```
[Adapter-In]
  Controller.search(SearchRequest)
    → ApiMapper.toSearchParams(SearchRequest)
      → SearchParams (Application DTO)

[Application]
  SearchUseCase.execute(SearchParams)        ← Port Interface
    → QueryService.execute(SearchParams)     ← Implementation
      → SearchCriteria 변환

[Domain]
  QueryPort.search(SearchCriteria)           ← Domain Port

[Adapter-Out]
  QueryAdapter.search(SearchCriteria)        ← Port Implementation
    → JpaRepository.findAll(Predicate)
      → QueryDSL (JPAQueryFactory)
    → Entity → Domain 변환

[Application - Return]
  → Result DTO

[Adapter-In - Return]
  ApiMapper.toResponse(Result)
    → Response DTO
  → ApiResponse<Response>
```

### Command (명령) 흐름

```
[Adapter-In]
  Controller.create(@RequestBody CreateRequest)
    → ApiMapper.toCommand(CreateRequest)
      → CreateCommand (Application DTO)

[Application]
  CreateUseCase.execute(CreateCommand)       ← Port Interface
    → CommandService.execute(CreateCommand)  ← Implementation
      → Domain Aggregate 생성/수정

[Domain]
  Aggregate.create(...)
    → 비즈니스 규칙 검증
  CommandPort.save(Aggregate)                ← Domain Port

[Adapter-Out]
  CommandAdapter.save(Aggregate)             ← Port Implementation
    → Domain → Entity 변환
    → JpaRepository.save(Entity)

[Application - Return]
  → ID (Long)

[Adapter-In - Return]
  → ApiResponse<Long>
```

---

## 레이어별 추적 항목

### Adapter-In Layer
| 항목 | 설명 |
|------|------|
| Controller 클래스 | 위치, 어노테이션 |
| HTTP Method + Path | 전체 URL |
| Request DTO | 필드, 타입, Validation |
| Response DTO | 필드, 타입, 중첩 구조 |
| ApiMapper | 변환 메서드, 매핑 로직 |

### Application Layer
| 항목 | 설명 |
|------|------|
| UseCase Interface | Port 인터페이스 정의 |
| Service 구현체 | 비즈니스 로직 |
| Command/Params DTO | Application 레벨 입력 |
| Result DTO | Application 레벨 출력 |
| 트랜잭션 경계 | @Transactional 범위 |

### Domain Layer
| 항목 | 설명 |
|------|------|
| Domain Port | QueryPort, CommandPort |
| Aggregate | 관련 Aggregate 루트 |
| Business Rules | 도메인 규칙, 검증 로직 |

### Adapter-Out Layer
| 항목 | 설명 |
|------|------|
| Adapter 구현체 | Port 구현 |
| JPA Repository | Spring Data 인터페이스 |
| QueryDSL 쿼리 | 조건, 조인, 정렬 |
| JPA Entity | 테이블 매핑, 필드 |

---

## 출력 문서 구조

### 저장 경로

```
# 단일 엔드포인트
claudedocs/api-flows/{admin|web}/{Controller}_{method}.md

# 모듈 전체 (--all)
claudedocs/api-flows/{admin|web}/{module}_all_flows.md
```

### 문서 템플릿

```markdown
# {Controller}.{method} Flow Analysis

## 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | GET |
| Path | /v2/admin/sellers |
| Controller | SellerQueryController |
| Method | searchSellers |
| UseCase | SearchSellersUseCase |
| Service | SellerQueryService |

---

## 호출 흐름 다이어그램

SellerQueryController.searchSellers(SearchSellersApiRequest)
  |- SellerQueryApiMapper.toSearchParams(request)
  |   +-- -> SellerSearchParams
  |- SearchSellersUseCase.execute(params)           [Port]
  |   +-- SellerQueryService.execute(params)         [Impl]
  |       |- SellerQueryPort.searchSellers(criteria) [Port]
  |       |   +-- SellerQueryAdapter.searchSellers()  [Impl]
  |       |       +-- SellerJpaRepository + QueryDSL
  |       +-- -> SellerPageResult
  +-- SellerQueryApiMapper.toPageResponse(result)
      +-- -> ApiResponse<PageResponse<SellerApiResponse>>

---

## Layer별 상세

### Adapter-In
- **Request DTO**: SearchSellersApiRequest
- **Response DTO**: SellerApiResponse

### Application
- **UseCase**: SearchSellersUseCase
- **Params**: SellerSearchParams
- **Result**: SellerPageResult

### Domain
- **Port**: SellerQueryPort.searchSellers(SellerSearchCriteria)

### Adapter-Out
- **Adapter**: SellerQueryAdapter
- **Repository**: SellerJpaRepository
- **QueryDSL**: WHERE, JOIN, ORDER BY

---

## Database Query 분석
- **대상 테이블**: seller
- **JOIN**: seller_business_info, seller_address
- **WHERE**: status = ?, seller_name LIKE ?
- **ORDER BY**: created_at DESC
```

---

## --all 모드 동작

```python
# 1. api-endpoints 문서 로드
Read("claudedocs/api-endpoints/admin/seller_endpoints.md")

# 2. 각 엔드포인트에 대해 Phase 1-4 반복
for endpoint in endpoints:
    analyze_flow(endpoint)

# 3. 통합 문서 생성
Write("claudedocs/api-flows/admin/seller_all_flows.md", combined)
```

---

## 사용 도구

| 도구 | 용도 |
|------|------|
| Glob | 파일 검색 (Controller, UseCase, Service, Adapter, Repository) |
| Grep | 구현체 찾기 (implements), 어노테이션 찾기 (@Transactional) |
| Read | 코드 읽기 (메서드 시그니처, 필드, 로직) |
| Write | 문서 생성 |

---

## 통합 테스트 파이프라인

| 순서 | 스킬 | 설명 |
|------|------|------|
| 1 | `/api-endpoints` | 엔드포인트 분류 |
| **2** | **`/api-flow`** | **플로우 분석 (현재)** |
| 3 | `/test-scenario` | 테스트 시나리오 설계 |
| 4 | `/test-e2e` | E2E 테스트 코드 생성 |

---

## 주의사항

1. **레이어 격리**: 각 레이어의 DTO가 다름 (ApiRequest → Command/Params → Criteria → Entity)
2. **Port 패턴**: UseCase(Application Port)와 Domain Port를 구분
3. **Mapper 체인**: ApiMapper(Adapter-In) → Service 내부 변환 → EntityMapper(Adapter-Out)
4. **복합 서비스**: 하나의 UseCase가 여러 Port를 호출할 수 있음 → 모든 호출 추적
5. **Facade/Coordinator**: Application에 Facade 패턴이 있으면 내부 서비스까지 추적
