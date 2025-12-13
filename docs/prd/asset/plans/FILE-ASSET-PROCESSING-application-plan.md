# FILE-ASSET-PROCESSING Application Layer TDD Plan

> **Jira Issue**: [KAN-339](https://ryuqqq.atlassian.net/browse/KAN-339)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)

**PRD**: docs/prd/file-asset-processing.md
**Layer**: Application
**브랜치**: feature/file-asset-processing-application
**예상 소요 시간**: 270분 (18 사이클 × 15분)

---

## 📋 TDD 사이클 체크리스트

### 1️⃣ ImageProcessingPort (Out Port) 정의 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `ImageProcessingPortTest.java` 생성 (인터페이스 정의 확인용)
- [ ] Port 인터페이스 메서드 시그니처 테스트
- [ ] `ImageProcessingResult`, `ImageMetadata` record 존재 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ImageProcessingPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ImageProcessingPort.java` 생성 (interface)
- [ ] `resize(byte[], ImageVariant, ImageFormat)` 메서드 정의
- [ ] `extractMetadata(byte[])` 메서드 정의
- [ ] `ImageProcessingResult` record 정의
- [ ] `ImageMetadata` record 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ImageProcessingPort 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `struct: ImageProcessingPort Javadoc 추가 (Refactor)`

---

### 2️⃣ HtmlProcessingPort (Out Port) 정의 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `HtmlProcessingPortTest.java` 생성
- [ ] Port 인터페이스 메서드 시그니처 확인
- [ ] `ExtractedImage`, `ImageSourceType` 존재 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: HtmlProcessingPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `HtmlProcessingPort.java` 생성 (interface)
- [ ] `extractImages(String)` 메서드 정의
- [ ] `replaceImageUrls(String, Map<String, String>)` 메서드 정의
- [ ] `ExtractedImage` record 정의
- [ ] `ImageSourceType` enum 정의 (IMG_SRC, CSS_BACKGROUND, INLINE_STYLE)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: HtmlProcessingPort 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `struct: HtmlProcessingPort Javadoc 추가 (Refactor)`

---

### 3️⃣ SqsMessagePort (Out Port) 정의 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `SqsMessagePortTest.java` 생성
- [ ] `sendMessage(String)` 메서드 시그니처 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SqsMessagePort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SqsMessagePort.java` 생성 (interface)
- [ ] `sendMessage(String payload)` 메서드 정의 - 반환값 String (메시지 ID)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SqsMessagePort 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `struct: SqsMessagePort Javadoc 추가 (Refactor)`

---

### 4️⃣ ProcessedFileAssetPersistencePort (Out Port) 정의 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] Port 인터페이스 메서드 시그니처 확인
- [ ] `save(ProcessedFileAsset)` 메서드 확인
- [ ] `saveAll(List<ProcessedFileAsset>)` 메서드 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessedFileAssetPersistencePort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessedFileAssetPersistencePort.java` 생성 (interface)
- [ ] `save()`, `saveAll()` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessedFileAssetPersistencePort 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 기존 Port 네이밍 패턴과 일관성 확인
- [ ] 커밋: `struct: ProcessedFileAssetPersistencePort 패턴 일관성 (Refactor)`

---

### 5️⃣ ProcessedFileAssetQueryPort (Out Port) 정의 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `findByOriginalAssetId(String)` 메서드 확인
- [ ] `findByParentAssetId(String)` 메서드 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessedFileAssetQueryPort 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessedFileAssetQueryPort.java` 생성 (interface)
- [ ] 조회 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessedFileAssetQueryPort 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Query Port 네이밍 일관성 확인
- [ ] 커밋: `struct: ProcessedFileAssetQueryPort 네이밍 일관성 (Refactor)`

---

### 6️⃣ FileProcessingOutbox Persistence/Query Port 정의 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingOutboxPersistencePort` - save, saveAll 확인
- [ ] `FileProcessingOutboxQueryPort` - findPendingEvents, findRetryableFailedEvents 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileProcessingOutbox Port 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingOutboxPersistencePort.java` 생성
- [ ] `FileProcessingOutboxQueryPort.java` 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileProcessingOutbox Port 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Port 패턴 일관성 확인
- [ ] 커밋: `struct: FileProcessingOutbox Port 패턴 정리 (Refactor)`

---

### 7️⃣ FileAssetStatusHistory Persistence/Query Port 정의 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetStatusHistoryPersistencePort` - save 확인
- [ ] `FileAssetStatusHistoryQueryPort` - findByFileAssetId, findLatestByFileAssetId, findExceedingSla 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileAssetStatusHistory Port 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetStatusHistoryPersistencePort.java` 생성
- [ ] `FileAssetStatusHistoryQueryPort.java` 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetStatusHistory Port 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Port 정리
- [ ] 커밋: `struct: FileAssetStatusHistory Port 정리 (Refactor)`

---

### 8️⃣ ProcessedFileAssetTransactionManager (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessedFileAssetTransactionManagerTest.java` 생성
- [ ] `shouldSaveProcessedFileAsset()` 작성 (Mock PersistencePort)
- [ ] `shouldSaveAllProcessedFileAssets()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessedFileAssetTransactionManager 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessedFileAssetTransactionManager.java` 생성
- [ ] `@Component`, `@Transactional` 어노테이션
- [ ] ProcessedFileAssetPersistencePort 의존성 주입
- [ ] `save()`, `saveAll()` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessedFileAssetTransactionManager 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 단일 PersistencePort만 의존하는지 확인
- [ ] 커밋: `struct: ProcessedFileAssetTransactionManager 의존성 정리 (Refactor)`

---

### 9️⃣ FileProcessingOutboxTransactionManager (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingOutboxTransactionManagerTest.java` 생성
- [ ] `shouldSaveOutbox()` 작성
- [ ] `shouldSaveAllOutbox()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileProcessingOutboxTransactionManager 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileProcessingOutboxTransactionManager.java` 생성
- [ ] `@Component`, `@Transactional` 어노테이션
- [ ] `save()`, `saveAll()` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileProcessingOutboxTransactionManager 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Manager 패턴 일관성 확인
- [ ] 커밋: `struct: FileProcessingOutboxTransactionManager 패턴 정리 (Refactor)`

---

### 🔟 FileAssetStatusHistoryTransactionManager (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetStatusHistoryTransactionManagerTest.java` 생성
- [ ] `shouldSaveHistory()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileAssetStatusHistoryTransactionManager 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetStatusHistoryTransactionManager.java` 생성
- [ ] `save()` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetStatusHistoryTransactionManager 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileAssetStatusHistoryTransactionManager 정리 (Refactor)`

---

### 1️⃣1️⃣ FileAssetProcessingFacade - requestProcessingWithOutbox (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `FileAssetProcessingFacadeTest.java` 생성
- [ ] `shouldRequestProcessingWithOutbox()` 작성
- [ ] FileAsset 저장 + StatusHistory 저장 + Outbox 저장 순서 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: FileAssetProcessingFacade.requestProcessingWithOutbox 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileAssetProcessingFacade.java` 생성
- [ ] `@Service`, `@Transactional` 어노테이션
- [ ] 세 개의 TransactionManager 의존성 주입
- [ ] `requestProcessingWithOutbox()` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetProcessingFacade.requestProcessingWithOutbox 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Facade 패턴 원칙 검증 (여러 Manager 조합)
- [ ] 커밋: `struct: FileAssetProcessingFacade 패턴 검증 (Refactor)`

---

### 1️⃣2️⃣ FileAssetProcessingFacade - updateStatusWithHistory (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUpdateStatusWithHistory()` 작성
- [ ] FileAsset 저장 + StatusHistory 저장 검증
- [ ] 테스트 실행 → 컴파일 에러/실패 확인
- [ ] 커밋: `test: FileAssetProcessingFacade.updateStatusWithHistory 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `updateStatusWithHistory()` 메서드 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: FileAssetProcessingFacade.updateStatusWithHistory 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 메서드 파라미터 정리
- [ ] 커밋: `struct: updateStatusWithHistory 파라미터 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Facade 테스트용 Mock 정리
- [ ] 커밋: `test: FileAssetProcessingFacade 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ ProcessFileAssetCommand DTO (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessFileAssetCommandTest.java` 생성
- [ ] `shouldCreateWithValidFileAssetId()` 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessFileAssetCommand DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessFileAssetCommand.java` 생성 (Java Record)
- [ ] `fileAssetId` 필드
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessFileAssetCommand DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO 패키지 위치 확인 (dto/command/)
- [ ] 커밋: `struct: ProcessFileAssetCommand 패키지 정리 (Refactor)`

---

### 1️⃣4️⃣ ProcessFileAssetResponse DTO (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessFileAssetResponseTest.java` 생성
- [ ] Response 필드 존재 확인
- [ ] ProcessedFileInfo 내부 record 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessFileAssetResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessFileAssetResponse.java` 생성 (Java Record)
- [ ] `ProcessedFileInfo.java` 생성 (Java Record)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessFileAssetResponse DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO 패키지 위치 확인 (dto/response/)
- [ ] 커밋: `struct: ProcessFileAssetResponse 패키지 정리 (Refactor)`

---

### 1️⃣5️⃣ ProcessFileAssetUseCase (In Port) 정의 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `execute(ProcessFileAssetCommand)` 메서드 시그니처 확인
- [ ] 반환 타입 `ProcessFileAssetResponse` 확인
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessFileAssetUseCase 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessFileAssetUseCase.java` 생성 (interface)
- [ ] `execute()` 메서드 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessFileAssetUseCase 인터페이스 정의 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] UseCase 패턴 확인 (port/in/command/)
- [ ] 커밋: `struct: ProcessFileAssetUseCase 패턴 확인 (Refactor)`

---

### 1️⃣6️⃣ ProcessFileAssetService - 기본 흐름 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `ProcessFileAssetServiceTest.java` 생성
- [ ] `shouldProcessFileAssetSuccessfully()` 작성
- [ ] Mock 준비: FileAssetQueryPort, ProcessingFacade, ImageProcessingPort, S3StoragePort
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ProcessFileAssetService 기본 흐름 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ProcessFileAssetService.java` 생성
- [ ] `@Service` 어노테이션 (⚠️ @Transactional 금지 - UseCase에)
- [ ] 의존성 주입 (Facade, QueryPort, ImageProcessingPort, S3StoragePort, Assembler)
- [ ] `execute()` 메서드 기본 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ProcessFileAssetService 기본 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Transaction 경계 확인 (외부 I/O는 트랜잭션 밖)
- [ ] 커밋: `struct: ProcessFileAssetService 트랜잭션 경계 확인 (Refactor)`

---

### 1️⃣7️⃣ UpdateFileAssetStatusUseCase 및 Service (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateFileAssetStatusCommand.java` DTO 테스트
- [ ] `UpdateFileAssetStatusResponse.java` DTO 테스트
- [ ] `UpdateFileAssetStatusUseCase.java` 인터페이스 테스트
- [ ] `UpdateFileAssetStatusServiceTest.java` 테스트
- [ ] 상태 전환 검증 (RESIZED → N8N_PROCESSING 등)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UpdateFileAssetStatus UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Command, Response DTO 생성
- [ ] UseCase 인터페이스 생성
- [ ] Service 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: UpdateFileAssetStatus UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 상태 전환 검증 로직 정리
- [ ] 커밋: `struct: UpdateFileAssetStatus 상태 전환 검증 정리 (Refactor)`

---

### 1️⃣8️⃣ ListFileAssetsForN8nUseCase 및 Service (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `ListFileAssetsForN8nQuery.java` DTO 테스트
- [ ] `FileAssetForN8nResponse.java` DTO 테스트
- [ ] `ListFileAssetsForN8nUseCase.java` 인터페이스 테스트
- [ ] `ListFileAssetsForN8nServiceTest.java` 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: ListFileAssetsForN8n UseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Query, Response DTO 생성
- [ ] UseCase 인터페이스 생성 (port/in/query/)
- [ ] Service 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: ListFileAssetsForN8n UseCase 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Query UseCase 패턴 확인
- [ ] 커밋: `struct: ListFileAssetsForN8n 패턴 확인 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Command/Query/Response Fixture 정리
- [ ] 커밋: `test: Application Layer DTO Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (`./gradlew :application:test`)
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수:
  - [ ] UseCase 인터페이스/구현체에 @Transactional 금지
  - [ ] TransactionManager만 @Transactional 가짐
  - [ ] 외부 I/O (S3, 이미지 가공)는 트랜잭션 밖에서 실행
  - [ ] Command/Query 분리 (CQRS)
  - [ ] Assembler 패턴 사용
- [ ] TestFixture 모두 정리

---

## 🔗 관련 문서

- PRD: docs/prd/file-asset-processing.md
- Application Layer 규칙: docs/coding_convention/03-application-layer/
- Port 규칙: docs/coding_convention/03-application-layer/port/
- Manager 규칙: docs/coding_convention/03-application-layer/manager/

---

## 📝 파일 생성 위치

```
application/src/main/java/com/fileflow/application/fileasset/
├─ dto/
│   ├─ command/
│   │   ├─ ProcessFileAssetCommand.java
│   │   └─ UpdateFileAssetStatusCommand.java
│   ├─ query/
│   │   └─ ListFileAssetsForN8nQuery.java
│   └─ response/
│       ├─ ProcessFileAssetResponse.java
│       ├─ ProcessedFileInfo.java
│       ├─ UpdateFileAssetStatusResponse.java
│       └─ FileAssetForN8nResponse.java
├─ port/
│   ├─ in/
│   │   ├─ command/
│   │   │   ├─ ProcessFileAssetUseCase.java
│   │   │   └─ UpdateFileAssetStatusUseCase.java
│   │   └─ query/
│   │       └─ ListFileAssetsForN8nUseCase.java
│   └─ out/
│       ├─ command/
│       │   ├─ ProcessedFileAssetPersistencePort.java
│       │   ├─ FileProcessingOutboxPersistencePort.java
│       │   └─ FileAssetStatusHistoryPersistencePort.java
│       ├─ query/
│       │   ├─ ProcessedFileAssetQueryPort.java
│       │   ├─ FileProcessingOutboxQueryPort.java
│       │   └─ FileAssetStatusHistoryQueryPort.java
│       └─ external/
│           ├─ ImageProcessingPort.java
│           ├─ HtmlProcessingPort.java
│           └─ SqsMessagePort.java
├─ manager/
│   ├─ ProcessedFileAssetTransactionManager.java
│   ├─ FileProcessingOutboxTransactionManager.java
│   └─ FileAssetStatusHistoryTransactionManager.java
├─ facade/
│   └─ FileAssetProcessingFacade.java
├─ assembler/
│   └─ ProcessFileAssetAssembler.java
└─ service/
    ├─ ProcessFileAssetService.java
    ├─ UpdateFileAssetStatusService.java
    └─ ListFileAssetsForN8nService.java
```

---

## 📝 다음 Plan

Application Layer 완료 후 → `FILE-ASSET-PROCESSING-persistence-plan.md`
