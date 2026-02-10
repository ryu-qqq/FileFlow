package com.ryuqq.fileflow.adapter.in.rest.download;

/**
 * DownloadTaskEndpoints - 다운로드 작업 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class DownloadTaskEndpoints {

    private DownloadTaskEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/download-tasks";

    // ========== Query Endpoints ==========

    /** 다운로드 작업 상세 조회 */
    public static final String DETAIL = "/{downloadTaskId}";

    // ========== Command Endpoints ==========

    /** 다운로드 작업 생성 */
    public static final String CREATE = "";
}
