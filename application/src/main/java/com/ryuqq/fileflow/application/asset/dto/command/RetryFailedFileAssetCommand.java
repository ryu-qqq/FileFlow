package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * 실패한 FileAsset 재처리 Command.
 *
 * <p>FAILED 상태인 파일을 재처리하기 위한 명령 DTO입니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 */
public record RetryFailedFileAssetCommand(
        String fileAssetId, String tenantId, String organizationId) {

    public static RetryFailedFileAssetCommand of(
            String fileAssetId, String tenantId, String organizationId) {
        return new RetryFailedFileAssetCommand(fileAssetId, tenantId, organizationId);
    }
}
