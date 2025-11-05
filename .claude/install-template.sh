#!/bin/bash

# =====================================================
# Claude Spring Standards Template Installer v2.3
# =====================================================
#
# 이 스크립트는 Claude Code Dynamic Hooks + Cache 시스템을
# 다른 Spring Boot 프로젝트로 복사하고 초기화합니다.
#
# 사용법:
#   bash install-template.sh /path/to/target-project
#
# v2.3 추가 항목:
# - Claude Skills (5개 전문가 에이전트: convention-reviewer, domain-expert, rest-api-expert, application-expert, test-expert)
#
# v2.2 항목:
# - ArchUnit 테스트 템플릿 자동 생성 ({{BASE_PACKAGE}} 치환)
#
# v2.1 항목:
# - .cursorrules (Cursor IDE 통합)
# - .env.example (LangFuse 설정 템플릿)
# - langfuse/ (메트릭 추적 스크립트)
# - config/ (Git Pre-commit Hooks, Checkstyle 등)
# - DEVELOPMENT_GUIDE.md (개발 가이드)
#
# =====================================================

set -e  # 에러 발생 시 즉시 종료

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로고
cat << 'EOF'

  ____  _                   _       ____            _             ____  _                  _               _
 / ___|| |_ __ _ _ __   __| | __ _|  _ \ ___  ___| |_   __   _/ ___|| |_ __ _ _ __   __| | __ _ _ __ __| |___
 \___ \| __/ _` | '_ \ / _` |/ _` | |_) / _ \/ __| __| / /  / \___ \| __/ _` | '_ \ / _` |/ _` | '__/ _` / __|
  ___) | || (_| | | | | (_| | (_| |  _ <  __/\__ \ |_ / /  /   ___) | || (_| | | | | (_| | (_| | | | (_| \__ \
 |____/ \__\__,_|_| |_|\__,_|\__,_|_| \_\___||___/\__/_/  /   |____/ \__\__,_|_| |_|\__,_|\__,_|_|  \__,_|___/

  Template Installer v2.3
  Dynamic Hooks + Cache System (100% Zero-Tolerance)
  + Claude Skills + ArchUnit + Cursor IDE + LangFuse + Git Hooks

EOF

# =====================================================
# 1. 인자 검증
# =====================================================

if [[ $# -ne 1 ]]; then
    echo -e "${RED}❌ 사용법: bash install-template.sh /path/to/target-project${NC}"
    exit 1
fi

TARGET_DIR="$1"
SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${BLUE}📁 Source: $SOURCE_DIR${NC}"
echo -e "${BLUE}🎯 Target: $TARGET_DIR${NC}"
echo ""

# 타겟 디렉토리 확인
if [[ ! -d "$TARGET_DIR" ]]; then
    echo -e "${RED}❌ 타겟 디렉토리가 존재하지 않습니다: $TARGET_DIR${NC}"
    exit 1
fi

# 타겟 프로젝트가 Spring Boot인지 확인 (선택적)
if [[ ! -f "$TARGET_DIR/build.gradle" && ! -f "$TARGET_DIR/build.gradle.kts" && ! -f "$TARGET_DIR/pom.xml" ]]; then
    echo -e "${YELLOW}⚠️  경고: 타겟 디렉토리에 Gradle/Maven 빌드 파일이 없습니다.${NC}"
    echo -e "${YELLOW}   계속하시겠습니까? (y/N)${NC}"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo -e "${RED}설치 중단${NC}"
        exit 1
    fi
fi

# =====================================================
# 2. 의존성 확인
# =====================================================

echo -e "${BLUE}🔍 Step 1/12: 의존성 확인${NC}"
echo "-----------------------------------"

# Python 3 확인
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Python 3가 설치되어 있지 않습니다.${NC}"
    echo "   설치: brew install python3 (macOS) 또는 apt-get install python3 (Ubuntu)"
    exit 1
fi
echo -e "${GREEN}✅ Python 3: $(python3 --version)${NC}"

# jq 확인
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  jq가 설치되어 있지 않습니다. (선택적, Hook 로깅에 사용)${NC}"
    echo "   설치: brew install jq (macOS) 또는 apt-get install jq (Ubuntu)"
else
    echo -e "${GREEN}✅ jq: $(jq --version)${NC}"
fi

# gh CLI 확인 (선택적)
if command -v gh &> /dev/null; then
    echo -e "${GREEN}✅ GitHub CLI: $(gh --version | head -1)${NC}"
else
    echo -e "${YELLOW}⚠️  GitHub CLI가 설치되어 있지 않습니다. (선택적, /ai-review 명령어에 사용)${NC}"
fi

echo ""

# =====================================================
# 3. .claude/ 디렉토리 복사
# =====================================================

echo -e "${BLUE}🔧 Step 2/12: .claude/ 디렉토리 복사${NC}"
echo "-----------------------------------"

if [[ -d "$TARGET_DIR/.claude" ]]; then
    echo -e "${YELLOW}⚠️  타겟 프로젝트에 이미 .claude/ 디렉토리가 존재합니다.${NC}"
    echo -e "${YELLOW}   덮어쓰시겠습니까? (y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}백업 생성: $TARGET_DIR/.claude.backup$(date +%Y%m%d_%H%M%S)${NC}"
        mv "$TARGET_DIR/.claude" "$TARGET_DIR/.claude.backup$(date +%Y%m%d_%H%M%S)"
    else
        echo -e "${RED}설치 중단${NC}"
        exit 1
    fi
fi

echo "복사 중..."
cp -r "$SOURCE_DIR/.claude" "$TARGET_DIR/.claude"

# .env 파일은 제외 (프로젝트별 설정)
if [[ -f "$TARGET_DIR/.claude/.env" ]]; then
    rm "$TARGET_DIR/.claude/.env"
fi

echo -e "${GREEN}✅ .claude/ 디렉토리 복사 완료${NC}"
echo ""

# =====================================================
# 4. .cursorrules 복사 (Cursor IDE 통합) ⭐ NEW
# =====================================================

echo -e "${BLUE}🎨 Step 3/12: .cursorrules 복사 (Cursor IDE 통합)${NC}"
echo "-----------------------------------"

if [[ -f "$SOURCE_DIR/.cursorrules" ]]; then
    cp "$SOURCE_DIR/.cursorrules" "$TARGET_DIR/.cursorrules"
    echo -e "${GREEN}✅ .cursorrules 복사 완료 (Cursor IDE가 자동으로 규칙 로드)${NC}"
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 .cursorrules가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 5. .env.example 복사 (LangFuse 설정 템플릿) ⭐ NEW
# =====================================================

echo -e "${BLUE}📝 Step 4/12: .env.example 복사 (LangFuse 설정 템플릿)${NC}"
echo "-----------------------------------"

if [[ -f "$SOURCE_DIR/.env.example" ]]; then
    cp "$SOURCE_DIR/.env.example" "$TARGET_DIR/.env.example"
    echo -e "${GREEN}✅ .env.example 복사 완료${NC}"
    echo "   사용자가 .env.example을 복사하여 .env 생성 가능"
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 .env.example이 없습니다.${NC}"
fi

echo ""

# =====================================================
# 6. Hook 스크립트 권한 설정
# =====================================================

echo -e "${BLUE}🔑 Step 5/12: Hook 스크립트 권한 설정${NC}"
echo "-----------------------------------"

chmod +x "$TARGET_DIR/.claude/hooks"/*.sh
chmod +x "$TARGET_DIR/.claude/hooks/scripts"/*.sh
chmod +x "$TARGET_DIR/.claude/hooks/scripts"/*.py

echo -e "${GREEN}✅ 권한 설정 완료${NC}"
echo ""

# =====================================================
# 7. 환경 변수 설정 안내
# =====================================================

echo -e "${BLUE}🌍 Step 6/12: 환경 변수 설정${NC}"
echo "-----------------------------------"

if [[ ! -f "$TARGET_DIR/.env" ]]; then
    echo "타겟 프로젝트에 .env 파일이 없습니다."
    echo ""
    echo -e "${YELLOW}LangFuse 자격 증명을 입력하시겠습니까? (y/N)${NC}"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo ""
        echo "LangFuse Public Key를 입력하세요 (예: pk-lf-...):"
        read -r LANGFUSE_PUBLIC_KEY

        echo "LangFuse Secret Key를 입력하세요 (예: sk-lf-...):"
        read -r LANGFUSE_SECRET_KEY

        echo "LangFuse Host를 입력하세요 (기본값: https://us.cloud.langfuse.com):"
        read -r LANGFUSE_HOST
        LANGFUSE_HOST="${LANGFUSE_HOST:-https://us.cloud.langfuse.com}"

        # .env 파일 생성
        cat > "$TARGET_DIR/.env" << EOF
# =====================================================
# LangFuse Configuration
# =====================================================

# LangFuse API Keys
LANGFUSE_PUBLIC_KEY=$LANGFUSE_PUBLIC_KEY
LANGFUSE_SECRET_KEY=$LANGFUSE_SECRET_KEY

# LangFuse Host
LANGFUSE_HOST=$LANGFUSE_HOST

# 실시간 자동 업로드 (비활성화 권장)
# LANGFUSE_AUTO_UPLOAD=false

EOF

        echo -e "${GREEN}✅ .env 파일 생성 완료${NC}"
    else
        echo -e "${YELLOW}⚠️  .env 파일을 수동으로 생성하세요:${NC}"
        echo "   cp $TARGET_DIR/.env.example $TARGET_DIR/.env"
        echo "   vim $TARGET_DIR/.env"
    fi
else
    echo -e "${GREEN}✅ 기존 .env 파일 사용${NC}"
fi

echo ""

# =====================================================
# 8. docs/coding_convention/ 복사 (선택적)
# =====================================================

echo -e "${BLUE}📚 Step 7/12: 코딩 컨벤션 규칙 복사 (필수)${NC}"
echo "-----------------------------------"

if [[ -d "$SOURCE_DIR/docs/coding_convention" ]]; then
    echo -e "${YELLOW}소스 프로젝트의 docs/coding_convention/을 복사하시겠습니까?${NC}"
    echo "   (타겟 프로젝트에 맞게 수정 가능)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        mkdir -p "$TARGET_DIR/docs"
        cp -r "$SOURCE_DIR/docs/coding_convention" "$TARGET_DIR/docs/coding_convention"
        echo -e "${GREEN}✅ 코딩 컨벤션 복사 완료${NC}"
    else
        echo -e "${YELLOW}⚠️  docs/coding_convention/ 폴더가 없으면 Cache를 빌드할 수 없습니다.${NC}"
        echo "   타겟 프로젝트에 맞는 규칙을 직접 작성하세요."
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 docs/coding_convention/ 폴더가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 9. langfuse/ 디렉토리 복사 (선택적) ⭐ NEW
# =====================================================

echo -e "${BLUE}📊 Step 8/12: LangFuse 통합 스크립트 복사 (선택적)${NC}"
echo "-----------------------------------"

if [[ -d "$SOURCE_DIR/langfuse" ]]; then
    echo -e "${YELLOW}LangFuse 통합 스크립트를 복사하시겠습니까?${NC}"
    echo "   (Hook 로그 모니터링, A/B 테스트, 메트릭 추적)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        cp -r "$SOURCE_DIR/langfuse" "$TARGET_DIR/"
        echo -e "${GREEN}✅ LangFuse 통합 스크립트 복사 완료${NC}"
        echo "   - langfuse/scripts/aggregate-logs.py"
        echo "   - langfuse/scripts/upload-to-langfuse.py"
        echo "   - langfuse/scripts/monitor.sh"
    else
        echo -e "${YELLOW}⚠️  LangFuse 통합을 건너뜁니다.${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 langfuse/ 폴더가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 10. config/ 디렉토리 복사 (선택적) ⭐ NEW
# =====================================================

echo -e "${BLUE}🔧 Step 9/12: config/ 디렉토리 복사 (선택적)${NC}"
echo "-----------------------------------"

if [[ -d "$SOURCE_DIR/config" ]]; then
    echo -e "${YELLOW}config/ 디렉토리를 복사하시겠습니까?${NC}"
    echo "   (Git Pre-commit Hooks, Checkstyle, PMD, SpotBugs 설정)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        cp -r "$SOURCE_DIR/config" "$TARGET_DIR/"
        echo -e "${GREEN}✅ config/ 디렉토리 복사 완료${NC}"

        # Git Hook 설치 안내
        if [[ -d "$TARGET_DIR/config/hooks" ]]; then
            echo ""
            echo -e "${BLUE}📋 Git Pre-commit Hook 설치 방법:${NC}"
            echo "   cd $TARGET_DIR"
            echo "   cp config/hooks/pre-commit .git/hooks/pre-commit"
            echo "   chmod +x .git/hooks/pre-commit"
        fi
    else
        echo -e "${YELLOW}⚠️  config/ 디렉토리 복사를 건너뜁니다.${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 config/ 폴더가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 11. ArchUnit 테스트 자동 생성 ⭐ NEW v2.2
# =====================================================

echo -e "${BLUE}🧪 Step 10/12: ArchUnit 테스트 자동 생성 (Zero-Tolerance 검증)${NC}"
echo "-----------------------------------"

if [[ -d "$SOURCE_DIR/.claude/templates/archunit" ]]; then
    echo -e "${YELLOW}ArchUnit 테스트를 자동 생성하시겠습니까?${NC}"
    echo "   (Zero-Tolerance 규칙 자동 검증: Lombok 금지, Transaction 경계, Long FK 전략 등)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo ""
        echo "ArchUnit 테스트 생성 중..."

        # generate-archunit-tests.sh 실행
        if [[ -f "$SOURCE_DIR/.claude/commands/lib/generate-archunit-tests.sh" ]]; then
            bash "$SOURCE_DIR/.claude/commands/lib/generate-archunit-tests.sh" \
                 "$SOURCE_DIR/.claude/templates/archunit" \
                 "$TARGET_DIR"

            if [[ $? -eq 0 ]]; then
                echo ""
                echo -e "${GREEN}✅ ArchUnit 테스트 생성 완료${NC}"
                echo ""
                echo -e "${BLUE}📊 생성된 테스트:${NC}"
                echo "   • Bootstrap 모듈: 5개 핵심 테스트"
                echo "     - ZeroToleranceArchitectureTest (Lombok, Transaction, Orchestration)"
                echo "     - JpaEntityConventionTest (Long FK, Static Factory Methods)"
                echo "     - HexagonalArchitectureTest (Layer 의존성)"
                echo "     - MapperConventionTest (Utility class, @Component 금지)"
                echo "     - OrchestrationConventionTest (@Async, Command Record, Outcome)"
                echo ""
                echo -e "${BLUE}🔄 다음 단계:${NC}"
                echo "   1. ./gradlew test 실행하여 ArchUnit 테스트 검증"
                echo "   2. 위반 사항 확인 및 코드 수정"
                echo "   3. 빌드 파이프라인에 ArchUnit 테스트 통합"
            else
                echo ""
                echo -e "${RED}❌ ArchUnit 테스트 생성 실패${NC}"
                echo "   수동 생성 방법:"
                echo "   bash $SOURCE_DIR/.claude/commands/lib/generate-archunit-tests.sh \\"
                echo "        $SOURCE_DIR/.claude/templates/archunit \\"
                echo "        $TARGET_DIR"
            fi
        else
            echo -e "${RED}❌ generate-archunit-tests.sh 스크립트를 찾을 수 없습니다.${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  ArchUnit 테스트 생성을 건너뜁니다.${NC}"
        echo "   수동 생성 방법:"
        echo "   bash $SOURCE_DIR/.claude/commands/lib/generate-archunit-tests.sh \\"
        echo "        $SOURCE_DIR/.claude/templates/archunit \\"
        echo "        $TARGET_DIR"
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 ArchUnit 템플릿이 없습니다.${NC}"
    echo "   위치: $SOURCE_DIR/.claude/templates/archunit/"
fi

echo ""

# =====================================================
# 12. Claude Skills 복사 ⭐ NEW v2.3
# =====================================================

echo -e "${BLUE}🎓 Step 11/12: Claude Skills 복사 (컨벤션 전문가)${NC}"
echo "-----------------------------------"

if [[ -d "$SOURCE_DIR/.claude/skills" ]]; then
    echo -e "${YELLOW}Claude Skills를 복사하시겠습니까?${NC}"
    echo "   (convention-reviewer, domain-expert, rest-api-expert, application-expert, test-expert)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        mkdir -p "$TARGET_DIR/.claude/skills"
        cp -r "$SOURCE_DIR/.claude/skills"/* "$TARGET_DIR/.claude/skills/"
        echo -e "${GREEN}✅ Claude Skills 복사 완료${NC}"
        echo ""
        echo -e "${BLUE}📋 복사된 Skills:${NC}"
        echo "   • convention-reviewer: 컨벤션 리뷰 및 리팩토링 TODO 생성"
        echo "   • rest-api-expert: REST API Layer 전문가"
        echo "   • domain-expert: Domain Layer 전문가"
        echo "   • application-expert: Application Layer 전문가"
        echo "   • test-expert: 테스트 전문가"
        echo ""
        echo -e "${BLUE}💡 사용 방법:${NC}"
        echo "   Claude Code에서 자연어로 Skills 호출:"
        echo "   \"convention-reviewer로 프로젝트 스캔하고 TODO 생성해줘\""
        echo "   \"Order Domain을 생성해줘\" (domain-expert 자동 활성화)"
    else
        echo -e "${YELLOW}⚠️  Claude Skills 복사를 건너뜁니다.${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 .claude/skills/ 폴더가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 13. DEVELOPMENT_GUIDE.md 복사 (선택적) ⭐ NEW
# =====================================================

echo -e "${BLUE}📖 Step 12/12: DEVELOPMENT_GUIDE.md 복사 (선택적)${NC}"
echo "-----------------------------------"

if [[ -f "$SOURCE_DIR/DEVELOPMENT_GUIDE.md" ]]; then
    echo -e "${YELLOW}DEVELOPMENT_GUIDE.md를 복사하시겠습니까?${NC}"
    echo "   (타겟 프로젝트에 맞게 수정 필요)"
    echo "   (y/N)"
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        cp "$SOURCE_DIR/DEVELOPMENT_GUIDE.md" "$TARGET_DIR/DEVELOPMENT_GUIDE.md"
        echo -e "${GREEN}✅ DEVELOPMENT_GUIDE.md 복사 완료${NC}"
        echo "   ⚠️  프로젝트 이름 등을 수정하세요."
    else
        echo -e "${YELLOW}⚠️  DEVELOPMENT_GUIDE.md 복사를 건너뜁니다.${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  소스 프로젝트에 DEVELOPMENT_GUIDE.md가 없습니다.${NC}"
fi

echo ""

# =====================================================
# 14. Cache 빌드
# =====================================================

echo -e "${BLUE}🏗️  추가: Cache 빌드${NC}"
echo "-----------------------------------"

cd "$TARGET_DIR"

# Cache 빌드
if [[ -d "$TARGET_DIR/docs/coding_convention" ]]; then
    echo "Cache 빌드 중..."
    python3 .claude/hooks/scripts/build-rule-cache.py

    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Cache 빌드 완료${NC}"
        echo ""
        echo -e "${BLUE}📊 A/B 테스트 검증 결과:${NC}"
        echo "   - Hook ON:  0 violations (100% Zero-Tolerance)"
        echo "   - Hook OFF: 40 violations (0% Zero-Tolerance)"
        echo "   - 효과: 컨벤션 준수율 100% 달성"
    else
        echo -e "${RED}❌ Cache 빌드 실패${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  docs/coding_convention/ 폴더가 없어 Cache를 빌드할 수 없습니다.${NC}"
fi

echo ""

# =====================================================
# 15. 설치 완료 및 다음 단계 안내
# =====================================================

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ 설치 완료! (v2.3)${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📦 복사된 항목:${NC}"
echo "   ✅ .claude/ (Dynamic Hooks + Cache 시스템)"
echo "   ✅ .cursorrules (Cursor IDE 통합)"
echo "   ✅ .env.example (LangFuse 설정 템플릿)"
echo "   ✅ docs/coding_convention/ (98개 규칙)"
echo "   ✅ Claude Skills (5개 전문가 에이전트) - 선택 시 ⭐ NEW v2.3"
echo "   ✅ ArchUnit 테스트 (5개 핵심 규칙 자동 검증) - 선택 시"
echo "   ✅ langfuse/ (메트릭 추적 스크립트) - 선택 시"
echo "   ✅ config/ (Git Hooks, Checkstyle 등) - 선택 시"
echo "   ✅ DEVELOPMENT_GUIDE.md - 선택 시"
echo ""
echo -e "${BLUE}📋 다음 단계:${NC}"
echo ""
echo "1. 환경 변수 로드:"
echo "   source $TARGET_DIR/.env"
echo ""
echo "2. Claude Code 실행:"
echo "   cd $TARGET_DIR"
echo "   claude code"
echo ""
echo "3. 첫 코드 생성 테스트:"
echo "   /code-gen-domain Order"
echo "   (자동 규칙 주입 + 실시간 검증)"
echo ""
echo -e "${BLUE}🎨 Cursor IDE 사용 시:${NC}"
echo "   1. Cursor IDE로 프로젝트 열기"
echo "   2. .cursorrules 자동 로드 확인"
echo "   3. AI에게 \"Order Aggregate 생성\" 요청"
echo "   4. 규칙이 자동으로 적용된 코드 생성 확인"
echo ""
echo -e "${BLUE}📚 참고 문서:${NC}"
echo "   - 사용 가이드: $TARGET_DIR/.claude/README.md"
echo "   - 개발 가이드: $TARGET_DIR/DEVELOPMENT_GUIDE.md"
echo "   - Dynamic Hooks: $TARGET_DIR/docs/DYNAMIC_HOOKS_GUIDE.md (작성 권장)"
echo "   - LangFuse 통합: $TARGET_DIR/langfuse/README.md (복사한 경우)"
echo ""
echo -e "${YELLOW}⚠️  중요:${NC}"
echo "   - .env 파일은 절대 Git에 커밋하지 마세요!"
echo "   - .gitignore에 .env가 포함되어 있는지 확인하세요."
echo "   - Git Pre-commit Hook을 설치하면 Transaction 경계를 자동 검증합니다."
echo ""

# .gitignore 확인
if [[ -f "$TARGET_DIR/.gitignore" ]]; then
    if ! grep -q "^\.env$" "$TARGET_DIR/.gitignore"; then
        echo -e "${YELLOW}⚠️  .gitignore에 .env 추가 중...${NC}"
        echo ".env" >> "$TARGET_DIR/.gitignore"
        echo -e "${GREEN}✅ .gitignore 업데이트 완료${NC}"
    fi
fi

echo ""
echo -e "${BLUE}🧪 ArchUnit 테스트 (NEW v2.2):${NC}"
echo "   • Zero-Tolerance 규칙을 빌드 시점에 자동 검증"
echo "   • Lombok 금지, Law of Demeter, Long FK 전략, Transaction 경계 등"
echo "   • ./gradlew test 실행 시 위반 사항 즉시 발견"
echo ""
echo -e "${BLUE}🎓 Claude Skills 사용법 (NEW v2.3):${NC}"
echo "   • Claude Code에서 자연어로 호출:"
echo "     \"convention-reviewer로 프로젝트를 스캔하고 리팩토링 TODO를 생성해줘\""
echo "     \"Order Domain을 생성해줘\" (domain-expert 자동 활성화)"
echo "     \"PlaceOrderUseCase를 생성해줘\" (application-expert 자동 활성화)"
echo "   • Skills는 자동으로 인식되며 별도 설정 불필요"
echo ""
echo -e "${GREEN}🎉 Happy coding with Claude Spring Standards v2.3!${NC}"
echo ""
