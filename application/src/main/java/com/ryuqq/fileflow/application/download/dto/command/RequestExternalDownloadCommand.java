package com.ryuqq.fileflow.application.download.dto.command;

/**
 * 외부 다운로드 요청 Command DTO.
 *
 * @param sourceUrl 외부 이미지 URL
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param webhookUrl 콜백 URL (nullable)
 */
public record RequestExternalDownloadCommand(
        String sourceUrl, long tenantId, long organizationId, String webhookUrl) {}
