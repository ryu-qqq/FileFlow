package com.ryuqq.fileflow.application.download.fixture;

import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.FileName;

/**
 * StartExternalDownloadCommand Test Fixture
 *
 * <p>테스트에서 StartExternalDownloadCommand 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class StartExternalDownloadCommandFixture {

    private static final String DEFAULT_IDEMPOTENCY_KEY = "test-idem-key-001";
    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String DEFAULT_SOURCE_URL = "https://example.com/files/document.pdf";
    private static final String DEFAULT_FILE_NAME = "document.pdf";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private StartExternalDownloadCommandFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 Command 생성
     *
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand create() {
        return StartExternalDownloadCommand.of(
            DEFAULT_IDEMPOTENCY_KEY,
            TenantId.of(DEFAULT_TENANT_ID),
            DEFAULT_SOURCE_URL,
            FileName.of(DEFAULT_FILE_NAME)
        );
    }

    /**
     * 특정 값으로 Command 생성
     *
     * @param idempotencyKey 멱등키
     * @param tenantId 테넌트 ID
     * @param sourceUrl 소스 URL
     * @param fileName 파일명
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand create(
        String idempotencyKey,
        Long tenantId,
        String sourceUrl,
        String fileName
    ) {
        return StartExternalDownloadCommand.of(
            idempotencyKey,
            TenantId.of(tenantId),
            sourceUrl,
            FileName.of(fileName)
        );
    }

    /**
     * HTTP URL로 Command 생성
     *
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand createWithHttpUrl() {
        return StartExternalDownloadCommand.of(
            DEFAULT_IDEMPOTENCY_KEY,
            TenantId.of(DEFAULT_TENANT_ID),
            "http://example.com/files/document.pdf",
            FileName.of(DEFAULT_FILE_NAME)
        );
    }

    /**
     * 다른 파일명으로 Command 생성
     *
     * @param fileName 파일명
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand createWithFileName(String fileName) {
        return StartExternalDownloadCommand.of(
            DEFAULT_IDEMPOTENCY_KEY,
            TenantId.of(DEFAULT_TENANT_ID),
            DEFAULT_SOURCE_URL,
            FileName.of(fileName)
        );
    }

    /**
     * 다른 테넌트로 Command 생성
     *
     * @param tenantId 테넌트 ID
     * @return StartExternalDownloadCommand
     */
    public static StartExternalDownloadCommand createWithTenant(Long tenantId) {
        return StartExternalDownloadCommand.of(
            DEFAULT_IDEMPOTENCY_KEY,
            TenantId.of(tenantId),
            DEFAULT_SOURCE_URL,
            FileName.of(DEFAULT_FILE_NAME)
        );
    }
}

