---
description: Intelligent Test Runner (변경 감지 → 자동 테스트 실행 → 실패 수정 제안)
---

# Intelligent Test Runner

**🎯 역할**: 변경 감지 기반 스마트 테스트 실행 및 자동 수정

**📋 통합**: Claude Code `/test-gen-*` + Git Diff + LangFuse

---

## 🚀 핵심 기능

### 1. Smart Execution (변경 감지)
- Git diff 분석 → 영향받는 Layer 파악
- 변경된 Layer의 테스트만 선택적 실행
- 30초 Fast Lane vs 5분 Full Lane

### 2. Auto-Fix (실패 수정)
- 테스트 실패 감지 → Claude Code 자동 수정 제안
- Serena Memory 학습 → 다음엔 예방

### 3. Metrics Tracking (LangFuse)
- 테스트 실행 시간, 성공률 자동 수집
- A/B 테스트: Fast Lane vs Full Lane 효율 비교

---

## 📚 사용법

### 기본 실행

```bash
# 변경된 Layer만 테스트 (Fast Lane)
/test-runner --smart

# 전체 테스트 (Full Lane)
/test-runner --full

# 특정 Layer만
/test-runner --layer domain
/test-runner --layer application
/test-runner --layer persistence
```

### 실패 시 자동 수정

```bash
# 실패 감지 → 자동 수정 제안
/test-runner --smart --fix-failures

# 워크플로우:
1. 테스트 실행
2. 실패 감지
3. Claude Code 분석
4. 수정 제안 제시
5. 사용자 승인 시 자동 적용
```

---

## 🧠 Smart Execution 로직

### 1. Git Diff 분석

```bash
# 변경된 파일 목록
git diff --name-only HEAD~1

# 예시 출력:
domain/src/.../OrderDomain.java
application/src/.../CreateOrderUseCase.java
```

### 2. Layer 매핑

```yaml
domain/:
  - /test-gen-domain Order
  - Run: ./gradlew :domain:test

application/:
  - /test-gen-usecase CreateOrder
  - Run: ./gradlew :application:test

adapter-in/rest-api/:
  - Run: ./gradlew :adapter-in-rest:test

adapter-out/persistence-mysql/:
  - /test-gen-repository-unit OrderRepository
  - /test-gen-repository-integration OrderRepository
  - Run: ./gradlew :adapter-out-persistence:test
```

### 3. 테스트 실행 전략

#### Fast Lane (< 30초)
```bash
# 변경된 Layer만
if [ "domain" in changed_layers ]; then
  ./gradlew :domain:test --tests "*Order*"
fi

# 병렬 실행
./gradlew :domain:test :application:test --parallel
```

#### Full Lane (2-5분)
```bash
# 전체 테스트 (PR Gate)
./gradlew test integrationTest
```

---

## 🛠️ Layer별 테스트 전략

### Domain Layer

**변경 감지**:
```bash
domain/src/main/java/com/ryuqq/domain/order/OrderDomain.java
```

**자동 실행**:
```bash
# 1. 테스트 존재 확인
if [ ! -f "domain/src/test/.../OrderDomainTest.java" ]; then
  # 없으면 자동 생성
  /test-gen-domain Order
fi

# 2. 테스트 실행
./gradlew :domain:test --tests "*OrderDomain*"
```

**실패 시**:
```bash
❌ OrderDomainTest.testConfirmOrder failed

✨ Claude Code Analysis:
- Law of Demeter 위반: order.getCustomer().getAddress()
- 제안: order.getCustomerAddress() 메서드 추가

Apply fix? [Y/n]
```

### Application Layer

**변경 감지**:
```bash
application/src/main/java/.../CreateOrderUseCase.java
```

**자동 실행**:
```bash
# 1. UseCase 테스트 생성/실행
if [ ! -f "application/src/test/.../CreateOrderUseCaseTest.java" ]; then
  /test-gen-usecase CreateOrder
fi

# 2. Transaction 경계 검증
./gradlew :application:test --tests "*CreateOrderUseCase*"
```

**실패 시**:
```bash
❌ CreateOrderUseCaseTest.testTransactionBoundary failed

✨ Claude Code Analysis:
- @Transactional 내 외부 API 호출 발견
- 제안: executeInTransaction() 분리

Apply fix? [Y/n]
```

### Persistence Layer

**변경 감지**:
```bash
adapter-out/persistence-mysql/src/.../OrderRepositoryImpl.java
```

**자동 실행**:
```bash
# 1. Unit Test (Mock) - Fast
if [ ! -f ".../OrderRepositoryUnitTest.java" ]; then
  /test-gen-repository-unit OrderRepository
fi
./gradlew :adapter-out-persistence:test --tests "*OrderRepositoryUnit*"

# 2. Integration Test (Testcontainers) - PR only
if [ "$CI" = "true" ]; then
  if [ ! -f ".../OrderRepositoryIntegrationTest.java" ]; then
    /test-gen-repository-integration OrderRepository
  fi
  ./gradlew :adapter-out-persistence:integrationTest
fi
```

**실패 시**:
```bash
❌ N+1 query detected in OrderRepository.findAllWithCustomer

✨ Claude Code Analysis:
- Fetch Join 누락
- 제안: @Query("... JOIN FETCH o.customer ...") 추가

Apply fix? [Y/n]
```

### REST API Layer

**변경 감지**:
```bash
adapter-in/rest-api/src/.../OrderController.java
```

**자동 실행**:
```bash
# Controller 테스트
./gradlew :adapter-in-rest:test --tests "*OrderController*"

# E2E 테스트 (선택적)
if [ "$RUN_E2E" = "true" ]; then
  /test-gen-e2e OrderAPI
  ./gradlew :adapter-in-rest:e2eTest
fi
```

---

## 📊 Metrics & Analytics

### LangFuse 자동 수집

```jsonl
# .pipeline-metrics/metrics.jsonl
{
  "task": "test_runner_smart",
  "status": 0,
  "duration_ms": 15234,
  "layers_tested": ["domain", "application"],
  "tests_run": 42,
  "tests_passed": 40,
  "tests_failed": 2,
  "auto_fixes_suggested": 2,
  "auto_fixes_applied": 1,
  "timestamp": "2025-01-30T10:30:00Z"
}
```

### A/B Test 비교

| Metric | Fast Lane | Full Lane | 개선율 |
|--------|-----------|-----------|--------|
| 실행 시간 | 15초 | 180초 | 92% ↓ |
| 테스트 개수 | 42 | 177 | 76% ↓ |
| 실패 조기 감지 | ✅ | ✅ | 동일 |
| 피드백 속도 | 즉시 | 3분 후 | 1200% ↑ |

---

## 🔧 Configuration

### `.test-runner.yaml` (선택)

```yaml
# Test Runner 설정
smart_mode:
  enabled: true
  git_diff_base: HEAD~1

auto_fix:
  enabled: true
  auto_apply: false  # 항상 확인 후 적용

layers:
  domain:
    test_pattern: "*Domain*Test"
    auto_generate: true

  application:
    test_pattern: "*UseCase*Test"
    auto_generate: true

  persistence:
    unit_test: true
    integration_test_on_ci: true

metrics:
  langfuse_upload: true
  local_report: .pipeline-metrics/test-metrics.jsonl
```

---

## 🎯 실행 예시

### 시나리오 1: Domain 변경 (로컬 개발)

```bash
# 1. OrderDomain.java 수정
vim domain/src/.../OrderDomain.java

# 2. Smart Test Runner 실행
/test-runner --smart

# 출력:
🔍 Analyzing changes...
   - domain/src/.../OrderDomain.java (modified)

📦 Layer: domain
   - Checking test: OrderDomainTest.java
   - Test exists ✓

🧪 Running domain tests...
   ⏱️  15s

✅ All tests passed (42 tests)

📊 Metrics uploaded to LangFuse
```

### 시나리오 2: Application 변경 + 실패 (자동 수정)

```bash
# 1. CreateOrderUseCase.java 수정
vim application/src/.../CreateOrderUseCase.java

# 2. Smart Test Runner (자동 수정 모드)
/test-runner --smart --fix-failures

# 출력:
🔍 Analyzing changes...
   - application/src/.../CreateOrderUseCase.java (modified)

📦 Layer: application
   - Checking test: CreateOrderUseCaseTest.java
   - Test exists ✓

🧪 Running application tests...
   ⏱️  8s

❌ Test failed: testTransactionBoundary
   - @Transactional 내 외부 API 호출 발견
   - Location: CreateOrderUseCase.java:42

✨ Claude Code Auto-Fix:
   1. executeInTransaction() 메서드 분리
   2. 외부 API 호출을 트랜잭션 밖으로 이동

Apply fix? [Y/n] Y

✅ Fix applied
🧪 Re-running tests...
   ⏱️  8s

✅ All tests passed (38 tests)

📝 Serena Memory: 패턴 저장 (다음엔 자동 예방)
📊 Metrics uploaded to LangFuse
```

### 시나리오 3: PR 전체 검증 (CI)

```bash
# PR Gate에서 자동 실행
/test-runner --full

# 출력:
🔍 Full Lane Mode (PR Gate)

📦 Running all tests...
   ✅ Domain: 120 tests (1m 15s)
   ✅ Application: 42 tests (30s)
   ✅ Persistence (Unit): 35 tests (20s)
   ✅ Persistence (Integration): 28 tests (1m 30s)
   ✅ REST API: 52 tests (45s)

✅ All tests passed (277 tests)
⏱️  Total: 4m 20s

📊 Coverage: 87%
📊 Metrics uploaded to LangFuse
```

---

## 🔗 Integration

### Claude Code Commands

```bash
# Test Runner는 다음 명령어들을 내부적으로 사용
/test-gen-domain <name>
/test-gen-usecase <name>
/test-gen-repository-unit <name>
/test-gen-repository-integration <name>
/test-gen-e2e <name>
```

### Pipeline Integration

```bash
# PR Gate 파이프라인
./tools/pipeline/pr_gate.sh
  ↓
1. Smart Test Runner (변경된 Layer만)
2. 실패 시 Full Lane으로 전환
3. LangFuse 메트릭 업로드
```

---

## 📚 Benefits

### 1. 빠른 피드백 (92% 시간 절감)
- 변경된 Layer만 테스트
- 로컬 개발 시 15-30초 안에 결과

### 2. 자동 수정 제안
- 실패 패턴 분석 → 수정 방법 제시
- Serena Memory 학습 → 재발 방지

### 3. 효율 측정
- LangFuse로 메트릭 추적
- Fast Lane vs Full Lane A/B 테스트

### 4. 테스트 커버리지 향상
- 누락된 테스트 자동 생성
- Layer별 테스트 전략 자동 적용

---

## 🚀 Quick Start

```bash
# 1. 코드 변경
vim domain/src/.../OrderDomain.java

# 2. Smart Test Runner
/test-runner --smart

# 3. 실패 시 자동 수정
/test-runner --smart --fix-failures

# 4. PR 전 전체 검증
/test-runner --full
```

**💡 핵심**: 변경 감지 → 스마트 실행 → 자동 수정 → 효율 측정!
