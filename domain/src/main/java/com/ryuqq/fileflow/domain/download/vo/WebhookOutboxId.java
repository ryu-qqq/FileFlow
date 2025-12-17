package com.ryuqq.fileflow.domain.download.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.Objects;
import java.util.UUID;

/**
 * Webhook Outbox ID Value Object.
 *
 * <p>UUID v7 (Time-Ordered) 사용으로 시간 기반 정렬 및 DB 인덱스 효율성 제공.
 *
 * <p><strong>생성 규칙</strong>:
 *
 * <ul>
 *   <li>{@link #forNew()}: 신규 생성 시 (UUID v7 자동 생성)
 *   <li>{@link #of(UUID)}: 조회/재구성 시
 * </ul>
 */
public record WebhookOutboxId(UUID value) {

    public WebhookOutboxId {
        Objects.requireNonNull(value, "WebhookOutboxId value must not be null");
    }

    /**
     * 신규 WebhookOutboxId 생성 (UUID v7 - Time-Ordered).
     *
     * @return 신규 WebhookOutboxId
     */
    public static WebhookOutboxId forNew() {
        return new WebhookOutboxId(UuidCreator.getTimeOrderedEpoch());
    }

    /**
     * UUID 기반 생성 (조회/재구성용).
     *
     * @param value UUID 값
     * @return WebhookOutboxId
     */
    public static WebhookOutboxId of(UUID value) {
        return new WebhookOutboxId(value);
    }

    /**
     * String 기반 생성 (조회/재구성용).
     *
     * @param value UUID 문자열
     * @return WebhookOutboxId
     */
    public static WebhookOutboxId of(String value) {
        return new WebhookOutboxId(UUID.fromString(value));
    }

    /**
     * UUID 문자열 반환.
     *
     * @return UUID 문자열
     */
    public String getValue() {
        return value.toString();
    }
}
