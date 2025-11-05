# Queue Start Command

**큐에서 작업을 시작하고 Worktree 자동 생성**

---

## 🎯 목적

큐에서 작업을 시작하며 자동으로:
1. 작업 상태 → `in_progress`
2. Worktree 생성 가이드 제공
3. 시작 시간 기록
4. 다음 단계 안내

---

## 📝 사용법

```bash
# 큐에서 작업 시작
/queue-start order
```

---

## 🔄 실행 프로세스

### Step 1: 큐 상태 업데이트

```bash
python3 .claude/scripts/queue-manager.py start order
```

**JSON 업데이트:**
```json
{
  "id": 1,
  "feature": "order",
  "work_order": "order-aggregate.md",
  "priority": "normal",
  "status": "in_progress",  ← 변경
  "created_at": "2024-11-04T17:00:00Z",
  "started_at": "2024-11-04T17:05:00Z",  ← 기록
  "completed_at": null
}
```

### Step 2: Worktree 생성 안내

작업이 시작되면 자동으로 Worktree 생성 스크립트를 안내합니다.

---

## 📦 출력

```
✅ 작업 시작됨: order

📝 다음 단계:
  1. bash .claude/scripts/worktree-manager.sh create order order-aggregate.md
  2. Cursor AI로 Boilerplate 생성
  3. Git Commit
  4. python3 .claude/scripts/queue-manager.py complete order

🌲 Worktree 자동 생성 (권장):
  bash .claude/scripts/worktree-manager.sh create order order-aggregate.md
  
  → Worktree 경로: ../wt-order
  → 브랜치: feature/order
  → 작업지시서: order-aggregate.md (자동 복사)
  → .cursorrules: 자동 복사
```

---

## 🌲 Worktree 자동 생성 흐름

작업 시작 시 `worktree-manager.sh`가 자동으로:

1. **브랜치 생성**: `feature/order`
2. **Worktree 추가**: `../wt-order`
3. **작업지시서 복사**: `order-aggregate.md`
4. **규칙 복사**: `.cursorrules`

**Cursor AI 작업 환경 준비 완료!**

---

## ⚠️ 주의사항

**이미 진행 중인 작업:**
```
⚠️  작업이 이미 진행 중: order

현재 상태: in_progress
시작 시간: 2024-11-04T17:05:00Z
```

**존재하지 않는 작업:**
```
❌ 작업을 찾을 수 없음: order

큐에 추가하려면:
/queue-add order order-aggregate.md
```

---

## 🔗 관련 커맨드

- `/queue-add {feature}` - 작업 추가
- `/queue-complete {feature}` - 작업 완료
- `/queue-status` - 큐 상태 확인

---

**✅ 이 커맨드는 작업 시작 및 Worktree 생성을 담당합니다!**
