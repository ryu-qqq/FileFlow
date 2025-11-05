#!/bin/bash
################################################################################
# ArchUnit Tests Generator
#
# 목적: 템플릿에서 프로젝트별 ArchUnit 테스트 생성
# 사용: generate-archunit-tests.sh <source-template-dir> <target-project-dir>
# 예시: generate-archunit-tests.sh \
#       /path/to/claude-spring-standards/.claude/templates/archunit \
#       /path/to/target-project
################################################################################

set -euo pipefail

# ============================================
# 색상 정의
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================
# Functions
# ============================================

error_exit() {
    echo -e "${RED}❌ Error: $1${NC}" >&2
    exit 1
}

info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

section() {
    echo -e "\n${CYAN}═══════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════${NC}\n"
}

# 템플릿 파일 처리 ({{BASE_PACKAGE}} 치환)
process_template() {
    local template_file="$1"
    local output_file="$2"
    local base_package="$3"

    info "템플릿 처리: $(basename "$template_file")"

    # {{BASE_PACKAGE}}를 실제 패키지로 치환
    sed "s/{{BASE_PACKAGE}}/$base_package/g" "$template_file" > "$output_file"

    success "생성 완료: $output_file"
}

# 디렉토리 생성 (존재하지 않으면)
ensure_dir() {
    local dir="$1"
    if [[ ! -d "$dir" ]]; then
        mkdir -p "$dir"
        info "디렉토리 생성: $dir"
    fi
}

# ============================================
# Main Script
# ============================================

# 인자 검증
if [[ $# -lt 2 ]]; then
    echo "Usage: $0 <source-template-dir> <target-project-dir>"
    echo ""
    echo "Example:"
    echo "  $0 ~/.claude/templates/archunit /path/to/target-project"
    echo ""
    exit 1
fi

TEMPLATE_DIR="$1"
TARGET_PROJECT="$2"

# 경로 검증
[[ ! -d "$TEMPLATE_DIR" ]] && error_exit "템플릿 디렉토리가 존재하지 않습니다: $TEMPLATE_DIR"
[[ ! -d "$TARGET_PROJECT" ]] && error_exit "대상 프로젝트 디렉토리가 존재하지 않습니다: $TARGET_PROJECT"

section "ArchUnit 테스트 생성 시작"

info "템플릿 디렉토리: $TEMPLATE_DIR"
info "대상 프로젝트: $TARGET_PROJECT"
echo ""

# ============================================
# Step 1: Base Package 감지
# ============================================
section "Step 1/4: Base Package 감지"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DETECT_SCRIPT="$SCRIPT_DIR/detect-base-package.sh"

if [[ ! -f "$DETECT_SCRIPT" ]]; then
    error_exit "Package 감지 스크립트를 찾을 수 없습니다: $DETECT_SCRIPT"
fi

BASE_PACKAGE=$("$DETECT_SCRIPT" "$TARGET_PROJECT" | tail -1)

if [[ -z "$BASE_PACKAGE" ]]; then
    error_exit "Base Package를 감지할 수 없습니다."
fi

success "Base Package 감지 완료: $BASE_PACKAGE"
echo ""

# ============================================
# Step 2: 모듈 구조 확인
# ============================================
section "Step 2/4: 모듈 구조 확인"

# Gradle Multi-Module 프로젝트 확인
if [[ -f "$TARGET_PROJECT/settings.gradle" ]] || [[ -f "$TARGET_PROJECT/settings.gradle.kts" ]]; then
    info "Gradle Multi-Module 프로젝트 감지"
    IS_MULTI_MODULE=true
else
    info "Single Module 프로젝트 감지"
    IS_MULTI_MODULE=false
fi

# Bootstrap 모듈 경로 찾기
if [[ $IS_MULTI_MODULE == true ]]; then
    BOOTSTRAP_MODULE=$(find "$TARGET_PROJECT" -type d -name "bootstrap*" -path "*/bootstrap*" | head -1)
    DOMAIN_MODULE=$(find "$TARGET_PROJECT" -type d -name "domain" -maxdepth 2 | head -1)
    APPLICATION_MODULE=$(find "$TARGET_PROJECT" -type d -name "application" -maxdepth 2 | head -1)
else
    BOOTSTRAP_MODULE="$TARGET_PROJECT"
    DOMAIN_MODULE="$TARGET_PROJECT"
    APPLICATION_MODULE="$TARGET_PROJECT"
fi

info "Bootstrap 모듈: ${BOOTSTRAP_MODULE:-없음}"
info "Domain 모듈: ${DOMAIN_MODULE:-없음}"
info "Application 모듈: ${APPLICATION_MODULE:-없음}"
echo ""

# ============================================
# Step 3: Bootstrap 모듈 테스트 생성
# ============================================
section "Step 3/4: Bootstrap 모듈 ArchUnit 테스트 생성"

if [[ -n "$BOOTSTRAP_MODULE" ]]; then
    # 패키지 경로 생성 (com.company.project → com/company/project)
    PACKAGE_PATH=$(echo "$BASE_PACKAGE" | tr '.' '/')

    # 테스트 디렉토리 경로
    TEST_DIR="$BOOTSTRAP_MODULE/src/test/java/$PACKAGE_PATH/bootstrap/architecture"

    ensure_dir "$TEST_DIR"

    # Bootstrap 템플릿 처리
    for template_file in "$TEMPLATE_DIR/bootstrap"/*.template; do
        if [[ -f "$template_file" ]]; then
            filename=$(basename "$template_file" .template)
            output_file="$TEST_DIR/$filename"

            process_template "$template_file" "$output_file" "$BASE_PACKAGE"
        fi
    done

    success "Bootstrap 모듈 테스트 생성 완료 (5개 파일)"
else
    warning "Bootstrap 모듈을 찾을 수 없습니다. 건너뜁니다."
fi

echo ""

# ============================================
# Step 4: Domain/Application 모듈 테스트 생성
# ============================================
section "Step 4/4: Domain/Application 모듈 ArchUnit 테스트 생성 (선택적)"

# Domain 모듈 테스트
if [[ -n "$DOMAIN_MODULE" ]] && [[ -d "$TEMPLATE_DIR/domain" ]]; then
    PACKAGE_PATH=$(echo "$BASE_PACKAGE" | tr '.' '/')
    TEST_DIR="$DOMAIN_MODULE/src/test/java/$PACKAGE_PATH/domain/architecture"

    ensure_dir "$TEST_DIR"

    for template_file in "$TEMPLATE_DIR/domain"/*.template; do
        if [[ -f "$template_file" ]]; then
            filename=$(basename "$template_file" .template)
            output_file="$TEST_DIR/$filename"

            process_template "$template_file" "$output_file" "$BASE_PACKAGE"
        fi
    done

    success "Domain 모듈 테스트 생성 완료"
fi

# Application 모듈 테스트
if [[ -n "$APPLICATION_MODULE" ]] && [[ -d "$TEMPLATE_DIR/application" ]]; then
    PACKAGE_PATH=$(echo "$BASE_PACKAGE" | tr '.' '/')
    TEST_DIR="$APPLICATION_MODULE/src/test/java/$PACKAGE_PATH/application/architecture"

    ensure_dir "$TEST_DIR"

    for template_file in "$TEMPLATE_DIR/application"/*.template; do
        if [[ -f "$template_file" ]]; then
            filename=$(basename "$template_file" .template)
            output_file="$TEST_DIR/$filename"

            process_template "$template_file" "$output_file" "$BASE_PACKAGE"
        fi
    done

    success "Application 모듈 테스트 생성 완료"
fi

echo ""

# ============================================
# 최종 요약
# ============================================
section "✅ ArchUnit 테스트 생성 완료"

echo -e "${GREEN}생성된 테스트:${NC}"
echo "  • Base Package: $BASE_PACKAGE"
echo "  • Bootstrap 모듈: 5개 핵심 테스트"
[[ -n "$DOMAIN_MODULE" ]] && echo "  • Domain 모듈: 1개 테스트"
[[ -n "$APPLICATION_MODULE" ]] && echo "  • Application 모듈: 1개 테스트"
echo ""

echo -e "${CYAN}다음 단계:${NC}"
echo "  1. ./gradlew test 실행하여 ArchUnit 테스트 검증"
echo "  2. 위반 사항 확인 및 코드 수정"
echo "  3. 빌드 파이프라인에 ArchUnit 테스트 통합"
echo ""

success "🎉 ArchUnit 테스트 생성이 완료되었습니다!"
