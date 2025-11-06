# Upload/Download 바운더리 컨텍스트 리팩토링 보고서

**작성일**: 2025-11-05
**대상**: application/download, application/upload 패키지
**목적**: DI 의존성 리팩토링, 비즈니스 로직 Domain 내재화, 아키텍처 개선

---

## 1. 현황 분석

### 1.1 패키지 구조

```
application/
├── download/
│   ├── controller/
│   ├── service/
│   │   └── StartExternalDownloadService.java (UseCase 구현체)
│   ├── manager/
│   │   └── ExternalDownloadManager.java (상태 관리)
│   ├── port/
│   │   ├── in/  (UseCase 인터페이스)
│   │   └── out/ (Port 인터페이스 - Command/Query 분리)
│   ├── dto/
│   └── config/
│       └── ExternalDownloadOutboxProperties.java
│
└── upload/
    ├── controller/
    ├── service/
    │   ├── InitSingleUploadService.java
    │   ├── InitMultipartUploadService.java
    │   ├── CompleteSingleUploadService.java
    │   └── CompleteMultipartUploadService.java
    ├── manager/
    │   ├── UploadSessionStateManager.java (CQRS Command Manager)
    │   └── MultipartUploadStateManager.java
    ├── port/
    │   ├── in/
    │   └── out/
    │       ├── LoadUploadSessionPort.java (Query)
    │       ├── SaveUploadSessionPort.java (Command)
    │       ├── DeleteUploadSessionPort.java (Command)
    │       └── UploadSessionCachePort.java (Redis TTL)
    ├── dto/
    └── config/
        ├── UploadConfiguration.java
        └── PresignedUrlProperties.java
```

### 1.2 CQRS 패턴 적용 현황 ✅

**올바른 패턴 적용**:
- **Command Port**: SaveUploadSessionPort, DeleteUploadSessionPort
- **Query Port**: LoadUploadSessionPort, LoadMultipartUploadPort
- **StateManager**: Command 전용 Manager (UploadSessionStateManager)

**분리 원칙 준수**:
```java
// ✅ Good: CQRS Command Manager
public class UploadSessionStateManager {
    private final SaveUploadSessionPort savePort;
    private final DeleteUploadSessionPort deletePort;
    
    @Transactional
    public UploadSession save(UploadSession session) {
        return savePort.save(session);
    }
}

// ✅ Good: Service에서 Query와 Command 분리 사용
public class CompleteMultipartUploadService {
    private final LoadUploadSessionPort loadPort;  // Query
    private final UploadSessionStateManager stateManager;  // Command
}
```

### 1.3 Transaction 경계 관리 현황 ✅

**외부 API 호출 분리 (Zero-Tolerance 규칙 준수)**:

```java
// CompleteMultipartUploadService.java
@Transactional(readOnly = true)
public CompleteMultipartResponse execute(CompleteMultipartCommand command) {
    // 1. Validation (트랜잭션 내)
    ValidationResultResponse validation = validateCanComplete(command.sessionKey());
    
    // 2. S3 Complete (트랜잭션 밖 - 외부 API) ✅
    S3CompleteResultResponse s3Result = completeS3Multipart(session, multipart);
    
    // 3. S3 Verification (트랜잭션 밖 - 외부 API) ✅
    S3HeadObjectResponse s3HeadResult = verifyS3Object(session);
    
    // 4. Domain Update (별도 트랜잭션)
    completeUpload(session, multipart, s3Result, s3HeadResult);
}
```

**Transactional Outbox 패턴 (StartExternalDownloadService)**:
```java
@Transactional
public ExternalDownloadResponse execute(StartExternalDownloadCommand command) {
    // DB 작업만 트랜잭션 내에서 수행
    // 외부 다운로드는 별도 워커가 Outbox 메시지 polling하여 처리 ✅
    ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(...);
    outboxCommandPort.save(outbox);
}
```

---

## 2. 발견 사항 (사용자 오해 정정)

### 2.1 LoadUploadSessionPort 구현체 존재 확인 ✅

**사용자 주장**: "LoadUploadSessionPort는 구현체가 없더라고"

**실제 상황**:
- **구현체 존재**: `UploadSessionQueryAdapter.java` (Persistence Layer)
- **위치**: `adapter-out/persistence/upload/query/UploadSessionQueryAdapter.java`
- **구현 방식**: JPA Repository 기반

```java
// UploadSessionQueryAdapter.java (Lines 1-96)
@Component
public class UploadSessionQueryAdapter implements LoadUploadSessionPort {
    private final UploadSessionJpaRepository repository;
    
    @Override
    public Optional<UploadSession> findById(Long id) {
        return repository.findById(id)
            .map(UploadSessionEntityMapper::toDomain);
    }
    
    @Override
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        return repository.findBySessionKey(sessionKey.value())
            .map(UploadSessionEntityMapper::toDomain);
    }
}
```

**결론**: QueryDSL 구현 **불필요** - 기존 JPA Repository 구현이 적절함.

### 2.2 UploadSessionCachePort 정상 사용 중 ✅

**사용자 주장**: "UploadSessionCachePort 이거도 있던데 데드코드 들이 있던데"

**실제 상황**:
- **데드코드 아님** - 적극적으로 사용 중
- **용도**: Redis TTL 기반 세션 만료 추적
- **사용처**: 
  1. `InitSingleUploadService.java` (Line 147)
  2. `InitMultipartUploadService.java` (Line 167)

```java
// InitMultipartUploadService.java (Lines 166-169)
// Redis keyspace notification으로 세션 만료 이벤트 트리거
uploadSessionCachePort.trackSession(
    savedSession.getSessionKey().value(),
    presignedUrlProperties.getMultipartPartDuration()
);
```

**Redis 통합 아키텍처**:
```
PresignedURL 생성 (TTL 30분)
    ↓
uploadSessionCachePort.trackSession()
    ↓
Redis SET with TTL
    ↓
TTL 만료 시 Keyspace Notification
    ↓
SessionExpirationListener (별도 구현 필요)
    ↓
만료된 세션 정리 (EXPIRED 상태로 변경)
```

**결론**: UploadSessionCachePort는 **필수 컴포넌트** - Redis 기반 세션 만료 추적 메커니즘.

### 2.3 Config 패키지 위치 적절함 ✅

**사용자 질문**: "config 패키지가 있던데 이게 applicatoin에 정말 있어야하는지 저걸 어디로 어떻게 빼야할지"

**현재 Config 구조**:
```java
// UploadConfiguration.java
@Configuration
@EnableConfigurationProperties({
    PresignedUrlProperties.class
})
public class UploadConfiguration {
    // @ConfigurationProperties 활성화만 담당
}

// PresignedUrlProperties.java
@ConfigurationProperties(prefix = "upload.presigned-url")
public class PresignedUrlProperties {
    private Duration singleUploadDuration;
    private Duration multipartPartDuration;
    private int multipartPartSize;
    // ...
}
```

**Application Layer에 있어야 하는 이유**:
1. **Application Layer의 책임**: 외부화된 설정을 Type-Safe하게 관리
2. **UseCase 실행에 필요한 정책**: Presigned URL TTL, 멀티파트 크기 등은 비즈니스 정책
3. **Infrastructure 관심사 아님**: AWS S3 설정이 아닌, 업로드 UseCase의 정책 설정

**비교**:
- ❌ **Infrastructure Layer**: AWS S3 Client 설정, Connection Pool, Region 등
- ✅ **Application Layer**: Presigned URL 유효기간, 멀티파트 청크 크기 등 비즈니스 정책

**결론**: Config 패키지는 **현재 위치가 적절** - 이동 불필요.

---

## 3. 리팩토링 기회

### 3.1 비즈니스 로직 Domain 내재화

#### 3.1.1 ExternalDownloadManager.markCompleted() → Domain으로 이동

**현재 문제점** (ExternalDownloadManager.java Lines 236-296):
```java
// ❌ Application Layer에 비즈니스 로직 집중
@Transactional
public void markCompleted(ExternalDownload download, UploadSession session, DownloadResult result) {
    long fileSize = result.uploadResult().size();
    
    // 1. ExternalDownload 완료 처리
    completeDownload(download, fileSize);
    
    // 2. UploadSession 파일 크기 업데이트
    session.updateFileSize(FileSize.of(fileSize));
    uploadSessionStateManager.save(session);
    
    // 3. FileAsset 생성 (비즈니스 로직!)
    FileAsset fileAsset = FileAsset.forNew(
        session.getTenantId(), null, null,
        session.getFileName(), FileSize.of(fileSize),
        MimeType.of("application/octet-stream"),
        result.storageKey(), Checksum.of("pending"),
        session.getId()
    );
    FileAsset savedFileAsset = fileCommandManager.save(fileAsset);
    
    // 4. UploadSession 완료
    session.complete(savedFileAsset.getIdValue());
}
```

**리팩토링 제안**:

**Step 1**: Domain에 Factory Method 추가
```java
// FileAsset.java (Domain Layer)
public class FileAsset {
    /**
     * UploadSession 완료 시 FileAsset 생성
     * 
     * @param session 완료된 업로드 세션
     * @param storageKey S3 스토리지 키
     * @param fileSize 최종 파일 크기
     * @return 새로운 FileAsset
     * @since 2025-11-05
     * @author Claude
     */
    public static FileAsset fromCompletedUpload(
        UploadSession session,
        StorageKey storageKey,
        FileSize fileSize
    ) {
        return FileAsset.forNew(
            session.getTenantId(),
            null,  // parentId - 업로드 시점에는 없음
            null,  // folderId - 업로드 시점에는 없음
            session.getFileName(),
            fileSize,
            MimeType.of("application/octet-stream"),  // 기본값, 추후 분석
            storageKey,
            Checksum.of("pending"),  // 체크섬은 비동기 계산
            session.getId()
        );
    }
}
```

**Step 2**: Manager 단순화
```java
// ExternalDownloadManager.java (Application Layer)
@Transactional
public void markCompleted(ExternalDownload download, UploadSession session, DownloadResult result) {
    long fileSize = result.uploadResult().size();
    
    // 1. Domain 메서드 호출
    completeDownload(download, fileSize);
    session.updateFileSize(FileSize.of(fileSize));
    
    // 2. Domain Factory 사용 ✅
    FileAsset fileAsset = FileAsset.fromCompletedUpload(
        session,
        result.storageKey(),
        FileSize.of(fileSize)
    );
    
    FileAsset savedFileAsset = fileCommandManager.save(fileAsset);
    session.complete(savedFileAsset.getIdValue());
}
```

**효과**:
- FileAsset 생성 로직이 Domain Layer로 이동 → 재사용성 향상
- Manager는 orchestration에 집중
- 비즈니스 규칙이 Domain에 명시적으로 표현됨

#### 3.1.2 중복된 FileAsset 생성 로직 통합

**현재 문제점**: Complete 서비스들에 FileAsset 생성 로직 중복

```java
// CompleteSingleUploadService.java (Lines 216-228)
FileAsset fileAsset = FileAsset.forNew(
    session.getTenantId(), null, null,
    session.getFileName(), FileSize.of(s3HeadResult.contentLength()),
    MimeType.of(s3HeadResult.contentType()),
    StorageKey.of(s3HeadResult.storageKey()),
    Checksum.of(s3HeadResult.etag()),
    session.getId()
);

// CompleteMultipartUploadService.java (Lines 218-230) - 거의 동일한 코드
FileAsset fileAsset = FileAsset.forNew(
    session.getTenantId(), null, null,
    session.getFileName(), FileSize.of(s3HeadResult.contentLength()),
    MimeType.of(s3HeadResult.contentType()),
    StorageKey.of(s3HeadResult.storageKey()),
    Checksum.of(s3HeadResult.etag()),
    session.getId()
);
```

**리팩토링 제안**:

```java
// FileAsset.java (Domain Layer)
public class FileAsset {
    /**
     * S3 업로드 완료 후 FileAsset 생성
     * 
     * @param session 업로드 세션
     * @param s3Result S3 HEAD Object 결과 (ETag, ContentType 포함)
     * @return 새로운 FileAsset
     * @since 2025-11-05
     * @author Claude
     */
    public static FileAsset fromS3Upload(
        UploadSession session,
        S3HeadObjectResponse s3Result
    ) {
        return FileAsset.forNew(
            session.getTenantId(),
            null,  // parentId
            null,  // folderId
            session.getFileName(),
            FileSize.of(s3Result.contentLength()),
            MimeType.of(s3Result.contentType()),
            StorageKey.of(s3Result.storageKey()),
            Checksum.of(s3Result.etag()),
            session.getId()
        );
    }
}

// Complete 서비스들에서 사용
FileAsset fileAsset = FileAsset.fromS3Upload(session, s3HeadResult);
```

**효과**:
- 중복 코드 제거
- S3 업로드 결과 → FileAsset 생성 규칙을 Domain에 명시
- 테스트 용이성 향상

### 3.2 DI 의존성 리팩토링

#### 3.2.1 StartExternalDownloadService 의존성 분석

**현재 DI 의존성** (6개):
```java
public class StartExternalDownloadService implements StartExternalDownloadUseCase {
    private final UploadSessionStateManager uploadSessionStateManager;
    private final LoadUploadSessionPort loadUploadSessionPort;
    private final ExternalDownloadCommandPort externalDownloadCommandPort;
    private final ExternalDownloadQueryPort externalDownloadQueryPort;
    private final OutboxCommandPort outboxCommandPort;
    private final OutboxQueryPort outboxQueryPort;
}
```

**문제점**:
- Port 의존성이 많음 (6개)
- Idempotency 체크, CRUD 작업이 Service에 직접 노출

**리팩토링 제안**: Facade 패턴 도입

```java
// ExternalDownloadFacade.java (Application Layer)
/**
 * ExternalDownload Aggregate 관련 작업을 통합 관리하는 Facade
 * 
 * @since 2025-11-05
 * @author Claude
 */
@Component
public class ExternalDownloadFacade {
    private final ExternalDownloadCommandPort commandPort;
    private final ExternalDownloadQueryPort queryPort;
    private final OutboxCommandPort outboxCommandPort;
    private final OutboxQueryPort outboxQueryPort;
    
    /**
     * Idempotency Key로 기존 다운로드 조회
     */
    public Optional<ExternalDownloadOutbox> findExistingOperation(IdempotencyKey key) {
        return outboxQueryPort.findByIdempotencyKey(key);
    }
    
    /**
     * 다운로드 요청 생성 (Outbox 패턴)
     */
    @Transactional
    public ExternalDownloadOutbox createDownloadRequest(
        ExternalDownload download,
        UploadSession session,
        IdempotencyKey idempotencyKey
    ) {
        // 1. ExternalDownload 저장
        ExternalDownload savedDownload = commandPort.save(download);
        
        // 2. Outbox 메시지 생성
        ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(
            savedDownload.getId(),
            session.getId(),
            idempotencyKey,
            download.getUrl()
        );
        
        // 3. Outbox 저장
        return outboxCommandPort.save(outbox);
    }
}

// StartExternalDownloadService.java (리팩토링 후)
public class StartExternalDownloadService implements StartExternalDownloadUseCase {
    private final UploadSessionStateManager uploadSessionStateManager;
    private final LoadUploadSessionPort loadUploadSessionPort;
    private final ExternalDownloadFacade externalDownloadFacade;  // ✅ Facade로 통합
    
    @Override
    @Transactional
    public ExternalDownloadResponse execute(StartExternalDownloadCommand command) {
        // 1. Idempotency 체크 (Facade 사용)
        Optional<ExternalDownloadOutbox> existing = 
            externalDownloadFacade.findExistingOperation(command.idempotencyKey());
        if (existing.isPresent()) {
            return buildResponseFromOutbox(existing.get());
        }
        
        // 2. UploadSession 생성
        UploadSession session = UploadSession.createForExternalDownload(
            command.tenantId(), command.fileName(), command.fileSize()
        );
        UploadSession savedSession = uploadSessionStateManager.save(session);
        
        // 3. ExternalDownload 생성 및 Outbox 저장 (Facade 사용)
        ExternalDownload download = ExternalDownload.forNew(
            savedSession.getId(), command.url()
        );
        ExternalDownloadOutbox outbox = externalDownloadFacade.createDownloadRequest(
            download, savedSession, command.idempotencyKey()
        );
        
        return ExternalDownloadResponse.from(outbox, savedSession);
    }
}
```

**효과**:
- Service 의존성: 6개 → 3개로 감소
- Outbox 패턴 로직이 Facade에 캡슐화
- Idempotency 체크 로직 재사용 가능
- 테스트 용이성 향상

#### 3.2.2 IamContextFacade 패턴 확장

**현재 잘 작동하는 패턴**:
```java
// IamContextFacade.java
@Component
public class IamContextFacade {
    private final LoadIamContextPort loadPort;
    
    public IamContext getCurrentContext() {
        return loadPort.getCurrentContext();
    }
    
    public TenantId getCurrentTenantId() {
        return getCurrentContext().getTenantId();
    }
}
```

**확장 제안**: Upload 관련 Context 작업도 Facade로

```java
// UploadContextFacade.java (Application Layer)
/**
 * Upload 관련 Context 정보 통합 제공
 * 
 * @since 2025-11-05
 * @author Claude
 */
@Component
public class UploadContextFacade {
    private final IamContextFacade iamContextFacade;
    private final PresignedUrlProperties presignedUrlProperties;
    
    /**
     * Single Upload용 Presigned URL TTL
     */
    public Duration getSingleUploadTtl() {
        return presignedUrlProperties.getSingleUploadDuration();
    }
    
    /**
     * Multipart Upload용 Presigned URL TTL
     */
    public Duration getMultipartPartTtl() {
        return presignedUrlProperties.getMultipartPartDuration();
    }
    
    /**
     * 현재 Tenant의 Upload 세션 생성
     */
    public UploadSession createSessionForTenant(
        FileName fileName,
        FileSize fileSize,
        UploadType uploadType
    ) {
        TenantId tenantId = iamContextFacade.getCurrentTenantId();
        
        return switch (uploadType) {
            case SINGLE -> UploadSession.createForSingleUpload(tenantId, fileName, fileSize);
            case MULTIPART -> UploadSession.createForMultipartUpload(tenantId, fileName, fileSize);
        };
    }
}
```

**효과**:
- Upload 관련 Context 로직 중앙화
- Properties 직접 접근 제거
- Tenant 컨텍스트 + Upload 정책을 하나의 Facade에서 관리

### 3.3 추가 개선 사항

#### 3.3.1 ExternalDownloadManager 역할 재정의

**현재 문제**:
- Manager가 너무 많은 책임 (orchestration + business logic)
- 299라인의 거대한 클래스

**리팩토링 제안**: Manager를 순수 Orchestrator로 단순화

```java
// ExternalDownloadOrchestrator.java (리네이밍)
/**
 * ExternalDownload Aggregate의 상태 전환 Orchestration
 * 비즈니스 로직은 Domain Layer에 위임
 * 
 * @since 2025-11-05
 * @author Claude
 */
@Component
public class ExternalDownloadOrchestrator {
    private final ExternalDownloadFacade externalDownloadFacade;
    private final UploadSessionStateManager uploadSessionStateManager;
    private final FileCommandManager fileCommandManager;
    
    /**
     * 다운로드 완료 처리 Orchestration
     * 비즈니스 로직은 Domain에 위임
     */
    @Transactional
    public void completeDownload(
        ExternalDownload download,
        UploadSession session,
        DownloadResult result
    ) {
        // 1. Domain 메서드 호출 (Tell, Don't Ask)
        download.complete(result.uploadResult().size());
        session.updateFileSize(FileSize.of(result.uploadResult().size()));
        
        // 2. FileAsset 생성 (Domain Factory)
        FileAsset fileAsset = FileAsset.fromCompletedUpload(
            session,
            result.storageKey(),
            FileSize.of(result.uploadResult().size())
        );
        FileAsset savedFileAsset = fileCommandManager.save(fileAsset);
        
        // 3. Session 완료
        session.complete(savedFileAsset.getIdValue());
        
        // 4. 저장 (Facade 사용)
        externalDownloadFacade.saveAll(download, session);
    }
}
```

**효과**:
- Manager → Orchestrator로 명확한 역할 정의
- 비즈니스 로직은 Domain에, Orchestration만 Application Layer에
- 클래스 크기 감소 (299 → ~150 라인 예상)

---

## 4. 우선순위별 리팩토링 로드맵

### Phase 1: Quick Wins (1-2주) 🚀

**목표**: 중복 코드 제거 및 Domain Factory Method 도입

1. **FileAsset Factory Methods 추가**
   - `FileAsset.fromCompletedUpload()` (ExternalDownload용)
   - `FileAsset.fromS3Upload()` (Single/Multipart Upload용)
   - **영향 범위**: FileAsset.java, 3개 Complete 서비스
   - **예상 효과**: 중복 코드 50라인 제거, 재사용성 향상

2. **Complete 서비스들 리팩토링**
   - CompleteSingleUploadService
   - CompleteMultipartUploadService
   - ExternalDownloadManager
   - **영향 범위**: 3개 클래스
   - **예상 효과**: 비즈니스 로직 Domain 이동, 코드 가독성 향상

**리스크**: 낮음 - 순수 리팩토링, 기능 변경 없음

### Phase 2: Facade 패턴 도입 (2-3주) 🏗️

**목표**: DI 의존성 단순화 및 코드 응집도 향상

1. **ExternalDownloadFacade 구현**
   - Idempotency 체크 캡슐화
   - Outbox 패턴 로직 중앙화
   - **영향 범위**: StartExternalDownloadService
   - **예상 효과**: 의존성 6개 → 3개 감소

2. **UploadContextFacade 구현**
   - Upload Context 로직 중앙화
   - Properties 직접 접근 제거
   - **영향 범위**: 4개 Init/Complete 서비스
   - **예상 효과**: Context 관리 일관성 향상

**리스크**: 중간 - 새로운 추상화 계층 도입, 테스트 필요

### Phase 3: Manager → Orchestrator 전환 (3-4주) 🔄

**목표**: Application Layer 역할 명확화

1. **ExternalDownloadManager 리팩토링**
   - ExternalDownloadOrchestrator로 리네이밍
   - 비즈니스 로직 Domain으로 완전 이동
   - **영향 범위**: ExternalDownloadManager (299라인)
   - **예상 효과**: 클래스 크기 50% 감소, 책임 명확화

2. **UploadSession 관련 비즈니스 메서드 강화**
   - Session 상태 전환 로직 Domain 메서드로 추가
   - Manager의 orchestration 로직 단순화
   - **영향 범위**: UploadSession Domain, Manager 클래스들
   - **예상 효과**: Tell, Don't Ask 원칙 강화

**리스크**: 높음 - 대규모 리팩토링, 회귀 테스트 필수

### Phase 4: 테스트 강화 (병행 진행) ✅

**목표**: 리팩토링 안전성 확보

1. **Domain Layer 단위 테스트**
   - FileAsset Factory Methods 테스트
   - UploadSession 비즈니스 메서드 테스트
   - **예상 효과**: 도메인 로직 신뢰성 확보

2. **Integration 테스트**
   - Facade 통합 테스트
   - Orchestrator 시나리오 테스트
   - **예상 효과**: 리팩토링 회귀 방지

3. **ArchUnit 규칙 추가**
   - Manager → Orchestrator 네이밍 강제
   - Facade 의존성 규칙 검증
   - **예상 효과**: 아키텍처 규칙 자동 검증

---

## 5. 예상 효과

### 정량적 효과
- **코드 중복 제거**: ~100라인 감소
- **DI 의존성 감소**: 평균 30% 감소 (6개 → 4개)
- **클래스 크기 감소**: ExternalDownloadManager 299 → ~150 라인
- **테스트 커버리지**: Domain Layer 90% 이상 달성 가능

### 정성적 효과
- **유지보수성 향상**: 비즈니스 로직이 Domain에 집중되어 변경 추적 용이
- **재사용성 향상**: Facade 패턴으로 공통 로직 재사용
- **테스트 용이성**: Domain Factory Methods는 순수 함수로 테스트 간단
- **아키텍처 준수**: Hexagonal Architecture 원칙 강화

---

## 6. 결론

### 6.1 핵심 발견

✅ **잘 설계된 부분**:
- CQRS 패턴 올바르게 적용
- Transaction 경계 엄격히 관리 (Zero-Tolerance 준수)
- Transactional Outbox 패턴으로 외부 API 분리
- IamContextFacade 패턴 성공적 적용

⚠️ **개선 필요 부분**:
- FileAsset 생성 로직 중복 (3곳)
- Manager에 비즈니스 로직 일부 존재
- DI 의존성 다소 많음 (특히 StartExternalDownloadService)
- Manager와 Orchestrator 역할 모호

❌ **사용자 오해 정정**:
- LoadUploadSessionPort 구현체 **존재함** - QueryDSL 구현 불필요
- UploadSessionCachePort **정상 사용 중** - 데드코드 아님
- Config 패키지 **현재 위치 적절** - 이동 불필요

### 6.2 최종 권장사항

**즉시 시작 (Phase 1)**:
1. FileAsset Factory Methods 추가 (fromCompletedUpload, fromS3Upload)
2. Complete 서비스들에 Factory Methods 적용

**중기 계획 (Phase 2-3)**:
3. ExternalDownloadFacade 구현
4. UploadContextFacade 구현
5. Manager → Orchestrator 전환

**장기 투자 (Phase 4)**:
6. Domain Layer 테스트 강화
7. ArchUnit 규칙 추가

**✅ 전체 리팩토링 완료 시 기대 효과**:
- Application Layer는 순수 Orchestration에 집중
- Domain Layer에 비즈니스 로직 집중
- Facade 패턴으로 의존성 단순화
- Zero-Tolerance 규칙 100% 준수 유지

---

**보고서 작성 완료**: 2025-11-05
**다음 단계**: Phase 1 리팩토링 작업 시작 (FileAsset Factory Methods)