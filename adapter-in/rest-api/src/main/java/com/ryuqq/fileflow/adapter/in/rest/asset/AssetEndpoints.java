package com.ryuqq.fileflow.adapter.in.rest.asset;

/**
 * AssetEndpoints - Asset 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class AssetEndpoints {

    private AssetEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/assets";

    // ========== Query Endpoints ==========

    /** Asset 상세 조회 */
    public static final String DETAIL = "/{assetId}";

    /** Asset 메타데이터 조회 */
    public static final String METADATA = "/{assetId}/metadata";

    // ========== Command Endpoints ==========

    /** Asset 삭제 */
    public static final String DELETE = "/{assetId}";
}
