#!/usr/bin/env python3

"""
=====================================================
Hook Log Summary Tool
Purpose: Summarize hook-execution.jsonl logs
Usage: python3 summarize-hook-logs.py [--sessions N] [--verbose]
=====================================================
"""

import json
import sys
from pathlib import Path
from datetime import datetime
from collections import defaultdict, Counter
from typing import List, Dict

# 경로
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent.parent.parent
LOG_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "hook-execution.jsonl"

# 색상
class Color:
    GREEN = '\033[0;32m'
    RED = '\033[0;31m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color


def load_logs() -> List[Dict]:
    """JSONL 로그 파일 로드"""
    if not LOG_FILE.exists():
        print(f"{Color.RED}❌ 로그 파일 없음: {LOG_FILE}{Color.NC}")
        print(f"\n💡 Hook이 아직 실행되지 않았습니다.")
        print(f"   Claude Code를 실행하고 프롬프트를 입력하세요.")
        sys.exit(1)

    logs = []
    with open(LOG_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            try:
                logs.append(json.loads(line.strip()))
            except json.JSONDecodeError:
                continue

    return logs


def group_by_session(logs: List[Dict]) -> Dict[str, List[Dict]]:
    """세션별로 로그 그룹화"""
    sessions = defaultdict(list)

    for log in logs:
        session_id = log.get('session_id', 'unknown')
        sessions[session_id].append(log)

    return dict(sessions)


def analyze_session(session_logs: List[Dict]) -> Dict:
    """개별 세션 분석"""
    analysis = {
        'session_id': session_logs[0].get('session_id', 'unknown'),
        'start_time': session_logs[0].get('timestamp', ''),
        'events': Counter([log.get('event') for log in session_logs]),
        'layers_detected': set(),
        'keywords_detected': set(),
        'context_score': 0,
        'rules_injected': 0,
        'serena_loaded': False,
        'cache_files': []
    }

    for log in session_logs:
        event = log.get('event')

        if event == 'keyword_analysis':
            analysis['context_score'] = log.get('context_score', 0)
            analysis['layers_detected'] = set(log.get('detected_layers', []))
            analysis['keywords_detected'] = set(log.get('detected_keywords', []))

        elif event == 'serena_memory_load':
            analysis['serena_loaded'] = True

        elif event == 'cache_injection':
            analysis['rules_injected'] += log.get('rules_loaded', 0)
            analysis['cache_files'].extend(log.get('cache_files', []))

    return analysis


def print_summary(logs: List[Dict], recent_sessions: int = 5, verbose: bool = False):
    """로그 요약 출력"""
    print(f"\n{Color.CYAN}🔍 Hook 로그 요약{Color.NC}")
    print("=" * 60)

    # 1. 전체 통계
    print(f"\n{Color.BLUE}## 1. 전체 통계{Color.NC}")
    print("-" * 60)
    print(f"총 로그 수: {len(logs)}")

    event_counts = Counter([log.get('event') for log in logs])
    print(f"\n이벤트 분포:")
    for event, count in event_counts.most_common():
        print(f"  - {event}: {count}")

    # 2. 세션별 분석
    sessions = group_by_session(logs)
    print(f"\n{Color.BLUE}## 2. 세션 분석{Color.NC}")
    print("-" * 60)
    print(f"총 세션 수: {len(sessions)}")

    # 최근 N개 세션
    recent_session_ids = sorted(
        sessions.keys(),
        key=lambda sid: sessions[sid][0].get('timestamp', ''),
        reverse=True
    )[:recent_sessions]

    print(f"\n최근 {recent_sessions}개 세션:")

    for i, session_id in enumerate(recent_session_ids, 1):
        session_logs = sessions[session_id]
        analysis = analyze_session(session_logs)

        print(f"\n{Color.CYAN}### 세션 {i}: {session_id[:20]}...{Color.NC}")
        print(f"  시작 시간: {analysis['start_time']}")
        print(f"  이벤트 수: {len(session_logs)}")
        print(f"  컨텍스트 점수: {analysis['context_score']}")

        if analysis['layers_detected']:
            print(f"  감지된 레이어: {', '.join(analysis['layers_detected'])}")

        if analysis['keywords_detected']:
            keywords_str = ', '.join(list(analysis['keywords_detected'])[:5])
            if len(analysis['keywords_detected']) > 5:
                keywords_str += f", ... ({len(analysis['keywords_detected'])} 총)"
            print(f"  감지된 키워드: {keywords_str}")

        # Serena 로드 여부
        if analysis['serena_loaded']:
            print(f"  {Color.GREEN}✅ Serena 메모리 로드됨{Color.NC}")
        else:
            print(f"  {Color.YELLOW}⚠️  Serena 메모리 로드 안됨{Color.NC}")

        # Cache injection
        if analysis['rules_injected'] > 0:
            print(f"  {Color.GREEN}✅ Cache 규칙 주입: {analysis['rules_injected']}개{Color.NC}")
        else:
            print(f"  {Color.YELLOW}⚠️  Cache 규칙 주입 안됨 (점수 낮음){Color.NC}")

        # Verbose 모드: 상세 정보
        if verbose and analysis['cache_files']:
            print(f"\n  주입된 규칙 파일 ({len(analysis['cache_files'])}개):")
            for cache_file in analysis['cache_files'][:3]:
                print(f"    - {cache_file}")
            if len(analysis['cache_files']) > 3:
                print(f"    - ... ({len(analysis['cache_files']) - 3}개 더)")

    # 3. Serena 메모리 사용 통계
    print(f"\n{Color.BLUE}## 3. Serena 메모리 사용 통계{Color.NC}")
    print("-" * 60)

    serena_events = [log for log in logs if log.get('event') == 'serena_memory_load']

    if serena_events:
        print(f"{Color.GREEN}✅ Serena 메모리 로드 이벤트: {len(serena_events)}회{Color.NC}")

        # 최근 Serena 로드
        recent_serena = serena_events[-1]
        print(f"\n최근 로드:")
        print(f"  시간: {recent_serena.get('timestamp')}")
        print(f"  세션: {recent_serena.get('session_id', 'unknown')[:20]}...")
        print(f"  로드된 레이어 수: {recent_serena.get('layers_loaded', 0)}")
    else:
        print(f"{Color.YELLOW}⚠️  Serena 메모리 로드 이벤트 없음{Color.NC}")
        print(f"\n💡 원인:")
        print(f"   1. 키워드가 감지되지 않음 (점수 < 25)")
        print(f"   2. Hook이 실행되지 않음")
        print(f"   3. user-prompt-submit.sh에 Serena 로드 코드 없음")

    # 4. Cache 주입 통계
    print(f"\n{Color.BLUE}## 4. Cache 규칙 주입 통계{Color.NC}")
    print("-" * 60)

    cache_events = [log for log in logs if log.get('event') == 'cache_injection']

    if cache_events:
        total_rules = sum(log.get('rules_loaded', 0) for log in cache_events)
        print(f"{Color.GREEN}✅ Cache 규칙 주입 이벤트: {len(cache_events)}회{Color.NC}")
        print(f"   총 주입된 규칙 수: {total_rules}개")

        # 레이어별 통계
        layer_counts = Counter([log.get('layer') for log in cache_events])
        print(f"\n레이어별 주입 횟수:")
        for layer, count in layer_counts.most_common():
            print(f"  - {layer}: {count}회")
    else:
        print(f"{Color.YELLOW}⚠️  Cache 규칙 주입 이벤트 없음{Color.NC}")

    # 5. 권장 사항
    print(f"\n{Color.BLUE}## 5. 권장 사항{Color.NC}")
    print("-" * 60)

    if not serena_events and len(sessions) > 0:
        print(f"{Color.YELLOW}⚠️  Serena 메모리가 사용되지 않고 있습니다.{Color.NC}")
        print(f"\n💡 해결 방법:")
        print(f"   1. 검증 도구 실행: bash .claude/hooks/scripts/verify-serena-memories.sh")
        print(f"   2. /cc:load 명령어 실행 (세션 시작 시)")
        print(f"   3. 키워드 포함하여 프롬프트 작성 (domain, usecase, controller 등)")

    if not cache_events and len(sessions) > 0:
        print(f"\n{Color.YELLOW}⚠️  Cache 규칙이 주입되지 않고 있습니다.{Color.NC}")
        print(f"\n💡 해결 방법:")
        print(f"   1. 컨텍스트 점수 확인 (threshold: 25점)")
        print(f"   2. 키워드 포함하여 프롬프트 작성")
        print(f"   3. Cache 빌드 확인: python3 .claude/hooks/scripts/build-rule-cache.py")

    if serena_events and cache_events:
        print(f"{Color.GREEN}✅ 시스템이 정상적으로 작동하고 있습니다!{Color.NC}")
        print(f"\n💡 최적 사용법:")
        print(f"   - 세션 시작: /cc:load")
        print(f"   - 키워드 사용: domain, usecase, controller, entity 등")
        print(f"   - Serena 메모리가 최우선, Cache는 보조 참조")

    print("")


def main():
    import argparse

    parser = argparse.ArgumentParser(description='Hook 로그 요약 도구')
    parser.add_argument('--sessions', type=int, default=5, help='표시할 최근 세션 수 (기본: 5)')
    parser.add_argument('--verbose', '-v', action='store_true', help='상세 정보 표시')

    args = parser.parse_args()

    logs = load_logs()

    if not logs:
        print(f"{Color.YELLOW}⚠️  로그가 비어있습니다.{Color.NC}")
        print(f"\n💡 Claude Code를 실행하고 프롬프트를 입력하세요.")
        sys.exit(0)

    print_summary(logs, recent_sessions=args.sessions, verbose=args.verbose)


if __name__ == '__main__':
    main()
