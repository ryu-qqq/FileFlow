# 자동 Queue 시스템 통합 가이드

**모든 Phase 구현 완료** ✅

---

## 🎯 시스템 개요

Claude Code 세션에서 **100% 자동으로 작업을 추적**하는 Queue 시스템입니다.

### 핵심 특징

1. **Hook 기반 자동 관리** - 사용자는 프롬프트만 입력
2. **TodoWrite 통합** - Claude의 TodoWrite와 양방향 동기화
3. **Statusline 통합** - 실시간 Queue 상태 표시
4. **LangFuse 연동** - 작업 효율 메트릭 자동 수집

---

## 📁 파일 구조

```
.claude/queue/
├── active.json              # 현재 진행 중인 작업
├── completed.jsonl          # 완료된 작업 (JSONL)
├── queue-manager.sh         # Queue 관리 스크립트 ⭐
├── sync-todo-to-queue.py    # TodoWrite 통합
├── log-queue-event.py       # LangFuse 로거
├── README.md                # 사용자 가이드
└── INTEGRATION_GUIDE.md     # 이 파일

.claude/hooks/
├── user-prompt-submit.sh    # ✅ Queue 자동 추가 (확장됨)
└── after-tool-use.sh        # ✅ Queue 자동 완료 (확장됨)

.claude/scripts/
└── context-monitor.py       # ✅ Statusline Queue 상태 표시 (신규)
```

---

## 🚀 설치 및 설정

### 1. 파일 권한 설정

```bash
chmod +x .claude/queue/queue-manager.sh
chmod +x .claude/queue/sync-todo-to-queue.py
chmod +x .claude/queue/log-queue-event.py
chmod +x .claude/scripts/context-monitor.py
```

### 2. Statusline 설정 (선택 사항)

```bash
# Claude Code Statusline 설정
claude code config statusline set .claude/scripts/context-monitor.py
```

### 3. 초기화 확인

```bash
# Queue 시스템 상태 확인
bash .claude/queue/queue-manager.sh status

# 출력 예시:
# 📋 Queue Status
#    Total: 0
#    In Progress: 0
#    Pending: 0
```

---

## 🔄 작동 원리

### Workflow 1: Hook 기반 자동 추가/완료

```
사용자: "Order Domain 비즈니스 로직 구현해줘"
    ↓
user-prompt-submit.sh (Hook 실행)
    ├─ 키워드 분석: "domain", "비즈니스" (CONTEXT_SCORE: 45)
    ├─ CONTEXT_SCORE >= 25 → Queue에 자동 추가
    ├─ queue-manager.sh add "Order Domain 비즈니스 로직 구현" 45 ["domain"]
    ├─ queue-manager.sh start (자동 시작)
    └─ LangFuse 로그: queue_add, queue_start
         ↓
Claude Code (작업 수행)
    └─ Order Domain 파일 생성/수정
         ↓
after-tool-use.sh (Hook 실행)
    ├─ Write/Edit 도구 감지
    ├─ queue-manager.sh complete (자동 완료)
    └─ LangFuse 로그: queue_complete
```

### Workflow 2: TodoWrite 통합

```
Claude Code (TodoWrite 사용)
    ├─ TodoWrite: "Order UseCase 구현" (status: in_progress)
    └─ sync-todo-to-queue.py 호출 (Hook 또는 명시적)
         ↓
Queue 시스템
    ├─ queue-manager.sh add "Order UseCase 구현"
    └─ queue-manager.sh start
         ↓
Claude Code (작업 완료)
    ├─ TodoWrite: "Order UseCase 구현" (status: completed)
    └─ sync-todo-to-queue.py 호출
         ↓
Queue 시스템
    └─ queue-manager.sh complete
```

### Workflow 3: Statusline 표시

```
Claude Code Statusline
    ↓
context-monitor.py (stdin JSON)
    ├─ get_queue_status() → active.json 읽기
    ├─ task_count 계산
    └─ 출력: [Claude Sonnet 4.5] 📋 3 | 🧠 🟢████████ 38% | ...
```

---

## 📊 데이터 구조

### active.json (현재 작업)

```json
{
  "tasks": [
    {
      "id": "task-8935b5ccfcb6",
      "description": "Order Domain 비즈니스 로직 구현",
      "status": "in_progress",
      "added_at": 1699999999.123,
      "started_at": 1700000000.456,
      "context_score": 45,
      "detected_layers": ["domain", "application"]
    }
  ]
}
```

### completed.jsonl (완료 이력)

```json
{"id":"task-8935b5ccfcb6","description":"Order Domain 비즈니스 로직 구현","status":"completed","added_at":1699999999.123,"started_at":1700000000.456,"completed_at":1700000100.789,"duration":100.33,"context_score":45,"detected_layers":["domain","application"]}
```

### LangFuse 로그 (hook-execution.jsonl)

```json
{"timestamp":1700000000.456,"event":"queue_add","data":{"task_id":"task-8935b5ccfcb6","description":"Order Domain 비즈니스 로직 구현","context_score":45,"layers":["domain","application"]}}
{"timestamp":1700000000.567,"event":"queue_start","data":{"task_id":"task-8935b5ccfcb6"}}
{"timestamp":1700000100.789,"event":"queue_complete","data":{"task_id":"task-8935b5ccfcb6","file":"domain/src/main/java/.../OrderDomain.java","layer":"domain"}}
```

---

## 🔧 커스터마이징

### 1. Context Score 임계값 변경

**user-prompt-submit.sh** (line 228):

```bash
# 기본값: 25
if [[ $CONTEXT_SCORE -ge 25 && -f "$QUEUE_MANAGER" ]]; then

# 변경 예시: 30으로 상향
if [[ $CONTEXT_SCORE -ge 30 && -f "$QUEUE_MANAGER" ]]; then
```

### 2. Queue 자동 완료 도구 확장

**after-tool-use.sh** (line 223):

```bash
# 기본값: Write, Edit, MultiEdit
if [[ "$TOOL_NAME" =~ ^(Write|Edit|MultiEdit)$ && -f "$QUEUE_MANAGER" ]]; then

# 변경 예시: NotebookEdit 추가
if [[ "$TOOL_NAME" =~ ^(Write|Edit|MultiEdit|NotebookEdit)$ && -f "$QUEUE_MANAGER" ]]; then
```

### 3. Statusline 포맷 변경

**context-monitor.py** (line 95):

```python
# 기본값
print(
    f"[{model_display}] {queue_status} | "
    f"🧠 {color}{bar} {usage_pct:.0f}% | "
    f"💰 {cost_cents}¢ ⏱ {duration_min}m 📝 +{lines_changed}"
)

# 변경 예시: Queue를 앞으로
print(
    f"{queue_status} | [{model_display}] "
    f"🧠 {color}{bar} {usage_pct:.0f}% | "
    f"💰 {cost_cents}¢ ⏱ {duration_min}m"
)
```

---

## 📈 메트릭 분석

### Queue 히스토리 확인

```bash
# 최근 10개 완료 작업
bash .claude/queue/queue-manager.sh history 10

# 출력 예시:
# 📜 Recent Completions (last 10):
# 2025-11-06 14:23:45 - Order Domain 비즈니스 로직 구현 (Duration: 100s)
# 2025-11-06 14:25:30 - UseCase Transaction 경계 관리 (Duration: 85s)
# ...
```

### LangFuse 로그 집계

```bash
# Hook 로그 요약
python3 .claude/hooks/scripts/summarize-hook-logs.py

# Queue 메트릭만 필터링
grep "queue_" langfuse/logs/hook-execution.jsonl
```

### 평균 작업 시간 계산

```bash
# completed.jsonl에서 평균 duration 계산
jq -s 'map(.duration) | add / length' .claude/queue/completed.jsonl

# 출력 예시: 92.5 (초)
```

---

## 🐛 문제 해결

### Q: Queue가 자동으로 추가되지 않아요
**A**: Context Score >= 25인지 확인

```bash
# Hook 로그 확인
tail -f langfuse/logs/hook-execution.jsonl | grep context_score

# 키워드 강화: "domain", "usecase" 등 명시적 사용
```

### Q: Queue가 자동으로 완료되지 않아요
**A**: Write/Edit 도구 사용 확인

```bash
# Hook 로그 확인
tail -f langfuse/logs/hook-execution.jsonl | grep queue_complete

# 수동 완료
bash .claude/queue/queue-manager.sh complete
```

### Q: Statusline에 Queue 상태가 안 보여요
**A**: context-monitor.py 설정 확인

```bash
# Statusline 스크립트 확인
claude code config statusline get

# 테스트 실행
echo '{"context_usage":{"used":50000,"limit":200000}}' | python3 .claude/scripts/context-monitor.py
```

### Q: active.json이 비어있어요
**A**: 초기화 또는 모든 작업 완료

```bash
# 상태 확인
bash .claude/queue/queue-manager.sh status

# 테스트 작업 추가
bash .claude/queue/queue-manager.sh add "Test Task"
```

---

## 🎓 Best Practices

### 1. 명확한 프롬프트 작성

```
✅ 좋은 예시:
"Order Domain Aggregate에 placeOrder 메서드 구현해줘"
→ CONTEXT_SCORE: 60 (domain, aggregate, method)
→ Queue 자동 추가

❌ 나쁜 예시:
"코드 좀 작성해줘"
→ CONTEXT_SCORE: 0
→ Queue 추가 안 됨
```

### 2. Layer 명시

```
✅ 좋은 예시:
"Application Layer UseCase에 Transaction 경계 관리 추가해줘"
→ CONTEXT_SCORE: 75 (application, usecase, transaction)
→ detected_layers: ["application"]

✅ 좋은 예시:
"Adapter-REST Controller에 validation 추가해줘"
→ CONTEXT_SCORE: 60 (controller, validation)
→ detected_layers: ["adapter-rest"]
```

### 3. TodoWrite 활용

```
Claude가 TodoWrite를 사용하면 자동으로 Queue와 동기화됩니다.
수동으로 sync-todo-to-queue.py를 호출할 필요 없음!
```

---

## 🔗 관련 문서

- [Queue README](./README.md) - 사용자 가이드
- [Dynamic Hooks Guide](../../docs/DYNAMIC_HOOKS_GUIDE.md) - Hook 시스템 전체
- [LangFuse Usage](../../docs/LANGFUSE_USAGE_GUIDE.md) - 메트릭 분석

---

## ✅ 체크리스트

### 시스템 확인

- [ ] `queue-manager.sh` 실행 권한 ✅
- [ ] `active.json`, `completed.jsonl` 생성 확인
- [ ] Hook 통합 (user-prompt-submit.sh, after-tool-use.sh)
- [ ] context-monitor.py Statusline 설정
- [ ] LangFuse 로그 경로 확인

### 기능 테스트

- [ ] Queue add/start/complete 수동 테스트
- [ ] Hook 자동 추가/완료 테스트
- [ ] Statusline Queue 상태 표시 확인
- [ ] LangFuse 로그 기록 확인

### 성능 확인

- [ ] Context Score >= 25 자동 추가 동작
- [ ] Write/Edit 도구 자동 완료 동작
- [ ] completed.jsonl 로그 누적 확인

---

**🎉 모든 Phase 구현 완료!**

**💡 핵심**: 사용자는 프롬프트만 입력하면 됩니다. 나머지는 자동입니다!
