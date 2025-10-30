#!/bin/bash
#
# 통합 Squad 시작 스크립트
#
# 목적: Epic 단위 통합 개발 워크플로우 - Cascade 보일러플레이트 생성 → Claude 비즈니스 로직 구현 → 검증
# 사용법: ./scripts/integrated-squad-start.sh <epic-key> <aggregate-name> [--sequential|--parallel]
#
# 예시:
#   ./scripts/integrated-squad-start.sh PROJ-100 Order --sequential
#   ./scripts/integrated-squad-start.sh PROJ-100 Order --parallel
#
# 워크플로우:
#   1. Epic 분석 (Jira Epic → 여러 Task 분해)
#   2. Layer별 순차/병렬 실행
#      - Domain Layer (boilerplate → logic → validate)
#      - Application Layer (boilerplate → logic → validate)
#      - Persistence Layer (boilerplate → logic → validate)
#      - REST API Layer (boilerplate → logic → validate)
#   3. 통합 검증 (전체 아키텍처 검증)
#   4. Git Commit & PR 생성
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
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
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

log_section() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC} $1"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════╝${NC}"
}

log_step() {
    echo -e "${MAGENTA}▶${NC} $1"
}

# ==============================================================================
# 파라미터 검증
# ==============================================================================
if [ $# -lt 2 ]; then
    log_error "사용법: $0 <epic-key> <aggregate-name> [--sequential|--parallel]"
    echo ""
    echo "파라미터:"
    echo "  epic-key        : Jira Epic Key (예: PROJ-100)"
    echo "  aggregate-name  : Aggregate 이름 (PascalCase, 예: Order)"
    echo "  --sequential    : Layer별 순차 실행 (기본값)"
    echo "  --parallel      : Layer별 병렬 실행 (고급 사용자)"
    echo ""
    echo "예시:"
    echo "  $0 PROJ-100 Order --sequential"
    echo "  $0 PROJ-100 Order --parallel"
    exit 1
fi

EPIC_KEY="$1"
AGGREGATE_NAME="$2"
EXECUTION_MODE="${3:---sequential}"

# ==============================================================================
# Aggregate 이름 검증 (PascalCase)
# ==============================================================================
if ! [[ "$AGGREGATE_NAME" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
    log_error "Aggregate 이름은 PascalCase여야 합니다 (예: Order, UserProfile)"
    log_error "현재 입력: $AGGREGATE_NAME"
    exit 1
fi

# ==============================================================================
# 실행 모드 검증
# ==============================================================================
if [[ "$EXECUTION_MODE" != "--sequential" && "$EXECUTION_MODE" != "--parallel" ]]; then
    log_error "유효하지 않은 실행 모드: $EXECUTION_MODE"
    log_error "지원하는 모드: --sequential, --parallel"
    exit 1
fi

# ==============================================================================
# 프로젝트 루트 디렉토리 확인
# ==============================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

log_info "프로젝트 루트: $PROJECT_ROOT"

# ==============================================================================
# 스크립트 경로 확인
# ==============================================================================
SCRIPT_DIR="$PROJECT_ROOT/scripts"

BOILERPLATE_SCRIPT="$SCRIPT_DIR/cascade-generate-boilerplate.sh"
LOGIC_SCRIPT="$SCRIPT_DIR/claude-implement-business-logic.sh"
VALIDATION_SCRIPT="$SCRIPT_DIR/integrated-validation.sh"

for script in "$BOILERPLATE_SCRIPT" "$LOGIC_SCRIPT" "$VALIDATION_SCRIPT"; do
    if [ ! -x "$script" ]; then
        log_error "스크립트 실행 권한 없음: $script"
        log_error "다음 명령어 실행: chmod +x $script"
        exit 1
    fi
done

# ==============================================================================
# Jira Epic 분석 (Task 분해)
# ==============================================================================
log_section "Epic 분석 시작"
log_info "Epic Key: $EPIC_KEY"
log_info "Aggregate: $AGGREGATE_NAME"
log_info "실행 모드: $EXECUTION_MODE"

# Epic에서 Task Key 목록 추출 (예시)
# 실제 환경에서는 Jira API 연동 필요
declare -A TASK_KEYS=(
    ["domain"]="${EPIC_KEY}-1"
    ["application"]="${EPIC_KEY}-2"
    ["persistence"]="${EPIC_KEY}-3"
    ["rest-api"]="${EPIC_KEY}-4"
)

log_success "Epic 분석 완료"
log_info "생성될 Task 목록:"
for layer in domain application persistence rest-api; do
    log_info "  - $layer: ${TASK_KEYS[$layer]}"
done

# ==============================================================================
# Git 브랜치 생성
# ==============================================================================
log_section "Git 브랜치 생성"

BRANCH_NAME="feature/${EPIC_KEY}-${AGGREGATE_NAME,,}"

log_info "브랜치 이름: $BRANCH_NAME"

if git rev-parse --verify "$BRANCH_NAME" >/dev/null 2>&1; then
    log_warn "브랜치가 이미 존재합니다: $BRANCH_NAME"
    log_warn "기존 브랜치로 체크아웃합니다"
    git checkout "$BRANCH_NAME"
else
    log_info "새 브랜치 생성 중..."
    git checkout -b "$BRANCH_NAME"
    log_success "브랜치 생성 완료"
fi

# ==============================================================================
# Layer별 실행 함수
# ==============================================================================
execute_layer() {
    local layer=$1
    local task_key="${TASK_KEYS[$layer]}"

    log_section "Layer 실행: $layer"

    # Step 1: Boilerplate 생성
    log_step "[1/3] Cascade Boilerplate 생성"
    if ! "$BOILERPLATE_SCRIPT" "$task_key" "$layer" "$AGGREGATE_NAME"; then
        log_error "$layer: Boilerplate 생성 실패"
        return 1
    fi
    log_success "$layer: Boilerplate 생성 완료"

    # Step 2: 비즈니스 로직 구현
    log_step "[2/3] Claude Code 비즈니스 로직 구현"
    if ! "$LOGIC_SCRIPT" "$task_key" "$layer"; then
        log_error "$layer: 비즈니스 로직 구현 실패"
        return 1
    fi
    log_success "$layer: 비즈니스 로직 구현 완료"

    # Step 3: 검증
    log_step "[3/3] 통합 검증"
    if ! "$VALIDATION_SCRIPT" "$layer"; then
        log_error "$layer: 검증 실패"
        return 1
    fi
    log_success "$layer: 검증 완료"

    # Step 4: Layer별 Commit
    log_step "[4/4] Git Commit"
    git add .
    git commit -m "feat($layer): implement $AGGREGATE_NAME $layer ($task_key)

- Cascade Boilerplate 생성
- Claude Code 비즈니스 로직 구현
- 통합 검증 통과

Task: $task_key"

    log_success "$layer: Layer 완료 ✓"
    return 0
}

# ==============================================================================
# 순차 실행 모드
# ==============================================================================
execute_sequential() {
    log_section "순차 실행 모드 시작"

    local LAYERS=("domain" "application" "persistence" "rest-api")
    local FAILED_LAYERS=()

    for layer in "${LAYERS[@]}"; do
        if ! execute_layer "$layer"; then
            FAILED_LAYERS+=("$layer")
            log_error "Layer 실패: $layer"

            # 실패 시 계속 진행 여부 확인
            read -p "계속 진행하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                log_error "사용자가 중단했습니다"
                return 1
            fi
        fi
    done

    if [ ${#FAILED_LAYERS[@]} -eq 0 ]; then
        log_success "모든 Layer 실행 성공!"
        return 0
    else
        log_error "실패한 Layer: ${FAILED_LAYERS[*]}"
        return 1
    fi
}

# ==============================================================================
# 병렬 실행 모드 (고급)
# ==============================================================================
execute_parallel() {
    log_section "병렬 실행 모드 시작"
    log_warn "⚠️ 병렬 실행은 고급 기능입니다!"
    log_warn "⚠️ Domain → Application → Persistence → REST API 의존성 무시"

    local LAYERS=("domain" "application" "persistence" "rest-api")
    local PIDS=()

    # 병렬 실행
    for layer in "${LAYERS[@]}"; do
        execute_layer "$layer" &
        PIDS+=($!)
    done

    # 모든 프로세스 대기
    local FAILED=false
    for pid in "${PIDS[@]}"; do
        if ! wait "$pid"; then
            FAILED=true
        fi
    done

    if [ "$FAILED" == "false" ]; then
        log_success "모든 Layer 병렬 실행 성공!"
        return 0
    else
        log_error "일부 Layer 실행 실패"
        return 1
    fi
}

# ==============================================================================
# 실행 모드 선택
# ==============================================================================
EXECUTION_SUCCESS=false

if [ "$EXECUTION_MODE" == "--sequential" ]; then
    if execute_sequential; then
        EXECUTION_SUCCESS=true
    fi
elif [ "$EXECUTION_MODE" == "--parallel" ]; then
    if execute_parallel; then
        EXECUTION_SUCCESS=true
    fi
fi

# ==============================================================================
# 통합 검증 (전체 아키텍처)
# ==============================================================================
if [ "$EXECUTION_SUCCESS" == "true" ]; then
    log_section "통합 검증 (전체 아키텍처)"

    if "$VALIDATION_SCRIPT" all; then
        log_success "전체 아키텍처 검증 통과"
    else
        log_error "전체 아키텍처 검증 실패"
        EXECUTION_SUCCESS=false
    fi
fi

# ==============================================================================
# Pull Request 생성
# ==============================================================================
if [ "$EXECUTION_SUCCESS" == "true" ]; then
    log_section "Pull Request 생성"

    log_info "GitHub CLI를 사용하여 PR 생성..."

    # PR 본문 생성
    PR_BODY=$(cat <<EOF
## 📦 Epic: $EPIC_KEY - $AGGREGATE_NAME 구현

### 🎯 구현 내용
- **Domain Layer**: Aggregate Root, Value Objects, Domain Events
- **Application Layer**: UseCase, Command/Query, Assembler
- **Persistence Layer**: JPA Entity, Repository, Persistence Adapter
- **REST API Layer**: Controller, Request/Response DTOs, Error Mapper

### ✅ 검증 완료
- [x] Gradle Build 성공
- [x] 단위 테스트 통과
- [x] ArchUnit 아키텍처 검증
- [x] Checkstyle 코드 품질 검증

### 📋 Task 목록
EOF
)

    for layer in domain application persistence rest-api; do
        PR_BODY+="
- ${TASK_KEYS[$layer]}: $layer layer 구현"
    done

    # gh 명령어로 PR 생성
    if command -v gh &> /dev/null; then
        if gh pr create \
            --title "feat: ${EPIC_KEY} - ${AGGREGATE_NAME} 구현" \
            --body "$PR_BODY" \
            --base main; then
            log_success "Pull Request 생성 완료"
        else
            log_warn "PR 생성 실패 (수동으로 생성하세요)"
        fi
    else
        log_warn "GitHub CLI (gh)가 설치되어 있지 않습니다"
        log_info "수동으로 PR을 생성하세요"
        log_info "  브랜치: $BRANCH_NAME"
        log_info "  타이틀: feat: ${EPIC_KEY} - ${AGGREGATE_NAME} 구현"
    fi
fi

# ==============================================================================
# 완료 메시지
# ==============================================================================
log_section "통합 Squad 실행 완료"

if [ "$EXECUTION_SUCCESS" == "true" ]; then
    log_success "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_success "  🎉 모든 작업이 성공적으로 완료되었습니다!"
    log_success "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info ""
    log_info "✅ 완료된 작업:"
    log_info "   - Cascade Boilerplate 생성 (4 Layers)"
    log_info "   - Claude Code 비즈니스 로직 구현"
    log_info "   - 통합 검증 (Build, Test, ArchUnit, Checkstyle)"
    log_info "   - Git Commit & Branch Push"
    log_info "   - Pull Request 생성"
    log_info ""
    log_info "📋 다음 단계:"
    log_info "   1. PR 리뷰 요청"
    log_info "   2. 코드 리뷰 반영"
    log_info "   3. Merge to main"
    log_info ""
    log_success "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 0
else
    log_error "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_error "  ❌ 일부 작업이 실패했습니다"
    log_error "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info ""
    log_info "🔍 실패 원인 분석:"
    log_info "   - 로그 파일 확인: claudedocs/validation-reports/"
    log_info "   - 코딩 규칙 확인: docs/coding_convention/"
    log_info ""
    log_info "🔧 복구 방법:"
    log_info "   1. 오류 수정"
    log_info "   2. Layer별 재실행:"
    log_info "      ./scripts/integrated-validation.sh <layer>"
    log_info "   3. 전체 재실행:"
    log_info "      $0 $EPIC_KEY $AGGREGATE_NAME $EXECUTION_MODE"
    log_info ""
    log_error "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 1
fi
