---
name: test-fix-complete
description: test-audit 감사 리포트 완료 처리. 모든 항목 완료 확인 후 문서 상태 업데이트 및 아카이브.
context: fork
agent: test-fixer
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-fix-complete

test-audit 감사 리포트의 모든 항목이 완료되었을 때 문서를 완료 처리합니다.

## 사용법

```bash
/test-fix-complete adapter-in token              # 문서 완료 처리 (상태 배지만 추가)
/test-fix-complete adapter-in token --archive    # 완료 처리 후 아카이브 폴더로 이동
/test-fix-complete adapter-in token --all        # 모든 우선순위 완료 확인 후 완료 처리
```

## 입력

- `$ARGUMENTS[0]`: 레이어 (domain, application, adapter-out, adapter-in)
- `$ARGUMENTS[1]`: 패키지명
- `$ARGUMENTS[2]`: (선택) `--archive`, `--all`

## 전제 조건

`/test-fix`로 HIGH/MEDIUM 우선순위 항목이 완료되어야 합니다.

## 처리 흐름

```
/test-audit adapter-in token     ← 감사 리포트 생성
         ↓
/test-fix adapter-in token       ← HIGH/MEDIUM 항목 보완
         ↓
/test-fix-complete adapter-in token  ← 완료 처리
```

## 완료 처리 작업

### 1. 문서 상단에 완료 상태 배지 추가

```markdown
# Test Coverage Audit: adapter-in/token

> **상태**: ✅ **완료** (또는 🟡 **부분 완료**)
> **보완 완료일**: YYYY-MM-DD
> **최종 업데이트**: YYYY-MM-DD
```

### 2. 요약 섹션 업데이트

- 테스트 커버리지 업데이트
- 우선순위 이슈 수 업데이트
- 완료된 항목 표시

### 3. 체크리스트 완료 표시

- 처리된 항목에 ✅ 표시

### 4. 완료 섹션 추가/업데이트

- 최종 완료 내역 정리
- 남은 항목 명시 (있는 경우)

### 5. 아카이브 처리 (`--archive` 옵션 시, 기본값)

- `claudedocs/test-audit/completed/` 폴더 생성 (없으면)
- 문서를 해당 폴더로 이동
- 문서 상단에 "위치: `claudedocs/test-audit/completed/` ✅ (완료 처리됨 - 아카이브됨)" 추가
- `completed/README.md` 생성/업데이트 (폴더 설명)

## 완료 조건 확인 (`--all` 옵션 시)

다음 조건을 모두 만족해야 완료 처리 가능:

- [ ] HIGH 우선순위 이슈: 0개
- [ ] MEDIUM 우선순위 이슈: 0개 또는 의도적으로 남김 (문서에 명시)
- [ ] 테스트 커버리지: 목표 달성 (95%+ 또는 프로젝트 기준)

## 출력 형식

```
✅ 감사 리포트 완료 처리: adapter-in/token

📋 완료 상태:
   - HIGH 우선순위: 0개 ✅
   - MEDIUM 우선순위: 0개 ✅ (또는 남은 항목 명시)
   - 테스트 커버리지: 95%+ ✅

📄 문서 업데이트:
   → claudedocs/test-audit/adapter-in-token-audit.md
   → 완료 상태 배지 추가
   → 요약 섹션 업데이트

📦 아카이브 처리 (--archive 옵션):
   → claudedocs/test-audit/completed/adapter-in-token-audit.md
```

## 문서 보존 정책

**권장**: 문서를 삭제하지 않고 보존합니다.

**이유**:
- 분석 결과와 기록 보존
- 향후 재검토 가능
- 프로젝트 히스토리 추적
- 완료 상태는 배지로 명확히 표시

**대안**:
- 완료된 문서는 `completed/` 폴더로 이동
- 활성 문서와 완료 문서 분리 관리
