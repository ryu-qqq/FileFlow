package com.ryuqq.fileflow.application.asset.dto.command;

import java.util.List;

/**
 * FileAsset 일괄 삭제 Command.
 *
 * <p>여러 파일 자산을 일괄 Soft Delete하기 위한 Command DTO입니다.
 *
 * @param fileAssetIds 삭제할 파일 자산 ID 목록
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param reason 삭제 사유 (선택적)
 */
public record BatchDeleteFileAssetCommand(
        List<String> fileAssetIds, String tenantId, String organizationId, String reason) {

    /**
     * 값 기반 생성.
     *
     * @param fileAssetIds 삭제할 파일 자산 ID 목록
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param reason 삭제 사유 (선택적)
     * @return BatchDeleteFileAssetCommand
     */
    public static BatchDeleteFileAssetCommand of(
            List<String> fileAssetIds, String tenantId, String organizationId, String reason) {
        return new BatchDeleteFileAssetCommand(fileAssetIds, tenantId, organizationId, reason);
    }
}
