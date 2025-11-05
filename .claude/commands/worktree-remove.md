# Worktree Remove Command

**Git Worktree 제거 및 정리**

---

## 🎯 목적

작업 완료 후 Worktree 제거:
1. Worktree 디렉토리 제거
2. 브랜치는 유지 (병합 후 수동 삭제)
3. 변경사항 확인 및 안전 제거

---

## 📝 사용법

```bash
# Worktree 제거
/worktree-remove order

# 다른 기능 예시
/worktree-remove payment
```

---

## 🔄 실행 프로세스

### Step 1: Worktree Manager 스크립트 실행

```bash
bash .claude/scripts/worktree-manager.sh remove {feature-name}
```

### Step 2: 자동 처리 항목

1. **변경사항 확인**: 커밋되지 않은 변경사항이 있는지 확인
2. **사용자 확인**: 변경사항이 있으면 삭제 확인 요청
3. **Worktree 제거**: `git worktree remove` 실행
4. **브랜치 유지**: 브랜치는 유지되어 병합 가능

---

## 📦 출력

**성공 (변경사항 없음):**
```
✅ Worktree 제거 완료!

🌿 브랜치는 유지됩니다: feature/order

📝 다음 단계:
  1. git merge feature/order (Merge)
  2. git branch -d feature/order (브랜치 삭제)
```

**변경사항 있음 (확인 요청):**
```
⚠️  커밋되지 않은 변경사항 존재
계속 진행하시겠습니까? (y/N): 
```

**Worktree 없음:**
```
❌ Worktree가 존재하지 않음: ../wt-order
```

---

## 🔄 Worktree 제거 후 워크플로우

### Step 1: Worktree 제거

```bash
/worktree-remove order
```

### Step 2: 메인 프로젝트로 복귀

```bash
cd /Users/sangwon-ryu/crawlinghub
```

### Step 3: 브랜치 병합

```bash
git checkout main
git merge feature/order
```

### Step 4: 브랜치 삭제

```bash
git branch -d feature/order
```

### Step 5: 큐 완료 (선택)

```bash
/queue-complete order
```

---

## ⚠️ 주의사항

**커밋되지 않은 변경사항:**
- Worktree에 커밋되지 않은 변경사항이 있으면 삭제 전 확인을 요청합니다
- `y` 입력 시 강제 삭제됩니다 (변경사항 손실)

**브랜치 유지:**
- Worktree 제거 시 브랜치는 자동으로 삭제되지 않습니다
- 병합 후 수동으로 브랜치를 삭제해야 합니다

**원격 브랜치:**
- 원격 브랜치가 있는 경우 별도로 삭제해야 합니다:
  ```bash
  git push origin --delete feature/order
  ```

---

## 🔗 관련 커맨드

- `/worktree-create {feature}` - Worktree 생성
- `/worktree-list` - 활성 Worktree 목록
- `/worktree-status` - Worktree 상태 확인
- `/queue-complete {feature}` - 큐 작업 완료

---

**✅ 이 커맨드는 Worktree를 안전하게 제거합니다!**

