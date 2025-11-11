# /create-tdd-plan - TDD Plan 자동 생성

**목적**: Jira Story에서 TDD Plan 문서를 자동 생성하여 Kent Beck TDD Workflow를 즉시 시작할 수 있도록 함

**위치**: `docs/prd/{STORY-KEY}-tdd-plan.md`

**참조**: `docs/prd/AESA-66-tdd-plan.md` (템플릿 예시)

---

## 📋 사용법

### 기본 사용법

```bash
/create-tdd-plan SC-57
```

**입력**: Jira Story Key (예: `SC-57`, `AESA-66`)

**출력**: `docs/prd/SC-57-tdd-plan.md` 파일 생성

### 고급 사용법

```bash
# PRD 파일 직접 지정
/create-tdd-plan SC-57 --prd docs/prd/user-authentication.md

# Layer 강제 지정 (Jira에서 못 가져올 경우)
/create-tdd-plan SC-57 --layer domain

# 기존 파일 덮어쓰기
/create-tdd-plan SC-57 --force
```

---

## 🔄 워크플로우

### 1. Jira Story 정보 수집

```typescript
// Jira API 호출
const storyKey = "SC-57";
const storyData = await fetchJiraStory(storyKey);

// 필요한 정보:
// - Story 제목
// - Epic 링크 (parent)
// - Layer 정보 (Summary에서 추출: "Domain Layer Implementation")
// - 하위 Task 목록
```

### 2. PRD 파일 찾기

**우선순위**:
1. `--prd` 옵션으로 직접 지정
2. Epic Key로 찾기: `docs/prd/{EPIC-KEY}-*.md`
3. Story Summary에서 도메인명 추출: `docs/prd/*{domain}*.md`
4. 사용자에게 PRD 파일 선택 요청

**예시**:
```bash
# Story: SC-57 "Domain Layer - User Authentication"
# Epic: SC-56 "User Authentication"
# PRD 찾기: docs/prd/user-authentication.md
```

### 3. Layer 감지

**Story Summary 패턴 매칭**:
```typescript
const layerPatterns = {
  domain: /domain.*layer/i,
  application: /application.*layer/i,
  persistence: /persistence.*layer/i,
  "adapter-rest": /rest.*api.*layer/i
};

// "Domain Layer Implementation - User Authentication"
// → Layer: "domain"
```

### 4. PRD 요구사항 추출

**Layer별 섹션 파싱**:
```markdown
# PRD 파일에서 추출
## 2. 기술 스펙

### Domain Layer
- **User Aggregate**:
  - userId: Long
  - email: String (unique)
  - password: String (BCrypt)
  ...
```

**추출 로직**:
- `## Domain Layer` 섹션 찾기
- Aggregate, Value Object, Enum 목록 추출
- 비즈니스 규칙 추출

### 5. TDD Plan 템플릿 적용

**템플릿 구조** (`docs/prd/AESA-66-tdd-plan.md` 기반):

```markdown
# kentback TDD Plan: {STORY-KEY}

**Jira Task**: [{STORY-KEY}]({JIRA-URL}) - {STORY-TITLE}
**Epic**: [{EPIC-KEY}]({EPIC-URL}) - {EPIC-TITLE}
**Layer**: {layer}
**생성일**: {YYYY-MM-DD}

---

## 📋 Task 개요

### {Layer} Layer 요구사항

{PRD에서 추출한 요구사항}

---

## 🔴 RED Phase: 실패하는 테스트 작성

### 0. TestFixture 생성 (FIRST STEP) ⭐

**목표**: 테스트 객체 생성 표준화

**TestFixture 구조**:
```
{layer}/src/
├── main/java/
│   └── com/company/template/{layer}/
└── testFixtures/java/
    └── com/company/template/{layer}/fixture/
        ├── {Aggregate}Fixture.java
        ├── {ValueObject}Fixture.java
        ...
```

{Layer별 TestFixture 템플릿 자동 생성}

### 1. Law of Demeter 테스트

{Layer별 주요 규칙 테스트 자동 생성}

### 2-N. 비즈니스 규칙 테스트

{PRD 비즈니스 규칙 → 테스트 케이스 자동 변환}

---

## 🟢 GREEN Phase: 최소 구현으로 테스트 통과

### 1. {Aggregate} 구현

**파일**: `{layer}/src/main/java/com/company/template/{layer}/{domain}/{Aggregate}.java`

**구현 요구사항**:
- ✅ Lombok 금지 (Pure Java)
- ✅ Law of Demeter 준수
- ✅ Tell, Don't Ask 원칙

{PRD 요구사항 기반 구현 가이드 자동 생성}

---

## 🔄 REFACTOR Phase: 코드 개선

### 1. Java 21 Record 패턴 적용

{Value Object → Record 변환 예시}

### 2. Tell, Don't Ask 원칙 강화

{Getter 체이닝 제거 예시}

---

## ✅ Zero-Tolerance 체크리스트

- [ ] Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] Lombok 미사용 (Pure Java/Record)
- [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] Tell, Don't Ask 원칙
- [ ] 비즈니스 규칙 Domain Layer에 구현
- [ ] Value Object는 Record 패턴 사용

---

## 🚀 실행 계획

### 1. 브랜치 생성
```bash
git checkout -b feature/{STORY-KEY}-{domain-name}
```

### 2. RED Phase 실행
```bash
# TestFixture 생성
touch {layer}/src/testFixtures/java/.../fixture/{Aggregate}Fixture.java

# 테스트 작성
touch {layer}/src/test/java/.../{Aggregate}Test.java

# 테스트 실행 (실패 확인)
./gradlew :{layer}:test
```

### 3. GREEN Phase 실행
```bash
# 구현
touch {layer}/src/main/java/.../{Aggregate}.java

# 테스트 실행 (통과 확인)
./gradlew :{layer}:test
```

### 4. REFACTOR Phase 실행
```bash
# Record 패턴 적용
# Tell, Don't Ask 원칙 강화

# 최종 테스트
./gradlew :{layer}:test
```

### 5. 검증
```bash
# ArchUnit 테스트
./gradlew test --tests "*ArchitectureTest"

# Lombok 사용 여부 확인
grep -r "@Data\|@Builder\|@Getter\|@Setter" {layer}/src/main/java/

# Law of Demeter 위반 확인
grep -r "\.get.*()\.get.*(" {layer}/src/main/java/
```

---

**다음 Task**: {하위 Task 중 첫 번째 Task 링크}
```

---

## 🛠️ 구현 로직

### Phase 1: Jira 정보 수집

```typescript
async function fetchJiraStoryInfo(storyKey: string) {
  // 1. Story 기본 정보
  const story = await jiraApi.getIssue(storyKey);

  // 2. Epic 정보
  const epicKey = story.fields.parent?.key;
  const epic = epicKey ? await jiraApi.getIssue(epicKey) : null;

  // 3. 하위 Task 목록
  const tasks = await jiraApi.search({
    jql: `parent = ${storyKey} ORDER BY created ASC`
  });

  return {
    storyKey,
    storyTitle: story.fields.summary,
    epicKey,
    epicTitle: epic?.fields.summary,
    layer: extractLayer(story.fields.summary),
    tasks: tasks.issues.map(t => ({
      key: t.key,
      title: t.fields.summary
    }))
  };
}

function extractLayer(summary: string): string {
  if (/domain.*layer/i.test(summary)) return "domain";
  if (/application.*layer/i.test(summary)) return "application";
  if (/persistence.*layer/i.test(summary)) return "persistence";
  if (/rest.*api.*layer/i.test(summary)) return "adapter-rest";
  return "domain"; // default
}
```

### Phase 2: PRD 파싱

```typescript
async function findPrdFile(epicKey: string, storyTitle: string): Promise<string> {
  const prdDir = "docs/prd/";

  // 1. Epic Key로 찾기
  const epicFiles = await glob(`${prdDir}${epicKey}-*.md`);
  if (epicFiles.length > 0) return epicFiles[0];

  // 2. 도메인명 추출해서 찾기
  const domain = extractDomain(storyTitle); // "User Authentication" → "user-authentication"
  const domainFiles = await glob(`${prdDir}*${domain}*.md`);
  if (domainFiles.length === 1) return domainFiles[0];

  // 3. 사용자에게 선택 요청
  if (domainFiles.length > 1) {
    return await askUser(`여러 PRD 파일을 찾았습니다. 선택해주세요:\n${domainFiles.join('\n')}`);
  }

  throw new Error(`PRD 파일을 찾을 수 없습니다. --prd 옵션으로 직접 지정해주세요.`);
}

async function extractRequirements(prdPath: string, layer: string) {
  const prdContent = await readFile(prdPath);

  // Layer 섹션 찾기
  const layerSection = extractSection(prdContent, `## ${capitalize(layer)} Layer`);

  return {
    aggregates: extractAggregates(layerSection),
    valueObjects: extractValueObjects(layerSection),
    businessRules: extractBusinessRules(layerSection),
    enums: extractEnums(layerSection)
  };
}
```

### Phase 3: 템플릿 생성

```typescript
async function generateTddPlan(jiraInfo, prdRequirements) {
  const template = `# kentback TDD Plan: ${jiraInfo.storyKey}

**Jira Task**: [${jiraInfo.storyKey}](${getJiraUrl(jiraInfo.storyKey)}) - ${jiraInfo.storyTitle}
**Epic**: [${jiraInfo.epicKey}](${getJiraUrl(jiraInfo.epicKey)}) - ${jiraInfo.epicTitle}
**Layer**: ${jiraInfo.layer}
**생성일**: ${formatDate(new Date())}

---

## 📋 Task 개요

### ${capitalize(jiraInfo.layer)} Layer 요구사항

${formatRequirements(prdRequirements)}

---

## 🔴 RED Phase: 실패하는 테스트 작성

### 0. TestFixture 생성 (FIRST STEP) ⭐

${generateTestFixtureSection(jiraInfo.layer, prdRequirements.aggregates)}

${generateTestCases(jiraInfo.layer, prdRequirements)}

---

## 🟢 GREEN Phase: 최소 구현으로 테스트 통과

${generateImplementationGuide(jiraInfo.layer, prdRequirements)}

---

## 🔄 REFACTOR Phase: 코드 개선

${generateRefactorGuide(jiraInfo.layer)}

---

## ✅ Zero-Tolerance 체크리스트

${generateZeroToleranceChecklist(jiraInfo.layer)}

---

## 🚀 실행 계획

${generateExecutionPlan(jiraInfo)}

---

**다음 Task**: ${jiraInfo.tasks[0] ? `[${jiraInfo.tasks[0].key}](${getJiraUrl(jiraInfo.tasks[0].key)}) - ${jiraInfo.tasks[0].title}` : "N/A"}
`;

  return template;
}
```

### Phase 4: 파일 생성

```typescript
async function createTddPlanFile(storyKey: string, content: string, force: boolean) {
  const filePath = `docs/prd/${storyKey}-tdd-plan.md`;

  // 파일 존재 확인
  if (await fileExists(filePath) && !force) {
    const overwrite = await askUser(
      `${filePath} 파일이 이미 존재합니다. 덮어쓰시겠습니까? (y/n)`
    );
    if (overwrite !== 'y') {
      console.log("취소되었습니다. --force 옵션으로 강제 덮어쓰기 가능합니다.");
      return;
    }
  }

  // 파일 생성
  await writeFile(filePath, content);
  console.log(`✅ TDD Plan 생성 완료: ${filePath}`);

  // 다음 단계 안내
  console.log(`\n🚀 다음 단계:\n/kb/go     # TDD 사이클 시작`);
}
```

---

## 🎯 Layer별 템플릿 차이점

### Domain Layer

**TestFixture 중점**:
- `{Aggregate}Fixture.java` 생성
- `{ValueObject}Fixture.java` 생성

**테스트 중점**:
- Law of Demeter 테스트 필수
- Tell, Don't Ask 원칙 테스트
- 비즈니스 규칙 테스트
- Value Object 불변성 테스트

**Zero-Tolerance**:
- Lombok 금지
- Getter 체이닝 금지
- JPA 관계 어노테이션 금지

### Application Layer

**TestFixture 중점**:
- `{Command}ObjectMother.java` 생성
- `{Response}ObjectMother.java` 생성

**테스트 중점**:
- UseCase 단위 테스트
- Transaction 경계 테스트
- Assembler 패턴 테스트
- Facade 패턴 테스트

**Zero-Tolerance**:
- `@Transactional` 내 외부 API 호출 금지
- Private 메서드에 `@Transactional` 금지
- Final 메서드에 `@Transactional` 금지

### Persistence Layer

**TestFixture 중점**:
- `{Entity}Fixture.java` 생성

**테스트 중점**:
- Repository 테스트
- QueryDSL 최적화 테스트
- Long FK 전략 테스트

**Zero-Tolerance**:
- `@ManyToOne`, `@OneToMany` 금지
- Lombok 금지 (Entity)
- Cascade 옵션 금지

### REST API Layer

**TestFixture 중점**:
- `{Request}Fixture.java` 생성
- `{Response}Fixture.java` 생성

**테스트 중점**:
- Controller 통합 테스트
- DTO 검증 테스트
- Exception Handler 테스트

**Zero-Tolerance**:
- Controller에 비즈니스 로직 금지
- UseCase 직접 호출 (Facade 사용 권장)

---

## 📊 예시 출력

### 입력

```bash
/create-tdd-plan AESA-66
```

### Jira 정보 (자동 수집)

```
Story: AESA-66 "Domain Layer Implementation - User Authentication"
Epic: AESA-65 "User Authentication"
Layer: domain
PRD: docs/prd/user-authentication.md
```

### 출력 파일

`docs/prd/AESA-66-tdd-plan.md` (473 lines)

**포함 내용**:
- ✅ Jira 링크 (Story, Epic)
- ✅ Layer 정보
- ✅ PRD 요구사항 (User Aggregate, Email, Password)
- ✅ TestFixture 템플릿 (UserDomainFixture, EmailFixture, PasswordFixture)
- ✅ RED Phase 테스트 케이스 (5개)
- ✅ GREEN Phase 구현 가이드 (4개 파일)
- ✅ REFACTOR Phase 개선 가이드 (2가지)
- ✅ Zero-Tolerance 체크리스트
- ✅ 실행 계획 (브랜치명, 파일 경로)

---

## ⚠️ 에러 처리

### Jira Story를 찾을 수 없음

```bash
❌ Error: Jira Story "SC-999"를 찾을 수 없습니다.

해결 방법:
1. Jira 연동 확인: ATLASSIAN_API_TOKEN 환경 변수 설정
2. Story Key 확인: SC-999가 올바른지 확인
3. 권한 확인: Story에 접근 권한이 있는지 확인
```

### PRD 파일을 찾을 수 없음

```bash
❌ Error: PRD 파일을 찾을 수 없습니다.

해결 방법:
/create-tdd-plan SC-57 --prd docs/prd/user-authentication.md
```

### Layer 감지 실패

```bash
⚠️ Warning: Story Summary에서 Layer를 감지할 수 없습니다.
   기본값 "domain"을 사용합니다.

명시적 지정:
/create-tdd-plan SC-57 --layer application
```

---

## 🔗 관련 명령어

- `/create-prd` - PRD 문서 생성
- `/jira-from-prd` - PRD에서 Jira 티켓 생성
- **`/create-tdd-plan`** - TDD Plan 자동 생성 (현재 문서)
- `/update-tdd-plan` - TDD Plan 수정 (피드백 반영)
- `/kb/go` - TDD 사이클 실행

---

## 🎓 Best Practices

### 1. PRD 먼저 작성

```bash
# ✅ 올바른 순서
/create-prd "User Authentication"
/jira-from-prd docs/prd/user-authentication.md
/create-tdd-plan AESA-66  # Epic의 첫 Story

# ❌ 잘못된 순서
/create-tdd-plan AESA-66  # PRD 없으면 요구사항 추출 불가
```

### 2. Epic 단위가 아닌 Story 단위로 생성

```bash
# ✅ Story별 TDD Plan
/create-tdd-plan AESA-66  # Domain Layer Story
/create-tdd-plan AESA-67  # Application Layer Story
/create-tdd-plan AESA-68  # Persistence Layer Story

# ❌ Epic에 TDD Plan 생성 불가
/create-tdd-plan AESA-65  # Epic은 여러 Story의 부모
```

### 3. Layer별 순서 준수

```bash
# ✅ 의존성 순서
/create-tdd-plan AESA-66  # 1. Domain (의존성 없음)
/create-tdd-plan AESA-68  # 2. Persistence (Domain 의존)
/create-tdd-plan AESA-67  # 3. Application (Domain + Persistence 의존)
/create-tdd-plan AESA-69  # 4. REST API (Application 의존)
```

### 4. 피드백 반영

```bash
# TDD Plan 생성 후 검토
/create-tdd-plan AESA-66

# 수정 필요 시
/update-tdd-plan AESA-66 "Email 형식 검증에 국제 도메인 추가"
```

---

## 💡 Tips

### Tip 1: PRD 요구사항 상세히 작성

**Good PRD**:
```markdown
## Domain Layer
- **User Aggregate**:
  - userId: Long (PK)
  - email: String (unique, RFC 5322)
  - password: String (BCrypt, 최소 8자, 영문+숫자+특수문자)
  - loginFailCount: Integer (5회 초과 시 계정 잠금)
```

**Result**: 자동으로 상세한 테스트 케이스 생성

**Bad PRD**:
```markdown
## Domain Layer
- User
```

**Result**: 일반적인 테스트만 생성, 수동 수정 필요

### Tip 2: Jira Summary에 Layer 명시

**Good Summary**:
```
"Domain Layer Implementation - User Authentication"
```

**Result**: Layer 자동 감지 (`domain`)

**Bad Summary**:
```
"User Authentication"
```

**Result**: Layer 감지 실패, `--layer` 옵션 필요

### Tip 3: TestFixture 먼저 생성

TDD Plan이 제안하는 순서:
1. **0. TestFixture 생성** ⭐ (FIRST STEP)
2. 1. Law of Demeter 테스트
3. 2-N. 비즈니스 규칙 테스트

TestFixture 없이 테스트 작성하면 중복 코드 발생!

---

## 🚀 다음 단계

TDD Plan 생성 후:

```bash
# 1. TDD Plan 검토
cat docs/prd/AESA-66-tdd-plan.md

# 2. 수정 필요 시
/update-tdd-plan AESA-66 "TestFixture에 createWithRole() 메서드 추가"

# 3. TDD 사이클 시작
/kb/go
```
