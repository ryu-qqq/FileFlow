package com.ryuqq.fileflow.application.download.dto.response;

/**
 * External Download Response
 * 외부 다운로드 응답 DTO
 *
 * @param idempotencyKey 멱등키
 * @param downloadId External Download ID
 * @param uploadSessionId Upload Session ID
 * @param sourceUrl 소스 URL
 * @param status 다운로드 상태
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ExternalDownloadResponse(
    String idempotencyKey,
    Long downloadId,
    Long uploadSessionId,
    String sourceUrl,
    String status
) {

    /**
     * Static Factory Method (멱등키 포함)
     *
     * @param idempotencyKey 멱등키
     * @param downloadId External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param status 다운로드 상태
     * @return ExternalDownloadResponse
     */
    public static ExternalDownloadResponse of(
        String idempotencyKey,
        Long downloadId,
        Long uploadSessionId,
        String sourceUrl,
        String status
    ) {
        return new ExternalDownloadResponse(
            idempotencyKey,
            downloadId,
            uploadSessionId,
            sourceUrl,
            status
        );
    }

    /**
     * Static Factory Method (기존 호환성 유지 - 멱등키 없음)
     *
     * @deprecated 멱등키를 포함한 새로운 of 메서드 사용 권장
     * @param downloadId External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param status 다운로드 상태
     * @return ExternalDownloadResponse (멱등키는 null)
     */
    @Deprecated(since = "1.1.0", forRemoval = false)
    public static ExternalDownloadResponse ofLegacy(
        Long downloadId,
        Long uploadSessionId,
        String sourceUrl,
        String status
    ) {
        return new ExternalDownloadResponse(
            null,  // 멱등키 없음
            downloadId,
            uploadSessionId,
            sourceUrl,
            status
        );
    }
}
