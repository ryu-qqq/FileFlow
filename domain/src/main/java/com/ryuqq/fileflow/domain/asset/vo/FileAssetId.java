package com.ryuqq.fileflow.domain.asset.vo;

import java.util.UUID;

/**
 * FileAsset 식별자 VO.
 *
 * @param value UUID 값
 */
public record FileAssetId(UUID value) {

    public FileAssetId {
        if (value == null) {
            throw new IllegalArgumentException("FileAssetId는 null일 수 없습니다.");
        }
    }

    /**
     * 신규 FileAsset ID 생성 (UUID 랜덤 생성).
     *
     * @return 신규 FileAssetId
     */
    public static FileAssetId forNew() {
        return new FileAssetId(UUID.randomUUID());
    }

    /**
     * @deprecated use {@link #forNew()} instead
     */
    @Deprecated
    public static FileAssetId generate() {
        return forNew();
    }

    public static FileAssetId of(UUID value) {
        return new FileAssetId(value);
    }

    public static FileAssetId of(String value) {
        return new FileAssetId(UUID.fromString(value));
    }

    /**
     * ID가 신규인지 확인 (항상 false, 생성 시 값이 필수).
     *
     * @return 항상 false (null 허용하지 않음)
     */
    public boolean isNew() {
        return false; // Record 생성 시 null 검증으로 항상 값이 존재
    }

    public String getValue() {
        return value.toString();
    }
}
