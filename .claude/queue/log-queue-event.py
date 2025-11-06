#!/usr/bin/env python3
"""
Queue 이벤트 LangFuse 로거

Queue 이벤트를 JSONL 형식으로 로깅하여 LangFuse 업로드 준비
"""

import json
import sys
import time
from pathlib import Path
from typing import Dict, Any

# LangFuse 로그 경로
LANGFUSE_LOG = Path("langfuse/logs/hook-execution.jsonl")


def log_queue_event(event_type: str, data: Dict[str, Any]) -> None:
    """
    Queue 이벤트 로깅

    Args:
        event_type: queue_add, queue_start, queue_complete
        data: 이벤트 데이터
    """
    # 로그 디렉토리 생성
    LANGFUSE_LOG.parent.mkdir(parents=True, exist_ok=True)

    # 로그 엔트리 생성
    log_entry = {
        "timestamp": time.time(),
        "event": event_type,
        "data": data
    }

    # JSONL 형식으로 추가
    with open(LANGFUSE_LOG, 'a', encoding='utf-8') as f:
        f.write(json.dumps(log_entry, ensure_ascii=False) + '\n')


def main():
    """
    메인 실행

    Usage:
        python3 log-queue-event.py queue_add '{"task_id": "task-123", ...}'
    """
    if len(sys.argv) < 3:
        print("Usage: log-queue-event.py <event_type> <data_json>", file=sys.stderr)
        sys.exit(1)

    event_type = sys.argv[1]
    data_json = sys.argv[2]

    try:
        data = json.loads(data_json)
        log_queue_event(event_type, data)
        print(f"✅ Logged: {event_type}")
    except json.JSONDecodeError as e:
        print(f"⚠️ Invalid JSON: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
