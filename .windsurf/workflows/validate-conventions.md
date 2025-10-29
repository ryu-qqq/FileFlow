---
description: Zero-Tolerance 코딩 규칙 자동 검증 (Thin Wrapper)
---

# Validate Conventions

**🎯 역할**: Zero-Tolerance 규칙 자동 검증 (SSOT 래퍼)

**📋 실제 로직**: `tools/pipeline/validate_conventions.sh` (단일 진실의 원천)

## What It Does

프로젝트 전체 코드에서 Zero-Tolerance 규칙 위반을 검사합니다:

1. ✅ **Lombok 금지** - `@Data`, `@Builder`, `@Getter`, `@Setter` 등
2. ✅ **Law of Demeter** - Getter 체이닝 (`getA().getB().getC()`)
3. ✅ **JPA 관계 금지** - `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
4. ✅ **Setter 금지** - Domain/Entity에서 `public void setXxx()`
5. ✅ **Transaction 경계** - `@Transactional` 내 외부 API 호출
6. ✅ **Spring 프록시 제약** - private/final 메서드의 `@Transactional`
7. ✅ **Code Format** - Spotless 포맷 규칙

## Usage

### 기본 실행 (로컬)

```bash
./tools/pipeline/validate_conventions.sh
```

### Cascade에서 실행

```
/validate-conventions
```

## Output

**성공 시**:
```
✅ Convention Validation PASSED!
Duration: 5s

All Zero-Tolerance rules are followed.
```

**실패 시**:
```
❌ Convention Validation FAILED
Duration: 8s

Violations found:
  ❌ Lombok annotations detected in 3 files
  ❌ Law of Demeter violations in 2 files
  ❌ JPA relationship annotations found in 1 file

Details: .cascade/report.md
```

## Metrics

실행 결과는 자동으로 `.cascade/metrics.jsonl`에 기록됩니다:
- Task name: `validate_conventions`
- Status code: `0` (성공) / `1` (실패)
- Duration: 초 단위

## Architecture

```
Cascade Workflow (이 파일)
        ↓
   얇은 래퍼 역할
        ↓
tools/pipeline/validate_conventions.sh (SSOT)
        ↓
    Zero-Tolerance 검증 로직
```

## Benefits of SSOT

- ✅ **No Drift**: Cascade와 CI가 동일한 스크립트 사용
- ✅ **Fast Execution**: 최적화된 검색 알고리즘 (grep + find)
- ✅ **Clear Reports**: 위반 사항 상세 리포트 (.cascade/report.md)
- ✅ **Metrics**: 자동 메트릭 수집 및 분석

## Related

- **Script**: `tools/pipeline/validate_conventions.sh`
- **Rules**: `.windsurf/rules-core.md`
- **Detailed Rules**: `docs/coding_convention/`
- **Metrics**: `.cascade/metrics.jsonl`
- **Reports**: `.cascade/report.md`
