---
description: PRD 문서에서 Layer별 Jira 티켓 자동 생성
tags: [project]
---

# Jira from PRD - Automated Jira Ticket Creation from PRD

당신은 PRD 문서를 파싱하여 Layer별 Jira 티켓을 자동으로 생성하는 작업을 수행합니다.

## 목적

PRD 문서의 구조화된 요구사항을 기반으로, Layer별 태그가 포함된 Jira 티켓을 자동 생성합니다.

## 입력 형식

사용자는 PRD 파일 경로를 제공합니다:
```bash
/jira-from-prd docs/prd/order-management.md
```

## 실행 단계

### 1. PRD 파일 읽기

**Read 도구 사용**:
```
Read 도구:
- file_path: docs/prd/{feature-name-kebab-case}.md
```

### 2. PRD 파싱

PRD 문서에서 다음 섹션을 추출합니다:

#### 2.1 프로젝트 개요 섹션
```markdown
## 📋 프로젝트 개요
- 비즈니스 목적
- 주요 사용자
- 성공 기준
```

#### 2.2 Layer별 요구사항 섹션
```markdown
## 🏗️ Layer별 요구사항

### 1. Domain Layer
- Aggregate 목록
- Value Object 목록
- 비즈니스 규칙

### 2. Application Layer
- Command UseCase 목록
- Query UseCase 목록
- Transaction 경계

### 3. Persistence Layer
- JPA Entity 목록
- Repository 목록
- QueryDSL 쿼리

### 4. REST API Layer
- API 엔드포인트 목록
- Request/Response DTO
```

#### 2.3 개발 계획 섹션
```markdown
## 🚀 개발 계획
- Phase 1: Domain Layer
- Phase 2: Application Layer
- Phase 3: Persistence Layer
- Phase 4: REST API Layer
```

### 3. Cloud ID 확인

Jira Cloud ID를 가져옵니다:
```
mcp__atlassian__getAccessibleAtlassianResources 도구 사용
```

### 4. Jira 티켓 구조 설계

PRD를 기반으로 다음과 같은 Jira 티켓 구조를 생성합니다:

```
Epic: {Feature Name}
  ├─ Story: Domain Layer Implementation
  │   ├─ Task: Aggregate 구현 ({Aggregate1})
  │   ├─ Task: Value Object 구현 ({VO1})
  │   └─ Task: Domain Unit Test
  │
  ├─ Story: Application Layer Implementation
  │   ├─ Task: UseCase 구현 ({UseCase1})
  │   ├─ Task: Command/Query DTO 구현
  │   └─ Task: Application Unit Test
  │
  ├─ Story: Persistence Layer Implementation
  │   ├─ Task: JPA Entity 구현 ({Entity1})
  │   ├─ Task: Repository 구현
  │   └─ Task: QueryDSL 쿼리 구현
  │
  └─ Story: REST API Layer Implementation
      ├─ Task: Controller 구현
      ├─ Task: Request/Response DTO 구현
      └─ Task: Integration Test
```

### 5. Epic 생성

**MCP 도구 사용**:
```
mcp__atlassian__createJiraIssue:
- cloudId: {Cloud ID}
- project: {PROJECT_KEY}
- issueType: "Epic"
- summary: "{Feature Name}"
- description: |
    ## 프로젝트 개요
    {PRD의 프로젝트 개요 섹션}

    ## 개발 계획
    {PRD의 개발 계획 섹션}

    ## 참고 문서
    - PRD: docs/prd/{feature-name-kebab-case}.md
- labels: ["prd-based", "layer-architecture"]
```

### 6. Layer별 Story 생성

각 Layer에 대한 Story를 생성합니다.

#### 6.1 Domain Layer Story

```
mcp__atlassian__createJiraIssue:
- cloudId: {Cloud ID}
- project: {PROJECT_KEY}
- issueType: "Story"
- summary: "Domain Layer Implementation - {Feature Name}"
- description: |
    ## Domain Layer 요구사항
    {PRD의 Domain Layer 섹션}

    ## Aggregate 목록
    {Aggregate1, Aggregate2, ...}

    ## Zero-Tolerance 규칙
    - ✅ Law of Demeter
    - ✅ Lombok 금지
    - ✅ Long FK 전략
- parent: {Epic ID}
- labels: ["domain", "layer-architecture"]
```

#### 6.2 Application Layer Story

```
mcp__atlassian__createJiraIssue:
- cloudId: {Cloud ID}
- project: {PROJECT_KEY}
- issueType: "Story"
- summary: "Application Layer Implementation - {Feature Name}"
- description: |
    ## Application Layer 요구사항
    {PRD의 Application Layer 섹션}

    ## UseCase 목록
    {UseCase1, UseCase2, ...}

    ## Zero-Tolerance 규칙
    - ✅ Command/Query 분리
    - ✅ Transaction 경계 엄격 관리
- parent: {Epic ID}
- labels: ["application", "layer-architecture"]
```

#### 6.3 Persistence Layer Story

```
mcp__atlassian__createJiraIssue:
- cloudId: {Cloud ID}
- project: {PROJECT_KEY}
- issueType: "Story"
- summary: "Persistence Layer Implementation - {Feature Name}"
- description: |
    ## Persistence Layer 요구사항
    {PRD의 Persistence Layer 섹션}

    ## Entity 목록
    {Entity1, Entity2, ...}

    ## Zero-Tolerance 규칙
    - ✅ Long FK 전략
    - ✅ QueryDSL 최적화
- parent: {Epic ID}
- labels: ["persistence", "layer-architecture"]
```

#### 6.4 REST API Layer Story

```
mcp__atlassian__createJiraIssue:
- cloudId: {Cloud ID}
- project: {PROJECT_KEY}
- issueType: "Story"
- summary: "REST API Layer Implementation - {Feature Name}"
- description: |
    ## REST API Layer 요구사항
    {PRD의 REST API Layer 섹션}

    ## API 엔드포인트
    {Endpoint1, Endpoint2, ...}

    ## Zero-Tolerance 규칙
    - ✅ RESTful 설계
    - ✅ 일관된 Error Response
- parent: {Epic ID}
- labels: ["adapter-rest", "layer-architecture"]
```

### 7. Task 생성

각 Story에 대한 세부 Task를 생성합니다.

#### 7.1 Domain Layer Tasks

```
# Aggregate 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "Aggregate 구현: {Aggregate1}"
- description: |
    ## 구현 내용
    - Aggregate Root: {Aggregate1}
    - 필드: {field1, field2, ...}
    - 비즈니스 규칙: {rule1, rule2, ...}

    ## Zero-Tolerance 체크리스트
    - [ ] Law of Demeter 준수
    - [ ] Lombok 미사용
    - [ ] Long FK 전략 적용

    ## kentback TDD
    - RED: Law of Demeter 테스트 작성
    - GREEN: Aggregate 구현
    - REFACTOR: Record 패턴 적용 (선택)
- parent: {Domain Story ID}
- labels: ["domain", "aggregate", "tdd"]

# Value Object 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "Value Object 구현: {VO1}"
- parent: {Domain Story ID}
- labels: ["domain", "value-object", "tdd"]

# Domain Unit Test Task
mcp__atlassian__createJiraIssue:
- summary: "Domain Unit Test 작성"
- parent: {Domain Story ID}
- labels: ["domain", "unit-test", "tdd"]
```

#### 7.2 Application Layer Tasks

```
# UseCase 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "UseCase 구현: {UseCase1}"
- description: |
    ## 구현 내용
    - Command: {CommandDTO}
    - Output: {ResponseDTO}
    - Transaction: Yes/No

    ## Zero-Tolerance 체크리스트
    - [ ] Command/Query 분리
    - [ ] Transaction 경계 확인
    - [ ] 외부 API 호출 위치 확인

    ## kentback TDD
    - RED: Transaction 경계 테스트
    - GREEN: UseCase 구현
    - REFACTOR: Facade 패턴 적용 (선택)
- parent: {Application Story ID}
- labels: ["application", "usecase", "tdd"]
```

#### 7.3 Persistence Layer Tasks

```
# JPA Entity 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "JPA Entity 구현: {Entity1}"
- description: |
    ## 구현 내용
    - 테이블: {table_name}
    - 필드: {field1, field2, ...}
    - 인덱스: {index1, index2, ...}

    ## Zero-Tolerance 체크리스트
    - [ ] Long FK 전략 (관계 어노테이션 금지)
    - [ ] QueryDSL 최적화
- parent: {Persistence Story ID}
- labels: ["persistence", "jpa-entity"]

# Repository 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "Repository 구현: {Repository1}"
- parent: {Persistence Story ID}
- labels: ["persistence", "repository"]
```

#### 7.4 REST API Layer Tasks

```
# Controller 구현 Task
mcp__atlassian__createJiraIssue:
- summary: "Controller 구현: {Resource}Controller"
- description: |
    ## 구현 내용
    - Endpoints: {GET, POST, PUT, DELETE}
    - Request DTO: {RequestDTO}
    - Response DTO: {ResponseDTO}

    ## Zero-Tolerance 체크리스트
    - [ ] RESTful 설계 원칙
    - [ ] 일관된 Error Response
- parent: {REST API Story ID}
- labels: ["adapter-rest", "controller"]
```

### 8. 출력 형식

```markdown
✅ Jira 티켓 생성 완료!

**Epic**: {Epic Key} - {Feature Name}
  **Story**: {Story1 Key} - Domain Layer Implementation
    **Task**: {Task1-1 Key} - Aggregate 구현: {Aggregate1}
    **Task**: {Task1-2 Key} - Value Object 구현: {VO1}
    **Task**: {Task1-3 Key} - Domain Unit Test 작성

  **Story**: {Story2 Key} - Application Layer Implementation
    **Task**: {Task2-1 Key} - UseCase 구현: {UseCase1}
    **Task**: {Task2-2 Key} - Command/Query DTO 구현
    **Task**: {Task2-3 Key} - Application Unit Test 작성

  **Story**: {Story3 Key} - Persistence Layer Implementation
    **Task**: {Task3-1 Key} - JPA Entity 구현: {Entity1}
    **Task**: {Task3-2 Key} - Repository 구현
    **Task**: {Task3-3 Key} - QueryDSL 쿼리 구현

  **Story**: {Story4 Key} - REST API Layer Implementation
    **Task**: {Task4-1 Key} - Controller 구현
    **Task**: {Task4-2 Key} - Request/Response DTO 구현
    **Task**: {Task4-3 Key} - Integration Test 작성

**총 티켓 수**: {총 개수}개 (Epic 1 + Story 4 + Task {N}개)

**다음 단계**:
1. Jira에서 티켓 확인 및 조정
2. Sprint에 티켓 할당
3. `/jira-task {Task Key}` - 첫 번째 Task부터 시작
```

## MCP 도구 사용 순서

1. `Read` → PRD 파일 읽기
2. `mcp__atlassian__getAccessibleAtlassianResources` → Cloud ID 획득
3. `mcp__atlassian__createJiraIssue` (Epic) → Epic 생성
4. `mcp__atlassian__createJiraIssue` (Story × 4) → Layer별 Story 생성
5. `mcp__atlassian__createJiraIssue` (Task × N) → 세부 Task 생성

## 에러 처리

- **PRD 파일 없음**: 파일 경로 확인 요청
- **Cloud ID 없음**: Jira 연동 설정 확인
- **티켓 생성 실패**: 권한 또는 프로젝트 설정 확인
- **중복 Epic**: 기존 Epic에 Story 추가 옵션 제공

## Layer 태그 전략

각 티켓에 다음과 같은 Layer 태그를 자동으로 부여합니다:

| Layer | Label | Color (Jira) |
|-------|-------|--------------|
| Domain | `domain` | Blue |
| Application | `application` | Green |
| Persistence | `persistence` | Orange |
| REST API | `adapter-rest` | Purple |

**추가 태그**:
- `prd-based`: PRD에서 자동 생성됨
- `layer-architecture`: 헥사고날 아키텍처
- `tdd`: kentback TDD 사이클 적용
- `zero-tolerance`: Zero-Tolerance 규칙 적용

## 고급 기능

### 1. 우선순위 자동 설정

PRD의 비즈니스 중요도에 따라 우선순위 자동 설정:
- Critical: Epic
- High: Story
- Medium: Task (핵심 구현)
- Low: Task (부가 기능)

### 2. Story Point 자동 추정

Layer별 복잡도 기반 Story Point 추정:
- Domain: Aggregate 개수 × 3
- Application: UseCase 개수 × 2
- Persistence: Entity 개수 × 2
- REST API: Endpoint 개수 × 1

### 3. Sprint 자동 할당

개발 계획(Phase)에 따라 Sprint 자동 할당:
- Phase 1 (Domain) → Sprint 1
- Phase 2 (Application) → Sprint 2
- Phase 3 (Persistence) → Sprint 3
- Phase 4 (REST API) → Sprint 4

## 사용 예시

```bash
/jira-from-prd docs/prd/order-management.md
```

## 주의사항

- PRD 문서의 구조가 `/create-prd` 커맨드로 생성된 형식과 일치해야 함
- Jira 프로젝트 키는 환경 변수 `JIRA_PROJECT_KEY`에서 가져옴
- Epic, Story, Task 생성 순서를 반드시 준수 (부모-자식 관계)
- Layer 태그는 대소문자 구분 (`domain`, `application`, `persistence`, `adapter-rest`)
