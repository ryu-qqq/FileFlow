package com.ryuqq.fileflow.integration.helper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 통합 테스트용 데이터 삽입 헬퍼.
 *
 * JdbcTemplate을 사용하여 테스트 데이터를 직접 삽입합니다.
 * Domain/Application 레이어를 우회하여 빠르게 테스트 데이터를 설정할 수 있습니다.
 *
 * 주의사항:
 * - 이 클래스는 통합 테스트에서만 사용해야 합니다.
 * - 테이블 구조가 변경되면 이 클래스도 업데이트해야 합니다.
 */
@Component
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;

    public TestDataHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ========================================
    // Single Upload Session
    // ========================================

    /**
     * SingleUploadSession 테스트 데이터를 삽입합니다.
     * 현재 스키마에 맞게 필수 컬럼들을 모두 설정합니다.
     */
    public String insertSingleUploadSession(
        String id,
        String idempotencyKey,
        String fileName,
        String contentType,
        long fileSize,
        String status
    ) {
        String sql = """
            INSERT INTO single_upload_session (
                id, idempotency_key, user_id, organization_id, organization_name,
                organization_namespace, tenant_id, tenant_name, user_role, email,
                file_name, file_size, content_type, bucket, s3_key, expires_at,
                status, presigned_url, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime now = LocalDateTime.now();
        String userId = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();
        String tenantId = UUID.randomUUID().toString();

        jdbcTemplate.update(sql,
            id, idempotencyKey, userId, organizationId, "TestOrg",
            "test-ns", tenantId, "TestTenant", "SELLER", "test@example.com",
            fileName, fileSize, contentType, "test-bucket",
            "uploads/test/" + fileName, now.plusHours(1),
            status, "https://presigned.url/example", 0L, now, now
        );

        return id;
    }

    public String insertSingleUploadSession(String fileName) {
        return insertSingleUploadSession(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            fileName,
            "image/jpeg",
            1024L,
            "COMPLETED"
        );
    }

    // ========================================
    // File Asset
    // ========================================

    public Long insertFileAsset(
        String assetId,
        Long uploadSessionId,
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize,
        String status
    ) {
        String sql = """
            INSERT INTO file_asset (
                asset_id, upload_session_id, original_file_name, stored_file_name,
                content_type, file_size, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
            assetId, uploadSessionId, originalFileName, storedFileName,
            contentType, fileSize, status, now, now
        );

        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Long insertFileAsset(Long uploadSessionId, String originalFileName) {
        return insertFileAsset(
            UUID.randomUUID().toString(),
            uploadSessionId,
            originalFileName,
            "stored_" + originalFileName,
            "image/jpeg",
            1024L,
            "UPLOADED"
        );
    }

    // ========================================
    // Processed File Asset
    // ========================================

    public Long insertProcessedFileAsset(
        Long fileAssetId,
        String variantType,
        String storedFileName,
        int width,
        int height,
        long fileSize
    ) {
        String sql = """
            INSERT INTO processed_file_asset (
                file_asset_id, variant_type, stored_file_name,
                width, height, file_size, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
            fileAssetId, variantType, storedFileName,
            width, height, fileSize, now, now
        );

        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    // ========================================
    // External Download
    // ========================================

    /**
     * ExternalDownload 테스트 데이터를 삽입합니다.
     * 현재 스키마에 맞게 필수 컬럼들을 모두 설정합니다.
     *
     * @return 생성된 ExternalDownload ID (UUID String)
     */
    public String insertExternalDownload(
        String id,
        String idempotencyKey,
        String sourceUrl,
        String tenantId,
        String organizationId,
        String status,
        String webhookUrl
    ) {
        String sql = """
            INSERT INTO external_download (
                id, idempotency_key, source_url, tenant_id, organization_id,
                s3_bucket, s3_path_prefix, status, retry_count, file_asset_id,
                error_message, webhook_url, version, created_at, updated_at
            ) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
            id, idempotencyKey, sourceUrl, tenantId, organizationId,
            "fileflow-uploads-prod", "external-downloads/" + tenantId, status, 0, null,
            null, webhookUrl, 0L, now, now
        );

        return id;
    }

    /**
     * 테넌트 정보를 포함하여 ExternalDownload를 삽입합니다.
     */
    public String insertExternalDownload(String sourceUrl, String tenantId, String organizationId) {
        return insertExternalDownload(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            sourceUrl,
            tenantId,
            organizationId,
            "PENDING",
            "https://webhook.example.com/callback"
        );
    }

    /**
     * 기본값으로 ExternalDownload를 삽입합니다.
     */
    public String insertExternalDownload(String sourceUrl) {
        return insertExternalDownload(
            sourceUrl,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
    }

    // ========================================
    // Utility Methods
    // ========================================

    public void executeRawSql(String sql, Object... args) {
        jdbcTemplate.update(sql, args);
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        return jdbcTemplate.queryForObject(sql, requiredType, args);
    }

    public int count(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + tableName, Integer.class);
    }
}
