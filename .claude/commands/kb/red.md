# TDD Red - Write Failing Test

You are in the RED phase of Kent Beck's TDD cycle.

## Instructions

1. **Understand the requirement** from `docs/prd/{ISSUE-KEY}-tdd-plan.md` or user input
   - Find the TDD plan in docs/prd/ directory (named with Jira issue key)
   - If not found, ask user for requirements
2. **Create TestFixture classes FIRST** (if not exists) - See TestFixture section below
3. **Write the simplest failing test** that defines one small increment of functionality
4. **Use TestFixture in tests** - Use Fixture.create() methods instead of inline object creation
5. **Use meaningful test names** that describe behavior (e.g., "shouldSumTwoPositiveNumbers")
6. **Run the test** and verify it FAILS for the right reason
7. **Report the failure** clearly

## TestFixture Pattern (MANDATORY)

### Why TestFixture?
- **Reusability**: Share test object creation across test classes
- **Maintainability**: Change test data in one place
- **Readability**: Clear intent with `Fixture.create()` methods
- **Consistency**: Standardized test objects across project

### Structure Requirements
```
domain/src/
├── main/java/
│   └── com/company/template/domain/
└── testFixtures/java/
    └── com/company/template/domain/fixture/
        ├── UserDomainFixture.java
        ├── EmailFixture.java
        └── PasswordFixture.java
```

### TestFixture Template
```java
package com.company.template.domain.fixture;

/**
 * TestFixture for UserDomain.
 *
 * <p>Object Mother 패턴으로 테스트 객체를 생성합니다.</p>
 *
 * @author Claude Code
 * @since 2025-11-10
 */
public class UserDomainFixture {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PASSWORD = "Test123!@#";
    private static final String DEFAULT_NAME = "Test User";

    /**
     * 기본 UserDomain 생성.
     */
    public static UserDomain create() {
        return UserDomain.create(
            DEFAULT_USER_ID,
            DEFAULT_EMAIL,
            DEFAULT_PASSWORD,
            DEFAULT_NAME,
            UserRole.USER
        );
    }

    /**
     * 특정 이메일로 UserDomain 생성.
     */
    public static UserDomain createWithEmail(String email) {
        return UserDomain.create(
            DEFAULT_USER_ID,
            email,
            DEFAULT_PASSWORD,
            DEFAULT_NAME,
            UserRole.USER
        );
    }

    /**
     * 관리자 UserDomain 생성.
     */
    public static UserDomain createAdmin() {
        return UserDomain.create(
            DEFAULT_USER_ID,
            "admin@example.com",
            DEFAULT_PASSWORD,
            "Admin User",
            UserRole.ADMIN
        );
    }

    /**
     * Fixture 클래스는 인스턴스화할 수 없습니다.
     */
    private UserDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
```

### Naming Conventions
- **Class Name**: `{Domain}Fixture` suffix (e.g., `UserDomainFixture`)
- **Methods**: `create*()` prefix (e.g., `create()`, `createWithEmail()`, `createAdmin()`)
- **All methods MUST be `static`**
- **Private constructor** to prevent instantiation

### RED Phase Workflow with TestFixture

**Step 1: Create Fixtures FIRST**
```bash
# Create testFixtures directory structure
mkdir -p domain/src/testFixtures/java/com/company/template/domain/fixture/

# Create Fixture classes
touch domain/src/testFixtures/java/.../UserDomainFixture.java
touch domain/src/testFixtures/java/.../EmailFixture.java
```

**Step 2: Write Tests Using Fixtures**
```java
@Test
@DisplayName("로그인 실패 5회 시 계정 잠금")
void shouldLockAccountAfterFiveFailedAttempts() {
    // Given - Use Fixture
    UserDomain user = UserDomainFixture.create();

    // When
    for (int i = 0; i < 5; i++) {
        user.recordLoginFailure();
    }

    // Then
    assertThat(user.isAccountLocked()).isTrue();
}
```

**❌ WRONG (Anti-Pattern)**:
```java
@Test
void testLogin() {
    // ❌ Inline object creation
    UserDomain user = UserDomain.create(1L, "test@example.com", "Pass123!", "Test", UserRole.USER);
    // ... test logic
}

// ❌ Private helper method in test class
private UserDomain createTestUser() {
    return UserDomain.create(...);
}
```

**✅ RIGHT (Fixture Pattern)**:
```java
@Test
@DisplayName("로그인 성공 시 실패 카운트 리셋")
void shouldResetFailCountAfterSuccessfulLogin() {
    // ✅ Use Fixture
    UserDomain user = UserDomainFixture.create();
    user.recordLoginFailure();
    user.recordLoginFailure();

    user.recordLoginSuccess();

    assertThat(user.getLoginFailCount()).isEqualTo(0);
}
```

## Core Principles

- **Fixture First**: Always create Fixture classes before writing tests
- Write the SIMPLEST test that could possibly fail
- Test should fail for the RIGHT reason (not compilation error)
- One assertion per test when possible
- Test name describes the expected behavior
- No implementation code yet - just the test
- **Use Fixture.create()** instead of inline object creation

## Success Criteria

- ✅ TestFixture classes created in `testFixtures/` directory
- ✅ Test written with clear, descriptive name
- ✅ Test uses Fixture.create() methods (NOT inline object creation)
- ✅ Test runs and FAILS
- ✅ Failure message is clear and informative
- ✅ Test defines a small, specific increment of functionality

## What NOT to Do

- ❌ Don't write implementation code yet
- ❌ Don't write multiple tests at once
- ❌ Don't skip running the test to verify failure
- ❌ Don't write tests that pass immediately
- ❌ Don't create objects inline in tests (use Fixture instead)
- ❌ Don't create private helper methods in test classes (use Fixture instead)

## Zero-Tolerance Rules

- **MUST** create TestFixture classes in `testFixtures/` directory
- **MUST** use Fixture.create() methods in tests
- **MUST** follow naming conventions (*Fixture, create*())
- **MUST** have private constructor in Fixture classes

This is Kent Beck's TDD: Start with RED, make the failure explicit, and use TestFixture for maintainability.
