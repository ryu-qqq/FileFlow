#!/bin/bash
# ========================================
# Claude Code After Tool Use Hook
# ========================================
# Claude가 도구(Write, Edit 등)를 사용한 AFTER 실행
# 실시간 코드 검증
# ========================================

set -e

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$HOOK_DIR/../.." && pwd)"
VALIDATORS_DIR="$PROJECT_ROOT/hooks/validators"

# Colors
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

log_error() {
    echo -e "${RED}❌ $1${NC}" >&2
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}" >&2
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}" >&2
}

# ========================================
# 도구 사용 정보 파싱
# ========================================

TOOL_NAME="$1"
FILE_PATH="$2"

# Write/Edit 도구가 아니면 스킵
if [[ "$TOOL_NAME" != "Write" && "$TOOL_NAME" != "Edit" && "$TOOL_NAME" != "MultiEdit" ]]; then
    exit 0
fi

# Java 파일이 아니면 스킵
if [[ ! "$FILE_PATH" =~ \.java$ ]]; then
    exit 0
fi

log_warning "Validating: $FILE_PATH"

# ========================================
# 레이어별 인라인 검증
# ========================================

log_info() {
    echo -e "\033[0;34mℹ️  $1\033[0m" >&2
}

validate_application_layer() {
    local file="$1"

    # Check @Transactional exists in UseCase implementations
    if [[ "$file" == *"UseCase.java" ]] || [[ "$file" == *"Service.java" ]]; then
        if ! grep -q "@Transactional" "$file"; then
            log_warning "$file: UseCase implementation should have @Transactional"
        fi
    fi
}

validate_persistence_layer() {
    local file="$1"

    # Check for forbidden JPA relationship annotations
    if grep -qE "@(OneToMany|ManyToOne|OneToOne|ManyToMany)" "$file"; then
        log_error "$file: JPA relationship annotations FORBIDDEN. Use Long foreign keys."
        return 1
    fi

    # Check for setter methods
    if grep -qE "public void set[A-Z]" "$file"; then
        log_error "$file: Setter methods FORBIDDEN in entities"
        return 1
    fi

    # Check for public constructors in Entity classes
    if [[ "$file" == *"Entity.java" ]]; then
        if grep -qE "^\s*public\s+\w+Entity\s*\(" "$file"; then
            log_error "$file: Public constructors FORBIDDEN. Use static factory methods."
            return 1
        fi
    fi

    # Check for @Transactional in adapters
    if grep -q "@Transactional" "$file"; then
        log_error "$file: @Transactional FORBIDDEN in adapters. Use Application layer."
        return 1
    fi

    return 0
}

validate_controller_layer() {
    local file="$1"

    # Check for inner classes
    if [[ "$file" == *"Controller.java" ]]; then
        if grep -qE "(static\s+)?class\s+\w+(Request|Response)" "$file"; then
            log_error "$file: Inner classes FORBIDDEN in controllers"
            return 1
        fi
    fi

    # Check if Request/Response are records
    if [[ "$file" == *"Request.java" ]] || [[ "$file" == *"Response.java" ]]; then
        if ! grep -q "public record" "$file"; then
            log_error "$file: Request/Response must be Java records"
            return 1
        fi
    fi

    return 0
}

# ========================================
# 모듈별 검증 라우팅
# ========================================

VALIDATION_FAILED=0

if [[ "$FILE_PATH" == *"domain/"* ]]; then
    log_warning "Running domain validator..."
    if bash "$VALIDATORS_DIR/domain-validator.sh" "$FILE_PATH"; then
        log_success "Domain validation passed"
    else
        log_error "Domain validation failed"
        VALIDATION_FAILED=1
    fi

elif [[ "$FILE_PATH" == *"application/"* ]]; then
    log_warning "Running application validator..."
    validate_application_layer "$FILE_PATH"
    if bash "$VALIDATORS_DIR/application-validator.sh" "$FILE_PATH"; then
        log_success "Application validation passed"
    else
        log_error "Application validation failed"
        VALIDATION_FAILED=1
    fi

elif [[ "$FILE_PATH" == *"adapter/adapter-in-"* ]]; then
    log_warning "Running adapter-in validator..."
    if ! validate_controller_layer "$FILE_PATH"; then
        VALIDATION_FAILED=1
    fi
    if bash "$VALIDATORS_DIR/adapter-in-validator.sh" "$FILE_PATH"; then
        log_success "Adapter-in validation passed"
    else
        log_error "Adapter-in validation failed"
        VALIDATION_FAILED=1
    fi

elif [[ "$FILE_PATH" == *"adapter/adapter-out-"* ]]; then
    log_warning "Running adapter-out validator..."
    if ! validate_persistence_layer "$FILE_PATH"; then
        VALIDATION_FAILED=1
    fi
    if bash "$VALIDATORS_DIR/adapter-out-validator.sh" "$FILE_PATH"; then
        log_success "Adapter-out validation passed"
    else
        log_error "Adapter-out validation failed"
        VALIDATION_FAILED=1
    fi
fi

# ========================================
# 공통 검증
# ========================================

log_warning "Running common validator..."
bash "$VALIDATORS_DIR/common-validator.sh" "$FILE_PATH" || true

# ========================================
# 데드코드 감지
# ========================================

log_warning "Running dead code detector..."
bash "$VALIDATORS_DIR/dead-code-detector.sh" "$FILE_PATH" || true

# ========================================
# 결과 처리
# ========================================

if [ $VALIDATION_FAILED -eq 1 ]; then
    log_error "Validation failed for: $FILE_PATH"
    log_warning "Please fix the issues above"
    log_warning "File was created/modified but violates architectural rules"
    exit 1
fi

log_success "All validations passed for: $FILE_PATH"
exit 0
