package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import java.time.Instant;

/**
 * 외부 다운로드 요청 API Response.
 *
 * @param id ExternalDownload ID
 * @param status 현재 상태
 * @param createdAt 생성 시간
 */
public record ExternalDownloadApiResponse(Long id, String status, Instant createdAt) {}
