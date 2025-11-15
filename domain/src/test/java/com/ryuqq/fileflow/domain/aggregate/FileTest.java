package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.vo.FileStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File Aggregate Root 테스트")
class FileTest {

    @Test
    @DisplayName("유효한 데이터로 File을 생성할 수 있어야 한다")
    void shouldCreateFileWithValidData() {
        // Given
        String fileId = "018e5f6c-1234-7890-abcd-1234567890ab";
        String fileName = "test-image.jpg";
        long fileSize = 1024000L; // 1MB
        String mimeType = "image/jpeg";
        FileStatus status = FileStatus.PENDING;
        String s3Key = "uploads/2024/01/test-image.jpg";
        String s3Bucket = "fileflow-storage";
        String cdnUrl = "https://cdn.example.com/uploads/2024/01/test-image.jpg";
        Long uploaderId = 12345L;
        String category = "IMAGE";
        String tags = "product,thumbnail";
        int version = 1;
        LocalDateTime deletedAt = null;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        File file = new File(
                fileId,
                fileName,
                fileSize,
                mimeType,
                status,
                s3Key,
                s3Bucket,
                cdnUrl,
                uploaderId,
                category,
                tags,
                version,
                deletedAt,
                createdAt,
                updatedAt
        );

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isEqualTo(fileId);
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
        assertThat(file.getStatus()).isEqualTo(status);
        assertThat(file.getS3Key()).isEqualTo(s3Key);
        assertThat(file.getS3Bucket()).isEqualTo(s3Bucket);
        assertThat(file.getCdnUrl()).isEqualTo(cdnUrl);
        assertThat(file.getUploaderId()).isEqualTo(uploaderId);
        assertThat(file.getCategory()).isEqualTo(category);
        assertThat(file.getTags()).isEqualTo(tags);
        assertThat(file.getVersion()).isEqualTo(version);
        assertThat(file.getDeletedAt()).isNull();
        assertThat(file.getCreatedAt()).isEqualTo(createdAt);
        assertThat(file.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("필수 필드가 올바르게 설정되어야 한다")
    void shouldHaveRequiredFields() {
        // Given
        String fileId = "018e5f6c-1234-7890-abcd-1234567890ab";
        String fileName = "document.pdf";
        long fileSize = 512000L;
        String mimeType = "application/pdf";

        // When
        File file = new File(
                fileId,
                fileName,
                fileSize,
                mimeType,
                FileStatus.PENDING,
                "uploads/doc.pdf",
                "fileflow-storage",
                "https://cdn.example.com/doc.pdf",
                100L,
                "DOCUMENT",
                null,
                1,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Then - 필수 필드 검증
        assertThat(file.getFileId()).isNotBlank();
        assertThat(file.getFileName()).isNotBlank();
        assertThat(file.getFileSize()).isPositive();
        assertThat(file.getMimeType()).isNotBlank();
        assertThat(file.getStatus()).isNotNull();
    }
}
