---
description: 대화형 PRD (Product Requirements Document) 생성
tags: [project]
---

# Create PRD - Interactive PRD Generation

당신은 대화형 방식으로 PRD (Product Requirements Document)를 생성하는 작업을 수행합니다.

## 목적

사용자와 Socratic 대화를 통해 요구사항을 수집하고, Layer별로 구조화된 PRD 문서를 생성합니다.

## 입력 형식

사용자는 다음과 같이 기능명을 제공합니다:
```bash
/create-prd "Order Management"
/create-prd "User Authentication"
```

## 실행 단계

### 1. 프로젝트 개요 수집

**질문 템플릿**:
```markdown
## 📋 프로젝트 개요

이 기능의 핵심 목적은 무엇인가요?
- 어떤 비즈니스 문제를 해결하나요?
- 주요 사용자는 누구인가요?
- 성공 기준은 무엇인가요?
```

**수집 항목**:
- 기능명 (Feature Name)
- 비즈니스 목적 (Business Purpose)
- 주요 사용자 (Target Users)
- 성공 기준 (Success Criteria)

### 2. Layer별 요구사항 수집

#### 2.1 Domain Layer 요구사항

**질문 템플릿**:
```markdown
## 🏗️ Domain Layer

핵심 비즈니스 개념(Aggregate)은 무엇인가요?
- 어떤 엔티티가 필요한가요? (예: Order, Customer, Product)
- 각 엔티티의 핵심 속성은?
- 엔티티 간 관계는? (단, Long FK 전략 사용 - JPA 관계 어노테이션 금지)
- 핵심 비즈니스 규칙은?
```

**수집 항목**:
- Aggregate 목록
- Entity 속성 (필드, 타입)
- 비즈니스 규칙 (Invariant)
- Value Object 목록

**Zero-Tolerance 규칙 자동 체크**:
- ✅ Law of Demeter 준수 (Getter 체이닝 금지)
- ✅ Lombok 금지 (Pure Java 또는 Record 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

#### 2.2 Application Layer 요구사항

**질문 템플릿**:
```markdown
## ⚙️ Application Layer

어떤 UseCase가 필요한가요?
- 사용자가 수행하는 주요 작업은? (예: 주문 생성, 주문 취소)
- 각 UseCase의 입력/출력은?
- Transaction 경계는 어떻게 설정하나요?
- 외부 API 호출이 필요한가요? (트랜잭션 밖에서 호출 필수!)
```

**수집 항목**:
- UseCase 목록 (Command/Query 분리)
- Command DTO 정의
- Query DTO 정의
- Transaction 경계 설계
- 외부 API 호출 전략

**Zero-Tolerance 규칙 자동 체크**:
- ✅ Command/Query 분리 (CQRS)
- ✅ Transaction 경계 엄격 관리 (`@Transactional` 내 외부 API 호출 금지)

#### 2.3 Persistence Layer 요구사항

**질문 템플릿**:
```markdown
## 💾 Persistence Layer

어떤 데이터 저장소가 필요한가요?
- JPA Entity 목록은? (Domain과 1:1 매핑 권장)
- 복잡한 쿼리가 필요한가요? (QueryDSL 사용)
- 인덱스 전략은?
```

**수집 항목**:
- JPA Entity 목록
- Repository 인터페이스 정의
- QueryDSL 쿼리 목록
- 인덱스 전략

**Zero-Tolerance 규칙 자동 체크**:
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)
- ✅ QueryDSL 최적화 (N+1 방지)

#### 2.4 REST API Layer 요구사항

**질문 템플릿**:
```markdown
## 🌐 REST API Layer

어떤 API 엔드포인트가 필요한가요?
- HTTP Method는? (POST, GET, PUT, DELETE)
- Request/Response DTO는?
- 인증/인가 전략은?
- 에러 처리 전략은?
```

**수집 항목**:
- API 엔드포인트 목록 (HTTP Method, Path)
- Request DTO 정의
- Response DTO 정의
- HTTP 상태 코드 전략
- Exception Handling 전략

**Zero-Tolerance 규칙 자동 체크**:
- ✅ RESTful 설계 원칙
- ✅ 일관된 Error Response 형식

### 3. 제약사항 및 테스트 전략

**질문 템플릿**:
```markdown
## ⚠️ 제약사항

비기능 요구사항은?
- 성능 목표는? (응답 시간, TPS)
- 보안 요구사항은?
- 확장성 요구사항은?

## 🧪 테스트 전략

테스트 범위는?
- Unit Test 범위는?
- Integration Test 범위는?
- E2E Test가 필요한가요?
```

**수집 항목**:
- 성능 요구사항 (응답 시간, TPS)
- 보안 요구사항 (인증/인가, 데이터 암호화)
- 확장성 요구사항 (동시 사용자, 데이터 크기)
- Unit Test 전략
- Integration Test 전략

### 4. PRD 문서 생성

수집한 정보를 바탕으로 구조화된 PRD 문서를 생성합니다.

**문서 경로**: `docs/prd/{feature-name-kebab-case}.md`

**문서 구조**:
```markdown
# PRD: {Feature Name}

**작성일**: {YYYY-MM-DD}
**작성자**: {사용자명}
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
{수집된 비즈니스 목적}

### 주요 사용자
{수집된 사용자 정보}

### 성공 기준
{수집된 성공 기준}

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### Aggregate 목록
- **{Aggregate1}**
  - 속성: {field1: type, field2: type}
  - 비즈니스 규칙: {rule1, rule2}

#### Value Object 목록
- **{VO1}**: {설명}

#### Zero-Tolerance 규칙 준수
- ✅ Law of Demeter (Getter 체이닝 금지)
- ✅ Lombok 금지 (Pure Java/Record 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

### 2. Application Layer

#### UseCase 목록

**Command UseCase**:
- **{UseCase1}**: {설명}
  - Input: {CommandDTO}
  - Output: {ResponseDTO}
  - Transaction: Yes
  - 외부 API: {Yes/No, 어디서 호출}

**Query UseCase**:
- **{UseCase2}**: {설명}
  - Input: {QueryDTO}
  - Output: {ResponseDTO}
  - Transaction: ReadOnly

#### Zero-Tolerance 규칙 준수
- ✅ Command/Query 분리 (CQRS)
- ✅ Transaction 경계 엄격 관리

### 3. Persistence Layer

#### JPA Entity 목록
- **{Entity1}Entity**
  - 테이블: {table_name}
  - 필드: {field1, field2}
  - 인덱스: {index1, index2}

#### Repository 목록
- **{Repository1}Repository**
  - 메서드: {findByXxx, saveXxx}

#### Zero-Tolerance 규칙 준수
- ✅ Long FK 전략 (관계 어노테이션 금지)
- ✅ QueryDSL 최적화 (N+1 방지)

### 4. REST API Layer

#### API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/{resource} | {설명} | {RequestDTO} | {ResponseDTO} | 201 Created |
| GET | /api/v1/{resource}/{id} | {설명} | - | {ResponseDTO} | 200 OK |

#### Zero-Tolerance 규칙 준수
- ✅ RESTful 설계 원칙
- ✅ 일관된 Error Response 형식

---

## ⚠️ 제약사항

### 비기능 요구사항
- 성능: {응답 시간, TPS}
- 보안: {인증/인가, 암호화}
- 확장성: {동시 사용자, 데이터 크기}

---

## 🧪 테스트 전략

### Unit Test
- Domain: {테스트 범위}
- Application: {테스트 범위}

### Integration Test
- Persistence: {테스트 범위}
- REST API: {테스트 범위}

### E2E Test
- {E2E 시나리오}

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 1주)
- [ ] Domain Aggregate 구현
- [ ] Value Object 구현
- [ ] Domain Unit Test

### Phase 2: Application Layer (예상: 1주)
- [ ] UseCase 구현
- [ ] Command/Query DTO 구현
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 3일)
- [ ] JPA Entity 구현
- [ ] Repository 구현
- [ ] QueryDSL 쿼리 구현

### Phase 4: REST API Layer (예상: 3일)
- [ ] Controller 구현
- [ ] Request/Response DTO 구현
- [ ] Exception Handling 구현

### Phase 5: Integration Test (예상: 2일)
- [ ] Integration Test 작성
- [ ] E2E Test 작성

---

## 📚 참고 문서

- [Domain Layer 규칙](../../docs/coding_convention/02-domain-layer/)
- [Application Layer 규칙](../../docs/coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../../docs/coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../../docs/coding_convention/01-adapter-rest-api-layer/)

---

**다음 단계**: `/jira-from-prd docs/prd/{feature-name-kebab-case}.md`
```

### 5. kentback TDD 계획 제안 (선택사항)

PRD 생성 후, kentback TDD 계획을 제안합니다:

```markdown
## 🎯 kentback TDD 계획 제안

이 PRD를 기반으로 kentback TDD 사이클을 시작할 수 있습니다:

1. **Domain Layer RED**: Law of Demeter 테스트 작성
2. **Domain Layer GREEN**: Domain Aggregate 구현
3. **Domain Layer REFACTOR**: Java 21 Record 패턴 적용

4. **Application Layer RED**: Transaction 경계 테스트 작성
5. **Application Layer GREEN**: UseCase 구현
6. **Application Layer REFACTOR**: Command/Query 분리 최적화

...

커맨드: `/kentback-plan docs/prd/{feature-name-kebab-case}.md`
```

## 대화 흐름 예시

```
사용자: /create-prd "Order Management"

Claude:
📋 Order Management 기능 PRD를 생성하겠습니다!

## 1단계: 프로젝트 개요

이 기능의 핵심 목적은 무엇인가요?
- 어떤 비즈니스 문제를 해결하나요?
- 주요 사용자는 누구인가요?
- 성공 기준은 무엇인가요?

사용자:
- 비즈니스 문제: 주문 관리 프로세스 자동화
- 사용자: 고객, 관리자
- 성공 기준: 주문 생성 응답 시간 < 500ms, 주문 취소율 < 5%

Claude:
✅ 프로젝트 개요 수집 완료

## 2단계: Domain Layer

핵심 비즈니스 개념(Aggregate)은 무엇인가요?
- 어떤 엔티티가 필요한가요? (예: Order, Customer, Product)
- 각 엔티티의 핵심 속성은?
- 엔티티 간 관계는? (Long FK 전략 사용)
- 핵심 비즈니스 규칙은?

사용자:
- Order: orderId, customerId (Long FK), productId (Long FK), status, totalPrice
- 비즈니스 규칙: PLACED 상태에서만 취소 가능

...

Claude:
✅ PRD 문서 생성 완료!

파일: docs/prd/order-management.md

다음 단계:
1. PRD 검토 및 수정
2. `/jira-from-prd docs/prd/order-management.md` - Jira 티켓 생성
3. kentback TDD 사이클 시작
```

## 에러 처리

- **docs/prd/ 디렉토리 없음**: 자동 생성
- **중복 PRD 파일**: 사용자에게 덮어쓰기 확인
- **불완전한 답변**: 추가 질문으로 보완

## Write 도구 사용

PRD 문서 생성 시 Write 도구를 사용합니다:
```
Write 도구:
- file_path: docs/prd/{feature-name-kebab-case}.md
- content: {위 PRD 구조 기반 생성}
```

## 추가 기능

- PRD 템플릿 커스터마이징 지원
- Layer별 우선순위 설정 (선택적 질문)
- 기존 PRD 확장 (v2, v3 등)
- PRD 요약 (1-page Executive Summary)

## 사용 예시

```bash
/create-prd "Order Management"
/create-prd "User Authentication"
/create-prd "Product Catalog"
```

## 출력 형식

```markdown
✅ PRD 생성 완료!

**파일**: docs/prd/{feature-name-kebab-case}.md
**Layer**: Domain, Application, Persistence, REST API
**Zero-Tolerance 규칙**: 자동 체크 완료

**다음 단계**:
1. PRD 검토 및 수정
2. `/jira-from-prd docs/prd/{feature-name-kebab-case}.md`
3. `/kentback-plan docs/prd/{feature-name-kebab-case}.md` (선택)
```
