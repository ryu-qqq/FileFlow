package com.ryuqq.fileflow.domain.upload.model;

import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadRequestTest {

    @Test
    @DisplayName("유효한 파라미터로 UploadRequest를 생성할 수 있다")
    void createUploadRequest() {
        // given
        String fileName = "test.jpg";
        FileType fileType = FileType.IMAGE;
        long fileSizeBytes = 1024L;
        String contentType = "image/jpeg";

        // when
        UploadRequest uploadRequest = UploadRequest.of(fileName, fileType, fileSizeBytes, contentType);

        // then
        assertThat(uploadRequest.fileName()).isEqualTo(fileName);
        assertThat(uploadRequest.fileType()).isEqualTo(fileType);
        assertThat(uploadRequest.fileSizeBytes()).isEqualTo(fileSizeBytes);
        assertThat(uploadRequest.contentType()).isEqualTo(contentType);
    }

    @Test
    @DisplayName("파일명이 null이면 예외가 발생한다")
    void createUploadRequest_withNullFileName() {
        assertThatThrownBy(() ->
                UploadRequest.of(null, FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileName cannot be null or empty");
    }

    @Test
    @DisplayName("파일명이 빈 문자열이면 예외가 발생한다")
    void createUploadRequest_withEmptyFileName() {
        assertThatThrownBy(() ->
                UploadRequest.of("   ", FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileName cannot be null or empty");
    }

    @Test
    @DisplayName("파일명이 255자를 초과하면 예외가 발생한다")
    void createUploadRequest_withTooLongFileName() {
        String longFileName = "a".repeat(256);

        assertThatThrownBy(() ->
                UploadRequest.of(longFileName, FileType.IMAGE, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileName cannot exceed 255 characters");
    }

    @Test
    @DisplayName("파일 타입이 null이면 예외가 발생한다")
    void createUploadRequest_withNullFileType() {
        assertThatThrownBy(() ->
                UploadRequest.of("test.jpg", null, 1024L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileType cannot be null");
    }

    @Test
    @DisplayName("파일 크기가 0 이하면 예외가 발생한다")
    void createUploadRequest_withNonPositiveFileSize() {
        assertThatThrownBy(() ->
                UploadRequest.of("test.jpg", FileType.IMAGE, 0L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileSizeBytes must be positive");

        assertThatThrownBy(() ->
                UploadRequest.of("test.jpg", FileType.IMAGE, -1L, "image/jpeg")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("FileSizeBytes must be positive");
    }

    @Test
    @DisplayName("Content Type이 null이면 예외가 발생한다")
    void createUploadRequest_withNullContentType() {
        assertThatThrownBy(() ->
                UploadRequest.of("test.jpg", FileType.IMAGE, 1024L, null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ContentType cannot be null or empty");
    }

    @Test
    @DisplayName("동일한 값을 가진 UploadRequest는 equals로 같다고 판단된다")
    void uploadRequest_equality() {
        UploadRequest request1 = UploadRequest.of("test.jpg", FileType.IMAGE, 1024L, "image/jpeg");
        UploadRequest request2 = UploadRequest.of("test.jpg", FileType.IMAGE, 1024L, "image/jpeg");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }
}
