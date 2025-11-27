package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetErrorCode 단위 테스트")
class FileAssetErrorCodeTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 에러 코드가 정의되어 있다")
        void values_ShouldContainAllErrorCodes() {
            // when
            FileAssetErrorCode[] values = FileAssetErrorCode.values();

            // then
            assertThat(values).hasSize(1);
            assertThat(values).containsExactly(FileAssetErrorCode.FILE_ASSET_NOT_FOUND);
        }

        @Test
        @DisplayName("ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface() {
            // given
            FileAssetErrorCode errorCode = FileAssetErrorCode.FILE_ASSET_NOT_FOUND;

            // then
            assertThat(errorCode).isInstanceOf(ErrorCode.class);
        }
    }

    @Nested
    @DisplayName("에러 코드 속성 테스트")
    class ErrorCodePropertiesTest {

        @Test
        @DisplayName("FILE_ASSET_NOT_FOUND 에러 코드 속성이 올바르다")
        void fileAssetNotFound_ShouldHaveCorrectProperties() {
            // given
            FileAssetErrorCode errorCode = FileAssetErrorCode.FILE_ASSET_NOT_FOUND;

            // then
            assertThat(errorCode.getCode()).isEqualTo("ASSET_001");
            assertThat(errorCode.getMessage()).isEqualTo("FileAsset을 찾을 수 없습니다.");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("HTTP 상태 코드 테스트")
    class HttpStatusTest {

        @Test
        @DisplayName("FILE_ASSET_NOT_FOUND는 404 상태 코드를 가진다")
        void fileAssetNotFound_ShouldHave404Status() {
            assertThat(FileAssetErrorCode.FILE_ASSET_NOT_FOUND.getHttpStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("valueOf 테스트")
    class ValueOfTest {

        @Test
        @DisplayName("문자열로 에러 코드를 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnErrorCode() {
            // when & then
            assertThat(FileAssetErrorCode.valueOf("FILE_ASSET_NOT_FOUND"))
                    .isEqualTo(FileAssetErrorCode.FILE_ASSET_NOT_FOUND);
        }
    }
}
