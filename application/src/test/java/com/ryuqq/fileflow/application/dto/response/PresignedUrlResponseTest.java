package com.ryuqq.fileflow.application.dto.response;

import com.ryuqq.fileflow.application.fixture.PresignedUrlResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PresignedUrlResponse Record 테스트
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 */
class PresignedUrlResponseTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * PresignedUrlResponse Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("PresignedUrlResponse는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Response 생성
        PresignedUrlResponse response = PresignedUrlResponseFixture.aResponse();

        // When & Then: Record 타입 검증
        assertThat(response).isNotNull();
        assertThat(response.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("PresignedUrlResponse는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Response 생성
        Long expectedFileId = 100L;
        PresignedUrlResponse response = PresignedUrlResponseFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(response.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("PresignedUrlResponse는 presignedUrl 필드를 가져야 한다")
    void shouldHavePresignedUrlField() {
        // Given: Fixture로 커스텀 presignedUrl Response 생성
        String expectedUrl = "https://s3.amazonaws.com/bucket/key?signature=xyz";
        PresignedUrlResponse response = PresignedUrlResponseFixture.withPresignedUrl(expectedUrl);

        // When & Then: presignedUrl 필드 검증
        assertThat(response.presignedUrl()).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("PresignedUrlResponse는 expiresIn 필드를 가져야 한다")
    void shouldHaveExpiresInField() {
        // Given: Fixture로 커스텀 expiresIn Response 생성
        Long expectedExpiresIn = 3600L;
        PresignedUrlResponse response = PresignedUrlResponseFixture.withExpiresIn(expectedExpiresIn);

        // When & Then: expiresIn 필드 검증
        assertThat(response.expiresIn()).isEqualTo(expectedExpiresIn);
    }

    @Test
    @DisplayName("PresignedUrlResponse는 s3Key 필드를 가져야 한다")
    void shouldHaveS3KeyField() {
        // Given: Fixture로 커스텀 s3Key Response 생성
        String expectedS3Key = "uploads/2024/11/16/file-12345.jpg";
        PresignedUrlResponse response = PresignedUrlResponseFixture.withS3Key(expectedS3Key);

        // When & Then: s3Key 필드 검증
        assertThat(response.s3Key()).isEqualTo(expectedS3Key);
    }
}
