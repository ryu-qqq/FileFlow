# TDD Commit - Disciplined Commit Practice

You are following Kent Beck's commit discipline for TDD.

## Prerequisites - ONLY Commit When:

1. ✅ **ALL tests are passing**
2. ✅ **ALL compiler/linter warnings resolved**
3. ✅ **Change represents a single logical unit of work**
4. ✅ **Clear whether structural OR behavioral change**

## Instructions

1. **Run all tests** (excluding long-running tests) one final time
2. **Verify no warnings** from compiler/linter
3. **Review the changes** to confirm single logical unit
4. **Determine commit type**:
   - **STRUCTURAL**: Refactoring, renaming, reorganizing (no behavior change)
   - **BEHAVIORAL**: New feature, bug fix, algorithm change
5. **Write clear commit message** stating the type
6. **Stage and commit** the changes

## Commit Message Format

### For Behavioral Changes:
```
[BEHAVIORAL] Add multiplication feature

- Implemented multiply() method
- Added test for multiplying two positive numbers
- Test passes with minimal implementation

TDD Cycle: RED → GREEN → REFACTOR complete
```

### For Structural Changes:
```
[STRUCTURAL] Extract calculation logic to separate method

- Extracted add() method for clarity
- Renamed variables for better readability
- No behavioral changes - all tests still pass
```

## Core Principles

- **Small, frequent commits** over large, infrequent ones
- **Never mix** structural and behavioral changes in one commit
- **Always** commit message clearly states change type
- **Never commit** with failing tests or warnings
- Commit represents **one logical unit** of work

## Success Criteria

- ✅ All tests passing
- ✅ No compiler/linter warnings
- ✅ Single logical change
- ✅ Clear commit message with type indicator
- ✅ Structural and behavioral changes separated

## What NOT to Do

- ❌ Don't commit with failing tests
- ❌ Don't commit with unresolved warnings
- ❌ Don't mix structural and behavioral changes
- ❌ Don't commit multiple unrelated changes together
- ❌ Don't commit without clear message

## When to Commit

- After completing RED → GREEN → REFACTOR cycle
- After tidying code (structural changes)
- After each small, working increment
- Before starting a new test or feature

This is Kent Beck's TDD discipline: Small commits, always green, clear intent.