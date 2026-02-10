package com.ryuqq.fileflow.domain.asset.id;

import java.util.Objects;

/**
 * AssetMetadata 식별자.
 *
 * @param value UUID v7 문자열 (Application Layer에서 생성)
 */
public record AssetMetadataId(String value) {

    public AssetMetadataId {
        Objects.requireNonNull(value, "AssetMetadataId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AssetMetadataId must not be blank");
        }
    }

    public static AssetMetadataId of(String value) {
        return new AssetMetadataId(value);
    }
}
