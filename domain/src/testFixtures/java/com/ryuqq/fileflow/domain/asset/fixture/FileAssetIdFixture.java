package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;

/**
 * FileAssetId Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileAssetIdFixture {

    private FileAssetIdFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 FileAssetId Fixture (신규 생성) */
    public static FileAssetId defaultFileAssetId() {
        return FileAssetId.forNew();
    }

    /** 고정된 FileAssetId Fixture (테스트 검증용) */
    public static FileAssetId fixedFileAssetId() {
        return FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
    }

    /** Custom FileAssetId Fixture (UUID 문자열) */
    public static FileAssetId customFileAssetId(String uuidString) {
        return FileAssetId.of(uuidString);
    }

    /** Custom FileAssetId Fixture (UUID) */
    public static FileAssetId customFileAssetId(java.util.UUID uuid) {
        return FileAssetId.of(uuid);
    }
}
