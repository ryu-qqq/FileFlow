package com.ryuqq.fileflow.application.session.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileResponse DTO 테스트
 * <p>
 * 파일 응답 DTO 검증
 * </p>
 */
class FileResponseTest {

    @Test
    @DisplayName("FileResponse를 생성해야 한다")
    void shouldCreateResponse() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        FileResponse response = new FileResponse(
            "01JD8000-1234-5678-9abc-def012345678",
            "01JD8001-1234-5678-9abc-def012345678",
            "example.jpg",
            1048576L,
            "image/jpeg",
            "COMPLETED",
            "uploads/1/admin/connectly/banner/01JD8001_example.jpg",
            "fileflow-uploads-1",
            now
        );

        // then
        assertThat(response.sessionId()).isEqualTo("01JD8000-1234-5678-9abc-def012345678");
        assertThat(response.fileId()).isNotEmpty();
        assertThat(response.fileName()).isEqualTo("example.jpg");
        assertThat(response.fileSize()).isEqualTo(1048576L);
        assertThat(response.mimeType()).isEqualTo("image/jpeg");
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.s3Key()).contains("uploads/1/admin");
        assertThat(response.s3Bucket()).isEqualTo("fileflow-uploads-1");
        assertThat(response.createdAt()).isEqualTo(now);
    }
}
