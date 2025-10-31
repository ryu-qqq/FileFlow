#!/bin/bash
# Slash Command 로깅 헬퍼
#
# 용도: Slash Command 실행을 Hook logs에 기록
# 호출: log-slash-command.sh <command_name> <status> [metadata]

set -euo pipefail

COMMAND_NAME="${1:-unknown}"
STATUS="${2:-start}"
METADATA="${3:-{}}"

# 프로젝트 루트 및 로그 파일
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
LOG_FILE="$PROJECT_ROOT/.claude/hooks/logs/hook-execution.jsonl"

# 로그 디렉토리 생성
mkdir -p "$(dirname "$LOG_FILE")"

# 세션 ID 생성 (타임스탬프 기반)
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%6N")
SESSION_ID="slash-${TIMESTAMP//[:-]/}"

# JSON 로그 생성
cat <<EOF >> "$LOG_FILE"
{"timestamp": "$TIMESTAMP", "event": "slash_command_$STATUS", "command": "$COMMAND_NAME", "session_id": "$SESSION_ID", "metadata": $METADATA}
EOF

# 성공 메시지 (stderr로 출력하여 Hook output과 분리)
>&2 echo "✅ Logged slash command: $COMMAND_NAME ($STATUS)"
