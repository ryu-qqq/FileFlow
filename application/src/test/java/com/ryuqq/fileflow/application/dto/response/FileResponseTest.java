package com.ryuqq.fileflow.application.dto.response;

import com.ryuqq.fileflow.application.fixture.FileResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileResponse Record 테스트
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 */
class FileResponseTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FileResponse Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FileResponse는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Response 생성
        FileResponse response = FileResponseFixture.aResponse();

        // When & Then: Record 타입 검증
        assertThat(response).isNotNull();
        assertThat(response.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("FileResponse는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Response 생성
        Long expectedFileId = 100L;
        FileResponse response = FileResponseFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(response.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("FileResponse는 status 필드를 가져야 한다")
    void shouldHaveStatusField() {
        // Given: Fixture로 커스텀 status Response 생성
        String expectedStatus = "COMPLETED";
        FileResponse response = FileResponseFixture.withStatus(expectedStatus);

        // When & Then: status 필드 검증
        assertThat(response.status()).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("FileResponse는 s3Url 필드를 가져야 한다")
    void shouldHaveS3UrlField() {
        // Given: Fixture로 커스텀 s3Url Response 생성
        String expectedS3Url = "https://s3.amazonaws.com/bucket/uploads/file.jpg";
        FileResponse response = FileResponseFixture.withS3Url(expectedS3Url);

        // When & Then: s3Url 필드 검증
        assertThat(response.s3Url()).isEqualTo(expectedS3Url);
    }

    @Test
    @DisplayName("FileResponse는 cdnUrl 필드를 가져야 한다")
    void shouldHaveCdnUrlField() {
        // Given: Fixture로 커스텀 cdnUrl Response 생성
        String expectedCdnUrl = "https://cdn.example.com/uploads/file.jpg";
        FileResponse response = FileResponseFixture.withCdnUrl(expectedCdnUrl);

        // When & Then: cdnUrl 필드 검증
        assertThat(response.cdnUrl()).isEqualTo(expectedCdnUrl);
    }
}
