---
name: load
description: 세션 시작 시 프로젝트 컨텍스트 로딩. Serena 프로젝트 자동 활성화 + Spring Standards 규칙 캐싱 + 프로젝트 현황 스캔.
context: fork
agent: session-loader
allowed-tools: Read, Write, Glob, Grep, Bash
---

# /load

세션 시작 시 프로젝트 컨텍스트를 로딩합니다.

## 사용법

```bash
/load                    # 기본 로딩 (캐시 우선)
/load --refresh          # 규칙 인덱스 캐시 무시하고 새로 조회
/load --full             # 모든 메모리 상세 내용 포함
/load --status-only      # 프로젝트 현황만 (Git 상태, 브랜치)
```

## 수행 작업

1. **Serena 프로젝트 자동 활성화** (`activate_project` - 자동 실행)
   - 현재 작업 디렉토리를 Serena에 등록
   - 메모리 컨텍스트 활성화

2. 기존 메모리 복원 (plans, epics, jira 등)

3. Spring Standards 규칙 인덱스 캐싱 (`list_rules` → Serena memory)

4. Git 상태 + 진행 중인 계획 스캔

5. 컨텍스트 요약 반환

## 주의사항

- `activate_project`는 session-loader 에이전트가 자동으로 실행합니다.
- 수동으로 호출할 필요가 없습니다.
- Serena MCP 서버가 실행 중이어야 합니다.

## 예상 출력

```
🔄 세션 로딩 시작...

✅ Serena 프로젝트 활성화
✅ 메모리 복원: 5개 항목
✅ 규칙 인덱스: 47개 규칙 캐싱됨

📋 현재 브랜치: dev
📝 미커밋 변경: 2개 파일
📊 최근 커밋: e50a9d68 chore: .claude 설정 파일 구조 개편

🧠 복원된 컨텍스트:
   - current_plan: "Claude Code 설정 리팩토링"
   - session_summary: "에이전트 및 스킬 파일 정리"

📂 진행 중인 계획:
   - claude-code-refactoring-plan.md
   - admin-auth-brand-category.md

🚀 세션 준비 완료
```
