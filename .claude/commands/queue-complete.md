# Queue Complete Command

**작업 완료 및 통계 표시**

---

## 🎯 목적

작업을 완료 처리하며 자동으로:
1. 작업 상태 → `completed`
2. 완료 시간 기록
3. 소요 시간 계산
4. Completed 목록으로 이동
5. 통계 표시

---

## 📝 사용법

```bash
# 작업 완료
/queue-complete order
```

---

## 🔄 실행 프로세스

### Step 1: 큐 상태 업데이트

```bash
python3 .claude/scripts/queue-manager.py complete order
```

**JSON 업데이트:**
```json
{
  "queue": [],  ← order 제거됨
  "completed": [
    {
      "id": 1,
      "feature": "order",
      "work_order": "order-aggregate.md",
      "priority": "normal",
      "status": "completed",  ← 변경
      "created_at": "2024-11-04T17:00:00Z",
      "started_at": "2024-11-04T17:05:00Z",
      "completed_at": "2024-11-04T17:30:00Z"  ← 기록
    }
  ]
}
```

### Step 2: 통계 계산 및 표시

자동으로 다음 통계를 계산합니다:
- 소요 시간 (started_at → completed_at)
- 남은 작업 수 (queue 배열)
- 완료된 작업 수 (completed 배열)

---

## 📦 출력

```
✅ 작업 완료됨: order

📊 통계:
  소요 시간: 25분
  남은 작업: 2개
  완료된 작업: 1개

📝 다음 단계:
  1. cd ../wt-order (Worktree로 이동)
  2. git log (커밋 확인)
  3. cd ~/claude-spring-standards (복귀)
  4. git merge feature/order (병합)
  5. bash .claude/scripts/worktree-manager.sh remove order (Worktree 제거)
  6. git branch -d feature/order (브랜치 삭제, 선택적)
```

---

## ⚠️ 주의사항

**진행 중이 아닌 작업:**
```
⚠️  작업이 진행 중이 아님: order

현재 상태: pending
힌트: /queue-start order 먼저 실행하세요
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
- `/queue-start {feature}` - 작업 시작
- `/queue-list` - 큐 목록 확인
- `/queue-status` - 큐 상태 확인

---

**✅ 이 커맨드는 작업 완료 및 통계 표시를 담당합니다!**
