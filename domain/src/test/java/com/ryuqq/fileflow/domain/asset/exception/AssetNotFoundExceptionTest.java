package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AssetNotFoundException 단위 테스트")
class AssetNotFoundExceptionTest {

    private static final String ERROR_CODE = "ASSET-NOT-FOUND";

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("assetId로 예외를 생성할 수 있다")
        void constructor_WithAssetId_ShouldCreateException() {
            // given
            String assetId = "asset-12345";

            // when
            AssetNotFoundException exception = new AssetNotFoundException(assetId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage()).contains("Asset ID: " + assetId);
        }

        @Test
        @DisplayName("UUID 형태의 assetId로 예외를 생성할 수 있다")
        void constructor_WithUuidAssetId_ShouldCreateException() {
            // given
            String assetId = "550e8400-e29b-41d4-a716-446655440000";

            // when
            AssetNotFoundException exception = new AssetNotFoundException(assetId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage()).contains(assetId);
        }
    }

    @Nested
    @DisplayName("상속 테스트")
    class InheritanceTest {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // given
            AssetNotFoundException exception = new AssetNotFoundException("test-asset");

            // then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void shouldExtendRuntimeException() {
            // given
            AssetNotFoundException exception = new AssetNotFoundException("test-asset");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("에러 코드 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("ASSET_NOT_FOUND 에러 코드를 사용한다")
        void shouldUseAssetNotFoundErrorCode() {
            // given
            AssetNotFoundException exception = new AssetNotFoundException("test-asset");

            // then
            assertThat(exception.code()).isEqualTo(AssetErrorCode.ASSET_NOT_FOUND.getCode());
        }
    }
}
