package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 외부 다운로드 요청 API Response.
 *
 * @param id ExternalDownload ID
 * @param status 현재 상태
 * @param createdAt 생성 시간
 */
@Schema(description = "외부 다운로드 요청 응답")
public record ExternalDownloadApiResponse(
        @Schema(description = "외부 다운로드 ID", example = "download-123") String id,
        @Schema(description = "현재 상태", example = "PENDING") String status,
        @Schema(description = "생성 시간") Instant createdAt) {}
