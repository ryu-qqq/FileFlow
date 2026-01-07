package com.ryuqq.fileflow.application.common.util;

import java.util.UUID;

/**
 * 분산 추적 유틸리티.
 *
 * <p>OpenTelemetry 표준 형식의 Trace/Span ID 생성을 제공합니다.
 *
 * <p><strong>Span ID 형식</strong>:
 *
 * <ul>
 *   <li>16자리 hex 문자열 (8바이트)
 *   <li>OpenTelemetry 표준 준수
 *   <li>앞쪽 0 패딩 포함
 * </ul>
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>{@code
 * String spanId = TraceUtils.generateSpanId();
 * // 결과: "a1b2c3d4e5f67890"
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class TraceUtils {

    private static final int SPAN_ID_HEX_LENGTH = 16;

    private TraceUtils() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * OpenTelemetry 표준 형식의 Span ID를 생성합니다.
     *
     * <p>16자리 hex 문자열 (8바이트)을 생성합니다.
     *
     * @return 생성된 Span ID (16자리 hex)
     */
    public static String generateSpanId() {
        UUID uuid = UUID.randomUUID();
        // 8바이트(64비트)를 16자리 hex로 변환 (앞쪽 0 패딩 포함)
        return String.format("%016x", uuid.getMostSignificantBits());
    }

    /**
     * 새로운 Trace ID를 생성합니다.
     *
     * <p>UUID 형식의 Trace ID를 생성합니다.
     *
     * @return 생성된 Trace ID (UUID 형식)
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
