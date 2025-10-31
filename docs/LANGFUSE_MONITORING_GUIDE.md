# LangFuse 모니터링 통합 가이드

이 가이드는 Claude Code 및 Cascade 로그를 LangFuse로 전송하여 실시간 모니터링 및 분석하는 방법을 설명합니다.

---

## 📋 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [멀티 테넌트 전략](#멀티-테넌트-전략)
3. [LangFuse 프로젝트 설정](#langfuse-프로젝트-설정)
4. [로그 수집 및 전송](#로그-수집-및-전송)
5. [대시보드 구성](#대시보드-구성)
6. [비용 최적화](#비용-최적화)

---

## 아키텍처 개요

### 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────┐
│ 개발자 로컬 환경                                          │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Claude Code                    IntelliJ Cascade         │
│      ↓                                ↓                  │
│  .claude/hooks/            .pipeline-metrics/metrics.jsonl        │
│    logs/hook-execution.jsonl                             │
│                                                           │
└──────────────┬──────────────────────┬────────────────────┘
               ↓                      ↓
┌─────────────────────────────────────────────────────────┐
│ 로그 집계 레이어 (Local/CI)                               │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  scripts/langfuse/                                        │
│    ├── aggregate-logs.py    # JSONL → LangFuse 형식     │
│    ├── upload-to-langfuse.py # LangFuse API 전송        │
│    └── monitor.sh            # 실시간 모니터링 (선택)    │
│                                                           │
└──────────────┬────────────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────────┐
│ LangFuse Cloud/Self-Hosted                               │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Project: spring-standards-{team/user}                   │
│    ├── Traces (세션 단위)                                │
│    ├── Observations (Hook 실행, Cascade 작업)           │
│    ├── Metrics (토큰, 시간, 위반 건수)                   │
│    └── Tags (layer, keyword, user, environment)         │
│                                                           │
└──────────────┬────────────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────────┐
│ 분석 및 대시보드                                          │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  - 토큰 사용량 추이                                       │
│  - 컨벤션 위반 통계                                       │
│  - Layer별 성능 분석                                      │
│  - 팀/사용자별 비교                                       │
│  - A/B 테스트 결과                                        │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 로그 소스

#### 1. Claude Code 로그
**위치**: `.claude/hooks/logs/hook-execution.jsonl`

**형식**: JSONL (JSON Lines)

**이벤트 타입**:
```json
{"timestamp":"2025-10-29T10:30:15Z","event":"session_start","project":"claude-spring-standards","user":"sangwon-ryu"}
{"timestamp":"2025-10-29T10:30:15Z","event":"keyword_analysis","context_score":45,"detected_layers":["domain"]}
{"timestamp":"2025-10-29T10:30:15Z","event":"cache_injection","layer":"domain","rules_loaded":5,"estimated_tokens":2500}
{"timestamp":"2025-10-29T10:30:16Z","event":"validation_complete","file":"Order.java","status":"passed","validation_time_ms":148}
```

#### 2. Cascade 로그
**위치**: `.pipeline-metrics/metrics.jsonl`

**형식**: JSONL (JSON Lines)

**이벤트 타입**:
```json
{"timestamp":"2025-10-29T10:30:30Z","task":"validate_conventions","status":0,"duration":5,"exit_code":0}
{"timestamp":"2025-10-29T10:30:45Z","task":"test_unit","status":0,"duration":15,"tests_passed":23,"coverage":89}
{"timestamp":"2025-10-29T10:31:30Z","task":"pipeline_pr","status":0,"duration":45,"checks_passed":5}
```

---

## 멀티 테넌트 전략

### 시나리오 분석

이 프로젝트는 **템플릿**이므로 다음 3가지 사용 방식이 있습니다:

#### 시나리오 1: 컨벤션만 복사 (설정 이전)
```bash
# 다른 프로젝트에 Claude Hooks만 복사
bash scripts/install-claude-hooks.sh
```
→ **LangFuse 프로젝트**: 각 팀/사용자의 독립 프로젝트

#### 시나리오 2: 템플릿 전체 클론 (Fork)
```bash
# 프로젝트 전체를 클론하여 커스터마이징
git clone https://github.com/your-org/claude-spring-standards.git my-project
```
→ **LangFuse 프로젝트**: 각 팀/사용자의 독립 프로젝트

#### 시나리오 3: 템플릿 메인테이너 (개발)
```bash
# 템플릿 자체를 개선/유지보수
git clone https://github.com/your-org/claude-spring-standards.git
```
→ **LangFuse 프로젝트**: 중앙 집중식 "spring-standards-template" (모든 기여자 데이터 집계)

### LangFuse 프로젝트 구조

#### Option 1: 중앙 집중식 (템플릿 메인테이너용)

**LangFuse 프로젝트**: `spring-standards-template`

**장점**:
- ✅ 모든 사용자 데이터 통합 분석
- ✅ 템플릿 효과성 측정 가능
- ✅ A/B 테스트 통계적 유의성 확보

**단점**:
- ⚠️ 개인정보 보호 이슈
- ⚠️ 팀별 데이터 격리 필요

**권장 구조**:
```
LangFuse Project: spring-standards-template
├── Tags:
│   ├── user: sangwon-ryu, team-a, team-b
│   ├── organization: company-x, company-y
│   ├── environment: dev, prod
│   └── template_version: v1.0.0, v1.1.0
└── Traces:
    ├── session_id: unique per session
    └── project_name: actual project name
```

#### Option 2: 분산형 (각 팀/사용자용)

**LangFuse 프로젝트**: `{team-name}-spring-project` 또는 `{user-name}-project`

**장점**:
- ✅ 데이터 완전 격리
- ✅ 팀별 독립적 분석
- ✅ 개인정보 보호

**단점**:
- ⚠️ 템플릿 효과성 측정 어려움
- ⚠️ 교차 분석 불가

**권장 구조**:
```
LangFuse Project: {team-name}-spring-project
├── Tags:
│   ├── user: team member names
│   ├── feature: order, payment, shipment
│   └── environment: dev, staging, prod
└── Traces:
    ├── session_id: unique per session
    └── module: domain, application, rest
```

### 권장 전략: 하이브리드 (익명화 + 옵트인)

**구조**:
```
1. 로컬: 각 팀/사용자의 독립 LangFuse 프로젝트
2. 중앙: 익명화된 통계만 선택적으로 공유
```

**설정 파일**: `.langfuse.config.json`
```json
{
  "langfuse": {
    "primary": {
      "enabled": true,
      "project_id": "team-a-spring-project",
      "public_key": "pk-lf-...",
      "secret_key": "sk-lf-...",
      "host": "https://cloud.langfuse.com"
    },
    "telemetry": {
      "enabled": false,
      "project_id": "spring-standards-template",
      "public_key": "pk-lf-template-...",
      "secret_key": "sk-lf-template-...",
      "anonymize": true,
      "opt_in": false
    }
  }
}
```

**익명화 전략**:
- ✅ 사용자명 해시 (`sangwon-ryu` → `user-a1b2c3`)
- ✅ 파일명 제거 (`Order.java` → `*.java`)
- ✅ 프로젝트명 제거 (`my-ecommerce` → `project-001`)
- ✅ IP 주소 제거
- ✅ 통계 데이터만 전송 (토큰, 시간, 위반 건수)

---

## LangFuse 프로젝트 설정

### 1. LangFuse 계정 생성

**Option A: LangFuse Cloud** (권장, 무료 플랜 제공)
```bash
# 1. https://cloud.langfuse.com 회원가입
# 2. 새 프로젝트 생성: "{team-name}-spring-project"
# 3. API 키 발급
```

**Option B: Self-Hosted** (프라이버시 중요 시)
```bash
# Docker Compose로 로컬 설치
git clone https://github.com/langfuse/langfuse.git
cd langfuse
docker-compose up -d
```

### 2. 환경 변수 설정

**개발자별 설정** (`.env.local` 또는 `~/.bashrc`):
```bash
# Primary Project (팀/개인 프로젝트)
export LANGFUSE_PUBLIC_KEY="pk-lf-your-team-..."
export LANGFUSE_SECRET_KEY="sk-lf-your-team-..."
export LANGFUSE_HOST="https://cloud.langfuse.com"

# Telemetry (선택 사항, 템플릿 개선용)
export LANGFUSE_TELEMETRY_ENABLED=false  # 기본 비활성화
export LANGFUSE_TELEMETRY_PUBLIC_KEY="pk-lf-template-..."
export LANGFUSE_TELEMETRY_SECRET_KEY="sk-lf-template-..."
```

**CI/CD 설정** (GitHub Actions, GitLab CI):
```yaml
# .github/workflows/monitor.yml
env:
  LANGFUSE_PUBLIC_KEY: ${{ secrets.LANGFUSE_PUBLIC_KEY }}
  LANGFUSE_SECRET_KEY: ${{ secrets.LANGFUSE_SECRET_KEY }}
  LANGFUSE_HOST: "https://cloud.langfuse.com"
  LANGFUSE_ENVIRONMENT: "ci"
```

### 3. 프로젝트 구조 생성

**Tags (필터링 및 그룹화용)**:
- `user`: 개발자 이름 또는 익명 ID
- `layer`: domain, application, rest, persistence
- `environment`: dev, staging, prod, ci
- `feature`: 개발 중인 기능명 (order, payment 등)
- `template_version`: 템플릿 버전 (v1.0.0)

---

## 로그 수집 및 전송

### 스크립트 구조

```
scripts/langfuse/
├── aggregate-logs.py          # JSONL → LangFuse 형식 변환
├── upload-to-langfuse.py      # LangFuse API 전송
├── monitor.sh                 # 실시간 모니터링 (선택)
└── config/
    ├── event-mapping.json     # 이벤트 → Observation 매핑
    └── anonymization.json     # 익명화 규칙
```

### 1. aggregate-logs.py

**목적**: Claude Code 및 Cascade 로그를 LangFuse Trace/Observation 형식으로 변환

**입력**:
- `.claude/hooks/logs/hook-execution.jsonl`
- `.pipeline-metrics/metrics.jsonl`

**출력**:
- LangFuse API 호환 JSON

**주요 로직**:
```python
#!/usr/bin/env python3
"""
LangFuse Log Aggregator

Claude Code 및 Cascade 로그를 LangFuse Trace/Observation 형식으로 변환
"""

import json
import hashlib
from datetime import datetime
from typing import Dict, List, Optional
from pathlib import Path

class LangFuseAggregator:
    def __init__(self, anonymize: bool = False):
        self.anonymize = anonymize
        self.traces: Dict[str, Dict] = {}
        self.observations: List[Dict] = []

    def load_claude_logs(self, log_path: str) -> None:
        """Claude Code 로그 로드 및 변환"""
        with open(log_path, 'r') as f:
            for line in f:
                event = json.loads(line.strip())
                self._process_claude_event(event)

    def load_cascade_logs(self, log_path: str) -> None:
        """Cascade 로그 로드 및 변환"""
        with open(log_path, 'r') as f:
            for line in f:
                event = json.loads(line.strip())
                self._process_cascade_event(event)

    def _process_claude_event(self, event: Dict) -> None:
        """Claude Code 이벤트 → LangFuse Observation"""
        event_type = event.get('event')

        if event_type == 'session_start':
            # 새 Trace 생성
            trace_id = event.get('session_id', self._generate_trace_id(event))
            self.traces[trace_id] = {
                'id': trace_id,
                'name': f"Claude Session - {event.get('project', 'unknown')}",
                'timestamp': event.get('timestamp'),
                'tags': self._extract_tags(event),
                'metadata': {
                    'project': self._anonymize_if_needed(event.get('project')),
                    'user': self._anonymize_if_needed(event.get('user')),
                    'tool': 'claude-code'
                }
            }

        elif event_type == 'keyword_analysis':
            # Observation: 키워드 분석
            self.observations.append({
                'traceId': event.get('session_id'),
                'name': 'Keyword Analysis',
                'type': 'SPAN',
                'startTime': event.get('timestamp'),
                'endTime': event.get('timestamp'),
                'metadata': {
                    'context_score': event.get('context_score'),
                    'detected_layers': event.get('detected_layers', []),
                    'detected_keywords': event.get('detected_keywords', [])
                },
                'tags': ['analysis', 'keyword-detection']
            })

        elif event_type == 'cache_injection':
            # Observation: Cache 규칙 주입
            self.observations.append({
                'traceId': event.get('session_id'),
                'name': f"Cache Injection - {event.get('layer')}",
                'type': 'SPAN',
                'startTime': event.get('timestamp'),
                'endTime': event.get('timestamp'),
                'usage': {
                    'input': event.get('estimated_tokens', 0),
                    'output': 0,
                    'total': event.get('estimated_tokens', 0)
                },
                'metadata': {
                    'layer': event.get('layer'),
                    'rules_loaded': event.get('rules_loaded'),
                    'total_rules_available': event.get('total_rules_available')
                },
                'tags': [event.get('layer'), 'cache-injection']
            })

        elif event_type == 'validation_complete':
            # Observation: 검증 완료
            status = event.get('status')
            self.observations.append({
                'traceId': event.get('session_id'),
                'name': f"Validation - {event.get('file', 'unknown')}",
                'type': 'SPAN',
                'startTime': event.get('timestamp'),
                'endTime': event.get('timestamp'),
                'level': 'DEFAULT' if status == 'passed' else 'WARNING',
                'statusMessage': f"Validation {status}",
                'metadata': {
                    'file': self._anonymize_if_needed(event.get('file')),
                    'total_rules': event.get('total_rules'),
                    'validation_time_ms': event.get('validation_time_ms')
                },
                'tags': ['validation', status]
            })

    def _process_cascade_event(self, event: Dict) -> None:
        """Cascade 이벤트 → LangFuse Observation"""
        task_name = event.get('task', 'unknown')
        status_code = event.get('status', 1)

        # Trace ID 추정 (Cascade는 session_id 없으므로 타임스탬프 기반)
        trace_id = self._estimate_trace_id(event.get('timestamp'))

        self.observations.append({
            'traceId': trace_id,
            'name': f"Cascade - {task_name}",
            'type': 'SPAN',
            'startTime': self._calculate_start_time(event),
            'endTime': event.get('timestamp'),
            'level': 'DEFAULT' if status_code == 0 else 'ERROR',
            'statusMessage': f"Exit code: {status_code}",
            'metadata': {
                'task': task_name,
                'duration_seconds': event.get('duration'),
                'exit_code': status_code,
                'tests_passed': event.get('tests_passed'),
                'coverage': event.get('coverage')
            },
            'tags': ['cascade', task_name, 'passed' if status_code == 0 else 'failed']
        })

    def _anonymize_if_needed(self, value: Optional[str]) -> Optional[str]:
        """익명화 처리"""
        if not self.anonymize or not value:
            return value

        # 파일명 익명화
        if value.endswith('.java') or value.endswith('.kt'):
            return '*.java'

        # 사용자명 익명화
        if '@' not in value:  # 이메일이 아닌 경우
            hashed = hashlib.sha256(value.encode()).hexdigest()[:8]
            return f"user-{hashed}"

        return value

    def _extract_tags(self, event: Dict) -> List[str]:
        """이벤트에서 태그 추출"""
        tags = []

        if 'layer' in event:
            tags.append(event['layer'])
        if 'environment' in event:
            tags.append(event['environment'])
        if 'user' in event and not self.anonymize:
            tags.append(f"user:{event['user']}")

        return tags

    def _generate_trace_id(self, event: Dict) -> str:
        """Trace ID 생성"""
        timestamp = event.get('timestamp', datetime.utcnow().isoformat())
        project = event.get('project', 'unknown')
        return f"{project}-{timestamp}"

    def _estimate_trace_id(self, timestamp: str) -> str:
        """Cascade 이벤트의 Trace ID 추정 (Claude 세션과 매칭)"""
        # 타임스탬프 기반으로 가장 가까운 Claude 세션 찾기
        # 실제 구현 시 더 정교한 매칭 로직 필요
        return list(self.traces.keys())[0] if self.traces else 'cascade-session'

    def _calculate_start_time(self, event: Dict) -> str:
        """시작 시간 계산 (endTime - duration)"""
        from datetime import datetime, timedelta

        end_time = datetime.fromisoformat(event['timestamp'].replace('Z', '+00:00'))
        duration = event.get('duration', 0)
        start_time = end_time - timedelta(seconds=duration)

        return start_time.isoformat()

    def export_to_langfuse(self) -> Dict:
        """LangFuse API 형식으로 내보내기"""
        return {
            'traces': list(self.traces.values()),
            'observations': self.observations
        }

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Aggregate logs for LangFuse')
    parser.add_argument('--claude-logs', default='.claude/hooks/logs/hook-execution.jsonl')
    parser.add_argument('--cascade-logs', default='.pipeline-metrics/metrics.jsonl')
    parser.add_argument('--output', default='langfuse-data.json')
    parser.add_argument('--anonymize', action='store_true', help='Anonymize sensitive data')

    args = parser.parse_args()

    aggregator = LangFuseAggregator(anonymize=args.anonymize)

    # 로그 로드
    if Path(args.claude_logs).exists():
        aggregator.load_claude_logs(args.claude_logs)
        print(f"✅ Loaded Claude logs: {args.claude_logs}")

    if Path(args.pipeline-metrics_logs).exists():
        aggregator.load_cascade_logs(args.pipeline-metrics_logs)
        print(f"✅ Loaded Cascade logs: {args.pipeline-metrics_logs}")

    # LangFuse 형식으로 내보내기
    data = aggregator.export_to_langfuse()

    with open(args.output, 'w') as f:
        json.dump(data, f, indent=2)

    print(f"✅ Exported to {args.output}")
    print(f"   Traces: {len(data['traces'])}")
    print(f"   Observations: {len(data['observations'])}")

if __name__ == '__main__':
    main()
```

### 2. upload-to-langfuse.py

**목적**: LangFuse API로 데이터 전송

**주요 로직**:
```python
#!/usr/bin/env python3
"""
LangFuse Uploader

LangFuse API로 Trace/Observation 데이터 전송
"""

import json
import os
from typing import Dict, List
import requests
from requests.auth import HTTPBasicAuth

class LangFuseUploader:
    def __init__(self,
                 public_key: str,
                 secret_key: str,
                 host: str = "https://cloud.langfuse.com"):
        self.public_key = public_key
        self.secret_key = secret_key
        self.host = host.rstrip('/')
        self.session = requests.Session()
        self.session.auth = HTTPBasicAuth(public_key, secret_key)

    def upload_traces(self, traces: List[Dict]) -> None:
        """Trace 업로드"""
        url = f"{self.host}/api/public/traces"

        for trace in traces:
            response = self.session.post(url, json=trace)
            response.raise_for_status()
            print(f"✅ Uploaded trace: {trace['id']}")

    def upload_observations(self, observations: List[Dict]) -> None:
        """Observation 업로드"""
        url = f"{self.host}/api/public/observations"

        for observation in observations:
            response = self.session.post(url, json=observation)
            response.raise_for_status()
            print(f"✅ Uploaded observation: {observation['name']}")

    def upload_from_file(self, file_path: str) -> None:
        """파일에서 데이터 읽고 업로드"""
        with open(file_path, 'r') as f:
            data = json.load(f)

        self.upload_traces(data.get('traces', []))
        self.upload_observations(data.get('observations', []))

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Upload logs to LangFuse')
    parser.add_argument('--input', default='langfuse-data.json')
    parser.add_argument('--public-key', default=os.getenv('LANGFUSE_PUBLIC_KEY'))
    parser.add_argument('--secret-key', default=os.getenv('LANGFUSE_SECRET_KEY'))
    parser.add_argument('--host', default=os.getenv('LANGFUSE_HOST', 'https://cloud.langfuse.com'))

    args = parser.parse_args()

    if not args.public_key or not args.secret_key:
        print("❌ Error: LANGFUSE_PUBLIC_KEY and LANGFUSE_SECRET_KEY required")
        return 1

    uploader = LangFuseUploader(args.public_key, args.secret_key, args.host)
    uploader.upload_from_file(args.input)

    print("✅ Upload complete!")

if __name__ == '__main__':
    main()
```

### 3. monitor.sh (실시간 모니터링)

**목적**: 로그 파일 변경 감지 → 자동 집계 및 업로드

```bash
#!/bin/bash
# 실시간 LangFuse 모니터링

CLAUDE_LOGS=".claude/hooks/logs/hook-execution.jsonl"
CASCADE_LOGS=".pipeline-metrics/metrics.jsonl"
INTERVAL=300  # 5분마다

echo "🚀 LangFuse Monitor Started"
echo "   Watching: $CLAUDE_LOGS, $CASCADE_LOGS"
echo "   Interval: ${INTERVAL}s"

while true; do
    echo "[$(date)] Aggregating logs..."

    python3 scripts/langfuse/aggregate-logs.py \
        --claude-logs "$CLAUDE_LOGS" \
        --cascade-logs "$CASCADE_LOGS" \
        --output /tmp/langfuse-data.json

    if [ $? -eq 0 ]; then
        echo "[$(date)] Uploading to LangFuse..."
        python3 scripts/langfuse/upload-to-langfuse.py \
            --input /tmp/langfuse-data.json
    fi

    sleep $INTERVAL
done
```

---

## 대시보드 구성

### LangFuse 대시보드 예시

#### 1. 토큰 사용량 추이
```
Graph: Token Usage Over Time
X-axis: Date
Y-axis: Token Count
Filters: layer, user, environment
```

#### 2. 컨벤션 위반 통계
```
Table: Validation Failures
Columns:
  - Layer
  - Rule
  - Failure Count
  - Last Failure Date
Filters: date range, user
```

#### 3. Layer별 성능
```
Bar Chart: Average Validation Time by Layer
X-axis: Layer (domain, application, rest, persistence)
Y-axis: Time (ms)
```

#### 4. Cascade 작업 성공률
```
Pie Chart: Cascade Task Status
Segments:
  - Passed (green)
  - Failed (red)
Filters: task type, date range
```

### Grafana 연동 (선택 사항)

LangFuse는 Prometheus 메트릭을 제공하지 않지만, API를 통해 Grafana 연동 가능:

```bash
# LangFuse API → Grafana Data Source
# scripts/grafana/langfuse-datasource.py
```

---

## 비용 최적화

### LangFuse 요금제

| 플랜 | 무료 | Pro | Enterprise |
|------|------|-----|------------|
| Traces/월 | 50K | 1M | Unlimited |
| 보관 기간 | 30일 | 90일 | Custom |
| 가격 | $0 | $59/월 | Custom |

### 최적화 전략

#### 1. 샘플링
```python
# 전체 로그의 10%만 전송
import random

if random.random() < 0.1:  # 10% 샘플링
    upload_to_langfuse(event)
```

#### 2. 집계
```python
# 개별 이벤트 대신 시간대별 집계 전송
hourly_stats = aggregate_by_hour(events)
upload_to_langfuse(hourly_stats)
```

#### 3. 필터링
```python
# 중요한 이벤트만 전송
important_events = [
    'validation_complete',
    'cache_injection',
    'pipeline_pr'
]

if event['event'] in important_events:
    upload_to_langfuse(event)
```

---

## 실행 가이드

### 개발 환경 (로컬)

```bash
# 1. 환경 변수 설정
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."

# 2. 수동 업로드 (개발 중)
python3 scripts/langfuse/aggregate-logs.py --anonymize
python3 scripts/langfuse/upload-to-langfuse.py

# 3. 실시간 모니터링 (선택)
bash scripts/langfuse/monitor.sh
```

### CI/CD 환경

```yaml
# .github/workflows/langfuse-upload.yml
name: LangFuse Upload

on:
  push:
    branches: [main]
  schedule:
    - cron: '0 * * * *'  # 매 시간

jobs:
  upload:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'

      - name: Aggregate Logs
        run: |
          python3 scripts/langfuse/aggregate-logs.py \
            --anonymize \
            --output langfuse-data.json

      - name: Upload to LangFuse
        env:
          LANGFUSE_PUBLIC_KEY: ${{ secrets.LANGFUSE_PUBLIC_KEY }}
          LANGFUSE_SECRET_KEY: ${{ secrets.LANGFUSE_SECRET_KEY }}
        run: |
          python3 scripts/langfuse/upload-to-langfuse.py \
            --input langfuse-data.json
```

---

## FAQ

### Q1: 여러 프로젝트에서 같은 LangFuse 프로젝트를 사용해도 되나요?

**A**: 권장하지 않습니다.

**이유**:
- ❌ 데이터 격리 불가 (팀 A가 팀 B 데이터 볼 수 있음)
- ❌ 태그 충돌 가능
- ❌ 비용 관리 어려움

**권장 방식**:
- ✅ 팀별 독립 LangFuse 프로젝트 생성
- ✅ Tags로 사용자/환경 구분
- ✅ 익명화된 통계만 중앙 집계 (선택)

### Q2: 개인정보 보호는 어떻게 하나요?

**A**: 익명화 옵션 활성화

```bash
python3 scripts/langfuse/aggregate-logs.py --anonymize
```

**익명화 항목**:
- 사용자명 → `user-a1b2c3` (해시)
- 파일명 → `*.java`
- 프로젝트명 → `project-001`

### Q3: LangFuse 없이 로컬에서만 분석 가능한가요?

**A**: 가능합니다.

```bash
# JSONL 직접 분석
cat .claude/hooks/logs/hook-execution.jsonl | jq '.event' | sort | uniq -c

# 로그 뷰어 사용
.claude/hooks/scripts/view-logs.sh -s
```

---

**생성일**: 2025-10-29
**버전**: 1.0.0
**작성자**: Claude Code
