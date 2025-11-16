package com.ryuqq.fileflow.application.dto.response;

import com.ryuqq.fileflow.application.fixture.FileSummaryResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileSummaryResponse Record 테스트
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 */
class FileSummaryResponseTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FileSummaryResponse Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FileSummaryResponse는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Response 생성
        FileSummaryResponse response = FileSummaryResponseFixture.aResponse();

        // When & Then: Record 타입 검증
        assertThat(response).isNotNull();
        assertThat(response.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("FileSummaryResponse는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Response 생성
        Long expectedFileId = 100L;
        FileSummaryResponse response = FileSummaryResponseFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(response.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("FileSummaryResponse는 fileName 필드를 가져야 한다")
    void shouldHaveFileNameField() {
        // Given: Fixture로 커스텀 fileName Response 생성
        String expectedFileName = "important-document.pdf";
        FileSummaryResponse response = FileSummaryResponseFixture.withFileName(expectedFileName);

        // When & Then: fileName 필드 검증
        assertThat(response.fileName()).isEqualTo(expectedFileName);
    }

    @Test
    @DisplayName("FileSummaryResponse는 status 필드를 가져야 한다")
    void shouldHaveStatusField() {
        // Given: Fixture로 커스텀 status Response 생성
        String expectedStatus = "COMPLETED";
        FileSummaryResponse response = FileSummaryResponseFixture.withStatus(expectedStatus);

        // When & Then: status 필드 검증
        assertThat(response.status()).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("FileSummaryResponse는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: Fixture로 커스텀 uploaderId Response 생성
        Long expectedUploaderId = 999L;
        FileSummaryResponse response = FileSummaryResponseFixture.withUploaderId(expectedUploaderId);

        // When & Then: uploaderId 필드 검증
        assertThat(response.uploaderId()).isEqualTo(expectedUploaderId);
    }

    @Test
    @DisplayName("FileSummaryResponse는 createdAt 필드를 가져야 한다")
    void shouldHaveCreatedAtField() {
        // Given: Fixture로 커스텀 createdAt Response 생성
        LocalDateTime expectedCreatedAt = LocalDateTime.of(2024, 11, 16, 14, 30, 0);
        FileSummaryResponse response = FileSummaryResponseFixture.withCreatedAt(expectedCreatedAt);

        // When & Then: createdAt 필드 검증
        assertThat(response.createdAt()).isEqualTo(expectedCreatedAt);
    }
}
