# /planner Skill

기획 및 Task 분해 스킬.

## 사용법

```bash
/planner "결제 기능 구현"
/planner --analyze "영향도 분석"
```

## 실행 흐름

1. `planning_context()` → 프로젝트 구조 파악
2. `serena.search_for_pattern()` → 영향도 분석
3. Task 분해 (컨텍스트 크기 ~15K 기준)
4. `serena.write_memory("epic-{name}")` → Epic 저장
