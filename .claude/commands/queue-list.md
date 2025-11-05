# Queue List Command

**큐 목록 확인 (대기 중 + 진행 중)**

---

## 🎯 목적

현재 큐에 있는 모든 작업을 표시:
1. 대기 중인 작업 (pending)
2. 진행 중인 작업 (in_progress)
3. 작업 ID, 우선순위, 작업지시서
4. 시작 시간 (진행 중인 경우)

---

## 📝 사용법

```bash
# 큐 목록 확인
/queue-list
```

---

## 🔄 실행 프로세스

### Step 1: 큐 파일 읽기

```bash
python3 .claude/scripts/queue-manager.py list
```

**JSON 읽기:**
```json
{
  "queue": [
    {
      "id": 1,
      "feature": "order",
      "work_order": "order-aggregate.md",
      "priority": "normal",
      "status": "in_progress",
      "started_at": "2024-11-04T17:05:00Z"
    },
    {
      "id": 2,
      "feature": "payment",
      "work_order": "payment-aggregate.md",
      "priority": "high",
      "status": "pending"
    },
    {
      "id": 3,
      "feature": "product",
      "work_order": null,
      "priority": "normal",
      "status": "pending"
    }
  ]
}
```

### Step 2: 포맷팅 및 표시

각 작업을 상태, 우선순위 아이콘과 함께 표시합니다.

---

## 📦 출력

```
📋 작업 큐

🔄 🔥 payment
   ID: 2 | 상태: in_progress
   작업지시서: payment-aggregate.md
   시작: 2024-11-04T17:05:00Z

⏳ 📌 order
   ID: 1 | 상태: pending
   작업지시서: order-aggregate.md

⏳ 📌 product
   ID: 3 | 상태: pending
   작업지시서: None

📝 다음 단계:
1. /queue-start {feature} (작업 시작)
2. /queue-status (상태 확인)
```

**아이콘 설명:**
- `⏳` - 대기 중 (pending)
- `🔄` - 진행 중 (in_progress)
- `🔥` - 높은 우선순위 (high)
- `📌` - 일반 우선순위 (normal)

---

## ⚠️ 주의사항

**큐가 비어있는 경우:**
```
ℹ️  큐에 작업이 없습니다

작업을 추가하려면:
/queue-add {feature} [work-order] [--priority high|normal]
```

---

## 🔗 관련 커맨드

- `/queue-add {feature}` - 작업 추가
- `/queue-start {feature}` - 작업 시작
- `/queue-complete {feature}` - 작업 완료
- `/queue-status` - 큐 상태 확인

---

**✅ 이 커맨드는 큐 목록 확인을 담당합니다!**
