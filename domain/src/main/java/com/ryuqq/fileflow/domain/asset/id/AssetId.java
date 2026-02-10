package com.ryuqq.fileflow.domain.asset.id;

/** Asset 식별자. String 타입 - Application Factory에서 UUID v7으로 생성하여 주입. */
public record AssetId(String value) {

    public static AssetId of(String value) {
        return new AssetId(value);
    }
}
