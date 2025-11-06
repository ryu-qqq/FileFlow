#!/bin/bash

# =====================================================
# Queue Manager - 자동 작업 추적 시스템
# =====================================================

QUEUE_DIR=".claude/queue"
ACTIVE_FILE="$QUEUE_DIR/active.json"
COMPLETED_FILE="$QUEUE_DIR/completed.jsonl"

# 디렉토리 생성
mkdir -p "$QUEUE_DIR"

# active.json 초기화
if [[ ! -f "$ACTIVE_FILE" ]]; then
    echo '{"tasks":[]}' > "$ACTIVE_FILE"
fi

# completed.jsonl 초기화
touch "$COMPLETED_FILE"

# ==================== Helper Functions ====================

# 타임스탬프 생성
get_timestamp() {
    python3 -c "import time; print(time.time())"
}

# Task ID 생성
generate_task_id() {
    echo "task-$(date +%s%N | md5 | head -c 12)"
}

# JSON 파싱 (jq 사용)
get_tasks() {
    jq -r '.tasks' "$ACTIVE_FILE" 2>/dev/null || echo "[]"
}

add_task() {
    local description="$1"
    local context_score="${2:-0}"
    local detected_layers="${3:-[]}"

    local task_id=$(generate_task_id)
    local timestamp=$(get_timestamp)

    # jq를 사용하여 JSON 업데이트
    jq --arg id "$task_id" \
       --arg desc "$description" \
       --arg ts "$timestamp" \
       --argjson score "$context_score" \
       --argjson layers "$detected_layers" \
       '.tasks += [{
           id: $id,
           description: $desc,
           status: "pending",
           added_at: ($ts | tonumber),
           context_score: $score,
           detected_layers: $layers
       }]' "$ACTIVE_FILE" > "$ACTIVE_FILE.tmp" && mv "$ACTIVE_FILE.tmp" "$ACTIVE_FILE"

    echo "$task_id"
}

start_task() {
    local timestamp=$(get_timestamp)

    # 첫 번째 pending 작업을 in_progress로 변경
    jq --arg ts "$timestamp" \
       '(.tasks[] | select(.status == "pending") | .status) = "in_progress" |
        (.tasks[] | select(.status == "in_progress") | .started_at) = ($ts | tonumber)' \
       "$ACTIVE_FILE" > "$ACTIVE_FILE.tmp" && mv "$ACTIVE_FILE.tmp" "$ACTIVE_FILE"
}

complete_task() {
    local timestamp=$(get_timestamp)

    # in_progress 작업 찾기 (compact JSON 한 줄로)
    local task_json=$(jq -c '.tasks[] | select(.status == "in_progress")' "$ACTIVE_FILE" 2>/dev/null | head -n 1)

    if [[ -z "$task_json" ]]; then
        return 1
    fi

    # 완료 처리
    local task_id=$(echo "$task_json" | jq -r '.id')
    local started_at=$(echo "$task_json" | jq -r '.started_at // 0')
    local duration=$(python3 -c "print(round($timestamp - $started_at, 2))" 2>/dev/null || echo "0")

    # completed.jsonl에 추가
    echo "$task_json" | jq -c --arg ts "$timestamp" \
       --arg dur "$duration" \
       '.status = "completed" |
        .completed_at = ($ts | tonumber) |
        .duration = ($dur | tonumber)' >> "$COMPLETED_FILE"

    # active.json에서 제거
    jq --arg id "$task_id" \
       '.tasks |= map(select(.id != $id))' \
       "$ACTIVE_FILE" > "$ACTIVE_FILE.tmp" && mv "$ACTIVE_FILE.tmp" "$ACTIVE_FILE"

    echo "$task_id"
}

get_status() {
    local total=$(jq -r '.tasks | length' "$ACTIVE_FILE" 2>/dev/null || echo "0")
    local in_progress=$(jq -r '[.tasks[] | select(.status == "in_progress")] | length' "$ACTIVE_FILE" 2>/dev/null || echo "0")
    local pending=$(jq -r '[.tasks[] | select(.status == "pending")] | length' "$ACTIVE_FILE" 2>/dev/null || echo "0")

    echo "total:$total,in_progress:$in_progress,pending:$pending"
}

# ==================== Main Commands ====================

case "${1:-status}" in
    add)
        task_id=$(add_task "$2" "${3:-0}" "${4:-[]}")
        echo "✅ Task added: $task_id"
        echo "   Description: $2"
        ;;

    start)
        start_task
        echo "🔄 Task started"
        ;;

    complete)
        task_id=$(complete_task)
        if [[ -n "$task_id" ]]; then
            echo "✅ Task completed: $task_id"
        else
            echo "⚠️ No active task to complete"
        fi
        ;;

    status)
        status=$(get_status)
        total=$(echo "$status" | cut -d',' -f1 | cut -d':' -f2)
        in_progress=$(echo "$status" | cut -d',' -f2 | cut -d':' -f2)
        pending=$(echo "$status" | cut -d',' -f3 | cut -d':' -f2)

        echo "📋 Queue Status"
        echo "   Total: $total"
        echo "   In Progress: $in_progress"
        echo "   Pending: $pending"

        # 진행 중인 작업 표시
        if [[ "$in_progress" -gt 0 ]]; then
            echo ""
            echo "🔄 Current Task:"
            jq -r '.tasks[] | select(.status == "in_progress") | "   \(.description)"' "$ACTIVE_FILE"
        fi
        ;;

    list)
        echo "📋 Active Tasks:"
        jq -r '.tasks[] | "[\(.status)] \(.description)"' "$ACTIVE_FILE"
        ;;

    clear)
        echo '{"tasks":[]}' > "$ACTIVE_FILE"
        echo "🗑️ Queue cleared"
        ;;

    history)
        count="${2:-10}"
        echo "📜 Recent Completions (last $count):"
        if [[ -f "$COMPLETED_FILE" && -s "$COMPLETED_FILE" ]]; then
            tail -n "$count" "$COMPLETED_FILE" 2>/dev/null | jq -r '"\(.completed_at | strftime("%Y-%m-%d %H:%M:%S")) - \(.description) (Duration: \(.duration | round)s)"' 2>/dev/null || echo "   (No completions yet)"
        else
            echo "   (No completions yet)"
        fi
        ;;

    *)
        echo "Usage: queue-manager.sh {add|start|complete|status|list|clear|history}"
        echo ""
        echo "Commands:"
        echo "  add <description>       - Add a new task"
        echo "  start                   - Start the first pending task"
        echo "  complete                - Complete the current task"
        echo "  status                  - Show queue status"
        echo "  list                    - List all active tasks"
        echo "  clear                   - Clear all tasks"
        echo "  history [count]         - Show recent completions (default: 10)"
        ;;
esac
