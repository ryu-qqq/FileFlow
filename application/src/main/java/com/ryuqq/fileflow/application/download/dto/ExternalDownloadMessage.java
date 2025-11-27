package com.ryuqq.fileflow.application.download.dto;

/**
 * SQS 메시지 DTO.
 *
 * @param externalDownloadId ExternalDownload ID
 * @param sourceUrl 외부 이미지 URL
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 */
public record ExternalDownloadMessage(
        Long externalDownloadId, String sourceUrl, Long tenantId, Long organizationId) {}
