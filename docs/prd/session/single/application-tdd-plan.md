# Application Layer TDD Plan - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Application
**작성일**: 2025-11-18
**TDD Methodology**: Kent Beck Red-Green-Refactor

---

## 📋 목차

1. [Cycle 1-2: Command/Response DTOs (4개)](#cycle-1-2-dtos)
2. [Cycle 3: UserContext](#cycle-3-usercontext)
3. [Cycle 4: SessionPreparationResult](#cycle-4-sessionpreparationresult)
4. [Cycle 5-6: Port In (UseCases)](#cycle-5-6-port-in)
5. [Cycle 7-9: Port Out (Persistence/Query/External)](#cycle-7-9-port-out)
6. [Cycle 10: SessionManager (Transaction 경계)](#cycle-10-sessionmanager)
7. [Cycle 11: GeneratePresignedUrlFacade (Orchestration)](#cycle-11-facade)
8. [Cycle 12: CompleteUploadService](#cycle-12-service)

**전체 12 Cycles**

---

## Cycle 1-2: Command/Response DTOs

### Cycle 1: Command DTOs (2개)

#### Red (test:)

```java
// GeneratePresignedUrlCommandTest.java
class GeneratePresignedUrlCommandTest {
    @Test
    @DisplayName("GeneratePresignedUrlCommand를 생성해야 한다")
    void shouldCreateCommand() {
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            FileCategory.of("banner", UploaderType.ADMIN)
        );

        assertThat(command.sessionId()).isNotNull();
        assertThat(command.fileName().value()).isEqualTo("example.jpg");
    }
}

// CompleteUploadCommandTest.java
class CompleteUploadCommandTest {
    @Test
    @DisplayName("CompleteUploadCommand를 생성해야 한다")
    void shouldCreateCommand() {
        CompleteUploadCommand command = new CompleteUploadCommand(
            SessionId.generate()
        );

        assertThat(command.sessionId()).isNotNull();
    }
}
```

#### Green (feat:)

```java
// GeneratePresignedUrlCommand.java
public record GeneratePresignedUrlCommand(
    SessionId sessionId,
    FileName fileName,
    FileSize fileSize,
    MimeType mimeType,
    FileCategory category  // Nullable
) {}

// CompleteUploadCommand.java
public record CompleteUploadCommand(
    SessionId sessionId
) {}
```

#### 커밋

```bash
test: Application Command DTO 테스트 추가 (2개)
feat: Application Command DTO 구현 (Record)
```

---

### Cycle 2: Response DTOs (2개)

#### Red (test:)

```java
// PresignedUrlResponseTest.java
class PresignedUrlResponseTest {
    @Test
    @DisplayName("PresignedUrlResponse를 생성해야 한다")
    void shouldCreateResponse() {
        PresignedUrlResponse response = new PresignedUrlResponse(
            "01JD8000-1234-5678-9abc-def012345678",
            "01JD8001-1234-5678-9abc-def012345678",
            "https://example.com/presigned",
            300,
            "SINGLE"
        );

        assertThat(response.sessionId()).isNotEmpty();
        assertThat(response.expiresIn()).isEqualTo(300);
    }
}

// FileResponseTest.java
class FileResponseTest {
    @Test
    @DisplayName("FileResponse를 생성해야 한다")
    void shouldCreateResponse() {
        FileResponse response = new FileResponse(
            "01JD8000-1234-5678-9abc-def012345678",
            "01JD8001-1234-5678-9abc-def012345678",
            "example.jpg",
            1048576L,
            "image/jpeg",
            "COMPLETED",
            "uploads/1/admin/connectly/banner/01JD8001_example.jpg",
            "fileflow-uploads-1",
            LocalDateTime.now()
        );

        assertThat(response.fileId()).isNotEmpty();
        assertThat(response.status()).isEqualTo("COMPLETED");
    }
}
```

#### Green (feat:)

```java
// PresignedUrlResponse.java
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,
    String uploadType
) {}

// FileResponse.java
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

#### 커밋

```bash
test: Application Response DTO 테스트 추가 (2개)
feat: Application Response DTO 구현 (Record)
```

---

## Cycle 3: UserContext

#### Red (test:)

```java
// UserContextTest.java
class UserContextTest {
    @Test
    @DisplayName("UserContext를 생성해야 한다")
    void shouldCreateUserContext() {
        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        assertThat(userContext.tenantId().value()).isEqualTo(1L);
        assertThat(userContext.uploaderId().value()).isEqualTo(100L);
        assertThat(userContext.uploaderType()).isEqualTo(UploaderType.ADMIN);
        assertThat(userContext.uploaderSlug()).isEqualTo("connectly");
    }
}
```

#### Green (feat:)

```java
// UserContext.java
/**
 * 사용자 컨텍스트 (JWT에서 추출)
 */
public record UserContext(
    TenantId tenantId,
    UploaderId uploaderId,
    UploaderType uploaderType,
    String uploaderSlug
) {}
```

#### 커밋

```bash
test: UserContext DTO 테스트 추가
feat: UserContext DTO 구현 (JWT 추출용)
```

---

## Cycle 4: SessionPreparationResult

#### Red (test:)

```java
// SessionPreparationResultTest.java
class SessionPreparationResultTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
    }

    @Test
    @DisplayName("새 세션 결과를 생성해야 한다")
    void shouldCreateNewSessionResult() {
        UploadSession session = createSession(clock);
        FileId fileId = FileId.generate();
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.ADMIN,
            "connectly",
            FileCategory.of("banner", UploaderType.ADMIN),
            fileId,
            FileName.of("example.jpg")
        );
        S3Bucket s3Bucket = S3Bucket.forTenant(TenantId.of(1L));

        SessionPreparationResult result = SessionPreparationResult.newSession(
            session,
            fileId,
            s3Key,
            s3Bucket
        );

        assertThat(result.isExistingSession()).isFalse();
        assertThat(result.session()).isNotNull();
        assertThat(result.fileId()).isNotNull();
        assertThat(result.s3Key()).isNotNull();
        assertThat(result.s3Bucket()).isNotNull();
    }

    @Test
    @DisplayName("기존 세션 결과를 생성해야 한다")
    void shouldCreateExistingSessionResult() {
        UploadSession session = createSession(clock);
        FileId fileId = FileId.generate();

        SessionPreparationResult result = SessionPreparationResult.existingSession(
            session,
            fileId,
            null,
            null
        );

        assertThat(result.isExistingSession()).isTrue();
    }

    private UploadSession createSession(Clock clock) {
        return UploadSession.initiate(
            SessionId.generate(),
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );
    }
}
```

#### Green (feat:)

```java
// SessionPreparationResult.java
/**
 * 세션 준비 결과 DTO
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
            true
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
            false
        );
    }
}
```

#### 커밋

```bash
test: SessionPreparationResult DTO 테스트 추가 (멱등성 구분)
feat: SessionPreparationResult DTO 구현 (Factory Methods)
```

---

## Cycle 5-6: Port In (UseCases)

### Cycle 5: GeneratePresignedUrlUseCase

#### Red (test:)

```java
// GeneratePresignedUrlUseCaseTest.java (Interface 테스트)
class GeneratePresignedUrlUseCaseTest {
    @Test
    @DisplayName("GeneratePresignedUrlUseCase 인터페이스가 존재해야 한다")
    void shouldHaveGeneratePresignedUrlUseCase() {
        assertThat(GeneratePresignedUrlUseCase.class).isInterface();
    }
}
```

#### Green (feat:)

```java
// GeneratePresignedUrlUseCase.java
public interface GeneratePresignedUrlUseCase {
    PresignedUrlResponse execute(GeneratePresignedUrlCommand command);
}
```

#### 커밋

```bash
test: GeneratePresignedUrlUseCase Port In 테스트 추가
feat: GeneratePresignedUrlUseCase Port In 구현
```

---

### Cycle 6: CompleteUploadUseCase

**동일 패턴**

---

## Cycle 7-9: Port Out (Ports)

### Cycle 7: Persistence Ports (2개)

#### Red (test:)

```java
// FilePersistencePortTest.java
class FilePersistencePortTest {
    @Test
    @DisplayName("FilePersistencePort 인터페이스가 존재해야 한다")
    void shouldHaveFilePersistencePort() {
        assertThat(FilePersistencePort.class).isInterface();
    }
}

// UploadSessionPersistencePortTest.java
class UploadSessionPersistencePortTest {
    @Test
    @DisplayName("UploadSessionPersistencePort 인터페이스가 존재해야 한다")
    void shouldHaveUploadSessionPersistencePort() {
        assertThat(UploadSessionPersistencePort.class).isInterface();
    }
}
```

#### Green (feat:)

```java
// FilePersistencePort.java
public interface FilePersistencePort {
    File save(File file);
}

// UploadSessionPersistencePort.java
public interface UploadSessionPersistencePort {
    UploadSession save(UploadSession session);
    UploadSession update(UploadSession session);
}
```

#### 커밋

```bash
test: Persistence Ports 테스트 추가 (CQRS Command)
feat: Persistence Ports 구현 (FilePersistencePort, UploadSessionPersistencePort)
```

---

### Cycle 8: Query Port

**동일 패턴** (UploadSessionQueryPort)

---

### Cycle 9: External Port

**동일 패턴** (S3ClientPort)

---

## Cycle 10: SessionManager (Transaction 경계)

#### Red (test:)

```java
// SessionManagerTest.java
class SessionManagerTest {

    private SessionManager sessionManager;
    private UploadSessionQueryPort uploadSessionQueryPort;
    private UploadSessionPersistencePort uploadSessionPersistencePort;
    private Clock clock;

    @BeforeEach
    void setUp() {
        uploadSessionQueryPort = mock(UploadSessionQueryPort.class);
        uploadSessionPersistencePort = mock(UploadSessionPersistencePort.class);
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
        sessionManager = new SessionManager(
            uploadSessionQueryPort,
            uploadSessionPersistencePort,
            clock
        );
    }

    @Test
    @DisplayName("새 세션을 준비해야 한다")
    void shouldPrepareNewSession() {
        // given
        GeneratePresignedUrlCommand cmd = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            null
        );
        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        when(uploadSessionQueryPort.findBySessionId(cmd.sessionId()))
            .thenReturn(Optional.empty());

        // when
        SessionPreparationResult result = sessionManager.prepareSession(cmd, userContext);

        // then
        assertThat(result.isExistingSession()).isFalse();
        assertThat(result.session()).isNotNull();
        assertThat(result.fileId()).isNotNull();
        verify(uploadSessionPersistencePort).save(any(UploadSession.class));
    }

    @Test
    @DisplayName("기존 세션을 반환해야 한다 (멱등성)")
    void shouldReturnExistingSession() {
        // given
        SessionId sessionId = SessionId.generate();
        UploadSession existingSession = UploadSession.initiate(
            sessionId,
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );

        when(uploadSessionQueryPort.findBySessionId(sessionId))
            .thenReturn(Optional.of(existingSession));

        GeneratePresignedUrlCommand cmd = new GeneratePresignedUrlCommand(
            sessionId,
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            null
        );
        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        // when
        SessionPreparationResult result = sessionManager.prepareSession(cmd, userContext);

        // then
        assertThat(result.isExistingSession()).isTrue();
        verify(uploadSessionPersistencePort, never()).save(any(UploadSession.class));
    }
}
```

#### Green (feat:)

```java
// SessionManager.java
/**
 * 세션 Transaction Manager
 * <p>
 * - Transaction 경계: 각 메서드마다 독립적인 트랜잭션
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

    @Transactional
    public SessionPreparationResult prepareSession(
        GeneratePresignedUrlCommand cmd,
        UserContext userContext
    ) {
        Optional<UploadSession> existingSession =
            uploadSessionQueryPort.findBySessionId(cmd.sessionId());

        if (existingSession.isPresent()) {
            return SessionPreparationResult.existingSession(
                existingSession.get(),
                FileId.generate(),
                null,
                null
            );
        }

        FileId fileId = FileId.generate();
        FileCategory category = determineCategory(cmd.category(), userContext.uploaderType());
        S3Key s3Key = S3Key.generate(
            userContext.tenantId(),
            userContext.uploaderType(),
            userContext.uploaderSlug(),
            category,
            fileId,
            cmd.fileName()
        );
        S3Bucket s3Bucket = S3Bucket.forTenant(userContext.tenantId());

        UploadSession session = UploadSession.initiate(
            cmd.sessionId(),
            userContext.tenantId(),
            cmd.fileName(),
            cmd.fileSize(),
            cmd.mimeType(),
            UploadType.SINGLE,
            null,
            clock
        );

        uploadSessionPersistencePort.save(session);

        return SessionPreparationResult.newSession(
            session,
            fileId,
            s3Key,
            s3Bucket
        );
    }

    @Transactional
    public UploadSession completeSessionPreparation(
        SessionId sessionId,
        PresignedUrl presignedUrl
    ) {
        UploadSession session = uploadSessionQueryPort.findBySessionId(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        session.markAsInProgress(clock);

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

#### 커밋

```bash
test: SessionManager 테스트 추가 (Transaction 경계 관리)
feat: SessionManager 구현 (@Transactional, 멱등성)
```

---

## Cycle 11: GeneratePresignedUrlFacade (Orchestration)

#### Red (test:)

```java
// GeneratePresignedUrlFacadeTest.java
class GeneratePresignedUrlFacadeTest {

    private GeneratePresignedUrlFacade facade;
    private SessionManager sessionManager;
    private S3ClientPort s3ClientPort;
    private Clock clock;

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        s3ClientPort = mock(S3ClientPort.class);
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
        facade = new GeneratePresignedUrlFacade(
            sessionManager,
            s3ClientPort,
            clock
        );
    }

    @Test
    @DisplayName("새 세션에 대해 Presigned URL을 발급해야 한다")
    void shouldGeneratePresignedUrlForNewSession() {
        // given
        GeneratePresignedUrlCommand cmd = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            null
        );

        SessionPreparationResult prepResult = SessionPreparationResult.newSession(
            createSession(clock),
            FileId.generate(),
            S3Key.of("uploads/1/admin/connectly/banner/01JD8001_example.jpg"),
            S3Bucket.of("fileflow-uploads-1")
        );

        when(sessionManager.prepareSession(any(), any())).thenReturn(prepResult);
        when(s3ClientPort.generatePresignedPutUrl(any(), any(), any(), any()))
            .thenReturn(PresignedUrl.of("https://example.com/presigned"));
        when(sessionManager.completeSessionPreparation(any(), any()))
            .thenReturn(prepResult.session());

        // when
        PresignedUrlResponse response = facade.execute(cmd);

        // then
        assertThat(response.presignedUrl()).isNotEmpty();
        verify(sessionManager).prepareSession(any(), any());
        verify(s3ClientPort).generatePresignedPutUrl(any(), any(), any(), any());
        verify(sessionManager).completeSessionPreparation(any(), any());
    }

    @Test
    @DisplayName("기존 세션에 대해 기존 Presigned URL을 반환해야 한다 (멱등성)")
    void shouldReturnExistingPresignedUrlForExistingSession() {
        // given
        GeneratePresignedUrlCommand cmd = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            null
        );

        SessionPreparationResult prepResult = SessionPreparationResult.existingSession(
            createSession(clock),
            FileId.generate(),
            null,
            null
        );

        when(sessionManager.prepareSession(any(), any())).thenReturn(prepResult);

        // when
        PresignedUrlResponse response = facade.execute(cmd);

        // then
        assertThat(response.presignedUrl()).isNotEmpty();
        verify(s3ClientPort, never()).generatePresignedPutUrl(any(), any(), any(), any());
        verify(sessionManager, never()).completeSessionPreparation(any(), any());
    }

    private UploadSession createSession(Clock clock) {
        return UploadSession.initiate(
            SessionId.generate(),
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );
    }
}
```

#### Green (feat:)

```java
// GeneratePresignedUrlFacade.java
/**
 * Presigned URL 발급 Facade (Orchestration Pattern)
 * <p>
 * - Transaction 없음: 외부 API 호출 허용
 * - SessionManager에 Transaction 위임
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
        UserContext userContext = extractUserContext();

        SessionPreparationResult result = sessionManager.prepareSession(
            cmd,
            userContext
        );

        if (result.isExistingSession()) {
            return buildResponse(
                result.session(),
                result.fileId()
            );
        }

        PresignedUrl presignedUrl = s3ClientPort.generatePresignedPutUrl(
            result.s3Bucket(),
            result.s3Key(),
            cmd.mimeType(),
            Duration.ofMinutes(5)
        );

        UploadSession session = sessionManager.completeSessionPreparation(
            result.session().sessionId(),
            presignedUrl
        );

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
            300,
            "SINGLE"
        );
    }
}
```

#### 커밋

```bash
test: GeneratePresignedUrlFacade 테스트 추가 (Orchestration Pattern)
feat: GeneratePresignedUrlFacade 구현 (트랜잭션 없음, 외부 API 허용)
```

---

## Cycle 12: CompleteUploadService

**동일 패턴** (Transaction 전체 적용)

---

## 완료 조건

- [x] 4개 DTOs (Command 2, Response 2) - Record
- [x] UserContext (JWT 추출용)
- [x] SessionPreparationResult (멱등성 구분)
- [x] 2개 Port In (GeneratePresignedUrlUseCase, CompleteUploadUseCase)
- [x] 4개 Port Out (FilePersistencePort, UploadSessionPersistencePort, UploadSessionQueryPort, S3ClientPort)
- [x] SessionManager (Transaction 경계 관리)
- [x] GeneratePresignedUrlFacade (Orchestration Pattern)
- [x] CompleteUploadService (@Transactional)
- [x] Zero-Tolerance Rule #4 준수 (S3 호출은 트랜잭션 밖)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: TDD Plan 변환 완료 (Orchestration Pattern, Transaction 경계)
