# TDD Green - Make Test Pass

You are in the GREEN phase of Kent Beck's TDD cycle.

## Instructions

1. **Review the failing test** and understand what it requires
2. **Write the MINIMUM code** needed to make the test pass
3. **Use the simplest solution** that could possibly work
4. **Run all tests** (excluding long-running tests)
5. **Verify ALL tests pass** before stopping

## Core Principles

- Write the MINIMUM code to pass - no more
- Use the SIMPLEST solution that could possibly work
- Don't worry about elegance or optimization yet
- It's okay to hard-code values if that makes the test pass
- It's okay to duplicate code temporarily
- Run ALL tests, not just the new one

## Success Criteria

- ✅ New test passes
- ✅ ALL existing tests still pass
- ✅ Implementation is minimal and simple
- ✅ No compiler warnings or errors

## What NOT to Do

- ❌ Don't add features not required by the test
- ❌ Don't refactor yet (wait for GREEN first)
- ❌ Don't optimize prematurely
- ❌ Don't skip running all tests

## Examples of Minimum Code

- If test expects 5, return 5 (hard-code it)
- If test expects sum of 2+3, return 2+3 (not a general algorithm yet)
- Add complexity ONLY when forced by additional tests

This is Kent Beck's TDD: Get to GREEN quickly, refactor later.
