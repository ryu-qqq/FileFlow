# TDD Refactor - Improve Code Structure

You are in the REFACTOR phase of Kent Beck's TDD cycle.

## Prerequisites

⚠️ **CRITICAL**: Only refactor when ALL tests are passing (GREEN phase)

## Instructions

1. **Verify all tests pass** before starting
2. **Identify code smells**:
   - Duplication
   - Long methods
   - Unclear names
   - Complex conditionals
   - Hidden dependencies
3. **Make ONE refactoring change at a time**
4. **Run all tests** after EACH change
5. **Stop immediately** if any test fails

## Core Principles

- Refactor ONLY when tests are passing
- Make ONE refactoring change at a time
- Run tests after EACH change
- Use established refactoring patterns
- Prioritize removing duplication
- Improve clarity through naming
- Make dependencies explicit

## Common Refactorings

- **Extract Method**: Pull out complex logic
- **Rename**: Improve variable/method names
- **Extract Variable**: Clarify complex expressions
- **Remove Duplication**: DRY principle
- **Simplify Conditionals**: Guard clauses, early returns
- **Extract Class**: Separate responsibilities

## Success Criteria

- ✅ All tests passing before refactoring
- ✅ All tests passing after refactoring
- ✅ Code is more readable and maintainable
- ✅ No behavioral changes introduced
- ✅ Duplication reduced or eliminated

## What NOT to Do

- ❌ Don't refactor with failing tests
- ❌ Don't change behavior while refactoring
- ❌ Don't make multiple refactorings at once
- ❌ Don't skip running tests between changes
- ❌ Don't add new features during refactoring

This is Kent Beck's TDD: Clean the code while keeping tests green.