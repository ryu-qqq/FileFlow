# Next Test - Find Next Test from Plan

You are following Kent Beck's incremental TDD approach using plan.md as your guide.

## Instructions

1. **Read plan.md** file from project root
2. **Parse the test list** to find unmarked tests
3. **Identify the next test** to implement:
   - Look for unchecked checkboxes `[ ]`
   - Look for unmarked items
   - Find the first incomplete test in sequence
4. **Display the test description** clearly
5. **Ask for confirmation** before proceeding

## Plan.md Format Examples

The plan file may use various formats:

### Checkbox Format:
```markdown
- [ ] Test addition of two positive numbers
- [x] Test addition of negative numbers
- [ ] Test addition with zero
```

### Numbered Format:
```markdown
1. ✅ Test addition of two positive numbers
2. Test addition of negative numbers
3. Test addition with zero
```

### Simple List Format:
```markdown
- Test addition of two positive numbers (DONE)
- Test addition of negative numbers
- Test addition with zero
```

## Output Format

```
📋 NEXT TEST FROM PLAN
======================

Test #N: [Test Description]

Context:
- Previous tests completed: X
- Total tests remaining: Y
- Category/Feature: [If applicable]

Ready to implement this test?
- Use /red to start the RED phase
- Use /go to execute the full TDD cycle
```

## If No Plan Found

```
⚠️  NO PLAN.MD FOUND

Recommendations:
1. Create plan.md with test cases to implement
2. Use Kent Beck's approach: List tests incrementally
3. Example format:
   - [ ] Test basic case
   - [ ] Test edge case
   - [ ] Test error case

Would you like to create a plan.md file?
```

## Core Principles

- **Follow the plan** - don't skip ahead
- **One test at a time** - complete before moving to next
- **Mark completed tests** - track progress
- **Update plan as needed** - plans can evolve
- **Small increments** - each test is a small step

## Success Criteria

- ✅ Plan.md file located and read
- ✅ Next unmarked test identified
- ✅ Test description displayed clearly
- ✅ Context provided (what's done, what's left)
- ✅ Ready to proceed with implementation

## What NOT to Do

- ❌ Don't skip tests in the plan
- ❌ Don't implement multiple tests at once
- ❌ Don't proceed without marking completed tests
- ❌ Don't ignore the plan sequence

This is Kent Beck's TDD: Follow the plan, one test at a time.
