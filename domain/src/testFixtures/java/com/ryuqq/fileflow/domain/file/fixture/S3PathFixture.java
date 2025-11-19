package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * S3Path Test Fixture (Object Mother)
 */
public final class S3PathFixture {

    public static final Long DEFAULT_TENANT_ID = 123L;
    public static final String DEFAULT_SELLER_NAME = "seller1";
    public static final String DEFAULT_CUSTOM_PATH = "uploads";
    public static final String DEFAULT_FILE_ID = "file-123";
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

    private S3PathFixture() {
    }

    /**
     * S3Path 생성 팩토리 메서드
     */
    public static S3Path from(
        UserRole role,
        Long tenantId,
        String sellerName,
        String customPath,
        String fileId,
        String mimeType
    ) {
        return S3Path.from(role, tenantId, sellerName, customPath, fileId, mimeType);
    }

    /**
     * ADMIN Role용 S3Path 생성
     */
    public static S3Path forAdmin() {
        return from(UserRole.ADMIN, DEFAULT_TENANT_ID, null, DEFAULT_CUSTOM_PATH, DEFAULT_FILE_ID, DEFAULT_MIME_TYPE);
    }

    /**
     * ADMIN Role용 S3Path 생성 (커스텀 파라미터)
     */
    public static S3Path forAdmin(Long tenantId, String customPath, String fileId, String mimeType) {
        return from(UserRole.ADMIN, tenantId, null, customPath, fileId, mimeType);
    }

    /**
     * SELLER Role용 S3Path 생성
     */
    public static S3Path forSeller() {
        return from(UserRole.SELLER, DEFAULT_TENANT_ID, DEFAULT_SELLER_NAME, DEFAULT_CUSTOM_PATH, DEFAULT_FILE_ID, DEFAULT_MIME_TYPE);
    }

    /**
     * SELLER Role용 S3Path 생성 (커스텀 파라미터)
     */
    public static S3Path forSeller(Long tenantId, String sellerName, String customPath, String fileId, String mimeType) {
        return from(UserRole.SELLER, tenantId, sellerName, customPath, fileId, mimeType);
    }

    /**
     * DEFAULT Role용 S3Path 생성
     */
    public static S3Path forDefault() {
        return from(UserRole.DEFAULT, DEFAULT_TENANT_ID, DEFAULT_SELLER_NAME, DEFAULT_CUSTOM_PATH, DEFAULT_FILE_ID, DEFAULT_MIME_TYPE);
    }

    /**
     * DEFAULT Role용 S3Path 생성 (커스텀 파라미터)
     */
    public static S3Path forDefault(Long tenantId, String sellerName, String customPath, String fileId, String mimeType) {
        return from(UserRole.DEFAULT, tenantId, sellerName, customPath, fileId, mimeType);
    }
}

