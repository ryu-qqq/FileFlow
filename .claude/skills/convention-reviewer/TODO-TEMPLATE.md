# Spring DDD Convention Refactoring TODOs

**생성일시**: {TIMESTAMP}
**스캔 범위**: {SCAN_SCOPE}
**스캔한 파일 수**: {FILE_COUNT}개
**발견된 위반 사항**: {TOTAL_VIOLATIONS}개

---

## 📊 요약

| 우선순위 | 위반 건수 | 예상 시간 |
|----------|-----------|-----------|
| 🔴 Critical | {CRITICAL_COUNT}개 | {CRITICAL_TIME}시간 |
| 🟡 Important | {IMPORTANT_COUNT}개 | {IMPORTANT_TIME}시간 |
| 🟢 Recommended | {RECOMMENDED_COUNT}개 | {RECOMMENDED_TIME}시간 |
| **합계** | **{TOTAL_VIOLATIONS}개** | **{TOTAL_TIME}시간** |

### 레이어별 위반 분포

- **Domain**: {DOMAIN_COUNT}개
- **Application**: {APPLICATION_COUNT}개
- **Persistence**: {PERSISTENCE_COUNT}개
- **REST API**: {REST_API_COUNT}개
- **Test**: {TEST_COUNT}개

---

## 🔴 Critical (Zero-Tolerance 위반)

Zero-Tolerance 규칙 위반은 **즉시 수정 필수**입니다.

### [ ] {VIOLATION_ID}: {VIOLATION_TITLE}

**파일**: `{FILE_PATH}:{LINE_NUMBER}`
**위반 규칙**: {RULE_NAME}
**심각도**: 🔴 Critical

**현재 코드**:
```java
{BEFORE_CODE}
```

**문제점**:
- {PROBLEM_DESCRIPTION}

**수정 방법**:
```java
{AFTER_CODE}
```

**참고 문서**:
- `{CONVENTION_DOC_PATH}`

**예상 작업 시간**: {ESTIMATED_TIME}분

---

## 🟡 Important (레이어 규칙 위반)

레이어별 코딩 컨벤션 위반 항목입니다. 리팩토링을 권장합니다.

### [ ] {VIOLATION_ID}: {VIOLATION_TITLE}

**파일**: `{FILE_PATH}:{LINE_NUMBER}`
**위반 규칙**: {RULE_NAME}
**심각도**: 🟡 Important

**현재 코드**:
```java
{BEFORE_CODE}
```

**문제점**:
- {PROBLEM_DESCRIPTION}

**수정 방법**:
```java
{AFTER_CODE}
```

**참고 문서**:
- `{CONVENTION_DOC_PATH}`

**예상 작업 시간**: {ESTIMATED_TIME}분

---

## 🟢 Recommended (Best Practices)

Best Practices 적용을 권장하는 항목입니다. 점진적으로 개선하세요.

### [ ] {VIOLATION_ID}: {VIOLATION_TITLE}

**파일**: `{FILE_PATH}:{LINE_NUMBER}`
**권장 사항**: {RULE_NAME}
**심각도**: 🟢 Recommended

**현재 코드**:
```java
{BEFORE_CODE}
```

**개선 사항**:
- {IMPROVEMENT_DESCRIPTION}

**개선 방법**:
```java
{AFTER_CODE}
```

**참고 문서**:
- `{CONVENTION_DOC_PATH}`

**예상 작업 시간**: {ESTIMATED_TIME}분

---

## 📝 작업 진행 가이드

### 1. Cursor AI에게 전달

이 TODO 문서를 Cursor AI에게 전달하여 자동 리팩토링을 시작하세요:

```
Cursor AI 지시사항:
".claude/work-orders/refactoring-todos.md를 읽고 우선순위 순으로 리팩토링을 진행해줘.
각 항목을 완료한 후 체크박스를 업데이트하고 커밋해줘."
```

### 2. 작업 순서

1. **🔴 Critical** 항목부터 시작
2. 각 항목 완료 후 체크박스 `[x]` 표시
3. 테스트 실행 (`./gradlew test`)
4. ArchUnit 검증 (`./gradlew test --tests "*ArchitectureTest"`)
5. Git 커밋 (커밋 메시지 예시: `refactor: Fix Lombok violation in Order.java`)

### 3. 검증 체크리스트

각 항목 완료 후 다음을 확인하세요:

- [ ] 코드가 정상 컴파일되는가?
- [ ] 기존 테스트가 통과하는가?
- [ ] ArchUnit 테스트가 통과하는가?
- [ ] Git Pre-commit Hook이 통과하는가?
- [ ] 비즈니스 로직이 변경되지 않았는가?

### 4. 진행 상황 추적

- **시작 일시**: {START_TIME}
- **완료 항목**: {COMPLETED_COUNT}/{TOTAL_VIOLATIONS}
- **진행률**: {PROGRESS_PERCENTAGE}%
- **남은 예상 시간**: {REMAINING_TIME}시간

---

## 🚨 주의사항

1. **한 번에 하나씩 수정**: 여러 항목을 동시에 수정하지 마세요
2. **테스트 우선**: 수정 전 관련 테스트를 먼저 확인하세요
3. **커밋 단위**: 각 항목마다 별도 커밋을 생성하세요
4. **검증 필수**: 수정 후 반드시 테스트 + ArchUnit 실행
5. **문서 참고**: 불확실하면 참고 문서를 반드시 읽으세요

---

## 📞 도움이 필요한 경우

- **컨벤션 질문**: `docs/coding_convention/` 문서 참조
- **Skill 재실행**: `convention-reviewer` Skill 다시 호출
- **Git Hook 문제**: `.claude/hooks/pre-commit` 확인
- **ArchUnit 오류**: `bootstrap/src/test/java/.../architecture/` 확인

---

**생성 시스템**: Claude Code - convention-reviewer Skill
**버전**: 1.0.0
**프로젝트**: Spring DDD Hexagonal Architecture Standards
