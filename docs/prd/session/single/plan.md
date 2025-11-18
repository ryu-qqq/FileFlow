# Development Plan - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Issue Key**: FILE-001
**작성일**: 2025-11-18
**예상 기간**: 5일
**총 TDD Cycles**: 20 Cycles

---

## 📅 5-Day Development Plan

### Day 1: Domain Layer - VOs (Cycles 1-5)

**목표**: 11개 Value Objects 구현

**오전 (3시간)**:
- [x] Cycle 1: FileId VO (UUID v7)
- [x] Cycle 2: FileName, FileSize, MimeType VO
- [x] Cycle 3: S3Key, S3Bucket, TenantId, UploaderId VO

**오후 (3시간)**:
- [x] Cycle 4: FileCategory VO (UploaderType별 검증)
- [x] Cycle 5: SessionId, PresignedUrl VO
- [x] VO 테스트 100% 통과 확인

**완료 조건**:
- [x] 11개 VO 테스트 통과
- [x] VO 간 의존성 검증 완료

---

### Day 2: Domain Layer - Aggregates (Cycles 6-10)

**목표**: UploadSession, File Aggregates 및 ArchUnit 검증

**오전 (3시간)**:
- [x] Cycle 6: UploadSession Aggregate - 생성 및 만료 체크
- [x] Cycle 7: UploadSession Aggregate - 상태 전환
- [x] Cycle 8: File Aggregate - 생성

**오후 (3시간)**:
- [x] Cycle 9: Domain Exceptions (5개)
- [x] Cycle 10: ArchUnit - Domain Layer 규칙 검증
- [x] Domain Layer 전체 테스트 통과 확인

**완료 조건**:
- [x] UploadSession, File Aggregate 테스트 통과
- [x] Domain Exceptions 테스트 통과
- [x] ArchUnit 테스트 통과 (Lombok 금지, 정적 팩토리 메서드)

---

### Day 3: Application Layer (Cycles 11-14)

**목표**: Orchestration Pattern 적용 (Facade + Manager)

**오전 (3시간)**:
- [x] Cycle 11: GeneratePresignedUrlCommand, Response DTOs
- [x] Cycle 12: SessionManager - prepareSession()
  - 멱등성 체크 구현
  - Transaction 경계 테스트

**오후 (3시간)**:
- [x] Cycle 13: GeneratePresignedUrlFacade - Orchestration
  - S3 호출 트랜잭션 밖 검증
  - InOrder 검증 (SessionManager → S3 → SessionManager)
- [x] Cycle 14: CompleteUploadService
  - 세션 만료 체크
  - File Aggregate 생성

**완료 조건**:
- [x] Orchestration Pattern 구현 완료
- [x] Transaction 경계 검증 통과
- [x] Application Layer Unit Test 100% 통과

---

### Day 4: Persistence Layer (Cycles 15-17)

**목표**: JPA Entities, Flyway, S3ClientAdapter 구현

**오전 (2.5시간)**:
- [x] Cycle 15: JPA Entities 및 Mappers
  - FileJpaEntity
  - UploadSessionJpaEntity
  - Domain ↔ Entity Mapper

**오후 (3.5시간)**:
- [x] Cycle 16: Flyway Migrations
  - V1__create_files_table.sql
  - V2__create_upload_sessions_table.sql
  - Migration 테스트 (TestContainers MySQL)
- [x] Cycle 17: S3ClientAdapter
  - LocalStack 연동
  - Presigned URL 생성 테스트

**완료 조건**:
- [x] JPA Entities 테스트 통과
- [x] Flyway Migration 검증 완료
- [x] S3ClientAdapter Integration Test 통과

---

### Day 5: REST API Layer + E2E Test (Cycles 18-20)

**목표**: API 구현 및 E2E 플로우 검증

**오전 (2.5시간)**:
- [x] Cycle 18: FileApiController - POST /presigned-url
  - Request/Response DTO 매핑
  - TestRestTemplate 테스트
- [x] Cycle 19: FileApiController - POST /upload-complete
  - GlobalExceptionHandler 구현
  - Domain Exception → HTTP Status 매핑

**오후 (3.5시간)**:
- [x] Cycle 20: E2E 플로우 테스트
  - Presigned URL 발급
  - S3 업로드 (LocalStack)
  - 업로드 완료 처리
- [x] 전체 테스트 통과 확인
- [x] ArchUnit 전체 레이어 검증

**완료 조건**:
- [x] POST /presigned-url, POST /upload-complete 구현 완료
- [x] GlobalExceptionHandler 테스트 통과
- [x] E2E 테스트 통과
- [x] 모든 레이어 ArchUnit 테스트 통과

---

## 📊 일일 진행률 추적

| Day | Cycles | Layer | 예상 시간 | 완료 조건 |
|-----|--------|-------|----------|----------|
| **1** | 1-5 | Domain (VOs) | 6h | 11개 VO 테스트 통과 |
| **2** | 6-10 | Domain (Aggregates) | 6h | UploadSession, File, ArchUnit 통과 |
| **3** | 11-14 | Application | 6h | Orchestration Pattern, Transaction 경계 검증 |
| **4** | 15-17 | Persistence | 6h | JPA, Flyway, S3Adapter 통과 |
| **5** | 18-20 | REST API + E2E | 6h | API 구현, E2E 플로우 통과 |

---

## ✅ Daily Checklist

### Day 1 Checklist
- [ ] `/kb/domain/go` → Cycle 1 (FileId)
- [ ] Cycle 1 완료 후 커밋 (`test:`, `feat:`)
- [ ] `/kb/domain/go` → Cycle 2 (FileName, FileSize, MimeType)
- [ ] Cycle 2 완료 후 커밋
- [ ] Cycle 3, 4, 5 동일 패턴 반복
- [ ] 전체 VO 테스트 통과 확인: `./gradlew test --tests *VOTest`

### Day 2 Checklist
- [ ] `/kb/domain/go` → Cycle 6 (UploadSession 생성)
- [ ] Cycle 6 완료 후 커밋
- [ ] Cycle 7-10 동일 패턴 반복
- [ ] ArchUnit 테스트 통과 확인: `./gradlew test --tests *ArchUnit*`

### Day 3 Checklist
- [ ] `/kb/application/go` → Cycle 11 (DTOs)
- [ ] Cycle 11 완료 후 커밋
- [ ] Cycle 12: SessionManager (Transaction 경계 주의!)
- [ ] Cycle 13: Facade (InOrder 검증 필수)
- [ ] Cycle 14: CompleteUploadService
- [ ] Transaction 경계 검증: Pre-commit hook 통과 확인

### Day 4 Checklist
- [ ] `/kb/persistence/go` → Cycle 15 (JPA Entities)
- [ ] Cycle 16: Flyway Migration 작성 및 검증
- [ ] Cycle 17: S3ClientAdapter (LocalStack 설정)
- [ ] Integration Test 통과 확인: `./gradlew integrationTest`

### Day 5 Checklist
- [ ] `/kb/rest-api/go` → Cycle 18 (POST /presigned-url)
- [ ] Cycle 19: POST /upload-complete, GlobalExceptionHandler
- [ ] `/kb/integration/go` → Cycle 20 (E2E Test)
- [ ] 전체 테스트 통과 확인: `./gradlew test integrationTest`
- [ ] ArchUnit 전체 레이어 검증: `./gradlew test --tests *ArchUnit*`

---

## 🚨 위험 관리

### 위험 요소 및 대응 방안

| 위험 요소 | 발생 확률 | 영향도 | 대응 방안 |
|---------|----------|--------|----------|
| **Transaction 경계 위반** | 중간 | 높음 | Pre-commit hook 자동 검증, InOrder 테스트 |
| **S3 LocalStack 설정 실패** | 낮음 | 중간 | Docker Compose 미리 준비, 대체 Mock 사용 |
| **ArchUnit 테스트 실패** | 낮음 | 높음 | Lombok 금지 규칙 사전 숙지, 정적 팩토리 메서드 패턴 준수 |
| **E2E 테스트 타임아웃** | 낮음 | 중간 | TestRestTemplate 타임아웃 설정, LocalStack 안정성 확인 |
| **Flyway Migration 충돌** | 낮음 | 낮음 | Clean DB 상태 유지, TestContainers 사용 |

---

## 📈 성공 기준

### 기능 요구사항
- [x] Presigned URL 발급 API 성공률 100%
- [x] 업로드 완료 API 성공률 100%
- [x] 멱등성 보장 (동일 sessionId로 중복 발급 방지)
- [x] 세션 만료 체크 (5분 초과 시 에러)

### 품질 요구사항
- [x] Unit Test Coverage > 90%
- [x] Integration Test 100% 통과
- [x] E2E Test 100% 통과
- [x] ArchUnit Test 100% 통과

### 성능 요구사항
- [x] Presigned URL 발급 응답 시간 < 200ms (P95)
- [x] 업로드 완료 처리 시간 < 100ms (P95)
- [x] DB Connection Pool 효율성 (외부 API 호출 시 Connection 미점유)

### Zero-Tolerance 준수
- [x] Lombok 금지 (Plain Java)
- [x] Law of Demeter 준수
- [x] Long FK 전략 (JPA 관계 어노테이션 금지)
- [x] Transaction 경계 (외부 API 호출은 트랜잭션 밖)
- [x] Spring 프록시 제약사항 (Private/Final/내부 호출 금지)
- [x] Orchestration Pattern (Facade + Manager)
- [x] Javadoc 필수 (public 메서드)
- [x] Scope 준수 (MVP 범위 초과 금지)

---

## 🔄 다음 단계

### 후속 Bounded Contexts 개발

**Level 2** (session/single 완료 후):
1. **messaging/outbox** (3일) - MessageOutbox Aggregate, SQS 연동
2. **session/multi** (4일) - Multipart Upload (100MB 이상)
3. **session/download** (3일) - 외부 URL 다운로드

**Level 3** (Level 2 완료 후):
1. **file/processing** (3일) - FileProcessingJob, 이미지 가공
2. **validation/checksum** (2일) - Checksum 검증
3. **session/cleanup** (2일) - 만료된 세션 정리

**Level 4** (Level 3 완료 후):
1. **file/retention** (1일) - 파일 만료 관리
2. **security/visibility** (2일) - 접근 제어

---

## 📚 참고 문서

- **Kent Beck TDD**: [Red → Green → Refactor](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)
- **Tidy First**: [Structural vs Behavioral Changes](https://www.oreilly.com/library/view/tidy-first/9781098151232/)
- **Zero-Tolerance Rules**: `/Users/sangwon-ryu/fileflow/.claude/CLAUDE.md`
- **Orchestration Pattern**: `docs/prd/session/single/application.md`

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (5-Day Development Plan)
