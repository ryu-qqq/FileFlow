package com.ryuqq.fileflow.application.asset.dto.query;

/**
 * N8N 워크플로우용 FileAsset 목록 조회 Query.
 *
 * <p>N8N 워크플로우에서 처리할 FileAsset 목록을 조회합니다. RESIZED 상태의 FileAsset을 조회하여 N8N_PROCESSING으로 전환합니다.
 *
 * @param status 조회할 상태 (기본: RESIZED)
 * @param limit 조회 건수 제한
 */
public record ListFileAssetsForN8nQuery(String status, Integer limit) {}
