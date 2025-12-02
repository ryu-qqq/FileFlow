# FILE-ASSET-PROCESSING Domain Layer Extended TDD Plan

> **Jira Issue**: [KAN-338](https://ryuqqq.atlassian.net/browse/KAN-338)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)
> **Status**: 🔄 진행 중 (In Progress)
> **Started**: 2025-12-02

**PRD**: docs/prd/file-asset-processing.md
**Layer**: Domain (Extended - StatusHistory, Outbox, Policy)
**브랜치**: feature/KAN-338-domain-extended
**예상 소요 시간**: 150분 (10 사이클 × 15분)

---

## 📋 TDD 사이클 체크리스트

### 1️⃣ FileAssetStatusHistoryId Value Object (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetStatusHistoryIdTest.java` 생성
- [ ] `shouldGenerateValidUuid()` 작성
- [ ] `shouldCreateFromValidString()` 작성
- [ ] `shouldThrowWhenValueIsNull()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileAssetStatusHistoryId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetStatusHistoryId.java` 생성 (Java Record)
- [ ] `generate()`, `of(String)` 정적 메서드
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetStatusHistoryId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 기존 ID 패턴과 일관성 확인
- [ ] 커밋: `struct: FileAssetStatusHistoryId 패턴 일관성 (Refactor)`

---

### 2️⃣ FileAssetStatusHistory Aggregate (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetStatusHistoryTest.java` 생성
- [ ] `shouldCreateWithForNew()` 작성
- [ ] `shouldCreateWithForSystemChange()` 작성
- [ ] `shouldCreateWithForN8nChange()` 작성
- [ ] `shouldReconstitute()` 작성
- [ ] `shouldReturnTrueForIsFailure()` 작성
- [ ] `shouldReturnTrueForIsInitialCreation()` 작성
- [ ] `shouldReturnTrueForExceedsSla()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileAssetStatusHistory Aggregate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetStatusHistory.java` 생성 (Plain Java)
- [ ] Private 생성자
- [ ] `forNew()` - 일반 생성
- [ ] `forSystemChange()` - 시스템 변경용 편의 메서드
- [ ] `forN8nChange()` - n8n 변경용 편의 메서드
- [ ] `reconstitute()` - DB 복원용
- [ ] 비즈니스 메서드: `isFailure()`, `isInitialCreation()`, `exceedsSla()`
- [ ] Getter 메서드 (Lombok 금지)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetStatusHistory Aggregate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 필드 순서 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: FileAssetStatusHistory 구조 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileAssetStatusHistoryFixture.java` 생성
- [ ] `aStatusHistory()`, `aFailedHistory()` 메서드 추가
- [ ] 커밋: `test: FileAssetStatusHistoryFixture 정리 (Tidy)`

---

### 3️⃣ FileProcessingOutboxId Value Object (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingOutboxIdTest.java` 생성
- [ ] `shouldGenerateValidUuid()` 작성
- [ ] `shouldCreateFromValidString()` 작성
- [ ] `shouldThrowWhenValueIsNull()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileProcessingOutboxId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingOutboxId.java` 생성 (Java Record)
- [ ] `generate()`, `of(String)` 정적 메서드
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileProcessingOutboxId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ID 패턴 일관성 확인
- [ ] 커밋: `struct: FileProcessingOutboxId 패턴 일관성 (Refactor)`

---

### 4️⃣ OutboxStatus Enum (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `OutboxStatusTest.java` 생성
- [ ] `shouldHavePendingStatus()` 작성
- [ ] `shouldHaveSentStatus()` 작성
- [ ] `shouldHaveFailedStatus()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: OutboxStatus enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxStatus.java` 생성 (Enum)
- [ ] PENDING, SENT, FAILED 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxStatus enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `struct: OutboxStatus Javadoc 추가 (Refactor)`

---

### 5️⃣ FileProcessingOutbox Aggregate - 기본 생성 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingOutboxTest.java` 생성
- [ ] `shouldCreateWithForProcessRequest()` 작성
- [ ] `shouldCreateWithForStatusChange()` 작성
- [ ] `shouldCreateWithForRetryRequest()` 작성
- [ ] `shouldReconstitute()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileProcessingOutbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingOutbox.java` 생성 (Plain Java)
- [ ] Private 생성자
- [ ] `forProcessRequest()` - 가공 요청용
- [ ] `forStatusChange()` - 상태 변경 알림용
- [ ] `forRetryRequest()` - 재처리 요청용
- [ ] `reconstitute()` - DB 복원용
- [ ] Getter 메서드 (Lombok 금지)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileProcessingOutbox 기본 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 필드 순서 정리
- [ ] 커밋: `struct: FileProcessingOutbox 구조 정리 (Refactor)`

---

### 6️⃣ FileProcessingOutbox - 상태 변경 메서드 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldMarkAsSent()` 작성
- [ ] `shouldMarkAsFailed()` 작성
- [ ] `shouldIncrementRetryCountOnFailed()` 작성
- [ ] `shouldReturnTrueForCanRetryWhenPendingAndBelowMax()` 작성
- [ ] `shouldReturnFalseForCanRetryWhenExhausted()` 작성
- [ ] `shouldReturnTrueForIsExhausted()` 작성
- [ ] `shouldReturnTrueForIsSent()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: FileProcessingOutbox 상태 변경 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `markAsSent()` 메서드 추가
- [ ] `markAsFailed(String errorMessage)` 메서드 추가
- [ ] `canRetry()` 메서드 추가
- [ ] `isExhausted()` 메서드 추가
- [ ] `isSent()` 메서드 추가
- [ ] MAX_RETRY_COUNT 상수 (3)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileProcessingOutbox 상태 변경 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Tell Don't Ask 원칙 검증
- [ ] 커밋: `struct: FileProcessingOutbox Tell Don't Ask 적용 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingOutboxFixture.java` 생성
- [ ] `aPendingOutbox()`, `aSentOutbox()`, `aFailedOutbox()` 메서드
- [ ] 커밋: `test: FileProcessingOutboxFixture 정리 (Tidy)`

---

### 7️⃣ ImageProcessingPolicy Domain Service - shouldProcess (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `ImageProcessingPolicyTest.java` 생성
- [ ] `shouldReturnTrueForImageContentType()` 작성
- [ ] `shouldReturnFalseForNonImageContentType()` 작성
- [ ] `shouldReturnTrueForBannerCategory()` 작성
- [ ] `shouldReturnTrueForProductImageCategory()` 작성
- [ ] `shouldReturnTrueForHtmlCategory()` 작성
- [ ] `shouldReturnFalseForExcelCategory()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageProcessingPolicy.shouldProcess 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageProcessingPolicy.java` 생성 (Domain Service)
- [ ] `shouldProcess(ContentType)` 메서드
- [ ] `shouldProcess(UploadCategory)` 메서드
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageProcessingPolicy.shouldProcess 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 메서드 오버로딩 정리
- [ ] 커밋: `struct: ImageProcessingPolicy 메서드 정리 (Refactor)`

---

### 8️⃣ ImageProcessingPolicy - getVariantsToGenerate (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturnLargeMediumThumbnailVariants()` 작성
- [ ] `shouldNotIncludeOriginalInVariants()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageProcessingPolicy.getVariantsToGenerate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `getVariantsToGenerate()` 메서드 추가
- [ ] LARGE, MEDIUM, THUMBNAIL 반환 (ORIGINAL 제외)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageProcessingPolicy.getVariantsToGenerate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 불변 List 반환 확인
- [ ] 커밋: `struct: getVariantsToGenerate 불변 List 반환 (Refactor)`

---

### 9️⃣ ImageProcessingPolicy - getFormatsToGenerate (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturnWebpAndJpegForJpgExtension()` 작성
- [ ] `shouldReturnWebpAndPngForPngExtension()` 작성
- [ ] `shouldAlwaysIncludeWebpFormat()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageProcessingPolicy.getFormatsToGenerate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `getFormatsToGenerate(String originalExtension)` 메서드 추가
- [ ] WebP + 원본 폴백 (JPEG 또는 PNG) 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageProcessingPolicy.getFormatsToGenerate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ImageFormat.fromOriginal 활용
- [ ] 커밋: `struct: getFormatsToGenerate ImageFormat 활용 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ImageProcessingPolicyFixture.java` 또는 직접 인스턴스 사용
- [ ] 커밋: `test: ImageProcessingPolicy 테스트 정리 (Tidy)`

---

### 🔟 FileAsset Aggregate 확장 - 상태 전환 메서드 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetTest.java`에 테스트 추가
- [ ] `shouldValidateCanProcessWhenPending()` 작성
- [ ] `shouldThrowWhenValidateCanProcessButNotPending()` 작성
- [ ] `shouldStartProcessing()` 작성 - PENDING → PROCESSING
- [ ] `shouldCompleteProcessing()` 작성 - PROCESSING → RESIZED
- [ ] `shouldRequestProcessing()` 작성 - UPLOADED → PENDING
- [ ] 테스트 실행 → 컴파일 에러/실패 확인
- [ ] 커밋: `test: FileAsset 상태 전환 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAsset.java`에 메서드 추가
- [ ] `validateCanProcess()` - PENDING 상태 검증
- [ ] `startProcessing()` - PROCESSING으로 전환
- [ ] `completeProcessing()` - RESIZED로 전환
- [ ] `requestProcessing()` - PENDING으로 전환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAsset 상태 전환 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상태 전환 검증 로직 정리
- [ ] 커밋: `struct: FileAsset 상태 전환 로직 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileAssetFixture.java` 업데이트 (신규 상태 지원)
- [ ] 커밋: `test: FileAssetFixture 상태 지원 업데이트 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (`./gradlew :domain:test`)
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수:
  - [ ] Lombok 금지 (Plain Java)
  - [ ] Law of Demeter (Getter 체이닝 금지)
  - [ ] Tell Don't Ask (행위 중심 메서드)
- [ ] TestFixture 모두 정리

---

## 🔗 관련 문서

- PRD: docs/prd/file-asset-processing.md
- Domain Layer 규칙: docs/coding_convention/02-domain-layer/
- 이전 Plan: FILE-ASSET-PROCESSING-domain-plan.md

---

## 📝 파일 생성 위치

```
domain/src/main/java/com/fileflow/domain/fileasset/
├── FileAssetStatusHistoryId.java (신규)
├── FileAssetStatusHistory.java (신규)
├── FileProcessingOutboxId.java (신규)
├── OutboxStatus.java (신규)
├── FileProcessingOutbox.java (신규)
├── ImageProcessingPolicy.java (신규)
└── FileAsset.java (수정 - 상태 전환 메서드 추가)

domain/src/test/java/com/fileflow/domain/fileasset/
├── FileAssetStatusHistoryIdTest.java (신규)
├── FileAssetStatusHistoryTest.java (신규)
├── FileProcessingOutboxIdTest.java (신규)
├── OutboxStatusTest.java (신규)
├── FileProcessingOutboxTest.java (신규)
├── ImageProcessingPolicyTest.java (신규)
└── FileAssetTest.java (수정)

domain/src/testFixtures/java/com/fileflow/domain/fileasset/
├── FileAssetStatusHistoryFixture.java (신규)
├── FileProcessingOutboxFixture.java (신규)
└── FileAssetFixture.java (수정)
```

---

## 📝 다음 Plan

Domain Layer Extended 완료 후 → `FILE-ASSET-PROCESSING-application-plan.md`
