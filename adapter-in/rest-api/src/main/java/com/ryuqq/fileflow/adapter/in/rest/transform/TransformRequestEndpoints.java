package com.ryuqq.fileflow.adapter.in.rest.transform;

/**
 * TransformRequestEndpoints - 이미지 변환 요청 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class TransformRequestEndpoints {

    private TransformRequestEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/transform-requests";

    // ========== Query Endpoints ==========

    /** 변환 요청 상세 조회 */
    public static final String DETAIL = "/{transformRequestId}";

    // ========== Command Endpoints ==========

    /** 변환 요청 생성 */
    public static final String CREATE = "";
}
