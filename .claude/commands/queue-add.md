# Queue Add Command

**작업 큐에 새 작업 추가**

---

## 🎯 목적

작업을 큐에 추가하여 체계적으로 관리:
1. Feature 이름 등록
2. 작업지시서 연결
3. 우선순위 설정
4. 작업 순서 관리

---

## 📝 사용법

```bash
# 기본 사용
/queue-add order

# 작업지시서 포함
/queue-add order order-aggregate.md

# Claude 예상 시간 포함 ⭐ NEW
/queue-add order order-aggregate.md --estimate "30분"

# 높은 우선순위 + 예상 시간
/queue-add payment payment-aggregate.md --priority high --estimate "1시간 30분"

# 일반 우선순위 (기본값)
/queue-add product --priority normal --estimate "45분"
```

---

## 🔄 실행 프로세스

### Step 1: 큐 시스템 호출

```bash
python3 .claude/scripts/queue-manager.py add {feature} [work-order] [--priority high|normal]
```

### Step 2: JSON 큐에 추가

**`.claude/work-queue.json` 업데이트:**

```json
{
  "queue": [
    {
      "id": 1,
      "feature": "order",
      "work_order": "order-aggregate.md",
      "priority": "normal",
      "status": "pending",
      "created_at": "2024-11-04T17:00:00Z",
      "started_at": null,
      "completed_at": null,
      "estimated_time": "30분",
      "actual_time": null,
      "accuracy": null,
      "code_lines": 0,
      "files_created": 0,
      "interruptions": 0
    }
  ],
  "completed": [],
  "metadata": {
    "version": "1.0",
    "created_at": "2024-11-04T17:00:00Z",
    "last_updated": "2024-11-04T17:00:00Z"
  }
}
```

---

## 📦 출력

**성공:**
```
✅ 작업 추가됨: order
  ID: 1
  작업지시서: order-aggregate.md
  우선순위: normal

📝 다음 단계:
1. /queue-start order (작업 시작)
2. 또는 /queue-list (큐 목록 확인)
```

**중복:**
```
⚠️  작업이 이미 큐에 존재: order

현재 상태: pending
작업지시서: order-aggregate.md
```

---

## 🔗 관련 커맨드

- `/queue-list` - 큐 목록 확인
- `/queue-status` - 큐 상태 확인
- `/queue-start {feature}` - 작업 시작
- `/queue-complete {feature}` - 작업 완료

---

**✅ 이 커맨드는 작업 큐 추가를 담당합니다!**
