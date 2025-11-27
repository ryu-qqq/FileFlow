package com.ryuqq.fileflow.application.download.dto.query;

/**
 * 외부 다운로드 조회 Query DTO.
 *
 * @param id ExternalDownload ID
 * @param tenantId 테넌트 ID (권한 체크)
 */
public record GetExternalDownloadQuery(Long id, long tenantId) {}
