# Check Tests - Verify All Tests Pass

You are verifying test status according to Kent Beck's TDD practices.

## Instructions

1. **Identify the test framework** being used (JUnit, Gradle, Maven, etc.)
2. **Run all tests** (excluding explicitly long-running tests)
3. **Analyze the results**:
   - Count passed tests
   - Count failed tests
   - Identify any warnings or errors
4. **Report clearly** with visual indicators
5. **Provide recommendations** based on results

## What to Check

- ✅ **Test Execution**: All tests run successfully
- ✅ **Test Results**: All tests pass
- ✅ **Compilation**: No compilation errors
- ✅ **Warnings**: No linter or compiler warnings
- ✅ **Coverage**: Tests exist for new functionality

## Expected Test Commands by Framework

### Spring Boot / Gradle:
```bash
./gradlew test
./gradlew test --tests "*ClassName*"
```

### Maven:
```bash
mvn test
mvn test -Dtest=ClassName
```

### JUnit directly:
```bash
mvn surefire:test
```

## Success Report Format

```
🧪 TEST RESULTS
================
✅ Tests Passed: X/Y
❌ Tests Failed: 0/Y
⚠️  Warnings: None

Status: 🟢 ALL TESTS PASSING - Ready to proceed
```

## Failure Report Format

```
🧪 TEST RESULTS
================
✅ Tests Passed: X/Y
❌ Tests Failed: N/Y
⚠️  Warnings: M

Status: 🔴 TESTS FAILING - Must fix before proceeding

Failed Tests:
1. TestClass.testMethod - Expected X but got Y
2. ...

Recommendations:
- Review failing test expectations
- Check recent code changes
- Debug implementation logic
```

## Core Principles

- Run **ALL** tests, not just the new one
- Exclude only explicitly long-running tests
- Tests must pass **completely** (no partial passes)
- Zero warnings is the standard
- Fast feedback loop is essential

## When to Check Tests

- After writing a test (RED phase)
- After implementing code (GREEN phase)
- After refactoring (REFACTOR phase)
- After tidying (TIDY phase)
- Before committing
- Frequently during development

## What NOT to Do

- ❌ Don't skip tests "because they're slow"
- ❌ Don't ignore warnings
- ❌ Don't proceed with failing tests
- ❌ Don't commit without checking

This is Kent Beck's TDD: Always run all the tests, keep them passing.