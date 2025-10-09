# FileFlow 5.0 달성을 위한 개선 계획

## 📊 현재 상태: 3.2/5.0

## 🎯 목표: 5.0/5.0

---

## 🔴 Phase 1: CRITICAL (즉시 조치) - 3.5점 달성

### 1.1 빌드 실패 수정 ⚡ BLOCKING
**우선순위**: P0
**예상 시간**: 30분
**담당**: Backend Dev

**작업 내역**:
- `IdempotencyValidatorTest.java` 컴파일 에러 수정
- `assertThatNoException()` → `assertThatCode().doesNotThrowAnyException()` 변경
- 영향받는 위치: 4곳 (line 74, 181, 389, 411)

**검증**:
```bash
./gradlew clean test
```

**Jira 서브태스크**: KAN-XX-1

---

### 1.2 테스트 커버리지 활성화 및 강화
**우선순위**: P0
**예상 시간**: 2일
**담당**: Backend Dev

**작업 내역**:
- Domain 모듈 단위 테스트 추가
  - `UploadSession` 상태 전이 테스트 완성
  - `UploadPolicy` 검증 로직 테스트
  - Value Object 테스트 강화
- `build.gradle.kts:117` 커버리지 검증 활성화
- 목표: Domain 90%, Application 80%, Adapter 70%

**검증**:
```bash
./gradlew test jacocoTestReport
```

**Jira 서브태스크**: KAN-XX-2

---

### 1.3 통합 테스트 기반 구축
**우선순위**: P1
**예상 시간**: 3일
**담당**: Backend Dev

**작업 내역**:
- Testcontainers 기반 통합 테스트 환경 구축
  - MySQL Testcontainer
  - LocalStack (S3 Mock)
  - Redis Testcontainer
- REST API → Application → Adapter 전체 흐름 테스트
- 주요 시나리오:
  - 업로드 세션 생성 → Presigned URL 발급 → 업로드 확인
  - 정책 검증 실패 시나리오
  - 멱등성 키 기반 중복 요청 처리

**검증**:
```bash
./gradlew :bootstrap:bootstrap-web-api:test
```

**Jira 서브태스크**: KAN-XX-3

---

## 🟡 Phase 2: HIGH (1-2주 내) - 4.0점 달성

### 2.1 UploadSessionService 리팩토링
**우선순위**: P1
**예상 시간**: 1일
**담당**: Backend Dev

**작업 내역**:
- `createSession()` 메서드 분리 (현재 196줄)
  - `validateAndExtractFileInfo()`: 파일 정보 추출 및 검증
  - `checkIdempotency()`: 멱등성 키 기반 중복 확인
  - `createNewSession()`: 신규 세션 생성 로직
  - `generatePresignedUrl()`: URL 발급 (이미 존재)
- 오타 수정: `UploadSessionService:64` 에러 메시지
- Helper 메서드 공통화 (`findSessionById`)

**검증**:
- Cyclomatic Complexity < 10
- Method Length < 50 lines

**Jira 서브태스크**: KAN-XX-4

---

### 2.2 Rate Limiting 실제 구현
**우선순위**: P1
**예상 시간**: 2일
**담당**: Backend Dev

**작업 내역**:
- Redis 기반 분산 Rate Limiter 구현
- Bucket4j + Redis 통합
- RateLimitingService 구현
  - `checkRateLimit(tenantId, userId): boolean`
  - Sliding Window Algorithm
- Interceptor/Filter 레벨 적용
- 모니터링 메트릭 추가

**검증**:
- 부하 테스트: 1000 req/sec
- Redis 장애 시 Fallback 동작 확인

**Jira 서브태스크**: KAN-XX-5

---

### 2.3 동시성 제어 구현
**우선순위**: P1
**예상 시간**: 2일
**담당**: Backend Dev

**작업 내역**:
- 멱등성 키 기반 분산 락 구현
- Redisson 또는 Redis Lua Script 활용
- `IdempotencyValidator` 동시성 처리 강화
- 낙관적 락 vs 비관적 락 전략 결정
- 데드락 방지 로직

**검증**:
- 동시 요청 시나리오 테스트 (100 concurrent requests)
- 동일 멱등성 키로 중복 요청 시 하나만 처리되는지 확인

**Jira 서브태스크**: KAN-XX-6

---

### 2.4 보안 강화 - 예외 처리 전략
**우선순위**: P2
**예상 시간**: 1일
**담당**: Backend Dev

**작업 내역**:
- Custom Exception Hierarchy 정리
  - `FileFlowException` (Base)
  - `PolicyException`, `UploadException`, `SecurityException`
- Generic Error Response 구현
  - 프로덕션: 상세 정보 숨김
  - 개발: 상세 Stack Trace
- GlobalExceptionHandler 강화
- 로그 마스킹 정책
  - PII (Personally Identifiable Information) 마스킹
  - 민감 파라미터 로깅 제외

**검증**:
- 프로덕션 프로필에서 내부 정보 노출 없는지 확인

**Jira 서브태스크**: KAN-XX-7

---

## 🟢 Phase 3: MEDIUM (1개월 내) - 4.5점 달성

### 3.1 성능 모니터링 및 최적화
**우선순위**: P2
**예상 시간**: 2일
**담당**: Backend Dev + DevOps

**작업 내역**:
- Micrometer 커스텀 메트릭 추가
  - 업로드 세션 생성 시간
  - Presigned URL 발급 시간
  - 정책 검증 시간
- Slow Query 모니터링
  - JPA Query Logging 활성화
  - QueryDSL 성능 최적화
- N+1 문제 해결
  - EntityGraph 또는 Fetch Join 적용
- Redis 캐시 Hit Rate 모니터링

**검증**:
- Grafana Dashboard 구축
- P95 latency < 200ms

**Jira 서브태스크**: KAN-XX-8

---

### 3.2 API 문서화 강화
**우선순위**: P2
**예상 시간**: 1일
**담당**: Backend Dev

**작업 내역**:
- SpringDoc OpenAPI 3.0 설정
- API 엔드포인트 상세 문서화
  - Request/Response 예시
  - Error Code 매핑
  - 정책 검증 규칙 문서화
- Postman Collection 생성
- API 테스트 시나리오 문서

**검증**:
- Swagger UI 접근 가능
- 모든 API 문서 완성도 확인

**Jira 서브태스크**: KAN-XX-9

---

### 3.3 대용량 파일 처리 완성
**우선순위**: P2
**예상 시간**: 3일
**담당**: Backend Dev

**작업 내역**:
- Multipart Upload 완전 구현
  - Part Upload 진행률 추적
  - Part 실패 시 재시도 로직
  - Complete Multipart Upload
- 스트리밍 업로드 지원
- 메모리 사용량 최적화
- 파일 청크 크기 정책 설정 (5MB 단위)

**검증**:
- 1GB 파일 업로드 성공
- 메모리 사용량 < 100MB

**Jira 서브태스크**: KAN-XX-10

---

## 🎨 Phase 4: POLISH (2개월 내) - 5.0점 달성

### 4.1 코드 품질 자동화 강화
**우선순위**: P3
**예상 시간**: 1일
**담당**: Backend Dev

**작업 내역**:
- SonarQube 통합
- Mutation Testing (PITest) 도입
- Checkstyle 규칙 강화
- SpotBugs 설정 최적화
- Pre-commit Hook 강화

**검증**:
- Code Coverage > 85%
- SonarQube Quality Gate PASSED

**Jira 서브태스크**: KAN-XX-11

---

### 4.2 아키텍처 문서화
**우선순위**: P3
**예상 시간**: 2일
**담당**: Backend Dev

**작업 내역**:
- Architecture Decision Records (ADR) 작성
  - Hexagonal Architecture 선택 이유
  - NO Lombok 정책 배경
  - AWS S3 Presigned URL 패턴
- C4 Model 다이어그램 작성
  - Context Diagram
  - Container Diagram
  - Component Diagram
- 도메인 모델 시각화 (PlantUML)
- 배포 아키텍처 다이어그램

**Jira 서브태스크**: KAN-XX-12

---

### 4.3 E2E 자동화 테스트
**우선순위**: P3
**예상 시간**: 3일
**담당**: QA + Backend Dev

**작업 내역**:
- REST Assured 기반 E2E 테스트
- 주요 시나리오 자동화
  - Happy Path: 업로드 → 검증 → 완료
  - 실패 시나리오: 정책 위반, 세션 만료
  - 경계값 테스트: 최대 파일 크기, Rate Limit
- CI/CD 파이프라인 통합
- 테스트 리포트 자동 생성

**검증**:
- E2E Test Coverage > 80%

**Jira 서브태스크**: KAN-XX-13

---

## 📊 달성 기준 매트릭스

| Phase | 완료 시 점수 | 핵심 지표 |
|-------|-------------|----------|
| Phase 1 | 3.5/5.0 | 빌드 성공, 테스트 커버리지 70% |
| Phase 2 | 4.0/5.0 | 통합 테스트, Rate Limiting, 동시성 제어 |
| Phase 3 | 4.5/5.0 | 성능 최적화, API 문서화, 대용량 파일 |
| Phase 4 | 5.0/5.0 | 코드 품질 85%+, 아키텍처 문서, E2E 자동화 |

---

## 🎯 최종 5.0 평가 기준

| 항목 | 목표 점수 | 달성 조건 |
|------|----------|----------|
| 아키텍처 설계 | ⭐⭐⭐⭐⭐ | Hexagonal 완벽 구현, ADR 문서화 |
| 코드 품질 | ⭐⭐⭐⭐⭐ | 빌드 성공, 커버리지 85%+, SonarQube Green |
| 테스트 | ⭐⭐⭐⭐⭐ | 단위/통합/E2E 완비, Mutation Testing |
| 보안 | ⭐⭐⭐⭐⭐ | Rate Limiting, 동시성 제어, 보안 스캔 통과 |
| 성능 | ⭐⭐⭐⭐⭐ | P95 < 200ms, 대용량 파일 처리, 모니터링 |
| 문서화 | ⭐⭐⭐⭐⭐ | API 문서, 아키텍처 문서, 운영 가이드 |

---

## 📅 예상 일정

- **Phase 1 (CRITICAL)**: 1주 (5 영업일)
- **Phase 2 (HIGH)**: 2주 (10 영업일)
- **Phase 3 (MEDIUM)**: 3주 (15 영업일)
- **Phase 4 (POLISH)**: 2주 (10 영업일)

**총 예상 기간**: 8주 (약 2개월)

---

## 🚀 시작하기

1. 현재 브랜치에서 main으로 체크아웃
2. 개선 브랜치 생성: `feature/KAN-XX-code-quality-5.0`
3. Jira Epic 생성: "Code Quality 5.0 달성"
4. 각 Phase별 서브태스크 생성
5. Phase 1부터 순차적 작업 시작

---

**작성일**: 2025-10-09
**작성자**: Claude Code Analysis
**버전**: 1.0
