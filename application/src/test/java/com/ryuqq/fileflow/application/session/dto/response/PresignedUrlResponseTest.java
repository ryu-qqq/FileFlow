package com.ryuqq.fileflow.application.session.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PresignedUrlResponse DTO 테스트
 * <p>
 * Presigned URL 발급 응답 검증
 * </p>
 */
class PresignedUrlResponseTest {

    @Test
    @DisplayName("PresignedUrlResponse를 생성해야 한다")
    void shouldCreateResponse() {
        // when
        PresignedUrlResponse response = new PresignedUrlResponse(
            "01JD8000-1234-5678-9abc-def012345678",
            "01JD8001-1234-5678-9abc-def012345678",
            "https://example.com/presigned",
            300,
            "SINGLE"
        );

        // then
        assertThat(response.sessionId()).isNotEmpty();
        assertThat(response.sessionId()).isEqualTo("01JD8000-1234-5678-9abc-def012345678");
        assertThat(response.fileId()).isEqualTo("01JD8001-1234-5678-9abc-def012345678");
        assertThat(response.presignedUrl()).isEqualTo("https://example.com/presigned");
        assertThat(response.expiresIn()).isEqualTo(300);
        assertThat(response.uploadType()).isEqualTo("SINGLE");
    }
}
