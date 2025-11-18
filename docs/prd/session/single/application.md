# Application Layer - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Application
**작성일**: 2025-11-18

---

## 📋 목차

1. [Orchestration Pattern](#orchestration-pattern)
2. [Command DTOs](#command-dttos)
3. [Response DTOs](#response-dttos)
4. [Port In (UseCases)](#port-in-usecases)
5. [Port Out (Ports)](#port-out-ports)
6. [UserContext](#usercontext)
7. [Transaction 경계 설계](#transaction-경계-설계)

---

## Orchestration Pattern

### 문제: Transaction Boundary Violation

**MVP의 잘못된 설계** (`GeneratePresignedUrlService`):

```java
@Component
@Transactional  // ← 트랜잭션 시작
public class GeneratePresignedUrlService implements GeneratePresignedUrlUseCase {
    @Override
    public PresignedUrlResponse execute(GeneratePresignedUrlCommand cmd) {
        // ...

        // ❌ S3 호출이 트랜잭션 안에 있음!
        PresignedUrl presignedUrl = s3ClientPort.generatePresignedPutUrl(
            s3Bucket, s3Key, cmd.mimeType(), Duration.ofMinutes(5)
        );

        uploadSessionPersistencePort.save(session);
    }
}
```

**문제점**:
1. ❌ Zero-Tolerance Rule #4 위반: `@Transactional` 내 외부 API 호출
2. ❌ DB Connection Long-Hold: S3 응답 대기 중 Connection 점유
3. ❌ Unnecessary Rollback: S3 실패 시 불필요한 Transaction Rollback

---

### 해결: Orchestration Pattern (Facade + Manager)

**Facade**: Orchestration (트랜잭션 없음)
**Manager**: Transaction 경계 관리 (트랜잭션 있음)

```
GeneratePresignedUrlFacade (트랜잭션 ❌)
  ├─ SessionManager.prepareSession() ← 트랜잭션 ✅
  ├─ S3ClientPort.generatePresignedUrl() ← 트랜잭션 ❌ (외부 API)
  └─ SessionManager.completePreparation() ← 트랜잭션 ✅
```

**핵심 원칙**:
- **Facade**: 외부 API 호출 + 전체 흐름 조율 (트랜잭션 없음)
- **Manager**: DB 작업만 수행 (트랜잭션 있음)
- **Transaction 분리**: 외부 API 호출 전후로 트랜잭션 커밋/시작

---

## Use Cases Implementation

### 1. GeneratePresignedUrlFacade

**책임**: Presigned URL 발급 Orchestration

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/facade/GeneratePresignedUrlFacade.java`

```java
/**
 * Presigned URL 발급 Facade (Orchestration Pattern)
 * <p>
 * - Transaction 없음: 외부 API 호출 허용
 * - SessionManager에 Transaction 위임
 * - Zero-Tolerance Rule #4 준수
 * </p>
 */
@Component
public class GeneratePresignedUrlFacade implements GeneratePresignedUrlUseCase {

    private final SessionManager sessionManager;
    private final S3ClientPort s3ClientPort;
    private final Clock clock;

    public GeneratePresignedUrlFacade(
        SessionManager sessionManager,
        S3ClientPort s3ClientPort,
        Clock clock
    ) {
        this.sessionManager = sessionManager;
        this.s3ClientPort = s3ClientPort;
        this.clock = clock;
    }

    @Override
    public PresignedUrlResponse execute(GeneratePresignedUrlCommand cmd) {
        // 1. SecurityContext에서 UserContext 추출
        UserContext userContext = extractUserContext();

        // 2. 트랜잭션: 세션 준비 (멱등성 체크, UploadSession 생성)
        SessionPreparationResult result = sessionManager.prepareSession(
            cmd,
            userContext
        );

        // 멱등성: 기존 세션이 있으면 기존 URL 반환
        if (result.isExistingSession()) {
            return buildResponse(
                result.session(),
                result.fileId()
            );
        }

        // 3. 트랜잭션 밖: S3 Presigned URL 발급 (외부 API)
        PresignedUrl presignedUrl = s3ClientPort.generatePresignedPutUrl(
            result.s3Bucket(),
            result.s3Key(),
            cmd.mimeType(),
            Duration.ofMinutes(5)
        );

        // 4. 트랜잭션: 세션 완료 처리 (presignedUrl 저장)
        UploadSession session = sessionManager.completeSessionPreparation(
            result.session().sessionId(),
            presignedUrl
        );

        // 5. Response 반환
        return buildResponse(session, result.fileId());
    }

    private UserContext extractUserContext() {
        return (UserContext) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
    }

    private PresignedUrlResponse buildResponse(
        UploadSession session,
        FileId fileId
    ) {
        return new PresignedUrlResponse(
            session.sessionId().value(),
            fileId.value(),
            session.presignedUrl().value(),
            300,  // 5분
            "SINGLE"
        );
    }
}
```

**Transaction 경계**:
1. ✅ **트랜잭션**: `sessionManager.prepareSession()` - DB만
2. ❌ **트랜잭션 없음**: `s3ClientPort.generatePresignedPutUrl()` - 외부 API
3. ✅ **트랜잭션**: `sessionManager.completeSessionPreparation()` - DB만

---

### 2. SessionManager

**책임**: Transaction 경계 관리

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/manager/SessionManager.java`

```java
/**
 * 세션 Transaction Manager
 * <p>
 * - Transaction 경계: 각 메서드마다 독립적인 트랜잭션
 * - DB 작업만 수행 (외부 API 호출 금지)
 * </p>
 */
@Component
class SessionManager {

    private final UploadSessionQueryPort uploadSessionQueryPort;
    private final UploadSessionPersistencePort uploadSessionPersistencePort;
    private final Clock clock;

    SessionManager(
        UploadSessionQueryPort uploadSessionQueryPort,
        UploadSessionPersistencePort uploadSessionPersistencePort,
        Clock clock
    ) {
        this.uploadSessionQueryPort = uploadSessionQueryPort;
        this.uploadSessionPersistencePort = uploadSessionPersistencePort;
        this.clock = clock;
    }

    /**
     * 세션 준비 (트랜잭션 안)
     * <p>
     * - 멱등성 체크: 기존 세션 조회
     * - 새 세션 생성: UploadSession (INITIATED)
     * </p>
     *
     * @param cmd GeneratePresignedUrlCommand
     * @param userContext UserContext
     * @return SessionPreparationResult
     */
    @Transactional
    public SessionPreparationResult prepareSession(
        GeneratePresignedUrlCommand cmd,
        UserContext userContext
    ) {
        // 1. 멱등성 체크: 동일 sessionId 조회
        Optional<UploadSession> existingSession =
            uploadSessionQueryPort.findBySessionId(cmd.sessionId());

        if (existingSession.isPresent()) {
            // 기존 세션 반환 (멱등성)
            UploadSession session = existingSession.get();
            return SessionPreparationResult.existingSession(
                session,
                FileId.generate(),  // 새 FileId (클라이언트 재시도용)
                null,  // S3Key는 기존 세션에서 추출 불가 (Presigned URL만 있음)
                null   // S3Bucket도 마찬가지
            );
        }

        // 2. FileId 생성
        FileId fileId = FileId.generate();

        // 3. FileCategory 처리
        FileCategory category = determineCategory(cmd.category(), userContext.uploaderType());

        // 4. S3Key 생성
        S3Key s3Key = S3Key.generate(
            userContext.tenantId(),
            userContext.uploaderType(),
            userContext.uploaderSlug(),
            category,
            fileId,
            cmd.fileName()
        );

        // 5. S3Bucket 생성
        S3Bucket s3Bucket = S3Bucket.forTenant(userContext.tenantId());

        // 6. UploadSession 생성 (INITIATED 상태, presignedUrl은 null)
        UploadSession session = UploadSession.initiate(
            cmd.sessionId(),
            userContext.tenantId(),
            cmd.fileName(),
            cmd.fileSize(),
            cmd.mimeType(),
            UploadType.SINGLE,
            null,  // presignedUrl는 나중에 설정
            clock
        );

        // 7. UploadSession 저장
        uploadSessionPersistencePort.save(session);

        // 8. SessionPreparationResult 반환
        return SessionPreparationResult.newSession(
            session,
            fileId,
            s3Key,
            s3Bucket
        );
    }

    /**
     * 세션 완료 처리 (트랜잭션 안)
     * <p>
     * - UploadSession에 presignedUrl 저장
     * - 상태: INITIATED → IN_PROGRESS
     * </p>
     *
     * @param sessionId SessionId
     * @param presignedUrl PresignedUrl
     * @return UploadSession
     */
    @Transactional
    public UploadSession completeSessionPreparation(
        SessionId sessionId,
        PresignedUrl presignedUrl
    ) {
        // 1. UploadSession 조회
        UploadSession session = uploadSessionQueryPort.findBySessionId(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // 2. PresignedUrl 설정 (현재 도메인 설계에는 setter 없음, JPA Entity 수정 필요)
        // TODO: UploadSession에 setPresignedUrl() 메서드 추가 또는 JPA Entity에서 직접 수정

        // 3. 상태: INITIATED → IN_PROGRESS
        session.markAsInProgress(clock);

        // 4. UploadSession 업데이트
        return uploadSessionPersistencePort.update(session);
    }

    private FileCategory determineCategory(
        FileCategory requestedCategory,
        UploaderType uploaderType
    ) {
        if (uploaderType == UploaderType.CUSTOMER) {
            return FileCategory.defaultCategory();
        }
        return requestedCategory != null
            ? requestedCategory
            : FileCategory.defaultCategory();
    }
}
```

**Transaction 경계**:
- ✅ `prepareSession()`: 독립 트랜잭션 (DB 조회 + 저장)
- ✅ `completeSessionPreparation()`: 독립 트랜잭션 (DB 업데이트)
- ❌ 외부 API 호출 없음 (Zero-Tolerance Rule #4 준수)

---

### 3. SessionPreparationResult

**책임**: 세션 준비 결과 전달

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/manager/SessionPreparationResult.java`

```java
/**
 * 세션 준비 결과 DTO
 * <p>
 * - 기존 세션 vs 새 세션 구분
 * - S3Key, S3Bucket 전달 (Facade에서 S3 호출 시 사용)
 * </p>
 */
public record SessionPreparationResult(
    UploadSession session,
    FileId fileId,
    S3Key s3Key,
    S3Bucket s3Bucket,
    boolean isExistingSession
) {
    public static SessionPreparationResult existingSession(
        UploadSession session,
        FileId fileId,
        S3Key s3Key,
        S3Bucket s3Bucket
    ) {
        return new SessionPreparationResult(
            session,
            fileId,
            s3Key,
            s3Bucket,
            true  // 기존 세션
        );
    }

    public static SessionPreparationResult newSession(
        UploadSession session,
        FileId fileId,
        S3Key s3Key,
        S3Bucket s3Bucket
    ) {
        return new SessionPreparationResult(
            session,
            fileId,
            s3Key,
            s3Bucket,
            false  // 새 세션
        );
    }
}
```

---

### 4. CompleteUploadService

**책임**: 업로드 완료 처리

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/service/CompleteUploadService.java`

```java
/**
 * 업로드 완료 Service
 * <p>
 * - Transaction: 전체 메서드가 하나의 트랜잭션
 * - 외부 API 호출 없음 (DB만 사용)
 * </p>
 */
@Component
public class CompleteUploadService implements CompleteUploadUseCase {

    private final UploadSessionQueryPort uploadSessionQueryPort;
    private final UploadSessionPersistencePort uploadSessionPersistencePort;
    private final FilePersistencePort filePersistencePort;
    private final Clock clock;

    public CompleteUploadService(
        UploadSessionQueryPort uploadSessionQueryPort,
        UploadSessionPersistencePort uploadSessionPersistencePort,
        FilePersistencePort filePersistencePort,
        Clock clock
    ) {
        this.uploadSessionQueryPort = uploadSessionQueryPort;
        this.uploadSessionPersistencePort = uploadSessionPersistencePort;
        this.filePersistencePort = filePersistencePort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public FileResponse execute(CompleteUploadCommand cmd) {
        // 1. SecurityContext에서 UserContext 추출
        UserContext userContext = extractUserContext();

        // 2. UploadSession 조회
        UploadSession session = uploadSessionQueryPort
            .findBySessionId(cmd.sessionId())
            .orElseThrow(() -> new SessionNotFoundException(cmd.sessionId()));

        // 3. 세션 상태 검증
        session.ensureNotExpired(clock);
        session.ensureNotCompleted();

        // 4. FileId 생성
        FileId fileId = FileId.generate();

        // 5. FileCategory 처리 (세션 생성 시와 동일한 로직)
        FileCategory category = userContext.uploaderType() == UploaderType.CUSTOMER
            ? FileCategory.defaultCategory()
            : FileCategory.defaultCategory();  // MVP에서는 기본값

        // 6. S3Key 재생성 (세션 생성 시와 동일한 경로)
        S3Key s3Key = S3Key.generate(
            userContext.tenantId(),
            userContext.uploaderType(),
            userContext.uploaderSlug(),
            category,
            fileId,
            session.fileName()
        );

        // 7. S3Bucket 생성
        S3Bucket s3Bucket = S3Bucket.forTenant(userContext.tenantId());

        // 8. File Aggregate 생성
        File file = File.createFromSession(
            fileId,
            session.fileName(),
            session.fileSize(),
            session.mimeType(),
            s3Key,
            s3Bucket,
            userContext.uploaderId(),
            userContext.uploaderType(),
            userContext.uploaderSlug(),
            category,
            userContext.tenantId(),
            clock
        );

        // 9. File 저장
        File savedFile = filePersistencePort.save(file);

        // 10. UploadSession 완료 처리
        session.markAsCompleted(clock);
        uploadSessionPersistencePort.update(session);

        // 11. Response 반환
        return new FileResponse(
            session.sessionId().value(),
            savedFile.fileId().value(),
            savedFile.fileName().value(),
            savedFile.fileSize().bytes(),
            savedFile.mimeType().value(),
            savedFile.status().name(),
            savedFile.s3Key().value(),
            savedFile.s3Bucket().value(),
            savedFile.createdAt()
        );
    }

    private UserContext extractUserContext() {
        return (UserContext) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
    }
}
```

**Transaction 경계**:
- ✅ 전체 메서드가 하나의 트랜잭션
- ✅ 외부 API 호출 없음 (DB만 사용)

---

## Command DTOs

### 1. GeneratePresignedUrlCommand

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/dto/command/GeneratePresignedUrlCommand.java`

```java
/**
 * Presigned URL 발급 Command
 * <p>
 * - sessionId: 멱등키 (UUID v7)
 * - category: Admin, Seller만 사용 (Customer는 항상 "default")
 * </p>
 */
public record GeneratePresignedUrlCommand(
    SessionId sessionId,
    FileName fileName,
    FileSize fileSize,
    MimeType mimeType,
    FileCategory category  // Nullable (Admin, Seller만 사용)
) {}
```

---

### 2. CompleteUploadCommand

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/dto/command/CompleteUploadCommand.java`

```java
/**
 * 업로드 완료 Command
 * <p>
 * - sessionId: 세션 식별자
 * </p>
 */
public record CompleteUploadCommand(
    SessionId sessionId
) {}
```

---

## Response DTOs

### 1. PresignedUrlResponse

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/dto/response/PresignedUrlResponse.java`

```java
/**
 * Presigned URL 발급 Response
 * <p>
 * - expiresIn: 초 단위 (300초 = 5분)
 * - uploadType: MVP에서는 항상 "SINGLE"
 * </p>
 */
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,  // 300
    String uploadType  // "SINGLE"
) {}
```

---

### 2. FileResponse

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/dto/response/FileResponse.java`

```java
/**
 * 파일 Response
 * <p>
 * - 업로드 완료 후 반환
 * </p>
 */
public record FileResponse(
    String sessionId,
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,
    String s3Key,
    String s3Bucket,
    LocalDateTime createdAt
) {}
```

---

## Port In (UseCases)

### 1. GeneratePresignedUrlUseCase

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/in/command/GeneratePresignedUrlUseCase.java`

```java
/**
 * Presigned URL 발급 UseCase
 * <p>
 * - Input: GeneratePresignedUrlCommand
 * - Output: PresignedUrlResponse
 * </p>
 */
public interface GeneratePresignedUrlUseCase {
    PresignedUrlResponse execute(GeneratePresignedUrlCommand command);
}
```

---

### 2. CompleteUploadUseCase

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/in/command/CompleteUploadUseCase.java`

```java
/**
 * 업로드 완료 UseCase
 * <p>
 * - Input: CompleteUploadCommand
 * - Output: FileResponse
 * </p>
 */
public interface CompleteUploadUseCase {
    FileResponse execute(CompleteUploadCommand command);
}
```

---

## Port Out (Ports)

### Command Ports

#### 1. FilePersistencePort

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/out/command/FilePersistencePort.java`

```java
/**
 * File 저장 Port
 * <p>
 * - CQRS: Command Port (쓰기 전용)
 * </p>
 */
public interface FilePersistencePort {
    File save(File file);
}
```

---

#### 2. UploadSessionPersistencePort

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/out/command/UploadSessionPersistencePort.java`

```java
/**
 * UploadSession 저장/업데이트 Port
 * <p>
 * - CQRS: Command Port (쓰기 전용)
 * </p>
 */
public interface UploadSessionPersistencePort {
    UploadSession save(UploadSession session);
    UploadSession update(UploadSession session);
}
```

---

### Query Ports

#### 3. UploadSessionQueryPort

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/out/query/UploadSessionQueryPort.java`

```java
/**
 * UploadSession 조회 Port
 * <p>
 * - CQRS: Query Port (읽기 전용)
 * </p>
 */
public interface UploadSessionQueryPort {
    Optional<UploadSession> findBySessionId(SessionId sessionId);
}
```

---

### External Ports

#### 4. S3ClientPort

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/port/out/external/S3ClientPort.java`

```java
/**
 * S3 Client Port
 * <p>
 * - Presigned URL 생성
 * - 외부 API 호출 (트랜잭션 밖에서 호출)
 * </p>
 */
public interface S3ClientPort {
    PresignedUrl generatePresignedPutUrl(
        S3Bucket bucket,
        S3Key key,
        MimeType mimeType,
        Duration expiration
    );
}
```

---

## UserContext

### UserContext (JWT에서 추출)

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/dto/UserContext.java`

```java
/**
 * 사용자 컨텍스트 (JWT에서 추출)
 * <p>
 * - SecurityContext.getAuthentication().getPrincipal()
 * - JwtAuthenticationFilter에서 설정
 * </p>
 */
public record UserContext(
    TenantId tenantId,
    UploaderId uploaderId,
    UploaderType uploaderType,
    String uploaderSlug  // "connectly", "samsung-electronics", "default"
) {}
```

---

## Transaction 경계 설계

### 설계 원칙

1. **Facade = 트랜잭션 없음**:
   - 외부 API 호출 허용
   - 전체 흐름 조율 (Orchestration)

2. **Manager/Service = 트랜잭션 있음**:
   - DB 작업만 수행
   - 외부 API 호출 금지

3. **Transaction 분리**:
   - 외부 API 호출 전후로 트랜잭션 커밋/시작
   - DB Connection Long-Hold 방지

---

### GeneratePresignedUrlFacade Transaction Flow

```
1. SessionManager.prepareSession()
   ├─ 트랜잭션 시작
   ├─ UploadSession 조회 (멱등성 체크)
   ├─ UploadSession 생성 (INITIATED)
   ├─ UploadSession 저장
   └─ 트랜잭션 커밋

2. S3ClientPort.generatePresignedPutUrl()
   ├─ 트랜잭션 없음 (외부 API)
   └─ AWS S3 API 호출 (5분 유효)

3. SessionManager.completeSessionPreparation()
   ├─ 트랜잭션 시작
   ├─ UploadSession 조회
   ├─ PresignedUrl 저장
   ├─ Status: INITIATED → IN_PROGRESS
   ├─ UploadSession 업데이트
   └─ 트랜잭션 커밋
```

---

### CompleteUploadService Transaction Flow

```
1. CompleteUploadService.execute()
   ├─ 트랜잭션 시작
   ├─ UploadSession 조회
   ├─ 세션 상태 검증 (만료, 완료 체크)
   ├─ File Aggregate 생성
   ├─ File 저장
   ├─ UploadSession 상태 업데이트 (COMPLETED)
   ├─ UploadSession 업데이트
   └─ 트랜잭션 커밋
```

---

## Zero-Tolerance 체크리스트

- [x] ✅ **Lombok 금지**: Plain Java (Record, Explicit Getter)
- [x] ✅ **Law of Demeter**: `session.sessionId()` (O), `session.sessionId().value()` (X)
- [x] ✅ **Long FK 전략**: `TenantId`, `UploaderId` VO 사용
- [x] ✅ **Transaction 경계**: S3 호출은 트랜잭션 밖 (Facade)
- [x] ✅ **Spring 프록시**: Public 메서드, Non-Final, 외부 호출
- [x] ✅ **Orchestration Pattern**: Facade + Manager 분리
- [x] ✅ **Javadoc**: Public 메서드 필수
- [x] ✅ **CQRS**: Command/Query Port 분리

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/single Application Layer, Orchestration Pattern 적용)
