package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * FileAsset 삭제 Command.
 *
 * <p>Soft Delete를 수행하기 위한 명령 DTO입니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param reason 삭제 사유 (Optional)
 */
public record DeleteFileAssetCommand(
        String fileAssetId, Long tenantId, Long organizationId, String reason) {

    public static DeleteFileAssetCommand of(
            String fileAssetId, Long tenantId, Long organizationId, String reason) {
        return new DeleteFileAssetCommand(fileAssetId, tenantId, organizationId, reason);
    }
}
