#!/bin/bash

# =====================================================
# Claude Code Hook: after-tool-use
# Trigger: Write/Edit 도구 사용 직후
# Strategy: Cache-based validation with validation-helper.py
# Logging: log-to-langfuse.py (JSONL, LangFuse 호환)
# =====================================================

# LangFuse 로거 경로
LANGFUSE_LOGGER="langfuse/scripts/log-to-langfuse.py"

# 프로젝트명 가져오기
PROJECT_NAME=$(basename "$(pwd)")

# 입력 읽기 (Claude Code가 전달하는 JSON)
TOOL_DATA=$(cat)

# 파일 경로 추출 (jq 사용으로 안전한 JSON 파싱)
FILE_PATH=$(echo "$TOOL_DATA" | jq -r '.file_path // empty')

if [[ -z "$FILE_PATH" ]]; then
    # 파일 경로가 없으면 스킵
    exit 0
fi

# JSONL 로그 함수
log_event() {
    local event_type="$1"
    local data="$2"

    if [[ -f "$LANGFUSE_LOGGER" ]]; then
        python3 "$LANGFUSE_LOGGER" log \
            --event-type "$event_type" \
            --data "$data" 2>/dev/null
    fi
}

# 파일이 실제로 존재하는지 확인
if [[ ! -f "$FILE_PATH" ]]; then
    log_event "file_not_found" "{\"file\":\"$FILE_PATH\",\"reason\":\"file_does_not_exist\"}"
    exit 0
fi

# 파일 정보
FILE_LINES=$(wc -l < "$FILE_PATH" | tr -d ' ')

# 코드 생성 감지 로그
log_event "code_generation_detected" "{\"project\":\"$PROJECT_NAME\",\"file\":\"$FILE_PATH\",\"lines\":$FILE_LINES}"

# =====================================================
# Phase 1: Layer 감지 (파일 경로 기반)
# =====================================================

LAYER="unknown"

# case 문으로 가독성 및 유지보수성 개선
case "$FILE_PATH" in
    *domain/*model*)
        LAYER="domain"
        ;;
    *adapter/in/web*)
        LAYER="adapter-rest"
        ;;
    *adapter/out/persistence*)
        LAYER="adapter-persistence"
        ;;
    *application/*)
        LAYER="application"
        ;;
    *test/*)
        LAYER="testing"
        ;;
    *)
        LAYER="unknown"
        ;;
esac

# Layer 감지 로그
log_event "layer_detection" "{\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\"}"

# =====================================================
# Phase 2: Cache-based Validation (validation-helper.py)
# =====================================================

VALIDATOR_SCRIPT=".claude/hooks/scripts/validation-helper.py"

if [[ -f "$VALIDATOR_SCRIPT" && "$LAYER" != "unknown" ]]; then
    # Python 검증기 실행
    VALIDATION_OUTPUT=$(python3 "$VALIDATOR_SCRIPT" "$FILE_PATH" "$LAYER" 2>&1)
    VALIDATION_EXIT_CODE=$?

    # 검증 결과 로그
    if [[ $VALIDATION_EXIT_CODE -eq 0 ]]; then
        log_event "validation_result" "{\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\",\"result\":\"passed\",\"validator\":\"cache_based\"}"
    else
        log_event "validation_result" "{\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\",\"result\":\"failed\",\"validator\":\"cache_based\",\"output\":\"$VALIDATION_OUTPUT\"}"
    fi

    # Python 스크립트 출력 그대로 표시
    echo "$VALIDATION_OUTPUT"
else
    log_event "fallback_validation" "{\"file\":\"$FILE_PATH\",\"reason\":\"cache_not_available\"}"

    # ===== Fallback: Basic Critical Validators =====

    VALIDATION_FAILED=false
    VIOLATIONS=0

    # 1. Lombok 금지 검증
    if grep -qE "@(Data|Builder|Getter|Setter|AllArgsConstructor|NoArgsConstructor|RequiredArgsConstructor)" "$FILE_PATH"; then
        VALIDATION_FAILED=true
        VIOLATIONS=$((VIOLATIONS + 1))
        cat << EOF

---
⚠️ **Validation Failed: Lombok 사용 감지**

**위반 파일**: \`$FILE_PATH\`

**문제**: Lombok 어노테이션이 발견되었습니다.
- @Data, @Builder, @Getter, @Setter 등 모든 Lombok 사용 금지

**해결 방법**:
1. Lombok 어노테이션 제거
2. Pure Java getter/setter 수동 작성

**참고**: \`docs/coding_convention/\` - Zero-Tolerance 규칙

---

EOF
    fi

    # 2. Javadoc 검증
    if ! grep -q "@author" "$FILE_PATH"; then
        VALIDATION_FAILED=true
        VIOLATIONS=$((VIOLATIONS + 1))
        cat << EOF

---
⚠️ **Validation Failed: Javadoc @author 누락**

**위반 파일**: \`$FILE_PATH\`

**문제**: @author 태그가 없습니다.

**해결 방법**:
\`\`\`java
/**
 * 클래스 설명
 *
 * @author Claude
 * @since $(date '+%Y-%m-%d')
 */
\`\`\`

---

EOF
    fi

    # 3. Layer-Specific: Domain 레이어
    if [[ "$LAYER" == "domain" ]]; then
        # Spring/JPA annotation 검증
        if grep -qE "@(Entity|Table|Column|Service|Repository|Transactional)" "$FILE_PATH"; then
            VALIDATION_FAILED=true
            VIOLATIONS=$((VIOLATIONS + 1))
            cat << EOF

---
⚠️ **Validation Failed: Domain에서 Spring/JPA 사용 감지**

**위반 파일**: \`$FILE_PATH\`

**문제**: Domain 레이어는 순수 Java만 사용해야 합니다.

**금지**:
- @Entity, @Table, @Column (JPA)
- @Service, @Repository, @Transactional (Spring)

**참고**: Domain은 인프라에 의존하지 않습니다.

---

EOF
        fi
    fi

    # 최종 결과 로그
    if [[ "$VALIDATION_FAILED" == true ]]; then
        log_event "validation_result" "{\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\",\"result\":\"failed\",\"violations\":$VIOLATIONS,\"validator\":\"fallback\"}"
        echo ""
        echo "💡 코드를 수정한 후 다시 시도하세요."
        echo ""
    else
        log_event "validation_result" "{\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\",\"result\":\"passed\",\"violations\":0,\"validator\":\"fallback\"}"
        cat << EOF

---
✅ **Validation Passed**

파일: \`$FILE_PATH\`

모든 규칙을 준수합니다!

---

EOF
    fi
fi

# =====================================================
# Phase 3: Queue 자동 완료 (Write/Edit 도구 사용 시)
# =====================================================

QUEUE_MANAGER=".claude/queue/queue-manager.sh"

# Write/Edit 도구 감지
if echo "$TOOL_DATA" | jq -e '.tool_name' &>/dev/null; then
    TOOL_NAME=$(echo "$TOOL_DATA" | jq -r '.tool_name // empty')

    if [[ "$TOOL_NAME" =~ ^(Write|Edit|MultiEdit)$ && -f "$QUEUE_MANAGER" ]]; then
        # Queue 작업 완료
        COMPLETED_TASK=$(bash "$QUEUE_MANAGER" complete 2>&1)

        if [[ "$COMPLETED_TASK" =~ "Task completed" ]]; then
            TASK_ID=$(echo "$COMPLETED_TASK" | grep -oE 'task-[a-f0-9]+')
            log_event "queue_complete" "{\"task_id\":\"$TASK_ID\",\"file\":\"$FILE_PATH\",\"layer\":\"$LAYER\"}"
        fi
    fi
fi
