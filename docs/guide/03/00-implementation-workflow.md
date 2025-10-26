# Phase 3: File Management Implementation Workflow

**목적**: Upload Management로 생성된 파일을 체계적으로 관리하고, 다양한 파일 변형(Variant), 관계(Relationship), 접근 제어(Visibility), 수명 주기(Lifecycle)를 처리하는 엔터프라이즈급 파일 관리 시스템 구현

**핵심 특징**:
- **Event-Driven Integration**: Upload 완료 이벤트 소비 → 자동 FileAsset 생성
- **File Variants**: 썸네일, 포맷 변환, 해상도 최적화 등 파일 변형 관리
- **File Relationships**: 버전 관리, 참조 관계, 그룹핑 등 파일 간 관계
- **Visibility Control**: PRIVATE/INTERNAL/PUBLIC 레벨 + IAM ABAC 통합
- **Lifecycle Management**: TTL 기반 자동 만료, Soft Delete, 보관 정책

---

## 전제 조건

### 1. Phase 1 (IAM) 완료
- `user_contexts`, `permissions`, `roles`, `role_permissions` 테이블 존재
- IAM ABAC 엔진 (CEL 기반) 구현 완료
- Permission 평가 인프라 준비

### 2. Phase 2 (Upload Management) 완료
- `upload_sessions`, `file_metadata` 테이블 존재
- S3 Presigned URL 기반 업로드 시스템 구동
- `upload.completed` 도메인 이벤트 발행 인프라

### 3. 인프라 요구사항
- **AWS S3**: 원본 파일 + 변형 파일 저장 (버킷 분리 권장)
- **Redis**: 파일 메타데이터 캐시, 다운로드 URL 캐싱
- **MySQL 8.x**: 파일 자산, 변형, 관계 메타데이터 저장
- **이벤트 시스템**: Spring Event or Message Queue (Phase 2 → Phase 3 통합)

---

## 프로젝트 구조

```
fileflow/
├── domain/
│   └── src/main/java/com/ryuqq/fileflow/domain/
│       └── file/
│           ├── asset/                              # Phase 3A
│           │   ├── FileAsset.java                  # Aggregate Root
│           │   ├── FileAssetId.java                # Value Object
│           │   ├── Visibility.java                 # Enum (PRIVATE/INTERNAL/PUBLIC)
│           │   ├── FileMetadataSnapshot.java       # VO (from Phase 2)
│           │   ├── FileAssetCreatedEvent.java      # Domain Event
│           │   └── FileAssetExpiredEvent.java
│           │
│           ├── variant/                            # Phase 3A
│           │   ├── FileVariant.java                # Aggregate Root
│           │   ├── FileVariantId.java              # Value Object
│           │   ├── VariantType.java                # Enum (THUMBNAIL, OPTIMIZED, etc.)
│           │   ├── VariantSpec.java                # VO (resolution, format, quality)
│           │   └── FileVariantCreatedEvent.java
│           │
│           ├── relationship/                       # Phase 3B
│           │   ├── FileRelationship.java           # Aggregate Root
│           │   ├── RelationshipType.java           # Enum (VERSION, REFERENCE, GROUP)
│           │   └── FileRelationshipCreatedEvent.java
│           │
│           └── access/                             # Phase 3C
│               ├── FileAccessLog.java              # Entity (감사)
│               └── FileDownloadRequest.java        # Value Object
│
├── application/
│   └── src/main/java/com/ryuqq/fileflow/application/
│       └── file/
│           ├── port/
│           │   ├── in/                             # Use Case Interfaces
│           │   │   ├── ConsumeUploadCompletedUseCase.java      # Phase 3A
│           │   │   ├── QueryFileMetadataUseCase.java           # Phase 3A
│           │   │   ├── GenerateDownloadUrlUseCase.java         # Phase 3A
│           │   │   ├── CreateFileVariantUseCase.java           # Phase 3A
│           │   │   ├── LinkFileRelationshipUseCase.java        # Phase 3B
│           │   │   ├── UpdateFileVisibilityUseCase.java        # Phase 3B
│           │   │   ├── QueryFilesByRelationshipUseCase.java    # Phase 3B
│           │   │   ├── SoftDeleteFileAssetUseCase.java         # Phase 3C
│           │   │   ├── QueryFileAccessLogsUseCase.java         # Phase 3C
│           │   │   └── ExpireFileAssetsUseCase.java            # Phase 3C (Batch)
│           │   │
│           │   └── out/                            # Port Interfaces
│           │       ├── LoadFileAssetPort.java
│           │       ├── SaveFileAssetPort.java
│           │       ├── LoadFileVariantPort.java
│           │       ├── SaveFileVariantPort.java
│           │       ├── LoadFileRelationshipPort.java
│           │       ├── SaveFileRelationshipPort.java
│           │       ├── SaveFileAccessLogPort.java
│           │       ├── GeneratePresignedDownloadUrlPort.java   # S3 Adapter
│           │       ├── EvaluateFilePermissionPort.java         # IAM Adapter
│           │       └── CacheFileMetadataPort.java              # Redis Adapter
│           │
│           ├── service/                            # Use Case Implementations
│           │   ├── ConsumeUploadCompletedService.java          # Phase 3A
│           │   ├── QueryFileMetadataService.java               # Phase 3A
│           │   ├── GenerateDownloadUrlService.java             # Phase 3A
│           │   ├── CreateFileVariantService.java               # Phase 3A
│           │   ├── LinkFileRelationshipService.java            # Phase 3B
│           │   ├── UpdateFileVisibilityService.java            # Phase 3B
│           │   ├── QueryFilesByRelationshipService.java        # Phase 3B
│           │   ├── SoftDeleteFileAssetService.java             # Phase 3C
│           │   ├── QueryFileAccessLogsService.java             # Phase 3C
│           │   └── ExpireFileAssetsService.java                # Phase 3C (Batch)
│           │
│           ├── command/                            # CQRS Commands
│           │   ├── ConsumeUploadCompletedCommand.java
│           │   ├── CreateFileVariantCommand.java
│           │   ├── LinkFileRelationshipCommand.java
│           │   ├── UpdateFileVisibilityCommand.java
│           │   └── SoftDeleteFileAssetCommand.java
│           │
│           └── query/                              # CQRS Queries
│               ├── FileMetadataQuery.java
│               ├── FilesByRelationshipQuery.java
│               └── FileAccessLogsQuery.java
│
├── adapter-out-persistence/
│   └── src/main/java/com/ryuqq/fileflow/adapter/out/persistence/
│       ├── file/
│       │   ├── entity/
│       │   │   ├── FileAssetJpaEntity.java          # file_assets 테이블 매핑
│       │   │   ├── FileVariantJpaEntity.java        # file_variants 테이블 매핑
│       │   │   ├── FileRelationshipJpaEntity.java   # file_relationships 테이블 매핑
│       │   │   └── FileAccessLogJpaEntity.java      # file_access_log 테이블 매핑
│       │   │
│       │   ├── repository/
│       │   │   ├── FileAssetJpaRepository.java
│       │   │   ├── FileVariantJpaRepository.java
│       │   │   ├── FileRelationshipJpaRepository.java
│       │   │   └── FileAccessLogJpaRepository.java
│       │   │
│       │   └── adapter/
│       │       ├── FileAssetPersistenceAdapter.java
│       │       ├── FileVariantPersistenceAdapter.java
│       │       ├── FileRelationshipPersistenceAdapter.java
│       │       └── FileAccessLogPersistenceAdapter.java
│       │
│       └── cache/
│           ├── entity/
│           │   └── FileMetadataCacheEntity.java     # Redis Hash 구조
│           └── adapter/
│               └── FileMetadataCacheAdapter.java
│
├── adapter-out-s3/
│   └── src/main/java/com/ryuqq/fileflow/adapter/out/s3/
│       └── FileDownloadPresignedUrlAdapter.java    # S3 다운로드 Presigned URL 생성
│
├── adapter-out-iam/
│   └── src/main/java/com/ryuqq/fileflow/adapter/out/iam/
│       └── FilePermissionEvaluationAdapter.java    # IAM ABAC 연동
│
├── adapter-in-event/
│   └── src/main/java/com/ryuqq/fileflow/adapter/in/event/
│       └── UploadCompletedEventListener.java       # Phase 2 이벤트 소비 → Phase 3 트리거
│
├── adapter-in-web/
│   └── src/main/java/com/ryuqq/fileflow/adapter/in/web/
│       └── file/
│           ├── FileMetadataQueryController.java    # GET /api/v1/files/{id}
│           ├── FileDownloadController.java         # GET /api/v1/files/{id}/download
│           ├── FileVariantController.java          # POST /api/v1/files/{id}/variants
│           ├── FileRelationshipController.java     # POST /api/v1/files/{id}/relationships
│           └── FileAccessLogController.java        # GET /api/v1/files/{id}/access-logs
│
└── bootstrap/
    └── src/main/resources/
        └── db/migration/
            └── V3__phase3_file_management.sql
```

---

## 구현 워크플로우

### Phase 3A: File Asset + Event 소비 (Core Foundation)
**목표**: Upload 완료 이벤트 소비 → FileAsset 자동 생성 + 메타데이터 조회 + 다운로드 URL 생성

**구현 순서**:
1. **Domain Layer** (2일)
   - `FileAsset` Aggregate Root 구현
   - `Visibility` Enum + `FileMetadataSnapshot` VO
   - `FileAssetCreatedEvent` 도메인 이벤트
   - `FileVariant` Aggregate Root 구현
   - `VariantType` Enum + `VariantSpec` VO

2. **Application Layer** (3일)
   - `ConsumeUploadCompletedUseCase` + Service (이벤트 소비 → FileAsset 생성)
   - `QueryFileMetadataUseCase` + Service (파일 메타데이터 조회)
   - `GenerateDownloadUrlUseCase` + Service (Presigned Download URL 생성)
   - `CreateFileVariantUseCase` + Service (파일 변형 생성)

3. **Adapter-Out-Persistence** (2일)
   - `FileAssetJpaEntity` + Repository + Adapter
   - `FileVariantJpaEntity` + Repository + Adapter
   - QueryDSL 복잡 조회 (메타데이터 필터링)

4. **Adapter-Out-S3** (1일)
   - `FileDownloadPresignedUrlAdapter` (S3 다운로드 URL 생성)

5. **Adapter-Out-IAM** (1일)
   - `FilePermissionEvaluationAdapter` (IAM ABAC 연동)

6. **Adapter-In-Event** (1일)
   - `UploadCompletedEventListener` (Phase 2 이벤트 소비)

7. **Adapter-In-Web** (2일)
   - `FileMetadataQueryController` (GET /api/v1/files/{id})
   - `FileDownloadController` (GET /api/v1/files/{id}/download)
   - `FileVariantController` (POST /api/v1/files/{id}/variants)

8. **통합 테스트** (2일)
   - 이벤트 발행 → 소비 → FileAsset 생성 검증
   - 메타데이터 조회 + 권한 평가 통합
   - 다운로드 URL 생성 + S3 연동

**DoD (Definition of Done)**:
- [ ] Upload 완료 이벤트 소비 시 FileAsset 자동 생성
- [ ] 파일 메타데이터 조회 API 정상 작동 (권한 평가 통합)
- [ ] 다운로드 Presigned URL 생성 (15분 유효, 권한 검증)
- [ ] 파일 변형(Variant) 생성 API 정상 작동
- [ ] 통합 테스트 커버리지 ≥ 80%

---

### Phase 3B: File Relationships + Visibility 관리 (Advanced Features)
**목표**: 파일 간 관계 관리 (버전, 참조, 그룹) + Visibility 레벨 변경 + 관계 기반 조회

**구현 순서**:
1. **Domain Layer** (2일)
   - `FileRelationship` Aggregate Root 구현
   - `RelationshipType` Enum (VERSION, REFERENCE, GROUP)
   - `FileRelationshipCreatedEvent` 도메인 이벤트
   - `FileAsset.updateVisibility()` 메서드 추가

2. **Application Layer** (3일)
   - `LinkFileRelationshipUseCase` + Service (파일 간 관계 설정)
   - `UpdateFileVisibilityUseCase` + Service (Visibility 변경)
   - `QueryFilesByRelationshipUseCase` + Service (관계 기반 파일 조회)

3. **Adapter-Out-Persistence** (2일)
   - `FileRelationshipJpaEntity` + Repository + Adapter
   - QueryDSL 복잡 조회 (관계 그래프 탐색)

4. **Adapter-In-Web** (2일)
   - `FileRelationshipController` (POST /api/v1/files/{id}/relationships)
   - `FileMetadataQueryController` 확장 (PUT /api/v1/files/{id}/visibility)

5. **통합 테스트** (2일)
   - 버전 관계 설정 → 이전 버전 조회
   - 참조 관계 설정 → 참조된 파일 조회
   - Visibility 변경 → 권한 평가 재검증

**DoD (Definition of Done)**:
- [ ] 파일 간 관계(VERSION, REFERENCE, GROUP) 설정 가능
- [ ] 관계 기반 파일 조회 API 정상 작동
- [ ] Visibility 변경 API 정상 작동 (PRIVATE ↔ INTERNAL ↔ PUBLIC)
- [ ] Visibility 변경 시 권한 평가 재검증
- [ ] 통합 테스트 커버리지 ≥ 80%

---

### Phase 3C: Lifecycle + Access Logging + Batch (Operations)
**목표**: 파일 수명 주기 관리 (만료, Soft Delete) + 접근 로그 + Batch 작업

**구현 순서**:
1. **Domain Layer** (1일)
   - `FileAsset.softDelete()` 메서드 추가
   - `FileAsset.isExpired()` 메서드 추가
   - `FileAccessLog` Entity 구현

2. **Application Layer** (3일)
   - `SoftDeleteFileAssetUseCase` + Service (Soft Delete)
   - `QueryFileAccessLogsUseCase` + Service (접근 로그 조회)
   - `ExpireFileAssetsUseCase` + Service (만료된 파일 처리 Batch)

3. **Adapter-Out-Persistence** (2일)
   - `FileAccessLogJpaEntity` + Repository + Adapter
   - QueryDSL 복잡 조회 (접근 로그 필터링)

4. **Adapter-In-Web** (1일)
   - `FileAccessLogController` (GET /api/v1/files/{id}/access-logs)

5. **Batch Job** (2일)
   - `ExpireFileAssetsBatchJob` (@Scheduled, 매일 자정 실행)
   - 만료된 파일 자동 Soft Delete + S3 삭제 (선택)

6. **통합 테스트** (2일)
   - Soft Delete → 조회 불가 + S3 파일 유지 검증
   - 만료 파일 Batch → 자동 처리 검증
   - 접근 로그 기록 → 조회 API 검증

**DoD (Definition of Done)**:
- [ ] 파일 Soft Delete API 정상 작동 (deleted_at 타임스탬프)
- [ ] 접근 로그 조회 API 정상 작동
- [ ] 만료 파일 Batch 작업 정상 실행 (매일 자정)
- [ ] Batch 작업 실행 로그 및 메트릭 수집
- [ ] 통합 테스트 커버리지 ≥ 80%

---

## Phase별 상세 가이드

### Phase 3A: File Asset + Event 소비

#### 1. Domain Layer - FileAsset Aggregate

**`FileAsset.java`** (Aggregate Root):
```java
package com.ryuqq.fileflow.domain.file.asset;

import org.springframework.data.domain.AbstractAggregateRoot;
import java.time.LocalDateTime;

/**
 * FileAsset Aggregate Root
 * Phase 2의 file_metadata 기반으로 생성되는 파일 자산 도메인 모델
 *
 * @author FileFlow Team
 * @since 1.0
 */
public class FileAsset extends AbstractAggregateRoot<FileAsset> {
    private FileAssetId id;
    private FileMetadataSnapshot metadata;  // Phase 2 메타데이터 스냅샷
    private Visibility visibility;
    private LocalDateTime expiresAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;

    /**
     * 정적 팩토리 메서드 - Upload 완료 이벤트로부터 FileAsset 생성
     */
    public static FileAsset fromUploadCompleted(
        String uploadSessionId,
        Long ownerUserId,
        String tenantId,
        Long organizationId,
        String s3Bucket,
        String s3Key,
        String mimeType,
        Long fileSizeBytes,
        String checksum,
        Visibility defaultVisibility,
        LocalDateTime expiresAt
    ) {
        FileAsset asset = new FileAsset();
        asset.id = FileAssetId.generate();
        asset.metadata = FileMetadataSnapshot.of(
            uploadSessionId, ownerUserId, tenantId, organizationId,
            s3Bucket, s3Key, mimeType, fileSizeBytes, checksum
        );
        asset.visibility = defaultVisibility;
        asset.expiresAt = expiresAt;
        asset.createdAt = LocalDateTime.now();

        asset.registerEvent(new FileAssetCreatedEvent(asset.id, asset.metadata));
        return asset;
    }

    /**
     * Visibility 변경 (Phase 3B)
     */
    public void updateVisibility(Visibility newVisibility) {
        if (this.visibility == newVisibility) {
            return;
        }
        this.visibility = newVisibility;
        this.registerEvent(new FileVisibilityChangedEvent(this.id, newVisibility));
    }

    /**
     * Soft Delete (Phase 3C)
     */
    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("Already deleted");
        }
        this.deletedAt = LocalDateTime.now();
        this.registerEvent(new FileAssetDeletedEvent(this.id));
    }

    /**
     * 만료 여부 확인 (Phase 3C)
     */
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // Getters (No Setters - Immutable after creation)
    public FileAssetId getId() { return id; }
    public FileMetadataSnapshot getMetadata() { return metadata; }
    public Visibility getVisibility() { return visibility; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**`Visibility.java`** (Enum):
```java
package com.ryuqq.fileflow.domain.file.asset;

/**
 * 파일 가시성 레벨
 *
 * @author FileFlow Team
 * @since 1.0
 */
public enum Visibility {
    /**
     * 소유자만 접근 가능
     */
    PRIVATE,

    /**
     * 조직 내부 사용자만 접근 가능
     */
    INTERNAL,

    /**
     * 모든 사용자 접근 가능 (인증 필요)
     */
    PUBLIC;

    public boolean allowsAccess(boolean isOwner, boolean isSameOrganization) {
        return switch (this) {
            case PRIVATE -> isOwner;
            case INTERNAL -> isOwner || isSameOrganization;
            case PUBLIC -> true;
        };
    }
}
```

**`FileMetadataSnapshot.java`** (Value Object):
```java
package com.ryuqq.fileflow.domain.file.asset;

/**
 * Phase 2 file_metadata의 스냅샷 (불변 VO)
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record FileMetadataSnapshot(
    String uploadSessionId,
    Long ownerUserId,
    String tenantId,
    Long organizationId,
    String s3Bucket,
    String s3Key,
    String mimeType,
    Long fileSizeBytes,
    String checksum
) {
    public static FileMetadataSnapshot of(
        String uploadSessionId,
        Long ownerUserId,
        String tenantId,
        Long organizationId,
        String s3Bucket,
        String s3Key,
        String mimeType,
        Long fileSizeBytes,
        String checksum
    ) {
        return new FileMetadataSnapshot(
            uploadSessionId, ownerUserId, tenantId, organizationId,
            s3Bucket, s3Key, mimeType, fileSizeBytes, checksum
        );
    }
}
```

#### 2. Domain Layer - FileVariant Aggregate

**`FileVariant.java`** (Aggregate Root):
```java
package com.ryuqq.fileflow.domain.file.variant;

import org.springframework.data.domain.AbstractAggregateRoot;
import java.time.LocalDateTime;

/**
 * FileVariant Aggregate Root
 * 원본 파일의 변형 (썸네일, 최적화 등)
 *
 * @author FileFlow Team
 * @since 1.0
 */
public class FileVariant extends AbstractAggregateRoot<FileVariant> {
    private FileVariantId id;
    private String parentFileAssetId;
    private VariantType variantType;
    private VariantSpec spec;
    private String s3Bucket;
    private String s3Key;
    private Long fileSizeBytes;
    private LocalDateTime createdAt;

    /**
     * 정적 팩토리 메서드 - 파일 변형 생성
     */
    public static FileVariant create(
        String parentFileAssetId,
        VariantType variantType,
        VariantSpec spec,
        String s3Bucket,
        String s3Key,
        Long fileSizeBytes
    ) {
        FileVariant variant = new FileVariant();
        variant.id = FileVariantId.generate();
        variant.parentFileAssetId = parentFileAssetId;
        variant.variantType = variantType;
        variant.spec = spec;
        variant.s3Bucket = s3Bucket;
        variant.s3Key = s3Key;
        variant.fileSizeBytes = fileSizeBytes;
        variant.createdAt = LocalDateTime.now();

        variant.registerEvent(new FileVariantCreatedEvent(variant.id, parentFileAssetId, variantType));
        return variant;
    }

    // Getters
    public FileVariantId getId() { return id; }
    public String getParentFileAssetId() { return parentFileAssetId; }
    public VariantType getVariantType() { return variantType; }
    public VariantSpec getSpec() { return spec; }
    public String getS3Bucket() { return s3Bucket; }
    public String getS3Key() { return s3Key; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**`VariantType.java`** (Enum):
```java
package com.ryuqq.fileflow.domain.file.variant;

/**
 * 파일 변형 타입
 *
 * @author FileFlow Team
 * @since 1.0
 */
public enum VariantType {
    THUMBNAIL,      // 썸네일 (예: 200x200px)
    OPTIMIZED,      // 최적화 (예: WebP 변환)
    RESIZED,        // 리사이징 (예: 1920x1080px)
    PREVIEW,        // 미리보기 (예: 저해상도)
    COMPRESSED      // 압축 (예: ZIP, GZIP)
}
```

**`VariantSpec.java`** (Value Object):
```java
package com.ryuqq.fileflow.domain.file.variant;

/**
 * 파일 변형 스펙 (불변 VO)
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record VariantSpec(
    Integer width,
    Integer height,
    String format,  // "jpeg", "png", "webp", etc.
    Integer quality // 1-100
) {
    public static VariantSpec thumbnail(int size) {
        return new VariantSpec(size, size, "jpeg", 85);
    }

    public static VariantSpec optimized(String format, int quality) {
        return new VariantSpec(null, null, format, quality);
    }

    public static VariantSpec resized(int width, int height) {
        return new VariantSpec(width, height, "jpeg", 90);
    }
}
```

#### 3. Application Layer - Event 소비 Use Case

**`ConsumeUploadCompletedUseCase.java`**:
```java
package com.ryuqq.fileflow.application.file.port.in;

/**
 * Upload 완료 이벤트 소비 → FileAsset 자동 생성
 *
 * @author FileFlow Team
 * @since 1.0
 */
public interface ConsumeUploadCompletedUseCase {
    void consume(ConsumeUploadCompletedCommand command);
}
```

**`ConsumeUploadCompletedCommand.java`**:
```java
package com.ryuqq.fileflow.application.file.command;

/**
 * Upload 완료 이벤트 소비 Command
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record ConsumeUploadCompletedCommand(
    String uploadSessionId,
    Long ownerUserId,
    String tenantId,
    Long organizationId,
    String s3Bucket,
    String s3Key,
    String mimeType,
    Long fileSizeBytes,
    String checksum
) {}
```

**`ConsumeUploadCompletedService.java`** (Use Case Implementation):
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.ConsumeUploadCompletedUseCase;
import com.ryuqq.fileflow.application.file.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Upload 완료 이벤트 소비 Service
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class ConsumeUploadCompletedService implements ConsumeUploadCompletedUseCase {

    private final SaveFileAssetPort saveFileAssetPort;

    public ConsumeUploadCompletedService(SaveFileAssetPort saveFileAssetPort) {
        this.saveFileAssetPort = saveFileAssetPort;
    }

    @Transactional
    @Override
    public void consume(ConsumeUploadCompletedCommand command) {
        // Default: PRIVATE visibility, 90일 만료
        FileAsset asset = FileAsset.fromUploadCompleted(
            command.uploadSessionId(),
            command.ownerUserId(),
            command.tenantId(),
            command.organizationId(),
            command.s3Bucket(),
            command.s3Key(),
            command.mimeType(),
            command.fileSizeBytes(),
            command.checksum(),
            Visibility.PRIVATE,
            LocalDateTime.now().plusDays(90)
        );

        saveFileAssetPort.save(asset);
    }
}
```

#### 4. Application Layer - 메타데이터 조회 Use Case

**`QueryFileMetadataUseCase.java`**:
```java
package com.ryuqq.fileflow.application.file.port.in;

/**
 * 파일 메타데이터 조회 Use Case
 *
 * @author FileFlow Team
 * @since 1.0
 */
public interface QueryFileMetadataUseCase {
    FileMetadataResponse query(FileMetadataQuery query);
}
```

**`FileMetadataQuery.java`**:
```java
package com.ryuqq.fileflow.application.file.query;

/**
 * 파일 메타데이터 조회 Query
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record FileMetadataQuery(
    String fileAssetId,
    Long requesterId  // 권한 평가용
) {}
```

**`FileMetadataResponse.java`**:
```java
package com.ryuqq.fileflow.application.file.query;

import com.ryuqq.fileflow.domain.file.asset.Visibility;
import java.time.LocalDateTime;

/**
 * 파일 메타데이터 조회 Response
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record FileMetadataResponse(
    String fileAssetId,
    String uploadSessionId,
    Long ownerUserId,
    String tenantId,
    Long organizationId,
    String s3Bucket,
    String s3Key,
    String mimeType,
    Long fileSizeBytes,
    String checksum,
    Visibility visibility,
    LocalDateTime expiresAt,
    LocalDateTime createdAt
) {}
```

**`QueryFileMetadataService.java`**:
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.QueryFileMetadataUseCase;
import com.ryuqq.fileflow.application.file.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.application.file.port.out.EvaluateFilePermissionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 메타데이터 조회 Service
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class QueryFileMetadataService implements QueryFileMetadataUseCase {

    private final LoadFileAssetPort loadFileAssetPort;
    private final EvaluateFilePermissionPort evaluatePermissionPort;

    public QueryFileMetadataService(
        LoadFileAssetPort loadFileAssetPort,
        EvaluateFilePermissionPort evaluatePermissionPort
    ) {
        this.loadFileAssetPort = loadFileAssetPort;
        this.evaluatePermissionPort = evaluatePermissionPort;
    }

    @Transactional(readOnly = true)
    @Override
    public FileMetadataResponse query(FileMetadataQuery query) {
        FileAsset asset = loadFileAssetPort.loadById(query.fileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(query.fileAssetId()));

        // 권한 평가 (IAM ABAC)
        boolean hasPermission = evaluatePermissionPort.evaluate(
            query.requesterId(),
            "file.read",
            asset.getMetadata().tenantId(),
            asset.getMetadata().organizationId(),
            Map.of(
                "resource.visibility", asset.getVisibility().name(),
                "resource.ownerId", asset.getMetadata().ownerUserId()
            )
        );

        if (!hasPermission) {
            throw new FileAccessDeniedException(query.fileAssetId());
        }

        return new FileMetadataResponse(
            asset.getId().getValue(),
            asset.getMetadata().uploadSessionId(),
            asset.getMetadata().ownerUserId(),
            asset.getMetadata().tenantId(),
            asset.getMetadata().organizationId(),
            asset.getMetadata().s3Bucket(),
            asset.getMetadata().s3Key(),
            asset.getMetadata().mimeType(),
            asset.getMetadata().fileSizeBytes(),
            asset.getMetadata().checksum(),
            asset.getVisibility(),
            asset.getExpiresAt(),
            asset.getCreatedAt()
        );
    }
}
```

#### 5. Application Layer - 다운로드 URL 생성 Use Case

**`GenerateDownloadUrlUseCase.java`**:
```java
package com.ryuqq.fileflow.application.file.port.in;

/**
 * 파일 다운로드 Presigned URL 생성 Use Case
 *
 * @author FileFlow Team
 * @since 1.0
 */
public interface GenerateDownloadUrlUseCase {
    DownloadUrlResponse generate(GenerateDownloadUrlCommand command);
}
```

**`GenerateDownloadUrlCommand.java`**:
```java
package com.ryuqq.fileflow.application.file.command;

/**
 * 다운로드 URL 생성 Command
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record GenerateDownloadUrlCommand(
    String fileAssetId,
    Long requesterId,
    Integer expirationMinutes  // 기본값: 15분
) {
    public GenerateDownloadUrlCommand {
        if (expirationMinutes == null) {
            expirationMinutes = 15;
        }
    }
}
```

**`DownloadUrlResponse.java`**:
```java
package com.ryuqq.fileflow.application.file.command;

import java.time.LocalDateTime;

/**
 * 다운로드 URL 생성 Response
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record DownloadUrlResponse(
    String downloadUrl,
    LocalDateTime expiresAt
) {}
```

**`GenerateDownloadUrlService.java`**:
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.file.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.application.file.port.out.GeneratePresignedDownloadUrlPort;
import com.ryuqq.fileflow.application.file.port.out.EvaluateFilePermissionPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileAccessLogPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.access.FileAccessLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 다운로드 URL 생성 Service
 *
 * ⚠️ @Transactional 내에서 S3 API 호출 금지 규칙 위반 방지:
 * 1. 권한 평가 + 메타데이터 로드 (@Transactional)
 * 2. 트랜잭션 커밋
 * 3. S3 Presigned URL 생성 (트랜잭션 밖)
 * 4. 접근 로그 비동기 저장
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class GenerateDownloadUrlService implements GenerateDownloadUrlUseCase {

    private final LoadFileAssetPort loadFileAssetPort;
    private final GeneratePresignedDownloadUrlPort generateUrlPort;
    private final EvaluateFilePermissionPort evaluatePermissionPort;
    private final SaveFileAccessLogPort saveAccessLogPort;

    public GenerateDownloadUrlService(
        LoadFileAssetPort loadFileAssetPort,
        GeneratePresignedDownloadUrlPort generateUrlPort,
        EvaluateFilePermissionPort evaluatePermissionPort,
        SaveFileAccessLogPort saveAccessLogPort
    ) {
        this.loadFileAssetPort = loadFileAssetPort;
        this.generateUrlPort = generateUrlPort;
        this.evaluatePermissionPort = evaluatePermissionPort;
        this.saveAccessLogPort = saveAccessLogPort;
    }

    @Override
    public DownloadUrlResponse generate(GenerateDownloadUrlCommand command) {
        // Phase 1: 권한 평가 + 메타데이터 로드 (트랜잭션 내)
        FileAsset asset = loadAndAuthorize(command.fileAssetId(), command.requesterId());

        // Phase 2: S3 Presigned URL 생성 (트랜잭션 밖)
        String downloadUrl = generateUrlPort.generateDownloadUrl(
            asset.getMetadata().s3Bucket(),
            asset.getMetadata().s3Key(),
            command.expirationMinutes()
        );

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(command.expirationMinutes());

        // Phase 3: 접근 로그 비동기 저장
        logAccessAsync(asset, command.requesterId());

        return new DownloadUrlResponse(downloadUrl, expiresAt);
    }

    @Transactional(readOnly = true)
    private FileAsset loadAndAuthorize(String fileAssetId, Long requesterId) {
        FileAsset asset = loadFileAssetPort.loadById(fileAssetId)
            .orElseThrow(() -> new FileAssetNotFoundException(fileAssetId));

        // 권한 평가 (IAM ABAC)
        boolean hasPermission = evaluatePermissionPort.evaluate(
            requesterId,
            "file.download",
            asset.getMetadata().tenantId(),
            asset.getMetadata().organizationId(),
            Map.of(
                "resource.visibility", asset.getVisibility().name(),
                "resource.ownerId", asset.getMetadata().ownerUserId()
            )
        );

        if (!hasPermission) {
            throw new FileAccessDeniedException(fileAssetId);
        }

        return asset;
    }

    private void logAccessAsync(FileAsset asset, Long requesterId) {
        // 비동기 접근 로그 저장 (성능 최적화)
        saveAccessLogPort.saveAsync(FileAccessLog.of(
            asset.getId().getValue(),
            requesterId,
            "DOWNLOAD",
            asset.getMetadata().tenantId(),
            asset.getMetadata().organizationId()
        ));
    }
}
```

#### 6. Adapter-In-Event - Upload 완료 이벤트 리스너

**`UploadCompletedEventListener.java`**:
```java
package com.ryuqq.fileflow.adapter.in.event;

import com.ryuqq.fileflow.application.file.port.in.ConsumeUploadCompletedUseCase;
import com.ryuqq.fileflow.application.file.command.ConsumeUploadCompletedCommand;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Upload 완료 이벤트 리스너
 * Phase 2 → Phase 3 통합 지점
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Component
public class UploadCompletedEventListener {

    private final ConsumeUploadCompletedUseCase consumeUseCase;

    public UploadCompletedEventListener(ConsumeUploadCompletedUseCase consumeUseCase) {
        this.consumeUseCase = consumeUseCase;
    }

    /**
     * Upload 완료 이벤트 소비 (트랜잭션 커밋 후 실행)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUploadCompleted(UploadCompletedEvent event) {
        consumeUseCase.consume(new ConsumeUploadCompletedCommand(
            event.uploadSessionId(),
            event.ownerUserId(),
            event.tenantId(),
            event.organizationId(),
            event.s3Bucket(),
            event.s3Key(),
            event.mimeType(),
            event.fileSizeBytes(),
            event.checksum()
        ));
    }
}
```

#### 7. Adapter-In-Web - REST API Controller

**`FileMetadataQueryController.java`**:
```java
package com.ryuqq.fileflow.adapter.in.web.file;

import com.ryuqq.fileflow.application.file.port.in.QueryFileMetadataUseCase;
import com.ryuqq.fileflow.application.file.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.query.FileMetadataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 파일 메타데이터 조회 Controller
 *
 * @author FileFlow Team
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileMetadataQueryController {

    private final QueryFileMetadataUseCase queryUseCase;

    public FileMetadataQueryController(QueryFileMetadataUseCase queryUseCase) {
        this.queryUseCase = queryUseCase;
    }

    /**
     * GET /api/v1/files/{id}
     * 파일 메타데이터 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(
        @PathVariable("id") String fileAssetId,
        @RequestHeader("X-User-Context-Id") Long requesterId
    ) {
        FileMetadataResponse response = queryUseCase.query(
            new FileMetadataQuery(fileAssetId, requesterId)
        );
        return ResponseEntity.ok(response);
    }
}
```

**`FileDownloadController.java`**:
```java
package com.ryuqq.fileflow.adapter.in.web.file;

import com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.file.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.file.command.DownloadUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 파일 다운로드 Controller
 *
 * @author FileFlow Team
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileDownloadController {

    private final GenerateDownloadUrlUseCase generateUrlUseCase;

    public FileDownloadController(GenerateDownloadUrlUseCase generateUrlUseCase) {
        this.generateUrlUseCase = generateUrlUseCase;
    }

    /**
     * GET /api/v1/files/{id}/download
     * 파일 다운로드 Presigned URL 생성
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<DownloadUrlResponse> generateDownloadUrl(
        @PathVariable("id") String fileAssetId,
        @RequestHeader("X-User-Context-Id") Long requesterId,
        @RequestParam(value = "expiration", defaultValue = "15") Integer expirationMinutes
    ) {
        DownloadUrlResponse response = generateUrlUseCase.generate(
            new GenerateDownloadUrlCommand(fileAssetId, requesterId, expirationMinutes)
        );
        return ResponseEntity.ok(response);
    }
}
```

---

### Phase 3B: File Relationships + Visibility 관리

#### 1. Domain Layer - FileRelationship Aggregate

**`FileRelationship.java`**:
```java
package com.ryuqq.fileflow.domain.file.relationship;

import org.springframework.data.domain.AbstractAggregateRoot;
import java.time.LocalDateTime;

/**
 * FileRelationship Aggregate Root
 * 파일 간 관계 (버전, 참조, 그룹)
 *
 * @author FileFlow Team
 * @since 1.0
 */
public class FileRelationship extends AbstractAggregateRoot<FileRelationship> {
    private Long id;
    private String sourceFileAssetId;
    private String targetFileAssetId;
    private RelationshipType relationshipType;
    private LocalDateTime createdAt;

    /**
     * 정적 팩토리 메서드 - 파일 간 관계 설정
     */
    public static FileRelationship link(
        String sourceFileAssetId,
        String targetFileAssetId,
        RelationshipType relationshipType
    ) {
        FileRelationship relationship = new FileRelationship();
        relationship.sourceFileAssetId = sourceFileAssetId;
        relationship.targetFileAssetId = targetFileAssetId;
        relationship.relationshipType = relationshipType;
        relationship.createdAt = LocalDateTime.now();

        relationship.registerEvent(new FileRelationshipCreatedEvent(
            sourceFileAssetId, targetFileAssetId, relationshipType
        ));
        return relationship;
    }

    // Getters
    public Long getId() { return id; }
    public String getSourceFileAssetId() { return sourceFileAssetId; }
    public String getTargetFileAssetId() { return targetFileAssetId; }
    public RelationshipType getRelationshipType() { return relationshipType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**`RelationshipType.java`**:
```java
package com.ryuqq.fileflow.domain.file.relationship;

/**
 * 파일 간 관계 타입
 *
 * @author FileFlow Team
 * @since 1.0
 */
public enum RelationshipType {
    VERSION,    // 버전 관계 (이전 버전 → 새 버전)
    REFERENCE,  // 참조 관계 (문서 → 첨부 파일)
    GROUP       // 그룹 관계 (앨범 → 사진들)
}
```

#### 2. Application Layer - 관계 설정 Use Case

**`LinkFileRelationshipUseCase.java`**:
```java
package com.ryuqq.fileflow.application.file.port.in;

/**
 * 파일 간 관계 설정 Use Case
 *
 * @author FileFlow Team
 * @since 1.0
 */
public interface LinkFileRelationshipUseCase {
    void link(LinkFileRelationshipCommand command);
}
```

**`LinkFileRelationshipCommand.java`**:
```java
package com.ryuqq.fileflow.application.file.command;

import com.ryuqq.fileflow.domain.file.relationship.RelationshipType;

/**
 * 파일 간 관계 설정 Command
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record LinkFileRelationshipCommand(
    String sourceFileAssetId,
    String targetFileAssetId,
    RelationshipType relationshipType,
    Long requesterId  // 권한 평가용
) {}
```

**`LinkFileRelationshipService.java`**:
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.LinkFileRelationshipUseCase;
import com.ryuqq.fileflow.application.file.port.out.SaveFileRelationshipPort;
import com.ryuqq.fileflow.application.file.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.domain.file.relationship.FileRelationship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 간 관계 설정 Service
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class LinkFileRelationshipService implements LinkFileRelationshipUseCase {

    private final SaveFileRelationshipPort saveRelationshipPort;
    private final LoadFileAssetPort loadFileAssetPort;

    public LinkFileRelationshipService(
        SaveFileRelationshipPort saveRelationshipPort,
        LoadFileAssetPort loadFileAssetPort
    ) {
        this.saveRelationshipPort = saveRelationshipPort;
        this.loadFileAssetPort = loadFileAssetPort;
    }

    @Transactional
    @Override
    public void link(LinkFileRelationshipCommand command) {
        // 두 파일 모두 존재하는지 검증
        loadFileAssetPort.loadById(command.sourceFileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.sourceFileAssetId()));
        loadFileAssetPort.loadById(command.targetFileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.targetFileAssetId()));

        // 관계 생성
        FileRelationship relationship = FileRelationship.link(
            command.sourceFileAssetId(),
            command.targetFileAssetId(),
            command.relationshipType()
        );

        saveRelationshipPort.save(relationship);
    }
}
```

---

### Phase 3C: Lifecycle + Access Logging + Batch

#### 1. Domain Layer - FileAccessLog Entity

**`FileAccessLog.java`**:
```java
package com.ryuqq.fileflow.domain.file.access;

import java.time.LocalDateTime;

/**
 * FileAccessLog Entity
 * 파일 접근 감사 로그
 *
 * @author FileFlow Team
 * @since 1.0
 */
public class FileAccessLog {
    private Long id;
    private String fileAssetId;
    private Long accessorUserId;
    private String accessType;  // "READ", "DOWNLOAD", "DELETE"
    private String tenantId;
    private Long organizationId;
    private LocalDateTime accessedAt;
    private String requestIp;
    private String userAgent;

    public static FileAccessLog of(
        String fileAssetId,
        Long accessorUserId,
        String accessType,
        String tenantId,
        Long organizationId
    ) {
        FileAccessLog log = new FileAccessLog();
        log.fileAssetId = fileAssetId;
        log.accessorUserId = accessorUserId;
        log.accessType = accessType;
        log.tenantId = tenantId;
        log.organizationId = organizationId;
        log.accessedAt = LocalDateTime.now();
        return log;
    }

    // Getters
    public Long getId() { return id; }
    public String getFileAssetId() { return fileAssetId; }
    public Long getAccessorUserId() { return accessorUserId; }
    public String getAccessType() { return accessType; }
    public String getTenantId() { return tenantId; }
    public Long getOrganizationId() { return organizationId; }
    public LocalDateTime getAccessedAt() { return accessedAt; }
}
```

#### 2. Application Layer - Soft Delete Use Case

**`SoftDeleteFileAssetUseCase.java`**:
```java
package com.ryuqq.fileflow.application.file.port.in;

/**
 * 파일 Soft Delete Use Case
 *
 * @author FileFlow Team
 * @since 1.0
 */
public interface SoftDeleteFileAssetUseCase {
    void softDelete(SoftDeleteFileAssetCommand command);
}
```

**`SoftDeleteFileAssetCommand.java`**:
```java
package com.ryuqq.fileflow.application.file.command;

/**
 * 파일 Soft Delete Command
 *
 * @author FileFlow Team
 * @since 1.0
 */
public record SoftDeleteFileAssetCommand(
    String fileAssetId,
    Long requesterId  // 권한 평가용
) {}
```

**`SoftDeleteFileAssetService.java`**:
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.SoftDeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.file.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.application.file.port.out.EvaluateFilePermissionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 Soft Delete Service
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class SoftDeleteFileAssetService implements SoftDeleteFileAssetUseCase {

    private final LoadFileAssetPort loadFileAssetPort;
    private final SaveFileAssetPort saveFileAssetPort;
    private final EvaluateFilePermissionPort evaluatePermissionPort;

    public SoftDeleteFileAssetService(
        LoadFileAssetPort loadFileAssetPort,
        SaveFileAssetPort saveFileAssetPort,
        EvaluateFilePermissionPort evaluatePermissionPort
    ) {
        this.loadFileAssetPort = loadFileAssetPort;
        this.saveFileAssetPort = saveFileAssetPort;
        this.evaluatePermissionPort = evaluatePermissionPort;
    }

    @Transactional
    @Override
    public void softDelete(SoftDeleteFileAssetCommand command) {
        FileAsset asset = loadFileAssetPort.loadById(command.fileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        // 권한 평가 (file.delete 권한 필요)
        boolean hasPermission = evaluatePermissionPort.evaluate(
            command.requesterId(),
            "file.delete",
            asset.getMetadata().tenantId(),
            asset.getMetadata().organizationId(),
            Map.of(
                "resource.ownerId", asset.getMetadata().ownerUserId()
            )
        );

        if (!hasPermission) {
            throw new FileAccessDeniedException(command.fileAssetId());
        }

        asset.softDelete();
        saveFileAssetPort.save(asset);
    }
}
```

#### 3. Batch Job - 만료 파일 처리

**`ExpireFileAssetsBatchJob.java`**:
```java
package com.ryuqq.fileflow.adapter.in.batch;

import com.ryuqq.fileflow.application.file.port.in.ExpireFileAssetsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 파일 자동 처리 Batch Job
 * 매일 자정 실행
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Component
public class ExpireFileAssetsBatchJob {

    private final ExpireFileAssetsUseCase expireUseCase;

    public ExpireFileAssetsBatchJob(ExpireFileAssetsUseCase expireUseCase) {
        this.expireUseCase = expireUseCase;
    }

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void expireFiles() {
        expireUseCase.expire();
    }
}
```

**`ExpireFileAssetsService.java`**:
```java
package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.ExpireFileAssetsUseCase;
import com.ryuqq.fileflow.application.file.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 만료된 파일 처리 Service
 *
 * @author FileFlow Team
 * @since 1.0
 */
@Service
public class ExpireFileAssetsService implements ExpireFileAssetsUseCase {

    private final LoadFileAssetPort loadFileAssetPort;
    private final SaveFileAssetPort saveFileAssetPort;

    public ExpireFileAssetsService(
        LoadFileAssetPort loadFileAssetPort,
        SaveFileAssetPort saveFileAssetPort
    ) {
        this.loadFileAssetPort = loadFileAssetPort;
        this.saveFileAssetPort = saveFileAssetPort;
    }

    @Transactional
    @Override
    public void expire() {
        List<FileAsset> expiredAssets = loadFileAssetPort.loadExpiredAssets();

        expiredAssets.forEach(asset -> {
            asset.softDelete();
            saveFileAssetPort.save(asset);
        });
    }
}
```

---

## 테스트 전략

### 1. Domain Layer 테스트
- **Aggregate 단위 테스트**: FileAsset, FileVariant, FileRelationship 생성/수정 로직
- **Value Object 테스트**: Visibility, VariantSpec 불변성 검증
- **Domain Event 테스트**: 이벤트 발행 확인

### 2. Application Layer 테스트
- **Use Case 단위 테스트**: Mock Port를 사용한 비즈니스 로직 검증
- **권한 평가 통합**: IAM ABAC 엔진과의 통합 테스트
- **이벤트 소비 테스트**: Upload 완료 → FileAsset 생성 흐름

### 3. Adapter Layer 테스트
- **Persistence Adapter**: JpaRepository + QueryDSL 통합 테스트 (TestContainers)
- **S3 Adapter**: LocalStack 기반 S3 Mock 테스트
- **IAM Adapter**: IAM 서비스 Mock 테스트

### 4. API 계층 테스트
- **Controller 단위 테스트**: MockMvc + @WebMvcTest
- **E2E 테스트**: @SpringBootTest + TestContainers (MySQL, Redis, S3)

### 5. Batch Job 테스트
- **Scheduled 작업 테스트**: @Scheduled 비활성화 후 수동 실행 검증

---

## 배포 및 운영

### 1. Database Migration
```sql
-- Flyway: V3__phase3_file_management.sql
-- file_assets, file_variants, file_relationships, file_access_log 테이블 생성
```

### 2. 환경 변수 설정
```yaml
aws:
  s3:
    bucket: fileflow-demo-bucket
    region: ap-northeast-2

spring:
  redis:
    host: localhost
    port: 6379

fileflow:
  file:
    default-expiration-days: 90
    download-url-expiration-minutes: 15
```

### 3. Monitoring & Observability
- **Batch Job 메트릭**: 만료 파일 처리 건수, 실행 시간
- **API 응답 시간**: 메타데이터 조회, 다운로드 URL 생성
- **접근 로그 통계**: 파일별 다운로드 횟수, 인기 파일 TOP 10

### 4. 알림 설정
- **Batch Job 실패**: Slack/Email 알림
- **S3 접근 실패**: CloudWatch 알람
- **권한 평가 실패**: 보안 이벤트 로그

---

## 참고 자료

- [Phase 1: IAM Implementation Workflow](../01/00-implementation-workflow.md)
- [Phase 2: Upload Management Implementation Workflow](../02/00-implementation-workflow.md)
- [Phase 3: File Management Requirements](./01-file-management.md)
- [Phase 3: File Management Development Guide](./02-file-managment-develop-guide.md)
- [Hexagonal Architecture 가이드](../../architecture/hexagonal-architecture.md)
- [AWS S3 Presigned URL Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html)
