# Upload Queue Metrics Command

**큐 메트릭 + Hook 로그 통합 LangFuse 업로드**

---

## 🎯 목적

Hook 실행 로그와 큐 작업 데이터를 병합하여 완전한 개발 효율 분석:
1. Hook 로그 (`hook-execution.jsonl`) 파싱
2. 큐 데이터 (`work-queue.json`) 파싱
3. 세션별로 데이터 병합
4. LangFuse로 업로드하여 시각화

---

## 📝 사용법

```bash
# 기본 사용 (집계 + LangFuse 업로드)
/upload-queue-metrics

# Dry-run (집계만, 업로드 X)
/upload-queue-metrics --dry-run

# 특정 세션만 분석
/upload-queue-metrics --session-id abc123
```

---

## 🔄 실행 프로세스

### Step 1: 데이터 로드

```bash
python3 langfuse/scripts/aggregate-queue-metrics.py
```

**로드 대상**:
- Hook 로그: `.claude/hooks/logs/hook-execution.jsonl`
- 큐 데이터: `.claude/work-queue.json`

### Step 2: 데이터 병합

**병합 로직**:
1. 세션별로 Hook 로그 그룹화
2. 시간 범위 기반으로 큐 작업 매칭
3. 통합 메트릭 생성

**통합 메트릭**:
```json
{
  "session_id": "abc123...",
  "timestamp": "2024-11-05T10:00:00Z",
  "session_duration": "1시간 30분",
  "hook_metrics": {
    "total_events": 45,
    "keyword_analysis_count": 5,
    "cache_injection_count": 8,
    "validation_count": 12,
    "total_rules_injected": 126,
    "detected_layers": ["domain", "application"]
  },
  "queue_metrics": {
    "completed_tasks": 3,
    "total_estimated_time_minutes": 90,
    "total_actual_time_minutes": 85,
    "total_code_lines": 1250,
    "total_files_created": 18,
    "total_interruptions": 2,
    "average_accuracy": 94.4,
    "tasks": [...]
  },
  "efficiency_score": 87.5
}
```

### Step 3: LangFuse 업로드

**Trace 생성**:
- 각 세션 → 하나의 Trace
- 메타데이터에 효율성 점수, Hook 메트릭, 큐 메트릭 포함

**Observation 생성**:
- 각 작업 → 하나의 Observation
- 작업별 예상 시간, 실제 시간, 정확도 포함

---

## 📦 출력

**성공:**
```
✅ Hook 로그 로드: 236개
✅ 큐 데이터 로드: 3개 완료 작업
✅ 집계 완료: 5개 세션
✅ 집계 결과 저장: langfuse/data/queue-metrics.json

📊 집계 통계:
  총 세션 수: 5개
  완료된 작업: 12개
  생성 코드: 3450 줄
  평균 효율성 점수: 85.2/100

🚀 LangFuse 업로드 중...
✅ 업로드 성공: abc123...
✅ 업로드 성공: def456...
✅ 모든 세션 업로드 완료!
```

**Dry-run:**
```
✅ Hook 로그 로드: 236개
✅ 큐 데이터 로드: 3개 완료 작업
✅ 집계 완료: 5개 세션
✅ 집계 결과 저장: langfuse/data/queue-metrics.json

📊 집계 통계:
  총 세션 수: 5개
  완료된 작업: 12개
  생성 코드: 3450 줄
  평균 효율성 점수: 85.2/100

⏭️  Dry-run 모드: LangFuse 업로드 생략
```

---

## 🎯 효율성 점수 계산 (0-100)

### 요소별 배점

1. **규칙 주입 효율** (25점)
   - 이벤트당 주입된 규칙 수
   - 높을수록 자동화가 잘 작동

2. **검증 통과율** (25점)
   - 전체 이벤트 대비 검증 통과 비율
   - 높을수록 코드 품질 우수

3. **예상 시간 정확도** (30점)
   - Claude 예측 vs 실제 소요 시간
   - 높을수록 예측 능력 우수

4. **코드 생산성** (20점)
   - 시간당 생성 코드 라인 수
   - 100 lines/hour = 20점

### 점수 해석

- **90-100점**: 🌟 탁월 (자동화 완벽, 예측 정확, 생산성 우수)
- **80-89점**: ✅ 우수 (대부분 자동화, 예측 양호, 생산성 좋음)
- **70-79점**: 👍 양호 (자동화 작동, 예측 개선 필요)
- **60-69점**: ⚠️ 보통 (개선 필요)
- **0-59점**: ❌ 미흡 (시스템 점검 필요)

---

## ⚙️ 환경 변수

LangFuse 업로드를 위해 다음 환경 변수 필수:

```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"
```

---

## 🔗 관련 커맨드

- `/queue-add {feature}` - 작업 추가 (예상 시간 포함)
- `/queue-complete {feature}` - 작업 완료 (메트릭 포함)
- `/queue-list` - 큐 목록 확인
- `/upload-langfuse` - Hook 로그만 업로드 (레거시)

---

## 📊 LangFuse에서 확인 가능한 메트릭

### Trace Level
- 세션 소요 시간
- 효율성 점수
- Hook 메트릭 (규칙 주입, 검증 통과율)
- 큐 메트릭 (작업 수, 코드 라인, 정확도)

### Observation Level
- 작업별 예상 시간 vs 실제 시간
- 작업별 정확도
- 작업별 생성 코드 라인 수
- 작업별 파일 수, 중단 횟수

### 활용 방안
- 📈 **트렌드 분석**: 시간에 따른 효율성 변화
- 🎯 **예측 개선**: 정확도 낮은 작업 타입 식별
- 🚀 **생산성 향상**: 병목 지점 발견 및 개선
- 📊 **자동화 효과**: Hook 시스템의 실제 효과 측정

---

**✅ 이 커맨드는 큐 메트릭 + Hook 로그 통합 업로드를 담당합니다!**
