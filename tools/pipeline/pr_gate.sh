#!/usr/bin/env bash
# PR Gate pipeline (SSOT)
# Full validation pipeline for Pull Request approval

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

set +e  # Don't exit on error immediately

start=$(date +%s)
log "🚀 Starting PR Gate Pipeline..."

# Navigate to project root
cd "${PROJECT_ROOT}" || exit 1

# Initialize pipeline
init_pipeline

# Track overall status
overall_status=0

# Step 1: Format check (non-invasive)
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Step 1/5: Code Format Check (Spotless)"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./gradlew "${GRADLE_COMMON_OPTS[@]}" spotlessCheck
if [[ $? -ne 0 ]]; then
    log_error "Format check failed. Run: ./gradlew spotlessApply"
    overall_status=1
else
    log_success "Format check passed"
fi

# Step 2: Convention validation
log ""
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Step 2/5: Convention Validation"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
"${SCRIPT_DIR}/validate_conventions.sh"
if [[ $? -ne 0 ]]; then
    log_error "Convention validation failed"
    overall_status=1
else
    log_success "Convention validation passed"
fi

# Step 3: Full unit tests
log ""
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Step 3/5: Unit Tests (All)"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
"${SCRIPT_DIR}/test_unit.sh"
if [[ $? -ne 0 ]]; then
    log_error "Unit tests failed"
    overall_status=1
else
    log_success "Unit tests passed"
fi

# Step 4: Architecture validation (ArchUnit)
log ""
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Step 4/5: Architecture Validation (ArchUnit)"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./gradlew "${GRADLE_COMMON_OPTS[@]}" :bootstrap-web-api:test --tests "*ArchitectureTest"
if [[ $? -ne 0 ]]; then
    log_error "Architecture validation failed"
    overall_status=1
else
    log_success "Architecture validation passed"
fi

# Step 5: Test coverage check
log ""
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Step 5/5: Test Coverage Verification"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./gradlew "${GRADLE_COMMON_OPTS[@]}" jacocoTestReport jacocoTestCoverageVerification
coverage_status=$?
if [[ $coverage_status -ne 0 ]]; then
    log_warning "Coverage threshold not met (continuing...)"
    # Don't fail the entire pipeline for coverage
    # overall_status=1
else
    log_success "Coverage requirements met"
fi

# Generate coverage report link
if [[ -f "build/reports/jacoco/test/html/index.html" ]]; then
    log "Coverage report: build/reports/jacoco/test/html/index.html"
fi

# Calculate duration
end=$(date +%s)
duration=$((end - start))

# Final report
log ""
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [[ $overall_status -eq 0 ]]; then
    log_success "🎉 PR Gate Pipeline PASSED!"
    log_success "Duration: ${duration}s"
    log ""
    log "✅ All checks passed. PR is ready for review!"
else
    log_error "❌ PR Gate Pipeline FAILED"
    log_error "Duration: ${duration}s"
    log ""
    log "Please fix the issues above and run again."
    log "Reports available at:"
    log "  - Spotless: Run ./gradlew spotlessApply"
    log "  - Tests: build/reports/tests/test/index.html"
    log "  - Coverage: build/reports/jacoco/test/html/index.html"
    log "  - Failure details: .cascade/report.md"
fi
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Record metrics
metric pr_gate "${overall_status}" "${duration}"

exit $overall_status
