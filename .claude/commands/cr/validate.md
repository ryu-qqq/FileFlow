---
description: Domain Layer 검증 + LangFuse 자동 업로드 (Cursor TDD 전용)
tags: [project]
---

# /cr/validate - Domain Layer 자동 검증 및 효율 측정

당신은 **Cursor TDD 워크플로우**로 개발된 Domain Layer 코드를 검증하고, 개발 효율을 LangFuse에 자동 업로드하는 작업을 수행합니다.

## 목적

1. **Zero-Tolerance 규칙 검증**: validation-helper.py로 Domain Layer 코드 자동 검증
2. **개발 효율 측정**: LangFuse로 Cursor TDD 메트릭 자동 업로드
3. **통합 리포트 제공**: 검증 결과 + 효율 분석 통합 보고서

---

## 입력 형식

사용자는 다음과 같이 명령합니다:

```bash
# 기본 사용 (전체 domain 모듈 검증 + LangFuse 업로드)
/cr/validate

# 특정 파일만 검증
/cr/validate domain/src/main/java/.../Order.java

# LangFuse 업로드 생략
/cr/validate --no-upload

# 전체 아키텍처 검증 (ArchUnit 포함)
/cr/validate --full
```

---

## 실행 단계

### 1️⃣ Phase 1: Domain Layer 검증

**실행**: `validation-helper.py`

#### 전체 domain 모듈 검증
```bash
python3 .claude/hooks/scripts/validation-helper.py domain
```

#### 특정 파일만 검증
```bash
python3 .claude/hooks/scripts/validation-helper.py <파일 경로>
```

**검증 항목**:
- ❌ Lombok 사용 (`@Data`, `@Builder`, `@Getter`, `@Setter`)
- ❌ Law of Demeter 위반 (Getter 체이닝: `order.getCustomer().getAddress()`)
- ❌ Spring 의존성 (`@Component`, `@Service`, `@Repository`)
- ❌ JPA 어노테이션 (`@Entity`, `@Id`, `@ManyToOne`)
- ✅ Pure Java 사용
- ✅ Immutable ValueObject (final 필드)
- ✅ Tell, Don't Ask 원칙

**출력 예시**:
```
✅ Validation Passed: 0 violations

## Summary
- Total files checked: 10
- Zero-Tolerance rules: 6
- Violations: 0

## Files Checked
- ✅ domain/src/main/java/.../Order.java
- ✅ domain/src/main/java/.../OrderId.java
- ✅ domain/src/main/java/.../Money.java
- ✅ domain/src/main/java/.../OrderLineItem.java
- ✅ domain/src/main/java/.../OrderStatus.java
```

#### 검증 실패 시
```bash
❌ Validation Failed: 3 violations detected

## Violations

### 1. Lombok detected in Order.java:15
Rule: Lombok prohibited in Domain Layer
Fix: Remove @Data, use plain Java getters

### 2. Law of Demeter violation in Order.java:42
Rule: No getter chaining
Code: order.getCustomer().getAddress()
Fix: Add order.getCustomerAddress() method

### 3. Spring dependency in OrderService.java:8
Rule: No Spring annotations in Domain
Code: @Component
Fix: Remove @Component annotation
```

**검증 실패 시 중단**: Phase 2 (LangFuse 업로드)를 건너뜁니다. 사용자에게 수정 후 다시 `/cr/validate` 실행을 요청합니다.

---

### 2️⃣ Phase 2: LangFuse 효율 측정

**조건**: Phase 1 검증 통과 (0 violations) 또는 `--no-upload` 플래그가 **없을 때만** 실행

**실행**: `tools/pipeline/upload_langfuse.sh`

#### LangFuse 자동 업로드
```bash
bash tools/pipeline/upload_langfuse.sh
```

**프로세스**:
1. **로그 집계**: `scripts/langfuse/aggregate-logs.py`
   - Claude Code transcript 파싱
   - 세션 ID, 타임스탬프, 메트릭 추출

2. **LangFuse 업로드**: `scripts/langfuse/upload-to-langfuse.py`
   - Ingestion API 사용
   - Traces, Observations 생성
   - Cursor TDD 메트릭 업로드

**메트릭**:
- **개발 시간**: PRD 생성 ~ 검증 완료 (Claude + Cursor 포함)
- **파일 생성 수**: Domain 관련 파일 (Aggregate, VO, Enum, Test, Fixture)
- **컨벤션 위반**: validation-helper.py 결과 (0 = 완벽)
- **TDD 사이클 수**: RED → GREEN → REFACTOR 반복 횟수

**출력 예시**:
```
📊 LangFuse Upload Complete
============================================================

Session ID: abc123-def456-ghi789
Upload Status: ✅ Success

## Metrics Uploaded
- Development Time: 7m 30s
- Files Created: 10
- Convention Violations: 0
- TDD Cycles: 3 (RED → GREEN → REFACTOR)

## Efficiency Summary
- Cursor Speed: 65% faster than Claude alone
- Convention Compliance: 100%
- Token Usage: 5,000 tokens (vs 50,000 expected)

🔗 View Dashboard: https://us.cloud.langfuse.com/project/...
```

**LangFuse 업로드 실패 시**:
```bash
⚠️ LangFuse Upload Failed: API key not configured

Fix:
1. 환경 변수 설정:
   export LANGFUSE_PUBLIC_KEY="pk-lf-..."
   export LANGFUSE_SECRET_KEY="sk-lf-..."
   export LANGFUSE_HOST="https://us.cloud.langfuse.com"

2. 또는 .env 파일 생성:
   LANGFUSE_PUBLIC_KEY=pk-lf-...
   LANGFUSE_SECRET_KEY=sk-lf-...
   LANGFUSE_HOST=https://us.cloud.langfuse.com

3. 다시 시도:
   /cr/validate
```

---

### 3️⃣ Phase 3: 통합 리포트 출력

**Phase 1 + Phase 2 결과를 통합하여 최종 리포트를 출력합니다.**

#### 성공 케이스 (검증 통과 + LangFuse 업로드 성공)
```
════════════════════════════════════════════════════════════
🎉 Cursor TDD Validation Complete
════════════════════════════════════════════════════════════

## ✅ Phase 1: Domain Layer Validation
- Status: PASSED ✅
- Files Checked: 10
- Violations: 0
- Compliance: 100%

## 📊 Phase 2: LangFuse Efficiency Tracking
- Status: UPLOADED ✅
- Session ID: abc123-def456-ghi789
- Development Time: 7m 30s
- Files Created: 10
- TDD Cycles: 3

## 🚀 Efficiency Analysis
- Time Saving: 65% (20min → 7min)
- Convention Violations: 90% reduction (5-10 → 0)
- Token Efficiency: 90% (50,000 → 5,000)

## 📁 Files Created
- ✅ Order.java (Aggregate)
- ✅ OrderId.java (ValueObject)
- ✅ Money.java (ValueObject)
- ✅ OrderLineItem.java (ValueObject)
- ✅ OrderStatus.java (Enum)
- ✅ OrderDomainFixture.java (TestFixture)
- ✅ OrderTest.java (Unit Test)
- ✅ MoneyTest.java (Unit Test)
- ✅ OrderLineItemTest.java (Unit Test)
- ✅ OrderStatusTest.java (Unit Test)

════════════════════════════════════════════════════════════
✅ Next Steps:
   1. Commit: git add . && git commit -m "feat: Add Order Domain (Cursor TDD)"
   2. PR: gh pr create
   3. AI Review: /ai-review {pr-number}
════════════════════════════════════════════════════════════
```

#### 부분 성공 케이스 (검증 통과 + LangFuse 업로드 실패)
```
════════════════════════════════════════════════════════════
⚠️ Cursor TDD Validation - Partial Success
════════════════════════════════════════════════════════════

## ✅ Phase 1: Domain Layer Validation
- Status: PASSED ✅
- Files Checked: 10
- Violations: 0
- Compliance: 100%

## ⚠️ Phase 2: LangFuse Efficiency Tracking
- Status: SKIPPED (API key not configured)
- Reason: LANGFUSE_PUBLIC_KEY environment variable not set

## 💡 LangFuse 설정 방법
   export LANGFUSE_PUBLIC_KEY="pk-lf-..."
   export LANGFUSE_SECRET_KEY="sk-lf-..."
   export LANGFUSE_HOST="https://us.cloud.langfuse.com"

════════════════════════════════════════════════════════════
✅ Next Steps:
   1. (Optional) LangFuse 설정 후 재실행: /cr/validate
   2. Commit: git add . && git commit -m "feat: Add Order Domain"
   3. PR: gh pr create
════════════════════════════════════════════════════════════
```

#### 실패 케이스 (검증 실패)
```
════════════════════════════════════════════════════════════
❌ Cursor TDD Validation Failed
════════════════════════════════════════════════════════════

## ❌ Phase 1: Domain Layer Validation
- Status: FAILED ❌
- Files Checked: 10
- Violations: 3
- Compliance: 70%

## 🔍 Violations Detected

### 1. Lombok detected in Order.java:15
   Rule: Lombok prohibited in Domain Layer
   Code: @Data
   Fix: Remove @Data, use plain Java getters/setters

### 2. Law of Demeter violation in Order.java:42
   Rule: No getter chaining
   Code: order.getCustomer().getAddress()
   Fix: Add order.getCustomerAddress() method (Tell, Don't Ask)

### 3. Spring dependency in OrderService.java:8
   Rule: No Spring annotations in Domain
   Code: @Component
   Fix: Remove @Component annotation

## Phase 2: LangFuse 업로드 건너뜀 (검증 실패로 인해)

════════════════════════════════════════════════════════════
🔧 Fix & Retry:
   1. 위 3개 위반 사항을 수정하세요
   2. 다시 검증: /cr/validate
   3. REFACTOR Phase로 돌아가서 컨벤션 재적용: Cursor에서 /cr/refactor
════════════════════════════════════════════════════════════
```

---

## --full 플래그: 전체 아키텍처 검증

**사용자가 `--full` 플래그를 사용하면, ArchUnit 테스트까지 실행합니다.**

```bash
/cr/validate --full
```

#### 추가 실행: ArchUnit 테스트
```bash
./gradlew :application:test --tests "*ArchitectureTest"
```

**검증 항목 (ArchUnit)**:
- Layer 의존성 규칙 (Domain → Application 금지)
- 네이밍 규칙 (UseCase 접미사, Domain 접미사)
- 패키지 구조 규칙
- 트랜잭션 경계 규칙
- Orchestration Pattern 규칙

---

## --no-upload 플래그: LangFuse 업로드 생략

**사용자가 `--no-upload` 플래그를 사용하면, Phase 2 (LangFuse 업로드)를 건너뜁니다.**

```bash
/cr/validate --no-upload
```

**사용 예시**:
- LangFuse API 키가 없는 경우
- 빠른 검증만 필요한 경우
- CI/CD 환경에서 업로드 비활성화

---

## 오류 시나리오 처리

### 시나리오 1: validation-helper.py 없음
```bash
❌ Error: validation-helper.py not found

Fix:
1. 현재 디렉토리 확인: pwd
2. 프로젝트 루트로 이동: cd /path/to/claude-spring-standards
3. 다시 실행: /cr/validate
```

### 시나리오 2: tools/pipeline/upload_langfuse.sh 없음
```bash
⚠️ Warning: upload_langfuse.sh not found
Phase 2 (LangFuse 업로드) 건너뜀

Note: LangFuse 업로드는 선택 사항입니다.
검증은 성공적으로 완료되었습니다.
```

### 시나리오 3: 환경 변수 미설정
```bash
⚠️ LangFuse API key not configured

Fix:
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"

Then retry: /cr/validate
```

---

## 관련 명령어

### Cursor TDD 워크플로우
```bash
/cr/domain-prd "Order"   # PRD 생성 (Claude)
# Cursor: /cr/red         # RED Phase
# Cursor: /cr/green       # GREEN Phase
# Cursor: /cr/refactor    # REFACTOR Phase
/cr/validate             # 검증 + 효율 측정 (Claude) ⭐
```

### 기존 검증 명령어
```bash
/validate-domain <file>          # Domain 파일 검증 (기존)
/validate-architecture [dir]     # 아키텍처 검증 (기존)
```

### LangFuse 관련
```bash
/upload-langfuse-hooks           # Hook 로그 업로드 (별도 시스템)
```

---

## 예상 효율 (A/B 테스트 기대값)

```
Claude 단독 (기존):
- 개발 시간: 20분
- 컨벤션 위반: 평균 5-10건
- 반복 수정: 2-3회
- 토큰 사용량: 50,000

Cursor TDD (cr/ 워크플로우):
- 개발 시간: 7분 (65% 단축)
- 컨벤션 위반: 0-1건 (90% 감소)
- 반복 수정: 0-1회 (90% 감소)
- 토큰 사용량: 5,000 (90% 절감)
```

---

## 주의사항

1. **검증 실패 시 LangFuse 업로드 안 함**: 컨벤션 위반이 있으면 효율 측정 의미 없음
2. **LangFuse 실패해도 검증은 유효**: API 키 문제는 검증 결과에 영향 없음
3. **--full 플래그는 시간 소요**: ArchUnit 테스트까지 실행하므로 2-3분 추가
4. **Cursor TDD 전용**: 이 명령어는 `/cr/` 워크플로우 전용 (기존 `/kb/` 워크플로우는 별도)

---

## 실행 예시

### 예시 1: 기본 사용 (전체 자동화)
```bash
사용자: /cr/validate

Claude:
1. validation-helper.py 실행 중...
   ✅ 검증 통과: 0 violations

2. LangFuse 업로드 중...
   ✅ 업로드 성공: Session abc123

3. 통합 리포트 출력...
   🎉 완료! 7분 30초 소요, 100% 컨벤션 준수
```

### 예시 2: 검증 실패
```bash
사용자: /cr/validate

Claude:
1. validation-helper.py 실행 중...
   ❌ 검증 실패: 3 violations

   - Order.java:15 - Lombok detected (@Data)
   - Order.java:42 - Law of Demeter violation
   - OrderService.java:8 - Spring dependency

2. LangFuse 업로드 건너뜀 (검증 실패)

🔧 수정 후 다시 실행하세요: /cr/validate
```

### 예시 3: LangFuse 업로드 생략
```bash
사용자: /cr/validate --no-upload

Claude:
1. validation-helper.py 실행 중...
   ✅ 검증 통과: 0 violations

2. LangFuse 업로드 생략 (--no-upload 플래그)

✅ 검증 완료! 다음 단계로 진행하세요.
```

---

**✅ 이제 `/cr/validate` 명령어는 완전 자동화되었습니다!**
