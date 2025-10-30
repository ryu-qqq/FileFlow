#!/usr/bin/env bash
# Convention validation script (SSOT)
# Validates Zero-Tolerance rules: Lombok, Law of Demeter, JPA relationships, etc.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

set +e  # Don't exit on error, we want to collect all violations

start=$(date +%s)
log "Starting convention validation..."

violations=0

# Navigate to project root
cd "${PROJECT_ROOT}" || exit 1

# 1. Lombok prohibition check
log "Checking for Lombok annotations..."
if find . -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" | \
   xargs grep -l "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor" 2>/dev/null; then
    log_error "❌ Lombok annotations found! Zero-Tolerance rule violated."
    violations=$((violations + 1))
    report_failure "validate_conventions" "Lombok annotations detected. Use pure Java instead."
else
    log_success "✅ No Lombok annotations found"
fi

# 2. Law of Demeter check (Getter chaining)
log "Checking for Getter chaining violations (Law of Demeter)..."
if find . -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" | \
   xargs grep -E "\.get[A-Z]\w*\(\)\.get[A-Z]\w*\(\)" 2>/dev/null; then
    log_error "❌ Getter chaining detected! Law of Demeter violated."
    violations=$((violations + 1))
    report_failure "validate_conventions" "Getter chaining detected. Use Tell, Don't Ask principle."
else
    log_success "✅ No Getter chaining violations"
fi

# 3. JPA relationship annotations prohibition
log "Checking for JPA relationship annotations..."
if find . -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" | \
   xargs grep -E "@(ManyToOne|OneToMany|OneToOne|ManyToMany)" 2>/dev/null; then
    log_error "❌ JPA relationship annotations found! Use Long FK strategy instead."
    violations=$((violations + 1))
    report_failure "validate_conventions" "JPA relationship annotations detected. Use Long FK (private Long userId) instead."
else
    log_success "✅ No JPA relationship annotations"
fi

# 4. Setter method prohibition (Domain/Entity layer)
log "Checking for Setter methods in Domain/Entity classes..."
if find domain -name "*.java" 2>/dev/null | \
   xargs grep -E "public void set[A-Z]\w*\(" 2>/dev/null; then
    log_error "❌ Setter methods found in Domain layer! Use business methods instead."
    violations=$((violations + 1))
    report_failure "validate_conventions" "Setter methods in Domain layer. Use business methods (e.g., confirm(), cancel())."
else
    log_success "✅ No Setter methods in Domain layer"
fi

# 5. Transaction boundary check (External API calls in @Transactional)
log "Checking for external API calls in @Transactional methods..."
# This is a simplified check - actual implementation may need more sophisticated analysis
if find . -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" | \
   xargs grep -B20 "@Transactional" 2>/dev/null | \
   grep -E "RestTemplate|WebClient|Feign|HttpClient" 2>/dev/null; then
    log_warning "⚠️  Potential external API call in @Transactional method detected."
    log_warning "Manual review recommended: Check if external calls are outside transaction boundaries."
fi

# 6. Spring proxy constraints check (Private @Transactional)
log "Checking for @Transactional on private methods..."
if find . -name "*.java" -not -path "*/build/*" -not -path "*/.gradle/*" | \
   xargs grep -B1 "@Transactional" 2>/dev/null | \
   grep -E "private.*\(" 2>/dev/null; then
    log_error "❌ @Transactional on private method! Spring proxy won't work."
    violations=$((violations + 1))
    report_failure "validate_conventions" "@Transactional on private method. Use public methods for @Transactional."
else
    log_success "✅ No @Transactional on private methods"
fi

# 7. Run Spotless check (code formatting)
log "Running Spotless format check..."
if ./gradlew "${GRADLE_COMMON_OPTS[@]}" spotlessCheck > /dev/null 2>&1; then
    log_success "✅ Code formatting is correct (Spotless)"
else
    log_error "❌ Code formatting issues detected. Run: ./gradlew spotlessApply"
    violations=$((violations + 1))
fi

# Calculate duration and report
end=$(date +%s)
duration=$((end - start))

if [[ $violations -eq 0 ]]; then
    log_success "🎉 All convention checks passed!"
    metric validate_conventions 0 "${duration}"
    exit 0
else
    log_error "❌ Convention validation failed with ${violations} violation(s)"
    metric validate_conventions 1 "${duration}"
    exit 1
fi
