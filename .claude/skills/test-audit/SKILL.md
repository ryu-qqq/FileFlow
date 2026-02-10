---
name: test-audit
description: 테스트 커버리지 갭 분석. 레이어별 소스 대비 테스트 누락/시나리오 부족을 분석하고 권장 조치 문서화.
context: fork
agent: test-auditor
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /test-audit

레이어별 테스트 커버리지 갭을 분석하고 권장 조치를 문서화합니다.

## 사용법

```bash
/test-audit domain seller             # Domain 레이어 seller 패키지 감사
/test-audit application seller        # Application 레이어 seller 패키지 감사
/test-audit domain --all              # Domain 레이어 전체 감사
/test-audit domain seller --fix       # 분석 후 HIGH 항목 자동 보완
/test-audit domain --high-only        # HIGH 우선순위만 리포트
/test-audit application --pattern-check  # 컨벤션 위반만 검사
```

## 입력

- `$ARGUMENTS[0]`: 레이어 (domain, application, adapter-out, adapter-in)
- `$ARGUMENTS[1]`: 패키지명 또는 `--all`
- `$ARGUMENTS[2]`: (선택) `--fix`, `--high-only`, `--pattern-check`

## 분석 항목

| 유형 | 설명 |
|------|------|
| `MISSING_TEST` | 소스 클래스에 대응하는 테스트 파일 없음 |
| `MISSING_FIXTURES` | testFixtures 없어서 하드코딩 사용 |
| `MISSING_METHOD` | public 메서드 테스트 누락 |
| `MISSING_EDGE_CASE` | 예외/null/경계값 테스트 부족 |
| `MISSING_STATE_TRANSITION` | 상태 전이 시나리오 누락 |
| `PATTERN_VIOLATION` | 프로젝트 컨벤션 불일치 |

## 우선순위 판정 (3축)

- **커버리지 갭**: 테스트 없음(HIGH) → 메서드 누락(MED) → 엣지케이스 부족(LOW)
- **클래스 역할**: Aggregate/Service(HIGH) → Entity/Factory(MED) → VO/Assembler(LOW)
- **복잡도**: 메서드 5+(HIGH) → 3~4개(MED) → 1~2개(LOW)

## 출력

```
→ claudedocs/test-audit/{layer}-{package}-audit.md
```
