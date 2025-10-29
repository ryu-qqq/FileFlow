# FileFlow Essential Commands

## Build & Run Commands

### Build
```bash
./gradlew clean build
```

### Run Application
```bash
./gradlew :bootstrap:bootstrap-web-api:bootRun
```

### Run Specific Module Tests
```bash
./gradlew :domain:test
./gradlew :application:test
./gradlew :adapter-in-rest-api:test
```

## Quality Verification Commands

### Run All Tests with Coverage
```bash
./gradlew test
```

### Code Quality Check (Checkstyle + SpotBugs)
```bash
./gradlew check
```

### Verify Test Coverage (with enforcement)
```bash
./gradlew jacocoTestCoverageVerification
```

### Generate Coverage Report
```bash
./gradlew jacocoTestReport
# Report location: build/reports/jacoco/test/html/index.html
```

### Dead Code Detection
```bash
./gradlew detectDeadCode
```

### Lombok Detection (should always pass - Lombok is prohibited)
```bash
./gradlew checkNoLombok
```

## Development Workflow Commands

### After Task Completion (Standard Checklist)
Always run these commands before committing:
```bash
# 1. Run tests
./gradlew test

# 2. Check code quality
./gradlew check

# 3. Verify coverage
./gradlew jacocoTestCoverageVerification
```

### Git Pre-commit Hooks
The project has automated pre-commit validation:
- Transaction boundary validation
- Spring proxy constraint checks
- No manual action needed (runs automatically)

## System Utilities (macOS/Darwin)

### File Operations
```bash
# Find files
find . -name "*.java" -type f

# Search in files
grep -r "pattern" --include="*.java" .

# List directory structure
ls -la
tree -L 3  # if tree is installed

# Change directory
cd /path/to/directory
```

### Git Operations
```bash
git status
git add .
git commit -m "message"
git push
git branch
git checkout -b feature/branch-name
```

### Process Management
```bash
# Find process by port
lsof -i :8080

# Kill process
kill -9 <PID>
```

## Important Notes
- All commands assume you're in the project root directory
- Gradle wrapper (./gradlew) should be used instead of system gradle
- On macOS, use `./gradlew` not `gradlew` (Unix-style execution)
