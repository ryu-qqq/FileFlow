package com.ryuqq.fileflow.application.asset.dto.query;

/**
 * FileAsset 목록 조회 Query.
 *
 * <p>REST API Layer와의 결합도를 낮추기 위해 String 기반으로 설계. Domain Enum 변환은 Application Layer 내부에서 처리.
 *
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param status 상태 필터 (nullable) - enum name as String
 * @param category 카테고리 필터 (nullable) - enum name as String
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record ListFileAssetsQuery(
        String organizationId,
        String tenantId,
        String status,
        String category,
        int page,
        int size) {

    public static ListFileAssetsQuery of(
            String organizationId,
            String tenantId,
            String status,
            String category,
            int page,
            int size) {
        return new ListFileAssetsQuery(organizationId, tenantId, status, category, page, size);
    }

    public long offset() {
        return (long) page * size;
    }
}
