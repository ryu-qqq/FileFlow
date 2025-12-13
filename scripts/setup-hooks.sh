#!/bin/bash

# =====================================================
# Git Hooks 자동 설치 스크립트
# =====================================================
# 용도: pre-commit hook을 자동으로 설치
# 실행: ./scripts/setup-hooks.sh
# =====================================================

set -e  # Exit on error

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# =====================================================
# Helper Functions
# =====================================================

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# =====================================================
# Main Installation
# =====================================================

echo ""
echo "=========================================="
echo "🔧 Git Hooks 설치"
echo "=========================================="
echo ""

# 0. .git/hooks 디렉토리 생성 (없을 경우)
if [[ ! -d ".git/hooks" ]]; then
    log_warning ".git/hooks directory not found, creating..."
    mkdir -p .git/hooks
    log_success ".git/hooks directory created"
fi

# 1. Hook 디렉토리 및 파일 확인
if [[ -d "config/hooks" ]]; then
    HOOKS_DIR="config/hooks"
    HOOKS_RELATIVE="../../config/hooks"
elif [[ -d ".claude/hooks" ]]; then
    HOOKS_DIR=".claude/hooks"
    HOOKS_RELATIVE="../../.claude/hooks"
else
    log_error "Hook directory not found!"
    echo ""
    echo "Please create one of the following directories and add hook files:"
    echo "  - ${GREEN}config/hooks/${NC} (for fileflow/crawlinghub style projects)"
    echo "  - ${GREEN}.claude/hooks/${NC} (for claude-spring-standards style projects)"
    echo ""
    echo "Required files:"
    echo "  - pre-commit (code validation)"
    echo ""
    exit 1
fi

# 2. pre-commit hook 설치
if [[ -f "$HOOKS_DIR/pre-commit" ]]; then
    log_info "Installing pre-commit hook..."

    if [[ -f ".git/hooks/pre-commit" ]] && [[ ! -L ".git/hooks/pre-commit" ]]; then
        log_warning "Existing pre-commit hook found (not a symlink)"
        read -p "   Overwrite? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_error "Installation cancelled"
            exit 1
        fi
        rm .git/hooks/pre-commit
    fi

    ln -sf "$HOOKS_RELATIVE/pre-commit" .git/hooks/pre-commit
    chmod +x "$HOOKS_DIR/pre-commit"
    log_success "pre-commit hook installed"
else
    log_error "pre-commit hook not found in $HOOKS_DIR/"
    echo ""
    echo "Please ensure pre-commit hook file exists."
    exit 1
fi

# 3. 설치 확인
echo ""
log_info "Verifying installation..."

if [[ -L ".git/hooks/pre-commit" ]]; then
    log_success "pre-commit hook is properly linked"
else
    log_error "Hook installation verification failed"
    exit 1
fi

# =====================================================
# Summary
# =====================================================

echo ""
echo "=========================================="
echo "✨ 설치 완료!"
echo "=========================================="
echo ""
echo "설치된 Hooks:"
echo "  ✅ pre-commit → 코드 품질 검증 (ArchUnit + Gradle)"
echo ""
echo "동작 방식:"
echo "  git commit 전 → pre-commit이 코드 검증"
echo ""
echo "다음 단계:"
echo "  git commit 테스트!"
echo ""
