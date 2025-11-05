#!/bin/bash
################################################################################
# Package Detector Script
#
# 목적: 프로젝트의 Base Package 자동 감지
# 사용: detect-base-package.sh [project-dir]
# 출력: com.company.project (Base Package)
################################################################################

set -euo pipefail

# ============================================
# 색상 정의
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Functions
# ============================================

# 에러 메시지 출력 후 종료
error_exit() {
    echo -e "${RED}❌ Error: $1${NC}" >&2
    exit 1
}

# 정보 메시지 출력
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 성공 메시지 출력
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 경고 메시지 출력
warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Base Package 감지 (여러 방법 시도)
detect_base_package() {
    local project_dir="$1"

    # Method 1: build.gradle 또는 build.gradle.kts에서 group 추출
    if [[ -f "$project_dir/build.gradle" ]]; then
        info "Method 1: build.gradle에서 group 추출 시도..."
        local group=$(grep -E "^group\s*=\s*" "$project_dir/build.gradle" | head -1 | sed -E "s/^group\s*=\s*['\"]?([^'\"]+)['\"]?.*/\1/" | tr -d "'" | tr -d '"' | xargs)
        if [[ -n "$group" ]]; then
            success "build.gradle에서 Base Package 발견: $group"
            echo "$group"
            return 0
        fi
    fi

    if [[ -f "$project_dir/build.gradle.kts" ]]; then
        info "Method 1: build.gradle.kts에서 group 추출 시도..."
        local group=$(grep -E "^group\s*=\s*" "$project_dir/build.gradle.kts" | head -1 | sed -E 's/^group\s*=\s*"([^"]+)".*/\1/' | xargs)
        if [[ -n "$group" ]]; then
            success "build.gradle.kts에서 Base Package 발견: $group"
            echo "$group"
            return 0
        fi
    fi

    # Method 2: pom.xml에서 groupId 추출 (Maven 프로젝트)
    if [[ -f "$project_dir/pom.xml" ]]; then
        info "Method 2: pom.xml에서 groupId 추출 시도..."
        local group_id=$(grep -m 1 "<groupId>" "$project_dir/pom.xml" | sed -E 's/.*<groupId>([^<]+)<\/groupId>.*/\1/' | xargs)
        if [[ -n "$group_id" ]]; then
            success "pom.xml에서 Base Package 발견: $group_id"
            echo "$group_id"
            return 0
        fi
    fi

    # Method 3: Main 클래스 또는 Application 클래스 찾기
    info "Method 3: Main 클래스에서 패키지 추출 시도..."
    local main_class_file=$(find "$project_dir" -name "*Application.java" -o -name "*Main.java" | head -1)
    if [[ -n "$main_class_file" ]]; then
        local package_line=$(grep -E "^package\s+" "$main_class_file" | head -1)
        if [[ -n "$package_line" ]]; then
            # awk로 패키지 이름 추출 (macOS sed 호환성 문제 해결)
            local package_name=$(echo "$package_line" | awk '{print $2}' | sed 's/;//' | xargs)
            # 마지막 패키지 세그먼트 제거 (예: com.company.project.bootstrap → com.company.project)
            local base_package=$(echo "$package_name" | sed -E 's/\.(bootstrap|application|domain|adapter).*$//')
            success "Main 클래스에서 Base Package 발견: $base_package"
            echo "$base_package"
            return 0
        fi
    fi

    # Method 4: 가장 깊은 공통 패키지 찾기 (heuristic)
    info "Method 4: 공통 패키지 패턴 분석 시도..."
    local java_files=$(find "$project_dir" -name "*.java" -path "*/src/main/java/*" | head -20)
    if [[ -n "$java_files" ]]; then
        # 패키지 추출 및 가장 짧은 공통 패키지 찾기
        local packages=$(echo "$java_files" | xargs grep -h "^package " | sed 's/^package \(.*\);/\1/' | sort -u)

        # 가장 짧은 패키지 (Base Package일 가능성 높음)
        local shortest_package=$(echo "$packages" | awk '{ print length, $0 }' | sort -n | head -1 | cut -d" " -f2-)

        # .bootstrap, .application, .domain, .adapter 등 제거
        local base_package=$(echo "$shortest_package" | sed -E 's/\.(bootstrap|application|domain|adapter|config).*$//')

        if [[ -n "$base_package" ]]; then
            success "공통 패키지 패턴에서 Base Package 발견: $base_package"
            echo "$base_package"
            return 0
        fi
    fi

    # 모든 방법 실패
    error_exit "Base Package를 자동으로 감지할 수 없습니다. build.gradle, pom.xml 또는 Java 소스 파일을 확인하세요."
}

# ============================================
# Main Script
# ============================================

# 인자 검증
if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <project-directory>"
    echo "Example: $0 /path/to/project"
    exit 1
fi

PROJECT_DIR="$1"

# 프로젝트 디렉토리 존재 확인
if [[ ! -d "$PROJECT_DIR" ]]; then
    error_exit "프로젝트 디렉토리가 존재하지 않습니다: $PROJECT_DIR"
fi

info "프로젝트 디렉토리: $PROJECT_DIR"
echo ""

# Base Package 감지
BASE_PACKAGE=$(detect_base_package "$PROJECT_DIR")

echo ""
success "✅ Base Package: $BASE_PACKAGE"

# 출력 (다른 스크립트에서 사용 가능하도록)
echo "$BASE_PACKAGE"
