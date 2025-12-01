package com.ryuqq.fileflow.application.download.dto.response;

import java.time.Instant;

/**
 * 외부 다운로드 응답 DTO.
 *
 * @param id 다운로드 ID (UUID 문자열)
 * @param status 현재 상태
 * @param createdAt 생성 시간
 */
public record ExternalDownloadResponse(String id, String status, Instant createdAt) {}
