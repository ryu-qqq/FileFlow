# Tidy First - Structural Changes Only

You are following Kent Beck's "Tidy First" approach: separate structural changes from behavioral changes.

## Prerequisites

⚠️ **CRITICAL**: Only tidy when ALL tests are passing

## Instructions

1. **Verify all tests pass** before starting
2. **Identify structural improvements** needed:
   - Rename variables, methods, classes for clarity
   - Extract methods for better organization
   - Move code to better locations
   - Reorganize file structure
   - Reorder methods logically
3. **Make structural changes** WITHOUT changing behavior
4. **Run all tests** after EACH change to verify no behavior change
5. **Commit structural changes SEPARATELY** from behavioral changes

## Core Principles

- **NEVER mix** structural and behavioral changes
- **ALWAYS** make structural changes FIRST when both are needed
- **VALIDATE** that tests pass before AND after each structural change
- **COMMIT** structural changes separately from behavioral changes
- Structural changes should be **pure refactoring** (no new features)

## Types of Structural Changes

### Safe Structural Changes:
- **Renaming**: Variables, methods, classes, files
- **Extracting**: Methods, classes, interfaces
- **Moving**: Code to different files or packages
- **Reordering**: Method order within a class
- **Formatting**: Code style, indentation, spacing
- **Organizing**: Imports, dependencies

### NOT Structural (These are behavioral):
- ❌ Adding new methods with logic
- ❌ Changing algorithms or logic
- ❌ Adding features or functionality
- ❌ Fixing bugs (even small ones)
- ❌ Changing return values or parameters

## Success Criteria

- ✅ All tests passing before tidying
- ✅ All tests passing after each tidy operation
- ✅ Code structure improved
- ✅ Zero behavioral changes
- ✅ Ready to commit separately

## Tidy First Workflow

1. **Tidy** (structural changes only) → Commit
2. **Then** make behavioral changes → Commit separately

## What NOT to Do

- ❌ Don't mix structural and behavioral changes in one commit
- ❌ Don't tidy with failing tests
- ❌ Don't skip running tests after tidying
- ❌ Don't change behavior while tidying

This is Kent Beck's "Tidy First": Clean structure, then add behavior.