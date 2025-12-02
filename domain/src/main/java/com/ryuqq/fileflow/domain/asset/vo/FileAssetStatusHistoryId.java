package com.ryuqq.fileflow.domain.asset.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

/**
 * FileAssetStatusHistory 식별자 VO.
 *
 * <p>UUID v7 (Time-Ordered) 사용으로 시간 기반 정렬 및 DB 인덱스 효율성 제공.
 *
 * @param value UUID 값
 */
public record FileAssetStatusHistoryId(UUID value) {

    public FileAssetStatusHistoryId {
        if (value == null) {
            throw new IllegalArgumentException("FileAssetStatusHistoryId는 null일 수 없습니다.");
        }
    }

    /**
     * 신규 FileAssetStatusHistory ID 생성 (UUID v7 - Time-Ordered).
     *
     * @return 신규 FileAssetStatusHistoryId
     */
    public static FileAssetStatusHistoryId forNew() {
        return new FileAssetStatusHistoryId(UuidCreator.getTimeOrderedEpoch());
    }

    /**
     * UUID로부터 ID 생성.
     *
     * @param value UUID 값
     * @return FileAssetStatusHistoryId
     */
    public static FileAssetStatusHistoryId of(UUID value) {
        return new FileAssetStatusHistoryId(value);
    }

    /**
     * 문자열 UUID로부터 ID 생성.
     *
     * @param value UUID 문자열
     * @return FileAssetStatusHistoryId
     */
    public static FileAssetStatusHistoryId of(String value) {
        return new FileAssetStatusHistoryId(UUID.fromString(value));
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
