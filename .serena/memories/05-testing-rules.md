# Testing Layer 규칙 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 Testing Layer 핵심 요약본
> **상세 규칙**: Hook이 자동으로 14개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance (절대 금지)

- ❌ **Spring Context 불필요한 로딩**: Domain 테스트에 `@SpringBootTest` 금지
- ❌ **실제 외부 의존성 사용**: DB, API는 Mock 또는 Testcontainers
- ❌ **테스트 간 의존성**: 테스트는 독립적으로 실행 가능해야 함

---

## ✅ 필수 규칙

### 1️⃣ ArchUnit Rules (아키텍처 검증)
- ✅ **Layer Dependency Rules**: Domain → Application → Adapter 의존성 검증
- ✅ **Naming Convention Rules**: UseCase, Port, Adapter 네이밍 규칙
- ✅ **Annotation Rules**: `@Transactional`, `@RestController` 규칙
- ✅ **JPA Entity Rules**: Long FK 전략, Lombok 금지 검증

### 2️⃣ Integration Testing (Testcontainers)
- ✅ **Testcontainers Setup**: Real DB 테스트 환경
- ✅ **API Integration Tests**: `@SpringBootTest` + `MockMvc`
- ✅ **Persistence Tests**: Repository 통합 테스트
- ✅ **Test Data Management**: Fixture + Object Mother
- ✅ **Performance & Benchmark**: 성능 측정

### 3️⃣ Multi-Module Testing
- ✅ **Module Isolation Strategy**: 모듈 간 독립성 유지
- ✅ **Shared Test Fixtures**: 공통 테스트 데이터
- ✅ **Cross-Module Integration**: 모듈 간 통합 테스트
- ✅ **Test Tags & Execution**: `@Tag("integration")`, `@Tag("unit")`

---

## 📊 레이어 통계

- **총 규칙 수**: 14개
- **Cache Rules**: 14개 (Hook 자동 주입)

---

## 🔗 상세 문서 (14개 Cache Rules)

- `02_test-fixture-pattern.md`
- `archunit-rules/01_layer-dependency-rules.md`, `02_naming-convention-rules.md`, `03_annotation-rules.md`, `05_archunit-jpa-entity-rules.md`
- `integration-testing/01_testcontainers-setup.md`, `02_api-integration-tests.md`, `03_persistence-tests.md`, `04_test-data-management.md`, `05_performance-and-benchmark.md`
- `multi-module-testing/01_module-isolation-strategy.md`, `02_shared-test-fixtures.md`, `03_cross-module-integration.md`, `04_test-tags-and-execution.md`

**완전한 규칙은 Hook 시스템이 실시간으로 제공합니다!**
