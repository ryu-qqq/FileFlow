package com.ryuqq.fileflow.domain.download.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.Objects;
import java.util.UUID;

/**
 * 외부 다운로드 요청 ID Value Object.
 *
 * <p>UUID v7 (Time-Ordered) 사용으로 시간 기반 정렬 및 DB 인덱스 효율성 제공.
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code forNew()} - 신규 생성용 (UUID v7 자동 생성)
 *   <li>{@code of(UUID value)} - 조회/재구성용
 * </ul>
 *
 * @param value UUID 값
 */
public record ExternalDownloadId(UUID value) {

    public ExternalDownloadId {
        Objects.requireNonNull(value, "ExternalDownloadId value must not be null");
    }

    /**
     * 신규 다운로드 요청 ID 생성 (UUID v7 - Time-Ordered).
     *
     * @return 신규 ExternalDownloadId
     */
    public static ExternalDownloadId forNew() {
        return new ExternalDownloadId(UuidCreator.getTimeOrderedEpoch());
    }

    /**
     * UUID 기반 생성 (조회/재구성용).
     *
     * @param value UUID 값
     * @return ExternalDownloadId
     */
    public static ExternalDownloadId of(UUID value) {
        return new ExternalDownloadId(value);
    }

    /**
     * String 기반 생성 (조회/재구성용).
     *
     * @param value UUID 문자열
     * @return ExternalDownloadId
     */
    public static ExternalDownloadId of(String value) {
        return new ExternalDownloadId(UUID.fromString(value));
    }

    /**
     * 신규 여부 확인 (항상 false, UUID는 생성 시 값 필수).
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
