# LangFuse Slash Command 로깅 가이드

## 🎯 목적

Slash Command 실행을 LangFuse에서 추적할 수 있도록 자동 로깅 시스템 구축

## 🔍 문제 배경

### 기존 시스템의 한계

```
✅ Hook Logs (자동 기록)
├─ UserPromptSubmit → session_start, keyword_analysis, serena_memory_load, cache_injection
└─ PostToolUse → validation_complete

❌ Slash Commands (기록 안 됨)
├─ /cc:load
├─ /code-gen-domain
├─ /validate-architecture
└─ 기타 모든 / 명령어
```

**원인**: Slash Command는 Hook이 아니므로 `user-prompt-submit.sh`가 트리거되지 않음

**영향**: LangFuse에 Slash Command 사용 통계가 누락됨

## ✅ 해결 방법

### 1. 자동 로깅 스크립트 (NEW)

**위치**: `.claude/hooks/scripts/log-slash-command.sh`

**기능**:
- Slash Command 실행 시 Hook logs에 자동 기록
- LangFuse 업로드 시 자동 포함
- 이벤트 타입: `slash_command_start`, `slash_command_complete`

**사용법**:
```bash
# 시작 로그
bash .claude/hooks/scripts/log-slash-command.sh "cc:load" "start"

# 완료 로그 (메타데이터 포함)
bash .claude/hooks/scripts/log-slash-command.sh "cc:load" "complete" '{"memories_loaded": 5}'
```

**생성되는 로그 포맷**:
```json
{
  "timestamp": "2025-10-31T11:30:00.123456",
  "event": "slash_command_start",
  "command": "cc:load",
  "session_id": "slash-20251031T113000123456",
  "metadata": {}
}
```

### 2. Slash Command 통합 (Updated)

**위치**: `.claude/commands/cc/load.md`

**업데이트 내용**:
```markdown
## 실행 내용

아래 작업들이 자동으로 수행됩니다:

\```python
# 0. Slash Command 로깅 (LangFuse 추적용) ⭐ NEW
# 실행: bash .claude/hooks/scripts/log-slash-command.sh "cc:load" "start"

# 1. Serena 프로젝트 활성화
mcp__serena__activate_project(...)

# ... (기존 로직)

# 5. Slash Command 완료 로깅 ⭐ NEW
# 실행: bash .claude/hooks/scripts/log-slash-command.sh "cc:load" "complete" '{"memories_loaded": 5}'
\```
```

### 3. LangFuse Aggregator 업데이트

**위치**: `scripts/langfuse/aggregate-logs.py`

**업데이트 내용**:
1. `slash_command_start`, `slash_command_complete` 이벤트 처리 추가
2. Slash Command 메타데이터 추출 (`command`, `metadata`)
3. 태그 자동 생성 (`slash-command:cc:load`)

**처리 로직**:
```python
# 이벤트 타입 확장
elif event_type in ['keyword_analysis', 'cache_injection', 'validation_complete',
                    'slash_command_start', 'slash_command_complete']:  # ⭐ NEW
    # LangFuse Observation 생성
    self.observations.append({
        'traceId': trace_id,
        'name': self._format_event_name(event_type, event),  # "/cc:load"
        'type': 'SPAN',
        'metadata': {'slash_command': 'cc:load', ...},
        'tags': ['slash-command:cc:load']
    })
```

## 📊 LangFuse에서 확인 가능한 데이터

### Trace (세션별)
```json
{
  "id": "session-2025-10-31T11:30:00",
  "name": "Claude Session",
  "metadata": {
    "project": "claude-spring-standards",
    "total_observations": 15,
    "slash_commands_used": ["/cc:load", "/code-gen-domain"]
  }
}
```

### Observations (이벤트별)

**Slash Command Start**:
```json
{
  "traceId": "session-2025-10-31T11:30:00",
  "name": "/cc:load",
  "type": "SPAN",
  "metadata": {
    "slash_command": "cc:load"
  },
  "tags": ["slash-command:cc:load"]
}
```

**Slash Command Complete**:
```json
{
  "traceId": "session-2025-10-31T11:30:00",
  "name": "/cc:load (completed)",
  "type": "SPAN",
  "metadata": {
    "slash_command": "cc:load",
    "memories_loaded": 5
  },
  "tags": ["slash-command:cc:load"]
}
```

## 🚀 실제 사용 워크플로우

### 방법 1: 자동 로깅 (권장)

**Slash Command 파일 업데이트**:
```markdown
# .claude/commands/my-command.md

## 실행 내용

\```python
# 시작 로그
bash .claude/hooks/scripts/log-slash-command.sh "my-command" "start"

# 비즈니스 로직
# ...

# 완료 로그
bash .claude/hooks/scripts/log-slash-command.sh "my-command" "complete" '{"result": "success"}'
\```
```

**Claude가 자동으로**:
1. 로깅 스크립트 실행
2. Hook logs에 이벤트 기록
3. Serena MCP 또는 다른 작업 수행

### 방법 2: 수동 로깅

```bash
# 1. Slash Command 실행
/cc:load

# 2. 수동으로 로그 추가 (필요 시)
bash .claude/hooks/scripts/log-slash-command.sh "cc:load" "manual" '{"note": "추가 메타데이터"}'
```

### 방법 3: LangFuse 업로드

```bash
# Hook logs → LangFuse 업로드
bash tools/pipeline/upload_langfuse.sh

# 또는 Windsurf Workflow
/upload-langfuse
```

## 📈 분석 예시

LangFuse Dashboard에서 다음을 확인할 수 있습니다:

### 1. Slash Command 사용 빈도
```sql
SELECT
  metadata.slash_command,
  COUNT(*) as usage_count
FROM observations
WHERE tags LIKE '%slash-command%'
GROUP BY metadata.slash_command
ORDER BY usage_count DESC;
```

**결과 예시**:
```
slash_command     | usage_count
------------------|------------
cc:load           | 87
code-gen-domain   | 45
validate-arch     | 32
```

### 2. 평균 실행 시간
```sql
SELECT
  metadata.slash_command,
  AVG(TIMESTAMPDIFF(end_time, start_time)) as avg_duration_seconds
FROM observations
WHERE name LIKE '% (completed)'
GROUP BY metadata.slash_command;
```

### 3. 메모리 로드 통계 (cc:load 전용)
```sql
SELECT
  metadata.memories_loaded,
  COUNT(*) as frequency
FROM observations
WHERE metadata.slash_command = 'cc:load'
  AND metadata.memories_loaded IS NOT NULL
GROUP BY metadata.memories_loaded;
```

## 🔧 다른 Slash Command에 적용하기

### 1. 명령어 파일 수정

**예시**: `/code-gen-domain`을 LangFuse 추적 가능하게 만들기

```markdown
# .claude/commands/code-gen-domain.md

Domain Aggregate를 생성합니다.

## 실행 내용

\```python
# ⭐ 시작 로깅
bash .claude/hooks/scripts/log-slash-command.sh "code-gen-domain" "start" '{"aggregate_name": "$1"}'

# Aggregate 생성 로직
# (기존 코드)

# ⭐ 완료 로깅
bash .claude/hooks/scripts/log-slash-command.sh "code-gen-domain" "complete" '{"files_created": 5, "lines_of_code": 250}'
\```
```

### 2. 업로드 및 확인

```bash
# LangFuse 업로드
bash tools/pipeline/upload_langfuse.sh

# Dashboard에서 확인
# - Traces > claude-spring-standards
# - Observations > Filter: tags contains "slash-command:code-gen-domain"
```

## 📚 참고 자료

- [LangFuse 통합 가이드](./LANGFUSE_INTEGRATION_GUIDE.md)
- [Hook 시스템 가이드](./DYNAMIC_HOOKS_GUIDE.md)
- [Slash Command 가이드](../.claude/commands/README.md)

## ⚠️ 주의사항

### 1. 민감한 정보 익명화

로그에 사용자 이름, 파일 경로 등이 포함될 수 있습니다.

**해결책**: `--anonymize` 플래그 사용 (기본 활성화)

```bash
python3 scripts/langfuse/aggregate-logs.py --anonymize
```

### 2. 로그 파일 크기 관리

Hook logs는 계속 누적됩니다.

**해결책**: 주기적으로 로그 정리
```bash
# 30일 이상 된 로그 백업 후 삭제
find .claude/hooks/logs -name "*.jsonl" -mtime +30 -exec gzip {} \;
mv .claude/hooks/logs/*.gz .claude/hooks/logs/archive/
```

### 3. LangFuse Rate Limiting

무료 플랜은 API 호출 제한이 있습니다.

**해결책**: 배치 업로드 (하루 1회 권장)
```bash
# Cron job 설정 (매일 자정)
0 0 * * * cd /Users/sangwon-ryu/claude-spring-standards && bash tools/pipeline/upload_langfuse.sh
```

## ✅ 체크리스트

### 초기 설정
- [ ] `.claude/hooks/scripts/log-slash-command.sh` 실행 권한 확인
- [ ] LangFuse 환경 변수 설정 (`LANGFUSE_PUBLIC_KEY`, `LANGFUSE_SECRET_KEY`)
- [ ] `aggregate-logs.py` 업데이트 확인

### Slash Command 통합
- [ ] 각 Slash Command 파일에 로깅 스크립트 추가
- [ ] 메타데이터 정의 (start, complete 각각)
- [ ] 테스트 실행 및 로그 확인

### LangFuse 업로드
- [ ] 수동 업로드 테스트 (`bash tools/pipeline/upload_langfuse.sh`)
- [ ] Dashboard에서 데이터 확인
- [ ] 자동 업로드 Cron job 설정 (선택 사항)

## 🎉 완료!

이제 모든 Slash Command 실행이 LangFuse에서 추적 가능합니다!

**다음 단계**:
1. Dashboard에서 메트릭 확인
2. 자주 사용하는 명령어 파악
3. 워크플로우 최적화
