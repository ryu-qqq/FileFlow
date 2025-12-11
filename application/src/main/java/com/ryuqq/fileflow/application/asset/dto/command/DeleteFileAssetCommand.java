package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * FileAsset 삭제 Command.
 *
 * <p>Soft Delete를 수행하기 위한 명령 DTO입니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param reason 삭제 사유 (Optional)
 */
public record DeleteFileAssetCommand(
        String fileAssetId, String tenantId, String organizationId, String reason) {

    public static DeleteFileAssetCommand of(
            String fileAssetId, String tenantId, String organizationId, String reason) {
        return new DeleteFileAssetCommand(fileAssetId, tenantId, organizationId, reason);
    }
}
