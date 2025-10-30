#!/usr/bin/env bash
# Unit test execution script (SSOT)
# Runs unit tests with optional impacted-only mode

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

set +e  # Don't exit on error immediately

start=$(date +%s)
log "Starting unit test execution..."

# Navigate to project root
cd "${PROJECT_ROOT}" || exit 1

# Parse arguments
IMPACTED_ONLY=false
PARALLEL=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --impacted)
            IMPACTED_ONLY=true
            shift
            ;;
        --no-parallel)
            PARALLEL=false
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            echo "Usage: $0 [--impacted] [--no-parallel]"
            exit 1
            ;;
    esac
done

# Build test command
test_cmd=("./gradlew" "${GRADLE_COMMON_OPTS[@]}" "test")

if [[ "$PARALLEL" == "true" ]]; then
    test_cmd+=("--parallel")
    log "Running tests in parallel mode"
else
    log "Running tests in sequential mode"
fi

# Impacted-only mode (analyze git diff for changed files)
if [[ "$IMPACTED_ONLY" == "true" ]]; then
    log "Running impacted tests only..."

    # Get changed files since last commit or main branch
    changed_files=$(git diff --name-only HEAD~1 2>/dev/null || git diff --name-only origin/main 2>/dev/null || echo "")

    if [[ -z "$changed_files" ]]; then
        log_warning "No changed files detected. Running all tests."
    else
        log "Changed files detected:"
        echo "$changed_files" | head -10
        echo ""

        # Extract packages from changed files
        # This is a simplified implementation - real one would be more sophisticated
        changed_packages=$(echo "$changed_files" | \
            grep "\.java$" | \
            sed 's|/src/main/java/||' | \
            sed 's|/src/test/java/||' | \
            sed 's|\.java$||' | \
            sed 's|/|.|g' | \
            cut -d'.' -f1-3 | \
            sort -u)

        if [[ -n "$changed_packages" ]]; then
            log "Impacted packages: $(echo "$changed_packages" | tr '\n' ' ')"
            # Add package filters to test command
            # Note: Actual implementation depends on your test framework
        fi
    fi
fi

# Run tests
log "Executing: ${test_cmd[*]}"
"${test_cmd[@]}"
test_status=$?

# Check test results
if [[ $test_status -eq 0 ]]; then
    log_success "✅ All unit tests passed!"

    # Check if test report exists
    if [[ -f "build/reports/tests/test/index.html" ]]; then
        log "Test report: build/reports/tests/test/index.html"
    fi
else
    log_error "❌ Unit tests failed!"
    report_failure "test_unit" "Unit tests failed. Check build/reports/tests/test/index.html for details."

    # Show failed test summary if available
    if [[ -f "build/test-results/test/TEST-*.xml" ]]; then
        log "Failed tests summary:"
        grep -h "testcase.*FAILED" build/test-results/test/TEST-*.xml 2>/dev/null || true
    fi
fi

# Calculate duration
end=$(date +%s)
duration=$((end - start))

# Record metrics
metric test_unit "${test_status}" "${duration}"

exit $test_status
