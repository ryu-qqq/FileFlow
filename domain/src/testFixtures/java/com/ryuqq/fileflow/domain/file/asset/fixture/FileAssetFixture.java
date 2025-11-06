package com.ryuqq.fileflow.domain.file.asset.fixture;

import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.fixture.FileIdFixture;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import com.ryuqq.fileflow.domain.file.asset.S3UploadMetadata;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.UploadType;

import java.time.LocalDateTime;

/**
 * FileAsset Test Fixture
 *
 * <p>테스트에서 FileAsset 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 FileAsset 생성
 * FileAsset fileAsset = FileAssetFixture.create();
 *
 * // 특정 ID로 생성
 * FileAsset fileAsset = FileAssetFixture.createWithId(1L);
 *
 * // AVAILABLE 상태로 생성
 * FileAsset fileAsset = FileAssetFixture.createAvailable();
 *
 * // PROCESSING 상태로 생성
 * FileAsset fileAsset = FileAssetFixture.createProcessing();
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAssetFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long DEFAULT_OWNER_USER_ID = 1L;
    private static final String DEFAULT_FILE_NAME = "test-file.jpg";
    private static final Long DEFAULT_FILE_SIZE = 1024000L;
    private static final String DEFAULT_MIME_TYPE = "image/jpeg";
    private static final String DEFAULT_STORAGE_KEY = "uploads/tenant-1/2024/11/06/test-file.jpg";
    private static final String DEFAULT_CHECKSUM = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final Long DEFAULT_UPLOAD_SESSION_ID = 1L;

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private FileAssetFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 FileAsset 생성 (ID 없음, PROCESSING 상태)
     *
     * @return 새로운 FileAsset
     */
    public static FileAsset create() {
        return FileAsset.forNew(
            TenantId.of(DEFAULT_TENANT_ID),
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            FileName.of(DEFAULT_FILE_NAME),
            FileSize.of(DEFAULT_FILE_SIZE),
            MimeType.of(DEFAULT_MIME_TYPE),
            StorageKey.of(DEFAULT_STORAGE_KEY),
            Checksum.of(DEFAULT_CHECKSUM),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID)
        );
    }

    /**
     * ID를 포함한 FileAsset 재구성 (AVAILABLE 상태)
     *
     * @param id FileAsset ID
     * @return 재구성된 FileAsset
     */
    public static FileAsset createWithId(Long id) {
        return FileAsset.reconstitute(
            FileIdFixture.create(id),
            TenantId.of(DEFAULT_TENANT_ID),
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            FileName.of(DEFAULT_FILE_NAME),
            FileSize.of(DEFAULT_FILE_SIZE),
            MimeType.of(DEFAULT_MIME_TYPE),
            StorageKey.of(DEFAULT_STORAGE_KEY),
            Checksum.of(DEFAULT_CHECKSUM),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID),
            FileStatus.AVAILABLE,
            Visibility.PRIVATE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1),
            null,
            null,
            null
        );
    }

    /**
     * AVAILABLE 상태의 FileAsset 생성
     *
     * @param id FileAsset ID
     * @return AVAILABLE 상태의 FileAsset
     */
    public static FileAsset createAvailable(Long id) {
        // createWithId()는 이미 AVAILABLE 상태로 재구성하므로 markAsAvailable() 호출 불필요
        return createWithId(id);
    }

    /**
     * AVAILABLE 상태의 FileAsset 생성 (기본 ID = 1L)
     *
     * @return AVAILABLE 상태의 FileAsset
     */
    public static FileAsset createAvailable() {
        return createAvailable(1L);
    }

    /**
     * PROCESSING 상태의 FileAsset 생성
     *
     * @param id FileAsset ID
     * @return PROCESSING 상태의 FileAsset
     */
    public static FileAsset createProcessing(Long id) {
        return FileAsset.reconstitute(
            FileIdFixture.create(id),
            TenantId.of(DEFAULT_TENANT_ID),
            DEFAULT_ORGANIZATION_ID,
            DEFAULT_OWNER_USER_ID,
            FileName.of(DEFAULT_FILE_NAME),
            FileSize.of(DEFAULT_FILE_SIZE),
            MimeType.of(DEFAULT_MIME_TYPE),
            StorageKey.of(DEFAULT_STORAGE_KEY),
            Checksum.of(DEFAULT_CHECKSUM),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID),
            FileStatus.PROCESSING,
            Visibility.PRIVATE,
            LocalDateTime.now().minusHours(1),
            null,
            null,
            null,
            null
        );
    }

    /**
     * PROCESSING 상태의 FileAsset 생성 (기본 ID = 1L)
     *
     * @return PROCESSING 상태의 FileAsset
     */
    public static FileAsset createProcessing() {
        return createProcessing(1L);
    }

    /**
     * DELETED 상태의 FileAsset 생성
     *
     * @param id FileAsset ID
     * @return DELETED 상태의 FileAsset
     */
    public static FileAsset createDeleted(Long id) {
        FileAsset fileAsset = createAvailable(id);
        fileAsset.softDelete();
        return fileAsset;
    }

    /**
     * DELETED 상태의 FileAsset 생성 (기본 ID = 1L)
     *
     * @return DELETED 상태의 FileAsset
     */
    public static FileAsset createDeleted() {
        return createDeleted(1L);
    }

    /**
     * 익명 업로드 FileAsset 생성 (ownerUserId = null)
     *
     * @param id FileAsset ID
     * @return ownerUserId가 null인 FileAsset
     */
    public static FileAsset createAnonymous(Long id) {
        return FileAsset.reconstitute(
            FileIdFixture.create(id),
            TenantId.of(DEFAULT_TENANT_ID),
            null,
            null,
            FileName.of(DEFAULT_FILE_NAME),
            FileSize.of(DEFAULT_FILE_SIZE),
            MimeType.of(DEFAULT_MIME_TYPE),
            StorageKey.of(DEFAULT_STORAGE_KEY),
            Checksum.of(DEFAULT_CHECKSUM),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID),
            FileStatus.AVAILABLE,
            Visibility.PRIVATE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1),
            null,
            null,
            null
        );
    }

    /**
     * S3 업로드로 FileAsset 생성
     *
     * @param session UploadSession
     * @return S3 업로드로 생성된 FileAsset
     */
    public static FileAsset createFromS3Upload(UploadSession session) {
        S3UploadMetadata s3Metadata = S3UploadMetadata.of(
            DEFAULT_FILE_SIZE,
            DEFAULT_CHECKSUM,
            DEFAULT_MIME_TYPE,
            DEFAULT_STORAGE_KEY
        );
        return FileAsset.fromS3Upload(session, s3Metadata);
    }

    /**
     * 커스텀 FileAsset 생성
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param ownerUserId 소유자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey 스토리지 키
     * @param checksum 체크섬
     * @return 새로운 FileAsset
     */
    public static FileAsset createCustom(
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksum
    ) {
        return FileAsset.forNew(
            TenantId.of(tenantId),
            organizationId,
            ownerUserId,
            FileName.of(fileName),
            FileSize.of(fileSize),
            MimeType.of(mimeType),
            StorageKey.of(storageKey),
            Checksum.of(checksum),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID)
        );
    }

    /**
     * Custom FileAsset 생성 (ID 포함, PROCESSING 상태)
     *
     * @param id             FileAsset ID
     * @param tenantId       Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId    Owner User ID
     * @param fileName       파일명
     * @param fileSize       파일 크기
     * @param mimeType       MIME 타입
     * @param storageKey     저장 키
     * @param checksum       체크섬
     * @return Custom FileAsset (ID 포함)
     */
    public static FileAsset createCustomWithId(
        Long id,
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksum
    ) {
        return FileAsset.reconstitute(
            FileIdFixture.create(id),
            TenantId.of(tenantId),
            organizationId,
            ownerUserId,
            FileName.of(fileName),
            FileSize.of(fileSize),
            MimeType.of(mimeType),
            StorageKey.of(storageKey),
            Checksum.of(checksum),
            UploadSessionId.of(DEFAULT_UPLOAD_SESSION_ID),
            FileStatus.PROCESSING,
            Visibility.PRIVATE,
            LocalDateTime.now().minusHours(1),
            null,
            null,
            null,
            null
        );
    }
}

