# Design Analysis Command

**Spring DDD 설계 분석 및 Cursor AI 작업지시서 생성**

---

## 🎯 목적

PRD 또는 요구사항을 기반으로:
1. Domain 모델 설계 분석 (Aggregate, Value Object, Domain Event)
2. UseCase 경계 정의 (Command/Query 분리)
3. API 명세 설계 (Request/Response DTO)
4. Cursor AI용 작업지시서 생성 (`.claude/work-orders/`)

---

## 📝 사용법

```bash
# 기본 사용
/design-analysis Order

# PRD 파일 참조
/design-analysis Order --prd docs/prd/order.md

# Jira 티켓 참조
/design-analysis Order --jira PROJ-123
```

---

## 🔄 실행 프로세스

당신은 **Spring DDD 설계 전문가**입니다.

### Step 1: 요구사항 수집

**입력 분석:**
- Aggregate 이름: `{aggregate}`
- PRD 파일 경로 (optional): `--prd` 옵션
- Jira 티켓 (optional): `--jira` 옵션

**질문 (필요시):**
- 핵심 비즈니스 로직은 무엇인가요?
- 어떤 상태 변경이 필요한가요?
- 외부 시스템 연동이 있나요?
- 주요 도메인 이벤트는 무엇인가요?

### Step 2: Domain 모델 설계

**Aggregate Root 분석:**
```
{Aggregate}Domain 설계:
- 식별자: {Aggregate}Id (Value Object)
- 상태: {Aggregate}Status (Enum)
- 불변식 (Invariants): [비즈니스 규칙]
- 비즈니스 메서드: [핵심 행위]
```

**Value Object 식별:**
```
- {Aggregate}Id: 식별자
- Address, Money, Email 등: 도메인 개념
```

**Domain Event 식별:**
```
- {Aggregate}Created
- {Aggregate}StatusChanged
- {Aggregate}Cancelled
```

### Step 3: UseCase 경계 정의

**Command UseCase:**
```
- Create{Aggregate}UseCase (생성)
- Update{Aggregate}UseCase (수정)
- Cancel{Aggregate}UseCase (취소)
```

**Query UseCase:**
```
- Get{Aggregate}UseCase (단건 조회)
- Search{Aggregate}UseCase (목록 조회)
```

**Command/Query DTO:**
```
Command:
- Create{Aggregate}Command
- Update{Aggregate}Command

Query:
- {Aggregate}SearchCondition
- {Aggregate}Response
```

### Step 4: API 명세 설계

**REST Endpoints:**
```
POST   /api/{aggregates}           - Create
GET    /api/{aggregates}/{id}      - Get
PUT    /api/{aggregates}/{id}      - Update
DELETE /api/{aggregates}/{id}      - Cancel
GET    /api/{aggregates}            - Search
```

**Request/Response DTO:**
```
Create{Aggregate}Request → Create{Aggregate}Command
{Aggregate}Response (공통 응답)
```

### Step 5: 작업지시서 생성

**파일 생성: `.claude/work-orders/{aggregate}-aggregate.md`**

작업지시서 구조:
```markdown
# 작업지시서: {Aggregate} Aggregate

## 📋 생성할 파일

### Domain Layer (domain/{aggregate}/)
- `model/{Aggregate}Domain.java` - Aggregate Root
- `model/{Aggregate}Id.java` - Value Object
- `model/{Aggregate}Status.java` - Enum
- `event/{Aggregate}CreatedEvent.java` - Domain Event

### Application Layer (application/{aggregate}/)
- `port/in/Create{Aggregate}Port.java` - Port Interface
- `port/in/Get{Aggregate}Port.java` - Query Port
- `usecase/Create{Aggregate}UseCase.java` - Command UseCase
- `usecase/Get{Aggregate}UseCase.java` - Query UseCase
- `dto/command/Create{Aggregate}Command.java`
- `dto/response/{Aggregate}Response.java`

### REST API Layer (adapter-in/web/{aggregate}/)
- `controller/{Aggregate}Controller.java`
- `dto/request/Create{Aggregate}Request.java`

## ✅ 필수 규칙 (Zero-Tolerance)

- ❌ **Lombok 금지** → Pure Java getter/setter
- ❌ **Getter 체이닝 금지** → Tell, Don't Ask 패턴
- ❌ **JPA 관계 어노테이션 금지** → Long FK 전략
- ❌ **`@Transactional` 내 외부 API 호출 금지**
- ✅ **Javadoc 필수** (모든 public 클래스/메서드에 `@author`, `@since`)

## 🎯 Domain 스켈레톤

[Domain/UseCase/Controller 스켈레톤 코드...]

## 📝 다음 단계

1. **Cursor AI 작업** (Git Worktree)
   - 위 스켈레톤 코드 생성
   - `.cursorrules` 자동 적용
   - `docs/coding_convention/` 참조

2. **Git Commit**
   - Hook 실행 → 변경 파일 추적
   - `.claude/cursor-changes.md` 자동 생성

3. **Claude Code 검증**
   - `/validate-cursor-changes`
   - validation-helper.py 실행
   - ArchUnit 테스트 실행

4. **Claude Code 비즈니스 로직 구현**
   - Domain 비즈니스 메서드 구현
   - UseCase 트랜잭션 경계 관리

5. **Claude Code 테스트 생성**
   - `/generate-fixtures {aggregate}`
   - Domain 테스트 (Happy/Edge/Exception)
```

---

## 🎯 작업지시서 스켈레톤 템플릿

**`.claude/skills/design-analysis.md`의 템플릿을 활용하여 자동 생성:**

1. **Domain 스켈레톤** (Aggregate Root + Factory + Getters)
2. **UseCase 스켈레톤** (Port + Implementation + @Transactional)
3. **Controller 스켈레톤** (REST Endpoints + Validation)

---

## 📦 출력

**생성 파일:**
```
.claude/work-orders/{aggregate}-aggregate.md
```

**확인 메시지:**
```
✅ 작업지시서 생성 완료: .claude/work-orders/{aggregate}-aggregate.md

📋 생성할 파일 (총 14개):
- Domain Layer: 4개
- Application Layer: 6개
- REST API Layer: 4개

📝 다음 단계:
1. Git Worktree 생성: git worktree add ../wt-{aggregate} feature/{aggregate}
2. Cursor AI로 이동하여 작업지시서 참조
3. Boilerplate 생성 후 Claude Code로 검증
```

---

## 🔧 자동 로드 규칙

- **Cache 시스템**: `.claude/cache/rules/` (O(1) 검색)
- **단일 진실 공급원**: Cache만 사용
- **실시간 검증**: validation-helper.py 자동 실행

---

**✅ 이 커맨드는 Claude Code의 설계 분석 역할을 수행합니다!**
