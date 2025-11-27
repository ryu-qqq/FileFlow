package com.ryuqq.fileflow.domain.download.vo;

import java.util.Objects;

/**
 * 외부 다운로드 요청 ID Value Object.
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code forNew()} - 신규 생성용 (ID null, DB에서 할당)
 *   <li>{@code of(Long value)} - 조회/재구성용 (ID 필수)
 * </ul>
 *
 * @param value Long 값 (신규 시 null)
 */
public record ExternalDownloadId(Long value) {

    /**
     * 신규 다운로드 요청 ID 생성 (ID 미할당).
     *
     * @return 신규 ExternalDownloadId (value=null)
     */
    public static ExternalDownloadId forNew() {
        return new ExternalDownloadId(null);
    }

    /**
     * 값 기반 생성 (조회/재구성용).
     *
     * @param value Long 값 (null 불가, 1 이상)
     * @return ExternalDownloadId
     * @throws NullPointerException value가 null인 경우
     * @throws IllegalArgumentException value가 1 미만인 경우
     */
    public static ExternalDownloadId of(Long value) {
        Objects.requireNonNull(value, "ExternalDownloadId value must not be null");
        if (value < 1) {
            throw new IllegalArgumentException("ExternalDownloadId는 1 이상이어야 합니다: " + value);
        }
        return new ExternalDownloadId(value);
    }

    /**
     * 신규 여부 확인.
     *
     * @return value가 null이면 true
     */
    public boolean isNew() {
        return value == null;
    }
}
