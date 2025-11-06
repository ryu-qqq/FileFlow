package com.ryuqq.fileflow.domain.file.asset;

import com.ryuqq.fileflow.domain.file.asset.S3UploadMetadata;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.*;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileAsset Factory Methods 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>fromS3Upload() - S3 업로드 완료 시 FileAsset 생성</li>
 *   <li>fromCompletedUpload() - 외부 다운로드 완료 시 FileAsset 생성</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Factory Method 로직 정확성</li>
 *   <li>Null 파라미터 검증</li>
 *   <li>Domain 값 객체 변환</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileAsset Factory Methods 단위 테스트")
class FileAssetTest {

    @Nested
    @DisplayName("fromS3Upload 메서드 테스트")
    class FromS3UploadTests {

        @Test
        @DisplayName("fromS3Upload_WithValidInputs_ShouldCreateFileAsset - 정상 입력으로 FileAsset 생성")
        void fromS3Upload_WithValidInputs_ShouldCreateFileAsset() {
            // Given - 업로드 세션 생성
            UploadSession session = UploadSession.forNew(
                TenantId.of(1L),
                FileName.of("test-file.jpg"),
                FileSize.of(1024L)
            );

            // S3 업로드 메타데이터 (Domain VO)
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1024L,
                "5d41402abc4b2a76b9719d911017c592",  // ETag (MD5)
                "image/jpeg",
                "uploads/tenant-1/2024/11/06/test-file.jpg"
            );

            // When - FileAsset 생성
            FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

            // Then - 검증
            assertThat(fileAsset).isNotNull();
            assertThat(fileAsset.getIdValue()).isNull();  // ID는 Persistence Layer에서 생성
            assertThat(fileAsset.getTenantId().value()).isEqualTo(1L);
            assertThat(fileAsset.getFileName().value()).isEqualTo("test-file.jpg");
            assertThat(fileAsset.getFileSize().bytes()).isEqualTo(1024L);
            assertThat(fileAsset.getMimeType().value()).isEqualTo("image/jpeg");
            assertThat(fileAsset.getChecksum().value()).isEqualTo("5d41402abc4b2a76b9719d911017c592");
            assertThat(fileAsset.getStorageKey().value()).isEqualTo("uploads/tenant-1/2024/11/06/test-file.jpg");
            assertThat(fileAsset.getUploadSessionId().value()).isEqualTo(session.getIdValue());
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.PROCESSING);
            assertThat(fileAsset.getVisibility()).isEqualTo(Visibility.PRIVATE);
        }

        @Test
        @DisplayName("fromS3Upload_WithMultipartETag_ShouldCreateFileAssetWithMultipartChecksum - 멀티파트 ETag로 생성")
        void fromS3Upload_WithMultipartETag_ShouldCreateFileAssetWithMultipartChecksum() {
            // Given
            UploadSession session = UploadSessionFixture.createMultipart(
                TenantId.of(1L),
                FileName.of("large-file.zip"),
                FileSize.of(100_000_000L)  // 100MB
            );

            // Multipart Upload ETag (형식: {MD5}-{parts})
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                100_000_000L,
                "abc123def456-5",  // Multipart ETag
                "application/zip",
                "uploads/tenant-1/2024/11/06/large-file.zip"
            );

            // When
            FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

            // Then - Multipart ETag도 정상적으로 저장됨
            assertThat(fileAsset.getChecksum().value()).isEqualTo("abc123def456-5");
            assertThat(fileAsset.getMimeType().value()).isEqualTo("application/zip");
        }

        @Test
        @DisplayName("fromS3Upload_WithNullContentType_ShouldUseDefaultMimeType - null ContentType 시 기본값 사용")
        void fromS3Upload_WithNullContentType_ShouldUseDefaultMimeType() {
            // Given
            UploadSession session = UploadSession.forNew(
                TenantId.of(1L),
                FileName.of("unknown-file"),
                FileSize.of(1024L)
            );

            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1024L,
                "etag123",
                null,  // null ContentType
                "uploads/tenant-1/2024/11/06/unknown-file"
            );

            // When
            FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

            // Then - 기본값 "application/octet-stream" 사용
            assertThat(fileAsset.getMimeType().value()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("fromS3Upload_WithNullSession_ShouldThrowException - null 세션 시 예외")
        void fromS3Upload_WithNullSession_ShouldThrowException() {
            // Given
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(1024L, "etag", "image/jpeg", "uploads/test.jpg");

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromS3Upload(null, s3Metadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UploadSession은 필수입니다");
        }

        @Test
        @DisplayName("fromS3Upload_WithNullS3Metadata_ShouldThrowException - null S3Metadata 시 예외")
        void fromS3Upload_WithNullS3Metadata_ShouldThrowException() {
            // Given
            UploadSession session = UploadSession.forNew(
                TenantId.of(1L),
                FileName.of("test.txt"),
                FileSize.of(100L)
            );

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromS3Upload(session, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3UploadMetadata는 필수입니다");
        }
    }

    @Nested
    @DisplayName("fromCompletedUpload 메서드 테스트")
    class FromCompletedUploadTests {

        @Test
        @DisplayName("fromCompletedUpload_WithValidInputs_ShouldCreateFileAsset - 정상 입력으로 FileAsset 생성")
        void fromCompletedUpload_WithValidInputs_ShouldCreateFileAsset() {
            // Given - 외부 다운로드 세션
            UploadSession session = UploadSession.createForExternalDownload(
                TenantId.of(1L),
                FileName.of("external-download.pdf"),
                FileSize.of(5000L)
            );

            StorageKey storageKey = StorageKey.of("tenant-1/external/2024/11/05/external-download.pdf");
            FileSize finalFileSize = FileSize.of(5000L);

            // When - FileAsset 생성
            FileAsset fileAsset = FileAsset.fromCompletedUpload(
                session,
                storageKey,
                finalFileSize
            );

            // Then - 검증
            assertThat(fileAsset).isNotNull();
            assertThat(fileAsset.getIdValue()).isNull();  // ID는 Persistence Layer에서 생성
            assertThat(fileAsset.getTenantId().value()).isEqualTo(1L);
            assertThat(fileAsset.getFileName().value()).isEqualTo("external-download.pdf");
            assertThat(fileAsset.getFileSize().bytes()).isEqualTo(5000L);
            assertThat(fileAsset.getStorageKey().value()).isEqualTo("tenant-1/external/2024/11/05/external-download.pdf");
            assertThat(fileAsset.getOrganizationId()).isNull();  // 외부 다운로드는 organization 없음
            assertThat(fileAsset.getOwnerUserId()).isNull();      // 외부 다운로드는 owner 없음
            assertThat(fileAsset.getMimeType().value()).isEqualTo("application/octet-stream");  // 기본값
            assertThat(fileAsset.getChecksum().value()).isEqualTo("pending");  // 비동기 계산 예정
            assertThat(fileAsset.getUploadSessionId().value()).isEqualTo(session.getIdValue());
            assertThat(fileAsset.getStatus()).isEqualTo(FileStatus.PROCESSING);
            assertThat(fileAsset.getVisibility()).isEqualTo(Visibility.PRIVATE);
        }

        @Test
        @DisplayName("fromCompletedUpload_WithLargeFile_ShouldCreateFileAsset - 대용량 파일도 정상 생성")
        void fromCompletedUpload_WithLargeFile_ShouldCreateFileAsset() {
            // Given - 1GB 파일
            UploadSession session = UploadSession.createForExternalDownload(
                TenantId.of(2L),
                FileName.of("large-video.mp4"),
                FileSize.of(1_000_000_000L)  // 1GB
            );

            StorageKey storageKey = StorageKey.of("tenant-2/external/large-video.mp4");
            FileSize finalFileSize = FileSize.of(1_000_000_000L);

            // When
            FileAsset fileAsset = FileAsset.fromCompletedUpload(
                session,
                storageKey,
                finalFileSize
            );

            // Then
            assertThat(fileAsset.getFileSize().bytes()).isEqualTo(1_000_000_000L);
            assertThat(fileAsset.getTenantId().value()).isEqualTo(2L);
        }

        @Test
        @DisplayName("fromCompletedUpload_WithNullSession_ShouldThrowException - null 세션 시 예외")
        void fromCompletedUpload_WithNullSession_ShouldThrowException() {
            // Given
            StorageKey storageKey = StorageKey.of("tenant-1/file.txt");
            FileSize fileSize = FileSize.of(100L);

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromCompletedUpload(null, storageKey, fileSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UploadSession은 필수입니다");
        }

        @Test
        @DisplayName("fromCompletedUpload_WithNullStorageKey_ShouldThrowException - null StorageKey 시 예외")
        void fromCompletedUpload_WithNullStorageKey_ShouldThrowException() {
            // Given
            UploadSession session = UploadSession.createForExternalDownload(
                TenantId.of(1L),
                FileName.of("file.txt"),
                FileSize.of(100L)
            );
            FileSize fileSize = FileSize.of(100L);

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromCompletedUpload(session, null, fileSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StorageKey는 필수입니다");
        }

        @Test
        @DisplayName("fromCompletedUpload_WithNullFileSize_ShouldThrowException - null FileSize 시 예외")
        void fromCompletedUpload_WithNullFileSize_ShouldThrowException() {
            // Given
            UploadSession session = UploadSession.createForExternalDownload(
                TenantId.of(1L),
                FileName.of("file.txt"),
                FileSize.of(100L)
            );
            StorageKey storageKey = StorageKey.of("tenant-1/file.txt");

            // When & Then
            assertThatThrownBy(() -> FileAsset.fromCompletedUpload(session, storageKey, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileSize는 필수입니다");
        }
    }

    @Nested
    @DisplayName("Factory Methods 비교 테스트")
    class FactoryMethodsComparisonTests {

        @Test
        @DisplayName("compareFactoryMethods_fromS3Upload_vs_fromCompletedUpload - 두 Factory Method 차이점 검증")
        void compareFactoryMethods_fromS3Upload_vs_fromCompletedUpload() {
            // Given - 동일한 세션
            UploadSession session = UploadSession.forNew(
                TenantId.of(1L),
                FileName.of("test.txt"),
                FileSize.of(1000L)
            );

            // S3 업로드 (실제 메타데이터 사용)
            S3UploadMetadata s3Metadata = S3UploadMetadata.of(
                1000L,
                "actual-etag-123",
                "text/plain",
                "uploads/tenant-1/2024/11/06/test.txt"
            );

            // 외부 다운로드 (기본값 사용)
            StorageKey storageKey = StorageKey.of("tenant-1/test.txt");
            FileSize fileSize = FileSize.of(1000L);

            // When
            FileAsset fromS3 = FileAsset.fromS3Upload(session, s3Metadata);
            FileAsset fromDownload = FileAsset.fromCompletedUpload(session, storageKey, fileSize);

            // Then - 차이점 검증
            // 1. MimeType 차이
            assertThat(fromS3.getMimeType().value()).isEqualTo("text/plain");         // S3에서 가져온 실제 값
            assertThat(fromDownload.getMimeType().value()).isEqualTo("application/octet-stream");  // 기본값

            // 2. Checksum 차이
            assertThat(fromS3.getChecksum().value()).isEqualTo("actual-etag-123");    // S3 ETag
            assertThat(fromDownload.getChecksum().value()).isEqualTo("pending");       // 비동기 계산 예정

            // 3. 공통점 - 기본 상태 동일
            assertThat(fromS3.getStatus()).isEqualTo(fromDownload.getStatus());        // PROCESSING
            assertThat(fromS3.getVisibility()).isEqualTo(fromDownload.getVisibility()); // PRIVATE
            assertThat(fromS3.getTenantId()).isEqualTo(fromDownload.getTenantId());
            assertThat(fromS3.getFileName()).isEqualTo(fromDownload.getFileName());
        }
    }
}
