package com.ryuqq.fileflow.application.download.dto.command;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;

/**
 * Start External Download Command
 * 외부 URL 다운로드 시작 Command
 *
 * @param idempotencyKey 멱등키 (중복 요청 방지)
 * @param tenantId 테넌트 ID
 * @param sourceUrl 소스 URL
 * @param fileName 저장할 파일명
 * @param fileSize 파일 크기 (초기 0, 다운로드 중 업데이트)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record StartExternalDownloadCommand(
    String idempotencyKey,
    TenantId tenantId,
    String sourceUrl,
    FileName fileName,
    FileSize fileSize
) {

    /**
     * Compact Constructor - 멱등키 검증
     */
    public StartExternalDownloadCommand {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("테넌트 ID는 필수입니다");
        }
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new IllegalArgumentException("소스 URL은 필수입니다");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("파일명은 필수입니다");
        }
    }

    /**
     * Static Factory Method
     *
     * @param idempotencyKey 멱등키
     * @param tenantId 테넌트 ID
     * @param sourceUrl 소스 URL
     * @param fileName 저장할 파일명
     * @return StartExternalDownloadCommand (fileSize는 0으로 초기화)
     */
    public static StartExternalDownloadCommand of(
        String idempotencyKey,
        TenantId tenantId,
        String sourceUrl,
        FileName fileName
    ) {
        return new StartExternalDownloadCommand(
            idempotencyKey,
            tenantId,
            sourceUrl,
            fileName,
            FileSize.of(0L)  // 초기값은 0 (다운로드 중 업데이트)
        );
    }
}
