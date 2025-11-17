package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;
import com.ryuqq.fileflow.application.fixture.UploadFromExternalUrlCommandFixture;
import com.ryuqq.fileflow.application.port.in.command.UploadFromExternalUrlPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadFromExternalUrlService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 */
class UploadFromExternalUrlServiceTest {

    private UploadFromExternalUrlPort uploadFromExternalUrlPort;

    @BeforeEach
    void setUp() {
        uploadFromExternalUrlPort = new UploadFromExternalUrlService();
    }

    /**
     * 🔴 RED Phase: URL 검증 테스트
     * <p>
     * HTTP URL은 허용하지 않습니다. HTTPS만 허용합니다.
     * </p>
     */
    @Test
    @DisplayName("HTTP URL은 업로드할 수 없다 (HTTPS만 허용)")
    void shouldThrowExceptionWhenInvalidUrl() {
        // Given: HTTP URL (보안상 허용하지 않음)
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture
                .withExternalUrl("http://example.com/image.jpg");

        // When & Then: IllegalArgumentException 발생 (InvalidUrlException)
        assertThatThrownBy(() -> uploadFromExternalUrlPort.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }
}
