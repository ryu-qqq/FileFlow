# Cursor TDD Commands (cr/)

**목적**: Cursor IDE에서 Kent Beck TDD로 Domain Layer를 빠르게 개발하기 위한 명령어 세트

---

## 🎯 설계 철학

### 왜 별도 패키지인가?
- **기존 `/kb/` 유지**: Claude Code 기반 TDD 워크플로우는 그대로
- **Cursor 최적화**: Cursor IDE의 빠른 코드 생성 능력 활용
- **Domain Layer 집중**: 의존성 적고 테스트 빠른 Domain Layer만
- **효율 측정**: LangFuse 자동 업로드로 생산성 추적

---

## 📋 워크플로우

```bash
# Phase 1: Claude Code - Domain PRD 생성
/cr/domain-prd "Order Management"
→ docs/prd/domain/order-domain-prd.md 생성

# Phase 2: Cursor IDE - TDD 사이클 (반복)
# (Cursor Composer에서 자동 실행)
/cr/red    # RED: 실패하는 테스트 작성
/cr/green  # GREEN: 최소 구현
/cr/refactor  # REFACTOR: 컨벤션 적용

# Phase 3: Claude Code - 검증 + 효율 측정
/cr/validate
→ validation-helper.py 실행
→ LangFuse 자동 업로드
```

---

## 🗂️ 명령어 목록

### 1. `/cr/domain-prd` (Claude Code)
**목적**: Domain Layer PRD 생성
**실행 환경**: Claude Code
**출력**: `docs/prd/domain/{name}-domain-prd.md`

**템플릿**:
- Aggregate Root 정의
- ValueObject 목록
- Business Rules
- TDD Plan

---

### 2. `/cr/red` (Cursor IDE)
**목적**: RED Phase - 실패하는 테스트 작성
**실행 환경**: Cursor Composer
**전제 조건**: Domain PRD 존재

**생성 파일**:
- `{Aggregate}Test.java`
- `{Aggregate}DomainFixture.java`

**컨벤션**:
- TestFixture Pattern 필수
- Given-When-Then 구조
- 비즈니스 규칙 명확히 표현

---

### 3. `/cr/green` (Cursor IDE)
**목적**: GREEN Phase - 최소 구현으로 테스트 통과
**실행 환경**: Cursor Composer
**전제 조건**: RED Phase 완료 (테스트 실패 중)

**생성 파일**:
- `{Aggregate}.java`
- `{ValueObject}.java`
- `{Enum}.java`

**원칙**:
- 테스트 통과만 목표
- 하드코딩 허용
- 빠르게 진행

---

### 4. `/cr/refactor` (Cursor IDE)
**목적**: REFACTOR Phase - 컨벤션 적용 및 개선
**실행 환경**: Cursor Composer
**전제 조건**: GREEN Phase 완료 (테스트 통과)

**적용 규칙**:
- ❌ Lombok 제거
- ✅ Law of Demeter 적용
- ✅ ValueObject 패턴
- ✅ Tell, Don't Ask

---

### 5. `/cr/validate` (Claude Code)
**목적**: 검증 + LangFuse 효율 측정
**실행 환경**: Claude Code
**전제 조건**: REFACTOR Phase 완료

**작업**:
1. `validation-helper.py` 실행
   - Domain Layer 규칙 검증
   - 위반 사항 보고

2. LangFuse 자동 업로드
   - 세션 ID 추출
   - 로그 집계 및 업로드
   - 효율 메트릭 측정

**출력**:
```
✅ Validation Passed: 0 violations
📊 LangFuse Uploaded: session-123
⏱️ Time: 5m 30s
📝 Files: 10 files created
```

---

## 🔄 TDD 사이클 예시

### 예시: Order Aggregate 개발

```bash
# 1. Claude Code: PRD 생성 (2분)
/cr/domain-prd "Order"
→ docs/prd/domain/order-domain-prd.md

# 2. Cursor Composer: RED (1분)
"docs/prd/domain/order-domain-prd.md를 읽고 RED Phase 실행"
→ OrderTest.java, OrderDomainFixture.java

# 3. Cursor Composer: GREEN (1분)
"GREEN Phase 실행"
→ Order.java, OrderId.java, OrderStatus.java

# 4. Cursor Composer: REFACTOR (2분)
"REFACTOR Phase 실행. .cursorrules 컨벤션 적용"
→ Lombok 제거, Law of Demeter 적용

# 5. Claude Code: 검증 (30초)
/cr/validate
→ ✅ 0 violations, LangFuse 업로드 완료

총 시간: ~7분 (vs Claude 단독: ~20분, 65% 단축)
```

---

## 📊 효율 측정 (LangFuse)

### 자동 추적 메트릭
- **개발 시간**: PRD 생성 ~ 검증 완료
- **파일 생성 수**: Domain 관련 파일
- **컨벤션 위반**: validation-helper.py 결과
- **TDD 사이클 수**: RED→GREEN→REFACTOR 반복 횟수

### LangFuse Dashboard
- Session별 효율 비교
- Cursor vs Claude 생산성
- 컨벤션 준수율 추이

---

## 🚀 사용 예시

### 1. Order Domain 개발
```bash
# Claude Code
/cr/domain-prd "Order Management"

# Cursor Composer
"order-domain-prd.md 기반 TDD 실행"
→ /cr/red → /cr/green → /cr/refactor

# Claude Code
/cr/validate
```

### 2. Payment Domain 개발
```bash
# Claude Code
/cr/domain-prd "Payment Processing"

# Cursor Composer
"payment-domain-prd.md 기반 TDD 실행"
→ TDD 사이클 자동 반복

# Claude Code
/cr/validate
```

---

## ⚠️ 제약사항

### 적용 가능
- ✅ Domain Layer (Aggregate, ValueObject, Enum)
- ✅ Pure Java 비즈니스 로직
- ✅ 단위 테스트

### 적용 불가능
- ❌ Application Layer (UseCase, Transaction)
- ❌ Persistence Layer (JPA Entity, Repository)
- ❌ Adapter Layer (Controller, REST API)

→ 이들은 기존 `/kb/` 또는 `/code-gen-*` 사용

---

## 🔗 관련 문서

- [Kent Beck TDD 가이드](.cursorrules)
- [Domain Layer 코딩 컨벤션](../../docs/coding_convention/02-domain-layer/)
- [TestFixture Pattern](../../docs/coding_convention/05-testing/02_test-fixture-pattern.md)
- [LangFuse 통합 가이드](../../docs/LANGFUSE_USAGE_GUIDE.md)

---

**✅ 이 패키지는 Domain Layer 개발 속도를 65% 향상시킵니다.**
