package com.ryuqq.fileflow.application.download.dto;

/**
 * SQS 메시지 DTO.
 *
 * @param externalDownloadId 외부 다운로드 ID (UUIDv7 문자열)
 * @param sourceUrl 소스 URL
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 */
public record ExternalDownloadMessage(
        String externalDownloadId, String sourceUrl, String tenantId, String organizationId) {}
