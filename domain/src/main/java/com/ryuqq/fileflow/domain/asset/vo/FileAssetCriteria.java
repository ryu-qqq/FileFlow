package com.ryuqq.fileflow.domain.asset.vo;

import java.time.Instant;

/**
 * FileAsset 검색 조건 Value Object.
 *
 * <p>FileAsset 목록 조회를 위한 검색 조건을 담은 Domain VO입니다. Application Layer에서 Domain Layer로 검색 조건을 전달할 때
 * 사용됩니다.
 *
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param fileName 파일명 검색 (부분 매칭, nullable)
 * @param createdAtFrom 생성일 시작 (nullable)
 * @param createdAtTo 생성일 종료 (nullable)
 * @param sortBy 정렬 기준 필드 (CREATED_AT, FILE_NAME, FILE_SIZE, PROCESSED_AT)
 * @param sortDirection 정렬 방향 (ASC, DESC)
 * @param offset 시작 위치
 * @param limit 조회 개수
 */
public record FileAssetCriteria(
        String organizationId,
        String tenantId,
        FileAssetStatus status,
        FileCategory category,
        String fileName,
        Instant createdAtFrom,
        Instant createdAtTo,
        String sortBy,
        String sortDirection,
        long offset,
        int limit) {

    private static final String DEFAULT_SORT_BY = "CREATED_AT";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    public static FileAssetCriteria of(
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            FileCategory category,
            long offset,
            int limit) {
        return new FileAssetCriteria(
                organizationId,
                tenantId,
                status,
                category,
                null,
                null,
                null,
                DEFAULT_SORT_BY,
                DEFAULT_SORT_DIRECTION,
                offset,
                limit);
    }

    public static FileAssetCriteria of(
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            FileCategory category,
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo,
            long offset,
            int limit) {
        return new FileAssetCriteria(
                organizationId,
                tenantId,
                status,
                category,
                fileName,
                createdAtFrom,
                createdAtTo,
                DEFAULT_SORT_BY,
                DEFAULT_SORT_DIRECTION,
                offset,
                limit);
    }

    public static FileAssetCriteria of(
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            FileCategory category,
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo,
            String sortBy,
            String sortDirection,
            long offset,
            int limit) {
        return new FileAssetCriteria(
                organizationId,
                tenantId,
                status,
                category,
                fileName,
                createdAtFrom,
                createdAtTo,
                sortBy != null ? sortBy : DEFAULT_SORT_BY,
                sortDirection != null ? sortDirection : DEFAULT_SORT_DIRECTION,
                offset,
                limit);
    }

    /** 오름차순 정렬인지 확인. */
    public boolean isAscending() {
        return "ASC".equalsIgnoreCase(sortDirection);
    }
}
