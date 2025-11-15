# FILE-001 Domain Refactoring Plan

**목적**: Domain Aggregate 컨벤션 위반사항 수정 (총 27개 위반사항)

**전략**: 점진적 마이그레이션 (Aggregate별 순차 수정)

**대상 Aggregate**:
1. MessageOutbox (9개 위반)
2. FileProcessingJob (9개 위반)
3. File (9개 위반)

**수정 우선순위**:
- 🔴 Priority 1: 생성자 private + 3종 팩토리, ID/외래키 VO
- 🟡 Priority 2: Clock 의존성, 불변→가변 패턴
- 🟢 Priority 3: updatedAt, getIdValue()

---

## Phase 1: MessageOutbox Refactoring (Cycles 1-6)

### Cycle 1: MessageOutboxId VO 생성

**🎯 목표**: ID Value Object 생성 및 테스트

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/vo/MessageOutboxId.java` (신규)
- `domain/src/test/java/com/ryuqq/fileflow/domain/vo/MessageOutboxIdTest.java` (신규)

**🔴 Red Phase**:
- [ ] MessageOutboxIdTest.java 생성
  - [ ] `shouldCreateValidMessageOutboxId()` - 유효한 ID 생성
  - [ ] `shouldThrowExceptionWhenValueIsNull()` - null 검증
  - [ ] `shouldThrowExceptionWhenValueIsBlank()` - blank 검증
  - [ ] `shouldReturnSameValueFromGetValue()` - getValue() 검증
  - [ ] `shouldBeEqualWhenValueIsSame()` - equals() 검증
  - [ ] `shouldHaveSameHashCodeWhenValueIsSame()` - hashCode() 검증
- [ ] 컴파일 에러 확인 (MessageOutboxId 클래스 없음)
- [ ] **커밋**: `test: MessageOutboxId VO 테스트 추가`

**🟢 Green Phase**:
- [ ] MessageOutboxId.java 구현
  - [ ] private final String value 필드
  - [ ] private 생성자 (검증 로직 포함)
  - [ ] `of(String value)` 정적 팩토리 메서드
  - [ ] `getValue()` 메서드
  - [ ] `equals()`, `hashCode()` 메서드
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: MessageOutboxId VO 구현`

**♻️ Refactor Phase**:
- [ ] 중복 코드 제거 (필요 시)
- [ ] 검증 로직 명확화
- [ ] **커밋**: `struct: MessageOutboxId 검증 로직 개선` (필요 시)

**🧹 Tidy Phase**:
- [ ] MessageOutboxIdFixture.java 생성 (testFixtures)
  - [ ] `aMessageOutboxId()` 기본 빌더
  - [ ] `aValidMessageOutboxId()` 유효한 ID
- [ ] **커밋**: `test: MessageOutboxId Fixture 추가`

**✅ 완료 체크**:
- [x] 6개 테스트 모두 통과
- [x] ArchUnit 통과 (Jacoco 커버리지는 전체 리팩토링 완료 후 해결)
- [x] Fixture 생성 완료
- [x] **총 커밋 수**: 3개

**📝 커밋 해시**:
- Red: `a824309`
- Green: `55a35d0`
- Refactor: (생략 - 리팩토링 불필요)
- Tidy: `599b932`

---

### Cycle 2: MessageOutbox 생성자 private + 3종 팩토리 메서드

**🎯 목표**: 생성자 private 변경 + forNew(), of(), reconstitute() 추가

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/MessageOutbox.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/MessageOutboxTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/MessageOutboxFixture.java`

**🔴 Red Phase**:
- [x] MessageOutboxTest.java에 3종 팩토리 테스트 추가
  - [x] `shouldCreateNewOutboxWithForNew()` - forNew() 테스트 (ID null)
  - [x] `shouldCreateOutboxWithOf()` - of() 테스트 (ID 필수)
  - [x] `shouldThrowExceptionWhenOfWithNullId()` - of() null 검증
  - [x] `shouldReconstituteOutbox()` - reconstitute() 테스트
  - [x] `shouldThrowExceptionWhenReconstituteWithNullId()` - reconstitute() null 검증
- [x] 컴파일 에러 확인 (메서드 없음)
- [x] **커밋**: `test: MessageOutbox 3종 팩토리 메서드 테스트 추가`

**🟢 Green Phase**:
- [x] MessageOutbox.java 수정
  - [x] 생성자를 `public` → `private`으로 변경
  - [x] `id` 타입을 `String` → `MessageOutboxId`로 변경
  - [x] `forNew()` 정적 팩토리 메서드 구현 (ID null)
  - [x] `of()` 정적 팩토리 메서드 구현 (ID 필수, 검증)
  - [x] `reconstitute()` 정적 팩토리 메서드 구현 (ID 필수, 검증)
  - [x] 기존 `create()` 메서드 `@Deprecated` 처리 (하위 호환)
- [x] 기존 테스트 수정 (MessageOutboxId 사용)
- [x] 모든 테스트 통과 확인 (17개 테스트)
- [x] **커밋**: `feat: MessageOutbox 3종 팩토리 메서드 구현`

**♻️ Refactor Phase**:
- [x] 생성자 검증 로직 메서드 추출 (`validateConstructorArguments()`)
- [x] null/blank 검증 추가
- [x] retryCount, maxRetryCount 범위 검증 추가
- [x] **커밋**: `struct: MessageOutbox 생성자 검증 로직 메서드 추출`

**🧹 Tidy Phase**:
- [x] MessageOutboxFixture 수정
  - [x] `createOutbox()`가 `forNew()` 사용하도록 변경
  - [x] `createOutboxLegacy()` 추가 (`@Deprecated`)
- [x] Builder가 `reconstitute()` 사용 확인
- [x] **커밋**: `test: MessageOutboxFixture forNew() 사용으로 변경`

**✅ 완료 체크**:
- [x] 기존 테스트 + 5개 신규 테스트 모두 통과 (총 17개)
- [x] 생성자 private 확인
- [x] MessageOutboxId 사용 확인
- [x] **총 커밋 수**: 4개

**📝 커밋 해시**:
- Red: `3691e4e`
- Green: `2b43035`
- Refactor: `5a75dcf`
- Tidy: `72327ee`

---

### Cycle 3: MessageOutbox Clock 의존성 주입

**🎯 목표**: Clock 필드 추가 및 LocalDateTime.now(clock) 사용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/MessageOutbox.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/MessageOutboxTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/MessageOutboxFixture.java`

**🔴 Red Phase**:
- [x] MessageOutboxTest.java에 Clock 관련 테스트 추가
  - [x] `shouldUseClockForCreatedAtInForNew()` - forNew() Clock 사용 검증
  - [x] `shouldUseClockForProcessedAtInMarkAsSent()` - markAsSent() Clock 사용 검증
  - [x] `shouldCreateOutboxWithFixedClock()` - 고정 시간 테스트
- [x] 컴파일 에러 확인
- [x] **커밋**: `test: MessageOutbox Clock 의존성 테스트 추가 (Red)`

**🟢 Green Phase**:
- [x] MessageOutbox.java 수정
  - [x] `private final Clock clock;` 필드 추가
  - [x] 생성자에 Clock 파라미터 추가 + 검증 로직
  - [x] 모든 `LocalDateTime.now()` → `LocalDateTime.now(clock)` 변경
  - [x] forNew(), of(), reconstitute() 메서드에 Clock 파라미터 추가
  - [x] markAsSent(), markAsFailed() Clock 파라미터 추가
  - [x] create() 레거시 메서드 Clock.systemUTC() 기본값 사용
- [x] MessageOutboxFixture 모든 메서드 Clock.systemUTC() 추가
- [x] 기존 테스트 수정 (Clock.systemUTC() 전달)
- [x] 모든 테스트 통과 확인 (20개 테스트)
- [x] **커밋**: `feat: MessageOutbox Clock 의존성 주입 (Green)`

**♻️ Refactor Phase**:
- [x] Clock 파라미터 Javadoc 명확화 (테스트 고정 시간, processedAt 생성 명시)
- [x] **커밋**: `struct: Clock 파라미터 Javadoc 명확화 (Refactor)`

**🧹 Tidy Phase**:
- [x] Plan 파일 업데이트 (Cycle 3 완료 표시)
- [ ] **커밋**: `docs: FILE-001-domain-plan.md Cycle 3 완료 표시`

**✅ 완료 체크**:
- [x] 기존 테스트 + 3개 신규 테스트 모두 통과 (총 20개)
- [x] Clock 필드 존재 확인
- [x] LocalDateTime.now() 직접 호출 0개 확인
- [x] **총 커밋 수**: 3개

**📝 커밋 해시**:
- Red: `f91b54d`
- Green: `7734e7a`
- Refactor: `8235a6b`
- Tidy: `46717c0`

---

### Cycle 4: MessageOutbox 불변→가변 패턴 전환

**🎯 목표**: final 제거 + 비즈니스 메서드 void 반환

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/MessageOutbox.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/MessageOutboxTest.java`

**🔴 Red Phase**:
- [ ] MessageOutboxTest.java 수정 (가변 패턴 검증)
  - [ ] `shouldMutateStatusWhenMarkAsSent()` - markAsSent() 가변 검증
  - [ ] `shouldMutateStatusWhenMarkAsFailed()` - markAsFailed() 가변 검증
  - [ ] `shouldMutateRetryCountWhenIncrement()` - incrementRetryCount() 가변 검증
  - [ ] `shouldNotReturnNewInstanceWhenMarkAsSent()` - 동일 객체 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: MessageOutbox 가변 패턴 테스트 추가`

**🟢 Green Phase**:
- [ ] MessageOutbox.java 수정
  - [ ] `status`, `retryCount`, `processedAt` final 제거
  - [ ] `markAsSent()` 반환 타입 `MessageOutbox` → `void`
    - [ ] `this.status = OutboxStatus.SENT;` (this 변경)
    - [ ] `this.processedAt = LocalDateTime.now(clock);`
  - [ ] `markAsFailed()` 반환 타입 `MessageOutbox` → `void`
  - [ ] `incrementRetryCount()` 반환 타입 `MessageOutbox` → `void`
    - [ ] `this.retryCount++;`
  - [ ] `withStatus()` private 헬퍼 메서드 제거
- [ ] 기존 테스트 수정 (void 반환 대응)
- [ ] Fixture 수정 (aSentOutbox, aFailedOutbox 패턴 변경)
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: MessageOutbox 불변→가변 패턴 전환`

**♻️ Refactor Phase**:
- [ ] 비즈니스 메서드 순서 정리
- [ ] 상태 전환 로직 명확화
- [ ] **커밋**: `struct: MessageOutbox 비즈니스 메서드 정리`

**🧹 Tidy Phase**:
- [ ] MessageOutboxFixture 수정
  - [ ] aSentOutbox(): 생성 후 markAsSent() 호출로 변경
  - [ ] aFailedOutbox(): 생성 후 markAsFailed() 호출로 변경
- [ ] **커밋**: `test: MessageOutboxFixture 가변 패턴 적용`

**✅ 완료 체크**:
- [ ] 기존 테스트 + 4개 신규 테스트 모두 통과
- [ ] status, retryCount, processedAt final 제거 확인
- [ ] 비즈니스 메서드 void 반환 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 5: MessageOutbox AggregateId VO 추가

**🎯 목표**: aggregateId를 String → AggregateId VO로 변경

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/vo/AggregateId.java` (신규)
- `domain/src/test/java/com/ryuqq/fileflow/domain/vo/AggregateIdTest.java` (신규)
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/MessageOutbox.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/MessageOutboxTest.java`

**🔴 Red Phase**:
- [ ] AggregateIdTest.java 생성 (MessageOutboxId 패턴 참조)
  - [ ] `shouldCreateValidAggregateId()` - 유효한 ID 생성
  - [ ] `shouldThrowExceptionWhenValueIsNull()` - null 검증
  - [ ] `shouldThrowExceptionWhenValueIsBlank()` - blank 검증
  - [ ] equals(), hashCode() 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: AggregateId VO 테스트 추가`

**🟢 Green Phase**:
- [ ] AggregateId.java 구현 (MessageOutboxId 패턴 참조)
  - [ ] private final String value
  - [ ] private 생성자
  - [ ] of(String value) 정적 팩토리
  - [ ] getValue(), equals(), hashCode()
- [ ] MessageOutbox.java 수정
  - [ ] `String aggregateId` → `AggregateId aggregateId`
  - [ ] 생성자 파라미터 타입 변경
  - [ ] Getter 타입 변경
- [ ] 테스트 수정 (AggregateId.of() 사용)
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: MessageOutbox AggregateId VO 적용`

**♻️ Refactor Phase**:
- [ ] 중복 코드 제거
- [ ] **커밋**: `struct: AggregateId 검증 로직 개선` (필요 시)

**🧹 Tidy Phase**:
- [ ] AggregateIdFixture.java 생성
- [ ] MessageOutboxFixture 수정 (AggregateId 사용)
- [ ] **커밋**: `test: AggregateId Fixture 추가`

**✅ 완료 체크**:
- [ ] AggregateId 테스트 4개 통과
- [ ] MessageOutbox aggregateId VO 사용 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 6: MessageOutbox updatedAt + getIdValue() 추가

**🎯 목표**: updatedAt 필드 추가 및 Law of Demeter 준수

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/MessageOutbox.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/MessageOutboxTest.java`

**🔴 Red Phase**:
- [ ] MessageOutboxTest.java에 테스트 추가
  - [ ] `shouldHaveUpdatedAtWhenCreated()` - forNew() updatedAt 검증
  - [ ] `shouldUpdateUpdatedAtWhenMarkAsSent()` - markAsSent() updatedAt 갱신 검증
  - [ ] `shouldUpdateUpdatedAtWhenMarkAsFailed()` - markAsFailed() updatedAt 갱신 검증
  - [ ] `shouldReturnIdValueWithoutChaining()` - getIdValue() 테스트
  - [ ] `shouldReturnAggregateIdValueWithoutChaining()` - getAggregateIdValue() 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: MessageOutbox updatedAt 및 getIdValue() 테스트 추가`

**🟢 Green Phase**:
- [ ] MessageOutbox.java 수정
  - [ ] `private LocalDateTime updatedAt;` 필드 추가
  - [ ] 생성자에 updatedAt 파라미터 추가
  - [ ] forNew()에서 `updatedAt = now` 설정
  - [ ] markAsSent()에서 `this.updatedAt = LocalDateTime.now(clock)` 추가
  - [ ] markAsFailed()에서 `this.updatedAt = LocalDateTime.now(clock)` 추가
  - [ ] incrementRetryCount()에서 updatedAt 갱신 (필요 시)
  - [ ] `getUpdatedAt()` 메서드 추가
  - [ ] `getIdValue()` 메서드 추가: `return id.getValue();`
  - [ ] `getAggregateIdValue()` 메서드 추가: `return aggregateId.getValue();`
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: MessageOutbox updatedAt 및 getIdValue() 추가`

**♻️ Refactor Phase**:
- [ ] updatedAt 갱신 로직 중복 제거 (필요 시)
- [ ] **커밋**: `struct: MessageOutbox updatedAt 갱신 로직 정리` (필요 시)

**🧹 Tidy Phase**:
- [ ] MessageOutboxFixture 수정 (updatedAt 설정)
- [ ] **커밋**: `test: MessageOutboxFixture updatedAt 추가`

**✅ 완료 체크**:
- [ ] 5개 신규 테스트 모두 통과
- [ ] updatedAt 필드 존재 및 갱신 확인
- [ ] getIdValue(), getAggregateIdValue() 메서드 존재 확인
- [ ] **총 커밋 수**: 3-4개
- [ ] **MessageOutbox 리팩토링 완료** 🎉

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

## Phase 2: FileProcessingJob Refactoring (Cycles 7-12)

### Cycle 7: FileProcessingJobId + FileId VO 생성

**🎯 목표**: FileProcessingJob 전용 ID VO 2개 생성

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/vo/FileProcessingJobId.java` (신규)
- `domain/src/test/java/com/ryuqq/fileflow/domain/vo/FileProcessingJobIdTest.java` (신규)
- `domain/src/main/java/com/ryuqq/fileflow/domain/vo/FileId.java` (신규)
- `domain/src/test/java/com/ryuqq/fileflow/domain/vo/FileIdTest.java` (신규)

**🔴 Red Phase**:
- [ ] FileProcessingJobIdTest.java 생성 (MessageOutboxId 패턴 참조)
  - [ ] 6개 테스트 (유효성, null, blank, getValue, equals, hashCode)
- [ ] FileIdTest.java 생성 (MessageOutboxId 패턴 참조)
  - [ ] 6개 테스트 (유효성, null, blank, getValue, equals, hashCode)
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: FileProcessingJobId, FileId VO 테스트 추가`

**🟢 Green Phase**:
- [ ] FileProcessingJobId.java 구현 (MessageOutboxId 패턴)
- [ ] FileId.java 구현 (MessageOutboxId 패턴)
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: FileProcessingJobId, FileId VO 구현`

**♻️ Refactor Phase**:
- [ ] 중복 코드 제거
- [ ] **커밋**: `struct: ID VO 검증 로직 개선` (필요 시)

**🧹 Tidy Phase**:
- [ ] FileProcessingJobIdFixture.java 생성
- [ ] FileIdFixture.java 생성
- [ ] **커밋**: `test: FileProcessingJobId, FileId Fixture 추가`

**✅ 완료 체크**:
- [ ] 12개 테스트 모두 통과 (각 VO당 6개)
- [ ] Fixture 생성 완료
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 8: FileProcessingJob 생성자 private + 3종 팩토리 메서드

**🎯 목표**: MessageOutbox Cycle 2와 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJob.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJobTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/FileProcessingJobFixture.java`

**🔴 Red Phase**:
- [ ] FileProcessingJobTest.java에 3종 팩토리 테스트 추가
  - [ ] `shouldCreateNewJobWithForNew()` - forNew() 테스트
  - [ ] `shouldCreateJobWithOf()` - of() 테스트
  - [ ] `shouldThrowExceptionWhenOfWithNullId()` - of() null 검증
  - [ ] `shouldReconstituteJob()` - reconstitute() 테스트
  - [ ] `shouldThrowExceptionWhenReconstituteWithNullId()` - reconstitute() null 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: FileProcessingJob 3종 팩토리 메서드 테스트 추가`

**🟢 Green Phase**:
- [ ] FileProcessingJob.java 수정
  - [ ] 생성자 `public` → `private`
  - [ ] `jobId` 타입 `String` → `FileProcessingJobId`
  - [ ] `fileId` 타입 `String` → `FileId`
  - [ ] `forNew()`, `of()`, `reconstitute()` 메서드 구현
  - [ ] 기존 `create()` 메서드 `@Deprecated`
- [ ] 기존 테스트 수정
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: FileProcessingJob 3종 팩토리 메서드 구현`

**♻️ Refactor Phase**:
- [ ] 생성자 검증 로직 개선
- [ ] **커밋**: `struct: FileProcessingJob 생성자 검증 로직 개선`

**🧹 Tidy Phase**:
- [ ] FileProcessingJobFixture 수정 (3종 팩토리 패턴)
- [ ] **커밋**: `test: FileProcessingJobFixture 3종 팩토리 패턴 적용`

**✅ 완료 체크**:
- [ ] 5개 신규 테스트 모두 통과
- [ ] 생성자 private 확인
- [ ] FileProcessingJobId, FileId 사용 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 9: FileProcessingJob Clock 의존성 주입

**🎯 목표**: MessageOutbox Cycle 3과 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJob.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJobTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/FileProcessingJobFixture.java`

**🔴 Red Phase**:
- [ ] FileProcessingJobTest.java에 Clock 관련 테스트 추가
  - [ ] `shouldUseClockForCreatedAt()` - forNew() Clock 검증
  - [ ] `shouldUseClockForProcessedAt()` - markAsCompleted() Clock 검증
  - [ ] `shouldCreateJobWithFixedClock()` - 고정 시간 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: FileProcessingJob Clock 의존성 테스트 추가`

**🟢 Green Phase**:
- [ ] FileProcessingJob.java 수정
  - [ ] `private final Clock clock;` 필드 추가
  - [ ] 생성자에 Clock 파라미터 추가
  - [ ] 모든 `LocalDateTime.now()` → `LocalDateTime.now(clock)` 변경
  - [ ] forNew(), of(), reconstitute() 메서드에 Clock 추가
- [ ] 기존 테스트 수정 (Clock.systemUTC())
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: FileProcessingJob Clock 의존성 주입`

**♻️ Refactor Phase**:
- [ ] Clock 관련 중복 코드 제거
- [ ] **커밋**: `struct: Clock 사용 로직 정리` (필요 시)

**🧹 Tidy Phase**:
- [ ] FileProcessingJobFixture 수정 (Clock 사용)
- [ ] **커밋**: `test: FileProcessingJob Clock Fixture 추가`

**✅ 완료 체크**:
- [ ] 3개 신규 테스트 모두 통과
- [ ] Clock 필드 존재 확인
- [ ] LocalDateTime.now() 직접 호출 0개 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 10: FileProcessingJob 불변→가변 패턴 전환

**🎯 목표**: MessageOutbox Cycle 4와 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJob.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJobTest.java`

**🔴 Red Phase**:
- [ ] FileProcessingJobTest.java 수정 (가변 패턴 검증)
  - [ ] `shouldMutateStatusWhenMarkAsProcessing()` - 가변 검증
  - [ ] `shouldMutateStatusWhenMarkAsCompleted()` - 가변 검증
  - [ ] `shouldMutateStatusWhenMarkAsFailed()` - 가변 검증
  - [ ] `shouldNotReturnNewInstanceWhenMarkAsCompleted()` - 동일 객체 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: FileProcessingJob 가변 패턴 테스트 추가`

**🟢 Green Phase**:
- [ ] FileProcessingJob.java 수정
  - [ ] `status`, `retryCount`, `processedAt`, `outputS3Key`, `errorMessage` final 제거
  - [ ] `markAsProcessing()` void 반환 (this 변경)
  - [ ] `markAsCompleted()` void 반환 (this 변경)
  - [ ] `markAsFailed()` void 반환 (this 변경)
  - [ ] `incrementRetryCount()` void 반환 (this 변경)
  - [ ] `withStatus()` private 헬퍼 메서드 제거
- [ ] 기존 테스트 수정 (void 반환 대응)
- [ ] Fixture 수정 (aCompletedJob, aFailedJob 패턴 변경)
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: FileProcessingJob 불변→가변 패턴 전환`

**♻️ Refactor Phase**:
- [ ] 비즈니스 메서드 순서 정리
- [ ] **커밋**: `struct: FileProcessingJob 비즈니스 메서드 정리`

**🧹 Tidy Phase**:
- [ ] FileProcessingJobFixture 수정 (가변 패턴)
- [ ] **커밋**: `test: FileProcessingJobFixture 가변 패턴 적용`

**✅ 완료 체크**:
- [ ] 4개 신규 테스트 모두 통과
- [ ] final 제거 확인
- [ ] 비즈니스 메서드 void 반환 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 11: FileProcessingJob 외래키 VO 추가 (이미 완료)

**🎯 목표**: fileId는 이미 Cycle 8에서 FileId VO로 변경됨

**✅ 완료 상태**:
- [x] fileId는 Cycle 8에서 FileId VO로 변경 완료
- [x] 추가 작업 불필요

**📝 참고**: FileProcessingJob은 외래키가 fileId 하나뿐이며, 이미 Cycle 8에서 VO로 변경되었으므로 별도 Cycle 불필요.

---

### Cycle 12: FileProcessingJob updatedAt + getIdValue() 추가

**🎯 목표**: MessageOutbox Cycle 6과 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJob.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileProcessingJobTest.java`

**🔴 Red Phase**:
- [ ] FileProcessingJobTest.java에 테스트 추가
  - [ ] `shouldHaveUpdatedAtWhenCreated()` - forNew() updatedAt 검증
  - [ ] `shouldUpdateUpdatedAtWhenMarkAsCompleted()` - updatedAt 갱신 검증
  - [ ] `shouldUpdateUpdatedAtWhenMarkAsFailed()` - updatedAt 갱신 검증
  - [ ] `shouldReturnJobIdValueWithoutChaining()` - getJobIdValue() 테스트
  - [ ] `shouldReturnFileIdValueWithoutChaining()` - getFileIdValue() 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: FileProcessingJob updatedAt 및 getIdValue() 테스트 추가`

**🟢 Green Phase**:
- [ ] FileProcessingJob.java 수정
  - [ ] `private LocalDateTime updatedAt;` 필드 추가
  - [ ] 생성자에 updatedAt 파라미터 추가
  - [ ] forNew()에서 updatedAt 설정
  - [ ] 모든 비즈니스 메서드에서 updatedAt 갱신
  - [ ] `getUpdatedAt()` 메서드 추가
  - [ ] `getJobIdValue()` 메서드 추가
  - [ ] `getFileIdValue()` 메서드 추가
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: FileProcessingJob updatedAt 및 getIdValue() 추가`

**♻️ Refactor Phase**:
- [ ] updatedAt 갱신 로직 중복 제거
- [ ] **커밋**: `struct: FileProcessingJob updatedAt 갱신 로직 정리` (필요 시)

**🧹 Tidy Phase**:
- [ ] FileProcessingJobFixture 수정 (updatedAt 설정)
- [ ] **커밋**: `test: FileProcessingJobFixture updatedAt 추가`

**✅ 완료 체크**:
- [ ] 5개 신규 테스트 모두 통과
- [ ] updatedAt 필드 존재 및 갱신 확인
- [ ] getJobIdValue(), getFileIdValue() 메서드 존재 확인
- [ ] **총 커밋 수**: 3-4개
- [ ] **FileProcessingJob 리팩토링 완료** 🎉

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

## Phase 3: File Refactoring (Cycles 13-18)

### Cycle 13: FileId VO 재사용 + UploaderId VO 생성

**🎯 목표**: FileId는 Cycle 7에서 생성 완료, UploaderId만 신규 생성

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/vo/UploaderId.java` (신규)
- `domain/src/test/java/com/ryuqq/fileflow/domain/vo/UploaderIdTest.java` (신규)

**🔴 Red Phase**:
- [ ] UploaderIdTest.java 생성 (MessageOutboxId 패턴 참조)
  - [ ] 6개 테스트 (유효성, null, blank, getValue, equals, hashCode)
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: UploaderId VO 테스트 추가`

**🟢 Green Phase**:
- [ ] UploaderId.java 구현 (MessageOutboxId 패턴)
  - [ ] private final Long value (Long 타입 주의!)
  - [ ] private 생성자
  - [ ] of(Long value) 정적 팩토리
  - [ ] getValue(), equals(), hashCode()
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: UploaderId VO 구현`

**♻️ Refactor Phase**:
- [ ] 중복 코드 제거
- [ ] **커밋**: `struct: UploaderId 검증 로직 개선` (필요 시)

**🧹 Tidy Phase**:
- [ ] UploaderIdFixture.java 생성
- [ ] **커밋**: `test: UploaderId Fixture 추가`

**✅ 완료 체크**:
- [ ] 6개 테스트 모두 통과
- [ ] FileId는 Cycle 7에서 이미 생성 완료 확인
- [ ] Fixture 생성 완료
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 14: File 생성자 private + 3종 팩토리 메서드

**🎯 목표**: MessageOutbox Cycle 2와 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/File.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/FileFixture.java`

**🔴 Red Phase**:
- [ ] FileTest.java에 3종 팩토리 테스트 추가
  - [ ] `shouldCreateNewFileWithForNew()` - forNew() 테스트
  - [ ] `shouldCreateFileWithOf()` - of() 테스트
  - [ ] `shouldThrowExceptionWhenOfWithNullId()` - of() null 검증
  - [ ] `shouldReconstituteFile()` - reconstitute() 테스트
  - [ ] `shouldThrowExceptionWhenReconstituteWithNullId()` - reconstitute() null 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: File 3종 팩토리 메서드 테스트 추가`

**🟢 Green Phase**:
- [ ] File.java 수정
  - [ ] 생성자 `public` → `private`
  - [ ] `fileId` 타입 `String` → `FileId`
  - [ ] `uploaderId` 타입 `Long` → `UploaderId`
  - [ ] `forNew()`, `of()`, `reconstitute()` 메서드 구현
  - [ ] 기존 `create()` 메서드 `@Deprecated`
- [ ] 기존 테스트 수정
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: File 3종 팩토리 메서드 구현`

**♻️ Refactor Phase**:
- [ ] 생성자 검증 로직 개선
- [ ] **커밋**: `struct: File 생성자 검증 로직 개선`

**🧹 Tidy Phase**:
- [ ] FileFixture 수정 (3종 팩토리 패턴)
- [ ] **커밋**: `test: FileFixture 3종 팩토리 패턴 적용`

**✅ 완료 체크**:
- [ ] 5개 신규 테스트 모두 통과
- [ ] 생성자 private 확인
- [ ] FileId, UploaderId 사용 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 15: File Clock 의존성 주입

**🎯 목표**: MessageOutbox Cycle 3과 동일한 패턴 적용

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/File.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileTest.java`
- `domain/src/testFixtures/java/com/ryuqq/fileflow/domain/fixture/FileFixture.java`

**🔴 Red Phase**:
- [ ] FileTest.java에 Clock 관련 테스트 추가
  - [ ] `shouldUseClockForCreatedAt()` - forNew() Clock 검증
  - [ ] `shouldUseClockForUpdatedAt()` - markAsCompleted() Clock 검증
  - [ ] `shouldCreateFileWithFixedClock()` - 고정 시간 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: File Clock 의존성 테스트 추가`

**🟢 Green Phase**:
- [ ] File.java 수정
  - [ ] `private final Clock clock;` 필드 추가
  - [ ] 생성자에 Clock 파라미터 추가
  - [ ] 모든 `LocalDateTime.now()` → `LocalDateTime.now(clock)` 변경 (6곳)
  - [ ] forNew(), of(), reconstitute() 메서드에 Clock 추가
- [ ] 기존 테스트 수정 (Clock.systemUTC())
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: File Clock 의존성 주입`

**♻️ Refactor Phase**:
- [ ] Clock 관련 중복 코드 제거
- [ ] **커밋**: `struct: Clock 사용 로직 정리` (필요 시)

**🧹 Tidy Phase**:
- [ ] FileFixture 수정 (Clock 사용)
- [ ] **커밋**: `test: File Clock Fixture 추가`

**✅ 완료 체크**:
- [ ] 3개 신규 테스트 모두 통과
- [ ] Clock 필드 존재 확인
- [ ] LocalDateTime.now() 직접 호출 0개 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 16: File 불변→가변 패턴 전환

**🎯 목표**: MessageOutbox Cycle 4와 동일한 패턴 적용 (updatedAt 유지)

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/File.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileTest.java`

**🔴 Red Phase**:
- [ ] FileTest.java 수정 (가변 패턴 검증)
  - [ ] `shouldMutateStatusWhenMarkAsUploading()` - 가변 검증
  - [ ] `shouldMutateStatusWhenMarkAsCompleted()` - 가변 검증
  - [ ] `shouldMutateStatusWhenMarkAsFailed()` - 가변 검증
  - [ ] `shouldNotReturnNewInstanceWhenMarkAsCompleted()` - 동일 객체 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: File 가변 패턴 테스트 추가`

**🟢 Green Phase**:
- [ ] File.java 수정
  - [ ] `status`, `retryCount`, `deletedAt`, `updatedAt` final 제거
  - [ ] `markAsUploading()` void 반환 (this 변경)
  - [ ] `markAsCompleted()` void 반환 (this 변경)
  - [ ] `markAsFailed()` void 반환 (this 변경)
  - [ ] `markAsProcessing()` void 반환 (this 변경)
  - [ ] `incrementRetryCount()` void 반환 (this 변경)
  - [ ] `softDelete()` void 반환 (this 변경)
  - [ ] `withStatus()` private 헬퍼 메서드 제거
- [ ] 기존 테스트 수정 (void 반환 대응)
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: File 불변→가변 패턴 전환`

**♻️ Refactor Phase**:
- [ ] 비즈니스 메서드 순서 정리
- [ ] **커밋**: `struct: File 비즈니스 메서드 정리`

**🧹 Tidy Phase**:
- [ ] FileFixture 수정 (가변 패턴)
- [ ] **커밋**: `test: FileFixture 가변 패턴 적용`

**✅ 완료 체크**:
- [ ] 4개 신규 테스트 모두 통과
- [ ] final 제거 확인
- [ ] 비즈니스 메서드 void 반환 확인
- [ ] **총 커밋 수**: 3-4개

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

### Cycle 17: File 외래키 VO 완료 확인

**🎯 목표**: uploaderId는 이미 Cycle 14에서 UploaderId VO로 변경됨

**✅ 완료 상태**:
- [x] uploaderId는 Cycle 14에서 UploaderId VO로 변경 완료
- [x] 추가 작업 불필요

**📝 참고**: File은 외래키가 uploaderId 하나뿐이며, 이미 Cycle 14에서 VO로 변경되었으므로 별도 Cycle 불필요.

---

### Cycle 18: File updatedAt final 제거 + getIdValue() 추가

**🎯 목표**: updatedAt은 이미 존재하나 final이므로 제거 + getIdValue() 추가

**📁 대상 파일**:
- `domain/src/main/java/com/ryuqq/fileflow/domain/aggregate/File.java`
- `domain/src/test/java/com/ryuqq/fileflow/domain/aggregate/FileTest.java`

**🔴 Red Phase**:
- [ ] FileTest.java에 테스트 추가
  - [ ] `shouldUpdateUpdatedAtWhenMarkAsCompleted()` - updatedAt 갱신 검증 (이미 있음, 확인만)
  - [ ] `shouldReturnFileIdValueWithoutChaining()` - getFileIdValue() 테스트
  - [ ] `shouldReturnUploaderIdValueWithoutChaining()` - getUploaderIdValue() 테스트
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: File getIdValue() 테스트 추가`

**🟢 Green Phase**:
- [ ] File.java 수정
  - [ ] `updatedAt` final 제거 (Cycle 16에서 이미 완료, 확인만)
  - [ ] `getFileIdValue()` 메서드 추가: `return fileId.getValue();`
  - [ ] `getUploaderIdValue()` 메서드 추가: `return uploaderId.getValue();`
- [ ] 모든 테스트 통과 확인
- [ ] **커밋**: `feat: File getIdValue() 추가`

**♻️ Refactor Phase**:
- [ ] getIdValue() 메서드 순서 정리
- [ ] **커밋**: `struct: File getter 메서드 정리` (필요 시)

**🧹 Tidy Phase**:
- [ ] FileFixture 최종 검토
- [ ] **커밋**: `test: FileFixture 최종 정리`

**✅ 완료 체크**:
- [ ] 2개 신규 테스트 모두 통과
- [ ] getFileIdValue(), getUploaderIdValue() 메서드 존재 확인
- [ ] **총 커밋 수**: 2-3개
- [ ] **File 리팩토링 완료** 🎉

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

## Phase 4: 최종 검증 및 정리 (Cycle 19)

### Cycle 19: 전체 검증 및 ArchUnit 규칙 추가

**🎯 목표**: 모든 컨벤션 위반사항 해결 검증 + ArchUnit 규칙 추가

**📁 대상 파일**:
- `application/src/test/java/com/ryuqq/fileflow/architecture/DomainAggregateRulesTest.java` (신규)
- 모든 Aggregate 및 VO 파일

**🔴 Red Phase**:
- [ ] DomainAggregateRulesTest.java 생성
  - [ ] `aggregateConstructorsShouldBePrivate()` - 생성자 private 검증
  - [ ] `aggregatesShouldHaveThreeFactoryMethods()` - 3종 팩토리 검증
  - [ ] `aggregatesShouldUseIdValueObjects()` - ID VO 사용 검증
  - [ ] `aggregatesShouldHaveClockField()` - Clock 필드 검증
  - [ ] `aggregatesShouldNotUseLocalDateTimeNow()` - LocalDateTime.now() 금지
  - [ ] `aggregatesShouldHaveUpdatedAtField()` - updatedAt 필드 검증
  - [ ] `aggregatesShouldHaveGetIdValueMethod()` - getIdValue() 메서드 검증
- [ ] 컴파일 에러 확인
- [ ] **커밋**: `test: 도메인 Aggregate ArchUnit 규칙 추가`

**🟢 Green Phase**:
- [ ] 이미 모든 리팩토링 완료되어 있음
- [ ] ArchUnit 규칙 실행 및 통과 확인
- [ ] **커밋**: `feat: 도메인 Aggregate 컨벤션 준수 완료`

**♻️ Refactor Phase**:
- [ ] ArchUnit 규칙 명확화
- [ ] **커밋**: `struct: ArchUnit 규칙 명확화` (필요 시)

**🧹 Tidy Phase**:
- [ ] 모든 `@Deprecated create()` 메서드 제거
- [ ] JavaDoc 최종 점검
- [ ] **커밋**: `chore: 도메인 레이어 최종 정리`

**✅ 완료 체크**:
- [ ] 7개 ArchUnit 규칙 모두 통과
- [ ] 27개 위반사항 모두 해결 확인
- [ ] 전체 테스트 통과 (Domain Layer)
- [ ] **총 커밋 수**: 2-3개
- [ ] **전체 리팩토링 완료** 🎉🎉🎉

**📝 커밋 해시**:
- Red: `________`
- Green: `________`
- Refactor: `________`
- Tidy: `________`

---

## 🎯 진행 현황

### Phase 1: MessageOutbox (6 Cycles)
- [ ] Cycle 1: MessageOutboxId VO 생성
- [ ] Cycle 2: 생성자 private + 3종 팩토리
- [ ] Cycle 3: Clock 의존성 주입
- [ ] Cycle 4: 불변→가변 패턴 전환
- [ ] Cycle 5: AggregateId VO 추가
- [ ] Cycle 6: updatedAt + getIdValue() 추가

### Phase 2: FileProcessingJob (6 Cycles)
- [ ] Cycle 7: FileProcessingJobId + FileId VO 생성
- [ ] Cycle 8: 생성자 private + 3종 팩토리
- [ ] Cycle 9: Clock 의존성 주입
- [ ] Cycle 10: 불변→가변 패턴 전환
- [ ] Cycle 11: 외래키 VO (이미 완료)
- [ ] Cycle 12: updatedAt + getIdValue() 추가

### Phase 3: File (6 Cycles)
- [ ] Cycle 13: UploaderId VO 생성 (FileId 재사용)
- [ ] Cycle 14: 생성자 private + 3종 팩토리
- [ ] Cycle 15: Clock 의존성 주입
- [ ] Cycle 16: 불변→가변 패턴 전환
- [ ] Cycle 17: 외래키 VO (이미 완료)
- [ ] Cycle 18: updatedAt final 제거 + getIdValue() 추가

### Phase 4: 최종 검증 (1 Cycle)
- [ ] Cycle 19: 전체 검증 및 ArchUnit 규칙 추가

---

## 📊 예상 작업량

| Phase | Cycles | 예상 커밋 수 | 예상 시간 |
|-------|--------|-------------|----------|
| Phase 1: MessageOutbox | 6 | 18-24개 | 1-2일 |
| Phase 2: FileProcessingJob | 6 | 18-24개 | 1-2일 |
| Phase 3: File | 6 | 18-24개 | 1-2일 |
| Phase 4: 최종 검증 | 1 | 2-3개 | 0.5일 |
| **총계** | **19** | **56-75개** | **3.5-6.5일** |

---

## 🚀 시작 커맨드

```bash
# Phase 1 시작
/kb:domain:refactor  # Cycle 1부터 순차 실행
```

---

## 📝 참고사항

**TDD Cycle 준수**:
- 모든 Cycle에서 Red → Green → Refactor → Tidy 순서 엄수
- 각 Phase마다 `struct:`, `test:`, `feat:` 커밋 분리
- 작은 커밋 (1-3 파일)

**컨벤션 체크리스트** (Cycle 19에서 최종 검증):
- ✅ 생성자 private 강제
- ✅ 정적 팩토리 3종 (forNew, of, reconstitute)
- ✅ Clock 의존성 주입
- ✅ ID/외래키 VO 사용
- ✅ 불변 ID (final)
- ✅ 가변 필드 (status, retryCount, updatedAt 등)
- ✅ updatedAt 자동 갱신
- ✅ getIdValue() 메서드 (Law of Demeter)
- ✅ ArchUnit 규칙 통과

**롤백 전략**:
- 각 Cycle마다 독립적으로 작업
- 문제 발생 시 해당 Cycle만 롤백 가능
- Git 브랜치 전략: `feature/FILE-001-domain-refactoring`
