# Task Completion Checklist for FileFlow

When completing ANY development task, follow this checklist:

## 1. Pre-Commit Verification (ALWAYS)

### Run Tests
```bash
./gradlew test
```
**Must Pass**: All tests green, no failures

### Code Quality Check
```bash
./gradlew check
```
**Validates**:
- Checkstyle (code formatting, Javadoc)
- SpotBugs (potential bugs)
- Lombok prohibition (build fails if Lombok detected)

### Coverage Verification
```bash
./gradlew jacocoTestCoverageVerification
```
**Enforces**:
- Domain: 90% coverage
- Application: 80% coverage
- Adapters: 70% coverage

## 2. Manual Code Review

### Zero-Tolerance Rules Check
- [ ] **NO Lombok**: No `@Data`, `@Builder`, `@Getter`, `@Setter` anywhere
- [ ] **NO Getter Chaining**: No `a.getB().getC()` patterns in domain/application
- [ ] **NO JPA Relationships**: No `@ManyToOne`, `@OneToMany`, etc. (use Long FK)
- [ ] **Transaction Boundaries**: No external API calls inside `@Transactional`
- [ ] **Javadoc Present**: All public classes/methods have Javadoc with `@author`, `@since`
- [ ] **Scope Adherence**: Only implemented what was requested (no extra features)

### Architecture Validation
- [ ] **Dependency Direction**: Domain ← Application ← Adapter (never reversed)
- [ ] **Package Structure**: Follows hexagonal architecture conventions
- [ ] **Pure Domain**: Domain module has no Spring/framework dependencies

## 3. Git Pre-commit Hook (Automatic)

The following validations run automatically on `git commit`:
- Transaction boundary check (fails commit if violations found)
- Spring proxy constraint check (fails if `@Transactional` on private/final)
- Additional project-specific validations

**Location**: `hooks/pre-commit`

## 4. Cache-Based Validation (Optional but Recommended)

### Using Validation Helper
```bash
# Validate specific file against layer rules
python3 .claude/hooks/scripts/validation-helper.py \
  --file <file-path> \
  --layer <domain|application|adapter-rest|adapter-persistence>

# Example
python3 .claude/hooks/scripts/validation-helper.py \
  --file domain/src/main/java/com/ryuqq/fileflow/domain/Order.java \
  --layer domain
```

### Slash Command Validation
```bash
# Validate domain layer
/validate-domain domain/src/main/java/.../Order.java

# Validate entire architecture
/validate-architecture

# Validate specific module
/validate-architecture domain
```

## 5. Build Verification

### Full Build (before pushing)
```bash
./gradlew clean build
```
**Ensures**:
- All modules compile
- All tests pass
- Code quality checks pass
- Coverage requirements met
- No Lombok detected
- ArchUnit architecture rules pass

## 6. Documentation (if applicable)

- [ ] **API Changes**: Update REST API documentation (Swagger/OpenAPI)
- [ ] **Architecture Changes**: Update architecture diagrams if structure changed
- [ ] **README**: Update if new features/commands added

## Quick Reference Card

### Minimal Checklist (Fast Workflow)
```bash
# 1. Test
./gradlew test

# 2. Check
./gradlew check

# 3. Commit (pre-commit hook runs automatically)
git add .
git commit -m "feat: your message"
```

### Full Checklist (Before PR/Push)
```bash
# 1. Full build with all checks
./gradlew clean build

# 2. Manual review of Zero-Tolerance rules
# (Lombok, Getter chaining, JPA relationships, Transactions, Javadoc)

# 3. Commit and push
git add .
git commit -m "feat: your message"
git push
```

## What NOT to Do

❌ **Never skip tests** to make build pass  
❌ **Never disable validation** to make build pass  
❌ **Never commit without running checks**  
❌ **Never add features not explicitly requested**  
❌ **Never use Lombok** (even for "just this one class")  
❌ **Never add JPA relationships** (even if it "seems cleaner")

## When in Doubt

If unsure about any rule or validation:
1. Check `.claude/CLAUDE.md` for full project standards
2. Use `/validate-architecture` to check current state
3. Review `docs/coding_convention/` for specific layer rules
4. Ask for clarification before implementing
