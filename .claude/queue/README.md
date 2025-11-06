# 자동 Queue 관리 시스템

Claude Code 세션에서 **자동으로 작업을 추적**하는 Queue 시스템입니다.

---

## 🎯 핵심 기능

### 1. **Hook 기반 자동 Queue 관리**
- ✅ 사용자 프롬프트 → 자동 `/queue-add`
- ✅ Write/Edit 완료 → 자동 `/queue-complete`
- ✅ 수동 명령어 불필요

### 2. **TodoWrite 통합**
- ✅ TodoWrite `in_progress` → Queue 자동 추가
- ✅ TodoWrite `completed` → Queue 자동 완료
- ✅ 양방향 동기화

### 3. **Statusline 통합**
- ✅ 현재 Queue 상태 실시간 표시
- ✅ `📋 3 tasks` 형식

### 4. **LangFuse 메트릭 연동**
- ✅ Queue 추가/완료 이벤트 자동 로깅
- ✅ 작업 효율 분석

---

## 📁 시스템 구조

```
.claude/queue/
├── active.json          # 현재 진행 중인 작업
├── completed.jsonl      # 완료된 작업 (JSONL)
├── queue-manager.sh     # Queue 관리 스크립트
└── README.md            # 이 파일
```

---

## 🚀 사용 방법

### 자동 모드 (권장)

```bash
# Hook이 자동으로 관리
사용자: "Order Domain 비즈니스 로직 구현해줘"

# 자동 실행:
# 1. user-prompt-submit.sh → /queue-add "Order Domain 비즈니스 로직 구현"
# 2. Claude Code → 작업 수행
# 3. after-tool-use.sh → /queue-complete
# 4. LangFuse 로깅
```

### 수동 모드 (선택 사항)

```bash
# 작업 추가
bash .claude/queue/queue-manager.sh add "Task description"

# 작업 시작
bash .claude/queue/queue-manager.sh start

# 작업 완료
bash .claude/queue/queue-manager.sh complete

# 상태 확인
bash .claude/queue/queue-manager.sh status
```

---

## 📊 Queue 상태 파일

### active.json (현재 작업)

```json
{
  "tasks": [
    {
      "id": "task-1234567890",
      "description": "Order Domain 비즈니스 로직 구현",
      "status": "in_progress",
      "started_at": 1699999999.123,
      "context_score": 45,
      "detected_layers": ["domain", "application"]
    }
  ]
}
```

### completed.jsonl (완료 이력)

```json
{"id":"task-1234567890","description":"Order Domain 비즈니스 로직 구현","status":"completed","started_at":1699999999.123,"completed_at":1700000100.456,"duration":101.333,"context_score":45,"detected_layers":["domain","application"]}
```

---

## 🔧 통합 포인트

### 1. Hook 통합

**user-prompt-submit.sh**:
- 키워드 감지 → Queue 자동 추가
- `CONTEXT_SCORE >= 25` → Queue에 추가

**after-tool-use.sh**:
- Write/Edit 도구 → Queue 자동 완료
- LangFuse 로깅

### 2. TodoWrite 통합

**sync-todo-to-queue.py**:
- TodoWrite 상태 변경 → Queue 동기화
- `in_progress` → Queue add
- `completed` → Queue complete

### 3. Statusline 통합

**context-monitor.py**:
- `get_queue_status()` → `📋 N tasks`
- Statusline에 실시간 표시

### 4. LangFuse 통합

**log-queue-event.py**:
- Queue 이벤트 → JSONL 로그
- `queue_add`, `queue_complete` 이벤트

---

## 📈 성능 메트릭

### 예상 효과

| 메트릭 | 수동 Queue | 자동 Queue | 개선율 |
|--------|------------|------------|--------|
| **Queue 명령어 입력** | 매번 수동 | 자동 | 100% 절감 |
| **작업 추적 누락** | 30% | 0% | 100% 개선 |
| **LangFuse 데이터** | 수동 입력 | 자동 수집 | 100% 자동화 |
| **개발자 인지 부하** | 높음 | 낮음 | 70% 감소 |

---

## 🎓 학습 경로

### Day 1: 시스템 이해
1. README.md 읽기 (Queue 시스템 개요)
2. queue-manager.sh 코드 읽기
3. Hook 통합 확인

### Week 1: 실전 사용
1. 자동 Queue 사용 (프롬프트만 입력)
2. Statusline Queue 상태 확인
3. completed.jsonl 로그 분석

### Month 1: 메트릭 분석
1. LangFuse 대시보드 확인
2. Queue 효율 측정
3. 작업 패턴 분석

---

**✅ 이 Queue 시스템은 100% 자동으로 작동합니다!**

**💡 핵심**: 사용자는 프롬프트만 입력하면 됩니다. Hook이 알아서 Queue를 관리합니다.
