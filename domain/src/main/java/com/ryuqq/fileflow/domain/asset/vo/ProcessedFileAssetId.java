package com.ryuqq.fileflow.domain.asset.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

/**
 * ProcessedFileAsset 식별자 VO.
 *
 * <p>UUID v7 (Time-Ordered) 사용으로 시간 기반 정렬 및 DB 인덱스 효율성 제공.
 *
 * @param value UUID 값
 */
public record ProcessedFileAssetId(UUID value) {

    /** Compact Constructor (검증 로직). */
    public ProcessedFileAssetId {
        if (value == null) {
            throw new IllegalArgumentException("ProcessedFileAssetId는 null일 수 없습니다.");
        }
    }

    /**
     * 신규 ProcessedFileAsset ID 생성 (UUID v7 - Time-Ordered).
     *
     * @return 신규 ProcessedFileAssetId
     */
    public static ProcessedFileAssetId forNew() {
        return new ProcessedFileAssetId(UuidCreator.getTimeOrderedEpoch());
    }

    /**
     * UUID로 ID를 생성한다.
     *
     * @param value UUID 값
     * @return ProcessedFileAssetId
     */
    public static ProcessedFileAssetId of(UUID value) {
        return new ProcessedFileAssetId(value);
    }

    /**
     * 문자열 UUID로 ID를 생성한다.
     *
     * @param value UUID 문자열
     * @return ProcessedFileAssetId
     */
    public static ProcessedFileAssetId of(String value) {
        return new ProcessedFileAssetId(UUID.fromString(value));
    }

    /**
     * ID가 신규인지 확인 (항상 false, 생성 시 값이 필수).
     *
     * @return 항상 false (null 허용하지 않음)
     */
    public boolean isNew() {
        return false; // Record 생성 시 null 검증으로 항상 값이 존재
    }

    /**
     * UUID 문자열을 반환한다.
     *
     * @return UUID 문자열
     */
    public String getValue() {
        return value.toString();
    }
}
