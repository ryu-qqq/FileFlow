package com.ryuqq.fileflow.application.download.dto.command;

/**
 * 외부 다운로드 요청 Command DTO.
 *
 * @param idempotencyKey 멱등성 키 (UUID 문자열)
 * @param sourceUrl 외부 이미지 URL
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param webhookUrl 콜백 URL (nullable)
 */
public record RequestExternalDownloadCommand(
        String idempotencyKey,
        String sourceUrl,
        String tenantId,
        String organizationId,
        String webhookUrl) {}
