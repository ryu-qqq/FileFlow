# LangFuse 사용 가이드

## 📋 목차

1. [빠른 시작](#1-빠른-시작)
2. [로그 시스템 개요](#2-로그-시스템-개요)
3. [수동 업로드 워크플로우](#3-수동-업로드-워크플로우)
4. [LangFuse 분석](#4-langfuse-분석)
5. [문제 해결](#5-문제-해결)

---

## 1. 빠른 시작

### 1.1 기본 사용 (LangFuse 없이)

**환경 변수 설정 불필요**. Hook이 자동으로 JSONL 로그를 기록합니다.

```bash
# 1. Claude Code 실행
claude code

# 2. 작업 수행
> domain aggregate Order 생성

# 3. 로그 확인
cat .claude/hooks/logs/hook-execution.jsonl
```

**결과**:
- `.claude/hooks/logs/hook-execution.jsonl` - 구조화된 JSONL 로그
- `.claude/hooks/logs/current-session.json` - 현재 세션 정보

### 1.2 LangFuse 사용 (선택적)

**Step 1: LangFuse 계정 및 API Key 발급**

1. https://cloud.langfuse.com 접속
2. 계정 생성 및 로그인
3. Project 생성 (예: "Claude Code - Spring Standards")
4. Settings → API Keys → Create new key
   - Public Key: `pk-lf-...`
   - Secret Key: `sk-lf-...`

**Step 2: 환경 변수 설정**

```bash
# 1. .env 파일 생성
cp .env.example .env

# 2. API Key 입력 (vim 또는 에디터 사용)
vim .env

# 3. 환경 변수 로드
source .env
```

**.env 내용 예시**:
```bash
LANGFUSE_PUBLIC_KEY=pk-lf-abc123...
LANGFUSE_SECRET_KEY=sk-lf-xyz789...
LANGFUSE_HOST=https://us.cloud.langfuse.com
```

**Step 3: 작업 및 업로드**

```bash
# 1. 작업 수행 (로그만 기록)
claude code
> domain aggregate Order 생성
> usecase CreateOrder 생성

# 2. 작업 완료 후 LangFuse 업로드
python3 scripts/langfuse/upload-to-langfuse.py

# 3. LangFuse UI에서 확인
# https://cloud.langfuse.com → Project → Traces
```

---

## 2. 로그 시스템 개요

### 2.1 자동 생성되는 파일

| 파일 | 설명 | 형식 |
|------|------|------|
| `hook-execution.jsonl` | 모든 Hook 이벤트 로그 | JSONL |
| `current-session.json` | 현재 세션 ID 및 Trace ID | JSON |

### 2.2 로그 이벤트 종류

**user-prompt-submit.sh**:
- `session_start`: 세션 시작
- `keyword_analysis`: 키워드 분석 결과
- `decision`: 규칙 주입 여부 결정
- `serena_memory_load`: Serena 메모리 로드
- `cache_injection_complete`: Cache 규칙 주입 완료

**after-tool-use.sh**:
- `code_generation_detected`: 코드 생성 감지
- `layer_detection`: Layer 감지 결과
- `validation_result`: 검증 결과

### 2.3 JSONL 로그 예시

```jsonl
{"timestamp":"2025-10-17T18:30:15Z","session_id":"uuid-1234","trace_id":"trace-abc","event_type":"session_start","data":{"project":"claude-spring-standards","user_command":"domain aggregate Order"}}
{"timestamp":"2025-10-17T18:30:15Z","session_id":"uuid-1234","trace_id":"trace-abc","event_type":"keyword_analysis","data":{"context_score":45,"detected_layers":["domain"]}}
{"timestamp":"2025-10-17T18:30:22Z","session_id":"uuid-1234","trace_id":"trace-abc","event_type":"validation_result","data":{"file":"Order.java","layer":"domain","result":"passed","violations":0}}
```

---

## 3. 수동 업로드 워크플로우

### 3.1 기본 워크플로우 (권장)

```bash
# ========================================
# Phase 1: 작업 수행 (로그만 기록)
# ========================================

claude code
> domain aggregate Order 생성
> usecase CreateOrder 생성
> controller OrderController 생성

# 로그 파일 확인
ls -lh .claude/hooks/logs/hook-execution.jsonl

# ========================================
# Phase 2: 작업 완료 후 LangFuse 업로드
# ========================================

# 환경 변수 로드 (최초 1회)
source .env

# LangFuse 업로드
python3 scripts/langfuse/upload-to-langfuse.py

# 출력:
# 🚀 LangFuse 업로드 시작...
# 📊 총 25개 이벤트 업로드
# ✅ 업로드 완료!

# ========================================
# Phase 3: LangFuse UI에서 분석
# ========================================

# 브라우저에서 확인
open https://cloud.langfuse.com
```

### 3.2 업로드 스크립트 옵션

**기본 업로드**:
```bash
python3 scripts/langfuse/upload-to-langfuse.py
```

**특정 세션만 업로드**:
```bash
python3 scripts/langfuse/upload-to-langfuse.py --session-id uuid-1234
```

**날짜 범위 지정**:
```bash
python3 scripts/langfuse/upload-to-langfuse.py --start-date 2025-10-17 --end-date 2025-10-18
```

**Dry-run (실제 업로드 없이 확인)**:
```bash
python3 scripts/langfuse/upload-to-langfuse.py --dry-run
```

---

## 4. LangFuse 분석

### 4.1 Trace 구조

**LangFuse UI에서 확인 가능한 구조**:

```
Trace: Claude Session (domain aggregate Order)
├─ Observation: session_start
├─ Observation: keyword_analysis (context_score: 45)
├─ Observation: cache_injection_complete (layers: 1)
├─ Observation: code_generation_detected (file: Order.java)
└─ Observation: validation_result (result: passed)
```

### 4.2 메트릭 분석

**LangFuse Dashboard에서 제공하는 메트릭**:

1. **세션별 통계**:
   - 총 세션 수
   - 평균 세션 시간
   - 이벤트 수 분포

2. **컨벤션 위반 분석**:
   - Layer별 위반 건수
   - 위반 유형 (Lombok, Javadoc, Law of Demeter 등)
   - 위반률 추세

3. **Context Score 분포**:
   - 평균 Context Score
   - Score별 규칙 주입 여부
   - 키워드 감지 효율성

4. **검증 성공률**:
   - 전체 검증 중 성공/실패 비율
   - Layer별 성공률
   - Validator 유형별 성공률 (cache_based vs fallback)

### 4.3 분석 쿼리 예시

**컨벤션 위반이 많은 Layer 찾기**:
```sql
SELECT
  metadata->>'layer' AS layer,
  COUNT(*) AS total_validations,
  SUM(CASE WHEN metadata->>'result' = 'failed' THEN 1 ELSE 0 END) AS failures,
  ROUND(SUM(CASE WHEN metadata->>'result' = 'failed' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS failure_rate
FROM observations
WHERE event_type = 'validation_result'
GROUP BY layer
ORDER BY failure_rate DESC
```

**시간대별 세션 활동**:
```sql
SELECT
  DATE_TRUNC('hour', timestamp) AS hour,
  COUNT(DISTINCT trace_id) AS sessions
FROM observations
WHERE event_type = 'session_start'
GROUP BY hour
ORDER BY hour DESC
```

---

## 5. 문제 해결

### 5.1 로그가 생성되지 않음

**증상**: `hook-execution.jsonl` 파일이 없음

**해결**:
```bash
# 1. Hook 권한 확인
ls -la .claude/hooks/*.sh

# 2. 권한 부여
chmod +x .claude/hooks/*.sh

# 3. 로그 디렉토리 생성
mkdir -p .claude/hooks/logs

# 4. Python 스크립트 확인
ls -la .claude/hooks/scripts/log-to-langfuse.py

# 5. 테스트
claude code
> test
```

### 5.2 LangFuse 업로드 실패

**증상**: `upload-to-langfuse.py` 실행 시 에러

**원인 1: 환경 변수 미설정**
```bash
# 확인
echo $LANGFUSE_PUBLIC_KEY
echo $LANGFUSE_SECRET_KEY

# 해결
source .env
```

**원인 2: API Key 오류**
```bash
# LangFuse UI에서 API Key 재확인
# Settings → API Keys → Copy

# .env 파일 수정
vim .env
```

**원인 3: 네트워크 오류**
```bash
# LangFuse 연결 테스트
curl -v https://us.cloud.langfuse.com

# Proxy 설정 (필요 시)
export HTTPS_PROXY=http://proxy.example.com:8080
```

### 5.3 JSONL 파싱 에러

**증상**: `jq` 명령어 실행 시 에러

**해결**:
```bash
# jq 설치 (macOS)
brew install jq

# jq 설치 (Linux)
sudo apt-get install jq

# 로그 파일 확인
cat .claude/hooks/logs/hook-execution.jsonl | jq '.'
```

### 5.4 세션 파일 충돌

**증상**: 여러 세션이 동일한 session_id 사용

**해결**:
```bash
# 세션 파일 삭제
rm .claude/hooks/logs/current-session.json

# 다음 실행 시 새로운 세션 생성됨
claude code
```

---

## 6. Best Practices

### 6.1 로그 관리

**로그 로테이션** (30일 이상 오래된 로그 삭제):
```bash
# Cron Job 설정
crontab -e

# 매주 일요일 자정에 실행
0 0 * * 0 find /path/to/claude-spring-standards/.claude/hooks/logs -name "*.jsonl" -mtime +30 -delete
```

**로그 백업**:
```bash
# 주기적으로 백업
tar -czf hook-logs-$(date +%Y%m%d).tar.gz .claude/hooks/logs/*.jsonl
mv hook-logs-*.tar.gz ~/backups/
```

### 6.2 LangFuse 활용

**주간 리포트 생성**:
```bash
# 매주 LangFuse 대시보드 확인
# 1. 컨벤션 위반 추세
# 2. Context Score 분포
# 3. 검증 성공률
```

**A/B 테스트**:
```bash
# Metadata에 실험 그룹 추가 (user-prompt-submit.sh 수정)
log_event "session_start" "{\"project\":\"$PROJECT_NAME\",\"experiment_group\":\"serena_memory\"}"

# LangFuse에서 그룹별 비교
```

### 6.3 성능 최적화

**로그 파일 크기 모니터링**:
```bash
# 로그 파일 크기 확인
du -h .claude/hooks/logs/hook-execution.jsonl

# 1MB 이상이면 업로드 후 삭제
if [ $(du -k .claude/hooks/logs/hook-execution.jsonl | cut -f1) -gt 1024 ]; then
    python3 scripts/langfuse/upload-to-langfuse.py
    rm .claude/hooks/logs/hook-execution.jsonl
fi
```

---

## 7. 참고 자료

- [LangFuse 자동 업로드 설계](./LANGFUSE_AUTO_UPLOAD_DESIGN.md)
- [LangFuse 공식 문서](https://langfuse.com/docs)
- [Hook 로깅 가이드](./.claude/hooks/HOOK_LOGGING_GUIDE.md)

---

**작성일**: 2025-10-17
**작성자**: Claude
**버전**: 1.0
