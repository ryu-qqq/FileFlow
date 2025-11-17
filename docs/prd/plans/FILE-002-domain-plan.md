# FILE-002: Domain Layer TDD Plan

**Issue Key**: FILE-002
**PRD**: [file-management-system.md](../file-management-system.md)
**Layer**: Domain
**Status**: 🟡 In Progress
**생성일**: 2025-01-17
**최종 수정일**: 2025-01-17

---

## 📋 현황 분석

### ✅ 구현 완료 항목

#### Aggregates (5개)
- [x] **File** - 596 lines
  - forNew/of/reconstitute 패턴 ✅
  - Private constructor ✅
  - 비즈니스 메서드 ✅
  - Clock 필드 ✅
- [x] **UploadSession** - 449 lines
  - forNew/of/reconstitute 패턴 ✅
  - Private constructor ✅
  - Record accessor 패턴 ✅
  - Clock 필드 ✅
- [x] **DownloadSession**
  - forNew/of/reconstitute 패턴 ✅
  - Private constructor ✅
  - RetryCount VO 사용 ✅
- [x] **FileProcessingJob**
  - forNew/of/reconstitute 패턴 ✅
  - Private constructor ✅
  - RetryCount.forJob() 사용 ✅
- [x] **MessageOutbox**
  - forNew/of/reconstitute 패턴 ✅
  - Private constructor ✅
  - RetryCount.forOutbox() 사용 ✅

#### Value Objects (30개)
**Core File VOs**:
- [x] FileName (Record, of 패턴)
- [x] FileSize (Record, of 패턴, Custom Exception)
- [x] MimeType (Record, of 패턴, Custom Exception)
- [x] FileCategory (Record, of 패턴)
- [x] Tags (Record, of 패턴)
- [x] Checksum (Record, of 패턴)
- [x] ETag (Record, of 패턴)
- [x] ExternalUrl (Record, of 패턴)

**Upload Session VOs**:
- [x] SessionId (Record, generate/of 패턴)
- [x] UploadType (Enum)
- [x] SessionStatus (Enum)
- [x] MultipartUpload (Class, 복잡한 로직)
- [x] UploadedPart (Record)
- [x] MultipartUploadId (Record, of 패턴)
- [x] MultipartStatus (Enum)

**Retry & Quota VOs**:
- [x] RetryCount (Class, forFile/forJob/forOutbox 패턴)

**ID VOs**:
- [x] FileId (Record, forNew/of 패턴)
- [x] UploaderId (Record, of 패턴)
- [x] TenantId (Record, of 패턴)
- [x] FileProcessingJobId (Record, forNew/of 패턴)
- [x] MessageOutboxId (Record, forNew/of 패턴)
- [x] AggregateId (Record, of 패턴)

**Status VOs**:
- [x] FileStatus (Enum)
- [x] OutboxStatus (Enum)
- [x] JobStatus (Enum)
- [x] JobType (Enum)

**Search Criteria VOs**:
- [x] FileSearchCriteria
- [x] FileProcessingJobSearchCriteria
- [x] MessageOutboxSearchCriteria

#### Exceptions (4개)
- [x] DomainException (Base)
- [x] ErrorCode (Interface)
- [x] InvalidFileSizeException + ErrorCode
- [x] InvalidMimeTypeException + ErrorCode

#### Utils (1개)
- [x] UuidV7Generator

---

## 🧪 TDD 사이클 계획

### Cycle 1-13: ✅ 실패한 VO 테스트 수정 (빠른 수정) - **완료**

**문제**: VO 예외 메시지가 변경되었지만 테스트 assertion은 구 메시지 기대

**실패 테스트**:
1. ✅ SessionIdTest (1개) - "SessionId는 null이거나 빈 값일 수 없습니다" → "SessionId는 null일 수 없습니다 (forNew() 사용)"
2. ✅ MultipartUploadIdTest (1개) - 메시지 불일치
3. ✅ MimeTypeTest (7개) - InvalidMimeTypeErrorCode 메시지 불일치
4. ✅ FileSizeTest (2개) - InvalidFileSizeErrorCode 메시지 불일치
5. ✅ FileTest (3개) - File Aggregate 검증 메시지 수정 (EMPTY_FILE_SIZE ErrorCode)

**작업**:
```
Cycle 1-13: test: 13개 VO 테스트 예외 메시지 수정 (Red → Green)
```

**커밋 패턴**:
```bash
git commit -m "test: SessionId VO 예외 메시지 수정 (Red → Green)"
git commit -m "test: MultipartUploadId VO 예외 메시지 수정 (Red → Green)"
git commit -m "test: MimeType VO 예외 메시지 7개 수정 (Red → Green)"
git commit -m "test: FileSize VO 예외 메시지 2개 수정 (Red → Green)"
git commit -m "test: File Aggregate 테스트 2개 수정 (Red → Green)"
```

---

### Cycle 14-18: Aggregate 비즈니스 로직 테스트 보강

**현황**:
- ✅ 5개 Aggregate 테스트 파일 존재
- ⚠️ 비즈니스 메서드 테스트 커버리지 불충분 가능성

**작업**:

#### Cycle 14: File Aggregate 테스트 보강
```
테스트 항목:
- [ ] markAsUploading() 상태 전환
- [ ] markAsCompleted() 상태 전환
- [ ] markAsFailed() 상태 전환 + 재시도 체크
- [ ] markAsProcessing() 상태 전환
- [ ] incrementRetryCount() 재시도 로직
- [ ] softDelete() Soft Delete 동작
```

**커밋**:
```bash
git commit -m "test: File Aggregate 비즈니스 메서드 테스트 추가"
git commit -m "feat: File Aggregate 비즈니스 메서드 구현 (이미 구현됨)"
```

#### Cycle 15: UploadSession Aggregate 테스트 보강 - ✅ **완료**
```
테스트 항목:
- [x] updateToInProgress() 상태 전환 ✅ 이미 존재
- [x] completeWithETag() 상태 전환 + ETag 저장 ✅ 이미 존재
- [x] updateToExpired() 만료 처리 ✅ 추가 완료
- [x] fail() 실패 처리 ✅ 추가 완료
- [x] initiateMultipartUpload() 멀티파트 초기화 ✅ 이미 존재
- [x] addUploadedPart() 파트 추가 ✅ 이미 존재
- [x] isExpired() 만료 여부 체크 ✅ 이미 존재
- [x] validateChecksum() 체크섬 검증 ✅ 추가 완료
```

**커밋**:
```bash
git commit -m "test: UploadSession Aggregate 비즈니스 메서드 테스트 추가"
git commit -m "feat: UploadSession Aggregate 비즈니스 메서드 구현 (이미 구현됨)"
```

#### Cycle 16: DownloadSession Aggregate 테스트 보강 - ✅ **완료**
```
테스트 항목:
- [x] updateToInProgress() 상태 전환 ✅ 이미 존재
- [x] completeWithFileInfo() 완료 처리 + 파일 정보 저장 ✅ 이미 존재
- [x] updateToExpired() 만료 처리 ✅ 추가 완료
- [x] fail() 실패 처리 ✅ 추가 완료
- [x] incrementRetryCount() 재시도 로직 ✅ 이미 존재
- [x] isExpired() 만료 여부 체크 ✅ 이미 존재
```

**커밋**:
```bash
git commit -m "test: DownloadSession Aggregate 비즈니스 메서드 테스트 추가"
git commit -m "feat: DownloadSession Aggregate 비즈니스 메서드 구현 (이미 구현됨)"
```

#### Cycle 17: FileProcessingJob Aggregate 테스트 보강 - ✅ **완료**
```
테스트 항목:
- [x] markAsProcessing() 상태 전환 ✅ 이미 존재 (Lines 320, 415)
- [x] markAsCompleted(String outputS3Key) 완료 처리 ✅ 이미 존재 (Lines 243, 335, 430, 466, 507)
- [x] markAsFailed(String errorMessage) 실패 처리 ✅ 이미 존재 (Lines 351, 448, 538)
- [x] incrementRetryCount() 재시도 로직 ✅ 이미 존재 (Line 369)
- [x] canRetry() 재시도 가능 여부 ✅ 이미 존재 (Lines 383, 398)
```

**결과**: 모든 비즈니스 메서드 테스트 이미 존재 (25+ 테스트)

#### Cycle 18: MessageOutbox Aggregate 테스트 보강 - ✅ **완료**
```
테스트 항목:
- [x] markAsSent() 상태 전환 ✅ 이미 존재 (Lines 96-107, 111-121)
- [x] markAsFailed() 실패 처리 ✅ 이미 존재 (Lines 125-135)
- [x] incrementRetryCount() 재시도 로직 ✅ 이미 존재 (Lines 141-151)
- [x] canRetry() 재시도 가능 여부 ✅ 이미 존재 (Lines 155-166, 170-173)
```

**결과**: 모든 비즈니스 메서드 테스트 이미 존재

**커밋**:
```bash
git commit -m "test: MessageOutbox Aggregate 비즈니스 메서드 테스트 추가"
git commit -m "feat: MessageOutbox Aggregate 비즈니스 메서드 구현 (이미 구현됨)"
```

---

### Cycle 19-23: VO 비즈니스 메서드 테스트 보강

#### Cycle 19: FileSize VO 비즈니스 메서드
```
테스트 항목:
- [ ] isSingleUpload() - 100MB 미만 체크
- [ ] isMultipartUpload() - 100MB 이상 체크
- [ ] 경계값 테스트 (99MB, 100MB, 101MB)
```

**커밋**:
```bash
git commit -m "test: FileSize VO 비즈니스 메서드 테스트 추가"
```

#### Cycle 20: MimeType VO 비즈니스 메서드
```
테스트 항목:
- [ ] isImage() - image/* 체크
- [ ] isPdf() - application/pdf 체크
- [ ] 다양한 MIME 타입 테스트
```

**커밋**:
```bash
git commit -m "test: MimeType VO 비즈니스 메서드 테스트 추가"
```

#### Cycle 21: RetryCount VO 비즈니스 메서드
```
테스트 항목:
- [ ] canRetry() - 재시도 가능 여부
- [ ] increment() - 재시도 횟수 증가
- [ ] remaining() - 남은 재시도 횟수
- [ ] 최대 재시도 초과 시 예외 처리
- [ ] forFile/forJob/forOutbox 팩토리 메서드
```

**커밋**:
```bash
git commit -m "test: RetryCount VO 비즈니스 메서드 테스트 추가"
```

#### Cycle 22: MultipartUpload VO 비즈니스 메서드
```
테스트 항목:
- [ ] addPart() - 파트 추가
- [ ] isAllPartsUploaded() - 모든 파트 업로드 완료 체크
- [ ] markAsCompleted() - 완료 처리
- [ ] markAsAborted() - 중단 처리
```

**커밋**:
```bash
git commit -m "test: MultipartUpload VO 비즈니스 메서드 테스트 추가"
```

#### Cycle 23: SessionId VO 생성 패턴 테스트
```
테스트 항목:
- [ ] generate() - UUID v7 생성
- [ ] of() - 기존 UUID 변환
- [ ] forNew() - 새 세션용 생성
- [ ] UUID v7 형식 검증
```

**커밋**:
```bash
git commit -m "test: SessionId VO 생성 패턴 테스트 추가"
```

---

### Cycle 24: TestFixture 누락 확인 및 추가

**현황**: 15개 Fixture 파일 존재

**확인 필요**:
- [ ] 모든 Aggregate에 대응하는 Fixture 존재 여부
- [ ] 모든 VO에 대응하는 Fixture 존재 여부
- [ ] forNew/of/reconstitute 패턴 준수 여부

**누락 시 추가**:
```bash
git commit -m "test: XXXFixture 추가 (TestFixture 패턴)"
```

---

### Cycle 25: ArchUnit 테스트 100% 통과 확인

**현황**: 51/52 통과 (1개 의도적 비활성화)

**확인 사항**:
- [ ] AggregateRootArchTest (23/24 통과, 1개 disabled)
- [ ] VOArchTest (8/8 통과)
- [ ] ExceptionArchTest (20/20 통과)

**작업**:
```
1. 전체 ArchUnit 테스트 재실행
2. 비활성화된 규칙 검토 (aggregateRoot_BusinessMethodsShouldHaveExplicitVerbs)
3. 필요 시 규칙 재활성화 또는 제거
```

**커밋**:
```bash
# 추가 수정 필요 시
git commit -m "test: ArchUnit 규칙 수정"
```

---

### Cycle 26: 테스트 커버리지 95% 달성

**목표**: Domain Layer 전체 95% 이상

**작업 순서**:
```
1. Jacoco 리포트 생성
./gradlew :domain:test :domain:jacocoTestReport

2. 커버리지 미달 클래스 식별
- Aggregate: File, UploadSession, DownloadSession, FileProcessingJob, MessageOutbox
- VO: 모든 VO 클래스
- Exception: DomainException, InvalidFileSizeException, InvalidMimeTypeException

3. 미달 메서드 테스트 추가
- 각 클래스별 누락된 메서드 테스트 작성
- Edge Case 테스트 추가

4. 최종 검증
./gradlew :domain:test :domain:jacocoTestReport
```

**커밋 패턴**:
```bash
git commit -m "test: XXX 클래스 커버리지 95% 달성"
git commit -m "test: Domain Layer 커버리지 95% 달성"
```

---

## 📊 완료 조건

### Must Have (필수)
- [x] 13개 실패 VO 테스트 수정 완료 (Cycle 1-13) ✅ **2025-01-17 완료**
- [ ] 5개 Aggregate 비즈니스 메서드 테스트 100% (Cycle 14-18)
- [ ] 핵심 VO 비즈니스 메서드 테스트 100% (Cycle 19-23)
- [ ] ArchUnit 테스트 100% 통과 (Cycle 25)
- [ ] Domain Layer 테스트 커버리지 95% 이상 (Cycle 26)

### Should Have (권장)
- [ ] TestFixture 패턴 100% 준수 (Cycle 24)
- [ ] Edge Case 테스트 추가
- [ ] Mutation Testing (PIT) 80% 이상

### Nice to Have (선택)
- [ ] Property-Based Testing (Jqwik)
- [ ] ArchUnit Custom Rules 추가

---

## 🚀 실행 방법

### TDD 사이클 자동 실행
```bash
# Cycle 1-13: VO 테스트 수정 (빠른 수정)
/kb/domain/go  # → Plan 파일 읽기 → Cycle 1 실행

# Cycle 14-18: Aggregate 테스트 보강
/kb/domain/go  # → 다음 Cycle 자동 실행

# Cycle 19-23: VO 비즈니스 메서드 테스트
/kb/domain/go  # → 계속 반복

# Cycle 24: TestFixture 확인
/kb/domain/go

# Cycle 25: ArchUnit 검증
/kb/domain/go

# Cycle 26: 커버리지 95% 달성
/kb/domain/go
```

### 수동 실행
```bash
# 테스트 실행
./gradlew :domain:test

# 커버리지 리포트 생성
./gradlew :domain:jacocoTestReport

# ArchUnit 단독 실행
./gradlew :domain:test --tests "*ArchTest"
```

---

## 📚 참고 규칙

### Domain Layer 컨벤션
- [Aggregate Root 규칙](../../coding_convention/02-domain-layer/aggregate/guide.md)
- [Value Object 규칙](../../coding_convention/02-domain-layer/vo/guide.md)
- [Exception 규칙](../../coding_convention/02-domain-layer/exception/guide.md)

### TDD 철학
- [Kent Beck TDD + Tidy First](../../.claude/CLAUDE.md#kent-beck-tdd--tidy-first-철학)

---

**작성자**: Claude Code (Sonnet 4.5)
**검토자**: ryu-qqq
**다음 Plan**: FILE-003-application-plan.md (Application Layer)
