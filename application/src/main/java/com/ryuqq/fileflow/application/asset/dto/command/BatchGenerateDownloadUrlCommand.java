package com.ryuqq.fileflow.application.asset.dto.command;

import java.util.List;

/**
 * Presigned Download URL 일괄 생성 Command.
 *
 * <p>여러 파일에 대한 Presigned URL 일괄 생성 요청 정보를 담습니다.
 *
 * @param fileAssetIds 파일 자산 ID 목록
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param expirationMinutes URL 유효 기간 (분 단위)
 */
public record BatchGenerateDownloadUrlCommand(
        List<String> fileAssetIds, Long tenantId, Long organizationId, int expirationMinutes) {

    /**
     * 값 기반 생성.
     *
     * @param fileAssetIds 파일 자산 ID 목록
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param expirationMinutes URL 유효 기간 (분 단위)
     * @return BatchGenerateDownloadUrlCommand
     */
    public static BatchGenerateDownloadUrlCommand of(
            List<String> fileAssetIds, Long tenantId, Long organizationId, int expirationMinutes) {
        return new BatchGenerateDownloadUrlCommand(
                fileAssetIds, tenantId, organizationId, expirationMinutes);
    }
}
