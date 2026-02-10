package com.ryuqq.fileflow.adapter.in.rest.session;

/**
 * SingleUploadSessionEndpoints - 단건 업로드 세션 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class SingleUploadSessionEndpoints {

    private SingleUploadSessionEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/sessions/single";

    // ========== Query Endpoints ==========

    /** 단건 업로드 세션 상세 조회 */
    public static final String DETAIL = "/{sessionId}";

    // ========== Command Endpoints ==========

    /** 단건 업로드 세션 생성 */
    public static final String CREATE = "";

    /** 단건 업로드 세션 완료 */
    public static final String COMPLETE = "/{sessionId}/complete";
}
