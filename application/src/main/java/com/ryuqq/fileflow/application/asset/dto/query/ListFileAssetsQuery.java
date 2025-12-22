package com.ryuqq.fileflow.application.asset.dto.query;

import java.time.Instant;

/**
 * FileAsset 목록 조회 Query.
 *
 * <p>REST API Layer와의 결합도를 낮추기 위해 String 기반으로 설계. Domain Enum 변환은 Application Layer 내부에서 처리.
 *
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 * @param status 상태 필터 (nullable) - enum name as String
 * @param category 카테고리 필터 (nullable) - enum name as String
 * @param fileName 파일명 검색 (부분 매칭, nullable)
 * @param createdAtFrom 생성일 시작 (nullable)
 * @param createdAtTo 생성일 종료 (nullable)
 * @param sortBy 정렬 기준 필드 (CREATED_AT, FILE_NAME, FILE_SIZE, PROCESSED_AT)
 * @param sortDirection 정렬 방향 (ASC, DESC)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record ListFileAssetsQuery(
        String organizationId,
        String tenantId,
        String status,
        String category,
        String fileName,
        Instant createdAtFrom,
        Instant createdAtTo,
        String sortBy,
        String sortDirection,
        int page,
        int size) {

    public static ListFileAssetsQuery of(
            String organizationId,
            String tenantId,
            String status,
            String category,
            int page,
            int size) {
        return new ListFileAssetsQuery(
                organizationId,
                tenantId,
                status,
                category,
                null,
                null,
                null,
                "CREATED_AT",
                "DESC",
                page,
                size);
    }

    public static ListFileAssetsQuery of(
            String organizationId,
            String tenantId,
            String status,
            String category,
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo,
            int page,
            int size) {
        return new ListFileAssetsQuery(
                organizationId,
                tenantId,
                status,
                category,
                fileName,
                createdAtFrom,
                createdAtTo,
                "CREATED_AT",
                "DESC",
                page,
                size);
    }

    public static ListFileAssetsQuery of(
            String organizationId,
            String tenantId,
            String status,
            String category,
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo,
            String sortBy,
            String sortDirection,
            int page,
            int size) {
        return new ListFileAssetsQuery(
                organizationId,
                tenantId,
                status,
                category,
                fileName,
                createdAtFrom,
                createdAtTo,
                sortBy != null ? sortBy : "CREATED_AT",
                sortDirection != null ? sortDirection : "DESC",
                page,
                size);
    }

    public long offset() {
        return (long) page * size;
    }
}
