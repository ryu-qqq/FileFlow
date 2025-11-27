package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("AssetErrorCode 단위 테스트")
class AssetErrorCodeTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 에러 코드가 정의되어 있다")
        void values_ShouldContainAllErrorCodes() {
            // when
            AssetErrorCode[] values = AssetErrorCode.values();

            // then
            assertThat(values).hasSize(3);
            assertThat(values)
                    .containsExactly(
                            AssetErrorCode.ASSET_NOT_FOUND,
                            AssetErrorCode.INVALID_ASSET_STATUS,
                            AssetErrorCode.PROCESSING_FAILED);
        }

        @Test
        @DisplayName("ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface() {
            // given
            AssetErrorCode errorCode = AssetErrorCode.ASSET_NOT_FOUND;

            // then
            assertThat(errorCode).isInstanceOf(ErrorCode.class);
        }
    }

    @Nested
    @DisplayName("에러 코드 속성 테스트")
    class ErrorCodePropertiesTest {

        @ParameterizedTest
        @CsvSource({
            "ASSET_NOT_FOUND, ASSET-NOT-FOUND, 파일 에셋을 찾을 수 없습니다., 404",
            "INVALID_ASSET_STATUS, INVALID-ASSET-STATUS, 유효하지 않은 에셋 상태입니다., 409",
            "PROCESSING_FAILED, PROCESSING-FAILED, 파일 가공에 실패했습니다., 500"
        })
        @DisplayName("에러 코드별 속성이 올바르게 반환된다")
        void errorCode_ShouldHaveCorrectProperties(
                AssetErrorCode errorCode,
                String expectedCode,
                String expectedMessage,
                int expectedHttpStatus) {
            // when & then
            assertThat(errorCode.getCode()).isEqualTo(expectedCode);
            assertThat(errorCode.getMessage()).isEqualTo(expectedMessage);
            assertThat(errorCode.getHttpStatus()).isEqualTo(expectedHttpStatus);
        }
    }

    @Nested
    @DisplayName("HTTP 상태 코드 테스트")
    class HttpStatusTest {

        @Test
        @DisplayName("ASSET_NOT_FOUND는 404 상태 코드를 가진다")
        void assetNotFound_ShouldHave404Status() {
            assertThat(AssetErrorCode.ASSET_NOT_FOUND.getHttpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("INVALID_ASSET_STATUS는 409 상태 코드를 가진다")
        void invalidAssetStatus_ShouldHave409Status() {
            assertThat(AssetErrorCode.INVALID_ASSET_STATUS.getHttpStatus()).isEqualTo(409);
        }

        @Test
        @DisplayName("PROCESSING_FAILED는 500 상태 코드를 가진다")
        void processingFailed_ShouldHave500Status() {
            assertThat(AssetErrorCode.PROCESSING_FAILED.getHttpStatus()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("valueOf 테스트")
    class ValueOfTest {

        @Test
        @DisplayName("문자열로 에러 코드를 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnErrorCode() {
            // when & then
            assertThat(AssetErrorCode.valueOf("ASSET_NOT_FOUND"))
                    .isEqualTo(AssetErrorCode.ASSET_NOT_FOUND);
            assertThat(AssetErrorCode.valueOf("INVALID_ASSET_STATUS"))
                    .isEqualTo(AssetErrorCode.INVALID_ASSET_STATUS);
            assertThat(AssetErrorCode.valueOf("PROCESSING_FAILED"))
                    .isEqualTo(AssetErrorCode.PROCESSING_FAILED);
        }
    }
}
