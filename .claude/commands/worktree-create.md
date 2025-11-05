# Worktree Create Command

**Git Worktree 생성 및 작업 환경 설정**

---

## 🎯 목적

새로운 기능 개발을 위한 독립적인 Git Worktree 생성:
1. Feature 브랜치 생성
2. Worktree 디렉토리 생성
3. 작업지시서 자동 복사
4. .cursorrules 자동 복사
5. Cursor AI 작업 환경 준비

---

## 📝 사용법

```bash
# 기본 사용 (작업지시서 없음)
/worktree-create order

# 작업지시서 포함
/worktree-create order order-aggregate.md

# 다른 기능 예시
/worktree-create payment payment-aggregate.md
```

---

## 🔄 실행 프로세스

### Step 1: Worktree Manager 스크립트 실행

```bash
bash .claude/scripts/worktree-manager.sh create {feature-name} [work-order]
```

### Step 2: 자동 처리 항목

1. **브랜치 생성**: `feature/{feature-name}` 브랜치 생성 (없는 경우)
2. **Worktree 추가**: `../wt-{feature-name}` 디렉토리에 Worktree 추가
3. **작업지시서 복사**: `.claude/work-orders/{work-order}` → Worktree 루트로 복사
4. **규칙 복사**: `.cursorrules` → Worktree 루트로 복사

---

## 📦 출력

**성공:**
```
✅ Worktree 생성 완료!

📂 Worktree 경로: /Users/sangwon-ryu/wt-order
🌿 브랜치: feature/order
📋 작업지시서: order-aggregate.md (자동 복사)
📝 .cursorrules: 자동 복사

📝 다음 단계:
  1. cd /Users/sangwon-ryu/wt-order
  2. Cursor AI로 Boilerplate 생성
  3. order-aggregate.md 참조하여 코드 작성
  4. git commit
  5. cd /Users/sangwon-ryu/crawlinghub (복귀)
  6. /validate-cursor-changes (검증)
```

**브랜치 이미 존재:**
```
⚠️  브랜치 이미 존재: feature/order
✅ Worktree 추가 완료
```

**작업지시서 없음:**
```
⚠️  작업지시서 없음: .claude/work-orders/invalid-order.md
✅ Worktree 생성 완료 (작업지시서 제외)
```

---

## 🌲 Worktree 구조

생성된 Worktree 디렉토리 구조:

```
/Users/sangwon-ryu/wt-order/
├── adapter-in/
├── adapter-out/
├── application/
├── domain/
├── bootstrap/
├── order-aggregate.md      # 작업지시서 (자동 복사)
├── .cursorrules            # Cursor AI 규칙 (자동 복사)
└── ... (프로젝트 전체 파일)
```

---

## 💡 사용 시나리오

### 시나리오 1: 큐 시스템과 함께 사용

```bash
# 1. 작업 큐에 추가
/queue-add order order-aggregate.md

# 2. 작업 시작
/queue-start order

# 3. Worktree 생성 (수동 또는 자동)
/worktree-create order order-aggregate.md
```

### 시나리오 2: 독립적으로 사용

```bash
# Worktree 생성
/worktree-create payment payment-aggregate.md

# Worktree로 이동
cd ../wt-payment

# Cursor AI에서 작업
# → order-aggregate.md 참조
# → .cursorrules 자동 로드
# → 코드 생성

# 커밋
git add .
git commit -m "feat: Payment Aggregate 생성"

# 메인 프로젝트로 복귀
cd /Users/sangwon-ryu/crawlinghub

# 검증
/validate-cursor-changes
```

---

## ⚠️ 주의사항

**중복 Worktree:**
```
❌ Worktree가 이미 존재함: ../wt-order

기존 Worktree를 제거하려면:
/worktree-remove order
```

**경로 제약:**
- Worktree는 메인 프로젝트의 부모 디렉토리(`../`)에 생성됩니다
- 절대 경로 사용 불가 (상대 경로만 지원)

**브랜치 충돌:**
- 이미 존재하는 브랜치인 경우 기존 브랜치를 사용합니다
- 새 브랜치가 필요한 경우 먼저 브랜치를 삭제하세요

---

## 🔗 관련 커맨드

- `/worktree-remove {feature}` - Worktree 제거
- `/worktree-list` - 활성 Worktree 목록
- `/worktree-status` - Worktree 상태 확인
- `/queue-start {feature}` - 큐 작업 시작 (Worktree 자동 생성 안내)

---

**✅ 이 커맨드는 독립적인 개발 환경을 제공합니다!**

