package com.ryuqq.fileflow.domain.download.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.Objects;
import java.util.UUID;

/**
 * 외부 다운로드 Outbox ID Value Object.
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
public record ExternalDownloadOutboxId(UUID value) {

    public ExternalDownloadOutboxId {
        Objects.requireNonNull(value, "ExternalDownloadOutboxId value must not be null");
    }

    /**
     * 신규 ExternalDownloadOutboxId 생성 (UUID v7 - Time-Ordered).
     *
     * @return 신규 ExternalDownloadOutboxId
     */
    public static ExternalDownloadOutboxId forNew() {
        return new ExternalDownloadOutboxId(UuidCreator.getTimeOrderedEpoch());
    }

    /**
     * UUID 기반 생성 (조회/재구성용).
     *
     * @param value UUID 값
     * @return ExternalDownloadOutboxId
     */
    public static ExternalDownloadOutboxId of(UUID value) {
        return new ExternalDownloadOutboxId(value);
    }

    /**
     * String 기반 생성 (조회/재구성용).
     *
     * @param value UUID 문자열
     * @return ExternalDownloadOutboxId
     */
    public static ExternalDownloadOutboxId of(String value) {
        return new ExternalDownloadOutboxId(UUID.fromString(value));
    }

    /**
     * 신규 ID 여부 확인 (항상 false, UUID는 생성 시 값 필수).
     *
     * @return 항상 false
     */
    public boolean isNew() {
        return false;
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
