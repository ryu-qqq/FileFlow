# FILE-ASSET-PROCESSING Domain Layer TDD Plan

> **Jira Issue**: [KAN-337](https://ryuqqq.atlassian.net/browse/KAN-337)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)
> **Status**: ✅ 완료 (TDD 사이클 12/12 완료, TestFixture는 추후 진행)
> **Started**: 2025-12-02

**PRD**: docs/prd/file-asset-processing.md
**Layer**: Domain
**브랜치**: feature/KAN-337-domain
**예상 소요 시간**: 180분 (12 사이클 × 15분)

---

## 📋 TDD 사이클 체크리스트

### 1️⃣ ContentType 확장 - HTML/XHTML 타입 추가 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [x] `ContentTypeTest.java`에 테스트 메서드 추가
- [x] `shouldRecognizeHtmlMimeType()` 작성 - text/html 인식
- [x] `shouldRecognizeXhtmlMimeType()` 작성 - application/xhtml+xml 인식
- [x] `shouldMapHtmlExtensionToMimeType()` 작성 - .html, .htm 확장자 매핑
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: ContentType HTML/XHTML 타입 인식 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ContentType.java` ALLOWED_MIME_TYPES에 HTML 타입 추가
- [x] EXTENSION_TO_MIME에 html, htm, xhtml 매핑 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ContentType HTML/XHTML 타입 지원 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 중복 상수 정리 (정리할 중복 없음 - 스킵)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: N/A (변경 없음)

---

### 2️⃣ ContentType 확장 - isHtml() 메서드 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [x] `shouldReturnTrueForHtmlContentType()` 작성
- [x] `shouldReturnTrueForXhtmlContentType()` 작성
- [x] `shouldReturnFalseForNonHtmlContentType()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인 (메서드 없음)
- [x] 커밋: `test: ContentType.isHtml() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ContentType.java`에 `isHtml()` 메서드 추가
- [x] text/html 또는 application/xhtml+xml이면 true 반환
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ContentType.isHtml() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] HTML 관련 MIME 타입 상수로 추출 (MIME_TEXT_HTML, MIME_APPLICATION_XHTML)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ContentType HTML MIME 타입 상수화 (Refactor)`

---

### 3️⃣ ContentType 확장 - isExcel() 메서드 (Cycle 3) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldReturnTrueForXlsContentType()` 작성
- [x] `shouldReturnTrueForXlsxContentType()` 작성
- [x] `shouldReturnFalseForNonExcelContentType()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ContentType.isExcel() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ContentType.java`에 `isExcel()` 메서드 추가
- [x] xls, xlsx MIME 타입 체크
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ContentType.isExcel() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Excel MIME 타입 상수로 추출 (MIME_EXCEL_XLS, MIME_EXCEL_XLSX)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ContentType Excel MIME 타입 상수화 (Refactor)`

---

### 4️⃣ UploadCategory 확장 - HTML 카테고리 (Cycle 4) ✅

#### 🔴 Red: 테스트 작성
- [x] `UploadCategoryTest.java`에 테스트 추가
- [x] `shouldHaveHtmlCategory()` 작성
- [x] `shouldReturnTrueForHtmlCategory()` 작성 (isHtml 메서드)
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: UploadCategory HTML 카테고리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `UploadCategory.java`에 `HTML("html", "HTML 문서")` 추가
- [x] `isHtml()` 메서드 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: UploadCategory HTML 카테고리 추가 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 카테고리 순서 검토 (enum 순서 변경은 호환성 이슈로 스킵)
- [x] 테스트 여전히 통과 확인
- [x] 리팩토링 불필요 (코드 이미 깔끔함)

---

### 5️⃣ UploadCategory - requiresImageProcessing() 메서드 (Cycle 5) ✅

#### 🔴 Red: 테스트 작성
- [x] `shouldRequireImageProcessingForBanner()` 작성
- [x] `shouldRequireImageProcessingForProductImage()` 작성
- [x] `shouldRequireImageProcessingForHtml()` 작성
- [x] `shouldNotRequireImageProcessingForExcel()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: UploadCategory.requiresImageProcessing() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `requiresImageProcessing()` 메서드 추가
- [x] BANNER, PRODUCT_IMAGE, HTML이면 true
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: UploadCategory.requiresImageProcessing() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Set 기반 검사로 변경 (IMAGE_PROCESSING_REQUIRED 상수)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: requiresImageProcessing Set 기반으로 변경 (Refactor)`

---

### 6️⃣ FileAssetStatus 확장 - 신규 상태 추가 (Cycle 6) ✅

#### 🔴 Red: 테스트 작성
- [x] `FileAssetStatusTest.java`에 테스트 추가
- [x] `shouldHaveResizedStatus()` 작성
- [x] `shouldHaveN8nProcessingStatus()` 작성
- [x] `shouldHaveN8nCompletedStatus()` 작성
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: FileAssetStatus 신규 상태 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `FileAssetStatus.java`에 RESIZED, N8N_PROCESSING, N8N_COMPLETED 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: FileAssetStatus 신규 상태 추가 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 상태 순서 검토 (ordinal 변경은 DB 호환성 이슈로 스킵)
- [x] 테스트 여전히 통과 확인
- [x] 리팩토링 불필요 (신규 상태는 끝에 추가하여 기존 호환성 유지)

---

### 7️⃣ ImageVariantType Enum (Cycle 7) ✅

#### 🔴 Red: 테스트 작성
- [x] `ImageVariantTypeTest.java` 생성
- [x] `shouldHaveOriginalType()` 작성
- [x] `shouldHaveLargeType()` 작성
- [x] `shouldHaveMediumType()` 작성
- [x] `shouldHaveThumbnailType()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageVariantType enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ImageVariantType.java` 생성 (Enum)
- [x] ORIGINAL, LARGE, MEDIUM, THUMBNAIL 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageVariantType enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ImageVariantType Javadoc 추가 (Refactor)`

---

### 8️⃣ ImageVariant Value Object (Cycle 8) ✅

#### 🔴 Red: 테스트 작성
- [x] `ImageVariantTest.java` 생성
- [x] `shouldCreateImageVariantWithValidData()` 작성
- [x] `shouldThrowWhenTypeIsNull()` 작성
- [x] `shouldThrowWhenSuffixIsNull()` 작성
- [x] `shouldReturnTrueForRequiresResizeWhenNotOriginal()` 작성
- [x] `shouldReturnFalseForRequiresResizeWhenOriginal()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageVariant VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ImageVariant.java` 생성 (Java Record)
- [x] Compact Constructor (검증 로직)
- [x] 정적 팩토리 메서드 `of()` 추가
- [x] 표준 사이즈 상수 (ORIGINAL, LARGE, MEDIUM, THUMBNAIL)
- [x] `requiresResize()` 메서드 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageVariant VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 상수 정의 순서 정리 (Javadoc 및 섹션 구분 추가)
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ImageVariant Javadoc 및 상수 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ImageVariantFixture.java` 생성 (추후 진행)
- [ ] `anOriginalVariant()`, `aLargeVariant()` 등 메서드 추가
- [ ] 테스트를 Fixture 사용하도록 리팩토링
- [ ] 커밋: `test: ImageVariantFixture 정리 (Tidy)`

---

### 9️⃣ ImageFormatType Enum (Cycle 9) ✅

#### 🔴 Red: 테스트 작성
- [x] `ImageFormatTypeTest.java` 생성
- [x] `shouldHaveWebpType()` 작성
- [x] `shouldHaveJpegType()` 작성
- [x] `shouldHavePngType()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageFormatType enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ImageFormatType.java` 생성 (Enum)
- [x] WEBP, JPEG, PNG 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageFormatType enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] Javadoc 추가
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ImageFormatType Javadoc 추가 (Refactor)`

---

### 🔟 ImageFormat Value Object (Cycle 10) ✅

#### 🔴 Red: 테스트 작성
- [x] `ImageFormatTest.java` 생성
- [x] `shouldCreateImageFormatWithValidData()` 작성
- [x] `shouldThrowWhenTypeIsNull()` 작성
- [x] `shouldThrowWhenExtensionIsBlank()` 작성
- [x] `shouldThrowWhenMimeTypeIsBlank()` 작성
- [x] `shouldReturnPngFromPngExtension()` 작성 (fromOriginal 테스트)
- [x] `shouldReturnJpegFromJpgExtension()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ImageFormat VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ImageFormat.java` 생성 (Java Record)
- [x] Compact Constructor (검증 로직)
- [x] 정적 팩토리 메서드 `of()` 추가
- [x] 표준 포맷 상수 (WEBP, JPEG, PNG)
- [x] `fromOriginal()` 메서드 추가 (PNG면 PNG, 그 외 JPEG)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ImageFormat VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 상수 정의 정리
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ImageFormat Javadoc 및 상수 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ImageFormatFixture.java` 생성
- [ ] `aWebpFormat()`, `aJpegFormat()` 등 메서드 추가
- [ ] 테스트를 Fixture 사용하도록 리팩토링
- [ ] 커밋: `test: ImageFormatFixture 정리 (Tidy)`

---

### 1️⃣1️⃣ ProcessedFileAssetId Value Object (Cycle 11) ✅

#### 🔴 Red: 테스트 작성
- [x] `ProcessedFileAssetIdTest.java` 생성
- [x] `shouldGenerateValidUuid()` 작성 (forNew_ShouldCreateNewId)
- [x] `shouldCreateFromValidString()` 작성 (of_WithStringUUID)
- [x] `shouldThrowWhenValueIsNull()` 작성
- [x] `shouldThrowWhenValueIsBlank()` 작성 (null UUID 테스트로 변경)
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ProcessedFileAssetId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ProcessedFileAssetId.java` 생성 (Java Record)
- [x] `forNew()` 정적 메서드 (UUID v7 생성)
- [x] `of(UUID)`, `of(String)` 정적 팩토리 메서드
- [x] `getValue()` 메서드 (value 반환)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ProcessedFileAssetId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 기존 FileAssetId 패턴과 일관성 확인
- [x] Javadoc 추가
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ProcessedFileAssetId Javadoc 추가 (Refactor)`

---

### 1️⃣2️⃣ ProcessedFileAsset Aggregate Root (Cycle 12) ✅

#### 🔴 Red: 테스트 작성
- [x] `ProcessedFileAssetTest.java` 생성
- [x] `shouldCreateProcessedFileAssetWithForNew()` 작성
- [x] `shouldCreateHtmlExtractedImageWithForHtmlExtractedImage()` 작성
- [x] `shouldReconstitute()` 작성
- [x] `shouldReturnTrueForHasParentAssetWhenParentExists()` 작성
- [x] `shouldReturnFalseForHasParentAssetWhenNoParent()` 작성
- [x] `shouldReturnTrueForIsOriginalVariantWhenOriginal()` 작성
- [x] `shouldReturnTrueForIsWebpFormatWhenWebp()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: ProcessedFileAsset Aggregate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ProcessedFileAsset.java` 생성 (Plain Java, Lombok 금지)
- [x] Private 생성자
- [x] `forNew()` 정적 팩토리 메서드
- [x] `forHtmlExtractedImage()` 정적 팩토리 메서드
- [x] `reconstitute()` 정적 팩토리 메서드
- [x] 비즈니스 메서드: `hasParentAsset()`, `isOriginalVariant()`, `isWebpFormat()`
- [x] Getter 메서드 (Lombok 금지)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: ProcessedFileAsset Aggregate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [x] 필드 순서 정리 (식별정보 → 가공정보 → 메타데이터 → S3 → 소유자 → 시간)
- [x] Law of Demeter 준수 확인
- [x] Tell Don't Ask 원칙 준수 확인
- [x] 테스트 여전히 통과 확인
- [x] 커밋: `struct: ProcessedFileAsset Javadoc 추가 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ProcessedFileAssetFixture.java` 생성
- [ ] `aProcessedFileAsset()` Builder 패턴 또는 Object Mother
- [ ] `aHtmlExtractedImage()` 메서드
- [ ] 테스트를 Fixture 사용하도록 리팩토링
- [ ] 커밋: `test: ProcessedFileAssetFixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (`./gradlew :domain:test`)
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수:
  - [ ] Lombok 금지 (Plain Java)
  - [ ] Law of Demeter (Getter 체이닝 금지)
  - [ ] Tell Don't Ask (행위 중심 메서드)
  - [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] TestFixture 모두 정리

---

## 🔗 관련 문서

- PRD: docs/prd/file-asset-processing.md
- Domain Layer 규칙: docs/coding_convention/02-domain-layer/
- VO 규칙: docs/coding_convention/02-domain-layer/vo/
- Aggregate 규칙: docs/coding_convention/02-domain-layer/aggregate/

---

## 📝 참고 사항

### 파일 생성 위치

```
domain/src/main/java/com/fileflow/domain/fileasset/
├── ContentType.java (수정)
├── UploadCategory.java (수정)
├── FileAssetStatus.java (수정)
├── ImageVariantType.java (신규)
├── ImageVariant.java (신규)
├── ImageFormatType.java (신규)
├── ImageFormat.java (신규)
├── ProcessedFileAssetId.java (신규)
└── ProcessedFileAsset.java (신규)

domain/src/test/java/com/fileflow/domain/fileasset/
├── ContentTypeTest.java (수정)
├── UploadCategoryTest.java (수정)
├── FileAssetStatusTest.java (수정)
├── ImageVariantTypeTest.java (신규)
├── ImageVariantTest.java (신규)
├── ImageFormatTypeTest.java (신규)
├── ImageFormatTest.java (신규)
├── ProcessedFileAssetIdTest.java (신규)
└── ProcessedFileAssetTest.java (신규)

domain/src/testFixtures/java/com/fileflow/domain/fileasset/
├── ImageVariantFixture.java (신규)
├── ImageFormatFixture.java (신규)
└── ProcessedFileAssetFixture.java (신규)
```

### 커밋 메시지 규칙

- `test:` - 실패하는 테스트 추가 (Red Phase)
- `feat:` - 테스트 통과 구현 (Green Phase)
- `struct:` - 구조 개선 (Refactor Phase, 동작 변경 없음)

### 다음 Plan

Domain Layer 완료 후 → `FILE-ASSET-PROCESSING-application-plan.md`
