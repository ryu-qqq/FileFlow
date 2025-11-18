# FILE-006-005: Integration Test TDD Plan

**Task**: FILE-006-005 (Integration Test 구현)
**Layer**: Integration Test
**브랜치**: feature/FILE-006-005-integration
**예상 소요 시간**: 255분 (17 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: TestContainers 환경 설정 (2 사이클)

#### 1️⃣ TestContainers Config 구현 (Cycle 1)

**🔴 Red: 테스트 작성**
- [ ] `TestContainersConfigTest.java` 생성
- [ ] `shouldStartMySQLContainer()` 테스트 작성
- [ ] `shouldStartRedisContainer()` 테스트 작성
- [ ] `shouldStartLocalStackContainer()` 테스트 작성
- [ ] 커밋: `test: TestContainersConfig 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `TestContainersConfig.java` 생성 (@TestConfiguration)
- [ ] MySQL Container Bean 정의 (@ServiceConnection)
- [ ] Redis Container Bean 정의 (Keyspace Notification 활성화)
- [ ] LocalStack Container Bean 정의 (S3 서비스)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TestContainersConfig 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Container 설정 최적화
- [ ] 커밋: `refactor: TestContainersConfig 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `IntegrationTestBase.java` 생성 (Base Class)
- [ ] 커밋: `test: IntegrationTestBase 정리 (Tidy)`

---

#### 2️⃣ LocalStack S3 설정 구현 (Cycle 2)

**🔴 Red: 테스트 작성**
- [ ] `LocalStackS3ConfigTest.java` 생성
- [ ] `shouldCreateS3Bucket()` 테스트 작성
- [ ] `shouldGeneratePresignedUrl()` 테스트 작성
- [ ] 커밋: `test: LocalStack S3 설정 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `@BeforeEach setupS3()` 메서드 구현
- [ ] S3Client 생성 (LocalStack Endpoint)
- [ ] Bucket 생성 (fileflow-test)
- [ ] 커밋: `impl: LocalStack S3 설정 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] S3 설정 헬퍼 메서드 추출
- [ ] 커밋: `refactor: LocalStack S3 설정 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `LocalStackS3Fixture.java` 생성
- [ ] 커밋: `test: LocalStack S3 Fixture 정리 (Tidy)`

---

### Phase 2: E2E 시나리오 테스트 (6 사이클)

#### 3️⃣ 단일 파일 업로드 E2E (Part 1: Presigned URL 요청) (Cycle 3)

**🔴 Red: 테스트 작성**
- [ ] `SingleFileUploadIntegrationTest.java` 생성
- [ ] `@SpringBootTest(webEnvironment = RANDOM_PORT)` 설정
- [ ] `@Testcontainers` 설정
- [ ] JWT 토큰 생성 로직 작성
- [ ] Presigned URL 요청 테스트 작성
- [ ] 커밋: `test: 단일 파일 업로드 E2E (Presigned URL) 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] TestRestTemplate 사용
- [ ] PrepareUploadRequest DTO 생성
- [ ] POST /api/v1/upload-sessions 호출
- [ ] PrepareUploadResponse 검증
- [ ] 커밋: `impl: 단일 파일 업로드 E2E (Presigned URL) 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] JWT 토큰 생성 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 단일 파일 업로드 E2E (Presigned URL) 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `JwtTokenFixture.java` 생성
- [ ] 커밋: `test: JWT Token Fixture 정리 (Tidy)`

---

#### 4️⃣ 단일 파일 업로드 E2E (Part 2: S3 업로드) (Cycle 4)

**🔴 Red: 테스트 작성**
- [ ] S3 직접 업로드 테스트 작성
- [ ] PUT 요청으로 파일 전송
- [ ] 커밋: `test: 단일 파일 업로드 E2E (S3 업로드) 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] byte[] 파일 콘텐츠 생성
- [ ] HttpHeaders.setContentType(IMAGE_JPEG) 설정
- [ ] RestTemplate.exchange() 호출 (PUT)
- [ ] 200 OK 응답 검증
- [ ] 커밋: `impl: 단일 파일 업로드 E2E (S3 업로드) 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] S3 업로드 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 단일 파일 업로드 E2E (S3 업로드) 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `S3UploadFixture.java` 생성
- [ ] 커밋: `test: S3 Upload Fixture 정리 (Tidy)`

---

#### 5️⃣ 단일 파일 업로드 E2E (Part 3: 완료/조회/삭제) (Cycle 5)

**🔴 Red: 테스트 작성**
- [ ] POST /complete 테스트 작성
- [ ] GET /files/{fileId} 테스트 작성
- [ ] GET /files (목록 조회) 테스트 작성
- [ ] DELETE /files/{fileId} 테스트 작성
- [ ] 삭제 후 404 확인 테스트 작성
- [ ] 커밋: `test: 단일 파일 업로드 E2E (완료/조회/삭제) 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] POST /complete 호출 → FileResponse 검증
- [ ] GET /files/{fileId} 호출 → FileDetailResponse 검증
- [ ] GET /files 호출 → PageResponse 검증
- [ ] DELETE /files/{fileId} 호출 → 204 No Content 검증
- [ ] 삭제 후 GET → 404 Not Found 검증
- [ ] 커밋: `impl: 단일 파일 업로드 E2E (완료/조회/삭제) 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 완료/조회/삭제 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 단일 파일 업로드 E2E (완료/조회/삭제) 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `FileOperationFixture.java` 생성
- [ ] 커밋: `test: File Operation Fixture 정리 (Tidy)`

---

#### 6️⃣ 멀티파트 파일 업로드 E2E (Part 1: Presigned URL 요청) (Cycle 6)

**🔴 Red: 테스트 작성**
- [ ] `MultipartFileUploadIntegrationTest.java` 생성
- [ ] MULTIPART 타입 Presigned URL 요청 테스트 작성
- [ ] 10개 PartUploadUrl 검증
- [ ] 커밋: `test: 멀티파트 업로드 E2E (Presigned URL) 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] PrepareUploadRequest (UploadType.MULTIPART, 50MB)
- [ ] POST /api/v1/upload-sessions 호출
- [ ] partUploadUrls.size() == 10 검증
- [ ] 커밋: `impl: 멀티파트 업로드 E2E (Presigned URL) 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] MULTIPART 요청 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 멀티파트 업로드 E2E (Presigned URL) 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `MultipartRequestFixture.java` 생성
- [ ] 커밋: `test: Multipart Request Fixture 정리 (Tidy)`

---

#### 7️⃣ 멀티파트 파일 업로드 E2E (Part 2: Part 업로드 및 완료) (Cycle 7)

**🔴 Red: 테스트 작성**
- [ ] 10개 Part 업로드 테스트 작성 (각 5MB)
- [ ] POST /complete 테스트 작성
- [ ] UploadType.MULTIPART 검증
- [ ] 커밋: `test: 멀티파트 업로드 E2E (Part 업로드) 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] for 루프로 10개 Part 업로드
- [ ] 각 Part마다 PUT 요청 (5MB byte[])
- [ ] POST /complete 호출
- [ ] FileResponse.uploadType() == MULTIPART 검증
- [ ] 커밋: `impl: 멀티파트 업로드 E2E (Part 업로드) 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Part 업로드 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 멀티파트 업로드 E2E (Part 업로드) 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `MultipartUploadFixture.java` 생성
- [ ] 커밋: `test: Multipart Upload Fixture 정리 (Tidy)`

---

#### 8️⃣ 세션 만료 E2E (Cycle 8)

**🔴 Red: 테스트 작성**
- [ ] `SessionExpirationIntegrationTest.java` 생성
- [ ] Presigned URL 요청 후 TTL 만료 테스트 작성
- [ ] POST /complete → 410 Gone 검증
- [ ] errorCode == "SESSION_EXPIRED" 검증
- [ ] 커밋: `test: 세션 만료 E2E 테스트 추가 (Red)`

**⚠️ TTL 테스트 전략**:
- 실제 15분 대기 불가 → Mock Clock 또는 짧은 TTL 설정 사용

**🟢 Green: 최소 구현**
- [ ] Presigned URL 요청
- [ ] Thread.sleep() 또는 Mock Clock 사용
- [ ] POST /complete 호출
- [ ] 410 Gone 응답 검증
- [ ] 커밋: `impl: 세션 만료 E2E 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] TTL Mock 전략 개선
- [ ] 커밋: `refactor: 세션 만료 E2E 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `SessionExpirationFixture.java` 생성
- [ ] 커밋: `test: Session Expiration Fixture 정리 (Tidy)`

---

### Phase 3: 동시성 테스트 (2 사이클)

#### 9️⃣ 동일 sessionId 동시 요청 (멱등성) (Cycle 9)

**🔴 Red: 테스트 작성**
- [ ] `IdempotencyIntegrationTest.java` 생성
- [ ] ExecutorService 2개 스레드 설정
- [ ] 동일 sessionId로 동시 요청 테스트 작성
- [ ] 동일 세션/파일 ID 반환 검증
- [ ] 커밋: `test: 멱등성 동시성 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] ExecutorService.newFixedThreadPool(2) 생성
- [ ] CountDownLatch(2) 설정
- [ ] CopyOnWriteArrayList로 응답 수집
- [ ] 동일 sessionId, fileId 검증
- [ ] 커밋: `impl: 멱등성 동시성 테스트 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 동시성 테스트 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 멱등성 동시성 테스트 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `ConcurrencyFixture.java` 생성
- [ ] 커밋: `test: Concurrency Fixture 정리 (Tidy)`

---

#### 🔟 동일 파일 동시 완료 요청 (Optimistic Lock) (Cycle 10)

**🔴 Red: 테스트 작성**
- [ ] `OptimisticLockIntegrationTest.java` 생성
- [ ] 세션 생성 및 S3 업로드 완료 후
- [ ] 2개 스레드가 동시 완료 요청 테스트 작성
- [ ] 1개 성공 (200 OK), 1개 실패 (409 Conflict) 검증
- [ ] 커밋: `test: Optimistic Lock 동시성 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] prepareAndUpload() 헬퍼 메서드 구현
- [ ] ExecutorService로 2개 스레드 동시 완료 요청
- [ ] successCount == 1, conflictCount == 1 검증
- [ ] 커밋: `impl: Optimistic Lock 동시성 테스트 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Optimistic Lock 테스트 헬퍼 메서드 추출
- [ ] 커밋: `refactor: Optimistic Lock 동시성 테스트 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `OptimisticLockFixture.java` 생성
- [ ] 커밋: `test: Optimistic Lock Fixture 정리 (Tidy)`

---

### Phase 4: 예외 시나리오 테스트 (3 사이클)

#### 1️⃣1️⃣ 파일 크기 초과 예외 (Cycle 11)

**🔴 Red: 테스트 작성**
- [ ] `FileSizeExceededIntegrationTest.java` 생성
- [ ] 200MB 파일 업로드 시도 (SINGLE 최대 100MB 초과)
- [ ] 400 Bad Request 검증
- [ ] errorCode == "FILE_SIZE_EXCEEDED" 검증
- [ ] 커밋: `test: 파일 크기 초과 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] PrepareUploadRequest (200MB)
- [ ] POST /api/v1/upload-sessions 호출
- [ ] 400 Bad Request 응답 검증
- [ ] 커밋: `impl: 파일 크기 초과 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 예외 테스트 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 파일 크기 초과 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `ExceptionScenarioFixture.java` 생성
- [ ] 커밋: `test: Exception Scenario Fixture 정리 (Tidy)`

---

#### 1️⃣2️⃣ 지원하지 않는 파일 타입 예외 (Cycle 12)

**🔴 Red: 테스트 작성**
- [ ] `UnsupportedFileTypeIntegrationTest.java` 생성
- [ ] application/pdf (허용되지 않는 타입) 업로드 시도
- [ ] 400 Bad Request 검증
- [ ] errorCode == "UNSUPPORTED_FILE_TYPE" 검증
- [ ] 커밋: `test: 지원하지 않는 파일 타입 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] PrepareUploadRequest (mimeType: "application/pdf")
- [ ] POST /api/v1/upload-sessions 호출
- [ ] 400 Bad Request 응답 검증
- [ ] 커밋: `impl: 지원하지 않는 파일 타입 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 파일 타입 검증 테스트 개선
- [ ] 커밋: `refactor: 지원하지 않는 파일 타입 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] Fixture 사용으로 테스트 정리
- [ ] 커밋: `test: 파일 타입 예외 Fixture 정리 (Tidy)`

---

#### 1️⃣3️⃣ 권한 없음 예외 (타인 파일 조회) (Cycle 13)

**🔴 Red: 테스트 작성**
- [ ] `UnauthorizedAccessIntegrationTest.java` 생성
- [ ] User A가 파일 업로드
- [ ] User B가 조회 시도
- [ ] 403 Forbidden 검증
- [ ] errorCode == "UNAUTHORIZED_ACCESS" 검증
- [ ] 커밋: `test: 권한 없음 예외 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] User A JWT 토큰으로 파일 업로드
- [ ] User B JWT 토큰으로 GET /files/{fileId} 호출
- [ ] 403 Forbidden 응답 검증
- [ ] 커밋: `impl: 권한 없음 예외 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 권한 검증 테스트 헬퍼 메서드 추출
- [ ] 커밋: `refactor: 권한 없음 예외 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `UnauthorizedAccessFixture.java` 생성
- [ ] 커밋: `test: Unauthorized Access Fixture 정리 (Tidy)`

---

### Phase 5: 스케줄러 테스트 (1 사이클)

#### 1️⃣4️⃣ ExpiredSessionCleanupScheduler 테스트 (Cycle 14)

**🔴 Red: 테스트 작성**
- [ ] `ExpiredSessionCleanupIntegrationTest.java` 생성
- [ ] 10개 세션 생성 (5개 만료, 5개 유효)
- [ ] 스케줄러 실행 후 만료된 세션만 삭제 확인
- [ ] 커밋: `test: ExpiredSessionCleanupScheduler 테스트 추가 (Red)`

**⚠️ 스케줄러 테스트 전략**:
- @SpyBean으로 Scheduler Mock
- 또는 짧은 Cron 표현식 설정

**🟢 Green: 최소 구현**
- [ ] createExpiredSessions(5) 헬퍼 메서드 구현
- [ ] createActiveSessions(5) 헬퍼 메서드 구현
- [ ] 스케줄러 수동 트리거 또는 대기
- [ ] redisTemplate.hasKey() 검증
- [ ] 커밋: `impl: ExpiredSessionCleanupScheduler 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 스케줄러 테스트 전략 개선
- [ ] 커밋: `refactor: ExpiredSessionCleanupScheduler 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `SchedulerFixture.java` 생성
- [ ] 커밋: `test: Scheduler Fixture 정리 (Tidy)`

---

### Phase 6: 테스트 데이터 정리 및 격리 (2 사이클)

#### 1️⃣5️⃣ 테스트 데이터 자동 정리 (@AfterEach) (Cycle 15)

**🔴 Red: 테스트 작성**
- [ ] `DataCleanupIntegrationTest.java` 생성
- [ ] 테스트 실행 후 데이터 정리 검증
- [ ] MySQL, Redis 데이터 모두 삭제 확인
- [ ] 커밋: `test: 테스트 데이터 정리 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] `@AfterEach cleanupTestData()` 메서드 구현
- [ ] MySQL: DELETE FROM files WHERE ...
- [ ] Redis: FLUSHDB 또는 개별 키 삭제
- [ ] 커밋: `impl: 테스트 데이터 정리 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] Cleanup 전략 최적화
- [ ] 커밋: `refactor: 테스트 데이터 정리 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `DataCleanupFixture.java` 생성
- [ ] 커밋: `test: Data Cleanup Fixture 정리 (Tidy)`

---

#### 1️⃣6️⃣ 테스트 격리 검증 (Cycle 16)

**🔴 Red: 테스트 작성**
- [ ] `TestIsolationIntegrationTest.java` 생성
- [ ] 2개 테스트가 서로 영향 없이 독립 실행 검증
- [ ] 테스트 순서 무관하게 통과 확인
- [ ] 커밋: `test: 테스트 격리 검증 테스트 추가 (Red)`

**🟢 Green: 최소 구현**
- [ ] @TestMethodOrder(Random) 설정
- [ ] 각 테스트 독립 실행 확인
- [ ] 커밋: `impl: 테스트 격리 검증 구현 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 테스트 격리 전략 개선
- [ ] 커밋: `refactor: 테스트 격리 검증 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] `TestIsolationFixture.java` 생성
- [ ] 커밋: `test: Test Isolation Fixture 정리 (Tidy)`

---

### Phase 7: 최종 검증 (1 사이클)

#### 1️⃣7️⃣ 최종 통합 검증 및 문서화 (Cycle 17)

**🔴 Red: 테스트 작성**
- [ ] 모든 Integration Test 실행 및 통과 확인
- [ ] 테스트 실행 시간 < 5분 확인
- [ ] 커밋: `test: 최종 통합 검증 체크리스트 (Red)`

**🟢 Green: 최소 구현**
- [ ] CI/CD 파이프라인 설정 (GitHub Actions)
- [ ] TestContainers 자동 실행 확인
- [ ] 커밋: `impl: 최종 통합 검증 통과 (Green)`

**♻️ Refactor: 리팩토링**
- [ ] 코드 리뷰 준비
- [ ] README 업데이트 (Integration Test 실행 방법)
- [ ] 커밋: `refactor: 최종 통합 검증 개선 (Refactor)`

**🧹 Tidy: TestFixture 정리**
- [ ] 모든 Fixture 정리 완료 확인
- [ ] 커밋: `test: 최종 Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

### 구현 완료
- [ ] TestContainers Config (MySQL, Redis, LocalStack)
- [ ] E2E 시나리오 테스트 3개 (단일 업로드, 멀티파트 업로드, 세션 만료)
- [ ] 동시성 테스트 2개 (멱등성, Optimistic Lock)
- [ ] 예외 시나리오 테스트 3개 (크기 초과, 타입 오류, 권한 없음)
- [ ] 스케줄러 테스트 1개 (Expired Session Cleanup)
- [ ] 테스트 데이터 정리 (@AfterEach)
- [ ] 테스트 격리 검증

### 테스트 완료
- [ ] 모든 E2E 테스트 통과 (3개)
- [ ] 모든 동시성 테스트 통과 (2개)
- [ ] 모든 예외 시나리오 테스트 통과 (3개)
- [ ] 스케줄러 테스트 통과 (1개)
- [ ] 테스트 실행 시간 < 5분

### 품질 검증
- [ ] 모든 Integration Test 통과
- [ ] 테스트 격리 확인 (독립 실행 가능)
- [ ] CI/CD 파이프라인 통합
- [ ] 코드 리뷰 승인

---

## 🎯 Zero-Tolerance 규칙 준수

### TestRestTemplate 필수
- [ ] E2E 테스트는 실제 HTTP 요청 (MockMvc 금지)
- [ ] @SpringBootTest(webEnvironment = RANDOM_PORT)

### Flyway 마이그레이션
- [ ] 테스트 DB 스키마 자동 생성
- [ ] @Sql 금지

### TestContainers
- [ ] MySQL 8.0
- [ ] Redis 7.0 (Keyspace Notification 활성화)
- [ ] LocalStack (S3 서비스)

### 테스트 격리
- [ ] 각 테스트는 독립적으로 실행 가능
- [ ] 트랜잭션 롤백 금지 (E2E는 실제 커밋)
- [ ] @AfterEach로 데이터 정리

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/fileflow/docs/prd/presigned-url-upload.md`
- **Task**: `/Users/sangwon-ryu/fileflow/docs/prd/session/FILE-006-005.md`
- **Integration Test 규칙**: `docs/coding_convention/05-testing/integration-testing/`

---

## 📝 참고사항

### TDD 진행 순서 (권장)

1. **TestContainers 설정** (Cycle 1-2):
   - MySQL, Redis, LocalStack
2. **E2E 시나리오 테스트** (Cycle 3-8):
   - 단일 업로드 → 멀티파트 업로드 → 세션 만료
3. **동시성 테스트** (Cycle 9-10):
   - 멱등성 → Optimistic Lock
4. **예외 시나리오 테스트** (Cycle 11-13):
   - 크기 초과 → 타입 오류 → 권한 없음
5. **스케줄러 테스트** (Cycle 14):
   - Expired Session Cleanup
6. **테스트 정리 및 격리** (Cycle 15-16):
   - 데이터 정리 → 격리 검증
7. **최종 검증** (Cycle 17):
   - CI/CD 통합, 문서화

### LocalStack S3 설정 예시

```java
@BeforeEach
void setupS3() {
    S3Client s3Client = S3Client.builder()
        .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                localstack.getAccessKey(),
                localstack.getSecretKey()
            )
        ))
        .region(Region.of(localstack.getRegion()))
        .build();

    s3Client.createBucket(b -> b.bucket("fileflow-test"));
}
```

### 테스트 실행 시간 최적화

- [ ] TestContainers 재사용 (static Container)
- [ ] Parallel 테스트 실행 (JUnit 5 @Execution)
- [ ] 불필요한 Thread.sleep() 제거 (Mock Clock 사용)

---

**다음 단계**:
1. `/kb/integration/go` - TDD 사이클 시작
2. 전체 구현 완료 후 PR 생성
