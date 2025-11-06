#!/usr/bin/env python3
"""
Claude Code Statusline - Context + Queue 통합 모니터

실시간 Context 사용량 + Queue 상태 표시
"""

import json
import sys
from pathlib import Path


def get_queue_status() -> str:
    """
    현재 Queue 상태 가져오기

    Returns:
        "📋 N tasks" 형식 문자열
    """
    active_file = Path(".claude/queue/active.json")

    if not active_file.exists():
        return "📋 0"

    try:
        with open(active_file) as f:
            queue = json.load(f)
            task_count = len(queue.get("tasks", []))
            return f"📋 {task_count}"
    except (json.JSONDecodeError, FileNotFoundError):
        return "📋 0"


def parse_actual_usage_from_transcript(transcript_path: str) -> dict:
    """
    Transcript 파일에서 실제 토큰 사용량 파싱

    Args:
        transcript_path: transcript JSONL 파일 경로

    Returns:
        {"used": int, "limit": int} 또는 None
    """
    if not transcript_path or not Path(transcript_path).exists():
        return None

    try:
        # 최근 20줄만 읽기 (성능 최적화)
        with open(transcript_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            recent_lines = lines[-20:] if len(lines) > 20 else lines

        # 최신 usage 정보 찾기 (역순으로)
        for line in reversed(recent_lines):
            try:
                data = json.loads(line.strip())

                # assistant 메시지의 usage 필드 확인
                if data.get('type') == 'assistant':
                    usage = data.get('message', {}).get('usage', {})
                    if usage:
                        # input + cache_read = 실제 context 사용량
                        input_tokens = usage.get('input_tokens', 0)
                        cache_read = usage.get('cache_read_input_tokens', 0)
                        total_used = input_tokens + cache_read

                        # Sonnet 4.5 기본 limit: 200k
                        return {"used": total_used, "limit": 200000}

            except (json.JSONDecodeError, KeyError):
                continue

        return None

    except (FileNotFoundError, PermissionError):
        return None


def get_context_indicator(usage_pct: float) -> tuple:
    """
    Context 사용량 → 색상 + 막대

    Args:
        usage_pct: Context 사용량 (0-100)

    Returns:
        (color_emoji, bar_string)
    """
    if usage_pct < 50:
        return ("🟢", "█" * 8)
    elif usage_pct < 75:
        return ("🟡", "█" * 6 + "░" * 2)
    elif usage_pct < 90:
        return ("🟠", "█" * 4 + "░" * 4)
    elif usage_pct < 95:
        return ("🔴", "█" * 2 + "░" * 6)
    else:
        return ("🚨", "░" * 8)


def main():
    """
    Statusline 출력

    Input (stdin JSON):
        {
            "model": "claude-sonnet-4-5-20250929",
            "context_usage": {"used": 50000, "limit": 200000},
            "session_duration": 300,
            "lines_changed": 142
        }

    Output:
        [Claude Sonnet 4.5] 📋 3 | 🧠 🟢████████ 25% | 💰 2¢ ⏱ 5m 📝 +142
    """
    try:
        # stdin에서 JSON 읽기
        data = json.load(sys.stdin)

        # Model 이름 (간략화)
        model = data.get("model", "claude-sonnet-4-5-20250929")

        # model이 문자열인지 확인
        if isinstance(model, dict):
            model_name = str(model.get("name", "claude"))
        else:
            model_name = str(model)

        if "sonnet" in model_name.lower():
            model_display = "Claude Sonnet 4.5"
        elif "opus" in model_name.lower():
            model_display = "Claude Opus"
        else:
            model_display = "Claude"

        # Context 사용량 (transcript에서 실제 값 파싱)
        transcript_path = data.get("transcript_path", "")
        actual_usage = parse_actual_usage_from_transcript(transcript_path)

        if actual_usage:
            # transcript에서 실제 사용량을 가져옴
            used = actual_usage["used"]
            limit = actual_usage["limit"]
        else:
            # fallback: Claude Code가 전달한 값 사용
            context = data.get("context_usage", {"used": 0, "limit": 200000})
            used = context.get("used", 0)
            limit = context.get("limit", 200000)

        usage_pct = (used / limit * 100) if limit > 0 else 0

        color, bar = get_context_indicator(usage_pct)

        # Queue 상태
        queue_status = get_queue_status()

        # 세션 정보 (cost 객체에서 읽기)
        cost_data = data.get("cost", {})

        # 비용 (실제 비용 사용)
        cost_usd = cost_data.get("total_cost_usd", 0)
        cost_cents = int(cost_usd * 100)

        # Duration (밀리초 → 분)
        duration_ms = cost_data.get("total_duration_ms", 0)
        duration_min = int(duration_ms / 60000)

        # Lines changed (추가 - 삭제)
        lines_added = cost_data.get("total_lines_added", 0)
        lines_removed = cost_data.get("total_lines_removed", 0)
        net_lines = lines_added - lines_removed

        # 최종 Statusline (net_lines에 부호 추가)
        lines_sign = "+" if net_lines >= 0 else ""
        print(
            f"[{model_display}] {queue_status} | "
            f"🧠 {color}{bar} {usage_pct:.0f}% | "
            f"💰 {cost_cents}¢ ⏱ {duration_min}m 📝 {lines_sign}{net_lines}"
        )

    except json.JSONDecodeError:
        # JSON 파싱 실패 시 기본 출력
        queue_status = get_queue_status()
        print(f"[Claude] {queue_status} | 🧠 🟢████████ 0%")
    except Exception as e:
        # 기타 에러
        print(f"[Claude] ⚠️ Error: {e}")


if __name__ == "__main__":
    main()
