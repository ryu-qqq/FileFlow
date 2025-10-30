#!/bin/bash
#
# 통합 검증 스크립트
#
# 목적: Cascade Rules 검증 + 테스트 실행 + Claude Code 분석 통합
# 사용법: ./scripts/integrated-validation.sh <layer>
#
# 예시:
#   ./scripts/integrated-validation.sh domain
#   ./scripts/integrated-validation.sh application
#   ./scripts/integrated-validation.sh persistence
#   ./scripts/integrated-validation.sh rest-api
#   ./scripts/integrated-validation.sh all  # 전체 검증
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
if [ $# -lt 1 ]; then
    log_error "사용법: $0 <layer>"
    echo ""
    echo "파라미터:"
    echo "  layer : Layer 타입 (domain|application|persistence|rest-api|all)"
    echo ""
    echo "예시:"
    echo "  $0 domain"
    echo "  $0 application"
    echo "  $0 all"
    exit 1
fi

LAYER="$1"

# ==============================================================================
# Layer 검증
# ==============================================================================
case "$LAYER" in
    domain|application|persistence|rest-api|all)
        log_info "Layer 검증 성공: $LAYER"
        ;;
    *)
        log_error "유효하지 않은 Layer: $LAYER"
        log_error "지원하는 Layer: domain, application, persistence, rest-api, all"
        exit 1
        ;;
esac

# ==============================================================================
# 프로젝트 루트 디렉토리 확인
# ==============================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

log_info "프로젝트 루트: $PROJECT_ROOT"

# ==============================================================================
# Layer별 모듈 경로 매핑
# ==============================================================================
declare -A LAYER_MODULES=(
    ["domain"]="domain"
    ["application"]="application"
    ["persistence"]="adapter-out/persistence-mysql"
    ["rest-api"]="adapter-in/rest-api"
)

declare -A LAYER_DESCRIPTIONS=(
    ["domain"]="Domain Layer (Aggregate, Value Object, Domain Event)"
    ["application"]="Application Layer (UseCase, Command, Query, Assembler)"
    ["persistence"]="Persistence Layer (Entity, Repository, Adapter)"
    ["rest-api"]="REST API Layer (Controller, DTO, Mapper)"
)

# ==============================================================================
# 검증 리포트 디렉토리 생성
# ==============================================================================
REPORT_DIR="$PROJECT_ROOT/claudedocs/validation-reports"
mkdir -p "$REPORT_DIR"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/validation-report-${LAYER}-${TIMESTAMP}.md"

# ==============================================================================
# 검증 시작
# ==============================================================================
log_info "=========================================="
log_info "통합 검증 시작"
log_info "=========================================="
log_info "Layer: $LAYER"
log_info "Report: $REPORT_FILE"
log_info "=========================================="

# ==============================================================================
# 검증 함수: 단일 Layer
# ==============================================================================
validate_single_layer() {
    local layer=$1
    local module_path="${LAYER_MODULES[$layer]}"
    local description="${LAYER_DESCRIPTIONS[$layer]}"

    log_info "------------------------------------------"
    log_info "검증 Layer: $description"
    log_info "------------------------------------------"

    # Step 1: Gradle 빌드 및 테스트
    log_info "[1/4] Gradle 빌드 및 테스트 실행 중..."

    if [ "$layer" == "domain" ]; then
        MODULE_NAME="domain"
    elif [ "$layer" == "application" ]; then
        MODULE_NAME="application"
    elif [ "$layer" == "persistence" ]; then
        MODULE_NAME="adapter-out:persistence-mysql"
    elif [ "$layer" == "rest-api" ]; then
        MODULE_NAME="adapter-in:rest-api"
    fi

    if ./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build --no-daemon 2>&1 | tee "${REPORT_DIR}/build-${layer}-${TIMESTAMP}.log"; then
        log_success "Gradle 빌드 성공"
    else
        log_error "Gradle 빌드 실패"
        log_error "로그: ${REPORT_DIR}/build-${layer}-${TIMESTAMP}.log"
        return 1
    fi

    # Step 2: 테스트 결과 분석
    log_info "[2/4] 테스트 결과 분석 중..."

    TEST_REPORT_DIR="${module_path}/build/reports/tests/test"
    if [ -d "$TEST_REPORT_DIR" ]; then
        log_success "테스트 리포트 생성됨: $TEST_REPORT_DIR/index.html"
    else
        log_warn "테스트 리포트 없음"
    fi

    # Step 3: Checkstyle 검증
    log_info "[3/4] Checkstyle 검증 중..."

    if ./gradlew :${MODULE_NAME}:checkstyleMain :${MODULE_NAME}:checkstyleTest --no-daemon 2>&1 | tee "${REPORT_DIR}/checkstyle-${layer}-${TIMESTAMP}.log"; then
        log_success "Checkstyle 검증 성공"
    else
        log_warn "Checkstyle 경고 발견"
    fi

    # Step 4: ArchUnit 검증 (application 모듈만)
    if [ "$layer" == "application" ] || [ "$layer" == "domain" ]; then
        log_info "[4/4] ArchUnit 아키텍처 검증 중..."

        if ./gradlew :bootstrap:bootstrap-web-api:test --tests "*ArchitectureTest" --no-daemon 2>&1 | tee "${REPORT_DIR}/archunit-${TIMESTAMP}.log"; then
            log_success "ArchUnit 검증 성공"
        else
            log_error "ArchUnit 검증 실패"
            return 1
        fi
    else
        log_info "[4/4] ArchUnit 검증 (해당 레이어는 스킵)"
    fi

    log_success "------------------------------------------"
    log_success "$description 검증 완료"
    log_success "------------------------------------------"

    return 0
}

# ==============================================================================
# 검증 실행
# ==============================================================================
VALIDATION_SUCCESS=true

if [ "$LAYER" == "all" ]; then
    log_info "전체 Layer 검증 시작..."

    for layer in domain application persistence rest-api; do
        if ! validate_single_layer "$layer"; then
            VALIDATION_SUCCESS=false
        fi
    done
else
    if ! validate_single_layer "$LAYER"; then
        VALIDATION_SUCCESS=false
    fi
fi

# ==============================================================================
# 검증 리포트 생성
# ==============================================================================
log_info "------------------------------------------"
log_info "검증 리포트 생성 중..."
log_info "------------------------------------------"

cat > "$REPORT_FILE" << EOF
# 통합 검증 리포트

**Generated**: $(date '+%Y-%m-%d %H:%M:%S')
**Layer**: ${LAYER}
**Result**: $(if [ "$VALIDATION_SUCCESS" == "true" ]; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)

---

## 검증 항목

### 1. Gradle 빌드 및 테스트
- **실행**: \`./gradlew clean build\`
- **로그**: \`${REPORT_DIR}/build-${LAYER}-${TIMESTAMP}.log\`

### 2. Checkstyle 검증
- **실행**: \`./gradlew checkstyleMain checkstyleTest\`
- **로그**: \`${REPORT_DIR}/checkstyle-${LAYER}-${TIMESTAMP}.log\`

### 3. ArchUnit 아키텍처 검증
- **실행**: \`./gradlew test --tests "*ArchitectureTest"\`
- **로그**: \`${REPORT_DIR}/archunit-${TIMESTAMP}.log\`

---

## 다음 단계

### 검증 실패 시
1. 로그 파일 확인하여 구체적인 오류 파악
2. 코딩 규칙 문서 참조: \`docs/coding_convention/\`
3. 수정 후 재검증

### 검증 성공 시
1. Git Commit
   \`\`\`bash
   git add .
   git commit -m "feat(${LAYER}): implement and validate ${LAYER} layer"
   \`\`\`

2. Pull Request 생성
   \`\`\`bash
   gh pr create --title "feat: ${LAYER} layer implementation" --body "..."
   \`\`\`

---

## 참고 문서
- [Coding Convention](../docs/coding_convention/)
- [Architecture Rules](../docs/architecture/)
- [Testing Guide](../docs/testing/)
EOF

log_success "검증 리포트 생성 완료: $REPORT_FILE"

# ==============================================================================
# Claude Code 분석 프롬프트 생성 (선택적)
# ==============================================================================
CLAUDE_PROMPT_FILE=$(mktemp)
trap "rm -f $CLAUDE_PROMPT_FILE" EXIT

cat > "$CLAUDE_PROMPT_FILE" << 'EOF'
# 검증 결과 분석

## Task
방금 실행된 통합 검증 결과를 분석하고 개선 사항을 제시해주세요.

### 1. 검증 리포트 확인
다음 리포트를 읽고 분석해주세요:
- **리포트 파일**: `${REPORT_FILE}`
- **빌드 로그**: `${REPORT_DIR}/build-${LAYER}-${TIMESTAMP}.log`
- **Checkstyle 로그**: `${REPORT_DIR}/checkstyle-${LAYER}-${TIMESTAMP}.log`

### 2. 분석 포인트
- **테스트 커버리지**: 충분한가?
- **코드 품질**: Checkstyle 위반 사항
- **아키텍처 준수**: ArchUnit 검증 결과
- **개선 필요 사항**: 구체적인 액션 아이템

### 3. 개선 제안
- 우선순위가 높은 개선 사항 (Critical/High/Medium/Low)
- 구체적인 수정 방법
- 참고할 규칙 문서

### 규칙
- ✅ 구체적이고 실행 가능한 개선 사항 제시
- ✅ 코딩 규칙 문서 참조
- ✅ 우선순위 명확히 구분
EOF

log_info "------------------------------------------"
log_info "Claude Code 분석 프롬프트 생성 완료"
log_info "------------------------------------------"
log_info "다음 명령어로 Claude Code 분석 실행:"
log_info "  claude code run < $CLAUDE_PROMPT_FILE"
log_info "------------------------------------------"

# ==============================================================================
# 완료 메시지
# ==============================================================================
log_info "=========================================="
if [ "$VALIDATION_SUCCESS" == "true" ]; then
    log_success "통합 검증 성공!"
    exit 0
else
    log_error "통합 검증 실패!"
    log_error "리포트 확인: $REPORT_FILE"
    exit 1
fi
