package com.ryuqq.fileflow.domain.upload.command;

import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUploadCommandTest {

    private PolicyKey createPolicyKey() {
        return PolicyKey.of("b2c", "CONSUMER", "REVIEW");
    }

    @Test
    @DisplayName("유효한 파라미터로 FileUploadCommand를 생성할 수 있다")
    void createFileUploadCommand() {
        // given
        PolicyKey policyKey = createPolicyKey();
        String uploaderId = "user123";
        String fileName = "test.jpg";
        FileType fileType = FileType.IMAGE;
        long fileSizeBytes = 1024L;
        String contentType = "image/jpeg";

        // when
        FileUploadCommand command = FileUploadCommand.of(
                policyKey, uploaderId, fileName, fileType, fileSizeBytes, contentType
        );

        // then
        assertThat(command.policyKey()).isEqualTo(policyKey);
        assertThat(command.uploaderId()).isEqualTo(uploaderId);
        assertThat(command.fileName()).isEqualTo(fileName);
        assertThat(command.fileType()).isEqualTo(fileType);
        assertThat(command.fileSizeBytes()).isEqualTo(fileSizeBytes);
        assertThat(command.contentType()).isEqualTo(contentType);
    }

    @Test
    @DisplayName("PolicyKey가 null이면 예외가 발생한다")
    void createFileUploadCommand_withNullPolicyKey() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(null, "user123", "test.jpg", FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("PolicyKey cannot be null");
    }

    @Test
    @DisplayName("UploaderId가 null이면 예외가 발생한다")
    void createFileUploadCommand_withNullUploaderId() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(createPolicyKey(), null, "test.jpg", FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("UploaderId cannot be null or empty");
    }

    @Test
    @DisplayName("FileName이 null이면 예외가 발생한다")
    void createFileUploadCommand_withNullFileName() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(createPolicyKey(), "user123", null, FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileName cannot be null or empty");
    }

    @Test
    @DisplayName("FileType이 null이면 예외가 발생한다")
    void createFileUploadCommand_withNullFileType() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(createPolicyKey(), "user123", "test.jpg", null, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileType cannot be null");
    }

    @Test
    @DisplayName("FileSizeBytes가 0 이하면 예외가 발생한다")
    void createFileUploadCommand_withNonPositiveFileSize() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(createPolicyKey(), "user123", "test.jpg", FileType.IMAGE, 0L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileSizeBytes must be positive");
    }

    @Test
    @DisplayName("ContentType이 null이면 예외가 발생한다")
    void createFileUploadCommand_withNullContentType() {
        assertThatThrownBy(() ->
                FileUploadCommand.of(createPolicyKey(), "user123", "test.jpg", FileType.IMAGE, 1024L, null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ContentType cannot be null or empty");
    }
}
