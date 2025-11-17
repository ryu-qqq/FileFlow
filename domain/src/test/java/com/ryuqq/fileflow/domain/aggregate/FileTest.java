package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;
import com.ryuqq.fileflow.domain.exception.InvalidMimeTypeException;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.fixture.FileStatusFixture;
import com.ryuqq.fileflow.domain.vo.RetryCount;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("File Aggregate Root 테스트")
class FileTest {

    @Test
    @DisplayName("유효한 데이터로 File을 생성할 수 있어야 한다")
    void shouldCreateFileWithValidData() {
        // Given & When
        File file = FileFixture.aFile()
                .fileName("test-image.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .category("IMAGE")
                .tags("product,thumbnail")
                .build();

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isNotNull();
        assertThat(file.getFileName()).isEqualTo("test-image.jpg");
        assertThat(file.getFileSize()).isEqualTo(1024000L);
        assertThat(file.getMimeType()).isEqualTo("image/jpeg");
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending());
        assertThat(file.getS3Key()).contains("test-image.jpg");
        assertThat(file.getS3Bucket()).isEqualTo("fileflow-storage");
        assertThat(file.getCdnUrl()).contains("test-image.jpg");
        assertThat(file.getUploaderId()).isNotNull();
        assertThat(file.getCategory()).isEqualTo("IMAGE");
        assertThat(file.getTags()).isEqualTo("product,thumbnail");
        assertThat(file.getVersion()).isEqualTo(1);
        assertThat(file.getDeletedAt()).isNull();
        assertThat(file.getCreatedAt()).isNotNull();
        assertThat(file.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드가 올바르게 설정되어야 한다")
    void shouldHaveRequiredFields() {
        // Given & When - PDF 문서 Fixture 사용
        File file = FileFixture.aPdfDocument();

        // Then - 필수 필드 검증
        assertThat(file.getFileId()).isNotNull();
        assertThat(file.getFileName()).isNotBlank();
        assertThat(file.getFileSize()).isPositive();
        assertThat(file.getMimeType()).isNotBlank();
        assertThat(file.getStatus()).isNotNull();
    }

    @Test
    @DisplayName("JPG 이미지 Fixture를 사용할 수 있어야 한다")
    void shouldUseJpgImageFixture() {
        // Given & When
        File file = FileFixture.aJpgImage();

        // Then
        assertThat(file.getFileName()).isEqualTo("test-image.jpg");
        assertThat(file.getMimeType()).isEqualTo("image/jpeg");
        assertThat(file.getCategory()).isEqualTo("IMAGE");
    }

    @Test
    @DisplayName("Excel 파일 Fixture를 사용할 수 있어야 한다")
    void shouldUseExcelFileFixture() {
        // Given & When
        File file = FileFixture.anExcelFile();

        // Then
        assertThat(file.getFileName()).isEqualTo("data.xlsx");
        assertThat(file.getMimeType()).contains("spreadsheetml");
        assertThat(file.getCategory()).isEqualTo("EXCEL");
    }

    // ===== 3종 팩토리 메서드 테스트 (forNew, of, reconstitute) =====

    @Test
    @DisplayName("forNew()는 ID가 null인 새 파일을 생성해야 한다")
    void shouldCreateNewFileWithForNew() {
        // Given
        String fileName = "test-image.jpg";
        long fileSize = 2048000L; // 2MB
        String mimeType = "image/jpeg";
        String s3Key = "uploads/2024/01/test-image.jpg";
        String s3Bucket = "fileflow-storage";
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();
        String category = "IMAGE";
        String tags = "product,thumbnail";

        // When
        File file = File.forNew(
                fileName,
                fileSize,
                mimeType,
                s3Key,
                s3Bucket,
                uploaderId,
                category,
                tags,
                java.time.Clock.systemUTC()
        );

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isNotNull();
        assertThat(file.getFileId().isNew()).isTrue(); // ID가 null이어야 함
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending());
        assertThat(file.getS3Key()).isEqualTo(s3Key);
        assertThat(file.getS3Bucket()).isEqualTo(s3Bucket);
        assertThat(file.getUploaderId()).isEqualTo(uploaderId);
        assertThat(file.getCategory()).isEqualTo(category);
        assertThat(file.getTags()).isEqualTo(tags);
        assertThat(file.getRetryCount()).isEqualTo(0);
        assertThat(file.getVersion()).isEqualTo(1);
        assertThat(file.getDeletedAt()).isNull();
        assertThat(file.getCreatedAt()).isNotNull();
        assertThat(file.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("of()는 유효한 ID로 파일을 생성해야 한다")
    void shouldCreateFileWithOf() {
        // Given
        com.ryuqq.fileflow.domain.vo.FileId fileId = com.ryuqq.fileflow.domain.fixture.FileIdFixture.aFileId();
        String fileName = "test-document.pdf";
        long fileSize = 5120000L; // 5MB
        String mimeType = "application/pdf";
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();
        String s3Key = "uploads/2024/01/test-document.pdf";
        String s3Bucket = "fileflow-storage";

        // When
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        File file = File.of(
                java.time.Clock.systemUTC(),
                fileId,
                fileName,
                fileSize,
                mimeType,
                FileStatusFixture.pending(),
                s3Key,
                s3Bucket,
                "https://cdn.fileflow.com/" + s3Key,
                uploaderId,
                "DOCUMENT",
                "important,contract",
                RetryCount.forFile(),
                1,
                null, // deletedAt
                now, // createdAt
                now  // updatedAt
        );

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isEqualTo(fileId);
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
    }

    @Test
    @DisplayName("of()는 null ID로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenOfWithNullId() {
        // Given
        com.ryuqq.fileflow.domain.vo.FileId nullFileId = null;
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();

        // When & Then
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        assertThatThrownBy(() -> File.of(
                java.time.Clock.systemUTC(),
                nullFileId,
                "test.jpg",
                1024000L,
                "image/jpeg",
                FileStatusFixture.pending(),
                "uploads/test.jpg",
                "fileflow-storage",
                "https://cdn.fileflow.com/uploads/test.jpg",
                uploaderId,
                "IMAGE",
                null,
                RetryCount.forFile(),
                1,
                null,
                now,
                now
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute()는 영속성 계층에서 파일을 재구성해야 한다")
    void shouldReconstituteFile() {
        // Given
        com.ryuqq.fileflow.domain.vo.FileId fileId = com.ryuqq.fileflow.domain.fixture.FileIdFixture.aFileId();
        String fileName = "reconstructed.jpg";
        long fileSize = 3072000L; // 3MB
        String mimeType = "image/jpeg";
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();

        // When
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        File file = File.reconstitute(
                java.time.Clock.systemUTC(),
                fileId,
                fileName,
                fileSize,
                mimeType,
                FileStatusFixture.completed(),
                "uploads/2024/01/reconstructed.jpg",
                "fileflow-storage",
                "https://cdn.fileflow.com/uploads/2024/01/reconstructed.jpg",
                uploaderId,
                "IMAGE",
                "archived",
                RetryCount.forFile().increment().increment(),
                5,
                null,
                now.minusDays(7), // createdAt
                now               // updatedAt
        );

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isEqualTo(fileId);
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
        assertThat(file.getRetryCount()).isEqualTo(2);
        assertThat(file.getVersion()).isEqualTo(5);
    }

    @Test
    @DisplayName("reconstitute()는 null ID로 재구성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenReconstituteWithNullId() {
        // Given
        com.ryuqq.fileflow.domain.vo.FileId nullFileId = null;
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();

        // When & Then
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        assertThatThrownBy(() -> File.reconstitute(
                java.time.Clock.systemUTC(),
                nullFileId,
                "test.jpg",
                1024000L,
                "image/jpeg",
                FileStatusFixture.pending(),
                "uploads/test.jpg",
                "fileflow-storage",
                "https://cdn.fileflow.com/uploads/test.jpg",
                uploaderId,
                "IMAGE",
                null,
                RetryCount.forFile(),
                1,
                null,
                now,
                now
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    // ===== create() 팩토리 메서드 테스트 =====

    @Test
    @DisplayName("create() 팩토리 메서드로 UUID v7과 PENDING 상태로 파일을 생성해야 한다")
    void shouldCreateFileWithUuidV7AndPendingStatus() {
        // Given
        String fileName = "new-file.jpg";
        long fileSize = 2048000L; // 2MB
        String mimeType = "image/jpeg";
        String s3Key = "uploads/new-file.jpg";
        String s3Bucket = "fileflow-storage";
        Long uploaderId = 999L;
        String category = "IMAGE";

        // When
        File file = File.forNew(fileName, fileSize, mimeType, s3Key, s3Bucket, UploaderId.of(uploaderId), category, null, Clock.systemUTC());

        // Then
        assertThat(file.getFileId()).isNotNull(); // FileId VO 자동 생성
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending()); // PENDING 상태
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
        assertThat(file.getCreatedAt()).isNotNull();
        assertThat(file.getUpdatedAt()).isNotNull();
        assertThat(file.getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("파일 크기가 0일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenFileSizeZero() {
        // Given
        String fileName = "invalid.jpg";
        long fileSize = 0L; // 잘못된 크기
        String mimeType = "image/jpeg";

        // When & Then
        assertThatThrownBy(() ->
                File.forNew(fileName, fileSize, mimeType, "s3key", "bucket", UploaderId.of(1L), "IMAGE", null, Clock.systemUTC())
        )
                .isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("파일 크기가 1GB를 초과할 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenFileSizeExceeds1GB() {
        // Given
        String fileName = "too-large.jpg";
        long fileSize = 1024L * 1024L * 1024L + 1L; // 1GB + 1 byte
        String mimeType = "image/jpeg";

        // When & Then
        assertThatThrownBy(() ->
                File.forNew(fileName, fileSize, mimeType, "s3key", "bucket", UploaderId.of(1L), "IMAGE", null, Clock.systemUTC())
        )
                .isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기 제한을 초과했습니다");
    }

    @Test
    @DisplayName("유효하지 않은 MIME 타입일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenInvalidMimeType() {
        // Given
        String fileName = "malicious.exe";
        long fileSize = 1024L;
        String mimeType = "application/x-msdownload"; // 실행 파일 (허용되지 않음)

        // When & Then
        assertThatThrownBy(() ->
                File.forNew(fileName, fileSize, mimeType, "s3key", "bucket", UploaderId.of(1L), "OTHER", null, Clock.systemUTC())
        )
                .isInstanceOf(InvalidMimeTypeException.class)
                .hasMessageContaining("지원하지 않는 MIME 타입입니다");
    }

    // ===== 상태 전환 메서드 테스트 =====

    @Test
    @DisplayName("PENDING 상태에서 UPLOADING 상태로 전환할 수 있어야 한다")
    void shouldMarkAsUploading() {
        // Given
        File file = FileFixture.aJpgImage();
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending());

        // When
        file.markAsUploading();

        // Then
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.uploading());
    }

    @Test
    @DisplayName("PENDING 또는 UPLOADING 상태에서 COMPLETED 상태로 전환할 수 있어야 한다")
    void shouldMarkAsCompleted() {
        // Given - PENDING에서 COMPLETED
        File pendingFile = FileFixture.aJpgImage();

        // When
        pendingFile.markAsCompleted();

        // Then
        assertThat(pendingFile.getStatus()).isEqualTo(FileStatusFixture.completed());
    }

    @Test
    @DisplayName("COMPLETED가 아닌 상태에서 markAsCompleted 호출 시 예외가 발생해야 한다")
    void shouldMarkAsCompletedOnlyWhenPendingOrUploading() {
        // Given - PENDING 파일을 UPLOADING으로 변경
        File file = FileFixture.aJpgImage();
        file.markAsUploading();

        // When - UPLOADING에서 COMPLETED로 전환
        file.markAsCompleted();

        // Then
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.completed());
    }

    @Test
    @DisplayName("임의의 상태에서 FAILED 상태로 전환할 수 있어야 한다")
    void shouldMarkAsFailed() {
        // Given
        File file = FileFixture.aJpgImage();

        // When
        file.markAsFailed();

        // Then
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.failed());
    }

    @Test
    @DisplayName("COMPLETED 상태에서만 PROCESSING 상태로 전환할 수 있어야 한다")
    void shouldMarkAsProcessing() {
        // Given - COMPLETED 파일
        File file = FileFixture.aJpgImage();
        file.markAsCompleted();

        // When
        file.markAsProcessing();

        // Then
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.processing());
    }

    @Test
    @DisplayName("COMPLETED가 아닌 상태에서 markAsProcessing 호출 시 예외가 발생해야 한다")
    void shouldMarkAsProcessingOnlyWhenCompleted() {
        // Given - PENDING 파일
        File file = FileFixture.aJpgImage();

        // When & Then
        assertThatThrownBy(() -> file.markAsProcessing())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED 상태에서만 PROCESSING으로 전환할 수 있습니다");
    }

    // ===== 부가 메서드 테스트 =====

    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있어야 한다")
    void shouldIncrementRetryCount() {
        // Given
        File file = FileFixture.aJpgImage();
        assertThat(file.getRetryCount()).isEqualTo(0);

        // When
        file.incrementRetryCount();

        // Then
        assertThat(file.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("파일을 소프트 삭제할 수 있어야 한다")
    void shouldSoftDelete() {
        // Given
        File file = FileFixture.aJpgImage();
        assertThat(file.getDeletedAt()).isNull();

        // When
        file.softDelete();

        // Then
        assertThat(file.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 파일은 다시 삭제할 수 없어야 한다")
    void shouldNotSoftDeleteTwice() {
        // Given
        File file = FileFixture.aJpgImage();
        file.softDelete();

        // When & Then
        assertThatThrownBy(() -> file.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 파일입니다");
    }

    // ===== Clock 의존성 테스트 =====

    @Test
    @DisplayName("forNew()는 Clock을 사용하여 createdAt을 설정해야 한다")
    void shouldUseClockForCreatedAt() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T10:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        String fileName = "test-image.jpg";
        long fileSize = 2048000L; // 2MB
        String mimeType = "image/jpeg";
        String s3Key = "uploads/2024/01/test-image.jpg";
        String s3Bucket = "fileflow-storage";
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();

        // When
        File file = File.forNew(
                fileName,
                fileSize,
                mimeType,
                s3Key,
                s3Bucket,
                uploaderId,
                "IMAGE",
                null,
                fixedClock
        );

        // Then
        assertThat(file.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 10, 0, 0));
        assertThat(file.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 10, 0, 0));
    }

    @Test
    @DisplayName("markAsCompleted()는 Clock을 사용하여 updatedAt을 설정해야 한다")
    void shouldUseClockForUpdatedAt() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T12:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();
        File file = File.forNew(
                "test.jpg",
                1024000L,
                "image/jpeg",
                "uploads/test.jpg",
                "fileflow-storage",
                uploaderId,
                "IMAGE",
                null,
                fixedClock
        );

        // When
        file.markAsCompleted();

        // Then
        assertThat(file.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 12, 0, 0));
    }

    @Test
    @DisplayName("고정된 Clock으로 파일을 생성할 수 있어야 한다")
    void shouldCreateFileWithFixedClock() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-20T15:30:00Z"),
                java.time.ZoneId.of("UTC")
        );
        com.ryuqq.fileflow.domain.vo.UploaderId uploaderId = com.ryuqq.fileflow.domain.fixture.UploaderIdFixture.anUploaderId();

        // When
        File file = File.forNew(
                "fixed-clock-test.jpg",
                1024000L,
                "image/jpeg",
                "uploads/fixed-clock-test.jpg",
                "fileflow-storage",
                uploaderId,
                "IMAGE",
                null,
                fixedClock
        );

        // Then
        java.time.LocalDateTime expectedTime = java.time.LocalDateTime.of(2025, 1, 20, 15, 30, 0);
        assertThat(file.getCreatedAt()).isEqualTo(expectedTime);
        assertThat(file.getUpdatedAt()).isEqualTo(expectedTime);
        assertThat(file.getFileId()).isNotNull();
        assertThat(file.getFileId().isNew()).isTrue();
    }

    // ===== 가변 패턴 테스트 =====

    @Test
    @DisplayName("markAsUploading()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsUploading() {
        // Given
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile()
                .status(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.pending())
                .build();

        // When
        file.markAsUploading();

        // Then - 동일한 객체가 변경됨
        assertThat(file.getStatus()).isEqualTo(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.uploading());
    }

    @Test
    @DisplayName("markAsCompleted()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsCompleted() {
        // Given
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile()
                .status(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.uploading())
                .build();

        // When
        file.markAsCompleted();

        // Then - 동일한 객체가 변경됨
        assertThat(file.getStatus()).isEqualTo(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.completed());
    }

    @Test
    @DisplayName("markAsFailed()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsFailed() {
        // Given
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile()
                .status(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.uploading())
                .build();

        // When
        file.markAsFailed();

        // Then - 동일한 객체가 변경됨
        assertThat(file.getStatus()).isEqualTo(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.failed());
    }

    @Test
    @DisplayName("markAsCompleted()는 새 객체를 반환하지 않아야 한다 (동일 객체 변경)")
    void shouldNotReturnNewInstanceWhenMarkAsCompleted() {
        // Given
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile()
                .status(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.uploading())
                .build();

        // When
        file.markAsCompleted();

        // Then - 반환값이 void이므로 상태만 검증
        assertThat(file.getStatus()).isEqualTo(com.ryuqq.fileflow.domain.fixture.FileStatusFixture.completed());
    }

    // ===== Law of Demeter 테스트 =====

    @Test
    @DisplayName("getFileIdValue()는 체이닝 없이 FileId 값을 반환해야 한다")
    void shouldReturnFileIdValueWithoutChaining() {
        // Given - reconstitute로 실제 FileId 값이 있는 파일 생성
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile().build();

        // When
        String fileIdValue = file.getFileIdValue();

        // Then
        assertThat(fileIdValue).isNotNull();
        assertThat(fileIdValue).isEqualTo(file.getFileId().getValue());
    }

    @Test
    @DisplayName("getUploaderIdValue()는 체이닝 없이 UploaderId 값을 반환해야 한다")
    void shouldReturnUploaderIdValueWithoutChaining() {
        // Given - reconstitute로 실제 UploaderId 값이 있는 파일 생성
        File file = com.ryuqq.fileflow.domain.fixture.FileFixture.aFile().build();

        // When
        Long uploaderIdValue = file.getUploaderIdValue();

        // Then
        assertThat(uploaderIdValue).isNotNull();
        assertThat(uploaderIdValue).isEqualTo(file.getUploaderId().getValue());
    }
}
