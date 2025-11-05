package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.fixture.FileIdFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileAsset Domain Exception 테스트
 *
 * <p>FileAsset 바운디드 컨텍스트의 Domain Exception 계층을 검증합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileAsset Domain Exception 테스트")
class FileAssetExceptionTest {

    @Nested
    @DisplayName("FileAssetNotFoundException 테스트")
    class FileAssetNotFoundExceptionTests {

        @Test
        @DisplayName("FileId로 예외 생성 성공")
        void constructor_WithFileId_Success() {
            // Given
            FileId fileId = FileIdFixture.create(123L);

            // When
            FileAssetNotFoundException exception = new FileAssetNotFoundException(fileId);

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_NOT_FOUND);
            assertThat(exception.code()).isEqualTo("FILE-001");
            assertThat(exception.getMessage()).contains("FileAsset not found");
            assertThat(exception.args().get("fileId")).isEqualTo(123L);
        }

        @Test
        @DisplayName("Long ID로 예외 생성 성공")
        void constructor_WithLongId_Success() {
            // When
            FileAssetNotFoundException exception = new FileAssetNotFoundException(456L);

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception.code()).isEqualTo("FILE-001");
            assertThat(exception.args().get("fileId")).isEqualTo(456L);
        }
    }

    @Nested
    @DisplayName("FileAssetAlreadyDeletedException 테스트")
    class FileAssetAlreadyDeletedExceptionTests {

        @Test
        @DisplayName("FileId로 예외 생성 성공")
        void constructor_WithFileId_Success() {
            // Given
            FileId fileId = FileIdFixture.create(789L);

            // When
            FileAssetAlreadyDeletedException exception = new FileAssetAlreadyDeletedException(fileId);

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_ALREADY_DELETED);
            assertThat(exception.code()).isEqualTo("FILE-002");
            assertThat(exception.getMessage()).contains("FileAsset already deleted");
            assertThat(exception.args().get("fileId")).isEqualTo(789L);
        }
    }

    @Nested
    @DisplayName("FileAssetAccessDeniedException 테스트")
    class FileAssetAccessDeniedExceptionTests {

        @Test
        @DisplayName("FileId와 requesterId로 예외 생성 성공")
        void constructor_WithFileIdAndRequesterId_Success() {
            // Given
            FileId fileId = FileIdFixture.create(100L);
            Long requesterId = 999L;

            // When
            FileAssetAccessDeniedException exception = new FileAssetAccessDeniedException(fileId, requesterId);

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_ACCESS_DENIED);
            assertThat(exception.code()).isEqualTo("FILE-003");
            assertThat(exception.getMessage()).contains("Access denied");
            assertThat(exception.args().get("fileId")).isEqualTo(100L);
            assertThat(exception.args().get("requesterId")).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("InvalidFileAssetStateException 테스트")
    class InvalidFileAssetStateExceptionTests {

        @Test
        @DisplayName("FileId, currentState, expectedState로 예외 생성 성공")
        void constructor_WithFileIdAndStates_Success() {
            // Given
            FileId fileId = FileIdFixture.create(200L);
            String currentState = "PROCESSING";
            String expectedState = "AVAILABLE";

            // When
            InvalidFileAssetStateException exception = new InvalidFileAssetStateException(
                fileId,
                currentState,
                expectedState
            );

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.INVALID_FILE_ASSET_STATE);
            assertThat(exception.code()).isEqualTo("FILE-004");
            assertThat(exception.getMessage()).contains("Invalid FileAsset state");
            assertThat(exception.args().get("fileId")).isEqualTo(200L);
            assertThat(exception.args().get("currentState")).isEqualTo("PROCESSING");
            assertThat(exception.args().get("expectedState")).isEqualTo("AVAILABLE");
        }
    }

    @Nested
    @DisplayName("FileAssetProcessingException 테스트")
    class FileAssetProcessingExceptionTests {

        @Test
        @DisplayName("FileId로 예외 생성 성공")
        void constructor_WithFileId_Success() {
            // Given
            FileId fileId = FileIdFixture.create(300L);

            // When
            FileAssetProcessingException exception = new FileAssetProcessingException(fileId);

            // Then
            assertThat(exception).isInstanceOf(FileAssetException.class);
            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_PROCESSING);
            assertThat(exception.code()).isEqualTo("FILE-005");
            assertThat(exception.getMessage()).contains("FileAsset is still processing");
            assertThat(exception.args().get("fileId")).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("FileErrorCode 테스트")
    class FileErrorCodeTests {

        @Test
        @DisplayName("모든 에러 코드가 올바르게 정의됨")
        void allErrorCodes_AreDefined() {
            // Then
            assertThat(FileErrorCode.FILE_ASSET_NOT_FOUND.getCode()).isEqualTo("FILE-001");
            assertThat(FileErrorCode.FILE_ASSET_ALREADY_DELETED.getCode()).isEqualTo("FILE-002");
            assertThat(FileErrorCode.FILE_ASSET_ACCESS_DENIED.getCode()).isEqualTo("FILE-003");
            assertThat(FileErrorCode.INVALID_FILE_ASSET_STATE.getCode()).isEqualTo("FILE-004");
            assertThat(FileErrorCode.FILE_ASSET_PROCESSING.getCode()).isEqualTo("FILE-005");
            assertThat(FileErrorCode.FILE_VARIANT_NOT_FOUND.getCode()).isEqualTo("FILE-101");
            assertThat(FileErrorCode.FILE_VARIANT_GENERATION_FAILED.getCode()).isEqualTo("FILE-102");
            assertThat(FileErrorCode.EXTRACTED_DATA_NOT_FOUND.getCode()).isEqualTo("FILE-201");
            assertThat(FileErrorCode.METADATA_EXTRACTION_FAILED.getCode()).isEqualTo("FILE-202");
            assertThat(FileErrorCode.PIPELINE_EXECUTION_FAILED.getCode()).isEqualTo("FILE-301");
            assertThat(FileErrorCode.PIPELINE_TIMEOUT.getCode()).isEqualTo("FILE-302");
        }

        @Test
        @DisplayName("모든 에러 코드가 기본 메시지를 가짐")
        void allErrorCodes_HaveDefaultMessages() {
            // Then
            assertThat(FileErrorCode.FILE_ASSET_NOT_FOUND.getDefaultMessage())
                .isEqualTo("FileAsset not found");
            assertThat(FileErrorCode.FILE_ASSET_ALREADY_DELETED.getDefaultMessage())
                .isEqualTo("FileAsset already deleted");
            assertThat(FileErrorCode.FILE_ASSET_ACCESS_DENIED.getDefaultMessage())
                .isEqualTo("Access denied to FileAsset");
            assertThat(FileErrorCode.INVALID_FILE_ASSET_STATE.getDefaultMessage())
                .isEqualTo("Invalid FileAsset state");
            assertThat(FileErrorCode.FILE_ASSET_PROCESSING.getDefaultMessage())
                .isEqualTo("FileAsset is still processing");
        }
    }
}

