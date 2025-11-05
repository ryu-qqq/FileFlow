#!/usr/bin/env python3
"""
Queue + Hook 통합 메트릭 집계 및 LangFuse 업로드

Purpose:
1. Hook 로그 (hook-execution.jsonl) 파싱
2. 큐 데이터 (work-queue.json) 파싱
3. 두 데이터 병합하여 완전한 개발 효율 분석
4. LangFuse Ingestion API로 업로드

Usage:
    # 데이터 집계만 (업로드 X)
    python3 aggregate-queue-metrics.py --dry-run

    # 집계 + LangFuse 업로드
    python3 aggregate-queue-metrics.py

    # 특정 세션만 분석
    python3 aggregate-queue-metrics.py --session-id abc123
"""

import json
import os
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Any, Optional
import requests
from requests.auth import HTTPBasicAuth

# 경로 설정
PROJECT_ROOT = Path(__file__).parent.parent.parent
HOOK_LOG_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "hook-execution.jsonl"
QUEUE_FILE = PROJECT_ROOT / ".claude" / "work-queue.json"
OUTPUT_FILE = PROJECT_ROOT / "langfuse" / "data" / "queue-metrics.json"


class QueueMetricsAggregator:
    """큐 메트릭 + Hook 로그 통합 집계"""

    def __init__(self):
        self.hook_logs: List[Dict] = []
        self.queue_data: Dict = {}
        self.aggregated_data: List[Dict] = []

    def load_data(self):
        """데이터 로드"""
        # Hook 로그 로드
        if HOOK_LOG_FILE.exists():
            self.hook_logs = []
            with open(HOOK_LOG_FILE, 'r') as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        log_entry = json.loads(line)
                        self.hook_logs.append(log_entry)
                    except json.JSONDecodeError as e:
                        # 파싱 실패한 라인은 무시
                        print(f"⚠️ 로그 파싱 실패: {str(e)[:50]}...", file=sys.stderr)
                        continue
            print(f"✅ Hook 로그 로드: {len(self.hook_logs)}개")
        else:
            print("⚠️ Hook 로그 파일 없음")

        # 큐 데이터 로드
        if QUEUE_FILE.exists():
            with open(QUEUE_FILE, 'r') as f:
                self.queue_data = json.load(f)
            print(f"✅ 큐 데이터 로드: {len(self.queue_data.get('completed', []))}개 완료 작업")
        else:
            print("⚠️ 큐 파일 없음")

    def aggregate(self) -> List[Dict]:
        """
        Hook 로그 + 큐 데이터 병합

        병합 로직:
        1. 세션별로 Hook 로그 그룹화
        2. 각 세션과 매칭되는 큐 작업 찾기 (시간 기반)
        3. 통합 메트릭 생성
        """
        # 세션별로 Hook 로그 그룹화
        sessions = self._group_by_session(self.hook_logs)

        # 완료된 큐 작업
        completed_tasks = self.queue_data.get('completed', [])

        # 병합
        for session_id, logs in sessions.items():
            # 세션 시작/종료 시간
            start_time = logs[0]['timestamp']
            end_time = logs[-1]['timestamp']

            # 시간 범위 내의 큐 작업 찾기
            matching_tasks = self._find_matching_tasks(
                completed_tasks, start_time, end_time
            )

            # 통합 메트릭 생성
            integrated_metric = self._create_integrated_metric(
                session_id, logs, matching_tasks
            )

            self.aggregated_data.append(integrated_metric)

        print(f"✅ 집계 완료: {len(self.aggregated_data)}개 세션")
        return self.aggregated_data

    def _group_by_session(self, logs: List[Dict]) -> Dict[str, List[Dict]]:
        """세션 ID별로 로그 그룹화"""
        sessions = {}
        for log in logs:
            # session_id 추출 (다양한 형식 지원)
            session_id = log.get('session_id')

            # session_id가 없으면 raw 데이터에서 추출 시도
            if not session_id and 'raw' in log:
                try:
                    raw_data = json.loads(log['raw'])
                    if 'session_id' in raw_data:
                        session_id = raw_data['session_id']
                except:
                    pass

            # session_id가 있으면 그룹화
            if session_id:
                if session_id not in sessions:
                    sessions[session_id] = []
                sessions[session_id].append(log)

        return sessions

    def _find_matching_tasks(
        self, tasks: List[Dict], start_time: str, end_time: str
    ) -> List[Dict]:
        """시간 범위 내의 큐 작업 찾기"""
        matching = []
        for task in tasks:
            completed_at = task.get('completed_at')
            if completed_at and start_time <= completed_at <= end_time:
                matching.append(task)
        return matching

    def _create_integrated_metric(
        self, session_id: str, logs: List[Dict], tasks: List[Dict]
    ) -> Dict:
        """통합 메트릭 생성"""
        # Hook 메트릭 집계 (event 필드 사용)
        hook_metrics = {
            'total_events': len(logs),
            'keyword_analysis_count': sum(
                1 for log in logs if log.get('event') == 'keyword_analysis'
            ),
            'cache_injection_count': sum(
                1 for log in logs if log.get('event') == 'cache_injection'
            ),
            'validation_count': sum(
                1 for log in logs if log.get('event') == 'validation_result'
            ),
            'total_rules_injected': sum(
                log.get('rules_injected', 0)
                for log in logs if log.get('event') == 'cache_injection'
            ),
            'detected_layers': list(set(
                layer
                for log in logs
                if 'detected_layers' in log
                for layer in log.get('detected_layers', [])
            ))
        }

        # 큐 메트릭 집계
        queue_metrics = {
            'completed_tasks': len(tasks),
            'total_estimated_time_minutes': 0,
            'total_actual_time_minutes': 0,
            'total_code_lines': sum(task.get('code_lines', 0) for task in tasks),
            'total_files_created': sum(task.get('files_created', 0) for task in tasks),
            'total_interruptions': sum(task.get('interruptions', 0) for task in tasks),
            'average_accuracy': 0.0,
            'tasks': []
        }

        # 각 작업별 메트릭
        for task in tasks:
            estimated_minutes = self._parse_time_to_minutes(task.get('estimated_time', ''))
            actual_minutes = self._parse_time_to_minutes(task.get('actual_time', ''))

            queue_metrics['total_estimated_time_minutes'] += estimated_minutes
            queue_metrics['total_actual_time_minutes'] += actual_minutes

            queue_metrics['tasks'].append({
                'feature': task.get('feature'),
                'estimated_time': task.get('estimated_time'),
                'actual_time': task.get('actual_time'),
                'accuracy': task.get('accuracy'),
                'code_lines': task.get('code_lines', 0),
                'files_created': task.get('files_created', 0),
                'interruptions': task.get('interruptions', 0)
            })

        # 평균 정확도 계산
        accuracies = [
            float(task.get('accuracy', '0').rstrip('%'))
            for task in tasks if task.get('accuracy') and task['accuracy'] != 'N/A'
        ]
        if accuracies:
            queue_metrics['average_accuracy'] = sum(accuracies) / len(accuracies)

        # 통합 메트릭
        return {
            'session_id': session_id,
            'timestamp': logs[0]['timestamp'],
            'session_duration': self._calculate_duration(
                logs[0]['timestamp'], logs[-1]['timestamp']
            ),
            'hook_metrics': hook_metrics,
            'queue_metrics': queue_metrics,
            'efficiency_score': self._calculate_efficiency_score(
                hook_metrics, queue_metrics
            )
        }

    def _parse_time_to_minutes(self, time_str: str) -> int:
        """시간 문자열을 분으로 변환"""
        if not time_str:
            return 0

        time_str = time_str.lower().replace(" ", "")
        total_minutes = 0

        if "시간" in time_str:
            parts = time_str.split("시간")
            try:
                hours = int(parts[0])
                total_minutes += hours * 60
                if len(parts) > 1 and "분" in parts[1]:
                    mins = int(parts[1].replace("분", ""))
                    total_minutes += mins
            except ValueError:
                return 0
        elif "분" in time_str:
            try:
                mins = int(time_str.replace("분", ""))
                total_minutes = mins
            except ValueError:
                return 0

        return total_minutes

    def _calculate_duration(self, start: str, end: str) -> str:
        """세션 소요 시간 계산"""
        try:
            start_dt = datetime.fromisoformat(start.replace('Z', '+00:00'))
            end_dt = datetime.fromisoformat(end.replace('Z', '+00:00'))
            delta = end_dt - start_dt
            minutes = int(delta.total_seconds() / 60)

            if minutes < 60:
                return f"{minutes}분"
            else:
                hours = minutes // 60
                mins = minutes % 60
                return f"{hours}시간 {mins}분"
        except:
            return "N/A"

    def _calculate_efficiency_score(
        self, hook_metrics: Dict, queue_metrics: Dict
    ) -> float:
        """
        효율성 점수 계산 (0-100)

        요소:
        - 규칙 자동 주입 횟수 (높을수록 좋음)
        - 검증 통과율
        - 예상 시간 정확도
        - 코드 생산성 (라인/시간)
        """
        score = 0.0

        # 1. 규칙 주입 효율 (0-25점)
        rules_per_event = (
            hook_metrics['total_rules_injected'] / hook_metrics['total_events']
            if hook_metrics['total_events'] > 0 else 0
        )
        score += min(25, rules_per_event * 2)

        # 2. 검증 통과율 (0-25점)
        validation_rate = (
            hook_metrics['validation_count'] / hook_metrics['total_events']
            if hook_metrics['total_events'] > 0 else 0
        )
        score += validation_rate * 25

        # 3. 예상 시간 정확도 (0-30점)
        score += (queue_metrics['average_accuracy'] / 100) * 30

        # 4. 코드 생산성 (0-20점)
        if queue_metrics['total_actual_time_minutes'] > 0:
            lines_per_hour = (
                queue_metrics['total_code_lines'] /
                (queue_metrics['total_actual_time_minutes'] / 60)
            )
            # 100 lines/hour = 20점
            score += min(20, (lines_per_hour / 100) * 20)

        return round(score, 1)

    def save_to_file(self):
        """집계 결과를 파일로 저장"""
        OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

        with open(OUTPUT_FILE, 'w') as f:
            json.dump({
                'generated_at': datetime.utcnow().isoformat() + 'Z',
                'total_sessions': len(self.aggregated_data),
                'sessions': self.aggregated_data
            }, f, indent=2, ensure_ascii=False)

        print(f"✅ 집계 결과 저장: {OUTPUT_FILE}")

    def upload_to_langfuse(self):
        """LangFuse로 업로드"""
        public_key = os.getenv("LANGFUSE_PUBLIC_KEY")
        secret_key = os.getenv("LANGFUSE_SECRET_KEY")
        host = os.getenv("LANGFUSE_HOST", "https://us.cloud.langfuse.com")

        if not public_key or not secret_key:
            print("❌ LANGFUSE_PUBLIC_KEY or LANGFUSE_SECRET_KEY not set")
            return False

        client = LangFuseUploader(public_key, secret_key, host)

        for session in self.aggregated_data:
            success = client.upload_session(session)
            if success:
                print(f"✅ 업로드 성공: {session['session_id'][:8]}...")
            else:
                print(f"❌ 업로드 실패: {session['session_id'][:8]}...")

        return True


class LangFuseUploader:
    """LangFuse Ingestion API 업로더"""

    def __init__(self, public_key: str, secret_key: str, host: str):
        self.host = host.rstrip('/')
        self.session = requests.Session()
        self.session.auth = HTTPBasicAuth(public_key, secret_key)
        self.session.headers.update({'Content-Type': 'application/json'})

    def upload_session(self, session_data: Dict) -> bool:
        """세션 데이터를 LangFuse Trace로 업로드"""
        try:
            # Trace 생성
            trace_id = f"trace-{session_data['session_id'][:8]}"

            trace_batch = {
                'batch': [{
                    'id': trace_id,
                    'type': 'trace-create',
                    'timestamp': session_data['timestamp'],
                    'body': {
                        'id': trace_id,
                        'name': f"Queue Session: {session_data['queue_metrics']['completed_tasks']} tasks",
                        'timestamp': session_data['timestamp'],
                        'metadata': {
                            'session_id': session_data['session_id'],
                            'session_duration': session_data['session_duration'],
                            'efficiency_score': session_data['efficiency_score'],
                            'hook_metrics': session_data['hook_metrics'],
                            'queue_metrics': session_data['queue_metrics']
                        },
                        'tags': ['claude-code', 'queue-metrics', 'efficiency']
                    }
                }]
            }

            url = f"{self.host}/api/public/ingestion"
            response = self.session.post(url, json=trace_batch, timeout=10)
            response.raise_for_status()

            # 각 작업별 Observation 생성
            for task in session_data['queue_metrics']['tasks']:
                self._create_task_observation(trace_id, task, session_data['timestamp'])

            return True

        except Exception as e:
            print(f"⚠️ LangFuse 업로드 실패: {e}", file=sys.stderr)
            return False

    def _create_task_observation(
        self, trace_id: str, task: Dict, base_timestamp: str
    ):
        """작업별 Observation 생성"""
        event_id = f"{trace_id}-{task['feature']}"

        obs_batch = {
            'batch': [{
                'id': event_id,
                'type': 'event-create',
                'timestamp': base_timestamp,
                'body': {
                    'id': event_id,
                    'traceId': trace_id,
                    'name': f"Task: {task['feature']}",
                    'startTime': base_timestamp,
                    'metadata': task,
                    'level': 'DEFAULT'
                }
            }]
        }

        try:
            url = f"{self.host}/api/public/ingestion"
            response = self.session.post(url, json=obs_batch, timeout=10)
            response.raise_for_status()
        except Exception as e:
            print(f"⚠️ Task observation 업로드 실패: {e}", file=sys.stderr)


def main():
    import argparse

    parser = argparse.ArgumentParser(
        description="Queue + Hook 통합 메트릭 집계 및 LangFuse 업로드"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="데이터 집계만 (LangFuse 업로드 X)"
    )
    parser.add_argument(
        "--session-id",
        help="특정 세션만 분석"
    )

    args = parser.parse_args()

    # 집계
    aggregator = QueueMetricsAggregator()
    aggregator.load_data()
    aggregator.aggregate()
    aggregator.save_to_file()

    # 통계 출력
    print("\n📊 집계 통계:")
    print(f"  총 세션 수: {len(aggregator.aggregated_data)}개")

    total_tasks = sum(s['queue_metrics']['completed_tasks'] for s in aggregator.aggregated_data)
    total_lines = sum(s['queue_metrics']['total_code_lines'] for s in aggregator.aggregated_data)
    avg_efficiency = (
        sum(s['efficiency_score'] for s in aggregator.aggregated_data) /
        len(aggregator.aggregated_data)
        if aggregator.aggregated_data else 0
    )

    print(f"  완료된 작업: {total_tasks}개")
    print(f"  생성 코드: {total_lines} 줄")
    print(f"  평균 효율성 점수: {avg_efficiency:.1f}/100")

    # LangFuse 업로드
    if not args.dry_run:
        print("\n🚀 LangFuse 업로드 중...")
        success = aggregator.upload_to_langfuse()
        if success:
            print("✅ 모든 세션 업로드 완료!")
        else:
            print("⚠️ 일부 세션 업로드 실패")
    else:
        print("\n⏭️  Dry-run 모드: LangFuse 업로드 생략")


if __name__ == "__main__":
    main()
