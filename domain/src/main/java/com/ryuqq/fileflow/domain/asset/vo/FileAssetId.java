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

    public static FileAssetId generate() {
        return new FileAssetId(UUID.randomUUID());
    }

    public static FileAssetId of(UUID value) {
        return new FileAssetId(value);
    }

    public static FileAssetId of(String value) {
        return new FileAssetId(UUID.fromString(value));
    }

    public String getValue() {
        return value.toString();
    }
}
