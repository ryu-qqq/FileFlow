# 📁 파일 관리 테이블 명세

## 1. file_assets (파일 자산)

### 테이블 설명
업로드된 모든 파일의 핵심 정보를 관리하는 메인 테이블입니다. 파일의 메타데이터, 저장 위치, 상태 등을 추적합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 파일 ID |
| file_id | VARCHAR(36) | UK, NOT NULL | UUID() | 파일 고유 식별자 (UUID) |
| session_id | VARCHAR(36) | NOT NULL | - | 업로드 세션 ID (upload_sessions 참조) |
| tenant_id | VARCHAR(50) | NOT NULL | - | 테넌트 ID (tenants 참조) |
| organization_id | BIGINT | NOT NULL | - | 조직 ID (organizations 참조) |
| uploader_id | BIGINT | NOT NULL | - | 업로더 사용자 컨텍스트 ID (user_contexts 참조) |
| original_name | VARCHAR(500) | NOT NULL | - | 원본 파일명 |
| stored_name | VARCHAR(500) | NOT NULL | - | 저장된 파일명 |
| file_type | ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'OTHER') | NOT NULL | - | 파일 타입 |
| mime_type | VARCHAR(100) | NOT NULL | - | MIME 타입 |
| file_extension | VARCHAR(20) | NULL | NULL | 파일 확장자 |
| file_size | BIGINT | NOT NULL | - | 파일 크기 (bytes) |
| storage_provider | ENUM('S3', 'GCS', 'AZURE', 'LOCAL') | NOT NULL | 'S3' | 스토리지 제공자 |
| storage_bucket | VARCHAR(255) | NOT NULL | - | 스토리지 버킷/컨테이너 |
| storage_path | VARCHAR(2048) | NOT NULL | - | 스토리지 경로 |
| storage_region | VARCHAR(50) | NULL | NULL | 스토리지 리전 |
| cdn_provider | ENUM('CLOUDFRONT', 'CLOUDFLARE', 'AKAMAI', 'NONE') | NULL | 'NONE' | CDN 제공자 |
| cdn_url | VARCHAR(2048) | NULL | NULL | CDN URL |
| checksum_md5 | VARCHAR(32) | NULL | NULL | MD5 체크섬 |
| checksum_sha256 | VARCHAR(64) | NULL | NULL | SHA256 체크섬 |
| encryption_status | ENUM('NONE', 'AT_REST', 'CLIENT_SIDE') | NOT NULL | 'AT_REST' | 암호화 상태 |
| encryption_key_id | VARCHAR(255) | NULL | NULL | 암호화 키 ID |
| status | ENUM('UPLOADING', 'PROCESSING', 'AVAILABLE', 'ARCHIVED', 'DELETED', 'ERROR') | NOT NULL | 'UPLOADING' | 파일 상태 |
| processing_status | ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'SKIPPED') | NULL | NULL | 처리 상태 |
| visibility | ENUM('PUBLIC', 'PRIVATE', 'INTERNAL') | NOT NULL | 'PRIVATE' | 공개 범위 |
| retention_days | INT | NULL | 365 | 보관 기간 (일) |
| expires_at | DATETIME | NULL | NULL | 만료 시각 |
| tags | JSON | NULL | '[]' | 태그 목록 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| version | INT | NOT NULL | 1 | 파일 버전 (낙관적 락) |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| deleted_at | DATETIME | NULL | NULL | 삭제 시각 (soft delete) |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_file_id (file_id)
INDEX idx_session_id (session_id)
INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC)
INDEX idx_uploader_id (uploader_id, created_at DESC)
INDEX idx_file_type_status (file_type, status, created_at DESC)
INDEX idx_original_name (original_name)
INDEX idx_storage_path (storage_path(255))
INDEX idx_checksum_sha256 (checksum_sha256)
INDEX idx_status (status)
INDEX idx_expires_at (expires_at)
INDEX idx_deleted_at (deleted_at)
INDEX idx_created_at (created_at DESC)
FULLTEXT idx_fulltext_name (original_name)
-- 외래키 제거: 운영 편의성 및 확장성을 위해 FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 파티셔닝
```sql
-- 월별 파티셔닝 (성장 단계에서 적용 고려)
/*
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01'))
    -- ... 계속
);
*/
```

---

## 2. file_variants (파일 변종)

### 테이블 설명
원본 파일로부터 생성된 변종 파일들을 관리합니다. 썸네일, 리사이즈 이미지, 변환된 포맷 등을 포함합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 변종 ID |
| variant_id | VARCHAR(36) | UK, NOT NULL | UUID() | 변종 고유 식별자 |
| original_file_id | BIGINT | NOT NULL | - | 원본 파일 ID (file_assets 참조) |
| variant_type | ENUM('THUMBNAIL', 'PREVIEW', 'OPTIMIZED', 'CONVERTED', 'RESIZED', 'WATERMARKED') | NOT NULL | - | 변종 타입 |
| variant_name | VARCHAR(100) | NOT NULL | - | 변종 이름 (예: 'thumb_200x200') |
| variant_config | JSON | NOT NULL | '{}' | 변종 생성 설정 |
| file_size | BIGINT | NOT NULL | - | 파일 크기 (bytes) |
| mime_type | VARCHAR(100) | NOT NULL | - | MIME 타입 |
| storage_path | VARCHAR(2048) | NOT NULL | - | 스토리지 경로 |
| cdn_url | VARCHAR(2048) | NULL | NULL | CDN URL |
| dimensions | JSON | NULL | NULL | 이미지 크기 정보 (width, height) |
| quality | INT | NULL | NULL | 품질 (1-100) |
| format | VARCHAR(20) | NULL | NULL | 파일 포맷 |
| processing_time_ms | INT | NULL | NULL | 처리 시간 (밀리초) |
| processor_version | VARCHAR(50) | NULL | NULL | 처리기 버전 |
| checksum_sha256 | VARCHAR(64) | NULL | NULL | SHA256 체크섬 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_variant_id (variant_id)
INDEX idx_original_file (original_file_id, variant_type)
INDEX idx_variant_type (variant_type)
INDEX idx_variant_name (variant_name)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

### 샘플 데이터
```sql
INSERT INTO file_variants (original_file_id, variant_type, variant_name, variant_config, file_size, mime_type, storage_path, dimensions) VALUES
(1, 'THUMBNAIL', 'thumb_200x200', '{"width":200,"height":200,"fit":"cover"}', 15234, 'image/webp', 's3://bucket/thumbs/...', '{"width":200,"height":200}'),
(1, 'OPTIMIZED', 'optimized_1920', '{"maxWidth":1920,"quality":85}', 245632, 'image/webp', 's3://bucket/optimized/...', '{"width":1920,"height":1080}'),
(1, 'WATERMARKED', 'watermarked', '{"position":"bottom-right","opacity":0.5}', 456789, 'image/jpeg', 's3://bucket/watermarked/...', '{"width":1920,"height":1080}');
```

---

## 3. file_metadata (파일 메타데이터)

### 테이블 설명
파일의 추가 메타데이터를 키-값 형태로 저장합니다. EXIF 데이터, 커스텀 속성 등을 유연하게 저장할 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 메타데이터 ID |
| file_id | BIGINT | NOT NULL | - | 파일 ID (file_assets 참조) |
| metadata_key | VARCHAR(100) | NOT NULL | - | 메타데이터 키 |
| metadata_value | TEXT | NULL | NULL | 메타데이터 값 |
| value_type | ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE', 'BINARY') | NOT NULL | 'STRING' | 값 타입 |
| category | VARCHAR(50) | NULL | NULL | 카테고리 (exif, custom, system 등) |
| is_indexed | BOOLEAN | NOT NULL | FALSE | 인덱싱 여부 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_file_metadata_key (file_id, metadata_key)
INDEX idx_file_id (file_id)
INDEX idx_metadata_key (metadata_key)
INDEX idx_category (category)
INDEX idx_metadata_value (metadata_value(100))  -- 앞 100자만 인덱싱
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

### 샘플 데이터
```sql
-- 이미지 EXIF 데이터
INSERT INTO file_metadata (file_id, metadata_key, metadata_value, value_type, category) VALUES
(1, 'exif.camera_make', 'Canon', 'STRING', 'exif'),
(1, 'exif.camera_model', 'EOS 5D Mark IV', 'STRING', 'exif'),
(1, 'exif.taken_at', '2025-01-15T14:30:00', 'DATE', 'exif'),
(1, 'exif.gps_latitude', '37.5665', 'NUMBER', 'exif'),
(1, 'exif.gps_longitude', '126.9780', 'NUMBER', 'exif'),

-- 커스텀 메타데이터
(1, 'product.category', 'fashion', 'STRING', 'custom'),
(1, 'product.sku', 'FSH-2025-001', 'STRING', 'custom'),
(1, 'product.season', '2025SS', 'STRING', 'custom'),

-- HTML 파일 메타데이터
(2, 'html.title', '상품 상세 설명', 'STRING', 'document'),
(2, 'html.word_count', '1523', 'NUMBER', 'document'),
(2, 'html.has_table', 'true', 'BOOLEAN', 'document');
```

---

## 4. file_relationships (파일 관계)

### 테이블 설명
파일 간의 관계를 정의합니다. 원본-썸네일, 관련 파일, 버전 관계 등을 표현합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 관계 ID |
| source_file_id | BIGINT | NOT NULL | - | 소스 파일 ID (file_assets 참조) |
| target_file_id | BIGINT | NOT NULL | - | 타겟 파일 ID (file_assets 참조) |
| relationship_type | ENUM('DERIVED', 'RELATED', 'VERSION', 'REPLACEMENT', 'BUNDLE', 'ATTACHMENT') | NOT NULL | - | 관계 타입 |
| relationship_name | VARCHAR(100) | NULL | NULL | 관계 이름 |
| sequence_order | INT | NULL | NULL | 순서 (번들 등에서 사용) |
| metadata | JSON | NULL | '{}' | 관계 메타데이터 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| created_by | BIGINT | NULL | NULL | 생성자 ID (user_contexts 참조) |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_file_relationship (source_file_id, target_file_id, relationship_type)
INDEX idx_source_file (source_file_id, relationship_type)
INDEX idx_target_file (target_file_id, relationship_type)
INDEX idx_relationship_type (relationship_type)
INDEX idx_created_by (created_by)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO file_relationships (source_file_id, target_file_id, relationship_type, relationship_name, metadata) VALUES
-- 이미지와 썸네일 관계
(1, 10, 'DERIVED', 'thumbnail', '{"purpose":"product_list"}'),
(1, 11, 'DERIVED', 'preview', '{"purpose":"product_detail"}'),

-- 상품 이미지 번들
(20, 21, 'BUNDLE', 'product_images', '{"position":"main"}'),
(20, 22, 'BUNDLE', 'product_images', '{"position":"detail_1"}'),
(20, 23, 'BUNDLE', 'product_images', '{"position":"detail_2"}'),

-- 파일 버전 관계
(30, 31, 'VERSION', 'v2', '{"changes":"resolution improved"}'),

-- HTML과 첨부 이미지
(40, 41, 'ATTACHMENT', 'inline_image_1', '{"element_id":"img_001"}');
```

---

## 5. file_versions (파일 버전 관리) - 선택적

### 테이블 설명
동일 파일의 버전 히스토리를 관리하는 테이블입니다. 파일 수정 이력을 추적할 때 사용합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 버전 ID |
| master_file_id | VARCHAR(36) | NOT NULL | - | 마스터 파일 ID (변하지 않음) |
| file_id | BIGINT | NOT NULL | - | 실제 파일 ID (file_assets 참조) |
| version_number | INT | NOT NULL | - | 버전 번호 |
| version_label | VARCHAR(50) | NULL | NULL | 버전 라벨 (예: 'v1.0', 'draft') |
| change_description | TEXT | NULL | NULL | 변경 설명 |
| is_current | BOOLEAN | NOT NULL | FALSE | 현재 버전 여부 |
| created_by | BIGINT | NOT NULL | - | 생성자 ID (user_contexts 참조) |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_master_version (master_file_id, version_number)
INDEX idx_master_file (master_file_id, is_current)
INDEX idx_file_id (file_id)
INDEX idx_created_by (created_by)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 6. file_access_logs (파일 접근 로그)

### 테이블 설명
파일 접근 이력을 기록하는 감사 로그 테이블입니다. 보안 및 사용 분석 목적으로 사용됩니다.

**⚠️ 하이브리드 로그 전략 적용**:
- MySQL: 7일 보관 (실시간 대시보드 및 분석)
- S3 + Athena: 장기 보관 (Parquet 포맷)
- CloudWatch: 백업 및 실시간 알람

자세한 내용은 [07-audit-logging.md](./07-audit-logging.md) 참조.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| file_id | BIGINT | NOT NULL | - | 파일 ID (file_assets 참조) |
| user_context_id | BIGINT | NULL | NULL | 사용자 컨텍스트 ID (user_contexts 참조) |
| action | ENUM('VIEW', 'DOWNLOAD', 'UPLOAD', 'UPDATE', 'DELETE', 'SHARE') | NOT NULL | - | 액션 타입 |
| ip_address | VARCHAR(45) | NOT NULL | - | IP 주소 |
| user_agent | TEXT | NULL | NULL | User Agent |
| referer | TEXT | NULL | NULL | Referer URL |
| session_id | VARCHAR(100) | NULL | NULL | 세션 ID |
| response_code | INT | NULL | NULL | HTTP 응답 코드 |
| response_time_ms | INT | NULL | NULL | 응답 시간 (밀리초) |
| bytes_transferred | BIGINT | NULL | NULL | 전송 바이트 |
| metadata | JSON | NULL | NULL | 추가 정보 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 접근 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_file_id_action (file_id, action, created_at DESC)
INDEX idx_user_context_id (user_context_id, created_at DESC)
INDEX idx_ip_address (ip_address)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 대용량 로그 특성상 FK는 성능 저하 요인

-- 파티셔닝: 월별 (성장 단계에서 적용 고려)
/*
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    ...
);
*/
```

---

## 7. 참조 무결성 검증 (Application Level)

외래키 제약조건을 제거했기 때문에 애플리케이션 레벨에서 참조 무결성을 보장해야 합니다.

### 7.1 파일 생성 시 검증

```java
@Service
@RequiredArgsConstructor
public class FileAssetService {

    private final FileAssetRepository fileAssetRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final UserContextRepository userContextRepository;
    private final UploadSessionRepository uploadSessionRepository;

    @Transactional
    public FileAsset createFileAsset(FileAssetCreateRequest request) {

        // 1. 참조 무결성 검증
        validateReferences(request);

        // 2. 파일 자산 생성
        FileAsset fileAsset = FileAsset.builder()
            .fileId(UUID.randomUUID().toString())
            .sessionId(request.getSessionId())
            .tenantId(request.getTenantId())
            .organizationId(request.getOrganizationId())
            .uploaderId(request.getUploaderId())
            .originalName(request.getOriginalName())
            // ... 나머지 필드
            .build();

        return fileAssetRepository.save(fileAsset);
    }

    private void validateReferences(FileAssetCreateRequest request) {

        // Tenant 존재 여부 검증
        if (!tenantRepository.existsByTenantId(request.getTenantId())) {
            throw new ReferenceNotFoundException(
                "Tenant not found: " + request.getTenantId()
            );
        }

        // Organization 존재 여부 검증
        if (!organizationRepository.existsById(request.getOrganizationId())) {
            throw new ReferenceNotFoundException(
                "Organization not found: " + request.getOrganizationId()
            );
        }

        // UserContext 존재 여부 검증
        if (!userContextRepository.existsById(request.getUploaderId())) {
            throw new ReferenceNotFoundException(
                "User context not found: " + request.getUploaderId()
            );
        }

        // UploadSession 존재 여부 및 상태 검증
        UploadSession session = uploadSessionRepository.findBySessionId(request.getSessionId())
            .orElseThrow(() -> new ReferenceNotFoundException(
                "Upload session not found: " + request.getSessionId()
            ));

        if (session.getStatus() != UploadSessionStatus.IN_PROGRESS) {
            throw new InvalidSessionStateException(
                "Upload session is not in progress: " + request.getSessionId()
            );
        }

        // Tenant-Organization 관계 검증
        Organization org = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new ReferenceNotFoundException(
                "Organization not found: " + request.getOrganizationId()
            ));

        if (!org.getTenantId().equals(request.getTenantId())) {
            throw new InvalidReferenceException(
                "Organization does not belong to the specified tenant"
            );
        }
    }
}
```

### 7.2 파일 삭제 시 CASCADE 처리

```java
@Service
@RequiredArgsConstructor
public class FileAssetService {

    private final FileAssetRepository fileAssetRepository;
    private final FileVariantRepository fileVariantRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileRelationshipRepository fileRelationshipRepository;
    private final StorageService storageService;

    @Transactional
    public void deleteFileAsset(Long fileId) {

        FileAsset fileAsset = fileAssetRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));

        // 1. 관련 변종 파일 삭제
        List<FileVariant> variants = fileVariantRepository.findByOriginalFileId(fileId);
        variants.forEach(variant -> {
            // 스토리지에서 실제 파일 삭제
            storageService.deleteFile(variant.getStoragePath());
        });
        fileVariantRepository.deleteByOriginalFileId(fileId);

        // 2. 메타데이터 삭제
        fileMetadataRepository.deleteByFileId(fileId);

        // 3. 파일 관계 삭제
        fileRelationshipRepository.deleteBySourceFileId(fileId);
        fileRelationshipRepository.deleteByTargetFileId(fileId);

        // 4. 스토리지에서 원본 파일 삭제
        storageService.deleteFile(fileAsset.getStoragePath());

        // 5. 파일 자산 soft delete
        fileAsset.setDeletedAt(LocalDateTime.now());
        fileAssetRepository.save(fileAsset);
    }
}
```

### 7.3 배치 검증 (정기 실행)

```java
@Service
@RequiredArgsConstructor
public class ReferenceIntegrityCheckService {

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 오전 3시
    public void checkFileAssetReferences() {

        // 1. 존재하지 않는 Tenant 참조
        List<FileAssetOrphanCheck> orphanTenants = fileAssetRepository.findOrphanTenants();
        if (!orphanTenants.isEmpty()) {
            log.warn("Found {} file_assets with invalid tenant_id", orphanTenants.size());
            alertService.sendAlert("File Assets with invalid tenant_id", orphanTenants);
        }

        // 2. 존재하지 않는 Organization 참조
        List<FileAssetOrphanCheck> orphanOrgs = fileAssetRepository.findOrphanOrganizations();
        if (!orphanOrgs.isEmpty()) {
            log.warn("Found {} file_assets with invalid organization_id", orphanOrgs.size());
            alertService.sendAlert("File Assets with invalid organization_id", orphanOrgs);
        }

        // 3. 존재하지 않는 UserContext 참조
        List<FileAssetOrphanCheck> orphanUsers = fileAssetRepository.findOrphanUserContexts();
        if (!orphanUsers.isEmpty()) {
            log.warn("Found {} file_assets with invalid uploader_id", orphanUsers.size());
            alertService.sendAlert("File Assets with invalid uploader_id", orphanUsers);
        }

        // 4. 존재하지 않는 UploadSession 참조
        List<FileAssetOrphanCheck> orphanSessions = fileAssetRepository.findOrphanUploadSessions();
        if (!orphanSessions.isEmpty()) {
            log.warn("Found {} file_assets with invalid session_id", orphanSessions.size());
            alertService.sendAlert("File Assets with invalid session_id", orphanSessions);
        }
    }
}
```

### 7.4 고아 레코드 검증 쿼리

```sql
-- 1. 존재하지 않는 Tenant를 참조하는 file_assets
SELECT 'file_assets_orphan_tenant' AS issue_type, COUNT(*) AS count
FROM file_assets fa
LEFT JOIN tenants t ON fa.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL
  AND fa.deleted_at IS NULL;

-- 2. 존재하지 않는 Organization을 참조하는 file_assets
SELECT 'file_assets_orphan_organization' AS issue_type, COUNT(*) AS count
FROM file_assets fa
LEFT JOIN organizations o ON fa.organization_id = o.id
WHERE o.id IS NULL
  AND fa.deleted_at IS NULL;

-- 3. 존재하지 않는 UserContext를 참조하는 file_assets
SELECT 'file_assets_orphan_user_context' AS issue_type, COUNT(*) AS count
FROM file_assets fa
LEFT JOIN user_contexts uc ON fa.uploader_id = uc.id
WHERE uc.id IS NULL
  AND fa.deleted_at IS NULL;

-- 4. 존재하지 않는 UploadSession을 참조하는 file_assets
SELECT 'file_assets_orphan_upload_session' AS issue_type, COUNT(*) AS count
FROM file_assets fa
LEFT JOIN upload_sessions us ON fa.session_id = us.session_id
WHERE us.session_id IS NULL
  AND fa.deleted_at IS NULL;

-- 5. 존재하지 않는 원본 파일을 참조하는 file_variants
SELECT 'file_variants_orphan' AS issue_type, COUNT(*) AS count
FROM file_variants fv
LEFT JOIN file_assets fa ON fv.original_file_id = fa.id
WHERE fa.id IS NULL;

-- 6. 존재하지 않는 파일을 참조하는 file_metadata
SELECT 'file_metadata_orphan' AS issue_type, COUNT(*) AS count
FROM file_metadata fm
LEFT JOIN file_assets fa ON fm.file_id = fa.id
WHERE fa.id IS NULL;

-- 7. 존재하지 않는 파일을 참조하는 file_relationships
SELECT 'file_relationships_orphan_source' AS issue_type, COUNT(*) AS count
FROM file_relationships fr
LEFT JOIN file_assets fa ON fr.source_file_id = fa.id
WHERE fa.id IS NULL;

SELECT 'file_relationships_orphan_target' AS issue_type, COUNT(*) AS count
FROM file_relationships fr
LEFT JOIN file_assets fa ON fr.target_file_id = fa.id
WHERE fa.id IS NULL;
```

### 7.5 Orphan 레코드 정리 스크립트

```sql
-- 1. file_variants의 고아 레코드 정리
DELETE FROM file_variants
WHERE original_file_id NOT IN (SELECT id FROM file_assets);

-- 2. file_metadata의 고아 레코드 정리
DELETE FROM file_metadata
WHERE file_id NOT IN (SELECT id FROM file_assets);

-- 3. file_relationships의 고아 레코드 정리
DELETE FROM file_relationships
WHERE source_file_id NOT IN (SELECT id FROM file_assets)
   OR target_file_id NOT IN (SELECT id FROM file_assets);

-- 4. 삭제된 원본 파일의 변종 정리
DELETE FROM file_variants
WHERE original_file_id IN (
    SELECT id FROM file_assets WHERE deleted_at IS NOT NULL
);
```

---

## 8. 관계 다이어그램

```
file_assets ──< file_variants
    │
    ├──< file_metadata
    │
    ├──< file_relationships (source)
    ├──< file_relationships (target)
    │
    ├──< file_versions
    │
    └──< file_access_logs
```

---

## 9. 비즈니스 로직

### 9.1 파일 업로드 플로우
1. 업로드 세션 생성
2. 참조 무결성 검증 (tenant, organization, user_context)
3. 파일 업로드 (청크/멀티파트)
4. file_assets 레코드 생성 (status: UPLOADING)
5. 체크섬 검증
6. status를 PROCESSING으로 변경
7. 파이프라인 트리거
8. 변종 생성 (썸네일 등)
9. 메타데이터 추출 및 저장
10. status를 AVAILABLE로 변경

### 9.2 파일 조회 플로우
1. 권한 체크
2. file_assets 조회
3. CDN URL 생성/반환
4. file_access_logs 기록
5. 캐시 업데이트

### 9.3 파일 삭제 정책
- Soft Delete 기본 (deleted_at 설정)
- 30일 후 물리적 삭제
- 관련 변종 파일 CASCADE 삭제 (애플리케이션 레벨)
- 스토리지에서 실제 삭제

---

## 10. 스토리지 전략

### 10.1 파일 경로 구조
```
/{tenant_id}/{year}/{month}/{day}/{file_type}/{file_id}/
예: /b2c_kr/2025/01/20/image/550e8400-e29b-41d4-a716-446655440000/original.jpg
```

### 10.2 CDN 전략
- 정적 파일: Long-term 캐싱 (1년)
- 동적 URL: Signed URL (유효기간 설정)
- 지역별 엣지 배포

### 10.3 백업 정책
- 실시간 복제: Cross-region
- 일일 백업: Glacier 저장
- 보관 기간: 법적 요구사항 준수

---

## 11. 성능 최적화

### 11.1 캐싱 전략
- 파일 메타데이터 Redis 캐싱 (TTL: 1시간)
- 자주 접근하는 파일 CDN 프리페칭
- 파일 리스팅 결과 캐싱 (TTL: 5분)

### 11.2 대용량 처리
- 대용량 파일 멀티파트 업로드
- 파일 리스팅 페이징 처리 (기본 20개, 최대 100개)
- 검색용 Elasticsearch 인덱싱

### 11.3 인덱스 전략
```sql
-- 파일 검색 성능 최적화
CREATE INDEX idx_file_search
ON file_assets(tenant_id, organization_id, file_type, status, created_at DESC);

-- 업로더별 파일 조회 성능 최적화
CREATE INDEX idx_uploader_files
ON file_assets(uploader_id, status, created_at DESC)
WHERE deleted_at IS NULL;

-- 만료 파일 정리 성능 최적화
CREATE INDEX idx_expired_files
ON file_assets(expires_at)
WHERE expires_at IS NOT NULL AND deleted_at IS NULL;
```

---

## 12. 보안 고려사항

### 12.1 접근 제어
- 파일 업로드: 인증된 사용자만 가능
- 파일 조회: 조직/테넌트 격리 강제
- Signed URL: 시간 제한 및 IP 제한

### 12.2 암호화
- At-Rest: S3 Server-Side Encryption (SSE-S3 또는 SSE-KMS)
- In-Transit: HTTPS 강제
- Client-Side: 민감 파일의 경우 클라이언트 암호화 지원

### 12.3 감사 추적
- 모든 파일 접근 로그 기록
- 파일 수정/삭제 이력 추적
- 의심스러운 접근 패턴 모니터링
