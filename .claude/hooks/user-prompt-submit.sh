#!/bin/bash

# =====================================================
# Claude Code Hook: user-prompt-submit (JSONL Logging)
# Trigger: 사용자가 프롬프트를 제출할 때
# Strategy: Keyword → Layer → inject-rules.py (Cache-based)
# Logging: log-to-langfuse.py (JSONL, LangFuse 호환)
# Bash 3.2 호환 (macOS 기본 Bash)
# =====================================================

# LangFuse 로거 경로
LANGFUSE_LOGGER="langfuse/scripts/log-to-langfuse.py"

# 프로젝트 정보
PROJECT_NAME=$(basename "$(pwd)")

# 입력 읽기
USER_INPUT=$(cat)

# JSONL 로그 함수 (log-to-langfuse.py 사용)
log_event() {
    local event_type="$1"
    local data="$2"

    if [[ -f "$LANGFUSE_LOGGER" ]]; then
        python3 "$LANGFUSE_LOGGER" log \
            --event-type "$event_type" \
            --data "$data" 2>/dev/null
    fi
}

# 세션 시작 로그
log_event "session_start" "{\"project\":\"$PROJECT_NAME\",\"user_command\":\"$USER_INPUT\"}"

# ==================== Context 분석 ====================

CONTEXT_SCORE=0
DETECTED_LAYERS=()
DETECTED_KEYWORDS=()
PRIORITY_FILTER=""

# Keyword → Layer 매핑 함수 (Bash 3.2 호환)
get_layer_from_keyword() {
    local keyword="$1"

    case "$keyword" in
        # Domain layer
        aggregate|entity|value*object|domain*event|getter|factory|policy)
            echo "domain"
            ;;
        # Application layer
        usecase|service|command|query|transaction|assembler|spring|proxy|orchestration|orchestrator|idempotency|idemkey|wal|write*ahead*log|outcome|finalizer|reaper)
            echo "application"
            ;;
        # Adapter-REST layer (adapter-in 포함)
        controller|rest*api|endpoint|adapter*in|validation|request|response|handling)
            echo "adapter-rest"
            ;;
        # Adapter-Persistence layer (adapter-out, persistence-mysql 포함)
        repository|jpa|entity*mapping|adapter*out|persistence*mysql|persistence*postgresql|persistence*mongo|querydsl|batch|specification)
            echo "adapter-persistence"
            ;;
        # Testing
        test|archunit|testcontainers|benchmark|fixture|mother|builder)
            echo "testing"
            ;;
        # Java21
        record|sealed|virtual|threads|async)
            echo "java21"
            ;;
        # Enterprise
        dto|mapper|cache|event|circuit*breaker|resilience|saga)
            echo "enterprise"
            ;;
        # Error handling
        exception|error)
            echo "error-handling"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 키워드 목록 (프로젝트별 패키지 구조 포함)
KEYWORDS="aggregate entity value.object value_object valueobject domain.event domain_event domainevent getter factory policy usecase service command query transaction assembler spring proxy orchestration orchestrator idempotency idemkey wal write.ahead.log write_ahead_log writeaheadlog outcome finalizer reaper controller rest.api rest_api restapi endpoint adapter-in adapter_in adapterin validation request response handling repository jpa entity.mapping entity_mapping entitymapping adapter-out adapter_out adapterout persistence-mysql persistence_mysql persistencemysql persistence-postgresql persistence-mongo querydsl batch specification test archunit testcontainers benchmark fixture mother builder record sealed virtual threads async dto mapper cache event circuit.breaker circuit_breaker circuitbreaker resilience saga exception error"

# Primary Keywords 검색 (30점)
for keyword in $KEYWORDS; do
    # . 와 _ 를 공백으로도 매칭되도록 패턴 변환
    pattern=$(echo "$keyword" | sed 's/[._]/ /g')

    if echo "$USER_INPUT" | grep -qiE "$pattern"; then
        layer=$(get_layer_from_keyword "$keyword")

        if [ -n "$layer" ]; then
            CONTEXT_SCORE=$((CONTEXT_SCORE + 30))

            # 레이어 중복 방지
            if [[ ! " ${DETECTED_LAYERS[@]} " =~ " ${layer} " ]]; then
                DETECTED_LAYERS+=("$layer")
            fi

            DETECTED_KEYWORDS+=("$keyword")
        fi
    fi
done

# 한글 키워드 검색
if echo "$USER_INPUT" | grep -q "애그리게이트"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 30))
    if [[ ! " ${DETECTED_LAYERS[@]} " =~ " domain " ]]; then
        DETECTED_LAYERS+=("domain")
    fi
    DETECTED_KEYWORDS+=("애그리게이트")
fi

if echo "$USER_INPUT" | grep -q "컨트롤러"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 30))
    if [[ ! " ${DETECTED_LAYERS[@]} " =~ " adapter-rest " ]]; then
        DETECTED_LAYERS+=("adapter-rest")
    fi
    DETECTED_KEYWORDS+=("컨트롤러")
fi

if echo "$USER_INPUT" | grep -q "테스트"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 30))
    if [[ ! " ${DETECTED_LAYERS[@]} " =~ " testing " ]]; then
        DETECTED_LAYERS+=("testing")
    fi
    DETECTED_KEYWORDS+=("테스트")
fi

# Secondary Keywords (15점)
if echo "$USER_INPUT" | grep -qiE "(domain|도메인)"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 15))
    DETECTED_KEYWORDS+=("domain_context")
fi

if echo "$USER_INPUT" | grep -qiE "(api|rest)"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 15))
    DETECTED_KEYWORDS+=("api_context")
fi

if echo "$USER_INPUT" | grep -qiE "(persistence|영속성)"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 15))
    DETECTED_KEYWORDS+=("persistence_context")
fi

if echo "$USER_INPUT" | grep -qiE "(transaction|트랜잭션)"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 15))
    DETECTED_KEYWORDS+=("transaction_context")
fi

if echo "$USER_INPUT" | grep -qiE "(validation|검증)"; then
    CONTEXT_SCORE=$((CONTEXT_SCORE + 15))
    DETECTED_KEYWORDS+=("validation_context")
fi

# Zero-Tolerance Keywords (20점)
if echo "$USER_INPUT" | grep -qiE "(lombok|getter\.chaining|law\.of\.demeter|@transactional|zero\.tolerance)"; then
    PRIORITY_FILTER="critical"
    CONTEXT_SCORE=$((CONTEXT_SCORE + 20))
    DETECTED_KEYWORDS+=("zero_tolerance")
fi

# 키워드 분석 결과 로그
LAYERS_JSON=$(printf '%s\n' "${DETECTED_LAYERS[@]}" | jq -R . | jq -s . 2>/dev/null || echo "[]")
KEYWORDS_JSON=$(printf '%s\n' "${DETECTED_KEYWORDS[@]}" | jq -R . | jq -s . 2>/dev/null || echo "[]")

log_event "keyword_analysis" "{\"context_score\":$CONTEXT_SCORE,\"threshold\":25,\"detected_layers\":$LAYERS_JSON,\"detected_keywords\":$KEYWORDS_JSON,\"priority_filter\":\"$PRIORITY_FILTER\"}"

# ==================== 규칙 주입 ====================

if [[ $CONTEXT_SCORE -ge 25 ]]; then
    log_event "decision" "{\"action\":\"cache_injection\",\"reason\":\"score_above_threshold\"}"

    # ==================== Cache 기반 규칙 주입 (단일 진실 공급원) ====================

    INJECT_SCRIPT=".claude/commands/lib/inject-rules.py"

    if [[ -f "$INJECT_SCRIPT" ]]; then
        for layer in "${DETECTED_LAYERS[@]}"; do
            # inject-rules.py 호출 (별도로 자체 JSON 로그 작성)
            if [[ -n "$PRIORITY_FILTER" ]]; then
                python3 "$INJECT_SCRIPT" "$layer" "$PRIORITY_FILTER"
            else
                python3 "$INJECT_SCRIPT" "$layer"
            fi
        done

        log_event "cache_injection_complete" "{\"layers_count\":${#DETECTED_LAYERS[@]}}"

        # 레이어가 없으면 일반 규칙
        if [[ ${#DETECTED_LAYERS[@]} -eq 0 ]]; then
            cat << 'EOF'

---

## 🎯 기본 프로젝트 규칙 (자동 주입됨)

### ❌ Zero-Tolerance 규칙
- **Lombok 절대 금지**: @Data, @Builder, @Getter, @Setter 등 모든 Lombok 어노테이션
- **Javadoc 필수**: 모든 public 클래스/메서드에 @author, @since 포함
- **트랜잭션 경계**: @Transactional 내 외부 API 호출 절대 금지

### ✅ 필수 규칙
- **Pure Java**: Domain 레이어는 순수 Java만 사용
- **Law of Demeter**: Getter 체이닝 금지

---

EOF
        fi
    else
        log_event "error" "{\"message\":\"inject-rules.py not found\"}"
    fi
else
    log_event "decision" "{\"action\":\"skip_injection\",\"reason\":\"score_below_threshold\"}"
fi

# ==================== Queue 자동 추가 ====================

# Queue Manager 경로
QUEUE_MANAGER=".claude/queue/queue-manager.sh"

# Context Score >= 25이면 Queue에 자동 추가
if [[ $CONTEXT_SCORE -ge 25 && -f "$QUEUE_MANAGER" ]]; then
    # Queue에 작업 추가 (첫 줄, 최대 100자)
    TASK_DESCRIPTION=$(echo "$USER_INPUT" | head -n 1 | cut -c 1-100 | sed 's/^[ \t]*//;s/[ \t]*$//')

    # Hook JSON이 섞인 경우 필터링
    if echo "$TASK_DESCRIPTION" | grep -q "session_id"; then
        # JSON에서 prompt 필드 추출 시도
        TASK_DESCRIPTION=$(echo "$USER_INPUT" | grep -o '"prompt":"[^"]*"' | head -n 1 | cut -d'"' -f4 | cut -c 1-100)
    fi

    # 여전히 비어있으면 스킵
    if [[ -z "$TASK_DESCRIPTION" || "$TASK_DESCRIPTION" == "null" ]]; then
        TASK_DESCRIPTION="(작업 설명 없음)"
    fi

    TASK_ID=$(bash "$QUEUE_MANAGER" add "$TASK_DESCRIPTION" "$CONTEXT_SCORE" "$LAYERS_JSON" 2>/dev/null)

    if [[ -n "$TASK_ID" ]]; then
        log_event "queue_add" "{\"task_id\":\"$TASK_ID\",\"description\":\"$TASK_DESCRIPTION\",\"context_score\":$CONTEXT_SCORE,\"layers\":$LAYERS_JSON}"

        # Queue 작업 시작
        bash "$QUEUE_MANAGER" start 2>/dev/null
        log_event "queue_start" "{\"task_id\":\"$TASK_ID\"}"
    fi
fi

# ==================== Skills 감지 ====================

DETECTED_SKILL=""

# Skills 디렉토리 확인
SKILLS_DIR=".claude/skills"
AVAILABLE_SKILLS="application-expert domain-expert rest-api-expert test-expert convention-reviewer"

# 명시적 Skill 호출 감지 (/skill-name 또는 "skill-name")
for skill in $AVAILABLE_SKILLS; do
    # / 접두사 패턴
    if echo "$USER_INPUT" | grep -qiE "/$skill"; then
        DETECTED_SKILL="$skill"
        break
    fi

    # 명시적 skill 이름 언급
    if echo "$USER_INPUT" | grep -qiE "(use|call|run|invoke|execute) $skill"; then
        DETECTED_SKILL="$skill"
        break
    fi
done

# 암시적 Skill 감지 (키워드 기반)
if [[ -z "$DETECTED_SKILL" ]]; then
    # Application layer 관련
    if echo "$USER_INPUT" | grep -qiE "(application|usecase|service|transaction|command|query) (review|analysis|check|expert)"; then
        DETECTED_SKILL="application-expert"
    fi

    # Domain layer 관련
    if echo "$USER_INPUT" | grep -qiE "(domain|aggregate|entity|value.object) (review|analysis|check|expert)"; then
        DETECTED_SKILL="domain-expert"
    fi

    # REST API 관련
    if echo "$USER_INPUT" | grep -qiE "(rest|api|controller|endpoint) (review|analysis|check|expert)"; then
        DETECTED_SKILL="rest-api-expert"
    fi

    # Test 관련
    if echo "$USER_INPUT" | grep -qiE "(test|testing|unit.test|integration) (review|write|create|expert)"; then
        DETECTED_SKILL="test-expert"
    fi

    # Convention review 관련
    if echo "$USER_INPUT" | grep -qiE "(convention|violation|compliance|standard) (review|check|verify)"; then
        DETECTED_SKILL="convention-reviewer"
    fi
fi

# Skills 감지 로그
if [[ -n "$DETECTED_SKILL" ]]; then
    log_event "skill_detected" "{\"skill\":\"$DETECTED_SKILL\",\"user_input\":\"$(echo "$USER_INPUT" | head -c 100 | sed 's/"/\\"/g')\"}"

    # Skill 실행 시작 타임스탬프
    SKILL_START_TIME=$(date +%s)

    # Skill 시작 로그
    log_event "skill_start" "{\"skill\":\"$DETECTED_SKILL\",\"start_time\":$SKILL_START_TIME}"
fi

# 원본 입력 반환
echo "$USER_INPUT"
