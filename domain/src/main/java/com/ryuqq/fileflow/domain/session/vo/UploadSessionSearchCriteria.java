package com.ryuqq.fileflow.domain.session.vo;

/**
 * UploadSession 검색 조건 VO.
 *
 * <p>업로드 세션 목록 조회 시 사용하는 검색 조건입니다.
 *
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param status 상태 필터 (nullable)
 * @param uploadType 업로드 타입 필터 (nullable: SINGLE/MULTIPART)
 * @param offset 오프셋
 * @param limit 조회 개수
 */
public record UploadSessionSearchCriteria(
        Long tenantId,
        Long organizationId,
        SessionStatus status,
        String uploadType,
        long offset,
        int limit) {

    /**
     * 검색 조건 생성.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param status 상태 필터 (nullable)
     * @param uploadType 업로드 타입 필터 (nullable: SINGLE/MULTIPART)
     * @param offset 오프셋
     * @param limit 조회 개수
     * @return UploadSessionSearchCriteria
     */
    public static UploadSessionSearchCriteria of(
            Long tenantId,
            Long organizationId,
            SessionStatus status,
            String uploadType,
            long offset,
            int limit) {
        return new UploadSessionSearchCriteria(
                tenantId, organizationId, status, uploadType, offset, limit);
    }
}
