#!/bin/bash
#
# Cascade Boilerplate 생성 스크립트
#
# 목적: Windsurf Cascade Workflow를 실행하여 Layer별 보일러플레이트 코드 생성
# 사용법: ./scripts/cascade-generate-boilerplate.sh <task-key> <layer> <aggregate-name>
#
# 예시:
#   ./scripts/cascade-generate-boilerplate.sh PROJ-123 domain Order
#   ./scripts/cascade-generate-boilerplate.sh PROJ-123 application Order
#   ./scripts/cascade-generate-boilerplate.sh PROJ-123 persistence Order
#   ./scripts/cascade-generate-boilerplate.sh PROJ-123 rest-api Order
#
# Author: Spring Standards Team
# Version: 1.0.0
# Created: 2025-01-28

set -euo pipefail

# ==============================================================================
# 색상 정의
# ==============================================================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ==============================================================================
# 로깅 함수
# ==============================================================================
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ==============================================================================
# 파라미터 검증
# ==============================================================================
if [ $# -lt 3 ]; then
    log_error "사용법: $0 <task-key> <layer> <aggregate-name>"
    echo ""
    echo "파라미터:"
    echo "  task-key        : Jira Task Key (예: PROJ-123)"
    echo "  layer           : Layer 타입 (domain|application|persistence|rest-api)"
    echo "  aggregate-name  : Aggregate 이름 (PascalCase, 예: Order)"
    echo ""
    echo "예시:"
    echo "  $0 PROJ-123 domain Order"
    echo "  $0 PROJ-123 application Order"
    exit 1
fi

TASK_KEY="$1"
LAYER="$2"
AGGREGATE_NAME="$3"

# ==============================================================================
# Layer 검증
# ==============================================================================
case "$LAYER" in
    domain|application|persistence|rest-api)
        log_info "Layer 검증 성공: $LAYER"
        ;;
    *)
        log_error "유효하지 않은 Layer: $LAYER"
        log_error "지원하는 Layer: domain, application, persistence, rest-api"
        exit 1
        ;;
esac

# ==============================================================================
# Aggregate 이름 검증 (PascalCase)
# ==============================================================================
if ! [[ "$AGGREGATE_NAME" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
    log_error "Aggregate 이름은 PascalCase여야 합니다 (예: Order, UserProfile)"
    log_error "현재 입력: $AGGREGATE_NAME"
    exit 1
fi

# ==============================================================================
# Windsurf 설치 확인
# ==============================================================================
if ! command -v windsurf &> /dev/null; then
    log_error "Windsurf CLI가 설치되어 있지 않습니다"
    log_error "설치 방법: https://docs.windsurf.com/installation"
    exit 1
fi

# ==============================================================================
# 프로젝트 루트 디렉토리 확인
# ==============================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

log_info "프로젝트 루트: $PROJECT_ROOT"

# ==============================================================================
# .windsurf/workflows 디렉토리 확인
# ==============================================================================
WORKFLOWS_DIR="$PROJECT_ROOT/.windsurf/workflows"
if [ ! -d "$WORKFLOWS_DIR" ]; then
    log_error ".windsurf/workflows 디렉토리가 존재하지 않습니다"
    log_error "경로: $WORKFLOWS_DIR"
    exit 1
fi

# ==============================================================================
# Layer별 Cascade Workflow 실행
# ==============================================================================
log_info "=========================================="
log_info "Cascade Boilerplate 생성 시작"
log_info "=========================================="
log_info "Task Key: $TASK_KEY"
log_info "Layer: $LAYER"
log_info "Aggregate: $AGGREGATE_NAME"
log_info "=========================================="

case "$LAYER" in
    domain)
        log_info "Domain Layer 워크플로우 실행 중..."

        # Domain Aggregate 생성
        log_info "[1/4] Aggregate 생성 중..."
        windsurf cascade run .windsurf/workflows/01-domain/create-aggregate.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || {
            log_error "Aggregate 생성 실패"
            exit 1
        }
        log_success "Aggregate 생성 완료"

        # Value Objects 생성 (선택적)
        log_info "[2/4] Value Objects 생성 중..."
        windsurf cascade run .windsurf/workflows/01-domain/create-value-object.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Value Objects 생성 건너뜀"

        # Domain Exceptions 생성
        log_info "[3/4] Domain Exceptions 생성 중..."
        windsurf cascade run .windsurf/workflows/01-domain/create-domain-exception.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Domain Exceptions 생성 건너뜀"

        # Domain Event 생성 (선택적)
        log_info "[4/4] Domain Event 생성 중..."
        windsurf cascade run .windsurf/workflows/01-domain/create-domain-event.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Domain Event 생성 건너뜀"

        log_success "Domain Layer 생성 완료!"
        ;;

    application)
        log_info "Application Layer 워크플로우 실행 중..."

        # UseCase 생성
        log_info "[1/4] UseCase 생성 중..."
        windsurf cascade run .windsurf/workflows/02-application/create-usecase.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --useCaseName "Create${AGGREGATE_NAME}" \
            --packageName "com.ryuqq" || {
            log_error "UseCase 생성 실패"
            exit 1
        }
        log_success "UseCase 생성 완료"

        # Application Service 생성
        log_info "[2/4] Application Service 생성 중..."
        windsurf cascade run .windsurf/workflows/02-application/create-application-service.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Application Service 생성 건너뜀"

        # Assembler 생성
        log_info "[3/4] Assembler 생성 중..."
        windsurf cascade run .windsurf/workflows/02-application/create-assembler.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Assembler 생성 건너뜀"

        # OutPort 생성
        log_info "[4/4] OutPort 생성 중..."
        windsurf cascade run .windsurf/workflows/02-application/create-outport.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --portType "command" \
            --packageName "com.ryuqq" || log_warn "OutPort 생성 건너뜀"

        log_success "Application Layer 생성 완료!"
        ;;

    persistence)
        log_info "Persistence Layer 워크플로우 실행 중..."

        # JPA Entity 생성
        log_info "[1/4] JPA Entity 생성 중..."
        windsurf cascade run .windsurf/workflows/03-persistence/create-jpa-entity.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || {
            log_error "JPA Entity 생성 실패"
            exit 1
        }
        log_success "JPA Entity 생성 완료"

        # Repository 생성
        log_info "[2/4] Repository 생성 중..."
        windsurf cascade run .windsurf/workflows/03-persistence/create-repository.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Repository 생성 건너뜀"

        # Entity Mapper 생성
        log_info "[3/4] Entity Mapper 생성 중..."
        windsurf cascade run .windsurf/workflows/03-persistence/create-entity-mapper.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Entity Mapper 생성 건너뜀"

        # Persistence Adapter 생성
        log_info "[4/4] Persistence Adapter 생성 중..."
        windsurf cascade run .windsurf/workflows/03-persistence/create-persistence-adapter.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Persistence Adapter 생성 건너뜀"

        log_success "Persistence Layer 생성 완료!"
        ;;

    rest-api)
        log_info "REST API Layer 워크플로우 실행 중..."

        # Controller 생성
        log_info "[1/4] Controller 생성 중..."
        windsurf cascade run .windsurf/workflows/04-rest-api/create-controller.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --endpoints "create,get,update,delete" \
            --packageName "com.ryuqq" || {
            log_error "Controller 생성 실패"
            exit 1
        }
        log_success "Controller 생성 완료"

        # Request DTOs 생성
        log_info "[2/4] Request DTOs 생성 중..."
        windsurf cascade run .windsurf/workflows/04-rest-api/create-request-dto.yaml \
            --name "$AGGREGATE_NAME" \
            --action "Create" \
            --packageName "com.ryuqq" || log_warn "Request DTOs 생성 건너뜀"

        # Response DTOs 생성
        log_info "[3/4] Response DTOs 생성 중..."
        windsurf cascade run .windsurf/workflows/04-rest-api/create-response-dto.yaml \
            --name "$AGGREGATE_NAME" \
            --type "all" \
            --packageName "com.ryuqq" || log_warn "Response DTOs 생성 건너뜀"

        # Error Mapper 생성
        log_info "[4/4] Error Mapper 생성 중..."
        windsurf cascade run .windsurf/workflows/04-rest-api/create-error-mapper.yaml \
            --aggregateName "$AGGREGATE_NAME" \
            --packageName "com.ryuqq" || log_warn "Error Mapper 생성 건너뜀"

        log_success "REST API Layer 생성 완료!"
        ;;
esac

# ==============================================================================
# 생성된 파일 목록 출력
# ==============================================================================
log_info "=========================================="
log_info "생성된 파일 목록"
log_info "=========================================="

AGGREGATE_LOWER=$(echo "$AGGREGATE_NAME" | tr '[:upper:]' '[:lower:]')

case "$LAYER" in
    domain)
        find domain/src/main/java -name "*${AGGREGATE_NAME}*" -type f 2>/dev/null || log_warn "생성된 파일 없음"
        ;;
    application)
        find application/src/main/java -name "*${AGGREGATE_NAME}*" -type f 2>/dev/null || log_warn "생성된 파일 없음"
        ;;
    persistence)
        find adapter-out/persistence-mysql/src/main/java -name "*${AGGREGATE_NAME}*" -type f 2>/dev/null || log_warn "생성된 파일 없음"
        ;;
    rest-api)
        find adapter-in/rest-api/src/main/java -name "*${AGGREGATE_NAME}*" -type f 2>/dev/null || log_warn "생성된 파일 없음"
        ;;
esac

# ==============================================================================
# 완료 메시지
# ==============================================================================
log_info "=========================================="
log_success "Cascade Boilerplate 생성 완료!"
log_info "=========================================="
log_info "다음 단계:"
log_info "  1. 생성된 코드 확인"
log_info "  2. Claude Code로 비즈니스 로직 구현"
log_info "     ./scripts/claude-implement-business-logic.sh $TASK_KEY $LAYER"
log_info "  3. 통합 검증 실행"
log_info "     ./scripts/integrated-validation.sh $LAYER"
log_info "=========================================="

exit 0
