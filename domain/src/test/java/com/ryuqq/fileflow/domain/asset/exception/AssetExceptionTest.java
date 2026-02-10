package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AssetException 예외")
class AssetExceptionTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("ErrorCode만으로 생성할 수 있다")
        void shouldCreateWithErrorCodeOnly() {
            // when
            AssetException ex = new AssetException(AssetErrorCode.ASSET_NOT_FOUND);

            // then
            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex.code()).isEqualTo("ASSET-001");
            assertThat(ex.httpStatus()).isEqualTo(404);
            assertThat(ex.getMessage()).isEqualTo("파일을 찾을 수 없습니다");
            assertThat(ex.args()).isEmpty();
        }

        @Test
        @DisplayName("ErrorCode + detail 메시지로 생성할 수 있다")
        void shouldCreateWithErrorCodeAndDetail() {
            // when
            AssetException ex =
                    new AssetException(
                            AssetErrorCode.ASSET_NOT_FOUND, "Asset not found: asset-123");

            // then
            assertThat(ex.code()).isEqualTo("ASSET-001");
            assertThat(ex.getMessage()).isEqualTo("Asset not found: asset-123");
            assertThat(ex.args()).isEmpty();
        }

        @Test
        @DisplayName("ErrorCode + detail + args로 생성할 수 있다")
        void shouldCreateWithErrorCodeDetailAndArgs() {
            // given
            Map<String, Object> args = Map.of("assetId", "asset-123");

            // when
            AssetException ex =
                    new AssetException(
                            AssetErrorCode.ASSET_ALREADY_DELETED,
                            "Already deleted: asset-123",
                            args);

            // then
            assertThat(ex.code()).isEqualTo("ASSET-002");
            assertThat(ex.httpStatus()).isEqualTo(409);
            assertThat(ex.getMessage()).isEqualTo("Already deleted: asset-123");
            assertThat(ex.args()).containsEntry("assetId", "asset-123");
        }
    }

    @Nested
    @DisplayName("ErrorCode 매핑")
    class ErrorCodeMapping {

        @Test
        @DisplayName("getErrorCode()로 원본 ErrorCode를 가져올 수 있다")
        void shouldReturnOriginalErrorCode() {
            // when
            AssetException ex = new AssetException(AssetErrorCode.ASSET_ALREADY_DELETED);

            // then
            assertThat(ex.getErrorCode()).isEqualTo(AssetErrorCode.ASSET_ALREADY_DELETED);
        }
    }
}
