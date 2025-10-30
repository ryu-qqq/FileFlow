#!/bin/bash
# ================================================
# Orchestration Pattern Validator
# ================================================
# Validates Orchestration Pattern conventions:
# 1. executeInternal() @Async required, @Transactional prohibited
# 2. Command must use Record pattern (Lombok prohibited)
# 3. IdemKey Unique constraint required
# 4. Outcome return type required
# 5. BaseOrchestrator inheritance required
# ================================================

set -e

# Colors
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

VALIDATION_FAILED=0

# ================================================
# Helper Functions
# ================================================

log_error() {
    echo -e "${RED}❌ $1${NC}" >&2
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}" >&2
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# ================================================
# Validation Functions
# ================================================

validate_orchestrator() {
    local file="$1"
    local content=$(cat "$file")

    # executeInternal 메서드가 있는지 확인
    if echo "$content" | grep -q "executeInternal"; then

        # 1. @Async 필수 체크
        if ! echo "$content" | grep -B5 "executeInternal" | grep -q "@Async"; then
            log_error "Orchestrator executeInternal() must have @Async annotation"
            log_warning "  File: $file"
            log_warning "  Rule: executeInternal()은 외부 API 호출을 위해 @Async가 필수입니다 (트랜잭션 밖에서 실행)"
            echo ""
            return 1
        fi

        # 2. @Transactional 금지 체크
        if echo "$content" | grep -B5 "executeInternal" | grep -q "@Transactional"; then
            log_error "Orchestrator executeInternal() must NOT have @Transactional"
            log_warning "  File: $file"
            log_warning "  Rule: 외부 API 호출은 트랜잭션 밖에서 실행해야 합니다"
            log_warning "  Fix: @Transactional을 제거하고 @Async만 사용하세요"
            echo ""
            return 1
        fi

        # 3. Outcome 반환 타입 체크
        if ! echo "$content" | grep "executeInternal" | grep -q "Outcome"; then
            log_error "Orchestrator executeInternal() must return Outcome"
            log_warning "  File: $file"
            log_warning "  Rule: executeInternal()은 Outcome (Ok/Retry/Fail)을 반환해야 타입 안전합니다"
            log_warning "  Fix: 반환 타입을 Outcome으로 변경하세요"
            echo ""
            return 1
        fi
    fi

    # 4. BaseOrchestrator 상속 체크
    if [[ $file == *"Orchestrator.java" ]] && [[ $file != *"Base"* ]]; then
        if ! echo "$content" | grep -q "extends BaseOrchestrator"; then
            log_error "Orchestrator must extend BaseOrchestrator"
            log_warning "  File: $file"
            log_warning "  Rule: Orchestrator는 BaseOrchestrator를 상속하여 3-Phase Lifecycle을 구현해야 합니다"
            log_warning "  Fix: public class XXXOrchestrator extends BaseOrchestrator<XXXCommand>"
            echo ""
            return 1
        fi
    fi

    return 0
}

validate_command() {
    local file="$1"
    local content=$(cat "$file")

    # 1. Record 패턴 체크
    if [[ $file == *"Command.java" ]]; then
        if ! echo "$content" | grep -q "public record"; then
            log_error "Command must use Record pattern"
            log_warning "  File: $file"
            log_warning "  Rule: Command는 Record 패턴을 사용해야 합니다 (Lombok 금지)"
            log_warning "  Fix: public record XXXCommand(String idemKey, ...) { ... }"
            echo ""
            return 1
        fi

        # 2. Lombok 금지 체크
        if echo "$content" | grep -qE "@(Data|Builder|Getter|Setter|AllArgsConstructor|NoArgsConstructor)"; then
            log_error "Command must NOT use Lombok annotations"
            log_warning "  File: $file"
            log_warning "  Rule: Command는 Record 패턴을 사용하므로 Lombok은 금지됩니다"
            log_warning "  Fix: Lombok 어노테이션을 제거하고 Record 패턴을 사용하세요"
            echo ""
            return 1
        fi

        # 3. IdemKey 필드 체크
        if ! echo "$content" | grep -qE "(idempotencyKey|idemKey)"; then
            log_error "Command must have IdemKey field"
            log_warning "  File: $file"
            log_warning "  Rule: Command는 멱등성 보장을 위해 IdemKey 필드가 필수입니다"
            log_warning "  Fix: String idempotencyKey 또는 String idemKey 필드를 추가하세요"
            echo ""
            return 1
        fi
    fi

    return 0
}

validate_operation_entity() {
    local file="$1"
    local content=$(cat "$file")

    # IdemKey Unique 제약 체크
    if [[ $file == *"OperationEntity.java" ]]; then
        if ! echo "$content" | grep -q "@UniqueConstraint"; then
            log_error "Operation Entity must have IdemKey Unique constraint"
            log_warning "  File: $file"
            log_warning "  Rule: IdemKey는 DB Unique 제약으로 중복 실행을 방지해야 합니다"
            log_warning "  Fix: @UniqueConstraint(name = \"uk_xxx_operations_idem_key\", columnNames = {\"idem_key\"})"
            echo ""
            return 1
        fi
    fi

    return 0
}

validate_scheduler() {
    local file="$1"
    local content=$(cat "$file")

    # Finalizer/Reaper @Scheduled 체크
    if [[ $file == *"Finalizer.java" ]] || [[ $file == *"Reaper.java" ]]; then
        if ! echo "$content" | grep -q "@Scheduled"; then
            log_error "Scheduler must have @Scheduled annotation"
            log_warning "  File: $file"
            log_warning "  Rule: Finalizer/Reaper는 주기적 처리를 위해 @Scheduled가 필수입니다"
            log_warning "  Fix: @Scheduled(fixedDelay = 5000) 어노테이션을 추가하세요"
            echo ""
            return 1
        fi
    fi

    return 0
}

# ================================================
# Main Validation Loop
# ================================================

echo "🔍 Orchestration Pattern Validation"
echo "=================================="
echo ""

for file in "$@"; do
    # .java 파일만 검증
    [[ $file != *.java ]] && continue

    # 파일이 존재하지 않으면 스킵 (삭제된 파일)
    [[ ! -f $file ]] && continue

    # Orchestrator 검증
    if [[ $file == *"Orchestrator.java" ]]; then
        if ! validate_orchestrator "$file"; then
            VALIDATION_FAILED=1
        fi
    fi

    # Command 검증
    if [[ $file == *"Command.java" ]]; then
        if ! validate_command "$file"; then
            VALIDATION_FAILED=1
        fi
    fi

    # Operation Entity 검증
    if [[ $file == *"OperationEntity.java" ]]; then
        if ! validate_operation_entity "$file"; then
            VALIDATION_FAILED=1
        fi
    fi

    # Scheduler 검증
    if [[ $file == *"Finalizer.java" ]] || [[ $file == *"Reaper.java" ]]; then
        if ! validate_scheduler "$file"; then
            VALIDATION_FAILED=1
        fi
    fi
done

# ================================================
# Result
# ================================================

if [ $VALIDATION_FAILED -eq 1 ]; then
    echo ""
    log_error "Orchestration Pattern validation failed!"
    echo ""
    echo "💡 규칙 참고: docs/coding_convention/09-orchestration-patterns/"
    echo "💡 자동 생성: /code-gen-orchestrator <Domain> <EventType>"
    echo ""
    exit 1
else
    log_success "Orchestration Pattern validation passed"
    exit 0
fi
