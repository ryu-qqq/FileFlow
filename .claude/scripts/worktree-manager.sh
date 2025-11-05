#!/bin/bash

# =====================================================
# Worktree Manager Script
# Purpose: Git Worktree 자동화 (생성, 복사, 제거)
# Usage: bash worktree-manager.sh [create|remove|status] [args...]
# =====================================================

set -e

PROJECT_ROOT="$(git rev-parse --show-toplevel)"
WORKTREE_BASE="../"
WORK_ORDERS_DIR=".claude/work-orders"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}ℹ️  ${NC}$1"
}

log_success() {
    echo -e "${GREEN}✅ ${NC}$1"
}

log_warning() {
    echo -e "${YELLOW}⚠️  ${NC}$1"
}

log_error() {
    echo -e "${RED}❌ ${NC}$1"
}

# 사용법 출력
usage() {
    cat << 'USAGE'
🌲 Worktree Manager

Usage:
  worktree-manager.sh create <feature-name> [work-order]
  worktree-manager.sh remove <feature-name>
  worktree-manager.sh status
  worktree-manager.sh list

Examples:
  # Worktree 생성 (작업지시서 자동 복사)
  worktree-manager.sh create order order-aggregate.md

  # Worktree 생성 (작업지시서 없음)
  worktree-manager.sh create order

  # Worktree 제거 및 정리
  worktree-manager.sh remove order

  # 활성 Worktree 목록
  worktree-manager.sh list

  # Worktree 상태 확인
  worktree-manager.sh status
USAGE
}

# Worktree 생성
create_worktree() {
    local feature_name="$1"
    local work_order="$2"
    
    if [[ -z "$feature_name" ]]; then
        log_error "Feature name required"
        usage
        exit 1
    fi
    
    local branch_name="feature/${feature_name}"
    local worktree_path="${WORKTREE_BASE}wt-${feature_name}"
    
    log_info "Worktree 생성 시작: ${feature_name}"
    
    # 1. 브랜치 생성 확인
    if git show-ref --verify --quiet "refs/heads/${branch_name}"; then
        log_warning "브랜치 이미 존재: ${branch_name}"
    else
        log_info "브랜치 생성: ${branch_name}"
        git branch "${branch_name}"
    fi
    
    # 2. Worktree 추가
    log_info "Worktree 추가: ${worktree_path}"
    git worktree add "${worktree_path}" "${branch_name}"
    
    # 3. 작업지시서 복사 (있는 경우)
    if [[ -n "$work_order" ]]; then
        local work_order_path="${PROJECT_ROOT}/${WORK_ORDERS_DIR}/${work_order}"
        
        if [[ -f "$work_order_path" ]]; then
            log_info "작업지시서 복사: ${work_order}"
            cp "$work_order_path" "${worktree_path}/"
            log_success "작업지시서 복사 완료"
        else
            log_warning "작업지시서 없음: ${work_order_path}"
        fi
    fi
    
    # 4. .cursorrules 복사 (Cursor AI 규칙)
    if [[ -f "${PROJECT_ROOT}/.cursorrules" ]]; then
        log_info ".cursorrules 복사"
        cp "${PROJECT_ROOT}/.cursorrules" "${worktree_path}/"
    fi
    
    # 5. 완료 메시지
    log_success "Worktree 생성 완료!"
    echo ""
    echo "📂 Worktree 경로: ${worktree_path}"
    echo "🌿 브랜치: ${branch_name}"
    if [[ -n "$work_order" ]]; then
        echo "📋 작업지시서: ${work_order}"
    fi
    echo ""
    echo "📝 다음 단계:"
    echo "  1. cd ${worktree_path}"
    echo "  2. Cursor AI로 Boilerplate 생성"
    if [[ -n "$work_order" ]]; then
        echo "  3. ${work_order} 참조하여 코드 작성"
    fi
    echo "  4. git commit"
    echo "  5. cd ${PROJECT_ROOT} (복귀)"
    echo "  6. /validate-cursor-changes (검증)"
}

# Worktree 제거
remove_worktree() {
    local feature_name="$1"
    
    if [[ -z "$feature_name" ]]; then
        log_error "Feature name required"
        usage
        exit 1
    fi
    
    local worktree_path="${WORKTREE_BASE}wt-${feature_name}"
    
    log_info "Worktree 제거 시작: ${feature_name}"
    
    # 1. Worktree 존재 확인
    if ! git worktree list | grep -q "${worktree_path}"; then
        log_error "Worktree가 존재하지 않음: ${worktree_path}"
        exit 1
    fi
    
    # 2. 변경사항 확인
    cd "${worktree_path}"
    if ! git diff-index --quiet HEAD --; then
        log_warning "커밋되지 않은 변경사항 존재"
        read -p "계속 진행하시겠습니까? (y/N): " confirm
        if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
            log_info "취소됨"
            exit 0
        fi
    fi
    cd "${PROJECT_ROOT}"
    
    # 3. Worktree 제거
    log_info "Worktree 제거: ${worktree_path}"
    git worktree remove "${worktree_path}" --force
    
    log_success "Worktree 제거 완료!"
    echo ""
    echo "🌿 브랜치는 유지됩니다: feature/${feature_name}"
    echo ""
    echo "📝 다음 단계:"
    echo "  1. git merge feature/${feature_name} (Merge)"
    echo "  2. git branch -d feature/${feature_name} (브랜치 삭제)"
}

# Worktree 목록
list_worktrees() {
    log_info "활성 Worktree 목록:"
    echo ""
    git worktree list
}

# Worktree 상태
status_worktree() {
    log_info "Worktree 상태:"
    echo ""
    
    local worktree_count=$(git worktree list | wc -l)
    
    if [[ $worktree_count -eq 1 ]]; then
        echo "활성 Worktree: 없음"
    else
        echo "활성 Worktree: $((worktree_count - 1))개"
        echo ""
        git worktree list | tail -n +2 | while read -r line; do
            local path=$(echo "$line" | awk '{print $1}')
            local branch=$(echo "$line" | awk '{print $2}' | tr -d '[]')
            echo "  📂 $path"
            echo "  🌿 $branch"
            echo ""
        done
    fi
}

# 메인 로직
case "${1:-}" in
    create)
        create_worktree "$2" "$3"
        ;;
    remove)
        remove_worktree "$2"
        ;;
    list)
        list_worktrees
        ;;
    status)
        status_worktree
        ;;
    *)
        usage
        exit 1
        ;;
esac
