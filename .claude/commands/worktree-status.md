# Worktree Status Command

**Git Worktree 상태 요약**

---

## 🎯 목적

Worktree 상태 요약 정보 확인:
1. 활성 Worktree 개수
2. 각 Worktree 경로 및 브랜치
3. 메인 프로젝트 정보

---

## 📝 사용법

```bash
# Worktree 상태 확인
/worktree-status
```

---

## 🔄 실행 프로세스

### Step 1: Worktree Manager 스크립트 실행

```bash
bash .claude/scripts/worktree-manager.sh status
```

### Step 2: 상태 정보 수집 및 출력

- 총 Worktree 개수 계산
- 각 Worktree 경로 및 브랜치 정보 추출
- 포맷팅된 출력

---

## 📦 출력

**Worktree가 있는 경우:**
```
ℹ️  Worktree 상태:

활성 Worktree: 2개

  📂 /Users/sangwon-ryu/wt-order
  🌿 feature/order

  📂 /Users/sangwon-ryu/wt-payment
  🌿 feature/payment
```

**Worktree가 없는 경우:**
```
ℹ️  Worktree 상태:

활성 Worktree: 없음
```

---

## 💡 사용 시나리오

### 시나리오 1: 작업 전 상태 확인

```bash
# 현재 Worktree 상태 확인
/worktree-status

# 출력:
# 활성 Worktree: 2개
#   - wt-order (feature/order)
#   - wt-payment (feature/payment)

# 새 Worktree 생성 가능 여부 확인
```

### 시나리오 2: 작업 완료 후 정리

```bash
# 상태 확인
/worktree-status

# 완료된 Worktree 제거
/worktree-remove order
/worktree-remove payment

# 다시 확인
/worktree-status
# 출력: 활성 Worktree: 없음
```

---

## 🔍 상태 정보 상세

**표시 정보:**
- 📂 Worktree 경로: Worktree 디렉토리 절대 경로
- 🌿 브랜치 이름: 체크아웃된 브랜치
- 총 개수: 메인 프로젝트를 제외한 추가 Worktree 개수

**메인 프로젝트:**
- 메인 프로젝트는 카운트에 포함되지 않습니다
- 총 개수는 추가로 생성된 Worktree만 포함합니다

---

## 🔗 관련 커맨드

- `/worktree-create {feature}` - Worktree 생성
- `/worktree-remove {feature}` - Worktree 제거
- `/worktree-list` - Worktree 목록 (상세)

---

**✅ 이 커맨드는 Worktree 상태를 요약합니다!**

