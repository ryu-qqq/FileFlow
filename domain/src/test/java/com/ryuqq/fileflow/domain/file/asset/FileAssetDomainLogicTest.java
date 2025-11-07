package com.ryuqq.fileflow.domain.file.asset;

import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetAccessDeniedException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetAlreadyDeletedException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetProcessingException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileErrorCode;
import com.ryuqq.fileflow.domain.file.asset.exception.InvalidFileAssetStateException;
import com.ryuqq.fileflow.domain.file.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileAsset Domain Logic 테스트
 *
 * <p>FileAsset의 비즈니스 로직 메서드를 테스트합니다.</p>
 * <ul>
 *   <li>checkAccessPermission() - 접근 권한 확인</li>
 *   <li>ensureAvailable() - 사용 가능 상태 확인</li>
 *   <li>markAsAvailable() - 상태 전이 (Custom Exception 사용)</li>
 *   <li>softDelete() - Soft Delete (Custom Exception 사용)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileAsset Domain Logic 테스트")
class FileAssetDomainLogicTest {

    @Nested
    @DisplayName("checkAccessPermission 메서드 테스트")
    class CheckAccessPermissionTests {

        @Test
        @DisplayName("checkAccessPermission_WithOwnerId_ShouldPass - 소유자 접근 허용")
        void checkAccessPermission_WithOwnerId_ShouldPass() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAvailable(1L);
            Long ownerId = fileAsset.getOwnerUserId();

            // When & Then - 예외가 발생하지 않아야 함
            fileAsset.checkAccessPermission(ownerId);
        }

        @Test
        @DisplayName("checkAccessPermission_WithAnonymousFile_ShouldPass - 익명 업로드 파일은 누구나 접근 가능")
        void checkAccessPermission_WithAnonymousFile_ShouldPass() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAnonymous(1L);
            Long requesterId = 999L;

            // When & Then - 예외가 발생하지 않아야 함
            fileAsset.checkAccessPermission(requesterId);
        }

        @Test
        @DisplayName("checkAccessPermission_WithDifferentUserId_ShouldThrowException - 다른 사용자 접근 거부")
        void checkAccessPermission_WithDifferentUserId_ShouldThrowException() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAvailable(1L);
            Long differentUserId = 999L;

            // When & Then
            assertThatThrownBy(() -> fileAsset.checkAccessPermission(differentUserId))
                .isInstanceOf(FileAssetAccessDeniedException.class)
                .satisfies(ex -> {
                    FileAssetAccessDeniedException exception = (FileAssetAccessDeniedException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_ACCESS_DENIED);
                    assertThat(exception.code()).isEqualTo("FILE-003");
                });
        }
    }

    @Nested
    @DisplayName("ensureAvailable 메서드 테스트")
    class EnsureAvailableTests {

        @Test
        @DisplayName("ensureAvailable_WithAvailableStatus_ShouldPass - AVAILABLE 상태는 통과")
        void ensureAvailable_WithAvailableStatus_ShouldPass() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAvailable(1L);

            // When & Then - 예외가 발생하지 않아야 함
            fileAsset.ensureAvailable();
        }

        @Test
        @DisplayName("ensureAvailable_WithProcessingStatus_ShouldThrowException - PROCESSING 상태는 예외")
        void ensureAvailable_WithProcessingStatus_ShouldThrowException() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createProcessing(1L);

            // When & Then
            assertThatThrownBy(() -> fileAsset.ensureAvailable())
                .isInstanceOf(FileAssetProcessingException.class)
                .satisfies(ex -> {
                    FileAssetProcessingException exception = (FileAssetProcessingException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_PROCESSING);
                    assertThat(exception.code()).isEqualTo("FILE-005");
                });
        }

        @Test
        @DisplayName("ensureAvailable_WithDeletedStatus_ShouldThrowException - DELETED 상태는 예외")
        void ensureAvailable_WithDeletedStatus_ShouldThrowException() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> fileAsset.ensureAvailable())
                .isInstanceOf(FileAssetAlreadyDeletedException.class)
                .satisfies(ex -> {
                    FileAssetAlreadyDeletedException exception = (FileAssetAlreadyDeletedException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_ALREADY_DELETED);
                    assertThat(exception.code()).isEqualTo("FILE-002");
                });
        }
    }

    @Nested
    @DisplayName("markAsAvailable 메서드 테스트 (Custom Exception)")
    class MarkAsAvailableTests {

        @Test
        @DisplayName("markAsAvailable_WithProcessingStatus_ShouldSucceed - PROCESSING → AVAILABLE 전이 성공")
        void markAsAvailable_WithProcessingStatus_ShouldSucceed() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createProcessing(1L);
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.PROCESSING);

            // When
            fileAsset.markAsAvailable();

            // Then
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.AVAILABLE);
            assertThat(fileAsset.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("markAsAvailable_WithNonProcessingStatus_ShouldThrowException - PROCESSING이 아니면 예외")
        void markAsAvailable_WithNonProcessingStatus_ShouldThrowException() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAvailable(1L);

            // When & Then
            assertThatThrownBy(() -> fileAsset.markAsAvailable())
                .isInstanceOf(InvalidFileAssetStateException.class)
                .satisfies(ex -> {
                    InvalidFileAssetStateException exception = (InvalidFileAssetStateException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.INVALID_FILE_ASSET_STATE);
                    assertThat(exception.code()).isEqualTo("FILE-004");
                    assertThat(exception.args().get("currentState")).isEqualTo("AVAILABLE");
                    assertThat(exception.args().get("expectedState")).isEqualTo("AVAILABLE");
                });
        }
    }

    @Nested
    @DisplayName("softDelete 메서드 테스트 (Custom Exception)")
    class SoftDeleteTests {

        @Test
        @DisplayName("softDelete_WithAvailableStatus_ShouldSucceed - AVAILABLE 상태 삭제 성공")
        void softDelete_WithAvailableStatus_ShouldSucceed() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createAvailable(1L);
            assertThat(fileAsset.isDeleted()).isFalse();

            // When
            fileAsset.softDelete();

            // Then
            assertThat(fileAsset.isDeleted()).isTrue();
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.DELETED);
            assertThat(fileAsset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("softDelete_WithAlreadyDeletedStatus_ShouldThrowException - 이미 삭제된 파일은 예외")
        void softDelete_WithAlreadyDeletedStatus_ShouldThrowException() {
            // Given
            FileAsset fileAsset = FileAssetFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> fileAsset.softDelete())
                .isInstanceOf(FileAssetAlreadyDeletedException.class)
                .satisfies(ex -> {
                    FileAssetAlreadyDeletedException exception = (FileAssetAlreadyDeletedException) ex;
                    assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.FILE_ASSET_ALREADY_DELETED);
                    assertThat(exception.code()).isEqualTo("FILE-002");
                });
        }
    }

    @Nested
    @DisplayName("fromS3Upload 메서드 테스트 (S3UploadMetadata 사용)")
    class FromS3UploadTests {

        @Test
        @DisplayName("fromS3Upload_WithS3UploadMetadata_ShouldCreateFileAsset - S3UploadMetadata로 생성")
        void fromS3Upload_WithS3UploadMetadata_ShouldCreateFileAsset() {
            // Given
            UploadSession session = UploadSessionFixture.reconstituteDefault(1L);
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1024L,
                "etag-123",
                "image/jpeg",
                "uploads/tenant-1/2024/11/06/test.jpg"
            );

            // When
            FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

            // Then
            assertThat(fileAsset).isNotNull();
            assertThat(fileAsset.getIdValue()).isNull();
            assertThat(fileAsset.getTenantId()).isEqualTo(session.getTenantId());
            assertThat(fileAsset.getFileName()).isEqualTo(session.getFileName());
            assertThat(fileAsset.getFileSize().bytes()).isEqualTo(1024L);
            assertThat(fileAsset.getMimeType().value()).isEqualTo("image/jpeg");
            assertThat(fileAsset.getStorageKey().value()).isEqualTo("uploads/tenant-1/2024/11/06/test.jpg");
            assertThat(fileAsset.getChecksum().value()).isEqualTo("etag-123");
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.PROCESSING);
            assertThat(fileAsset.getVisibility()).isEqualTo(Visibility.PRIVATE);
            assertThat(fileAsset.getOrganizationId()).isNull();
            assertThat(fileAsset.getOwnerUserId()).isNull();
        }

        @Test
        @DisplayName("fromS3Upload_WithNullContentType_ShouldUseDefaultMimeType - null ContentType 시 기본값 사용")
        void fromS3Upload_WithNullContentType_ShouldUseDefaultMimeType() {
            // Given
            UploadSession session = UploadSessionFixture.reconstituteDefault(1L);
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1024L,
                "etag-123",
                null,
                "uploads/test.jpg"
            );

            // When
            FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

            // Then
            assertThat(fileAsset.getMimeType().value()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("fromS3Upload_WithNullSession_ShouldThrowException - null 세션")
        void fromS3Upload_WithNullSession_ShouldThrowException() {
            // Given
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1024L,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            );

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromS3Upload(null, s3Metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UploadSession은 필수입니다");
        }

        @Test
        @DisplayName("fromS3Upload_WithNullS3Metadata_ShouldThrowException - null S3Metadata")
        void fromS3Upload_WithNullS3Metadata_ShouldThrowException() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromS3Upload(session, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3UploadMetadata는 필수입니다");
        }
    }
}

