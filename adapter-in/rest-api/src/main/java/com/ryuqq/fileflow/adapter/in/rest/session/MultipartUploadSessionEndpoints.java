package com.ryuqq.fileflow.adapter.in.rest.session;

/**
 * MultipartUploadSessionEndpoints - 멀티파트 업로드 세션 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class MultipartUploadSessionEndpoints {

    private MultipartUploadSessionEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/sessions/multipart";

    // ========== Query Endpoints ==========

    /** 멀티파트 업로드 세션 상세 조회 */
    public static final String DETAIL = "/{sessionId}";

    // ========== Command Endpoints ==========

    /** 멀티파트 업로드 세션 생성 */
    public static final String CREATE = "";

    /** 파트별 Presigned URL 발급 */
    public static final String PRESIGNED_PART_URL = "/{sessionId}/parts/{partNumber}/presigned-url";

    /** 파트 업로드 완료 기록 */
    public static final String PARTS = "/{sessionId}/parts";

    /** 멀티파트 업로드 세션 완료 */
    public static final String COMPLETE = "/{sessionId}/complete";

    /** 멀티파트 업로드 세션 중단 */
    public static final String ABORT = "/{sessionId}/abort";
}
