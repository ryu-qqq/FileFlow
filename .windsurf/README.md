# Cascade Workflows - 자동화 & 검증 시스템

**✅ Cascade 완전 호환**: 이 디렉토리의 모든 파일은 IntelliJ Cascade가 직접 실행할 수 있는 Markdown 형식입니다.

---

## 🎯 설계 철학

### Claude Code vs Cascade 역할 분담

| 항목 | Claude Code | Cascade Workflows |
|------|-------------|-------------------|
| **강점** | 컨텍스트 유지, 비즈니스 로직 | 빠른 자동화, 파이프라인 |
| **약점** | 단순 반복 작업 느림 | 컨텍스트 이해 부족 |
| **용도** | 도메인 코드 생성, 상세 구현 | 검증, 테스트, 빌드, 배포 |
| **예시** | OrderDomain 비즈니스 로직 구현 | 컨벤션 체크, Unit 테스트 실행 |

### 통합 워크플로우 예시

```
1. Claude Code: 비즈니스 로직 구현
   /sc:implement Order aggregate with place/cancel/confirm

2. Cascade: 컨벤션 자동 검증
   /validate-conventions
   → 실패 시 TODO 주석 자동 추가

3. Claude Code: TODO 수정
   "Fix the TODO comments in OrderDomain.java"

4. Cascade: 테스트 실행
   /run-unit-tests

5. Cascade: 빌드
   /build-docker

6. Cascade: 배포
   /deploy-dev
```

---

## 📂 디렉토리 구조 (SSOT 적용)

```
프로젝트 루트/
├── tools/pipeline/                    # ✨ NEW: 단일 진실의 원천 (SSOT)
│   ├── common.sh                      # 공통 설정 및 헬퍼
│   ├── validate_conventions.sh        # 컨벤션 검증 로직
│   ├── test_unit.sh                   # 유닛 테스트 로직
│   └── pr_gate.sh                     # PR 게이트 파이프라인
│
├── .cascade/                          # ✨ NEW: 메트릭 & 리포트
│   ├── metrics.jsonl                  # 실행 메트릭 (시간, 성공률)
│   └── report.md                      # 실패 리포트
│
└── .windsurf/
    ├── README.md (이 파일)
    ├── rules.md ⭐ (핵심 규칙 - 7,000자, Cascade 자동 로드)
    └── workflows/ (12개 Markdown - SSOT 얇은 래퍼)
        ├── 검증 (3개)
        ├── 테스트 (4개)
        ├── 빌드/배포 (4개)
        ├── 파이프라인 (3개)
        ├── Git 자동화 (4개)
        └── 코드 품질 (3개)
```

**주요 사항**:
- ✅ **SSOT 적용**: 실제 로직은 `tools/pipeline/`에만 존재
- ✅ **얇은 래퍼**: Cascade workflows는 스크립트 호출만
- ✅ **메트릭 수집**: 모든 실행 결과 자동 기록
- ✅ **No Drift**: Cascade와 CI가 동일한 스크립트 사용
---

## 📋 Rules 파일 정보

### rules.md (Cascade 자동 로드) ⭐

**파일 정보**:
- **크기**: ~7,000자 (322줄)
- **내용**: Zero-Tolerance 규칙 9개 + 간단한 예시
- **형식**: Cascade 최적화 버전 (구 rules-core.md)

**장점**:
- ✅ Cascade의 11,500자 권장 사이즈 내
- ✅ 빠른 로딩 및 적용
- ✅ 핵심 규칙에 집중
- ✅ 메모리 효율적

**사용 방법**:
- Windsurf IDE가 자동으로 로드
- `.windsurf/rules.md` 파일명으로 자동 인식
- 상세 규칙은 `docs/coding_convention/`에서 참조

### 권장 사용 전략

```
1. Windsurf IDE 작업: rules.md 자동 로드됨
2. 상세 규칙 참조: docs/coding_convention/ 문서 읽기
3. 자동 검증: tools/pipeline/validate_conventions.sh 실행
4. Cache 시스템: .claude/cache/rules/ 고속 검색
```

---

## 🚀 Cascade Workflows (12개) - 정리 완료 ✨

### ⭐ 필수 (Essential) - 5개

1. **`/pipeline-pr`** - PR 검증 파이프라인 (SSOT)
   - Format → Conventions → Tests → Architecture → Coverage
   - 실제 로직: `tools/pipeline/pr_gate.sh`

2. **`/validate-conventions`** - Zero-Tolerance 규칙 검증
   - Lombok, Law of Demeter, JPA 관계, Setter 등
   - 실제 로직: `tools/pipeline/validate_conventions.sh`

3. **`/run-unit-tests`** - 단위 테스트 (Fast/Full Lane)
   - `--impacted` 옵션으로 빠른 피드백
   - 실제 로직: `tools/pipeline/test_unit.sh`

4. **`/validate-architecture`** - ArchUnit 검증
   - 헥사고날 아키텍처, 레이어 의존성
   - 테스트 위치: `bootstrap-web-api/src/test/.../architecture/`

5. **`/git-pr`** - GitHub PR 자동 생성
   - gh CLI 사용, 자동 라벨, 템플릿 적용

### 📌 권장 (Recommended) - 7개

6. **`/format-code`** - Spotless 포맷팅
   - Google Java Format 적용

7. **`/git-commit-workflow`** - Conventional Commits
   - 표준화된 커밋 메시지 가이드

8. **`/git-workflow`** - Git Branching 전략
   - Feature/Hotfix/Release 워크플로우

9. **`/validate-tests`** - JaCoCo 커버리지 검증
   - 최소 80% 커버리지 요구

10. **`/run-integration-tests`** - 통합 테스트
    - Testcontainers 기반 실제 DB 테스트

11. **`/run-e2e-tests`** - E2E 테스트
    - RestAssured 기반 전체 시스템 테스트

12. **`/run-all-tests`** - 전체 테스트 실행
    - Unit → Integration → E2E 순차 실행

---

## ✅ Cascade 인식 요구사항

### 필수 형식

```markdown
---
description: {간단한 설명}  ← 필수!
---

# {Title}

{Description}  ← 필수! (3번째 줄)

## Parameters
...
```

### 제약사항

- **Description 필수**: 첫 3줄 형식 정확히 준수
- **파일 크기**: 11,500자 이하 (12,000자는 인식 안 됨)
- **디렉토리 권한**: 755 (읽기 권한 필수)

---

**생성일**: 2025-10-29
**버전**: 1.0.0
**IDE**: IntelliJ IDEA + Codeium/Windsurf Plugin
