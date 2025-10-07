package com.ryuqq.fileflow.domain.upload.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PresignedUrlInfoTest {

    @Test
    @DisplayName("유효한 파라미터로 PresignedUrlInfo를 생성할 수 있다")
    void createPresignedUrlInfo() {
        // given
        String presignedUrl = "https://example.com/upload?signature=abc123";
        String uploadPath = "uploads/2024/test.jpg";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        // when
        PresignedUrlInfo urlInfo = PresignedUrlInfo.of(presignedUrl, uploadPath, expiresAt);

        // then
        assertThat(urlInfo.presignedUrl()).isEqualTo(presignedUrl);
        assertThat(urlInfo.uploadPath()).isEqualTo(uploadPath);
        assertThat(urlInfo.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("Presigned URL이 null이면 예외가 발생한다")
    void createPresignedUrlInfo_withNullUrl() {
        assertThatThrownBy(() ->
                PresignedUrlInfo.of(null, "path", LocalDateTime.now().plusMinutes(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("PresignedUrl cannot be null or empty");
    }

    @Test
    @DisplayName("Presigned URL이 http:// 또는 https://로 시작하지 않으면 예외가 발생한다")
    void createPresignedUrlInfo_withInvalidUrlScheme() {
        assertThatThrownBy(() ->
                PresignedUrlInfo.of("ftp://example.com", "path", LocalDateTime.now().plusMinutes(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("PresignedUrl must start with http:// or https://");
    }

    @Test
    @DisplayName("uploadPath가 null이면 예외가 발생한다")
    void createPresignedUrlInfo_withNullPath() {
        assertThatThrownBy(() ->
                PresignedUrlInfo.of("https://example.com", null, LocalDateTime.now().plusMinutes(30))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("UploadPath cannot be null or empty");
    }

    @Test
    @DisplayName("expiresAt이 null이면 예외가 발생한다")
    void createPresignedUrlInfo_withNullExpiresAt() {
        assertThatThrownBy(() ->
                PresignedUrlInfo.of("https://example.com", "path", null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ExpiresAt cannot be null");
    }

    @Test
    @DisplayName("expiresAt이 과거 시간이면 예외가 발생한다")
    void createPresignedUrlInfo_withPastExpiresAt() {
        assertThatThrownBy(() ->
                PresignedUrlInfo.of("https://example.com", "path", LocalDateTime.now().minusHours(1))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ExpiresAt cannot be in the past");
    }

    @Test
    @DisplayName("만료 시간 전에는 isExpired가 false를 반환한다")
    void isExpired_beforeExpiration() {
        PresignedUrlInfo urlInfo = PresignedUrlInfo.of(
                "https://example.com",
                "path",
                LocalDateTime.now().plusMinutes(30)
        );

        assertThat(urlInfo.isExpired()).isFalse();
        assertThat(urlInfo.isValid()).isTrue();
    }

    @Test
    @DisplayName("만료 시간 후에는 isExpired가 true를 반환한다")
    void isExpired_afterExpiration() {
        PresignedUrlInfo urlInfo = PresignedUrlInfo.of(
                "https://example.com",
                "path",
                LocalDateTime.now().plusSeconds(1)
        );

        try {
            Thread.sleep(1100); // 1.1초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(urlInfo.isExpired()).isTrue();
        assertThat(urlInfo.isValid()).isFalse();
    }
}
