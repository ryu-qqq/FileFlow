# Persistence Layer TDD Plan - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Persistence
**작성일**: 2025-11-18
**TDD Methodology**: Kent Beck Red-Green-Refactor

---

## 📋 목차

1. [Cycle 1-2: JPA Entities (2개)](#cycle-1-2-jpa-entities)
2. [Cycle 3-4: Flyway Migrations (2개)](#cycle-3-4-flyway-migrations)
3. [Cycle 5-6: JPA Repositories (2개)](#cycle-5-6-repositories)
4. [Cycle 7-9: Persistence Adapters (3개)](#cycle-7-9-adapters)
5. [Cycle 10: S3ClientAdapter](#cycle-10-s3-adapter)

**전체 10 Cycles**

---

## Cycle 1-2: JPA Entities

### Cycle 1: FileJpaEntity

#### Red (test:)

```java
// FileJpaEntityTest.java
@DataJpaTest
class FileJpaEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("FileJpaEntity를 저장해야 한다")
    void shouldSaveFileJpaEntity() {
        FileJpaEntity entity = new FileJpaEntity();
        entity.setFileId("01JD8001-1234-5678-9abc-def012345678");
        entity.setFileName("example.jpg");
        entity.setFileSize(1048576L);
        entity.setMimeType("image/jpeg");
        entity.setS3Key("uploads/1/admin/connectly/banner/01JD8001_example.jpg");
        entity.setS3Bucket("fileflow-uploads-1");
        entity.setUploaderId(100L);
        entity.setUploaderType("ADMIN");
        entity.setUploaderSlug("connectly");
        entity.setCategory("banner");
        entity.setTenantId(1L);
        entity.setStatus("COMPLETED");

        FileJpaEntity saved = entityManager.persistAndFlush(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileId()).isEqualTo("01JD8001-1234-5678-9abc-def012345678");
    }

    @Test
    @DisplayName("fileId는 unique 제약 조건을 가져야 한다")
    void shouldEnforceUniqueFileId() {
        FileJpaEntity entity1 = createEntity("01JD8001-1234-5678-9abc-def012345678");
        FileJpaEntity entity2 = createEntity("01JD8001-1234-5678-9abc-def012345678");

        entityManager.persistAndFlush(entity1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(entity2))
            .isInstanceOf(PersistenceException.class);
    }

    private FileJpaEntity createEntity(String fileId) {
        FileJpaEntity entity = new FileJpaEntity();
        entity.setFileId(fileId);
        entity.setFileName("example.jpg");
        entity.setFileSize(1048576L);
        entity.setMimeType("image/jpeg");
        entity.setS3Key("uploads/1/admin/connectly/banner/" + fileId + "_example.jpg");
        entity.setS3Bucket("fileflow-uploads-1");
        entity.setUploaderId(100L);
        entity.setUploaderType("ADMIN");
        entity.setUploaderSlug("connectly");
        entity.setCategory("banner");
        entity.setTenantId(1L);
        entity.setStatus("COMPLETED");
        return entity;
    }
}
```

#### Green (feat:)

```java
// FileJpaEntity.java
@Entity
@Table(name = "files")
public class FileJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true, length = 36)
    private String fileId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "s3_bucket", nullable = false, length = 100)
    private String s3Bucket;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "uploader_type", nullable = false, length = 20)
    private String uploaderType;

    @Column(name = "uploader_slug", nullable = false, length = 100)
    private String uploaderSlug;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Getter, Setter (Plain Java, Lombok 금지)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public String getUploaderType() { return uploaderType; }
    public void setUploaderType(String uploaderType) { this.uploaderType = uploaderType; }
    public String getUploaderSlug() { return uploaderSlug; }
    public void setUploaderSlug(String uploaderSlug) { this.uploaderSlug = uploaderSlug; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 커밋

```bash
test: FileJpaEntity 테스트 추가 (Unique fileId 제약 검증)
feat: FileJpaEntity 구현 (Lombok 금지, Long FK 전략)
```

---

### Cycle 2: UploadSessionJpaEntity

**동일 패턴**

---

## Cycle 3-4: Flyway Migrations

### Cycle 3: V1__create_files_table.sql

#### Red (test:)

```java
// FlywayMigrationTest.java (files 테이블 생성 검증)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class FlywayMigrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb");

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("files 테이블이 생성되어야 한다")
    void shouldCreateFilesTable() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "files", null);

            assertThat(tables.next()).isTrue();
        }
    }

    @Test
    @DisplayName("files.file_id는 unique 인덱스를 가져야 한다")
    void shouldHaveUniqueIndexOnFileId() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "files", true, false);

            boolean hasFileIdIndex = false;
            while (indexes.next()) {
                if ("file_id".equals(indexes.getString("COLUMN_NAME"))) {
                    hasFileIdIndex = true;
                    break;
                }
            }

            assertThat(hasFileIdIndex).isTrue();
        }
    }
}
```

#### Green (feat:)

```sql
-- V1__create_files_table.sql
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(100) NOT NULL,
    uploader_id BIGINT NOT NULL,
    uploader_type VARCHAR(20) NOT NULL,
    uploader_slug VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_file_id (file_id),
    INDEX idx_uploader (uploader_id, uploader_type),
    INDEX idx_tenant_created (tenant_id, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 커밋

```bash
test: files 테이블 Flyway Migration 테스트 추가
feat: V1__create_files_table.sql 구현 (Unique 제약, 인덱스)
```

---

### Cycle 4: V2__create_upload_sessions_table.sql

**동일 패턴**

---

## Cycle 5-6: JPA Repositories

### Cycle 5: FileJpaRepository

#### Red (test:)

```java
// FileJpaRepositoryTest.java
@DataJpaTest
class FileJpaRepositoryTest {

    @Autowired
    private FileJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("fileId로 FileJpaEntity를 조회해야 한다")
    void shouldFindByFileId() {
        FileJpaEntity entity = createEntity("01JD8001-1234-5678-9abc-def012345678");
        entityManager.persistAndFlush(entity);
        entityManager.clear();

        Optional<FileJpaEntity> found = repository.findByFileId("01JD8001-1234-5678-9abc-def012345678");

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("example.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 fileId 조회 시 empty를 반환해야 한다")
    void shouldReturnEmptyWhenFileIdNotFound() {
        Optional<FileJpaEntity> found = repository.findByFileId("non-existent-id");

        assertThat(found).isEmpty();
    }

    private FileJpaEntity createEntity(String fileId) {
        FileJpaEntity entity = new FileJpaEntity();
        entity.setFileId(fileId);
        entity.setFileName("example.jpg");
        entity.setFileSize(1048576L);
        entity.setMimeType("image/jpeg");
        entity.setS3Key("uploads/1/admin/connectly/banner/" + fileId + "_example.jpg");
        entity.setS3Bucket("fileflow-uploads-1");
        entity.setUploaderId(100L);
        entity.setUploaderType("ADMIN");
        entity.setUploaderSlug("connectly");
        entity.setCategory("banner");
        entity.setTenantId(1L);
        entity.setStatus("COMPLETED");
        return entity;
    }
}
```

#### Green (feat:)

```java
// FileJpaRepository.java
public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    Optional<FileJpaEntity> findByFileId(String fileId);
}
```

#### 커밋

```bash
test: FileJpaRepository 테스트 추가 (findByFileId 조회)
feat: FileJpaRepository 구현 (Spring Data JPA)
```

---

### Cycle 6: UploadSessionJpaRepository

**동일 패턴** (findBySessionId)

---

## Cycle 7-9: Persistence Adapters

### Cycle 7: FilePersistenceAdapter

#### Red (test:)

```java
// FilePersistenceAdapterTest.java
@SpringBootTest
@Transactional
class FilePersistenceAdapterTest {

    @Autowired
    private FilePersistenceAdapter adapter;

    @Autowired
    private FileJpaRepository repository;

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
    }

    @Test
    @DisplayName("File Aggregate를 저장해야 한다")
    void shouldSaveFileAggregate() {
        File file = File.createFromSession(
            FileId.of("01JD8001-1234-5678-9abc-def012345678"),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            S3Key.of("uploads/1/admin/connectly/banner/01JD8001_example.jpg"),
            S3Bucket.of("fileflow-uploads-1"),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly",
            FileCategory.of("banner", UploaderType.ADMIN),
            TenantId.of(1L),
            clock
        );

        File saved = adapter.save(file);

        assertThat(saved.fileId().value()).isEqualTo("01JD8001-1234-5678-9abc-def012345678");
        assertThat(repository.findByFileId("01JD8001-1234-5678-9abc-def012345678")).isPresent();
    }
}
```

#### Green (feat:)

```java
// FilePersistenceAdapter.java
@Component
public class FilePersistenceAdapter implements FilePersistencePort {

    private final FileJpaRepository repository;
    private final FileMapper mapper;

    public FilePersistenceAdapter(
        FileJpaRepository repository,
        FileMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public File save(File file) {
        FileJpaEntity entity = mapper.toEntity(file);
        FileJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

#### 커밋

```bash
test: FilePersistenceAdapter 테스트 추가 (Aggregate → Entity 변환)
feat: FilePersistenceAdapter 구현 (FilePersistencePort)
```

---

### Cycle 8: UploadSessionPersistenceAdapter

**동일 패턴** (save, update)

---

### Cycle 9: UploadSessionQueryAdapter

**동일 패턴** (findBySessionId)

---

## Cycle 10: S3ClientAdapter

#### Red (test:)

```java
// S3ClientAdapterTest.java
@SpringBootTest
class S3ClientAdapterTest {

    @MockBean
    private S3Presigner presigner;

    @Autowired
    private S3ClientAdapter adapter;

    @Test
    @DisplayName("S3 Presigned PUT URL을 생성해야 한다")
    void shouldGeneratePresignedPutUrl() {
        // given
        S3Bucket bucket = S3Bucket.of("fileflow-uploads-1");
        S3Key key = S3Key.of("uploads/1/admin/connectly/banner/01JD8001_example.jpg");
        MimeType mimeType = MimeType.of("image/jpeg");
        Duration expiration = Duration.ofMinutes(5);

        PresignedPutObjectRequest mockRequest = mock(PresignedPutObjectRequest.class);
        when(mockRequest.url()).thenReturn(URI.create("https://s3.amazonaws.com/presigned").toURL());
        when(presigner.presignPutObject(any(Consumer.class))).thenReturn(mockRequest);

        // when
        PresignedUrl result = adapter.generatePresignedPutUrl(bucket, key, mimeType, expiration);

        // then
        assertThat(result.value()).contains("https://s3.amazonaws.com/presigned");
        verify(presigner).presignPutObject(any(Consumer.class));
    }
}
```

#### Green (feat:)

```java
// S3ClientAdapter.java
@Component
public class S3ClientAdapter implements S3ClientPort {

    private final S3Presigner presigner;

    public S3ClientAdapter(S3Presigner presigner) {
        this.presigner = presigner;
    }

    @Override
    public PresignedUrl generatePresignedPutUrl(
        S3Bucket bucket,
        S3Key key,
        MimeType mimeType,
        Duration expiration
    ) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket.value())
            .key(key.value())
            .contentType(mimeType.value())
            .build();

        PresignedPutObjectRequest presignedRequest =
            presigner.presignPutObject(r -> r
                .putObjectRequest(putRequest)
                .signatureDuration(expiration)
            );

        return PresignedUrl.of(presignedRequest.url().toString());
    }
}
```

#### 커밋

```bash
test: S3ClientAdapter 테스트 추가 (Presigned URL 생성)
feat: S3ClientAdapter 구현 (AWS S3 SDK 래핑)
```

---

## 완료 조건

- [x] 2개 JPA Entities (FileJpaEntity, UploadSessionJpaEntity) - Lombok 금지
- [x] 2개 Flyway Migrations (files, upload_sessions 테이블)
- [x] 2개 JPA Repositories (FileJpaRepository, UploadSessionJpaRepository)
- [x] 3개 Persistence Adapters (FilePersistenceAdapter, UploadSessionPersistenceAdapter, UploadSessionQueryAdapter)
- [x] 1개 S3ClientAdapter (AWS S3 SDK 래핑)
- [x] Testcontainers 통합 테스트
- [x] Long FK 전략 준수 (JPA 관계 어노테이션 금지)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: TDD Plan 변환 완료 (Flyway, Testcontainers)
