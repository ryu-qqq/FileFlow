package com.ryuqq.fileflow.domain.asset.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public record ProcessedFileAssetId(UUID value) {

    public ProcessedFileAssetId {
        if (value == null) {
            throw new IllegalArgumentException("ProcessedFileAssetId는 null일 수 없습니다.");
        }
    }

    public static ProcessedFileAssetId forNew() {
        return new ProcessedFileAssetId(UuidCreator.getTimeOrderedEpoch());
    }

    public static ProcessedFileAssetId of(UUID value) {
        return new ProcessedFileAssetId(value);
    }

    public static ProcessedFileAssetId of(String value) {
        return new ProcessedFileAssetId(UUID.fromString(value));
    }

    public String getValue() {
        return value.toString();
    }
}
