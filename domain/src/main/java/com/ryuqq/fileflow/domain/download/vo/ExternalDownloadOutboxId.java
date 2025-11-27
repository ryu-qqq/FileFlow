package com.ryuqq.fileflow.domain.download.vo;

import java.util.Objects;

/**
 * 외부 다운로드 Outbox ID Value Object.
 *
 * <p><strong>생성 규칙</strong>:
 *
 * <ul>
 *   <li>{@link #forNew()}: 신규 생성 시 (ID null)
 *   <li>{@link #of(Long)}: 조회/재구성 시 (ID 필수, 1 이상)
 * </ul>
 */
public record ExternalDownloadOutboxId(Long value) {

    /**
     * 신규 ExternalDownloadOutboxId 생성 (ID null).
     *
     * @return value가 null인 ExternalDownloadOutboxId
     */
    public static ExternalDownloadOutboxId forNew() {
        return new ExternalDownloadOutboxId(null);
    }

    /**
     * 기존 ExternalDownloadOutboxId 재구성.
     *
     * @param value ID 값 (null 불가, 1 이상)
     * @return value가 설정된 ExternalDownloadOutboxId
     * @throws NullPointerException value가 null인 경우
     * @throws IllegalArgumentException value가 1 미만인 경우
     */
    public static ExternalDownloadOutboxId of(Long value) {
        Objects.requireNonNull(value, "ExternalDownloadOutboxId value must not be null");
        if (value < 1) {
            throw new IllegalArgumentException("ExternalDownloadOutboxId는 1 이상이어야 합니다: " + value);
        }
        return new ExternalDownloadOutboxId(value);
    }

    /**
     * 신규 ID 여부 확인.
     *
     * @return value가 null이면 true
     */
    public boolean isNew() {
        return value == null;
    }
}
