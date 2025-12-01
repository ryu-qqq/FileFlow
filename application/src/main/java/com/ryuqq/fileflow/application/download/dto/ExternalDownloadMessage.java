package com.ryuqq.fileflow.application.download.dto;

/**
 * SQS 메시지 DTO.
 *
 * @param externalDownloadId 외부 다운로드 ID (UUID 문자열)
 * @param sourceUrl 소스 URL
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 */
public record ExternalDownloadMessage(
        String externalDownloadId, String sourceUrl, Long tenantId, Long organizationId) {}
