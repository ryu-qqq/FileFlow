#!/bin/bash

# =====================================================
# Git Hooks 자동 설치 스크립트
# =====================================================
# 용도: pre-commit + post-commit hooks를 자동으로 설치
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

# 1. pre-commit hook 설치
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

ln -sf ../../config/hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
chmod +x config/hooks/pre-commit

log_success "pre-commit hook installed"

# 2. post-commit hook 설치
log_info "Installing post-commit hook..."

if [[ -f ".git/hooks/post-commit" ]] && [[ ! -L ".git/hooks/post-commit" ]]; then
    log_warning "Existing post-commit hook found (not a symlink)"
    read -p "   Overwrite? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "Installation cancelled"
        exit 1
    fi
    rm .git/hooks/post-commit
fi

ln -sf ../../config/hooks/post-commit .git/hooks/post-commit
chmod +x .git/hooks/post-commit
chmod +x config/hooks/post-commit

log_success "post-commit hook installed"

# 3. 설치 확인
echo ""
log_info "Verifying installation..."

if [[ -L ".git/hooks/pre-commit" ]] && [[ -L ".git/hooks/post-commit" ]]; then
    log_success "Both hooks are properly linked"
else
    log_error "Hook installation verification failed"
    exit 1
fi

# 4. LangFuse 의존성 확인
echo ""
log_info "Checking LangFuse dependencies..."

# Python langfuse 패키지 확인
if python3 -c "import langfuse" 2>/dev/null; then
    log_success "langfuse package is installed"
else
    log_warning "langfuse package NOT installed"
    echo ""
    echo "   LangFuse 메트릭 수집을 사용하려면 다음 명령을 실행하세요:"
    echo ""
    echo "   ${GREEN}pip3 install langfuse${NC}"
    echo ""
    echo "   (선택사항: LangFuse 없이도 JSONL 로그는 작동합니다)"
    echo ""
fi

# .env 파일 확인
if [[ -f ".env" ]]; then
    log_success ".env file exists"

    # 환경 변수 확인
    if grep -q "LANGFUSE_PUBLIC_KEY" .env && grep -q "LANGFUSE_SECRET_KEY" .env; then
        log_success "LangFuse environment variables configured"
    else
        log_warning "LangFuse environment variables NOT configured in .env"
        echo ""
        echo "   .env 파일에 다음 변수를 추가하세요:"
        echo ""
        echo "   ${GREEN}LANGFUSE_PUBLIC_KEY=pk-lf-...${NC}"
        echo "   ${GREEN}LANGFUSE_SECRET_KEY=sk-lf-...${NC}"
        echo "   ${GREEN}LANGFUSE_HOST=https://us.cloud.langfuse.com${NC}"
        echo ""
    fi
else
    log_warning ".env file NOT found"
    echo ""
    echo "   LangFuse 클라우드 업로드를 사용하려면 .env 파일을 생성하세요:"
    echo ""
    echo "   ${GREEN}cp .env.example .env${NC}"
    echo ""
    echo "   그리고 API 키를 입력하세요 (https://us.cloud.langfuse.com)"
    echo ""
    echo "   (선택사항: .env 없이도 JSONL 로그는 작동합니다)"
    echo ""
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
echo "  ✅ pre-commit  → 코드 품질 검증 (ArchUnit + Gradle)"
echo "  ✅ post-commit → TDD 메트릭 수집 (LangFuse)"
echo ""
echo "동작 방식:"
echo "  1. git commit 전 → pre-commit이 코드 검증"
echo "  2. git commit 후 → post-commit이 메트릭 수집"
echo ""
echo "메트릭 로그 위치:"
echo "  📁 ~/.claude/logs/tdd-cycle.jsonl (항상 작동)"
echo "  ☁️  LangFuse Cloud (환경 변수 설정 시)"
echo ""
echo "다음 단계:"
echo "  1. LangFuse 사용 원하면: pip3 install langfuse"
echo "  2. .env 파일 생성 (선택사항)"
echo "  3. git commit 테스트!"
echo ""
