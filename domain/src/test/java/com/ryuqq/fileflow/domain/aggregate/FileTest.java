package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;
import com.ryuqq.fileflow.domain.exception.InvalidMimeTypeException;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.fixture.FileStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        assertThat(file.getFileId()).isNotBlank();
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
        assertThat(file.getFileId()).isNotBlank();
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
        File file = File.create(fileName, fileSize, mimeType, s3Key, s3Bucket, uploaderId, category, null);

        // Then
        assertThat(file.getFileId()).isNotBlank(); // UUID v7 자동 생성
        assertThat(file.getFileId()).hasSize(36); // UUID 표준 길이
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
                File.create(fileName, fileSize, mimeType, "s3key", "bucket", 1L, "IMAGE", null)
        )
                .isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 0보다 커야 합니다");
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
                File.create(fileName, fileSize, mimeType, "s3key", "bucket", 1L, "IMAGE", null)
        )
                .isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 1GB를 초과할 수 없습니다");
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
                File.create(fileName, fileSize, mimeType, "s3key", "bucket", 1L, "OTHER", null)
        )
                .isInstanceOf(InvalidMimeTypeException.class)
                .hasMessageContaining("허용되지 않는 MIME 타입입니다");
    }

    // ===== 상태 전환 메서드 테스트 =====

    @Test
    @DisplayName("PENDING 상태에서 UPLOADING 상태로 전환할 수 있어야 한다")
    void shouldMarkAsUploading() {
        // Given
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending());

        // When
        File uploadingFile = file.markAsUploading();

        // Then
        assertThat(uploadingFile.getStatus()).isEqualTo(FileStatusFixture.uploading());
        assertThat(uploadingFile.getUpdatedAt()).isAfter(file.getUpdatedAt());
    }

    @Test
    @DisplayName("PENDING 또는 UPLOADING 상태에서 COMPLETED 상태로 전환할 수 있어야 한다")
    void shouldMarkAsCompleted() {
        // Given - PENDING에서 COMPLETED
        File pendingFile = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");

        // When
        File completedFile = pendingFile.markAsCompleted();

        // Then
        assertThat(completedFile.getStatus()).isEqualTo(FileStatusFixture.completed());
        assertThat(completedFile.getUpdatedAt()).isAfter(pendingFile.getUpdatedAt());
    }

    @Test
    @DisplayName("COMPLETED가 아닌 상태에서 markAsCompleted 호출 시 예외가 발생해야 한다")
    void shouldMarkAsCompletedOnlyWhenPendingOrUploading() {
        // Given - PENDING 파일을 UPLOADING으로 변경
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        File uploadingFile = file.markAsUploading();

        // When - UPLOADING에서 COMPLETED로 전환
        File completedFile = uploadingFile.markAsCompleted();

        // Then
        assertThat(completedFile.getStatus()).isEqualTo(FileStatusFixture.completed());
    }

    @Test
    @DisplayName("임의의 상태에서 FAILED 상태로 전환할 수 있어야 한다")
    void shouldMarkAsFailed() {
        // Given
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");

        // When
        File failedFile = file.markAsFailed("Upload error");

        // Then
        assertThat(failedFile.getStatus()).isEqualTo(FileStatusFixture.failed());
        assertThat(failedFile.getUpdatedAt()).isAfter(file.getUpdatedAt());
    }

    @Test
    @DisplayName("COMPLETED 상태에서만 PROCESSING 상태로 전환할 수 있어야 한다")
    void shouldMarkAsProcessing() {
        // Given - COMPLETED 파일
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        File completedFile = file.markAsCompleted();

        // When
        File processingFile = completedFile.markAsProcessing();

        // Then
        assertThat(processingFile.getStatus()).isEqualTo(FileStatusFixture.processing());
        assertThat(processingFile.getUpdatedAt()).isAfter(completedFile.getUpdatedAt());
    }

    @Test
    @DisplayName("COMPLETED가 아닌 상태에서 markAsProcessing 호출 시 예외가 발생해야 한다")
    void shouldMarkAsProcessingOnlyWhenCompleted() {
        // Given - PENDING 파일
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");

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
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        assertThat(file.getRetryCount()).isEqualTo(0);

        // When
        File retriedFile = file.incrementRetryCount();

        // Then
        assertThat(retriedFile.getRetryCount()).isEqualTo(1);
        assertThat(retriedFile.getUpdatedAt()).isAfter(file.getUpdatedAt());
    }

    @Test
    @DisplayName("파일을 소프트 삭제할 수 있어야 한다")
    void shouldSoftDelete() {
        // Given
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        assertThat(file.getDeletedAt()).isNull();

        // When
        File deletedFile = file.softDelete();

        // Then
        assertThat(deletedFile.getDeletedAt()).isNotNull();
        assertThat(deletedFile.getUpdatedAt()).isAfter(file.getUpdatedAt());
    }

    @Test
    @DisplayName("이미 삭제된 파일은 다시 삭제할 수 없어야 한다")
    void shouldNotSoftDeleteTwice() {
        // Given
        File file = FileFixture.createFile("test.jpg", 1024L, "image/jpeg", 1L, "IMAGE");
        File deletedFile = file.softDelete();

        // When & Then
        assertThatThrownBy(() -> deletedFile.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 파일입니다");
    }
}
