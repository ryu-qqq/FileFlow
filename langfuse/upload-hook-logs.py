#!/usr/bin/env python3
"""
Hook 로그를 LangFuse Trace로 변환하여 업로드

목적: hook-execution.jsonl 로그를 LangFuse로 전송하여 실시간 모니터링

사용법:
    # 최근 로그만 업로드 (기본)
    python3 scripts/langfuse/upload-hook-logs.py

    # 전체 로그 업로드
    python3 scripts/langfuse/upload-hook-logs.py --full

    # 특정 세션만 업로드
    python3 scripts/langfuse/upload-hook-logs.py --session 1761875155-77368
"""

import json
import os
import sys
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional
from langfuse import Langfuse
from dotenv import load_dotenv

# 프로젝트 루트 경로
PROJECT_ROOT = Path(__file__).parent.parent.parent
HOOK_LOG_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "hook-execution.jsonl"
STATE_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "langfuse-upload-state.json"

# .env 파일 로드
load_dotenv(PROJECT_ROOT / ".env")


class HookLogUploader:
    """Hook 로그를 LangFuse로 업로드하는 클래스"""

    def __init__(self):
        """LangFuse 클라이언트 초기화"""
        # 환경 변수에서 API 키 로드
        public_key = os.getenv('LANGFUSE_PUBLIC_KEY')
        secret_key = os.getenv('LANGFUSE_SECRET_KEY')
        host = os.getenv('LANGFUSE_HOST', 'https://us.cloud.langfuse.com')

        if not public_key or not secret_key:
            raise ValueError("LANGFUSE_PUBLIC_KEY 및 LANGFUSE_SECRET_KEY 환경 변수가 필요합니다")

        self.langfuse = Langfuse(
            public_key=public_key,
            secret_key=secret_key,
            host=host
        )
        print(f"✅ LangFuse 클라이언트 초기화 완료 (Host: {host})")

    def load_upload_state(self) -> Dict:
        """이전 업로드 상태 로드"""
        if STATE_FILE.exists():
            with open(STATE_FILE, 'r') as f:
                return json.load(f)
        return {"last_uploaded_line": 0, "last_uploaded_timestamp": None}

    def save_upload_state(self, state: Dict):
        """업로드 상태 저장"""
        STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
        with open(STATE_FILE, 'w') as f:
            json.dump(state, f, indent=2)

    def parse_hook_logs(self, start_line: int = 0, session_id: Optional[str] = None) -> Dict[str, List[Dict]]:
        """
        Hook 로그를 파싱하여 세션별로 그룹화

        Args:
            start_line: 시작 라인 (이전에 업로드한 다음 라인부터)
            session_id: 특정 세션만 필터링 (None이면 전체)

        Returns:
            세션별로 그룹화된 이벤트 딕셔너리
        """
        sessions = {}

        if not HOOK_LOG_FILE.exists():
            print(f"⚠️ Hook 로그 파일이 없습니다: {HOOK_LOG_FILE}")
            return sessions

        with open(HOOK_LOG_FILE, 'r') as f:
            for line_num, line in enumerate(f, start=1):
                # 이미 업로드한 라인은 스킵
                if line_num <= start_line:
                    continue

                try:
                    event = json.loads(line.strip())

                    # 세션 ID 추출
                    sid = event.get('session_id', 'unknown')

                    # 특정 세션만 필터링
                    if session_id and sid != session_id:
                        continue

                    # 세션별로 이벤트 그룹화
                    if sid not in sessions:
                        sessions[sid] = []

                    sessions[sid].append({
                        'event': event,
                        'line_num': line_num
                    })

                except json.JSONDecodeError as e:
                    print(f"⚠️ JSON 파싱 실패 (Line {line_num}): {e}")
                    continue

        return sessions

    def create_hook_execution_trace(self, session_id: str, events: List[Dict]):
        """
        세션별 Hook 실행 Trace 생성

        Args:
            session_id: Claude Code 세션 ID
            events: 해당 세션의 이벤트 리스트
        """
        # 세션 시작 이벤트 찾기
        session_start = next((e['event'] for e in events if e['event'].get('event') == 'session_start'), None)
        if not session_start:
            print(f"⚠️ Session {session_id}: session_start 이벤트 없음")
            return

        # Trace 생성
        trace_name = f"hook-execution-{session_id}"
        trace_timestamp = datetime.fromisoformat(session_start['timestamp'])

        # 키워드 분석 이벤트
        keyword_analysis = next((e['event'] for e in events if e['event'].get('event') == 'keyword_analysis'), {})

        # Cache 주입 이벤트들
        cache_injections = [e['event'] for e in events if e['event'].get('event') == 'cache_injection']

        # Serena Memory 로드 이벤트
        serena_memory = next((e['event'] for e in events if e['event'].get('event') == 'serena_memory_load'), {})

        # 검증 결과 이벤트
        validation_results = [e['event'] for e in events if e['event'].get('event') == 'validation_result']

        # Trace 입력 데이터
        trace_input = {
            "session_id": session_id,
            "context_score": keyword_analysis.get('context_score', 0),
            "detected_keywords": keyword_analysis.get('detected_keywords', []),
            "detected_layers": keyword_analysis.get('detected_layers', [])
        }

        # Trace 출력 데이터
        trace_output = {
            "cache_injection_success": len(cache_injections) > 0,
            "total_rules_injected": sum(ci.get('rules_loaded', 0) for ci in cache_injections),
            "layers_injected": len(set(ci.get('layer') for ci in cache_injections if ci.get('layer'))),
            "serena_memory_loaded": serena_memory.get('layers_loaded', 0) > 0,
            "validation_passed": all(vr.get('violations', []) == [] for vr in validation_results)
        }

        # Trace 메타데이터
        trace_metadata = {
            "threshold": keyword_analysis.get('threshold', 25),
            "decision": next((e['event'].get('action') for e in events if e['event'].get('event') == 'decision'), 'unknown'),
            "serena_layers_loaded": serena_memory.get('layers_loaded', 0),
            "estimated_tokens": sum(ci.get('estimated_tokens', 0) for ci in cache_injections),
            "total_violations": sum(len(vr.get('violations', [])) for vr in validation_results)
        }

        # LangFuse Trace 생성
        try:
            trace = self.langfuse.trace(
                id=f"hook-{session_id}",
                name=trace_name,
                input=trace_input,
                output=trace_output,
                metadata=trace_metadata,
                tags=["hook-system", "cache-injection"],
                session_id=session_id
            )

            # Observation 추가: Cache Injection
            for ci in cache_injections:
                layer = ci.get('layer', 'unknown')
                trace.span(
                    name=f"cache-injection-{layer}",
                    input={"layer": layer, "priority_filter": ci.get('priority_filter', 'all')},
                    output={
                        "rules_loaded": ci.get('rules_loaded', 0),
                        "estimated_tokens": ci.get('estimated_tokens', 0),
                        "cache_files": ci.get('cache_files', [])
                    },
                    metadata={"total_rules_available": ci.get('total_rules_available', 0)},
                    timestamp=datetime.fromisoformat(ci['timestamp'])
                )

            # Observation 추가: Validation Results
            for vr in validation_results:
                trace.span(
                    name="validation",
                    input={
                        "validation_type": vr.get('validation_type', 'unknown'),
                        "file_path": vr.get('file_path', ''),
                        "layer": vr.get('layer', '')
                    },
                    output={
                        "violations": vr.get('violations', []),
                        "passed_rules": vr.get('passed_rules', []),
                        "validation_time_ms": vr.get('validation_time_ms', 0)
                    },
                    timestamp=datetime.fromisoformat(vr['timestamp']) if 'timestamp' in vr else None
                )

            print(f"✅ Trace 생성 완료: {trace_name}")
            print(f"   - Detected Layers: {trace_input['detected_layers']}")
            print(f"   - Rules Injected: {trace_output['total_rules_injected']}")
            print(f"   - Violations: {trace_metadata['total_violations']}")

        except Exception as e:
            print(f"❌ Trace 생성 실패 ({session_id}): {e}")

    def upload_logs(self, full: bool = False, session_id: Optional[str] = None, delete_after_upload: bool = True):
        """
        Hook 로그를 LangFuse로 업로드

        Args:
            full: 전체 로그 업로드 여부 (False면 증분 업로드)
            session_id: 특정 세션만 업로드 (None이면 전체)
            delete_after_upload: 업로드 성공 후 로그 파일 삭제 여부 (기본값: True)
        """
        # 업로드 상태 로드
        state = self.load_upload_state()
        start_line = 0 if full else state['last_uploaded_line']

        print(f"📊 Hook 로그 파싱 중... (시작 라인: {start_line})")

        # 로그 파싱
        sessions = self.parse_hook_logs(start_line, session_id)

        if not sessions:
            print("⚠️ 업로드할 새로운 로그가 없습니다")
            return

        print(f"📤 {len(sessions)}개 세션의 로그를 LangFuse로 업로드 중...")

        # 세션별로 Trace 생성
        max_line_num = 0
        for sid, events in sessions.items():
            self.create_hook_execution_trace(sid, events)
            # 가장 큰 라인 번호 추적
            max_line_num = max(max_line_num, max(e['line_num'] for e in events))

        # LangFuse flush (실제 전송)
        self.langfuse.flush()

        # 업로드 상태 저장
        state['last_uploaded_line'] = max_line_num
        state['last_uploaded_timestamp'] = datetime.now().isoformat()
        self.save_upload_state(state)

        print(f"✅ 업로드 완료! ({max_line_num} 라인까지 처리)")

        # 업로드 성공 후 로그 삭제 (옵션)
        if delete_after_upload:
            try:
                if HOOK_LOG_FILE.exists():
                    HOOK_LOG_FILE.unlink()
                    print(f"🗑️ 로그 파일 삭제 완료: {HOOK_LOG_FILE}")

                    # State 파일 리셋 (새로운 로그는 0부터 시작)
                    state['last_uploaded_line'] = 0
                    self.save_upload_state(state)
                    print(f"♻️ 업로드 상태 리셋 완료")
            except Exception as e:
                print(f"⚠️ 로그 파일 삭제 실패: {e}")
                print(f"   → 로그는 보존되었습니다: {HOOK_LOG_FILE}")


def main():
    """메인 함수"""
    import argparse

    parser = argparse.ArgumentParser(description="Hook 로그를 LangFuse로 업로드")
    parser.add_argument('--full', action='store_true', help="전체 로그 업로드 (증분 업로드 비활성화)")
    parser.add_argument('--session', type=str, help="특정 세션만 업로드")
    parser.add_argument('--keep-logs', action='store_true', help="업로드 후 로그 파일 보존 (기본: 삭제)")

    args = parser.parse_args()

    try:
        uploader = HookLogUploader()
        # --keep-logs 옵션이 있으면 delete_after_upload=False
        uploader.upload_logs(
            full=args.full,
            session_id=args.session,
            delete_after_upload=not args.keep_logs
        )

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
