package com.ryuqq.fileflow.adapter.in.rest.monitoring;

/**
 * OutboxMonitoringEndpoints - 모니터링 엔드포인트 정의.
 *
 * <p>API-END-001: Endpoints 클래스에서 모든 엔드포인트 경로 정의.
 *
 * <p>API-END-002: BASE 경로와 각 엔드포인트 경로를 상수로 관리.
 */
public final class OutboxMonitoringEndpoints {

    private OutboxMonitoringEndpoints() {}

    /** 기본 경로 */
    public static final String BASE = "/api/v1/monitoring";

    // ========== Query Endpoints ==========

    /** Outbox 상태 조회 */
    public static final String OUTBOX_STATUS = "/outbox-status";
}
