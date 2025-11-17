package com.ryuqq.fileflow.application.port.out.external;

/**
 * Webhook Client Port (Outbound Port - External API)
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드
 * - Timeout, Retry 정책 명시 필수
 * </p>
 * <p>
 * Application Layer에서 외부 Webhook URL로 이벤트를 전송하기 위한 Port입니다.
 * </p>
 */
public interface WebhookClientPort {

    /**
     * Webhook 이벤트 전송
     * <p>
     * Timeout: 10초
     * Retry: 3회 (Exponential Backoff: 1초, 2초, 4초)
     * </p>
     *
     * @param webhookUrl Webhook URL (고객사가 등록한 콜백 URL)
     * @param payload 이벤트 페이로드 (JSON 형식)
     */
    void send(String webhookUrl, String payload);
}
