package com.ryuqq.fileflow.application.session.dto.query;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;

/**
 * UploadSession 목록 조회 Query.
 *
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param status 상태 필터 (nullable)
 * @param uploadType 업로드 타입 필터 (nullable: SINGLE/MULTIPART)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record ListUploadSessionsQuery(
        Long tenantId,
        Long organizationId,
        SessionStatus status,
        String uploadType,
        int page,
        int size) {

    public static ListUploadSessionsQuery of(
            Long tenantId,
            Long organizationId,
            SessionStatus status,
            String uploadType,
            int page,
            int size) {
        return new ListUploadSessionsQuery(
                tenantId, organizationId, status, uploadType, page, size);
    }

    public long offset() {
        return (long) page * size;
    }
}
