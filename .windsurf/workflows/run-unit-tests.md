---
description: 단위 테스트 실행 (Thin Wrapper)
---

# Run Unit Tests

**🎯 역할**: 단위 테스트 실행 및 결과 리포트 (SSOT 래퍼)

**📋 실제 로직**: `tools/pipeline/test_unit.sh` (단일 진실의 원천)

## What It Does

이 워크플로우는 다음을 **자동으로** 실행합니다:

1. ✅ **Unit Tests** - 모든 유닛 테스트 실행
2. ✅ **Parallel Execution** - 병렬 실행으로 속도 최적화
3. ✅ **Test Report** - 실패 시 상세 리포트 제공
4. ✅ **Metrics** - 실행 시간 및 성공률 자동 기록

## Usage

### 기본 실행 (전체 테스트, 병렬)

```bash
./tools/pipeline/test_unit.sh
```

### Impacted-Only 모드 (변경된 패키지만)

```bash
./tools/pipeline/test_unit.sh --impacted
```

### Sequential 모드 (디버깅용)

```bash
./tools/pipeline/test_unit.sh --no-parallel
```

## Parameters

- `--impacted`: 변경된 파일의 패키지만 테스트 (Fast lane)
- `--no-parallel`: 병렬 실행 비활성화

## Output

**성공 시**:
```
✅ All unit tests passed!
Test report: build/reports/tests/test/index.html
```

**실패 시**:
```
❌ Unit tests failed!
Failed tests summary:
  - OrderDomainTest.testConfirmOrder
  - CustomerServiceTest.testCreateCustomer
Check build/reports/tests/test/index.html for details.
```

## Metrics

실행 결과는 자동으로 `.cascade/metrics.jsonl`에 기록됩니다:
- Task name: `test_unit`
- Status code: `0` (성공) / `1` (실패)
- Duration: 초 단위

## Architecture

```
Cascade Workflow (이 파일)
        ↓
   얇은 래퍼 역할
        ↓
tools/pipeline/test_unit.sh (SSOT)
        ↓
    실제 테스트 실행
```

## Fast Lane vs Full Lane

| 모드 | 명령어 | 사용 시점 | 예상 시간 |
|------|--------|----------|----------|
| **Fast Lane** | `--impacted` | 로컬 개발 중 | < 30초 |
| **Full Lane** | (기본) | PR 전, CI | 2-5분 |

## Related

- **Script**: `tools/pipeline/test_unit.sh`
- **Common**: `tools/pipeline/common.sh`
- **Metrics**: `.cascade/metrics.jsonl`
- **Reports**: `build/reports/tests/test/index.html`
