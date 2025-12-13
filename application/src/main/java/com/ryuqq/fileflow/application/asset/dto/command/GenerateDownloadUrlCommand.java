package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * Presigned Download URL 생성 Command.
 *
 * <p>단건 파일에 대한 Presigned URL 생성 요청 정보를 담습니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param expirationMinutes URL 유효 기간 (분 단위)
 */
public record GenerateDownloadUrlCommand(
        String fileAssetId, String tenantId, String organizationId, int expirationMinutes) {

    /**
     * 값 기반 생성.
     *
     * @param fileAssetId 파일 자산 ID
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param expirationMinutes URL 유효 기간 (분 단위)
     * @return GenerateDownloadUrlCommand
     */
    public static GenerateDownloadUrlCommand of(
            String fileAssetId, String tenantId, String organizationId, int expirationMinutes) {
        return new GenerateDownloadUrlCommand(
                fileAssetId, tenantId, organizationId, expirationMinutes);
    }
}
