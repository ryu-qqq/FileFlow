package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;

import java.time.LocalDateTime;

/**
 * FileAssetJpaEntity Test Fixture
 *
 * <p>테스트에서 FileAssetJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * FileAssetJpaEntity file = FileAssetJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * FileAssetJpaEntity file = FileAssetJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * FileAssetJpaEntity file = FileAssetJpaEntityFixture.create(1L, 1L, 1L, "test.jpg");
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetJpaEntityFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long DEFAULT_OWNER_USER_ID = 1L;
    private static final String DEFAULT_FILE_NAME = "test-file.jpg";
    private static final Long DEFAULT_FILE_SIZE = 1024000L;
    private static final String DEFAULT_MIME_TYPE = "image/jpeg";
    private static final String DEFAULT_STORAGE_KEY = "uploads/2024/01/test-file.jpg";
    private static final String DEFAULT_CHECKSUM_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final Long DEFAULT_UPLOAD_SESSION_ID = 1L;
    private static final FileStatus DEFAULT_STATUS = FileStatus.AVAILABLE;
    private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
    private static final LocalDateTime DEFAULT_UPLOADED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private FileAssetJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 FileAssetJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity create() {
        return FileAssetJpaEntity.create(
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_UPLOADED_AT
        );
    }

    /**
     * 커스텀 FileAssetJpaEntity 생성 (ID 없음)
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId Owner User ID
     * @param fileName File Name
     * @return 새로운 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity create(Long tenantId, Long organizationId, Long ownerUserId, String fileName) {
        return FileAssetJpaEntity.create(
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_UPLOADED_AT
        );
    }

    /**
     * ID를 포함한 FileAssetJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id File Asset ID
     * @return 재구성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity createWithId(Long id) {
        return FileAssetJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_STATUS,
            DEFAULT_VISIBILITY,
            DEFAULT_UPLOADED_AT,
            null,
            null,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 ID를 포함한 FileAssetJpaEntity 생성 (재구성)
     *
     * @param id File Asset ID
     * @param tenantId Tenant ID
     * @param fileName File Name
     * @return 재구성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity createWithId(Long id, Long tenantId, String fileName) {
        return FileAssetJpaEntity.reconstitute(
            id,
            tenantId,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            fileName,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            DEFAULT_STATUS,
            DEFAULT_VISIBILITY,
            DEFAULT_UPLOADED_AT,
            null,
            null,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 특정 상태의 FileAssetJpaEntity 생성 (재구성)
     *
     * @param id File Asset ID
     * @param status File Status
     * @return 재구성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity createWithStatus(Long id, FileStatus status) {
        return FileAssetJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            status,
            DEFAULT_VISIBILITY,
            DEFAULT_UPLOADED_AT,
            null,
            null,
            null,
            null,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 삭제된 FileAssetJpaEntity 생성 (재구성)
     *
     * @param id File Asset ID
     * @return 삭제된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity createDeleted(Long id) {
        return FileAssetJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            DEFAULT_FILE_NAME,
            DEFAULT_FILE_SIZE,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY,
            DEFAULT_CHECKSUM_SHA256,
            DEFAULT_UPLOAD_SESSION_ID,
            FileStatus.DELETED,
            DEFAULT_VISIBILITY,
            DEFAULT_UPLOADED_AT,
            null,
            null,
            null,
            LocalDateTime.now(),
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 여러 개의 FileAssetJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return FileAssetJpaEntity 배열
     */
    public static FileAssetJpaEntity[] createMultiple(int count) {
        FileAssetJpaEntity[] entities = new FileAssetJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_TENANT_ID,
                "test-file-" + (i + 1) + ".jpg"
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 FileAssetJpaEntity 생성 (재구성)
     *
     * @param id File Asset ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId Owner User ID
     * @param fileName File Name
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param storageKey Storage Key
     * @param checksumSha256 Checksum SHA-256
     * @param uploadSessionId Upload Session ID
     * @param status File Status
     * @param visibility Visibility
     * @param uploadedAt Uploaded At
     * @param processedAt Processed At
     * @param expiresAt Expires At
     * @param retentionDays Retention Days
     * @param deletedAt Deleted At
     * @param createdAt Created At
     * @param updatedAt Updated At
     * @return 재구성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity reconstitute(
        Long id,
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksumSha256,
        Long uploadSessionId,
        FileStatus status,
        Visibility visibility,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return FileAssetJpaEntity.reconstitute(
            id,
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            fileSize,
            mimeType,
            storageKey,
            checksumSha256,
            uploadSessionId,
            status,
            visibility,
            uploadedAt,
            processedAt,
            expiresAt,
            retentionDays,
            deletedAt,
            createdAt,
            updatedAt
        );
    }
}
