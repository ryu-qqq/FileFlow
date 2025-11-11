# /update-tdd-plan - TDD Plan 피드백 반영

**목적**: 생성된 TDD Plan에 사용자 피드백을 반영하여 테스트 케이스, 구현 가이드 등을 수정

**위치**: `docs/prd/{STORY-KEY}-tdd-plan.md` (기존 파일 수정)

---

## 📋 사용법

### 기본 사용법

```bash
/update-tdd-plan AESA-66 "Email 형식 검증에 국제 도메인(.co.kr, .org 등) 테스트 추가"
```

**입력**:
- `AESA-66`: Story Key (TDD Plan 파일 식별)
- `"피드백 내용"`: 수정 또는 추가하고 싶은 내용

**출력**: `docs/prd/AESA-66-tdd-plan.md` 업데이트

### 대화형 사용법

```bash
/update-tdd-plan AESA-66
```

**프롬프트**:
```
📝 TDD Plan 수정 피드백을 입력하세요:
> (사용자 입력 대기)
```

### 섹션별 수정

```bash
# TestFixture 메서드 추가
/update-tdd-plan AESA-66 "UserDomainFixture에 createAdmin() 메서드 추가"

# 테스트 케이스 추가
/update-tdd-plan AESA-66 "로그인 실패 카운트 테스트에 경계값 테스트 추가 (4회, 5회, 6회)"

# 구현 가이드 수정
/update-tdd-plan AESA-66 "UserDomain에 Builder 패턴 적용 예시 추가"

# 비즈니스 규칙 추가
/update-tdd-plan AESA-66 "계정 잠금 해제 시 관리자 권한 체크 로직 추가"
```

---

## 🔄 워크플로우

### 1. TDD Plan 파일 찾기

```typescript
async function findTddPlanFile(storyKey: string): Promise<string> {
  const filePath = `docs/prd/${storyKey}-tdd-plan.md`;

  if (!(await fileExists(filePath))) {
    throw new Error(
      `TDD Plan 파일을 찾을 수 없습니다: ${filePath}\n` +
      `먼저 /create-tdd-plan ${storyKey} 명령어로 생성해주세요.`
    );
  }

  return filePath;
}
```

### 2. 기존 TDD Plan 읽기

```typescript
async function readTddPlan(filePath: string) {
  const content = await readFile(filePath);

  // 섹션별 파싱
  return {
    header: extractSection(content, /^# kentback TDD Plan:.*$/m, /^---$/m),
    taskOverview: extractSection(content, /^## 📋 Task 개요$/m, /^---$/m),
    redPhase: extractSection(content, /^## 🔴 RED Phase:.*$/m, /^---$/m),
    greenPhase: extractSection(content, /^## 🟢 GREEN Phase:.*$/m, /^---$/m),
    refactorPhase: extractSection(content, /^## 🔄 REFACTOR Phase:.*$/m, /^---$/m),
    zeroTolerance: extractSection(content, /^## ✅ Zero-Tolerance 체크리스트$/m, /^---$/m),
    executionPlan: extractSection(content, /^## 🚀 실행 계획$/m, /^\*\*다음 Task\*\*:/m),
    nextTask: extractSection(content, /^\*\*다음 Task\*\*:.*$/m, null)
  };
}
```

### 3. 피드백 분석

```typescript
async function analyzeFeedback(feedback: string, currentPlan: TddPlan) {
  // LLM으로 피드백 의도 파악
  const analysis = await llm.analyze(`
사용자 피드백: "${feedback}"

현재 TDD Plan 구조:
- RED Phase: TestFixture + 테스트 케이스들
- GREEN Phase: 구현 가이드
- REFACTOR Phase: 개선 가이드

어떤 섹션을 어떻게 수정해야 하는지 분석해주세요.

출력 형식:
{
  "target_section": "red_phase" | "green_phase" | "refactor_phase" | "test_fixture" | "business_rules",
  "action": "add" | "modify" | "remove",
  "content": "추가/수정할 내용"
}
`);

  return analysis;
}
```

### 4. 섹션별 업데이트

#### TestFixture 메서드 추가

**피드백**: `"UserDomainFixture에 createAdmin() 메서드 추가"`

**분석 결과**:
```json
{
  "target_section": "test_fixture",
  "action": "add",
  "subsection": "UserDomainFixture",
  "method_name": "createAdmin",
  "content": "관리자 권한을 가진 User 객체 생성"
}
```

**업데이트 로직**:
```typescript
async function addTestFixtureMethod(plan: TddPlan, analysis: Analysis) {
  const fixtureSection = plan.redPhase.sections.find(
    s => s.title === "0. TestFixture 생성"
  );

  // 기존 UserDomainFixture.java 코드 찾기
  const fixtureCodeBlock = fixtureSection.codeBlocks.find(
    cb => cb.language === "java" && cb.content.includes("UserDomainFixture")
  );

  // createAdmin() 메서드 추가
  const newMethod = `
    public static UserDomain createAdmin() {
        return UserDomain.create(
            DEFAULT_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_PASSWORD,
            DEFAULT_NAME,
            UserRole.ADMIN  // ✅ Admin 권한
        );
    }
`;

  // private 생성자 바로 위에 삽입
  const updatedCode = fixtureCodeBlock.content.replace(
    /(\n\s+private UserDomainFixture\(\))/,
    newMethod + "$1"
  );

  fixtureCodeBlock.content = updatedCode;
}
```

**결과**:
```java
public class UserDomainFixture {
    // ... 기존 메서드들 ...

    public static UserDomain createAdmin() {  // ✅ 추가됨
        return UserDomain.create(
            DEFAULT_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_PASSWORD,
            DEFAULT_NAME,
            UserRole.ADMIN
        );
    }

    private UserDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

#### 테스트 케이스 추가

**피드백**: `"로그인 실패 카운트 테스트에 경계값 테스트 추가 (4회, 5회, 6회)"`

**분석 결과**:
```json
{
  "target_section": "red_phase",
  "action": "add",
  "subsection": "4. 로그인 실패 카운트 테스트",
  "content": "경계값 테스트 케이스 추가"
}
```

**업데이트 로직**:
```typescript
async function addTestCase(plan: TddPlan, analysis: Analysis) {
  const testSection = plan.redPhase.sections.find(
    s => s.title === "4. 로그인 실패 카운트 테스트"
  );

  // 새 테스트 케이스 생성
  const newTestCase = `
### 경계값 테스트 (추가됨)

**목표**: 로그인 실패 카운트 경계값 검증

**테스트 케이스**:
\`\`\`java
// UserDomainTest.java
@Test
void shouldNotLockAccountAfterFourFailedAttempts() {
    // Given - Use Fixture
    UserDomain user = UserDomainFixture.create();

    // When - 4회 실패 (경계값)
    for (int i = 0; i < 4; i++) {
        user.recordLoginFailure();
    }

    // Then - 잠금 안 됨
    assertThat(user.isAccountLocked()).isFalse();
    assertThat(user.getLoginFailCount()).isEqualTo(4);
}

@Test
void shouldLockAccountExactlyAtFifthFailedAttempt() {
    // Given - Use Fixture
    UserDomain user = UserDomainFixture.create();

    // When - 5회 실패 (경계값)
    for (int i = 0; i < 5; i++) {
        user.recordLoginFailure();
    }

    // Then - 정확히 5회에 잠김
    assertThat(user.isAccountLocked()).isTrue();
    assertThat(user.getLoginFailCount()).isEqualTo(5);
}

@Test
void shouldRemainLockedAfterSixthFailedAttempt() {
    // Given - Use Fixture
    UserDomain user = UserDomainFixture.create();

    // When - 6회 실패 (경계값 초과)
    for (int i = 0; i < 6; i++) {
        user.recordLoginFailure();
    }

    // Then - 여전히 잠김
    assertThat(user.isAccountLocked()).isTrue();
    assertThat(user.getLoginFailCount()).isEqualTo(6);
}
\`\`\`
`;

  // 기존 테스트 케이스 뒤에 추가
  testSection.content += newTestCase;
}
```

#### 구현 가이드 수정

**피드백**: `"UserDomain에 Builder 패턴 적용 예시 추가"`

**분석 결과**:
```json
{
  "target_section": "green_phase",
  "action": "add",
  "subsection": "1. User Domain Aggregate 구현",
  "content": "Builder 패턴 예시 추가"
}
```

**업데이트 로직**:
```typescript
async function addImplementationExample(plan: TddPlan, analysis: Analysis) {
  const implSection = plan.greenPhase.sections.find(
    s => s.title === "1. User Domain Aggregate 구현"
  );

  // Builder 패턴 예시 추가
  const builderExample = `
### Builder 패턴 적용 (추가됨)

**목적**: 복잡한 객체 생성 단순화

**구현**:
\`\`\`java
// UserDomain.java
public static class Builder {
    private Long userId;
    private String email;
    private String encryptedPassword;
    private String name;
    private UserRole role = UserRole.USER;  // 기본값

    public Builder userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Builder email(String email) {
        this.email = email;
        return this;
    }

    public Builder encryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }

    public Builder name(String name) {
        this.name = name;
        return this;
    }

    public Builder role(UserRole role) {
        this.role = role;
        return this;
    }

    public UserDomain build() {
        // Validation
        if (userId == null || email == null || encryptedPassword == null || name == null) {
            throw new IllegalArgumentException("Required fields are missing");
        }

        return new UserDomain(userId, email, encryptedPassword, name, role);
    }
}

public static Builder builder() {
    return new Builder();
}
\`\`\`

**사용 예시**:
\`\`\`java
UserDomain user = UserDomain.builder()
    .userId(1L)
    .email("test@example.com")
    .encryptedPassword("encrypted123")
    .name("John Doe")
    .role(UserRole.ADMIN)
    .build();
\`\`\`

⚠️ **주의**: Lombok `@Builder` 사용 금지! (Zero-Tolerance 규칙)
`;

  // 기존 구현 예시 뒤에 추가
  implSection.content += builderExample;
}
```

#### 비즈니스 규칙 추가

**피드백**: `"계정 잠금 해제 시 관리자 권한 체크 로직 추가"`

**분석 결과**:
```json
{
  "target_section": "green_phase",
  "action": "modify",
  "subsection": "unlockAccount 메서드",
  "content": "관리자 권한 체크 로직 추가"
}
```

**업데이트 로직**:
```typescript
async function modifyBusinessLogic(plan: TddPlan, analysis: Analysis) {
  const implSection = plan.greenPhase.sections.find(
    s => s.title === "1. User Domain Aggregate 구현"
  );

  // unlockAccount 메서드 찾아서 수정
  const updatedCode = implSection.codeBlocks[0].content.replace(
    /public void unlockAccount\(\) \{[^}]+\}/s,
    `public void unlockAccount(UserDomain admin) {
        // ✅ 관리자 권한 체크 (추가됨)
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only administrators can unlock accounts");
        }

        this.accountLocked = false;
        this.loginFailCount = 0;
    }`
  );

  implSection.codeBlocks[0].content = updatedCode;

  // RED Phase에 테스트 케이스도 추가
  const redSection = plan.redPhase.sections.find(
    s => s.title === "4. 로그인 실패 카운트 테스트"
  );

  const newTest = `
### 관리자 권한 체크 테스트 (추가됨)

\`\`\`java
@Test
void shouldThrowExceptionWhenNonAdminTriesToUnlock() {
    // Given - Use Fixture
    UserDomain lockedUser = UserDomainFixture.createLockedAccount();
    UserDomain regularUser = UserDomainFixture.create();  // USER 권한

    // When & Then
    assertThrows(
        UnauthorizedException.class,
        () -> lockedUser.unlockAccount(regularUser)
    );
}

@Test
void shouldAllowAdminToUnlock() {
    // Given - Use Fixture
    UserDomain lockedUser = UserDomainFixture.createLockedAccount();
    UserDomain admin = UserDomainFixture.createAdmin();  // ADMIN 권한

    // When
    assertDoesNotThrow(() -> lockedUser.unlockAccount(admin));

    // Then
    assertThat(lockedUser.isAccountLocked()).isFalse();
}
\`\`\`
`;

  redSection.content += newTest;
}
```

### 5. 파일 저장

```typescript
async function saveTddPlan(filePath: string, updatedPlan: TddPlan) {
  // 섹션들을 다시 조합
  const content = `${updatedPlan.header}

---

${updatedPlan.taskOverview}

---

${updatedPlan.redPhase}

---

${updatedPlan.greenPhase}

---

${updatedPlan.refactorPhase}

---

${updatedPlan.zeroTolerance}

---

${updatedPlan.executionPlan}

---

${updatedPlan.nextTask}
`;

  await writeFile(filePath, content);
  console.log(`✅ TDD Plan 업데이트 완료: ${filePath}`);

  // 변경 사항 요약
  console.log(`\n📝 변경 사항:`);
  console.log(`- RED Phase 테스트 케이스: ${redPhaseChanges}개 추가`);
  console.log(`- GREEN Phase 구현 가이드: ${greenPhaseChanges}개 수정`);
  console.log(`- TestFixture 메서드: ${fixtureMethodChanges}개 추가`);
}
```

---

## 📊 피드백 패턴별 처리

### 패턴 1: TestFixture 메서드 추가

**피드백 예시**:
- "UserDomainFixture에 createWithEmail() 메서드 추가"
- "createLockedAccount() 메서드 추가"
- "createVipUser() 추가"

**처리**:
1. TestFixture 섹션 찾기
2. 해당 Fixture 클래스 찾기
3. 새 메서드 생성 (파라미터, 반환 타입 추론)
4. private 생성자 위에 삽입

### 패턴 2: 테스트 케이스 추가

**피드백 예시**:
- "Email 국제 도메인 테스트 추가"
- "경계값 테스트 추가 (4회, 5회, 6회)"
- "null 입력 테스트 추가"

**처리**:
1. 관련 테스트 섹션 찾기 (Email 테스트, 로그인 실패 테스트 등)
2. 새 테스트 케이스 생성
3. TestFixture 사용하도록 자동 작성
4. Given-When-Then 구조 준수

### 패턴 3: 구현 가이드 수정

**피드백 예시**:
- "Builder 패턴 예시 추가"
- "Factory Method 패턴 적용"
- "불변성 강화 예시 추가"

**처리**:
1. GREEN Phase 해당 구현 섹션 찾기
2. 새 구현 패턴 예시 생성
3. Zero-Tolerance 규칙 준수 확인
4. 기존 구현 뒤에 추가

### 패턴 4: 비즈니스 규칙 추가/수정

**피드백 예시**:
- "관리자 권한 체크 로직 추가"
- "이메일 중복 검사 로직 추가"
- "패스워드 만료 정책 추가"

**처리**:
1. RED Phase에 테스트 케이스 추가
2. GREEN Phase에 구현 로직 추가
3. 관련 Domain 메서드 시그니처 업데이트
4. Exception 클래스 추가 (필요 시)

### 패턴 5: REFACTOR Phase 개선

**피드백 예시**:
- "Record 패턴 적용 예시 추가"
- "Stream API 활용 예시 추가"
- "불변 컬렉션 사용 예시 추가"

**처리**:
1. REFACTOR Phase 찾기
2. Before/After 예시 생성
3. Java 21 기능 활용
4. 성능, 가독성 개선 포인트 명시

---

## 🎯 고급 피드백 처리

### 복합 피드백 (여러 섹션 동시 수정)

**피드백**: `"Email 국제 도메인 지원 추가 - 테스트, 구현, Refactor 모두"`

**처리**:
1. **RED Phase**: Email 국제 도메인 테스트 추가
   ```java
   @Test
   void shouldValidateInternationalDomains() {
       assertDoesNotThrow(() -> new Email("user@example.co.kr"));
       assertDoesNotThrow(() -> new Email("user@example.org"));
       assertDoesNotThrow(() -> new Email("user@example.museum"));
   }
   ```

2. **GREEN Phase**: Email 정규식 수정
   ```java
   // Before
   private static final Pattern EMAIL_PATTERN =
       Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

   // After (국제 도메인 지원)
   private static final Pattern EMAIL_PATTERN =
       Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
   ```

3. **REFACTOR Phase**: 정규식 상수화
   ```java
   // Email.java
   public record Email(String value) {
       private static final Pattern EMAIL_PATTERN = buildEmailPattern();

       private static Pattern buildEmailPattern() {
           // RFC 5322 기반 정규식 (국제 도메인 포함)
           return Pattern.compile("...");
       }
   }
   ```

### 대규모 구조 변경

**피드백**: `"User Aggregate를 Person과 Account로 분리"`

**처리**:
1. **경고 표시**:
   ```
   ⚠️ 대규모 구조 변경이 감지되었습니다.
      이 변경은 TDD Plan 전체를 재생성하는 것을 권장합니다.

   옵션:
   1. /create-tdd-plan AESA-66 --force (Plan 재생성)
   2. /update-tdd-plan AESA-66 --manual (수동 가이드 제공)
   3. 계속 진행 (자동 업데이트 시도)

   선택: _
   ```

2. **수동 가이드 제공** (옵션 2 선택 시):
   ```markdown
   ## 🔄 대규모 구조 변경 가이드

   ### 변경 사항
   - User Aggregate → Person Aggregate + Account Aggregate

   ### 수정 필요 섹션
   1. **Task 개요**: Aggregate 구조 재작성
   2. **TestFixture**: PersonFixture + AccountFixture 생성
   3. **RED Phase 테스트**: 두 Aggregate로 분리
   4. **GREEN Phase 구현**: 각각 별도 파일 생성

   ### 권장 작업
   1. /create-tdd-plan AESA-66 --force
   2. PRD 먼저 수정: docs/prd/user-authentication.md
   3. /jira-from-prd로 Task 재생성
   ```

---

## ⚠️ 에러 처리

### TDD Plan 파일이 없음

```bash
❌ Error: TDD Plan 파일을 찾을 수 없습니다: docs/prd/AESA-66-tdd-plan.md

해결 방법:
/create-tdd-plan AESA-66
```

### 피드백이 불명확함

```bash
⚠️ Warning: 피드백 의도를 정확히 파악할 수 없습니다.

입력된 피드백: "테스트 추가"

더 구체적인 피드백 예시:
- "Email 국제 도메인 테스트 추가"
- "UserDomainFixture에 createAdmin() 메서드 추가"
- "로그인 실패 카운트 테스트에 경계값 테스트 추가"

다시 시도: /update-tdd-plan AESA-66 "구체적인 피드백"
```

### 섹션을 찾을 수 없음

```bash
❌ Error: "Builder 패턴" 관련 섹션을 찾을 수 없습니다.

피드백: "Builder 패턴 예시 추가"

가능한 섹션:
- 1. User Domain Aggregate 구현 (GREEN Phase)
- REFACTOR Phase: 코드 개선

명시적 지정:
/update-tdd-plan AESA-66 "GREEN Phase의 User Domain Aggregate 구현에 Builder 패턴 예시 추가"
```

---

## 🔗 관련 명령어

- `/create-prd` - PRD 문서 생성
- `/jira-from-prd` - PRD에서 Jira 티켓 생성
- `/create-tdd-plan` - TDD Plan 자동 생성
- **`/update-tdd-plan`** - TDD Plan 수정 (현재 문서)
- `/kb/go` - TDD 사이클 실행

---

## 🎓 Best Practices

### 1. 구체적인 피드백 제공

**❌ 나쁜 예**:
```bash
/update-tdd-plan AESA-66 "테스트 추가"
```

**✅ 좋은 예**:
```bash
/update-tdd-plan AESA-66 "Email 형식 검증 테스트에 국제 도메인(.co.kr, .org) 케이스 추가"
```

### 2. 섹션 명시

**❌ 모호한 피드백**:
```bash
/update-tdd-plan AESA-66 "Builder 패턴 추가"
```

**✅ 명확한 피드백**:
```bash
/update-tdd-plan AESA-66 "GREEN Phase의 UserDomain 구현에 Builder 패턴 예시 추가"
```

### 3. 대규모 변경은 재생성

**❌ 비효율적**:
```bash
/update-tdd-plan AESA-66 "User를 Person과 Account로 분리"
# → 수많은 섹션 수정 필요
```

**✅ 효율적**:
```bash
# 1. PRD 먼저 수정
vim docs/prd/user-authentication.md

# 2. TDD Plan 재생성
/create-tdd-plan AESA-66 --force
```

### 4. 반복 피드백

TDD Plan은 **반복적으로 개선**하는 문서입니다:

```bash
# 1차 생성
/create-tdd-plan AESA-66

# 2차 개선
/update-tdd-plan AESA-66 "Email 국제 도메인 테스트 추가"

# 3차 개선
/update-tdd-plan AESA-66 "UserDomainFixture에 createAdmin() 메서드 추가"

# 4차 개선
/update-tdd-plan AESA-66 "관리자 권한 체크 로직 추가"
```

---

## 💡 Tips

### Tip 1: 피드백 템플릿 활용

**TestFixture 메서드 추가**:
```
"{FixtureName}에 {methodName}() 메서드 추가"
예: "UserDomainFixture에 createAdmin() 메서드 추가"
```

**테스트 케이스 추가**:
```
"{테스트명}에 {케이스 설명} 테스트 추가"
예: "Email 형식 검증에 국제 도메인 테스트 추가"
```

**구현 예시 추가**:
```
"{구현명}에 {패턴명} 적용 예시 추가"
예: "UserDomain에 Builder 패턴 적용 예시 추가"
```

**비즈니스 규칙 추가**:
```
"{메서드명}에 {규칙 설명} 로직 추가"
예: "unlockAccount에 관리자 권한 체크 로직 추가"
```

### Tip 2: 변경 이력 추적

TDD Plan 파일은 Git으로 관리:

```bash
# 변경 전 커밋
git add docs/prd/AESA-66-tdd-plan.md
git commit -m "docs: TDD Plan 초기 생성"

# 피드백 반영
/update-tdd-plan AESA-66 "Email 국제 도메인 테스트 추가"

# 변경 후 커밋
git add docs/prd/AESA-66-tdd-plan.md
git commit -m "docs: Email 국제 도메인 테스트 추가"

# 이력 확인
git log --oneline docs/prd/AESA-66-tdd-plan.md
```

### Tip 3: 팀과 공유

TDD Plan 피드백은 팀 전체가 참여:

```bash
# Code Review에서 피드백 수집
PR Comment: "로그인 실패 경계값 테스트 추가 필요"

# 피드백 반영
/update-tdd-plan AESA-66 "로그인 실패 카운트 테스트에 경계값 테스트 추가 (4회, 5회, 6회)"

# PR 업데이트
git add docs/prd/AESA-66-tdd-plan.md
git commit -m "docs: Code Review 피드백 반영 - 경계값 테스트 추가"
git push
```

---

## 🚀 다음 단계

피드백 반영 후:

```bash
# 1. 변경사항 확인
git diff docs/prd/AESA-66-tdd-plan.md

# 2. TDD 사이클 재시작
/kb/go

# 3. 추가 피드백 있으면 반복
/update-tdd-plan AESA-66 "다음 피드백..."
```
