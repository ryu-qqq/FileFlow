#!/bin/bash
#
# Claude Code 비즈니스 로직 구현 스크립트
#
# 목적: Cascade 생성 보일러플레이트에 Claude Code로 비즈니스 로직 구현
# 사용법: ./scripts/claude-implement-business-logic.sh <task-key> <layer>
#
# 예시:
#   ./scripts/claude-implement-business-logic.sh PROJ-123 domain
#   ./scripts/claude-implement-business-logic.sh PROJ-123 application
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
if [ $# -lt 2 ]; then
    log_error "사용법: $0 <task-key> <layer>"
    echo ""
    echo "파라미터:"
    echo "  task-key : Jira Task Key (예: PROJ-123)"
    echo "  layer    : Layer 타입 (domain|application|persistence|rest-api)"
    echo ""
    echo "예시:"
    echo "  $0 PROJ-123 domain"
    echo "  $0 PROJ-123 application"
    exit 1
fi

TASK_KEY="$1"
LAYER="$2"

# ==============================================================================
# Layer 검증
# ==============================================================================
case "$LAYER" in
    domain|application|persistence|rest-api)
        log_info "Layer 검증 성공: $LAYER"
        ;;
    *)
        log_error "유효하지 않은 Layer: $LAYER"
        log_error "지원하는 Layer: domain, application, persistence, rest-api"
        exit 1
        ;;
esac

# ==============================================================================
# Claude Code 설치 확인
# ==============================================================================
if ! command -v claude &> /dev/null; then
    log_error "Claude Code CLI가 설치되어 있지 않습니다"
    log_error "설치 방법: https://docs.claude.com/claude-code"
    exit 1
fi

# ==============================================================================
# 프로젝트 루트 디렉토리 확인
# ==============================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

log_info "프로젝트 루트: $PROJECT_ROOT"

# ==============================================================================
# Claude Code 프롬프트 파일 생성
# ==============================================================================
PROMPT_FILE=$(mktemp)
trap "rm -f $PROMPT_FILE" EXIT

log_info "=========================================="
log_info "Claude Code 비즈니스 로직 구현 시작"
log_info "=========================================="
log_info "Task Key: $TASK_KEY"
log_info "Layer: $LAYER"
log_info "=========================================="

# Layer별 프롬프트 생성
case "$LAYER" in
    domain)
        cat > "$PROMPT_FILE" << EOF
# Domain Layer 비즈니스 로직 구현

## Task: $TASK_KEY

### 1. Jira Task 분석
\`/jira-task $TASK_KEY\`

### 2. 생성된 Domain 파일 분석
다음 디렉토리의 생성된 파일들을 분석해주세요:
- domain/src/main/java/com/ryuqq/domain/

### 3. 비즈니스 로직 구현
Jira Task의 요구사항을 기반으로 다음을 구현해주세요:

#### Aggregate Root
- Factory Methods (create, of) 구현
- 비즈니스 메서드 추가
- 상태 변경 로직
- 불변성 보장

#### Value Objects
- 검증 로직 구현
- 도메인 규칙 적용

#### Domain Events (필요 시)
- 이벤트 정의
- 이벤트 발행 시점

### 4. 검증
- Law of Demeter 준수 확인
- Lombok 미사용 확인
- Tell, Don't Ask 패턴 적용
- Pure Java getter/setter 사용

### 5. 테스트 추가
- 단위 테스트 작성
- 경계값 테스트
- 예외 케이스 테스트

### 규칙
- ❌ Lombok 절대 사용 금지
- ❌ Getter 체이닝 금지
- ✅ Factory Method 패턴
- ✅ 불변성 원칙
- ✅ Javadoc 필수
EOF
        ;;

    application)
        cat > "$PROMPT_FILE" << EOF
# Application Layer 비즈니스 로직 구현

## Task: $TASK_KEY

### 1. Jira Task 분석
\`/jira-task $TASK_KEY\`

### 2. 생성된 Application 파일 분석
다음 디렉토리의 생성된 파일들을 분석해주세요:
- application/src/main/java/com/ryuqq/application/

### 3. 비즈니스 로직 구현
Jira Task의 요구사항을 기반으로 다음을 구현해주세요:

#### Application Service
- UseCase execute() 메서드 구현
- Domain 비즈니스 로직 오케스트레이션
- OutPort 호출
- Transaction 경계 설정

#### Assembler
- DTO → Domain 변환 로직
- Domain → Response 변환 로직
- 검증 로직

### 4. 검증
- @Transactional 경계 확인
- 트랜잭션 내 외부 API 호출 금지
- Spring 프록시 제약사항 (Private/Final 금지)
- Single Responsibility 준수

### 5. 테스트 추가
- Service 단위 테스트 (Mockito)
- OutPort Mocking
- BDDMockito 패턴

### 규칙
- ❌ @Transactional 내 RestTemplate/WebClient 호출 금지
- ❌ Private/Final 메서드에 @Transactional 금지
- ✅ Command/Query 분리
- ✅ Javadoc 필수
EOF
        ;;

    persistence)
        cat > "$PROMPT_FILE" << EOF
# Persistence Layer 비즈니스 로직 구현

## Task: $TASK_KEY

### 1. Jira Task 분석
\`/jira-task $TASK_KEY\`

### 2. 생성된 Persistence 파일 분석
다음 디렉토리의 생성된 파일들을 분석해주세요:
- adapter-out/persistence-mysql/src/main/java/

### 3. 비즈니스 로직 구현
Jira Task의 요구사항을 기반으로 다음을 구현해주세요:

#### Repository
- 커스텀 쿼리 메서드 추가
- QueryDSL 동적 쿼리 (필요 시)

#### Entity Mapper
- Entity ↔ Domain 변환 로직
- Long FK 전략 적용

#### Persistence Adapter
- OutPort 구현
- Repository 호출
- 예외 처리

### 4. 검증
- Long FK Strategy 적용 확인
- JPA 관계 어노테이션 미사용 확인
- Entity Immutability 확인

### 5. 테스트 추가
- Repository 테스트 (@DataJpaTest)
- Adapter 통합 테스트 (@SpringBootTest)
- QueryDSL 쿼리 테스트

### 규칙
- ❌ @ManyToOne, @OneToMany 등 관계 어노테이션 금지
- ✅ Long userId 형태로 FK 사용
- ✅ Javadoc 필수
EOF
        ;;

    rest-api)
        cat > "$PROMPT_FILE" << EOF
# REST API Layer 비즈니스 로직 구현

## Task: $TASK_KEY

### 1. Jira Task 분석
\`/jira-task $TASK_KEY\`

### 2. 생성된 REST API 파일 분석
다음 디렉토리의 생성된 파일들을 분석해주세요:
- adapter-in/rest-api/src/main/java/

### 3. 비즈니스 로직 구현
Jira Task의 요구사항을 기반으로 다음을 구현해주세요:

#### Controller
- CRUD 엔드포인트 구현
- HTTP Status 매핑
- ApiResponse<T> 래핑

#### API Mapper
- API DTO ↔ Application DTO 변환
- Validation 연동

#### Error Mapper
- Domain Exception → ErrorInfo 변환
- HTTP Status 매핑

### 4. 검증
- Controller Thin 패턴 확인
- 비즈니스 로직 미포함 확인
- HTTP ↔ DTO 변환만 담당

### 5. 테스트 추가
- Controller 테스트 (@WebMvcTest)
- MockMvc HTTP 시뮬레이션
- Validation 테스트

### 규칙
- ❌ Controller에 비즈니스 로직 금지
- ✅ Controller Thin 패턴
- ✅ Jakarta Validation
- ✅ Javadoc 필수
EOF
        ;;
esac

# ==============================================================================
# Claude Code 실행
# ==============================================================================
log_info "Claude Code 실행 중..."
log_info "프롬프트 파일: $PROMPT_FILE"

# Claude Code Interactive Mode 실행
if claude code run < "$PROMPT_FILE"; then
    log_success "Claude Code 실행 완료!"
else
    log_error "Claude Code 실행 실패"
    exit 1
fi

# ==============================================================================
# 완료 메시지
# ==============================================================================
log_info "=========================================="
log_success "비즈니스 로직 구현 완료!"
log_info "=========================================="
log_info "다음 단계:"
log_info "  1. 구현된 코드 리뷰"
log_info "  2. 통합 검증 실행"
log_info "     ./scripts/integrated-validation.sh $LAYER"
log_info "  3. Git Commit"
log_info "     git add ."
log_info "     git commit -m \"feat($LAYER): implement $TASK_KEY business logic\""
log_info "=========================================="

exit 0
