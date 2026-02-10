package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AssetErrorCode 열거형")
class AssetErrorCodeTest {

    @Test
    @DisplayName("ASSET_NOT_FOUND는 ASSET-001, 404이다")
    void shouldHaveAssetNotFoundProperties() {
        // given
        AssetErrorCode code = AssetErrorCode.ASSET_NOT_FOUND;

        // then
        assertThat(code.getCode()).isEqualTo("ASSET-001");
        assertThat(code.getHttpStatus()).isEqualTo(404);
        assertThat(code.getMessage()).isEqualTo("파일을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("ASSET_ALREADY_DELETED는 ASSET-002, 409이다")
    void shouldHaveAssetAlreadyDeletedProperties() {
        // given
        AssetErrorCode code = AssetErrorCode.ASSET_ALREADY_DELETED;

        // then
        assertThat(code.getCode()).isEqualTo("ASSET-002");
        assertThat(code.getHttpStatus()).isEqualTo(409);
        assertThat(code.getMessage()).isEqualTo("이미 삭제된 파일입니다");
    }

    @Test
    @DisplayName("ErrorCode 인터페이스를 구현한다")
    void shouldImplementErrorCode() {
        for (AssetErrorCode code : AssetErrorCode.values()) {
            assertThat(code).isInstanceOf(ErrorCode.class);
        }
    }

    @Test
    @DisplayName("ASSET_METADATA_NOT_FOUND는 ASSET-003, 404이다")
    void shouldHaveAssetMetadataNotFoundProperties() {
        // given
        AssetErrorCode code = AssetErrorCode.ASSET_METADATA_NOT_FOUND;

        // then
        assertThat(code.getCode()).isEqualTo("ASSET-003");
        assertThat(code.getHttpStatus()).isEqualTo(404);
        assertThat(code.getMessage()).isEqualTo("파일 메타데이터를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("ASSET_ACCESS_DENIED는 ASSET-004, 403이다")
    void shouldHaveAssetAccessDeniedProperties() {
        // given
        AssetErrorCode code = AssetErrorCode.ASSET_ACCESS_DENIED;

        // then
        assertThat(code.getCode()).isEqualTo("ASSET-004");
        assertThat(code.getHttpStatus()).isEqualTo(403);
        assertThat(code.getMessage()).isEqualTo("해당 파일에 대한 권한이 없습니다");
    }

    @Test
    @DisplayName("총 4개의 enum 값이 존재한다")
    void shouldHaveFourValues() {
        assertThat(AssetErrorCode.values()).hasSize(4);
    }
}
