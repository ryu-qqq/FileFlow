# FILE-003: Persistence Layer 구현

**Epic**: File Management System (파일 관리 시스템)
**Layer**: Persistence Layer (Adapter-Out)
**브랜치**: feature/FILE-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 업로드 데이터 저장소를 구현합니다.
- JPA Entities (2개)
- Flyway Migrations (2개)
- Adapters (4개)
- S3 Client Adapter (Presigned URL)

---

## 🎯 요구사항

### A. JPA Entities (2개)

#### 1. FileJpaEntity
**필드**:
- [ ] `id`: Long (PK, AUTO_INCREMENT)
- [ ] `fileId`: String (36자, UNIQUE)
- [ ] `fileName`: String (255자)
- [ ] `fileSize`: Long
- [ ] `mimeType`: String (100자)
- [ ] `s3Key`: String (500자)
- [ ] `s3Bucket`: String (100자)
- [ ] `uploaderId`: Long
- [ ] `uploaderType`: String (20자)
- [ ] `uploaderSlug`: String (100자)
- [ ] `category`: String (50자)
- [ ] `tenantId`: Long
- [ ] `status`: String (20자)
- [ ] `createdAt`: LocalDateTime (BaseAuditEntity)
- [ ] `updatedAt`: LocalDateTime (BaseAuditEntity)

**인덱스**:
- [ ] `idx_file_id` (file_id)
- [ ] `idx_uploader` (uploader_id, uploader_type)
- [ ] `idx_tenant_created` (tenant_id, created_at DESC)
- [ ] `idx_category` (category)

**제약조건**:
- [ ] Long FK 전략 (JPA 관계 어노테이션 금지)
- [ ] Lombok 금지 (Plain Java Getter/Setter)
- [ ] BaseAuditEntity 상속

#### 2. UploadSessionJpaEntity
**필드**:
- [ ] `id`: Long (PK, AUTO_INCREMENT)
- [ ] `sessionId`: String (36자, UNIQUE)
- [ ] `tenantId`: Long
- [ ] `fileName`: String (255자)
- [ ] `fileSize`: Long
- [ ] `mimeType`: String (100자)
- [ ] `uploadType`: String (20자)
- [ ] `presignedUrl`: TEXT
- [ ] `expiresAt`: LocalDateTime
- [ ] `status`: String (20자)
- [ ] `createdAt`: LocalDateTime (BaseAuditEntity)
- [ ] `updatedAt`: LocalDateTime (BaseAuditEntity)

**인덱스**:
- [ ] `idx_session_id` (session_id)
- [ ] `idx_status_expires` (status, expires_at)

**제약조건**:
- [ ] Long FK 전략
- [ ] Lombok 금지
- [ ] BaseAuditEntity 상속

---

### B. Flyway Migrations (2개)

#### V1__create_files_table.sql
```sql
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

#### V2__create_upload_sessions_table.sql
```sql
CREATE TABLE upload_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    upload_type VARCHAR(20) NOT NULL,
    presigned_url TEXT,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_id (session_id),
    INDEX idx_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**마이그레이션 규칙**:
- [ ] 순차 번호 (V1, V2, ...)
- [ ] 롤백 스크립트 없음 (Forward-only)
- [ ] 테스트 DB는 Flyway로 초기화 (@Sql 금지)

---

### C. Mappers (2개)

#### 1. FileMapper
- [ ] `toDomain(FileJpaEntity)`: File
- [ ] `toEntity(File)`: FileJpaEntity

**매핑 규칙**:
- [ ] VO 변환: String ↔ VO (FileId, FileName, FileSize 등)
- [ ] Enum 변환: String ↔ Enum (FileStatus, UploaderType)

#### 2. UploadSessionMapper
- [ ] `toDomain(UploadSessionJpaEntity)`: UploadSession
- [ ] `toEntity(UploadSession)`: UploadSessionJpaEntity

**매핑 규칙**:
- [ ] VO 변환: String ↔ VO (SessionId, FileName 등)
- [ ] Enum 변환: String ↔ Enum (SessionStatus, UploadType)

---

### D. Repositories (2개)

#### 1. FileJpaRepository
```java
public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    Optional<FileJpaEntity> findByFileId(String fileId);
}
```

#### 2. UploadSessionJpaRepository
```java
public interface UploadSessionJpaRepository extends JpaRepository<UploadSessionJpaEntity, Long> {
    Optional<UploadSessionJpaEntity> findBySessionId(String sessionId);
}
```

**Repository 규칙**:
- [ ] Spring Data JPA 기본 메서드 사용
- [ ] 복잡한 쿼리는 QueryDSL로 별도 구현 (v2)

---

### E. Command Adapters (2개)

#### 1. FilePersistenceAdapter
- [ ] `save(File)`: File
- [ ] Port 구현: `FilePersistencePort`

**구현 로직**:
1. Domain → Entity (FileMapper)
2. JPA save
3. Entity → Domain

#### 2. UploadSessionPersistenceAdapter
- [ ] `save(UploadSession)`: UploadSession
- [ ] `update(UploadSession)`: UploadSession
- [ ] Port 구현: `UploadSessionPersistencePort`

**구현 로직**:
1. Domain → Entity (UploadSessionMapper)
2. JPA save
3. Entity → Domain

---

### F. Query Adapter (1개)

#### UploadSessionQueryAdapter
- [ ] `findBySessionId(SessionId)`: Optional<UploadSession>
- [ ] Port 구현: `UploadSessionQueryPort`

**구현 로직**:
1. SessionId → String
2. JPA findBySessionId
3. Entity → Domain (Mapper)

---

### G. External Adapter (1개)

#### S3ClientAdapter
- [ ] `generatePresignedPutUrl(S3Bucket, S3Key, MimeType, Duration)`: PresignedUrl
- [ ] Port 구현: `S3ClientPort`

**구현 로직**:
1. PutObjectRequest 생성 (bucket, key, contentType)
2. S3Presigner.presignPutObject (signatureDuration)
3. URL → PresignedUrl VO

**의존성**:
- [ ] AWS SDK S3 Presigner
- [ ] PresignedUrl VO 변환

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지**: Entity는 Plain Java Getter/Setter
- [ ] **Long FK 전략**: JPA 관계 어노테이션 (@OneToMany, @ManyToOne) 절대 금지
- [ ] **QueryDSL DTO Projection**: 복잡한 조회는 DTO로 (v2)
- [ ] **Flyway Only**: @Sql 금지, Flyway Migration만 사용

### Persistence Layer 규칙
- [ ] **Adapter 패턴**: Port 인터페이스 구현
- [ ] **Mapper 분리**: Entity ↔ Domain 변환 전용 클래스
- [ ] **VO 변환**: String ↔ VO (FileId, FileName 등)
- [ ] **BaseAuditEntity 상속**: createdAt, updatedAt 자동 관리

### 테스트 규칙
- [ ] **ArchUnit 테스트 필수**:
  - Entity: JPA 어노테이션, Long FK 전략
  - Adapter: @Component, Port 구현
  - Mapper: 정적 메서드 또는 Spring Bean
- [ ] **Integration Test**: Flyway + TestRestTemplate
- [ ] **TestFixture 사용**: Entity 생성 시
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] 2개 JPA Entities 구현 완료
- [ ] 2개 Flyway Migrations 작성 완료
- [ ] 2개 Mappers 구현 완료
- [ ] 2개 JPA Repositories 정의 완료
- [ ] 2개 Command Adapters 구현 완료
- [ ] 1개 Query Adapter 구현 완료
- [ ] 1개 External Adapter (S3) 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] Integration Test 통과 (Flyway + DB)
- [ ] ArchUnit 테스트 통과
  - `PersistenceLayerDependencyRules`
  - `EntityArchTest`
  - `AdapterArchTest`
  - `MapperArchTest`
- [ ] Zero-Tolerance 규칙 100% 준수
- [ ] 테스트 커버리지 > 80%
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mvp/file-upload-mvp.md
- **Domain Layer**: docs/prd/tasks/FILE-001.md
- **Application Layer**: docs/prd/tasks/FILE-002.md
- **Plan**: docs/prd/plans/FILE-003-persistence-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: docs/coding_convention/04-persistence-layer/mysql/persistence-mysql-guide.md

---

## 📚 참고 규칙

- `docs/coding_convention/04-persistence-layer/mysql/entity/guide.md` (Entity 패턴)
- `docs/coding_convention/04-persistence-layer/mysql/adapter/command/guide.md` (Command Adapter)
- `docs/coding_convention/04-persistence-layer/mysql/adapter/query/query-adapter-guide.md` (Query Adapter)
- `docs/coding_convention/04-persistence-layer/mysql/mapper/guide.md` (Mapper 패턴)
- `docs/coding_convention/04-persistence-layer/mysql/config/flyway-testing.md` (Flyway 테스트)
