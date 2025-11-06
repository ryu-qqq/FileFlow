#!/usr/bin/env python3
"""
TodoWrite와 Queue 시스템 양방향 동기화

Claude Code의 TodoWrite 도구와 Queue 시스템을 자동 동기화합니다.
- TodoWrite in_progress → Queue add + start
- TodoWrite completed → Queue complete
"""

import json
import subprocess
import sys
from pathlib import Path
from typing import List, Dict

# Queue Manager 경로
QUEUE_MANAGER = Path(".claude/queue/queue-manager.sh")


def run_queue_command(command: str, *args) -> str:
    """
    Queue Manager 명령 실행

    Args:
        command: add, start, complete, status 등
        args: 추가 인자

    Returns:
        명령 출력
    """
    if not QUEUE_MANAGER.exists():
        return ""

    try:
        result = subprocess.run(
            ["bash", str(QUEUE_MANAGER), command, *args],
            capture_output=True,
            text=True,
            check=True
        )
        return result.stdout.strip()
    except subprocess.CalledProcessError:
        return ""


def sync_todo_to_queue(todos: List[Dict]) -> None:
    """
    TodoWrite 항목을 Queue에 동기화

    Args:
        todos: TodoWrite todos 배열
    """
    for todo in todos:
        content = todo.get("content", "")
        status = todo.get("status", "pending")

        if status == "in_progress":
            # Queue에 추가 및 시작
            task_id = run_queue_command("add", content, "0", "[]")
            if task_id:
                run_queue_command("start")
                print(f"✅ Queue synced: {content} (started)")

        elif status == "completed":
            # Queue에서 완료 처리
            completed = run_queue_command("complete")
            if completed:
                print(f"✅ Queue synced: {content} (completed)")


def main():
    """
    메인 실행

    Usage:
        echo '{"todos": [...]}' | python3 sync-todo-to-queue.py
    """
    try:
        # stdin에서 TodoWrite 데이터 읽기
        data = json.load(sys.stdin)
        todos = data.get("todos", [])

        if todos:
            sync_todo_to_queue(todos)

    except json.JSONDecodeError:
        print("⚠️ Invalid JSON input", file=sys.stderr)
    except Exception as e:
        print(f"⚠️ Error: {e}", file=sys.stderr)


if __name__ == "__main__":
    main()
