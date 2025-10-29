---
description: PR 생성 시 검증 파이프라인 (Thin Wrapper)
---

# PR Pipeline

**🎯 역할**: PR 승인 전 전체 검증 파이프라인 (SSOT 래퍼)

**📋 실제 로직**: `tools/pipeline/pr_gate.sh` (단일 진실의 원천)

## What It Does

이 워크플로우는 다음 검증을 **자동으로** 실행합니다:

1. ✅ **Code Format** - Spotless check
2. ✅ **Convention Validation** - Zero-Tolerance 규칙 (Lombok, Law of Demeter 등)
3. ✅ **Unit Tests** - 전체 유닛 테스트 (병렬 실행)
4. ✅ **Architecture Validation** - ArchUnit 테스트 (레이어 의존성)
5. ✅ **Test Coverage** - JaCoCo 커버리지 검증

## Usage

### 기본 실행 (로컬)

```bash
./tools/pipeline/pr_gate.sh
```

### CI에서 실행

```yaml
# .github/workflows/pr.yml
jobs:
  pr-gate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run PR Gate
        run: ./tools/pipeline/pr_gate.sh
```

## Parameters

없음 - 스크립트가 모든 검증을 자동으로 수행합니다.

## Output

**성공 시**:
```
🎉 PR Gate Pipeline PASSED!
Duration: 120s

✅ All checks passed. PR is ready for review!
```

**실패 시**:
```
❌ PR Gate Pipeline FAILED
Duration: 180s

Please fix the issues above and run again.
Reports available at:
  - Tests: build/reports/tests/test/index.html
  - Coverage: build/reports/jacoco/test/html/index.html
  - Failure details: .cascade/report.md
```

## Metrics

실행 결과는 자동으로 `.cascade/metrics.jsonl`에 기록됩니다:
- Task name: `pr_gate`
- Status code: `0` (성공) / `1` (실패)
- Duration: 초 단위

## Architecture

```
Cascade Workflow (이 파일)
        ↓
   얇은 래퍼 역할
        ↓
tools/pipeline/pr_gate.sh (SSOT)
        ↓
    실제 검증 로직
```

## Benefits of SSOT

- ✅ **No Drift**: Cascade와 CI가 동일한 스크립트 사용
- ✅ **Single Update**: 로직 변경 시 한 곳만 수정
- ✅ **Version Control**: Git으로 스크립트 버전 관리
- ✅ **Metrics**: 자동 메트릭 수집 및 리포트

## Related

- **Script**: `tools/pipeline/pr_gate.sh`
- **Common**: `tools/pipeline/common.sh`
- **Metrics**: `.cascade/metrics.jsonl`
- **Reports**: `.cascade/report.md`
