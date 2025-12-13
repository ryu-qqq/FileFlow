package com.ryuqq.fileflow.application.session.dto.query;

/**
 * UploadSession 단건 조회 Query.
 *
 * @param sessionId 세션 ID
 * @param tenantId 테넌트 ID (UUIDv7 문자열, 스코프 검증용)
 */
public record GetUploadSessionQuery(String sessionId, String tenantId) {

    public static GetUploadSessionQuery of(String sessionId, String tenantId) {
        return new GetUploadSessionQuery(sessionId, tenantId);
    }
}
