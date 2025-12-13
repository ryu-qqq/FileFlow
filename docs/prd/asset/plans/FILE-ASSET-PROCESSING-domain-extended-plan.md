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

### 1️⃣ FileAssetStatusHistoryId Value Object (Cycle 1) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileAssetStatusHistoryIdTest.java` 생성
- [x] `shouldGenerateValidUuid()` 작성
- [x] `shouldCreateFromValidString()` 작성
- [x] `shouldThrowWhenValueIsNull()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileAssetStatusHistoryId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileAssetStatusHistoryId.java` 생성 (Java Record)
- [x] `forNew()`, `of(String)` 정적 메서드
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileAssetStatusHistoryId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 기존 ID 패턴과 일관성 확인 (변경 불필요 - 패턴 동일)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 2️⃣ FileAssetStatusHistory Aggregate (Cycle 2) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileAssetStatusHistoryTest.java` 생성
- [x] `shouldCreateWithForNew()` 작성
- [x] `shouldCreateWithForSystemChange()` 작성
- [x] `shouldCreateWithForN8nChange()` 작성
- [x] `shouldReconstitute()` 작성
- [x] `shouldReturnTrueForIsFailure()` 작성
- [x] `shouldReturnTrueForIsInitialCreation()` 작성
- [x] `shouldReturnTrueForExceedsSla()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileAssetStatusHistory Aggregate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileAssetStatusHistory.java` 생성 (Plain Java)
- [x] Private 생성자
- [x] `forNew()` - 일반 생성
- [x] `forSystemChange()` - 시스템 변경용 편의 메서드
- [x] `forN8nChange()` - n8n 변경용 편의 메서드
- [x] `reconstitute()` - DB 복원용
- [x] 비즈니스 메서드: `isFailure()`, `isInitialCreation()`, `exceedsSla()`
- [x] Getter 메서드 (Lombok 금지)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileAssetStatusHistory Aggregate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 필드 순서 정리 (변경 불필요 - 이미 정리됨)
- [x] 테스트 여전히 통과 확인
- [x] 리팩토링 불필요 (struct: 커밋 생략)

#### 🧹 Tidy: TestFixture 정리
- [x] `FileAssetStatusHistoryFixture.java` 생성
- [x] `aStatusHistory()`, `aFailedHistory()` 메서드 추가
- [x] 커밋: `test: FileAssetStatusHistoryFixture 추가 (Tidy)`

---

### 3️⃣ FileProcessingOutboxId Value Object (Cycle 3) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileProcessingOutboxIdTest.java` 생성
- [x] `shouldGenerateValidUuid()` 작성
- [x] `shouldCreateFromValidString()` 작성
- [x] `shouldThrowWhenValueIsNull()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileProcessingOutboxId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileProcessingOutboxId.java` 생성 (Java Record)
- [x] `forNew()`, `of(String)` 정적 메서드
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileProcessingOutboxId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ID 패턴 일관성 확인 (변경 불필요 - 패턴 동일)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 4️⃣ OutboxStatus Enum (Cycle 4) ✅

#### 🔴 Red: 테스트 작성
- [x] `OutboxStatusTest.java` 생성
- [x] `shouldHavePendingStatus()` 작성
- [x] `shouldHaveSentStatus()` 작성
- [x] `shouldHaveFailedStatus()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: OutboxStatus enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `OutboxStatus.java` 생성 (Enum)
- [x] PENDING, SENT, FAILED 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: OutboxStatus enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가 (Green 단계에서 완료)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 5️⃣ FileProcessingOutbox Aggregate - 기본 생성 (Cycle 5) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileProcessingOutboxTest.java` 생성
- [x] `shouldCreateWithForProcessRequest()` 작성
- [x] `shouldCreateWithForStatusChange()` 작성
- [x] `shouldCreateWithForRetryRequest()` 작성
- [x] `shouldReconstitute()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileProcessingOutbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileProcessingOutbox.java` 생성 (Plain Java)
- [x] Private 생성자
- [x] `forProcessRequest()` - 가공 요청용
- [x] `forStatusChange()` - 상태 변경 알림용
- [x] `forRetryRequest()` - 재처리 요청용
- [x] `reconstitute()` - DB 복원용
- [x] Getter 메서드 (Lombok 금지)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileProcessingOutbox 기본 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 필드 순서 정리 (변경 불필요 - 이미 정리됨)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 6️⃣ FileProcessingOutbox - 상태 변경 메서드 (Cycle 6) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldMarkAsSent()` 작성
- [x] `shouldMarkAsFailed()` 작성
- [x] `shouldIncrementRetryCountOnFailed()` 작성
- [x] `shouldReturnTrueForCanRetryWhenPendingAndBelowMax()` 작성
- [x] `shouldReturnFalseForCanRetryWhenExhausted()` 작성
- [x] `shouldReturnTrueForIsExhausted()` 작성
- [x] `shouldReturnTrueForIsSent()` 작성
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: FileProcessingOutbox 상태 변경 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `markAsSent()` 메서드 추가
- [x] `markAsFailed(String errorMessage)` 메서드 추가
- [x] `canRetry()` 메서드 추가
- [x] `isExhausted()` 메서드 추가
- [x] `isSent()` 메서드 추가
- [x] MAX_RETRY_COUNT 상수 (3)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileProcessingOutbox 상태 변경 메서드 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Tell Don't Ask 원칙 검증 (이미 준수 - 변경 불필요)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

#### 🧹 Tidy: TestFixture 정리
- [x] `FileProcessingOutboxFixture.java` 생성
- [x] `aPendingOutbox()`, `aSentOutbox()`, `aFailedOutbox()` 메서드
- [x] 커밋: `test: FileProcessingOutboxFixture 정리 (Tidy)`

---

### 7️⃣ ImageProcessingPolicy Domain Service - shouldProcess (Cycle 7) ✅

#### 🔴 Red: 테스트 작성
- [x] `ImageProcessingPolicyTest.java` 생성
- [x] `shouldReturnTrueForImageContentType()` 작성
- [x] `shouldReturnFalseForNonImageContentType()` 작성
- [x] `shouldReturnTrueForBannerCategory()` 작성
- [x] `shouldReturnTrueForProductImageCategory()` 작성
- [x] `shouldReturnTrueForHtmlCategory()` 작성
- [x] `shouldReturnFalseForExcelCategory()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageProcessingPolicy.shouldProcess 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ImageProcessingPolicy.java` 생성 (Domain Service)
- [x] `shouldProcess(ContentType)` 메서드
- [x] `shouldProcess(UploadCategory)` 메서드
- [x] `shouldProcess(ContentType, UploadCategory)` 복합 조건 메서드 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageProcessingPolicy.shouldProcess 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 메서드 오버로딩 정리 (변경 불필요 - 이미 정리됨)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 8️⃣ ImageProcessingPolicy - getVariantsToGenerate (Cycle 8) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldReturnLargeMediumThumbnailVariants()` 작성
- [x] `shouldNotIncludeOriginalInVariants()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageProcessingPolicy.getVariantsToGenerate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `getVariantsToGenerate()` 메서드 추가
- [x] LARGE, MEDIUM, THUMBNAIL 반환 (ORIGINAL 제외)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageProcessingPolicy.getVariantsToGenerate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 불변 List 반환 확인 (List.of()는 이미 불변 - 변경 불필요)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

---

### 9️⃣ ImageProcessingPolicy - getFormatsToGenerate (Cycle 9) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldReturnWebpAndJpegForJpgExtension()` 작성
- [x] `shouldReturnWebpAndPngForPngExtension()` 작성
- [x] `shouldAlwaysIncludeWebpAsFirstFormat()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageProcessingPolicy.getFormatsToGenerate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `getFormatsToGenerate(String originalExtension)` 메서드 추가
- [x] WebP + 원본 폴백 (JPEG 또는 PNG) 반환
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageProcessingPolicy.getFormatsToGenerate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] ImageFormat.fromOriginal 활용 (Green 단계에서 이미 적용)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

#### 🧹 Tidy: TestFixture 정리
- [x] ImageProcessingPolicy는 stateless Domain Service로 직접 인스턴스 사용
- [x] Fixture 불필요 (Tidy 커밋 생략)

---

### 🔟 FileAsset Aggregate 확장 - 상태 전환 메서드 (Cycle 10) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileAssetTest.java`에 테스트 추가
- [x] `shouldValidateCanProcessWhenPending()` 작성
- [x] `shouldThrowWhenValidateCanProcessButNotPending()` 작성
- [x] `shouldStartProcessing()` - 이미 존재 (기존 테스트 유지)
- [x] `shouldCompleteProcessing()` - 이미 존재 (기존 테스트 유지)
- [x] `shouldRequestProcessing()` - UPLOADED 상태 없음, 제외
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: FileAsset.validateCanProcess 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileAsset.java`에 메서드 추가
- [x] `validateCanProcess()` - PENDING 상태 검증
- [x] `startProcessing()` - validateCanProcess() 재사용하도록 수정
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileAsset.validateCanProcess 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] startProcessing()에서 validateCanProcess() 재사용 (Green에서 완료)
- [x] 리팩토링 불필요 (struct: 커밋 생략)

#### 🧹 Tidy: TestFixture 정리
- [x] FileAssetFixture - 기존 상태 지원 충분
- [x] Fixture 업데이트 불필요 (Tidy 커밋 생략)

---

## ✅ 완료 조건

- [x] 모든 TDD 사이클 완료 (체크박스 모두 ✅)
- [x] 모든 테스트 통과 (`./gradlew :domain:test`)
- [x] ArchUnit 테스트 통과
- [x] Zero-Tolerance 규칙 준수:
  - [x] Lombok 금지 (Plain Java)
  - [x] Law of Demeter (Getter 체이닝 금지)
  - [x] Tell Don't Ask (행위 중심 메서드)
- [x] TestFixture 모두 정리

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
