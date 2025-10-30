#!/bin/bash

# =====================================================
# Spring Standards 완전 통합 설치 스크립트
# Claude + Windsurf + Docs + CodeRabbit + Scripts + LangFuse
# =====================================================

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# y/N 입력 검증 함수
ask_yes_no() {
    local prompt="$1"
    local reply

    while true; do
        read -p "$prompt (y/N): " -n 1 -r reply
        echo ""

        case "$reply" in
            [Yy])
                return 0  # Yes
                ;;
            [Nn]|"")
                return 1  # No (기본값)
                ;;
            *)
                echo -e "${RED}❌ 잘못된 입력입니다. y 또는 N을 입력하세요.${NC}"
                ;;
        esac
    done
}

# 현재 스크립트 위치
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_PROJECT="$(dirname "$SCRIPT_DIR")"
TARGET_PROJECT="$(pwd)"

echo -e "${CYAN}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║  🚀 Spring Standards 완전 통합 설치 시스템          ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}이 스크립트는 다음을 모두 설치합니다:${NC}"
echo "  ✅ Claude Code (Hooks + Cache + Commands + Serena)"
echo "  ✅ Windsurf/Cascade (Rules + Workflows + Templates)"
echo "  ✅ Coding Convention Docs (90+ 규칙)"
echo "  ✅ CodeRabbit 설정 (.coderabbit.yaml)"
echo "  ✅ Scripts (Pipeline, LangFuse)"
echo "  ✅ Git Hooks (Pre-commit 검증)"
echo "  ✅ Tools (Gradle 설정, ArchUnit)"
echo ""

# 설치 대상 디렉토리 확인
echo -e "${YELLOW}설치 대상 디렉토리:${NC} $TARGET_PROJECT"
echo ""

if ! ask_yes_no "계속하시겠습니까?"; then
    echo -e "${RED}❌ 설치를 취소합니다.${NC}"
    exit 1
fi

echo ""
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 1/7 Claude Code 설치${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 기존 설치 확인 및 백업
if [[ -d "$TARGET_PROJECT/.claude" ]]; then
    echo -e "${YELLOW}⚠️  기존 .claude/ 디렉토리가 존재합니다.${NC}"

    if ask_yes_no "백업 후 덮어쓰시겠습니까?"; then
        BACKUP_DIR="$TARGET_PROJECT/.claude.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.claude" "$BACKUP_DIR"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_DIR${NC}"
        echo ""
    else
        echo -e "${RED}❌ 설치를 취소합니다.${NC}"
        exit 1
    fi
fi

# 디렉토리 구조 생성
echo -e "${BLUE}📁 디렉토리 구조 생성...${NC}"
mkdir -p "$TARGET_PROJECT/.claude/hooks/scripts"
mkdir -p "$TARGET_PROJECT/.claude/hooks/logs"
mkdir -p "$TARGET_PROJECT/.claude/cache/rules"
mkdir -p "$TARGET_PROJECT/.claude/commands/lib"
mkdir -p "$TARGET_PROJECT/.claude/commands/cc"

# 1.1. Claude Hooks 복사
echo -e "${BLUE}📋 Hooks 복사...${NC}"
cp "$SOURCE_PROJECT/.claude/hooks/user-prompt-submit.sh" "$TARGET_PROJECT/.claude/hooks/"
cp "$SOURCE_PROJECT/.claude/hooks/after-tool-use.sh" "$TARGET_PROJECT/.claude/hooks/"
chmod +x "$TARGET_PROJECT/.claude/hooks/"*.sh

# 1.2. Scripts 복사
echo -e "${BLUE}📋 Scripts 복사...${NC}"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/log-helper.py" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/view-logs.sh" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/validation-helper.py" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/build-rule-cache.py" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/init-session.sh" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/preserve-rules.sh" "$TARGET_PROJECT/.claude/hooks/scripts/"
cp "$SOURCE_PROJECT/.claude/hooks/scripts/setup-serena-conventions.sh" "$TARGET_PROJECT/.claude/hooks/scripts/"
chmod +x "$TARGET_PROJECT/.claude/hooks/scripts/"*.{sh,py} 2>/dev/null || true

# 1.3. Commands 복사
echo -e "${BLUE}📋 Commands 복사...${NC}"
cp "$SOURCE_PROJECT/.claude/commands/README.md" "$TARGET_PROJECT/.claude/commands/"
cp "$SOURCE_PROJECT/.claude/commands/lib/inject-rules.py" "$TARGET_PROJECT/.claude/commands/lib/"
chmod +x "$TARGET_PROJECT/.claude/commands/lib/"*.py

# 모든 명령어 파일 복사 (존재하는 파일만)
for cmd_file in "$SOURCE_PROJECT/.claude/commands/"*.md; do
    if [[ -f "$cmd_file" ]]; then
        cp "$cmd_file" "$TARGET_PROJECT/.claude/commands/"
    fi
done

# cc 네임스페이스 (Coding Convention)
if [[ -d "$SOURCE_PROJECT/.claude/commands/cc" ]]; then
    for cc_file in "$SOURCE_PROJECT/.claude/commands/cc/"*.md; do
        if [[ -f "$cc_file" ]]; then
            cp "$cc_file" "$TARGET_PROJECT/.claude/commands/cc/"
        fi
    done
fi

# 1.4. CLAUDE.md 복사
echo -e "${BLUE}📋 CLAUDE.md 복사...${NC}"
cp "$SOURCE_PROJECT/.claude/CLAUDE.md" "$TARGET_PROJECT/.claude/"
echo -e "${YELLOW}⚠️  프로젝트에 맞게 .claude/CLAUDE.md를 수정하세요!${NC}"

# 1.5. README 복사
cp "$SOURCE_PROJECT/.claude/hooks/logs/README.md" "$TARGET_PROJECT/.claude/hooks/logs/"

echo -e "${GREEN}✅ Claude Code 설치 완료${NC}"
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 2/7 Windsurf/Cascade 설치${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ ! -d "$SOURCE_PROJECT/.windsurf" ]]; then
    echo -e "${YELLOW}⚠️  소스 프로젝트에 .windsurf/ 디렉토리가 없습니다. 건너뜁니다.${NC}"
else
    # 기존 백업
    if [[ -d "$TARGET_PROJECT/.windsurf" ]]; then
        echo -e "${YELLOW}⚠️  기존 .windsurf/ 디렉토리를 백업합니다.${NC}"
        BACKUP_WINDSURF="$TARGET_PROJECT/.windsurf.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.windsurf" "$BACKUP_WINDSURF"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_WINDSURF${NC}"
    fi

    # 디렉토리 생성
    mkdir -p "$TARGET_PROJECT/.windsurf/rules"
    mkdir -p "$TARGET_PROJECT/.windsurf/workflows"
    mkdir -p "$TARGET_PROJECT/.windsurf/templates"

    # 파일 복사
    echo -e "${BLUE}📋 Windsurf 파일 복사...${NC}"
    cp "$SOURCE_PROJECT/.windsurf/README.md" "$TARGET_PROJECT/.windsurf/"
    cp "$SOURCE_PROJECT/.windsurf/rules/"*.md "$TARGET_PROJECT/.windsurf/rules/"
    cp "$SOURCE_PROJECT/.windsurf/workflows/"*.md "$TARGET_PROJECT/.windsurf/workflows/"

    # Templates 복사 (있으면)
    if [[ -d "$SOURCE_PROJECT/.windsurf/templates" ]]; then
        cp -r "$SOURCE_PROJECT/.windsurf/templates/"* "$TARGET_PROJECT/.windsurf/templates/" 2>/dev/null || true
    fi

    echo -e "${GREEN}✅ Windsurf/Cascade 설치 완료${NC}"
    echo -e "${YELLOW}💡 IntelliJ Cascade에서 .windsurf/rules/*.md를 자동으로 읽습니다${NC}"
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 3/7 Coding Convention Docs 설치${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ ! -d "$SOURCE_PROJECT/docs/coding_convention" ]]; then
    echo -e "${YELLOW}⚠️  소스 프로젝트에 docs/coding_convention/ 디렉토리가 없습니다.${NC}"
    echo -e "${YELLOW}   나중에 수동으로 생성하세요.${NC}"
else
    # 기존 백업
    if [[ -d "$TARGET_PROJECT/docs/coding_convention" ]]; then
        echo -e "${YELLOW}⚠️  기존 docs/coding_convention/ 디렉토리를 백업합니다.${NC}"
        BACKUP_DOCS="$TARGET_PROJECT/docs/coding_convention.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/docs/coding_convention" "$BACKUP_DOCS"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_DOCS${NC}"
    fi

    # 복사
    echo -e "${BLUE}📋 Coding Convention Docs 복사...${NC}"
    mkdir -p "$TARGET_PROJECT/docs"
    cp -r "$SOURCE_PROJECT/docs/coding_convention" "$TARGET_PROJECT/docs/"

    # 다른 docs 파일들도 복사
    cp "$SOURCE_PROJECT/docs/DYNAMIC_HOOKS_GUIDE.md" "$TARGET_PROJECT/docs/" 2>/dev/null || true
    cp "$SOURCE_PROJECT/docs/LANGFUSE_INTEGRATION_GUIDE.md" "$TARGET_PROJECT/docs/" 2>/dev/null || true
    cp "$SOURCE_PROJECT/docs/LANGFUSE_MONITORING_GUIDE.md" "$TARGET_PROJECT/docs/" 2>/dev/null || true
    cp "$SOURCE_PROJECT/docs/LANGFUSE_TELEMETRY_GUIDE.md" "$TARGET_PROJECT/docs/" 2>/dev/null || true
    cp "$SOURCE_PROJECT/docs/USAGE_GUIDE.md" "$TARGET_PROJECT/docs/" 2>/dev/null || true

    echo -e "${GREEN}✅ Coding Convention Docs 설치 완료${NC}"
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 4/7 CodeRabbit 설정 설치${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ -f "$SOURCE_PROJECT/.coderabbit.yaml" ]]; then
    # 기존 백업
    if [[ -f "$TARGET_PROJECT/.coderabbit.yaml" ]]; then
        echo -e "${YELLOW}⚠️  기존 .coderabbit.yaml을 백업합니다.${NC}"
        BACKUP_CR="$TARGET_PROJECT/.coderabbit.yaml.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.coderabbit.yaml" "$BACKUP_CR"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_CR${NC}"
    fi

    echo -e "${BLUE}📋 CodeRabbit 설정 복사...${NC}"
    cp "$SOURCE_PROJECT/.coderabbit.yaml" "$TARGET_PROJECT/"
    echo -e "${GREEN}✅ CodeRabbit 설정 설치 완료${NC}"
    echo -e "${YELLOW}💡 프로젝트에 맞게 .coderabbit.yaml을 수정하세요!${NC}"
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 .coderabbit.yaml이 없습니다. 건너뜁니다.${NC}"
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 5/7 Scripts 설치 (Pipeline + LangFuse)${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ ! -d "$SOURCE_PROJECT/scripts" ]]; then
    echo -e "${YELLOW}⚠️  소스 프로젝트에 scripts/ 디렉토리가 없습니다. 건너뜁니다.${NC}"
else
    # 기존 백업
    if [[ -d "$TARGET_PROJECT/scripts" ]]; then
        echo -e "${YELLOW}⚠️  기존 scripts/ 디렉토리를 백업합니다.${NC}"
        BACKUP_SCRIPTS="$TARGET_PROJECT/scripts.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/scripts" "$BACKUP_SCRIPTS"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_SCRIPTS${NC}"
    fi

    # 복사
    echo -e "${BLUE}📋 Scripts 복사...${NC}"
    mkdir -p "$TARGET_PROJECT/scripts/langfuse"

    # LangFuse 스크립트
    if [[ -d "$SOURCE_PROJECT/scripts/langfuse" ]]; then
        cp "$SOURCE_PROJECT/scripts/langfuse/"*.py "$TARGET_PROJECT/scripts/langfuse/"
        cp "$SOURCE_PROJECT/scripts/langfuse/README.md" "$TARGET_PROJECT/scripts/langfuse/" 2>/dev/null || true
        chmod +x "$TARGET_PROJECT/scripts/langfuse/"*.py
    fi

    # 기타 스크립트
    cp "$SOURCE_PROJECT/scripts/"*.sh "$TARGET_PROJECT/scripts/" 2>/dev/null || true
    chmod +x "$TARGET_PROJECT/scripts/"*.sh 2>/dev/null || true

    echo -e "${GREEN}✅ Scripts 설치 완료${NC}"
fi
echo ""

# Tools 디렉토리 복사
if [[ -d "$SOURCE_PROJECT/tools" ]]; then
    echo -e "${BLUE}📋 Tools 복사...${NC}"

    # 기존 백업
    if [[ -d "$TARGET_PROJECT/tools" ]]; then
        echo -e "${YELLOW}⚠️  기존 tools/ 디렉토리를 백업합니다.${NC}"
        BACKUP_TOOLS="$TARGET_PROJECT/tools.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/tools" "$BACKUP_TOOLS"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_TOOLS${NC}"
    fi

    cp -r "$SOURCE_PROJECT/tools" "$TARGET_PROJECT/"
    chmod +x "$TARGET_PROJECT/tools/pipeline/"*.sh 2>/dev/null || true
    echo -e "${GREEN}✅ Tools 설치 완료${NC}"
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 6/7 Git Hooks 설치${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ ! -d "$TARGET_PROJECT/.git" ]]; then
    echo -e "${YELLOW}⚠️  Git 저장소가 아닙니다 (.git 디렉토리 없음).${NC}"
    echo -e "${YELLOW}   git init 실행 후 수동으로 설치하세요.${NC}"
elif [[ ! -d "$SOURCE_PROJECT/hooks" ]]; then
    echo -e "${YELLOW}⚠️  소스 프로젝트에 hooks/ 디렉토리가 없습니다. 건너뜁니다.${NC}"
else
    # 기존 pre-commit hook 백업
    if [[ -f "$TARGET_PROJECT/.git/hooks/pre-commit" ]] || [[ -L "$TARGET_PROJECT/.git/hooks/pre-commit" ]]; then
        echo -e "${YELLOW}⚠️  기존 pre-commit hook을 백업합니다.${NC}"
        BACKUP_HOOK="$TARGET_PROJECT/.git/hooks/pre-commit.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.git/hooks/pre-commit" "$BACKUP_HOOK"
        echo -e "${GREEN}✅ 백업 완료: $BACKUP_HOOK${NC}"
    fi

    # 프로젝트 루트에 hooks 디렉토리 복사
    echo -e "${BLUE}📋 Git hooks 파일 복사...${NC}"
    mkdir -p "$TARGET_PROJECT/hooks/validators"
    cp "$SOURCE_PROJECT/hooks/pre-commit" "$TARGET_PROJECT/hooks/"
    cp -r "$SOURCE_PROJECT/hooks/validators/"* "$TARGET_PROJECT/hooks/validators/"

    # 실행 권한 부여
    chmod +x "$TARGET_PROJECT/hooks/pre-commit"
    chmod +x "$TARGET_PROJECT/hooks/validators/"*.sh

    # .git/hooks에 심볼릭 링크 생성
    ln -sf "../../hooks/pre-commit" "$TARGET_PROJECT/.git/hooks/pre-commit"

    echo -e "${GREEN}✅ Git Hooks 설치 완료${NC}"
    echo -e "${YELLOW}💡 프로젝트에 맞게 hooks/validators/ 스크립트를 수정하세요!${NC}"
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📦 7/7 의존성 확인 및 Cache 빌드${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Python 확인
echo -e "${BLUE}🐍 Python 의존성 확인...${NC}"
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Python 3가 설치되지 않았습니다.${NC}"
    echo "Python 3를 설치한 후 다시 실행하세요."
    exit 1
fi

# tiktoken 확인
if ! python3 -c "import tiktoken" 2>/dev/null; then
    echo -e "${YELLOW}⚠️  tiktoken이 설치되지 않았습니다.${NC}"

    if ask_yes_no "tiktoken을 설치하시겠습니까?"; then
        pip3 install tiktoken
        echo -e "${GREEN}✅ tiktoken 설치 완료${NC}"
    else
        echo -e "${YELLOW}⚠️  Cache 빌드를 위해 tiktoken이 필요합니다.${NC}"
    fi
fi

# jq 확인
echo -e "${BLUE}🔧 jq 확인...${NC}"
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  jq가 설치되지 않았습니다 (JSON 로그 분석 도구).${NC}"
    echo ""
    echo "jq 설치 방법:"
    echo "  macOS: brew install jq"
    echo "  Ubuntu: sudo apt-get install jq"
    echo ""
else
    echo -e "${GREEN}✅ jq 설치 확인 완료${NC}"
fi
echo ""

# Cache 빌드
if [[ -d "$TARGET_PROJECT/docs/coding_convention" ]]; then
    echo -e "${BLUE}💾 Cache 빌드${NC}"

    if ask_yes_no "지금 Cache를 빌드하시겠습니까?"; then
        cd "$TARGET_PROJECT"
        python3 .claude/hooks/scripts/build-rule-cache.py
        echo -e "${GREEN}✅ Cache 빌드 완료${NC}"
    else
        echo -e "${YELLOW}⚠️  나중에 다음 명령어로 Cache를 빌드하세요:${NC}"
        echo "   python3 .claude/hooks/scripts/build-rule-cache.py"
    fi
else
    echo -e "${YELLOW}⚠️  docs/coding_convention/ 디렉토리가 없어 Cache를 빌드할 수 없습니다.${NC}"
    echo "코딩 규칙 문서를 준비한 후 Cache를 빌드하세요."
fi
echo ""

# 텔레메트리 설정
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📊 텔레메트리 (익명 사용 통계)${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Spring Standards 템플릿 개선을 위해 익명화된 사용 통계를"
echo "수집하도록 허용하시겠습니까?"
echo ""
echo "수집 데이터:"
echo "  ✅ 토큰 사용량 (익명)"
echo "  ✅ 검증 시간 (익명)"
echo "  ✅ 컨벤션 위반 통계 (익명)"
echo "  ❌ 사용자 이름 (수집 안 됨)"
echo "  ❌ 파일 이름 (수집 안 됨)"
echo "  ❌ 코드 내용 (수집 안 됨)"
echo ""

if ask_yes_no "텔레메트리를 활성화하시겠습니까?"; then
    cat > "$TARGET_PROJECT/.langfuse.telemetry" <<'EOF'
enabled=true
public_key=pk-lf-d028249b-630d-4100-8edb-0a4a89d25b0a
secret_key=sk-lf-43cd007f-183b-4fbb-a114-8289da1f327f
host=https://us.cloud.langfuse.com
anonymize=true
EOF
    echo -e "${GREEN}✅ 텔레메트리 활성화 완료${NC}"
    echo -e "${YELLOW}💡 언제든지 비활성화: rm -f .langfuse.telemetry${NC}"
else
    echo -e "${YELLOW}⚠️  텔레메트리를 비활성화했습니다.${NC}"
fi
echo ""

# 완료 메시지
echo -e "${GREEN}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║           ✅ 설치 완료!                              ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}📊 설치된 컴포넌트:${NC}"
echo "  ✅ Claude Code (Hooks + Cache + Commands + Serena)"
if [[ -d "$TARGET_PROJECT/.windsurf" ]]; then
    echo "  ✅ Windsurf/Cascade (Rules + Workflows)"
fi
if [[ -d "$TARGET_PROJECT/docs/coding_convention" ]]; then
    echo "  ✅ Coding Convention Docs (90+ 규칙)"
fi
if [[ -f "$TARGET_PROJECT/.coderabbit.yaml" ]]; then
    echo "  ✅ CodeRabbit 설정"
fi
if [[ -d "$TARGET_PROJECT/scripts" ]]; then
    echo "  ✅ Scripts (Pipeline + LangFuse)"
fi
if [[ -d "$TARGET_PROJECT/tools" ]]; then
    echo "  ✅ Tools (Gradle + Pipeline)"
fi
if [[ -L "$TARGET_PROJECT/.git/hooks/pre-commit" ]]; then
    echo "  ✅ Git Hooks (Pre-commit)"
fi
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📖 다음 단계${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo -e "${YELLOW}1️⃣ 프로젝트별 설정 수정:${NC}"
echo "   - .claude/CLAUDE.md (프로젝트 정보)"
echo "   - docs/coding_convention/ (규칙 추가/수정)"
if [[ -d "$TARGET_PROJECT/hooks" ]]; then
    echo "   - hooks/validators/ (검증 로직)"
fi
if [[ -d "$TARGET_PROJECT/.windsurf" ]]; then
    echo "   - .windsurf/rules/rules.md (Cascade 규칙)"
fi
if [[ -f "$TARGET_PROJECT/.coderabbit.yaml" ]]; then
    echo "   - .coderabbit.yaml (CodeRabbit 설정)"
fi
echo ""

echo -e "${YELLOW}2️⃣ Cache 빌드 (규칙 변경 시):${NC}"
echo "   python3 .claude/hooks/scripts/build-rule-cache.py"
echo ""

echo -e "${YELLOW}3️⃣ Serena 메모리 초기화 (1회만):${NC}"
echo "   bash .claude/hooks/scripts/setup-serena-conventions.sh"
echo "   # 이후 Claude Code에서 /cc:load 실행"
echo ""

echo -e "${YELLOW}4️⃣ Claude Code 사용:${NC}"
echo "   1. 세션 시작: /cc:load"
echo "   2. 작업: /domain, /application 등"
echo "   3. 자동: Layer별 규칙 주입 및 검증"
echo ""

if [[ -d "$TARGET_PROJECT/.windsurf" ]]; then
    echo -e "${YELLOW}5️⃣ IntelliJ Cascade 사용:${NC}"
    echo "   1. IntelliJ에서 Cascade 활성화"
    echo "   2. .windsurf/rules/*.md 자동 로드"
    echo "   3. Boilerplate 빠른 생성"
    echo ""
fi

echo -e "${YELLOW}6️⃣ 로그 확인:${NC}"
echo "   ./.claude/hooks/scripts/view-logs.sh     # 최근 로그"
echo "   ./.claude/hooks/scripts/view-logs.sh -f  # 실시간"
echo "   ./.claude/hooks/scripts/view-logs.sh -s  # 통계"
echo ""

if [[ -L "$TARGET_PROJECT/.git/hooks/pre-commit" ]]; then
    echo -e "${YELLOW}7️⃣ Git pre-commit hooks 테스트:${NC}"
    echo "   git add <file>"
    echo "   git commit -m \"test\""
    echo ""
fi

echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}🎉 Spring Standards 완전 통합 시스템이 준비되었습니다!${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
