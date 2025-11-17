# FILE-003: Persistence Layer 구현

**Epic**: File Management System
**Layer**: Persistence Layer (Adapter-Out)
**브랜치**: feature/FILE-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 관리 시스템의 데이터 영속성을 담당합니다. JPA Entity, Repository, Adapter를 구현하여 Application Layer의 Port를 충족합니다. Long FK 전략을 준수하고, QueryDSL을 통해 복잡한 쿼리를 최적화합니다.

---

## 🎯 요구사항

### JPA Entity 설계

#### A. FileJpaEntity

- [ ] **테이블 설계** (`files`)
  - `id`: Long (PK, Auto Increment)
  - `file_id`: String (UUID v7, Unique, Not Null)
  - `file_name`: String (Not Null)
  - `file_size`: Long (Not Null, CHECK > 0)
  - `mime_type`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `s3_key`: String (Not Null)
  - `s3_bucket`: String (Not Null)
  - `cdn_url`: String (Nullable)
  - `uploader_id`: Long (FK, Not Null, Index) ← **Long FK 전략**
  - `category`: String (Nullable, Index)
  - `tags`: String (JSON, Nullable)
  - `version`: Integer (Not Null, Default: 1)
  - `deleted_at`: LocalDateTime (Nullable)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `updated_at`: LocalDateTime (Not Null)

- [ ] **인덱스 설계**
  - Primary Key: `id`
  - Unique: `file_id`
  - 복합 인덱스: `(uploader_id, status, created_at DESC)` - 사용자별 상태 필터링 + 정렬 최적화
  - 단일 인덱스: `category` (카테고리별 조회)

- [ ] **Optimistic Lock**
  - `@Version` 필드 추가 (동시성 제어)

#### B. FileProcessingJobJpaEntity

- [ ] **테이블 설계** (`file_processing_jobs`)
  - `id`: Long (PK, Auto Increment)
  - `job_id`: String (UUID v7, Unique, Not Null)
  - `file_id`: String (FK, Not Null, Index) ← **Long FK 전략**
  - `job_type`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default: 0)
  - `max_retry_count`: Integer (Not Null, Default: 2)
  - `input_s3_key`: String (Not Null)
  - `output_s3_key`: String (Nullable)
  - `error_message`: String (Nullable)
  - `created_at`: LocalDateTime (Not Null)
  - `processed_at`: LocalDateTime (Nullable)

- [ ] **인덱스 설계**
  - Primary Key: `id`
  - Unique: `job_id`
  - 복합 인덱스: `(file_id, status)` - 파일별 상태 필터링

#### C. MessageOutboxJpaEntity

- [ ] **테이블 설계** (`message_outbox`)
  - `id`: Long (PK, Auto Increment)
  - `event_type`: String (Not Null)
  - `aggregate_id`: String (Not Null)
  - `payload`: String (JSON, Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default: 0)
  - `max_retry_count`: Integer (Not Null, Default: 3)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `processed_at`: LocalDateTime (Nullable)

- [ ] **인덱스 설계**
  - Primary Key: `id`
  - 복합 인덱스: `(status, created_at)` - 스케줄러 성능 최적화

### Repository 구현

#### A. JpaRepository

- [ ] **FileJpaRepository**
  - `findByFileId(String fileId): Optional<FileJpaEntity>`
  - `findByUploaderIdAndStatusWithCursor(...)`: Cursor Pagination

- [ ] **FileProcessingJobJpaRepository**
  - `findByFileId(String fileId): List<FileProcessingJobJpaEntity>`
  - `findByJobId(String jobId): Optional<FileProcessingJobJpaEntity>`

- [ ] **MessageOutboxJpaRepository**
  - `findPendingMessages(LocalDateTime threshold, Pageable): List<MessageOutboxJpaEntity>`

#### B. QueryDSL Repository

- [ ] **FileQueryDslRepository**
  - `findByUploaderIdAndStatusAndCategoryWithCursor(...)`: 복잡한 필터링 + Cursor Pagination
  - DTO Projection 최적화 (N+1 방지)

- [ ] **FileProcessingJobQueryDslRepository**
  - `findByFileIdWithDetails(String fileId)`: File 정보 포함 조회

### Adapter 구현

#### A. Command Adapter

- [ ] **FileCommandAdapter** (FileCommandPort 구현)
  - `save(File file): File` - Domain → JpaEntity 변환 + 저장
  - `saveAll(List<File> files): List<File>`
  - `updateStatus(String fileId, FileStatus status): void`
  - `softDelete(String fileId): void`

- [ ] **FileProcessingJobCommandAdapter**
  - `save(FileProcessingJob job): FileProcessingJob`
  - `saveAll(List<FileProcessingJob> jobs): List<FileProcessingJob>`
  - `updateStatus(String jobId, JobStatus status): void`

- [ ] **MessageOutboxCommandAdapter**
  - `save(MessageOutbox outbox): MessageOutbox`

#### B. Query Adapter

- [ ] **FileQueryAdapter** (FileQueryPort 구현)
  - `findById(String fileId): Optional<File>` - JpaEntity → Domain 변환
  - `findByIdWithLock(String fileId): Optional<File>` (Optimistic Lock)
  - `findByUploaderIdAndStatusWithCursor(...)`: CursorPageResponse<File>

- [ ] **FileProcessingJobQueryAdapter**
  - `findByFileId(String fileId): List<FileProcessingJob>`
  - `findById(String jobId): Optional<FileProcessingJob>`

- [ ] **MessageOutboxQueryAdapter**
  - `findPendingMessages(int limit): List<MessageOutbox>`

### Mapper 구현

- [ ] **FileMapper**
  - `toJpaEntity(File domain): FileJpaEntity`
  - `toDomain(FileJpaEntity entity): File`
  - `toJpaEntities(List<File> domains): List<FileJpaEntity>`
  - `toDomains(List<FileJpaEntity> entities): List<File>`

- [ ] **FileProcessingJobMapper**
  - `toJpaEntity(FileProcessingJob domain): FileProcessingJobJpaEntity`
  - `toDomain(FileProcessingJobJpaEntity entity): FileProcessingJob`

- [ ] **MessageOutboxMapper**
  - `toJpaEntity(MessageOutbox domain): MessageOutboxJpaEntity`
  - `toDomain(MessageOutboxJpaEntity entity): MessageOutbox`

### Flyway Migration

- [ ] **V1__create_files_table.sql**
  - `files` 테이블 생성
  - 인덱스 생성
  - CHECK 제약조건 (file_size > 0)

- [ ] **V2__create_file_processing_jobs_table.sql**
  - `file_processing_jobs` 테이블 생성
  - 인덱스 생성

- [ ] **V3__create_message_outbox_table.sql**
  - `message_outbox` 테이블 생성
  - 인덱스 생성

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Long FK 전략** (JPA 관계 어노테이션 금지)
  - `private Long uploaderId;` (O)
  - `@ManyToOne private User user;` (X)
  - `private String fileId;` (O, FileProcessingJob에서)
  - `@ManyToOne private File file;` (X)

- [ ] **QueryDSL 최적화**
  - N+1 방지 (DTO Projection 사용)
  - Join 최소화
  - 인덱스 활용 쿼리 작성

- [ ] **Lombok 금지**
  - Pure Java 또는 Record 사용
  - Mapper는 클래스 (상태 없음)

- [ ] **BaseAuditEntity 상속**
  - createdAt, updatedAt 자동 관리
  - @EntityListeners(AuditingEntityListener.class)

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - JPA 관계 어노테이션 사용 금지 검증
  - Lombok 사용 금지 검증
  - Adapter는 Port 인터페이스 구현 검증

- [ ] **Integration Test (TestContainers)**
  - MySQL TestContainer 사용
  - Flyway 마이그레이션 자동 실행
  - Repository CRUD 테스트
  - QueryDSL 쿼리 테스트
  - Cursor Pagination 테스트

- [ ] **테스트 커버리지 > 80%**
  - Mapper 변환 로직 테스트
  - Adapter 테스트

---

## ✅ 완료 조건

- [ ] 3개 JPA Entity 구현 완료
- [ ] 3개 JpaRepository 구현 완료
- [ ] 2개 QueryDSL Repository 구현 완료
- [ ] 3개 Command Adapter 구현 완료
- [ ] 3개 Query Adapter 구현 완료
- [ ] 3개 Mapper 구현 완료
- [ ] 3개 Flyway Migration SQL 작성
- [ ] Integration Test (TestContainers) 통과
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/file-management-system.md
- **Plan**: docs/prd/plans/FILE-003-persistence-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **컨벤션**: docs/coding_convention/04-persistence-layer/

---

## 📝 참고사항

### Long FK 전략 예시
```java
@Entity
@Table(name = "files")
public class FileJpaEntity extends BaseAuditEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", unique = true, nullable = false)
    private String fileId;

    // ✅ Long FK 전략 (관계 어노테이션 사용 안 함)
    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    // ❌ 금지 (JPA 관계 어노테이션)
    // @ManyToOne
    // @JoinColumn(name = "uploader_id")
    // private User user;
}
```

### Cursor Pagination 예시
```java
public interface FileQueryDslRepository {
    CursorPageResponse<File> findByUploaderIdAndStatusWithCursor(
        Long uploaderId,
        String status,
        LocalDateTime cursor,
        int size
    );
}

// 구현
@Repository
public class FileQueryDslRepositoryImpl implements FileQueryDslRepository {
    @Override
    public CursorPageResponse<File> findByUploaderIdAndStatusWithCursor(...) {
        QFileJpaEntity file = QFileJpaEntity.fileJpaEntity;

        List<FileJpaEntity> entities = queryFactory
            .selectFrom(file)
            .where(
                file.uploaderId.eq(uploaderId),
                file.status.eq(status),
                file.createdAt.lt(cursor), // cursor 기반
                file.deletedAt.isNull()
            )
            .orderBy(file.createdAt.desc())
            .limit(size + 1) // hasNext 확인용
            .fetch();

        boolean hasNext = entities.size() > size;
        List<FileJpaEntity> content = hasNext
            ? entities.subList(0, size)
            : entities;

        LocalDateTime nextCursor = hasNext
            ? content.get(content.size() - 1).getCreatedAt()
            : null;

        return new CursorPageResponse<>(
            fileMapper.toDomains(content),
            nextCursor,
            hasNext
        );
    }
}
```

### Flyway Migration 예시
```sql
-- V1__create_files_table.sql
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(100) NOT NULL,
    cdn_url VARCHAR(500),
    uploader_id BIGINT NOT NULL,
    category VARCHAR(100),
    tags JSON,
    version INT NOT NULL DEFAULT 1,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_uploader_status_created (uploader_id, status, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Mapper 예시
```java
public class FileMapper {
    public FileJpaEntity toJpaEntity(File domain) {
        FileJpaEntity entity = new FileJpaEntity();
        entity.setFileId(domain.getFileId());
        entity.setFileName(domain.getFileName());
        entity.setFileSize(domain.getFileSize());
        entity.setMimeType(domain.getMimeType());
        entity.setStatus(domain.getStatus().name());
        entity.setS3Key(domain.getS3Key());
        entity.setS3Bucket(domain.getS3Bucket());
        entity.setCdnUrl(domain.getCdnUrl());
        entity.setUploaderId(domain.getUploaderId());
        entity.setCategory(domain.getCategory());
        entity.setTags(toJson(domain.getTags()));
        entity.setVersion(domain.getVersion());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    public File toDomain(FileJpaEntity entity) {
        return new File(
            entity.getFileId(),
            entity.getFileName(),
            entity.getFileSize(),
            entity.getMimeType(),
            FileStatus.valueOf(entity.getStatus()),
            entity.getS3Key(),
            entity.getS3Bucket(),
            entity.getCdnUrl(),
            entity.getUploaderId(),
            entity.getCategory(),
            fromJson(entity.getTags()),
            entity.getVersion(),
            entity.getDeletedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private String toJson(List<String> tags) {
        // Jackson ObjectMapper 사용
    }

    private List<String> fromJson(String tags) {
        // Jackson ObjectMapper 사용
    }
}
```
