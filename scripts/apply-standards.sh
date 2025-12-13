#!/bin/bash

# ============================================================================
# Claude Spring Standards - 프로젝트 적용 스크립트
# ============================================================================
# 사용법:
#   ./apply-standards.sh <target-project-path> <package-name>
#
# 예시:
#   ./apply-standards.sh /path/to/my-project com.mycompany.orderservice
#   ./apply-standards.sh ../existing-project com.ryuqq.productapi
#
# 패키지명 형식: com.company.projectname (3단계 권장)
# ============================================================================

set -e

# ============================================================================
# 색상 정의
# ============================================================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================================================
# 유틸리티 함수
# ============================================================================
print_header() {
    echo -e "\n${PURPLE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${PURPLE}  $1${NC}"
    echo -e "${PURPLE}════════════════════════════════════════════════════════════════${NC}\n"
}

print_step() {
    echo -e "${CYAN}▶ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# ============================================================================
# 인자 검증
# ============================================================================
validate_args() {
    if [ $# -lt 2 ]; then
        print_error "사용법: $0 <target-project-path> <package-name>"
        echo ""
        echo "예시:"
        echo "  $0 /path/to/my-project com.mycompany.orderservice"
        echo "  $0 ../existing-project com.ryuqq.productapi"
        echo ""
        echo "패키지명 형식: com.company.projectname"
        exit 1
    fi

    TARGET_PROJECT="$1"
    PACKAGE_NAME="$2"

    # 패키지명 형식 검증 (최소 2단계, 권장 3단계)
    if [[ ! "$PACKAGE_NAME" =~ ^[a-z]+(\.[a-z0-9]+)+$ ]]; then
        print_error "잘못된 패키지명 형식: $PACKAGE_NAME"
        echo "올바른 형식: com.company.projectname (소문자, 점으로 구분)"
        exit 1
    fi

    # 대상 디렉토리 존재 확인
    if [ ! -d "$TARGET_PROJECT" ]; then
        print_error "대상 프로젝트 디렉토리가 존재하지 않습니다: $TARGET_PROJECT"
        exit 1
    fi

    # 절대 경로로 변환
    TARGET_PROJECT=$(cd "$TARGET_PROJECT" && pwd)

    print_success "대상 프로젝트: $TARGET_PROJECT"
    print_success "패키지명: $PACKAGE_NAME"
}

# ============================================================================
# 소스 프로젝트 경로 설정
# ============================================================================
setup_source_path() {
    # 스크립트 위치 기준으로 소스 프로젝트 경로 결정
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    SOURCE_PROJECT="$(dirname "$SCRIPT_DIR")"

    if [ ! -f "$SOURCE_PROJECT/.claude/CLAUDE.md" ]; then
        print_error "소스 프로젝트를 찾을 수 없습니다: $SOURCE_PROJECT"
        exit 1
    fi

    print_info "소스 프로젝트: $SOURCE_PROJECT"
}

# ============================================================================
# 패키지명 변환 함수
# ============================================================================
# com.company.project → com/company/project
package_to_path() {
    echo "$1" | tr '.' '/'
}

# com.ryuqq → com.company.project 변환
replace_package_in_file() {
    local file="$1"
    local old_package="com.ryuqq"
    local new_package="$PACKAGE_NAME"

    if [ -f "$file" ]; then
        # macOS와 Linux 호환 sed
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s/${old_package}/${new_package}/g" "$file"
        else
            sed -i "s/${old_package}/${new_package}/g" "$file"
        fi
    fi
}

# ============================================================================
# 모드 선택
# ============================================================================
select_mode() {
    print_header "적용 모드 선택"

    echo "1) 🆕 신규 적용 - 빈 프로젝트 또는 처음 적용"
    echo "2) 🔄 업데이트 - 기존 standards 프로젝트 최신화"
    echo "3) 📋 선택적 적용 - 원하는 항목만 선택"
    echo ""
    read -p "선택 (1/2/3): " MODE_CHOICE

    case $MODE_CHOICE in
        1) MODE="new" ;;
        2) MODE="update" ;;
        3) MODE="selective" ;;
        *)
            print_warning "기본값 '신규 적용' 선택됨"
            MODE="new"
            ;;
    esac
}

# ============================================================================
# 선택적 적용 메뉴
# ============================================================================
selective_menu() {
    print_header "적용할 항목 선택"

    echo "적용할 항목을 선택하세요 (쉼표로 구분, 예: 1,2,3):"
    echo ""
    echo "1) Claude 설정 (.claude/) - Skills, Commands, Hooks"
    echo "2) 코딩 컨벤션 문서 (docs/coding_convention/)"
    echo "3) ArchUnit 테스트 - Domain Layer"
    echo "4) ArchUnit 테스트 - Application Layer"
    echo "5) ArchUnit 테스트 - REST API Layer"
    echo "6) ArchUnit 테스트 - Persistence MySQL Layer"
    echo "7) ArchUnit 테스트 - Persistence Redis Layer"
    echo "8) Gradle 설정 (libs.versions.toml)"
    echo "9) 정적 분석 설정 (config/)"
    echo "S) Serena MCP 설정 (.serena/) - Memories 포함"
    echo "A) 전체 선택"
    echo ""
    read -p "선택: " SELECTED_ITEMS

    if [[ "$SELECTED_ITEMS" == "A" || "$SELECTED_ITEMS" == "a" ]]; then
        SELECTED_ITEMS="1,2,3,4,5,6,7,8,9,S"
    fi
}

# ============================================================================
# Claude 설정 복사
# ============================================================================
copy_claude_settings() {
    print_step "Claude 설정 복사 중..."

    # 기존 .claude 백업
    if [ -d "$TARGET_PROJECT/.claude" ]; then
        BACKUP_DIR="$TARGET_PROJECT/.claude.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.claude" "$BACKUP_DIR"
        print_info "기존 .claude 백업: $BACKUP_DIR"
    fi

    # 복사
    cp -r "$SOURCE_PROJECT/.claude" "$TARGET_PROJECT/"

    # 패키지명 치환
    find "$TARGET_PROJECT/.claude" -type f \( -name "*.md" -o -name "*.sh" \) | while read file; do
        replace_package_in_file "$file"
    done

    print_success "Claude 설정 복사 완료 (14 Skills, 12 Commands)"
}

# ============================================================================
# 코딩 컨벤션 문서 복사
# ============================================================================
copy_coding_conventions() {
    print_step "코딩 컨벤션 문서 복사 중..."

    mkdir -p "$TARGET_PROJECT/docs"

    # 기존 coding_convention 백업
    if [ -d "$TARGET_PROJECT/docs/coding_convention" ]; then
        BACKUP_DIR="$TARGET_PROJECT/docs/coding_convention.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/docs/coding_convention" "$BACKUP_DIR"
        print_info "기존 문서 백업: $BACKUP_DIR"
    fi

    cp -r "$SOURCE_PROJECT/docs/coding_convention" "$TARGET_PROJECT/docs/"

    # index.md, _config.yml도 복사
    [ -f "$SOURCE_PROJECT/docs/index.md" ] && cp "$SOURCE_PROJECT/docs/index.md" "$TARGET_PROJECT/docs/"
    [ -f "$SOURCE_PROJECT/docs/_config.yml" ] && cp "$SOURCE_PROJECT/docs/_config.yml" "$TARGET_PROJECT/docs/"
    [ -f "$SOURCE_PROJECT/docs/Gemfile" ] && cp "$SOURCE_PROJECT/docs/Gemfile" "$TARGET_PROJECT/docs/"

    # 패키지명 치환
    find "$TARGET_PROJECT/docs" -type f -name "*.md" | while read file; do
        replace_package_in_file "$file"
    done

    print_success "코딩 컨벤션 문서 복사 완료 (146개 문서)"
}

# ============================================================================
# ArchUnit 테스트 복사 - 레이어별
# ============================================================================
copy_archunit_tests() {
    local layer="$1"
    local source_path="$2"
    local target_module="$3"

    print_step "ArchUnit 테스트 복사 중: $layer"

    local source_arch="$SOURCE_PROJECT/$source_path"

    if [ ! -d "$source_arch" ]; then
        print_warning "소스 ArchUnit 디렉토리 없음: $source_arch"
        return
    fi

    # 대상 패키지 경로 생성
    local package_path=$(package_to_path "$PACKAGE_NAME")
    local target_dir="$TARGET_PROJECT/$target_module/src/test/java/$package_path/architecture"

    mkdir -p "$target_dir"

    # 복사
    cp -r "$source_arch"/* "$target_dir/" 2>/dev/null || true

    # 패키지명 치환
    find "$target_dir" -type f -name "*.java" | while read file; do
        replace_package_in_file "$file"
    done

    local count=$(find "$target_dir" -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
    print_success "$layer ArchUnit 테스트 복사 완료 ($count개)"
}

copy_archunit_domain() {
    copy_archunit_tests "Domain" "domain/src/test/java/com/ryuqq/domain/architecture" "domain"
}

copy_archunit_application() {
    copy_archunit_tests "Application" "application/src/test/java/com/ryuqq/application/architecture" "application"
}

copy_archunit_rest_api() {
    copy_archunit_tests "REST API" "adapter-in/rest-api/src/test/java/com/ryuqq/adapter/in/rest/architecture" "adapter-in/rest-api"
}

copy_archunit_persistence_mysql() {
    copy_archunit_tests "Persistence MySQL" "adapter-out/persistence-mysql/src/test/java/com/ryuqq/adapter/out/persistence/architecture" "adapter-out/persistence-mysql"
}

copy_archunit_persistence_redis() {
    copy_archunit_tests "Persistence Redis" "adapter-out/persistence-redis/src/test/java/com/ryuqq/adapter/out/persistence/redis/architecture" "adapter-out/persistence-redis"
}

# ============================================================================
# Gradle 설정 병합
# ============================================================================
merge_gradle_settings() {
    print_step "Gradle 설정 병합 중..."

    local target_toml="$TARGET_PROJECT/gradle/libs.versions.toml"
    local source_toml="$SOURCE_PROJECT/gradle/libs.versions.toml"

    mkdir -p "$TARGET_PROJECT/gradle"

    if [ -f "$target_toml" ]; then
        # 기존 파일 백업
        cp "$target_toml" "$target_toml.backup.$(date +%Y%m%d_%H%M%S)"
        print_info "기존 libs.versions.toml 백업됨"

        # 병합 안내
        print_warning "기존 libs.versions.toml이 있습니다."
        echo ""
        echo "다음 항목을 수동으로 병합해주세요:"
        echo ""
        echo "[versions] 섹션에 추가:"
        echo "  archunit = \"1.2.1\""
        echo "  checkstyle = \"10.14.0\""
        echo "  spotbugs = \"4.8.3\""
        echo "  spotbugsPlugin = \"6.0.9\""
        echo "  jacoco = \"0.8.11\""
        echo "  pmd = \"7.0.0\""
        echo ""
        echo "[libraries] 섹션에 추가:"
        echo "  archunit-junit5 = { module = \"com.tngtech.archunit:archunit-junit5\", version.ref = \"archunit\" }"
        echo ""

        read -p "소스 libs.versions.toml을 참조용으로 복사할까요? (y/n): " COPY_REF
        if [[ "$COPY_REF" == "y" || "$COPY_REF" == "Y" ]]; then
            cp "$source_toml" "$target_toml.reference"
            print_success "참조용 파일 생성: $target_toml.reference"
        fi
    else
        # 새로 복사
        cp "$source_toml" "$target_toml"
        print_success "libs.versions.toml 복사 완료"
    fi

    # build.gradle 패치 안내
    print_info "build.gradle에 다음 내용 추가 필요:"
    echo ""
    cat << 'EOF'
// 모든 서브프로젝트에 추가
subprojects {
    dependencies {
        // ArchUnit for Architecture Testing
        testImplementation rootProject.libs.archunit.junit5
    }

    // Lombok 금지 검증
    tasks.register('checkNoLombok') {
        doLast {
            def lombokFound = configurations.collect { config ->
                config.dependencies.findAll { dep ->
                    dep.group == 'org.projectlombok' && dep.name == 'lombok'
                }
            }.flatten()
            if (!lombokFound.isEmpty()) {
                throw new GradleException("❌ LOMBOK DETECTED in ${project.name}")
            }
        }
    }

    tasks.named('build') {
        dependsOn 'checkNoLombok'
    }
}
EOF
    echo ""
}

# ============================================================================
# 정적 분석 설정 복사
# ============================================================================
copy_static_analysis_config() {
    print_step "정적 분석 설정 복사 중..."

    # 기존 config 백업
    if [ -d "$TARGET_PROJECT/config" ]; then
        BACKUP_DIR="$TARGET_PROJECT/config.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/config" "$BACKUP_DIR"
        print_info "기존 config 백업: $BACKUP_DIR"
    fi

    cp -r "$SOURCE_PROJECT/config" "$TARGET_PROJECT/"

    print_success "정적 분석 설정 복사 완료 (checkstyle, spotbugs, pmd)"
}

# ============================================================================
# GitHub Actions 복사
# ============================================================================
copy_github_actions() {
    print_step "GitHub Actions 워크플로우 복사 중..."

    mkdir -p "$TARGET_PROJECT/.github/workflows"

    # docs-deploy.yml 복사
    if [ -f "$SOURCE_PROJECT/.github/workflows/docs-deploy.yml" ]; then
        cp "$SOURCE_PROJECT/.github/workflows/docs-deploy.yml" "$TARGET_PROJECT/.github/workflows/"
        print_success "docs-deploy.yml 복사 완료"
    fi
}

# ============================================================================
# Serena MCP 설정 복사
# ============================================================================
copy_serena_settings() {
    print_step "Serena MCP 설정 복사 중..."

    # 기존 .serena 백업
    if [ -d "$TARGET_PROJECT/.serena" ]; then
        BACKUP_DIR="$TARGET_PROJECT/.serena.backup.$(date +%Y%m%d_%H%M%S)"
        mv "$TARGET_PROJECT/.serena" "$BACKUP_DIR"
        print_info "기존 .serena 백업: $BACKUP_DIR"
    fi

    # .serena 디렉토리 생성
    mkdir -p "$TARGET_PROJECT/.serena/memories"

    # project.yml 복사 및 수정
    if [ -f "$SOURCE_PROJECT/.serena/project.yml" ]; then
        cp "$SOURCE_PROJECT/.serena/project.yml" "$TARGET_PROJECT/.serena/"

        # 프로젝트명 치환 (패키지명의 마지막 부분 사용)
        local project_name=$(echo "$PACKAGE_NAME" | awk -F. '{print $NF}')
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s/project_name: .*/project_name: \"${project_name}\"/" "$TARGET_PROJECT/.serena/project.yml"
        else
            sed -i "s/project_name: .*/project_name: \"${project_name}\"/" "$TARGET_PROJECT/.serena/project.yml"
        fi
        print_success "project.yml 복사 완료 (프로젝트명: $project_name)"
    fi

    # .gitignore 복사
    if [ -f "$SOURCE_PROJECT/.serena/.gitignore" ]; then
        cp "$SOURCE_PROJECT/.serena/.gitignore" "$TARGET_PROJECT/.serena/"
    fi

    # memories 복사
    if [ -d "$SOURCE_PROJECT/.serena/memories" ]; then
        cp -r "$SOURCE_PROJECT/.serena/memories"/* "$TARGET_PROJECT/.serena/memories/" 2>/dev/null || true

        # memories 내 패키지명 치환
        find "$TARGET_PROJECT/.serena/memories" -type f -name "*.md" | while read file; do
            replace_package_in_file "$file"
        done

        local memory_count=$(ls -1 "$TARGET_PROJECT/.serena/memories" 2>/dev/null | wc -l | tr -d ' ')
        print_success "Serena Memories 복사 완료 ($memory_count개)"
    fi

    print_info "Memory 파일 목록:"
    ls -1 "$TARGET_PROJECT/.serena/memories" 2>/dev/null | while read f; do
        echo "   • $f"
    done
}

# ============================================================================
# 업데이트 모드 실행
# ============================================================================
run_update_mode() {
    print_header "업데이트 모드 실행"

    echo "업데이트할 항목을 선택하세요:"
    echo ""
    echo "1) Claude 설정만 (.claude/)"
    echo "2) 코딩 컨벤션 문서만 (docs/coding_convention/)"
    echo "3) ArchUnit 테스트만"
    echo "4) Serena MCP 설정만 (.serena/)"
    echo "5) 전체 업데이트"
    echo ""
    read -p "선택 (1/2/3/4/5): " UPDATE_CHOICE

    case $UPDATE_CHOICE in
        1)
            copy_claude_settings
            ;;
        2)
            copy_coding_conventions
            ;;
        3)
            copy_archunit_domain
            copy_archunit_application
            copy_archunit_rest_api
            copy_archunit_persistence_mysql
            copy_archunit_persistence_redis
            ;;
        4)
            copy_serena_settings
            ;;
        5)
            copy_claude_settings
            copy_coding_conventions
            copy_serena_settings
            copy_archunit_domain
            copy_archunit_application
            copy_archunit_rest_api
            copy_archunit_persistence_mysql
            copy_archunit_persistence_redis
            copy_static_analysis_config
            copy_github_actions
            ;;
    esac
}

# ============================================================================
# 신규 적용 모드 실행
# ============================================================================
run_new_mode() {
    print_header "신규 적용 모드 실행"

    copy_claude_settings
    copy_coding_conventions
    copy_serena_settings
    copy_static_analysis_config
    copy_github_actions
    merge_gradle_settings

    echo ""
    print_info "ArchUnit 테스트를 복사할 레이어를 선택하세요."
    echo "기존 프로젝트 구조에 맞는 레이어만 선택하세요."
    echo ""

    read -p "Domain Layer ArchUnit 복사? (y/n): " COPY_DOMAIN
    [[ "$COPY_DOMAIN" == "y" || "$COPY_DOMAIN" == "Y" ]] && copy_archunit_domain

    read -p "Application Layer ArchUnit 복사? (y/n): " COPY_APP
    [[ "$COPY_APP" == "y" || "$COPY_APP" == "Y" ]] && copy_archunit_application

    read -p "REST API Layer ArchUnit 복사? (y/n): " COPY_REST
    [[ "$COPY_REST" == "y" || "$COPY_REST" == "Y" ]] && copy_archunit_rest_api

    read -p "Persistence MySQL Layer ArchUnit 복사? (y/n): " COPY_MYSQL
    [[ "$COPY_MYSQL" == "y" || "$COPY_MYSQL" == "Y" ]] && copy_archunit_persistence_mysql

    read -p "Persistence Redis Layer ArchUnit 복사? (y/n): " COPY_REDIS
    [[ "$COPY_REDIS" == "y" || "$COPY_REDIS" == "Y" ]] && copy_archunit_persistence_redis
}

# ============================================================================
# 선택적 적용 모드 실행
# ============================================================================
run_selective_mode() {
    selective_menu

    IFS=',' read -ra ITEMS <<< "$SELECTED_ITEMS"
    for item in "${ITEMS[@]}"; do
        item=$(echo "$item" | tr -d ' ')
        case $item in
            1) copy_claude_settings ;;
            2) copy_coding_conventions ;;
            3) copy_archunit_domain ;;
            4) copy_archunit_application ;;
            5) copy_archunit_rest_api ;;
            6) copy_archunit_persistence_mysql ;;
            7) copy_archunit_persistence_redis ;;
            8) merge_gradle_settings ;;
            9) copy_static_analysis_config ;;
            [Ss]) copy_serena_settings ;;
        esac
    done
}

# ============================================================================
# 결과 요약
# ============================================================================
print_summary() {
    print_header "적용 완료"

    echo "📁 대상 프로젝트: $TARGET_PROJECT"
    echo "📦 패키지명: $PACKAGE_NAME"
    echo ""

    echo "✅ 적용된 항목:"
    [ -d "$TARGET_PROJECT/.claude" ] && echo "   • Claude 설정 (15 Skills, 13 Commands)"
    [ -d "$TARGET_PROJECT/docs/coding_convention" ] && echo "   • 코딩 컨벤션 문서 (146개)"
    [ -d "$TARGET_PROJECT/.serena" ] && echo "   • Serena MCP 설정 (Memories 포함)"
    [ -d "$TARGET_PROJECT/config" ] && echo "   • 정적 분석 설정 (checkstyle, spotbugs, pmd)"

    echo ""
    echo "📋 다음 단계:"
    echo "   1. build.gradle에 ArchUnit 의존성 추가"
    echo "   2. libs.versions.toml 병합 (필요시)"
    echo "   3. ./gradlew test 실행하여 ArchUnit 테스트 확인"
    echo "   4. 위반 항목 점진적으로 수정"
    echo ""

    print_success "Claude Spring Standards 적용 완료!"
}

# ============================================================================
# 메인 실행
# ============================================================================
main() {
    print_header "Claude Spring Standards 적용 스크립트 v1.0"

    validate_args "$@"
    setup_source_path
    select_mode

    case $MODE in
        "new") run_new_mode ;;
        "update") run_update_mode ;;
        "selective") run_selective_mode ;;
    esac

    print_summary
}

# 실행
main "$@"
