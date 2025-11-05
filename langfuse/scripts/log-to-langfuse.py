#!/usr/bin/env python3
"""
Structured Logger + LangFuse Auto-Upload

Hook에서 직접 호출하여:
1. 구조화된 JSONL 로그 기록
2. LangFuse에 실시간 업로드 (선택적)

Usage:
    # 로그만 기록 (LangFuse 업로드 X)
    python3 log-to-langfuse.py log --event-type user_prompt_submit --data '{...}'

    # 로그 기록 + LangFuse 업로드
    python3 log-to-langfuse.py log --event-type user_prompt_submit --data '{...}' --upload
"""

import json
import os
import sys
import argparse
from datetime import datetime
from pathlib import Path
from typing import Dict, Any

# LangFuse 업로드 (선택적)
try:
    import requests
    from requests.auth import HTTPBasicAuth
    LANGFUSE_AVAILABLE = True
except ImportError:
    LANGFUSE_AVAILABLE = False

# 설정
PROJECT_ROOT = Path(__file__).parent.parent.parent
JSONL_LOG_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "hook-execution.jsonl"
SESSION_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "current-session.json"

class StructuredLogger:
    """구조화된 로그 기록 + LangFuse 업로드"""

    def __init__(self, upload_enabled: bool = False):
        self.upload_enabled = upload_enabled and LANGFUSE_AVAILABLE
        self.langfuse_client = None

        # LangFuse 클라이언트 초기화
        if self.upload_enabled:
            public_key = os.getenv("LANGFUSE_PUBLIC_KEY")
            secret_key = os.getenv("LANGFUSE_SECRET_KEY")
            host = os.getenv("LANGFUSE_HOST", "https://us.cloud.langfuse.com")

            if not public_key or not secret_key:
                print("⚠️ LANGFUSE_PUBLIC_KEY or LANGFUSE_SECRET_KEY not set. Skipping upload.", file=sys.stderr)
                self.upload_enabled = False
            else:
                self.langfuse_client = LangFuseClient(public_key, secret_key, host)

    def log_event(self, event_type: str, data: Dict[str, Any]):
        """
        이벤트 로그 기록 (JSONL 형식)

        Args:
            event_type: "user_prompt_submit", "after_tool_use", "serena_memory_load", etc.
            data: 이벤트 데이터
        """
        # 세션 정보 로드
        session = self._load_session()

        # 구조화된 로그 엔트리
        log_entry = {
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "session_id": session["session_id"],
            "trace_id": session["trace_id"],
            "event_type": event_type,
            "data": data
        }

        # JSONL 로그 기록
        self._write_jsonl(log_entry)

        # LangFuse 업로드 (선택적)
        if self.upload_enabled:
            self._upload_to_langfuse(log_entry)

    def _load_session(self) -> Dict[str, str]:
        """현재 세션 정보 로드 (또는 생성)"""
        if SESSION_FILE.exists():
            with open(SESSION_FILE, 'r') as f:
                return json.load(f)
        else:
            # 새 세션 생성
            import uuid
            session_id = str(uuid.uuid4())
            trace_id = f"trace-{session_id[:8]}"

            session = {
                "session_id": session_id,
                "trace_id": trace_id,
                "started_at": datetime.utcnow().isoformat() + "Z"
            }

            SESSION_FILE.parent.mkdir(parents=True, exist_ok=True)
            with open(SESSION_FILE, 'w') as f:
                json.dump(session, f)

            return session

    def _write_jsonl(self, log_entry: Dict[str, Any]):
        """JSONL 로그 파일에 기록"""
        JSONL_LOG_FILE.parent.mkdir(parents=True, exist_ok=True)

        with open(JSONL_LOG_FILE, 'a') as f:
            f.write(json.dumps(log_entry) + '\n')

    def _upload_to_langfuse(self, log_entry: Dict[str, Any]):
        """LangFuse에 실시간 업로드"""
        if not self.langfuse_client:
            return

        event_type = log_entry["event_type"]

        # Event 타입에 따라 Trace 또는 Observation 생성
        if event_type == "user_prompt_submit":
            # 새로운 Trace 시작
            self.langfuse_client.create_trace({
                "id": log_entry["trace_id"],
                "name": f"Claude Session: {log_entry['data'].get('prompt', '')[:50]}",
                "timestamp": log_entry["timestamp"],
                "metadata": {
                    "session_id": log_entry["session_id"],
                    "context_score": log_entry["data"].get("context_score"),
                    "detected_layers": log_entry["data"].get("detected_layers", [])
                },
                "tags": ["claude-code", "hook"]
            })

        # Observation 생성
        self.langfuse_client.create_observation({
            "traceId": log_entry["trace_id"],
            "name": event_type,
            "startTime": log_entry["timestamp"],
            "level": "DEFAULT",
            "metadata": log_entry["data"],
            "input": log_entry["data"].get("input"),
            "output": log_entry["data"].get("output")
        })

class LangFuseClient:
    """LangFuse API 클라이언트 (requests 사용)"""

    def __init__(self, public_key: str, secret_key: str, host: str):
        self.host = host.rstrip('/')
        self.session = requests.Session()
        self.session.auth = HTTPBasicAuth(public_key, secret_key)
        self.session.headers.update({'Content-Type': 'application/json'})

    def create_trace(self, trace_data: Dict[str, Any]):
        """Trace 생성"""
        url = f"{self.host}/api/public/ingestion"
        batch = {
            'batch': [{
                'id': trace_data['id'],
                'type': 'trace-create',
                'timestamp': trace_data['timestamp'],
                'body': trace_data
            }]
        }

        try:
            response = self.session.post(url, json=batch, timeout=5)
            response.raise_for_status()
        except Exception as e:
            print(f"⚠️ LangFuse trace upload failed: {e}", file=sys.stderr)

    def create_observation(self, obs_data: Dict[str, Any]):
        """Observation 생성"""
        url = f"{self.host}/api/public/ingestion"

        # Event ID 생성
        event_id = f"{obs_data['traceId']}-{obs_data['name']}-{obs_data['startTime']}"

        batch = {
            'batch': [{
                'id': event_id,
                'type': 'event-create',
                'timestamp': obs_data['startTime'],
                'body': {
                    'id': event_id,
                    **obs_data
                }
            }]
        }

        try:
            response = self.session.post(url, json=batch, timeout=5)
            response.raise_for_status()
        except Exception as e:
            print(f"⚠️ LangFuse observation upload failed: {e}", file=sys.stderr)

def main():
    parser = argparse.ArgumentParser(description="Structured Logger + LangFuse Auto-Upload")
    parser.add_argument("command", choices=["log"], help="Command to execute")
    parser.add_argument("--event-type", required=True, help="Event type (e.g., user_prompt_submit)")
    parser.add_argument("--data", required=True, help="Event data (JSON string)")
    parser.add_argument("--upload", action="store_true", help="Upload to LangFuse")

    args = parser.parse_args()

    # 데이터 파싱
    try:
        data = json.loads(args.data)
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON data: {e}", file=sys.stderr)
        sys.exit(1)

    # 로그 기록
    logger = StructuredLogger(upload_enabled=args.upload)
    logger.log_event(args.event_type, data)

    print(f"✅ Logged: {args.event_type}")
    if args.upload and logger.upload_enabled:
        print(f"✅ Uploaded to LangFuse")

if __name__ == "__main__":
    main()
