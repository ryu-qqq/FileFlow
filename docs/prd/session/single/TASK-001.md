# TASK-001: Single Presigned URL Upload - TDD Cycles

**Bounded Context**: `session/single`
**Issue Key**: FILE-001
**작성일**: 2025-11-18
**예상 기간**: 5일
**TDD Cycles**: 20 Cycles

---

## 📋 TDD Workflow

각 Cycle은 **Red → Green → Refactor → Struct** 패턴을 따릅니다:

1. **Red**: `test:` 커밋 - 실패하는 테스트 작성
2. **Green**: `feat:` 커밋 - 최소 구현으로 테스트 통과
3. **Refactor**: (필요 시) 구조 개선 (동작 변경 없음)
4. **Struct**: `struct:` 커밋 - 별도 커밋 (Tidy First)

---

## Domain Layer (Cycles 1-10)

### Cycle 1: FileId VO

**목표**: UUID v7 기반 FileId VO 구현

**Red (test:)**:
```java
// FileIdTest.java
@Test
void UUID_v7_형식_검증() {
    FileId fileId = FileId.generate();
    assertThat(fileId.value()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
}

@Test
void 시간_순서_정렬_가능() {
    FileId id1 = FileId.generate();
    FileId id2 = FileId.generate();
    assertThat(id1.value()).isLessThan(id2.value());
}
```

**Green (feat:)**:
```java
public record FileId(String value) {
    public static FileId generate() {
        return new FileId(UuidCreator.getTimeOrderedEpoch().toString());
    }
    public String uuid() { return value; }
}
```

**커밋**:
- `test: FileId VO 테스트 추가 (UUID v7 형식, 시간 순서)`
- `feat: FileId VO 구현 (UUID v7)`

---

### Cycle 2: FileName, FileSize, MimeType VO

**목표**: 파일 기본 검증 VOs 구현

**Red (test:)**:
```java
// FileNameTest.java
@Test
void 정상_파일명_생성() {
    FileName fileName = FileName.of("example.jpg");
    assertThat(fileName.value()).isEqualTo("example.jpg");
}

@Test
void null_검증_실패() {
    assertThatThrownBy(() -> FileName.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("파일명은 필수입니다");
}

@Test
void 255자_초과_검증_실패() {
    String longName = "a".repeat(256);
    assertThatThrownBy(() -> FileName.of(longName))
        .isInstanceOf(IllegalArgumentException.class);
}

// FileSizeTest.java
@Test
void 정상_파일_크기_생성() {
    FileSize fileSize = FileSize.of(1048576L);
    assertThat(fileSize.bytes()).isEqualTo(1048576L);
}

@Test
void 1GB_초과_검증_실패() {
    assertThatThrownBy(() -> FileSize.of(1073741825L))
        .isInstanceOf(FileSizeExceededException.class);
}

// MimeTypeTest.java
@Test
void 허용된_MIME_타입_생성() {
    MimeType mimeType = MimeType.of("image/jpeg");
    assertThat(mimeType.value()).isEqualTo("image/jpeg");
}

@Test
void 허용되지_않은_MIME_타입_실패() {
    assertThatThrownBy(() -> MimeType.of("video/mp4"))
        .isInstanceOf(UnsupportedMimeTypeException.class);
}
```

**Green (feat:)**:
```java
// FileName, FileSize, MimeType 구현
```

**커밋**:
- `test: FileName, FileSize, MimeType VO 테스트 추가`
- `feat: FileName, FileSize, MimeType VO 구현`

---

### Cycle 3: S3Key, S3Bucket, TenantId, UploaderId VO

**목표**: 스토리지 경로 및 식별자 VOs 구현

**Red (test:)**:
```java
// S3KeyTest.java
@Test
void Admin_경로_생성() {
    S3Key s3Key = S3Key.generate(
        TenantId.of(1L),
        UploaderType.ADMIN,
        "connectly",
        FileCategory.of("banner", UploaderType.ADMIN),
        FileId.generate(),
        FileName.of("메인배너.jpg")
    );
    assertThat(s3Key.value()).startsWith("uploads/1/admin/connectly/banner/");
}

@Test
void Seller_경로_생성() {
    S3Key s3Key = S3Key.generate(
        TenantId.of(1L),
        UploaderType.SELLER,
        "samsung-electronics",
        FileCategory.of("product", UploaderType.SELLER),
        FileId.generate(),
        FileName.of("갤럭시.jpg")
    );
    assertThat(s3Key.value()).startsWith("uploads/1/seller/samsung-electronics/product/");
}

@Test
void Customer_경로_생성() {
    S3Key s3Key = S3Key.generate(
        TenantId.of(1L),
        UploaderType.CUSTOMER,
        "default",
        FileCategory.defaultCategory(),
        FileId.generate(),
        FileName.of("리뷰.jpg")
    );
    assertThat(s3Key.value()).startsWith("uploads/1/customer/default/");
}
```

**커밋**:
- `test: S3Key, S3Bucket, TenantId, UploaderId VO 테스트 추가`
- `feat: S3Key, S3Bucket, TenantId, UploaderId VO 구현`

---

### Cycle 4: FileCategory VO

**목표**: UploaderType별 카테고리 검증 구현

**Red (test:)**:
```java
@Test
void Admin_허용_카테고리_생성() {
    FileCategory category = FileCategory.of("banner", UploaderType.ADMIN);
    assertThat(category.value()).isEqualTo("banner");
}

@Test
void Admin_허용되지_않은_카테고리_실패() {
    assertThatThrownBy(() -> FileCategory.of("product", UploaderType.ADMIN))
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void Customer_기본_카테고리만_허용() {
    FileCategory category = FileCategory.of("default", UploaderType.CUSTOMER);
    assertThat(category.value()).isEqualTo("default");
}
```

**커밋**:
- `test: FileCategory VO 테스트 추가 (UploaderType별 검증)`
- `feat: FileCategory VO 구현`

---

### Cycle 5: SessionId, PresignedUrl VO

**목표**: 세션 관련 VOs 구현

**Red (test:)**:
```java
// SessionIdTest.java
@Test
void UUID_v7_생성() {
    SessionId sessionId = SessionId.generate();
    assertThat(sessionId.value()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
}

@Test
void 기존_UUID_문자열로_생성() {
    SessionId sessionId = SessionId.of("01234567-89ab-cdef-0123-456789abcdef");
    assertThat(sessionId.value()).isEqualTo("01234567-89ab-cdef-0123-456789abcdef");
}

// PresignedUrlTest.java
@Test
void Presigned_URL_생성() {
    PresignedUrl url = PresignedUrl.of("https://s3.amazonaws.com/...");
    assertThat(url.value()).startsWith("https://");
}
```

**커밋**:
- `test: SessionId, PresignedUrl VO 테스트 추가`
- `feat: SessionId, PresignedUrl VO 구현`

---

### Cycle 6: UploadSession Aggregate - 생성 및 만료 체크

**목표**: UploadSession 생성 및 만료 검증

**Red (test:)**:
```java
@Test
void 세션_초기화_성공() {
    UploadSession session = UploadSession.initiate(
        SessionId.generate(),
        TenantId.of(1L),
        FileName.of("test.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        UploadType.SINGLE,
        PresignedUrl.of("https://..."),
        Clock.systemUTC()
    );
    assertThat(session.status()).isEqualTo(SessionStatus.INITIATED);
}

@Test
void 만료_체크_성공() {
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    UploadSession session = UploadSession.initiate(..., clock);

    // 5분 이내
    assertThatCode(() -> session.ensureNotExpired(clock)).doesNotThrowAnyException();
}

@Test
void 만료_체크_실패() {
    Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    UploadSession session = UploadSession.initiate(..., clock);

    // 6분 후
    Clock afterClock = Clock.offset(clock, Duration.ofMinutes(6));
    assertThatThrownBy(() -> session.ensureNotExpired(afterClock))
        .isInstanceOf(SessionExpiredException.class);
}
```

**커밋**:
- `test: UploadSession 생성 및 만료 체크 테스트 추가`
- `feat: UploadSession Aggregate 구현 (생성, 만료 체크)`

---

### Cycle 7: UploadSession Aggregate - 상태 전환

**목표**: UploadSession 상태 전환 로직 구현

**Red (test:)**:
```java
@Test
void 상태_전환_성공_INITIATED_to_IN_PROGRESS() {
    UploadSession session = UploadSession.initiate(...);
    session.markAsInProgress(clock);
    assertThat(session.status()).isEqualTo(SessionStatus.IN_PROGRESS);
}

@Test
void 상태_전환_성공_IN_PROGRESS_to_COMPLETED() {
    UploadSession session = UploadSession.initiate(...);
    session.markAsInProgress(clock);
    session.markAsCompleted(clock);
    assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
}

@Test
void 완료된_세션_체크_실패() {
    UploadSession session = UploadSession.initiate(...);
    session.markAsCompleted(clock);

    assertThatThrownBy(() -> session.ensureNotCompleted())
        .isInstanceOf(SessionAlreadyCompletedException.class);
}
```

**커밋**:
- `test: UploadSession 상태 전환 테스트 추가`
- `feat: UploadSession 상태 전환 로직 구현`

---

### Cycle 8: File Aggregate - 생성

**목표**: File Aggregate 생성 로직 구현

**Red (test:)**:
```java
@Test
void File_생성_성공() {
    File file = File.createFromSession(
        FileId.generate(),
        FileName.of("test.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        S3Key.generate(...),
        S3Bucket.forTenant(TenantId.of(1L)),
        UploaderId.of(100L),
        UploaderType.ADMIN,
        "connectly",
        FileCategory.of("banner", UploaderType.ADMIN),
        TenantId.of(1L),
        Clock.systemUTC()
    );

    assertThat(file.status()).isEqualTo(FileStatus.COMPLETED);
    assertThat(file.uploaderType()).isEqualTo(UploaderType.ADMIN);
}
```

**커밋**:
- `test: File Aggregate 생성 테스트 추가`
- `feat: File Aggregate 구현`

---

### Cycle 9: Domain Exceptions

**목표**: Domain Exceptions 구현

**Red (test:)**:
```java
@Test
void SessionExpiredException_메시지_검증() {
    SessionId sessionId = SessionId.generate();
    SessionExpiredException exception = new SessionExpiredException(sessionId);
    assertThat(exception.getMessage()).contains("세션이 만료되었습니다");
}

@Test
void FileSizeExceededException_메시지_검증() {
    FileSizeExceededException exception = new FileSizeExceededException(2L * 1024 * 1024 * 1024, 1L * 1024 * 1024 * 1024);
    assertThat(exception.getMessage()).contains("파일 크기 초과");
}
```

**커밋**:
- `test: Domain Exceptions 테스트 추가`
- `feat: Domain Exceptions 구현 (5개)`

---

### Cycle 10: ArchUnit - Domain Layer 규칙 검증

**목표**: Domain Layer ArchUnit 테스트 작성

**Red (test:)**:
```java
@Test
void Domain_Layer는_Lombok을_사용하지_않는다() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("lombok..")
        .check(importedClasses);
}

@Test
void Aggregate는_public_정적_팩토리_메서드를_가진다() {
    classes()
        .that().resideInAPackage("..domain..")
        .and().areAnnotatedWith(AggregateRoot.class)
        .should().haveOnlyPrivateConstructors()
        .check(importedClasses);
}
```

**커밋**:
- `test: Domain Layer ArchUnit 테스트 추가`
- `feat: ArchUnit 테스트 통과 (Domain Layer)`

---

## Application Layer (Cycles 11-14)

### Cycle 11: GeneratePresignedUrlCommand, Response DTOs

**목표**: Application Layer Command/Response DTOs 구현

**Red (test:)**:
```java
@Test
void GeneratePresignedUrlCommand_생성() {
    GeneratePresignedUrlCommand cmd = new GeneratePresignedUrlCommand(
        SessionId.generate(),
        FileName.of("test.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        FileCategory.of("banner", UploaderType.ADMIN)
    );
    assertThat(cmd.sessionId()).isNotNull();
}
```

**커밋**:
- `test: Application DTOs 테스트 추가`
- `feat: Application DTOs 구현 (Command, Response)`

---

### Cycle 12: SessionManager - prepareSession()

**목표**: SessionManager의 prepareSession() 구현

**Red (test:)**:
```java
@Test
void 새_세션_준비_성공() {
    // Given
    GeneratePresignedUrlCommand cmd = GeneratePresignedUrlCommandFixture.create();
    UserContext userContext = UserContextFixture.admin();

    when(uploadSessionQueryPort.findBySessionId(any())).thenReturn(Optional.empty());

    // When
    SessionPreparationResult result = sessionManager.prepareSession(cmd, userContext);

    // Then
    assertThat(result.isExistingSession()).isFalse();
    verify(uploadSessionPersistencePort).save(any());
}

@Test
void 기존_세션_반환_멱등성() {
    // Given
    UploadSession existingSession = UploadSessionFixture.initiated();
    when(uploadSessionQueryPort.findBySessionId(any())).thenReturn(Optional.of(existingSession));

    // When
    SessionPreparationResult result = sessionManager.prepareSession(cmd, userContext);

    // Then
    assertThat(result.isExistingSession()).isTrue();
    verify(uploadSessionPersistencePort, never()).save(any());
}
```

**커밋**:
- `test: SessionManager.prepareSession() 테스트 추가`
- `feat: SessionManager.prepareSession() 구현`

---

### Cycle 13: GeneratePresignedUrlFacade - Orchestration

**목표**: GeneratePresignedUrlFacade 구현 (Orchestration Pattern)

**Red (test:)**:
```java
@Test
void Presigned_URL_발급_성공() {
    // Given
    SessionPreparationResult prepResult = SessionPreparationResultFixture.newSession();
    when(sessionManager.prepareSession(any(), any())).thenReturn(prepResult);

    PresignedUrl presignedUrl = PresignedUrl.of("https://s3.amazonaws.com/...");
    when(s3ClientPort.generatePresignedPutUrl(any(), any(), any(), any())).thenReturn(presignedUrl);

    UploadSession completedSession = UploadSessionFixture.inProgress();
    when(sessionManager.completeSessionPreparation(any(), any())).thenReturn(completedSession);

    // When
    PresignedUrlResponse response = facade.execute(command);

    // Then
    assertThat(response.presignedUrl()).isEqualTo(presignedUrl.value());
    assertThat(response.uploadType()).isEqualTo("SINGLE");

    // Transaction 경계 검증
    InOrder inOrder = inOrder(sessionManager, s3ClientPort);
    inOrder.verify(sessionManager).prepareSession(any(), any());  // 트랜잭션 안
    inOrder.verify(s3ClientPort).generatePresignedPutUrl(any(), any(), any(), any());  // 트랜잭션 밖
    inOrder.verify(sessionManager).completeSessionPreparation(any(), any());  // 트랜잭션 안
}
```

**커밋**:
- `test: GeneratePresignedUrlFacade 테스트 추가 (Orchestration)`
- `feat: GeneratePresignedUrlFacade 구현 (Transaction 경계 분리)`

---

### Cycle 14: CompleteUploadService

**목표**: CompleteUploadService 구현

**Red (test:)**:
```java
@Test
void 업로드_완료_처리_성공() {
    // Given
    UploadSession session = UploadSessionFixture.inProgress();
    when(uploadSessionQueryPort.findBySessionId(any())).thenReturn(Optional.of(session));

    File savedFile = FileFixture.completed();
    when(filePersistencePort.save(any())).thenReturn(savedFile);

    // When
    FileResponse response = service.execute(command);

    // Then
    assertThat(response.status()).isEqualTo("COMPLETED");
    verify(uploadSessionPersistencePort).update(any());
}

@Test
void 만료된_세션_실패() {
    // Given
    UploadSession expiredSession = UploadSessionFixture.expired();
    when(uploadSessionQueryPort.findBySessionId(any())).thenReturn(Optional.of(expiredSession));

    // When & Then
    assertThatThrownBy(() -> service.execute(command))
        .isInstanceOf(SessionExpiredException.class);
}
```

**커밋**:
- `test: CompleteUploadService 테스트 추가`
- `feat: CompleteUploadService 구현`

---

## Persistence Layer (Cycles 15-17)

### Cycle 15: JPA Entities 및 Mappers

**목표**: JPA Entities 및 Domain ↔ Entity Mappers 구현

**Red (test:)**:
```java
@Test
void File_Domain을_Entity로_변환() {
    File domain = FileFixture.completed();
    FileJpaEntity entity = FileMapper.toEntity(domain);

    assertThat(entity.getFileId()).isEqualTo(domain.fileId().value());
    assertThat(entity.getFileName()).isEqualTo(domain.fileName().value());
}

@Test
void Entity를_Domain으로_변환() {
    FileJpaEntity entity = FileJpaEntityFixture.create();
    File domain = FileMapper.toDomain(entity);

    assertThat(domain.fileId().value()).isEqualTo(entity.getFileId());
}
```

**커밋**:
- `test: JPA Entities 및 Mappers 테스트 추가`
- `feat: JPA Entities 및 Mappers 구현`

---

### Cycle 16: Flyway Migrations

**목표**: Flyway Migration 작성 및 검증

**Red (test:)**:
```java
@Test
@Sql(scripts = "/db/migration/V1__create_files_table.sql")
void files_테이블_생성_검증() {
    String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'files'";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
    assertThat(count).isEqualTo(1);
}

@Test
@Sql(scripts = {
    "/db/migration/V1__create_files_table.sql",
    "/db/migration/V2__create_upload_sessions_table.sql"
})
void upload_sessions_테이블_생성_검증() {
    String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'upload_sessions'";
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
    assertThat(count).isEqualTo(1);
}
```

**커밋**:
- `test: Flyway Migration 테스트 추가`
- `feat: Flyway Migration 구현 (V1, V2)`

---

### Cycle 17: S3ClientAdapter

**목표**: S3ClientAdapter 구현

**Red (test:)**:
```java
@Test
void Presigned_URL_생성_성공() {
    // Given (LocalStack 사용)
    S3Bucket bucket = S3Bucket.forTenant(TenantId.of(1L));
    S3Key key = S3Key.generate(...);
    MimeType mimeType = MimeType.of("image/jpeg");

    // When
    PresignedUrl presignedUrl = adapter.generatePresignedPutUrl(
        bucket, key, mimeType, Duration.ofMinutes(5)
    );

    // Then
    assertThat(presignedUrl.value()).startsWith("https://");
    assertThat(presignedUrl.value()).contains(bucket.value());
}
```

**커밋**:
- `test: S3ClientAdapter 테스트 추가 (LocalStack)`
- `feat: S3ClientAdapter 구현`

---

## REST API Layer (Cycles 18-19)

### Cycle 18: FileApiController - POST /presigned-url

**목표**: Presigned URL 발급 API 구현

**Red (test:)**:
```java
@Test
void Presigned_URL_발급_성공() {
    // Given
    GeneratePresignedUrlRequest request = GeneratePresignedUrlRequestFixture.create();
    PresignedUrlResponse expectedResponse = PresignedUrlResponseFixture.create();
    when(generatePresignedUrlUseCase.execute(any())).thenReturn(expectedResponse);

    // When
    ResponseEntity<PresignedUrlResponse> response = restTemplate.postForEntity(
        "/api/v1/files/presigned-url",
        request,
        PresignedUrlResponse.class
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().presignedUrl()).isNotBlank();
}
```

**커밋**:
- `test: POST /presigned-url API 테스트 추가`
- `feat: POST /presigned-url API 구현`

---

### Cycle 19: FileApiController - POST /upload-complete

**목표**: 업로드 완료 API 구현

**Red (test:)**:
```java
@Test
void 업로드_완료_성공() {
    // Given
    CompleteUploadRequest request = CompleteUploadRequestFixture.create();
    FileResponse expectedResponse = FileResponseFixture.create();
    when(completeUploadUseCase.execute(any())).thenReturn(expectedResponse);

    // When
    ResponseEntity<FileResponse> response = restTemplate.postForEntity(
        "/api/v1/files/upload-complete",
        request,
        FileResponse.class
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo("COMPLETED");
}
```

**커밋**:
- `test: POST /upload-complete API 테스트 추가`
- `feat: POST /upload-complete API 구현`

---

## E2E Test (Cycle 20)

### Cycle 20: E2E 플로우 테스트

**목표**: Presigned URL 발급 → S3 업로드 → 완료 처리 E2E 테스트

**Red (test:)**:
```java
@Test
void E2E_Presigned_URL_발급_업로드_완료() {
    // 1. Presigned URL 발급
    GeneratePresignedUrlRequest urlRequest = GeneratePresignedUrlRequestFixture.create();
    ResponseEntity<PresignedUrlResponse> urlResponse = restTemplate.postForEntity(
        "/api/v1/files/presigned-url",
        urlRequest,
        PresignedUrlResponse.class
    );

    assertThat(urlResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String presignedUrl = urlResponse.getBody().presignedUrl();
    String sessionId = urlResponse.getBody().sessionId();

    // 2. S3로 직접 업로드 (LocalStack)
    byte[] fileContent = "test content".getBytes();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);

    ResponseEntity<Void> uploadResponse = restTemplate.exchange(
        presignedUrl,
        HttpMethod.PUT,
        new HttpEntity<>(fileContent, headers),
        Void.class
    );

    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 3. 업로드 완료 알림
    CompleteUploadRequest completeRequest = new CompleteUploadRequest(sessionId);
    ResponseEntity<FileResponse> completeResponse = restTemplate.postForEntity(
        "/api/v1/files/upload-complete",
        completeRequest,
        FileResponse.class
    );

    assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(completeResponse.getBody().status()).isEqualTo("COMPLETED");
}
```

**커밋**:
- `test: E2E 플로우 테스트 추가 (Presigned URL → S3 → 완료)`
- `feat: E2E 테스트 통과`

---

## 완료 조건

### Domain Layer
- [x] 11개 VO 구현 및 테스트 통과
- [x] UploadSession Aggregate 구현 및 테스트 통과
- [x] File Aggregate 구현 및 테스트 통과
- [x] 5개 Domain Exceptions 구현
- [x] ArchUnit 테스트 통과

### Application Layer
- [x] GeneratePresignedUrlFacade 구현 (Orchestration Pattern)
- [x] SessionManager 구현 (Transaction 경계)
- [x] CompleteUploadService 구현
- [x] Port 인터페이스 구현

### Persistence Layer
- [x] JPA Entities 및 Mappers 구현
- [x] Flyway Migration 완료 (V1, V2)
- [x] S3ClientAdapter 구현

### REST API Layer
- [x] POST /api/v1/files/presigned-url 구현
- [x] POST /api/v1/files/upload-complete 구현
- [x] GlobalExceptionHandler 구현

### E2E Test
- [x] Presigned URL 발급 → S3 업로드 → 완료 처리 플로우 통과

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (20 TDD Cycles)
