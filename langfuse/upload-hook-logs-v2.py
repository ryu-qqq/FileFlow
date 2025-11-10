#!/usr/bin/env python3
"""
Hook 로그를 LangFuse Event로 변환하여 업로드 (SDK v2.x 호환)

목적: hook-execution.jsonl 로그를 LangFuse로 전송하여 실시간 모니터링

사용법:
    # 최근 로그만 업로드 (기본)
    python3 scripts/langfuse/upload-hook-logs-v2.py

    # 전체 로그 업로드
    python3 scripts/langfuse/upload-hook-logs-v2.py --full

    # Dry-run (실제 전송 안 함)
    python3 scripts/langfuse/upload-hook-logs-v2.py --dry-run
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

    def __init__(self, dry_run: bool = False):
        """
        LangFuse 클라이언트 초기화

        Args:
            dry_run: True면 실제 전송하지 않고 출력만
        """
        self.dry_run = dry_run

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
        self.host = host
        print(f"✅ LangFuse 클라이언트 초기화 완료 (Host: {host}, Dry-run: {dry_run})")

    def load_upload_state(self) -> Dict:
        """이전 업로드 상태 로드"""
        if STATE_FILE.exists():
            with open(STATE_FILE, 'r') as f:
                return json.load(f)
        return {"last_uploaded_line": 0, "last_uploaded_timestamp": None}

    def save_upload_state(self, state: Dict):
        """업로드 상태 저장"""
        if not self.dry_run:
            STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
            with open(STATE_FILE, 'w') as f:
                json.dump(state, f, indent=2)

    def parse_hook_logs(self, start_line: int = 0) -> Dict[str, List[Dict]]:
        """
        Hook 로그를 파싱하여 세션별로 그룹화

        Args:
            start_line: 시작 라인 (이전에 업로드한 다음 라인부터)

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

    def create_hook_event(self, session_id: str, events: List[Dict]):
        """
        세션별 Hook 실행 Event 생성 (LangFuse SDK v2.x)

        Args:
            session_id: Claude Code 세션 ID
            events: 해당 세션의 이벤트 리스트
        """
        # 이벤트 타입별로 분류
        event_by_type = {}
        for e in events:
            event_type = e['event'].get('event', 'unknown')
            if event_type not in event_by_type:
                event_by_type[event_type] = []
            event_by_type[event_type].append(e['event'])

        # 프로젝트명 추출 (session_start 이벤트에서)
        session_start = event_by_type.get('session_start', [{}])[0]
        project_name = session_start.get('project', 'unknown-project')

        # 키워드 분석 이벤트
        keyword_analysis = event_by_type.get('keyword_analysis', [{}])[0]

        # Cache 주입 이벤트들
        cache_injections = event_by_type.get('cache_injection', [])

        # Serena Memory 로드 이벤트 (기본)
        serena_memory = event_by_type.get('serena_memory_load', [{}])[0]

        # Serena Memory 상세 로드 이벤트 (NEW)
        serena_memory_details = event_by_type.get('serena_memory_load_detail', [])

        # Serena Memory 목록 이벤트 (NEW)
        serena_memory_list = event_by_type.get('serena_memory_list', [])

        # Serena 도구 사용 이벤트 (NEW)
        serena_tool_usage = event_by_type.get('serena_tool_usage', [])

        # Skills 감지 이벤트 (NEW)
        skills_detected = event_by_type.get('skill_detected', [])

        # Skills 시작 이벤트 (NEW)
        skills_start = event_by_type.get('skill_start', [])

        # 검증 결과 이벤트
        validation_results = event_by_type.get('validation_result', [])

        # Event 데이터 구성 (프로젝트명 포함)
        event_name = f"{project_name}-hook-execution-{session_id[:8]}"
        event_input = {
            "project": project_name,
            "session_id": session_id,
            "context_score": keyword_analysis.get('context_score', 0),
            "detected_keywords": keyword_analysis.get('detected_keywords', []),
            "detected_layers": keyword_analysis.get('detected_layers', [])
        }

        event_output = {
            "cache_injection_success": len(cache_injections) > 0,
            "total_rules_injected": sum(ci.get('rules_loaded', 0) for ci in cache_injections),
            "layers_injected": len(set(ci.get('layer') for ci in cache_injections if ci.get('layer'))),
            "serena_memory_loaded": serena_memory.get('layers_loaded', 0) > 0,
            "serena_memory_files_loaded": len(serena_memory_details),
            "serena_total_memory_tokens": sum(smd.get('estimated_tokens', 0) for smd in serena_memory_details),
            "serena_tool_usage_count": len(serena_tool_usage),
            "skills_detected_count": len(skills_detected),
            "skills_used": [sd.get('skill') for sd in skills_detected],
            "validation_passed": all(vr.get('violations', []) == [] for vr in validation_results),
            "total_violations": sum(len(vr.get('violations', [])) for vr in validation_results)
        }

        event_metadata = {
            "project_name": project_name,
            "threshold": keyword_analysis.get('threshold', 25),
            "serena_layers_loaded": serena_memory.get('layers_loaded', 0),
            "serena_memory_files": [smd.get('memory_file') for smd in serena_memory_details],
            "serena_tools_used": [stu.get('tool') for stu in serena_tool_usage],
            "serena_total_memories": serena_memory_list[0].get('total_memories', 0) if serena_memory_list else 0,
            "skills_detected": [sd.get('skill') for sd in skills_detected],
            "skills_start_times": [ss.get('start_time') for ss in skills_start],
            "estimated_tokens": sum(ci.get('estimated_tokens', 0) for ci in cache_injections),
            "event_count": len(events)
        }

        # LangFuse Event 생성
        try:
            if self.dry_run:
                print(f"\n[DRY-RUN] Event: {event_name}")
                print(f"  Project: {project_name}")
                print(f"  Input: {json.dumps(event_input, indent=2)}")
                print(f"  Output: {json.dumps(event_output, indent=2)}")
            else:
                # Tags를 metadata에 포함 (프로젝트명 추가)
                event_metadata_with_tags = {
                    **event_metadata,
                    "tags": ["hook-system", "cache-injection", f"project-{project_name}", f"session-{session_id[:8]}"]
                }

                self.langfuse.create_event(
                    name=event_name,
                    input=event_input,
                    output=event_output,
                    metadata=event_metadata_with_tags
                )
                print(f"✅ Event 생성: {event_name}")
                print(f"   - Project: {project_name}")
                print(f"   - Detected Layers: {event_input['detected_layers']}")
                print(f"   - Rules Injected: {event_output['total_rules_injected']}")
                print(f"   - Violations: {event_output['total_violations']}")

        except Exception as e:
            print(f"❌ Event 생성 실패 ({project_name}/{session_id[:8]}): {e}")

    def upload_logs(self, full: bool = False):
        """
        Hook 로그를 LangFuse로 업로드

        Args:
            full: 전체 로그 업로드 여부 (False면 증분 업로드)
        """
        # 업로드 상태 로드
        state = self.load_upload_state()
        start_line = 0 if full else state['last_uploaded_line']

        print(f"📊 Hook 로그 파싱 중... (시작 라인: {start_line})")

        # 로그 파싱
        sessions = self.parse_hook_logs(start_line)

        if not sessions:
            print("⚠️ 업로드할 새로운 로그가 없습니다")
            return

        print(f"📤 {len(sessions)}개 세션의 로그를 LangFuse로 업로드 중...")

        # 세션별로 Event 생성
        max_line_num = 0
        for sid, events in sessions.items():
            self.create_hook_event(sid, events)
            # 가장 큰 라인 번호 추적
            max_line_num = max(max_line_num, max(e['line_num'] for e in events))

        # LangFuse flush (실제 전송)
        if not self.dry_run:
            self.langfuse.flush()

        # 업로드 상태 저장
        state['last_uploaded_line'] = max_line_num
        state['last_uploaded_timestamp'] = datetime.now().isoformat()
        self.save_upload_state(state)

        print(f"\n✅ 업로드 완료! ({max_line_num} 라인까지 처리)")
        print(f"   → LangFuse Dashboard: {self.host}/projects/claude-spring-standards")


def main():
    """메인 함수"""
    import argparse

    parser = argparse.ArgumentParser(description="Hook 로그를 LangFuse로 업로드")
    parser.add_argument('--full', action='store_true', help="전체 로그 업로드 (증분 업로드 비활성화)")
    parser.add_argument('--dry-run', action='store_true', help="실제 전송하지 않고 출력만")

    args = parser.parse_args()

    try:
        uploader = HookLogUploader(dry_run=args.dry_run)
        uploader.upload_logs(full=args.full)

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
