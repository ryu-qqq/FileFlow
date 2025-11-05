# Worktree List Command

**활성 Git Worktree 목록 확인**

---

## 🎯 목적

현재 활성화된 모든 Worktree 목록 확인:
1. Worktree 경로
2. 브랜치 정보
3. 총 Worktree 개수

---

## 📝 사용법

```bash
# 활성 Worktree 목록 확인
/worktree-list
```

---

## 🔄 실행 프로세스

### Step 1: Worktree Manager 스크립트 실행

```bash
bash .claude/scripts/worktree-manager.sh list
```

### Step 2: Git Worktree 목록 출력

```bash
git worktree list
```

---

## 📦 출력

**Worktree가 있는 경우:**
```
ℹ️  활성 Worktree 목록:

/Users/sangwon-ryu/crawlinghub              5c320fa [main]
/Users/sangwon-ryu/wt-order                 8a9b2c1 [feature/order]
/Users/sangwon-ryu/wt-payment               3d4e5f6 [feature/payment]
```

**Worktree가 없는 경우:**
```
ℹ️  활성 Worktree 목록:

/Users/sangwon-ryu/crawlinghub              5c320fa [main]
```

---

## 💡 사용 시나리오

### 시나리오 1: 진행 중인 작업 확인

```bash
# 현재 활성 Worktree 확인
/worktree-list

# 출력:
# /Users/sangwon-ryu/wt-order     [feature/order]
# /Users/sangwon-ryu/wt-payment   [feature/payment]

# 특정 Worktree로 이동
cd /Users/sangwon-ryu/wt-order
```

### 시나리오 2: Worktree 정리 전 확인

```bash
# 활성 Worktree 확인
/worktree-list

# 불필요한 Worktree 제거
/worktree-remove order
```

---

## 🔗 관련 커맨드

- `/worktree-create {feature}` - Worktree 생성
- `/worktree-remove {feature}` - Worktree 제거
- `/worktree-status` - Worktree 상태 요약

---

**✅ 이 커맨드는 현재 활성 Worktree를 확인합니다!**

