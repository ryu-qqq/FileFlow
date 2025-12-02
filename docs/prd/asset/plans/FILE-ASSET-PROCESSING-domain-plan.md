# FILE-ASSET-PROCESSING Domain Layer TDD Plan

> **Jira Issue**: [KAN-337](https://ryuqqq.atlassian.net/browse/KAN-337)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)
> **Status**: 🔄 진행 중 (In Progress)
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
- [x] `shouldReturnTrueForHtmlContentType()` 작성 (🔄 진행 중)
- [ ] `shouldReturnTrueForXhtmlContentType()` 작성
- [ ] `shouldReturnFalseForNonHtmlContentType()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인 (메서드 없음)
- [ ] 커밋: `test: ContentType.isHtml() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ContentType.java`에 `isHtml()` 메서드 추가
- [ ] text/html 또는 application/xhtml+xml이면 true 반환
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ContentType.isHtml() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] HTML 관련 MIME 타입 상수로 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ContentType HTML MIME 타입 상수화 (Refactor)`

---

### 3️⃣ ContentType 확장 - isExcel() 메서드 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReturnTrueForXlsContentType()` 작성
- [ ] `shouldReturnTrueForXlsxContentType()` 작성
- [ ] `shouldReturnFalseForNonExcelContentType()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ContentType.isExcel() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ContentType.java`에 `isExcel()` 메서드 추가
- [ ] xls, xlsx MIME 타입 체크
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ContentType.isExcel() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Excel MIME 타입 상수로 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ContentType Excel MIME 타입 상수화 (Refactor)`

---

### 4️⃣ UploadCategory 확장 - HTML 카테고리 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `UploadCategoryTest.java`에 테스트 추가
- [ ] `shouldHaveHtmlCategory()` 작성
- [ ] `shouldReturnTrueForHtmlCategory()` 작성 (isHtml 메서드)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UploadCategory HTML 카테고리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadCategory.java`에 `HTML("html", "HTML 문서")` 추가
- [ ] `isHtml()` 메서드 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: UploadCategory HTML 카테고리 추가 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 카테고리 순서 정리 (알파벳순 또는 논리순)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: UploadCategory 정리 (Refactor)`

---

### 5️⃣ UploadCategory - requiresImageProcessing() 메서드 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRequireImageProcessingForBanner()` 작성
- [ ] `shouldRequireImageProcessingForProductImage()` 작성
- [ ] `shouldRequireImageProcessingForHtml()` 작성
- [ ] `shouldNotRequireImageProcessingForExcel()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UploadCategory.requiresImageProcessing() 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `requiresImageProcessing()` 메서드 추가
- [ ] BANNER, PRODUCT_IMAGE, HTML이면 true
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: UploadCategory.requiresImageProcessing() 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Set 기반 검사로 변경 (성능 최적화)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: requiresImageProcessing Set 기반으로 변경 (Refactor)`

---

### 6️⃣ FileAssetStatus 확장 - 신규 상태 추가 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetStatusTest.java`에 테스트 추가
- [ ] `shouldHaveResizedStatus()` 작성
- [ ] `shouldHaveN8nProcessingStatus()` 작성
- [ ] `shouldHaveN8nCompletedStatus()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: FileAssetStatus 신규 상태 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetStatus.java`에 RESIZED, N8N_PROCESSING, N8N_COMPLETED 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetStatus 신규 상태 추가 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상태 순서를 전환 흐름에 맞게 정렬
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: FileAssetStatus 상태 순서 정리 (Refactor)`

---

### 7️⃣ ImageVariantType Enum (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `ImageVariantTypeTest.java` 생성
- [ ] `shouldHaveOriginalType()` 작성
- [ ] `shouldHaveLargeType()` 작성
- [ ] `shouldHaveMediumType()` 작성
- [ ] `shouldHaveThumbnailType()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageVariantType enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageVariantType.java` 생성 (Enum)
- [ ] ORIGINAL, LARGE, MEDIUM, THUMBNAIL 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageVariantType enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ImageVariantType Javadoc 추가 (Refactor)`

---

### 8️⃣ ImageVariant Value Object (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `ImageVariantTest.java` 생성
- [ ] `shouldCreateImageVariantWithValidData()` 작성
- [ ] `shouldThrowWhenTypeIsNull()` 작성
- [ ] `shouldThrowWhenSuffixIsNull()` 작성
- [ ] `shouldReturnTrueForRequiresResizeWhenNotOriginal()` 작성
- [ ] `shouldReturnFalseForRequiresResizeWhenOriginal()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageVariant VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageVariant.java` 생성 (Java Record)
- [ ] Compact Constructor (검증 로직)
- [ ] 정적 팩토리 메서드 `of()` 추가
- [ ] 표준 사이즈 상수 (ORIGINAL, LARGE, MEDIUM, THUMBNAIL)
- [ ] `requiresResize()` 메서드 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageVariant VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상수 정의 순서 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ImageVariant 상수 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ImageVariantFixture.java` 생성
- [ ] `anOriginalVariant()`, `aLargeVariant()` 등 메서드 추가
- [ ] 테스트를 Fixture 사용하도록 리팩토링
- [ ] 커밋: `test: ImageVariantFixture 정리 (Tidy)`

---

### 9️⃣ ImageFormatType Enum (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `ImageFormatTypeTest.java` 생성
- [ ] `shouldHaveWebpType()` 작성
- [ ] `shouldHaveJpegType()` 작성
- [ ] `shouldHavePngType()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageFormatType enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageFormatType.java` 생성 (Enum)
- [ ] WEBP, JPEG, PNG 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageFormatType enum 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ImageFormatType Javadoc 추가 (Refactor)`

---

### 🔟 ImageFormat Value Object (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `ImageFormatTest.java` 생성
- [ ] `shouldCreateImageFormatWithValidData()` 작성
- [ ] `shouldThrowWhenTypeIsNull()` 작성
- [ ] `shouldThrowWhenExtensionIsBlank()` 작성
- [ ] `shouldThrowWhenMimeTypeIsBlank()` 작성
- [ ] `shouldReturnPngFromPngExtension()` 작성 (fromOriginal 테스트)
- [ ] `shouldReturnJpegFromJpgExtension()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageFormat VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageFormat.java` 생성 (Java Record)
- [ ] Compact Constructor (검증 로직)
- [ ] 정적 팩토리 메서드 `of()` 추가
- [ ] 표준 포맷 상수 (WEBP, JPEG, PNG)
- [ ] `fromOriginal()` 메서드 추가 (PNG면 PNG, 그 외 JPEG)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageFormat VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상수 정의 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ImageFormat 상수 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `ImageFormatFixture.java` 생성
- [ ] `aWebpFormat()`, `aJpegFormat()` 등 메서드 추가
- [ ] 테스트를 Fixture 사용하도록 리팩토링
- [ ] 커밋: `test: ImageFormatFixture 정리 (Tidy)`

---

### 1️⃣1️⃣ ProcessedFileAssetId Value Object (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessedFileAssetIdTest.java` 생성
- [ ] `shouldGenerateValidUuid()` 작성
- [ ] `shouldCreateFromValidString()` 작성
- [ ] `shouldThrowWhenValueIsNull()` 작성
- [ ] `shouldThrowWhenValueIsBlank()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessedFileAssetId VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessedFileAssetId.java` 생성 (Java Record)
- [ ] `generate()` 정적 메서드 (UUID 생성)
- [ ] `of(String)` 정적 팩토리 메서드
- [ ] `getValue()` 메서드 (value 반환)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessedFileAssetId VO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 기존 FileAssetId 패턴과 일관성 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ProcessedFileAssetId 패턴 일관성 확보 (Refactor)`

---

### 1️⃣2️⃣ ProcessedFileAsset Aggregate Root (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessedFileAssetTest.java` 생성
- [ ] `shouldCreateProcessedFileAssetWithForNew()` 작성
- [ ] `shouldCreateHtmlExtractedImageWithForHtmlExtractedImage()` 작성
- [ ] `shouldReconstitute()` 작성
- [ ] `shouldReturnTrueForHasParentAssetWhenParentExists()` 작성
- [ ] `shouldReturnFalseForHasParentAssetWhenNoParent()` 작성
- [ ] `shouldReturnTrueForIsOriginalVariantWhenOriginal()` 작성
- [ ] `shouldReturnTrueForIsWebpFormatWhenWebp()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessedFileAsset Aggregate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessedFileAsset.java` 생성 (Plain Java, Lombok 금지)
- [ ] Private 생성자
- [ ] `forNew()` 정적 팩토리 메서드
- [ ] `forHtmlExtractedImage()` 정적 팩토리 메서드
- [ ] `reconstitute()` 정적 팩토리 메서드
- [ ] 비즈니스 메서드: `hasParentAsset()`, `isOriginalVariant()`, `isWebpFormat()`
- [ ] Getter 메서드 (Lombok 금지)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessedFileAsset Aggregate 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 필드 순서 정리 (식별정보 → 가공정보 → 메타데이터 → S3 → 소유자 → 시간)
- [ ] Law of Demeter 준수 확인
- [ ] Tell Don't Ask 원칙 준수 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: ProcessedFileAsset 구조 정리 (Refactor)`

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
