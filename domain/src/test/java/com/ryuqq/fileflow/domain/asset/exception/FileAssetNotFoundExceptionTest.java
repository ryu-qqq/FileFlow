package com.ryuqq.fileflow.domain.asset.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetNotFoundException 단위 테스트")
class FileAssetNotFoundExceptionTest {

    private static final String ERROR_CODE = "ASSET_001";

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("fileAssetId로 예외를 생성할 수 있다")
        void constructor_WithFileAssetId_ShouldCreateException() {
            // given
            String fileAssetId = "file-asset-12345";

            // when
            FileAssetNotFoundException exception = new FileAssetNotFoundException(fileAssetId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage()).contains("FileAsset not found: " + fileAssetId);
        }

        @Test
        @DisplayName("UUID 형태의 fileAssetId로 예외를 생성할 수 있다")
        void constructor_WithUuidFileAssetId_ShouldCreateException() {
            // given
            String fileAssetId = "550e8400-e29b-41d4-a716-446655440000";

            // when
            FileAssetNotFoundException exception = new FileAssetNotFoundException(fileAssetId);

            // then
            assertThat(exception.code()).isEqualTo(ERROR_CODE);
            assertThat(exception.getMessage()).contains(fileAssetId);
        }
    }

    @Nested
    @DisplayName("상속 테스트")
    class InheritanceTest {

        @Test
        @DisplayName("DomainException을 상속한다")
        void shouldExtendDomainException() {
            // given
            FileAssetNotFoundException exception =
                    new FileAssetNotFoundException("test-file-asset");

            // then
            assertThat(exception).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void shouldExtendRuntimeException() {
            // given
            FileAssetNotFoundException exception =
                    new FileAssetNotFoundException("test-file-asset");

            // then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("에러 코드 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("FILE_ASSET_NOT_FOUND 에러 코드를 사용한다")
        void shouldUseFileAssetNotFoundErrorCode() {
            // given
            FileAssetNotFoundException exception =
                    new FileAssetNotFoundException("test-file-asset");

            // then
            assertThat(exception.code())
                    .isEqualTo(FileAssetErrorCode.FILE_ASSET_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("메시지 포맷 테스트")
    class MessageFormatTest {

        @Test
        @DisplayName("메시지에 FileAsset not found 접두사가 포함된다")
        void message_ShouldContainFileAssetNotFoundPrefix() {
            // given
            String fileAssetId = "test-123";
            FileAssetNotFoundException exception = new FileAssetNotFoundException(fileAssetId);

            // then
            assertThat(exception.getMessage()).startsWith("FileAsset not found:");
        }
    }
}
